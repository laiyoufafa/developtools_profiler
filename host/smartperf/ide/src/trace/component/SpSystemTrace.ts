/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {BaseElement, element} from "../../base-ui/BaseElement.js";
import "./trace/TimerShaftElement.js";
import "./trace/base/TraceRow.js";
import {queryEbpfSamplesCount, querySearchFunc, threadPool} from "../database/SqlLite.js";
import {RangeSelectStruct, TraceRow} from "./trace/base/TraceRow.js";
import {TimerShaftElement} from "./trace/TimerShaftElement.js";
import "./trace/base/TraceSheet.js";
import {TraceSheet} from "./trace/base/TraceSheet.js";
import {RangeSelect} from "./trace/base/RangeSelect.js";
import {SelectionParam} from "../bean/BoxSelection.js";
import {procedurePool} from "../database/Procedure.js";
import {SpApplication} from "../SpApplication.js";
import {SPT} from "../bean/StateProcessThread.js";
import {Flag} from "./trace/timer-shaft/Flag.js";
import {SportRuler} from "./trace/timer-shaft/SportRuler.js";
import {SpHiPerf} from "./chart/SpHiPerf.js";
import {SearchSdkBean, SearchThreadProcessBean} from "../bean/SearchFuncBean.js";
import {error, info} from "../../log/Log.js";
import {
    drawFlagLineSegment,
    drawLines,
    drawLinkLines,
    drawWakeUp,
    isFrameContainPoint,
    ns2x,
    ns2xByTimeShaft,
    PairPoint,
    Rect
} from "../database/ui-worker/ProcedureWorkerCommon.js";
import {SpChartManager} from "./chart/SpChartManager.js";
import {CpuStruct} from "../database/ui-worker/ProcedureWorkerCPU.js";
import {ProcessStruct} from "../database/ui-worker/ProcedureWorkerProcess.js";
import {CpuFreqStruct} from "../database/ui-worker/ProcedureWorkerFreq.js";
import {CpuFreqLimitsStruct} from "../database/ui-worker/ProcedureWorkerCpuFreqLimits.js";
import {ThreadStruct} from "../database/ui-worker/ProcedureWorkerThread.js";
import {func, FuncStruct} from "../database/ui-worker/ProcedureWorkerFunc.js";
import {CpuStateStruct} from "../database/ui-worker/ProcedureWorkerCpuState.js";
import {HiPerfCpuStruct} from "../database/ui-worker/ProcedureWorkerHiPerfCPU.js";
import {HiPerfProcessStruct} from "../database/ui-worker/ProcedureWorkerHiPerfProcess.js";
import {HiPerfThreadStruct} from "../database/ui-worker/ProcedureWorkerHiPerfThread.js";
import {HiPerfEventStruct} from "../database/ui-worker/ProcedureWorkerHiPerfEvent.js";
import {HiPerfReportStruct} from "../database/ui-worker/ProcedureWorkerHiPerfReport.js";
import {FpsStruct} from "../database/ui-worker/ProcedureWorkerFPS.js";
import {CpuAbilityMonitorStruct} from "../database/ui-worker/ProcedureWorkerCpuAbility.js";
import {DiskAbilityMonitorStruct} from "../database/ui-worker/ProcedureWorkerDiskIoAbility.js";
import {MemoryAbilityMonitorStruct} from "../database/ui-worker/ProcedureWorkerMemoryAbility.js";
import {NetworkAbilityMonitorStruct} from "../database/ui-worker/ProcedureWorkerNetworkAbility.js";
import {ClockStruct} from "../database/ui-worker/ProcedureWorkerClock.js";
import {Utils} from "./trace/base/Utils.js";
import {IrqStruct} from "../database/ui-worker/ProcedureWorkerIrq.js";
import {JanksStruct} from "../bean/JanksStruct.js";
import {JankStruct} from "../database/ui-worker/ProcedureWorkerJank.js";

function dpr(){
    return window.devicePixelRatio||1;
}

@element('sp-system-trace')
export class SpSystemTrace extends BaseElement {
    static scrollViewWidth = 0
    static isCanvasOffScreen = true;
    static SPT_DATA: Array<SPT> = [];
    static DATA_DICT: Map<number, string> = new Map<number, string>();
    static SDK_CONFIG_MAP: any;
    static sliceRangeMark: any;
    intersectionObserver: IntersectionObserver | undefined;
    tipEL: HTMLDivElement | undefined | null;
    rowsEL: HTMLDivElement | undefined | null;
    rowsPaneEL: HTMLDivElement | undefined | null;
    spacerEL: HTMLDivElement | undefined | null;
    favoriteRowsEL: HTMLDivElement | undefined | null;
    visibleRows: Array<TraceRow<any>> = [];
    collectRows: Array<TraceRow<any>> = [];
    keyboardEnable = true;
    currentRowType = "";/*保存当前鼠标所在行的类型*/
    observerScrollHeightEnable: boolean = false;
    observerScrollHeightCallback: Function | undefined;
    // @ts-ignore
    observer = new ResizeObserver((entries) => {
        if (this.observerScrollHeightEnable && this.observerScrollHeightCallback) {
            this.observerScrollHeightCallback();
        }
    });
    isMousePointInSheet = false;
    hoverFlag: Flag | undefined | null = undefined
    selectFlag: Flag | undefined | null = undefined
    public timerShaftEL: TimerShaftElement | null | undefined;
    private traceSheetEL: TraceSheet | undefined | null;
    private rangeSelect!: RangeSelect;
    private chartManager: SpChartManager | undefined | null;
    private loadTraceCompleted: boolean = false;
    canvasFavoritePanel: HTMLCanvasElement | null | undefined;//绘制收藏泳道图
    canvasFavoritePanelCtx: CanvasRenderingContext2D | null | undefined;
    canvasPanel: HTMLCanvasElement | null | undefined; //绘制取消收藏后泳道图
    canvasPanelCtx: CanvasRenderingContext2D | undefined | null;
    linkNodes: PairPoint[][] = [];
    public currentClickRow: HTMLDivElement | undefined | null;

    addPointPair(a: PairPoint, b: PairPoint) {
        if (a.rowEL.collect) {
            a.rowEL.translateY = a.rowEL.getBoundingClientRect().top - 195;
        } else {
            a.rowEL.translateY = a.rowEL.offsetTop - this.rowsPaneEL!.scrollTop;
        }
        if (b.rowEL.collect) {
            b.rowEL.translateY = b.rowEL.getBoundingClientRect().top - 195;
        } else {
            b.rowEL.translateY = b.rowEL.offsetTop - this.rowsPaneEL!.scrollTop;
        }
        a.y = a.rowEL!.translateY! + a.offsetY
        b.y = b.rowEL!.translateY! + b.offsetY
        this.linkNodes.push([a, b])
    }

    clearPointPair() {
        this.linkNodes.length = 0
    }

    initElements(): void {
        this.rowsEL = this.shadowRoot?.querySelector<HTMLDivElement>('.rows');
        this.tipEL = this.shadowRoot?.querySelector<HTMLDivElement>('.tip');
        this.rowsPaneEL = this.shadowRoot?.querySelector<HTMLDivElement>('.rows-pane');
        this.spacerEL = this.shadowRoot?.querySelector<HTMLDivElement>('.spacer');
        this.canvasFavoritePanel = this.shadowRoot?.querySelector<HTMLCanvasElement>('.panel-canvas-favorite');
        this.timerShaftEL = this.shadowRoot?.querySelector('.timer-shaft');
        this.traceSheetEL = this.shadowRoot?.querySelector('.trace-sheet');
        this.favoriteRowsEL = this.shadowRoot?.querySelector('.favorite-rows');
        this.rangeSelect = new RangeSelect(this);
        document?.addEventListener("triangle-flag", (event: any) => {
            let temporaryTime = this.timerShaftEL?.drawTriangle(event.detail.time, event.detail.type);
            if (event.detail.timeCallback && temporaryTime) event.detail.timeCallback(temporaryTime);
        })

        document?.addEventListener("flag-change", (event: any) => {
            this.timerShaftEL?.modifyFlagList(event.detail)
            if (event.detail.hidden) {
                this.selectFlag = undefined;
                this.traceSheetEL?.setAttribute("mode", 'hidden');
                this.refreshCanvas(true);
            }
        })
        if (this.timerShaftEL?.collecBtn) {
            this.timerShaftEL.collecBtn.onclick = () => {
                if (this.timerShaftEL!.collecBtn!.hasAttribute('close')) {
                    this.timerShaftEL!.collecBtn!.removeAttribute('close');
                } else {
                    this.timerShaftEL!.collecBtn!.setAttribute('close', '');
                }
                if (this.collectRows.length > 0) {
                    this.collectRows.forEach((row) => {
                        row?.collectEL?.onclick?.(new MouseEvent("auto-collect", undefined))
                    })
                }
            }
        }
        document?.addEventListener("collect", (event: any) => {
            let currentRow = event.detail.row;
            if (currentRow.collect) {
                if (!this.collectRows.find((find) => {
                    return find === currentRow
                })) {
                    this.collectRows.push(currentRow)
                }
                if (event.detail.type !== "auto-collect" && this.timerShaftEL!.collecBtn!.hasAttribute('close')) {
                    currentRow.collect = false;
                    this.timerShaftEL!.collecBtn!.click()
                    return
                }
                let replaceRow = document.createElement("div")
                replaceRow.setAttribute("row-id", currentRow.rowId + "-" + currentRow.rowType);
                replaceRow.setAttribute("type", "replaceRow")
                replaceRow.style.display = 'none';
                this.rowsEL!.replaceChild(replaceRow, currentRow);
                this.favoriteRowsEL!.append(currentRow)
            } else {
                this.favoriteRowsEL!.removeChild(currentRow);
                if (event.detail.type !== "auto-collect") {
                    let rowIndex = this.collectRows.indexOf(currentRow);
                    if (rowIndex !== -1) {
                        this.collectRows.splice(rowIndex, 1)
                    }
                }
                let replaceRow = this.rowsEL!.querySelector<HTMLCanvasElement>(`div[row-id='${currentRow.rowId}-${currentRow.rowType}']`)
                if (replaceRow != null) {
                    this.expansionAllParentRow(currentRow)
                    this.rowsEL!.replaceChild(currentRow, replaceRow)
                    currentRow.style.boxShadow = `0 10px 10px #00000000`;
                }
                this.canvasFavoritePanel!.style.transform = `translateY(${this.favoriteRowsEL!.scrollTop - currentRow.clientHeight}px)`;
            }
            this.timerShaftEL?.displayCollect(this.collectRows.length !== 0)
            this.refreshFavoriteCanvas()
            this.refreshCanvas(true);
            // 收藏夹元素拖动排序功能
            this.currentClickRow = null;
            currentRow.setAttribute("draggable", "true");
            currentRow.addEventListener("dragstart", () => {
                this.currentClickRow = currentRow;
            });
            currentRow.addEventListener("dragover", (ev: any) => {
                ev.preventDefault();
                ev.dataTransfer.dropEffect = "move";
            });
            currentRow.addEventListener("drop", (ev: any) => {
                if (this.favoriteRowsEL != null && this.currentClickRow != null && this.currentClickRow !== currentRow) {
                    let rect = currentRow.getBoundingClientRect();
                    if (ev.clientY >= rect.top && ev.clientY < rect.top + rect.height / 2) { //向上移动
                        this.favoriteRowsEL.insertBefore(this.currentClickRow, currentRow);
                    } else if (ev.clientY <= rect.bottom && ev.clientY > rect.top + rect.height / 2) { //向下移动
                        this.favoriteRowsEL.insertBefore(this.currentClickRow, currentRow.nextSibling);
                    }
                    this.refreshFavoriteCanvas();
                }
            });
            currentRow.addEventListener("dragend", () => {
                this.currentClickRow = null;
            });
        })
        SpSystemTrace.scrollViewWidth = this.getScrollWidth();
        this.rangeSelect.selectHandler = (rows, refreshCheckBox) => {
            if (rows.length == 0) {
                this.shadowRoot!.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => {
                    it.checkType = "-1"
                })
                this.refreshCanvas(true);
                this.traceSheetEL?.setAttribute("mode", 'hidden');
                return;
            }
            if (refreshCheckBox) {
                if (rows.length > 0) {
                    this.shadowRoot?.querySelectorAll<TraceRow<any>>("trace-row").forEach(row => row.checkType = "0")
                    rows.forEach(it => it.checkType = "2")
                } else {
                    this.shadowRoot?.querySelectorAll<TraceRow<any>>("trace-row").forEach(row => row.checkType = "-1")
                    return
                }
            }
            let selection = new SelectionParam();
            selection.leftNs = 0;
            selection.rightNs = 0;
            selection.recordStartNs = (window as any).recordStartNS
            let native_memory = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"];
            rows.forEach(it => {
                if (it.rowType == TraceRow.ROW_TYPE_CPU) {
                    selection.cpus.push(parseInt(it.rowId!))
                    info("load CPU traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_CPU_STATE) {
                    let filterId = parseInt(it.rowId!);
                    if (selection.cpuStateFilterIds.indexOf(filterId) == -1) {
                        selection.cpuStateFilterIds.push(filterId);
                    }
                } else if (it.rowType == TraceRow.ROW_TYPE_CPU_FREQ) {
                    let filterId = parseInt(it.rowId!);
                    if (selection.cpuFreqFilterIds.indexOf(filterId) == -1) {
                        selection.cpuFreqFilterIds.push(filterId);
                    }
                } else if (it.rowType == TraceRow.ROW_TYPE_CPU_FREQ_LIMIT) {
                    selection.cpuFreqLimitDatas.push(it.dataList!)
                } else if (it.rowType == TraceRow.ROW_TYPE_PROCESS) {
                    this.shadowRoot?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
                        th.rangeSelect = true;
                        th.checkType = "2"
                        if (th.rowType == TraceRow.ROW_TYPE_THREAD) {
                            selection.threadIds.push(parseInt(th.rowId!))
                        } else if (th.rowType == TraceRow.ROW_TYPE_FUNC) {
                            if (th.asyncFuncName) {
                                selection.funAsync.push({
                                    name: th.asyncFuncName,
                                    pid: th.asyncFuncNamePID || 0,
                                })
                            } else {
                                selection.funTids.push(parseInt(th.rowId!))
                            }
                        } else if (th.rowType == TraceRow.ROW_TYPE_MEM) {
                            selection.processTrackIds.push(parseInt(th.rowId!))
                        }
                    })
                    info("load process traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_NATIVE_MEMORY) {
                    this.shadowRoot?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
                        th.rangeSelect = true;
                        th.checkType = "2"
                        if(th.getAttribute("heap-type") === "native_hook_statistic"){
                            selection.nativeMemoryStatistic.push(it.rowId!);
                        }else{
                            selection.nativeMemory.push(it.rowId!);
                        }
                    })
                    info("load nativeMemory traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_THREAD) {
                    selection.threadIds.push(parseInt(it.rowId!))
                    info("load thread traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_FUNC) {
                    if (it.asyncFuncName) {
                        selection.funAsync.push({
                            name: it.asyncFuncName,
                            pid: it.asyncFuncNamePID || 0,
                        })
                    } else {
                        selection.funTids.push(parseInt(it.rowId!))
                    }
                    info("load func traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_MEM || it.rowType == TraceRow.ROW_TYPE_VIRTUAL_MEMORY) {
                    if (it.rowType == TraceRow.ROW_TYPE_MEM) {
                        selection.processTrackIds.push(parseInt(it.rowId!))
                    } else {
                        selection.virtualTrackIds.push(parseInt(it.rowId!))
                    }
                    info("load memory traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_FPS) {
                    selection.hasFps = true;
                    info("load FPS traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_HEAP) {
                    if(it.getAttribute("heap-type") === "native_hook_statistic"){
                        selection.nativeMemoryStatistic.push(it.rowId!);
                    }else{
                        selection.nativeMemory.push(it.rowId!);
                    }
                    info("load nativeMemory traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_CPU_ABILITY) {
                    selection.cpuAbilityIds.push(it.rowId!)
                    info("load CPU Ability traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_MEMORY_ABILITY) {
                    selection.memoryAbilityIds.push(it.rowId!)
                    info("load Memory Ability traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_DISK_ABILITY) {
                    selection.diskAbilityIds.push(it.rowId!)
                    info("load DiskIo Ability traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_NETWORK_ABILITY) {
                    selection.networkAbilityIds.push(it.rowId!)
                    info("load Network Ability traceRow id is : ", it.rowId)
                } else if (it.rowType?.startsWith(TraceRow.ROW_TYPE_SDK)) {
                    if (it.rowType == TraceRow.ROW_TYPE_SDK) {
                        this.shadowRoot?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
                            th.rangeSelect = true;
                            th.checkType = "2"
                        })
                    }
                    if (it.rowType == TraceRow.ROW_TYPE_SDK_COUNTER) {
                        selection.sdkCounterIds.push(it.rowId!)
                    }
                    if (it.rowType == TraceRow.ROW_TYPE_SDK_SLICE) {
                        selection.sdkSliceIds.push(it.rowId!)
                    }
                } else if (it.rowType?.startsWith("hiperf")) {
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_EVENT || it.rowType == TraceRow.ROW_TYPE_HIPERF_REPORT) {
                        return;
                    }
                    selection.perfSampleIds.push(1)
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_PROCESS) {
                        this.shadowRoot?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
                            th.rangeSelect = true;
                            th.checkType = "2"
                        })
                    }
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF || it.rowId == "HiPerf-cpu-merge") {
                        selection.perfAll = true;
                    }
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_CPU) {
                        selection.perfCpus.push(it.index);
                    }
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_PROCESS) {
                        selection.perfProcess.push(parseInt(it.rowId!.split("-")[0]));
                    }
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_THREAD) {
                        selection.perfThread.push(parseInt(it.rowId!.split("-")[0]));
                    }
                } else if (it.rowType == TraceRow.ROW_TYPE_FILE_SYSTEM) {
                    if (it.rowId == "FileSystemLogicalWrite") {
                        if (selection.fileSystemType.length == 0) {
                            selection.fileSystemType = [0, 1, 3];
                        } else {
                            if (selection.fileSystemType.indexOf(3) == -1) {
                                selection.fileSystemType.push(3)
                            }
                        }
                    } else if (it.rowId == "FileSystemLogicalRead") {
                        if (selection.fileSystemType.length == 0) {
                            selection.fileSystemType = [0, 1, 2];
                        } else {
                            if (selection.fileSystemType.indexOf(2) == -1) {
                                selection.fileSystemType.push(2)
                            }
                        }
                    } else if (it.rowId == "FileSystemVirtualMemory") {
                        selection.fileSysVirtualMemory = true;
                    } else if (it.rowId == "FileSystemDiskIOLatency") {
                        selection.diskIOLatency = true;
                    } else {
                        if (!selection.diskIOLatency) {
                            let arr = it.rowId!.split("-").reverse();
                            let ipid = parseInt(arr[0]);
                            if (selection.diskIOipids.indexOf(ipid) == -1) {
                                selection.diskIOipids.push(ipid);
                            }
                            if (arr[1] == 'read') {
                                selection.diskIOReadIds.indexOf(ipid) == -1 ? selection.diskIOReadIds.push(ipid) : "";
                            } else if (arr[1] == 'write') {
                                selection.diskIOWriteIds.indexOf(ipid) == -1 ? selection.diskIOWriteIds.push(ipid) : "";
                            }
                        }
                    }
                } else if (it.rowType == TraceRow.ROW_TYPE_POWER_ENERGY) {
                    selection.powerEnergy.push(it.rowId!)
                } else if (it.rowType == TraceRow.ROW_TYPE_SYSTEM_ENERGY) {
                    selection.systemEnergy.push(it.rowId!)
                } else if (it.rowType == TraceRow.ROW_TYPE_ANOMALY_ENERGY) {
                    selection.anomalyEnergy.push(it.rowId!)
                } else if (it.rowType == TraceRow.ROW_TYPE_SYSTEM_ENERGY) {
                    info("load anomaly Energy traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_SMAPS) {
                    selection.smapsType.push(it.rowId!)
                } else if (it.rowType == TraceRow.ROW_TYPE_CLOCK) {
                    selection.clockMapData.set(it.rowId || "", it.dataList.filter((clockData) => {
                        return Utils.getTimeIsCross(clockData.startNS, clockData.startNS + clockData.dur, (TraceRow.rangeSelectObject?.startNS || 0), (TraceRow.rangeSelectObject?.endNS || 0))
                    }))
                } else if (it.rowType == TraceRow.ROW_TYPE_IRQ) {
                    it.dataList.forEach((irqData) => {
                        if (Utils.getTimeIsCross(irqData.startNS, irqData.startNS + irqData.dur, (TraceRow.rangeSelectObject?.startNS || 0), (TraceRow.rangeSelectObject?.endNS || 0))) {
                            if (selection.irqMapData.has(irqData.name)) {
                                selection.irqMapData.get(irqData.name)?.push(irqData)
                            } else {
                                selection.irqMapData.set(irqData.name, [irqData])
                            }
                        }
                    })
                } else if (it.rowType == TraceRow.ROW_TYPE_JANK && it.name == "Actual Timeline") {
                    let isIntersect = (a: JanksStruct, b: RangeSelectStruct) => (Math.max(a.ts! + a.dur!, b!.endNS || 0) - Math.min(a.ts!, b!.startNS || 0) < a.dur! + (b!.endNS || 0) - (b!.startNS || 0));
                    let jankDatas = it.dataList.filter((jankData: any) => {
                        return isIntersect(jankData, TraceRow.rangeSelectObject!)
                    })
                    selection.jankFramesData.push(jankDatas)
                }
            })
            if (selection.diskIOipids.length > 0 && !selection.diskIOLatency) {
                selection.promiseList.push(queryEbpfSamplesCount(TraceRow.rangeSelectObject?.startNS || 0, TraceRow.rangeSelectObject?.endNS || 0, selection.diskIOipids).then((res) => {
                    if (res.length > 0) {
                        selection.fsCount = res[0].fsCount;
                        selection.vmCount = res[0].vmCount;
                    }
                    return new Promise(resolve => resolve(1))
                }))
            }
            selection.leftNs = TraceRow.rangeSelectObject?.startNS || 0;
            selection.rightNs = TraceRow.rangeSelectObject?.endNS || 0;
            this.selectStructNull();
            this.timerShaftEL?.removeTriangle("inverted")
            if (selection.promiseList.length > 0) {
                Promise.all(selection.promiseList).then(() => {
                    selection.promiseList = [];
                    this.traceSheetEL?.rangeSelect(selection);
                })
            } else {
                this.traceSheetEL?.rangeSelect(selection);
            }
        }
        // @ts-ignore
        new ResizeObserver((entries) => {
            let width = entries[0].contentRect.width - 1 - SpSystemTrace.scrollViewWidth;
            requestAnimationFrame(() => {
                this.timerShaftEL?.updateWidth(width)
                this.shadowRoot!.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => {
                    it.updateWidth(width)
                })
            })
        }).observe(this);

        new ResizeObserver((entries) => {
            this.canvasPanelConfig();
            if (this.traceSheetEL!.getAttribute("mode") == "hidden") {
                this.timerShaftEL?.removeTriangle("triangle")
            }
            this.refreshFavoriteCanvas()
            this.refreshCanvas(true)

        }).observe(this.rowsPaneEL!);
        new MutationObserver((mutations, observer) => {
            for (const mutation of mutations) {
                if (mutation.type === "attributes") {
                    if (this.style.visibility === "visible") {
                        if (TraceRow.rangeSelectObject && SpSystemTrace.sliceRangeMark) {
                            this.timerShaftEL?.setSlicesMark((TraceRow.rangeSelectObject.startNS || 0), (TraceRow.rangeSelectObject.endNS || 0));
                            SpSystemTrace.sliceRangeMark = undefined;
                            window.publish(window.SmartEvent.UI.RefreshCanvas, {});
                        }
                    }
                }
            }
        }).observe(this, {attributes: true, childList: false, subtree: false});

        this.intersectionObserver = new IntersectionObserver((entries) => {
            entries.forEach(it => {
                let tr = it.target as TraceRow<any>;
                if (!it.isIntersecting) {
                    tr.sleeping = true;
                    this.visibleRows = this.visibleRows.filter(it => !it.sleeping)
                } else {
                    this.visibleRows.push(tr);
                    tr.sleeping = false;
                }
                if (this.handler) clearTimeout(this.handler);
                this.handler = setTimeout(() => this.refreshCanvas(false), 100);
            })
        });
        window.addEventListener("keydown", ev => {
            if (ev.key.toLocaleLowerCase() === "escape") {
                this.shadowRoot?.querySelectorAll<TraceRow<any>>("trace-row").forEach((it) => {
                    it.checkType = "-1"
                })
                TraceRow.rangeSelectObject = undefined;
                this.rangeSelect.rangeTraceRow = []
                this.selectStructNull();
                this.timerShaftEL?.setSlicesMark();
                this.traceSheetEL?.setAttribute("mode", 'hidden');
            }
        });
        this.chartManager = new SpChartManager(this);
        this.canvasPanel = this.shadowRoot!.querySelector<HTMLCanvasElement>("#canvas-panel")!;
        this.canvasFavoritePanel = this.shadowRoot!.querySelector<HTMLCanvasElement>("#canvas-panel-favorite")!;
        this.canvasPanelCtx = this.canvasPanel.getContext('2d');
        this.canvasFavoritePanelCtx = this.canvasFavoritePanel.getContext('2d');
        this.canvasPanelConfig();
        window.subscribe(window.SmartEvent.UI.SliceMark, this.sliceMarkEventHandler)
        window.subscribe(window.SmartEvent.UI.TraceRowComplete, (tr) => {
        })
        window.subscribe(window.SmartEvent.UI.RefreshCanvas, () => {
            this.refreshCanvas(false)
        });
    }

    refreshFavoriteCanvas() {
        let collectList = this.favoriteRowsEL?.querySelectorAll<TraceRow<any>>(`trace-row[collect-type]`) || [];
        let height = 0;
        collectList.forEach((row, index) => {
            height += row.offsetHeight
            if (index == collectList.length - 1) {
                row.style.boxShadow = `0 10px 10px #00000044`;
            } else {
                row.style.boxShadow = `0 10px 10px #00000000`;
            }
        })
        if (height > this.rowsPaneEL!.offsetHeight) {
            this.favoriteRowsEL!.style.height = this.rowsPaneEL!.offsetHeight + "px"
        } else {
            this.favoriteRowsEL!.style.height = height + "px"
        }
        this.favoriteRowsEL!.style.width = this.canvasPanel?.offsetWidth + 'px'
        this.spacerEL!.style.height = height + "px"
        this.canvasFavoritePanel!.style.height = this.favoriteRowsEL!.style.height;
        this.canvasFavoritePanel!.style.width = this.canvasPanel?.offsetWidth + 'px'
        this.canvasFavoritePanel!.width = this.canvasFavoritePanel!.offsetWidth * dpr();
        this.canvasFavoritePanel!.height = this.canvasFavoritePanel!.offsetHeight * dpr();
        this.canvasFavoritePanel!.getContext('2d')!.scale(dpr(), dpr());
    }

    expansionAllParentRow(currentRow: TraceRow<any>) {
        let parentRow = this.rowsEL!.querySelector<TraceRow<any>>(`trace-row[row-id='${currentRow.rowParentId}'][folder]`)
        if (parentRow) {
            parentRow.expansion = true
            if (this.rowsEL!.querySelector<TraceRow<any>>(`trace-row[row-id='${parentRow.rowParentId}'][folder]`)) {
                this.expansionAllParentRow(parentRow)
            }
        }

    }

    canvasPanelConfig() {
        this.canvasPanel!.style.left = `${this.timerShaftEL!.canvas!.offsetLeft!}px`
        this.canvasPanel!.width = this.canvasPanel!.offsetWidth * dpr();
        this.canvasPanel!.height = this.canvasPanel!.offsetHeight * dpr();
        this.canvasPanelCtx!.scale(dpr(), dpr());
        this.canvasFavoritePanel!.style.left = `${this.timerShaftEL!.canvas!.offsetLeft!}px`;
        this.canvasFavoritePanel!.width = this.canvasFavoritePanel!.offsetWidth * dpr();
        this.canvasFavoritePanel!.height = this.canvasFavoritePanel!.offsetHeight * dpr();
        this.canvasFavoritePanelCtx!.scale(dpr(), dpr());
    }

    getScrollWidth() {
        let totalScrollDiv, scrollDiv, overflowDiv = document.createElement('div');
        overflowDiv.style.cssText = 'position:absolute; top:-2000px;width:200px; height:200px; overflow:hidden;';
        totalScrollDiv = document.body.appendChild(overflowDiv).clientWidth;
        overflowDiv.style.overflowY = 'scroll';
        scrollDiv = overflowDiv.clientWidth;
        document.body.removeChild(overflowDiv);
        return totalScrollDiv - scrollDiv;
    }

    timerShaftELFlagClickHandler = (flag: Flag | undefined | null) => {
        if (flag) {
            setTimeout(() => {
                this.traceSheetEL?.displayFlagData(flag);
            }, 100)
        }
    }

    timerShaftELFlagChange = (hoverFlag: Flag | undefined | null, selectFlag: Flag | undefined | null) => {
        this.hoverFlag = hoverFlag;
        this.selectFlag = selectFlag;
        this.refreshCanvas(true);
    }

    timerShaftELRangeChange = (e: any) => {
        TraceRow.range = e;
        if (TraceRow.rangeSelectObject) {
            TraceRow.rangeSelectObject!.startX = Math.floor(ns2x(TraceRow.rangeSelectObject!.startNS!, TraceRow.range?.startNS!, TraceRow.range?.endNS!, TraceRow.range?.totalNS!, this.timerShaftEL!.sportRuler!.frame));
            TraceRow.rangeSelectObject!.endX = Math.floor(ns2x(TraceRow.rangeSelectObject!.endNS!, TraceRow.range?.startNS!, TraceRow.range?.endNS!, TraceRow.range?.totalNS!, this.timerShaftEL!.sportRuler!.frame));
        }
        //在rowsEL显示范围内的 trace-row组件将收到时间区间变化通知
        this.linkNodes.forEach(it => {
            it[0].x = ns2xByTimeShaft(it[0].ns, this.timerShaftEL!)
            it[1].x = ns2xByTimeShaft(it[1].ns, this.timerShaftEL!)
        })
        this.refreshCanvas(false);
    }
    tim: number = -1;
    top: number = 0;
    handler:any=undefined;
    rowsElOnScroll = (e: any) => {
        this.linkNodes.forEach(itln => {
            if (itln[0].rowEL.collect) {
                itln[0].rowEL.translateY = itln[0].rowEL.getBoundingClientRect().top - 195;
            } else {
                itln[0].rowEL.translateY = itln[0].rowEL.offsetTop - this.rowsPaneEL!.scrollTop;
            }
            if (itln[1].rowEL.collect) {
                itln[1].rowEL.translateY = itln[1].rowEL.getBoundingClientRect().top - 195;
            } else {
                itln[1].rowEL.translateY = itln[1].rowEL.offsetTop - this.rowsPaneEL!.scrollTop;
            }
            itln[0].y = itln[0].rowEL.translateY + itln[0].offsetY;
            itln[1].y = itln[1].rowEL.translateY + itln[1].offsetY;
        })
        requestAnimationFrame(() => this.refreshCanvas(false))
    }

    favoriteRowsElOnScroll = (e: any) => {
        this.rowsElOnScroll(e)
    }

    offset = 147;

    getRowsContentHeight(): number {
        return [...this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row:not([sleeping])`)]
            .map(it => it.clientHeight)
            .reduce((acr, cur) => acr + cur, 0);
    }

    // refresh main canvas and favorite canvas
    refreshCanvas(cache: boolean) {
        if (this.visibleRows.length == 0) {
            return;
        }
        //clear main canvas
        this.canvasPanelCtx?.clearRect(0, 0, this.canvasPanel!.offsetWidth, this.canvasPanel!.offsetHeight);
        //clear favorite canvas
        this.canvasFavoritePanelCtx?.clearRect(0, 0, this.canvasFavoritePanel!.offsetWidth, this.canvasFavoritePanel!.offsetHeight);
        //draw lines for main canvas
        let rowsContentHeight = this.getRowsContentHeight();
        let canvasHeight = rowsContentHeight > this.canvasPanel!.clientHeight ? this.canvasPanel!.clientHeight : rowsContentHeight;
        canvasHeight += this.canvasFavoritePanel!.clientHeight;
        drawLines(this.canvasPanelCtx!, TraceRow.range?.xs || [], canvasHeight, this.timerShaftEL!.lineColor());
        //draw lines for favorite canvas
        drawLines(this.canvasFavoritePanelCtx!, TraceRow.range?.xs || [], this.canvasFavoritePanel!.clientHeight, this.timerShaftEL!.lineColor());
        //canvas translate
        this.canvasPanel!.style.transform = `translateY(${this.rowsPaneEL!.scrollTop}px)`;
        this.canvasFavoritePanel!.style.transform = `translateY(${this.favoriteRowsEL!.scrollTop}px)`;
        //draw trace row
        this.visibleRows.forEach((v, i) => {
            if (v.collect) {
                v.translateY = v.getBoundingClientRect().top - 195;
            } else {
                v.translateY = v.offsetTop - this.rowsPaneEL!.scrollTop;
            }
            v.draw(cache)
        })
        //draw flag line segment for canvas
        drawFlagLineSegment(this.canvasPanelCtx, this.hoverFlag, this.selectFlag, {
            x: 0, y: 0, width: this.timerShaftEL?.canvas?.clientWidth, height: this.canvasPanel?.clientHeight
        });
        //draw flag line segment for favorite canvas
        drawFlagLineSegment(this.canvasFavoritePanelCtx, this.hoverFlag, this.selectFlag, {
            x: 0, y: 0, width: this.timerShaftEL?.canvas?.clientWidth, height: this.canvasFavoritePanel?.clientHeight
        });
        //draw wakeup for main canvas
        drawWakeUp(this.canvasPanelCtx, CpuStruct.wakeupBean, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS, {
            x: 0, y: 0, width: this.timerShaftEL!.canvas!.clientWidth, height: this.canvasPanel!.clientHeight!
        } as Rect);
        //draw wakeup for favorite canvas
        drawWakeUp(this.canvasFavoritePanelCtx, CpuStruct.wakeupBean, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS, {
            x: 0, y: 0, width: this.timerShaftEL!.canvas!.clientWidth, height: this.canvasFavoritePanel!.clientHeight!
        } as Rect);
        // Draw the connection curve
        if (this.linkNodes) {
            drawLinkLines(this.canvasPanelCtx!, this.linkNodes, this.timerShaftEL!)
        }
    }

    documentOnMouseDown = (ev: MouseEvent) => {
        if (!this.loadTraceCompleted) return;
        TraceRow.isUserInteraction = true;
        if (this.isMouseInSheet(ev)) return;
        this.observerScrollHeightEnable = false;
        if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft) {
            let x = ev.offsetX - this.timerShaftEL!.canvas!.offsetLeft;
            let y = ev.offsetY;
            this.timerShaftEL?.documentOnMouseDown(ev);
            if (this.timerShaftEL!.sportRuler!.frame.contains(x, y) && x > (TraceRow.rangeSelectObject?.startX || 0) && x < (TraceRow.rangeSelectObject?.endX || 0)) {
                let time = Math.round((x * (TraceRow.range?.endNS! - TraceRow.range?.startNS!) / this.timerShaftEL!.canvas!.offsetWidth) + TraceRow.range?.startNS!);
                this.timerShaftEL!.sportRuler!.drawTriangle(time, "triangle")
            } else {
                this.rangeSelect.mouseDown(ev)
                this.rangeSelect.drag = true;
            }
        } else {
            this.rangeSelect.drag = false;
        }
    }

    documentOnMouseUp = (ev: MouseEvent) => {
        if (!this.loadTraceCompleted) return;
        TraceRow.isUserInteraction = false;
        this.rangeSelect.isMouseDown = false;
        if (this.isMouseInSheet(ev)) return;
        let x = ev.offsetX - this.timerShaftEL!.canvas!.offsetLeft;
        let y = ev.offsetY;
        if (this.timerShaftEL!.sportRuler!.frame.contains(x, y) && x > (TraceRow.rangeSelectObject?.startX || 0) && x < (TraceRow.rangeSelectObject?.endX || 0)) {
        } else {
            this.rangeSelect.mouseUp(ev);
            this.timerShaftEL?.documentOnMouseUp(ev)
        }
    }

    documentOnMouseOut = (ev: MouseEvent) => {
        if (!this.loadTraceCompleted) return;
        TraceRow.isUserInteraction = false;
        if (this.isMouseInSheet(ev)) return;
        if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft) {
            this.rangeSelect.mouseOut(ev)
            this.timerShaftEL?.documentOnMouseOut(ev)
        }
    }

    documentOnKeyPress = (ev: KeyboardEvent) => {
        if (!this.loadTraceCompleted) return;
        TraceRow.isUserInteraction = true;
        if (ev.key.toLocaleLowerCase() == "m") {
            if (CpuStruct.selectCpuStruct) {
                this.timerShaftEL?.setSlicesMark((CpuStruct.selectCpuStruct.startTime || 0), (CpuStruct.selectCpuStruct.startTime || 0) + (CpuStruct.selectCpuStruct.dur || 0));
            } else if (ThreadStruct.selectThreadStruct) {
                this.timerShaftEL?.setSlicesMark((ThreadStruct.selectThreadStruct.startTime || 0), (ThreadStruct.selectThreadStruct.startTime || 0) + (ThreadStruct.selectThreadStruct.dur || 0));
            } else if (FuncStruct.selectFuncStruct) {
                this.timerShaftEL?.setSlicesMark((FuncStruct.selectFuncStruct.startTs || 0), (FuncStruct.selectFuncStruct.startTs || 0) + (FuncStruct.selectFuncStruct.dur || 0));
            } else if (TraceRow.rangeSelectObject) {
                this.timerShaftEL?.setSlicesMark((TraceRow.rangeSelectObject.startNS || 0), (TraceRow.rangeSelectObject.endNS || 0));
            } else if (JankStruct.selectJankStruct) {
                this.timerShaftEL?.setSlicesMark((JankStruct.selectJankStruct.ts || 0), (JankStruct.selectJankStruct.ts || 0) + (JankStruct.selectJankStruct.dur || 0));
            } else {
                this.timerShaftEL?.setSlicesMark();
            }
        }
        if (this.isMousePointInSheet) {
            return;
        }
        this.observerScrollHeightEnable = false;
        this.keyboardEnable && this.timerShaftEL!.documentOnKeyPress(ev);
    }

    documentOnKeyUp = (ev: KeyboardEvent) => {
        if (!this.loadTraceCompleted) return;
        TraceRow.isUserInteraction = false;
        this.observerScrollHeightEnable = false;
        this.keyboardEnable && this.timerShaftEL!.documentOnKeyUp(ev);
        if (ev.code == "Enter") {
            if (ev.shiftKey) {
                this.dispatchEvent(new CustomEvent("previous-data", {
                    detail: {},
                    composed: false
                }));
            } else {
                this.dispatchEvent(new CustomEvent("next-data", {
                    detail: {},
                    composed: false
                }));
            }
        }
    }

    isMouseInSheet = (ev: MouseEvent) => {
        this.isMousePointInSheet = this.traceSheetEL?.getAttribute("mode") != "hidden" && ev.offsetX > this.traceSheetEL!.offsetLeft && ev.offsetY > this.traceSheetEL!.offsetTop;
        return this.isMousePointInSheet;
    }

    favoriteChangeHandler = (row: TraceRow<any>) => {
        info("favoriteChangeHandler", row.frame, row.offsetTop, row.offsetHeight);
    }

    selectChangeHandler = (rows: Array<TraceRow<any>>) => {
        this.rangeSelect.rangeTraceRow = rows;
        this.rangeSelect.selectHandler?.(this.rangeSelect.rangeTraceRow, false);
    }

    documentOnMouseMove = (ev: MouseEvent) => {
        if ((window as any).isSheetMove) return;
        if (!this.loadTraceCompleted || (window as any).flagInputFocus) return;
        if (this.isMouseInSheet(ev)) {
            this.hoverStructNull();
            return;
        }
        let rows = this.visibleRows;
        if (this.timerShaftEL?.isScaling()) {
            return;
        }
        this.timerShaftEL?.documentOnMouseMove(ev)
        this.rangeSelect.mouseMove(rows, ev);
        if (this.rangeSelect.isMouseDown) {
            this.refreshCanvas(true);
        } else {
            if (!this.rowsPaneEL!.containPoint(ev, {left: 248})) {
                this.tipEL!.style.display = "none";
                this.hoverStructNull();
            }
            rows.filter(it => it.focusContain(ev)).forEach(tr => {
                if (this.currentRowType != tr.rowType) {
                    this.hoverStructNull();
                    this.tipEL!.style.display = "none";
                    this.currentRowType = tr.rowType || "";
                }
                if (tr.rowType == TraceRow.ROW_TYPE_CPU) {
                    CpuStruct.hoverCpuStruct = undefined;
                    for (let re of tr.dataListCache) {
                        if (re.frame && isFrameContainPoint(re.frame, tr.hoverX, tr.hoverY)) {
                            CpuStruct.hoverCpuStruct = re;
                            break
                        }
                    }
                } else {
                    CpuStruct.hoverCpuStruct = undefined;
                }
                tr.focusHandler?.(ev);
            })
        }
    }

    hoverStructNull() {
        CpuStruct.hoverCpuStruct = undefined;
        CpuFreqStruct.hoverCpuFreqStruct = undefined;
        ThreadStruct.hoverThreadStruct = undefined;
        FuncStruct.hoverFuncStruct = undefined;
        HiPerfCpuStruct.hoverStruct = undefined;
        HiPerfProcessStruct.hoverStruct = undefined;
        HiPerfThreadStruct.hoverStruct = undefined;
        HiPerfEventStruct.hoverStruct = undefined;
        HiPerfReportStruct.hoverStruct = undefined;
        CpuStateStruct.hoverStateStruct = undefined;
        CpuAbilityMonitorStruct.hoverCpuAbilityStruct = undefined;
        DiskAbilityMonitorStruct.hoverDiskAbilityStruct = undefined;
        MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct = undefined;
        NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = undefined;
        CpuFreqLimitsStruct.hoverCpuFreqLimitsStruct = undefined;
        FpsStruct.hoverFpsStruct = undefined;
        ClockStruct.hoverClockStruct = undefined;
        IrqStruct.hoverIrqStruct = undefined;
        JankStruct.hoverJankStruct = undefined;
    }

    selectStructNull() {
        CpuStruct.selectCpuStruct = undefined;
        CpuStruct.wakeupBean = null;
        CpuFreqStruct.selectCpuFreqStruct = undefined;
        ThreadStruct.selectThreadStruct = undefined;
        FuncStruct.selectFuncStruct = undefined;
        SpHiPerf.selectCpuStruct = undefined;
        CpuStateStruct.selectStateStruct = undefined;
        CpuFreqLimitsStruct.selectCpuFreqLimitsStruct = undefined
        ClockStruct.selectClockStruct = undefined;
        IrqStruct.selectIrqStruct = undefined;
        JankStruct.selectJankStruct = undefined;
    }

    documentOnClick = (ev: MouseEvent) => {
        if (!this.loadTraceCompleted) return;
        if (this.isMouseInSheet(ev)) return;
        if ((window as any).isPackUpTable) {
            (window as any).isPackUpTable = false;
            return;
        }
        if (this.rangeSelect.isDrag()) {
            return;
        }
        let x = ev.offsetX - this.timerShaftEL!.canvas!.offsetLeft;
        let y = ev.offsetY;
        if (this.timerShaftEL!.sportRuler!.frame.contains(x, y) && x > (TraceRow.rangeSelectObject?.startX || 0) && x < (TraceRow.rangeSelectObject?.endX || 0)) {
        } else {
            let row = this.visibleRows.filter((it) => it.focusContain(ev))
            if (row.length > 0) {
                this.onClickHandler();
                this.documentOnMouseMove(ev)
            }
        }
    }

    onClickHandler() {
        if (!this.loadTraceCompleted) return;
        this.shadowRoot?.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => it.rangeSelect = false)
        this.selectStructNull();
        let threadClickHandler: any;
        let cpuClickHandler: any;
        let jankClickHandler: any;
        threadClickHandler = (d: ThreadStruct) => {
            this.observerScrollHeightEnable = false;
            this.scrollToProcess(`${d.cpu}`, "", "cpu-data", true);
            let cpuRow = this.shadowRoot?.querySelectorAll<TraceRow<CpuStruct>>(`trace-row[row-id='${d.cpu}'][row-type='cpu-data']`)[0];
            let findEntry = cpuRow!.dataList!.find((dat: any) => dat.startTime === d.startTime);
            if (findEntry!.startTime! + findEntry!.dur! < TraceRow.range!.startNS || findEntry!.startTime! > TraceRow.range!.endNS) {
                this.timerShaftEL?.setRangeNS(findEntry!.startTime! - findEntry!.dur! * 2, findEntry!.startTime! + findEntry!.dur! + findEntry!.dur! * 2);
            }
            this.hoverStructNull();
            this.selectStructNull();
            CpuStruct.hoverCpuStruct = findEntry;
            CpuStruct.selectCpuStruct = findEntry;
            this.timerShaftEL?.drawTriangle(findEntry!.startTime || 0, "inverted");
            this.traceSheetEL?.displayCpuData(CpuStruct.selectCpuStruct!, (wakeUpBean) => {
                CpuStruct.wakeupBean = wakeUpBean;
                this.refreshCanvas(true);
            }, cpuClickHandler);
        }

        cpuClickHandler = (d: CpuStruct) => {
            this.observerScrollHeightEnable = true;
            let threadRow = this.shadowRoot?.querySelectorAll<TraceRow<ThreadStruct>>(`trace-row[row-id='${d.tid}'][row-type='thread']`)[0];
            let task = () => {
                if (threadRow) {
                    let findEntry = threadRow!.dataList!.find((dat) => dat.startTime === d.startTime);
                    if (findEntry!.startTime! + findEntry!.dur! < TraceRow.range!.startNS || findEntry!.startTime! > TraceRow.range!.endNS) {
                        this.timerShaftEL?.setRangeNS(findEntry!.startTime! - findEntry!.dur! * 2, findEntry!.startTime! + findEntry!.dur! + findEntry!.dur! * 2);
                    }
                    this.hoverStructNull();
                    this.selectStructNull();
                    ThreadStruct.hoverThreadStruct = findEntry;
                    ThreadStruct.selectThreadStruct = findEntry;
                    this.closeAllExpandRows(d.processId + "")
                    this.timerShaftEL?.drawTriangle(findEntry!.startTime || 0, "inverted");
                    this.traceSheetEL?.displayThreadData(ThreadStruct.selectThreadStruct!, threadClickHandler, cpuClickHandler);
                    this.scrollToProcess(`${d.tid}`, `${d.processId}`, "thread", true);
                }
            }
            if (threadRow) {
                this.scrollToProcess(`${d.tid}`, `${d.processId}`, "process", false);
                this.scrollToProcess(`${d.tid}`, `${d.processId}`, "thread", true);
                if (threadRow!.isComplete) {
                    task()
                } else {
                    threadRow!.onComplete = task
                }
            }
        }

        jankClickHandler = (d: any) => {
            this.observerScrollHeightEnable = true;
            let jankRow = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='${d.rowId}'][row-type='janks']`);
            let task = () => {
                if (jankRow) {
                    JankStruct.selectJankStructList.length = 0;
                    let findJankEntry = jankRow!.dataList!.find((dat: any) => dat.name == d.name && dat.pid == d.pid);
                    if (findJankEntry!.ts! + findJankEntry!.dur! < TraceRow.range!.startNS || findJankEntry!.ts! > TraceRow.range!.endNS) {
                        this.timerShaftEL?.setRangeNS(findJankEntry!.ts! - findJankEntry!.dur! * 2, findJankEntry!.ts! + findJankEntry!.dur! + findJankEntry!.dur! * 2);
                    }
                    this.hoverStructNull();
                    this.selectStructNull();
                    JankStruct.hoverJankStruct = findJankEntry;
                    JankStruct.selectJankStruct = findJankEntry;
                    this.timerShaftEL?.drawTriangle(findJankEntry!.ts || 0, "inverted");
                    this.traceSheetEL?.displayJankData(JankStruct.selectJankStruct!, (datas) => {
                        this.clearPointPair();
                        // 绘制跟自己关联的线
                        datas.forEach(data => {
                            let endParentRow = this.shadowRoot?.querySelector<TraceRow<any>>(`trace-row[row-id='${data.pid}'][folder]`);
                            this.drawJankLine(endParentRow, JankStruct.selectJankStruct!, data);
                        })
                    }, jankClickHandler);
                    this.scrollToProcess(jankRow.rowId!, jankRow.rowParentId!, jankRow.rowType!, true);
                }
            }
            if (jankRow) {
                this.scrollToProcess(jankRow.rowId!, jankRow.rowParentId!, jankRow.rowType!, false);
            }
            if (jankRow!.isComplete) {
                task()
            } else {
                window.subscribe(window.SmartEvent.UI.TraceRowComplete, (tr) => {
                    let row = tr as TraceRow<JankStruct>
                    if (row.rowId == jankRow!.rowId && row.rowType == jankRow!.rowType) {
                        task();
                    }
                })
            }
        }

        if (CpuStruct.hoverCpuStruct) {
            CpuStruct.selectCpuStruct = CpuStruct.hoverCpuStruct
            this.timerShaftEL?.drawTriangle(CpuStruct.selectCpuStruct!.startTime || 0, "inverted");
            this.traceSheetEL?.displayCpuData(CpuStruct.selectCpuStruct, (wakeUpBean) => {
                CpuStruct.wakeupBean = wakeUpBean;
                this.refreshCanvas(false);
            }, cpuClickHandler);
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (ThreadStruct.hoverThreadStruct) {
            ThreadStruct.selectThreadStruct = ThreadStruct.hoverThreadStruct;
            this.timerShaftEL?.drawTriangle(ThreadStruct.selectThreadStruct!.startTime || 0, "inverted");
            this.traceSheetEL?.displayThreadData(ThreadStruct.selectThreadStruct, threadClickHandler, cpuClickHandler);
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (FuncStruct.hoverFuncStruct) {
            FuncStruct.selectFuncStruct = FuncStruct.hoverFuncStruct;
            let hoverFuncStruct = FuncStruct.hoverFuncStruct
            this.timerShaftEL?.drawTriangle(FuncStruct.selectFuncStruct!.startTs || 0, "inverted");
            FuncStruct.selectFuncStruct = hoverFuncStruct
            this.traceSheetEL?.displayFuncData(FuncStruct.selectFuncStruct, (funcStract: any) => {
                this.observerScrollHeightEnable = true;
                this.moveRangeToCenter(funcStract.startTime!, funcStract.dur!)
                this.scrollToActFunc(funcStract, false)
            })
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (CpuFreqStruct.hoverCpuFreqStruct) {
            CpuFreqStruct.selectCpuFreqStruct = CpuFreqStruct.hoverCpuFreqStruct
            this.traceSheetEL?.displayFreqData()
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (CpuStateStruct.hoverStateStruct) {
            CpuStateStruct.selectStateStruct = CpuStateStruct.hoverStateStruct;
            this.traceSheetEL?.displayCpuStateData()
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (CpuFreqLimitsStruct.hoverCpuFreqLimitsStruct) {
            CpuFreqLimitsStruct.selectCpuFreqLimitsStruct = CpuFreqLimitsStruct.hoverCpuFreqLimitsStruct
            this.traceSheetEL?.displayFreqLimitData()
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (ClockStruct.hoverClockStruct) {
            ClockStruct.selectClockStruct = ClockStruct.hoverClockStruct
            this.traceSheetEL?.displayClockData(ClockStruct.selectClockStruct)
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (IrqStruct.hoverIrqStruct) {
            IrqStruct.selectIrqStruct = IrqStruct.hoverIrqStruct;
            this.traceSheetEL?.displayIrqData(IrqStruct.selectIrqStruct);
            this.timerShaftEL?.modifyFlagList(undefined);

        } else if (JankStruct.hoverJankStruct) {
            JankStruct.selectJankStructList.length = 0;
            this.clearPointPair();
            JankStruct.selectJankStruct = JankStruct.hoverJankStruct;
            this.timerShaftEL?.drawTriangle(JankStruct.selectJankStruct!.ts || 0, "inverted");
            this.traceSheetEL?.displayJankData(JankStruct.selectJankStruct, (datas) => {
                datas.forEach(data => {
                    let endParentRow;
                    if (data.frame_type == "frameTime") {
                        endParentRow = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='frameTime'][row-type='janks']`);
                    } else {
                        endParentRow = this.shadowRoot?.querySelector<TraceRow<any>>(`trace-row[row-id='${data.pid}'][folder]`);
                    }
                    this.drawJankLine(endParentRow, JankStruct.selectJankStruct!, data);
                })
            }, jankClickHandler);
        } else {
            if (!JankStruct.hoverJankStruct && JankStruct.delJankLineFlag) {
                this.clearPointPair();
            }
            this.observerScrollHeightEnable = false;
            this.selectFlag = null;
            this.timerShaftEL?.removeTriangle("inverted");
            if (!SportRuler.isMouseInSportRuler) {
                this.traceSheetEL?.setAttribute("mode", 'hidden');
                this.refreshCanvas(true);
            }
        }
        if (!JankStruct.selectJankStruct) {
            this.clearPointPair();
        }
    }

    drawJankLine(endParentRow: any, selectJankStruct: JankStruct, data: any) {
        let startRow: any;
        if (selectJankStruct.frame_type == "frameTime") {
            startRow = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='actual frameTime'][row-type='janks']`);
        } else {
            startRow = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='${selectJankStruct?.type + '-' + selectJankStruct?.pid}'][row-type='janks']`);
        }
        let startPointNS: number;
        if (endParentRow) {
            endParentRow.expansion = true;
            let endRowStruct: any;
            if (data.frame_type == "frameTime") {
                endRowStruct = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='actual frameTime'][row-type='janks']`);
            } else {
                endRowStruct = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='${data.type + '-' + data.pid}'][row-type='janks']`);
            }
            let task = () => {
                if (endRowStruct) {
                    let findJankEntry = endRowStruct!.dataList!.find((dat: any) => dat.name == data.name && dat.pid == data.pid);
                    //连线规则：frametimeline的头----app的头，app的尾----renderservice的头
                    let tts: number = 0
                    if (selectJankStruct.frame_type == 'app') {
                        tts = findJankEntry.frame_type == 'frameTime' ? selectJankStruct.ts! : (selectJankStruct.ts! + selectJankStruct.dur!)
                        this.addPointPair({
                            x: ns2xByTimeShaft(tts, this.timerShaftEL!),
                            y: startRow!.translateY! + (20 * (selectJankStruct!.depth! + 0.5)),
                            offsetY: (20 * (selectJankStruct!.depth! + 0.5)),
                            ns: tts,
                            rowEL: startRow!,
                            isRight: selectJankStruct.ts == tts
                        }, {
                            x: ns2xByTimeShaft(findJankEntry.ts!, this.timerShaftEL!),
                            y: endRowStruct!.translateY! + (20 * (findJankEntry!.depth! + 0.5)),
                            offsetY: (20 * (findJankEntry!.depth! + 0.5)),
                            ns: findJankEntry.ts!,
                            rowEL: endRowStruct!,
                            isRight: true
                        });
                    }
                    if (findJankEntry.frame_type == 'app') {
                        tts = selectJankStruct.frame_type == 'frameTime' ? findJankEntry.ts : (findJankEntry.ts! + findJankEntry.dur!)
                        this.addPointPair({
                            x: ns2xByTimeShaft(selectJankStruct.ts!, this.timerShaftEL!),
                            y: startRow!.translateY! + (20 * (selectJankStruct!.depth! + 0.5)),
                            offsetY: (20 * (selectJankStruct!.depth! + 0.5)),
                            ns: selectJankStruct.ts!,
                            rowEL: startRow!,
                            isRight: true
                        }, {
                            x: ns2xByTimeShaft(tts, this.timerShaftEL!),
                            y: endRowStruct!.translateY! + (20 * (findJankEntry!.depth! + 0.5)),
                            offsetY: (20 * (findJankEntry!.depth! + 0.5)),
                            ns: tts,
                            rowEL: endRowStruct!,
                            isRight: selectJankStruct.ts == tts
                        });
                    }
                    if (data.children.length >= 1) {
                        let endP;
                        if (data.children[0].frame_type == "frameTime") {
                            endP = this.shadowRoot?.querySelector<TraceRow<any>>(`trace-row[row-id='frameTime']`);
                        } else {
                            endP = this.shadowRoot?.querySelector<TraceRow<any>>(`trace-row[row-id='${data.children[0].pid}'][folder]`);
                        }
                        // @ts-ignore
                        this.drawJankLine(endP, findJankEntry, data.children[0]);
                    }
                    this.refreshCanvas(true);
                }
            }
            if (endRowStruct) {
                if (endRowStruct!.isComplete) {
                    task();
                    let jankRow: any;
                    if (JankStruct.selectJankStruct!.frame_type == "frameTime") {
                        jankRow = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='actual frameTime'][row-type='janks']`);
                    } else {
                        jankRow = this.shadowRoot?.querySelector<TraceRow<JankStruct>>(`trace-row[row-id='${JankStruct.selectJankStruct?.type + '-' + JankStruct.selectJankStruct?.pid}'][row-type='janks']`);
                    }
                    if (jankRow) {
                        this.scrollToProcess(jankRow.rowId!, jankRow.rowParentId!, jankRow.rowType!, true);
                    }
                } else {
                    window.subscribe(window.SmartEvent.UI.TraceRowComplete, (tr) => {
                        let row = tr as TraceRow<JankStruct>
                        if (row.rowId == endRowStruct.rowId && row.rowType == endRowStruct.rowType) {
                            task();
                        }
                    })
                    endRowStruct!.sleeping = false;
                    endRowStruct!.draw(false);
                }
            }
        }
    }


    connectedCallback() {
        /**
         * 监听时间轴区间变化
         */
        this.timerShaftEL!.rangeChangeHandler = this.timerShaftELRangeChange;
        this.timerShaftEL!.flagChangeHandler = this.timerShaftELFlagChange;
        this.timerShaftEL!.flagClickHandler = this.timerShaftELFlagClickHandler;
        /**
         * 监听rowsEL的滚动时间，刷新可见区域的trace-row组件的时间区间（将触发trace-row组件重绘）
         */
        this.rowsPaneEL?.addEventListener('scroll', this.rowsElOnScroll, {passive: true})
        this.favoriteRowsEL?.addEventListener('scroll', this.favoriteRowsElOnScroll, {passive: true})
        /**
         * 监听document的mousemove事件 坐标通过换算后找到当前鼠标所在的trace-row组件，将坐标传入
         */
        this.addEventListener('mousemove', this.documentOnMouseMove)
        this.addEventListener('click', this.documentOnClick)
        this.addEventListener('mousedown', this.documentOnMouseDown)
        this.addEventListener('mouseup', this.documentOnMouseUp)
        this.addEventListener('mouseout', this.documentOnMouseOut)
        document.addEventListener('keypress', this.documentOnKeyPress)
        document.addEventListener('keyup', this.documentOnKeyUp)
        SpApplication.skinChange2 = (val: boolean) => {
            this.timerShaftEL?.render()
        }

    }

    scrollToProcess(rowId: string, rowParentId: string, rowType: string, smooth: boolean = true) {
        let rootRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
        if (rootRow?.collect) {
            this.favoriteRowsEL!.scroll({
                top: (rootRow?.offsetTop || 0) - this.canvasFavoritePanel!.offsetHeight + (rootRow?.offsetHeight || 0),
                left: 0,
                behavior: smooth ? "smooth" : undefined
            })
        } else {
            let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowParentId}'][folder]`);
            if (row) {
                row.expansion = true
            }
            this.rowsPaneEL!.scroll({
                top: (rootRow?.offsetTop || 0) - this.canvasPanel!.offsetHeight + (rootRow?.offsetHeight || 0),
                left: 0,
                behavior: smooth ? "smooth" : undefined
            })
        }
    }

    scrollToDepth(rowId: string, rowParentId: string, rowType: string, smooth: boolean = true, depth: number) {
        let rootRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
        if (rootRow &&　rootRow!.collect) {
            this.favoriteRowsEL!.scroll({
                top: (rootRow?.offsetTop || 0) - this.canvasFavoritePanel!.offsetHeight + ((++depth) * 20 || 0),
                left: 0,
                behavior: smooth ? "smooth" : undefined
            })
        } else {
            let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowParentId}'][folder]`);
            if (row) {
                row.expansion = true
            }
            this.rowsPaneEL!.scroll({
                top: (rootRow?.offsetTop || 0) - this.canvasPanel!.offsetHeight + ((++depth) * 20 || 0),
                left: 0,
                behavior: smooth ? "smooth" : undefined
            })
        }
    }

    scrollToFunction(rowId: string, rowParentId: string, rowType: string, smooth: boolean = true, afterScroll: any) {
        let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowParentId}'][folder]`);
        if (row) {
            row.expansion = true
        }
        let funcRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
        if (funcRow == null) {
            let threadRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='thread']`);
            this.rowsPaneEL!.scroll({
                top: threadRow!.offsetTop - this.canvasPanel!.offsetHeight + threadRow!.offsetHeight + threadRow!.offsetHeight,
                left: 0,
                behavior: undefined
            })
            if (threadRow != null) {
                if (threadRow.isComplete) {
                    afterScroll()
                } else {
                    threadRow.onComplete = () => {
                        funcRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
                        afterScroll()
                    }
                }
            }
        } else {
            afterScroll()
        }
    }

    rowScrollTo(offset: number, callback: Function) {
        const fixedOffset = offset;
        const onScroll = () => {
            if (this.rowsPaneEL!.scrollTop === fixedOffset) {
                this.rowsEL!.removeEventListener('scroll', onScroll)
                callback()
            }
        }

        this.rowsEL!.addEventListener('scroll', onScroll)
        onScroll()
        this.rowsPaneEL!.scrollTo({
            top: offset,
            behavior: 'smooth'
        })
    }

    disconnectedCallback() {
        this.timerShaftEL?.removeEventListener('range-change', this.timerShaftELRangeChange);
        this.rowsPaneEL?.removeEventListener('scroll', this.rowsElOnScroll);
        this.favoriteRowsEL?.removeEventListener('scroll', this.favoriteRowsElOnScroll)
        this.removeEventListener('mousemove', this.documentOnMouseMove);
        this.removeEventListener('click', this.documentOnClick);
        this.removeEventListener('mousedown', this.documentOnMouseDown)
        this.removeEventListener('mouseup', this.documentOnMouseUp)
        this.removeEventListener('mouseout', this.documentOnMouseOut)
        document.removeEventListener('keypress', this.documentOnKeyPress)
        document.removeEventListener('keyup', this.documentOnKeyUp)
        window.unsubscribe(window.SmartEvent.UI.SliceMark, this.sliceMarkEventHandler.bind(this));
    }

    sliceMarkEventHandler(ev: any) {
        SpSystemTrace.sliceRangeMark = ev;
        let startNS = (ev).timestamp - ((window as any).recordStartNS)
        let endNS = (ev).maxDuration + startNS;
        TraceRow.rangeSelectObject = {startX: 0, startNS: startNS, endNS: endNS, endX: 0}
        window.publish(window.SmartEvent.UI.MenuTrace, {});
        window.publish(window.SmartEvent.UI.TimeRange, {
            startNS: startNS - (ev).maxDuration,
            endNS: endNS + (ev).maxDuration
        })
    }

    loadDatabaseUrl(url: string, progress: Function, complete?: ((res: { status: boolean, msg: string }) => void) | undefined) {
        this.observerScrollHeightEnable = false;
        this.init({url: url}, "", progress).then((res) => {
            if (complete) {
                complete(res);
            }
        })
    }

    loadDatabaseArrayBuffer(buf: ArrayBuffer, thirdPartyWasmConfigUrl: string, progress: ((name: string, percent: number) => void), complete?: ((res: { status: boolean, msg: string }) => void) | undefined) {
        this.observerScrollHeightEnable = false;
        this.init({buf}, thirdPartyWasmConfigUrl, progress).then((res) => {
            let scrollTop = this.rowsEL?.scrollTop || 0
            let scrollHeight = this.rowsEL?.clientHeight || 0
            this.rowsEL?.querySelectorAll("trace-row").forEach((it: any) => this.observer.observe(it))
            if (complete) {
                complete(res);
            }
        })
    }

    search(query: string) {
        this.shadowRoot?.querySelectorAll<TraceRow<any>>('trace-row').forEach(item => {
            if (query == null || query == undefined || query == '') {
                if (item.rowType == TraceRow.ROW_TYPE_CPU ||
                    item.rowType == TraceRow.ROW_TYPE_CPU_FREQ ||
                    item.rowType == TraceRow.ROW_TYPE_NATIVE_MEMORY ||
                    item.rowType == TraceRow.ROW_TYPE_FPS ||
                    item.rowType == TraceRow.ROW_TYPE_PROCESS ||
                    item.rowType == TraceRow.ROW_TYPE_CPU_ABILITY ||
                    item.rowType == TraceRow.ROW_TYPE_MEMORY_ABILITY ||
                    item.rowType == TraceRow.ROW_TYPE_DISK_ABILITY ||
                    item.rowType == TraceRow.ROW_TYPE_NETWORK_ABILITY) {
                    item.expansion = false;
                    item.rowHidden = false;
                } else {
                    item.rowHidden = true;
                }
            } else {
                if (item.name.toLowerCase().indexOf(query.toLowerCase()) >= 0) {
                    item.rowHidden = false;
                } else {
                    item.rowHidden = true;
                }
            }
        })
        this.visibleRows.forEach(it => it.rowHidden = false && it.draw(true))
    }

    searchCPU(query: string): Array<CpuStruct> {
        let searchResults: Array<CpuStruct> = []
        this.shadowRoot!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu-data']`).forEach(item => {
            let res = item!.dataList!.filter(it => (it.name && it.name.indexOf(query) >= 0) || it.tid == query
                || it.processId == query
                || (it.processName && it.processName.indexOf(query) >= 0)
            )
            searchResults.push(...res);
        })
        searchResults.sort((a, b) => (a.startTime || 0) - (b.startTime || 0));
        return searchResults;
    }

    async searchFunction(cpuList: Array<any>, query: string): Promise<Array<any>> {
        let list = await querySearchFunc(query)
        cpuList = cpuList.concat(list)
        cpuList.sort((a, b) => (a.startTime || 0) - (b.startTime || 0));
        return cpuList
    }

    searchSdk(dataList: Array<any>, query: string): Array<any> {
        this.shadowRoot!.querySelectorAll<TraceRow<any>>(`trace-row[row-type^='sdk']`).forEach((row) => {
            if (row!.name.indexOf(query) >= 0) {
                let searchSdkBean = new SearchSdkBean()
                searchSdkBean.startTime = TraceRow.range!.startNS
                searchSdkBean.dur = TraceRow.range!.totalNS
                searchSdkBean.name = row.name
                searchSdkBean.rowId = row.rowId
                searchSdkBean.type = "sdk"
                searchSdkBean.rowType = row.rowType
                searchSdkBean.rowParentId = row.rowParentId
                dataList.push(searchSdkBean)
            }
        })
        return dataList
    }

    searchThreadsAndProcesses(query: string): Array<any> {
        let searchResults: Array<any> = []
        this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='thread'][row-type='process']`).forEach(item => {
            if (item!.name.indexOf(query) >= 0) {
                let searchBean = new SearchThreadProcessBean()
                searchBean.name = item.name
                searchBean.rowId = item.rowId
                searchBean.type = "thread||process"
                searchBean.rowType = item.rowType
                searchBean.rowParentId = item.rowParentId
                searchResults.push(searchBean)
            }
        })
        return searchResults
    }

    showStruct(previous: boolean, currentIndex: number, structs: Array<any>) {
        if (structs.length == 0) {
            return 0;
        }
        let findIndex = -1;
        if (previous) {
            for (let i = structs.length - 1; i >= 0; i--) {
                let it = structs[i];
                if (i < currentIndex && (it.startTime!) >= (TraceRow.range!.startNS) && (it.startTime!) + (it.dur!) <= (TraceRow.range!.endNS)) {
                    findIndex = i;
                    break;
                }
            }
        } else {
            findIndex = structs.findIndex((it, idx) => {
                return idx > currentIndex && (it.startTime!) >= (TraceRow.range!.startNS) && (it.startTime!) + (it.dur!) <= (TraceRow.range!.endNS)
            })
        }
        let findEntry: any
        if (findIndex >= 0) {
            findEntry = structs[findIndex];
        } else {
            if (previous) {
                for (let i = structs.length - 1; i >= 0; i--) {
                    let it = structs[i];
                    if ((it.startTime! + it.dur!) < (TraceRow.range!.startNS)) {
                        findIndex = i;
                        break;
                    }
                }
                if (findIndex == -1) {
                    findIndex = structs.length - 1;
                }
            } else {
                findIndex = structs.findIndex((it) => (it.startTime!) > (TraceRow.range!.endNS))
                if (findIndex == -1) {
                    findIndex = 0;
                }
            }
            findEntry = structs[findIndex];
            this.moveRangeToCenter(findEntry.startTime!, findEntry.dur!)
        }
        this.shadowRoot!.querySelectorAll<TraceRow<any>>(`trace-row`).forEach(item => {
            item.highlight = false;
        })
        if (findEntry.type == 'thread') {
            CpuStruct.selectCpuStruct = findEntry;
            CpuStruct.hoverCpuStruct = CpuStruct.selectCpuStruct;
            this.shadowRoot!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu-data']`).forEach(item => {
                item.highlight = item.rowId == `${findEntry.cpu}`;
                item.draw(true)
            })
            this.scrollToProcess(`${findEntry.cpu}`, "", "cpu-data", true)
            this.onClickHandler();
        } else if (findEntry.type == "func") {
            this.observerScrollHeightEnable = true;
            this.scrollToActFunc(findEntry, true)
        } else if (findEntry.type == "thread||process") {
            let threadProcessRow = this.rowsEL?.querySelectorAll<TraceRow<ThreadStruct>>(`trace-row[row-id='${findEntry.rowId}'][row-type='${findEntry.rowType}']`)[0];
            threadProcessRow!.highlight = true
            this.closeAllExpandRows(findEntry.rowParentId)
            this.scrollToProcess(`${findEntry.rowId}`, `${findEntry.rowParentId}`, findEntry.rowType, true);
            let completeEntry = () => {
                let searchEntry = threadProcessRow!.dataList!.find((dat) => dat.startTime === findEntry.startTime);
                this.hoverStructNull();
                this.selectStructNull();
                ThreadStruct.hoverThreadStruct = searchEntry;
                ThreadStruct.selectThreadStruct = searchEntry;
                this.scrollToProcess(`${findEntry.rowId}`, `${findEntry.rowParentId}`, findEntry.rowType, true)
            }
            if (threadProcessRow!.isComplete) {
                completeEntry()
            } else {
                threadProcessRow!.onComplete = completeEntry
            }
        } else if (findEntry.type == "sdk") {
            let sdkRow = this.shadowRoot?.querySelectorAll<TraceRow<any>>(`trace-row[row-id='${findEntry.rowId}'][row-type='${findEntry.rowType}']`)[0];
            sdkRow!.highlight = true
            this.hoverStructNull();
            this.selectStructNull();
            this.onClickHandler();
            this.closeAllExpandRows(findEntry.rowParentId)
            this.scrollToProcess(`${findEntry.rowId}`, `${findEntry.rowParentId}`, findEntry.rowType, true);
        }
        this.timerShaftEL?.drawTriangle(findEntry.startTime || 0, "inverted");
        return findIndex;
    }

    scrollToActFunc(funcStract: any, highlight: boolean) {
        let funcRowID = funcStract.cookie == null ? funcStract.tid : `${funcStract.funName}-${funcStract.pid}`
        let funcRow = this.shadowRoot?.querySelector<TraceRow<FuncStruct>>(`trace-row[row-id='${funcRowID}'][row-type='func']`);
        if (funcRow == null) return
        funcRow!.highlight = highlight;
        this.closeAllExpandRows(funcStract.pid)
        let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${funcStract.pid}'][folder]`);
        if (row && !row.expansion) {
            row.expansion = true
        }
        if (funcStract.cookie == null) {
            this.scrollToProcess(`${funcStract.tid}`, `${funcStract.pid}`, "thread", false)
        }
        this.scrollToDepth(`${funcRowID}`, `${funcStract.pid}`, funcStract.type, true, funcStract.depth || 0)
        let completeEntry = () => {
            let searchEntry = funcRow!.dataList!.find((dat) => dat.startTs === funcStract.startTime);
            this.hoverStructNull();
            this.selectStructNull();
            FuncStruct.hoverFuncStruct = searchEntry;
            FuncStruct.selectFuncStruct = searchEntry;
            this.onClickHandler();
            this.scrollToDepth(`${funcRowID}`, `${funcStract.pid}`, funcStract.type, true, funcStract.depth || 0)
        }
        if (funcRow!.isComplete) {
            completeEntry()
        } else {
            funcRow!.onComplete = completeEntry
        }
    }

    closeAllExpandRows(pid: string) {
        let expandRows = this.rowsEL?.querySelectorAll<TraceRow<ProcessStruct>>(`trace-row[row-type='process'][expansion]`);
        expandRows?.forEach((row) => {
            if (row.rowId != pid) {
                row.expansion = false
            }
        })
    }

    moveRangeToCenter(startTime: number, dur: number) {
        let startNS = this.timerShaftEL?.getRange()?.startNS || 0;
        let endNS = this.timerShaftEL?.getRange()?.endNS || 0;
        let harfDur = Math.trunc((endNS - startNS) / 2 - dur / 2);
        this.timerShaftEL?.setRangeNS(startTime - harfDur, startTime + dur + harfDur);
    }

    showPreCpuStruct(currentIndex: number, cpuStructs: Array<CpuStruct>): number {
        if (cpuStructs.length == 0) {
            return 0;
        }
        let findIndex = -1;
        for (let i = cpuStructs.length - 1; i >= 0; i--) {
            let it = cpuStructs[i];
            if (i < currentIndex && (it.startTime!) >= (TraceRow.range!.startNS) && (it.startTime!) + (it.dur!) <= (TraceRow.range!.endNS)) {
                findIndex = i;
                break;
            }
        }
        if (findIndex >= 0) {
            let findEntry = cpuStructs[findIndex];
            CpuStruct.selectCpuStruct = findEntry;
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu-data']`).forEach(item => {
                item.highlight = item.rowId == `${findEntry.cpu}`;
                item.draw(true);
            })
            this.timerShaftEL?.drawTriangle(findEntry.startTime || 0, "inverted");
        } else {
            for (let i = cpuStructs.length - 1; i >= 0; i--) {
                let it = cpuStructs[i];
                if ((it.startTime! + it.dur!) < (TraceRow.range!.startNS)) {
                    findIndex = i;
                    break;
                }
            }
            let findEntry: CpuStruct;
            if (findIndex == -1) {
                findIndex = cpuStructs.length - 1;
            }
            findEntry = cpuStructs[findIndex];
            CpuStruct.selectCpuStruct = findEntry;
            let startNS = this.timerShaftEL?.getRange()?.startNS || 0;
            let endNS = this.timerShaftEL?.getRange()?.endNS || 0;
            let harfDur = Math.trunc((endNS - startNS) / 2 - findEntry.dur! / 2);
            this.timerShaftEL?.setRangeNS(findEntry.startTime! - harfDur, findEntry.startTime! + findEntry.dur! + harfDur);
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu-data']`).forEach(item => {
                item.highlight = item.rowId == `${findEntry.cpu}`;
                item.draw(true)
            })
            this.timerShaftEL?.drawTriangle(findEntry.startTime || 0, "inverted");
        }
        CpuStruct.hoverCpuStruct = CpuStruct.selectCpuStruct;
        this.onClickHandler();
        return findIndex;
    }

    showNextCpuStruct(currentIndex: number, cpuStructs: Array<CpuStruct>): number {
        if (cpuStructs.length == 0) {
            return 0;
        }
        let findIndex = cpuStructs.findIndex((it, idx) => {
            return idx > currentIndex && (it.startTime!) >= (TraceRow.range!.startNS) && (it.startTime!) + (it.dur!) <= (TraceRow.range!.endNS)
        })
        if (findIndex >= 0) {
            let findEntry = cpuStructs[findIndex];
            CpuStruct.selectCpuStruct = findEntry;
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu-data']`).forEach(item => {
                item.highlight = item.rowId == `${findEntry.cpu}`;
                item.draw(true);
            })
            this.timerShaftEL?.drawTriangle(findEntry.startTime || 0, "inverted");
        } else {
            findIndex = cpuStructs.findIndex((it) => (it.startTime!) > (TraceRow.range!.endNS))
            let findEntry: CpuStruct;
            if (findIndex == -1) {
                findIndex = 0;
            }
            findEntry = cpuStructs[findIndex];
            CpuStruct.selectCpuStruct = findEntry;
            let startNS = this.timerShaftEL?.getRange()?.startNS || 0;
            let endNS = this.timerShaftEL?.getRange()?.endNS || 0;
            let harfDur = Math.trunc((endNS - startNS) / 2 - findEntry.dur! / 2);
            this.timerShaftEL?.setRangeNS(findEntry.startTime! - harfDur, findEntry.startTime! + findEntry.dur! + harfDur);
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu-data']`).forEach(item => {
                item.highlight = item.rowId == `${findEntry.cpu}`;
                item.draw(true);
            })
            this.timerShaftEL?.drawTriangle(findEntry.startTime || 0, "inverted");
        }
        CpuStruct.hoverCpuStruct = CpuStruct.selectCpuStruct;
        this.onClickHandler();
        return findIndex;
    }

    reset(progress: Function | undefined | null) {
        this.visibleRows.length = 0;
        this.tipEL!.style.display = 'none';
        this.canvasPanelCtx?.clearRect(0, 0, this.canvasPanel!.clientWidth, this.canvasPanel!.offsetHeight);
        this.canvasFavoritePanelCtx?.clearRect(0, 0, this.canvasFavoritePanel!.clientWidth, this.canvasFavoritePanel!.clientHeight);
        this.favoriteRowsEL!.style.height = '0'
        this.canvasFavoritePanel!.style.height = '0';
        this.loadTraceCompleted = false;
        if (this.favoriteRowsEL) {
            this.favoriteRowsEL.querySelectorAll(`trace-row`).forEach((row) => {
                this.favoriteRowsEL!.removeChild(row);
            })
        }
        if (this.rowsEL) this.rowsEL.innerHTML = ''
        this.spacerEL!.style.height = '0px';
        this.rangeSelect.rangeTraceRow = [];
        this.collectRows = []
        this.timerShaftEL?.displayCollect(false)
        this.timerShaftEL!.collecBtn!.removeAttribute('close');
        CpuStruct.wakeupBean = undefined;
        this.selectStructNull();
        this.hoverStructNull();
        this.traceSheetEL?.setAttribute("mode", "hidden")
        progress && progress("rest timershaft", 8);
        this.timerShaftEL?.reset();
        progress && progress("clear cache", 10);
        procedurePool.clearCache();
    }

    init = async (param: { buf?: ArrayBuffer, url?: string }, wasmConfigUri: string, progress: Function) => {
        progress("Load database", 6);
        this.rowsPaneEL!.scroll({
            top: 0,
            left: 0
        })
        this.reset(progress);
        if (param.buf) {
            let configJson = "";
            try {
                configJson = await fetch(wasmConfigUri).then(res => res.text());
            } catch (e) {
                error("getWasmConfigFailed", e)
            }
            let {status, msg, sdkConfigMap} = await threadPool.initSqlite(param.buf, configJson, progress);
            if (!status) {
                return {status: false, msg: msg}
            }
            SpSystemTrace.SDK_CONFIG_MAP = sdkConfigMap == undefined ? undefined : sdkConfigMap;
        }
        if (param.url) {
            let {status, msg} = await threadPool.initServer(param.url, progress);
            if (!status) {
                return {status: false, msg: msg}
            }
        }
        await this.chartManager?.init(progress);
        this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach((it: any) => {
            it.addEventListener('expansion-change', () => {
                this.refreshCanvas(true);
            })
        })
        progress("completed", 100);
        info("All TraceRow Data initialized")
        this.loadTraceCompleted = true;
        this.rowsEL!.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => {
            this.intersectionObserver?.observe(it);
        })
        return {status: true, msg: "success"}
    }

    displayTip(row: TraceRow<any>, struct: any, html: string) {
        let x = row.hoverX + 248;
        let y = row.getBoundingClientRect().top - 195 + (this.rowsPaneEL?.scrollTop ?? 0);
        if ((struct == undefined || struct == null) && this.tipEL) {
            this.tipEL.style.display = 'none';
            return
        }
        if (this.tipEL) {
            this.tipEL.innerHTML = html;
            this.tipEL.style.display = 'flex';
            this.tipEL.style.height = row.style.height
            if (x + this.tipEL.clientWidth > (this.canvasPanel!.clientWidth ?? 0)) {
                this.tipEL.style.transform = `translateX(${x - this.tipEL.clientWidth - 1}px) translateY(${y}px)`;
            } else {
                this.tipEL.style.transform = `translateX(${x}px) translateY(${y}px)`;
            }
        }
    }

    initHtml(): string {
        return `
        <style>
        :host{
            display: block;
            width: 100%;
            height: 100%;
        }
        .timer-shaft{
            width: 100%;
            z-index: 2;
        }
        .rows-pane{
            overflow: overlay;
            overflow-anchor: none;
            /*height: 100%;*/
            max-height: calc(100vh - 147px - 48px);
        }
        .rows{
            min-height: 100%;
            color: #fff;
            display: flex;
            box-sizing: border-box;
            flex-direction: column;
            overflow-y: auto;
            flex: 1;
            width: 100%;
            background: var(--dark-background4,#ffffff);
            /*scroll-behavior: smooth;*/
        }
        .favorite-rows{
            width: 100%;
            position:fixed;
            overflow-y: auto;
            overflow-x: hidden;
            z-index:1001;
            background: var(--dark-background5,#ffffff);
            box-shadow: 0 10px 10px #00000044;
        }
        .container{
            width: 100%;
            box-sizing: border-box;
            height: 100%;
            display: grid;
            grid-template-columns: 1fr;
            grid-template-rows: min-content 1fr min-content;
            /*grid-template-areas:    'head'*/
                                    /*'body'*/
                                    /*'sheet';*/
            position:relative;
        }
        .panel-canvas{
            position: absolute;
            top: 0;
            right: 0px;
            bottom: 0px;
            width: 100%;
            /*height: calc(100vh - 195px);*/
            height: 100%;
            box-sizing: border-box;
            /*background: #f0f0f0;*/
            /*border: 1px solid #000000;*/
            z-index: 0;
        }
        .panel-canvas-favorite{
            width: 100% ;
            display: block;
            position: absolute;
            height: 0;
            top: 0;
            right: 0;
            box-sizing: border-box;
            z-index: 100;
        }
        .trace-sheet{
            cursor: default;
        }
        .tip{
            z-index: 1001;
            position: absolute;
            top: 0;
            left: 0;
            /*height: 100%;*/
            background-color: white;
            border: 1px solid #f9f9f9;
            width: auto;
            font-size: 8px;
            color: #50809e;
            flex-direction: column;
            justify-content: center;
            align-items: flex-start;
            padding: 2px 10px;
            box-sizing: border-box;
            display: none;
            user-select: none;
        }

        </style>
        <div class="container">
            <timer-shaft-element class="timer-shaft" style="position: relative;top: 0"></timer-shaft-element>
            <div class="rows-pane" style="position: relative;display: block;flex-direction: column;overflow-x: hidden;">
                <div class="favorite-rows">
                    <canvas id="canvas-panel-favorite" class="panel-canvas-favorite" ondragstart="return false"></canvas>
                </div>
                <canvas id="canvas-panel" class="panel-canvas" ondragstart="return false"></canvas>
                <div class="spacer" ondragstart="return false"></div>
                <div class="rows" ondragstart="return false"></div>
                <div id="tip" class="tip"></div>
            </div>
            <trace-sheet class="trace-sheet" mode="hidden" ondragstart="return false"></trace-sheet>
        </div>
        `;
    }
}

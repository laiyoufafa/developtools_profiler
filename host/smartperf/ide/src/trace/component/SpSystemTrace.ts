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
import {
    getAsyncEvents,
    getCpuUtilizationRate,
    getFps,
    getFunDataByTid,
    getSliceData,
    getSliceDataCount,
    getStatesProcessThreadData,
    getStatesProcessThreadDataCount,
    getThreadProcessData,
    getThreadStateData,
    getThreadStateDataCount, query,
    queryAbilityExits,
    queryAllHeapByEvent,
    queryBytesInAbilityData,
    queryBytesOutAbilityData,
    queryBytesReadAbilityData,
    queryBytesWrittenAbilityData,
    queryCachedFilesAbilityData,
    queryCompressedAbilityData,
    queryCpuAbilityData,
    queryCPuAbilityMaxData,
    queryCpuAbilitySystemData,
    queryCpuAbilityUserData,
    queryCpuData,
    queryCpuFreq,
    queryCpuFreqData,
    queryCpuMax,
    queryCpuMaxFreq,
    queryDataDICT,
    queryDiskIoMaxData,
    queryEventCountMap,
    queryHeapGroupByEvent,
    queryMemoryMaxData,
    queryMemoryUsedAbilityData,
    queryNativeHookProcess,
    queryNetWorkMaxData,
    queryPacketsInAbilityData,
    queryPacketsOutAbilityData,
    queryProcess,
    queryProcessAsyncFunc,
    queryProcessAsyncFuncCount,
    queryProcessByTable,
    queryProcessData,
    queryProcessMem,
    queryProcessMemData,
    queryProcessThreads,
    queryProcessThreadsByTable,
    queryReadAbilityData,
    querySearchFunc,
    queryThreadData,
    queryTotalTime,
    queryWrittenAbilityData,
    threadPool
} from "../database/SqlLite.js";
import {TraceRow} from "./trace/base/TraceRow.js";
import {TimerShaftElement} from "./trace/TimerShaftElement.js";
import {CpuStruct} from "../bean/CpuStruct.js";
import {CpuFreqStruct} from "../bean/CpuFreqStruct.js";
import {ProcessStruct} from "../bean/ProcessStruct.js";
import {ColorUtils} from "./trace/base/ColorUtils.js";
import "./trace/base/TraceSheet.js";
import {TraceSheet} from "./trace/base/TraceSheet.js";
import {ThreadStruct} from "../bean/ThreadStruct.js";
import {ProcessMemStruct} from "../bean/ProcessMemStruct.js";
import {FuncStruct} from "../bean/FuncStruct.js";
import {FpsStruct} from "../bean/FpsStruct.js";
import {RangeSelect} from "./trace/base/RangeSelect.js";
import {SelectionParam} from "../bean/BoxSelection.js";
import {HeapStruct} from "../bean/HeapStruct.js";
import {procedurePool} from "../database/Procedure.js";
import {Utils} from "./trace/base/Utils.js";
import {SpApplication} from "../SpApplication.js";
import {SPT, SptSlice, ThreadProcess, ThreadState} from "../bean/StateProcessThread.js";
import {HeapTreeDataBean} from "../bean/HeapTreeDataBean.js";
import {Flag} from "./trace/timer-shaft/Flag.js";
import {SportRuler} from "./trace/timer-shaft/SportRuler.js";
import {NativeEvent, NativeEventHeap, NativeHookCallInfo} from "../bean/NativeHook.js";
import {CpuAbilityMonitorStruct} from "../bean/CpuAbilityMonitorStruct.js";
import {MemoryAbilityMonitorStruct} from "../bean/MemoryAbilityMonitorStruct.js";
import {DiskAbilityMonitorStruct} from "../bean/DiskAbilityMonitorStruct.js";
import {NetworkAbilityMonitorStruct} from "../bean/NetworkAbilityMonitorStruct.js";
import {SpHiPerf} from "./hiperf/SpHiPerf.js";
import {perfDataQuery} from "./hiperf/PerfDataQuery.js";
import {SearchThreadProcessBean} from "../bean/SearchFuncBean.js";
import {info} from "../../log/Log.js";
import {ns2x} from "../database/ProcedureWorkerCommon.js";

@element('sp-system-trace')
export class SpSystemTrace extends BaseElement {
    static scrollViewWidth = 0
    static isCanvasOffScreen = true;
    static SPT_DATA: Array<SPT> = [];
    static DATA_DICT: Map<number, string> = new Map<number, string>();
    static EVENT_HEAP: Array<NativeEventHeap> = [];
    static NATIVE_MEMORY_DATA: Array<NativeEvent> = [];
    rowsEL: HTMLDivElement | undefined | null;
    spacerEL: HTMLDivElement | undefined | null;
    visibleRows: Array<TraceRow<any>> = [];
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
    private processThreads: Array<ThreadStruct> = []
    private processAsyncEvent: Array<ProcessMemStruct> = []
    private processMem: Array<any> = []
    private processAsyncFuncMap: any = {}
    private spHiPerf: SpHiPerf | undefined | null;
    private loadTraceCompleted: boolean = false;
    private eventCountMap: any;

    initElements(): void {
        this.rowsEL = this.shadowRoot?.querySelector<HTMLDivElement>('.rows');
        this.spacerEL = this.shadowRoot?.querySelector<HTMLDivElement>('.spacer');
        this.timerShaftEL = this.shadowRoot?.querySelector('.timer-shaft');
        this.traceSheetEL = this.shadowRoot?.querySelector('.trace-sheet');
        this.rangeSelect = new RangeSelect(this.timerShaftEL);
        this.rangeSelect.rowsEL = this.rowsEL;
        document?.addEventListener("triangle-flag", (event: any) => {
            let temporaryTime = this.timerShaftEL?.drawTriangle(event.detail.time, event.detail.type);
            if (event.detail.timeCallback && temporaryTime) event.detail.timeCallback(temporaryTime);
        })

        document?.addEventListener("flag-change", (event: any) => {
            this.timerShaftEL?.modifyFlagList(event.detail)
            if (event.detail.hidden) {
                this.selectFlag = undefined;
                this.traceSheetEL?.setAttribute("mode", 'hidden');
                this.visibleRows.forEach(it => it.draw(true));
            }
        })

        SpSystemTrace.scrollViewWidth = this.getScrollWidth();
        this.rangeSelect.selectHandler = (rows, refreshCheckBox) => {
            if (rows.length == 0) {
                this.rowsEL!.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => {
                    it.checkType = "-1"
                })
                this.getVisibleRows().forEach(it => {
                    it.draw(true);
                });
                this.traceSheetEL?.setAttribute("mode", 'hidden');
                return;
            }
            if (refreshCheckBox) {
                if (rows.length > 0) {
                    this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach(row => row.checkType = "0")
                    rows.forEach(it => it.checkType = "2")
                } else {
                    this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach(row => row.checkType = "-1")
                    return
                }
            }
            let selection = new SelectionParam();
            selection.cpus = [];
            selection.threadIds = [];
            selection.funTids = [];
            selection.trackIds = [];
            selection.leftNs = 0;
            selection.rightNs = 0;
            let native_memory = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"];
            rows.forEach(it => {
                if (it.rowType == TraceRow.ROW_TYPE_CPU) {
                    selection.cpus.push(parseInt(it.rowId!))
                    info("load CPU traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_PROCESS) {
                    this.rowsEL?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
                        th.rangeSelect = true;
                        th.checkType = "2"
                        selection.threadIds.push(parseInt(th.rowId!))
                    })
                    info("load process traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_NATIVE_MEMORY) {
                    this.rowsEL?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
                        th.rangeSelect = true;
                        th.checkType = "2"
                        selection.nativeMemory.push(th.rowId!);
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
                } else if (it.rowType == TraceRow.ROW_TYPE_MEM) {
                    selection.trackIds.push(parseInt(it.rowId!))
                    info("load memory traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_FPS) {
                    selection.hasFps = true;
                    info("load FPS traceRow id is : ", it.rowId)
                } else if (it.rowType == TraceRow.ROW_TYPE_HEAP) {
                    if (native_memory.indexOf(it.rowId ?? "") != -1) {
                        selection.nativeMemory.push(it.rowId!);
                        info("load nativeMemory traceRow id is : ", it.rowId)
                    } else {
                        selection.heapIds.push(parseInt(it.rowId!))
                        info("load heap traceRow id is : ", it.rowId)
                    }
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
                } else if (it.rowType?.startsWith("hiperf")) {
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_EVENT || it.rowType == TraceRow.ROW_TYPE_HIPERF_REPORT) {
                        return;
                    }
                    selection.perfSampleIds.push(1)
                    if (it.rowType == TraceRow.ROW_TYPE_HIPERF_PROCESS) {
                        this.rowsEL?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${it.rowId}']`).forEach(th => {
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
                }
            })
            selection.leftNs = TraceRow.rangeSelectObject?.startNS || 0;
            selection.rightNs = TraceRow.rangeSelectObject?.endNS || 0;
            this.traceSheetEL?.boxSelection(selection);
        }
        // @ts-ignore
        new ResizeObserver((entries) => {
            let width = entries[0].contentRect.width - 1 - SpSystemTrace.scrollViewWidth;
            requestAnimationFrame(() => {
                this.timerShaftEL?.updateWidth(width)
                this.shadowRoot!.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => it.updateWidth(width))
            })
        }).observe(this);

        new ResizeObserver((entries) => {
            this.getVisibleRows().forEach(it => {
                it.draw(true);
            });
            if (this.traceSheetEL!.getAttribute("mode") == "hidden") {
                this.timerShaftEL?.removeTriangle("triangle")
            }
        }).observe(this.rowsEL!);
        this.spHiPerf = new SpHiPerf(this);
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

    getVisibleRows(): Array<TraceRow<any>> {
        let scrollTop = this.rowsEL?.scrollTop || 0;
        let scrollHeight = this.rowsEL?.clientHeight || 0;
        let res = [...this.rowsEL!.querySelectorAll<TraceRow<any>>("trace-row")].filter((it) => {
            let tr = (it as TraceRow<any>);
            let top = it.offsetTop - (this.rowsEL?.offsetTop || 0);
            if ((top + it.clientHeight > scrollTop && top + it.clientHeight < scrollTop + scrollHeight + it.clientHeight) || it.collect) {
                it.sleeping = false;
                return true
            } else {
                if (!it.hasAttribute("collect-type")) {
                    it.sleeping = true;
                }
                return false;
            }
        })
        this.visibleRows = res;
        info("Visible TraceRow size is :", this.visibleRows!.length)
        return res;
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
        this.visibleRows.forEach(it => it.draw(true));
    }

    timerShaftELRangeChange = (e: any) => {
        TraceRow.range = e;
        if (TraceRow.rangeSelectObject) {
            TraceRow.rangeSelectObject!.startX = Math.floor(ns2x(TraceRow.rangeSelectObject!.startNS!, TraceRow.range?.startNS!, TraceRow.range?.endNS!, TraceRow.range?.totalNS!, this.timerShaftEL!.sportRuler!.frame));
            TraceRow.rangeSelectObject!.endX = Math.floor(ns2x(TraceRow.rangeSelectObject!.endNS!, TraceRow.range?.startNS!, TraceRow.range?.endNS!, TraceRow.range?.totalNS!, this.timerShaftEL!.sportRuler!.frame));
        }
        //在rowsEL显示范围内的 trace-row组件将收到时间区间变化通知
        for (let i = 0; i < this.visibleRows.length; i++) {
            this.visibleRows[i].draw();
        }
    }

    rowsElOnScroll = (e: any) => {
        this.hoverStructNull();
        if (TraceRow.range) {
            TraceRow.range.refresh = true;
        }
        this.visibleRows = this.getVisibleRows();
        for (let index = 0; index < this.visibleRows.length; index++) {
            if (index == 0 || index == this.visibleRows.length - 1) {
                this.visibleRows[index].isHover = false;
            }
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
            }
            this.visibleRows.forEach(it => it.draw());
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
        if (this.isMouseInSheet(ev)) return;
        if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft) {
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
        this.getVisibleRows();
    }

    selectChangeHandler = (rows: Array<TraceRow<any>>) => {
        this.rangeSelect.rangeTraceRow = rows;
        this.rangeSelect.selectHandler?.(this.rangeSelect.rangeTraceRow, false);
    }

    documentOnMouseMove = (ev: MouseEvent) => {
        if (!this.loadTraceCompleted) return;
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
            for (let i = 0; i < rows.length; i++) {
                rows[i].tipEL!.style.display = "none";
                rows[i].draw(true);
            }
        } else {
            for (let i = 0; i < rows.length; i++) {
                let tr = rows[i];
                let rowsELScrollTop = this.rowsEL?.scrollTop || 0;
                let x = ev.offsetX - (tr.canvasContainer?.offsetLeft || 0);
                let y = ev.offsetY - (tr.canvasContainer?.offsetTop || 0) + rowsELScrollTop;
                if ((!tr.collect && x > tr.frame.x && x < tr.frame.x + tr.frame.width && ev.offsetY + rowsELScrollTop > tr.offsetTop && ev.offsetY + rowsELScrollTop < tr.offsetTop + tr.frame.height) ||
                    (tr.collect && x > tr.frame.x && x < tr.frame.x + tr.frame.width && ev.offsetY > tr.offsetTop - 48 && ev.offsetY < tr.offsetTop - 48 + tr.frame.height)) {
                    tr.isHover = true;
                    tr.hoverX = x;
                    tr.hoverY = tr.collect ? (ev.offsetY + 48 - tr.offsetTop) : y;
                    if (tr.rowType === TraceRow.ROW_TYPE_CPU) {
                        this.currentRowType = TraceRow.ROW_TYPE_CPU;
                        if (CpuStruct.hoverCpuStruct) {
                            tr.tip = `<span>P：${CpuStruct.hoverCpuStruct.processName || "Process"} [${CpuStruct.hoverCpuStruct.processId}]</span><span>T：${CpuStruct.hoverCpuStruct.name} [${CpuStruct.hoverCpuStruct.tid}]</span>`;
                        }
                        tr.setTipLeft(x, CpuStruct.hoverCpuStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_CPU_FREQ) {
                        this.currentRowType = TraceRow.ROW_TYPE_CPU_FREQ;
                        if (CpuFreqStruct.hoverCpuFreqStruct) {
                            CpuStruct.hoverCpuStruct = undefined;
                            tr.tip = `<span>${ColorUtils.formatNumberComma(CpuFreqStruct.hoverCpuFreqStruct.value!)} kHz</span>`
                        }
                        tr.setTipLeft(x, CpuFreqStruct.hoverCpuFreqStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_FPS) {
                        this.currentRowType = TraceRow.ROW_TYPE_FPS;
                        if (FpsStruct.hoverFpsStruct) {
                            tr.tip = `<span>${FpsStruct.hoverFpsStruct.fps}</span>`
                        }
                        tr.setTipLeft(x, FpsStruct.hoverFpsStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_THREAD) {
                        FuncStruct.hoverFuncStruct = undefined;
                        this.currentRowType = TraceRow.ROW_TYPE_THREAD;
                    } else if (tr.rowType === TraceRow.ROW_TYPE_FUNC) {
                        ThreadStruct.hoverThreadStruct = undefined;
                        this.currentRowType = TraceRow.ROW_TYPE_FUNC;
                    } else if (tr.rowType === TraceRow.ROW_TYPE_HEAP) {
                        this.currentRowType = TraceRow.ROW_TYPE_HEAP;
                        if (HeapStruct.hoverHeapStruct) {
                            if (tr.drawType === 1) {
                                tr.tip = `<span>${HeapStruct.hoverHeapStruct.heapsize}</span>`
                            } else {
                                tr.tip = `<span>${Utils.getByteWithUnit(HeapStruct.hoverHeapStruct.heapsize!)}</span>`
                            }
                        }
                        tr.setTipLeft(x, HeapStruct.hoverHeapStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_CPU_ABILITY) {
                        this.currentRowType = TraceRow.ROW_TYPE_CPU_ABILITY;
                        if (!SpSystemTrace.isCanvasOffScreen) CpuAbilityMonitorStruct.hoverCpuAbilityStruct = tr.onMouseHover(x, y);
                        if (CpuAbilityMonitorStruct.hoverCpuAbilityStruct) {
                            let monitorCpuTip = (CpuAbilityMonitorStruct.hoverCpuAbilityStruct.value!).toFixed(2) + "%"
                            tr.tip = `<span>${monitorCpuTip}</span>`
                        }
                        tr.setTipLeft(x, CpuAbilityMonitorStruct.hoverCpuAbilityStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_MEMORY_ABILITY) {
                        this.currentRowType = TraceRow.ROW_TYPE_MEMORY_ABILITY;
                        if (!SpSystemTrace.isCanvasOffScreen) MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct = tr.onMouseHover(x, y);
                        if (MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct) {
                            tr.tip = `<span>${Utils.getBinaryByteWithUnit(MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct.value!)}</span>`
                        }
                        tr.setTipLeft(x, MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_DISK_ABILITY) {
                        this.currentRowType = TraceRow.ROW_TYPE_DISK_ABILITY;
                        if (!SpSystemTrace.isCanvasOffScreen) DiskAbilityMonitorStruct.hoverDiskAbilityStruct = tr.onMouseHover(x, y);
                        if (DiskAbilityMonitorStruct.hoverDiskAbilityStruct) {
                            tr.tip = `<span>${DiskAbilityMonitorStruct.hoverDiskAbilityStruct.value!} KB/S</span>`
                        }
                        tr.setTipLeft(x, DiskAbilityMonitorStruct.hoverDiskAbilityStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_NETWORK_ABILITY) {
                        this.currentRowType = TraceRow.ROW_TYPE_NETWORK_ABILITY;
                        if (!SpSystemTrace.isCanvasOffScreen) NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = tr.onMouseHover(x, y);
                        if (NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct) {
                            tr.tip = `<span>${Utils.getBinaryByteWithUnit(NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct.value!)}</span>`
                        }
                        tr.setTipLeft(x, NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_HIPERF_CPU) {
                        this.currentRowType = TraceRow.ROW_TYPE_HIPERF_CPU;
                        if (SpHiPerf.hoverCpuStruct) {
                            let num = Math.trunc((SpHiPerf.hoverCpuStruct.height || 0) / 40 * 100);
                            if (num > 0) {
                                if (tr.rowId == "HiPerf-cpu-merge") {
                                    tr.tip = `<span>${num * (this.spHiPerf!.maxCpuId + 1)}% (10.00ms)</span>`
                                } else {
                                    tr.tip = `<span>${num}% (10.00ms)</span>`
                                }
                            } else {
                                let perfCall = perfDataQuery.callChainMap.get(SpHiPerf.hoverCpuStruct.sample_id || 0);
                                tr.tip = `<span>${perfCall ? perfCall.name : ''} (${perfCall ? perfCall.depth : '0'} other frames)</span>`
                            }
                        }
                        tr.setTipLeft(x, SpHiPerf.hoverCpuStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_HIPERF_EVENT) {
                        this.currentRowType = TraceRow.ROW_TYPE_HIPERF_EVENT;
                        if (SpHiPerf.hoverEventuctStruct) {
                            let num = Math.trunc((SpHiPerf.hoverEventuctStruct.sum || 0) / (SpHiPerf.hoverEventuctStruct.max || 0) * 100);
                            if (num > 0) {
                                tr.tip = `<span>${num}% (10.00ms)</span>`
                            } else {
                                let perfCall = perfDataQuery.callChainMap.get(SpHiPerf.hoverEventuctStruct.sample_id || 0);
                                tr.tip = `<span>${perfCall ? perfCall.name : ''} (${perfCall ? perfCall.depth : '0'} other frames)</span>`
                            }
                        }
                        tr.setTipLeft(x, SpHiPerf.hoverEventuctStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_HIPERF_PROCESS) {
                        this.currentRowType = TraceRow.ROW_TYPE_HIPERF_PROCESS;
                        if (SpHiPerf.hoverProcessStruct) {
                            let num = Math.trunc((SpHiPerf.hoverProcessStruct.height || 0) / 40 * 100);
                            if (num > 0) {
                                tr.tip = `<span>${num}% (10.00ms)</span>`
                            } else {
                                let perfCall = perfDataQuery.callChainMap.get(SpHiPerf.hoverProcessStruct.sample_id || 0);
                                tr.tip = `<span>${perfCall ? perfCall.name : ''} (${perfCall ? perfCall.depth : '0'} other frames)</span>`
                            }
                        }
                        tr.setTipLeft(x, SpHiPerf.hoverProcessStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_HIPERF_THREAD) {
                        this.currentRowType = TraceRow.ROW_TYPE_HIPERF_THREAD;
                        if (SpHiPerf.hoverThreadStruct) {
                            let num = Math.trunc((SpHiPerf.hoverThreadStruct.height || 0) / 40 * 100);
                            if (num > 0) {
                                tr.tip = `<span>${num}% (10.00ms)</span>`
                            } else {
                                let perfCall = perfDataQuery.callChainMap.get(SpHiPerf.hoverThreadStruct.sample_id || -1);
                                tr.tip = `<span>${perfCall ? perfCall.name : ''} (${perfCall ? perfCall.depth : '0'} other frames)</span>`
                            }
                        }
                        tr.setTipLeft(x, SpHiPerf.hoverThreadStruct)
                    } else if (tr.rowType == TraceRow.ROW_TYPE_MEM) {
                        if (ProcessMemStruct.hoverProcessMemStruct) {
                            CpuStruct.hoverCpuStruct = undefined;
                            tr.tip = `<span>${ProcessMemStruct.hoverProcessMemStruct.value}</span>`
                        }
                        tr.setTipLeft(x, ProcessMemStruct.hoverProcessMemStruct)
                    } else {
                        this.hoverStructNull();
                    }
                    if (tr.isComplete) {
                        tr.draw(true);
                    }
                } else {
                    tr.onMouseLeave(x, y);
                    tr.isHover = false;
                    tr.hoverX = x;
                    tr.hoverY = y;
                }

            }
            if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft!
                && ev.offsetX < this.timerShaftEL!.canvas!.offsetLeft! + this.timerShaftEL!.canvas!.offsetWidth!
                && ev.offsetY > this.rowsEL!.offsetTop
                && ev.offsetY < this.rowsEL!.offsetTop + this.rowsEL!.offsetHeight
            ) {
            } else {
                this.hoverStructNull();
                for (let i = 0, len = rows.length; i < len; i++) {
                    if (!(rows[i].rowType === TraceRow.ROW_TYPE_PROCESS) && this.currentRowType === rows[i].rowType) { //
                        if (rows[i].isComplete) {
                            rows[i].draw(true);
                        }
                    }
                }
            }
        }
    }

    hoverStructNull() {
        CpuStruct.hoverCpuStruct = undefined;
        CpuFreqStruct.hoverCpuFreqStruct = undefined;
        ThreadStruct.hoverThreadStruct = undefined;
        FuncStruct.hoverFuncStruct = undefined;
        SpHiPerf.hoverCpuStruct = undefined;
    }

    selectStructNull() {
        CpuStruct.selectCpuStruct = undefined;
        CpuStruct.wakeupBean = null;
        CpuFreqStruct.selectCpuFreqStruct = undefined;
        ThreadStruct.selectThreadStruct = undefined;
        FuncStruct.selectFuncStruct = undefined;
        SpHiPerf.selectCpuStruct = undefined;
    }

    documentOnClick = (ev: MouseEvent) => {
        if (!this.loadTraceCompleted) return;
        if (this.isMouseInSheet(ev)) return;
        if (this.rangeSelect.isDrag()) {
            return;
        }
        let x = ev.offsetX - this.timerShaftEL!.canvas!.offsetLeft;
        let y = ev.offsetY;
        if (this.timerShaftEL!.sportRuler!.frame.contains(x, y) && x > (TraceRow.rangeSelectObject?.startX || 0) && x < (TraceRow.rangeSelectObject?.endX || 0)) {
        } else {
            this.onClickHandler();
            this.documentOnMouseMove(ev)
        }
    }

    onClickHandler() {
        if (!this.loadTraceCompleted) return;
        this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => it.rangeSelect = false)
        this.selectStructNull();
        let threadClickHandler: any;
        let cpuClickHandler: any;
        threadClickHandler = (d: ThreadStruct) => {
            this.observerScrollHeightEnable = false;
            this.goProcess(`${d.cpu}`, "", "cpu", true);
            let cpuRow = this.rowsEL?.querySelectorAll<TraceRow<CpuStruct>>(`trace-row[row-id='${d.cpu}'][row-type='cpu']`)[0];
            let findEntry = cpuRow!.dataList!.find((dat) => dat.startTime === d.startTime);
            if (findEntry!.startTime! + findEntry!.dur! < TraceRow.range!.startNS || findEntry!.startTime! > TraceRow.range!.endNS) {
                this.timerShaftEL?.setRangeNS(findEntry!.startTime! - findEntry!.dur! * 2, findEntry!.startTime! + findEntry!.dur! + findEntry!.dur! * 2);
            }
            this.hoverStructNull();
            this.selectStructNull();
            CpuStruct.hoverCpuStruct = findEntry;
            CpuStruct.selectCpuStruct = findEntry;
            this.timerShaftEL?.drawTriangle(findEntry!.startTime || 0, "inverted");
            cpuRow!.draw();
            this.traceSheetEL?.displayCpuData(CpuStruct.selectCpuStruct!, (wakeUpBean) => {
                CpuStruct.wakeupBean = wakeUpBean;
                this.visibleRows.forEach(it => it.draw());
            }, cpuClickHandler);
        }

        cpuClickHandler = (d: CpuStruct) => {
            this.observerScrollHeightEnable = true;
            let threadRow = this.rowsEL?.querySelectorAll<TraceRow<ThreadStruct>>(`trace-row[row-id='${d.tid}'][row-type='thread']`)[0];
            let task = () => {
                if (threadRow!.isComplete) {
                    let findEntry = threadRow!.dataList!.find((dat) => dat.startTime === d.startTime);
                    if (findEntry!.startTime! + findEntry!.dur! < TraceRow.range!.startNS || findEntry!.startTime! > TraceRow.range!.endNS) {
                        this.timerShaftEL?.setRangeNS(findEntry!.startTime! - findEntry!.dur! * 2, findEntry!.startTime! + findEntry!.dur! + findEntry!.dur! * 2);
                    }
                    this.hoverStructNull();
                    this.selectStructNull();
                    ThreadStruct.hoverThreadStruct = findEntry;
                    ThreadStruct.selectThreadStruct = findEntry;
                    this.timerShaftEL?.drawTriangle(findEntry!.startTime || 0, "inverted");
                    threadRow!.draw();
                    this.traceSheetEL?.displayThreadData(ThreadStruct.selectThreadStruct!, threadClickHandler, cpuClickHandler);
                    this.goProcess(`${d.tid}`, `${d.processId}`, "thread", true)
                } else {
                    threadRow!.onComplete = () => {
                        let findEntry = threadRow!.dataList!.find((dat) => dat.startTime === d.startTime);
                        if (findEntry!.startTime! + findEntry!.dur! < TraceRow.range!.startNS || findEntry!.startTime! > TraceRow.range!.endNS) {
                            this.timerShaftEL?.setRangeNS(findEntry!.startTime! - findEntry!.dur! * 2, findEntry!.startTime! + findEntry!.dur! + findEntry!.dur! * 2);
                        }
                        this.hoverStructNull();
                        this.selectStructNull();
                        ThreadStruct.hoverThreadStruct = findEntry;
                        ThreadStruct.selectThreadStruct = findEntry;
                        this.timerShaftEL?.drawTriangle(findEntry!.startTime || 0, "inverted");
                        threadRow!.draw();
                        this.traceSheetEL?.displayThreadData(ThreadStruct.selectThreadStruct!, threadClickHandler, cpuClickHandler);
                        this.goProcess(`${d.tid}`, `${d.processId}`, "thread", false);
                    }
                }
            }
            this.observerScrollHeightCallback = () => task();
            this.goProcess(`${d.tid}`, `${d.processId}`, "thread", true);
            task();
        }

        if (CpuStruct.hoverCpuStruct) {
            CpuStruct.selectCpuStruct = CpuStruct.hoverCpuStruct
            this.timerShaftEL?.drawTriangle(CpuStruct.selectCpuStruct!.startTime || 0, "inverted");
            this.traceSheetEL?.displayCpuData(CpuStruct.selectCpuStruct, (wakeUpBean) => {
                CpuStruct.wakeupBean = wakeUpBean;
                this.visibleRows.forEach(it => it.draw());
            }, cpuClickHandler);
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (ThreadStruct.hoverThreadStruct) {
            ThreadStruct.selectThreadStruct = ThreadStruct.hoverThreadStruct;
            this.timerShaftEL?.drawTriangle(ThreadStruct.selectThreadStruct!.startTime || 0, "inverted");
            this.traceSheetEL?.displayThreadData(ThreadStruct.selectThreadStruct, threadClickHandler, cpuClickHandler);
            this.timerShaftEL?.modifyFlagList(undefined);
        } else if (FuncStruct.hoverFuncStruct) {
            FuncStruct.selectFuncStruct = FuncStruct.hoverFuncStruct;
            this.timerShaftEL?.drawTriangle(FuncStruct.selectFuncStruct!.startTs || 0, "inverted");
            this.traceSheetEL?.displayFuncData(FuncStruct.hoverFuncStruct)
            this.timerShaftEL?.modifyFlagList(undefined);
        } else {
            this.observerScrollHeightEnable = false;
            this.selectFlag = null;
            this.timerShaftEL?.removeTriangle("inverted");
            if (!SportRuler.isMouseInSportRuler) {
                this.traceSheetEL?.setAttribute("mode", 'hidden');
                this.getVisibleRows().forEach(it => it.draw(true));
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
        this.rowsEL?.addEventListener('scroll', this.rowsElOnScroll)
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
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row:not([sleeping])`).forEach(it => {
                this.hoverStructNull();
                it.draw();
            })
        }
    }

    goProcess(rowId: string, rowParentId: string, rowType: string, smooth: boolean = true) {
        let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowParentId}'][folder]`);
        if (row) {
            row.expansion = true
        }
        let rootRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
        this.rowsEL!.scroll({
            top: (rootRow?.offsetTop || 0) - this.rowsEL!.offsetTop - this.rowsEL!.offsetHeight + (rootRow?.offsetHeight || 0),
            left: 0,
            behavior: smooth ? "smooth" : undefined
        })
    }

    goFunction(rowId: string, rowParentId: string, rowType: string, smooth: boolean = true, afterScroll: any) {
        let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowParentId}'][folder]`);
        if (row) {
            row.expansion = true
        }
        let funcRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
        if (funcRow == null) {
            let threadRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='thread']`);
            this.rowsEL!.scroll({
                top: threadRow!.offsetTop - this.rowsEL!.offsetTop - this.rowsEL!.offsetHeight + threadRow!.offsetHeight + threadRow!.offsetHeight,
                left: 0,
                behavior: smooth ? "smooth" : undefined
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
            if (this.rowsEL!.scrollTop === fixedOffset) {
                this.rowsEL!.removeEventListener('scroll', onScroll)
                callback()
            }
        }

        this.rowsEL!.addEventListener('scroll', onScroll)
        onScroll()
        this.rowsEL!.scrollTo({
            top: offset,
            behavior: 'smooth'
        })
    }

    disconnectedCallback() {
        this.timerShaftEL?.removeEventListener('range-change', this.timerShaftELRangeChange);
        this.rowsEL?.removeEventListener('scroll', this.rowsElOnScroll);
        this.removeEventListener('mousemove', this.documentOnMouseMove);
        this.removeEventListener('click', this.documentOnClick);
        this.removeEventListener('mousedown', this.documentOnMouseDown)
        this.removeEventListener('mouseup', this.documentOnMouseUp)
        this.removeEventListener('mouseout', this.documentOnMouseOut)
        document.removeEventListener('keypress', this.documentOnKeyPress)
        document.removeEventListener('keyup', this.documentOnKeyUp)
    }

    loadDatabaseUrl(url: string, progress: Function, complete?: ((res: { status: boolean, msg: string }) => void) | undefined) {
        this.observerScrollHeightEnable = false;
        this.init({url: url}, progress).then((res) => {
            if (complete) {
                complete(res);
            }
        })
    }

    loadDatabaseArrayBuffer(buf: ArrayBuffer, progress: ((name: string, percent: number) => void), complete?: ((res: { status: boolean, msg: string }) => void) | undefined) {
        this.observerScrollHeightEnable = false;
        this.init({buf}, progress).then((res) => {
            let scrollTop = this.rowsEL?.scrollTop || 0
            let scrollHeight = this.rowsEL?.clientHeight || 0
            this.rowsEL?.querySelectorAll("trace-row").forEach((it: any) => {
                this.observer.observe(it);
            })
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
        this.getVisibleRows().forEach(it => it.rowHidden = false && it.draw(true))
    }

    searchCPU(query: string): Array<CpuStruct> {
        let searchResults: Array<CpuStruct> = []
        this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu']`).forEach(item => {
            let res = item!.dataList!.filter(it => (it.name && it.name.search(query) >= 0) || it.tid == query
                || it.processId == query
                || (it.processName && it.processName.search(query) >= 0)
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

    searchThreadsAndProcesses(query: string): Array<any> {
        let searchResults: Array<any> = []
        this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='thread'][row-type='process']`).forEach(item => {
            if (item!.name.search(query) >= 0) {
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
        this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row`).forEach(item => {
            item.highlight = false;
            if (!item.sleeping) {
                item.draw(true)
            }
        })
        if (findEntry.type == 'thread') {
            CpuStruct.selectCpuStruct = findEntry;
            CpuStruct.hoverCpuStruct = CpuStruct.selectCpuStruct;
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu']`).forEach(item => {
                item.highlight = item.rowId == `${findEntry.cpu}`;
                item.draw(true)
            })
            this.goProcess(`${findEntry.cpu}`, "", "cpu", true)
            this.onClickHandler();
        } else if (findEntry.type == "func") {
            this.observerScrollHeightEnable = true;
            this.goFunction(`${findEntry.tid}`, `${findEntry.pid}`, findEntry.type, true, () => {
                let funcRow = this.rowsEL?.querySelector<TraceRow<FuncStruct>>(`trace-row[row-id='${findEntry.tid}'][row-type='func']`);
                if (funcRow == null) return
                this.goProcess(`${findEntry.tid}`, `${findEntry.pid}`, "func", true)
                funcRow!.highlight = true
                let completeEntry = () => {
                    if (funcRow == null) return
                    let searchEntry = funcRow!.dataList!.find((dat) => dat.startTs === findEntry.startTime);
                    this.hoverStructNull();
                    this.selectStructNull();
                    FuncStruct.hoverFuncStruct = searchEntry;
                    FuncStruct.selectFuncStruct = searchEntry;
                    this.visibleRows.forEach(it => it.draw());
                }
                let scrollTimer: any;
                this.observerScrollHeightCallback = () => {
                    funcRow = this.rowsEL?.querySelector<TraceRow<FuncStruct>>(`trace-row[row-id='${findEntry.tid}'][row-type='func']`);
                    if (funcRow == null) {
                        return
                    }
                    if (funcRow!.isComplete) {
                        completeEntry()
                        this.onClickHandler();
                        this.goProcess(`${findEntry.tid}`, `${findEntry.pid}`, "func", false)
                    } else {
                        funcRow!.onComplete = () => {
                            completeEntry()
                            this.onClickHandler();
                            clearTimeout(scrollTimer);
                            scrollTimer = setTimeout(() => this.goProcess(`${findEntry.tid}`, `${findEntry.pid}`, "func", false), 100)
                        }
                    }
                }
                if (funcRow?.isComplete) {
                    completeEntry()
                    this.onClickHandler();
                    this.goProcess(`${findEntry.tid}`, `${findEntry.pid}`, "func", true)
                }
            });
        } else if (findEntry.type == "thread||process") {
            let threadProcessRow = this.rowsEL?.querySelectorAll<TraceRow<ThreadStruct>>(`trace-row[row-id='${findEntry.rowId}'][row-type='${findEntry.rowType}']`)[0];
            threadProcessRow!.highlight = true
            this.goProcess(`${findEntry.rowId}`, `${findEntry.rowParentId}`, findEntry.rowType, true);
            let completeEntry = () => {
                let searchEntry = threadProcessRow!.dataList!.find((dat) => dat.startTime === findEntry.startTime);
                this.hoverStructNull();
                this.selectStructNull();

                ThreadStruct.hoverThreadStruct = searchEntry;
                ThreadStruct.selectThreadStruct = searchEntry;
                threadProcessRow!.draw();
            }
            let scrollTimer: any;
            this.observerScrollHeightCallback = () => {
                if (threadProcessRow!.isComplete) {
                    completeEntry()
                    this.goProcess(`${findEntry.rowId}`, `${findEntry.rowParentId}`, findEntry.rowType, true)
                } else {
                    threadProcessRow!.onComplete = () => {
                        completeEntry()
                        clearTimeout(scrollTimer);
                        scrollTimer = setTimeout(() => this.goProcess(`${findEntry.rowId}`, `${findEntry.rowParentId}`, findEntry.rowType, false), 100)
                    }
                }
            }
        }
        this.timerShaftEL?.drawTriangle(findEntry.startTime || 0, "inverted");
        return findIndex;
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
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu']`).forEach(item => {
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
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu']`).forEach(item => {
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
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu']`).forEach(item => {
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
            this.rowsEL!.querySelectorAll<TraceRow<any>>(`trace-row[row-type='cpu']`).forEach(item => {
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
        this.loadTraceCompleted = false;
        if (this.rowsEL) this.rowsEL.innerHTML = ''
        this.spacerEL!.style.height = '0px';
        this.rangeSelect.rangeTraceRow = [];
        CpuStruct.wakeupBean = undefined;
        this.selectStructNull();
        this.hoverStructNull();
        this.traceSheetEL?.setAttribute("mode", "hidden")
        progress && progress("rest timershaft", 8);
        this.timerShaftEL?.reset();
        progress && progress("clear cache", 10);
        procedurePool.clearCache();
    }

    init = async (param: { buf?: ArrayBuffer, url?: string }, progress: Function) => {
        progress("Load database", 6);
        this.reset(progress);
        if (param.buf) {
            let {status, msg} = await threadPool.initSqlite(param.buf, progress);
            if (!status) {
                return {status: false, msg: msg}
            }
        }
        if (param.url) {
            let {status, msg} = await threadPool.initServer(param.url, progress);
            if (!status) {
                return {status: false, msg: msg}
            }
        }

        progress("load process threads", 50);
        let eventCountList: Array<any> = await queryEventCountMap();
        this.eventCountMap = eventCountList.reduce((pre, current, i, array) => {
            pre[`${current.eventName}`] = current.count;
            return pre;
        }, {});
        let queryProcessThreadResult = await queryProcessThreads();
        let queryProcessThreadsByTableResult = await queryProcessThreadsByTable()
        this.processThreads = Utils.removeDuplicates(queryProcessThreadResult, queryProcessThreadsByTableResult, "tid")
        SpSystemTrace.DATA_DICT.clear();
        let dict = await queryDataDICT();
        dict.map((d) => SpSystemTrace.DATA_DICT.set(d['id'], d['data']));
        info("The amount of initialized process threads data is : ", this.processThreads!.length)
        progress("process memory", 60);
        this.processMem = await queryProcessMem()
        info("The amount of initialized process memory data is : ", this.processMem!.length)
        progress("async event", 63);
        this.processAsyncEvent = await getAsyncEvents()
        info("The amount of initialized process Event data is : ", this.processAsyncEvent!.length)
        progress("time range", 65);
        await this.initTotalTime();
        info("timerShaftEL Data initialized")
        progress("cpu", 70);
        await this.initCpu();
        info("cpu Data initialized")
        progress("cpu rate", 75);
        await this.initCpuRate();
        info("Cpu Rate Data initialized")
        progress("cpu freq", 80);
        await this.initCpuFreq();
        info("Cpu Freq Data initialized")
        progress("fps", 85);
        await this.initFPS();
        info("FPS Data initialized")
        progress("native memory", 87);
        await this.initNativeMemory();
        info("Native Memory Data initialized")
        progress("ability monitor", 88);
        await this.initAbilityMonitor();
        info("Ability Monitor Data initialized")
        await perfDataQuery.initPerfCache()
        info("Perf Files Data initialized")
        await this.spHiPerf!.init();
        info("HiPerf Data initialized")
        progress("process", 90);
        await this.initAsyncFuncData()
        await this.initProcess();
        info("Process Data initialized")
        progress("process", 93);
        // await this.initProcessThreadStateData(progress);
        await this.initProcessThreadStateData2(progress);
        info("ProcessThreadState Data initialized")
        await this.initHeapStateData(progress)
        info("Heap State Data initialized")
        progress("display", 95);
        this.processThreads.length = 0;
        this.processMem.length = 0;
        this.processAsyncEvent.length = 0;
        this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach((it: any) => {
            it.addEventListener('expansion-change', () => {
                this.getVisibleRows().forEach(it2 => it2.draw());
            })
        })
        progress("completed", 100);
        info("All TraceRow Data initialized")
        this.loadTraceCompleted = true;
        this.getVisibleRows().forEach(it => {
            it.draw();
        });
        return {status: true, msg: "success"}
    }

    initCpuRate = async () => {
        let rates = await getCpuUtilizationRate(0, this.timerShaftEL?.totalNS || 0);
        if (this.timerShaftEL) this.timerShaftEL.cpuUsage = rates;
        info("Cpu UtilizationRate data size is: ", rates.length)
    }

    initTotalTime = async () => {
        let res = await queryTotalTime();
        if (this.timerShaftEL) {
            this.timerShaftEL.totalNS = res[0].total
            this.timerShaftEL.loadComplete = true;
        }
    }

    initCpu = async () => {
        let CpuStartTime = new Date().getTime();
        let array = await queryCpuMax();
        info("Cpu trace row data size is: ", array.length)
        if (array && array.length > 0 && array[0]) {
            let cpuMax = array[0].cpu
            CpuStruct.cpuCount = cpuMax + 1;
            for (let i1 = 0; i1 < CpuStruct.cpuCount; i1++) {
                const cpuId = i1;
                let traceRow = new TraceRow<CpuStruct>({
                    canvasNumber: 1,
                    alpha: true,
                    contextId: '2d',
                    isOffScreen: SpSystemTrace.isCanvasOffScreen
                });
                traceRow.rowId = `${cpuId}`
                traceRow.rowType = TraceRow.ROW_TYPE_CPU
                traceRow.rowParentId = ''
                traceRow.style.height = '40px'
                traceRow.name = `Cpu ${cpuId}`
                traceRow.favoriteChangeHandler = this.favoriteChangeHandler;
                traceRow.selectChangeHandler = this.selectChangeHandler;
                traceRow.supplier = () => queryCpuData(cpuId, TraceRow.range?.startNS || 0, TraceRow.range?.endNS || 0)
                traceRow.onThreadHandler = ((useCache: boolean, buf: ArrayBuffer | undefined | null) => {
                    procedurePool.submitWithName(`cpu${cpuId % procedurePool.cpusLen.length}`, `cpu${cpuId}`, {
                        list: traceRow.must ? traceRow.dataList : undefined,
                        offscreen: !traceRow.isTransferCanvas ? traceRow.offscreen[0] : undefined,//是否离屏
                        dpr: traceRow.dpr,//屏幕dpr值
                        xs: TraceRow.range?.xs,//线条坐标信息
                        isHover: traceRow.isHover,
                        flagMoveInfo: this.hoverFlag,
                        flagSelectedInfo: this.selectFlag,
                        hoverX: traceRow.hoverX,
                        hoverY: traceRow.hoverY,
                        canvasWidth: traceRow.canvasWidth,
                        canvasHeight: traceRow.canvasHeight,
                        hoverCpuStruct: CpuStruct.hoverCpuStruct,
                        selectCpuStruct: CpuStruct.selectCpuStruct,
                        wakeupBean: CpuStruct.wakeupBean,
                        isRangeSelect: traceRow.rangeSelect,
                        rangeSelectObject: TraceRow.rangeSelectObject,
                        useCache: useCache,
                        lineColor: traceRow.getLineColor(),
                        startNS: TraceRow.range?.startNS || 0,
                        endNS: TraceRow.range?.endNS || 0,
                        totalNS: TraceRow.range?.totalNS || 0,
                        slicesTime: TraceRow.range?.slicesTime,
                        range: TraceRow.range,
                        frame: traceRow.frame
                    }, !traceRow.isTransferCanvas ? traceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                        traceRow.must = false;
                        if (traceRow.isHover) {
                            CpuStruct.hoverCpuStruct = hover;
                            if (TraceRow.range) TraceRow.range.refresh = false;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU && it.name !== traceRow.name).forEach(it => it.draw(true));
                        }
                    })
                    traceRow.isTransferCanvas = true;
                })
                this.rowsEL?.appendChild(traceRow)
            }
        }
        let CpuDurTime = new Date().getTime() - CpuStartTime;
        info('The time to load the Cpu data is: ', CpuDurTime)
    }

    initCpuFreq = async () => {
        let cpuFreqStartTime = new Date().getTime();
        let freqList = await queryCpuFreq();
        info("Cpu Freq data size is: ", freqList!.length)
        let freqMaxList = await queryCpuMaxFreq();
        CpuFreqStruct.maxFreq = freqMaxList[0].maxFreq;
        let math = () => {
            let units: Array<string> = ["", "K", "M", "G", "T", "E"];
            let sb = " ";
            CpuFreqStruct.maxFreqName = " ";
            if (CpuFreqStruct.maxFreq > 0) {
                let log10: number = Math.ceil(Math.log10(CpuFreqStruct.maxFreq));
                let pow10: number = Math.pow(10, log10);
                let afterCeil: number = Math.ceil(CpuFreqStruct.maxFreq / (pow10 / 4)) * (pow10 / 4);
                CpuFreqStruct.maxFreq = afterCeil;
                let unitIndex: number = Math.floor(log10 / 3);
                sb = `${afterCeil / Math.pow(10, unitIndex * 3)}${units[unitIndex + 1]}hz`
            }
            CpuFreqStruct.maxFreqName = sb.toString();
        }
        math();
        for (let i = 0; i < freqList.length; i++) {
            const it = freqList[i];
            let traceRow = new TraceRow<CpuFreqStruct>({
                canvasNumber: 1,
                alpha: true,
                contextId: '2d',
                isOffScreen: SpSystemTrace.isCanvasOffScreen
            });
            traceRow.rowId = `${it.cpu}`
            traceRow.rowType = TraceRow.ROW_TYPE_CPU_FREQ
            traceRow.rowParentId = ''
            traceRow.style.height = '40px'
            traceRow.name = `Cpu ${it.cpu} Frequency`;
            traceRow.favoriteChangeHandler = this.favoriteChangeHandler;
            traceRow.selectChangeHandler = this.selectChangeHandler;
            traceRow.supplier = () => queryCpuFreqData(it.cpu)
            traceRow.onThreadHandler = (useCache) => {
                procedurePool.submitWithName(`freq${it.cpu % procedurePool.freqLen.length}`, `freq${it.cpu}`, {
                    list: traceRow.must ? traceRow.dataList : undefined,
                    offscreen: !traceRow.isTransferCanvas ? traceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: traceRow.dpr,
                    isHover: traceRow.isHover,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    hoverX: traceRow.hoverX,
                    hoverY: traceRow.hoverY,
                    canvasWidth: traceRow.canvasWidth,
                    canvasHeight: traceRow.canvasHeight,
                    hoverCpuFreqStruct: CpuFreqStruct.hoverCpuFreqStruct,
                    selectCpuFreqStruct: CpuFreqStruct.selectCpuFreqStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: traceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxFreq: CpuFreqStruct.maxFreq,
                    maxFreqName: CpuFreqStruct.maxFreqName,
                    useCache: useCache,
                    lineColor: traceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: traceRow.frame
                }, !traceRow.isTransferCanvas ? traceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    traceRow.must = false;
                    if (traceRow.args.isOffScreen == true) {
                        if (traceRow.isHover) {
                            CpuFreqStruct.hoverCpuFreqStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU_FREQ && it.name !== traceRow.name).forEach(it => it.draw(true));
                        }
                        return;
                    }
                })
                traceRow.isTransferCanvas = true;
            }
            this.rowsEL?.appendChild(traceRow)
        }
        let durTime = new Date().getTime() - cpuFreqStartTime;
        info('The time to load the CpuFreq data is: ', durTime)
    }

    initFPS = async () => {
        let res = await getFps();
        if (res.length == 0) {
            return;
        }
        let startTime = new Date().getTime();
        let fpsRow = new TraceRow<FpsStruct>({canvasNumber: 1, alpha: true, contextId: '2d', isOffScreen: true});
        fpsRow.rowId = `fps`
        fpsRow.rowType = TraceRow.ROW_TYPE_FPS
        fpsRow.rowParentId = ''
        FpsStruct.maxFps = 0
        fpsRow.style.height = '40px'
        fpsRow.name = "FPS"
        fpsRow.supplier = () => new Promise<Array<any>>((resolve, reject) => resolve(res));
        fpsRow.favoriteChangeHandler = this.favoriteChangeHandler;
        fpsRow.selectChangeHandler = this.selectChangeHandler;
        fpsRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`process0`, `fps0`, {
                list: fpsRow.must ? fpsRow.dataList : undefined,
                offscreen: !fpsRow.isTransferCanvas ? fpsRow.offscreen[0] : undefined,
                xs: TraceRow.range?.xs,
                dpr: fpsRow.dpr,
                isHover: fpsRow.isHover,
                flagMoveInfo: this.hoverFlag,
                flagSelectedInfo: this.selectFlag,
                hoverX: fpsRow.hoverX,
                hoverY: fpsRow.hoverY,
                hoverFpsStruct: FpsStruct.hoverFpsStruct,
                canvasWidth: fpsRow.canvasWidth,
                canvasHeight: fpsRow.canvasHeight,
                wakeupBean: CpuStruct.wakeupBean,
                isRangeSelect: fpsRow.rangeSelect,
                rangeSelectObject: TraceRow.rangeSelectObject,
                useCache: useCache,
                lineColor: fpsRow.getLineColor(),
                startNS: TraceRow.range?.startNS || 0,
                endNS: TraceRow.range?.endNS || 0,
                totalNS: TraceRow.range?.totalNS || 0,
                slicesTime: TraceRow.range?.slicesTime,
                range: TraceRow.range,
                frame: fpsRow.frame
            }, !fpsRow.isTransferCanvas ? fpsRow.offscreen[0] : undefined, (res: any, hover: any) => {
                fpsRow.must = false;
                if (fpsRow.args.isOffScreen == true) {
                    if (fpsRow.isHover) {
                        FpsStruct.hoverFpsStruct = hover;
                        this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_FPS && it.name !== fpsRow.name).forEach(it => it.draw(true));
                    }
                    return;
                }
            });
            fpsRow.isTransferCanvas = true;
        }
        this.rowsEL?.appendChild(fpsRow)
        let durTime = new Date().getTime() - startTime;
        info('The time to load the FPS data is: ', durTime)
    }

    initNativeMemory = async () => {
        let time = new Date().getTime();
        let nativeProcess = await queryNativeHookProcess();
        info("NativeHook Process data size is: ", nativeProcess!.length)
        if (nativeProcess.length == 0) {
            return;
        }
        SpSystemTrace.EVENT_HEAP = await queryHeapGroupByEvent();
        SpSystemTrace.NATIVE_MEMORY_DATA = await queryAllHeapByEvent();
        let nativeRow = new TraceRow({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        let process = "";
        if (nativeProcess.length > 0) {
            process = ` ${nativeProcess[0].pid}`
        }
        nativeRow.rowId = `native-memory`
        nativeRow.index = 0;
        nativeRow.rowType = TraceRow.ROW_TYPE_NATIVE_MEMORY
        nativeRow.drawType = 0;
        nativeRow.rowParentId = '';
        nativeRow.folder = true;
        nativeRow.name = `Native Memory` + process;
        nativeRow.favoriteChangeHandler = this.favoriteChangeHandler;
        nativeRow.selectChangeHandler = this.selectChangeHandler;
        nativeRow.onDrawTypeChangeHandler = (type) => {
            this.rowsEL?.querySelectorAll<TraceRow<any>>(`trace-row[row-type='heap']`).forEach(it => {
                it.drawType = type;
                it.isComplete = false;
                it.draw();
            })
        };
        nativeRow.supplier = () => new Promise<Array<any>>((resolve, reject) => resolve([]));
        nativeRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`process${nativeRow.index}`, `native-memory`, {
                list: nativeRow.must ? nativeRow.dataList : undefined,
                offscreen: !nativeRow.isTransferCanvas ? nativeRow.offscreen[0] : undefined,
                xs: TraceRow.range?.xs,
                dpr: nativeRow.dpr,
                isHover: nativeRow.isHover,
                flagMoveInfo: this.hoverFlag,
                flagSelectedInfo: this.selectFlag,
                hoverX: nativeRow.hoverX,
                hoverY: nativeRow.hoverY,
                canvasWidth: nativeRow.canvasWidth,
                canvasHeight: nativeRow.canvasHeight,
                isRangeSelect: nativeRow.rangeSelect,
                rangeSelectObject: TraceRow.rangeSelectObject,
                useCache: useCache,
                lineColor: nativeRow.getLineColor(),
                startNS: TraceRow.range?.startNS || 0,
                endNS: TraceRow.range?.endNS || 0,
                totalNS: TraceRow.range?.totalNS || 0,
                slicesTime: TraceRow.range?.slicesTime,
                range: TraceRow.range,
                frame: nativeRow.frame
            }, !nativeRow.isTransferCanvas ? nativeRow.offscreen[0] : undefined, (res: any) => {
                nativeRow.must = false;
            });
            nativeRow.isTransferCanvas = true;
        }

        this.rowsEL?.appendChild(nativeRow)
        /**
         * 添加heap信息
         */
        let native_memory = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"];
        for (let i = 0; i < native_memory.length; i++) {
            let nm = native_memory[i];
            let allHeapRow = new TraceRow<HeapStruct>({
                canvasNumber: 1,
                alpha: false,
                contextId: '2d',
                isOffScreen: true
            });
            allHeapRow.index = i;
            allHeapRow.rowParentId = `native-memory`
            allHeapRow.rowHidden = !nativeRow.expansion
            allHeapRow.style.height = '40px'
            allHeapRow.name = nm;
            allHeapRow.rowId = nm;
            allHeapRow.drawType = 0;
            allHeapRow.folder = false;
            allHeapRow.rowType = TraceRow.ROW_TYPE_HEAP;
            allHeapRow.favoriteChangeHandler = this.favoriteChangeHandler;
            allHeapRow.selectChangeHandler = this.selectChangeHandler;
            allHeapRow.setAttribute('children', '')
            allHeapRow.supplier = () => {
                return this.getNativeMemoryDataByChartType(i, allHeapRow.drawType)
            }
            allHeapRow.onThreadHandler = (useCache) => {
                procedurePool.submitWithName(`process${allHeapRow.index}`, `heap-${nm}`, {
                    list: allHeapRow.must ? allHeapRow.dataList : undefined,
                    offscreen: !allHeapRow.isTransferCanvas ? allHeapRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: allHeapRow.dpr,
                    isHover: allHeapRow.isHover,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    hoverX: allHeapRow.hoverX,
                    hoverY: allHeapRow.hoverY,
                    canvasWidth: allHeapRow.canvasWidth,
                    canvasHeight: allHeapRow.canvasHeight,
                    isRangeSelect: allHeapRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    useCache: useCache,
                    lineColor: allHeapRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: allHeapRow.frame
                }, !allHeapRow.isTransferCanvas ? allHeapRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    allHeapRow.must = false;
                    if (allHeapRow.isHover) {
                        HeapStruct.hoverHeapStruct = hover;
                    }
                });
                allHeapRow.isTransferCanvas = true;
            }
            this.rowsEL?.appendChild(allHeapRow)
        }
        let durTime = new Date().getTime() - time;
        info('The time to load the Native Memory data is: ', durTime)
    }

    getNativeMemoryDataByChartType = async (nativeMemoryType: number, chartType: number): Promise<Array<HeapStruct>> => {
        let arr: Array<HeapStruct> = [];
        let source: Array<NativeEvent> = [];
        if (nativeMemoryType == 0) {
            source = SpSystemTrace.NATIVE_MEMORY_DATA;
        } else if (nativeMemoryType == 1) {
            SpSystemTrace.NATIVE_MEMORY_DATA.map((ne) => {
                if (ne.eventType == 'AllocEvent' || ne.eventType == 'FreeEvent') {
                    source.push(ne);
                }
            })
        } else {
            SpSystemTrace.NATIVE_MEMORY_DATA.map((ne) => {
                if (ne.eventType == 'MmapEvent' || ne.eventType == 'MunmapEvent') {
                    source.push(ne);
                }
            })
        }
        if (source.length > 0) {
            let first = new HeapStruct();
            first.startTime = source[0].startTime;
            first.eventType = source[0].eventType;
            if (first.eventType == "AllocEvent" || first.eventType == "MmapEvent") {
                first.heapsize = chartType == 1 ? 1 : source[0].heapSize;
            } else {
                first.heapsize = chartType == 1 ? -1 : (0 - source[0].heapSize);
            }
            arr.push(first);
            let max = first.heapsize;
            let min = first.heapsize;
            for (let i = 1, len = source.length; i < len; i++) {
                let heap = new HeapStruct();
                heap.startTime = source[i].startTime;
                heap.eventType = source[i].eventType;
                arr[i - 1].dur = heap.startTime! - arr[i - 1].startTime!;
                if (i == len - 1) {
                    heap.dur = TraceRow.range?.totalNS! - heap.startTime!;
                }
                if (heap.eventType == "AllocEvent" || heap.eventType == "MmapEvent") {
                    if (chartType == 1) {
                        heap.heapsize = arr[i - 1].heapsize! + 1;
                    } else {
                        heap.heapsize = arr[i - 1].heapsize! + source[i].heapSize;
                    }
                } else {
                    if (chartType == 1) {
                        heap.heapsize = arr[i - 1].heapsize! - 1;
                    } else {
                        heap.heapsize = arr[i - 1].heapsize! - source[i].heapSize;
                    }
                }
                if (heap.heapsize > max) {
                    max = heap.heapsize;
                }
                if (heap.heapsize < min) {
                    min = heap.heapsize;
                }
                arr.push(heap);
            }
            arr.map((heap) => {
                heap.maxHeapSize = max;
                heap.minHeapSize = min;
            })
        }
        return arr;
    }

    initProcessThreadStateData = async (progress: Function) => {
        let time = new Date().getTime();
        SpSystemTrace.SPT_DATA = [];
        let res = await getStatesProcessThreadDataCount();
        let count: number = (res[0] as any).count;
        let pageSize = 500000;
        let pages = Math.ceil(count / pageSize);
        let percent = 93;
        for (let i = 0; i < pages; i++) {
            progress("StateProcessThread", percent + ((i + 1) / 100));
            let arr = await getStatesProcessThreadData(pageSize, i * pageSize);
            SpSystemTrace.SPT_DATA = SpSystemTrace.SPT_DATA.concat(arr);
        }
        let durTime = new Date().getTime() - time;
        info('The time to load the first ProcessThreadState data is: ', durTime)
    }

    initProcessThreadStateData2 = async (progress: Function) => {
        let time = new Date().getTime();
        SpSystemTrace.SPT_DATA = [];
        let pageSize = 500000;
        let percent = 93;
        let threadStateRes = await getThreadStateDataCount();
        let count: number = (threadStateRes[0] as any).count;
        let pages = Math.ceil(count / pageSize);
        let arrTs: Array<ThreadState> = [];
        let ps = 0;
        for (let i = 0; i < pages; i++) {
            ps += 1;
            progress("StateProcessThread", percent + (ps / 100));
            let arr = await getThreadStateData(pageSize, i * pageSize);
            arrTs = arrTs.concat(arr);
        }
        ps += 1;
        progress("StateProcessThread", percent + (ps / 100));
        let arrTp: Array<ThreadProcess> = await getThreadProcessData();
        let mapTp: Map<number, ThreadProcess> = new Map<number, ThreadProcess>();
        for (let tp of arrTp) {
            mapTp.set(tp.id, tp);
        }
        let threadSliceRes = await getSliceDataCount();
        let sliceCount: number = (threadSliceRes[0] as any).count;
        let slicePages = Math.ceil(sliceCount / pageSize);
        let arrSlice: Array<SptSlice> = [];
        for (let i = 0; i < slicePages; i++) {
            ps += 1;
            progress("StateProcessThread", percent + (ps / 100));
            let arr = await getSliceData(pageSize, i * pageSize);
            arrSlice = arrSlice.concat(arr);
        }
        let mapSlice: Map<string, SptSlice> = new Map<string, SptSlice>();
        for (let slice of arrSlice) {
            mapSlice.set(`${slice.itid}-${slice.ts}`, slice);
        }
        for (let tr of arrTs) {
            if (mapTp.has(tr.itid)) {
                let tp = mapTp.get(tr.itid);
                let spt = new SPT();
                spt.processId = tp!.processId
                spt.process = tp!.process
                spt.thread = tp!.thread
                spt.threadId = tp!.threadId
                spt.state = tr.state;
                spt.dur = tr.dur;
                spt.end_ts = tr.end_ts;
                spt.start_ts = tr.start_ts;
                spt.cpu = tr.cpu;
                let slice = mapSlice.get(`${tr.itid}-${tr.ts}`);
                spt.priority = (slice != undefined && slice != null) ? slice!.priority.toString() : "";
                spt.note = "-";
                SpSystemTrace.SPT_DATA.push(spt);
            }
        }
        let durTime = new Date().getTime() - time;
        info('The time to load the second ProcessThreadState data is: ', durTime)
    }

    initHeapStateData = async (progress: Function) => {
        let time = new Date().getTime();
        progress("StateHeap", 94);
        await query("InitNativeMemory","","","native-memory-init")
        let durTime = new Date().getTime() - time;
        info('The time to load the second HeapState data is: ', durTime)
    }

    initAsyncFuncData = async () => {
        let count = 0;
        let res = await queryProcessAsyncFuncCount();
        info("AsyncFuncData Count is: ", res!.length)
        if (res != undefined && res.length > 0 && (res[0] as any).count != undefined) {
            count = (res[0] as any).count;
        }
        let asyncFuncList: any[] = []
        if (count > 0) {
            let pageSize = 300000;
            let pages = Math.ceil(count / pageSize);
            for (let i = 0; i < pages; i++) {
                let arr = await queryProcessAsyncFunc(pageSize, i * pageSize);
                asyncFuncList = asyncFuncList.concat(arr)
            }
        }
        this.processAsyncFuncMap = Utils.groupBy(asyncFuncList, "pid");
    }

    /**
     * 添加进程信息
     */
    initProcess = async () => {
        if (this.eventCountMap["print"] == 0 &&
            this.eventCountMap["tracing_mark_write"] == 0 &&
            this.eventCountMap["sched_switch"] == 0
        ) {
            return;
        }
        let time = new Date().getTime();
        let processes = await queryProcess();
        let processFromTable = await queryProcessByTable();
        let processList = Utils.removeDuplicates(processes, processFromTable, "pid")
        info("ProcessList Data size is: ", processList!.length)
        for (let i = 0; i < processList.length; i++) {
            const it = processList[i];
            let processRow = new TraceRow<ProcessStruct>({
                canvasNumber: 1,
                alpha: false,
                contextId: '2d',
                isOffScreen: SpSystemTrace.isCanvasOffScreen
            });
            processRow.rowId = `${it.pid}`
            processRow.index = i;
            processRow.rowType = TraceRow.ROW_TYPE_PROCESS
            processRow.rowParentId = '';
            processRow.folder = true;
            processRow.name = `${it.processName || "Process"} ${it.pid}`;
            processRow.supplier = () => queryProcessData(it.pid || -1, 0, TraceRow.range?.totalNS || 0);
            processRow.favoriteChangeHandler = this.favoriteChangeHandler;
            processRow.selectChangeHandler = this.selectChangeHandler;
            processRow.onThreadHandler = (useCache) => {
                procedurePool.submitWithName(`process${(processRow.index) % procedurePool.processLen.length}`, `process ${processRow.index} ${it.processName}`, {
                    list: processRow.must ? processRow.dataList : undefined,
                    offscreen: !processRow.isTransferCanvas ? processRow.offscreen[0] : undefined,
                    pid: it.pid,
                    xs: TraceRow.range?.xs,
                    dpr: processRow.dpr,
                    isHover: processRow.isHover,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    hoverX: processRow.hoverX,
                    hoverY: processRow.hoverY,
                    canvasWidth: processRow.canvasWidth,
                    canvasHeight: processRow.canvasHeight,
                    isRangeSelect: processRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    wakeupBean: CpuStruct.wakeupBean,
                    cpuCount: CpuStruct.cpuCount,
                    useCache: useCache,
                    lineColor: processRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: processRow.frame
                }, !processRow.isTransferCanvas ? processRow.offscreen[0] : undefined, (res: any) => {
                    processRow.must = false;
                })
                processRow.isTransferCanvas = true;
            }
            this.rowsEL?.appendChild(processRow)
            /**
             * Async Function
             */
            let asyncFuncList = this.processAsyncFuncMap[it.pid] || [];
            let asyncFuncGroup = Utils.groupBy(asyncFuncList, "funName");
            Reflect.ownKeys(asyncFuncGroup).map((key: any) => {
                let asyncFunctions: Array<any> = asyncFuncGroup[key];
                if (asyncFunctions.length > 0) {
                    let isIntersect = (a: any, b: any) => (Math.max(a.startTs + a.dur, b.startTs + b.dur) - Math.min(a.startTs, b.startTs) < a.dur + b.dur);
                    let depthArray: any = []
                    let createDepth = (currentDepth: number, index: number) => {
                        if (depthArray[currentDepth] == undefined || !isIntersect(depthArray[currentDepth], asyncFunctions[index])) {
                            asyncFunctions[index].depth = currentDepth;
                            depthArray[currentDepth] = asyncFunctions[index]
                        } else {
                            createDepth(++currentDepth, index)
                        }
                    }
                    asyncFunctions.forEach((it, i) => {
                        if (it.dur == -1) {
                            it.dur = TraceRow.range?.endNS || 0 - it.startTs;
                        }
                        createDepth(0, i);
                    });
                    const groupedBy: Array<any> = [];
                    for (let i = 0; i < asyncFunctions.length; i++) {
                        if (groupedBy[asyncFunctions[i].depth || 0]) {
                            groupedBy[asyncFunctions[i].depth || 0].push(asyncFunctions[i]);
                        } else {
                            groupedBy[asyncFunctions[i].depth || 0] = [asyncFunctions[i]];
                        }
                    }
                    let max = Math.max(...asyncFunctions.map(it => it.depth || 0)) + 1
                    let maxHeight = max * 20;
                    let funcRow = new TraceRow<FuncStruct>({
                        canvasNumber: max,
                        alpha: false,
                        contextId: '2d',
                        isOffScreen: SpSystemTrace.isCanvasOffScreen
                    });
                    funcRow.rowId = `${asyncFunctions[0].funName}-${it.pid}`
                    funcRow.asyncFuncName = asyncFunctions[0].funName;
                    funcRow.asyncFuncNamePID = it.pid;
                    funcRow.rowType = TraceRow.ROW_TYPE_FUNC
                    funcRow.rowParentId = `${it.pid}`
                    funcRow.rowHidden = !processRow.expansion
                    // funcRow.checkType = threadRow.checkType;
                    funcRow.style.width = `100%`;
                    funcRow.setAttribute("height", `${maxHeight}`);
                    funcRow.name = `${asyncFunctions[0].funName}`;
                    funcRow.setAttribute('children', '')
                    funcRow.supplier = () => new Promise((resolve, reject) => resolve(asyncFunctions))
                    funcRow.favoriteChangeHandler = this.favoriteChangeHandler;
                    funcRow.selectChangeHandler = this.selectChangeHandler;
                    funcRow.onThreadHandler = (useCache) => {
                        let asy = async (useCache: boolean) => {
                            let scrollTop = this.rowsEL?.scrollTop || 0;
                            let scrollHeight = this.rowsEL?.clientHeight || 0;
                            let promises: Array<any> = [];
                            for (let k = 0; k < groupedBy.length; k++) {
                                let top = funcRow.offsetTop - (this.rowsEL?.offsetTop || 0) - scrollTop + funcRow.canvas[k].offsetTop;
                                let isLive = ((top + funcRow.canvas[k].clientHeight >= 0) && (top < scrollHeight)) || funcRow.collect
                                let promise = await procedurePool.submitWithNamePromise(`cpu${k % procedurePool.cpusLen.length}`, `func-${asyncFunctions[0].funName}-${it.pid}-${k}`, {
                                    isLive: isLive,
                                    list: funcRow.must ? groupedBy[k] : undefined,
                                    offscreen: !funcRow.isTransferCanvas ? funcRow.offscreen[k] : undefined,//是否离屏
                                    dpr: funcRow.dpr,//屏幕dpr值
                                    xs: TraceRow.range?.xs,//线条坐标信息
                                    isHover: funcRow.isHover,
                                    flagMoveInfo: this.hoverFlag,
                                    flagSelectedInfo: this.selectFlag,
                                    hoverX: funcRow.hoverX,
                                    hoverY: funcRow.hoverY,
                                    depth: k,
                                    canvasWidth: funcRow.canvasWidth,
                                    canvasHeight: funcRow.canvasHeight,
                                    maxHeight: maxHeight,
                                    hoverFuncStruct: FuncStruct.hoverFuncStruct,
                                    selectFuncStruct: FuncStruct.selectFuncStruct,
                                    wakeupBean: CpuStruct.wakeupBean,
                                    isRangeSelect: funcRow.rangeSelect,
                                    rangeSelectObject: TraceRow.rangeSelectObject,
                                    useCache: useCache,
                                    lineColor: funcRow.getLineColor(),
                                    startNS: TraceRow.range?.startNS || 0,
                                    endNS: TraceRow.range?.endNS || 0,
                                    totalNS: TraceRow.range?.totalNS || 0,
                                    slicesTime: TraceRow.range?.slicesTime,
                                    range: TraceRow.range,
                                    frame: funcRow.frame
                                }, !funcRow.isTransferCanvas ? funcRow.offscreen[k] : undefined)
                                if (funcRow.isHover && promise.hover) {
                                    FuncStruct.hoverFuncStruct = promise.hover;
                                }
                                promises.push(promise);
                            }
                            if (funcRow.isHover && promises.every(it => !it.hover)) {
                                FuncStruct.hoverFuncStruct = undefined;
                            }
                            funcRow.must = false;
                            funcRow.isTransferCanvas = true;
                        }
                        asy(useCache).then()
                    }
                    this.rowsEL?.appendChild(funcRow);
                }
            });

            /**
             * 添加进程内存信息
             */
            let processMem = this.processMem.filter(mem => mem.pid === it.pid);
            processMem.forEach(mem => {
                let row = new TraceRow<ProcessMemStruct>({
                    canvasNumber: 1,
                    alpha: false,
                    contextId: '2d',
                    isOffScreen: SpSystemTrace.isCanvasOffScreen
                });
                row.rowId = `${mem.trackId}`
                row.rowType = TraceRow.ROW_TYPE_MEM
                row.rowParentId = `${it.pid}`
                row.rowHidden = !processRow.expansion
                row.style.height = '40px'
                row.style.width = `100%`;
                row.name = `${mem.trackName}`;
                row.setAttribute('children', '');
                row.favoriteChangeHandler = this.favoriteChangeHandler;
                row.selectChangeHandler = this.selectChangeHandler;
                row.supplier = () => queryProcessMemData(mem.trackId).then(res => {
                    let maxValue = Math.max(...res.map(it => it.value || 0))
                    for (let j = 0; j < res.length; j++) {
                        res[j].maxValue = maxValue;
                        if (j == res.length - 1) {
                            res[j].duration = (TraceRow.range?.totalNS || 0) - (res[j].startTime || 0);
                        } else {
                            res[j].duration = (res[j + 1].startTime || 0) - (res[j].startTime || 0);
                        }
                        if (j > 0) {
                            res[j].delta = (res[j].value || 0) - (res[j - 1].value || 0);
                        } else {
                            res[j].delta = 0;
                        }
                    }
                    return res
                });
                row.onThreadHandler = (useCache) => {
                    procedurePool.submitWithName(`cpu${mem.trackId % procedurePool.cpusLen.length}`, `mem ${mem.trackId} ${mem.trackName}`, {
                        list: row.must ? row.dataList : undefined,
                        offscreen: !row.isTransferCanvas ? row.offscreen[0] : undefined,//是否离屏
                        dpr: row.dpr,//屏幕dpr值
                        xs: TraceRow.range?.xs,//线条坐标信息
                        isHover: row.isHover,
                        flagMoveInfo: this.hoverFlag,
                        flagSelectedInfo: this.selectFlag,
                        hoverX: row.hoverX,
                        hoverY: row.hoverY,
                        canvasWidth: row.canvasWidth,
                        canvasHeight: row.canvasHeight,
                        wakeupBean: CpuStruct.wakeupBean,
                        isRangeSelect: row.rangeSelect,
                        rangeSelectObject: TraceRow.rangeSelectObject,
                        useCache: useCache,
                        lineColor: row.getLineColor(),
                        startNS: TraceRow.range?.startNS || 0,
                        endNS: TraceRow.range?.endNS || 0,
                        totalNS: TraceRow.range?.totalNS || 0,
                        slicesTime: TraceRow.range?.slicesTime,
                        hoverProcessMemStruct: ProcessMemStruct.hoverProcessMemStruct,
                        range: TraceRow.range,
                        frame: row.frame
                    }, !row.isTransferCanvas ? row.offscreen[0] : undefined, (res: any, hover: any) => {
                        row.must = false;
                        if (row.isHover) {
                            ProcessMemStruct.hoverProcessMemStruct = hover;
                        }
                        return;
                    });
                    row.isTransferCanvas = true;
                }
                this.rowsEL?.appendChild(row)
            });
            /**
             * add thread list
             */
            let threads = this.processThreads.filter(thread => thread.pid === it.pid && thread.tid != 0);
            for (let j = 0; j < threads.length; j++) {
                let thread = threads[j];
                let threadRow = new TraceRow<ThreadStruct>({
                    canvasNumber: 1,
                    alpha: false,
                    contextId: '2d',
                    isOffScreen: SpSystemTrace.isCanvasOffScreen
                });
                threadRow.rowId = `${thread.tid}`
                threadRow.rowType = TraceRow.ROW_TYPE_THREAD
                threadRow.rowParentId = `${it.pid}`
                threadRow.rowHidden = !processRow.expansion
                threadRow.index = j
                threadRow.style.height = '30px'
                threadRow.setAttribute("height", `30`);
                threadRow.style.width = `100%`;
                threadRow.name = `${thread.threadName || 'Thread'} ${thread.tid}`;
                threadRow.setAttribute('children', '')
                threadRow.favoriteChangeHandler = this.favoriteChangeHandler;
                threadRow.selectChangeHandler = this.selectChangeHandler;
                threadRow.supplier = () => queryThreadData(thread.tid || 0).then(res => {
                    getFunDataByTid(thread.tid || 0).then((funs: Array<FuncStruct>) => {
                        if (funs.length > 0) {
                            let isBinder = (data: FuncStruct): boolean => {
                                if (data.funName != null && (
                                    data.funName.toLowerCase().startsWith("binder transaction async") //binder transaction
                                    || data.funName.toLowerCase().startsWith("binder async")
                                    || data.funName.toLowerCase().startsWith("binder reply")
                                )
                                ) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                            funs.forEach(fun => {
                                if (isBinder(fun)) {
                                } else {
                                    if (fun.dur == -1) {
                                        fun.dur = (TraceRow.range?.totalNS || 0) - (fun.startTs || 0);
                                    }
                                }
                            })
                            const groupedBy: Array<any> = [];
                            for (let i = 0; i < funs.length; i++) {
                                if (groupedBy[funs[i].depth || 0]) {
                                    groupedBy[funs[i].depth || 0].push(funs[i]);
                                } else {
                                    groupedBy[funs[i].depth || 0] = [funs[i]];
                                }
                            }
                            let max = Math.max(...funs.map(it => it.depth || 0)) + 1
                            let maxHeight = max * 20;
                            let funcRow = new TraceRow<FuncStruct>({
                                canvasNumber: max,
                                alpha: false,
                                contextId: '2d',
                                isOffScreen: SpSystemTrace.isCanvasOffScreen
                            });
                            funcRow.rowId = `${thread.tid}`
                            funcRow.rowType = TraceRow.ROW_TYPE_FUNC
                            funcRow.rowParentId = `${it.pid}`
                            funcRow.rowHidden = !processRow.expansion
                            funcRow.checkType = threadRow.checkType;
                            funcRow.style.width = `100%`;
                            funcRow.setAttribute("height", `${maxHeight}`);
                            funcRow.name = `${thread.threadName || 'Thread'} ${thread.tid}`;
                            funcRow.setAttribute('children', '')
                            funcRow.supplier = () => new Promise((resolve, reject) => resolve(funs))
                            funcRow.favoriteChangeHandler = this.favoriteChangeHandler;
                            funcRow.selectChangeHandler = this.selectChangeHandler;
                            funcRow.onThreadHandler = (useCache) => {
                                let asy = async (useCache: boolean) => {
                                    let scrollTop = this.rowsEL?.scrollTop || 0;
                                    let scrollHeight = this.rowsEL?.clientHeight || 0;
                                    let promises: Array<any> = [];
                                    for (let k = 0; k < groupedBy.length; k++) {
                                        let top = funcRow.offsetTop - (this.rowsEL?.offsetTop || 0) - scrollTop + funcRow.canvas[k].offsetTop;
                                        let isLive = ((top + funcRow.canvas[k].clientHeight >= 0) && (top < scrollHeight)) || funcRow.collect
                                        let promise = await procedurePool.submitWithNamePromise(`cpu${k % procedurePool.cpusLen.length}`, `func${thread.tid}${k}${thread.threadName}`, {
                                            isLive: isLive,
                                            list: funcRow.must ? groupedBy[k] : undefined,
                                            offscreen: !funcRow.isTransferCanvas ? funcRow.offscreen[k] : undefined,//是否离屏
                                            dpr: funcRow.dpr,//屏幕dpr值
                                            xs: TraceRow.range?.xs,//线条坐标信息
                                            isHover: funcRow.isHover,
                                            flagMoveInfo: this.hoverFlag,
                                            flagSelectedInfo: this.selectFlag,
                                            hoverX: funcRow.hoverX,
                                            hoverY: funcRow.hoverY,
                                            depth: k,
                                            canvasWidth: funcRow.canvasWidth,
                                            canvasHeight: funcRow.canvasHeight,
                                            maxHeight: maxHeight,
                                            hoverFuncStruct: FuncStruct.hoverFuncStruct,
                                            selectFuncStruct: FuncStruct.selectFuncStruct,
                                            wakeupBean: CpuStruct.wakeupBean,
                                            isRangeSelect: funcRow.rangeSelect,
                                            rangeSelectObject: TraceRow.rangeSelectObject,
                                            useCache: useCache,
                                            lineColor: funcRow.getLineColor(),
                                            startNS: TraceRow.range?.startNS || 0,
                                            endNS: TraceRow.range?.endNS || 0,
                                            totalNS: TraceRow.range?.totalNS || 0,
                                            slicesTime: TraceRow.range?.slicesTime,
                                            range: TraceRow.range,
                                            frame: funcRow.frame
                                        }, !funcRow.isTransferCanvas ? funcRow.offscreen[k] : undefined);
                                        if (funcRow.isHover && promise.hover) {
                                            FuncStruct.hoverFuncStruct = promise.hover;
                                        }
                                        promises.push(promise);
                                    }
                                    if (funcRow.isHover && promises.every(it => !it.hover)) {
                                        FuncStruct.hoverFuncStruct = undefined;
                                    }
                                    funcRow.must = false;
                                    funcRow.isTransferCanvas = true;
                                }
                                asy(useCache).then();
                            }
                            this.insertAfter(funcRow, threadRow)
                            this.observer.observe(funcRow)
                            funcRow.draw();
                            if (threadRow.onComplete) {
                                threadRow.onComplete()
                            }
                            this.getVisibleRows();//function 由于后插入dom，所以需要重新获取可见行
                        }
                    })
                    if (res.length <= 0) {
                        threadRow.rowDiscard = true;
                    }
                    return res;
                })
                threadRow.onThreadHandler = (useCache) => {
                    procedurePool.submitWithName(`process${(threadRow.index) % procedurePool.processLen.length}`, `thread ${thread.tid} ${thread.threadName}`, {
                        list: threadRow.must ? threadRow.dataList : undefined,
                        offscreen: !threadRow.isTransferCanvas ? threadRow.offscreen[0] : undefined,//是否离屏
                        dpr: threadRow.dpr,//屏幕dpr值
                        xs: TraceRow.range?.xs,//线条坐标信息
                        isHover: threadRow.isHover,
                        flagMoveInfo: this.hoverFlag,
                        flagSelectedInfo: this.selectFlag,
                        hoverX: threadRow.hoverX,
                        hoverY: threadRow.hoverY,
                        canvasWidth: threadRow.canvasWidth,
                        canvasHeight: threadRow.canvasHeight,
                        hoverThreadStruct: ThreadStruct.hoverThreadStruct,
                        selectThreadStruct: ThreadStruct.selectThreadStruct,
                        wakeupBean: CpuStruct.wakeupBean,
                        isRangeSelect: threadRow.rangeSelect,
                        rangeSelectObject: TraceRow.rangeSelectObject,
                        useCache: useCache,
                        lineColor: threadRow.getLineColor(),
                        startNS: TraceRow.range?.startNS || 0,
                        endNS: TraceRow.range?.endNS || 0,
                        totalNS: TraceRow.range?.totalNS || 0,
                        slicesTime: TraceRow.range?.slicesTime,
                        range: TraceRow.range,
                        frame: threadRow.frame
                    }, !threadRow.isTransferCanvas ? threadRow.offscreen[0] : undefined, (res: any, hover: any) => {
                        threadRow.must = false;
                        if (threadRow.args.isOffScreen == true) {
                            if (threadRow.isHover) {
                                ThreadStruct.hoverThreadStruct = hover;
                                // this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU && it.name !== traceRow.name).forEach(it => it.draw());
                            }
                            return;
                        }
                    })
                    threadRow.isTransferCanvas = true;
                }
                if (threadRow.rowId == threadRow.rowParentId) {
                    this.insertAfter(threadRow, processRow)
                } else {
                    this.rowsEL?.appendChild(threadRow)
                }
            }
        }
        let durTime = new Date().getTime() - time;
        info('The time to load the Process data is: ', durTime)
    }

    insertAfter(newEl: HTMLElement, targetEl: HTMLElement) {
        let parentEl = targetEl.parentNode;
        if (parentEl!.lastChild == targetEl) {
            parentEl!.appendChild(newEl);
        } else {
            parentEl!.insertBefore(newEl, targetEl.nextSibling);
        }
    }

    initAbilityMonitor = async () => {
        let time = new Date().getTime();
        let result = await queryAbilityExits();
        info("Ability Monitor Exits Tables size is: ", result!.length)
        if (result.length <= 0) return;
        let processRow = this.initAbilityRow();
        if (this.hasTable(result, "trace_cpu_usage")) {
            await this.initCpuAbility(processRow);
        }
        if (this.hasTable(result, "sys_memory")) {
            await this.initMemoryAbility(processRow);
        }
        if (this.hasTable(result, "trace_diskio")) {
            await this.initDiskAbility(processRow);
        }
        if (this.hasTable(result, "trace_network")) {
            await this.initNetworkAbility(processRow);
        }
        let durTime = new Date().getTime() - time;
        info('The time to load the AbilityMonitor data is: ', durTime)
    }

    memoryMath = (maxByte: number) => {
        let maxByteName = ""
        if (maxByte > 0) {
            maxByteName = Utils.getBinaryByteWithUnit(maxByte)
        }
        return maxByteName;
    }

    diskIOMath = (maxByte: number) => {
        let maxByteName = ""
        if (maxByte > 0) {
            maxByteName = maxByte + "KB/S"
        }
        return maxByteName;
    }

    networkMath = (maxValue: number) => {
        let maxByteName = ""
        if (maxValue > 0) {
            maxByteName = Utils.getBinaryByteWithUnit(maxValue)
        }
        return maxByteName;
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
        .rows{
            color: #fff;
            display: flex;
            box-sizing: border-box;
            flex-direction: column;
            /*overflow-y: auto;*/
            overflow: overlay;
            max-height: calc(100vh - 147px - 48px);
            flex: 1;
            width: 100%;
            background: var(--dark-background4,#ffffff);
            /*scroll-behavior: smooth;*/
        }
        .container{
            width: 100%;
            box-sizing: border-box;
            height: 100%;
            display: grid;
            grid-template-columns: 1fr;
            grid-template-rows: min-content min-content 1fr min-content;
        }
        .trace-sheet{
            cursor: default;
        }

        </style>
        <div class="container">
            <timer-shaft-element class="timer-shaft">
            </timer-shaft-element>
            <div class="spacer"></div>
            <div class="rows"></div>
            <trace-sheet class="trace-sheet" mode="hidden">
            </trace-sheet>
        </div>
        `;
    }

    private hasTable(result: Array<any>, tableName: string) {
        return result.find((o) => {
            return o.event_name === tableName
        })
    }

    private initAbilityRow = () => {
        let processRow = new TraceRow<ProcessStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        processRow.rowId = `abilityMonitor`
        processRow.rowType = TraceRow.ROW_TYPE_MONITOR
        processRow.rowParentId = '';
        processRow.folder = true;
        processRow.name = 'Ability Monitor';
        processRow.favoriteChangeHandler = this.favoriteChangeHandler;
        processRow.selectChangeHandler = this.selectChangeHandler;
        processRow.supplier = () => new Promise<Array<any>>((resolve) => resolve([]));
        processRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu0`, `monitorGroup`, {
                    list: processRow.must ? processRow.dataList : undefined,
                    offscreen: processRow.must ? processRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: processRow.dpr,
                    isHover: processRow.isHover,
                    hoverX: processRow.hoverX,
                    hoverY: processRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: processRow.canvasWidth,
                    canvasHeight: processRow.canvasHeight,
                    isRangeSelect: processRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    useCache: useCache,
                    lineColor: processRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: processRow.frame,
                }, processRow.must && processRow.args.isOffScreen ? processRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    processRow.must = false;
                }
            )
        }
        this.rowsEL?.appendChild(processRow)
        return processRow;
    }

    private initCpuAbility = async (processRow: TraceRow<ProcessStruct>) => {
        let time = new Date().getTime();
        let cpuMaxData = await queryCPuAbilityMaxData();
        let hasTotal = false;
        let hasUserLoad = false;
        let hasSystemLoad = false;
        let userLoad = cpuMaxData[0].userLoad;
        if (userLoad > 0) {
            hasUserLoad = true;
        }
        let systemLoad = cpuMaxData[0].systemLoad;
        if (systemLoad > 0) {
            hasSystemLoad = true;
        }
        let totalLoad = cpuMaxData[0].totalLoad;
        if (totalLoad > 0) {
            hasTotal = true;
        }
        let cpuNameList: Array<string> = ['Total', 'User', 'System']
        let traceRow = new TraceRow<CpuAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        traceRow.rowParentId = `abilityMonitor`
        traceRow.rowHidden = !processRow.expansion
        traceRow.rowId = cpuNameList[0]
        traceRow.rowType = TraceRow.ROW_TYPE_CPU_ABILITY
        traceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        traceRow.selectChangeHandler = this.selectChangeHandler;
        traceRow.style.height = '40px'
        traceRow.style.width = `100%`;
        traceRow.setAttribute('children', '');
        traceRow.name = `CPU ${cpuNameList[0]} Load`;
        traceRow.supplier = () => queryCpuAbilityData()
        traceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu0`, `monitorCpu0`, {
                    list: traceRow.must ? traceRow.dataList : undefined,
                    offscreen: traceRow.must ? traceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: traceRow.dpr,
                    isHover: traceRow.isHover,
                    hoverX: traceRow.hoverX,
                    hoverY: traceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: traceRow.canvasWidth,
                    canvasHeight: traceRow.canvasHeight,
                    hoverCpuAbilityStruct: CpuAbilityMonitorStruct.hoverCpuAbilityStruct,
                    selectCpuAbilityStruct: CpuAbilityMonitorStruct.selectCpuAbilityStruct,
                    isRangeSelect: traceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxCpuUtilization: 100,
                    maxCpuUtilizationName: hasTotal ? "100%" : '0%',
                    useCache: useCache,
                    lineColor: traceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: traceRow.frame,
                }, traceRow.must && traceRow.args.isOffScreen ? traceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    traceRow.must = false;
                    if (traceRow.args.isOffScreen == true) {
                        if (traceRow.isHover) {
                            CpuAbilityMonitorStruct.hoverCpuAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU_ABILITY && it.name !== traceRow.name).forEach(it => it.draw(true));
                        }
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(traceRow)

        let userTraceRow = new TraceRow<CpuAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        userTraceRow.rowParentId = `abilityMonitor`
        userTraceRow.rowHidden = !processRow.expansion
        userTraceRow.rowId = cpuNameList[1]
        userTraceRow.rowType = TraceRow.ROW_TYPE_CPU_ABILITY
        userTraceRow.style.height = '40px'
        userTraceRow.style.width = `100%`;
        userTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        userTraceRow.selectChangeHandler = this.selectChangeHandler;
        userTraceRow.setAttribute('children', '');
        userTraceRow.name = `CPU ${cpuNameList[1]} Load`;
        userTraceRow.supplier = () => queryCpuAbilityUserData()
        userTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu1`, `monitorCpu1`, {
                    list: userTraceRow.must ? userTraceRow.dataList : undefined,
                    offscreen: userTraceRow.must ? userTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: userTraceRow.dpr,
                    isHover: userTraceRow.isHover,
                    hoverX: userTraceRow.hoverX,
                    hoverY: userTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: userTraceRow.canvasWidth,
                    canvasHeight: userTraceRow.canvasHeight,
                    hoverCpuAbilityStruct: CpuAbilityMonitorStruct.hoverCpuAbilityStruct,
                    selectCpuAbilityStruct: CpuAbilityMonitorStruct.selectCpuAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: userTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxCpuUtilization: 100,
                    maxCpuUtilizationName: hasUserLoad ? "100%" : '0%',
                    useCache: useCache,
                    lineColor: userTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: userTraceRow.frame,
                    isAbilityRow: true,
                    isStartAbilityRow: true,
                    isEndAbilityRow: false,
                }, userTraceRow.must && userTraceRow.args.isOffScreen ? userTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    userTraceRow.must = false;
                    if (userTraceRow.args.isOffScreen == true) {
                        if (userTraceRow.isHover) {
                            CpuAbilityMonitorStruct.hoverCpuAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU_ABILITY && it.name !== userTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (userTraceRow.dataList) userTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(userTraceRow)

        let sysTraceRow = new TraceRow<CpuAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        sysTraceRow.rowParentId = `abilityMonitor`
        sysTraceRow.rowHidden = !processRow.expansion
        sysTraceRow.rowId = cpuNameList[2]
        sysTraceRow.rowType = TraceRow.ROW_TYPE_CPU_ABILITY
        sysTraceRow.style.height = '40px'
        sysTraceRow.style.width = `100%`;
        sysTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        sysTraceRow.selectChangeHandler = this.selectChangeHandler;
        sysTraceRow.setAttribute('children', '');
        sysTraceRow.name = `CPU ${cpuNameList[2]} Load`;
        sysTraceRow.supplier = () => queryCpuAbilitySystemData()
        sysTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu2`, `monitorCpu2`, {
                    list: sysTraceRow.must ? sysTraceRow.dataList : undefined,
                    offscreen: sysTraceRow.must ? sysTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: sysTraceRow.dpr,
                    isHover: sysTraceRow.isHover,
                    hoverX: sysTraceRow.hoverX,
                    hoverY: sysTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: sysTraceRow.canvasWidth,
                    canvasHeight: sysTraceRow.canvasHeight,
                    hoverCpuAbilityStruct: CpuAbilityMonitorStruct.hoverCpuAbilityStruct,
                    selectCpuAbilityStruct: CpuAbilityMonitorStruct.selectCpuAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: sysTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxCpuUtilization: 100,
                    maxCpuUtilizationName: hasSystemLoad ? "100%" : '0%',
                    useCache: useCache,
                    lineColor: sysTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: sysTraceRow.frame,
                    isAbilityRow: true,
                    isStartAbilityRow: true,
                    isEndAbilityRow: false,
                }, sysTraceRow.must && sysTraceRow.args.isOffScreen ? sysTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    sysTraceRow.must = false;
                    if (sysTraceRow.args.isOffScreen == true) {
                        if (sysTraceRow.isHover) {
                            CpuAbilityMonitorStruct.hoverCpuAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU_ABILITY && it.name !== sysTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (sysTraceRow.dataList) sysTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(sysTraceRow)
        let durTime = new Date().getTime() - time;
        info('The time to load the Ability Cpu is: ', durTime)
    }

    private initMemoryAbility = async (processRow: TraceRow<ProcessStruct>) => {
        let time = new Date().getTime();
        // sys.mem.total  sys.mem.cached  sys.mem.swap.total
        let memoryNameList: Array<string> = ['MemoryTotal', 'Cached', 'SwapTotal']
        let memoryTotal = await queryMemoryMaxData("sys.mem.total");
        let memoryTotalValue = memoryTotal[0].maxValue
        let memoryTotalId = memoryTotal[0].filter_id

        let memoryTotalValueName = this.memoryMath(memoryTotalValue);
        let memoryUsedTraceRow = new TraceRow<MemoryAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        memoryUsedTraceRow.rowParentId = `abilityMonitor`
        memoryUsedTraceRow.rowHidden = !processRow.expansion
        memoryUsedTraceRow.rowId = memoryNameList[0]
        memoryUsedTraceRow.rowType = TraceRow.ROW_TYPE_MEMORY_ABILITY
        memoryUsedTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        memoryUsedTraceRow.selectChangeHandler = this.selectChangeHandler;
        memoryUsedTraceRow.style.height = '40px'
        memoryUsedTraceRow.style.width = `100%`;
        memoryUsedTraceRow.setAttribute('children', '');
        memoryUsedTraceRow.name = memoryNameList[0];
        memoryUsedTraceRow.supplier = () => queryMemoryUsedAbilityData(memoryTotalId)
        memoryUsedTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu0`, `monitorMemory0`, {
                    list: memoryUsedTraceRow.must ? memoryUsedTraceRow.dataList : undefined,
                    offscreen: memoryUsedTraceRow.must ? memoryUsedTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: memoryUsedTraceRow.dpr,
                    isHover: memoryUsedTraceRow.isHover,
                    hoverX: memoryUsedTraceRow.hoverX,
                    hoverY: memoryUsedTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: memoryUsedTraceRow.canvasWidth,
                    canvasHeight: memoryUsedTraceRow.canvasHeight,
                    hoverMemoryAbilityStruct: MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct,
                    selectMemoryAbilityStruct: MemoryAbilityMonitorStruct.selectMemoryAbilityStruct,
                    isRangeSelect: memoryUsedTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxMemoryByte: memoryTotalValue,
                    maxMemoryByteName: memoryTotalValueName,
                    useCache: useCache,
                    lineColor: memoryUsedTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: memoryUsedTraceRow.frame,
                }, memoryUsedTraceRow.must && memoryUsedTraceRow.args.isOffScreen ? memoryUsedTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    memoryUsedTraceRow.must = false;
                    if (memoryUsedTraceRow.args.isOffScreen == true) {
                        if (memoryUsedTraceRow.isHover) {
                            MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_MEMORY_ABILITY && it.name !== memoryUsedTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (memoryUsedTraceRow.dataList) memoryUsedTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(memoryUsedTraceRow)

        let cached = await queryMemoryMaxData("sys.mem.cached");
        let cachedValue = cached[0].maxValue
        let cachedValueName = this.memoryMath(cachedValue);
        let cachedId = cached[0].filter_id

        let cachedFilesTraceRow = new TraceRow<MemoryAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        cachedFilesTraceRow.rowParentId = `abilityMonitor`
        cachedFilesTraceRow.rowHidden = !processRow.expansion
        cachedFilesTraceRow.rowId = memoryNameList[1]
        cachedFilesTraceRow.rowType = TraceRow.ROW_TYPE_MEMORY_ABILITY
        cachedFilesTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        cachedFilesTraceRow.selectChangeHandler = this.selectChangeHandler;
        cachedFilesTraceRow.style.height = '40px'
        cachedFilesTraceRow.style.width = `100%`;
        cachedFilesTraceRow.setAttribute('children', '');
        cachedFilesTraceRow.name = memoryNameList[1];
        cachedFilesTraceRow.supplier = () => queryCachedFilesAbilityData(cachedId)
        cachedFilesTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu1`, `monitorMemory1`, {
                    list: cachedFilesTraceRow.must ? cachedFilesTraceRow.dataList : undefined,
                    offscreen: cachedFilesTraceRow.must ? cachedFilesTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: cachedFilesTraceRow.dpr,
                    isHover: cachedFilesTraceRow.isHover,
                    hoverX: cachedFilesTraceRow.hoverX,
                    hoverY: cachedFilesTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: cachedFilesTraceRow.canvasWidth,
                    canvasHeight: cachedFilesTraceRow.canvasHeight,
                    hoverMemoryAbilityStruct: MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct,
                    selectMemoryAbilityStruct: MemoryAbilityMonitorStruct.selectMemoryAbilityStruct,
                    isRangeSelect: cachedFilesTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxMemoryByte: cachedValue,
                    maxMemoryByteName: cachedValueName,
                    useCache: useCache,
                    lineColor: cachedFilesTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: cachedFilesTraceRow.frame,
                }, cachedFilesTraceRow.must && cachedFilesTraceRow.args.isOffScreen ? cachedFilesTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    cachedFilesTraceRow.must = false;
                    if (cachedFilesTraceRow.args.isOffScreen == true) {
                        if (cachedFilesTraceRow.isHover) {
                            MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_MEMORY_ABILITY && it.name !== cachedFilesTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (cachedFilesTraceRow.dataList) cachedFilesTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(cachedFilesTraceRow)


        let swap = await queryMemoryMaxData("sys.mem.swap.total");
        let swapValue = swap[0].maxValue
        let swapValueName = this.memoryMath(swapValue);
        let swapId = swap[0].filter_id

        let compressedTraceRow = new TraceRow<MemoryAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        compressedTraceRow.rowParentId = `abilityMonitor`
        compressedTraceRow.rowHidden = !processRow.expansion
        compressedTraceRow.rowId = memoryNameList[2]
        compressedTraceRow.rowType = TraceRow.ROW_TYPE_MEMORY_ABILITY
        compressedTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        compressedTraceRow.selectChangeHandler = this.selectChangeHandler;
        compressedTraceRow.style.height = '40px'
        compressedTraceRow.style.width = `100%`;
        compressedTraceRow.setAttribute('children', '');
        compressedTraceRow.name = memoryNameList[2];
        compressedTraceRow.supplier = () => queryCompressedAbilityData(swapId)
        compressedTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu2`, `monitorMemory2`, {
                    list: compressedTraceRow.must ? compressedTraceRow.dataList : undefined,
                    offscreen: compressedTraceRow.must ? compressedTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: compressedTraceRow.dpr,
                    isHover: compressedTraceRow.isHover,
                    hoverX: compressedTraceRow.hoverX,
                    hoverY: compressedTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: compressedTraceRow.canvasWidth,
                    canvasHeight: compressedTraceRow.canvasHeight,
                    hoverMemoryAbilityStruct: MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct,
                    selectMemoryAbilityStruct: MemoryAbilityMonitorStruct.selectMemoryAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: compressedTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxMemoryByte: swapValue,
                    maxMemoryByteName: swapValueName,
                    useCache: useCache,
                    lineColor: compressedTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: compressedTraceRow.frame,
                }, compressedTraceRow.must && compressedTraceRow.args.isOffScreen ? compressedTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    compressedTraceRow.must = false;
                    if (compressedTraceRow.args.isOffScreen == true) {
                        if (compressedTraceRow.isHover) {
                            MemoryAbilityMonitorStruct.hoverMemoryAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_MEMORY_ABILITY && it.name !== compressedTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (compressedTraceRow.dataList) compressedTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(compressedTraceRow)
        let durTime = new Date().getTime() - time;
        info('The time to load the Ability Memory is: ', durTime)
    }

    private initDiskAbility = async (processRow: TraceRow<ProcessStruct>) => {
        let time = new Date().getTime();
        let maxList = await queryDiskIoMaxData();
        let maxBytesRead = maxList[0].bytesRead;
        let maxBytesReadName = this.diskIOMath(maxBytesRead);
        let diskIONameList: Array<string> = ['Bytes Read/Sec', 'Bytes Written/Sec', 'Read Ops/Sec', 'Written Ops/Sec']
        let bytesReadTraceRow = new TraceRow<DiskAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        bytesReadTraceRow.rowParentId = `abilityMonitor`
        bytesReadTraceRow.rowHidden = !processRow.expansion
        bytesReadTraceRow.rowId = diskIONameList[0]
        bytesReadTraceRow.rowType = TraceRow.ROW_TYPE_DISK_ABILITY
        bytesReadTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        bytesReadTraceRow.selectChangeHandler = this.selectChangeHandler;
        bytesReadTraceRow.style.height = '40px'
        bytesReadTraceRow.style.width = `100%`;
        bytesReadTraceRow.setAttribute('children', '');
        bytesReadTraceRow.name = 'Disk ' + diskIONameList[0];
        bytesReadTraceRow.supplier = () => queryBytesReadAbilityData()
        bytesReadTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu0`, `monitorDiskIo0`, {
                    list: bytesReadTraceRow.must ? bytesReadTraceRow.dataList : undefined,
                    offscreen: bytesReadTraceRow.must ? bytesReadTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: bytesReadTraceRow.dpr,
                    isHover: bytesReadTraceRow.isHover,
                    hoverX: bytesReadTraceRow.hoverX,
                    hoverY: bytesReadTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: bytesReadTraceRow.canvasWidth,
                    canvasHeight: bytesReadTraceRow.canvasHeight,
                    hoverDiskAbilityStruct: DiskAbilityMonitorStruct.hoverDiskAbilityStruct,
                    selectDiskAbilityStruct: DiskAbilityMonitorStruct.selectDiskAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: bytesReadTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxDiskRate: maxBytesRead,
                    maxDiskRateName: maxBytesReadName,
                    useCache: useCache,
                    lineColor: bytesReadTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: bytesReadTraceRow.frame,
                }, bytesReadTraceRow.must && bytesReadTraceRow.args.isOffScreen ? bytesReadTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    bytesReadTraceRow.must = false;
                    if (bytesReadTraceRow.args.isOffScreen == true) {
                        if (bytesReadTraceRow.isHover) {
                            DiskAbilityMonitorStruct.hoverDiskAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_DISK_ABILITY && it.name !== bytesReadTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (bytesReadTraceRow.dataList) bytesReadTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(bytesReadTraceRow)

        let maxBytesWrite = maxList[0].bytesWrite;
        let maxBytesWriteName = this.diskIOMath(maxBytesWrite);
        let bytesWrittenTraceRow = new TraceRow<DiskAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        bytesWrittenTraceRow.rowParentId = `abilityMonitor`
        bytesWrittenTraceRow.rowHidden = !processRow.expansion
        bytesWrittenTraceRow.rowId = diskIONameList[1]
        bytesWrittenTraceRow.rowType = TraceRow.ROW_TYPE_DISK_ABILITY
        bytesWrittenTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        bytesWrittenTraceRow.selectChangeHandler = this.selectChangeHandler;
        bytesWrittenTraceRow.style.height = '40px'
        bytesWrittenTraceRow.style.width = `100%`;
        bytesWrittenTraceRow.setAttribute('children', '');
        bytesWrittenTraceRow.name = 'Disk ' + diskIONameList[1];
        bytesWrittenTraceRow.supplier = () => queryBytesWrittenAbilityData()
        bytesWrittenTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu1`, `monitorDiskIo1`, {
                    list: bytesWrittenTraceRow.must ? bytesWrittenTraceRow.dataList : undefined,
                    offscreen: bytesWrittenTraceRow.must ? bytesWrittenTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: bytesWrittenTraceRow.dpr,
                    isHover: bytesWrittenTraceRow.isHover,
                    hoverX: bytesWrittenTraceRow.hoverX,
                    hoverY: bytesWrittenTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: bytesWrittenTraceRow.canvasWidth,
                    canvasHeight: bytesWrittenTraceRow.canvasHeight,
                    hoverDiskAbilityStruct: DiskAbilityMonitorStruct.hoverDiskAbilityStruct,
                    selectDiskAbilityStruct: DiskAbilityMonitorStruct.selectDiskAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: bytesWrittenTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxDiskRate: maxBytesWrite,
                    maxDiskRateName: maxBytesWriteName,
                    useCache: useCache,
                    lineColor: bytesWrittenTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: bytesWrittenTraceRow.frame,
                }, bytesWrittenTraceRow.must && bytesWrittenTraceRow.args.isOffScreen ? bytesWrittenTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    bytesWrittenTraceRow.must = false;
                    if (bytesWrittenTraceRow.args.isOffScreen == true) {
                        if (bytesWrittenTraceRow.isHover) {
                            DiskAbilityMonitorStruct.hoverDiskAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_DISK_ABILITY && it.name !== bytesWrittenTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (bytesWrittenTraceRow.dataList) bytesWrittenTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(bytesWrittenTraceRow)


        let maxReadOps = maxList[0].readOps;
        let maxReadOpsName = this.diskIOMath(maxReadOps);
        let readOpsTraceRow = new TraceRow<DiskAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        readOpsTraceRow.rowParentId = `abilityMonitor`
        readOpsTraceRow.rowHidden = !processRow.expansion
        readOpsTraceRow.rowId = diskIONameList[2]
        readOpsTraceRow.rowType = TraceRow.ROW_TYPE_DISK_ABILITY
        readOpsTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        readOpsTraceRow.selectChangeHandler = this.selectChangeHandler;
        readOpsTraceRow.style.height = '40px'
        readOpsTraceRow.style.width = `100%`;
        readOpsTraceRow.setAttribute('children', '');
        readOpsTraceRow.name = 'Disk ' + diskIONameList[2];
        readOpsTraceRow.supplier = () => queryReadAbilityData()
        readOpsTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu2`, `monitorDiskIo2`, {
                    list: readOpsTraceRow.must ? readOpsTraceRow.dataList : undefined,
                    offscreen: readOpsTraceRow.must ? readOpsTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: readOpsTraceRow.dpr,
                    isHover: readOpsTraceRow.isHover,
                    hoverX: readOpsTraceRow.hoverX,
                    hoverY: readOpsTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: readOpsTraceRow.canvasWidth,
                    canvasHeight: readOpsTraceRow.canvasHeight,
                    hoverDiskAbilityStruct: DiskAbilityMonitorStruct.hoverDiskAbilityStruct,
                    selectDiskAbilityStruct: DiskAbilityMonitorStruct.selectDiskAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: readOpsTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxDiskRate: maxReadOps,
                    maxDiskRateName: maxReadOpsName,
                    useCache: useCache,
                    lineColor: readOpsTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: readOpsTraceRow.frame,
                }, readOpsTraceRow.must && readOpsTraceRow.args.isOffScreen ? readOpsTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    readOpsTraceRow.must = false;
                    if (readOpsTraceRow.args.isOffScreen == true) {
                        if (readOpsTraceRow.isHover) {
                            DiskAbilityMonitorStruct.hoverDiskAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_DISK_ABILITY && it.name !== readOpsTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (readOpsTraceRow.dataList) readOpsTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(readOpsTraceRow)

        let maxWriteOps = maxList[0].writeOps;
        let maxWriteOpsName = this.diskIOMath(maxWriteOps);
        let writtenOpsTraceRow = new TraceRow<DiskAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        writtenOpsTraceRow.rowParentId = `abilityMonitor`
        writtenOpsTraceRow.rowHidden = !processRow.expansion
        writtenOpsTraceRow.rowId = diskIONameList[3]
        writtenOpsTraceRow.rowType = TraceRow.ROW_TYPE_DISK_ABILITY
        writtenOpsTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        writtenOpsTraceRow.selectChangeHandler = this.selectChangeHandler;
        writtenOpsTraceRow.style.height = '40px'
        writtenOpsTraceRow.style.width = `100%`;
        writtenOpsTraceRow.setAttribute('children', '');
        writtenOpsTraceRow.name = 'Disk ' + diskIONameList[3];
        writtenOpsTraceRow.supplier = () => queryWrittenAbilityData()
        writtenOpsTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu3`, `monitorDiskIo3`, {
                    list: writtenOpsTraceRow.must ? writtenOpsTraceRow.dataList : undefined,
                    offscreen: writtenOpsTraceRow.must ? writtenOpsTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: writtenOpsTraceRow.dpr,
                    isHover: writtenOpsTraceRow.isHover,
                    hoverX: writtenOpsTraceRow.hoverX,
                    hoverY: writtenOpsTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: writtenOpsTraceRow.canvasWidth,
                    canvasHeight: writtenOpsTraceRow.canvasHeight,
                    hoverDiskAbilityStruct: DiskAbilityMonitorStruct.hoverDiskAbilityStruct,
                    selectDiskAbilityStruct: DiskAbilityMonitorStruct.selectDiskAbilityStruct,
                    wakeupBean: CpuStruct.wakeupBean,
                    isRangeSelect: writtenOpsTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxDiskRate: maxWriteOps,
                    maxDiskRateName: maxWriteOpsName,
                    useCache: useCache,
                    lineColor: writtenOpsTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: writtenOpsTraceRow.frame,
                }, writtenOpsTraceRow.must && writtenOpsTraceRow.args.isOffScreen ? writtenOpsTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    writtenOpsTraceRow.must = false;
                    if (writtenOpsTraceRow.args.isOffScreen == true) {
                        if (writtenOpsTraceRow.isHover) {
                            DiskAbilityMonitorStruct.hoverDiskAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_DISK_ABILITY && it.name !== writtenOpsTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (writtenOpsTraceRow.dataList) writtenOpsTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(writtenOpsTraceRow)
        let durTime = new Date().getTime() - time;
        info('The time to load the Ability DiskIO is: ', durTime)
    }

    private initNetworkAbility = async (processRow: TraceRow<ProcessStruct>) => {
        let time = new Date().getTime();
        let maxList = await queryNetWorkMaxData();
        let maxBytesIn = maxList[0].maxIn;
        let maxInByteName = this.networkMath(maxBytesIn);
        let networkNameList: Array<string> = ['Bytes In/Sec', 'Bytes Out/Sec', 'Packets In/Sec', 'Packets Out/Sec']
        let bytesInTraceRow = new TraceRow<NetworkAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        bytesInTraceRow.rowParentId = `abilityMonitor`
        bytesInTraceRow.rowHidden = !processRow.expansion
        bytesInTraceRow.rowId = networkNameList[0]
        bytesInTraceRow.rowType = TraceRow.ROW_TYPE_NETWORK_ABILITY
        bytesInTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        bytesInTraceRow.selectChangeHandler = this.selectChangeHandler;
        bytesInTraceRow.style.height = '40px'
        bytesInTraceRow.style.width = `100%`;
        bytesInTraceRow.setAttribute('children', '');
        bytesInTraceRow.name = 'Network ' + networkNameList[0];
        bytesInTraceRow.supplier = () => queryBytesInAbilityData()
        bytesInTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu0`, `monitorNetwork0`, {
                    list: bytesInTraceRow.must ? bytesInTraceRow.dataList : undefined,
                    offscreen: bytesInTraceRow.must ? bytesInTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: bytesInTraceRow.dpr,
                    isHover: bytesInTraceRow.isHover,
                    hoverX: bytesInTraceRow.hoverX,
                    hoverY: bytesInTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: bytesInTraceRow.canvasWidth,
                    canvasHeight: bytesInTraceRow.canvasHeight,
                    hoverNetworkAbilityStruct: NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct,
                    selectNetworkAbilityStruct: NetworkAbilityMonitorStruct.selectNetworkAbilityStruct,
                    isRangeSelect: bytesInTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxNetworkRate: maxBytesIn,
                    maxNetworkRateName: maxInByteName,
                    useCache: useCache,
                    lineColor: bytesInTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: bytesInTraceRow.frame,
                }, bytesInTraceRow.must && bytesInTraceRow.args.isOffScreen ? bytesInTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    bytesInTraceRow.must = false;
                    if (bytesInTraceRow.args.isOffScreen == true) {
                        if (bytesInTraceRow.isHover) {
                            NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_NETWORK_ABILITY && it.name !== bytesInTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (bytesInTraceRow.dataList) bytesInTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(bytesInTraceRow)

        let bytesOutTraceRow = new TraceRow<NetworkAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        let maxBytesOut = maxList[0].maxOut;
        let maxOutByteName = this.networkMath(maxBytesOut);
        bytesOutTraceRow.rowParentId = `abilityMonitor`
        bytesOutTraceRow.rowHidden = !processRow.expansion
        bytesOutTraceRow.rowId = networkNameList[1]
        bytesOutTraceRow.rowType = TraceRow.ROW_TYPE_NETWORK_ABILITY
        bytesOutTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        bytesOutTraceRow.selectChangeHandler = this.selectChangeHandler;
        bytesOutTraceRow.style.height = '40px'
        bytesOutTraceRow.style.width = `100%`;
        bytesOutTraceRow.setAttribute('children', '');
        bytesOutTraceRow.name = 'Network ' + networkNameList[1];
        bytesOutTraceRow.supplier = () => queryBytesOutAbilityData();
        bytesOutTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu1`, `monitorNetwork1`, {
                    list: bytesOutTraceRow.must ? bytesOutTraceRow.dataList : undefined,
                    offscreen: bytesOutTraceRow.must ? bytesOutTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: bytesOutTraceRow.dpr,
                    isHover: bytesOutTraceRow.isHover,
                    hoverX: bytesOutTraceRow.hoverX,
                    hoverY: bytesOutTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: bytesOutTraceRow.canvasWidth,
                    canvasHeight: bytesOutTraceRow.canvasHeight,
                    hoverNetworkAbilityStruct: NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct,
                    selectNetworkAbilityStruct: NetworkAbilityMonitorStruct.selectNetworkAbilityStruct,
                    isRangeSelect: bytesOutTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxNetworkRate: maxBytesOut,
                    maxNetworkRateName: maxOutByteName,
                    useCache: useCache,
                    lineColor: bytesOutTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: bytesOutTraceRow.frame,
                }, bytesOutTraceRow.must && bytesOutTraceRow.args.isOffScreen ? bytesOutTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    bytesOutTraceRow.must = false;
                    if (bytesOutTraceRow.args.isOffScreen == true) {
                        if (bytesOutTraceRow.isHover) {
                            NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_NETWORK_ABILITY && it.name !== bytesOutTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (bytesOutTraceRow.dataList) bytesOutTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(bytesOutTraceRow)


        let packetInTraceRow = new TraceRow<NetworkAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        let maxPacketIn = maxList[0].maxPacketIn;
        let maxInPacketName = this.networkMath(maxPacketIn);
        packetInTraceRow.rowParentId = `abilityMonitor`
        packetInTraceRow.rowHidden = !processRow.expansion
        packetInTraceRow.rowId = networkNameList[2]
        packetInTraceRow.rowType = TraceRow.ROW_TYPE_NETWORK_ABILITY
        packetInTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        packetInTraceRow.selectChangeHandler = this.selectChangeHandler;
        packetInTraceRow.style.height = '40px'
        packetInTraceRow.style.width = `100%`;
        packetInTraceRow.setAttribute('children', '');
        packetInTraceRow.name = 'Network ' + networkNameList[2];
        packetInTraceRow.supplier = () => queryPacketsInAbilityData();
        packetInTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu2`, `monitorNetwork-Packet2`, {
                    list: packetInTraceRow.must ? packetInTraceRow.dataList : undefined,
                    offscreen: packetInTraceRow.must ? packetInTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: packetInTraceRow.dpr,
                    isHover: packetInTraceRow.isHover,
                    hoverX: packetInTraceRow.hoverX,
                    hoverY: packetInTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: packetInTraceRow.canvasWidth,
                    canvasHeight: packetInTraceRow.canvasHeight,
                    hoverNetworkAbilityStruct: NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct,
                    selectNetworkAbilityStruct: NetworkAbilityMonitorStruct.selectNetworkAbilityStruct,
                    isRangeSelect: packetInTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxNetworkRate: maxPacketIn,
                    maxNetworkRateName: maxInPacketName,
                    useCache: useCache,
                    lineColor: packetInTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: packetInTraceRow.frame,
                }, packetInTraceRow.must && packetInTraceRow.args.isOffScreen ? packetInTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    packetInTraceRow.must = false;
                    if (packetInTraceRow.args.isOffScreen == true) {
                        if (packetInTraceRow.isHover) {
                            NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_NETWORK_ABILITY && it.name !== packetInTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (packetInTraceRow.dataList) packetInTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(packetInTraceRow)


        let packetOutTraceRow = new TraceRow<NetworkAbilityMonitorStruct>({
            canvasNumber: 1,
            alpha: false,
            contextId: '2d',
            isOffScreen: SpSystemTrace.isCanvasOffScreen
        });
        let maxPacketOut = maxList[0].maxPacketOut;
        let maxOutPacketName = this.networkMath(maxPacketOut);
        packetOutTraceRow.rowParentId = `abilityMonitor`
        packetOutTraceRow.rowHidden = !processRow.expansion
        packetOutTraceRow.rowId = networkNameList[3]
        packetOutTraceRow.rowType = TraceRow.ROW_TYPE_NETWORK_ABILITY
        packetOutTraceRow.favoriteChangeHandler = this.favoriteChangeHandler;
        packetOutTraceRow.selectChangeHandler = this.selectChangeHandler;
        packetOutTraceRow.style.height = '40px'
        packetOutTraceRow.style.width = `100%`;
        packetOutTraceRow.setAttribute('children', '');
        packetOutTraceRow.name = 'Network ' + networkNameList[3];
        packetOutTraceRow.supplier = () => queryPacketsOutAbilityData();
        packetOutTraceRow.onThreadHandler = (useCache) => {
            procedurePool.submitWithName(`cpu3`, `monitorNetwork3`, {
                    list: packetOutTraceRow.must ? packetOutTraceRow.dataList : undefined,
                    offscreen: packetOutTraceRow.must ? packetOutTraceRow.offscreen[0] : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: packetOutTraceRow.dpr,
                    isHover: packetOutTraceRow.isHover,
                    hoverX: packetOutTraceRow.hoverX,
                    hoverY: packetOutTraceRow.hoverY,
                    flagMoveInfo: this.hoverFlag,
                    flagSelectedInfo: this.selectFlag,
                    canvasWidth: packetOutTraceRow.canvasWidth,
                    canvasHeight: packetOutTraceRow.canvasHeight,
                    hoverNetworkAbilityStruct: NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct,
                    selectNetworkAbilityStruct: NetworkAbilityMonitorStruct.selectNetworkAbilityStruct,
                    isRangeSelect: packetOutTraceRow.rangeSelect,
                    rangeSelectObject: TraceRow.rangeSelectObject,
                    maxNetworkRate: maxPacketOut,
                    maxNetworkRateName: maxOutPacketName,
                    useCache: useCache,
                    lineColor: packetOutTraceRow.getLineColor(),
                    startNS: TraceRow.range?.startNS || 0,
                    endNS: TraceRow.range?.endNS || 0,
                    totalNS: TraceRow.range?.totalNS || 0,
                    slicesTime: TraceRow.range?.slicesTime,
                    range: TraceRow.range,
                    frame: packetOutTraceRow.frame,
                }, packetOutTraceRow.must && packetOutTraceRow.args.isOffScreen ? packetOutTraceRow.offscreen[0] : undefined, (res: any, hover: any) => {
                    packetOutTraceRow.must = false;
                    if (packetOutTraceRow.args.isOffScreen == true) {
                        if (packetOutTraceRow.isHover) {
                            NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_NETWORK_ABILITY && it.name !== packetOutTraceRow.name).forEach(it => it.draw(true));
                        }
                        if (packetOutTraceRow.dataList) packetOutTraceRow.dataList.length = 0;
                        return;
                    }
                }
            )
        }
        this.rowsEL?.appendChild(packetOutTraceRow)
        let durTime = new Date().getTime() - time;
        info('The time to load the Ability Network is: ', durTime)
    }
}

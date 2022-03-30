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
    DbPool,
    getAsyncEvents,
    getCpuUtilizationRate,
    getFps,
    getFunDataByTid,
    queryCpuData,
    queryCpuFreq,
    queryCpuFreqData,
    queryCpuMax,
    queryCpuMaxFreq,
    queryHeapByPid,
    queryHeapPid,
    queryProcess,
    queryProcessData,
    queryProcessMem,
    queryProcessMemData,
    queryProcessThreads,
    queryThreadData,
    queryTotalTime,
    threadPool
} from "../database/SqlLite.js";
import {TraceRow} from "./trace/base/TraceRow.js";
import {TimerShaftElement} from "./trace/TimerShaftElement.js";
import {TimeRange} from "./trace/timer-shaft/RangeRuler.js";
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
import {SportRuler} from "./trace/timer-shaft/SportRuler.js";
import {Utils} from "./trace/base/Utils.js";
import {SpApplication} from "../SpApplication.js";

@element('sp-system-trace')
export class SpSystemTrace extends BaseElement {
    static scrollViewWidth = 0
    static isCanvasOffScreen = true;

    rowsEL: HTMLDivElement | undefined | null;
    visibleRows: Array<TraceRow<any>> = [];
    keyboardEnable = true;
    currentRowType = "";
    private timerShaftEL: TimerShaftElement | null | undefined;
    private traceSheetEL: TraceSheet | undefined | null;
    private rangeSelect!: RangeSelect;

    private processThreads: Array<ThreadStruct> = []
    private processAsyncEvent: Array<ProcessMemStruct> = []
    private processMem: Array<any> = []

    initElements(): void {
        this.rowsEL = this.shadowRoot?.querySelector<HTMLDivElement>('.rows')
        this.timerShaftEL = this.shadowRoot?.querySelector('.timer-shaft')
        this.traceSheetEL = this.shadowRoot?.querySelector('.trace-sheet')
        this.rangeSelect = new RangeSelect();
        this.rangeSelect.rowsEL = this.rowsEL;
        document?.addEventListener("flag-change", (event: any) => {
            this.timerShaftEL?.modifyList(event.detail.type, event.detail.flagObj)
            if (event.detail.type == "remove") {
                this.traceSheetEL?.setAttribute("mode", 'hidden');
            }
        })
        document?.addEventListener("flag-draw", (event: any) => {
            if (event.detail == null) {
            }
        })

        SpSystemTrace.scrollViewWidth = this.getScrollWidth()
        this.rangeSelect.selectHandler = (rows) => {
            if (rows.length > 0) {
                this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach(row => row.checkType = "0")
                rows.forEach(it => it.checkType = "2")
            } else {
                this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach(row => row.checkType = "-1")
                return
            }
            let selection = new SelectionParam();
            selection.cpus = [];
            selection.threadIds = [];
            selection.funTids = [];
            selection.trackIds = [];
            selection.leftNs = 0;
            selection.rightNs = 0;
            rows.forEach(it => {
                if (it.rowType == TraceRow.ROW_TYPE_CPU) {
                    selection.cpus.push(parseInt(it.rowId!))
                } else if (it.rowType == TraceRow.ROW_TYPE_THREAD) {
                    selection.threadIds.push(parseInt(it.rowId!))
                } else if (it.rowType == TraceRow.ROW_TYPE_FUNC) {
                    selection.funTids.push(parseInt(it.rowId!))
                } else if (it.rowType == TraceRow.ROW_TYPE_MEM) {
                    selection.trackIds.push(parseInt(it.rowId!))
                } else if (it.rowType == TraceRow.ROW_TYPE_FPS) {
                    selection.hasFps = true;
                } else if (it.rowType == TraceRow.ROW_TYPE_HEAP) {
                    selection.heapIds.push(parseInt(it.rowId!))
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
        }).observe(this)
    }

    getScrollWidth() {
        let noScroll, scroll, oDiv = document.createElement('div');
        oDiv.style.cssText = 'position:absolute; top:-1000px;     width:100px; height:100px; overflow:hidden;';
        noScroll = document.body.appendChild(oDiv).clientWidth;
        oDiv.style.overflowY = 'scroll';
        scroll = oDiv.clientWidth;
        document.body.removeChild(oDiv);
        return noScroll - scroll + 1;
    }

    getVisibleRows(): Array<TraceRow<any>> {
        let scrollTop = this.rowsEL?.scrollTop || 0;
        let scrollHeight = this.rowsEL?.clientHeight || 0;
        let res = [...this.rowsEL!.querySelectorAll<TraceRow<any>>("trace-row")].filter((it) => {
            let tr = (it as TraceRow<any>);
            let top = it.offsetTop - (this.rowsEL?.offsetTop || 0);
            if (top + it.clientHeight > scrollTop && top + it.clientHeight < scrollTop + scrollHeight + it.clientHeight) {
                it.sleeping = false;
                return true
            } else {
                it.sleeping = true;
                return false;
            }
        })
        this.visibleRows = res;
        return res;
    }

    timerShaftELRangeChange = (e: any) => {
        TraceRow.range = e;
        let scrollTop = this.rowsEL?.scrollTop || 0
        let scrollHeight = this.rowsEL?.clientHeight || 0
        for (let i = 0; i < this.visibleRows.length; i++) {
            this.visibleRows[i].dataListCache.length = 0;
            this.visibleRows[i].isHover = false;
            this.hoverStructNull();
            this.visibleRows[i].draw();
        }
    }

    rowsElOnScroll = (e: any) => {
        this.hoverStructNull();
        this.visibleRows = this.getVisibleRows();
        for (let index = 0; index < this.visibleRows.length; index++) {
            if (index == 0 || index == this.visibleRows.length - 1) {
                this.visibleRows[index].isHover = false;
                this.visibleRows[index].dataListCache.length = 0;
            }
        }
    }
    documentOnMouseDown = (ev: MouseEvent) => {
        if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft) {
            this.rangeSelect.mouseDown(ev)
            this.timerShaftEL?.documentOnMouseDown(ev)
            this.visibleRows.forEach(it => {
                it.draw();
            })
        }
    }
    documentOnMouseUp = (ev: MouseEvent) => {
        if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft) {
            this.rangeSelect.mouseUp(ev);
            this.timerShaftEL?.documentOnMouseUp(ev)
        }
    }
    documentOnMouseOut = (ev: MouseEvent) => {
        if (ev.offsetX > this.timerShaftEL!.canvas!.offsetLeft) {
            this.timerShaftEL?.documentOnMouseOut(ev)
        }
    }

    documentOnKeyPress = (ev: KeyboardEvent) => {
        this.keyboardEnable && this.timerShaftEL!.documentOnKeyPress(ev);
    }

    documentOnKeyUp = (ev: KeyboardEvent) => {
        this.keyboardEnable && this.timerShaftEL!.documentOnKeyUp(ev);
    }

    documentOnMouseMove = (ev: MouseEvent) => {
        let rows = this.visibleRows;
        if (this.timerShaftEL?.isScaling()) {
            rows.forEach(it => it.isHover = false);
            this.hoverStructNull();
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
                let x = ev.offsetX - (tr.canvasContainer?.offsetLeft || 0);
                let y = ev.offsetY - (tr.canvasContainer?.offsetTop || 0) + (this.rowsEL?.scrollTop || 0);

                if (x > tr.frame.x && x < tr.frame.x + tr.frame.width && y > tr.frame.y && y < tr.frame.y + tr.frame.height) {
                    tr.isHover = true;
                    tr.hoverX = x;
                    tr.hoverY = y;
                    if (!SpSystemTrace.isCanvasOffScreen) this.hoverStructNull();
                    if (tr.rowType === TraceRow.ROW_TYPE_CPU) {
                        this.currentRowType = TraceRow.ROW_TYPE_CPU;
                        if (!SpSystemTrace.isCanvasOffScreen) CpuStruct.hoverCpuStruct = tr.onMouseHover(x, y);
                        if (CpuStruct.hoverCpuStruct) {
                            tr.tip = `<span>P：${CpuStruct.hoverCpuStruct.processName || "Process"} [${CpuStruct.hoverCpuStruct.processId}]</span><span>T：${CpuStruct.hoverCpuStruct.name} [${CpuStruct.hoverCpuStruct.tid}]</span>`;
                        }
                        tr.setTipLeft(x, CpuStruct.hoverCpuStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_CPU_FREQ) {
                        this.currentRowType = TraceRow.ROW_TYPE_CPU_FREQ;
                        if (!SpSystemTrace.isCanvasOffScreen) CpuFreqStruct.hoverCpuFreqStruct = tr.onMouseHover(x, y);
                        if (CpuFreqStruct.hoverCpuFreqStruct) {
                            tr.tip = `<span>${ColorUtils.formatNumberComma(CpuFreqStruct.hoverCpuFreqStruct.value!)} kHz</span>`
                        }
                        tr.setTipLeft(x, CpuFreqStruct.hoverCpuFreqStruct)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_THREAD) {
                        this.currentRowType = TraceRow.ROW_TYPE_THREAD;
                        if (!SpSystemTrace.isCanvasOffScreen) ThreadStruct.hoverThreadStruct = tr.onMouseHover(x, y, false);
                    } else if (tr.rowType === TraceRow.ROW_TYPE_FUNC) {
                        this.currentRowType = TraceRow.ROW_TYPE_FUNC;
                        if (!SpSystemTrace.isCanvasOffScreen) FuncStruct.hoverFuncStruct = tr.onMouseHover(x, y, false)
                    } else if (tr.rowType === TraceRow.ROW_TYPE_HEAP) {
                        this.currentRowType = TraceRow.ROW_TYPE_HEAP;
                        if (!SpSystemTrace.isCanvasOffScreen) HeapStruct.hoverHeapStruct = tr.onMouseHover(x, y, false)
                        if (HeapStruct.hoverHeapStruct) {
                            tr.tip = `<span>${Utils.getByteWithUnit(HeapStruct.hoverHeapStruct.heapsize!)}</span>`
                        }
                        tr.setTipLeft(x, HeapStruct.hoverHeapStruct)
                    } else {
                        this.hoverStructNull();
                    }
                    tr.draw(true);
                } else {
                    tr.onMouseLeave(x, y);
                    tr.isHover = false;
                    tr.hoverX = x;
                    tr.hoverY = y;
                    if (!SpSystemTrace.isCanvasOffScreen) this.hoverStructNull();
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
                        rows[i].draw(true);
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
    }

    selectStructNull() {
        CpuStruct.selectCpuStruct = undefined;
        CpuStruct.wakeupBean = null;
        CpuFreqStruct.selectCpuFreqStruct = undefined;
        ThreadStruct.selectThreadStruct = undefined;
        FuncStruct.selectFuncStruct = undefined;
    }

    documentOnClick = (ev: MouseEvent) => {
        if (this.rangeSelect.isDrag()) {
            return;
        }
        this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach(it => it.rangeSelect = false)
        this.selectStructNull();
        if (CpuStruct.hoverCpuStruct) {
            CpuStruct.selectCpuStruct = CpuStruct.hoverCpuStruct
            this.traceSheetEL?.displayCpuData(CpuStruct.hoverCpuStruct, (wakeUpBean) => {
                CpuStruct.wakeupBean = wakeUpBean;
                this.visibleRows.forEach(it => it.draw());
            })
        } else if (ThreadStruct.hoverThreadStruct) {
            ThreadStruct.selectThreadStruct = ThreadStruct.hoverThreadStruct;
            this.traceSheetEL?.displayThreadData(ThreadStruct.hoverThreadStruct)
        } else if (FuncStruct.hoverFuncStruct) {
            FuncStruct.selectFuncStruct = FuncStruct.hoverFuncStruct;
            this.traceSheetEL?.displayFuncData(FuncStruct.hoverFuncStruct)
        } else if (SportRuler.rulerFlagObj) {

        } else {
            this.traceSheetEL?.setAttribute("mode", 'hidden');
            this.getVisibleRows().forEach(it => it.draw(true));
        }
        this.documentOnMouseMove(ev)
    }

    connectedCallback() {
        this.timerShaftEL!.rangeChangeHandler = this.timerShaftELRangeChange;
        this.rowsEL?.addEventListener('scroll', this.rowsElOnScroll)
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

    goProcess(rowId: string, rowParentId: string, rowType: string) {
        let row = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowParentId}'][folder]`);
        row!.expansion = true
        let rootRow = this.shadowRoot!.querySelector<TraceRow<any>>(`trace-row[row-id='${rowId}'][row-type='${rowType}']`);
        this.rowsEL!.scrollTop = rootRow!.offsetTop - this.rowsEL!.offsetTop - this.rowsEL!.offsetHeight + rootRow!.offsetHeight
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
        this.init({url: url}, progress).then((res) => {
            let scrollTop = this.rowsEL?.scrollTop || 0
            let scrollHeight = this.rowsEL?.clientHeight || 0
            this.rowsEL?.querySelectorAll("trace-row").forEach((it: any) => {
                let top = it.offsetTop - (this.rowsEL?.offsetTop || 0);
                if (top + it.clientHeight > scrollTop && top + it.clientHeight < scrollTop + scrollHeight + it.clientHeight) {
                    (it as TraceRow<any>).dataListCache.length = 0;
                }
            })
            if (complete) {
                complete(res);
            }
        })
    }

    loadDatabaseArrayBuffer(buf: ArrayBuffer, progress: ((name: string, percent: number) => void), complete?: ((res: { status: boolean, msg: string }) => void) | undefined) {
        this.init({buf}, progress).then((res) => {
            let scrollTop = this.rowsEL?.scrollTop || 0
            let scrollHeight = this.rowsEL?.clientHeight || 0
            this.rowsEL?.querySelectorAll("trace-row").forEach((it: any) => {
                let top = it.offsetTop - (this.rowsEL?.offsetTop || 0);
                if (top + it.clientHeight > scrollTop && top + it.clientHeight < scrollTop + scrollHeight + it.clientHeight) {
                    (it as TraceRow<any>).dataListCache.length = 0;
                }
            })
            if (complete) {
                complete(res);
            }
        })
    }

    init = async (param: { buf?: ArrayBuffer, url?: string }, progress: Function) => {
        progress("Load database", 6);
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
        if (this.rowsEL) this.rowsEL.innerHTML = ''
        this.traceSheetEL?.setAttribute("mode", "hidden")
        progress("rest timershaft", 8);
        this.timerShaftEL?.reset();
        progress("clear cache", 10);
        procedurePool.clearCache();

        progress("load process threads", 50);
        this.processThreads = await queryProcessThreads();
        progress("process memory", 60);
        this.processMem = await queryProcessMem()
        progress("async event", 63);
        this.processAsyncEvent = await getAsyncEvents()
        progress("time range", 65);
        await this.initTotalTime();
        progress("cpu", 70);
        await this.initCpu();
        progress("cpu rate", 75);
        await this.initCpuRate();
        progress("cpu freq", 80);
        await this.initCpuFreq();
        progress("fps", 85);
        await this.initFPS();
        progress("process", 90);
        await this.initProcess();
        progress("display", 95);
        this.getVisibleRows().forEach(it => {
            it.draw();
        });
        this.rowsEL?.querySelectorAll<TraceRow<any>>("trace-row").forEach((it: any) => {
            it.addEventListener('expansion-change', () => {
                this.getVisibleRows().forEach(it2 => it2.draw());
            })
        })
        progress("completed", 100);
        return {status: true, msg: "success"}
    }

    initCpuRate = async () => {
        let rates = await getCpuUtilizationRate(0, this.timerShaftEL?.totalNS || 0);
        if (this.timerShaftEL) this.timerShaftEL.cpuUsage = rates;
    }
    initTotalTime = async () => {
        let res = await queryTotalTime();
        if (this.timerShaftEL) {
            this.timerShaftEL.totalNS = res[0].total
            this.timerShaftEL.loadComplete = true;
        }
    }
    initCpu = async () => {
        let array = await queryCpuMax();
        if (array && array.length > 0 && array[0]) {
            let cpuMax = array[0].cpu
            CpuStruct.cpuCount = cpuMax + 1;
            for (let i1 = 0; i1 < CpuStruct.cpuCount; i1++) {
                const cpuId = i1;
                let traceRow = new TraceRow<CpuStruct>({
                    alpha: true,
                    contextId: '2d',
                    isOffScreen: SpSystemTrace.isCanvasOffScreen
                });
                traceRow.rowId = `${cpuId}`
                traceRow.rowType = TraceRow.ROW_TYPE_CPU
                traceRow.rowParentId = ''
                traceRow.style.height = '40px'
                traceRow.name = `Cpu ${cpuId}`
                traceRow.supplier = () => queryCpuData(cpuId, 0, this.timerShaftEL?.totalNS || 0)
                traceRow.onThreadHandler = ((ctx: CanvasRenderingContext2D, useCache: boolean) => {
                    // _measureCpu("cpu",`cpu${cpuId}`,traceRow.must ? traceRow.dataList : undefined, TraceRow.range?.startNS || 0,TraceRow.range?.endNS || 0,TraceRow.range?.totalNS || 0,traceRow.frame,ctx,traceRow);
                    if (traceRow.dataListCache && traceRow.dataListCache.length > 0 && !traceRow.args.isOffScreen) {
                        traceRow.clearCanvas(ctx);
                        ctx.beginPath();
                        traceRow.drawLines(ctx);
                        for (let i = 0; i < traceRow.dataListCache.length; i++) {
                            CpuStruct.draw(ctx, traceRow.dataListCache[i])
                        }
                        traceRow.drawSelection(ctx);
                        ctx.closePath();
                        return;
                    }
                    procedurePool.submitWithName(`cpu${cpuId % procedurePool.cpusLen.length}`, `cpu${cpuId}`, {
                        list: traceRow.must ? traceRow.dataList : undefined,
                        offscreen: traceRow.must ? traceRow.offscreen : undefined,
                        dpr: traceRow.dpr,
                        xs: TraceRow.range?.xs,
                        isHover: traceRow.isHover,
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
                        frame: traceRow.frame
                    }, traceRow.must && traceRow.args.isOffScreen ? traceRow.offscreen : undefined, (res: any, hover: any) => {
                        traceRow.must = false;
                        if (traceRow.args.isOffScreen == true) {
                            if (traceRow.isHover) {
                                CpuStruct.hoverCpuStruct = hover;
                                this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU && it.name !== traceRow.name).forEach(it => it.draw(true));
                            }
                            return;
                        }
                        traceRow.dataListCache = [...res];
                        traceRow.clearCanvas(ctx);
                        ctx.beginPath();
                        traceRow.drawLines(ctx);
                        for (let re of res) {
                            CpuStruct.draw(ctx, re)
                        }

                        traceRow.drawSelection(ctx);
                        ctx.closePath();

                    })
                })
                this.rowsEL?.appendChild(traceRow)
            }
        }
    }

    initCpuFreq = async () => {
        let freqList = await queryCpuFreq();
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
                alpha: true,
                contextId: '2d',
                isOffScreen: SpSystemTrace.isCanvasOffScreen
            });
            traceRow.rowId = `${it.cpu}`
            traceRow.rowType = TraceRow.ROW_TYPE_CPU_FREQ
            traceRow.rowParentId = ''
            traceRow.style.height = '40px'
            traceRow.name = `Cpu ${it.cpu} Frequency`;
            traceRow.supplier = () => queryCpuFreqData(it.cpu)
            traceRow.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
                if (traceRow.dataListCache && traceRow.dataListCache.length > 0 && !traceRow.args.isOffScree) {
                    traceRow.clearCanvas(ctx);
                    traceRow.drawLines(ctx);
                    ctx.beginPath();
                    for (let i = 0; i < traceRow.dataListCache.length; i++) {
                        CpuFreqStruct.draw(ctx, traceRow.dataListCache[i])
                    }
                    traceRow.drawSelection(ctx);
                    ctx.closePath();
                    let s = CpuFreqStruct.maxFreqName
                    let textMetrics = ctx.measureText(s);
                    ctx.globalAlpha = 0.8
                    ctx.fillStyle = "#f0f0f0"
                    ctx.fillRect(0, 5, textMetrics.width + 8, 18)
                    ctx.globalAlpha = 1
                    ctx.fillStyle = "#333"
                    ctx.textBaseline = "middle"
                    ctx.fillText(s, 4, 5 + 9)
                    return;
                }
                procedurePool.submitWithName(`process${it.cpu % procedurePool.processLen.length}`, `freq${it.cpu}`, {
                    list: traceRow.must ? traceRow.dataList : undefined,
                    offscreen: traceRow.must ? traceRow.offscreen : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: traceRow.dpr,
                    isHover: traceRow.isHover,
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
                    frame: traceRow.frame
                }, traceRow.must && traceRow.args.isOffScreen ? traceRow.offscreen : undefined, (res: any, hover: any) => {
                    traceRow.must = false;
                    if (traceRow.args.isOffScreen == true) {
                        if (traceRow.isHover) {
                            CpuFreqStruct.hoverCpuFreqStruct = hover;
                            this.visibleRows.filter(it => it.rowType === TraceRow.ROW_TYPE_CPU_FREQ && it.name !== traceRow.name).forEach(it => it.draw(true));
                        }
                        return;
                    }
                    traceRow.dataListCache = [...res];
                    traceRow.clearCanvas(ctx);
                    traceRow.drawLines(ctx);
                    ctx.beginPath();
                    for (let re of res) {
                        CpuFreqStruct.draw(ctx, re)
                    }
                    traceRow.drawSelection(ctx);
                    ctx.closePath();
                    let s = CpuFreqStruct.maxFreqName
                    let textMetrics = ctx.measureText(s);
                    ctx.globalAlpha = 0.8
                    ctx.fillStyle = "#f0f0f0"
                    ctx.fillRect(0, 5, textMetrics.width + 8, 18)
                    ctx.globalAlpha = 1
                    ctx.fillStyle = "#333"
                    ctx.textBaseline = "middle"
                    ctx.fillText(s, 4, 5 + 9)
                })
            }
            this.rowsEL?.appendChild(traceRow)
        }
    }

    initFPS = async () => {
        let fpsRow = new TraceRow<FpsStruct>({alpha: true, contextId: '2d', isOffScreen: true});
        fpsRow.rowId = `fps`
        fpsRow.rowType = TraceRow.ROW_TYPE_FPS
        fpsRow.rowParentId = ''
        FpsStruct.maxFps = 0
        fpsRow.style.height = '40px'
        fpsRow.name = "FPS"
        fpsRow.supplier = () => getFps()
        fpsRow.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
            procedurePool.submitWithName(`process0`, `fps0`, {
                list: fpsRow.must ? fpsRow.dataList : undefined,
                offscreen: fpsRow.must ? fpsRow.offscreen : undefined,
                xs: TraceRow.range?.xs,
                dpr: fpsRow.dpr,
                isHover: fpsRow.isHover,
                hoverX: fpsRow.hoverX,
                hoverY: fpsRow.hoverY,
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
                frame: fpsRow.frame
            }, fpsRow.must && fpsRow.args.isOffScreen ? fpsRow.offscreen : undefined, (res: any, hover: any) => {
                fpsRow.must = false;
                if (fpsRow.args.isOffScreen == true) {
                    return;
                }
            })
        }
        this.rowsEL?.appendChild(fpsRow)
    }

    initProcess = async () => {
        let processList = await queryProcess();
        let heapPidList = await queryHeapPid()
        for (let i = 0; i < processList.length; i++) {
            const it = processList[i];
            let processRow = new TraceRow<ProcessStruct>({
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
            processRow.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
                if (processRow.dataListCache && processRow.dataListCache.length > 0 && !processRow.args.isOffScreen) {
                    processRow.clearCanvas(ctx);
                    processRow.drawLines(ctx);
                    ctx.beginPath();
                    for (let i = 0; i < processRow.dataListCache.length; i++) {
                        ProcessStruct.draw(ctx, processRow.dataListCache[i])
                    }
                    processRow.drawSelection(ctx);
                    ctx.closePath();
                    return;
                }
                procedurePool.submitWithName(`process${(processRow.index) % procedurePool.processLen.length}`, `process ${processRow.index} ${it.processName}`, {
                    list: processRow.must ? processRow.dataList : undefined,
                    offscreen: processRow.must ? processRow.offscreen : undefined,
                    xs: TraceRow.range?.xs,
                    dpr: processRow.dpr,
                    isHover: processRow.isHover,
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
                    frame: processRow.frame
                }, processRow.must && processRow.args.isOffScreen ? processRow.offscreen : undefined, (res: any) => {
                    processRow.must = false;
                    if (processRow.args.isOffScreen == true) {
                        return;
                    }
                    processRow.dataListCache = [...res];
                    processRow.clearCanvas(ctx);
                    processRow.drawLines(ctx);
                    ctx.beginPath();
                    for (let re of res) {
                        ProcessStruct.draw(ctx, re)
                    }
                    processRow.drawSelection(ctx);
                    ctx.closePath();
                })
            }
            this.rowsEL?.appendChild(processRow)
            if (heapPidList != undefined && Array.isArray(heapPidList) && heapPidList.filter((item) => {
                return item.pid == it.pid
            }).length > 0) {
                let heapPid = heapPidList.filter((item) => {
                    return item.pid == it.pid
                })[0];
                let allHeapRow = new TraceRow<HeapStruct>({alpha: false, contextId: '2d', isOffScreen: true});
                allHeapRow.rowParentId = `${it.pid}`
                allHeapRow.rowHidden = !processRow.expansion
                allHeapRow.style.height = '40px'
                allHeapRow.name = "All Heap Allocations";
                allHeapRow.rowId = heapPid.ipid
                allHeapRow.folder = false;
                allHeapRow.rowType = TraceRow.ROW_TYPE_HEAP
                allHeapRow.setAttribute('children', '')
                allHeapRow.supplier = () => queryHeapByPid(0, TraceRow.range?.totalNS || 0, heapPid.ipid || 0)
                allHeapRow.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
                    procedurePool.submitWithName(`process0`, `heap0`, {
                        list: allHeapRow.must ? allHeapRow.dataList : undefined,
                        offscreen: allHeapRow.must ? allHeapRow.offscreen : undefined,
                        xs: TraceRow.range?.xs,
                        dpr: allHeapRow.dpr,
                        isHover: allHeapRow.isHover,
                        hoverX: allHeapRow.hoverX,
                        hoverY: allHeapRow.hoverY,
                        canvasWidth: allHeapRow.canvasWidth,
                        canvasHeight: allHeapRow.canvasHeight,
                        isRangeSelect: allHeapRow.rangeSelect,
                        rangeSelectObject: TraceRow.rangeSelectObject,
                        wakeupBean: CpuStruct.wakeupBean,
                        useCache: useCache,
                        lineColor: allHeapRow.getLineColor(),
                        startNS: TraceRow.range?.startNS || 0,
                        endNS: TraceRow.range?.endNS || 0,
                        totalNS: TraceRow.range?.totalNS || 0,
                        frame: allHeapRow.frame
                    }, allHeapRow.must && allHeapRow.args.isOffScreen ? allHeapRow.offscreen : undefined, (res: any, hover: any) => {
                        allHeapRow.must = false;
                        if (allHeapRow.args.isOffScreen == true) {
                            if (allHeapRow.isHover) {
                                HeapStruct.hoverHeapStruct = hover;
                            }
                            return;
                        }
                    })
                }
                this.rowsEL?.appendChild(allHeapRow)
            }

            let processMem = this.processMem.filter(mem => mem.pid === it.pid);
            processMem.forEach(mem => {
                let row = new TraceRow<ProcessMemStruct>({
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
                row.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
                    if (row.dataListCache && row.dataListCache.length > 0 && !row.args.isOffScreen) {
                        row.clearCanvas(ctx);
                        row.drawLines(ctx);
                        ctx.beginPath();
                        for (let i = 0; i < row.dataListCache.length; i++) {
                            ProcessMemStruct.draw(ctx, row.dataListCache[i])
                        }
                        row.drawSelection(ctx);
                        ctx.closePath();
                        return;
                    }
                    procedurePool.submitWithName(`cpu${mem.trackId % procedurePool.cpusLen.length}`, `mem ${mem.trackId} ${mem.trackName}`, {
                        list: row.must ? row.dataList : undefined,
                        offscreen: row.must ? row.offscreen : undefined,
                        dpr: row.dpr,
                        xs: TraceRow.range?.xs,
                        isHover: row.isHover,
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
                        frame: row.frame
                    }, row.must && row.args.isOffScreen ? row.offscreen : undefined, (res: any) => {
                        row.must = false;

                        if (row.args.isOffScreen == true) {
                            return;
                        }
                        row.dataListCache = [...res];
                        row.clearCanvas(ctx);
                        row.drawLines(ctx);
                        ctx.beginPath();
                        for (let re of res) {
                            ProcessMemStruct.draw(ctx, re)
                        }
                        row.drawSelection(ctx);
                        ctx.closePath();
                    })
                }
                this.rowsEL?.appendChild(row)
            });
            let threads = this.processThreads.filter(thread => thread.pid === it.pid && thread.tid != 0 && thread.threadName != null);
            threads.forEach((thread, i) => {
                let threadRow = new TraceRow<ThreadStruct>({
                    alpha: false,
                    contextId: '2d',
                    isOffScreen: SpSystemTrace.isCanvasOffScreen
                });
                threadRow.rowId = `${thread.tid}`
                threadRow.rowType = TraceRow.ROW_TYPE_THREAD
                threadRow.rowParentId = `${it.pid}`
                threadRow.rowHidden = !processRow.expansion
                threadRow.style.height = '40px'
                threadRow.style.width = `100%`;
                threadRow.name = `${thread.threadName} ${thread.tid}`;
                threadRow.setAttribute('children', '')
                threadRow.supplier = () => queryThreadData(thread.tid || 0).then(res => {
                    getFunDataByTid(thread.tid || 0).then((funs: Array<FuncStruct>) => {
                        if (funs.length > 0) {
                            let maxHeight = (Math.max(...funs.map(it => it.depth || 0)) + 1) * 20 + 20;
                            let funcRow = new TraceRow<FuncStruct>({
                                alpha: false,
                                contextId: '2d',
                                isOffScreen: SpSystemTrace.isCanvasOffScreen
                            });
                            funcRow.rowId = `${thread.tid}`
                            funcRow.rowType = TraceRow.ROW_TYPE_FUNC
                            funcRow.rowParentId = `${it.pid}`
                            funcRow.rowHidden = !processRow.expansion
                            funcRow.style.width = `100%`;
                            funcRow.setAttribute("height", `${maxHeight}`);
                            funcRow.name = `${thread.threadName} ${thread.tid}`;
                            funcRow.setAttribute('children', '')
                            funcRow.supplier = () => new Promise((resolve, reject) => resolve(funs))
                            funcRow.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
                                if (funcRow.dataListCache && funcRow.dataListCache.length > 0 && !funcRow.args.isOffScreen) {
                                    funcRow.clearCanvas(ctx);
                                    funcRow.drawLines(ctx);
                                    ctx.beginPath();
                                    for (let i = 0; i < funcRow.dataListCache.length; i++) {
                                        FuncStruct.draw(ctx, funcRow.dataListCache[i])
                                    }
                                    funcRow.drawSelection(ctx);
                                    ctx.closePath();
                                    return;
                                }
                                procedurePool.submitWithName(`cpu${(thread.tid || 0) % procedurePool.cpusLen.length}`, `func ${thread.tid} ${thread.threadName}`, {
                                    list: funcRow.must ? funcRow.dataList : undefined,
                                    offscreen: funcRow.must ? funcRow.offscreen : undefined,
                                    dpr: funcRow.dpr,
                                    xs: TraceRow.range?.xs,
                                    isHover: funcRow.isHover,
                                    hoverX: funcRow.hoverX,
                                    hoverY: funcRow.hoverY,
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
                                    frame: funcRow.frame
                                }, funcRow.must && funcRow.args.isOffScreen ? funcRow.offscreen : undefined, (res: any, hover: any) => {
                                    funcRow.must = false;
                                    if (funcRow.args.isOffScreen == true) {
                                        if (funcRow.isHover) {
                                            FuncStruct.hoverFuncStruct = hover;
                                        }
                                        return;
                                    }
                                    funcRow.dataListCache = [...res];
                                    funcRow.clearCanvas(ctx);
                                    funcRow.drawLines(ctx);
                                    ctx.beginPath();
                                    for (let re of res) {
                                        FuncStruct.draw(ctx, re)
                                    }
                                    funcRow.drawSelection(ctx);
                                    ctx.closePath();
                                })
                            }
                            this.insertAfter(funcRow, threadRow)
                            funcRow.draw();
                            this.getVisibleRows();
                        }
                    })
                    return res;
                })
                threadRow.onThreadHandler = (ctx: CanvasRenderingContext2D, useCache) => {
                    if (threadRow.dataListCache && threadRow.dataListCache.length > 0 && !threadRow.args.isOffScreen) {
                        threadRow.clearCanvas(ctx);
                        threadRow.drawLines(ctx);
                        ctx.beginPath();
                        for (let i = 0; i < threadRow.dataListCache.length; i++) {
                            ThreadStruct.draw(ctx, threadRow.dataListCache[i])
                        }
                        threadRow.drawSelection(ctx);
                        ctx.closePath();
                        return;
                    }
                    procedurePool.submitWithName(`cpu${(thread.tid || 0) % procedurePool.cpusLen.length}`, `thread ${thread.tid} ${thread.threadName}`, {
                        list: threadRow.must ? threadRow.dataList : undefined,
                        offscreen: threadRow.must ? threadRow.offscreen : undefined,
                        dpr: threadRow.dpr,
                        xs: TraceRow.range?.xs,
                        isHover: threadRow.isHover,
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
                        frame: threadRow.frame
                    }, threadRow.must && threadRow.args.isOffScreen ? threadRow.offscreen : undefined, (res: any, hover: any) => {
                        threadRow.must = false;
                        if (threadRow.args.isOffScreen == true) {
                            if (threadRow.isHover) {
                                ThreadStruct.hoverThreadStruct = hover;
                            }
                            return;
                        }
                        threadRow.dataListCache = [...res];
                        threadRow.clearCanvas(ctx);
                        threadRow.drawLines(ctx);
                        ctx.beginPath();
                        for (let re of res) {
                            ThreadStruct.draw(ctx, re)
                        }
                        threadRow.drawSelection(ctx);
                        ctx.closePath();
                    })
                }
                this.rowsEL?.appendChild(threadRow)
            })
        }
    }

    insertAfter(newEl: HTMLElement, targetEl: HTMLElement) {
        let parentEl = targetEl.parentNode;
        if (parentEl!.lastChild == targetEl) {
            parentEl!.appendChild(newEl);
        } else {
            parentEl!.insertBefore(newEl, targetEl.nextSibling);
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
.rows{
     color: #fff;
    display: flex;
    box-sizing: border-box;
    flex-direction: column;
    overflow: overlay;
    max-height: calc(100vh - 147px - 48px);
    flex: 1;
    width: 100%;
    background: var(--dark-background4,#ffffff);
    scroll-behavior:smooth;
}
.container{
    width: 100%;
    box-sizing: border-box;
    height: 100%;
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: min-content 1fr min-content;
}

</style>
<div class="container">
    <timer-shaft-element class="timer-shaft"></timer-shaft-element>
    <div class="rows"></div>
    <trace-sheet class="trace-sheet" mode="hidden"></trace-sheet>
</div>
        `;
    }
}

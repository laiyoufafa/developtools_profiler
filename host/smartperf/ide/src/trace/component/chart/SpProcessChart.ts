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

import {SpSystemTrace} from "../SpSystemTrace.js";
import {
    getAsyncEvents,
    getFunDataByTid, getMaxDepthByTid, queryAllActualData, queryAllExpectedData, queryAllJankProcess,
    queryEventCountMap,
    queryProcess,
    queryProcessAsyncFunc,
    queryProcessByTable, queryProcessContentCount,
    queryProcessData,
    queryProcessMem,
    queryProcessMemData,
    queryProcessThreads,
    queryProcessThreadsByTable,
    queryThreadData
} from "../../database/SqlLite.js";
import {Utils} from "../trace/base/Utils.js";
import {info} from "../../../log/Log.js";
import {TraceRow} from "../trace/base/TraceRow.js";
import {renders} from "../../database/ui-worker/ProcedureWorker.js";
import {ProcessRender, ProcessStruct} from "../../database/ui-worker/ProcedureWorkerProcess.js";
import {ThreadRender, ThreadStruct} from "../../database/ui-worker/ProcedureWorkerThread.js";
import {FuncRender, FuncStruct} from "../../database/ui-worker/ProcedureWorkerFunc.js";
import {MemRender, ProcessMemStruct} from "../../database/ui-worker/ProcedureWorkerMem.js";
import {FolderSupplier, FolderThreadHandler} from "./SpChartManager.js";
import {JankRender, JankStruct} from "../../database/ui-worker/ProcedureWorkerJank.js";
import {ns2xByTimeShaft} from "../../database/ui-worker/ProcedureWorkerCommon.js";

export class SpProcessChart {
    private readonly trace: SpSystemTrace;
    private processAsyncFuncMap: any = {}
    private processAsyncFuncArray: any[] = []
    private eventCountMap: any;
    private processThreads: Array<ThreadStruct> = []
    private processAsyncEvent: Array<ProcessMemStruct> = []
    private processMem: Array<any> = []
    private processThreadCountMap: Map<number, number> = new Map();
    private processThreadDataCountMap: Map<number, number> = new Map();
    private processFuncDataCountMap: Map<number, number> = new Map();
    private processMemDataCountMap: Map<number, number> = new Map();
    private threadFuncMaxDepthMap: Map<number, number> = new Map();

    constructor(trace: SpSystemTrace) {
        this.trace = trace;
    }

    initAsyncFuncData = async () => {
        let asyncFuncList: any[] = await queryProcessAsyncFunc();
        info("AsyncFuncData Count is: ", asyncFuncList!.length);
        this.processAsyncFuncArray = asyncFuncList;
        this.processAsyncFuncMap = Utils.groupBy(asyncFuncList, "pid");
    }

    initDeliverInputEvent = async () => {
        let row = TraceRow.skeleton();
        row.setAttribute('disabled-check', '')
        row.rowId = `DeliverInputEvent`;
        row.index = 0;
        row.rowType = TraceRow.ROW_TYPE_DELIVER_INPUT_EVENT
        row.rowParentId = '';
        row.folder = true;
        row.style.height = '40px'
        row.name = `DeliverInputEvent`;
        row.supplier = FolderSupplier();
        row.onThreadHandler = FolderThreadHandler(row, this.trace);

        let asyncFuncGroup = Utils.groupBy(this.processAsyncFuncArray.filter(it=>it.funName==="deliverInputEvent"), "tid");
        if (Reflect.ownKeys(asyncFuncGroup).length > 0) {
            this.trace.rowsEL?.appendChild(row)
        }
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
                        it.dur = (TraceRow.range?.endNS || 0) - it.startTs;
                        it.flag = "Did not end"
                    }
                    createDepth(0, i);
                });
                let max = Math.max(...asyncFunctions.map(it => it.depth || 0)) + 1
                let maxHeight = max * 20;
                let funcRow = TraceRow.skeleton<FuncStruct>();
                funcRow.rowId = `${asyncFunctions[0].funName}-${key}`
                funcRow.asyncFuncName = asyncFunctions[0].funName;
                funcRow.asyncFuncNamePID = key;
                funcRow.rowType = TraceRow.ROW_TYPE_FUNC
                funcRow.rowParentId = `${row.rowId}`
                funcRow.rowHidden = !row.expansion
                funcRow.style.width = `100%`;
                funcRow.style.height = `${maxHeight}px`
                funcRow.setAttribute("height", `${maxHeight}`);
                funcRow.name = `${asyncFunctions[0].funName} ${key}`;
                funcRow.setAttribute('children', '')
                funcRow.supplier = () => new Promise((resolve) => resolve(asyncFunctions))
                funcRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                funcRow.selectChangeHandler = this.trace.selectChangeHandler;
                funcRow.onThreadHandler = (useCache) => {
                    let context = funcRow.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                    funcRow.canvasSave(context);
                    (renders["func"] as FuncRender).renderMainThread(
                        {
                            context: context,
                            useCache: useCache,
                            type: `func-${asyncFunctions[0].funName}-${key}`,
                        },
                        funcRow
                    );
                    funcRow.canvasRestore(context);
                }
                this.trace.rowsEL?.appendChild(funcRow);
            }
        });
    }

    async init() {
        let threadFuncMaxDepthArray = await getMaxDepthByTid();
        info("Gets the maximum tier per thread , tid and maxDepth");
        threadFuncMaxDepthArray.forEach((it) => {
            this.threadFuncMaxDepthMap.set(it.tid, it.maxDepth)
        })
        info("convert tid and maxDepth array to map");
        let pidCountArray = await queryProcessContentCount();
        info("fetch per process  pid,switch_count,thread_count,slice_count,mem_count")
        pidCountArray.forEach(it => {
            this.processThreadDataCountMap.set(it.pid, it.switch_count);
            this.processThreadCountMap.set(it.pid, it.thread_count);
            this.processFuncDataCountMap.set(it.pid, it.slice_count);
            this.processMemDataCountMap.set(it.pid, it.mem_count);
        })
        let queryProcessThreadResult = await queryProcessThreads();
        let queryProcessThreadsByTableResult = await queryProcessThreadsByTable()
        this.processAsyncEvent = await getAsyncEvents();
        info("The amount of initialized process Event data is : ", this.processAsyncEvent!.length)
        this.processMem = await queryProcessMem();
        info("The amount of initialized process memory data is : ", this.processMem!.length)
        let eventCountList: Array<any> = await queryEventCountMap();
        this.eventCountMap = eventCountList.reduce((pre, current) => {
            pre[`${current.eventName}`] = current.count;
            return pre;
        }, {});
        this.processThreads = Utils.removeDuplicates(queryProcessThreadResult, queryProcessThreadsByTableResult, "tid")

        info("The amount of initialized process threads data is : ", this.processThreads!.length)
        if (this.eventCountMap["print"] == 0 &&
            this.eventCountMap["tracing_mark_write"] == 0 &&
            this.eventCountMap["sched_switch"] == 0) {
            return;
        }
        let time = new Date().getTime();
        let processes = await queryProcess();
        let processFromTable = await queryProcessByTable();
        let processList = Utils.removeDuplicates(processes, processFromTable, "pid")
        let allJankProcessData = await queryAllJankProcess();
        let allJankProcess: Array<number> = [];
        let allExpectedProcess: Array<any> = [];
        let allActualProcess: Array<any> = [];
        if (allJankProcessData.length > 0) {
            allJankProcessData.forEach((name, index) => {
                allJankProcess.push(name.pid!)
            })
            allExpectedProcess = await queryAllExpectedData()
            allActualProcess = await queryAllActualData()
        }
        info("ProcessList Data size is: ", processList!.length)
        for (let i = 0; i < processList.length; i++) {
            const it = processList[i];
            if ((this.processThreadDataCountMap.get(it.pid) || 0) == 0 &&
                (this.processThreadCountMap.get(it.pid) || 0) == 0 &&
                (this.processFuncDataCountMap.get(it.pid) || 0) == 0 &&
                (this.processMemDataCountMap.get(it.pid) || 0) == 0) {
                continue;
            }
            let processRow = TraceRow.skeleton<ProcessStruct>();
            processRow.rowId = `${it.pid}`
            processRow.index = i;
            processRow.rowType = TraceRow.ROW_TYPE_PROCESS
            processRow.rowParentId = '';
            processRow.style.height = "40px";
            processRow.folder = true;
            processRow.name = `${it.processName || "Process"} ${it.pid}`;
            processRow.supplier = () => queryProcessData(it.pid || -1, 0, TraceRow.range?.totalNS || 0);
            processRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
            processRow.selectChangeHandler = this.trace.selectChangeHandler;
            processRow.onThreadHandler = (useCache) => {
                processRow.canvasSave(this.trace.canvasPanelCtx!);
                if (processRow.expansion) {
                    this.trace.canvasPanelCtx?.clearRect(0, 0, processRow.frame.width, processRow.frame.height);
                } else {
                    (renders["process"] as ProcessRender).renderMainThread(
                        {
                            context: this.trace.canvasPanelCtx,
                            pid: it.pid,
                            useCache: useCache,
                            type: `process ${processRow.index} ${it.processName}`,
                        },
                        processRow,
                    );
                }
                processRow.canvasRestore(this.trace.canvasPanelCtx!);
            }
            this.trace.rowsEL?.appendChild(processRow)
            /**
             * Janks Frames
             */
            let actualRow: TraceRow<JankStruct> | null = null;
            let expectedRow: TraceRow<JankStruct> | null = null;
            if (allJankProcess.indexOf(it.pid) > -1 && allExpectedProcess.length > 0) {
                let expectedData = allExpectedProcess.filter(ite => ite.pid == it.pid)
                if (expectedData.length > 0) {
                    // @ts-ignore
                    let isIntersect = (a: JanksStruct, b: JanksStruct) => (Math.max(a.ts + a.dur, b.ts + b.dur) - Math.min(a.ts, b.ts) < a.dur + b.dur);
                    let depthArray: any = [];
                    for (let j = 0; j < expectedData.length; j++) {
                        let it = expectedData[j]
                        if(it.cmdline != 'render_service'){
                            it.frame_type = 'app'
                        } else {
                            it.frame_type = it.cmdline
                        }
                        if (!it.dur || it.dur < 0) {
                            continue;
                        }
                        if (depthArray.length === 0) {
                            it.depth = 0;
                            depthArray.push(it);
                        } else {
                            if (isIntersect(depthArray[0], it)) {
                                if (isIntersect(depthArray[depthArray.length - 1], it)) {
                                    it.depth = depthArray.length
                                    depthArray.push(it)
                                }
                            } else {
                                it.depth = 0
                                depthArray = [it];
                            }
                        }
                    }
                    let max = Math.max(...expectedData.map(it => it.depth || 0)) + 1
                    let maxHeight = max * 20;
                    expectedRow = TraceRow.skeleton<JankStruct>();
                    let timeLineType = expectedData[0].type;
                    expectedRow.rowId = `${timeLineType}-${it.pid}`
                    expectedRow.asyncFuncName = it.processName;
                    expectedRow.asyncFuncNamePID = it.pid;
                    expectedRow.rowType = TraceRow.ROW_TYPE_JANK
                    expectedRow.rowParentId = `${it.pid}`
                    expectedRow.rowHidden = !processRow.expansion
                    expectedRow.style.width = `100%`;
                    expectedRow.style.height = `${maxHeight}px`
                    expectedRow.setAttribute("height", `${maxHeight}`);
                    expectedRow.name = `Expected Timeline`;
                    expectedRow.setAttribute('children', '')
                    expectedRow.supplier = () => new Promise((resolve) => {
                        resolve(expectedData);
                    })
                    expectedRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                    expectedRow.selectChangeHandler = this.trace.selectChangeHandler;
                    expectedRow.onThreadHandler = (useCache) => {
                        let context = expectedRow!.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                        expectedRow!.canvasSave(context);
                        (renders["jank"] as JankRender).renderMainThread(
                            {
                                context: context,
                                useCache: useCache,
                                type: `expected_frame_timeline_slice`
                            },
                            expectedRow!
                        );
                        expectedRow!.canvasRestore(context);
                    }
                    this.insertAfter(expectedRow, processRow)
                    let actualData = allActualProcess.filter(ite => ite.pid == it.pid)
                    if (actualData.length > 0) {
                        let isIntersect = (a: any, b: any) => (Math.max(a.ts + a.dur, b.ts + b.dur) - Math.min(a.ts, b.ts) < a.dur + b.dur);
                        let depthArray: any = []
                        for (let j = 0; j < actualData.length; j++) {
                            let it = actualData[j]
                            if(it.cmdline != 'render_service'){
                                it.frame_type = 'app'
                            } else {
                                it.frame_type = it.cmdline
                            }
                            if (!it.dur || it.dur < 0) {
                               continue;
                            }
                            if (depthArray.length === 0) {
                                it.depth = 0;
                                depthArray.push(it);
                            } else {
                                if (isIntersect(depthArray[0], it)) {
                                    if (isIntersect(depthArray[depthArray.length - 1], it)) {
                                        it.depth = depthArray.length
                                        depthArray.push(it)
                                    }
                                } else {
                                    it.depth = 0
                                    depthArray = [it];
                                }
                            }
                        }
                        let max = Math.max(...actualData.map(it => it.depth || 0)) + 1
                        let maxHeight = max * 20;
                        actualRow = TraceRow.skeleton<JankStruct>();
                        let timeLineType = actualData[0].type;
                        actualRow.rowId = `${timeLineType}-${it.pid}`
                        actualRow.asyncFuncName = it.processName;
                        actualRow.asyncFuncNamePID = it.pid;
                        actualRow.rowType = TraceRow.ROW_TYPE_JANK
                        actualRow.rowParentId = `${it.pid}`
                        actualRow.rowHidden = !processRow.expansion
                        actualRow.style.width = `100%`;
                        actualRow.style.height = `${maxHeight}px`
                        actualRow.setAttribute("height", `${maxHeight}`);
                        actualRow.name = `Actual Timeline`;
                        actualRow.setAttribute('children', '')
                        actualRow.supplier = () => new Promise((resolve) => resolve(actualData))
                        actualRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                        actualRow.selectChangeHandler = this.trace.selectChangeHandler;
                        actualRow.onThreadHandler = (useCache) => {
                            let context = actualRow!.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                            actualRow!.canvasSave(context);
                            (renders["jank"] as JankRender).renderMainThread(
                                {
                                    context: context,
                                    useCache: useCache,
                                    type: `actual_frame_timeline_slice`
                                },
                                actualRow!
                            );
                            actualRow!.canvasRestore(context);
                        }
                        this.insertAfter(actualRow, expectedRow)
                        processRow.addEventListener('expansion-change', (e:any) => {
                            if (e.detail.expansion) {
                                if(JankStruct!.selectJankStruct){
                                    JankStruct.delJankLineFlag = true;
                                }else{
                                    JankStruct.delJankLineFlag = false;
                                }
                                this.trace.linkNodes.forEach(linkNode => {
                                    JankStruct.selectJankStructList?.forEach((dat: any) => {
                                        if(e.detail.rowId == dat.pid){
                                            JankStruct.selectJankStruct = dat;
                                            JankStruct.hoverJankStruct = dat;
                                        }
                                    })
                                    if (linkNode[0].rowEL.rowId == e.detail.rowId) {
                                        linkNode[0] = {
                                            x: ns2xByTimeShaft(linkNode[0].ns, this.trace.timerShaftEL!),
                                            y: actualRow!.translateY! + (linkNode[0].offsetY * 2),
                                            offsetY: (linkNode[0].offsetY * 2),
                                            ns: linkNode[0].ns,
                                            rowEL:actualRow!,
                                            isRight: true
                                        }
                                    } else if (linkNode[1].rowEL.rowId == e.detail.rowId) {
                                        linkNode[1] =  {
                                            x: ns2xByTimeShaft(linkNode[1].ns, this.trace.timerShaftEL!),
                                            y: actualRow!.translateY! + (linkNode[1].offsetY * 2),
                                            offsetY: (linkNode[1].offsetY * 2),
                                            ns: linkNode[1].ns,
                                            rowEL:actualRow!,
                                            isRight: true
                                        }
                                    }
                                })
                                this.trace.refreshCanvas(true);
                            } else {
                                JankStruct.delJankLineFlag = false;
                                if(JankStruct!.selectJankStruct){
                                    JankStruct.selectJankStructList?.push(<JankStruct>JankStruct!.selectJankStruct);
                                }
                                this.trace.linkNodes?.forEach(linkNode => {
                                    if (linkNode[0].rowEL.rowParentId == e.detail.rowId) {
                                        linkNode[0] = {
                                            x: ns2xByTimeShaft(linkNode[0].ns, this.trace.timerShaftEL!),
                                            y: processRow!.translateY! + (linkNode[0].offsetY / 2),
                                            offsetY: (linkNode[0].offsetY /2),
                                            ns: linkNode[0].ns,
                                            rowEL:processRow,
                                            isRight: true
                                        }
                                    } else if (linkNode[1].rowEL.rowParentId == e.detail.rowId) {
                                        linkNode[1] = {
                                            x: ns2xByTimeShaft(linkNode[1].ns, this.trace.timerShaftEL!),
                                            y: processRow!.translateY! + (linkNode[1].offsetY / 2),
                                            offsetY: (linkNode[1].offsetY / 2),
                                            ns: linkNode[1].ns,
                                            rowEL:processRow,
                                            isRight: true
                                        }
                                    }
                                })
                                this.trace.refreshCanvas(true);
                            }
                        })

                    }
                }
            }
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
                            it.dur = (TraceRow.range?.endNS || 0) - it.startTs;
                            it.flag = "Did not end"
                        }
                        createDepth(0, i);
                    });
                    let max = Math.max(...asyncFunctions.map(it => it.depth || 0)) + 1
                    let maxHeight = max * 20;
                    let funcRow = TraceRow.skeleton<FuncStruct>();
                    funcRow.rowId = `${asyncFunctions[0].funName}-${it.pid}`
                    funcRow.asyncFuncName = asyncFunctions[0].funName;
                    funcRow.asyncFuncNamePID = it.pid;
                    funcRow.rowType = TraceRow.ROW_TYPE_FUNC
                    funcRow.rowParentId = `${it.pid}`
                    funcRow.rowHidden = !processRow.expansion
                    funcRow.style.width = `100%`;
                    funcRow.style.height = `${maxHeight}px`
                    funcRow.setAttribute("height", `${maxHeight}`);
                    funcRow.name = `${asyncFunctions[0].funName}`;
                    funcRow.setAttribute('children', '')
                    funcRow.supplier = () => new Promise((resolve) => resolve(asyncFunctions))
                    funcRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                    funcRow.selectChangeHandler = this.trace.selectChangeHandler;
                    funcRow.onThreadHandler = (useCache) => {
                        let context = funcRow.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                        funcRow.canvasSave(context);
                        (renders["func"] as FuncRender).renderMainThread(
                            {
                                context: context,
                                useCache: useCache,
                                type: `func-${asyncFunctions[0].funName}-${it.pid}`,
                            },
                            funcRow
                        );
                        funcRow.canvasRestore(context);
                    }
                    this.trace.rowsEL?.appendChild(funcRow);
                }
            });

            /**
             * 添加进程内存信息
             */
            let processMem = this.processMem.filter(mem => mem.pid === it.pid);
            processMem.forEach(mem => {
                let row = TraceRow.skeleton<ProcessMemStruct>();
                row.rowId = `${mem.trackId}`
                row.rowType = TraceRow.ROW_TYPE_MEM
                row.rowParentId = `${it.pid}`
                row.rowHidden = !processRow.expansion
                row.style.height = '40px'
                row.style.width = `100%`;
                row.name = `${mem.trackName}`;
                row.setAttribute('children', '');
                row.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                row.selectChangeHandler = this.trace.selectChangeHandler;
                row.focusHandler = () => {
                    this.trace.displayTip(row, ProcessMemStruct.hoverProcessMemStruct, `<span>${ProcessMemStruct.hoverProcessMemStruct?.value || ""}</span>`);
                }
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
                    let context = row.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                    row.canvasSave(context);
                    (renders["mem"] as MemRender).renderMainThread(
                        {
                            context: context,
                            useCache: useCache,
                            type: `mem ${mem.trackId} ${mem.trackName}`,
                        },
                        row
                    );
                    row.canvasRestore(context);
                }
                this.trace.rowsEL?.appendChild(row)
            });
            /**
             * add thread list
             */
            let threads = this.processThreads.filter(thread => thread.pid === it.pid && thread.tid != 0);
            for (let j = 0; j < threads.length; j++) {
                let thread = threads[j];
                let threadRow = TraceRow.skeleton<ThreadStruct>();
                threadRow.rowId = `${thread.tid}`
                threadRow.rowType = TraceRow.ROW_TYPE_THREAD
                threadRow.rowParentId = `${it.pid}`
                threadRow.rowHidden = !processRow.expansion
                threadRow.index = j
                threadRow.style.height = '30px'
                threadRow.style.width = `100%`;
                threadRow.name = `${thread.threadName || 'Thread'} ${thread.tid}`;
                threadRow.setAttribute('children', '')
                threadRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                threadRow.selectChangeHandler = this.trace.selectChangeHandler;
                threadRow.supplier = () => queryThreadData(thread.tid || 0).then(res => {
                    if (res.length <= 0) {
                        threadRow.rowDiscard = true;
                        this.trace.refreshCanvas(true)
                    }
                    return res;
                })
                threadRow.focusHandler = ev => {
                }
                threadRow.onThreadHandler = (useCache) => {
                    let context = threadRow.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                    threadRow.canvasSave(context);
                    (renders["thread"] as ThreadRender).renderMainThread(
                        {
                            context: context,
                            useCache: useCache,
                            type: `thread ${thread.tid} ${thread.threadName}`,
                            translateY: threadRow.translateY,
                        },
                        threadRow
                    );
                    threadRow.canvasRestore(context);
                }
                if (threadRow.rowId == threadRow.rowParentId) {
                    if (actualRow != null) {
                        this.insertAfter(threadRow, actualRow)
                    } else if (expectedRow != null) {
                        this.insertAfter(threadRow, expectedRow)
                    } else {
                        this.insertAfter(threadRow, processRow)
                    }
                } else {
                    this.trace.rowsEL?.appendChild(threadRow)
                }
                if (this.threadFuncMaxDepthMap.get(thread.tid!) != undefined) {
                    let max = this.threadFuncMaxDepthMap.get(thread.tid!) || 1;
                    let maxHeight = max * 20;
                    let funcRow = TraceRow.skeleton<FuncStruct>();
                    funcRow.rowId = `${thread.tid}`
                    funcRow.rowType = TraceRow.ROW_TYPE_FUNC
                    funcRow.rowParentId = `${it.pid}`
                    funcRow.rowHidden = !processRow.expansion
                    funcRow.checkType = threadRow.checkType;
                    funcRow.style.width = `100%`;
                    funcRow.style.height = `${maxHeight}px`;
                    funcRow.name = `${thread.threadName || 'Thread'} ${thread.tid}`;
                    funcRow.setAttribute('children', '')
                    funcRow.supplier = () => getFunDataByTid(thread.tid || 0).then((funs: Array<FuncStruct>) => {
                        if (funs.length > 0) {
                            let isBinder = (data: FuncStruct): boolean => {
                                return data.funName != null && (
                                    data.funName.toLowerCase().startsWith("binder transaction async") //binder transaction
                                    || data.funName.toLowerCase().startsWith("binder async")
                                    || data.funName.toLowerCase().startsWith("binder reply")
                                );
                            }
                            funs.forEach(fun => {
                                if (isBinder(fun)) {
                                } else {
                                    if (fun.dur == -1) {
                                        fun.dur = (TraceRow.range?.totalNS || 0) - (fun.startTs || 0);
                                        fun.flag = "Did not end";
                                    }
                                }
                            })
                        } else {
                            funcRow.rowDiscard = true;
                            this.trace.refreshCanvas(true)
                        }
                        return funs
                    })
                    funcRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
                    funcRow.selectChangeHandler = this.trace.selectChangeHandler;
                    funcRow.onThreadHandler = (useCache) => {
                        let context = funcRow.collect ? this.trace.canvasFavoritePanelCtx! : this.trace.canvasPanelCtx!;
                        funcRow.canvasSave(context);
                        (renders["func"] as FuncRender).renderMainThread(
                            {
                                context: context,
                                useCache: useCache,
                                type: `func${thread.tid}${thread.threadName}`,
                            },
                            funcRow
                        );
                        funcRow.canvasRestore(context);
                    }
                    this.insertAfter(funcRow, threadRow)
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
}

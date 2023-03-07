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

import {
    convertJSON, formatRealDateMs,
    getByteWithUnit,
    getTimeString,
    LogicHandler,
    MerageBean, merageBeanDataSplit, postMessage, setFileName
} from "./ProcedureLogicWorkerCommon.js";

export class ProcedureLogicWorkerNativeMemory extends LogicHandler {
    selectTotalSize = 0;
    selectTotalCount = 0;
    stackCount = 0;
    DATA_DICT: Map<number, string> = new Map<number, string>();
    FILE_DICT: Map<number, string> = new Map<number, string>();
    HEAP_FRAME_MAP: Map<number,Array<HeapTreeDataBean>> = new Map<number, Array<HeapTreeDataBean>>();
    NATIVE_MEMORY_DATA: Array<NativeEvent> = [];
    currentTreeMapData: any = {}
    currentTreeList: any[] = []
    queryAllCallchainsSamples:NativeHookStatistics[] = [];
    currentSamples:NativeHookStatistics[] = []
    allThreads:NativeHookCallInfo[] = []
    splitMapData: any = {}
    searchValue: string = ""
    currentEventId: string = ""
    chartComplete:Map<number,boolean> = new Map<number, boolean>();
    realTimeDif:number = 0;
    responseTypes:{key:number, value:string}[] = []

    handle(data: any): void {
        this.currentEventId = data.id
        if (data && data.type) {
            switch (data.type) {
                case "native-memory-init":
                    this.clearAll();
                    if(data.params.isRealtime) {
                        this.realTimeDif = data.params.realTimeDif
                    }
                    this.initDataDict();
                    break
                case "native-memory-queryDataDICT":
                    let dict = convertJSON(data.params.list) || []
                    dict.map((d: any) => this.DATA_DICT.set(d['id'], d['data']));
                    this.initNMChartData();
                    break;
                case "native-memory-queryNMChartData":
                    this.NATIVE_MEMORY_DATA = convertJSON(data.params.list) || [];
                    this.initNMFrameData();
                    break;
                case "native-memory-queryNMFrameData":
                    let arr = convertJSON(data.params.list) || [];
                    this.initNMStack(arr);
                    arr = [];
                    self.postMessage({
                        id: data.id,
                        action: "native-memory-init",
                        results: []
                    });
                    break;
                case "native-memory-queryCallchainsSamples":
                    if (data.params.list) {
                        let callchainsSamples = convertJSON(data.params.list) || [];
                        this.queryAllCallchainsSamples = callchainsSamples;
                        this.freshCurrentCallchains(callchainsSamples,true)
                        // @ts-ignore
                        self.postMessage({
                            id: data.id,
                            action: data.action,
                            results: this.allThreads
                        });
                    }else {
                        this.queryCallchainsSamples(data.params.leftNs, data.params.rightNs,data.params.types)
                    }
                    break;
                case "native-memory-action":
                    if (data.params) {
                        // @ts-ignore

                        self.postMessage({
                            id: data.id,
                            action: data.action,
                            results:  this.resolvingAction(data.params)
                        });
                    }
                    break;
                case "native-memory-chart-action":
                    if (data.params) {
                        postMessage(data.id, data.action, this.resolvingActionNativeMemoryChartData(data.params));
                    }
                    break;
                case "native-memory-calltree-action":
                    if (data.params) {
                        self.postMessage({id: data.id, action: data.action, results: this.resolvingNMCallAction(data.params)});
                    }
                    break;
                case "native-memory-init-responseType":
                    this.initResponseTypeList(data.params);
                    self.postMessage({
                        id: data.id,
                        action: data.action,
                        results: []
                    });
                    break;
                case "native-memory-get-responseType":
                    self.postMessage({id: data.id, action: data.action, results: this.responseTypes});
                    break;
            }
        }
    }

    queryData(queryName: string, sql: string, args: any) {
        self.postMessage({
            id: this.currentEventId,
            type: queryName,
            isQuery: true,
            args: args,
            sql: sql
        })
    }

    initDataDict() {
        this.queryData("native-memory-queryDataDICT", `select * from data_dict;`, {})
    }

    initNMChartData() {
        this.queryData("native-memory-queryNMChartData", `
            select * from (
                select 
                    h.start_ts - t.start_ts as startTime,
                    h.heap_size as heapSize,
                    (case when h.event_type = 'AllocEvent' then 0 else 1 end) as eventType
                from native_hook h ,trace_range t
                where h.start_ts >= t.start_ts 
                    and h.start_ts <= t.end_ts 
                    and (h.event_type = 'AllocEvent' or h.event_type = 'MmapEvent')
                union
                select 
                    h.end_ts - t.start_ts as startTime,
                    h.heap_size as heapSize,
                    (case when h.event_type = 'AllocEvent' then 2 else 3 end) as eventType
                from native_hook h ,trace_range t
                where h.start_ts >= t.start_ts
                    and h.start_ts <= t.end_ts
                    and h.end_ts not null 
                    and (h.event_type = 'AllocEvent' or h.event_type = 'MmapEvent')
            )
            order by startTime;
        `, {})
    }

    initResponseTypeList(list:any[]){
        this.responseTypes = [{
            key:-1,
            value:"ALL"
        }]
        list.forEach((item)=>{
            if(item.lastLibId == null){
                this.responseTypes.push({
                    key:0,
                    value:"-"
                })
            }else {
                this.responseTypes.push({
                    key:item.lastLibId,
                    value:this.groupCutFilePath(item.lastLibId,item.value)||"-"
                })
            }
        })
    }

    initNMFrameData() {
        this.queryData("native-memory-queryNMFrameData", `
            select h.symbol_id as symbolId, h.file_id as fileId, h.depth, h.callchain_id as eventId, h.vaddr as addr
                    from native_hook_frame h
        `, {})
    }

    initNMStack(frameArr:Array<HeapTreeDataBean>){
        frameArr.map((frame) => {
            let frameEventId = frame.eventId;
            if(this.HEAP_FRAME_MAP.has(frameEventId)){
                this.HEAP_FRAME_MAP.get(frameEventId)!.push(frame);
            }else{
                this.HEAP_FRAME_MAP.set(frameEventId,[frame])
            }
        })
    }

    resolvingAction(paramMap: Map<string, any>): Array<NativeHookCallInfo | NativeMemory | HeapStruct> {
        let actionType = paramMap.get("actionType");
        if (actionType == "call-info") {
            return this.resolvingActionCallInfo(paramMap);
        } else if (actionType == "native-memory") {
            return this.resolvingActionNativeMemory(paramMap);
        } else if (actionType == "memory-stack") {
            return this.resolvingActionNativeMemoryStack(paramMap);
        } else {
            return []
        }
    }

    resolvingActionNativeMemoryChartData(paramMap: Map<string, any>): Array<HeapStruct> {
        let nativeMemoryType: number = paramMap.get("nativeMemoryType") as number;
        let totalNS: number = paramMap.get("totalNS") as number;
        let arr: Array<HeapStruct> = [];
        let maxSize = 0,maxDensity = 0,minSize = 0,minDensity = 0;
        let tempSize = 0,tempDensity = 0;
        let filterLen = 0,filterLevel = 0;
        let putArr = (ne:NativeEvent,filterLevel:number)=>{
            let heap = new HeapStruct();
            heap.startTime = ne.startTime;
            if(arr.length == 0){
                if (ne.eventType == 0 || ne.eventType == 1) {
                    heap.density = 1;
                    heap.heapsize = ne.heapSize;
                } else {
                    heap.density = -1;
                    heap.heapsize = (0 - ne.heapSize);
                }
                maxSize = heap.heapsize;
                maxDensity = heap.density;
                minSize = heap.heapsize;
                minDensity = heap.density;
                arr.push(heap);
            }else{
                let last = arr[arr.length -1];
                last.dur = heap.startTime! - last.startTime!;
                if(last.dur > filterLevel){
                    if (ne.eventType == 0 || ne.eventType == 1) {
                        heap.density = last.density! + tempDensity + 1
                        heap.heapsize = last.heapsize! + tempSize + ne.heapSize;
                    } else {
                        heap.density = last.density! + tempDensity - 1
                        heap.heapsize = last.heapsize! + tempSize - ne.heapSize;
                    }
                    tempDensity = 0;
                    tempSize = 0;
                    if(heap.density > maxDensity){
                        maxDensity = heap.density
                    }
                    if(heap.density < minDensity){
                        minDensity = heap.density
                    }
                    if (heap.heapsize > maxSize) {
                        maxSize = heap.heapsize;
                    }
                    if (heap.heapsize < minSize) {
                        minSize = heap.heapsize;
                    }
                    arr.push(heap)
                }else{
                    if (ne.eventType == 0 || ne.eventType == 1) {
                        tempDensity = tempDensity +1
                        tempSize = tempSize + ne.heapSize;
                    } else {
                        tempDensity = tempDensity - 1
                        tempSize = tempSize - ne.heapSize;
                    }
                }
            }
        }
        if(nativeMemoryType == 1){
            let temp = this.NATIVE_MEMORY_DATA.filter((ne) => ne.eventType == 0 || ne.eventType == 2);
            filterLen = temp.length;
            filterLevel = this.getFilterLevel(filterLen);
            temp.map((ne) => putArr(ne,filterLevel))
            temp.length = 0;
        }else if(nativeMemoryType == 2){
            let temp = this.NATIVE_MEMORY_DATA.filter((ne) => ne.eventType == 1 || ne.eventType == 3);
            filterLen = temp.length;
            filterLevel = this.getFilterLevel(filterLen);
            temp.map((ne) => putArr(ne,filterLevel));
            temp.length = 0;
        }else{
            filterLen = this.NATIVE_MEMORY_DATA.length;
            let filterLevel = this.getFilterLevel(filterLen);
            this.NATIVE_MEMORY_DATA.map((ne) => putArr(ne,filterLevel))
        }
        if(arr.length > 0){
            arr[arr.length -1].dur = totalNS - arr[arr.length -1].startTime!
        }
        arr.map((heap)=>{
            heap.maxHeapSize = maxSize;
            heap.maxDensity = maxDensity;
            heap.minHeapSize = minSize;
            heap.minDensity = minDensity;
        })
        this.chartComplete.set(nativeMemoryType,true);
        if(this.chartComplete.has(0) && this.chartComplete.has(1) && this.chartComplete.has(2)){
            this.NATIVE_MEMORY_DATA = [];
        }
        return arr;
    }

    resolvingActionNativeMemoryStack(paramMap: Map<string, any>) {
        let eventId = paramMap.get("eventId");
        let frameArr = this.HEAP_FRAME_MAP.get(eventId) || [];
        let arr: Array<NativeHookCallInfo> = [];
        frameArr.map((frame) => {
            let target = new NativeHookCallInfo();
            target.eventId = frame.eventId;
            target.depth = frame.depth;
            target.addr = frame.addr;
            target.symbol =  this.groupCutFilePath(frame.symbolId,this.DATA_DICT.get(frame.symbolId)||"" )??"";
            target.library = this.groupCutFilePath(frame.fileId,this.DATA_DICT.get(frame.fileId)||"") ?? "";
            target.title = `[ ${target.symbol} ]  ${target.library}`;
            target.type = (target.library.endsWith(".so.1") || target.library.endsWith(".dll") || target.library.endsWith(".so")) ? 0 : 1;
            arr.push(target);
        })
        return arr;
    }

    resolvingActionNativeMemory(paramMap: Map<string, any>): Array<NativeMemory> {
        let dataSource = paramMap.get("data") as Array<NativeHookStatistics>;
        let filterAllocType = paramMap.get("filterAllocType");
        let filterEventType = paramMap.get("filterEventType");
        let filterResponseType = paramMap.get("filterResponseType");
        let leftNs = paramMap.get("leftNs");
        let rightNs = paramMap.get("rightNs");
        let statisticsSelection = paramMap.get("statisticsSelection");
        let filter = dataSource.filter((item) => {
            if (item.subTypeId != null && item.subType == undefined) {
                item.subType = this.DATA_DICT.get(item.subTypeId)||"-"
            }
            let filterAllocation = true
            if (filterAllocType == "1") {
                filterAllocation = item.startTs >= leftNs && item.startTs <= rightNs
                    && (item.endTs > rightNs || item.endTs == 0 || item.endTs == null)
            } else if (filterAllocType == "2") {
                filterAllocation = item.startTs >= leftNs && item.startTs <= rightNs
                    && item.endTs <= rightNs && item.endTs != 0 && item.endTs != null;
            }
            let filterNative = this.getTypeFromIndex(parseInt(filterEventType), item, statisticsSelection)
            let filterLastLib = filterResponseType == -1?true:(filterResponseType == item.lastLibId)
            return filterAllocation && filterNative && filterLastLib
        })
        let data: Array<NativeMemory> = [];
        for (let i = 0, len = filter.length; i < len; i++) {
            let hook = filter[i];
            let memory = new NativeMemory();
            memory.index = i;
            memory.eventId = hook.eventId;
            memory.eventType = hook.eventType;
            memory.subType = hook.subType;
            memory.heapSize = hook.heapSize;
            memory.endTs = hook.endTs;
            memory.heapSizeUnit = getByteWithUnit(hook.heapSize);
            memory.addr = "0x" + hook.addr;
            memory.startTs = hook.startTs;
            memory.timestamp = this.realTimeDif == 0?getTimeString(hook.startTs):formatRealDateMs(hook.startTs + this.realTimeDif);
            memory.state = (hook.endTs > leftNs && hook.endTs <= rightNs) ? "Freed" : "Existing";
            memory.threadId = hook.tid;
            memory.threadName = hook.threadName;
            memory.lastLibId = hook.lastLibId;
            (memory as any).isSelected = hook.isSelected;
            let arr = this.HEAP_FRAME_MAP.get(hook.eventId) || []
            let frame = Array.from(arr).reverse().find((item) => {
               let fileName = this.DATA_DICT.get(item.fileId);
               return !(( fileName?? "").includes("libc++") || (fileName ?? "").includes("musl"));
            })
            if(frame == null || frame == undefined){
                if(arr.length > 0){
                    frame = arr[0];
                }
            }
            if (frame != null && frame != undefined) {
                memory.symbol =  this.groupCutFilePath(frame.symbolId,this.DATA_DICT.get(frame.symbolId)||"" ) ;
                memory.library = this.groupCutFilePath(frame.fileId,this.DATA_DICT.get(frame.fileId)||"Unknown Path");
            }else{
                memory.symbol = "-";
                memory.library = "-";
            }
            data.push(memory);
        }
        return data
    }

    resolvingActionCallInfo(paramMap: Map<string, any>): Array<NativeHookCallInfo> {
        let dataSource = paramMap.get("data") as Array<NativeHookStatistics>;
        let filterAllocType = paramMap.get("filterAllocType");
        let filterEventType = paramMap.get("filterEventType");
        let leftNs = paramMap.get("leftNs");
        let rightNs = paramMap.get("rightNs");
        let filter: Array<NativeHookStatistics> = [];
        dataSource.map((item) => {
            let filterAllocation = true;
            let filterNative = true;
            if (filterAllocType == "1") {
                filterAllocation = item.startTs >= leftNs && item.startTs <= rightNs
                    && (item.endTs > rightNs || item.endTs == 0 || item.endTs == null)
            } else if (filterAllocType == "2") {
                filterAllocation = item.startTs >= leftNs && item.startTs <= rightNs
                    && item.endTs <= rightNs && item.endTs != 0 && item.endTs != null;
            }
            if (filterEventType == "1") {
                filterNative = item.eventType == "AllocEvent"
            } else if (filterEventType == "2") {
                filterNative = item.eventType == "MmapEvent"
            }
            if (filterAllocation && filterNative) {
                filter.push(item);
            }
        })
        this.freshCurrentCallchains(filter,true)
        return this.allThreads;
    }

    groupCutFilePath(fileId:number,path:string):string{
        let name = "";
        if(this.FILE_DICT.has(fileId)){
            name = this.FILE_DICT.get(fileId) ?? "";
        }else {
            let currentPath = path.substring(path.lastIndexOf("/")+1);
            this.FILE_DICT.set(fileId,currentPath)
            name = currentPath;
        }
        return name == "" ? "-" : name
    }

    groupByWithTid(data: Array<NativeHookCallInfo>): Array<NativeHookCallInfo> {
        let tidMap = new Map<number, NativeHookCallInfo>();
        for (let call of data) {
            if (tidMap.has(call.tid)) {
                let tidCall = tidMap.get(call.tid);
                tidCall!.size += call.size;
                tidCall!.heapSizeStr = `${getByteWithUnit(tidCall!.size)}`;
                tidCall!.heapPercent = `${(tidCall!.size / this.selectTotalSize * 100).toFixed(1)}%`
                tidCall!.count += call.count;
                tidCall!.countValue = `${tidCall!.count}`
                tidCall!.countPercent = `${(tidCall!.count / this.selectTotalCount * 100).toFixed(1)}%`
                tidCall!.children.push(call);
            } else {
                let tidCall = new NativeHookCallInfo();
                tidCall.id = "tid_" + call.tid;
                tidCall.count = call.count;
                tidCall!.countValue = `${call.count}`
                tidCall!.countPercent = `${(tidCall!.count / this.selectTotalCount * 100).toFixed(1)}%`
                tidCall.size = call.size;
                tidCall.heapSizeStr = `${getByteWithUnit(tidCall!.size)}`;
                tidCall!.heapPercent = `${(tidCall!.size / this.selectTotalSize * 100).toFixed(1)}%`
                tidCall.title = (call.threadName == null ? 'Thread' : call.threadName) + " [ " + call.tid + " ]";
                tidCall.symbol = tidCall.title;
                tidCall.addr = tidCall.addr;
                tidCall.type = -1;
                tidCall.children.push(call);
                tidMap.set(call.tid, tidCall);
            }
        }
        let showData = Array.from(tidMap.values())
        return showData;
    }

    mergeTree(target: NativeHookCallInfo, src: NativeHookCallInfo) {
        let len = src.children.length;
        src.size += target.size;
        src.heapSizeStr = `${getByteWithUnit(src!.size)}`;
        src.heapPercent = `${(src!.size / this.selectTotalSize * 100).toFixed(1)}%`
        if (len == 0) {
            src.children.push(target);
        } else {
            let index = src.children.findIndex((hook) => hook.symbol == target.symbol && hook.depth == target.depth);
            if (index != -1) {
                let srcChild = <NativeHookCallInfo>src.children[index];
                srcChild.count += target.count;
                srcChild!.countValue = `${srcChild.count}`
                srcChild!.countPercent = `${(srcChild!.count / this.selectTotalCount * 100).toFixed(1)}%`
                if (target.children.length > 0) {
                    this.mergeTree(<NativeHookCallInfo>target.children[0], <NativeHookCallInfo>srcChild)
                } else {
                    srcChild.size += target.size;
                    srcChild.heapSizeStr = `${getByteWithUnit(src!.size)}`;
                    srcChild.heapPercent = `${(srcChild!.size / this.selectTotalSize * 100).toFixed(1)}%`
                }
            } else {
                src.children.push(target)
            }
        }
    }

    traverseSampleTree(stack: NativeHookCallInfo, hook: NativeHookStatistics) {
        stack.count += 1;
        stack.countValue = `${stack.count}`
        stack.countPercent = `${(stack.count / this.selectTotalCount * 100).toFixed(1)}%`
        stack.size += hook.heapSize;
        stack.tid = hook.tid;
        stack.threadName = hook.threadName;
        stack.heapSizeStr = `${getByteWithUnit(stack.size)}`;
        stack.heapPercent = `${(stack.size / this.selectTotalSize * 100).toFixed(1)}%`;
        if (stack.children.length > 0) {
            stack.children.map((child) => {
                this.traverseSampleTree(child as NativeHookCallInfo, hook);
            })
        }
    }

    traverseTree(stack: NativeHookCallInfo, hook: NativeHookStatistics) {
        stack.count = 1;
        stack.countValue = `${stack.count}`
        stack.countPercent = `${(stack!.count / this.selectTotalCount * 100).toFixed(1)}%`
        stack.size = hook.heapSize;
        stack.tid = hook.tid;
        stack.threadName = hook.threadName;
        stack.heapSizeStr = `${getByteWithUnit(stack!.size)}`;
        stack.heapPercent = `${(stack!.size / this.selectTotalSize * 100).toFixed(1)}%`;
        if (stack.children.length > 0) {
            stack.children.map((child) => {
                this.traverseTree(child as NativeHookCallInfo, hook);
            })
        }
    }

    getTypeFromIndex(indexOf: number, item: NativeHookStatistics, statisticsSelection: Array<StatisticsSelection>): boolean {
        if (indexOf == -1) {
            return false;
        }
        if (indexOf < 3) {
            if (indexOf == 0) {
                return true
            } else if (indexOf == 1) {
                return item.eventType == "AllocEvent"
            } else if (indexOf == 2) {
                return item.eventType == "MmapEvent"
            }
        } else if (indexOf - 3 < statisticsSelection.length) {
            let selectionElement = statisticsSelection[indexOf - 3];

            if (selectionElement.memoryTap != undefined && selectionElement.max != undefined) {
                if (selectionElement.memoryTap.indexOf("Malloc") != -1) {
                    return item.eventType == "AllocEvent" && item.heapSize == selectionElement.max
                } else if (selectionElement.memoryTap.indexOf("Mmap") != -1) {
                    return item.eventType == "MmapEvent" && item.heapSize == selectionElement.max
                } else {
                    return item.subType == selectionElement.memoryTap && item.heapSize == selectionElement.max
                }
            }
        }
        return false;
    }

    clearAll() {
        this.DATA_DICT.clear();
        this.FILE_DICT.clear();
        this.splitMapData = {};
        this.currentSamples = [];
        this.allThreads = [];
        this.queryAllCallchainsSamples = [];
        this.HEAP_FRAME_MAP.clear();
        this.NATIVE_MEMORY_DATA = [];
        this.chartComplete.clear();
        this.realTimeDif = 0;
    }

    listToTree(target: NativeHookCallInfo, src: NativeHookCallInfo) {
        if (target.depth == src.depth + 1) {
            src.children.push(target)
        } else {
            if (src.children.length > 0) {
                this.listToTree(target, <NativeHookCallInfo>src.children[0]);
            }
        }
    }

    queryCallchainsSamples(leftNs: number, rightNs: number, types: Array<string>){
        this.queryData("native-memory-queryCallchainsSamples", `
            select
      callchain_id as eventId,
      event_type as eventType,
      heap_size as heapSize,
      (A.start_ts - B.start_ts) as startTs,
      (A.end_ts - B.start_ts) as endTs,
      tid,
      ifnull(last_lib_id,0) as lastLibId,
      t.name as threadName
    from
      native_hook A,
      trace_range B
    left join
      thread t
    on
      A.itid = t.id
    where
      A.start_ts - B.start_ts
    between ${leftNs} and ${rightNs} and A.event_type in (${types.join(",")})
        `, {})
    }

    freshCurrentCallchains(samples: NativeHookStatistics[], isTopDown: boolean){
        this.currentTreeMapData = {};
        this.currentTreeList = [];
        let totalSize = 0;
        let totalCount = 0;
        samples.forEach((sample) => {
            if(sample.eventId == -1){
                return
            }
            totalSize += sample.heapSize;
            totalCount += sample.count||1
            let callChains = this.createThreadSample(sample)
            let topIndex = isTopDown ? 0 : (callChains.length - 1);
            let root = this.currentTreeMapData[sample.tid + "-" + (callChains[topIndex].symbolId||"")+"-"+(callChains[topIndex].fileId||"")];
            if (root == undefined) {
                root = new NativeHookCallInfo()
                this.currentTreeMapData[sample.tid + "-" + (callChains[topIndex].symbolId||"")+"-"+(callChains[topIndex].fileId||"")] = root
                this.currentTreeList.push(root)
            }
            NativeHookCallInfo.merageCallChainSample(root,callChains[topIndex],sample)
            if(callChains.length > 1){
                this.merageChildrenByIndex(root,callChains,topIndex,sample,isTopDown)
            }
        })
        let rootMerageMap: any = {}
        let threads = Object.values(this.currentTreeMapData);
        threads.forEach((merageData: any) => {
            if (rootMerageMap[merageData.tid] == undefined) {
                let threadMerageData = new NativeHookCallInfo()//新增进程的节点数据
                threadMerageData.canCharge = false
                threadMerageData.type = -1;
                threadMerageData.symbolName = `${merageData.threadName||"Thread"} [${merageData.tid}]`
                threadMerageData.symbol = threadMerageData.symbolName
                threadMerageData.children.push(merageData)
                threadMerageData.initChildren.push(merageData)
                threadMerageData.count = merageData.count||1;
                threadMerageData.heapSize = merageData.heapSize
                threadMerageData.totalCount = totalCount;
                threadMerageData.totalSize = totalSize;
                rootMerageMap[merageData.tid] = threadMerageData
            } else {
                rootMerageMap[merageData.tid].children.push(merageData)
                rootMerageMap[merageData.tid].initChildren.push(merageData)
                rootMerageMap[merageData.tid].count += merageData.count||1;
                rootMerageMap[merageData.tid].heapSize += merageData.heapSize
                rootMerageMap[merageData.tid].totalCount = totalCount;
                rootMerageMap[merageData.tid].totalSize = totalSize;
            }
            merageData.parentNode = rootMerageMap[merageData.tid]//子节点添加父节点的引用
        })
        let id = 0;
        this.currentTreeList.forEach((node) => {
            node.totalCount = totalCount;
            node.totalSize = totalSize;
            this.setMerageName(node)
            if (node.id == "") {
                node.id = id + ""
                id++
            }
            if (node.parentNode) {
                if (node.parentNode.id == "") {
                    node.parentNode.id = id + ""
                    id++
                }
                node.parentId = node.parentNode.id
            }
        })
        this.allThreads = Object.values(rootMerageMap) as NativeHookCallInfo[]
    }

    groupCallchainSample(paramMap: Map<string, any>){
        let groupMap:any = {}
        let filterAllocType = paramMap.get("filterAllocType");
        let filterEventType = paramMap.get("filterEventType");
        let filterResponseType = paramMap.get("filterResponseType");
        let leftNs = paramMap.get("leftNs");
        let rightNs = paramMap.get("rightNs");
        if(filterAllocType == "0"&&filterEventType == "0" && filterResponseType == -1){
            this.currentSamples = this.queryAllCallchainsSamples
            return
        }
        let filter = this.queryAllCallchainsSamples.filter((item) => {
            let filterAllocation = true
            if (filterAllocType == "1") {
                filterAllocation = item.startTs >= leftNs && item.startTs <= rightNs
                    && (item.endTs > rightNs || item.endTs == 0 || item.endTs == null)
            } else if (filterAllocType == "2") {
                filterAllocation = item.startTs >= leftNs && item.startTs <= rightNs
                    && item.endTs <= rightNs && item.endTs != 0 && item.endTs != null;
            }
            let filterLastLib = filterResponseType == -1?true:(filterResponseType == item.lastLibId)
            let filterNative = this.getTypeFromIndex(parseInt(filterEventType), item, [])
            return filterAllocation && filterNative && filterLastLib
        })
        filter.forEach((sample)=>{
            let currentNode = groupMap[sample.tid + "-" + sample.eventId]||new NativeHookStatistics()
            if(currentNode.count == 0){
                Object.assign(currentNode,sample)
                currentNode.count++
            }else {
                currentNode.count++
                currentNode.heapSize += sample.heapSize
            }
            groupMap[sample.tid + "-" + sample.eventId] = currentNode
        })
        this.currentSamples = Object.values(groupMap)
    }

    createThreadSample(sample:NativeHookStatistics){
        return this.HEAP_FRAME_MAP.get(sample.eventId)||[]
    }

    merageChildrenByIndex(currentNode:NativeHookCallInfo,callChainDataList: any[], index: number, sample: NativeHookStatistics, isTopDown: boolean){
        isTopDown ? index++ : index--;
        let isEnd = isTopDown ? (callChainDataList.length == index + 1) : (index == 0)
        let node;
        if (currentNode.initChildren.filter((child: any) => {
            if ((child.symbolId == callChainDataList[index]?.symbolId&&child.fileId == callChainDataList[index]?.fileId)) {
                node = child;
                NativeHookCallInfo.merageCallChainSample(child, callChainDataList[index], sample)
                return true;
            }
            return false;
        }).length == 0) {
            node = new NativeHookCallInfo()
            NativeHookCallInfo.merageCallChainSample(node, callChainDataList[index], sample)
            currentNode.children.push(node)
            currentNode.initChildren.push(node)
            this.currentTreeList.push(node)
            node.parentNode = currentNode
        }
        if (node && !isEnd) this.merageChildrenByIndex(node, callChainDataList, index, sample, isTopDown)
    }

    setMerageName(currentNode: NativeHookCallInfo){
        currentNode.symbol = this.groupCutFilePath(currentNode.symbolId,this.DATA_DICT.get(currentNode.symbolId)||"" )??"unkown";
        currentNode.path = this.DATA_DICT.get(currentNode.fileId)||"unkown"
        currentNode.libName = setFileName(currentNode.path)
        currentNode.lib = currentNode.path
        currentNode.symbolName = `[${currentNode.symbol}] ${currentNode.libName}`
        currentNode.type = (currentNode.libName.endsWith(".so.1") || currentNode.libName.endsWith(".dll") || currentNode.libName.endsWith(".so")) ? 0 : 1;
    }

    clearSplitMapData(symbolName: string) {
        delete this.splitMapData[symbolName]
    }

    resolvingNMCallAction(params: any[]){
        if (params.length > 0) {
            params.forEach((item) => {
                if (item.funcName && item.funcArgs) {
                    switch (item.funcName) {
                        case "groupCallchainSample":
                            this.groupCallchainSample(item.funcArgs[0] as Map<string,any>)
                            break
                        case "getCallChainsBySampleIds":
                            this.freshCurrentCallchains(this.currentSamples, item.funcArgs[0])
                            break
                        case "hideSystemLibrary":
                            merageBeanDataSplit.hideSystemLibrary(this.allThreads, this.splitMapData);
                            break
                        case "hideNumMaxAndMin":
                            merageBeanDataSplit.hideNumMaxAndMin(this.allThreads, this.splitMapData, item.funcArgs[0], item.funcArgs[1])
                            break
                        case "splitAllProcess":
                            merageBeanDataSplit.splitAllProcess(this.allThreads, this.splitMapData, item.funcArgs[0])
                            break
                        case "resetAllNode":
                            merageBeanDataSplit.resetAllNode(this.allThreads, this.currentTreeList, this.searchValue)
                            break
                        case "resotreAllNode":
                            merageBeanDataSplit.resotreAllNode(this.splitMapData, item.funcArgs[0])
                            break
                        case "splitTree":
                            merageBeanDataSplit.splitTree(this.splitMapData, this.allThreads, item.funcArgs[0], item.funcArgs[1], item.funcArgs[2], this.currentTreeList, this.searchValue);
                            break
                        case "setSearchValue":
                            this.searchValue = item.funcArgs[0]
                            break
                        case "clearSplitMapData":
                            this.clearSplitMapData(item.funcArgs[0])
                            break
                    }
                }
            })
        }
        return this.allThreads.filter((thread) => {
            return thread.children && thread.children.length > 0
        })
    }

    getFilterLevel(len:number):number{
        if(len > 100_0000){
            return 10_0000;
        }else if(len > 50_0000){
            return 5_0000;
        }else if(len > 30_0000){
            return 3_5000
        }else if(len > 15_0000){
            return 1_5000
        }else{
            return 0
        }
    }
}

export class HeapTreeDataBean {
    MoudleName: string | undefined
    AllocationFunction: string | undefined
    symbolId: number = 0
    fileId: number = 0
    startTs: number = 0
    endTs: number = 0
    eventType: string | undefined
    depth: number = 0
    heapSize: number = 0
    eventId: number = 0
    addr : string = ''
}

export class NativeHookStatistics {
    eventId: number = 0;
    eventType: string = "";
    subType: string = "";
    subTypeId: number = 0;
    heapSize: number = 0;
    addr: string = "";
    startTs: number = 0;
    endTs: number = 0;
    sumHeapSize: number = 0;
    max: number = 0;
    count: number = 0;
    tid: number = 0;
    threadName: string = "";
    lastLibId:number = 0;
    isSelected: boolean = false;
}

export class NativeHookCallInfo extends MerageBean {
    #totalCount: number = 0
    #totalSize:number = 0;
    library: string = "";
    symbolId: number = 0
    fileId: number = 0
    title: string = "";
    count: number = 0;
    countValue: string = ""
    countPercent: string = "";
    type: number = 0;
    heapSize: number = 0;
    heapPercent: string = "";
    heapSizeStr: string = "";
    eventId: number = 0;
    tid: number = 0;
    threadName: string = "";
    eventType:string = "";
    isSelected: boolean = false;

    set totalCount(total:number){
        this.#totalCount = total;
        this.countValue = this.count + "";
        this.size = this.heapSize;
        this.countPercent = `${(this.count / total * 100).toFixed(1)}%`;
    }

    get totalCount(){
        return this.#totalCount
    }

    set totalSize(total:number){
        this.#totalSize = total;
        this.heapSizeStr = `${getByteWithUnit(this.heapSize)}`
        this.heapPercent = `${(this.heapSize / total * 100).toFixed(1)}%`;
    }

    get totalSize(){
        return this.#totalSize
    }

    static merageCallChainSample(currentNode: NativeHookCallInfo, callChain: HeapTreeDataBean, sample: NativeHookStatistics){
        if(currentNode.symbol == undefined||currentNode.symbol == ""){
            currentNode.symbol =  callChain.AllocationFunction||"";
            currentNode.addr = callChain.addr;
            currentNode.eventId = sample.eventId;
            currentNode.eventType = sample.eventType;
            currentNode.symbolId = callChain.symbolId;
            currentNode.fileId = callChain.fileId;
            currentNode.tid = sample.tid
        }
        currentNode.count += sample.count||1;
        currentNode.heapSize += sample.heapSize;
    }

}

export class NativeMemory {
    index: number = 0;
    eventId: number = 0;
    eventType: string = "";
    subType: string = "";
    addr: string = "";
    startTs: number = 0;
    endTs: number = 0;
    timestamp: string = ""
    heapSize: number = 0;
    heapSizeUnit: string = "";
    symbol: string = "";
    library: string = "";
    lastLibId:number = 0;
    isSelected: boolean = false;
    state: string = "";
    threadId: number = 0;
    threadName: string = "";
}

export class HeapStruct {
    startTime: number | undefined
    endTime: number | undefined
    dur: number | undefined
    density: number | undefined
    heapsize: number | undefined
    maxHeapSize: number = 0
    maxDensity: number = 0
    minHeapSize: number = 0
    minDensity: number = 0
}

export class NativeEvent {
    startTime: number = 0;
    heapSize: number = 0;
    eventType: number = 0;
}

export class StatisticsSelection {
    memoryTap: string = "";
    max: number = 0;
}
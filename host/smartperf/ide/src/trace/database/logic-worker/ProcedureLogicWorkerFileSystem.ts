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
    convertJSON, getByteWithUnit,
    getProbablyTime,
    getTimeString,
    LogicHandler,
    MerageBean,
    merageBeanDataSplit,
    postMessage,
    setFileName
} from "./ProcedureLogicWorkerCommon.js";

export let FILE_TYPE_MAP = {
    '0': 'OPEN',
    '1': 'CLOSE',
    '2': 'READ',
    '3': 'WRITE',
};

export let DISKIO_TYPE_MAP = {
    '1': 'DATA_READ',
    '2': 'DATA_WRITE',
    '3': 'METADATA_READ',
    '4': 'METADATA_WRITE',
    '5': 'PAGE_IN',
    '6': 'PAGE_OUT',
};

export let VM_TYPE_MAP = {
    '1': 'File Backed In',
    '2': 'Page Cache Hit',
    '3': 'Swap From Zram',
    '4': 'Swap From Disk',
    '5': 'Zero Fill Page',
    '6': 'Zero FAKE Page',
    '7': 'Copy On Write',
};

export class ProcedureLogicWorkerFileSystem extends LogicHandler {
    static data_dict: Map<number, string> = new Map<number, string>();
    static callChainsMap: Map<number, FileCallChain[]> = new Map<number, FileCallChain[]>()
    handlerMap:Map<string,any> = new Map<string,any>();
    currentEventId: string = ""
    tab:string = "";

    handle(data: any): void {
        if(data.id){
            this.currentEventId = data.id
        }
        if (data && data.type) {
            switch (data.type) {
                case "fileSystem-init":
                    ProcedureLogicWorkerFileSystem.data_dict = data.params as Map<number, string>
                    this.initCallchains();
                    break
                case "fileSystem-queryCallchains":
                    let callChains = convertJSON(data.params.list) || [];
                    this.initCallChainTopDown(callChains)
                    // @ts-ignore
                    self.postMessage({id: data.id, action: data.action, results: []});
                    break;
                case "fileSystem-queryFileSamples":
                    this.handlerMap.get("fileSystem").samplesList = convertJSON(data.params.list) || []
                    self.postMessage({
                        id: this.currentEventId, action: data.action, results: this.handlerMap.get("fileSystem").resolvingAction([{
                            funcName: "getCallChainsBySampleIds",
                            funcArgs: [true]
                        }])
                    });
                    break;
                case "fileSystem-queryIoSamples":
                    this.handlerMap.get("io").samplesList = convertJSON(data.params.list) || []
                    self.postMessage({
                        id: this.currentEventId, action: data.action, results: this.handlerMap.get("io").resolvingAction([{
                            funcName: "getCallChainsBySampleIds",
                            funcArgs: [true]
                        }])
                    });
                    break;
                case "fileSystem-queryVirtualMemorySamples":
                    this.handlerMap.get("virtualMemory").samplesList = convertJSON(data.params.list) || []
                    self.postMessage({
                        id: this.currentEventId, action: data.action, results: this.handlerMap.get("virtualMemory").resolvingAction([{
                            funcName: "getCallChainsBySampleIds",
                            funcArgs: [true]
                        }])
                    });
                    break;
                case "fileSystem-action":
                    if (data.params) {
                        let filter = data.params.args.filter((item: any) => item.funcName == "getCurrentDataFromDb");
                        if (filter.length == 0) {
                            // @ts-ignore
                            self.postMessage({
                                id: data.id,
                                action: data.action,
                                results: this.handlerMap.get(data.params.callType).resolvingAction(data.params.args)
                            });
                        } else {
                            this.handlerMap.get(data.params.callType).resolvingAction(data.params.args)
                        }
                    }
                    break;
                case "fileSystem-queryStack":
                    let res = this.getStacksByCallchainId(data.params.callchainId)
                    self.postMessage({id: data.id, action: data.action, results: res})
                    break;
                case "fileSystem-queryFileSysEvents":
                    if (data.params.list) {
                        let res = convertJSON(data.params.list) || []
                        postMessage(data.id, data.action, this.supplementFileSysEvents(res, this.tab));
                    } else {
                        this.tab = data.params.tab;
                        this.queryFileSysEvents(data.params.leftNs, data.params.rightNs, data.params.typeArr, data.params.tab)
                    }
                    break;
                case "fileSystem-queryVMEvents":
                    if (data.params.list) {
                        let res = convertJSON(data.params.list) || []
                        postMessage(data.id, data.action, this.supplementVMEvents(res));
                    } else {
                        this.queryVMEvents(data.params.leftNs, data.params.rightNs, data.params.typeArr)
                    }
                    break
                case "fileSystem-queryIOEvents":
                    if (data.params.list) {
                        let res = convertJSON(data.params.list) || []
                        postMessage(data.id, data.action, this.supplementIoEvents(res));
                    } else {
                        this.queryIOEvents(data.params.leftNs, data.params.rightNs, data.params.diskIOipids)
                    }
                    break
            }
        }
    }

    queryFileSysEvents(leftNs: number, rightNs: number, typeArr: Array<number>, tab: string) {
        let types = Array.from(typeArr).join(",");
        let sql = "";
        if (tab == "events") {
            sql = `
            select
                A.callchain_id as callchainId,
                (A.start_ts - B.start_ts) as startTs,
                dur,
                A.type,
                ifnull(C.name,'Process') || '[' || C.pid || ']' as process,
                ifnull(D.name,'Thread') || '[' || D.tid || ']' as thread,
                first_argument as firstArg,
                second_argument as secondArg,
                third_argument as thirdArg,
                fourth_argument as fourthArg,
                return_value as returnValue,
                fd,
                file_id as fileId,
                error_code as error
            from file_system_sample A,trace_range B
            left join process C on A.ipid = C.id
            left join thread D on A.itid = D.id
            where A.type in (${types})
            and(
                (A.end_ts - B.start_ts) between $leftNS and $rightNS
            )
            order by A.end_ts;
        `
        } else if (tab == "history") {
            sql = `
            select
                A.callchain_id as callchainId,
                (A.start_ts - B.start_ts) as startTs,
                dur,
                fd,
                A.type,
                A.file_id as fileId,
                ifnull(C.name,'Process') || '[' || C.pid || ']' as process
            from file_system_sample A,trace_range B
            left join process C on A.ipid = C.id
            where A.type in (${types})
            and fd not null
            and(
                (A.start_ts - B.start_ts) between $leftNS and $rightNS 
            )
            order by A.end_ts;
        `
        } else {
            sql = `
            select TB.callchain_id                                  as callchainId,
                (TB.start_ts - TR.start_ts)                         as startTs,
                (${rightNs} - TB.start_ts)                          as dur,
                TB.fd,
                TB.type,
                TB.file_id                                          as fileId,
                ifnull(TC.name, 'Process') || '[' || TC.pid || ']'  as process
            from (
                select fd,ipid,
                    max(case when type = 0 then A.end_ts else 0 end) as openTs,
                    max(case when type = 1 then A.end_ts else 0 end) as closeTs
                from file_system_sample A
                where type in (0, 1) and A.end_ts between $leftNS and $rightNS group by fd,ipid
                ) TA
            left join file_system_sample TB on TA.fd = TB.fd and TA.ipid = TB.ipid and TA.openTs = TB.end_ts
            left join process TC on TB.ipid = TC.ipid
            left join trace_range TR
            where startTs not null and TB.fd not null and TA.closeTs < TA.openTs
            order by TB.end_ts;  `
        }
        this.queryData("fileSystem-queryFileSysEvents", sql, {$leftNS: leftNs, $rightNS: rightNs})
    }

    queryVMEvents(leftNs: number, rightNs: number, typeArr: Array<number>){
        let types = Array.from(typeArr).join(",");
        let sql = `select
                A.callchain_id as callchainId,
                (A.start_ts - B.start_ts) as startTs,
                dur,
                addr as address,
                C.pid,
                T.tid,
                size,
                A.type,
                ifnull(T.name,'Thread') || '[' || T.tid || ']' as thread,
                ifnull(C.name,'Process') || '[' || C.pid || ']' as process
            from paged_memory_sample A,trace_range B
            left join process C on A.ipid = C.id
            left join thread T on T.id = A.itid 
            where (
                (A.end_ts - B.start_ts) between $leftNS and $rightNS
            );`;
        this.queryData("fileSystem-queryVMEvents", sql, {$leftNS: leftNs, $rightNS: rightNs})
    }

    queryIOEvents(leftNs: number, rightNs: number, diskIOipids: Array<number>){
        let ipidsSql = '';
        if(diskIOipids.length > 0){
            ipidsSql += `and A.ipid in (${diskIOipids.join(",")})`
        }
        let sql = `select
                A.callchain_id as callchainId,
                (A.start_ts - B.start_ts) as startTs,
                latency_dur as dur,
                path_id as pathId,
                dur_per_4k as durPer4k,
                tier,
                size,
                A.type,
                block_number as blockNumber,
                T.tid,
                C.pid,
                ifnull(T.name,'Thread') || '[' || T.tid || ']' as thread,
                ifnull(C.name,'Process') || '[' || C.pid || ']' as process
            from bio_latency_sample A,trace_range B
            left join process C on A.ipid = C.id
            left join thread T on T.id = A.itid 
            where (
                (A.end_ts - B.start_ts) between $leftNS and $rightNS
            ) ${ipidsSql};`;
        this.queryData("fileSystem-queryIOEvents", sql, {$leftNS: leftNs, $rightNS: rightNs})
    }

    getStacksByCallchainId(id: number) {
        let stacks = ProcedureLogicWorkerFileSystem.callChainsMap.get(id) ?? [];
        let arr: Array<Stack> = [];
        for (let s of stacks) {
            let st: Stack = new Stack()
            st.path = (ProcedureLogicWorkerFileSystem.data_dict.get(s.pathId) ?? "Unknown Path").split("/").reverse()[0];
            st.symbol = `${s.symbolsId == null ? s.ip : ProcedureLogicWorkerFileSystem.data_dict.get(s.symbolsId) ?? ''} (${st.path})`;
            st.type = (st.path.endsWith(".so.1") || st.path.endsWith(".dll") || st.path.endsWith(".so")) ? 0 : 1;
            arr.push(st);
        }
        return arr;
    }

    supplementIoEvents(res: Array<IoCompletionTimes>){
        return res.map((event) => {
            if(typeof event.pathId == 'string'){
                event.pathId = parseInt(event.pathId)
            }
            event.startTsStr = getTimeString(event.startTs)
            event.durPer4kStr = event.durPer4k==0?"-":getProbablyTime(event.durPer4k)
            event.sizeStr = getByteWithUnit(event.size)
            event.durStr = getProbablyTime(event.dur)
            event.path = event.pathId?ProcedureLogicWorkerFileSystem.data_dict.get(event.pathId) ?? '-':"-"
            // @ts-ignore
            event.operation = DISKIO_TYPE_MAP[`${event.type}`]||"UNKNOW"
            let stacks = ProcedureLogicWorkerFileSystem.callChainsMap.get(event.callchainId)||[];
            if(stacks.length > 0){
                let stack = stacks[0]
                event.backtrace = [stack.symbolsId == null ? stack.ip : ProcedureLogicWorkerFileSystem.data_dict.get(stack.symbolsId) ?? "", `(${stacks.length} other frames)`];
            }else {
                event.backtrace = [];
            }
            return event
        })
    }

    supplementVMEvents(res: Array<VirtualMemoryEvent>){
        return res.map((event) => {
            event.startTsStr = getTimeString(event.startTs)
            event.sizeStr = getByteWithUnit(event.size*4096)
            event.durStr = getProbablyTime(event.dur)
            // @ts-ignore
            event.operation = VM_TYPE_MAP[`${event.type}`]||"UNKNOW"
            return event
        })
    }


    supplementFileSysEvents(res: Array<FileSysEvent>, tab: string) {
        res.map((r) => {
            let stacks = ProcedureLogicWorkerFileSystem.callChainsMap.get(r.callchainId);
            r.startTsStr = getTimeString(r.startTs);
            r.durStr = getProbablyTime(r.dur);
            if (tab == "events") {
                r.firstArg = r.firstArg ?? "0x0"
                r.secondArg = r.secondArg ?? "0x0"
                r.thirdArg = r.thirdArg ?? "0x0"
                r.fourthArg = r.fourthArg ?? "0x0"
                r.returnValue = r.returnValue ?? "0x0"
                r.error = r.error ?? "0x0"
                r.path = ProcedureLogicWorkerFileSystem.data_dict.get(r.fileId) ?? "-";
            }
            // @ts-ignore
            r.typeStr = FILE_TYPE_MAP[`${r.type}`] ?? "";
            if (stacks && stacks.length > 0) {
                let stack = stacks[0]
                r.depth = stacks.length;
                r.symbol = stack.symbolsId == null ? stack.ip : ProcedureLogicWorkerFileSystem.data_dict.get(stack.symbolsId) ?? ""
                if (tab != "events") {
                    r.path = ProcedureLogicWorkerFileSystem.data_dict.get(r.fileId) ?? "-";
                }
                r.backtrace = [r.symbol, `(${r.depth} other frames)`];
            } else {
                r.depth = 0;
                r.symbol = "";
                r.path = "";
                r.backtrace = [];
            }
        })
        return res;
    }

    initCallchains() {
        if (this.handlerMap.size > 0) {
            this.handlerMap.forEach((value)=>{
                value.clearAll();
            })
            this.handlerMap.clear();
        }
        this.handlerMap.set("fileSystem",new FileSystemCallTreeHandler("fileSystem",this.queryData))
        this.handlerMap.set("io",new FileSystemCallTreeHandler("io",this.queryData))
        this.handlerMap.set("virtualMemory",new FileSystemCallTreeHandler("virtualMemory",this.queryData))
        ProcedureLogicWorkerFileSystem.callChainsMap.clear();
        this.queryData("fileSystem-queryCallchains", `select callchain_id as callChainId,depth,symbols_id as symbolsId,file_path_id as pathId,ip from ebpf_callstack`, {})
    }


    initCallChainTopDown(list: any[]) {
        list.forEach((callchain: FileCallChain) => {
            if (ProcedureLogicWorkerFileSystem.callChainsMap.has(callchain.callChainId)) {
                ProcedureLogicWorkerFileSystem.callChainsMap.get(callchain.callChainId)!.push(callchain)
            } else {
                ProcedureLogicWorkerFileSystem.callChainsMap.set(callchain.callChainId, [callchain])
            }
        })
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

}

class FileSystemCallTreeHandler {
    currentTreeMapData: any = {}
    allProcess: FileMerageBean[] = []
    dataSource: FileMerageBean[] = []
    currentDataType:string = ""
    currentTreeList: any[] = []
    samplesList:FileSample[] = [];
    splitMapData: any = {}
    searchValue: string = ""
    queryData = (action: string,sql: string,args: any) =>{}

    constructor(type:string,queryData:any) {
        this.currentDataType = type;
        this.queryData = queryData
    }

    queryCallchainsSamples(selectionParam: any){
        switch (this.currentDataType) {
            case "fileSystem":
                this.queryFileSamples(selectionParam)
                break;
            case "io":
                this.queryIOSamples(selectionParam)
                break;
            case "virtualMemory":
                this.queryEpbfSamples(selectionParam)
                break;
        }
    }

    queryFileSamples(selectionParam: any) {
        let sql = '';
        if (selectionParam.fileSystemType != undefined && selectionParam.fileSystemType.length > 0) {
            sql += " and s.type in ("
            sql += selectionParam.fileSystemType.join(",")
            sql += ")"
        }
        if(selectionParam.diskIOipids.length > 0&&!selectionParam.diskIOLatency&&selectionParam.fileSystemType.length == 0){
            sql += ` and s.ipid in (${selectionParam.diskIOipids.join(",")})`
        }
        this.queryData("fileSystem-queryFileSamples", `select s.callchain_id as callChainId,h.tid,h.name as threadName,s.dur,s.type,p.pid,p.name as processName from file_system_sample s,trace_range t 
left join process p on p.id = s.ipid  
left join thread h on h.id = s.itid 
where s.end_ts between $startTime + t.start_ts and $endTime + t.start_ts ${sql} and callchain_id != -1;`
            , {$startTime: selectionParam.leftNs, $endTime: selectionParam.rightNs})

    }

    queryIOSamples(selectionParam: any){
        let sql = '';
        if(selectionParam.diskIOipids.length > 0){
            sql += `and (s.ipid in (${selectionParam.diskIOipids.join(",")}) and s.type in (5,6)) `
        }
        if(selectionParam.diskIOReadIds.length > 0){
            sql += `or (s.ipid in (${selectionParam.diskIOReadIds.join(",")}) and s.type in (1,3)) `
        }
        if(selectionParam.diskIOWriteIds.length > 0){
            sql += `or (s.ipid in (${selectionParam.diskIOWriteIds.join(",")}) and s.type in (2,4)) `
        }
        this.queryData("fileSystem-queryIoSamples", `select s.callchain_id as callChainId,h.tid,h.name as threadName,s.latency_dur as dur,s.type,p.pid,p.name as processName from bio_latency_sample s,trace_range t
left join process p on p.id = s.ipid
left join thread h on h.id = s.itid
where s.end_ts between $startTime + t.start_ts and $endTime + t.start_ts ${sql} and callchain_id != -1;`
            , {$startTime: selectionParam.leftNs, $endTime: selectionParam.rightNs})
    }

    queryEpbfSamples(selectionParam: any){
        let sql = '';
        if(selectionParam.diskIOipids.length > 0&&!selectionParam.diskIOLatency&&!selectionParam.fileSysVirtualMemory){
            sql += ` and s.ipid in (${selectionParam.diskIOipids.join(",")})`
        }
        this.queryData("fileSystem-queryVirtualMemorySamples", `select s.callchain_id as callChainId,h.tid,h.name as threadName,s.dur,s.type,p.pid,p.name as processName from paged_memory_sample s,trace_range t 
left join process p on p.id = s.ipid  
left join thread h on h.id = s.itid 
where s.end_ts between $startTime + t.start_ts and $endTime + t.start_ts ${sql} and callchain_id != -1;`
            , {$startTime: selectionParam.leftNs, $endTime: selectionParam.rightNs})
    }

    freshCurrentCallchains(samples: FileSample[], isTopDown: boolean) {
        this.currentTreeMapData = {}
        this.currentTreeList = []
        this.allProcess = [];
        this.dataSource = []
        let totalCount = 0

        samples.forEach((sample) => {
            totalCount += sample.dur;
            let callChains = this.createThreadAndType(sample)
            if(callChains.length == 2){
                return
            }
            let topIndex = isTopDown ? 0 : (callChains.length - 1);
            if (callChains.length > 1) {
                let root = this.currentTreeMapData[callChains[topIndex].symbolsId + "" + callChains[topIndex].pathId + sample.pid];
                if (root == undefined) {
                    root = new FileMerageBean();
                    this.currentTreeMapData[callChains[topIndex].symbolsId + "" + callChains[topIndex].pathId + sample.pid] = root;
                    this.currentTreeList.push(root)
                }
                FileMerageBean.merageCallChainSample(root, callChains[topIndex], sample, false);
                this.merageChildrenByIndex(root, callChains, topIndex, sample, isTopDown);
            }
        })
        let rootMerageMap: any = {}
        Object.values(this.currentTreeMapData).forEach((merageData: any) => {
            if (rootMerageMap[merageData.pid] == undefined) {
                let processMerageData = new FileMerageBean()//新增进程的节点数据
                processMerageData.canCharge = false
                processMerageData.symbolName = merageData.processName
                processMerageData.symbol = processMerageData.symbolName
                processMerageData.children.push(merageData)
                processMerageData.initChildren.push(merageData)
                processMerageData.dur = merageData.dur;
                processMerageData.count = merageData.count;
                processMerageData.total = totalCount;
                rootMerageMap[merageData.pid] = processMerageData
            } else {
                rootMerageMap[merageData.pid].children.push(merageData)
                rootMerageMap[merageData.pid].initChildren.push(merageData)
                rootMerageMap[merageData.pid].dur += merageData.dur;
                rootMerageMap[merageData.pid].count += merageData.count;
                rootMerageMap[merageData.pid].total = totalCount;
            }
            merageData.parentNode = rootMerageMap[merageData.pid]//子节点添加父节点的引用
        })
        let id = 0;
        this.currentTreeList.forEach((node) => {
            node.total = totalCount;
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
        this.allProcess = Object.values(rootMerageMap)
    }

    createThreadAndType(sample: FileSample){
        let typeCallchain = new FileCallChain();
        typeCallchain.callChainId = sample.callChainId
        let map:any = {}
        if(this.currentDataType == "fileSystem"){
            map = FILE_TYPE_MAP
        }else if(this.currentDataType == "io"){
            map = DISKIO_TYPE_MAP
        }else if(this.currentDataType == "virtualMemory"){
            map = VM_TYPE_MAP
        }
        // @ts-ignore
        typeCallchain.ip = map[sample.type.toString()]||"UNKNOW";
        typeCallchain.symbolsId = sample.type
        typeCallchain.pathId = -1;
        let threadCallChain = new FileCallChain();
        threadCallChain.callChainId = sample.callChainId
        threadCallChain.ip = (sample.threadName||"Thread")+`-${sample.tid}`
        threadCallChain.symbolsId = sample.tid;
        threadCallChain.pathId = -1;
        return [typeCallchain,threadCallChain,...(ProcedureLogicWorkerFileSystem.callChainsMap.get(sample.callChainId)||[])]
    }


    merageChildrenByIndex(currentNode: FileMerageBean, callChainDataList: any[], index: number, sample: FileSample, isTopDown: boolean) {
        isTopDown ? index++ : index--;
        let isEnd = isTopDown ? (callChainDataList.length == index + 1) : (index == 0)
        let node;
        if (currentNode.initChildren.filter((child: any) => {
            if (child.ip == callChainDataList[index]?.ip
                ||((child.symbolsId!=null)
                    &&child.symbolsId == callChainDataList[index]?.symbolsId
                    &&child.pathId == callChainDataList[index]?.pathId)) {
                node = child;
                FileMerageBean.merageCallChainSample(child, callChainDataList[index], sample, isEnd)
                return true;
            }
            return false;
        }).length == 0) {
            node = new FileMerageBean()
            FileMerageBean.merageCallChainSample(node, callChainDataList[index], sample, isEnd)
            currentNode.children.push(node)
            currentNode.initChildren.push(node)
            this.currentTreeList.push(node)
            node.parentNode = currentNode
        }
        if (node && !isEnd) this.merageChildrenByIndex(node, callChainDataList, index, sample, isTopDown)
    }

    setMerageName(currentNode: FileMerageBean) {
        if (currentNode.pathId == -1) {
            currentNode.canCharge = false;
            currentNode.symbol = currentNode.ip;
            currentNode.symbolName = currentNode.symbol;
            currentNode.libName = "";
            currentNode.path = "";
        } else {
            currentNode.symbol = ProcedureLogicWorkerFileSystem.data_dict.get(currentNode.symbolsId) || currentNode.ip || 'unkown'
            currentNode.path = ProcedureLogicWorkerFileSystem.data_dict.get(currentNode.pathId) || 'unkown'
            currentNode.libName = setFileName(currentNode.path)
            currentNode.symbolName = `${currentNode.symbol} (${currentNode.libName})`
        }
    }

    resolvingAction(params: any[]) {
        if (params.length > 0) {
            params.forEach((item) => {
                if (item.funcName && item.funcArgs) {
                    switch (item.funcName) {
                        case "getCallChainsBySampleIds":
                            this.freshCurrentCallchains(this.samplesList, item.funcArgs[0])
                            break
                        case "getCurrentDataFromDb":
                            this.queryCallchainsSamples(item.funcArgs[0]);
                            break
                        case "hideSystemLibrary":
                            merageBeanDataSplit.hideSystemLibrary(this.allProcess, this.splitMapData);
                            break
                        case "hideNumMaxAndMin":
                            merageBeanDataSplit.hideNumMaxAndMin(this.allProcess, this.splitMapData, item.funcArgs[0], item.funcArgs[1])
                            break
                        case "splitAllProcess":
                            merageBeanDataSplit.splitAllProcess(this.allProcess, this.splitMapData, item.funcArgs[0])
                            break
                        case "resetAllNode":
                            merageBeanDataSplit.resetAllNode(this.allProcess, this.currentTreeList, this.searchValue)
                            break
                        case "resotreAllNode":
                            merageBeanDataSplit.resotreAllNode(this.splitMapData, item.funcArgs[0])
                            break
                        case "clearSplitMapData":
                            this.clearSplitMapData(item.funcArgs[0])
                            break
                        case "splitTree":
                            merageBeanDataSplit.splitTree(this.splitMapData, this.allProcess, item.funcArgs[0], item.funcArgs[1], item.funcArgs[2], this.currentTreeList, this.searchValue);
                            break
                        case "setSearchValue":
                            this.searchValue = item.funcArgs[0]
                            break
                    }
                }
            })
            this.dataSource = this.allProcess.filter((process) => {
                return process.children && process.children.length > 0
            })
        }
        return this.dataSource
    }

    clearAll() {
        this.samplesList = []
        this.splitMapData = {}
        this.currentTreeMapData = {}
        this.currentTreeList = []
        this.searchValue = ""
        this.allProcess = []
        this.dataSource = []
        this.splitMapData = {}
        this.currentDataType = ""
    }

    clearSplitMapData(symbolName: string) {
        delete this.splitMapData[symbolName]
    }
}

class FileCallChain {
    callChainId: number = 0;
    depth: number = 0;
    symbolsId: number = 0;
    pathId: number = 0;
    ip: string = ""
}

class FileSample {
    type:number = 0
    callChainId:number = 0;
    dur:number = 0;
    pid:number = 0;
    tid:number = 0;
    threadName:string = "";
    processName:string = ""
}

export class FileMerageBean extends MerageBean {
    ip: string = ""
    symbolsId: number = 0;
    pathId: number = 0;
    processName: string = "";
    type: number = 0

    static merageCallChainSample(currentNode: FileMerageBean, callChain: FileCallChain, sample: FileSample, isEnd: boolean) {
        if (currentNode.processName == "") {
            currentNode.ip = callChain.ip
            currentNode.pid = sample.pid
            currentNode.canCharge = true
            currentNode.pathId = callChain.pathId;
            currentNode.symbolsId = callChain.symbolsId;
            currentNode.processName = sample.processName || `Process(${sample.pid})`
        }
        if (isEnd) {
            currentNode.selfDur += sample.dur;
            currentNode.self = getProbablyTime(currentNode.selfDur)
        }
        currentNode.dur += sample.dur;
        currentNode.count++;
    }
}

export class Stack {
    type: number = 0;
    symbol: string = "";
    path: string = "";
}

export class FileSysEvent {
    isSelected:boolean = false;
    id: number = 0;
    callchainId: number = 0;
    startTs: number = 0;
    startTsStr: string = "";
    durStr: string = "";
    dur: number = 0;
    process: string = "";
    thread: string = "";
    type: number = 0;
    typeStr: string = "";
    fd: number = 0;
    size: number = 0;
    depth: number = 0;
    firstArg: string = "";
    secondArg: string = "";
    thirdArg: string = "";
    fourthArg: string = "";
    returnValue: string = "";
    error: string = "";
    path: string = "";
    symbol: string = "";
    backtrace: Array<string> = [];
    fileId: number = 0;
}

export class IoCompletionTimes{
    isSelected:boolean = false;
    type:number = 0;
    callchainId: number = 0;
    startTs: number = 0;
    startTsStr: string = "";
    durStr: string = "";
    dur: number = 0;
    tid: number = 0;
    pid: number = 0;
    process: string = "";
    thread: string = "";
    path: string = "";
    pathId:number = 0;
    operation: string = "";
    size: number = 0;
    sizeStr:string = "";
    blockNumber:string = "";
    tier:number = 0;
    backtrace: Array<string> = [];
    durPer4kStr: string = ""
    durPer4k: number = 0;
}

export class VirtualMemoryEvent{
    isSelected:boolean = false;
    callchainId: number = 0;
    startTs: number = 0;
    startTsStr: string = "";
    durStr: string = "";
    dur: number = 0;
    process: string = "";
    thread: string = "";
    address:string = "";
    size:number = 0;
    sizeStr:string = "";
    type:number = 0;
    tid:number = 0;
    pid:number = 0;
    operation: string = "";
}

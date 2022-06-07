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

import {queryPerfCallchains, queryPerfFiles, queryPerfThread} from "../../database/SqlLite.js";
import {PerfCallChain, PerfCallChainMerageData, PerfFile} from "../../bean/PerfProfile.js";
import {Utils} from "../trace/base/Utils.js";

export class PerfDataQuery {

    filesData: any = {}
    samplesData: any = {}
    threadData: any = {}
    callChainData: any = {}
    splitMapData: any = {}
    currentTreeMapData: any = {}
    currentTreeList: any[] = []
    private textEncoder = new TextEncoder()

    initPrefData() {

    }

    async initPerfFiles() {
        let files = await queryPerfFiles()
        files.forEach((file) => {
            this.filesData[file.fileId] = this.filesData[file.fileId] || []
            PerfFile.setFileName(file)
            this.filesData[file.fileId].push(file)
        })
        let threads = await queryPerfThread()
        threads.forEach((thread) => {
            this.threadData[thread.tid] = thread
        })
        let callChains = await queryPerfCallchains()
        this.initCallChainBottomUp(callChains)
    }

    initCallChain(callChains: PerfCallChain[]) {
        callChains.forEach((callChain, index) => {
            if (this.threadData[callChain.tid] == undefined) {
                return
            }
            callChain.name = this.setCallChainName(callChain)
            this.callChainData[callChain.sampleId] = this.callChainData[callChain.sampleId] || []
            if (callChain.callChainId == 0) {
                this.addProcessThreadStateData(callChain)
            } else {
                PerfCallChain.setNextNode(callChains[index - 1], callChain)
            }
            this.callChainData[callChain.sampleId].push(callChain)
            callChain.depth = callChain.callChainId + 2
            if (callChains.length == index + 1 || callChains[index + 1].callChainId == 0) {
                let previousNode = callChain.previousNode
                callChain.bottomUpMerageId = callChain.name
                while (previousNode != undefined) {
                    previousNode = previousNode.previousNode
                }
            }
            this.addGroupData(callChain)
        })
    }

    initCallChainBottomUp(callChains: PerfCallChain[]) {
        callChains.forEach((callChain, index) => {
            if (this.threadData[callChain.tid] == undefined) {
                return
            }
            callChain.name = this.setCallChainName(callChain)
            this.addGroupData(callChain)
            if (index + 1 < callChains.length && callChains[index + 1].callChainId != 0) {
                PerfCallChain.setPreviousNode(callChain, callChains[index + 1])
            }
            if (callChains.length == index + 1 || callChains[index + 1].callChainId == 0) {
                this.addProcessThreadStateData(callChain)
            }
        })
    }

    setCallChainName(callChain: PerfCallChain): string {//设置调用栈的名称
        if (callChain.symbolId == -1) {
            if (this.filesData[callChain.fileId] && this.filesData[callChain.fileId].length > 0) {
                callChain.fileName = this.filesData[callChain.fileId][0].fileName
                callChain.path = this.filesData[callChain.fileId][0].path
                return this.filesData[callChain.fileId][0].fileName + "+0x" + callChain.vaddrInFile
            } else {
                callChain.fileName = "unkown"
                return "+0x" + callChain.vaddrInFile
            }
        } else {
            if (this.filesData[callChain.fileId] && this.filesData[callChain.fileId].length > callChain.symbolId) {
                callChain.fileName = this.filesData[callChain.fileId][callChain.symbolId].fileName
                callChain.path = this.filesData[callChain.fileId][callChain.symbolId].path
                return this.filesData[callChain.fileId][callChain.symbolId].symbol
            } else {
                callChain.fileName = "unkown"
                return "+0x" + callChain.vaddrInFile
            }
        }
    }

    addProcessThreadStateData(callChain: PerfCallChain) {//当调用栈为调用的根节点时
        let threadCallChain = new PerfCallChain()//新增的线程数据
        threadCallChain.depth = 0
        PerfCallChain.merageCallChain(threadCallChain, callChain)
        threadCallChain.name = this.threadData[callChain.tid].threadName + "(" + callChain.tid + ")"
        let threadStateCallChain = new PerfCallChain()//新增的线程状态数据
        PerfCallChain.merageCallChain(threadStateCallChain, callChain)
        threadStateCallChain.name = callChain.threadState || "Unkown State"
        threadStateCallChain.fileName = "Unkown Thead State"
        this.addGroupData(threadStateCallChain)
        this.addGroupData(threadCallChain)
        PerfCallChain.setNextNode(threadCallChain, threadStateCallChain)
        PerfCallChain.setNextNode(threadStateCallChain, callChain)
    }

    addGroupData(callChain: PerfCallChain) {
        this.callChainData[callChain.sampleId] = this.callChainData[callChain.sampleId] || []
        this.callChainData[callChain.sampleId].push(callChain)
    }

    getCallChainsBySampleIds(sampleIds: string[], isTopDown: boolean) {
        return this.groupNewTreeNoId(sampleIds,isTopDown)
        this.splitMapData = {};
        this.currentTreeMapData = {}
        let topCallChains: PerfCallChain[] = []
        for (let i = 0; i < sampleIds.length; i++) {
            let callChain = this.callChainData[sampleIds[i]]
            if (callChain != undefined && callChain.length > 0) {
                if (isTopDown) {
                    topCallChains.push(callChain[callChain.length - 1])
                } else {
                    topCallChains.push(callChain[0])
                }
            }
        }
        let roots = this.groupByCallChain(topCallChains, isTopDown)

        return roots;
    }

    groupByCallChain(callChains: PerfCallChain[], isTopDown: boolean) {
        let merageMap: any = {}
        callChains.forEach((rootCallChain) => {
            this.recursionCreateData(merageMap, rootCallChain, isTopDown)
        })
        this.currentTreeMapData = merageMap;
        let rootMerageMap: any = {}
        Object.values(merageMap).forEach((merageData: any) => {
            this.recursionCreateTree(merageData);
            merageData.total = callChains.length//设置weight的分母 数量为当前调用栈数量 一个算1ms
            if (merageData.parentId == '') {
                if (rootMerageMap[merageData.pid] == undefined) {
                    let processMerageData = new PerfCallChainMerageData()//新增进程的节点数据
                    processMerageData.symbolName = this.threadData[merageData.tid].processName
                    processMerageData.symbol = processMerageData.symbolName
                    processMerageData.tid = merageData.tid
                    processMerageData.children.push(merageData)
                    processMerageData.initChildren.push(merageData)
                    processMerageData.dur = merageData.dur;
                    processMerageData.total = callChains.length;
                    rootMerageMap[merageData.pid] = processMerageData
                } else {
                    rootMerageMap[merageData.pid].children.push(merageData)
                    rootMerageMap[merageData.pid].initChildren.push(merageData)
                    rootMerageMap[merageData.pid].dur += merageData.dur;
                    rootMerageMap[merageData.pid].total = callChains.length
                }
                merageData.parentNode = rootMerageMap[merageData.pid]//子节点添加父节点的引用
            }
        })
        return Object.values(rootMerageMap)
    }

    recursionCreateData(merageMap: any, currentCallChain: any, isTopDown: boolean) {
        let nextKey = isTopDown ? "nextNode" : "previousNode";
        this.mapGroupBy(merageMap, currentCallChain, isTopDown)
        if (currentCallChain[nextKey] != undefined) {
            this.recursionCreateData(merageMap, currentCallChain[nextKey], isTopDown)
        }
    }

    recursionCreateTree(merageData: any) {
        let parentNode = this.currentTreeMapData[merageData.parentId]
        while (parentNode != undefined && parentNode.isStore != 0) {
            parentNode = this.currentTreeMapData[parentNode.parentId]
        }
        if (parentNode) {
            parentNode.children.push(merageData)
            parentNode.initChildren.push(merageData)
            merageData.parentNode = parentNode//子节点添加父节点的引用
        }
    }

    mapGroupBy(map: any, callChain: PerfCallChain, isTopDown: boolean) {
        let id = isTopDown ? callChain.topDownMerageId : callChain.bottomUpMerageId
        if (map[id] == undefined) {
            let merageData = new PerfCallChainMerageData()
            PerfCallChainMerageData.merageCallChain(merageData, callChain, isTopDown)
            map[id] = merageData
        } else {
            PerfCallChainMerageData.merageCallChain(map[id], callChain, isTopDown)
        }
    }

    groupNewTreeNoId(sampleIds: string[], isTopDown: boolean) {
        this.currentTreeMapData = {}
        this.currentTreeList = []
        for (let i = 0; i < sampleIds.length; i++) {
            let callChains = this.callChainData[sampleIds[i]]
            let topIndex = isTopDown?(callChains.length - 1):0;
            if (callChains.length > 0) {
                let root = this.currentTreeMapData[callChains[topIndex].name + callChains[topIndex].pid];
                if (root == undefined) {
                    root = new PerfCallChainMerageData();
                    root.id = Utils.uuid()
                    this.currentTreeMapData[callChains[topIndex].name + callChains[topIndex].pid] = root;
                    this.currentTreeList.push(root)
                }
                PerfCallChainMerageData.merageCallChain(root, callChains[topIndex], isTopDown);
                this.merageChildren(root, callChains[topIndex], isTopDown);
            }
        }
        let rootMerageMap: any = {}
        Object.values(this.currentTreeMapData).forEach((merageData: any) => {
            if (rootMerageMap[merageData.pid] == undefined) {
                let processMerageData = new PerfCallChainMerageData()//新增进程的节点数据
                processMerageData.symbolName = this.threadData[merageData.tid].processName
                processMerageData.symbol = processMerageData.symbolName
                processMerageData.tid = merageData.tid
                processMerageData.children.push(merageData)
                processMerageData.initChildren.push(merageData)
                processMerageData.dur = merageData.dur;
                processMerageData.count =  merageData.dur;
                processMerageData.total = sampleIds.length;
                processMerageData.id = Utils.uuid()
                rootMerageMap[merageData.pid] = processMerageData
            } else {
                merageData.parentId = rootMerageMap[merageData.pid].id
                rootMerageMap[merageData.pid].children.push(merageData)
                rootMerageMap[merageData.pid].initChildren.push(merageData)
                rootMerageMap[merageData.pid].dur += merageData.dur;
                rootMerageMap[merageData.pid].count += merageData.dur;
                rootMerageMap[merageData.pid].total = sampleIds.length;
            }
            merageData.parentNode = rootMerageMap[merageData.pid]//子节点添加父节点的引用
        })
        this.currentTreeList.forEach((node) => {
            node.total = sampleIds.length;
        })
        return Object.values(rootMerageMap)
    }

    merageChildren(currentNode: PerfCallChainMerageData, callChain: any, isTopDown: boolean) {
        let nextNodeKey = isTopDown?"nextNode":"previousNode"
        if (callChain[nextNodeKey] == undefined) return
        let node;
        if (currentNode.initChildren.filter((child: PerfCallChainMerageData) => {
            if (child.symbolName == callChain[nextNodeKey]?.name) {
                node = child;
                PerfCallChainMerageData.merageCallChain(child, callChain[nextNodeKey], isTopDown)
                return true;
            }
            return false;
        }).length == 0) {
            node = new PerfCallChainMerageData()
            PerfCallChainMerageData.merageCallChain(node, callChain[nextNodeKey], isTopDown)
            node.id = Utils.uuid()
            node.parentId = currentNode.id
            currentNode.children.push(node)
            currentNode.initChildren.push(node)
            this.currentTreeList.push(node)
            node.parentNode = currentNode
        }
        if (node) this.merageChildren(node, callChain[nextNodeKey], isTopDown)
    }

    //所有的操作都是针对整个树结构的 不区分特定的数据
    splitTree(data: PerfCallChainMerageData[], name: string, isCharge: boolean, isSymbol: boolean) {
        data.forEach((process) => {
            process.children = []
            if (isCharge) {
                this.recursionChargeInitTree(process, name, isSymbol)
            } else {
                this.recursionPruneInitTree(process, name, isSymbol)
            }
        })
        this.resetAllNode(data)
    }

    recursionChargeInitTree(node: PerfCallChainMerageData, symbolName: string, isSymbol: boolean) {
        if ((isSymbol && node.symbolName == symbolName) || (!isSymbol && node.libName == symbolName)) {
            (this.splitMapData[symbolName] = this.splitMapData[symbolName] || []).push(node)
            node.isStore++;
        }
        if (node.initChildren.length > 0) {
            node.initChildren.forEach((child) => {
                this.recursionChargeInitTree(child, symbolName, isSymbol)
            })
        }
    }

    //symbol lib charge
    recursionChargeTree(node: PerfCallChainMerageData, symbolName: string, isSymbol: boolean) {
        if ((isSymbol && node.symbolName == symbolName) || (!isSymbol && node.libName == symbolName)) {
            node.currentTreeParentNode && node.currentTreeParentNode.children.splice(node.currentTreeParentNode.children.indexOf(node), 1, ...node.children);
            node.children.forEach((child) => {
                child.currentTreeParentNode = node.currentTreeParentNode
            })
        }
        if (node.children.length > 0) {
            node.children.forEach((child) => {
                this.recursionChargeTree(child, symbolName, isSymbol)
            })
        }
    }

    recursionPruneInitTree(node: PerfCallChainMerageData, symbolName: string, isSymbol: boolean) {
        if (isSymbol && node.symbolName == symbolName || (!isSymbol && node.libName == symbolName)) {
            (this.splitMapData[symbolName] = this.splitMapData[symbolName] || []).push(node)
            node.isStore++;
            this.pruneChildren(node, symbolName)
        } else if (node.initChildren.length > 0) {
            node.initChildren.forEach((child) => {
                this.recursionPruneInitTree(child, symbolName, isSymbol)
            })
        }
    }

    //symbol lib prune
    recursionPruneTree(node: PerfCallChainMerageData, symbolName: string, isSymbol: boolean) {
        if (isSymbol && node.symbolName == symbolName || (!isSymbol && node.libName == symbolName)) {
            node.currentTreeParentNode && node.currentTreeParentNode.children.splice(node.currentTreeParentNode.children.indexOf(node), 1);
        } else {
            node.children.forEach((child) => {
                this.recursionPruneTree(child, symbolName, isSymbol)
            })
        }
    }

    recursionChargeByRule(node: PerfCallChainMerageData,ruleName: string,rule:(node: PerfCallChainMerageData)=>boolean){
        if (node.initChildren.length > 0) {
            node.initChildren.forEach((child) => {
                if (rule(child)) {
                    (this.splitMapData[ruleName] = this.splitMapData[ruleName] || []).push(child)
                    child.isStore++;
                }
                this.recursionChargeByRule(child,ruleName,rule)
            })
        }
    }

    pruneChildren(node: PerfCallChainMerageData, symbolName: string) {
        if (node.initChildren.length > 0) {
            node.initChildren.forEach((child) => {
                child.isStore++;
                (this.splitMapData[symbolName] = this.splitMapData[symbolName] || []).push(child);
                this.pruneChildren(child, symbolName)
            })
        }
    }

    clearSplitMapData(symbolName: string) {
        delete this.splitMapData[symbolName]
    }

    resotreAllNode(symbols: string[]) {
        symbols.forEach((symbol) => {
            let list = this.splitMapData[symbol];
            if (list != undefined) {
                list.forEach((item: any) => {
                    item.isStore--
                })
            }
        })
    }

    resetAllNode(data: PerfCallChainMerageData[]) {
        this.resetNewAllNode(data)
    }

    resetNewAllNode(data: PerfCallChainMerageData[]) {
        data.forEach((process) => {
            process.children = []
        })
        let values = this.currentTreeList.map((item: any) => {
            item.children = []
            return item
        })
        values.forEach((item: any) => {
            if (item.parentNode != undefined) {
                if (item.isStore == 0) {
                    let parentNode = item.parentNode
                    while (parentNode != undefined && parentNode.isStore != 0 ) {
                        parentNode = parentNode.parentNode
                    }
                    if (parentNode) {
                        item.currentTreeParentNode = parentNode
                        parentNode.children.push(item)
                    }
                }
            }
        })
    }

    searchData(data: PerfCallChainMerageData[],search: string){
        this.clearSearchNode()
        this.findSearchNode(data,search)
    }

    findSearchNode(data: PerfCallChainMerageData[],search: string){
        data.forEach((node)=>{
            if(node.symbol.includes(search)){
                node.searchShow = true
                let parentNode = node.parentNode
                while(parentNode!=undefined&&!parentNode.searchShow){
                    parentNode.searchShow = true
                    parentNode = parentNode.parentNode
                }
            }else {
                node.searchShow = false
            }
            if(node.initChildren.length > 0){
                this.findSearchNode(node.initChildren,search)
            }
        })
    }

    clearSearchNode(){
        this.currentTreeList.forEach((node)=>{
            node.searchShow = true
        })
    }
}

export const perfDataQuery = new PerfDataQuery()

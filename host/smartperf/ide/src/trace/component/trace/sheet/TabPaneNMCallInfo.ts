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

import {BaseElement, element} from "../../../../base-ui/BaseElement.js";
import {LitTable} from "../../../../base-ui/table/lit-table.js";
import {SelectionParam} from "../../../bean/BoxSelection.js";
import {queryNativeHookEventTid} from "../../../database/SqlLite.js";
import {NativeHookCallInfo, NativeHookStatistics} from "../../../bean/NativeHook.js";
import {SpSystemTrace} from "../../SpSystemTrace.js";
import {Utils} from "../base/Utils.js";
import "./TabPaneFilter.js"
import {FilterData, TabPaneFilter} from "./TabPaneFilter";

@element('tabpane-native-callinfo')
export class TabPaneNMCallInfo extends BaseElement {
    private tbl: LitTable | null | undefined;
    private tblData: LitTable | null | undefined;
    private source: Array<NativeHookCallInfo> = []
    private queryResult: Array<NativeHookStatistics> = []
    private native_type:Array<string> = ["All Heap & Anonymous VM","All Heap","All Anonymous VM"];
    private filterAllocationType:string = "0"
    private filterNativeType:string = "0"
    private currentSelection:SelectionParam|undefined
    set data(val: SelectionParam | any) {
        if(val!=this.currentSelection){
            this.currentSelection = val
            this.initFilterTypes()
        }
        let types:Array<string> = []
        if(val.nativeMemory.indexOf(this.native_type[0]) != -1){
            types.push("'AllocEvent'");
            types.push("'MmapEvent'");
        }else{
            if(val.nativeMemory.indexOf(this.native_type[1]) != -1){
                types.push("'AllocEvent'");
            }
            if(val.nativeMemory.indexOf(this.native_type[2]) != -1){
                types.push("'MmapEvent'");
            }
        }
        // @ts-ignore
        this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 20 - 31) + "px"
        // @ts-ignore
        this.tblData?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight) + "px"
        // @ts-ignore
        this.tblData?.recycleDataSource = [];
        // @ts-ignore
        this.tbl?.recycleDataSource = [];
        queryNativeHookEventTid(val.leftNs,val.rightNs,types).then((result)=>{
            if(result.length > 0){
                this.queryResult = result
                this.source = this.handleQueryResult(result);
            }else{
                this.source = [];
            }
            this.filterQueryData()
        })
    }

    handleQueryResult(result:Array<NativeHookStatistics>):Array<NativeHookCallInfo>{
        let resultMap = new Map<number,NativeHookStatistics>();
        result.map((r)=>{
            resultMap.set(r.eventId,r);
        })
        let map = new Map<string,NativeHookCallInfo>();
        SpSystemTrace.HEAP_FRAME_DATA.map((frame) => {
            let frameEventId = parseInt(frame.eventId);
            if(frameEventId >= result[0].eventId && frameEventId <= result[result.length - 1].eventId){
                if(resultMap.has(frameEventId)){
                    let hook = resultMap.get(frameEventId);
                    if(hook != undefined){
                        let target = new NativeHookCallInfo();
                        target.id = frame.eventId + "_" + frame.depth;
                        target.eventId = frameEventId;
                        target.depth = frame.depth;
                        target.count = 1;
                        target.heapSize = hook.heapSize;
                        target.threadId = hook.tid;
                        target.heapSizeStr = Utils.getByteWithUnit(target.heapSize);
                        let sym_arr = frame.AllocationFunction?.split("/");
                        let lib_arr = frame.MoudleName?.split("/");
                        target.symbol = sym_arr![sym_arr!.length - 1];
                        target.library = lib_arr![lib_arr!.length - 1];
                        target.title = `[ ${target.symbol} ]  ${target.library}`;
                        target.type = (target.library.endsWith(".so.1") || target.library.endsWith(".dll") || target.library.endsWith(".so")) ? 0 : 1;
                        if(map.has(frame.eventId)){
                            let src = map.get(frame.eventId);
                            this.listToTree(target,src!);
                        }else{
                            map.set(frame.eventId,target);
                        }
                    }
                }
            }
            if(frameEventId > result[result.length -1].eventId){
                return false;
            }
        });
        let groupMap = new Map<string,Array<NativeHookCallInfo>>();
        for (let value of map.values()) {
            let key = value.threadId+ "_" + value.symbol;
            if(groupMap.has(key)){
                groupMap.get(key)!.push(value);
            }else{
                let arr:Array<NativeHookCallInfo> = [];
                arr.push(value);
                groupMap.set(key,arr);
            }
        }
        let data:Array<NativeHookCallInfo> = [];
        for (let arr of groupMap.values()) {
            if(arr.length > 1){
                for (let i = 1; i < arr.length; i++) {
                    if(arr[i].children.length > 0){
                        this.mergeTree(arr[i].children[0],arr[0]);
                    }else{
                        arr[0].heapSize += arr[i].heapSize;
                        arr[0].heapSizeStr = Utils.getByteWithUnit(arr[0].heapSize);
                    }
                }
            }
            arr[0].count = arr.length;
            data.push(arr[0]);
        }
        return this.groupByWithTid(data)
    }

    groupByWithTid(data:Array<NativeHookCallInfo>):Array<NativeHookCallInfo>{
        let tidMap = new Map<number,NativeHookCallInfo>();
        for (let call of data) {
            call.pid = "tid_"+call.threadId;
            if(tidMap.has(call.threadId)){
                let tidCall = tidMap.get(call.threadId);
                tidCall!.heapSize += call.heapSize;
                tidCall!.heapSizeStr = Utils.getByteWithUnit(tidCall!.heapSize);
                tidCall!.count += call.count;
                tidCall!.children.push(call);
            }else{
                let tidCall = new NativeHookCallInfo();
                tidCall.id = "tid_" + call.threadId;
                tidCall.count = call.count;
                tidCall.heapSize = call.heapSize;
                tidCall.heapSizeStr = Utils.getByteWithUnit(call.heapSize);
                tidCall.title = "Thread " + call.threadId;
                tidCall.type = -1;
                tidCall.children.push(call);
                tidMap.set(call.threadId,tidCall);
            }
        }
        return Array.from(tidMap.values());
    }

    listToTree(target:NativeHookCallInfo,src:NativeHookCallInfo){
        if(target.depth == src.depth + 1){
            target.pid = src.id;
            src.children.push(target)
        }else{
            if(src.children.length > 0){
                this.listToTree(target,src.children[0]);
            }
        }
    }

    mergeTree(target:NativeHookCallInfo,src:NativeHookCallInfo){
        let len = src.children.length;
        if(len == 0){
            target.pid = src.id;
            src.heapSize += target.heapSize;
            src.heapSizeStr = Utils.getByteWithUnit(src.heapSize);
            src.children.push(target);
        }else{
            let index = src.children.findIndex((hook) => hook.symbol == target.symbol && hook.depth == target.depth);
            src.heapSize += target.heapSize;
            src.heapSizeStr = Utils.getByteWithUnit(src.heapSize);
            if(index != -1){
                let srcChild = src.children[index];
                srcChild.count += 1;
                if(target.children.length > 0){
                    this.mergeTree(target.children[0],srcChild)
                }else{
                    srcChild.heapSize += target.heapSize;
                    srcChild.heapSizeStr = Utils.getByteWithUnit(srcChild.heapSize)
                }
            }else{
                target.pid = src.id;
                src.children.push(target)
            }
        }
    }

    setRightTableData(hook:NativeHookCallInfo){
        let arr:Array<NativeHookCallInfo> = [];
        let maxEventId = hook.eventId;
        let maxHeap = 0;
        function findMaxStack(hook:NativeHookCallInfo){
            if(hook.children.length == 0){
                if(hook.heapSize > maxHeap){
                    maxHeap = hook.heapSize;
                    maxEventId = hook.eventId;
                }
            }else{
                hook.children.map((hookChild)=>{
                    findMaxStack(hookChild);
                })
            }
        }
        findMaxStack(hook);
        SpSystemTrace.HEAP_FRAME_DATA.map((frame) => {
            let eventId = parseInt(frame.eventId);
            if(eventId == maxEventId){
                let target = new NativeHookCallInfo();
                target.eventId = eventId;
                target.depth = frame.depth;
                let sym_arr = frame.AllocationFunction?.split("/");
                let lib_arr = frame.MoudleName?.split("/");
                target.symbol = sym_arr![sym_arr!.length - 1];
                target.library = lib_arr![lib_arr!.length - 1];
                target.title = `[ ${target.symbol} ]  ${target.library}`;
                target.type = (target.library.endsWith(".so.1") || target.library.endsWith(".dll") || target.library.endsWith(".so")) ? 0 : 1;
                arr.push(target);
            }
            if(eventId > maxEventId){
                return false;
            }
        });
            // @ts-ignore
        this.tblData?.recycleDataSource = arr;
    }

    initFilterTypes(){
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>("#filter")
        this.queryResult = []
        filter!.firstSelect = "0"
        filter!.secondSelect = "0"
        this.filterAllocationType = "0"
        this.filterNativeType = "0"
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-native-callinfo');
        this.tblData = this.shadowRoot?.querySelector<LitTable>('#tb-native-data');
        this.tbl!.addEventListener("row-click", (e) => {
            // @ts-ignore
            let data = (e.detail as NativeHookCallInfo)
            this.setRightTableData(data);
        })
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector(".table").style.height =  (this.parentElement.clientHeight)-10-31+"px";
                this.tbl?.reMeauseHeight()
                // @ts-ignore
                this.tblData?.shadowRoot.querySelector(".table").style.height =  (this.parentElement.clientHeight) -10+"px"
                this.tblData?.reMeauseHeight()
            }
        }).observe(this.parentElement!);
        this.shadowRoot?.querySelector<TabPaneFilter>("#filter")!.getFilterData((data:FilterData)=>{
            this.filterAllocationType = data.firstSelect||"0"
            this.filterNativeType = data.secondSelect||"0"
            this.filterQueryData()
        })
        this.initFilterTypes()
    }

    filterQueryData(){
        if (this.queryResult.length > 0&&this.currentSelection) {
            let filter = this.queryResult.filter((item)=>{
                let filterAllocation = true
                let filterNative = true
                if(this.filterAllocationType=="1"){
                    filterAllocation = item.startTs>=this.currentSelection!.leftNs&&item.startTs<=this.currentSelection!.rightNs&&item.endTs>this.currentSelection!.rightNs
                }else if(this.filterAllocationType=="2"){
                    filterAllocation = item.startTs>=this.currentSelection!.leftNs&&item.startTs<=this.currentSelection!.rightNs&&item.endTs<=this.currentSelection!.rightNs
                }
                if(this.filterNativeType=="1"){
                    filterNative = item.eventType == "AllocEvent"
                }else if(this.filterNativeType=="2"){
                    filterNative = item.eventType == "MmapEvent"
                }
                return filterAllocation&&filterNative
            })
            if(filter.length>0){
                this.source = this.handleQueryResult(filter);
                this.tbl!.recycleDataSource = this.source;
            }else {
                this.source = []
                this.tbl!.recycleDataSource = [];
            }
        }
    }

    initHtml(): string {
        return `
<style>
:host{
    display: flex;
    flex-direction: column;
    padding: 10px 10px 0 10px;
}
</style>
<div style="display: flex;flex-direction: row">
    <div style="width: 65%">
        <lit-table id="tb-native-callinfo" style="height: auto" tree>
            <lit-table-column width="70%" title="Symbol Name" data-index="title" key="title"  align="flex-start"></lit-table-column>
            <lit-table-column width="1fr" title="Size" data-index="heapSizeStr" key="heapSizeStr"  align="flex-end"></lit-table-column>
            <lit-table-column width="1fr" title="Count" data-index="count" key="count"  align="flex-end"></lit-table-column>
            <lit-table-column width="1fr" title="  " data-index="type" key="type"  align="flex-start" >
                <template>
                    <img src="img/library.png" size="20" v-if=" type == 1 ">
                    <img src="img/function.png" size="20" v-if=" type == 0 ">
                    <div v-if=" type == - 1 "></div>
                </template>
            </lit-table-column>
        </lit-table>
        <tab-pane-filter id="filter" first second></tab-pane-filter>
    </div>
    <div style="width: 35%">
        <lit-table id="tb-native-data" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)">
            <lit-table-column width="60px" title="" data-index="type" key="type"  align="flex-start" >
                <template>
                    <img src="img/library.png" size="20" v-if=" type == 1 ">
                    <img src="img/function.png" size="20" v-if=" type == 0 ">
                </template>
            </lit-table-column>
            <lit-table-column width="1fr" title="" data-index="title" key="title"  align="flex-start"></lit-table-column>
        </lit-table>
    </div>
</div>

        `;
    }
}
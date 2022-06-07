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
import "../../FrameChart.js";
import {FrameChart} from "../../FrameChart.js";
import {ChartMode} from "../../../database/ProcedureWorkerCommon.js";


@element('tabpane-native-callinfo')
export class TabPaneNMCallInfo extends BaseElement {
    private tbl: LitTable | null | undefined;
    private tblData: LitTable | null | undefined;
    private source: Array<NativeHookCallInfo> = []
    private rightSource: Array<NativeHookCallInfo> = []
    private queryResult: Array<NativeHookStatistics> = []
    private native_type: Array<string> = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"];
    private filterAllocationType: string = "0"
    private filterNativeType: string = "0"
    private currentSelection: SelectionParam | undefined
    private frameChart: FrameChart | null | undefined;
    private isChartShow: boolean = false;
    private selectTotalSize = 0;

    set data(val: SelectionParam | any) {
        if (val != this.currentSelection) {
            this.currentSelection = val
            this.initFilterTypes()
        }
        let types: Array<string> = []
        if (val.nativeMemory.indexOf(this.native_type[0]) != -1) {
            types.push("'AllocEvent'");
            types.push("'MmapEvent'");
        } else {
            if (val.nativeMemory.indexOf(this.native_type[1]) != -1) {
                types.push("'AllocEvent'");
            }
            if (val.nativeMemory.indexOf(this.native_type[2]) != -1) {
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
        queryNativeHookEventTid(val.leftNs, val.rightNs, types).then((result) => {
            if (result.length > 0) {
                this.queryResult = result;
                this.source = this.handleQueryResult(result);
            } else {
                this.source = [];
                this.frameChart!.data = [];
                this.frameChart?.clearCanvas();
            }
            this.filterQueryData()
        })
    }

    handleQueryResult(result: Array<NativeHookStatistics>): Array<NativeHookCallInfo> {
        this.selectTotalSize = 0;
        let resultMap = new Map<number, NativeHookStatistics>();
        for (let event of result) {
            this.selectTotalSize += event.heapSize;
        }

        let maxDepth = 0;
        this.frameChart!.selectTotalSize = this.selectTotalSize;

        result.map((r) => {
            resultMap.set(r.eventId, r);
        });
        let map = new Map<string, NativeHookCallInfo>();
        SpSystemTrace.HEAP_FRAME_DATA.map((frame) => {
            let frameEventId = parseInt(frame.eventId);
            if (frameEventId >= result[0].eventId && frameEventId <= result[result.length - 1].eventId) {
                if (resultMap.has(frameEventId)) {
                    let hook = resultMap.get(frameEventId);
                    if (hook != undefined) {
                        maxDepth = Math.max(maxDepth, frame.depth);
                        let target = new NativeHookCallInfo();
                        target.id = frame.eventId + "_" + frame.depth;
                        target.eventId = frameEventId;
                        target.depth = frame.depth;
                        target.count = 1;
                        target.size = hook.heapSize;
                        target.threadId = hook.tid;
                        target.heapSizeStr = Utils.getByteWithUnit(target.size);
                        let sym_arr = frame.AllocationFunction?.split("/");
                        let lib_arr = frame.MoudleName?.split("/");
                        target.symbol = sym_arr![sym_arr!.length - 1];
                        target.library = lib_arr![lib_arr!.length - 1];
                        target.title = `[ ${target.symbol} ]  ${target.library}`;
                        target.type = (target.library.endsWith(".so.1") || target.library.endsWith(".dll") || target.library.endsWith(".so")) ? 0 : 1;
                        if (map.has(frame.eventId)) {
                            let src = map.get(frame.eventId);
                            this.listToTree(target, src!);
                        } else {
                            map.set(frame.eventId, target);
                        }
                    }
                }
            }
            if (frameEventId > result[result.length - 1].eventId) {
                return false;
            }
        });
        let groupMap = new Map<string, Array<NativeHookCallInfo>>();
        for (let value of map.values()) {
            let key = value.threadId + "_" + value.symbol;
            if (groupMap.has(key)) {
                groupMap.get(key)!.push(value);
            } else {
                let arr: Array<NativeHookCallInfo> = [];
                arr.push(value);
                groupMap.set(key, arr);
            }
        }
        let data: Array<NativeHookCallInfo> = [];
        for (let arr of groupMap.values()) {
            if (arr.length > 1) {
                for (let i = 1; i < arr.length; i++) {
                    if (arr[i].children.length > 0) {
                        this.mergeTree(<NativeHookCallInfo>arr[i].children[0], arr[0]);
                    } else {
                        arr[0].size += arr[i].size;
                        arr[0].heapSizeStr = Utils.getByteWithUnit(arr[0].size);

                    }
                }
            }
            arr[0].count = arr.length;
            data.push(arr[0]);
        }
        this.frameChart!.mode = ChartMode.Byte;
        this.frameChart!.maxDepth = maxDepth;
        return this.groupByWithTid(data)
    }

    groupByWithTid(data: Array<NativeHookCallInfo>): Array<NativeHookCallInfo> {
        let tidMap = new Map<number, NativeHookCallInfo>();
        for (let call of data) {
            call.pid = "tid_" + call.threadId;
            if (tidMap.has(call.threadId)) {
                let tidCall = tidMap.get(call.threadId);
                tidCall!.size += call.size;
                tidCall!.heapSizeStr = Utils.getByteWithUnit(tidCall!.size);
                tidCall!.count += call.count;
                tidCall!.children.push(call);
            } else {
                let tidCall = new NativeHookCallInfo();
                tidCall.id = "tid_" + call.threadId;
                tidCall.count = call.count;
                tidCall.size = call.size;
                tidCall.heapSizeStr = Utils.getByteWithUnit(call.size);
                tidCall.title = "Thread " + call.threadId;
                tidCall.symbol = tidCall.title;
                tidCall.type = -1;
                tidCall.children.push(call);
                tidMap.set(call.threadId, tidCall);
            }
        }
        this.source = data;
        let showData = Array.from(tidMap.values())
        this.frameChart!.data = showData;
        this.frameChart?.updateCanvas();
        this.frameChart?.calculateChartData();
        return showData;
    }

    listToTree(target: NativeHookCallInfo, src: NativeHookCallInfo) {
        if (target.depth == src.depth + 1) {
            target.pid = src.id;
            src.children.push(target)
        } else {
            if (src.children.length > 0) {
                this.listToTree(target, <NativeHookCallInfo>src.children[0]);
            }
        }
    }

    mergeTree(target: NativeHookCallInfo, src: NativeHookCallInfo) {
        let len = src.children.length;
        if (len == 0) {
            target.pid = src.id;
            src.size += target.size;
            src.heapSizeStr = Utils.getByteWithUnit(src.size);
            src.children.push(target);
        } else {
            let index = src.children.findIndex((hook) => hook.symbol == target.symbol && hook.depth == target.depth);
            src.size += target.size;
            src.heapSizeStr = Utils.getByteWithUnit(src.size);
            if (index != -1) {
                let srcChild = <NativeHookCallInfo>src.children[index];
                srcChild.count += 1;
                if (target.children.length > 0) {
                    this.mergeTree(<NativeHookCallInfo>target.children[0], <NativeHookCallInfo>srcChild)
                } else {
                    srcChild.size += target.size;
                    srcChild.heapSizeStr = Utils.getByteWithUnit(srcChild.size)

                }
            } else {
                target.pid = src.id;
                src.children.push(target)
            }
        }
    }

    getParentTree(src: Array<NativeHookCallInfo>, target: NativeHookCallInfo, parents: Array<NativeHookCallInfo>): boolean {
        for (let hook of src) {
            if (hook.id == target.id) {
                parents.push(hook)
                return true
            } else {
                if (this.getParentTree(hook.children as Array<NativeHookCallInfo>, target, parents)) {
                    parents.push(hook);
                    return true;
                }
            }
        }
        return false;
    }

    getChildTree(src: Array<NativeHookCallInfo>, eventId: number, children: Array<NativeHookCallInfo>): boolean {
        for (let hook of src) {
            if (hook.eventId == eventId && hook.children.length == 0) {
                children.push(hook)
                return true
            } else {
                if (this.getChildTree(hook.children as Array<NativeHookCallInfo>, eventId, children)) {
                    children.push(hook);
                    return true;
                }
            }
        }
        return false;
    }

    setRightTableData(hook: NativeHookCallInfo) {
        let parents: Array<NativeHookCallInfo> = [];
        let children: Array<NativeHookCallInfo> = [];
        this.getParentTree(this.source, hook, parents);
        let maxEventId = hook.eventId;
        let maxHeap = 0;

        function findMaxStack(hook: NativeHookCallInfo) {
            if (hook.children.length == 0) {
                if (hook.size > maxHeap) {
                    maxHeap = hook.size;
                    maxEventId = hook.eventId;
                }
            } else {
                hook.children.map((hookChild) => {
                    findMaxStack(<NativeHookCallInfo>hookChild);
                })
            }
        }

        findMaxStack(hook);
        this.getChildTree(hook.children as Array<NativeHookCallInfo>, maxEventId, children);
        this.rightSource = parents.reverse().concat(children.reverse());
        let len = this.rightSource.length;
        // @ts-ignore
        this.tblData?.recycleDataSource = len == 0 ? [] : this.rightSource.slice(1, len);

    }

    initFilterTypes() {
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>("#filter")
        this.queryResult = []
        filter!.firstSelect = "0"
        filter!.secondSelect = "0"
        this.filterAllocationType = "0"
        this.filterNativeType = "0"
    }

    sortTreeByColumn(column: string, sort: number) {
        this.tbl!.recycleDataSource = this.sortTree(this.source, column, sort)
    }

    sortTree(arr: Array<NativeHookCallInfo>, column: string, sort: number): Array<NativeHookCallInfo> {
        let sortArr = arr.sort((a, b) => {
            if (column == 'size') {
                if (sort == 0) {
                    return a.eventId - b.eventId;
                } else if (sort == 1) {
                    return a.size - b.size;
                } else {
                    return b.size - a.size;
                }
            } else {
                if (sort == 0) {
                    return a.eventId - b.eventId;
                } else if (sort == 1) {
                    return a.count - b.count;
                } else {
                    return b.count - a.count;
                }
            }
        })
        sortArr.map((call) => {
            call.children = this.sortTree(call.children as Array<NativeHookCallInfo>, column, sort);
        })
        return sortArr;
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-native-callinfo');
        this.tblData = this.shadowRoot?.querySelector<LitTable>('#tb-native-data');
        this.frameChart = this.shadowRoot?.querySelector<FrameChart>('#framechart');
        let pageTab = this.shadowRoot?.querySelector('#show_table');
        let pageChart = this.shadowRoot?.querySelector('#show_chart');
        this.tbl!.addEventListener("row-click", (e) => {
            // @ts-ignore
            let data = (e.detail.data as NativeHookCallInfo);
            (data as any).isSelected = true
            this.setRightTableData(data);
            this.tblData?.clearAllSelection(data)
            this.tblData?.setCurrentSelection(data)
            // @ts-ignore
            if ((e.detail as any).callBack) {
                // @ts-ignore
                (e.detail as any).callBack(true)
            }
        })
        this.tblData!.addEventListener("row-click", (e) => {
            this.tbl!.expandList(this.rightSource)
            // @ts-ignore
            let detail = e.detail.data as NativeHookCallInfo;
            this.tbl?.clearAllSelection(detail)
            detail.isSelected = true
            this.tbl!.scrollToData(this.rightSource[detail.depth + 1])
            // @ts-ignore
            if ((e.detail as any).callBack) {
                // @ts-ignore
                (e.detail as any).callBack(true)
            }
        })
        this.tbl!.addEventListener('column-click', (evt) => {
            // @ts-ignore
            this.sortTreeByColumn(evt.detail.key == "count" ? 'count' : 'size', evt.detail.sort)
        });
        let filterHeight = 0;
        new ResizeObserver((entries) => {
            let tabPaneFilter = this.shadowRoot!.querySelector("#filter") as HTMLElement;
            if (tabPaneFilter.clientHeight > 0) filterHeight = tabPaneFilter.clientHeight;
            if (this.parentElement!.clientHeight > filterHeight) {
                tabPaneFilter.style.display = "flex";
            } else {
                tabPaneFilter.style.display = "none";
            }
            if (this.parentElement?.clientHeight != 0) {
                if (!this.isChartShow) {
                    // @ts-ignore
                    this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight) + "px"
                    this.tbl?.reMeauseHeight()
                } else {
                    // @ts-ignore
                    this.frameChart?.updateCanvas();
                    this.frameChart?.calculateChartData();
                }
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight) - 10 - 31 + "px";
                this.tbl?.reMeauseHeight();
                // @ts-ignore
                this.tblData?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight) - 10 - 31 + "px";
                this.tblData?.reMeauseHeight()
            }
        }).observe(this.parentElement!);
        this.shadowRoot?.querySelector<TabPaneFilter>("#filter")!.getFilterData((data: FilterData) => {
            if (data.icon == 'block') {
                pageChart?.setAttribute('class', 'show');
                pageTab?.setAttribute('class', '');
                this.isChartShow = true;
            } else if (data.icon == 'tree') {
                pageChart?.setAttribute('class', '');
                pageTab?.setAttribute('class', 'show');
                this.isChartShow = false;
            }
            this.filterAllocationType = data.firstSelect || "0"
            this.filterNativeType = data.secondSelect || "0"
            this.filterQueryData()
        });

        this.parentElement!.onscroll = () => {
            this.frameChart!.tabPaneScrollTop = this.parentElement!.scrollTop;
        };
        this.initFilterTypes()
    }

    filterQueryData() {
        if (this.queryResult.length > 0 && this.currentSelection) {
            let filter = this.queryResult.filter((item) => {
                let filterAllocation = true;
                let filterNative = true;
                if (this.filterAllocationType == "1") {
                    filterAllocation = item.startTs >= this.currentSelection!.leftNs && item.startTs <= this.currentSelection!.rightNs && item.endTs > this.currentSelection!.rightNs
                } else if (this.filterAllocationType == "2") {
                    filterAllocation = item.startTs >= this.currentSelection!.leftNs && item.startTs <= this.currentSelection!.rightNs && item.endTs <= this.currentSelection!.rightNs
                }
                if (this.filterNativeType == "1") {
                    filterNative = item.eventType == "AllocEvent"
                } else if (this.filterNativeType == "2") {
                    filterNative = item.eventType == "MmapEvent"
                }
                return filterAllocation && filterNative
            });
            if (filter.length > 0) {
                this.source = this.handleQueryResult(filter);
                this.tbl!.recycleDataSource = this.source;
            } else {
                this.source = []
                this.tbl!.recycleDataSource = [];
                this.frameChart!.data = [];
                this.frameChart!.clearCanvas()
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
        tab-pane-filter {
            border: solid rgb(216,216,216) 1px;
            float: left;
            text-align: center;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
        selector{
            display: none;
        }
        .show{
            margin: 0 0 60px 0;
            display: flex;
            flex: 1;
        }
        </style>
        <div style="display: flex;flex-direction: row">
            <selector id='show_table' class="show">
                <div style="width: 65%">
                    <lit-table id="tb-native-callinfo" style="height: auto" tree>
                        <lit-table-column width="70%" title="Symbol Name" data-index="title" key="title"  align="flex-start">
                        </lit-table-column>
                        <lit-table-column width="1fr" title="Size" data-index="heapSizeStr" key="heapSizeStr"  order>
                        </lit-table-column>
                        <lit-table-column width="1fr" title="Count" data-index="count" key="count" order>
                        </lit-table-column>
                        <lit-table-column width="1fr" title="  " data-index="type" key="type"  align="flex-start" >
                            <template>
                                <img src="img/library.png" size="20" v-if=" type == 1 ">
                                <img src="img/function.png" size="20" v-if=" type == 0 ">
                                <div v-if=" type == - 1 "></div>
                            </template>
                        </lit-table-column>
                    </lit-table>
                </div>
                <div style="width: 35%">
                    <lit-table id="tb-native-data" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)">
                        <lit-table-column width="60px" title="" data-index="type" key="type"  align="flex-start" >
                            <template>
                                <img src="img/library.png" size="20" v-if=" type == 1 ">
                                <img src="img/function.png" size="20" v-if=" type == 0 ">
                            </template>
                        </lit-table-column>
                        <lit-table-column width="1fr" title="" data-index="title" key="title"  align="flex-start">
                        </lit-table-column>
                    </lit-table>
                </div>
            </selector>
            <selector id='show_chart'>
                <tab-framechart id='framechart' style='width: 100%;height: auto'> </tab-framechart>
            </selector>
            <tab-pane-filter id="filter" icon first second></tab-pane-filter>
        </div>
        `;
    }
}
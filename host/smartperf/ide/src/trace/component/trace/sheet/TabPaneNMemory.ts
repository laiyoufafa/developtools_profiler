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
import {queryNativeHookEventId} from "../../../database/SqlLite.js";
import {NativeHookCallInfo, NativeHookStatistics, NativeMemory} from "../../../bean/NativeHook.js";
import {SpSystemTrace} from "../../SpSystemTrace.js";
import {HeapTreeDataBean} from "../../../bean/HeapTreeDataBean.js";
import {Utils} from "../base/Utils.js";
import "./TabPaneFilter.js"
import {FilterData, TabPaneFilter} from "./TabPaneFilter";
import {TabPaneNMSampleList} from "./TabPaneNMSampleList.js";

@element('tabpane-native-memory')
export class TabPaneNMemory extends BaseElement {
    private defaultNativeTypes = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"];
    private tbl: LitTable | null | undefined;
    private tblData: LitTable | null | undefined;
    private source: Array<NativeMemory> = []
    private native_type: Array<string> = [...this.defaultNativeTypes];
    private statsticsSelection: Array<any> = []
    private queryResult: Array<NativeHookStatistics> = []
    private filterAllocationType: string = "0"
    private filterNativeType: string = "0"
    private currentSelection: SelectionParam | undefined
    private rowSelectData: any = undefined;

    set data(val: SelectionParam | any) {
        if (val == this.currentSelection) {
            return
        }
        this.currentSelection = val
        this.initFilterTypes()
        let types: Array<string> = []
        if (val.nativeMemory.indexOf(this.defaultNativeTypes[0]) != -1) {
            types.push("'AllocEvent'");
            types.push("'MmapEvent'");
        } else {
            if (val.nativeMemory.indexOf(this.defaultNativeTypes[1]) != -1) {
                types.push("'AllocEvent'");
            }
            if (val.nativeMemory.indexOf(this.defaultNativeTypes[2]) != -1) {
                types.push("'MmapEvent'");
            }
        }
        TabPaneNMSampleList.serSelection(val)
        // @ts-ignore
        this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 20 - 31) + "px"
        // @ts-ignore
        this.tblData?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 20) + "px"
        // @ts-ignore
        this.tblData?.recycleDataSource = [];
        // @ts-ignore
        this.tbl?.recycleDataSource = [];
        queryNativeHookEventId(val.leftNs, val.rightNs, types).then((result) => {
            if (result.length > 0) {
                this.queryResult = result
                this.source = this.handleQueryResult(result);
            } else {
                this.source = [];
            }
            this.filterQueryData()
        })
    }

    fromStastics(val: SelectionParam | any) {
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>("#filter")
        let typeIndexOf = this.native_type.indexOf(val.statisticsSelectData.memoryTap);
        if (this.statsticsSelection.indexOf(val.statisticsSelectData) == -1 && typeIndexOf == -1) {
            this.statsticsSelection.push(val.statisticsSelectData)
            this.native_type.push(val.statisticsSelectData.memoryTap)
            typeIndexOf = this.native_type.length - 1
        }
        if (this.currentSelection != val) {
            //设置选项后刷新当前的数据
            this.data = val
            //todo 设置filter当前的选项和选中项
            filter!.setSelectList(null, this.native_type)
            filter!.secondSelect = typeIndexOf + ""
            this.filterNativeType = typeIndexOf + ""
        } else {
            this.tblData!.recycleDataSource = [];
            this.rowSelectData = undefined
            filter!.setSelectList(null, this.native_type)
            filter!.secondSelect = typeIndexOf + ""
            this.filterNativeType = typeIndexOf + ""
            //直接将当前数据过滤即可
            this.filterQueryData()
        }
    }

    getTypeFromIndex(indexOf: number, item: NativeHookStatistics): boolean {
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
        } else if (indexOf - 3 < this.statsticsSelection.length) {
            let selectionElement = this.statsticsSelection[indexOf - 3];
            if (selectionElement.memoryTap != undefined && selectionElement.max != undefined) {
                if (selectionElement.memoryTap.indexOf("Malloc") != -1) {
                    return item.eventType == "AllocEvent" && item.heapSize == selectionElement.max
                } else {
                    return item.subType == selectionElement.memoryTap && item.heapSize == selectionElement.max
                }
            }
        }
        return false;
    }

    handleQueryResult(result: Array<NativeHookStatistics>): Array<NativeMemory> {
        let resultMap = new Map<number, NativeHookStatistics>();
        result.map((r) => {
            resultMap.set(r.eventId, r);
        })
        let data: Array<NativeMemory> = [];
        let frameArr: Array<HeapTreeDataBean> = [];
        SpSystemTrace.HEAP_FRAME_DATA.map((frame) => {
            let frameEventId = parseInt(frame.eventId);
            if (frameEventId >= result[0].eventId && frameEventId <= result[result.length - 1].eventId) {
                if (resultMap.has(frameEventId) && frame.depth == 0) {
                    frameArr.push(frame);
                }
            }
            if (frameEventId > result[result.length - 1].eventId) {
                return false;
            }
        });

        let frameMap = new Map<number, HeapTreeDataBean>();
        frameArr.map((frame) => {
            frameMap.set(parseInt(frame.eventId), frame);
        })
        for (let i = 0, len = result.length; i < len; i++) {
            let hook = result[i];
            let memory = new NativeMemory();
            memory.index = i;
            memory.eventId = hook.eventId;
            memory.eventType = hook.eventType;
            memory.subType = hook.subType;
            memory.heapSize = hook.heapSize;
            memory.heapSizeUnit = Utils.getByteWithUnit(hook.heapSize);
            memory.addr = "0x" + hook.addr;
            memory.startTs = hook.startTs;
            memory.timestamp = Utils.getTimeString(hook.startTs);
            (memory as any).isSelected = hook.isSelected;
            let frame = frameMap.get(hook.eventId);
            if (frame != null && frame != undefined) {
                let sym_arr = frame.AllocationFunction?.split("/");
                let lib_arr = frame.MoudleName?.split("/");
                memory.symbol = sym_arr![sym_arr!.length - 1];
                memory.library = lib_arr![lib_arr!.length - 1];
            }
            data.push(memory);
        }
        return data
    }

    initFilterTypes() {
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>("#filter")
        this.queryResult = []
        filter!.setSelectList(null, this.defaultNativeTypes)
        filter!.firstSelect = "0"
        filter!.secondSelect = "0"
        this.filterAllocationType = "0"
        this.filterNativeType = "0"
        this.rowSelectData = undefined
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-native-memory');
        this.tblData = this.shadowRoot?.querySelector<LitTable>('#tb-native-data');
        this.tbl!.addEventListener("row-click", (e) => {
            // @ts-ignore
            let data = (e.detail.data as NativeMemory);
            this.rowSelectData = data
            this.setRightTableData(data);
            document.dispatchEvent(new CustomEvent('triangle-flag', {detail: {time: data.startTs, type: "triangle"}}));
        })
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight) - 10 - 31 + "px";
                this.tbl?.reMeauseHeight();
                // @ts-ignore
                this.tblData?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight) - 10 + "px"
                this.tblData?.reMeauseHeight()
            }
        }).observe(this.parentElement!)
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>("#filter")
        this.shadowRoot?.querySelector<TabPaneFilter>("#filter")!.getFilterData((data: FilterData) => {
            if (data.mark) {
                document.dispatchEvent(new CustomEvent('triangle-flag', {
                    detail: {
                        time: "", type: "square", timeCallback: (t: any) => {
                            let minTs = 0
                            let minItem: any = undefined
                            let filterTemp = this.source.filter((tempItem) => {
                                if (minTs == 0 || (tempItem.startTs - t != 0 && Math.abs(tempItem.startTs - t) < minTs)) {
                                    minTs = Math.abs(tempItem.startTs - t)
                                    minItem = tempItem
                                }
                                return tempItem.startTs == t
                            })
                            if (filterTemp.length > 0) {
                                filterTemp[0].isSelected = true
                            } else {
                                if (minItem) {
                                    filterTemp.push(minItem)
                                    minItem.isSelected = true
                                }
                            }
                            if (filterTemp.length > 0) {
                                this.rowSelectData = filterTemp[0]
                                let currentSelection = this.queryResult.filter((item) => {
                                    return item.startTs == this.rowSelectData.startTs
                                })
                                if (currentSelection.length > 0) {
                                    currentSelection[0].isSelected = true
                                }
                                TabPaneNMSampleList.addSampleData(this.rowSelectData)
                                this.tbl!.scrollToData(this.rowSelectData)
                            }
                        }
                    }
                }));
            } else {
                this.filterAllocationType = data.firstSelect || "0"
                this.filterNativeType = data.secondSelect || "0"
                this.filterQueryData()
            }
        })
        filter!.firstSelect = "1"
    }

    filterQueryData() {
        if (this.queryResult.length > 0 && this.currentSelection) {
            let filter = this.queryResult.filter((item) => {
                let filterAllocation = true
                if (this.filterAllocationType == "1") {
                    filterAllocation = item.startTs >= this.currentSelection!.leftNs && item.startTs <= this.currentSelection!.rightNs && item.endTs > this.currentSelection!.rightNs
                } else if (this.filterAllocationType == "2") {
                    filterAllocation = item.startTs >= this.currentSelection!.leftNs && item.startTs <= this.currentSelection!.rightNs && item.endTs <= this.currentSelection!.rightNs
                }
                let filterNative = this.getTypeFromIndex(parseInt(this.filterNativeType), item)
                return filterAllocation && filterNative
            })
            if (filter.length > 0) {
                this.source = this.handleQueryResult(filter);
                this.tbl!.recycleDataSource = this.source;
            } else {
                this.source = []
                this.tbl!.recycleDataSource = [];
            }
        }
    }

    setRightTableData(hook: NativeMemory) {
        let arr: Array<NativeHookCallInfo> = [];
        let frameArr = SpSystemTrace.HEAP_FRAME_DATA.filter((frame) => parseInt(frame.eventId) == hook.eventId);
        frameArr.map((frame) => {
            let target = new NativeHookCallInfo();
            target.eventId = parseInt(frame.eventId);
            target.depth = frame.depth;
            let sym_arr = frame.AllocationFunction?.split("/");
            let lib_arr = frame.MoudleName?.split("/");
            target.symbol = sym_arr![sym_arr!.length - 1];
            target.library = lib_arr![lib_arr!.length - 1];
            target.title = `[ ${target.symbol} ]  ${target.library}`;
            target.type = (target.library.endsWith(".so.1") || target.library.endsWith(".dll") || target.library.endsWith(".so")) ? 0 : 1;
            arr.push(target);
        })
        // @ts-ignore
        this.tblData?.recycleDataSource = arr;
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
                <lit-table id="tb-native-memory" style="height: auto">
                    <lit-table-column width="40px" title="#" data-index="index" key="index"  align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="1fr" title="Address" data-index="addr" key="addr"  align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="1fr" title="Memory Type" data-index="eventType" key="eventType"  align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="1fr" title="Timestamp" data-index="timestamp" key="timestamp"  align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="1fr" title="Size" data-index="heapSizeUnit" key="heapSizeUnit"  align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="20%" title="Responsible Library" data-index="library" key="library"  align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="20%" title="Responsible Caller" data-index="symbol" key="symbol"  align="flex-start">
                    </lit-table-column>
                </lit-table>
                <tab-pane-filter id="filter" mark first second></tab-pane-filter>
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
        </div>
        `;
    }
}

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
import {
    queryNativeHookStatistics,
    queryNativeHookStatisticsMalloc,
    queryNativeHookStatisticsSubType
} from "../../../database/SqlLite.js";
import {NativeHookMalloc, NativeHookStatistics, NativeHookStatisticsTableData} from "../../../bean/NativeHook.js";
import {Utils} from "../base/Utils.js";
import {SpSystemTrace} from "../../SpSystemTrace.js";
import "./TabProgressBar.js"

@element('tabpane-native-statistics')
export class TabPaneNMStatstics extends BaseElement {
    private tbl: LitTable | null | undefined;
    private source: Array<NativeHookStatisticsTableData> = []
    private native_type: Array<string> = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"];
    private allMax: number = 0;

    set data(val: SelectionParam | any) {
        this.allMax = 0;
        SpSystemTrace.EVENT_HEAP.map((heap) => {
            this.allMax += heap.sumHeapSize;
        });
        // @ts-ignore
        this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 20) + "px"
        // @ts-ignore
        this.tbl?.recycleDataSource = [];
        Promise.all([queryNativeHookStatistics(val.leftNs, val.rightNs),
            queryNativeHookStatisticsSubType(val.leftNs, val.rightNs),
            queryNativeHookStatisticsMalloc(val.leftNs, val.rightNs)
        ]).then((values) => {
            let arr: Array<NativeHookStatisticsTableData> = [];
            let index1 = val.nativeMemory.indexOf(this.native_type[0])
            let index2 = val.nativeMemory.indexOf(this.native_type[1])
            let index3 = val.nativeMemory.indexOf(this.native_type[2])
            this.setMemoryTypeData(val, values[0], arr);
            if (index1 != -1 || index3 != -1) {
                this.setSubTypeTableData(values[1], arr);
            }
            if (index1 != -1 || index2 != -1) {
                this.setMallocTableData(values[2], arr);
            }
            this.tbl!.recycleDataSource = arr;
        })
    }

    setMallocTableData(result: Array<NativeHookMalloc>, arr: Array<NativeHookStatisticsTableData>) {
        result.map((malloc) => {
            let data = new NativeHookStatisticsTableData();
            data.memoryTap = "Malloc " + Utils.getByteWithUnit(malloc.heapSize);
            data.existing = malloc.allocByte;
            data.allocCount = malloc.allocCount;
            data.freeCount = malloc.freeCount;
            data.totalBytes = malloc.allocByte + malloc.freeByte;
            data.totalCount = malloc.allocCount + malloc.freeCount;
            data.max = malloc.heapSize;
            data.existingString = Utils.getByteWithUnit(data.existing);
            data.totalBytesString = Utils.getByteWithUnit(data.totalBytes);
            data.maxStr = Utils.getByteWithUnit(malloc.heapSize);
            data.existingValue = [data.existing, data.totalBytes, this.allMax];
            arr.push(data);
        })
    }

    setSubTypeTableData(result: Array<NativeHookMalloc>, arr: Array<NativeHookStatisticsTableData>) {
        result.map((sub) => {
            if (sub.subType != null && sub.subType != "") {
                let data = new NativeHookStatisticsTableData();
                data.memoryTap = sub.subType
                data.existing = sub.allocByte
                data.allocCount = sub.allocCount;
                data.freeCount = sub.freeCount;
                data.totalBytes = sub.allocByte + sub.freeByte;
                data.totalCount = sub.allocCount + sub.freeCount;
                data.max = sub.heapSize;
                data.existingString = Utils.getByteWithUnit(data.existing);
                data.totalBytesString = Utils.getByteWithUnit(data.totalBytes);
                data.maxStr = Utils.getByteWithUnit(sub.heapSize);
                data.existingValue = [data.existing, data.totalBytes, this.allMax];
                arr.push(data);
            }
        })
    }

    setMemoryTypeData(val: SelectionParam, result: Array<NativeHookStatistics>, arr: Array<NativeHookStatisticsTableData>) {
        let all: NativeHookStatisticsTableData | null = null
        let heap: NativeHookStatisticsTableData | null = null
        let anonymous: NativeHookStatisticsTableData | null = null
        if (val.nativeMemory.indexOf(this.native_type[0]) != -1) {
            all = new NativeHookStatisticsTableData();
            all.memoryTap = this.native_type[0];
        }
        if (val.nativeMemory.indexOf(this.native_type[1]) != -1) {
            heap = new NativeHookStatisticsTableData();
            heap.memoryTap = this.native_type[1];
        }
        if (val.nativeMemory.indexOf(this.native_type[2]) != -1) {
            anonymous = new NativeHookStatisticsTableData();
            anonymous.memoryTap = this.native_type[2];
        }
        for (let hook of result) {
            if (all != null) {
                if (hook.eventType == "AllocEvent" || hook.eventType == "MmapEvent") {
                    all.existing += hook.sumHeapSize;
                    all.allocCount += hook.count;
                    all.totalBytes += hook.sumHeapSize;
                    all.totalCount += hook.count;
                    if (hook.max > all.max) {
                        all.max = hook.max;
                        all.maxStr = Utils.getByteWithUnit(all.max);
                    }
                } else if (hook.eventType == "FreeEvent" || hook.eventType == "MunmapEvent") {
                    all.totalBytes += hook.sumHeapSize;
                    all.freeCount += hook.count;
                    all.totalCount += hook.count;
                }
            }
            if (heap != null) {
                if (hook.eventType == "AllocEvent") {
                    heap.existing += hook.sumHeapSize;
                    heap.allocCount += hook.count;
                    heap.totalBytes += hook.sumHeapSize;
                    heap.totalCount += hook.count;
                    if (hook.max > heap.max) {
                        heap.max = hook.max;
                        heap.maxStr = Utils.getByteWithUnit(heap.max);
                    }
                } else if (hook.eventType == "FreeEvent") {
                    heap.totalBytes += hook.sumHeapSize;
                    heap.totalCount += hook.count;
                    heap.freeCount += hook.count;
                }
            }
            if (anonymous != null) {
                if (hook.eventType == "MmapEvent") {
                    anonymous.existing += hook.sumHeapSize;
                    anonymous.allocCount += hook.count;
                    anonymous.totalBytes += hook.sumHeapSize;
                    anonymous.totalCount += hook.count;
                    if (hook.max > anonymous.max) {
                        anonymous.max = hook.max;
                        anonymous.maxStr = Utils.getByteWithUnit(anonymous.max);
                    }
                } else if (hook.eventType == "MunmapEvent") {
                    anonymous.totalBytes += hook.sumHeapSize;
                    anonymous.freeCount += hook.count;
                    anonymous.totalCount += hook.count;
                }
            }
        }
        if (all != null) {
            all.existingString = Utils.getByteWithUnit(all.existing)
            all.totalBytesString = Utils.getByteWithUnit(all.totalBytes)
            all.existingValue = [all.existing, all.totalBytes, this.allMax]
            arr.push(all)
        }
        if (heap != null) {
            heap.existingString = Utils.getByteWithUnit(heap.existing)
            heap.totalBytesString = Utils.getByteWithUnit(heap.totalBytes)
            heap.existingValue = [heap.existing, heap.totalBytes, this.allMax]
            arr.push(heap)
        }
        if (anonymous != null) {
            anonymous.existingString = Utils.getByteWithUnit(anonymous.existing)
            anonymous.totalBytesString = Utils.getByteWithUnit(anonymous.totalBytes)
            anonymous.existingValue = [anonymous.existing, anonymous.totalBytes, this.allMax]
            arr.push(anonymous)
        }
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-native-statstics');
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 20) + "px"
                this.tbl?.reMeauseHeight()
            }
        }).observe(this.parentElement!)
    }


    initHtml(): string {
        return `
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px;
        }
        </style>
        <lit-table id="tb-native-statstics" style="height: auto">
            <lit-table-column width="25%" title="Memory Type" data-index="memoryTap" key="memoryTap"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="1fr" title="Existing" data-index="existingString" key="existingString"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="1fr" title="# Existing" data-index="allocCount" key="allocCount"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="1fr" title="# Transient" data-index="freeCount" key="freeCount"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="1fr" title="Total Bytes" data-index="totalBytesString" key="totalBytesString"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="1fr" title="Peak Value" data-index="maxStr" key="maxStr"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="1fr" title="# Total" data-index="totalCount" key="totalCount"  align="flex-start">
            </lit-table-column>
            <lit-table-column width="160px" title="Existing / Total" data-index="existingValue" key="existingValue"  align="flex-start" >
                <template>
                <tab-progress-bar data="{{existingValue}}">
                </tab-progress-bar>
                </template>
            </lit-table-column>
        </lit-table>
        `;
    }
}

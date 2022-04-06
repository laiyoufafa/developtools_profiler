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

import "../../../../base-ui/table/lit-table-column.js";
import {BaseElement, element} from "../../../../base-ui/BaseElement.js";
import {LitTable} from "../../../../base-ui/table/lit-table.js";
import {SelectionParam} from "../../../bean/BoxSelection.js";
import {queryHeapTable} from "../../../database/SqlLite.js";
import {Utils} from "../base/Utils.js";

@element('tabpane-heap')
export class TabPaneHeap extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;

    set data(val: SelectionParam | any) {
        queryHeapTable(val.leftNs, val.rightNs, val.heapIds).then((result) => {
            result.forEach((item) => {
                console.log(item);
                item.AllocationSize = Utils.getByteWithUnit(Number(item.AllocationSize))
                item.DeAllocationSize = Utils.getByteWithUnit(Number(item.DeAllocationSize))
                item.RemainingSize = Utils.getByteWithUnit(Number(item.RemainingSize))
            })
            console.log(result);
            this.tbl!.dataSource = result
        })
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-heap');
        this.range = this.shadowRoot?.querySelector('#time-range')
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

<lit-table id="tb-heap" style="height: auto">
    <lit-table-column width="170px" title="Allocation Function" data-index="AllocationFunction" key="AllocationFunction" align="center" >
    <template><div style="font-size:0.8rem;padding: 0 5px">{{AllocationFunction}}</div></template>
</lit-table-column>
    <lit-table-column width="170px" title="Moudle Name" data-index="MoudleName" key="MoudleName" align="center">
    <template><div style="font-size:0.8rem;padding: 0 5px;word-break: break-word">{{MoudleName}}</div></template>
</lit-table-column>
    <lit-table-column width="1fr" title="Allocations" data-index="Allocations" key="Allocations" align="center" >
    <template><div style="font-size:0.8rem;padding: 0 5px">{{Allocations}}</div></template>
</lit-table-column>
    <lit-table-column width="1fr" title="Deallocations" data-index="Deallocations" key="Deallocations" align="center" >
        <template><div style="font-size:0.8rem;padding: 0 5px">{{Deallocations}}</div></template>
</lit-table-column>
    <lit-table-column width="1fr" title="Allocation Size" data-index="AllocationSize" key="AllocationSize" align="center" >
    <template><div style="font-size:0.8rem;padding: 0 5px">{{AllocationSize}}</div></template>
</lit-table-column>
    <lit-table-column width="1fr" title="DeAllocation Size" data-index="DeAllocationSize" key="DeAllocationSize" align="center" >
    <template><div style="font-size:0.8rem;padding: 0 5px">{{DeAllocationSize}}</div></template>
</lit-table-column>
    <lit-table-column title="Total Count" data-index="Total" key="Total" align="center" >
    <template><div style="font-size:0.8rem;padding: 0 5px">{{Total}}</div></template>
</lit-table-column>
    <lit-table-column width="1fr" title="Remaining Size" data-index="RemainingSize" key="RemainingSize" align="center" >
    <template><div style="font-size:0.8rem;padding: 0 5px">{{RemainingSize}}</div></template>
</lit-table-column>
</lit-table>
        `;
    }

}
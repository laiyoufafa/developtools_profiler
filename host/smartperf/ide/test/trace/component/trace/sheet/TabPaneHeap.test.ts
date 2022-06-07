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

//@ts-ignore
import {TabPaneHeap} from "../../../../../dist/trace/component/trace/sheet/TabPaneHeap.js";

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe('TabPaneHeap Test', () => {
    let tabPaneHeap = new TabPaneHeap();
    let list = [{
        AllocationSize:0,
        DeAllocationSize:0,
        RemainingSize:0,
        children:[{
            length:1
        }]
    }]
    let selection = {
        leftNs:1,
        rightNs:1
    }

    it('TabPaneHeapTest01', function () {
        tabPaneHeap.setTreeDataSize = jest.fn(()=>true)
        expect(tabPaneHeap.setTreeDataSize(list)).toBeTruthy();
    });

    it('TabPaneHeapTest02', function () {
        expect(tabPaneHeap.merageTree(1, [{length:1}],[{length: 3}],selection)).toBeUndefined();
    });

    it('TabPaneHeapTest03', function () {
        expect(tabPaneHeap.initHtml()).toMatchInlineSnapshot(`
"
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px;
        }
        </style>

        <lit-table id=\\"tb-heap\\" style=\\"height: auto\\" tree>
        <lit-table-column width=\\"30%\\" title=\\"Allocation Function\\" data-index=\\"AllocationFunction\\" key=\\"AllocationFunction\\" align=\\"center\\" >
        </lit-table-column>
        <lit-table-column width=\\"170px\\" title=\\"Moudle Name\\" data-index=\\"MoudleName\\" key=\\"MoudleName\\" >
        </lit-table-column>
        <lit-table-column width=\\"1fr\\" title=\\"Allocations\\" data-index=\\"Allocations\\" key=\\"Allocations\\" align=\\"center\\" >
        </lit-table-column>
        <lit-table-column width=\\"1fr\\" title=\\"Deallocations\\" data-index=\\"Deallocations\\" key=\\"Deallocations\\" align=\\"center\\" >
        </lit-table-column>
        <lit-table-column width=\\"1fr\\" title=\\"Allocation Size\\" data-index=\\"AllocationSize\\" key=\\"AllocationSize\\" align=\\"center\\" >
        </lit-table-column>
        <lit-table-column width=\\"1fr\\" title=\\"DeAllocation Size\\" data-index=\\"DeAllocationSize\\" key=\\"DeAllocationSize\\" align=\\"center\\" >
        </lit-table-column>
        <lit-table-column title=\\"Total Count\\" data-index=\\"Total\\" key=\\"Total\\" align=\\"center\\" >
        </lit-table-column>
        <lit-table-column width=\\"1fr\\" title=\\"Remaining Size\\" data-index=\\"RemainingSize\\" key=\\"RemainingSize\\" align=\\"center\\" >
        </lit-table-column>
        </lit-table>
        "
`);
    });
})
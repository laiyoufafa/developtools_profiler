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

// @ts-ignore
import {TabPaneNMStatstics} from "../../../../../dist/trace/component/trace/sheet/TabPaneNMStatstics.js"

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe('TabPaneNMStatstics Test', () => {
    let tabPaneNMStatstics = new TabPaneNMStatstics();

    it('TabPaneNMStatsticsTest01', function () {
        expect(tabPaneNMStatstics.setMallocTableData([1],[1])).toBeUndefined();
    });

    it('TabPaneNMStatsticsTest02', function () {
        expect(tabPaneNMStatstics.setSubTypeTableData([1],[1])).toBeUndefined();
    });

    it('TabPaneNMStatsticsTest03', function () {
        expect(tabPaneNMStatstics.initHtml()).toMatchInlineSnapshot(`
"
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px;
        }
        </style>
        <lit-table id=\\"tb-native-statstics\\" style=\\"height: auto\\">
            <lit-table-column width=\\"25%\\" title=\\"Memory Type\\" data-index=\\"memoryTap\\" key=\\"memoryTap\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Existing\\" data-index=\\"existingString\\" key=\\"existingString\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"# Existing\\" data-index=\\"allocCount\\" key=\\"allocCount\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"# Transient\\" data-index=\\"freeCount\\" key=\\"freeCount\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Total Bytes\\" data-index=\\"totalBytesString\\" key=\\"totalBytesString\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Peak Value\\" data-index=\\"maxStr\\" key=\\"maxStr\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"# Total\\" data-index=\\"totalCount\\" key=\\"totalCount\\"  align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"160px\\" title=\\"Existing / Total\\" data-index=\\"existingValue\\" key=\\"existingValue\\"  align=\\"flex-start\\" >
                <template>
                <tab-progress-bar data=\\"{{existingValue}}\\">
                </tab-progress-bar>
                </template>
            </lit-table-column>
        </lit-table>
        "
`);
    });

    it('TabPaneNMStatsticsTest04', function () {
        const val= {
            nativeMemory:[],
        }
        const result=[""]
        expect(tabPaneNMStatstics.setMemoryTypeData(val,result)).toBeUndefined();
    });
})
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
import {TabPanePTS} from "../../../../../dist/trace/component/trace/sheet/TabPanePTS.js"
window.ResizeObserver = window.ResizeObserver ||jest.fn().mockImplementation(() => ({
    disconnect: jest.fn(),
    observe: jest.fn(),
    unobserve: jest.fn(),
}));
describe('TabPanePTS Test', () => {
    let tabPanePTS = new TabPanePTS();

    let dataArray = [{
        id: "",
        pid: "",
        title: "",
        children: [],
        process: "",
        processId: 0,
        thread: "",
        threadId: 0,
        state: "",
        wallDuration: 0,
        avgDuration: "",
        count: 0,
        minDuration: 0,
        maxDuration: 0,
        stdDuration: "",
    }]

    it('TabPanePTSTest01', function () {
        let result = tabPanePTS.groupByProcessThreadToMap(dataArray);
        expect(result.get(0).length).toBeUndefined();
    });

    it('TabPanePTSTest02', function () {
        let result = tabPanePTS.groupByProcessToMap(dataArray)
        expect(result.get(0).length).toBe(1);
    });

    it('TabPanePTSTest03', function () {
        let result = tabPanePTS.groupByThreadToMap(dataArray)
        expect(result.get(0).length).toBe(1);
    });

    it('TabPanePTSTest04', function () {
        expect(tabPanePTS.initHtml()).toMatchInlineSnapshot(`
"
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px;
        }
        </style>
        <label id=\\"time-range\\" style=\\"width: 100%;height: 20px;text-align: end;font-size: 10pt;margin-bottom: 5px\\">Selected range:0.0 ms</label>
        <lit-table id=\\"tb-states\\" style=\\"height: auto\\" tree>
            <lit-table-column width=\\"27%\\" title=\\"Process/Thread/State\\" data-index=\\"title\\" key=\\"title\\" align=\\"flex-start\\">
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Count\\" data-index=\\"count\\" key=\\"count\\" align=\\"flex-start\\" >
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Duration(ns)\\" data-index=\\"wallDuration\\" key=\\"wallDuration\\" align=\\"flex-start\\" >
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Min Duration(ns)\\" data-index=\\"minDuration\\" key=\\"minDuration\\" align=\\"flex-start\\" >
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Avg Duration(ns)\\" data-index=\\"avgDuration\\" key=\\"avgDuration\\" align=\\"flex-start\\" >
            </lit-table-column>
            <lit-table-column width=\\"1fr\\" title=\\"Max Duration(ns)\\" data-index=\\"maxDuration\\" key=\\"maxDuration\\" align=\\"flex-start\\" >
            </lit-table-column>
        </lit-table>
        "
`);
    });
})

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
import {TabPaneSlices} from "../../../../../dist/trace/component/trace/sheet/TabPaneSlices.js"

const sqlit = require("../../../../../dist/trace/database/SqlLite.js")
jest.mock("../../../../../dist/trace/database/SqlLite.js");
describe('TabPaneSlices Test', () => {
    let tabPaneSlices = new TabPaneSlices();

    tabPaneSlices.sortByColumn = jest.fn(()=> true)

    it('TabPaneSlicesTest01', function () {
        expect(tabPaneSlices.sortByColumn({
            key: 'name',
            sort: () => {
            }
        })).toBeTruthy();
    });

    it('TabPaneSlicesTest02', function () {
        let mockgetTabThreadStates = sqlit.getTabSlices
        mockgetTabThreadStates.mockResolvedValue([{name : "11",
                wallDuration: 10,
                occurrences: 10,
            },{name : "22",
                wallDuration: 20,
                occurrences: 20,
            }]
        )
        let a = {rightNs: 1, leftNs: 0, funTids: [11, 12, 13]}
        expect(tabPaneSlices.data = a).toBeTruthy();
    });

    it('TabPaneSlicesTest03', function () {
        let mockgetTabThreadStates = sqlit.getTabSlices
        mockgetTabThreadStates.mockResolvedValue([])
        let a = {rightNs: 1, leftNs: 0, funTids: [11, 12, 13]}
        expect(tabPaneSlices.data = a).toBeTruthy();
    });
})

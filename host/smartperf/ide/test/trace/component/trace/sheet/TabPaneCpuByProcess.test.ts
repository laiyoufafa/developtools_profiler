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
import {TabPaneCpuByProcess} from "../../../../../dist/trace/component/trace/sheet/TabPaneCpuByProcess.js"
const sqlit = require("../../../../../dist/trace/database/SqlLite.js")
jest.mock("../../../../../dist/trace/database/SqlLite.js");
describe('TabPaneCpuByProcess Test', () => {
    let tabPaneCpuByProcess = new TabPaneCpuByProcess();
    tabPaneCpuByProcess.sortByColumn = jest.fn(()=> true)

    it('TabPaneCpuByProcessTest01', function () {
        expect(tabPaneCpuByProcess.sortByColumn({
            key: 'name',
            sort: () => {
            }
        })).toBeTruthy();
    });

    it('TabPaneCpuByProcessTest02', function () {
        let mockgetTabCpuByProcess = sqlit.getTabCpuByProcess
        mockgetTabCpuByProcess.mockResolvedValue([{process  : "test",
               wallDuration: 10,
               occurrences: 10
            },
            {process  : "test2",
            wallDuration: 11,
            occurrences: 11
            }]
        )
        let a = {rightNs: 1, cpus: [11, 12, 13]}
        expect(tabPaneCpuByProcess.data = a).toBeTruthy();
    });

    it('TabPaneCpuByProcessTest03', function () {
        let mockgetTabCpuByProcess = sqlit.getTabCpuByProcess
        mockgetTabCpuByProcess.mockResolvedValue([])
        let a = {rightNs: 1, cpus: [11, 12, 13]}
        expect(tabPaneCpuByProcess.data = a).toBeTruthy();
    });
})

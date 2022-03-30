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
import {TabPaneSPT} from "../../../../../dist/trace/component/trace/sheet/TabPaneSPT.js"

describe('TabPaneSPT Test', () => {
    let tabPaneSPT = new TabPaneSPT();

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

    it('TabPaneSPTTest01', function () {
        let result = tabPaneSPT.groupByStateToMap(dataArray);
        expect(result.get('').length).toBe(1);
    });

    it('TabPaneSPTTest02', function () {
        let result = tabPaneSPT.groupByProcessToMap(dataArray)
        expect(result.get(0).length).toBe(1);
    });

    it('TabPaneSPTTest03', function () {
        let result = tabPaneSPT.groupByStateProcessToMap(dataArray)
        expect(result.get('').get(0).length).toBe(1);
    });
})

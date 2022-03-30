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
import {TabPaneCpuUsage} from "../../../../../dist/trace/component/trace/sheet/TabPaneCpuUsage.js"

describe('TabPaneCpuUsage Test', () => {
    let tabPaneCpuUsage = new TabPaneCpuUsage();

    it('TabPaneCpuUsageTest02', function () {
        expect(tabPaneCpuUsage.sortTable([[1,2,3,9,6,4],[5,2,1,4,9,6]],1,true)).toBeUndefined();
    });

    it('TabPaneCpuUsageTest03', function () {
        expect(tabPaneCpuUsage.sortTable([[1,2,3,9,6,4],[5,2,1,4,9,6]],1,false)).toBeUndefined();
    });
    it('TabPaneCpuUsageTest04', function () {
        let result = tabPaneCpuUsage.sortFreq([{
            cpu: 0,
            value: 0,
            startNs: 0,
            dur: 0,
        },{
            cpu: 1,
            value: 2,
            startNs: 2,
            dur: 4,
        }]);
        expect(result[0][0]).toBe(2);
    });
    it('TabPaneCpuUsageTest05', function () {
        expect(tabPaneCpuUsage.getFreqTop3({
            cpu: 0,
            usage: 0,
            usageStr: "usage",
            top1: 1,
            top2: 2,
            top3: 3,
            top1Percent: 11,
            top1PercentStr: "Str1",
            top2Percent: 22,
            top2PercentStr: "Str2",
            top3Percent: 33,
            top3PercentStr: "Str3",
        }, undefined, undefined, undefined, 1)).toBeUndefined();
    });
    it('TabPaneCpuUsageTest06', function () {
        let result = tabPaneCpuUsage.groupByCpuToMap([{
            cpu: 0,
            value: 0,
            startNs: 0,
            dur: 0,
        },{
            cpu: 1,
            value: 2,
            startNs: 2,
            dur: 4,
        }])
        expect(result.get(0).length).toBe(1);
    });
})

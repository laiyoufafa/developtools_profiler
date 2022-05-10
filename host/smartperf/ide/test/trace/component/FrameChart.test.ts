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
import {FrameChart} from "../../../dist/trace/component/FrameChart.js"


describe('FrameChart Test', () => {

    let node= [
        {children: ''},
        {children:{length:1}}
        ]


    it('FrameChartTest01', function () {
        let frameChart = new FrameChart();
        frameChart.data = false;
        expect(frameChart.data).toBeFalsy();
    });

    it('FrameChartTest02', function () {
        let frameChart = new FrameChart();
        expect(frameChart.data).toBeUndefined();
    });

    it('FrameChartTest03', function () {
        let frameChart = new FrameChart();
        frameChart.selectTotalSize = true;
        expect(frameChart.selectTotalSize).toBeUndefined();
    });

    it('FrameChartTest04', function () {
        let frameChart = new FrameChart();
        frameChart.maxDepth = true;
        expect(frameChart.maxDepth).toBeUndefined();
    });

    it('FrameChartTest05',function () {
        let frameChart = new FrameChart();
        let result = frameChart.cavasContext.lineWidth ;
        expect(result).toBe(1);
    })

    it('FrameChartTest06', function () {
        let frameChart = new FrameChart();
        expect(frameChart.drawScale()).toBeUndefined();
    });

    it('FrameChartTest07', function () {
        let frameChart = new FrameChart();
        expect(frameChart.calculateChartData()).toBeUndefined();
    });

    it('FrameChartTest08', function () {
        let frameChart = new FrameChart();
        expect(frameChart.darwTypeChart(node)).toBeUndefined();
    });

    it('FrameChartTest09', function () {
        let frameChart = new FrameChart();
        frameChart.mode = true;
        expect(frameChart.mode).toBeTruthy();
    });
})
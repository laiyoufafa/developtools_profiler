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
import {SpApplication} from "../../../src/trace/SpApplication";

describe('FrameChart Test', () => {

    let node= [
        {children: ''},
        {children:{length:1}}
    ]
    let selectData = [length=1]
    const e = {
        button:0,
        clientX:1,
        clientY:1,

    }

    it('FrameChartTest01', function () {
        let frameChart = new FrameChart();
        frameChart.tabPaneScrollTop = false;
        expect(frameChart.tabPaneScrollTop).toBeFalsy();
    });

    it('FrameChartTest02', function () {
            let frameChart = new FrameChart();
            expect(frameChart.updateFloatHint()).toBeUndefined();
        });

    it('FrameChartTest03', function () {
        let frameChart = new FrameChart();
        frameChart.calculateChartData = jest.fn(()=>true)
        expect(frameChart.redrawChart(selectData)).toBeUndefined();
    });

    it('FrameChartTest04', function () {
        let frameChart = new FrameChart();
        expect(frameChart.scale()).toBeUndefined();
    });

    it('FrameChartTest05', function () {
        let frameChart = new FrameChart();
        let index = frameChart.scale(2)
        expect(index).toBe(undefined);
    });

    it('FrameChartTest06', function () {
        let frameChart = new FrameChart();
        let index = frameChart.scale(-1)
        expect(index).toBe(undefined);
    });

    it('FrameChartTest07', function () {
        let frameChart = new FrameChart();
        expect(frameChart.scale()).toBeUndefined();
    });

    it('FrameChartTest08', function () {
        let frameChart = new FrameChart();
        frameChart.translationDraw = jest.fn(()=>true)
        expect(frameChart.translation()).toBeUndefined();
    });

    it('FrameChartTest14', function () {
        let frameChart = new FrameChart();
        frameChart.translationDraw = jest.fn(()=>true)
        expect(frameChart.translation(-1)).toBeUndefined();
    });

    it('FrameChartTest09', function () {
        let frameChart = new FrameChart();
        frameChart.selectTotalCount = true;
        expect(frameChart.selectTotalCount).toBeUndefined();
    });

    it('FrameChartTest10', function () {
        let frameChart = new FrameChart();
        frameChart.data = true;
        expect(frameChart.data).toBeUndefined();
    });

    it('FrameChartTest11', function () {
        let frameChart = new FrameChart();
        frameChart.drawScale = jest.fn(()=>true)
        expect(frameChart.calculateChartData()).toBeUndefined();
    });

    it('FrameChartTest12', function () {
        let frameChart = new FrameChart();
        expect(frameChart.updateCanvas()).toBeUndefined();
    });

    it('FrameChartTest13', function () {
        let frameChart = new FrameChart();
        frameChart.translationDraw = jest.fn(()=>true)
        frameChart.lastCanvasXInScale = 0
        expect(frameChart.translationByScale()).toBe(undefined);
    });

    it('FrameChartTest21', function () {
        let frameChart = new FrameChart();
        frameChart.translationDraw = jest.fn(()=>true)
        frameChart.canvasX = 4
        frameChart.lastCanvasXInScale = 1
        expect(frameChart.translationByScale()).toBe(undefined);
    });

    it('FrameChartTest22', function () {
        let frameChart = new FrameChart();
        frameChart.translationDraw = jest.fn(()=>true)
        expect(frameChart.translationByScale(1)).toBe(undefined);
    });
    it('FrameChartTest211', function () {
        let frameChart = new FrameChart();
        expect(frameChart.searchData([],2,2)).toBeNull();
    });

    it('FrameChartTest15', function () {
        let frameChart = new FrameChart();
        frameChart.calculateChartData = jest.fn(()=>true)
        frameChart.xPoint = 1
        expect(frameChart.translationDraw()).toBe(undefined);
    });

    it('FrameChartTest16', function () {
        let frameChart = new FrameChart();
        expect(frameChart.onMouseClick({button:0})).toBeUndefined();
    });

    it('FrameChartTest17', function () {
        let frameChart = new FrameChart();
        expect(frameChart.initHtml()).toMatchInlineSnapshot(`
"
            <style>
            :host{
                display: flex;
                padding: 10px 10px;
            }
            .tip{
                position:absolute;
                left: 0;
                background-color: white;
                border: 1px solid #f9f9f9;
                width: auto;
                font-size: 8px;
                color: #50809e;
                flex-direction: column;
                justify-content: center;
                align-items: flex-start;
                padding: 2px 10px;
                display: none;
                user-select: none;
            }
            </style>
            <canvas id=\\"canvas\\"></canvas>
            <div id =\\"float_hint\\" class=\\"tip\\">
            </div>
            "
`);
    });

    it('FrameChartTest18', function () {
        let frameChart = new FrameChart();
        expect(frameChart.drawFrameChart(node)).toBeUndefined();
    });

    it('FrameChartTest19', function () {
        let frameChart = new FrameChart();
        frameChart.searchData = jest.fn(()=>true)
        frameChart.calculateChartData = jest.fn(()=>true)
        expect(frameChart.onMouseMove()).toBeUndefined();
    });

    it('FrameChartTest20', function () {
        let frameChart = new FrameChart();
        expect(frameChart.searchData([],1,1)).toBeNull();
    });

    it('FrameChartTest23', function () {
        let frameChart = new FrameChart();
        expect(frameChart.onMouseClick({button:2})).toBeUndefined();
    });

    it('FrameChartTest24', function () {
        let frameChart = new FrameChart();
        frameChart.drawScale = jest.fn(()=>true)
        expect(frameChart.drawScale()).toBeUndefined();
    });

    it('FrameChartTest25', function () {
        let frameChart = new FrameChart();
        frameChart.selectTotalSize = true;
        expect(frameChart.selectTotalSize).toBeUndefined();
    });

    it('FrameChartTest26', function () {
        let frameChart = new FrameChart();
        frameChart.maxDepth = true;
        expect(frameChart.maxDepth).toBeUndefined();
    });


    it('FrameChartTest27 ', function () {
        let frameChart = new FrameChart();
        expect(frameChart.calMaxDepth(node,1)).toBeUndefined()

    });

})

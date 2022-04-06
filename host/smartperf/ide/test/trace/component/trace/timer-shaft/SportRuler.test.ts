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
import {SportRuler} from "../../../../../dist/trace/component/trace/timer-shaft/SportRuler.js"

describe('SportRuler Test', ()=>{
    const canvas = document.createElement('canvas');
    canvas.width = 1;
    canvas.height = 1;
    const ctx = canvas.getContext('2d');

    let sportRuler = new SportRuler(canvas, ctx, {
        x: 20,
        y: 20,
        width: 100,
        height: 100
    });

    sportRuler.range = {
        totalNS: 20,
        startX: 0,
        endX: 10,
        startNS: 10,
        endNS: 20,
        xs: [],
        xsTxt: [],
    }

    it('SportRulerTest01', function () {
        expect(sportRuler.drawTheFlag(2, '#999999', false, 'text')).toBeUndefined();
    });

    it('SportRulerTest02', function () {
        let randomRgbColor = sportRuler.randomRgbColor();
        let isColor = randomRgbColor.length > 4;
        expect(isColor).toBeTruthy()
    });

    it('SportRulerTest03', function () {
        expect(sportRuler.onFlagRangeEvent({
            x: 0,
            y: 0,
            width: 0,
            height: 0,
            time: 0,
            color: "",
            selected: false,
            text: "",
        }, 2)).toBeUndefined();
    });

    it('SportRulerTest04', function () {
        expect(sportRuler.mouseMove({
            offsetY: 20,
            offsetX: 20,
        })).toBeUndefined();
    });

    it('SportRulerTest05', function () {
        let ranges = sportRuler.range;
        expect(ranges.endNS).toBe(20);
    })

    it('SportRulerTest06', function () {
        sportRuler.flagListIdx = jest.fn(()=>"flagListIdx")
        sportRuler.flagList = jest.fn(()=>true)
        expect(sportRuler.modifyFlagList('amend', {})).toBeUndefined();
    })

    it('SportRulerTest07', function () {
        sportRuler.flagList.splice = jest.fn(()=>true)
        expect(sportRuler.modifyFlagList('remove', {})).toBeUndefined();
    })

    it('SportRulerTest08', function () {
        expect(sportRuler.draw()).toBeUndefined();
    })

    it('SportRulerTest09', function () {
        expect(sportRuler.mouseUp()).toBeUndefined();
    })

    it('SportRulerTest10', function () {
        sportRuler.draw = jest.fn(()=>true)
        expect(sportRuler.mouseMove({
            offsetX: 10000,
            offsetY: 10000
        })).toBeUndefined();
    })
})

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
import {LitSlider} from "../../../dist/base-ui/slider/LitSlider.js";

describe('LitSlider Test', ()=>{
    let litSliderPanel = new LitSlider();

    litSliderPanel.disabledX = 'disabledX'
    litSliderPanel.customSlider = 'customSlider'
    litSliderPanel.customLine = 'customLine'
    litSliderPanel.customButton = 'customButton'
    litSliderPanel.percent = 'percent'
    litSliderPanel.resultUnit = 'resultUnit'

    litSliderPanel.litSlider = jest.fn(()=> true)

    litSliderPanel.sliderStyle = 'sliderStyle'

    it('LitSliderTest01', ()=>{
        expect(litSliderPanel.disabledX).toEqual('');
    })

    it('LitSliderTest02', ()=>{
        expect(litSliderPanel.customSlider).toEqual('');
    })

    it('LitSliderTest03', ()=>{
        expect(litSliderPanel.customLine).toEqual('customLine');
    })

    it('LitSliderTest04', ()=>{
        expect(litSliderPanel.customButton).toEqual('customButton');
    })

    it('LitSliderTest05', ()=>{
        expect(litSliderPanel.percent).toEqual('percent');
    })

    it('LitSliderTest06', ()=>{
        expect(litSliderPanel.resultUnit).toEqual('resultUnit');
    })

    it('LitSliderTest07', ()=>{
        expect(litSliderPanel.formatSeconds(10)).toBe('00:00:10');
    })

    it('LitSliderTest08', ()=>{
        expect(litSliderPanel.renderDefaultSlider()).toBeUndefined();
    })

    it('LitSliderTest9', ()=>{
        expect(litSliderPanel.adoptedCallback()).toBeUndefined();
    })

    it('LitSliderTest10', ()=>{
        litSliderPanel.litSlider.removeEventListener = jest.fn(()=> true)
        litSliderPanel.litSlider.removeEventListener = jest.fn(()=> true)
        litSliderPanel.litSliderButton = jest.fn(()=> true)
        litSliderPanel.litSliderButton.removeEventListener = jest.fn(()=> true)
        expect(litSliderPanel.disconnectedCallback()).toBeUndefined();
    })
})

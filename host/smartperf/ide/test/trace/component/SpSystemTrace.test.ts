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
import {SpSystemTrace} from "../../../dist/trace/component/SpSystemTrace.js"

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe('SpSystemTrace Test', ()=>{
    let spSystemTrace = new SpSystemTrace();

    spSystemTrace.initElements = jest.fn(()=> true)


    it('SpSystemTraceTest01', function () {
        expect(spSystemTrace.getScrollWidth()).toBe(1)
    });

    it('SpSystemTraceTest02', function () {
        let resultLength = spSystemTrace.getVisibleRows([{}]).length;
        expect(resultLength).toBe(0)
    });

    it('SpSystemTraceTest03', function () {
        expect(spSystemTrace.timerShaftELRangeChange('')).toBeUndefined()
    });

    it('SpSystemTraceTest04', function () {
        expect(spSystemTrace.rowsElOnScroll('Scroll')).toBeUndefined()
    });

    it('SpSystemTraceTest05', function () {
        expect(spSystemTrace.documentOnMouseDown('MouseDown')).toBeUndefined()
    });

    it('SpSystemTraceTest06', function () {
        expect(spSystemTrace.documentOnMouseUp('MouseUp')).toBeUndefined()
    });

    it('SpSystemTraceTest07', function () {
        expect(spSystemTrace.documentOnMouseMove('MouseMove')).toBeUndefined()
    });

    it('SpSystemTraceTest08', function () {
        expect(spSystemTrace.hoverStructNull('')).toBeUndefined()
    });

    it('SpSystemTraceTest09', function () {
        expect(spSystemTrace.selectStructNull('')).toBeUndefined()
    });

    it('SpSystemTraceTest10', function () {
        expect(spSystemTrace.documentOnClick('OnClick')).toBeUndefined()
    });

    it('SpSystemTraceTest11', function () {
        expect(spSystemTrace.connectedCallback()).toBeUndefined()
    });

    it('SpSystemTraceTest12', function () {
        expect(spSystemTrace.disconnectedCallback()).toBeUndefined()
    });
})

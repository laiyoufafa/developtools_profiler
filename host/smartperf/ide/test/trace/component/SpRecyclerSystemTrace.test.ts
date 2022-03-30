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

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

// @ts-ignore
import {SpRecyclerSystemTrace} from "../../../dist/trace/component/SpRecyclerSystemTrace.js"

describe('SpRecyclerSystemTrace Test', ()=>{

    let spRecyclerSystemTrace = new SpRecyclerSystemTrace();

    spRecyclerSystemTrace.initElements = jest.fn(()=> true)


    it('SpRecyclerSystemTraceTest01', function () {
        expect(spRecyclerSystemTrace.getScrollWidth()).toBe(1)
    });

    it('SpRecyclerSystemTraceTest02', function () {
        let resultLength = spRecyclerSystemTrace.getVisibleRows([{}]).length;
        expect(resultLength).toBe(0)
    });

    it('SpRecyclerSystemTraceTest03', function () {
        expect(spRecyclerSystemTrace.timerShaftELRangeChange('')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest04', function () {
        expect(spRecyclerSystemTrace.rowsElOnScroll('Scroll')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest05', function () {
        expect(spRecyclerSystemTrace.documentOnMouseDown('MouseDown')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest06', function () {
        expect(spRecyclerSystemTrace.documentOnMouseUp('MouseUp')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest07', function () {
        expect(spRecyclerSystemTrace.documentOnMouseMove('MouseMove')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest08', function () {
        expect(spRecyclerSystemTrace.hoverStructNull('')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest09', function () {
        expect(spRecyclerSystemTrace.selectStructNull('')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest10', function () {
        expect(spRecyclerSystemTrace.documentOnClick('OnClick')).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest11', function () {
        expect(spRecyclerSystemTrace.connectedCallback()).toBeUndefined()
    });

    it('SpRecyclerSystemTraceTest12', function () {
        expect(spRecyclerSystemTrace.disconnectedCallback()).toBeUndefined()
    });
})

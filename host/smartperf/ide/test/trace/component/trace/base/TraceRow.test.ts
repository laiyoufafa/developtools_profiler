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
import {TraceRow} from "../../../../../dist/trace/component/trace/base/TraceRow.js";

describe("TraceRow Test", () => {
    beforeAll(() => {
    })

    it('TraceRow Test01', () => {
        let traceRow = new TraceRow<any>();
        expect(traceRow).not.toBeUndefined();
    });

    it('TraceRow Test02', () => {
        let traceRow = new TraceRow<any>();
        expect(traceRow.sleeping).toBeFalsy();
    });

    it('TraceRow Test03', () => {
        let traceRow = new TraceRow<any>();
        traceRow.sleeping = true
        expect(traceRow.sleeping).toBeTruthy();
    });

    it('TraceRow Test04', () => {
        let traceRow = new TraceRow<any>();
        traceRow.sleeping = false
        expect(traceRow.sleeping).toBeFalsy();
    });

    it('TraceRow Test05', () => {
        let traceRow = new TraceRow<any>();
        expect(traceRow.rangeSelect).toBeFalsy();
    });

    it('TraceRow Test06', () => {
        let traceRow = new TraceRow<any>();
        traceRow.rangeSelect = true
        expect(traceRow.rangeSelect).toBeTruthy();
    });


    it('TraceRow Test07', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        let traceRow = new TraceRow<any>();
        traceRow.dataList = {
            supplier:true,
            isLoading:false,
        }
        traceRow.supplier = true;
        traceRow.isLoading = false;
        traceRow.name = "111"
        traceRow.height = 20
        traceRow.height = 30
        expect(traceRow.initCanvas()).toBeUndefined();
    });



    it('TraceRow Test08', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        let traceRow = new TraceRow<any>();
        traceRow.dataList = {
            supplier:true,
            isLoading:false,
        }
        traceRow.supplier = true;
        traceRow.isLoading = false;
        traceRow.name = "111"
        traceRow.height = 20
        traceRow.height = 30
        expect(traceRow.drawObject()).toBeUndefined();
    });

    it('TraceRow Test08', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        let traceRow = new TraceRow<any>();
        traceRow.dataList = {
            supplier:true,
            isLoading:false,
        }
        traceRow.supplier = true;
        traceRow.isLoading = false;
        traceRow.name = "111"
        traceRow.height = 20
        traceRow.height = 30
        expect(traceRow.drawObject()).toBeUndefined();
    });

    it('TraceRow Test08', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        let traceRow = new TraceRow<any>();
        traceRow.dataList = {
            supplier:true,
            isLoading:false,
        }
        traceRow.supplier = true;
        traceRow.isLoading = false;
        traceRow.name = "111"
        traceRow.height = 20
        traceRow.height = 30
        expect(traceRow.clearCanvas()).toBeUndefined();
    });

    it('TraceRow Test08', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        let traceRow = new TraceRow<any>();
        traceRow.dataList = {
            supplier:true,
            isLoading:false,
        }
        traceRow.supplier = true;
        traceRow.isLoading = false;
        traceRow.name = "111"
        traceRow.height = 20
        traceRow.height = 30
        expect(traceRow.drawLines()).toBeUndefined();
    });

    it('TraceRow Test08', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        let traceRow = new TraceRow<any>();
        traceRow.dataList = {
            supplier:true,
            isLoading:false,
        }
        traceRow.supplier = true;
        traceRow.isLoading = false;
        traceRow.name = "111"
        traceRow.height = 20
        traceRow.height = 30
        expect(traceRow.drawSelection()).toBeUndefined();
    });
})
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
import {TraceSheet} from "../../../../../dist/trace/component/trace/base/TraceSheet.js";

describe("TraceSheet Test", () => {
    beforeAll(() => {
    })

    it('TraceSheet Test01', () => {
        let traceRow = new TraceSheet();
        expect(traceRow).not.toBeUndefined()
    });

    it('TraceSheet Test02', () => {
        let traceRow = new TraceSheet();
        expect(traceRow.recoveryBoxSelection).not.toBeUndefined()
    });



    it('TraceSheet Test03', () => {
        let traceRow = new TraceSheet();
        expect(traceRow.hideBoxTab).not.toBeUndefined()
    });

   /* it('TraceSheet Test04', () => {
        let traceRow = new TraceSheet();
        expect(traceRow.hideOtherBoxTab("11")).not.toBeUndefined()
    });


    it('TraceSheet Test05', () => {
        let traceRow = new TraceSheet();
        expect(traceRow.hideOtherBoxTab("12")).not.toBeUndefined()
    });


    it('TraceSheet Test06', () => {
        let traceRow = new TraceSheet();
        expect(traceRow.hideOtherBoxTab("13")).not.toBeUndefined()
    });

    it('TraceSheet Test07', () => {
        let traceRow = new TraceSheet();
        expect(traceRow.hideOtherBoxTab("14")).not.toBeUndefined()
    });*/
})
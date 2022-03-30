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
import {SelectionParam, SelectionData, Counter, Fps} from "../../../dist/trace/bean/BoxSelection.js"

describe('BoxSelection Test', ()=>{
    let selectionParam = new SelectionParam();
    let selectionData = new SelectionData();
    let counter = new Counter();
    let fps = new Fps();

    it('BoxSelectionTest01', function () {
        expect(selectionParam).not.toBeUndefined()
    });

    it('BoxSelectionTest02', function () {
        expect(selectionData).not.toBeUndefined()
    });

    it('BoxSelectionTest03', function () {
        expect(counter).not.toBeUndefined()
    });

    it('BoxSelectionTest04', function () {
        expect(fps).not.toBeUndefined()
    });
})

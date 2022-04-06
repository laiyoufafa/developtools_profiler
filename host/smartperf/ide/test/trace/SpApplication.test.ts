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
import {SpApplication} from "../../dist/trace/SpApplication.js";

window.ResizeObserver = window.ResizeObserver ||
    jest.fn().mockImplementation(() => ({
        disconnect: jest.fn(),
        observe: jest.fn(),
        unobserve: jest.fn(),
    }));

describe('spApplication Test', ()=>{
    it('spApplicationTest01', function () {
        let spApplication = new SpApplication();
        expect(spApplication).not.toBeUndefined()
    });


    it('spApplicationTest02', function () {
        let spApplication = new SpApplication();
        expect(spApplication.sqlite).toBeFalsy()
    });


    it('spApplicationTest03', function () {
        let spApplication = new SpApplication();
        expect(spApplication.wasm).toBeFalsy()
    });

    it('spApplicationTest04', function () {
        let spApplication = new SpApplication();
        expect(spApplication.server).toBeFalsy()
    });


    it('spApplicationTest05', function () {
        let spApplication = new SpApplication();
        spApplication.server = true;
        expect(spApplication.server).toBeTruthy()
    });


    it('spApplicationTest06', function () {
        let spApplication = new SpApplication();
        spApplication.server = false;
        expect(spApplication.server).toBeFalsy()
    });

    it('spApplicationTest07', function () {
        let spApplication = new SpApplication();
        spApplication.search = false;
        expect(spApplication.search).toBeFalsy()
    });

    it('spApplicationTest08', function () {
        let spApplication = new SpApplication();
        spApplication.search = true;
        expect(spApplication.search).toBeUndefined()
    });
})

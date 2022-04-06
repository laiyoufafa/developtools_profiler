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
import {LitMainMenuItem} from "../../../dist/base-ui/menu/LitMainMenuItem.js";

describe("litMainMenuItem Test", () => {

    it('litMainMenuItem01', () => {
        let litMainMenuItem = new LitMainMenuItem();
        expect(litMainMenuItem).not.toBeUndefined()
        expect(litMainMenuItem).not.toBeNull()
    });

    it('litMainMenuItem02', () => {
        let litMainMenuItem = new LitMainMenuItem();
        expect(litMainMenuItem.title).toEqual("")
    });

    it('litMainMenuItem03', () => {
        let litMainMenuItem = new LitMainMenuItem();
        litMainMenuItem.title ="test"
        expect(litMainMenuItem.title).toEqual("test")
    });


    it('litMainMenuItem04', () => {
        document.body.innerHTML = `<lit-main-menu-item file></lit-main-menu-item>
        `
        let litMainMenuItem = new LitMainMenuItem();
        litMainMenuItem.title ="test02"
        expect(litMainMenuItem.title).toEqual("test02")
    });

    it('litMainMenuItem05', () => {
        document.body.innerHTML = `<lit-main-menu-item></lit-main-menu-item>
        `
        let litMainMenuItem = new LitMainMenuItem();
        litMainMenuItem.title ="test03"
        expect(litMainMenuItem.title).toEqual("test03")
    });
})

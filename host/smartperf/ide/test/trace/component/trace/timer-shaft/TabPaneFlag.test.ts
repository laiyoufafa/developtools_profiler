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
import {TabPaneFlag} from "../../../../../dist/trace/component/trace/timer-shaft/TabPaneFlag.js"

describe('TabPaneFlag Test', ()=>{
    const canvas = document.createElement('canvas');
    canvas.width = 1;
    canvas.height = 1;
    const ctx = canvas.getContext('2d');

    let tabPaneFlag = new TabPaneFlag(canvas , ctx, {
        x: 20,
        y: 20,
        width: 100,
        height: 100
    }, 10000000000);

    it('TabPaneFlagTest01', function () {
        expect(tabPaneFlag.initElements()).toBeUndefined();
    });

    it('TabPaneFlagTest01', function () {
        expect(tabPaneFlag.initHtml()).not.toBe('')
    });

    it('TabPaneFlagTest01', function () {
        expect(tabPaneFlag.setFlagObj({
            x:  0,
            y:  0,
            width:  0,
            height:  0,
            time:  0,
            color:  "",
            selected: false,
            text:  "",
        }, 5)).toBeUndefined();
    });
})

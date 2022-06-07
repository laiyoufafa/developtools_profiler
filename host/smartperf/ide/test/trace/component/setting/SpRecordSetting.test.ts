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
import {SpRecordSetting} from "../../../../dist/trace/component/setting/SpRecordSetting.js";

describe('SpRecordSetting Test', ()=>{
    beforeAll(() => {
        document.body.innerHTML =  `
            <record-setting id = "setting"><sp-allocations>
        `
    })
    it('new SpRecordSetting', function () {
        expect(new SpRecordSetting()).not.toBeNull();
    });

    it(' SpAllocations get Default attrValue', function () {
        let spEle = document.querySelector("#setting") as SpRecordSetting
        expect(spEle.recordMod).toBeTruthy();
        expect(spEle.bufferSize).toEqual(64)
        expect(spEle.maxDur).toEqual(50)
    });

    it(' SpRecordSetting test', function () {
        let spEle = document.querySelector("#setting") as SpRecordSetting
        expect(spEle.initHtml()).toMatchInlineSnapshot(`
"
        <style>
        :host{
            display: block;
            width: 100%;
            height: 100%;
            position: relative;
            background: background: var(--dark-background3,#FFFFFF);
            border-radius: 0px 16px 16px 0px;
        }
        .root {
            display: grid;
            grid-template-columns: repeat(1, 1fr);
            grid-template-rows: min-content min-content min-content;
            grid-gap: 50px;
            padding-top: 45px;
            padding-left: 41px;
            background: var(--dark-background3,#FFFFFF);
            font-size:16px;
            border-radius: 0px 16px 16px 0px;
            overflow-y: auto;
        }
        .record-mode{
            font-family: Helvetica-Bold;
            font-size: 16px;
            color: var(--dark-color1,#000000);
            line-height: 28px;
            font-weight: 700;
            margin-bottom: 16px;
            grid-column: span 2 / auto;
        }
        .record{
            display:flex;
            flex-direction: column;
        }

        .buffer-size{
            height: min-content;
            display: grid;
            grid-template-rows: 1fr 1fr;
            grid-template-columns: 1fr min-content;
        }

        .max-duration{
            height: min-content;
            display: grid;
            grid-template-rows: 1fr 1fr;
            grid-template-columns: 1fr min-content;
        }

        #litradio{
            opacity: 0.9;
            font-family: Helvetica;
            font-size: 14px;
            color: var(--dark-color1,#000000);
            text-align: left;
            line-height: 16px;
            font-weight: 400;
        }

        button{
            height: 25px;
            width: 100%;
            border: 0;
            text-align: left;
            padding-left: 20px;
            margin-top: 10px;
            background-color: #E4E3E9;
        }

        .line{
            border-top: 1px solid #C5C7CF;
            background: #E4E3E9;
            margin-top: 4px;
            display: inline-block;
            width: 100%;
            height: 1px;
            overflow: hidden;
            vertical-align: middle;
        }

        input{
            margin: 0 30px 0 0;
            height: 40px;
            background-color: var(--dark-background5,#F2F2F2);
            -webkit-appearance:none;
            outline:0;
            font-size:14px;
            border-radius:20px;
            border:1px solid var(--dark-border,#c8cccf);
            color:var(--dark-color,#6a6f77);
            text-align: left;
        }

        #memory-buffer, #max-duration {
            margin: 0 8px;
        }

        </style>
        <div class=\\"root\\">
          <div class=\\"record\\">
            <span class=\\"record-mode\\">Record mode</span>
            <lit-radio name=\\"Stop when full\\" dis=\\"round\\" id=\\"litradio\\" checked>Stop when full</lit-radio>
          </div>
          <div class=\\"buffer-size\\">
            <span class=\\"record-mode\\">In-memory buffer size</span>
            <lit-slider id=\\"memory-buffer\\" defaultColor=\\"var(--dark-color3,#46B1E3)\\" open dir=\\"right\\">
            </lit-slider>
            <input class=\\"memory_buffer_result\\" type=\\"text\\" value='          64'>
          </div>
          <div class=\\"max-duration\\">
            <span class=\\"record-mode\\" >Max duration</span>
            <lit-slider id=\\"max-duration\\" defaultColor=\\"var(--dark-color4,#61CFBE)\\" open dir=\\"right\\">
            </lit-slider>
            <input class=\\"max_duration_result\\" type=\\"text\\" value = '     00:00:50'>
          </div>
        </div>
        "
`);
    });
})
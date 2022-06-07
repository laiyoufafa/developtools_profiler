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
import {SpAllocations} from "../../../../dist/trace/component/setting/SpAllocations.js";

describe('SpAllocations Test', ()=>{
    beforeAll(() => {
        document.body.innerHTML =  `
            <sp-allocations id = "sp"><sp-allocations>
        `
    })
    it('new SpAllocations', function () {
        expect(new SpAllocations()).not.toBeUndefined();
    });

    it(' SpAllocations get Default attrValue', function () {
        let spEle = document.querySelector("#sp") as SpAllocations
        expect(spEle.pid).toEqual(undefined)
        expect(spEle.unwind).toEqual(10)
        expect(spEle.shared).toEqual(2048)
        expect(spEle.filter).toEqual(0)
    });

    it(' SpAllocations set  attrValue', function () {
        let spEle = document.querySelector("#sp") as SpAllocations
        spEle.processId.value ="2"
        spEle.unwindEL.value = "111"
        spEle.shareMemory.value = "222"
        spEle.shareMemoryUnit.value = "MB"
        spEle.filterMemory.value = "111"
        spEle.filterMemoryUnit.value = "MB"
        expect(spEle.pid).toEqual(undefined)
        expect(spEle.unwind).toEqual(111)
        expect(spEle.shared).toEqual(0)
        expect(spEle.filter).toEqual(111)

    });

    it(' SpAllocations set  attrValue', function () {
        let spEle = document.querySelector("#sp") as SpAllocations
        spEle.processId.value ="3"
        spEle.unwindEL.value = "1121"
        spEle.shareMemory.value = "222"
        spEle.shareMemoryUnit.value = "KB"
        spEle.filterMemory.value = "111"
        spEle.filterMemoryUnit.value = "KB"
        expect(spEle.pid).toEqual(undefined)
        expect(spEle.unwind).toEqual(1121)
        expect(spEle.shared).toEqual(55)
        expect(spEle.filter).toEqual(111)
    });

    it(' SpAllocations set  attrValue03', function () {
        let spEle = document.querySelector("#sp") as SpAllocations
        spEle.processId.value ="3"
        spEle.unwindEL.value = "1121"
        spEle.shareMemory.value = "222"
        spEle.shareMemoryUnit.value = "G"
        spEle.filterMemory.value = "111"
        spEle.filterMemoryUnit.value = "G"
        expect(spEle.pid).toEqual(undefined)
        expect(spEle.unwind).toEqual(1121)
        expect(spEle.shared).toEqual(0)
        expect(spEle.filter).toEqual(111)
    });

    it('SpAllocations test04', function () {
        let spEle = document.querySelector("#sp") as SpAllocations;
        expect(spEle.initHtml()).toMatchInlineSnapshot(`
"
        <style>
        :host{
            display: block;
            width: 100%;
            height: 100%;
            border-radius: 0px 16px 16px 0px;
        }
        .root {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            grid-template-rows: min-content 1fr min-content;
            padding-top: 45px;
            margin-left: 40px;
            width: 90%;
            border-radius: 0px 16px 16px 0px;
        }
        .title {
            grid-column: span 2 / auto;
        }

        .font-style{
            font-family: Helvetica-Bold;
            font-size: 1em;
            color: var(--dark-color1,#000000);
            line-height: 28px;
            font-weight: 700;
        }
        .inner-font-style {
            font-family: Helvetica,serif;
            font-size: 1em;
            color: var(--dark-color1,#000000);
            text-align: left;
            line-height: 20px;
            font-weight: 400;
        }
        input {
           width: 72%;
           height: 25px;
           border:0;
           outline:none;
           border-radius: 16px;
           text-indent:2%
        }
        input::-webkit-input-placeholder{
            color:var(--bark-prompt,#999999);
        }
        .select {
            height: 30px;
            border:0;
            border-radius: 3px;
            outline:none;
            border: 1px solid var(--dark-border,#B3B3B3);
            width: 60px;
            background-color:var(--dark-background5, #FFFFFF)
            font-family: Helvetica;
            font-size: 14px;
            color:var(--dark-color,#212121)
            text-align: center;
            line-height: 16px;
            font-weight: 400;
            border-radius: 16px;
        }
        .application{
         display: flex;
         flex-direction: column;
         grid-gap: 15px;
         margin-top: 40px;
        }
        .inputstyle{
            background: var(--dark-background5,#FFFFFF);
            border: 1px solid var(--dark-background5,#999999);
            font-family: Helvetica;
            font-size: 14px;
            color: var(--dark-color1,#212121);
            text-align: left;
            line-height: 16px;
            font-weight: 400;
        }
        .inputstyle::-webkit-input-placeholder {
           background: var(--dark-background5,#FFFFFF);
        }
        #one_mb{
            background-color:var(--dark-background5, #FFFFFF)
        }
        #one_kb{
            background-color:var(--dark-background5, #FFFFFF)
        }
        #two_mb{
            background-color:var(--dark-background5, #FFFFFF)
        }
        #two_kb{
            background-color:var(--dark-background5, #FFFFFF)
        }
        </style>
        <div class=\\"root\\">
          <div class = \\"title\\">
            <span class=\\"font-style\\">Allocations</span>
          </div>
          <div class=\\"application\\">
             <span class=\\"inner-font-style\\">ProcessId or ProcessName :</span>
             <input id= \\"pid\\" class=\\"inputstyle\\" type=\\"text\\" placeholder=\\"Enter the pid or ProcessName\\" oninput=\\"if(this.value > 4194304) this.value = ''\\">
          </div>
          <div class=\\"application\\">
            <span class=\\"inner-font-style\\" >Max unwind level :</span>
            <input id= \\"unwind\\"  class=\\"inputstyle\\" type=\\"text\\" placeholder=\\"Enter the Max Unwind Level\\" oninput=\\"if(this.value > 2147483647) this.value = ''\\" onkeyup=\\"this.value=this.value.replace(/\\\\D/g,'')\\" value=\\"10\\">
          </div>
          <div class=\\"application\\">
            <span class=\\"inner-font-style\\">Shared Memory Size (Must be a multiple of 4 KB) :</span>
            <div>
              <input id = \\"shareMemory\\" class=\\"inputstyle\\" type=\\"text\\" placeholder=\\"Enter the Shared Memory Size\\" oninput=\\"if(this.value > 2147483647) this.value = ''\\" onkeyup=\\"this.value=this.value.replace(/\\\\D/g,'')\\" value=\\"8192\\">
              <select class=\\"select\\" id=\\"shareMemoryUnit\\" >
                <option id= \\"one_kb\\" class=\\"select\\" value=\\"KB\\">KB</option>
              </select>
            </div>
          </div>
          <div class=\\"application\\">
            <span class=\\"inner-font-style\\" >Filter Memory Size :</span>
            <div>
                <input id = \\"filterSized\\" class=\\"inputstyle\\" type=\\"text\\" placeholder=\\"Enter the Filter Memory Size\\" oninput=\\"if(this.value > 65535) this.value = ''\\" onkeyup=\\"this.value=this.value.replace(/\\\\\\\\D/g,'')\\" value=\\"0\\">
                <select class=\\"select\\" id=\\"filterSizedUnit\\">
                   <option id= \\"two_kb\\" class=\\"select\\" value=\\"Byte\\">Byte</option>
                 </select>
            </div>
          </div>
        </div>
        "
`);
    });

    it('SpAllocations test05', function () {
        let spAllocations = new SpAllocations();
        expect(spAllocations.appProcess).toBe("")
    });

    it('SpAllocations test06', function () {
        let spAllocations = new SpAllocations();
        expect(spAllocations.convertToValue("","MB")).toBe(0)
    });

    it('SpAllocations test07', function () {
        let spAllocations = new SpAllocations();
        expect(spAllocations.convertToValue("","KB")).toBe(0)
    })
})
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
        expect(spEle.pid).toEqual(-1)
        expect(spEle.unwind).toEqual(10)
        expect(spEle.shared).toEqual(1024)
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
        expect(spEle.pid).toEqual(2)
        expect(spEle.unwind).toEqual(111)
        expect(spEle.shared).toEqual(56832)
        expect(spEle.filter).toEqual(28416)
    });

    it(' SpAllocations set  attrValue', function () {
        let spEle = document.querySelector("#sp") as SpAllocations
        spEle.processId.value ="3"
        spEle.unwindEL.value = "1121"
        spEle.shareMemory.value = "222"
        spEle.shareMemoryUnit.value = "KB"
        spEle.filterMemory.value = "111"
        spEle.filterMemoryUnit.value = "KB"
        expect(spEle.pid).toEqual(3)
        expect(spEle.unwind).toEqual(1121)
        expect(spEle.shared).toEqual(55.5)
        expect(spEle.filter).toEqual(27.75)
    });

    it(' SpAllocations set  attrValue03', function () {
        let spEle = document.querySelector("#sp") as SpAllocations
        spEle.processId.value ="3"
        spEle.unwindEL.value = "1121"
        spEle.shareMemory.value = "222"
        spEle.shareMemoryUnit.value = "G"
        spEle.filterMemory.value = "111"
        spEle.filterMemoryUnit.value = "G"
        expect(spEle.pid).toEqual(3)
        expect(spEle.unwind).toEqual(1121)
        expect(spEle.shared).toEqual(0)
        expect(spEle.filter).toEqual(0)
    });
    // it('CpuStructTest02', function () {
    //     expect(FpsStruct.equals({}, data)).toBeTruthy();
    // });
})
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
import {LitTable} from "../../../dist/base-ui/table/lit-table.js";

describe('LitTable Test', ()=>{
    let litTable = new LitTable();
    litTable.selectable = true
    litTable.selectable = false
    litTable.scrollY = 'scrollY'

    litTable.dataSource = []

    litTable.dataSource = [{
        id: 1,
        name: 'name'
    },{
        id: 2,
        name: 'nameValue'
    }]

    JSON.parse = jest.fn(()=>[['children', 'father'], ['children', 'father']])

    litTable.columns = litTable.columns || jest.fn(()=>true)
    litTable.ds = jest.fn(()=>[{
        id: 1,
        name: 'name'
    },{
        id: 2,
        name: 'nameValue'
    }])

    litTable.tbodyElement = jest.fn(()=> ({
        innerHTML: ''
    }))

    litTable.tableColumns = jest.fn(()=>[])

    litTable.tableColumns.forEach = jest.fn(()=>[])


    it('LitTableTest01', ()=>{
        expect(litTable.adoptedCallback()).toBeUndefined();
    })

    it('LitTableTest02', ()=>{
        litTable.ds.forEach = jest.fn(()=> true)
        expect(litTable.renderTable()).toBeUndefined();
    })

    // it('LitTableTest03', ()=>{
    //     litTable.parentNode = jest.fn(()=> true)
    //     litTable.parentNode.append = jest.fn(()=> true)
    //     expect(litTable.renderTreeTable()).toBeUndefined();
    // })

    it('LitTableTest04', ()=>{
        litTable.switch = document.querySelector("#switch") as HTMLInputElement;
        expect(litTable.connectedCallback()).toBeUndefined()
    })

    it('LitTableTest05', ()=>{
        let rowLength = litTable.getCheckRows().length == 0;
        expect(rowLength).toBeTruthy()
    })

    it('LitTableTest06', ()=>{
        expect(litTable.deleteRowsCondition(()=>{
            return true
        })).toBeUndefined()
    })
})

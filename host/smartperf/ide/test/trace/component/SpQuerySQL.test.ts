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
import {SpQuerySQL} from "../../../dist/trace/component/SpQuerySQL.js"

describe('SpQuerySQL Test', () => {
    let spQuerySQL= new SpQuerySQL();

    it('SpQuerySQLTest01', function () {
        expect(spQuerySQL.checkSupportSqlAbility()).toBeFalsy()
    });

    it('SpQuerySQLTest02', function () {
        expect(spQuerySQL.checkSafetySelectSql()).toBeTruthy()
    });

    it('SpQuerySQLTest03', function () {
        expect(spQuerySQL.getSelectSqlField()).toBe("")
    });

    it('SpQuerySQLTest04', function () {
        expect(spQuerySQL.getSelectSqlTableName()).not.toBeUndefined()
    });

    it('SpQuerySQLTest05', function () {
        expect(spQuerySQL.initDataElement()).toBeUndefined()
    });

    it('SpQuerySQLTest06', function () {
        spQuerySQL.statDataArray.length = 1
        expect(spQuerySQL.initData()).toBeUndefined()
    });

    it('SpQuerySQLTest07', function () {
        expect(spQuerySQL.attributeChangedCallback()).toBeUndefined()
    });

    it('SpQuerySQLTest08', function () {
        expect(spQuerySQL.initHtml()).toMatchInlineSnapshot(`
"
        <style>
        :host{
            width: 100%;
            height: 100%;
            font-size: 16px;
            background-color: var(--dark-background5,#F6F6F6);
            margin: 0;
            padding: 0;
        }

        input{
            box-sizing: border-box;
            width: 95%;
            font-family: Helvetica,serif;
            font-size: 0.875em;
            color: var(--dark-color1,#212121);
            text-align: left;
            line-height: 1em;
            font-weight: 400;
            height: 32px;
            margin-left: 10px;
        }

        .query{
            display: flex;
            flex-direction: column;
            background-color: var(--dark-background5,#F6F6F6);
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
        }

        .query-message{
            background-color: var(--dark-background3,#FFFFFF);
            padding: 1% 2%;
            margin: 2% 2.5% 0 2.5%;
            border-radius: 16px;
            width: 90%;
        }

        .request{
            display: flex;
            flex-direction: column;
        }

        .response{
            flex-grow: 1;
            margin-bottom: 1%;
            display: flex;
            flex-direction: column;
            min-height: inherit;
            max-height: 60vh;

        }

        #dataResult{
            flex-grow: 1;
            overflow-y: auto;
            overflow-x: visible;
            margin-bottom: 1%;
            border-radius: 16px;
        }

        p{
            display: table-cell;
            padding: 7px 10px;
            color: #999999;
            font-size:0.875em;
            line-height: 20px;
            font-weight: 400;
            text-align: left;
        }

        #response-json{
             margin-top: 20px;
             background-color: var(--dark-background5,#F6F6F6);
             margin-left: 10px;
             flex-grow: 1;
             scroll-y: visible;
        }

        .sql-select{
            background-color: var(--dark-background5, #F6F6F6);
            border: 0 solid;
        }

        /*Define the height, width and background of the scroll bar*/
        ::-webkit-scrollbar
        {
          width: 8px;
          background-color: var(--dark-background3,#FFFFFF);
        }

        /*define slider*/
        ::-webkit-scrollbar-thumb
        {
          border-radius: 6px;
          background-color: var(--dark-background7,rgba(0,0,0,0.1));
        }

        </style>
        <div class=\\"query\\">
            <div class=\\"query-message request\\">
                <p class=\\"query_select\\">Enter query and press cmd/ctrl + Enter</p>
                <input class=\\"sql-select\\"/>
            </div>
            <div class=\\"query-message response\\">
                   <p class=\\"query_size\\">Query result - 0 counts</p>
                   <div id=\\"dataResult\\"></div>
            </div>
        </div>
        "
`);
    });

    it('SpQuerySQLTest09', function () {
        expect(spQuerySQL.initDataTableStyle({children:[{length:3,style:{backgroundColor:'var(--dark-background5,#F6F6F6)'}}]})).toBeUndefined()
    });
})
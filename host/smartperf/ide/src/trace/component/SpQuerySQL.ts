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

import {BaseElement, element} from "../../base-ui/BaseElement.js";
import {queryCustomizeSelect, querySelectTraceStats} from "../database/SqlLite.js";
import {LitTable} from "../../base-ui/table/lit-table.js";
import "../../base-ui/table/lit-table.js";
import {LitTableColumn} from "../../base-ui/table/lit-table-column.js";

@element('sp-query-sql')
export class SpQuerySQL extends BaseElement {
    private queryTableEl: LitTable | undefined;
    private queryText: string | undefined;
    private resultText: string | undefined;
    private notSupportList: Array<string> | undefined;
    private querySize: HTMLElement | undefined;
    private keyList: Array<string> | undefined;
    private selector: HTMLInputElement | undefined;
    private isSupportSql: boolean = true;
    private querySelectTables: string = '';
    private response: HTMLDivElement | undefined;
    private statDataArray: any = []
    private querySqlErrorText: string = ''
    private _queryStr?: string;

    static get observedAttributes() {
        return ["queryStr"]
    }

    get queryStr(): string {
        return this.queryStr;
    }

    set queryStr(value: string) {
        this._queryStr = value;
    }

    initElements(): void {
        this.selector = this.shadowRoot?.querySelector('.sql-select') as HTMLInputElement;
        this.queryTableEl = new LitTable()
        this.querySize = this.shadowRoot?.querySelector('.query_size') as HTMLElement;
        this.response = this.shadowRoot?.querySelector('#dataResult') as HTMLDivElement;
        this.notSupportList?.push('insert', 'delete', 'update', 'drop', 'alter', 'truncate');
    }

    selectEventListener = async (event: any) => {
        if (event.ctrlKey && event.keyCode == 13) {
            this.queryTableEl!.innerHTML = ''
            this.queryText = this.selector!.value;
            this.initDataElement();

            this.response!.appendChild(this.queryTableEl!);
            setTimeout(() => {
                this.queryTableEl!.dataSource = this.statDataArray;
                this.initData()
            }, 20)
        }
    }

    initDataTableStyle(styleTable: HTMLDivElement): void {
        for (let index = 0; index < styleTable.children.length; index++) {
            // @ts-ignore
            styleTable.children[index].style.backgroundColor = 'var(--dark-background5,#F6F6F6)'
        }
    }

    async initMetricData(): Promise<any> {
        if (!this.selector || this.selector.value == null) {
            return [];
        }
        if (this.queryText == '' || this.queryText == null) {
            let statList = await querySelectTraceStats();
            for (let index = 0; index < statList.length; index++) {
                const statsResult = statList[index];
                let indexArray = {
                    event_name: statsResult.event_name,
                    start_type: statsResult.stat_type,
                    count: statsResult.count,
                    serverity: statsResult.serverity,
                    source: statsResult.source,
                };
            }
            if (this.querySize) {
                this.querySize!.textContent = 'Query result - ' + statList.length + ' counts:  select * from stat';
            }
            this.resultText = 'Query result - ' + statList.length + ' counts:  select * from stat';
        } else {
            return this.statDataArray
        }
    }

    checkSupportSqlAbility(): boolean {
        let noSupportChart = ['insert', 'delete', 'update', 'drop', 'alter', 'truncate']
        let result = noSupportChart.filter(item => {
            return this.selector!.value.indexOf(item) > -1;
        });
        return result.length > 0;
    }

    checkSafetySelectSql(): boolean {
        let split = this.selector?.value.trim().split(' ');
        // if (!this.selector?.value.match('^\\s*?[sS]+$')) return
        return !(split && split[0] == 'select');

    }

    getSelectSqlField(): string {
        if (this.selector!.value.indexOf('from') < 0) {
            return '';
        }
        let splitSql = this.selector?.value.split('from');
        if (splitSql) {
            if (splitSql[0].indexOf('*') > -1) {
                return '*'
            } else {
                let fields = splitSql[0].split(',')
                return fields[0];
            }
        }
        return '';
    }

    getSelectSqlTableName(str: string): Array<string> {
        if (this.selector!.value.indexOf(str) < 0) {
            return [];
        }
        let tableNameList = [];
        let splitSql = this.selector?.value.split(str);
        if (splitSql) {
            for (let index = 1; index < splitSql?.length; index++) {
                let splitSqlItem = splitSql[index].trim();
                let tableItem = splitSqlItem.split(' ');
                let tableName = tableItem[0].trim();
                tableNameList.push(tableName);
                if (tableName.indexOf('(') >= 0) {
                    tableNameList.pop();
                } else if (tableName.indexOf(')') >= 0) {
                    tableNameList.pop();
                    let unitTableName = tableName.split(')');
                    let tableNewName = unitTableName[0];
                    tableNameList.push(tableNewName);
                }
            }
        }
        return tableNameList
    }

    initDataElement() {
        if (this.keyList) {
            this.keyList.forEach((item) => {
                let htmlElement = document.createElement('lit-table-column') as LitTableColumn;
                htmlElement.setAttribute('title', item);
                htmlElement.setAttribute('data-index', item);
                htmlElement.setAttribute('key', item);
                htmlElement.setAttribute('align', 'flex-start');
                htmlElement.setAttribute('height', '32px');
                this.queryTableEl!.appendChild(htmlElement);
            })
        }
    }

    connectedCallback() {
        let selectQuery = this.shadowRoot?.querySelector('.query_select');
        if (selectQuery) {
            let querySql = selectQuery.textContent;
        }
        // Listen to the sql execution of the query
        this.addEventListener("keydown", this.selectEventListener);
        this.selector!.addEventListener('input', this.inputSqlListener)
        this.selector!.addEventListener('change', this.inputSqlListener)
    }

    inputSqlListener = async (event: any) => {
        let startData = new Date().getTime();
        if (!this.selector || this.selector!.value == null) {
            this.querySqlErrorText = 'Query result - ' + ' 0 counts';
            return;
        }
        if (this.selector!.value.length < 15) {
            this.isSupportSql = false;
            this.querySqlErrorText = 'Query result - ' + ' 0 counts:  ' + this.selector!.value;
            if (this.checkSafetySelectSql() || this.checkSupportSqlAbility()) {
                this.querySqlErrorText = 'Error: Statement contains a change action keyword,The change operation is not supported.';
                return;
            }
            return;
        }
        if (this.checkSafetySelectSql() || this.checkSupportSqlAbility()) {
            this.isSupportSql = false;
            return;
        }
        this.querySelectTables = this.getSelectSqlTableName('from').concat(this.getSelectSqlTableName('join')).toLocaleString();
        this.isSupportSql = true;

        this.getInputSqlResult(this.selector!.value).then(resultList => {
            let dur = new Date().getTime() - startData;
            this.statDataArray = []
            this.keyList = [];
            for (let index = 0; index < resultList.length; index++) {
                const dataResult = resultList[index];
                let keys = Object.keys(dataResult);
                let values = Object.values(dataResult);
                let jsonText = '{';
                for (let keyIndex = 0; keyIndex < keys.length; keyIndex++) {
                    let key = keys[keyIndex];
                    if (this.keyList.indexOf(key) <= -1) {
                        this.keyList.push(key)
                    }
                    let value = values[keyIndex];
                    jsonText += '"' + key + '"' + ': ' + '"' + value + '"';
                    if (keyIndex != keys.length - 1) {
                        jsonText += ','
                    } else {
                        jsonText += '}';
                    }
                }
                this.statDataArray.push(JSON.parse(jsonText))
            }
        })
    }

    async getInputSqlResult(sql: string): Promise<any> {
        return await queryCustomizeSelect(sql);
    }

    disconnectedCallback() {
        this.removeEventListener("keydown", this.selectEventListener);
        this.selector!.removeEventListener('input', this.inputSqlListener)
        this.selector!.removeEventListener('change', this.inputSqlListener)
    }

    initData() {
        if (this.statDataArray.length > 0) {
            this.querySize!.textContent = 'Error: ' + this.selector?.value;
        }
        if (this.isSupportSql) {
            let sqlField = this.keyList?.length == 0 ? '*' : this.keyList?.toLocaleString();
            this.querySize!.textContent = 'Query result - ' + this.statDataArray.length + ' counts:  select ' + sqlField + ' from ' + this.querySelectTables;
        } else {
            this.querySize!.textContent = this.querySqlErrorText;
        }
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
        let queryDataSty: HTMLDivElement | undefined | null = this.queryTableEl?.shadowRoot?.querySelector('div.tbody') as HTMLDivElement
        if (queryDataSty && queryDataSty.hasChildNodes()) {
            for (let index = 0; index < queryDataSty.children.length; index++) {
                // @ts-ignore
                queryDataSty.children[index].style.backgroundColor = 'var(--dark-background5,#F6F6F6)'
            }
        }
    }

    initHtml(): string {
        return `
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
        <div class="query">
            <div class="query-message request">
                <p class="query_select">Enter query and press cmd/ctrl + Enter</p>
                <input class="sql-select"/>
            </div>
            <div class="query-message response">
                   <p class="query_size">Query result - 0 counts</p>
                   <div id="dataResult"></div>
            </div>
        </div>
        `;
    }
}

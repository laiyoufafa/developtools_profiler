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

import { BaseElement, element } from '../../../../../base-ui/BaseElement.js';
import { LitTable } from '../../../../../base-ui/table/lit-table.js';
import {
    SelectionData,
    SelectionParam,
} from '../../../../bean/BoxSelection.js';
import {
    getTabSdkSliceData,
    queryStartTime,
    queryTotalTime,
} from '../../../../database/SqlLite.js';
import '../../../SpFilter.js';
import { LitTableColumn } from '../../../../../base-ui/table/lit-table-column.js';
import { Utils } from '../../base/Utils.js';
import { SpSystemTrace } from '../../../SpSystemTrace.js';

@element('tabpane-sdk-slice')
export class TabPaneSdkSlice extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;
    private keyList: Array<string> | undefined;
    private statDataArray: any = [];
    private columnMap: any = {};
    private sqlMap: Map<number, string> = new Map<number, string>();

    set data(val: SelectionParam | any) {
        this.range!.textContent =
            'Selected range: ' +
            ((val.rightNs - val.leftNs) / 1000000.0).toFixed(5) +
            ' ms';
        this.queryDataByDB(val);
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-sdk-slice');
        this.range = this.shadowRoot?.querySelector('#time-range');
        this.tbl!.addEventListener('column-click', (evt) => {
            // @ts-ignore
            this.sortByColumn(evt.detail);
        });
    }

    connectedCallback() {
        super.connectedCallback();
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 45 + 'px';
                this.tbl?.reMeauseHeight();
            }
        }).observe(this.parentElement!);
    }

    queryDataByDB(val: SelectionParam | any) {
        queryTotalTime().then((res) => {
            let startTime = res[0].recordStartNS;
            let totalTime = res[0].total;
            let componentId: number = -1;
            let slices: Array<string> = [];
            for (let index = 0; index < val.sdkSliceIds.length; index++) {
                let values = val.sdkSliceIds[index].split('-');
                let value = values[0];
                componentId = Number(values[1]);
                slices.push(value);
            }
            this.parseJson(SpSystemTrace.SDK_CONFIG_MAP);
            let sql = this.sqlMap.get(componentId);
            if (sql == undefined) {
                return;
            }
            getTabSdkSliceData(
                sql,
                startTime,
                val.leftNs,
                val.rightNs,
                slices,
                componentId
            ).then((item) => {
                this.keyList = [];
                this.tbl!.innerHTML = '';
                this.statDataArray = [];
                if (item.length != null && item.length > 0) {
                    for (let index = 0; index < item.length; index++) {
                        const dataResult = item[index];
                        let keys = Object.keys(dataResult);
						 // @ts-ignore
                        let values = Object.values(dataResult);
                        let jsonText = '{';
                        for (
                            let keyIndex = 0;
                            keyIndex < keys.length;
                            keyIndex++
                        ) {
                            let key = keys[keyIndex];
                            if (this.keyList.indexOf(key) <= -1) {
                                this.keyList.push(key);
                            }
                            let value = values[keyIndex];
                            if (this.columnMap[key] == 'TimeStamp') {
                                value = Utils.getTimeString(Number(value));
                            } else if (this.columnMap[key] == 'ClockTime') {
                                value = Utils.getTimeStampHMS(Number(value));
                            } else if (this.columnMap[key] == 'RangTime') {
                                value = Utils.getDurString(Number(value));
                            } else if (this.columnMap[key] == 'PercentType') {
                                value = value + '%';
                            } else if (this.columnMap[key] == 'CurrencyType') {
                                // @ts-ignore
                                value = value
                                    .toString()
                                    .replace(/\B(?=(\d{3})+$)/g, ',');
                            } else if (this.columnMap[key] == 'FIXED') {
                                value = value.toFixed(2);
                            }
                            if (typeof value == 'string') {
                                value = value
                                    .replace(/</gi, '&lt;')
                                    .replace(/>/gi, '&gt;');
                            }
                            jsonText +=
                                '"' + key + '"' + ': ' + '"' + value + '"';
                            if (keyIndex != keys.length - 1) {
                                jsonText += ',';
                            } else {
                                jsonText += '}';
                            }
                        }
                        let data = JSON.parse(jsonText);
                        if (
                            data.start_ts != null &&
                            data.end_ts != null &&
                            data.start_ts > data.end_ts &&
                            data.end_ts == 0
                        ) {
                            data.end_ts = totalTime;
                        }
                        if (
                            this.isDateIntersection(
                                val.leftNs,
                                val.rightNs,
                                data.start_ts,
                                data.end_ts
                            )
                        ) {
                            this.statDataArray.push(data);
                        }
                    }
                    this.tbl!.recycleDataSource = this.statDataArray;
                } else {
                    this.tbl!.recycleDataSource = [];
                }
                this.initDataElement();

                setTimeout(() => {
                    this.tbl!.recycleDataSource = this.statDataArray;
                    new ResizeObserver(() => {
                        if (this.parentElement?.clientHeight != 0) {
                            this.tbl!.style.height = '100%';
                            this.tbl!.reMeauseHeight();
                        }
                    }).observe(this.parentElement!);
                }, 200);
            });
        });
    }

    private isDateIntersection(
        selectStartTime: number,
        selectEndTime: number,
        startTime: number,
        endTime: number
    ) {
        if (selectStartTime > startTime && selectStartTime < endTime) {
            return true;
        }
        if (selectEndTime > startTime && selectEndTime < endTime) {
            return true;
        }
        if (selectStartTime < startTime && selectEndTime > endTime) {
            return true;
        }
        return false;
    }

    parseJson(map: Map<number, string>): string {
        let keys = map.keys();
        for (let key of keys) {
            let configObj: any = map.get(key);
            if (configObj != undefined) {
                let configStr = configObj.jsonConfig;
                let json = JSON.parse(configStr);
                let tableConfig = json.tableConfig;
                if (tableConfig != null) {
                    let showTypes = tableConfig.showType;
                    for (let i = 0; i < showTypes.length; i++) {
                        let showType = showTypes[i];
                        let innerTableName = this.getInnerTableName(showType);
                        let type = this.getTableType(showType);
                        if (type == 'slice') {
                            let selectSql = 'select ';
                            for (let j = 0; j < showType.columns.length; j++) {
                                this.columnMap[showType.columns[j].column] =
                                    showType.columns[j].displayName;
                                if (
                                    showType.columns[j].showType.indexOf(3) > -1
                                ) {
                                    switch (showType.columns[j].column) {
                                        case 'slice_id':
                                            selectSql +=
                                                'a.slice_id,b.slice_name,';
                                            break;
                                        case 'start_ts':
                                            selectSql +=
                                                '(a.start_ts - $startTime) as start_ts,';
                                            break;
                                        case 'end_ts':
                                            selectSql +=
                                                '(a.end_ts - $startTime) as end_ts,';
                                            break;
                                        default:
                                            selectSql +=
                                                'a.' +
                                                showType.columns[j].column +
                                                ',';
                                    }
                                }
                            }
                            let sql =
                                selectSql.substring(0, selectSql.length - 1) +
                                ' from ' +
                                showType.tableName +
                                '   as a,' +
                                innerTableName +
                                ' as b' +
                                '  where a.slice_id in ($slices)' +
                                ' and a.slice_id = b.slice_id' +
                                '  and ((a.start_ts - $startTime) >= $leftNs and (a.end_ts - $startTime) <= $rightNs ' +
                                'or (a.start_ts - $startTime) <= $leftNs and $leftNs <= (a.end_ts - $startTime) ' +
                                'or (a.start_ts - $startTime) <= $rightNs and $rightNs <= (a.end_ts - $startTime))';
                            this.sqlMap.set(key, sql);
                        }
                    }
                }
            }
        }
        return '';
    }

    initDataElement() {
        if (this.keyList) {
            this.keyList.forEach((item) => {
                let htmlElement = document.createElement(
                    'lit-table-column'
                ) as LitTableColumn;
                htmlElement.setAttribute('title', item);
                htmlElement.setAttribute('data-index', item);
                htmlElement.setAttribute('key', item);
                htmlElement.setAttribute('align', 'flex-start');
                if (item == 'slice_id') {
                    htmlElement.setAttribute('width', '0.5fr');
                } else {
                    htmlElement.setAttribute('width', '1fr');
                }
                htmlElement.setAttribute('order', '');
                this.tbl!.appendChild(htmlElement);
            });
        }
    }

    initHtml(): string {
        return `
<style>
:host{
    display: flex;
    flex-direction: column;
    padding: 10px 10px;
}
</style>
<div style="display: flex;height: 20px;align-items: center;flex-direction: row;margin-bottom: 5px">
            <stack-bar id="stack-bar" style="flex: 1"></stack-bar>
            <label id="time-range"  style="width: auto;text-align: end;font-size: 10pt;">Selected range:0.0 ms</label>
        </div>
<lit-table id="tb-sdk-slice" style="height: auto">
</lit-table>
        `;
    }

    sortByColumn(detail: any) {
        // @ts-ignore
        function compare(property, sort, type) {
            return function (a: SelectionData, b: SelectionData) {
                if (a.process == ' ' || b.process == ' ') {
                    return 0;
                }
                if (type === 'number') {
                    // @ts-ignore
                    return sort === 2 ? parseFloat(b[property]) - parseFloat(a[property]) : parseFloat(a[property]) - parseFloat(b[property]);
                }
                // @ts-ignore
                if (b[property] > a[property]) {
                    return sort === 2 ? 1 : -1;
                } else {
                    // @ts-ignore
                    if (b[property] == a[property]) {
                        return 0;
                    } else {
                        return sort === 2 ? -1 : 1;
                    }
                }
            };
        }
        if (detail.key.indexOf('name') != -1) {
            this.statDataArray.sort(compare(detail.key, detail.sort, 'string'));
        } else {
            this.statDataArray.sort(compare(detail.key, detail.sort, 'number'));
        }
        this.tbl!.recycleDataSource = this.statDataArray;
    }

    private getInnerTableName(showType: any) {
        let inner = showType.inner;
        if (inner != null) {
            return inner.tableName;
        }
        return '';
    }
    private getTableType(showType: any) {
        let columns = showType.columns;
        for (let i = 0; i < columns.length; i++) {
            let column = columns[i];
            let showType = column.showType;
            if (showType != null) {
                if (showType.indexOf(1) != -1) {
                    return 'counter';
                }
                if (showType.indexOf(2) != -1) {
                    return 'slice';
                }
            }
        }
        return '';
    }
}

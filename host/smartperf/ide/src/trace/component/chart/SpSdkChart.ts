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

import { SpSystemTrace } from '../SpSystemTrace.js';
import { TraceRow } from '../trace/base/TraceRow.js';

import { BaseStruct } from '../../bean/BaseStruct.js';
import {
    queryCounterMax,
    querySdkCount,
    querySdkCounterData,
    querySdkSliceData,
    queryStartTime,
} from '../../database/SqlLite.js';
import {
    CounterStruct,
    SdkCounterRender,
} from '../../database/ui-worker/ProduceWorkerSdkCounter.js';
import { renders } from '../../database/ui-worker/ProcedureWorker.js';
import {
    SdkSliceRender,
    SdkSliceStruct,
} from '../../database/ui-worker/ProduceWorkerSdkSlice.js';
import { EmptyRender } from '../../database/ui-worker/ProcedureWorkerCPU.js';

export class SpSdkChart {
    private trace: SpSystemTrace;
    private pluginName = 'dubai-plugin';

    constructor(trace: SpSystemTrace) {
        this.trace = trace;
    }

    parseJson(startTime: number, map: Map<number, string>) {
        let tablesMap = new Map();
        let keys = map.keys();
        for (let key of keys) {
            let table = [];
            let configObj: any = map.get(key);
            if (configObj != undefined) {
                let configStr = configObj.jsonConfig;
                let json = JSON.parse(configStr);
                let tableConfig = json.tableConfig;
                if (tableConfig != null) {
                    let showTypes = tableConfig.showType;
                    for (let i = 0; i < showTypes.length; i++) {
                        let showType = showTypes[i];
                        let type = this.getTableType(showType);
                        if (type == 'counter') {
                            let chartSql = this.createSql(
                                startTime,
                                showType.tableName,
                                showType.columns,
                                'where counter_id' + ' = $counter_id'
                            );
                            let maxValue = this.createMaxValueSql(
                                showType.tableName,
                                'where counter_id = $counter_id'
                            );
                            let innerTable = showType.inner;
                            let countSql = this.createSql(
                                startTime,
                                innerTable.tableName,
                                innerTable.columns
                            );
                            table.push({
                                countSql: countSql,
                                chartSql: chartSql,
                                maxSql: maxValue,
                                type: 'counter',
                                name: configObj.disPlayName,
                                pluginName: configObj.pluginName,
                            });
                        } else if (type == 'slice') {
                            let chartSql = this.createSliceSql(
                                startTime,
                                showType.tableName,
                                showType.columns,
                                'where' +
                                    ' slice_id = $column_id and (start_ts - ' +
                                    startTime +
                                    ') between $startNS and $endNS;'
                            );
                            let innerTable = showType.inner;
                            let countSql;
                            let countOtherSql = '';
                            if (configObj.pluginName == this.pluginName) {
                                countSql = this.createSql(
                                    startTime,
                                    innerTable.tableName,
                                    innerTable.columns,
                                    'where slice_name like $suffix'
                                );
                                countOtherSql = this.createSql(
                                    startTime,
                                    innerTable.tableName,
                                    innerTable.columns,
                                    '' +
                                        "where slice_name not like '%_cpu' and slice_name not like '%_display' and slice_name not like '%_gpu'" +
                                        "and slice_name not like '%_System_idle' and slice_name not like '%_wifi_data' " +
                                        "and slice_name not like '%_sensor' and slice_name not like '%_audio' "
                                );
                            } else {
                                countSql = this.createSql(
                                    startTime,
                                    innerTable.tableName,
                                    innerTable.columns
                                );
                            }
                            table.push({
                                countSql: countSql,
                                chartSql: chartSql,
                                type: 'slice',
                                name: configObj.disPlayName,
                                pluginName: configObj.pluginName,
                                countOtherSql: countOtherSql,
                            });
                        }
                    }
                    tablesMap.set(key, table);
                }
            }
        }
        return tablesMap;
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

    private createSliceSql(
        startTime: number,
        tableName: string,
        columns: Array<any>,
        where?: string
    ): string {
        let selectSql = 'select ';
        for (let i = 0; i < columns.length; i++) {
            let column = columns[i];
            if (column.column == 'start_ts') {
                column.column = '(start_ts - ' + startTime + ') AS start_ts';
            }
            if (column.column == 'end_ts') {
                column.column = '(end_ts - ' + startTime + ') AS end_ts';
            }
            if (i == columns.length - 1) {
                selectSql = selectSql + column.column + ' ';
            } else {
                selectSql = selectSql + column.column + ', ';
            }
        }
        selectSql = selectSql + 'from ' + tableName;
        if (where != undefined) {
            selectSql = selectSql + ' ' + where;
        }
        return selectSql;
    }

    private createMaxValueSql(tableName: string, where?: string): string {
        let selectSql = 'select max(value) as max_value from ' + tableName;
        if (where != undefined) {
            selectSql = selectSql + ' ' + where;
        }
        return selectSql;
    }

    private createSql(
        startTime: number,
        tableName: string,
        columns: Array<any>,
        where?: string
    ): string {
        let selectSql = 'select ';
        for (let i = 0; i < columns.length; i++) {
            let column = columns[i];
            if (column.column == 'ts') {
                column.column = 'ts - ' + startTime + ' AS ts';
            }
            if (i == columns.length - 1) {
                selectSql = selectSql + column.column + ' ';
            } else {
                selectSql = selectSql + column.column + ', ';
            }
        }
        selectSql = selectSql + 'from ' + tableName;
        if (where != undefined) {
            selectSql = selectSql + ' ' + where;
        }
        return selectSql;
    }

    async init() {
        let configMap = SpSystemTrace.SDK_CONFIG_MAP;
        if (configMap == undefined) return;
        let res = await queryStartTime();
        let startTime = res[0].start_ts;
        let tablesMap = this.parseJson(startTime, configMap);
        let tableKeys = tablesMap.keys();
        for (let componentId of tableKeys) {
            let table = tablesMap.get(componentId);
            if (table != null) {
                let nodeRow = this.initNodeRow(componentId, table[0].name);
                for (let index = 0; index < table.length; index++) {
                    let sqlMap = table[index];
                    if (sqlMap.type == 'counter') {
                        let result = await querySdkCount(
                            sqlMap.countSql,
                            componentId
                        );
                        for (let i = 0; i < result.length; i++) {
                            await this.initCounter(
                                nodeRow,
                                i,
                                result[i],
                                sqlMap,
                                componentId
                            );
                        }
                    } else if (
                        sqlMap.type == 'slice' &&
                        sqlMap.pluginName == this.pluginName
                    ) {
                        let suffixList = [
                            'cpu',
                            'display',
                            'gpu',
                            'System_idle',
                            'wifi_data',
                            'sensor',
                            'audio',
                        ];
                        for (let i = 0; i < suffixList.length; i++) {
                            let result = await querySdkCount(
                                sqlMap.countSql,
                                componentId,
                                { $suffix: '%' + suffixList[i] }
                            );
                            if (result.length > 0) {
                                let groupNodeRow = await this.initSecondaryRow(
                                    nodeRow,
                                    i,
                                    suffixList[i]
                                );
                                for (let i = 0; i < result.length; i++) {
                                    await this.initSlice(
                                        groupNodeRow,
                                        i,
                                        result[i],
                                        sqlMap,
                                        componentId
                                    );
                                }
                            }
                        }
                        let result = await querySdkCount(
                            sqlMap.countOtherSql,
                            componentId
                        );
                        if (result.length > 0) {
                            let groupNodeRow = await this.initSecondaryRow(
                                nodeRow,
                                7,
                                'other'
                            );
                            for (let i = 0; i < result.length; i++) {
                                await this.initSlice(
                                    groupNodeRow,
                                    i,
                                    result[i],
                                    sqlMap,
                                    componentId
                                );
                            }
                        }
                    } else if (sqlMap.type == 'slice') {
                        let result = await querySdkCount(
                            sqlMap.countSql,
                            componentId,
                            {}
                        );
                        for (let i = 0; i < result.length; i++) {
                            await this.initSlice(
                                nodeRow,
                                i,
                                result[i],
                                sqlMap,
                                componentId
                            );
                        }
                    }
                }
            }
        }
    }

    private initCounter = async (
        nodeRow: TraceRow<BaseStruct>,
        index: number,
        result: any,
        sqlMap: any,
        componentId: number
    ) => {
        let traceRow = TraceRow.skeleton<CounterStruct>();
        traceRow.rowParentId = `Sdk-${componentId}`;
        traceRow.rowHidden = !nodeRow.expansion;
        traceRow.rowId = result.counter_id + '-' + componentId;
        traceRow.rowType = TraceRow.ROW_TYPE_SDK_COUNTER;
        traceRow.folderPaddingLeft = 30;
        traceRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        traceRow.selectChangeHandler = this.trace.selectChangeHandler;
        traceRow.style.height = '40px';
        traceRow.style.width = `100%`;
        traceRow.setAttribute('children', '');
        traceRow.name = `${result.counter_name}`;
        traceRow.supplier = () =>
            querySdkCounterData(
                sqlMap.chartSql,
                result.counter_id,
                componentId
            );
        traceRow.focusHandler = () => {
            this.trace?.displayTip(
                traceRow,
                CounterStruct.hoverCounterStruct,
                `<span>${CounterStruct.hoverCounterStruct?.value?.toFixed(
                    2
                )}</span>`
            );
        };
        let maxList = await queryCounterMax(
            sqlMap.maxSql,
            result.counter_id,
            componentId
        );
        let maxCounter = maxList[0].max_value;
        traceRow.onThreadHandler = (useCache) => {
            let context = traceRow.collect
                ? this.trace.canvasFavoritePanelCtx!
                : this.trace.canvasPanelCtx!;
            traceRow.canvasSave(context);
            (
                renders[TraceRow.ROW_TYPE_SDK_COUNTER] as SdkCounterRender
            ).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `sdk-counter-${index}`,
                    maxName: `${maxCounter}`,
                    maxValue: maxCounter,
                },
                traceRow
            );
            traceRow.canvasRestore(context);
        };
        this.trace.rowsEL?.appendChild(traceRow);
    };

    private initNodeRow = (index: number, name: string) => {
        let folder = TraceRow.skeleton();
        folder.rowId = `Sdk-${index}`;
        folder.index = index;
        folder.rowType = TraceRow.ROW_TYPE_SDK;
        folder.rowParentId = '';
        folder.style.height = '40px';
        folder.folder = true;
        folder.name = `${name}`;
        folder.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        folder.selectChangeHandler = this.trace.selectChangeHandler;
        folder.supplier = () =>
            new Promise<Array<any>>((resolve) => resolve([]));
        folder.onThreadHandler = (useCache) => {
            folder.canvasSave(this.trace.canvasPanelCtx!);
            if (folder.expansion) {
                this.trace.canvasPanelCtx?.clearRect(
                    0,
                    0,
                    folder.frame.width,
                    folder.frame.height
                );
            } else {
                (renders['empty'] as EmptyRender).renderMainThread(
                    {
                        context: this.trace.canvasPanelCtx,
                        useCache: useCache,
                        type: ``,
                    },
                    folder
                );
            }
            folder.canvasRestore(this.trace.canvasPanelCtx!);
        };
        this.trace.rowsEL?.appendChild(folder);
        return folder;
    };

    private initSecondaryRow = async (
        nodeRow: TraceRow<BaseStruct>,
        index: number,
        name: string
    ) => {
        let folder = TraceRow.skeleton();
        folder.rowId = `Sdk-${name}-${index}`;
        folder.index = index;
        folder.rowType = TraceRow.ROW_TYPE_SDK;
        folder.rowParentId = nodeRow.rowId;
        folder.rowHidden = !nodeRow.expansion;
        folder.style.height = '40px';
        folder.folder = true;
        folder.folderPaddingLeft = 30;
        folder.name = `${name}`;
        folder.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        folder.selectChangeHandler = this.trace.selectChangeHandler;
        folder.supplier = () =>
            new Promise<Array<any>>((resolve) => resolve([]));
        folder.onThreadHandler = (useCache) => {
            folder.canvasSave(this.trace.canvasPanelCtx!);
            if (folder.expansion) {
                this.trace.canvasPanelCtx?.clearRect(
                    0,
                    0,
                    folder.frame.width,
                    folder.frame.height
                );
            } else {
                (renders['empty'] as EmptyRender).renderMainThread(
                    {
                        context: this.trace.canvasPanelCtx,
                        useCache: useCache,
                        type: ``,
                    },
                    folder
                );
            }
            folder.canvasRestore(this.trace.canvasPanelCtx!);
        };
        this.trace.rowsEL?.appendChild(folder);
        return folder;
    };

    private initSlice = async (
        nodeRow: TraceRow<BaseStruct>,
        index: number,
        result: any,
        sqlMap: any,
        componentId: number
    ) => {
        let traceRow = TraceRow.skeleton<SdkSliceStruct>();
        traceRow.rowType = TraceRow.ROW_TYPE_SDK_SLICE;
        traceRow.rowHidden = !nodeRow.expansion;
        traceRow.rowParentId = nodeRow.rowId;
        traceRow.folderPaddingLeft = 30;
        traceRow.style.height = '40px';
        traceRow.style.width = `100%`;
        traceRow.name = `${result.slice_name}`;
        traceRow.setAttribute('children', '');
        traceRow.favoriteChangeHandler = this.trace.favoriteChangeHandler;
        traceRow.selectChangeHandler = this.trace.selectChangeHandler;
        traceRow.rowId = result.slice_id + '-' + componentId;
        traceRow.supplier = () =>
            querySdkSliceData(
                sqlMap.chartSql,
                result.slice_id,
                TraceRow.range?.startNS || 0,
                TraceRow.range?.endNS || 0,
                componentId
            );
        traceRow.focusHandler = () => {
            this.trace?.displayTip(
                traceRow,
                SdkSliceStruct.hoverSdkSliceStruct,
                `<span>${SdkSliceStruct.hoverSdkSliceStruct?.value}</span>`
            );
        };
        traceRow.onThreadHandler = (useCache: boolean) => {
            let context = traceRow.collect
                ? this.trace.canvasFavoritePanelCtx!
                : this.trace.canvasPanelCtx!;
            traceRow.canvasSave(context);
            (
                renders[TraceRow.ROW_TYPE_SDK_SLICE] as SdkSliceRender
            ).renderMainThread(
                {
                    context: context,
                    useCache: useCache,
                    type: `sdk-slice-${index}`,
                    maxName: '',
                    maxValue: 0,
                },
                traceRow
            );
            traceRow.canvasRestore(context);
        };
        this.trace.rowsEL?.appendChild(traceRow);
    };
}

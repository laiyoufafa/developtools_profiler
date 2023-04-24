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

import { BaseElement, element } from '../../../base-ui/BaseElement.js';
import { LitTable } from '../../../base-ui/table/lit-table.js';
import { SpSchedulingAnalysis } from './SpSchedulingAnalysis.js';
import { procedurePool } from '../../database/Procedure.js';
import { info } from '../../../log/Log.js';
import '../../../base-ui/progress-bar/LitProgressBar.js';
import { LitProgressBar } from '../../../base-ui/progress-bar/LitProgressBar.js';
import './TableNoData.js';
import { TableNoData } from './TableNoData.js';

@element('top20-thread-run-time')
export class Top20ThreadRunTime extends BaseElement {
    traceChange: boolean = false;
    private table: LitTable | null | undefined;
    private progress: LitProgressBar | null | undefined;
    private nodata: TableNoData | null | undefined;
    private data: Array<any> = [];

    initElements(): void {
        this.progress =
            this.shadowRoot!.querySelector<LitProgressBar>('#loading');
        this.table = this.shadowRoot!.querySelector<LitTable>(
            '#tb-thread-run-time'
        );
        this.nodata = this.shadowRoot!.querySelector<TableNoData>('#nodata');

        this.table!.addEventListener('row-click', (evt: any) => {
            let data = evt.detail.data;
            data.isSelected = true;
            // @ts-ignore
            if ((evt.detail as any).callBack) {
                // @ts-ignore
                (evt.detail as any).callBack(true);
            }
        });

        this.table!.addEventListener('column-click', (evt) => {
            // @ts-ignore
            this.sortByColumn(evt.detail);
        });
    }

    init() {
        if (!this.traceChange) {
            if (this.table!.recycleDataSource.length > 0) {
                this.table?.reMeauseHeight();
            }
            return;
        }
        this.progress!.loading = true;
        this.traceChange = false;
        this.queryLogicWorker(
            `scheduling-Thread RunTime`,
            `query Thread Cpu Run Time Analysis Time:`,
            (res) => {
                this.nodata!.noData = res === undefined || res.length === 0;
                this.table!.recycleDataSource = res;
                this.table?.reMeauseHeight();
                this.progress!.loading = false;
                this.data = res;
            }
        );
    }

    clearData() {
        this.traceChange = true;
        this.table!.recycleDataSource = [];
    }

    queryLogicWorker(option: string, log: string, handler: (res: any) => void) {
        let time = new Date().getTime();
        procedurePool.submitWithName(
            'logic1',
            option,
            { cpuMax: SpSchedulingAnalysis.cpuCount - 1 },
            undefined,
            handler
        );
        let durTime = new Date().getTime() - time;
        info(log, durTime);
    }

    sortByColumn(detail: any) {
        // @ts-ignore
        function compare(property, sort, type) {
            return function (a: any, b: any) {
                if (type === 'number') {
                    // @ts-ignore
                    return sort === 2
                        ? parseFloat(b[property]) - parseFloat(a[property])
                        : parseFloat(a[property]) - parseFloat(b[property]);
                } else {
                    if (sort === 2) {
                        return b[property]
                            .toString()
                            .localeCompare(a[property].toString());
                    } else {
                        return a[property]
                            .toString()
                            .localeCompare(b[property].toString());
                    }
                }
            };
        }

        if (detail.key === 'maxDurationStr') {
            detail.key = 'maxDuration';
            this.data.sort(compare(detail.key, detail.sort, 'number'));
        } else if (
            detail.key === 'cpu' ||
            detail.key === 'no' ||
            detail.key === 'pid' ||
            detail.key === 'tid' ||
            detail.key === 'timestamp'
        ) {
            this.data.sort(compare(detail.key, detail.sort, 'number'));
        } else {
            this.data.sort(compare(detail.key, detail.sort, 'string'));
        }
        this.table!.recycleDataSource = this.data;
    }

    initHtml(): string {
        return `
        <style>
        :host {
            width: 100%;
            height: 100%;
            background-color: var(--dark-background5,#F6F6F6);
        }
        .tb_run_time{
            overflow: auto ;
            border-radius: 5px;
            border: solid 1px var(--dark-border1,#e0e0e0);
            margin: 10px 10px 0 10px;
            padding: 5px 15px
        }
        </style>
        <lit-progress-bar id="loading" style="height: 1px;width: 100%" loading></lit-progress-bar>
        <div style="height: 5px"></div>
        <div class="tb_run_time" >
            <table-no-data id="nodata" contentHeight="500px">
                <lit-table id="tb-thread-run-time" style="height: auto;" hideDownload>
                    <lit-table-column width="90px" title="NO" data-index="no" key="no" align="flex-start" order></lit-table-column>
                    <lit-table-column width="140px" title="tid" data-index="tid" key="tid" align="flex-start" order></lit-table-column>
                    <lit-table-column width="240px" title="t_name" data-index="tName" key="tName" align="flex-start" order></lit-table-column>
                    <lit-table-column width="140px" title="pid" data-index="pid" key="pid" align="flex-start" order></lit-table-column>
                    <lit-table-column width="240px" title="p_name" data-index="pName" key="pName" align="flex-start" order></lit-table-column>
                    <lit-table-column width="140px" title="max duration" data-index="maxDurationStr" key="maxDurationStr" align="flex-start" order></lit-table-column>
                    <lit-table-column width="200px" title="timestamp" data-index="timestamp" key="timestamp" align="flex-start" order>
                        <template>
                            <div onclick="{
                                window.publish(window.SmartEvent.UI.SliceMark,this.parentElement.parentElement.data)
                            }">{{timestamp}}</div>
                        </template>
                    </lit-table-column>
                    <lit-table-column width="140px" title="cpu" data-index="cpu" key="cpu" align="flex-start" order></lit-table-column>
                </lit-table>
            </table-no-data>
        </div>
        <div style="height: 10px"></div>
        `;
    }
}

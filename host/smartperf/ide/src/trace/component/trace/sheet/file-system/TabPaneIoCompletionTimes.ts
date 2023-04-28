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
import { SelectionParam } from '../../../../bean/BoxSelection.js';
import '../../../../../base-ui/slicer/lit-slicer.js';
import { LitProgressBar } from '../../../../../base-ui/progress-bar/LitProgressBar.js';
import { procedurePool } from '../../../../database/Procedure.js';
import {
    DISKIO_TYPE_MAP,
    FileSysEvent,
    IoCompletionTimes,
    VirtualMemoryEvent,
    VM_TYPE_MAP,
} from '../../../../database/logic-worker/ProcedureLogicWorkerFileSystem.js';
import { FilterData, TabPaneFilter } from '../TabPaneFilter.js';
import {
    getTabIoCompletionTimesType,
    getTabVirtualMemoryType,
} from '../../../../database/SqlLite.js';

@element('tabpane-io-completiontimes')
export class TabPaneIoCompletionTimes extends BaseElement {
	 // @ts-ignore
    private defaultNativeTypes = ['All', ...Object.values(VM_TYPE_MAP)];
    private native_type: Array<string> = [...this.defaultNativeTypes];
    private tbl: LitTable | null | undefined;
    private tblData: LitTable | null | undefined;
    private progressEL: LitProgressBar | null | undefined;
    private loadingList: number[] = [];
    private loadingPage: any;
    private sortKey: string = 'startTs';
    private sortType: number = 0;
    private currentSelection: SelectionParam | undefined | null;
    private source: Array<IoCompletionTimes> = [];
    private queryDataSource: Array<IoCompletionTimes> = [];
    private statsticsSelection: Array<any> = [];

    set data(val: SelectionParam | null | undefined) {
        if (val == this.currentSelection) {
            return;
        }
        this.currentSelection = val;
        this.initFilterTypes(val!).then(() => {
            this.queryData(val!);
        });
        // @ts-ignore
        this.tbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 20 - 31 + 'px';
        // @ts-ignore
        this.tblData?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 20 - 31 + 'px';
        this.tbl!.recycleDataSource = [];
        this.tblData!.recycleDataSource = [];
    }

    connectedCallback() {
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 10 - 33 + 'px';
                this.tbl?.reMeauseHeight();
                // @ts-ignore
                this.tblData?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 10 - 33 + 'px';
                this.tblData?.reMeauseHeight();
                this.loadingPage.style.height =
                    this.parentElement!.clientHeight - 24 + 'px';
            }
        }).observe(this.parentElement!);
    }

    initElements(): void {
        this.loadingPage = this.shadowRoot?.querySelector('.loading');
        this.progressEL = this.shadowRoot?.querySelector(
            '.progress'
        ) as LitProgressBar;
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tbl');
        this.tblData = this.shadowRoot?.querySelector<LitTable>('#tbr');
        this.tbl!.addEventListener('row-click', (e) => {
            // @ts-ignore
            let data = e.detail.data as FileSysEvent;
            procedurePool.submitWithName(
                'logic0',
                'fileSystem-queryStack',
                { callchainId: data.callchainId },
                undefined,
                (res: any) => {
                    this.tblData!.recycleDataSource = res;
                }
            );
        });
        this.tbl!.addEventListener('column-click', (evt) => {
            // @ts-ignore
            this.sortKey = evt.detail.key;
            // @ts-ignore
            this.sortType = evt.detail.sort;
            // @ts-ignore
            this.sortTable(evt.detail.key, evt.detail.sort);
        });
        this.shadowRoot
            ?.querySelector<TabPaneFilter>('#filter')!
            .getFilterData((data: FilterData) => {
                let index = parseInt(data.firstSelect || '0');
                if (index > this.defaultNativeTypes.length - 1) {
                    this.filterTypeData(
                        this.statsticsSelection[
                            index - this.defaultNativeTypes.length
                        ]
                    );
                } else {
                    this.filterTypeData(undefined);
                }
                this.tbl!.recycleDataSource = this.source;
            });
    }

    async initFilterTypes(val: SelectionParam) {
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter');
        let typeKeys = await getTabIoCompletionTimesType(
            val.leftNs,
            val.rightNs
        );
        this.defaultNativeTypes = ['All'];
        this.statsticsSelection = [];
        typeKeys.forEach((item) => {
            // @ts-ignore
            this.defaultNativeTypes.push(item.tier + '');
        });
        this.native_type = [...this.defaultNativeTypes];
        filter!.setSelectList([...this.defaultNativeTypes], null, 'Tier');
        filter!.firstSelect = '0';
    }

    async fromStastics(val: SelectionParam | any) {
        if (val.fileSystemIoData == undefined) {
            return;
        }
        this.tblData!.recycleDataSource = [];
        this.tblData?.clearAllSelection(undefined);
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter');
        if (this.currentSelection != val) {
            await this.initFilterTypes(val);
        }
        let typeIndexOf = this.native_type.indexOf(
            val.fileSystemIoData.path.value
        );
        if (typeIndexOf == -1) {
            this.statsticsSelection.push(val.fileSystemIoData.path);
            this.native_type.push(val.fileSystemIoData.path.value);
            typeIndexOf = this.native_type.length - 1;
        }
        if (this.currentSelection != val) {
            this.currentSelection = val;
            filter!.setSelectList(this.native_type, null, 'Tier');
            filter!.firstSelect = typeIndexOf + '';
            this.queryData(val);
        } else {
            if (typeIndexOf == parseInt(filter!.firstSelect)) {
                return;
            }
            filter!.setSelectList(this.native_type, null, 'Tier');
            filter!.firstSelect = typeIndexOf + '';
            this.filterTypeData(val?.fileSystemIoData?.path || undefined);
            val.fileSystemIoData = undefined;
            this.tbl!.recycleDataSource = this.source;
        }
    }

    queryData(val: SelectionParam) {
        this.loadingList.push(1);
        this.progressEL!.loading = true;
        this.loadingPage.style.visibility = 'visible';
        this.source = [];
        this.queryDataSource = [];
        procedurePool.submitWithName(
            'logic0',
            'fileSystem-queryIOEvents',
            {
                leftNs: val.leftNs,
                rightNs: val.rightNs,
                diskIOipids: val.diskIOipids,
            },
            undefined,
            (res: any) => {
                this.source = this.source.concat(res.data);
                this.queryDataSource = this.queryDataSource.concat(res.data);
                this.filterTypeData(val?.fileSystemIoData?.path || undefined);
                val.fileSystemIoData = undefined;
                res.data = null;
                if (!res.isSending) {
                    this.tbl!.recycleDataSource = this.source;
                    this.loadingList.splice(0, 1);
                    if (this.loadingList.length == 0) {
                        this.progressEL!.loading = false;
                        this.loadingPage.style.visibility = 'hidden';
                    }
                }
            }
        );
    }

    filterTypeData(pathData: any) {
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter');
        let firstSelect = filter!.firstSelect;
        let tier = -1;
        let path = '';
        let pid = -1;
        if (parseInt(firstSelect) <= this.defaultNativeTypes.length - 1) {
            let index = parseInt(firstSelect);
            tier = index == 0 ? -1 : parseInt(this.defaultNativeTypes[index]);
        } else if (pathData != undefined) {
            tier = parseInt(pathData.tier);
            path = pathData.path || '';
            pid = pathData.pid || -1;
        } else if (pathData == undefined) {
            return;
        }
        let isTierFilter = false;
        let isPidFilter = false;
        let isPathFilter = false;
        this.source = this.queryDataSource.filter((item) => {
            if (tier == -1) {
                isTierFilter = true;
            } else {
                isTierFilter = item.tier == tier;
            }
            if (pid == -1) {
                isPidFilter = true;
            } else {
                isPidFilter = item.pid == pid;
            }
            isPathFilter = path == '' || item.path == path;
            return isTierFilter && isPidFilter && isPathFilter;
        });
    }

    sortTable(key: string, type: number) {
        if (type == 0) {
            this.tbl!.recycleDataSource = this.source;
        } else {
            let arr = Array.from(this.source);
            arr.sort((a, b): number => {
                if (key == 'startTsStr') {
                    if (type == 1) {
                        return a.startTs - b.startTs;
                    } else {
                        return b.startTs - a.startTs;
                    }
                } else if (key == 'durStr') {
                    if (type == 1) {
                        return a.dur - b.dur;
                    } else {
                        return b.dur - a.dur;
                    }
                } else if (key == 'process') {
                    if (a.process > b.process) {
                        return type === 2 ? 1 : -1;
                    } else if (a.process == b.process) {
                        return 0;
                    } else {
                        return type === 2 ? -1 : 1;
                    }
                } else if (key == 'durPer4kStr') {
                    if (type == 1) {
                        return a.durPer4k - b.durPer4k;
                    } else {
                        return b.durPer4k - a.durPer4k;
                    }
                } else if (key == 'thread') {
                    if (a.thread > b.thread) {
                        return type === 2 ? 1 : -1;
                    } else if (a.thread == b.thread) {
                        return 0;
                    } else {
                        return type === 2 ? -1 : 1;
                    }
                } else if (key == 'operation') {
                    if (a.operation > b.operation) {
                        return type === 2 ? 1 : -1;
                    } else if (a.operation == b.operation) {
                        return 0;
                    } else {
                        return type === 2 ? -1 : 1;
                    }
                } else if (key == 'sizeStr') {
                    if (type == 1) {
                        return a.size - b.size;
                    } else {
                        return b.size - a.size;
                    }
                } else if (key == 'tier') {
                    if (type == 1) {
                        return a.tier - b.tier;
                    } else {
                        return b.tier - a.tier;
                    }
                } else {
                    return 0;
                }
            });
            this.tbl!.recycleDataSource = arr;
        }
    }

    initHtml(): string {
        return `
    <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px 0 10px;
        }
        .loading{
            bottom: 0;
            position: absolute;
            left: 0;
            right: 0;
            width:100%;
            background:transparent;
            z-index: 999999;
        }
        .progress{
            bottom: 33px;
            position: absolute;
            height: 1px;
            z-index: 99;
            left: 0;
            right: 0;
        }
        tab-pane-filter {
            border: solid rgb(216,216,216) 1px;
            float: left;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
        </style>
        <div style="display: flex;flex-direction: column">
            <div style="display: flex;flex-direction: row;">
                <lit-slicer style="width:100%">
                    <div style="width: 65%">
                        <lit-table id="tbl" style="height: auto">
                            <lit-table-column width="200px" title="Start" data-index="startTsStr" key="startTsStr" align="flex-start" order></lit-table-column>
                            <lit-table-column width="260px" title="Total Latency" data-index="durStr" key="durStr" align="flex-start" order></lit-table-column>
                            <lit-table-column width="200px" title="Process" data-index="process" key="process" align="flex-start" order></lit-table-column>
                            <lit-table-column width="200px" title="Latency per 4KB" data-index="durPer4kStr" key="durPer4kStr" align="flex-start" order></lit-table-column>
                            <lit-table-column width="200px" title="Thread" data-index="thread" key="thread" align="flex-start" order></lit-table-column>
                            <lit-table-column width="200px" title="Operation" data-index="operation" key="operation" align="flex-start" order></lit-table-column>
                            <lit-table-column width="200px" title="Bytes" data-index="sizeStr" key="sizeStr" align="flex-start" order></lit-table-column>
                            <lit-table-column width="280px" title="Path" data-index="path" key="path" align="flex-start" ></lit-table-column>
                            <lit-table-column width="200px" title="Block number" data-index="blockNumber" key="blockNumber" align="flex-start" ></lit-table-column>
                            <lit-table-column width="240px" title="Tier" data-index="tier" key="tier" align="flex-start" order></lit-table-column>
                            <lit-table-column width="600px" title="Backtrace" data-index="backtrace" key="backtrace" align="flex-start" >
                                <template>
                                    <div>
                                        <span>{{backtrace[0]}}</span>
                                        <span v-if="backtrace.length > 1">⬅</span>
                                        <span v-if="backtrace.length > 1"style="color: #565656"> {{backtrace[1]}}</span>
                                    </div>
                                </template>
                            </lit-table-column>
                        </lit-table>
                    </div>
                    <lit-slicer-track ></lit-slicer-track>
                    <lit-table id="tbr" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)" hideDownload>
                        <lit-table-column width="60px" title="" data-index="type" key="type"  align="flex-start" >
                            <template>
                                <div v-if=" type == -1 ">Thread:</div>
                                <img src="img/library.png" size="20" v-if=" type == 1 ">
                                <img src="img/function.png" size="20" v-if=" type == 0 ">
                            </template>
                        </lit-table-column>
                        <lit-table-column width="1fr" title="" data-index="symbol" key="symbol"  align="flex-start">
                        </lit-table-column>
                    </lit-table>
                </lit-slicer>
            </div>
            <lit-progress-bar class="progress"></lit-progress-bar>
            <tab-pane-filter id="filter" first></tab-pane-filter>
            <div class="loading"></div>
        </div>
`;
    }
}

// @ts-nocheck
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

import {BaseElement, element} from "../../../../base-ui/BaseElement.js";
import {LitTable} from "../../../../base-ui/table/lit-table.js";
import {BoxJumpParam} from "../../../bean/BoxSelection.js";
import {getTabBoxChildData} from "../../../database/SqlLite.js";
import {Utils} from "../base/Utils.js";

@element('tabpane-box-child')
export class TabPaneBoxChild extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;
    private source: Array<SelectionData> = []

    set data(val: BoxJumpParam) {
        this.range!.textContent = "Selected range: " + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + " ms"
        getTabBoxChildData(val.leftNs, val.rightNs, val.state, val.processId, val.threadId).then((result) => {
            if (result.length != null && result.length > 0) {
                result.map((e) => {
                    e.startTime = Utils.getTimeString(e.startNs)
                    e.state = Utils.getEndState(e.state)
                    e.prior = e.priority == undefined || e.priority == null ? "-" : e.priority + ""
                    e.core = e.cpu == undefined || e.cpu == null ? "-" : "CPU" + e.cpu
                    e.processName = (e.process == undefined || e.process == null ? "process" : e.process) + "(" + e.processId + ")"
                    e.threadName = (e.thread == undefined || e.thread == null ? "process" : e.thread) + "(" + e.threadId + ")"
                })
                this.source = result;
                this.tbl?.dataSource = result;
            } else {
                this.source = [];
                this.tbl?.dataSource = []
            }
        })
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-cpu-thread');
        this.range = this.shadowRoot?.querySelector('#time-range');
        this.tbl!.addEventListener('column-click', (evt) => {
            this.sortByColumn(evt.detail)
        });
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
<label id="time-range" style="width: 100%;height: 20px;text-align: end;font-size: 10pt;margin-bottom: 5px">Selected range:0.0 ms</label>
<lit-table id="tb-cpu-thread" style="height: auto">
    <lit-table-column order width="1fr" title="Start Time" data-index="startTime" key="startTime" align="flex-start" order ></lit-table-column>
    <lit-table-column order width="25%" title="Process" data-index="processName" key="processName" align="flex-start" order ></lit-table-column>
    <lit-table-column order width="25%" title="Thread" data-index="threadName" key="threadName" align="flex-start" order ></lit-table-column>
    <lit-table-column order width="1fr" title="State" data-index="state" key="state" align="flex-start" order ></lit-table-column>
    <lit-table-column order width="1fr" title="Core" data-index="core" key="core" align="flex-start" order ></lit-table-column>
    <lit-table-column order width="1fr" title="Priority" data-index="prior" key="prior" align="flex-start" order ></lit-table-column>
    <lit-table-column order width="1fr" title="Note" data-index="note" key="note" align="flex-start" ></lit-table-column>
</lit-table>
        `;
    }

    sortByColumn(detail) {
        function compare(property, sort, type) {
            return function (a: SelectionData, b: SelectionData) {
                if (type === 'number') {
                    return sort === 2 ? parseFloat(b[property]) - parseFloat(a[property]) : parseFloat(a[property]) - parseFloat(b[property]);
                } else {
                    if (b[property] > a[property]) {
                        return sort === 2 ? 1 : -1;
                    } else if (b[property] == a[property]) {
                        return 0;
                    } else {
                        return sort === 2 ? -1 : 1;
                    }
                }
            }
        }

        this.source.sort(compare(detail.key, detail.sort, 'string'))
        this.tbl!.dataSource = this.source;
    }

}
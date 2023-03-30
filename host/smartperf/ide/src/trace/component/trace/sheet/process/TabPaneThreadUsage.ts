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

import {BaseElement, element} from "../../../../../base-ui/BaseElement.js";
import {LitTable} from "../../../../../base-ui/table/lit-table.js";
import {SelectionData, SelectionParam} from "../../../../bean/BoxSelection.js";
import "../../../StackBar.js"
import { getTabThreadStatesCpu} from "../../../../database/SqlLite.js";
import {StackBar} from "../../../StackBar.js";
import {log} from "../../../../../log/Log.js";
import {getProbablyTime} from "../../../../database/logic-worker/ProcedureLogicWorkerCommon.js";

@element('tabpane-thread-usage')
export class TabPaneThreadUsage extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;
    private stackBar: StackBar | null | undefined;
    private source: Array<SelectionData> = []
    private cpuCount = 0;
    private pubColumns = `
            <lit-table-column width="200px" title="Process" data-index="process" key="process"  align="flex-start" order>
            </lit-table-column>
            <lit-table-column width="100px" title="PID" data-index="pid" key="pid"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="200px" title="Thread" data-index="thread" key="thread"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="100px" title="TID" data-index="tid" key="tid"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="160px" title="Wall duration" data-index="wallDurationTimeStr" key="wallDurationTimeStr"  align="flex-start" order >
            </lit-table-column>
    `

    set data(val: SelectionParam | any) {
        if(this.cpuCount !== (window as any).cpuCount){
            this.cpuCount = (window as any).cpuCount
            this.tbl!.innerHTML = this.getTableColumns()
        }
        //@ts-ignore
        this.tbl?.shadowRoot?.querySelector(".table")?.style?.height = (this.parentElement!.clientHeight - 45) + "px";
        // // @ts-ignore
        this.range!.textContent = "Selected range: " + ((val.rightNs - val.leftNs) / 1000000.0).toFixed(5) + " ms"
        getTabThreadStatesCpu(val.threadIds, val.leftNs, val.rightNs).then((result) => {
            if (result != null && result.length > 0) {
                log("getTabThreadStates result size : " + result.length)
                let map:Map<number,any> = new Map<number, any>();
                for (let e of result) {
                    if(map.has(e.tid)){
                        map.get(e.tid)[`cpu${e.cpu}`] = e.wallDuration || 0
                        map.get(e.tid)[`cpu${e.cpu}TimeStr`] = getProbablyTime(e.wallDuration || 0)
                        map.get(e.tid)[`cpu${e.cpu}Ratio`] = ((e.wallDuration || 0) / (val.rightNs - val.leftNs)).toFixed(2)
                        map.get(e.tid)[`wallDuration`] = map.get(e.tid)[`wallDuration`] + (e.wallDuration || 0)
                        map.get(e.tid)[`wallDurationTimeStr`] = getProbablyTime(map.get(e.tid)[`wallDuration`])
                    }else{
                        let obj:any = {
                            tid:e.tid,
                            pid:e.pid,
                            thread:e.thread,
                            process:e.process,
                            wallDuration:e.wallDuration || 0,
                            wallDurationTimeStr:getProbablyTime(e.wallDuration||0)
                        }
                        for (let i = 0; i < this.cpuCount; i++) {
                            obj[`cpu${i}`] = 0
                            obj[`cpu${i}TimeStr`] = '0'
                            obj[`cpu${i}Ratio`] = '0'
                        }
                        obj[`cpu${e.cpu}`] = e.wallDuration || 0
                        obj[`cpu${e.cpu}TimeStr`] = getProbablyTime(e.wallDuration || 0)
                        obj[`cpu${e.cpu}Ratio`] = (100.0 * (e.wallDuration || 0) / (val.rightNs - val.leftNs)).toFixed(2)
                        map.set(e.tid,obj)
                    }
                }
                this.source = Array.from(map.values());
                this.tbl!.recycleDataSource = this.source
            } else {
                this.source = []
                this.tbl!.recycleDataSource = []
            }
        })
    }

    getTableColumns(){
        let col = `${this.pubColumns}`
        let cpuCount = (window as any).cpuCount;
        for (let i=0;i < cpuCount;i++) {
            col = `${col}
            <lit-table-column width="100px" title="cpu${i}" data-index="cpu${i}TimeStr" key="cpu${i}TimeStr"  align="flex-start" order>
            </lit-table-column>
            <lit-table-column width="100px" title="%" data-index="cpu${i}Ratio" key="cpu${i}Ratio"  align="flex-start" order>
            </lit-table-column>
            `
        }
        return col;
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-thread-states');
        this.range = this.shadowRoot?.querySelector('#time-range');
        this.stackBar = this.shadowRoot?.querySelector('#stack-bar');
        this.tbl!.addEventListener('column-click', (evt: any) => {
            this.sortByColumn(evt.detail)
        });
    }

    connectedCallback() {
        super.connectedCallback();
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 45) + "px"
                this.tbl?.reMeauseHeight()
            }
        }).observe(this.parentElement!)
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
        <div style="overflow: auto">
            <lit-table id="tb-thread-states" style="height: auto"></lit-table>
        </div>
        `;
    }

    sortByColumn(detail: any) {
        function compare(property: any, sort: any, type: any) {
            return function (a: SelectionData | any, b: SelectionData | any) {
                if (a.process == " " || b.process == " ") {
                    return 0;
                }
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

        if (detail.key === "process" || detail.key === "thread" || (detail.key as string).includes("Ratio")) {
            this.source.sort(compare(detail.key, detail.sort, 'string'))
        } else {
            this.source.sort(compare((detail.key as string).replace("TimeStr",""), detail.sort, 'number'))
        }
        this.tbl!.recycleDataSource = this.source;
    }

}

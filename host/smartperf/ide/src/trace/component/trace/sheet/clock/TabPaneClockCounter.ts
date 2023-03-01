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
import {Counter, SelectionData, SelectionParam} from "../../../../bean/BoxSelection.js";
import {getTabCounters, getTabVirtualCounters} from "../../../../database/SqlLite.js";

@element('tabpane-clock-counter')
export class TabPaneClockCounter extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;
    private source: Array<SelectionData> = []

    set data(val: SelectionParam | any) {
        //@ts-ignore
        this.tbl?.shadowRoot?.querySelector(".table")?.style?.height = (this.parentElement!.clientHeight - 45) + "px";
        this.range!.textContent = "Selected range: " + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + " ms";
        let dataSource: Array<SelectionData> = [];
        let collect = val.clockMapData;
        let sumCount = 0;
        for (let key of collect.keys()) {
            let counters = collect.get(key);
            let sd = this.createSelectCounterData(key,counters, val.leftNs, val.rightNs);
            sumCount += Number.parseInt(sd.count||"0");
            dataSource.push(sd);
        }
        let sumData = new SelectionData();
        sumData.count = sumCount.toString();
        sumData.process = " ";
        dataSource.splice(0, 0, sumData);
        this.source = dataSource
        this.tbl!.recycleDataSource = dataSource

    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-counter');
        this.range = this.shadowRoot?.querySelector('#time-range');
        this.tbl!.addEventListener('column-click', (evt) => {
            // @ts-ignore
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
        }).observe(this.parentElement!);
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
        <lit-table id="tb-counter" style="height: auto">
            <lit-table-column width="25%" title="Name" data-index="name" key="name"  align="flex-start" order>
            </lit-table-column>
            <lit-table-column width="1fr" title="Delta value" data-index="delta" key="delta"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Rate /s" data-index="rate" key="rate"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Weighted avg value" data-index="avgWeight" key="avgWeight"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Count" data-index="count" key="count"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="First value" data-index="first" key="first"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Last value" data-index="last" key="last"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Min value" data-index="min" key="min"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Max value" data-index="max" key="max"  align="flex-start" order >
            </lit-table-column>
        </lit-table>
        `;
    }

    createSelectCounterData(name: string,list: Array<any>, leftNs: number, rightNs: number): SelectionData {
        let selectData = new SelectionData();
        if (list.length > 0) {
            let range = rightNs - leftNs;
            let first = list[0];
            selectData.trackId = first.filterId;
            selectData.name = name;
            selectData.first = first.value + "";
            selectData.count = list.length + "";
            selectData.last = list[list.length - 1].value + "";
            selectData.delta = (parseInt(selectData.last) - parseInt(selectData.first)) + "";
            selectData.rate = (parseInt(selectData.delta) / (range * 1.0 / 1000000000)).toFixed(4);
            selectData.min = first.value + "";
            selectData.max = "0";
            let weightAvg = 0.0;
            for (let i = 0; i < list.length; i++) {
                let counter = list[i];
                if (counter.value < parseInt(selectData.min)) {
                    selectData.min = counter.value.toString();
                }
                if (counter.value > parseInt(selectData.max)) {
                    selectData.max = counter.value.toString();
                }
                let start = i == 0 ? leftNs : counter.startNS
                let end = i == list.length - 1 ? rightNs : list[i + 1].startNS
                weightAvg += counter.value * ((end - start) * 1.0 / range);
            }
            selectData.avgWeight = weightAvg.toFixed(2)
        }
        return selectData;
    }

    sortByColumn(detail: any) {
        // @ts-ignore
        function compare(property, sort, type) {
            return function (a: SelectionData, b: SelectionData) {
                if (a.process == " " || b.process == " ") {
                    return 0;
                }
                if (type === 'number') {
                    // @ts-ignore
                    return sort === 2 ? parseFloat(b[property]) - parseFloat(a[property]) : parseFloat(a[property]) - parseFloat(b[property]);
                } else {
                    // @ts-ignore
                    if (b[property] > a[property]) {
                        return sort === 2 ? 1 : -1;
                    } else { // @ts-ignore
                        if (b[property] == a[property]) {
                            return 0;
                        } else {
                            return sort === 2 ? -1 : 1;
                        }
                    }
                }
            }
        }

        if (detail.key === 'name') {
            this.source.sort(compare(detail.key, detail.sort, 'string'))
        } else {
            this.source.sort(compare(detail.key, detail.sort, 'number'))
        }
        this.tbl!.recycleDataSource = this.source;
    }

}
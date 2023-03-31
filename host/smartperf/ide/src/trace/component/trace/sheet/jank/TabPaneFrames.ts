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
import {SelectionData, SelectionParam} from "../../../../bean/BoxSelection";
import {LitTable} from "../../../../../base-ui/table/lit-table";
import {JankFramesStruct} from "../../../../bean/JankFramesStruct.js";
import {JanksStruct} from "../../../../bean/JanksStruct.js";

@element('tabpane-frames')
export class TabPaneFrames extends BaseElement {
    private litTable: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;
    private source: Array<any> = []
    set data(val: SelectionParam | any) {
        this.range!.textContent = "Selected range: " + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + " ms"
        this.queryDataByDB(val)
    }


    queryDataByDB(val: SelectionParam | any) {
        let tablelist = new Array<JankFramesStruct>()
        let sumRes: JankFramesStruct = new JankFramesStruct();
        let appJank: JankFramesStruct = new JankFramesStruct();
        let rsJank: JankFramesStruct = new JankFramesStruct();
        let noJank: JankFramesStruct = new JankFramesStruct();
        val.jankFramesData.forEach((data:Array<JanksStruct>) => {
            sumRes.occurrences += data.length
            data.forEach((da:JanksStruct) => {
                if (da.dur == null || da.dur == undefined) {
                    da.dur = 0;
                }
                if (da.frame_type == 'app') {
                    if (da.jank_tag) {
                        appJank.flag = da.jank_tag
                        appJank.jankType = 'APP Deadline Missed'
                        appJank.occurrences += 1
                        appJank.maxDuration = Math.max(da.dur , appJank.maxDuration!)
                        if (appJank.minDuration == -1) {
                            appJank.minDuration =  da.dur
                        } else {
                            appJank.minDuration = Math.min(da.dur,appJank.minDuration!)
                        }
                        if (appJank.meanDuration == -1) {
                            appJank.meanDuration = da.dur
                        } else {
                            appJank.meanDuration = Number(((da.dur + appJank.meanDuration!) /2).toFixed(2));
                        }
                    } else {
                        noJank.flag =  da.jank_tag
                        noJank.jankType = 'None'
                        noJank.occurrences += 1
                        noJank.maxDuration = Math.max(da.dur, noJank.maxDuration!)
                        if (noJank.minDuration == -1) {
                            noJank.minDuration =  da.dur
                        } else {
                            noJank.minDuration = Math.min(da.dur,noJank.minDuration!)
                        }
                        if (noJank.meanDuration == -1) {
                            noJank.meanDuration = da.dur
                        } else {
                            noJank.meanDuration = Number(((da.dur + noJank.meanDuration!) /2).toFixed(2));
                        }
                    }
                } else if(da.frame_type == 'renderService') {
                    if (da.jank_tag) {
                        rsJank.flag = da.jank_tag
                        rsJank.jankType = 'RenderService Deadline Missed'
                        rsJank.occurrences += 1
                        rsJank.maxDuration = Math.max(da.dur, rsJank.maxDuration!)
                        if (rsJank.minDuration == -1) {
                            rsJank.minDuration =  da.dur
                        } else {
                            rsJank.minDuration = Math.min(da.dur,rsJank.minDuration!)
                        }
                        if (rsJank.meanDuration == -1) {
                            rsJank.meanDuration = da.dur
                        } else {
                            rsJank.meanDuration = Number(((da.dur + rsJank.meanDuration!) /2).toFixed(2));
                        }
                    } else {
                        noJank.flag =  da.jank_tag
                        noJank.jankType = 'None'
                        noJank.occurrences += 1
                        noJank.maxDuration = Math.max(da.dur, noJank.maxDuration)
                        if (noJank.minDuration == -1) {
                            noJank.minDuration =  da.dur
                        } else {
                            noJank.minDuration = Math.min(da.dur,noJank.minDuration!)
                        }
                        if (noJank.meanDuration == -1) {
                            noJank.meanDuration = da.dur
                        } else {
                            noJank.meanDuration = Number(((da.dur + noJank.meanDuration) /2).toFixed(2));
                        }
                    }
                } else {
                    // frameTime
                    if (da.jank_tag) {
                        appJank.flag = da.jank_tag
                        appJank.jankType = 'Deadline Missed'
                        appJank.occurrences += 1
                        appJank.maxDuration = Math.max(da.dur, appJank.maxDuration)
                        appJank.minDuration = Math.min(da.dur,appJank.minDuration)
                        if (appJank.minDuration == -1) {
                            appJank.minDuration =  da.dur
                        } else {
                            appJank.minDuration = Math.min(da.dur,appJank.minDuration!)
                        }
                        if (appJank.meanDuration == -1) {
                            appJank.meanDuration = da.dur
                        } else {
                            appJank.meanDuration = Number(((da.dur + appJank.meanDuration) /2).toFixed(2));
                        }
                    } else {
                        noJank.flag =  da.jank_tag
                        noJank.jankType = 'None'
                        noJank.occurrences += 1
                        noJank.maxDuration = Math.max(da.dur, noJank.maxDuration)
                        if (noJank.minDuration == -1) {
                            noJank.minDuration =  da.dur
                        } else {
                            noJank.minDuration = Math.min(da.dur,noJank.minDuration!)
                        }
                        if (noJank.meanDuration == -1) {
                            noJank.meanDuration = da.dur
                        } else {
                            noJank.meanDuration = Number(((da.dur + noJank.meanDuration) /2).toFixed(2));
                        }

                    }
                }
            })
        })
        tablelist.push(sumRes)
        if (appJank.occurrences > 0){
            appJank.maxDurationStr = appJank.maxDuration + ""
            appJank.minDurationStr = appJank.minDuration  + ""
            appJank.meanDurationStr = appJank.meanDuration  + ""
            tablelist.push(appJank)
        }
        if (rsJank.occurrences > 0){
            rsJank.maxDurationStr = rsJank.maxDuration  + ""
            rsJank.minDurationStr = rsJank.minDuration  + ""
            rsJank.meanDurationStr = rsJank.meanDuration  + ""
            tablelist.push(rsJank)
        }
        if (noJank.occurrences > 0){
            noJank.maxDurationStr = noJank.maxDuration  + ""
            noJank.minDurationStr = noJank.minDuration + ""
            noJank.meanDurationStr = noJank.meanDuration + ""
            tablelist.push(noJank)
        }
        this.source = tablelist
        this.litTable!.recycleDataSource = tablelist
    }

    initElements(): void {
        this.litTable = this.shadowRoot?.querySelector<LitTable>('#tb-frames');
        this.range = this.shadowRoot?.querySelector('#time-range');
        this.litTable!.addEventListener('column-click', (evt) => {
            // @ts-ignore
            this.sortByColumn(evt.detail)
        });
    }

    connectedCallback() {
        super.connectedCallback();
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.litTable?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 45) + "px"
                this.litTable?.reMeauseHeight()
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
        <label id="time-range" style="width: 100%;height: 20px;text-align: end;font-size: 10pt;margin-bottom: 5px">Selected range:0.0 ms</label>
        <lit-table id="tb-frames" style="height: auto">
            <lit-table-column title="Jank Type" width="1fr" data-index="jankType" key="jankType"  align="flex-start" order>
            </lit-table-column>
            <lit-table-column title="Min duration" width="1fr" data-index="minDurationStr" key="minDurationStr"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column title="Max duration" width="1fr" data-index="maxDurationStr" key="maxDurationStr"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column title="Mean duration" width="1fr" data-index="meanDurationStr" key="meanDurationStr"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column title="Occurrences" width="1fr" data-index="occurrences" key="occurrences"  align="flex-start" order >
            </lit-table-column>
        </lit-table>
        `;
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

        if (detail.key === "jankType") {
            this.source.sort(compare(detail.key, detail.sort, 'string'))
        } else {
            this.source.sort(compare(detail.key, detail.sort, 'number'))
        }
        this.litTable!.recycleDataSource = this.source;
    }
}
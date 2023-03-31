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

import {BaseElement, element} from "../../../base-ui/BaseElement.js";
import {LitChartPie} from "../../../base-ui/chart/pie/LitChartPie.js";
import {procedurePool} from "../../database/Procedure.js";
import {SpSchedulingAnalysis} from "./SpSchedulingAnalysis.js";
import {info} from "../../../log/Log.js";
import {LitTable} from "../../../base-ui/table/lit-table.js";
import "../../../base-ui/progress-bar/LitProgressBar.js"
import {LitProgressBar} from "../../../base-ui/progress-bar/LitProgressBar.js";
import {getDataNo} from "./utils/Utils.js";
import "./TableNoData.js"
import {TableNoData} from "./TableNoData.js";
import {pieChartColors} from "../../../base-ui/chart/pie/LitChartPieData.js";

@element('tab-cpu-details-idle')
export class TabCpuDetailsIdle extends BaseElement {

    private tableNoData:TableNoData | null | undefined;
    private table:LitTable | null | undefined;
    private progress:LitProgressBar | null | undefined;
    traceChange:boolean = false;
    private pie:LitChartPie | null | undefined;
    private data:Array<any> = [];

    initElements(): void {
        this.tableNoData = this.shadowRoot!.querySelector<TableNoData>("#table-no-data")
        this.progress = this.shadowRoot!.querySelector<LitProgressBar>("#loading")
        this.pie = this.shadowRoot!.querySelector<LitChartPie>("#chart-pie")
        this.table = this.shadowRoot!.querySelector<LitTable>("#tb-cpu-usage")
    }

    init(cpu:number){
        this.queryPieChartDataByType("CPU Idle",cpu)
    }

    queryPieChartDataByType(type: string,cpu:number) {
        if(this.traceChange){return;}
        this.progress!.loading = true;
        this.queryLoginWorker(`scheduling-${type}`, "query Cpu Frequency Analysis Time:", (res) => {
            this.traceChange = true;
            this.progress!.loading = false;
            this.data = res.get(cpu) || [];
            this.data = getDataNo(this.data);
            this.tableNoData!.noData = (this.data.length == 0);
            this.noData(this.data.length == 0);
            this.pie!.config = {
                appendPadding: 0,
                data: res.get(cpu) || [],
                angleField: 'sum',
                colorField: 'value',
                radius: 1,
                label: {
                    type: 'outer',
                    color:type !== "CPU Idle" ? undefined : (it )=>{
                        return pieChartColors[(it as any).value]
                    }
                },
                tip:(obj)=>{
                    return `<div>
                                <div>idle:${obj.obj.value}</div> 
                                <div>min:${obj.obj.min}</div>
                                <div>max:${obj.obj.max}</div>
                                <div>average:${obj.obj.avg}</div>
                                <div>duration:${obj.obj.sumTimeStr}</div>
                                <div>ratio:${obj.obj.ratio}%</div>
                            </div>
                                `
                },
                interactions: [
                    {
                        type: 'element-active',
                    },
                ],
            }
            this.table!.recycleDataSource = this.data;
            this.table?.reMeauseHeight()
        })
    }

    noData(value:boolean){
        this.shadowRoot!.querySelector<HTMLDivElement>(".chart-box")!.style.display = value?"none":"block"
        this.shadowRoot!.querySelector<HTMLDivElement>(".table-box")!.style.width = value?"100%":"60%"
    }

    clearData(){
        this.traceChange = false;
        this.pie!.dataSource = []
        this.table!.recycleDataSource = []
        this.noData(false)
    }

    queryLoginWorker(option:string,log:string,handler:(res:any) => void){
        let time = new Date().getTime();
        procedurePool.submitWithName("logic1", option, { endTs:SpSchedulingAnalysis.endTs,total:SpSchedulingAnalysis.totalDur }, undefined, handler)
        let durTime = new Date().getTime() - time;
        info(log, durTime)
    }

    initHtml(): string {
        return `
        <style>
        :host {
            width: 100%;
            height: 100%;
            background-color: var(--dark-background,#FFFFFF);
        }
        .d-box{
            display: flex;
            margin: 20px;
            height: calc(100vh - 165px);
        }
        .chart-box{
            width: 40%;
        }
        #tb-cpu-usage{
            height: 100%;
        }
        .table-box{
            width: 60%;
            max-height: calc(100vh - 165px);
            border: solid 1px var(--dark-border1,#e0e0e0);
            border-radius: 5px;
            padding: 10px;
        }
        #chart-pie{
            height: 360px;
        }
        </style>
        <lit-progress-bar id="loading" style="height: 1px;width: 100%"></lit-progress-bar>
        <div class="d-box">
            <div class="chart-box">
                <div style="text-align: center">Statistics By Duration</div>
                <lit-chart-pie  id="chart-pie"></lit-chart-pie>
            </div>
            <div class="table-box">
                <table-no-data id="table-no-data">
                    <lit-table id="tb-cpu-usage">
                        <lit-table-column width="100px" title="No" data-index="index" key="index" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="idle" data-index="value" key="value" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="min" data-index="min" key="min" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="max" data-index="max" key="max" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="average" data-index="avg" key="avg" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="duration" data-index="sumTimeStr" key="sumTimeStr" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="%" data-index="ratio" key="ratio" align="flex-start"></lit-table-column>
                     </lit-table>
                 </table-no-data>
            </div>
        </div>
        `;
    }
}

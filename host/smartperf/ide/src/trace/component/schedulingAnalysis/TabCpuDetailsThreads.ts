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
import {LitTable} from "../../../base-ui/table/lit-table.js";
import {LitProgressBar} from "../../../base-ui/progress-bar/LitProgressBar.js";
import "../../../base-ui/progress-bar/LitProgressBar.js"
import {getDataNo} from "./utils/Utils.js";
import "./TableNoData.js"
import {TableNoData} from "./TableNoData.js";

@element('tab-cpu-details-threads')
export class TabCpuDetailsThreads extends BaseElement {

    private tableNoData:TableNoData | null | undefined;
    private table:LitTable | null | undefined;
    private progress:LitProgressBar | null | undefined;
    private pie:LitChartPie | null | undefined
    private data:Array<any> = [];

    initElements(): void {
        this.tableNoData = this.shadowRoot!.querySelector<TableNoData>("#table-no-data")
        this.progress = this.shadowRoot!.querySelector<LitProgressBar>("#loading")
        this.pie = this.shadowRoot!.querySelector<LitChartPie>("#chart-pie")
        this.table = this.shadowRoot!.querySelector<LitTable>("#tb-cpu-usage")

        this.shadowRoot!.querySelector<HTMLDivElement>(".go-back")!.onclick = (e)=>{
            if (!this.progress!.loading) {
                this.parentNode!.querySelector<HTMLDivElement>(".d-box")!.style.display = "flex";
                this.setShow = false;
            }
        }
    }

    init(cpu:number,it:any){
        this.shadowRoot!.querySelector<HTMLDivElement>(".subheading")!.textContent = ("Threads in Freq " + it.value);
        this.progress!.loading = true;
        procedurePool.submitWithName("logic1", "scheduling-CPU Frequency Thread", { cpu:cpu,freq:(it as any).value }, undefined, (res:any)=>{
            this.progress!.loading = false;
            this.queryPieChartDataByType(res)
        })
    }

    set setShow(v:boolean){
        if (v) {
            this.style.display = "flex";
        }else {
            this.clearData();
            this.style.display = "none";
        }
    }

    queryPieChartDataByType(res:any) {
        this.data = res || [];
        this.data = getDataNo(this.data);
        this.tableNoData!.noData = (this.data.length == 0);
        this.noData(this.data.length == 0);
        this.pie!.config = {
            appendPadding: 0,
            data: res || [],
            angleField: 'dur',
            colorField: 'tName',
            radius: 1,
            label: {
                type: 'outer',
            },
            tip:(obj)=>{
                return `<div>
                                <div>t_name:${obj.obj.tName}</div> 
                                <div>tid:${obj.obj.tid}</div>
                                <div>p_name:${obj.obj.pName}</div>
                                <div>p_pid:${obj.obj.pid}</div>
                                <div>duration:${obj.obj.durStr}</div>
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
    }

    noData(value:boolean){
        this.shadowRoot!.querySelector<HTMLDivElement>(".chart-box")!.style.display = value?"none":"block"
        this.shadowRoot!.querySelector<HTMLDivElement>(".table-box")!.style.width = value?"100%":"60%"
    }

    clearData(){
        this.pie!.dataSource = []
        this.table!.recycleDataSource = []
        this.noData(false)
    }

    initHtml(): string {
        return `
        <style>
        :host {
            width: 100%;
            height: 100%;
            background-color: var(--dark-background,#FFFFFF);
            display: none;
        }
        .d-box{
            display: flex;
            margin: 20px;
            height: calc(100vh - 165px);
        }
        .chart-box{
            width: 40%;
        }
        .subheading{
            font-weight: bold;
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
        .go-back{
            display:flex;
            align-items: center;
            cursor: pointer;
        }
        .back-box{
            background-color: var(--bark-expansion,#0C65D1);
            border-radius: 5px;
            color: #fff;
            display: flex;
            margin-right: 10px;
            width: 40px;
            height: 20px;
            justify-content: center;
            align-items: center;
        }
        </style>
        <lit-progress-bar id="loading" style="height: 1px;width: 100%"></lit-progress-bar>
        <div class="d-box">
            <div class="chart-box">
                <div class="go-back">
                    <div class="back-box">
                        <lit-icon name="arrowleft"></lit-icon>
                    </div>
                    <!--<lit-icon name="arrowleft"></lit-icon>-->
                    <div class="subheading">Threads in Freq</div>
                </div>
                <div style="margin-top:15px;text-align: center">Statistics By Duration</div>
                <lit-chart-pie  id="chart-pie"></lit-chart-pie>
            </div>
            <div class="table-box">
                <table-no-data id="table-no-data">
                    <lit-table id="tb-cpu-usage">
                        <lit-table-column width="100px" title="No" data-index="index" key="index" align="flex-start"></lit-table-column>
                        <lit-table-column width="200px" title="t_name" data-index="tName" key="tName" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="tid" data-index="tid" key="tid" align="flex-start"></lit-table-column>
                        <lit-table-column width="200px" title="p_name" data-index="pName" key="pName" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="p_pid" data-index="pid" key="pid" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="duration" data-index="durStr" key="durStr" align="flex-start"></lit-table-column>
                        <lit-table-column width="100px" title="%" data-index="ratio" key="ratio" align="flex-start"></lit-table-column>
                    </lit-table>
                </table-no-data>
            </div>
        </div>
        `;
    }
}

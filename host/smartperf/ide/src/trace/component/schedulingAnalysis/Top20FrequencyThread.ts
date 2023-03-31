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
import {LitTable} from "../../../base-ui/table/lit-table.js";
import {procedurePool} from "../../database/Procedure.js";
import {info} from "../../../log/Log.js";
import '../../../base-ui/chart/pie/LitChartPie.js'
import {LitChartPie} from "../../../base-ui/chart/pie/LitChartPie.js";
import {LitSelect} from "../../../base-ui/select/LitSelect.js";
import {queryThreads} from "../../database/SqlLite.js";
import {LitSelectOption} from "../../../base-ui/select/LitSelectOption.js";
import "../../../base-ui/progress-bar/LitProgressBar.js"
import {LitProgressBar} from "../../../base-ui/progress-bar/LitProgressBar.js";
import "./TableNoData.js"
import {TableNoData} from "./TableNoData.js";

@element('top20-frequency-thread')
export class Top20FrequencyThread extends BaseElement {

    static threads:{id:number,tid:number,name:string}[] | undefined
    traceChange:boolean = false;
    private table:LitTable | null | undefined
    private threadSelect:LitSelect | null | undefined
    private pie:LitChartPie | null | undefined
    private currentThread:HTMLDivElement | null | undefined
    private progress:LitProgressBar | null | undefined;
    private nodata:TableNoData | null | undefined;
    private currentTid:number = 0;

    initElements(): void {
        this.nodata = this.shadowRoot!.querySelector<TableNoData>("#nodata")
        this.progress = this.shadowRoot!.querySelector<LitProgressBar>("#loading")
        this.table = this.shadowRoot!.querySelector<LitTable>("#tb-process-thread-count")
        this.currentThread = this.shadowRoot!.querySelector<HTMLDivElement>("#current_thread")
        this.threadSelect = this.shadowRoot!.querySelector<LitSelect>("#thread_select")
        this.pie = this.shadowRoot!.querySelector<LitChartPie>("#pie")

        this.threadSelect!.onchange = (e)=>{
            this.currentThread!.textContent = (e as any).detail.text;
            this.currentTid = parseInt((e as any).detail.value);
            this.progress!.loading = true
            this.queryData();
        }
    }

    async init(){
        if(!this.traceChange){
            if(this.table!.recycleDataSource.length > 0){
                this.table?.reMeauseHeight();
            }
            return;
        }
        this.traceChange = false;
        this.progress!.loading = true
        if(Top20FrequencyThread.threads === undefined){
            Top20FrequencyThread.threads = await queryThreads() || []
            this.nodata!.noData = Top20FrequencyThread.threads === undefined || Top20FrequencyThread.threads.length === 0;
            this.threadSelect!.innerHTML = '';
            let threads = Top20FrequencyThread.threads.map(it => {
                let option = new LitSelectOption();
                option.setAttribute("value",it.tid+"")
                option.textContent = it.name
                return option
            })
            this.threadSelect!.append(...threads)
            this.threadSelect?.initOptions()
            this.threadSelect!.value = Top20FrequencyThread.threads[0].tid+""
            this.currentThread!.textContent = Top20FrequencyThread.threads[0].name;
            this.currentTid = Top20FrequencyThread.threads[0].tid;
            this.queryData()
        }
    }

    queryData(){
        this.queryLogicWorker("scheduling-Thread Freq","query Thread Top 20 Frequency Time:",(res)=>{
            this.nodata!.noData = Top20FrequencyThread.threads === undefined || Top20FrequencyThread.threads.length === 0 || res === undefined || res.length === 0;
            (res as any[]).map((it:any,index:number)=> {
                it.no = index + 1
            })
            this.table!.recycleDataSource = res;
            this.table!.reMeauseHeight()
            this.pie!.config = {
                appendPadding: 10,
                data:res,
                angleField: 'time',
                colorField: 'freq',
                radius: 0.8,
                label: {
                    type: 'outer',
                },
                tip:(obj)=>{
                    return `<div>
                             <div>freq:${obj.obj.freq}</div> 
                             <div>cpu:${obj.obj.cpu}</div> 
                             <div>time:${obj.obj.timeStr}</div> 
                             <div>ratio:${obj.obj.ratio}%</div>
                        </div>
                `;
                },
                interactions: [
                    {
                        type: 'element-active',
                    },
                ],
            }
            this.progress!.loading = false
        })
    }

    clearData(){
        this.traceChange = true;
        this.threadSelect!.innerHTML = ''
        this.pie!.dataSource = []
        this.table!.recycleDataSource = []
    }

    queryLogicWorker(option:string,log:string,handler:(res:any) => void){
        let time = new Date().getTime();
        procedurePool.submitWithName("logic1", option, {tid:this.currentTid}, undefined, handler)
        let durTime = new Date().getTime() - time;
        info(log, durTime)
    }

    initHtml(): string {
        return `
        <style>
        :host {
            width: 100%;
            height: 100%;
            background-color: var(--dark-background5,#F6F6F6);
        }
        .tb_thread_count{
            width: calc(100% - 50px);
            overflow: auto ;
            border-radius: 5px;
            border: solid 1px var(--dark-border1,#e0e0e0);
            margin: 15px;
            padding: 5px 15px
        }
        .pie-chart{
            display: flex;
            box-sizing: border-box;
            width: 80%;
            height: 500px;
        }
        .root{
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: row;
            overflow-x: hidden;
            overflow-y: auto;
            box-sizing: border-box;
        }
        </style>
        <lit-progress-bar id="loading" style="height: 1px;width: 100%" loading></lit-progress-bar>
        <div style="padding: 15px">
                    Thread Search
                    <lit-select default-value="1" id="thread_select" show-search placement="bottom"></lit-select>
        </div>
        <table-no-data id="nodata" contentHeight="500px">
        <div class="root">
            <div style="width: 40%;padding: 15px;display: flex;flex-direction: column;align-items: center">
                <div>Statistics By Duration</div>
                <lit-chart-pie id="pie" class="pie-chart"></lit-chart-pie>
            </div>
            <div style="flex: 1;display: flex;flex-direction: column;align-items: center;padding-top: 15px">
                <div id="current_thread" style="font-weight: bold"></div>
                <div class="tb_thread_count">
                    <lit-table id="tb-process-thread-count" style="height: auto">
                        <lit-table-column width="1fr" title="NO" data-index="no" key="no" align="flex-start"></lit-table-column>
                        <lit-table-column width="1fr" title="cpu" data-index="cpu" key="cpu" align="flex-start"></lit-table-column>
                        <lit-table-column width="1fr" title="frequency" data-index="freq" key="freq" align="flex-start"></lit-table-column>
                        <lit-table-column width="1fr" title="duration" data-index="timeStr" key="timeStr" align="flex-start"></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="ratio" key="ratio" align="flex-start"></lit-table-column>        
                    </lit-table>
                </div>
            </div>
        </div>
        </table-no-data>
        `;
    }
}

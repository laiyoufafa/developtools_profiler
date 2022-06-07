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
import "./TabPaneFilter.js";
import {FilterData, TabPaneFilter} from "./TabPaneFilter.js";
import {SelectionParam} from "../../../bean/BoxSelection.js";
import {perfDataQuery} from "../../hiperf/PerfDataQuery.js";
import {PerfCallChainMerageData} from "../../../bean/PerfProfile.js";
import "../../FrameChart.js";
import { FrameChart } from "../../FrameChart.js";
import { ChartMode } from "../../../database/ProcedureWorkerCommon.js";

@element('tabpane-perf-profile')
export class TabpanePerfProfile extends BaseElement {
    private tbl: LitTable | null | undefined;
    private tbr: LitTable | null | undefined;
    private rightSource: Array<PerfCallChainMerageData> = [];
    private filter: any
    private sampleIds:string[] = []
    private dataSource:any[] = []
    private currentSelectedData:any = undefined
    private frameChart: FrameChart | null | undefined;
    private isChartShow: boolean = false;
    private systmeRuleName = "/system/"
    private numRuleName = "/max/min/"

    set data(val: SelectionParam | any) {
        this.filter!.initializeFilterTree(true,true,true)
        this.sampleIds = val.perfSampleIds
        this.dataSource = perfDataQuery.getCallChainsBySampleIds(val.perfSampleIds,true)
        this.frameChart!.mode = ChartMode.Count;
        this.frameChart!.data = this.dataSource;
        this.frameChart?.updateCanvas();
        this.frameChart?.calculateChartData();
        this.tbl!.recycleDataSource = this.dataSource;
        this.tbr!.recycleDataSource = []
    }

    getParentTree(src: Array<PerfCallChainMerageData>, target: PerfCallChainMerageData, parents: Array<PerfCallChainMerageData>): boolean {
        for (let call of src) {
            if (call.id == target.id) {
                parents.push(call)
                return true
            } else {
                if (this.getParentTree(call.children as Array<PerfCallChainMerageData>, target, parents)) {
                    parents.push(call);
                    return true;
                }
            }
        }
        return false;
    }

    getChildTree(src: Array<PerfCallChainMerageData>, id: string, children: Array<PerfCallChainMerageData>): boolean {
        for (let call of src) {
            if (call.id == id && call.children.length == 0) {
                children.push(call)
                return true
            } else {
                if (this.getChildTree(call.children as Array<PerfCallChainMerageData>, id, children)) {
                    children.push(call);
                    return true;
                }
            }
        }
        return false;
    }

    setRightTableData(call: PerfCallChainMerageData) {
        let parents: Array<PerfCallChainMerageData> = [];
        let children: Array<PerfCallChainMerageData> = [];
        this.getParentTree(this.dataSource, call, parents);
        let maxId = call.id;
        let maxDur = 0;

        function findMaxStack(call: PerfCallChainMerageData) {
            if (call.children.length == 0) {
                if (call.dur > maxDur) {
                    maxDur = call.dur;
                    maxId = call.id;
                }
            } else {
                call.children.map((callChild) => {
                    findMaxStack(<PerfCallChainMerageData>callChild);
                })
            }
        }
        findMaxStack(call);
        this.getChildTree(call.children as Array<PerfCallChainMerageData>, maxId, children);
        let arr = parents.reverse().concat(children.reverse());
        for (let data of arr) {
            data.type = (data.libName.endsWith(".so.1") || data.libName.endsWith(".dll") || data.libName.endsWith(".so")) ? 0 : 1;
        }
        let len = arr.length;
        this.rightSource = arr;
        this.tbr!.recycleDataSource = len == 0 ? [] : this.rightSource.slice(3,len);
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-perf-profile');
        this.frameChart = this.shadowRoot?.querySelector<FrameChart>('#framechart');
        let pageTab = this.shadowRoot?.querySelector('#show_table');
        let pageChart = this.shadowRoot?.querySelector('#show_chart');
        this.parentElement!.onscroll = () => {
            this.frameChart!.tabPaneScrollTop = this.parentElement!.scrollTop;
        };
        this.tbl!.rememberScrollTop = true;
        this.tbl!.addEventListener('row-click', (evt:any) => {
            // @ts-ignore
            let data = (evt.detail.data as PerfCallChainMerageData);
            this.setRightTableData(data);
            data.isSelected = true;
            this.currentSelectedData = data;
            this.tbr?.clearAllSelection(data);
            this.tbr?.setCurrentSelection(data);
            // @ts-ignore
            if((evt.detail as any).callBack){
                // @ts-ignore
                (evt.detail as any).callBack(true)
            }
        })
        this.tbr = this.shadowRoot?.querySelector<LitTable>('#tb-perf-list');
        this.tbr!.addEventListener('row-click', (evt:any) => {
            // @ts-ignore
            let data = (evt.detail.data as PerfCallChainMerageData);
            this.tbl?.clearAllSelection(data);
            (data as any).isSelected = true
            this.tbl!.scrollToData(data)
            // @ts-ignore
            if ((evt.detail as any).callBack) {
                // @ts-ignore
                (evt.detail as any).callBack(true)
            }
        })
        this.tbr = this.shadowRoot?.querySelector<LitTable>('#tb-perf-list');
        let filterHeight = 0;
        new ResizeObserver((entries) => {
            let tabPaneFilter = this.shadowRoot!.querySelector("#filter") as HTMLElement;
            if (tabPaneFilter.clientHeight > 0) filterHeight = tabPaneFilter.clientHeight;
            if (this.parentElement!.clientHeight > filterHeight) {
                tabPaneFilter.style.display = "flex";
            } else {
                tabPaneFilter.style.display = "none";
            }
            if (this.parentElement?.clientHeight != 0) {
                if (!this.isChartShow) {
                    // @ts-ignore
                    this.tbl?.shadowRoot.querySelector(".table").style.height =  (this.parentElement.clientHeight - 10 - 35)+"px"
                    this.tbl?.reMeauseHeight()
                } else {
                    // @ts-ignore
                    this.frameChart?.updateCanvas();
                    this.frameChart?.calculateChartData();
                }
                // @ts-ignore
                this.tbr?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 45 - 21) + "px"
                this.tbr?.reMeauseHeight()
            }
        }).observe(this.parentElement!)
        this.filter = this.shadowRoot?.querySelector<TabPaneFilter>("#filter")
        this.filter!.getDataMining((data:any)=>{
            if(this.currentSelectedData&&this.currentSelectedData.symbolName == perfDataQuery.threadData[this.currentSelectedData.tid]?.processName){
                return
            }
            if (data.type == "check") {
                if(data.item.checked) {
                    perfDataQuery.splitTree(this.dataSource,data.item.name,data.item.select == "0",data.item.type=="symbol")
                }else {
                    perfDataQuery.resotreAllNode([data.item.name])
                    perfDataQuery.resetAllNode(this.dataSource)
                    perfDataQuery.clearSplitMapData(data.item.name)
                }
            }else if (data.type == "select") {
                perfDataQuery.resotreAllNode([data.item.name])
                perfDataQuery.clearSplitMapData(data.item.name)
                perfDataQuery.splitTree(this.dataSource,data.item.name,data.item.select == "0",data.item.type=="symbol")
            }else if (data.type=="button") {
                if (data.item == "symbol") {
                    if (this.currentSelectedData != undefined) {
                        this.filter!.addDataMining({name:this.currentSelectedData.symbolName},data.item)
                        perfDataQuery.splitTree(this.dataSource,this.currentSelectedData.symbolName,true,true)
                    }
                }else if (data.item == "library") {
                    if (this.currentSelectedData != undefined) {
                        this.filter!.addDataMining({name:this.currentSelectedData.libName},data.item)
                        perfDataQuery.splitTree(this.dataSource,this.currentSelectedData.libName,true,false)
                    }
                }else if (data.item == "restore") {
                    if (data.remove != undefined&&data.remove.length > 0) {
                        let list = data.remove.map((item:any)=>{
                            return item.name
                        })
                        perfDataQuery.resotreAllNode(list)
                        perfDataQuery.resetAllNode(this.dataSource)
                        list.forEach((symbolName:string)=>{
                            perfDataQuery.clearSplitMapData(symbolName)
                        })
                    }
                }
            }
            this.tbl!.recycleDataSource = this.dataSource
            this.frameChart!.data = this.dataSource;
            if (this.isChartShow) this.frameChart?.calculateChartData();
            this.tbl!.move1px()
            if(this.currentSelectedData){
                this.currentSelectedData.isSelected = false;
                this.tbl?.clearAllSelection(this.currentSelectedData)
                this.tbr!.recycleDataSource = []
                this.currentSelectedData = undefined
            }
        })
        this.filter!.getCallTreeData((data:any)=>{
            if(data.value == 0){
                this.refreshAllNode(this.filter!.getFilterTreeData())
            }else {
                if(data.checks[1]){
                    this.hideSystemLibrary()
                    perfDataQuery.resetAllNode(this.dataSource)
                }else {
                    perfDataQuery.resotreAllNode([this.systmeRuleName])
                    perfDataQuery.resetAllNode(this.dataSource)
                    perfDataQuery.clearSplitMapData(this.systmeRuleName)
                }
                this.tbl!.recycleDataSource = this.dataSource
                this.frameChart!.data = this.dataSource;
                if (this.isChartShow) this.frameChart?.calculateChartData();
            }
        })
        this.filter!.getCallTreeConstraintsData((data:any)=>{
            perfDataQuery.resotreAllNode([this.numRuleName])
            perfDataQuery.clearSplitMapData(this.numRuleName)
            if(data.checked){
                this.hideNumMaxAndMin(parseInt(data.min),data.max)
            }
            perfDataQuery.resetAllNode(this.dataSource)
            this.tbl!.recycleDataSource = this.dataSource
        })
        this.filter!.getFilterData((data:FilterData)=>{
            if (data.icon == 'block'){
                pageChart?.setAttribute('class', 'show');
                pageTab?.setAttribute('class', '');
                this.isChartShow = true;
                this.frameChart?.calculateChartData();
            } else if (data.icon == 'tree') {
                pageChart?.setAttribute('class', '');
                pageTab?.setAttribute('class', 'show');
                this.isChartShow = false;
                this.frameChart!.clearCanvas();
            }
        })
    }

    filterSampleIds(checked:boolean,min:string,max:string):Array<string>{
        let ids:Array<string> = [];
        if(checked){
            let minId = parseInt(min);
            let maxId = max == "∞" ? -999 : parseInt(max);
            if(minId != NaN && maxId != NaN){
                for (let sampleId of this.sampleIds) {
                    let id = parseInt(sampleId);
                    if(id != NaN){
                        if(id >= minId) {
                            if (maxId == -999) {
                                ids.push(sampleId);
                            } else if (id <= maxId) {
                                ids.push(sampleId);
                            }else{
                                continue;
                            }
                        }
                    }
                }
            }
        }else{
            ids.push(...this.sampleIds)
        }
        return ids;
    }

    hideSystemLibrary(){
        this.dataSource.forEach((item)=>{
            item.children = []
            perfDataQuery.recursionChargeByRule(item,this.systmeRuleName,(node)=>{
                return node.path.startsWith(this.systmeRuleName)
            })
        })
    }

    hideNumMaxAndMin(startNum:number,endNum:string){
        let max = endNum == "∞"?Number.POSITIVE_INFINITY :parseInt(endNum)
        console.log(max);
        this.dataSource.forEach((item)=>{
            item.children = []
            perfDataQuery.recursionChargeByRule(item,this.numRuleName,(node)=>{
                return node.dur < startNum || node.dur > max
            })
        })
    }

    refreshAllNode(filterData:any){
        let isTopDown:boolean = !filterData.callTree[0];
        let isHideSystemLibrary = filterData.callTree[1];
        let list = filterData.dataMining;
        this.dataSource = perfDataQuery.getCallChainsBySampleIds(this.sampleIds,isTopDown);
        this.tbr!.recycleDataSource = []
        if(isHideSystemLibrary){
            this.hideSystemLibrary()
        }
        if(filterData.callTreeConstraints.checked){
            this.hideNumMaxAndMin(parseInt(filterData.callTreeConstraints.inputs[0]),filterData.callTreeConstraints.inputs[1])
        }
        list.forEach((item:any)=>{
            this.dataSource.forEach((process)=>{
                if(item.select == "0"){
                    perfDataQuery.recursionChargeInitTree(process, item.name, item.type == "symbol")
                }else {
                    perfDataQuery.recursionPruneInitTree(process, item.name, item.type == "symbol")
                }
            })
            if(!item.checked){
                perfDataQuery.resotreAllNode([item.name])
            }
        })
        perfDataQuery.resetAllNode(this.dataSource)
        this.tbl!.recycleDataSource = this.dataSource
        this.frameChart!.data = this.dataSource;
        if (this.isChartShow) this.frameChart?.calculateChartData();
    }

    initHtml(): string {
        return `
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px 0 10px;
        }
        tab-pane-filter {
            border: solid rgb(216,216,216) 1px;
            float: left;
            text-align: center;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
        selector{
            display: none;
        }
        .show{
            display: flex;
            flex: 1;
        }
    </style>
    <div style="display: flex;flex-direction: row">
    <selector id='show_table' class="show">
        <div style="width: 65%">
            <lit-table id="tb-perf-profile" style="height: auto" tree>
                <lit-table-column width="70%" title="Call Stack" data-index="symbol" key="symbol"  align="flex-start" ></lit-table-column>
                <lit-table-column width="1fr" title="Local" data-index="self" key="self"  align="flex-start"  ></lit-table-column>
                <lit-table-column width="1fr" title="Weight" data-index="weight" key="weight"  align="flex-start"  ></lit-table-column>
            </lit-table>
        </div>
        <div style="width: 35%">
            <div>Heaviest Stack Trace</div>
                <lit-table id="tb-perf-list" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)">
                    <lit-table-column width="60px" title="" data-index="type" key="type"  align="flex-start" >
                        <template>
                            <img src="img/library.png" size="20" v-if=" type == 1 ">
                            <img src="img/function.png" size="20" v-if=" type == 0 ">
                        </template>
                    </lit-table-column>
                    <lit-table-column width="1fr" title="" data-index="symbolName" key="symbolName"  align="flex-start"></lit-table-column>
                </lit-table>
            </div>
        </div>
     </selector>
    <selector id='show_chart'>
        <tab-framechart id='framechart' style='width: 100%;height: auto'> </tab-framechart>
    </selector>
    <tab-pane-filter id="filter" icon tree></tab-pane-filter>
    <tab-native-data-modal style="display:none;"/>`;
    }
}

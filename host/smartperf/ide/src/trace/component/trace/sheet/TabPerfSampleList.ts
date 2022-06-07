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
import {SelectionParam} from "../../../bean/BoxSelection.js";
import {perfDataQuery} from "../../hiperf/PerfDataQuery.js";
import {queryPerfProcess, queryPerfSampleCallChain, queryPerfSampleListByTimeRange} from "../../../database/SqlLite.js";
import {PerfCallChain, PerfFile, PerfSample, PerfStack, PerfThread} from "../../../bean/PerfProfile.js";
import {Utils} from "../base/Utils.js";

@element('tabpane-perf-sample')
export class TabPanePerfSample extends BaseElement {
    private tbl: LitTable | null | undefined;
    private tblData: LitTable | null | undefined;
    private source: Array<PerfSample> = [];
    private processMap: Map<number, PerfThread> = new Map<number, PerfThread>();

    set data(val: SelectionParam | null | undefined) {
        // @ts-ignore
        this.tbl?.shadowRoot?.querySelector(".table")?.style?.height = (this.parentElement!.clientHeight - 40) + "px";
        this.tbl!.recycleDataSource = [];
        // @ts-ignore
        this.tblData?.shadowRoot?.querySelector(".table")?.style?.height = (this.parentElement!.clientHeight - 25) + "px";
        this.tblData!.recycleDataSource = [];
        if (val) {
            Promise.all([queryPerfProcess(),
                queryPerfSampleListByTimeRange(val.leftNs, val.rightNs, val.perfAll ? [] : val.perfCpus, val.perfAll ? [] : val.perfProcess, val.perfAll ? [] : val.perfThread)
            ]).then((results) => {
                let processes = results[0] as Array<PerfThread>;
                let samples = results[1] as Array<PerfSample>;
                this.processMap.clear();
                for (let process of processes) {
                    this.processMap.set(process.pid, process)
                }
                for (let sample of samples) {
                    let process = this.processMap.get(sample.pid);
                    sample.processName = process == null || process == undefined ? `Process(${sample.pid})` : `${process!.processName}(${sample.pid})`;
                    sample.threadName = sample.threadName == null || sample.threadName == undefined ? `Thread(${sample.tid})` : `${sample.threadName}(${sample.tid})`;
                    sample.coreName = `CPU ${sample.core}`;
                    sample.timeString = Utils.getTimeString(sample.time);
                    sample.state = sample.state == "-" ? sample.state : Utils.getEndState(sample.state) ?? sample.state;
                    let arr = (perfDataQuery.callChainData[sample.sampleId] ?? []) as Array<PerfCallChain>;
                    let calls = arr.slice(0, arr.length - 2)
                    let last = calls[calls.length - 1];
                    sample.depth = calls.length;
                    sample.fileId = last.fileId;
                    sample.symbolId = last.symbolId;
                    sample.addr = last.vaddrInFile.toString();
                    let files = (perfDataQuery.filesData[sample.fileId] ?? []) as Array<PerfFile>;
                    sample.backtrace = [];
                    if (sample.symbolId == -1 || sample.symbolId > files.length - 1) {
                        sample.backtrace.push(`0x${sample.addr}`)
                    } else {
                        sample.backtrace.push(files[sample.symbolId].symbol)
                    }
                    sample.backtrace.push(`(${sample.depth} other frames)`);
                }
                this.source = samples;
                this.tbl!.recycleDataSource = this.source;
            })

        }

    }

    setRightTableData(sample: PerfSample) {
        queryPerfSampleCallChain(sample.sampleId).then((result) => {
            let stackArr: Array<PerfStack> = [];
            for (let perfCallChain of result) {
                let stack = new PerfStack();
                stack.fileId = perfCallChain.fileId;
                let files = (perfDataQuery.filesData[stack.fileId] ?? []) as Array<PerfFile>;
                if (perfCallChain.symbolId == -1 || perfCallChain.symbolId > files.length - 1) {
                    stack.symbol = `0x${perfCallChain.vaddrInFile}`
                    stack.path = "";
                } else {
                    stack.symbol = files[perfCallChain.symbolId].symbol
                    stack.path = files[perfCallChain.symbolId].path
                }
                stack.type = (stack.path.endsWith(".so.1") || stack.path.endsWith(".dll") || stack.path.endsWith(".so")) ? 0 : 1;
                stackArr.push(stack)
            }
            this.tblData!.recycleDataSource = stackArr
        })
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-perf-sample');
        this.tblData = this.shadowRoot?.querySelector<LitTable>('#tb-stack-data');
        this.tbl!.addEventListener('row-click', (e) => {
            // @ts-ignore
            let data = (e.detail.data as PerfSample);
            this.setRightTableData(data);
        });
        new ResizeObserver((entries) => {
            if (this.parentElement?.clientHeight != 0) {
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 40) + "px"
                // @ts-ignore
                this.tblData?.shadowRoot.querySelector(".table").style.height = (this.parentElement.clientHeight - 25) + "px"
                this.tbl?.reMeauseHeight()
                this.tblData?.reMeauseHeight();
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
<div style="display: flex;flex-direction: row">
    <div style="width: 65%">
        <lit-table id="tb-perf-sample" style="height: auto">
            <lit-table-column order width="1fr" title="Sample Time" data-index="timeString" key="timeString" align="flex-start" ></lit-table-column>
            <lit-table-column order width="70px" title="Core" data-index="coreName" key="coreName" align="flex-start" ></lit-table-column>
            <lit-table-column order width="1fr" title="Process" data-index="processName" key="processName" align="flex-start" ></lit-table-column>
            <lit-table-column order width="1fr" title="Thread" data-index="threadName" key="threadName" align="flex-start" ></lit-table-column>
            <lit-table-column order width="1fr" title="State" data-index="state" key="state" align="flex-start" ></lit-table-column>
            <lit-table-column order width="1fr" title="Backtrace" data-index="backtrace" key="backtrace" align="flex-start" >
                <template>
                    <div>
                        <span>{{backtrace[0]}}</span>
                        <span>⬅</span>
                        <span style="color: #565656"> {{backtrace[1]}}</span>
                    </div>
                </template>
            </lit-table-column>
        </lit-table>
<!--        <tab-pane-filter id="filter" first></tab-pane-filter>-->
    </div>
    <div style="width: 35%">
        <lit-table id="tb-stack-data" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)">
            <lit-table-column width="60px" title="" data-index="type" key="type"  align="flex-start" >
                <template>
                    <img src="img/library.png" size="20" v-if=" type == 1 ">
                    <img src="img/function.png" size="20" v-if=" type == 0 ">
                </template>
            </lit-table-column>
            <lit-table-column width="1fr" title="" data-index="symbol" key="symbol"  align="flex-start"></lit-table-column>
        </lit-table>
    </div>
</div>
        `;
    }
}

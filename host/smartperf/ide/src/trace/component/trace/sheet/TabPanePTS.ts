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
import {SelectionParam} from "../../../bean/BoxSelection.js";
import {
    getTabStatesGroupByProcess,
    getTabStatesGroupByProcessThread,
    getTabStatesGroupByStatePidTid
} from "../../../database/SqlLite.js";
import {StateProcessThread} from "../../../bean/StateProcessThread.js";
import {Utils} from "../base/Utils.js";

@element('tabpane-pts')
export class TabPanePTS extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;

    set data(val: SelectionParam | any) {
        this.range!.textContent = "Selected range: " + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + " ms"
        Promise.all([
            getTabStatesGroupByProcess(val.leftNs, val.rightNs),
            getTabStatesGroupByProcessThread(val.leftNs, val.rightNs),
            getTabStatesGroupByStatePidTid(val.leftNs, val.rightNs)]).then((values) => {
            let processes = values[0];
            processes.map((spt) => {
                spt.id = "p" + spt.processId
                spt.title = (spt.process ?? "Process") + "(" + spt.processId + ")";
            });
            let threadMap = this.groupByProcessToMap(values[1]);
            let stateMap = this.groupByProcessThreadToMap(values[2]);
            for (let process of processes) {
                let threads = threadMap.get(process.processId);
                if (threads != undefined) {
                    threads!.map((spt) => {
                        spt.id = "p" + spt.processId + "_" + "t" + spt.threadId;
                        spt.pid = "p" + spt.processId;
                        spt.title = (spt.thread ?? "Thread") + "(" + spt.threadId + ")"
                    })
                }
                process.children = threads ?? [];
                let map = stateMap.get(process.processId);
                for (let thread of threads!) {
                    let states = map!.get(thread.threadId);
                    states!.map((spt) => {
                        spt.id = "p" + spt.processId + "_" + "t" + spt.threadId + "_" + (spt.state == "R+" ? "RP" : spt.state)
                        spt.pid = "p" + spt.processId + "_" + "t" + spt.threadId;
                        spt.title = Utils.getEndState(spt.state)
                    })
                    thread.children = states ?? [];
                }
            }
            this.tbl!.dataSource = processes;
        })
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-states');
        this.range = this.shadowRoot?.querySelector('#time-range')
    }

    groupByThreadToMap(arr: Array<StateProcessThread>): Map<number, Array<StateProcessThread>> {
        let map = new Map<number, Array<StateProcessThread>>();
        for (let spt of arr) {
            if (map.has(spt.threadId)) {
                map.get(spt.threadId)!.push(spt);
            } else {
                let list: Array<StateProcessThread> = [];
                list.push(spt);
                map.set(spt.threadId, list);
            }
        }
        return map;
    }

    groupByProcessToMap(arr: Array<StateProcessThread>): Map<number, Array<StateProcessThread>> {
        let map = new Map<number, Array<StateProcessThread>>();
        for (let spt of arr) {
            if (map.has(spt.processId)) {
                map.get(spt.processId)!.push(spt);
            } else {
                let list: Array<StateProcessThread> = [];
                list.push(spt);
                map.set(spt.processId, list);
            }
        }
        return map;
    }

    groupByProcessThreadToMap(arr: Array<StateProcessThread>): Map<number, Map<number, Array<StateProcessThread>>> {
        let map = new Map<number, Map<number, Array<StateProcessThread>>>();
        let processMap = this.groupByProcessToMap(arr);
        for (let key of processMap.keys()) {
            let threadMap = this.groupByThreadToMap(processMap.get(key)!)
            map.set(key, threadMap);
        }
        return map;
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
<lit-table id="tb-states" style="height: auto" tree>
    <lit-table-column width="27%" title="Process/Thread/State" data-index="title" key="title" align="flex-start"></lit-table-column>
    <lit-table-column width="1fr" title="Count" data-index="count" key="count" align="flex-start" ></lit-table-column>
    <lit-table-column width="1fr" title="Duration(ns)" data-index="wallDuration" key="wallDuration" align="flex-start" ></lit-table-column>
    <lit-table-column width="1fr" title="Min Duration(ns)" data-index="minDuration" key="minDuration" align="flex-start" ></lit-table-column>
    <lit-table-column width="1fr" title="Avg Duration(ns)" data-index="avgDuration" key="avgDuration" align="flex-start" ></lit-table-column>
    <lit-table-column width="1fr" title="Max Duration(ns)" data-index="maxDuration" key="maxDuration" align="flex-start" ></lit-table-column>
</lit-table>
        `;
    }

}
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
    getTabStatesGroupByState,
    getTabStatesGroupByStatePid,
    getTabStatesGroupByStatePidTid
} from "../../../database/SqlLite.js";
import {StateProcessThread} from "../../../bean/StateProcessThread.js";
import {Utils} from "../base/Utils.js";

@element('tabpane-thread-switch')
export class TabPaneThreadSwitch extends BaseElement {
    private tbl: LitTable | null | undefined;
    private range: HTMLLabelElement | null | undefined;

    set data(val: SelectionParam | any) {
        this.range!.textContent = "Selected range: " + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + " ms"
        Promise.all([
            getTabStatesGroupByState(val.leftNs, val.rightNs),
            getTabStatesGroupByStatePid(val.leftNs, val.rightNs),
            getTabStatesGroupByStatePidTid(val.leftNs, val.rightNs)]).then((values) => {
            let states = values[0];
            states.map((spt) => {
                spt.id = (spt.state == "R+" ? "RP" : spt.state)
                // @ts-ignore
                spt.title = Utils.getEndState(spt.state);
            });
            let processMap = this.groupByStateToMap(values[1]);
            let threadMap = this.groupByStateProcessToMap(values[2]);
            for (let state of states) {
                let processes = processMap.get(state.state);
                processes!.map((spt) => {
                    spt.id = (spt.state == "R+" ? "RP" : spt.state) + "_" + spt.processId;
                    spt.pid = (spt.state == "R+" ? "RP" : spt.state);
                    spt.title = (spt.process ?? "Process") + "(" + spt.processId + ")"
                })
                state.children = processes ?? [];
                let map = threadMap.get(state.state);
                for (let process of processes!) {
                    let threads = map!.get(process.processId);
                    threads!.map((spt) => {
                        spt.id = (spt.state == "R+" ? "RP" : spt.state) + "_" + spt.processId + "_" + spt.threadId
                        spt.pid = (spt.state == "R+" ? "RP" : spt.state) + "_" + spt.processId
                        spt.title = (spt.thread ?? "Thread") + "(" + spt.threadId + ")"
                    })
                    process.children = threads ?? [];
                }
            }
            this.tbl!.dataSource = states;
        })
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-ts');
        this.range = this.shadowRoot?.querySelector('#time-range')
    }

    groupByStateToMap(arr: Array<StateProcessThread>): Map<string, Array<StateProcessThread>> {
        let map = new Map<string, Array<StateProcessThread>>();
        for (let spt of arr) {
            if (map.has(spt.state)) {
                map.get(spt.state)!.push(spt);
            } else {
                let list: Array<StateProcessThread> = [];
                list.push(spt);
                map.set(spt.state, list);
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

    groupByStateProcessToMap(arr: Array<StateProcessThread>): Map<string, Map<number, Array<StateProcessThread>>> {
        let map = new Map<string, Map<number, Array<StateProcessThread>>>();
        let stateMap = this.groupByStateToMap(arr);
        for (let key of stateMap.keys()) {
            let processMap = this.groupByProcessToMap(stateMap.get(key)!)
            map.set(key, processMap);
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
<lit-table id="tb-ts" style="height: auto" tree>
    <lit-table-column width="500px" title="Event/Process/Thread" data-index="title" key="title" align="flex-start"></lit-table-column>
    <lit-table-column width="1fr" title="Count" data-index="count" key="count" align="flex-start" ></lit-table-column>
</lit-table>
        `;
    }

}
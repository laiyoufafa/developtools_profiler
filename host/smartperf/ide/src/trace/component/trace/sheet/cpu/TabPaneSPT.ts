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

import { BaseElement, element } from '../../../../../base-ui/BaseElement.js';
import { LitTable } from '../../../../../base-ui/table/lit-table.js';
import { SelectionParam } from '../../../../bean/BoxSelection.js';
import { getStatesProcessThreadDataByRange } from '../../../../database/SqlLite.js';
import { SPT, StateProcessThread } from '../../../../bean/StateProcessThread.js';
import { Utils } from '../../base/Utils.js';
import { SpSystemTrace } from '../../../SpSystemTrace.js';
import { resizeObserver } from "../SheetUtils.js";

@element('tabpane-spt')
export class TabPaneSPT extends BaseElement {
  private sptTbl: LitTable | null | undefined;
  private range: HTMLLabelElement | null | undefined;
  private loadDataInCache: boolean = true;
  private selectionParam: SelectionParam | null | undefined;

  set data(sptValue: SelectionParam | any) {
    if (sptValue == this.selectionParam) {
      return;
    }
    this.selectionParam = sptValue;
    // @ts-ignore
    this.sptTbl?.shadowRoot?.querySelector('.table').style.height = this.parentElement!.clientHeight - 45 + 'px';
    this.range!.textContent =
      'Selected range: ' + parseFloat(((sptValue.rightNs - sptValue.leftNs) / 1000000.0).toFixed(5)) + ' ms';
    if (this.loadDataInCache) {
      this.getDataBySPT(sptValue.leftNs, sptValue.rightNs, SpSystemTrace.SPT_DATA);
    } else {
      this.queryDataByDB(sptValue);
    }
  }

  initElements(): void {
    this.sptTbl = this.shadowRoot?.querySelector<LitTable>('#spt-tbl');
    this.range = this.shadowRoot?.querySelector('#spt-time-range');
  }

  connectedCallback() {
    super.connectedCallback();
    resizeObserver(this.parentElement!, this.sptTbl!)
  }

  getDataBySPT(leftNs: number, rightNs: number, source: Array<SPT>) {
    this.sptTbl!.loading = true;
    let statesMap: Map<string, StateProcessThread> = new Map<string, StateProcessThread>();
    let spMap: Map<string, StateProcessThread> = new Map<string, StateProcessThread>();
    let sptMap: Map<string, StateProcessThread> = new Map<string, StateProcessThread>();
    source.map((d) => {
      if (!(d.end_ts < leftNs || d.start_ts > rightNs)) {
        if (statesMap.has(d.state)) {
          let sptStateMapObj = statesMap.get(d.state);
          sptStateMapObj!.count++;
          sptStateMapObj!.wallDuration += d.dur;
          sptStateMapObj!.avgDuration = (sptStateMapObj!.wallDuration / sptStateMapObj!.count).toFixed(2);
          if (d.dur > sptStateMapObj!.maxDuration) {
            sptStateMapObj!.maxDuration = d.dur;
          }
          if (d.dur < sptStateMapObj!.minDuration) {
            sptStateMapObj!.minDuration = d.dur;
          }
        } else {
          let sptStateMapObj = new StateProcessThread();
          sptStateMapObj.id = d.state == 'R+' ? 'RP' : d.state;
          sptStateMapObj.title = Utils.getEndState(d.state);
          sptStateMapObj.state = d.state;
          sptStateMapObj.minDuration = d.dur;
          sptStateMapObj.maxDuration = d.dur;
          sptStateMapObj.count = 1;
          sptStateMapObj.avgDuration = d.dur + '';
          sptStateMapObj.wallDuration = d.dur;
          statesMap.set(d.state, sptStateMapObj);
        }
        if (spMap.has(d.state + '_' + d.processId)) {
          let sptSpMapObj = spMap.get(d.state + '_' + d.processId);
          sptSpMapObj!.count++;
          sptSpMapObj!.wallDuration += d.dur;
          sptSpMapObj!.avgDuration = (sptSpMapObj!.wallDuration / sptSpMapObj!.count).toFixed(2);
          if (d.dur > sptSpMapObj!.maxDuration) {
            sptSpMapObj!.maxDuration = d.dur;
          }
          if (d.dur < sptSpMapObj!.minDuration) {
            sptSpMapObj!.minDuration = d.dur;
          }
        } else {
          let sptSpMapObj = new StateProcessThread();
          sptSpMapObj.id = (d.state == 'R+' ? 'RP' : d.state) + '_' + d.processId;
          sptSpMapObj.pid = d.state == 'R+' ? 'RP' : d.state;
          sptSpMapObj.title = (d.process == null || d.process == '' ? 'Process' : d.process) + '(' + d.processId + ')';
          sptSpMapObj.processId = d.processId;
          sptSpMapObj.process = d.process;
          sptSpMapObj.state = d.state;
          sptSpMapObj.minDuration = d.dur;
          sptSpMapObj.maxDuration = d.dur;
          sptSpMapObj.count = 1;
          sptSpMapObj.avgDuration = d.dur + '';
          sptSpMapObj.wallDuration = d.dur;
          spMap.set(d.state + '_' + d.processId, sptSpMapObj);
        }
        if (sptMap.has(d.state + '_' + d.processId + '_' + d.threadId)) {
          let sptMapObject = sptMap.get(d.state + '_' + d.processId + '_' + d.threadId);
          sptMapObject!.count++;
          sptMapObject!.wallDuration += d.dur;
          sptMapObject!.avgDuration = (sptMapObject!.wallDuration / sptMapObject!.count).toFixed(2);
          if (d.dur > sptMapObject!.maxDuration) {
            sptMapObject!.maxDuration = d.dur;
          }
          if (d.dur < sptMapObject!.minDuration) {
            sptMapObject!.minDuration = d.dur;
          }
        } else {
          let sptMapObject = new StateProcessThread();
          sptMapObject.id = (d.state == 'R+' ? 'RP' : d.state) + '_' + d.processId + '_' + d.threadId;
          sptMapObject.pid = (d.state == 'R+' ? 'RP' : d.state) + '_' + d.processId;
          sptMapObject.title = (d.thread == null || d.thread == '' ? 'Thread' : d.thread) + '(' + d.threadId + ')';
          sptMapObject.processId = d.processId;
          sptMapObject.process = d.process;
          sptMapObject.thread = d.thread;
          sptMapObject.threadId = d.threadId;
          sptMapObject.state = d.state;
          sptMapObject.minDuration = d.dur;
          sptMapObject.maxDuration = d.dur;
          sptMapObject.count = 1;
          sptMapObject.avgDuration = d.dur + '';
          sptMapObject.wallDuration = d.dur;
          sptMap.set(d.state + '_' + d.processId + '_' + d.threadId, sptMapObject);
        }
      }
    });
    let sptArr: Array<StateProcessThread> = [];
    for (let key of statesMap.keys()) {
      let stateValue = statesMap.get(key);
      stateValue!.children = [];
      for (let spKey of spMap.keys()) {
        if (spKey.startsWith(key + '_')) {
          let sp = spMap.get(spKey);
          sp!.children = [];
          for (let stKey of sptMap.keys()) {
            if (stKey.startsWith(spKey + '_')) {
              let spt = sptMap.get(stKey);
              sp!.children.push(spt!);
            }
          }
          stateValue!.children.push(sp!);
        }
      }
      sptArr.push(stateValue!);
    }
    this.sptTbl!.loading = false;
    this.sptTbl!.recycleDataSource = sptArr;
  }

  queryDataByDB(sptParam: SelectionParam | any) {
    getStatesProcessThreadDataByRange(sptParam.leftNs, sptParam.rightNs).then((result) => {
      this.getDataBySPT(sptParam.leftNs, sptParam.rightNs, result);
    });
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
        <label id="spt-time-range" style="width: 100%;height: 20px;text-align: end;font-size: 10pt;margin-bottom: 5px">Selected range:0.0 ms</label>
        <lit-table id="spt-tbl" style="height: auto" tree>
            <lit-table-column class="spt-column" width="27%" data-index="title" key="title" align="flex-start" title="State/Process/Thread">
            </lit-table-column>
            <lit-table-column class="spt-column" width="1fr" data-index="count" key="count" align="flex-start" title="Count">
            </lit-table-column>
            <lit-table-column class="spt-column" width="1fr" data-index="wallDuration" key="wallDuration" align="flex-start" title="Duration(ns)">
            </lit-table-column>
            <lit-table-column class="spt-column" width="1fr" data-index="minDuration" key="minDuration" align="flex-start" title="Min Duration(ns)">
            </lit-table-column>
            <lit-table-column class="spt-column" width="1fr" data-index="avgDuration" key="avgDuration" align="flex-start" title="Avg Duration(ns)">
            </lit-table-column>
            <lit-table-column class="spt-column" width="1fr" data-index="maxDuration" key="maxDuration" align="flex-start" title="Max Duration(ns)">
            </lit-table-column>
        </lit-table>
        `;
  }
}

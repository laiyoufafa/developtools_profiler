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

@element('tabpane-pts')
export class TabPanePTS extends BaseElement {
  private ptsTbl: LitTable | null | undefined;
  private ptsRange: HTMLLabelElement | null | undefined;
  private loadDataInCache: boolean = true;
  private selectionParam: SelectionParam | null | undefined;

  set data(ptsValue: SelectionParam | any) {
    if (ptsValue == this.selectionParam) {
      return;
    }
    this.selectionParam = ptsValue;
    this.ptsRange!.textContent =
      'Selected range: ' + parseFloat(((ptsValue.rightNs - ptsValue.leftNs) / 1000000.0).toFixed(5)) + ' ms';
    if (this.loadDataInCache) {
      this.getDataBySPT(ptsValue.leftNs, ptsValue.rightNs, SpSystemTrace.SPT_DATA);
    } else {
      this.queryDataByDB(ptsValue);
    }
  }

  initElements(): void {
    this.ptsTbl = this.shadowRoot?.querySelector<LitTable>('#pts-tbl');
    this.ptsRange = this.shadowRoot?.querySelector('#pts-time-range');
  }

  connectedCallback() {
    super.connectedCallback();
    resizeObserver(this.parentElement!, this.ptsTbl!)
  }

  queryDataByDB(ptsVal: SelectionParam | any) {
    getStatesProcessThreadDataByRange(ptsVal.leftNs, ptsVal.rightNs).then((result) => {
      this.getDataBySPT(ptsVal.leftNs, ptsVal.rightNs, result);
    });
  }

  getDataBySPT(ptsLeftNs: number, ptsRightNs: number, ptsSource: Array<SPT>) {
    this.ptsTbl!.loading = true;
    let pMap: Map<string, StateProcessThread> = new Map<string, StateProcessThread>();
    let ptMap: Map<string, StateProcessThread> = new Map<string, StateProcessThread>();
    let ptsMap: Map<string, StateProcessThread> = new Map<string, StateProcessThread>();
    ptsSource.map((d) => {
      if (!(d.end_ts < ptsLeftNs || d.start_ts > ptsRightNs)) {
        if (pMap.has(d.processId + '')) {
          let ptsPMapObj = pMap.get(d.processId + '');
          ptsPMapObj!.count++;
          ptsPMapObj!.wallDuration += d.dur;
          ptsPMapObj!.avgDuration = (ptsPMapObj!.wallDuration / ptsPMapObj!.count).toFixed(2);
          if (d.dur > ptsPMapObj!.maxDuration) {
            ptsPMapObj!.maxDuration = d.dur;
          }
          if (d.dur < ptsPMapObj!.minDuration) {
            ptsPMapObj!.minDuration = d.dur;
          }
        } else {
          let ptsPMapObj = new StateProcessThread();
          ptsPMapObj.id = 'p' + d.processId;
          ptsPMapObj.title = (d.process == null || d.process == '' ? 'Process' : d.process) + '(' + d.processId + ')';
          ptsPMapObj.process = d.process;
          ptsPMapObj.processId = d.processId;
          ptsPMapObj.minDuration = d.dur;
          ptsPMapObj.maxDuration = d.dur;
          ptsPMapObj.count = 1;
          ptsPMapObj.avgDuration = d.dur + '';
          ptsPMapObj.wallDuration = d.dur;
          pMap.set(d.processId + '', ptsPMapObj);
        }
        if (ptMap.has(d.processId + '_' + d.threadId)) {
          let ptsPtMapObj = ptMap.get(d.processId + '_' + d.threadId);
          ptsPtMapObj!.count++;
          ptsPtMapObj!.wallDuration += d.dur;
          ptsPtMapObj!.avgDuration = (ptsPtMapObj!.wallDuration / ptsPtMapObj!.count).toFixed(2);
          if (d.dur > ptsPtMapObj!.maxDuration) {
            ptsPtMapObj!.maxDuration = d.dur;
          }
          if (d.dur < ptsPtMapObj!.minDuration) {
            ptsPtMapObj!.minDuration = d.dur;
          }
        } else {
          let ptsPtMapObj = new StateProcessThread();
          ptsPtMapObj.id = 'p' + d.processId + '_' + 't' + d.threadId;
          ptsPtMapObj.pid = 'p' + d.processId;
          ptsPtMapObj.title = (d.thread == null || d.thread == '' ? 'Thread' : d.thread) + '(' + d.threadId + ')';
          ptsPtMapObj.processId = d.processId;
          ptsPtMapObj.process = d.process;
          ptsPtMapObj.thread = d.thread;
          ptsPtMapObj.threadId = d.threadId;
          ptsPtMapObj.minDuration = d.dur;
          ptsPtMapObj.maxDuration = d.dur;
          ptsPtMapObj.count = 1;
          ptsPtMapObj.avgDuration = d.dur + '';
          ptsPtMapObj.wallDuration = d.dur;
          ptMap.set(d.processId + '_' + d.threadId, ptsPtMapObj);
        }
        if (ptsMap.has(d.processId + '_' + d.threadId + '_' + d.state)) {
          let ptsPtsMapObj = ptsMap.get(d.processId + '_' + d.threadId + '_' + d.state);
          ptsPtsMapObj!.count++;
          ptsPtsMapObj!.wallDuration += d.dur;
          ptsPtsMapObj!.avgDuration = (ptsPtsMapObj!.wallDuration / ptsPtsMapObj!.count).toFixed(2);
          if (d.dur > ptsPtsMapObj!.maxDuration) {
            ptsPtsMapObj!.maxDuration = d.dur;
          }
          if (d.dur < ptsPtsMapObj!.minDuration) {
            ptsPtsMapObj!.minDuration = d.dur;
          }
        } else {
          let ptsPtsMapObj = new StateProcessThread();
          ptsPtsMapObj.id = 'p' + d.processId + '_' + 't' + d.threadId + '_' + (d.state == 'R+' ? 'RP' : d.state);
          ptsPtsMapObj.pid = 'p' + d.processId + '_' + 't' + d.threadId;
          ptsPtsMapObj.title = Utils.getEndState(d.state);
          ptsPtsMapObj.processId = d.processId;
          ptsPtsMapObj.process = d.process;
          ptsPtsMapObj.thread = d.thread;
          ptsPtsMapObj.threadId = d.threadId;
          ptsPtsMapObj.state = d.state;
          ptsPtsMapObj.minDuration = d.dur;
          ptsPtsMapObj.maxDuration = d.dur;
          ptsPtsMapObj.count = 1;
          ptsPtsMapObj.avgDuration = d.dur + '';
          ptsPtsMapObj.wallDuration = d.dur;
          ptsMap.set(d.processId + '_' + d.threadId + '_' + d.state, ptsPtsMapObj);
        }
      }
    });
    let ptsArr: Array<StateProcessThread> = [];
    for (let key of pMap.keys()) {
      let ptsValues = pMap.get(key);
      ptsValues!.children = [];
      for (let itemKey of ptMap.keys()) {
        if (itemKey.startsWith(key + '_')) {
          let sp = ptMap.get(itemKey);
          sp!.children = [];
          for (let keySt of ptsMap.keys()) {
            if (keySt.startsWith(itemKey + '_')) {
              let spt = ptsMap.get(keySt);
              sp!.children.push(spt!);
            }
          }
          ptsValues!.children.push(sp!);
        }
      }
      ptsArr.push(ptsValues!);
    }
    this.ptsTbl!.loading = false;
    this.ptsTbl!.recycleDataSource = ptsArr;
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
        <label id="pts-time-range" style="width: 100%;height: 20px;text-align: end;font-size: 10pt;margin-bottom: 5px">Selected range:0.0 ms</label>
        <lit-table id="pts-tbl" style="height: auto" tree>
            <lit-table-column class="pts-column" title="Process/Thread/State" data-index="title" key="title" align="flex-start" width="27%">
            </lit-table-column>
            <lit-table-column class="pts-column" title="Count" data-index="count" key="count" align="flex-start" width="1fr">
            </lit-table-column>
            <lit-table-column class="pts-column" title="Duration(ns)" data-index="wallDuration" key="wallDuration" align="flex-start" width="1fr">
            </lit-table-column>
            <lit-table-column class="pts-column" title="Min Duration(ns)" data-index="minDuration" key="minDuration" align="flex-start" width="1fr">
            </lit-table-column>
            <lit-table-column class="pts-column" title="Avg Duration(ns)" data-index="avgDuration" key="avgDuration" align="flex-start" width="1fr">
            </lit-table-column>
            <lit-table-column class="pts-column" title="Max Duration(ns)" data-index="maxDuration" key="maxDuration" align="flex-start" width="1fr">
            </lit-table-column>
        </lit-table>
        `;
  }
}

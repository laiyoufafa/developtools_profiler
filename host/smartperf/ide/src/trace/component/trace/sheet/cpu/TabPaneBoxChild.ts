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
import { BoxJumpParam, SelectionData } from '../../../../bean/BoxSelection.js';
import { getTabBoxChildData } from '../../../../database/SqlLite.js';
import { Utils } from '../../base/Utils.js';
import { SpSystemTrace } from '../../../SpSystemTrace.js';
import { SPTChild } from '../../../../bean/StateProcessThread.js';
import { TraceRow } from '../../base/TraceRow.js';
import { resizeObserver } from "../SheetUtils.js";

@element('tabpane-box-child')
export class TabPaneBoxChild extends BaseElement {
  private boxChildTbl: LitTable | null | undefined;
  private boxChildRange: HTMLLabelElement | null | undefined;
  private boxChildSource: Array<SPTChild> = [];
  private loadDataInCache: boolean = false;

  set data(boxChildValue: BoxJumpParam) {
    // @ts-ignore
    this.boxChildTbl?.shadowRoot?.querySelector('.table')?.style?.height = this.parentElement!.clientHeight - 45 + 'px';
    this.boxChildRange!.textContent =
      'Selected range: ' + parseFloat(((boxChildValue.rightNs - boxChildValue.leftNs) / 1000000.0).toFixed(5)) + ' ms';
    if (boxChildValue.state != null && boxChildValue.state != undefined && boxChildValue.processId && boxChildValue.threadId) {
      this.boxChildTbl!.recycleDataSource = [];
      if (this.loadDataInCache) {
        this.getDataByCache(boxChildValue).then((arr) => {
          this.boxChildSource = arr;
          // @ts-ignore
          this.boxChildTbl?.recycleDataSource = arr;
        });
      } else {
        this.getDataByDB(boxChildValue);
      }
    }
  }

  initElements(): void {
    this.boxChildTbl = this.shadowRoot?.querySelector<LitTable>('#tb-cpu-thread');
    this.boxChildRange = this.shadowRoot?.querySelector('#time-range');
    this.boxChildTbl!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail);
    });
  }

  connectedCallback() {
    super.connectedCallback();
    resizeObserver(this.parentElement!, this.boxChildTbl!)
  }

  getDataByDB(val: BoxJumpParam) {
    getTabBoxChildData(val.leftNs, val.rightNs, val.state, val.processId, val.threadId).then((result) => {
      if (result.length != null && result.length > 0) {
        result.map((e) => {
          e.startTime = Utils.getTimeString(e.startNs);
          e.absoluteTime = ((window as any).recordStartNS + e.startNs) / 1000000000;
          e.state = Utils.getEndState(e.state)!;
          e.prior = e.priority == undefined || e.priority == null ? '-' : e.priority + '';
          e.core = e.cpu == undefined || e.cpu == null ? '-' : 'CPU' + e.cpu;
          e.processName =
            (e.process == undefined || e.process == null ? 'process' : e.process) + '(' + e.processId + ')';
          e.threadName = (e.thread == undefined || e.thread == null ? 'thread' : e.thread) + '(' + e.threadId + ')';
          e.note = '-';
        });
        this.boxChildSource = result;
        // @ts-ignore
        this.boxChildTbl?.recycleDataSource = result;
      } else {
        this.boxChildSource = [];
        // @ts-ignore
        this.boxChildTbl?.recycleDataSource = [];
      }
    });
  }

  getDataByCache(val: BoxJumpParam): Promise<Array<SPTChild>> {
    return new Promise<Array<SPTChild>>((resolve, reject) => {
      let arr: Array<SPTChild> = [];
      SpSystemTrace.SPT_DATA.map((spt) => {
        let b1 = val.state != undefined && val.state != '' ? spt.state == val.state : true;
        let b2 = val.processId != undefined && val.processId != -1 ? spt.processId == val.processId : true;
        let b3 = val.threadId != undefined && val.threadId != -1 ? spt.threadId == val.threadId : true;
        if (!(spt.end_ts < val.leftNs || spt.start_ts > val.rightNs) && b1 && b2 && b3) {
          let sptChild = new SPTChild();
          sptChild.startTime = Utils.getTimeString(spt.start_ts);
          sptChild.absoluteTime = ((window as any).recordStartNS + spt.start_ts) / 1000000000;
          sptChild.startNs = spt.start_ts;
          sptChild.state = Utils.getEndState(spt.state)!;
          sptChild.prior = spt.priority == undefined || spt.priority == null ? '-' : spt.priority + '';
          sptChild.core = spt.cpu == undefined || spt.cpu == null ? '-' : 'CPU' + spt.cpu;
          sptChild.processName =
            (spt.process == undefined || spt.process == null || spt.process == '' ? 'process' : spt.process) +
            '(' +
            spt.processId +
            ')';
          sptChild.threadName =
            (spt.thread == undefined || spt.thread == null || spt.thread == '' ? 'thread' : spt.thread) +
            '(' +
            spt.threadId +
            ')';
          arr.push(sptChild);
        }
      });
      resolve(arr);
    });
  }

  initHtml(): string {
    return `
        <style>
        .box-child-label{
          text-align: end;
          width: 100%;
          height: 20px;
        }
        :host{
            padding: 10px 10px;
            display: flex;
            flex-direction: column;
        }
        </style>
        <label id="time-range" class="box-child-label" style="font-size: 10pt;margin-bottom: 5px">Selected range:0.0 ms</label>
        <lit-table id="tb-cpu-thread" style="height: auto">
            <lit-table-column order title="StartTime(Relative)" width="15%" data-index="startTime" key="startTime" align="flex-start" order >
            </lit-table-column>
            <lit-table-column order title="StartTime(Absolute)" width="15%" data-index="absoluteTime" key="absoluteTime" align="flex-start" order >
            </lit-table-column>
            <lit-table-column order width="20%" data-index="processName" key="processName" title="Process" align="flex-start" order >
            </lit-table-column>
            <lit-table-column order width="20%" data-index="threadName" key="threadName" align="flex-start" order title="Thread">
            </lit-table-column>
            <lit-table-column order width="1fr" data-index="state" key="state" align="flex-start" order title="State">
            </lit-table-column>
            <lit-table-column order width="1fr"data-index="core"  title="Core" key="core" align="flex-start" order >
            </lit-table-column>
            <lit-table-column order width="1fr" data-index="prior" title="Priority" key="prior" align="flex-start" order >
            </lit-table-column>
            <lit-table-column order width="1fr" data-index="note" key="note" align="flex-start" title="Note">
            </lit-table-column>
        </lit-table>
        `;
  }

  sortByColumn(detail: any) {
    // @ts-ignore
    function compare(property, sort, type) {
      return function (boxChildLeftData: SelectionData, boxChildRightData: SelectionData) {
        if (type === 'number') {
          // @ts-ignore
          return sort === 2 ? parseFloat(boxChildRightData[property]) - parseFloat(boxChildLeftData[property]) : parseFloat(boxChildLeftData[property]) - parseFloat(boxChildRightData[property]);
        } else {
          // @ts-ignore
          if (boxChildRightData[property] > boxChildLeftData[property]) {
            return sort === 2 ? 1 : -1;
          } else {
            // @ts-ignore
            if (boxChildRightData[property] == boxChildLeftData[property]) {
              return 0;
            } else {
              return sort === 2 ? -1 : 1;
            }
          }
        }
      };
    }

    // @ts-ignore
    this.boxChildSource.sort(compare(detail.key, detail.sort, 'string'));
    this.boxChildTbl!.recycleDataSource = this.boxChildSource;
  }
}

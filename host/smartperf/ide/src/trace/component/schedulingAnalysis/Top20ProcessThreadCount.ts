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

import { BaseElement, element } from '../../../base-ui/BaseElement.js';
import { LitTable } from '../../../base-ui/table/lit-table.js';
import { procedurePool } from '../../database/Procedure.js';
import { info } from '../../../log/Log.js';
import '../../../base-ui/chart/pie/LitChartPie.js';
import { LitChartPie } from '../../../base-ui/chart/pie/LitChartPie.js';
import '../../../base-ui/progress-bar/LitProgressBar.js';
import { LitProgressBar } from '../../../base-ui/progress-bar/LitProgressBar.js';
import './TableNoData.js';
import { TableNoData } from './TableNoData.js';

@element('top20-process-thread-count')
export class Top20ProcessThreadCount extends BaseElement {
  traceChange: boolean = false;
  private table: LitTable | null | undefined;
  private pie: LitChartPie | null | undefined;
  private progress: LitProgressBar | null | undefined;
  private nodata: TableNoData | null | undefined;
  private data: Array<any> = [];

  initElements(): void {
    this.nodata = this.shadowRoot!.querySelector<TableNoData>('#nodata');
    this.progress = this.shadowRoot!.querySelector<LitProgressBar>('#loading');
    this.table = this.shadowRoot!.querySelector<LitTable>('#tb-process-thread-count');
    this.pie = this.shadowRoot!.querySelector<LitChartPie>('#pie');

    this.table!.addEventListener('row-click', (evt: any) => {
      let data = evt.detail.data;
      data.isSelected = true;
      // @ts-ignore
      if ((evt.detail as any).callBack) {
        // @ts-ignore
        (evt.detail as any).callBack(true);
      }
    });

    this.table!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail);
    });
    this.table!.addEventListener('row-hover', (evt: any) => {
      if (evt.detail.data) {
        let data = evt.detail.data;
        data.isHover = true;
        if ((evt.detail as any).callBack) {
          (evt.detail as any).callBack(true);
        }
      }
      this.pie?.showHover();
    });
  }

  init() {
    if (!this.traceChange) {
      if (this.table!.recycleDataSource.length > 0) {
        this.table?.reMeauseHeight();
      }
      return;
    }
    this.traceChange = false;
    this.progress!.loading = true;
    this.queryLogicWorker('scheduling-Process ThreadCount', 'query Process Thread Count Analysis Time:', (res) => {
      this.nodata!.noData = res === undefined || res.length === 0;
      this.table!.recycleDataSource = res;
      this.data = res;
      this.table?.reMeauseHeight();
      this.pie!.config = {
        appendPadding: 10,
        data: res,
        angleField: 'threadNumber',
        colorField: 'pid',
        radius: 0.8,
        label: {
          type: 'outer',
        },
        hoverHandler: (data) => {
          if (data) {
            this.table!.setCurrentHover(data);
          } else {
            this.table!.mouseOut();
          }
        },
        tip: (obj) => {
          return `<div>
                             <div>pid:${obj.obj.pid}</div> 
                             <div>p_name:${obj.obj.pName}</div> 
                             <div>thread count:${obj.obj.threadNumber}</div> 
                        </div>
                `;
        },
        interactions: [
          {
            type: 'element-active',
          },
        ],
      };
      this.progress!.loading = false;
    });
  }

  clearData() {
    this.traceChange = true;
    this.pie!.dataSource = [];
    this.table!.recycleDataSource = [];
  }

  queryLogicWorker(option: string, log: string, handler: (res: any) => void) {
    let time = new Date().getTime();
    procedurePool.submitWithName('logic1', option, {}, undefined, handler);
    let durTime = new Date().getTime() - time;
    info(log, durTime);
  }

  sortByColumn(detail: any) {
    // @ts-ignore
    function compare(property, sort, type) {
      return function (a: any, b: any) {
        if (type === 'number') {
          // @ts-ignore
          return sort === 2
            ? parseFloat(b[property]) - parseFloat(a[property])
            : parseFloat(a[property]) - parseFloat(b[property]);
        } else {
          if (sort === 2) {
            return b[property].toString().localeCompare(a[property].toString());
          } else {
            return a[property].toString().localeCompare(b[property].toString());
          }
        }
      };
    }

    if (detail.key === 'NO' || detail.key === 'pid' || detail.key === 'threadNumber') {
      this.data.sort(compare(detail.key, detail.sort, 'number'));
    } else {
      this.data.sort(compare(detail.key, detail.sort, 'string'));
    }
    this.table!.recycleDataSource = this.data;
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
            flex: 1;
            overflow: auto ;
            border-radius: 5px;
            border: solid 1px var(--dark-border1,#e0e0e0);
            margin: 15px;
            padding: 5px 15px
        }
        .pie-chart{
            display: flex;
            box-sizing: border-box;
            width: 500px;
            height: 500px;
        }
        .root{
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: row;
            box-sizing: border-box;
        }
        </style>
        <lit-progress-bar id="loading" style="height: 1px;width: 100%" loading></lit-progress-bar>
        <table-no-data id="nodata" contentHeight="500px">
        <div class="root">
            <div style="display: flex;flex-direction: column;align-items: center">
                <div>Statistics By Thread Count</div>
                <lit-chart-pie id="pie" class="pie-chart"></lit-chart-pie>
            </div>
            <div class="tb_thread_count">
                <lit-table id="tb-process-thread-count" hideDownload style="height: auto">
                    <lit-table-column width="1fr" title="NO" data-index="NO" key="NO" align="flex-start" order></lit-table-column>
                    <lit-table-column width="1fr" title="pid" data-index="pid" key="pid" align="flex-start" order></lit-table-column>
                    <lit-table-column width="1fr" title="p_name" data-index="pName" key="pName" align="flex-start" order></lit-table-column>
                    <lit-table-column width="1fr" title="thread count" data-index="threadNumber" key="threadNumber" align="flex-start" order></lit-table-column>        
                </lit-table>
            </div>
        </div>
        </table-no-data>
        `;
  }
}

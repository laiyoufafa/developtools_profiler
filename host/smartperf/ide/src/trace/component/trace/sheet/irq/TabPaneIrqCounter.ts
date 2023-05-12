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
import { Counter, SelectionData, SelectionParam } from '../../../../bean/BoxSelection.js';
import { getTabCounters, getTabVirtualCounters } from '../../../../database/SqlLite.js';
import { Utils } from '../../base/Utils.js';

@element('tabpane-irq-counter')
export class TabPaneIrqCounter extends BaseElement {
  private tbl: LitTable | null | undefined;
  private range: HTMLLabelElement | null | undefined;
  private source: Array<SelectionData> = [];

  set data(val: SelectionParam | any) {
    //@ts-ignore
    this.tbl?.shadowRoot?.querySelector('.table')?.style?.height = this.parentElement!.clientHeight - 45 + 'px';
    this.range!.textContent =
      'Selected range: ' + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + ' ms';
    let dataSource: Array<SelectionData> = [];
    let collect = val.irqMapData;
    let sumCount = 0;
    for (let key of collect.keys()) {
      let counters = collect.get(key);
      let sd = this.createSelectCounterData(key, counters);
      sumCount += Number.parseInt(sd.count || '0');
      sd.avgDuration = Utils.getProbablyTime(sd.wallDuration / parseInt(sd.count));
      dataSource.push(sd);
    }
    this.source = dataSource;
    this.tbl!.recycleDataSource = dataSource;
  }

  initElements(): void {
    this.tbl = this.shadowRoot?.querySelector<LitTable>('#tb-counter');
    this.range = this.shadowRoot?.querySelector('#time-range');
    this.tbl!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail);
    });
  }

  connectedCallback() {
    super.connectedCallback();
    new ResizeObserver((entries) => {
      if (this.parentElement?.clientHeight != 0) {
        // @ts-ignore
        this.tbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 45 + 'px';
        this.tbl?.reMeauseHeight();
      }
    }).observe(this.parentElement!);
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
        <lit-table id="tb-counter" style="height: auto">
            <lit-table-column width="30%" title="Name" data-index="name" key="name"  align="flex-start" order>
            </lit-table-column>
            <lit-table-column width="1fr" title="Duration" data-index="wallDurationFormat" key="wallDurationFormat"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Average Duration" data-index="avgDuration" key="avgDuration"  align="flex-start" order >
            </lit-table-column>
            <lit-table-column width="1fr" title="Occurrences" data-index="count" key="count"  align="flex-start" order >
            </lit-table-column>
        </lit-table>
        `;
  }

  createSelectCounterData(name: string, list: Array<any>): SelectionData {
    let selectData = new SelectionData();
    if (list.length > 0) {
      selectData.name = name;
      selectData.count = list.length + '';
      for (let i = 0; i < list.length; i++) {
        selectData.wallDuration += list[i].dur;
      }
      selectData.wallDurationFormat = Utils.getProbablyTime(selectData.wallDuration);
    }
    return selectData;
  }

  sortByColumn(detail: any) {
    let type = detail.sort;
    let key = detail.key;
    if (type == 0) {
      this.tbl!.recycleDataSource = this.source;
    } else {
      let arr = Array.from(this.source);
      arr.sort((a, b): number => {
        if (key == 'wallDurationFormat') {
          if (type == 1) {
            return a.wallDuration - b.wallDuration;
          } else {
            return b.wallDuration - a.wallDuration;
          }
        } else if (key == 'count') {
          if (type == 1) {
            return parseInt(a.count) >= parseInt(b.count) ? 1 : -1;
          } else {
            return parseInt(b.count) >= parseInt(a.count) ? 1 : -1;
          }
        } else if (key == 'avgDuration') {
          if (type == 1) {
            return a.wallDuration / parseInt(a.count) - b.wallDuration / parseInt(b.count);
          } else {
            return b.wallDuration / parseInt(b.count) - a.wallDuration / parseInt(a.count);
          }
        } else if (key == 'name') {
          if (a.name > b.name) {
            return type === 2 ? 1 : -1;
          } else if (a.name == b.name) {
            return 0;
          } else {
            return type === 2 ? -1 : 1;
          }
        } else {
          return 0;
        }
      });
      this.tbl!.recycleDataSource = arr;
    }
  }
}

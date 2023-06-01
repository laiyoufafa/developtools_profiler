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
import { query, queryNativeHookEventTid } from '../../../../database/SqlLite.js';
import { NativeHookCallInfo, NativeHookStatistics } from '../../../../bean/NativeHook.js';
import '../TabPaneFilter.js';
import { FilterData, TabPaneFilter } from '../TabPaneFilter.js';
import '../../../chart/FrameChart.js';
import '../../../../../base-ui/slicer/lit-slicer.js';
import { FrameChart } from '../../../chart/FrameChart.js';
import { ChartMode } from '../../../../bean/FrameChartStruct.js';
import { LitProgressBar } from '../../../../../base-ui/progress-bar/LitProgressBar.js';
import { procedurePool } from '../../../../database/Procedure.js';

@element('tabpane-native-callinfo')
export class TabPaneNMCallInfo extends BaseElement {
  private callInfoTbl: LitTable | null | undefined;
  private tblData: LitTable | null | undefined;
  private progressEL: LitProgressBar | null | undefined;
  private loadingList: number[] = [];
  private callInfoLoadingPage: any;
  private callInfoSource: Array<NativeHookCallInfo> = [];
  private rightSource: Array<NativeHookCallInfo> = [];
  private queryResult: Array<NativeHookStatistics> = [];
  private native_type: Array<string> = ['All Heap & Anonymous VM', 'All Heap', 'All Anonymous VM'];
  private filterAllocationType: string = '0';
  private filterNativeType: string = '0';
  private currentSelection: SelectionParam | undefined;
  private frameChart: FrameChart | null | undefined;
  private isChartShow: boolean = false;
  private sortColumn: string = '';
  private sortType: number = 0;

  set data(callInfoParam: SelectionParam | any) {
    if (callInfoParam == this.currentSelection) {
      return;
    }
    this.currentSelection = callInfoParam;
    this.initFilterTypes();
    let types: Array<string> = [];
    if (callInfoParam.nativeMemory.indexOf(this.native_type[0]) != -1) {
      types.push("'AllocEvent'");
      types.push("'MmapEvent'");
    } else {
      if (callInfoParam.nativeMemory.indexOf(this.native_type[1]) != -1) {
        types.push("'AllocEvent'");
      }
      if (callInfoParam.nativeMemory.indexOf(this.native_type[2]) != -1) {
        types.push("'MmapEvent'");
      }
    }
    // @ts-ignore
    this.callInfoTbl?.shadowRoot?.querySelector('.table').style.height = this.parentElement.clientHeight - 20 - 31 + 'px';
    // @ts-ignore
    this.tblData?.shadowRoot?.querySelector('.table').style.height = this.parentElement.clientHeight + 'px';
    // @ts-ignore
    this.tblData?.recycleDataSource = [];
    // @ts-ignore
    this.callInfoTbl?.recycleDataSource = [];
    this.progressEL!.loading = true;
    this.callInfoLoadingPage.style.visibility = 'visible';
    queryNativeHookEventTid(callInfoParam.leftNs, callInfoParam.rightNs, types).then((result) => {
      this.queryResult = result;
      this.getDataByNativeMemoryWorker(callInfoParam);
    });
  }

  getDataByNativeMemoryWorker(callInfoParam: SelectionParam | any) {
    let callInfoArgs = new Map<string, any>();
    callInfoArgs.set('data', this.queryResult);
    callInfoArgs.set('filterAllocType', this.filterAllocationType);
    callInfoArgs.set('filterEventType', this.filterNativeType);
    callInfoArgs.set('leftNs', callInfoParam.leftNs);
    callInfoArgs.set('rightNs', callInfoParam.rightNs);
    callInfoArgs.set('actionType', 'call-info');
    this.startWorker(callInfoArgs, (results: any[]) => {
      this.tblData!.recycleDataSource = [];
      this.progressEL!.loading = false;
      if (results.length > 0) {
        this.callInfoSource = results;
        this.sortTreeByColumn(this.sortColumn, this.sortType);
        this.frameChart!.mode = ChartMode.Byte;
        this.frameChart!.data = this.callInfoSource;
        this.frameChart?.updateCanvas(true, this.clientWidth);
        this.frameChart?.calculateChartData();
      } else {
        this.callInfoSource = [];
        this.callInfoTbl!.recycleDataSource = [];
        this.frameChart!.data = [];
        this.frameChart!.clearCanvas();
      }
    });
  }

  startWorker(callInfoArgs: Map<string, any>, handler: Function) {
    this.loadingList.push(1);
    this.progressEL!.loading = true;
    this.callInfoLoadingPage.style.visibility = 'visible';
    procedurePool.submitWithName('logic1', 'native-memory-action', callInfoArgs, undefined, (res: any) => {
      handler(res);
      this.loadingList.splice(0, 1);
      if (this.loadingList.length == 0) {
        this.progressEL!.loading = false;
        this.callInfoLoadingPage.style.visibility = 'hidden';
      }
    });
  }

  getParentTree(
    src: Array<NativeHookCallInfo>,
    target: NativeHookCallInfo,
    parents: Array<NativeHookCallInfo>
  ): boolean {
    for (let hook of src) {
      if (hook.id == target.id) {
        parents.push(hook);
        return true;
      } else {
        if (this.getParentTree(hook.children as Array<NativeHookCallInfo>, target, parents)) {
          parents.push(hook);
          return true;
        }
      }
    }
    return false;
  }

  getChildTree(src: Array<NativeHookCallInfo>, eventId: number, children: Array<NativeHookCallInfo>): boolean {
    for (let hook of src) {
      if (hook.eventId == eventId && hook.children.length == 0) {
        children.push(hook);
        return true;
      } else {
        if (this.getChildTree(hook.children as Array<NativeHookCallInfo>, eventId, children)) {
          children.push(hook);
          return true;
        }
      }
    }
    return false;
  }

  setRightTableData(hook: NativeHookCallInfo) {
    let parents: Array<NativeHookCallInfo> = [];
    let children: Array<NativeHookCallInfo> = [];
    this.getParentTree(this.callInfoSource, hook, parents);
    let maxEventId = hook.eventId;
    let maxHeap = 0;

    function findMaxStack(hook: NativeHookCallInfo) {
      if (hook.children.length == 0) {
        if (hook.size > maxHeap) {
          maxHeap = hook.size;
          maxEventId = hook.eventId;
        }
      } else {
        hook.children.map((hookChild) => {
          findMaxStack(<NativeHookCallInfo>hookChild);
        });
      }
    }

    findMaxStack(hook);
    this.getChildTree(hook.children as Array<NativeHookCallInfo>, maxEventId, children);
    this.rightSource = parents.reverse().concat(children.reverse());
    let len = this.rightSource.length;
    // @ts-ignore
    this.tblData?.dataSource = len == 0 ? [] : this.rightSource.slice(1, len);
  }

  initFilterTypes() {
    let filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter');
    this.queryResult = [];
    filter!.firstSelect = '0';
    filter!.secondSelect = '0';
    this.filterAllocationType = '0';
    this.filterNativeType = '0';
  }

  sortTreeByColumn(column: string, sort: number) {
    this.sortColumn = column;
    this.sortType = sort;
    this.callInfoTbl!.recycleDataSource = this.sortTree(this.callInfoSource, column, sort);
  }

  sortTree(arr: Array<NativeHookCallInfo>, column: string, sort: number): Array<NativeHookCallInfo> {
    let sortArr = arr.sort((callInfoLeftData, callInfoRightData) => {
      if (column == 'size') {
        if (sort == 0) {
          return callInfoLeftData.eventId - callInfoRightData.eventId;
        } else if (sort == 1) {
          return callInfoLeftData.size - callInfoRightData.size;
        } else {
          return callInfoRightData.size - callInfoLeftData.size;
        }
      } else {
        if (sort == 0) {
          return callInfoLeftData.eventId - callInfoRightData.eventId;
        } else if (sort == 1) {
          return callInfoLeftData.count - callInfoRightData.count;
        } else {
          return callInfoRightData.count - callInfoLeftData.count;
        }
      }
    });
    sortArr.map((call) => {
      call.children = this.sortTree(call.children as Array<NativeHookCallInfo>, column, sort);
    });
    return sortArr;
  }

  showButtomMenu(isShow: boolean) {
    let filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter')!;
    if (isShow) {
      filter.setAttribute('first', '');
      filter.setAttribute('second', '');
    } else {
      filter.removeAttribute('first');
      filter.removeAttribute('second');
    }
  }

  initElements(): void {
    this.callInfoLoadingPage = this.shadowRoot?.querySelector('.nm-call-info-loading');
    this.progressEL = this.shadowRoot?.querySelector('.nm-call-info-progress') as LitProgressBar;
    this.callInfoTbl = this.shadowRoot?.querySelector<LitTable>('#tb-native-callinfo');
    this.tblData = this.shadowRoot?.querySelector<LitTable>('#tb-native-data');
    this.frameChart = this.shadowRoot?.querySelector<FrameChart>('#framechart');
    let pageTab = this.shadowRoot?.querySelector('#show_table');
    let pageChart = this.shadowRoot?.querySelector('#show_chart');
    this.frameChart?.addChartClickListener((showMenu: boolean) => {
      this.parentElement!.scrollTo(0, 0);
      this.showButtomMenu(showMenu);
    });

    this.callInfoTbl!.addEventListener('row-click', (e) => {
      // @ts-ignore
      let data = e.detail.data as NativeHookCallInfo;
      this.setRightTableData(data);
      data.isSelected = true;
      this.tblData?.clearAllSelection(data);
      this.tblData?.setCurrentSelection(data);
      // @ts-ignore
      if ((e.detail as any).callBack) {
        // @ts-ignore
        (e.detail as any).callBack(true);
      }
    });
    this.tblData!.addEventListener('row-click', (e) => {
      // @ts-ignore
      let detail = e.detail.data as NativeHookCallInfo;
      this.callInfoTbl?.clearAllSelection(detail);
      detail.isSelected = true;
      this.callInfoTbl!.scrollToData(detail);
      // @ts-ignore
      if ((e.detail as any).callBack) {
        // @ts-ignore
        (e.detail as any).callBack(true);
      }
    });
    this.callInfoTbl!.addEventListener('column-click', (evt) => {
      this.sortTreeByColumn(
        // @ts-ignore
        evt.detail.key == 'countValue' || evt.detail.key == 'countPercent' ? 'countValue' : 'size', evt.detail.sort
      );
    });

    this.shadowRoot?.querySelector<TabPaneFilter>('#filter')!.getFilterData((data: FilterData) => {
      this.filterAllocationType = data.firstSelect || '0';
      this.filterNativeType = data.secondSelect || '0';
      this.getDataByNativeMemoryWorker(this.currentSelection);
      if (data.icon == 'block') {
        pageChart?.setAttribute('class', 'show');
        pageTab?.setAttribute('class', '');
        this.isChartShow = true;
        this.frameChart!.data = this.callInfoSource;
        this.frameChart?.calculateChartData();
      } else if (data.icon == 'tree') {
        pageChart?.setAttribute('class', '');
        pageTab?.setAttribute('class', 'show');
        this.isChartShow = false;
        this.frameChart!.clearCanvas();
        this.callInfoTbl!.reMeauseHeight();
      }
    });
    this.initFilterTypes();
  }

  connectedCallback() {
    super.connectedCallback();
    this.parentElement!.onscroll = () => {
      this.frameChart!.tabPaneScrollTop = this.parentElement!.scrollTop;
    };
    let filterHeight = 0;
    new ResizeObserver((entries) => {
      let nmCallInfoTabFilter = this.shadowRoot!.querySelector('#filter') as HTMLElement;
      if (nmCallInfoTabFilter.clientHeight > 0) filterHeight = nmCallInfoTabFilter.clientHeight;
      if (this.parentElement!.clientHeight > filterHeight) {
        nmCallInfoTabFilter.style.display = 'flex';
      } else {
        nmCallInfoTabFilter.style.display = 'none';
      }
      if (this.parentElement?.clientHeight != 0) {
        if (!this.isChartShow) {
          // @ts-ignore
          this.callInfoTbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight + 'px';
          this.callInfoTbl?.reMeauseHeight();
        } else {
          // @ts-ignore
          this.frameChart?.updateCanvas(false, entries[0].contentRect.width);
          this.frameChart?.calculateChartData();
        }
        // @ts-ignore
        this.callInfoTbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 10 - 31 + 'px';
        this.callInfoTbl?.reMeauseHeight();
        // @ts-ignore
        this.tblData?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 10 - 31 + 'px';
        this.tblData?.reMeauseHeight();
        this.callInfoLoadingPage.style.height = this.parentElement!.clientHeight - 24 + 'px';
      }
    }).observe(this.parentElement!);
  }

  initHtml(): string {
    return `
        <style>
        :host{
            padding: 10px 10px 0 10px;
            display: flex;
            flex-direction: column;
        }
        .nm-call-info-filter {
            border: solid rgb(216,216,216) 1px;
            float: left;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
        selector{
            display: none;
        }
        .nm-call-info-progress{
            bottom: 33px;
            position: absolute;
            height: 1px;
            left: 0;
            right: 0;
        } 
        .nm-call-info-loading{
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            width:100%;
            background:transparent;
            z-index: 999999;
        }
        .show{
            display: flex;
            flex: 1;
        }
        </style>
        <div class="nm-call-info-content" style="display: flex;flex-direction: row">
            <selector id='show_table' class="show">
            <lit-slicer style="width:100%">
                <div style="width: 65%">
                    <lit-table id="tb-native-callinfo" style="height: auto" tree>
                        <lit-table-column class="nm-call-info-column" width="60%" title="Symbol Name" data-index="symbolName" key="symbolName"  align="flex-start">
                        </lit-table-column>
                        <lit-table-column class="nm-call-info-column" width="1fr" title="Size" data-index="heapSizeStr" key="heapSizeStr"  align="flex-start" order>
                        </lit-table-column>
                        <lit-table-column class="nm-call-info-column" width="1fr" title="%" data-index="heapPercent" key="heapPercent" align="flex-start"  order>
                        </lit-table-column>
                        <lit-table-column class="nm-call-info-column" width="1fr" title="Count" data-index="countValue" key="countValue" align="flex-start" order>
                        </lit-table-column>
                        <lit-table-column class="nm-call-info-column" width="1fr" title="%" data-index="countPercent" key="countPercent" align="flex-start" order>
                        </lit-table-column>
                        <lit-table-column class="nm-call-info-column" width="1fr" title="  " data-index="type" key="type"  align="flex-start" >
                            <template>
                                <img src="img/library.png" size="20" v-if=" type == 1 ">
                                <img src="img/function.png" size="20" v-if=" type == 0 ">
                                <div v-if=" type == - 1 "></div>
                            </template>
                        </lit-table-column>
                    </lit-table>
                </div>
                <lit-slicer-track ></lit-slicer-track>
                <lit-table id="tb-native-data" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)" hideDownload>
                    <lit-table-column class="nm-call-info-column" title="" width="60px" data-index="type" key="type"  align="flex-start" >
                        <template>
                            <img src="img/library.png" size="20" v-if=" type == 1 ">
                            <img src="img/function.png" size="20" v-if=" type == 0 ">
                        </template>
                    </lit-table-column>
                    <lit-table-column class="nm-call-info-column" width="1fr" title="" data-index="symbolName" key="symbolName"  align="flex-start">
                    </lit-table-column>
                </lit-table>
                </lit-slicer>
            </selector>
            <selector class="nm-call-info-selector" id='show_chart'>
                <tab-framechart id='framechart' style='width: 100%;height: auto'> </tab-framechart>
            </selector>
            <lit-progress-bar class="progress nm-call-info-progress"></lit-progress-bar>
            <tab-pane-filter id="filter" class="nm-call-info-filter" icon first second></tab-pane-filter>
            <div class="loading nm-call-info-loading"></div>
        </div>
        `;
  }
}

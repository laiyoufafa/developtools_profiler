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
import { LitChartPie } from '../../../../../base-ui/chart/pie/LitChartPie.js';
import '../../../../../base-ui/chart/pie/LitChartPie.js';
import { LitProgressBar } from '../../../../../base-ui/progress-bar/LitProgressBar.js';
import { Utils } from '../../base/Utils.js';
import { SpSystemTrace } from '../../../SpSystemTrace.js';
import { procedurePool } from '../../../../database/Procedure.js';
const TYPE_ALLOC_STRING = 'AllocEvent';
const TYPE_MAP_STRING = 'MmapEvent';

const TYPE_ALLOC = 0;
const TYPE_MAP = 1;
const TYPE_FREE = 2;
const TYPE_UN_MAP = 3;
const PIE_CHART_LIMIT = 20;

class AnalysisObj {
  tName?: string;
  tid?: number;
  typeName?: string;
  typeId?: number;
  libName?: string;
  libId?: number;
  symbolName?: string;
  symbolId?: number;

  tableName = '';

  applySize: number;
  applySizeFormat: string;
  applyCount: number;
  releaseSize: number;
  releaseSizeFormat: string;
  releaseCount: number;
  existSize: number;
  existSizeFormat: string;
  existCount: number;

  applySizePercent?: string;
  applyCountPercent?: string;
  releaseSizePercent?: string;
  releaseCountPercent?: string;
  existSizePercent?: string;
  existCountPercent?: string;

  constructor(applySize: number, applyCount: number, releaseSize: number, releaseCount: number) {
    this.applySize = applySize;
    this.applyCount = applyCount;
    this.releaseSize = releaseSize;
    this.releaseCount = releaseCount;
    this.existSize = applySize - releaseSize;
    this.existCount = applyCount - releaseCount;
    this.applySizeFormat = Utils.getBinaryByteWithUnit(this.applySize);
    this.releaseSizeFormat = Utils.getBinaryByteWithUnit(this.releaseSize);
    this.existSizeFormat = Utils.getBinaryByteWithUnit(this.existSize);
  }
}

class SizeObj {
  applySize = 0;
  applyCount = 0;
  releaseSize = 0;
  releaseCount = 0;
}

@element('tabpane-nm-statistic-analysis')
export class TabPaneNMStatisticAnalysis extends BaseElement {
  private currentSelection: SelectionParam | any;
  private pie: LitChartPie | null | undefined;
  private processData!: Array<any>;
  private eventTypeData!: any[];
  private threadData!: any[];
  private soData!: any[];
  private functionData!: any[];
  private tableType: LitTable | null | undefined;
  private tableThread: LitTable | null | undefined;
  private tableSo: LitTable | null | undefined;
  private tableFunction: LitTable | null | undefined;
  private range: HTMLLabelElement | null | undefined;
  private back: HTMLDivElement | null | undefined;
  private tabName: HTMLDivElement | null | undefined;
  private progressEL: LitProgressBar | null | undefined;
  private type: string | null | undefined;
  private sortColumn: string = '';
  private sortType: number = 0;
  private isStatistic = false;
  private typeMap!: Map<number, Array<any>>;
  private currentLevel = -1;
  private currentLevelApplySize = 0;
  private currentLevelReleaseSize = 0;
  private currentLevelExistSize = 0;
  private currentLevelApplyCount = 0;
  private currentLevelReleaseCount = 0;
  private currentLevelExistCount = 0;
  private releaseLibMap!: Map<number, any>;
  private currentLevelData!: Array<any>;
  private typeStatisticsData!: {};
  private libStatisticsData!: {};
  private functionStatisticsData!: {};

  set data(val: SelectionParam | any) {
    if (val == this.currentSelection) {
      return;
    }
    this.clearData();
    this.currentSelection = val;
    this.tableType!.style.display = 'grid';
    this.tableThread!.style.display = 'none';
    this.tableSo!.style.display = 'none';
    this.tableFunction!.style.display = 'none';
    this.back!.style.visibility = 'hidden';
    this.range!.textContent =
      'Selected range: ' + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + ' ms';
    this.isStatistic = val.nativeMemory.length === 0;

    this.getNMEventTypeSize(val);
  }
  initElements(): void {
    this.range = this.shadowRoot?.querySelector('#time-range');
    this.pie = this.shadowRoot!.querySelector<LitChartPie>('#chart-pie');
    this.tableType = this.shadowRoot!.querySelector<LitTable>('#tb-eventtype-usage');
    this.tableThread = this.shadowRoot!.querySelector<LitTable>('#tb-thread-usage');
    this.tableSo = this.shadowRoot!.querySelector<LitTable>('#tb-so-usage');
    this.tableFunction = this.shadowRoot!.querySelector<LitTable>('#tb-function-usage');
    this.back = this.shadowRoot!.querySelector<HTMLDivElement>('.go-back');
    this.tabName = this.shadowRoot!.querySelector<HTMLDivElement>('.subheading');
    this.progressEL = this.shadowRoot?.querySelector('.progress') as LitProgressBar;
    this.getBack();
  }
  clearData() {
    this.pie!.dataSource = [];
    this.tableType!.recycleDataSource = [];
    this.tableThread!.recycleDataSource = [];
    this.tableSo!.recycleDataSource = [];
    this.tableFunction!.recycleDataSource = [];
  }
  getBack() {
    this.back!.addEventListener('click', () => {
      if (this.tabName!.textContent === 'Statistic By Library Size') {
        this.tableType!.style.display = 'grid';
        this.tableSo!.style.display = 'none';
        this.back!.style.visibility = 'hidden';
        this.tableSo!.setAttribute('hideDownload', '');
        this.tableType?.removeAttribute('hideDownload');
        this.currentLevel = 0;
        this.currentLevelData = this.eventTypeData;
        this.typePieChart(this.currentSelection);
      } else if (this.tabName!.textContent === 'Statistic By Function Size') {
        this.tableSo!.style.display = 'grid';
        this.tableFunction!.style.display = 'none';
        this.tableFunction!.setAttribute('hideDownload', '');
        this.tableSo?.removeAttribute('hideDownload');
        this.currentLevelData = this.soData;
        this.currentLevel = 1;
        this.libraryPieChart(this.currentSelection);
      }
    });
  }
  typePieChart(val: any) {
    this.pie!.config = {
      appendPadding: 0,
      data: this.eventTypeData,
      angleField: 'existSize',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>   
                        <div>Type:${obj.obj.tableName}</div>
                        <div>Exist Size:${obj.obj.existSizeFormat} (${obj.obj.existSizePercent}%)</div>
                        <div>Exist Count:${obj.obj.existCount} (${obj.obj.existCountPercent}%)</div>
                        </div>`;
      },
      angleClick: (it: any) => {
        this.clearData();
        this.back!.style.visibility = 'visible';
        this.tableType!.style.display = 'none';
        this.tableSo!.style.display = 'grid';
        this.tableType!.setAttribute('hideDownload', '');
        this.tableSo?.removeAttribute('hideDownload');
        this.getLibSize(it, val);
        // @ts-ignore
        this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = it.typeName;
        // @ts-ignore
        this.type = it.typeName;
      },
      hoverHandler: (data) => {
        if (data) {
          this.tableType!.setCurrentHover(data);
        } else {
          this.tableType!.mouseOut();
        }
      },
      interactions: [
        {
          type: 'element-active',
        },
      ],
    };
    this.tableType!.addEventListener('row-hover', (evt: any) => {
      if (evt.detail.data) {
        let data = evt.detail.data;
        data.isHover = true;
        if ((evt.detail as any).callBack) {
          (evt.detail as any).callBack(true);
        }
      }
      this.pie?.showHover();
    });
    this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = '';
    this.tabName!.textContent = 'Statistic By Event Type Size';
    this.eventTypeData.unshift(this.typeStatisticsData);
    this.tableType!.recycleDataSource = this.eventTypeData;
    this.currentLevelData = JSON.parse(JSON.stringify(this.eventTypeData));
    // @ts-ignore
    this.eventTypeData.shift(this.typeStatisticsData);
    this.tableType?.reMeauseHeight();
  }
  threadPieChart(val: any) {
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.threadData),
      angleField: 'existSize',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                        <div>Thread:${obj.obj.tableName}</div>
                        <div>Exist Size:${obj.obj.existSizeFormat} (${obj.obj.existSizePercent}%)</div>
                        <div>Exist Count:${obj.obj.existCount} (${obj.obj.existCountPercent}%)</div>
                    </div>`;
      },
      angleClick: (it: any) => {
        // @ts-ignore
        if (it.tid != 'other') {
          this.clearData();
          this.tableThread!.style.display = 'none';
          this.tableSo!.style.display = 'grid';
          this.getLibSize(it, val);
          // @ts-ignore
          this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = it.type + ' / ' + 'Thread ' + it.tid;
          // @ts-ignore
          this.tid = it.tid;
        }
      },
      hoverHandler: (data) => {
        if (data) {
          this.tableThread!.setCurrentHover(data);
        } else {
          this.tableThread!.mouseOut();
        }
      },
      interactions: [
        {
          type: 'element-active',
        },
      ],
    };
    this.tableThread!.addEventListener('row-hover', (evt: any) => {
      if (evt.detail.data) {
        let data = evt.detail.data;
        data.isHover = true;
        if ((evt.detail as any).callBack) {
          (evt.detail as any).callBack(true);
        }
      }
      this.pie?.showHover();
    });
    this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = this.type + '';
    this.tabName!.textContent = 'Statistic By Thread Size';
    this.tableThread!.recycleDataSource = this.threadData;
    this.tableThread?.reMeauseHeight();
  }
  libraryPieChart(val: any) {
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.soData),
      angleField: 'existSize',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                        <div>Library:${obj.obj.libName}</div>
                        <div>Exist Size:${obj.obj.existSizeFormat} (${obj.obj.existSizePercent}%)</div>
                        <div>Exist Count:${obj.obj.existCount} (${obj.obj.existCountPercent}%)</div>
                    </div>`;
      },
      angleClick: (it: any) => {
        // @ts-ignore
        if (it.tableName != 'other') {
          this.clearData();
          this.tableSo!.style.display = 'none';
          this.tableFunction!.style.display = 'grid';
          this.tableSo!.setAttribute('hideDownload', '');
          this.tableFunction?.removeAttribute('hideDownload');
          this.getNMFunctionSize(it, val);
          // @ts-ignore
          this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = this.type + ' / ' + it.libName;
        }
      },
      hoverHandler: (data) => {
        if (data) {
          this.tableSo!.setCurrentHover(data);
        } else {
          this.tableSo!.mouseOut();
        }
      },
      interactions: [
        {
          type: 'element-active',
        },
      ],
    };
    this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = this.type + '';
    this.tableSo!.addEventListener('row-hover', (evt: any) => {
      if (evt.detail.data) {
        let data = evt.detail.data;
        data.isHover = true;
        if ((evt.detail as any).callBack) {
          (evt.detail as any).callBack(true);
        }
      }
      this.pie?.showHover();
    });
    this.tabName!.textContent = 'Statistic By Library Size';
    this.soData.unshift(this.libStatisticsData);
    this.currentLevelData = JSON.parse(JSON.stringify(this.soData));
    this.tableSo!.recycleDataSource = this.soData;
    // @ts-ignore
    this.soData.shift(this.libStatisticsData);
    this.tableSo?.reMeauseHeight();
    this.tableSo!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
  }

  functionPieChart(val: any) {
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.functionData),
      angleField: 'existSize',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                        <div>Function:${obj.obj.symbolName}</div>
                        <div>Exist Size:${obj.obj.existSizeFormat} (${obj.obj.existSizePercent}%)</div>
                        <div>Exist Count:${obj.obj.existCount} (${obj.obj.existCountPercent}%)</div>
                    </div>`;
      },
      hoverHandler: (data) => {
        if (data) {
          this.tableFunction!.setCurrentHover(data);
        } else {
          this.tableFunction!.mouseOut();
        }
      },
      interactions: [
        {
          type: 'element-active',
        },
      ],
    };
    this.tableFunction!.addEventListener('row-hover', (evt: any) => {
      if (evt.detail.data) {
        let data = evt.detail.data;
        data.isHover = true;
        if ((evt.detail as any).callBack) {
          (evt.detail as any).callBack(true);
        }
      }
      this.pie?.showHover();
    });
    this.functionData.unshift(this.functionStatisticsData);
    this.currentLevelData = JSON.parse(JSON.stringify(this.functionData));
    this.tableFunction!.recycleDataSource = this.functionData;
    // @ts-ignore
    this.functionData.shift(this.functionStatisticsData);
    this.tableFunction?.reMeauseHeight();
    this.tableFunction!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
  }

  sortByColumn(column: string, sort: number) {
    this.sortColumn = column;
    this.sortType = sort;
    let currentTable: LitTable | null | undefined;
    switch (this.currentLevel) {
      case 0:
        currentTable = this.tableType;
        break;
      case 1:
        currentTable = this.tableSo;
        break;
      case 2:
        currentTable = this.tableFunction;
        break;
    }
    if (!currentTable) {
      return;
    }
    if (sort == 0) {
      currentTable!.recycleDataSource = this.currentLevelData;
    } else {
      let arr = [...this.currentLevelData];
      switch (this.currentLevel) {
        case 0:
          // @ts-ignore
          arr.shift(this.typeStatisticsData);
          break;
        case 1:
          // @ts-ignore
          arr.shift(this.libStatisticsData);
          break;
        case 2:
          // @ts-ignore
          arr.shift(this.functionStatisticsData);
          break;
      }
      if (column == 'tableName') {
        currentTable!.recycleDataSource = arr.sort((a, b) => {
          if (sort == 1) {
            if (a.tableName > b.tableName) {
              return 1;
            } else if (a.tableName == b.tableName) {
              return 0;
            } else {
              return -1;
            }
          } else {
            if (b.tableName > a.tableName) {
              return 1;
            } else if (a.tableName == b.tableName) {
              return 0;
            } else {
              return -1;
            }
          }
        });
      } else if (column == 'existSizeFormat') {
        currentTable!.recycleDataSource = arr.sort((a, b) => {
          return sort == 1 ? a.existSize - b.existSize : b.existSize - a.existSize;
        });
      } else if (column == 'existSizePercent') {
        currentTable!.recycleDataSource = arr.sort((a, b) => {
          return sort == 1 ? a.existSize - b.existSize : b.existSize - a.existSize;
        });
      } else if (column == 'existCount') {
        currentTable!.recycleDataSource = arr.sort((a, b) => {
          return sort == 1 ? a.existCount - b.existCount : b.existCount - a.existCount;
        });
      } else if (column == 'existCountPercent') {
        currentTable!.recycleDataSource = arr.sort((a, b) => {
          return sort == 1 ? a.existCount - b.existCount : b.existCount - a.existCount;
        });
      }
      switch (this.currentLevel) {
        case 0:
          arr.unshift(this.typeStatisticsData);
          break;
        case 1:
          arr.unshift(this.libStatisticsData);
          break;
        case 2:
          arr.unshift(this.functionStatisticsData);
          break;
      }
      currentTable!.recycleDataSource = arr;
    }
  }

  getNMEventTypeSize(val: any) {
    this.progressEL!.loading = true;
    let typeFilter = [];
    if (this.isStatistic) {
      for (let type of val.nativeMemoryStatistic) {
        if (type === 'All Heap & Anonymous VM') {
          typeFilter = [0, 1];
          break;
        } else if (type === 'All Heap') {
          typeFilter.push(0);
        } else {
          typeFilter.push(1);
        }
      }
      this.getDataFromWorker(val, typeFilter);
    } else {
      for (let type of val.nativeMemory) {
        if (type === 'All Heap & Anonymous VM') {
          typeFilter = [];
          typeFilter.push(...["'AllocEvent'", "'FreeEvent'", "'MmapEvent'", "'MunmapEvent'"]);
          break;
        } else if (type === 'All Heap') {
          typeFilter.push(...["'AllocEvent'", "'FreeEvent'"]);
        } else {
          typeFilter.push(...["'MmapEvent'", "'MunmapEvent'"]);
        }
      }
      this.getDataFromWorker(val, typeFilter);
    }

    this.tableType!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
  }

  private calTypeSize(val: any, result: any) {
    this.processData = JSON.parse(JSON.stringify(result));
    this.resetCurrentLevelData();
    this.typeMap = this.typeSizeGroup(this.processData);
    this.currentLevelExistSize = this.currentLevelApplySize - this.currentLevelReleaseSize;
    this.currentLevelExistCount = this.currentLevelApplyCount - this.currentLevelReleaseCount;
    this.eventTypeData = [];
    if (this.typeMap.has(TYPE_ALLOC)) {
      let allocType = this.setTypeMap(this.typeMap, TYPE_ALLOC, TYPE_ALLOC_STRING);
      if (allocType) {
        this.calPercent(allocType);
        this.eventTypeData.push(allocType);
      }
    }
    if (this.typeMap.has(TYPE_MAP)) {
      let mapType = this.setTypeMap(this.typeMap, TYPE_MAP, TYPE_MAP_STRING);
      if (mapType) {
        this.calPercent(mapType);
        this.eventTypeData.push(mapType);
      }
    }
    this.eventTypeData.sort((a, b) => b.existSize - a.existCount);
    this.typeStatisticsData = this.totalData(this.typeStatisticsData);
    this.progressEL!.loading = false;
    this.currentLevel = 0;
    this.typePieChart(val);
  }

  getNMThreadSize(item: any, val: any) {
    this.progressEL!.loading = true;
    let threadMap = new Map<number, Array<any>>();
    let types = this.getTypes(item);
    this.resetCurrentLevelData(item);

    for (let itemData of this.processData) {
      if (!types.includes(itemData.type)) {
        continue;
      }
      if (threadMap.has(itemData.tid)) {
        threadMap.get(itemData.tid)?.push(itemData);
      } else {
        let itemArray = new Array<any>();
        itemArray.push(itemData);
        threadMap.set(itemData.tid, itemArray);
      }
    }
    this.threadData = [];
    threadMap.forEach((dbData: Array<any>, tid: number) => {
      const sizeObj = this.calSizeObj(dbData);
      let analysis = new AnalysisObj(sizeObj.applySize, sizeObj.applyCount, sizeObj.releaseSize, sizeObj.releaseCount);
      this.calPercent(analysis);
      analysis.typeId = item.typeId;
      analysis.typeName = item.typeName;
      analysis.tid = tid;
      analysis.tName = 'Thread ' + tid;
      analysis.tableName = analysis.tName;
      this.threadData.push(analysis);
    });
    this.threadData.sort((a, b) => b.existSize - a.existSize);
    this.currentLevelData = this.threadData;
    this.progressEL!.loading = false;
    this.threadPieChart(val);
    this.tableThread!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
  }

  getLibSize(item: any, val: any) {
    this.progressEL!.loading = true;
    let typeId = item.typeId;
    let typeName = item.typeName;
    let tid = item.tid;
    let libMap = new Map<number, Array<any>>();
    this.resetCurrentLevelData(item);
    let types = this.getTypes(item);
    this.soData = [];
    if (!this.processData) return;
    for (let itemData of this.processData) {
      if (!types.includes(itemData.type)) {
        continue;
      }
      let libId = itemData.libId;

      if (libMap.has(libId)) {
        libMap.get(libId)?.push(itemData);
      } else {
        let dataArray = new Array<any>();
        dataArray.push(itemData);
        libMap.set(libId, dataArray);
      }
    }
    libMap.forEach((libItems, libId) => {
      let libPath = SpSystemTrace.DATA_DICT.get(libId)?.split('/');
      let libName = '';
      if (libPath) {
        libName = libPath[libPath.length - 1];
      }
      const sizeObj = this.calSizeObj(libItems);
      let analysis = new AnalysisObj(sizeObj.applySize, sizeObj.applyCount, sizeObj.releaseSize, sizeObj.releaseCount);
      this.calPercent(analysis);
      analysis.typeId = typeId;
      analysis.typeName = typeName;
      analysis.tid = tid;
      analysis.tName = 'Thread ' + tid;
      analysis.libId = libId;
      analysis.libName = libName;
      analysis.tableName = analysis.libName;
      this.soData.push(analysis);
    });
    this.soData.sort((a, b) => b.existSize - a.existSize);
    this.libStatisticsData = this.totalData(this.libStatisticsData);
    this.currentLevel = 1;
    this.libraryPieChart(val);
    this.progressEL!.loading = false;
  }

  getNMFunctionSize(item: any, val: any) {
    this.progressEL!.loading = true;
    this.shadowRoot!.querySelector<HTMLDivElement>('.subheading')!.textContent = 'Statistic By Function Size';
    let typeId = item.typeId;
    let typeName = item.typeName;
    let tid = item.tid;
    let libId = item.libId;
    let symbolMap = new Map<number, Array<any>>();
    this.resetCurrentLevelData(item);
    let types = this.getTypes(item);
    if (!this.processData) {
      return;
    }
    for (let data of this.processData) {
      if (!types.includes(data.type) || data.libId !== libId) {
        continue;
      }
      if (symbolMap.has(data.symbolId)) {
        symbolMap.get(data.symbolId)?.push(data);
      } else {
        let dataArray = new Array<any>();
        dataArray.push(data);
        symbolMap.set(data.symbolId, dataArray);
      }
    }

    this.functionData = [];
    symbolMap.forEach((symbolItems, symbolId) => {
      let symbolPath = SpSystemTrace.DATA_DICT.get(symbolId)?.split('/');
      let symbolName = symbolPath ? symbolPath[symbolPath.length - 1] : 'null';
      const sizeObj = this.calSizeObj(symbolItems);
      let analysis = new AnalysisObj(sizeObj.applySize, sizeObj.applyCount, sizeObj.releaseSize, sizeObj.releaseCount);
      this.calPercent(analysis);
      analysis.typeId = typeId;
      analysis.typeName = typeName;
      analysis.tid = tid;
      analysis.tName = 'Thread ' + tid;
      analysis.libId = libId;
      analysis.libName = item.libName;
      analysis.symbolId = symbolId;
      analysis.symbolName = symbolName;
      analysis.tableName = analysis.symbolName;
      this.functionData.push(analysis);
    });
    this.functionData.sort((a, b) => b.existSize - a.existSize);
    // @ts-ignore
    this.functionStatisticsData = this.totalData(this.functionStatisticsData);
    this.currentLevel = 2;
    this.progressEL!.loading = false;
    this.functionPieChart(val);
  }

  getPieChartData(res: any[]) {
    if (res.length > PIE_CHART_LIMIT) {
      let pieChartArr: any[] = [];
      let other: any = {
        tableName: 'other',
        symbolName: 'other',
        existSizePercent: 0,
        libName: 'other',
        existSize: 0,
        existSizeFormat: '',
        existCount: 0,
        countPercent: 'other',
        existCountPercent: 0,
      };
      for (let i = 0; i < res.length; i++) {
        if (i < PIE_CHART_LIMIT - 1) {
          pieChartArr.push(res[i]);
        } else {
          other.existCount += res[i].existCount;
          other.existSize += res[i].existSize;
          other.existSizeFormat = Utils.getBinaryByteWithUnit(other.existSize);
          other.existSizePercent = ((other.existSize / this.currentLevelExistSize) * 100).toFixed(2);
          other.existCountPercent = ((other.existCount / this.currentLevelExistCount) * 100).toFixed(2);
        }
      }
      pieChartArr.push(other);
      return pieChartArr;
    }
    return res;
  }

  setTypeMap(typeMap: Map<number, any>, tyeId: number, typeName: string): AnalysisObj | null {
    let applySize = 0;
    let releaseSize = 0;
    let applyCount = 0;
    let releaseCount = 0;
    let releaseTypeId = tyeId === TYPE_ALLOC ? TYPE_FREE : TYPE_UN_MAP;
    let currentType = typeMap.get(tyeId);
    if (!currentType) {
      return null;
    }

    if (!this.isStatistic) {
      if (typeMap.has(releaseTypeId)) {
        for (let freeSample of typeMap.get(releaseTypeId)!) {
          releaseSize += freeSample.size;
          releaseCount += freeSample.count;
        }
      }
    }

    for (let applySample of typeMap.get(tyeId)!) {
      applySize += applySample.size;
      applyCount += applySample.count;
      if (this.isStatistic) {
        releaseSize += applySample.releaseSize;
        releaseCount += applySample.releaseCount;
      }
    }
    let typeItem = new AnalysisObj(applySize, applyCount, releaseSize, releaseCount);
    typeItem.typeId = tyeId;
    typeItem.typeName = typeName;
    typeItem.tableName = typeName;
    return typeItem;
  }

  calSize(sizeObj: SizeObj, itemData: any): any {
    switch (itemData.type) {
      case TYPE_ALLOC:
      case TYPE_MAP:
        sizeObj.applySize += itemData.size;
        sizeObj.applyCount += itemData.count;
        if (this.isStatistic) {
          sizeObj.releaseSize += itemData.releaseSize;
          sizeObj.releaseCount += itemData.releaseCount;
        }
        break;
      case TYPE_FREE:
      case TYPE_UN_MAP:
        sizeObj.releaseSize += itemData.size;
        sizeObj.releaseCount += itemData.count;
        break;
    }
  }

  private calPercent(item: AnalysisObj) {
    item.applySizePercent = ((item.applySize / this.currentLevelApplyCount) * 100).toFixed(2);
    item.applyCountPercent = ((item.applyCount / this.currentLevelApplyCount) * 100).toFixed(2);
    item.releaseSizePercent = ((item.releaseSize / this.currentLevelReleaseSize) * 100).toFixed(2);
    item.releaseCountPercent = ((item.releaseCount / this.currentLevelReleaseCount) * 100).toFixed(2);
    item.existSizePercent = ((item.existSize / this.currentLevelExistSize) * 100).toFixed(2);
    item.existCountPercent = ((item.existCount / this.currentLevelExistCount) * 100).toFixed(2);
  }

  private resetCurrentLevelData(parent?: any) {
    if (parent) {
      this.currentLevelApplySize = parent.applySize;
      this.currentLevelApplyCount = parent.applyCount;
      this.currentLevelExistSize = parent.existSize;
      this.currentLevelExistCount = parent.existCount;
      this.currentLevelReleaseSize = parent.releaseSize;
      this.currentLevelReleaseCount = parent.releaseCount;
    } else {
      this.currentLevelApplySize = 0;
      this.currentLevelApplyCount = 0;
      this.currentLevelExistSize = 0;
      this.currentLevelExistCount = 0;
      this.currentLevelReleaseSize = 0;
      this.currentLevelReleaseCount = 0;
    }
  }

  private typeSizeGroup(dbArray: Array<any>): Map<number, Array<any>> {
    let typeMap = new Map<number, Array<any>>();
    if (!dbArray || dbArray.length == 0) {
      return typeMap;
    }

    for (let itemData of dbArray) {
      switch (itemData.type) {
        case TYPE_ALLOC:
          this.currentLevelApplySize += itemData.size;
          this.currentLevelApplyCount += itemData.count;
          if (this.isStatistic) {
            this.currentLevelReleaseSize += itemData.releaseSize;
            this.currentLevelReleaseCount += itemData.releaseCount;
          }
          if (typeMap.has(TYPE_ALLOC)) {
            typeMap.get(TYPE_ALLOC)?.push(itemData);
          } else {
            let itemArray = new Array<any>();
            itemArray.push(itemData);
            typeMap.set(TYPE_ALLOC, itemArray);
          }
          break;
        case TYPE_MAP:
          this.currentLevelApplySize += itemData.size;
          this.currentLevelApplyCount += itemData.count;
          if (this.isStatistic) {
            this.currentLevelReleaseSize += itemData.releaseSize;
            this.currentLevelReleaseCount += itemData.releaseCount;
          }
          if (typeMap.has(TYPE_MAP)) {
            typeMap.get(TYPE_MAP)?.push(itemData);
          } else {
            let itemArray = new Array<any>();
            itemArray.push(itemData);
            typeMap.set(TYPE_MAP, itemArray);
          }
          break;
        case TYPE_FREE:
          this.currentLevelReleaseSize += itemData.size;
          this.currentLevelReleaseCount += itemData.count;
          if (typeMap.has(TYPE_FREE)) {
            typeMap.get(TYPE_FREE)?.push(itemData);
          } else {
            let itemArray = new Array<any>();
            itemArray.push(itemData);
            typeMap.set(TYPE_FREE, itemArray);
          }
          break;
        case TYPE_UN_MAP:
          this.currentLevelReleaseSize += itemData.size;
          this.currentLevelReleaseCount += itemData.count;
          if (typeMap.has(TYPE_UN_MAP)) {
            typeMap.get(TYPE_UN_MAP)?.push(itemData);
          } else {
            let itemArray = new Array<any>();
            itemArray.push(itemData);
            typeMap.set(TYPE_UN_MAP, itemArray);
          }
          break;
      }
    }

    return typeMap;
  }

  totalData(total: any) {
    total = {
      existSizeFormat: Utils.getBinaryByteWithUnit(this.currentLevelExistSize),
      existSizePercent: ((this.currentLevelExistSize / this.currentLevelExistSize) * 100).toFixed(2),
      existCount: this.currentLevelExistCount,
      existCountPercent: ((this.currentLevelExistCount / this.currentLevelExistCount) * 100).toFixed(2),
      existSize: 0,
      tableName: '',
      libName: '',
      symbolName: '',
    };
    return total;
  }

  calSizeObj(dbData: Array<any>) {
    let sizeObj = new SizeObj();
    for (let item of dbData) {
      if (this.isStatistic) {
        sizeObj.applyCount += item.count;
        sizeObj.applySize += item.size;
        sizeObj.releaseCount += item.releaseCount;
        sizeObj.releaseSize += item.releaseSize;
      } else {
        if ([TYPE_ALLOC, TYPE_MAP].includes(item.type)) {
          sizeObj.applyCount += item.count;
          sizeObj.applySize += item.size;
        } else {
          sizeObj.releaseCount += item.count;
          sizeObj.releaseSize += item.size;
        }
      }
    }
    return sizeObj;
  }

  getTypes(parent: AnalysisObj) {
    let types = new Array<number>();
    types.push(parent.typeId!);
    if (!this.isStatistic) {
      let releaseType;
      if (parent.typeId === TYPE_ALLOC) {
        releaseType = TYPE_FREE;
      } else {
        releaseType = TYPE_UN_MAP;
      }
      types.push(releaseType);
    }
    return types;
  }

  getDataFromWorker(val: SelectionParam | any, typeFilter: Array<number | string>) {
    this.getDataByWorkerQuery(
      {
        leftNs: val.leftNs,
        rightNs: val.rightNs,
        types: typeFilter,
        isStatistic: this.isStatistic,
      },
      (results: any) => {
        this.calTypeSize(val, results);
      }
    );
  }
  getDataByWorkerQuery(args: any, handler: Function) {
    this.progressEL!.loading = true;
    procedurePool.submitWithName('logic1', 'native-memory-queryAnalysis', args, undefined, (results: any) => {
      handler(results);
      this.progressEL!.loading = false;
    });
  }

  initHtml(): string {
    return `
        <style>
        :host {
            display: flex;
            flex-direction: column;
        }
        #chart-pie{
            height: 300px;
        }
        .table-box{
            width: 60%;
            border-left: solid 1px var(--dark-border1,#e0e0e0);
            border-radius: 5px;
            padding: 10px;
        }
        .go-back{
            display:flex;
            align-items: center;
            cursor: pointer;
            margin-left: 20px;
            visibility: hidden;
        }
        .back-box{
            background-color: var(--bark-expansion,#0C65D1);
            border-radius: 5px;
            color: #fff;
            display: flex;
            margin-right: 10px;
            width: 40px;
            height: 20px;
            justify-content: center;
            align-items: center;
        }
        .subheading{
            font-weight: bold;
            text-align: center;
        }
        .progress{
            position: absolute;
            height: 1px;
            left: 0;
            right: 0;
        } 
        </style>
        <label id="time-range" style="width: 100%;height: 20px;text-align: end;font-size: 10pt;margin-bottom: 5px">Selected range:0.0 ms</label>
        <div style="display: flex;flex-direction: row;"class="d-box">
           <lit-progress-bar class="progress"></lit-progress-bar>
                     <div id="left_table" style="width: 40%;height:auto;">
                        <div style="display: flex;margin-bottom: 10px">
                           <div class="go-back">
                              <div class="back-box">
                                  <lit-icon name="arrowleft"></lit-icon>
                              </div>
                           </div>
                         <div class="title"></div>
                        </div>
                         <div class="subheading"></div>
                         <lit-chart-pie  id="chart-pie"></lit-chart-pie>
                     </div>
                     <div class="table-box" style="height:auto;width: 60%;">
                    <lit-table id="tb-eventtype-usage"style="height:60vh;display: none">
                        <lit-table-column width="1fr" title="Event Type" data-index="tableName" key="tableName" align="flex-start" ></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Size" data-index="existSizeFormat" key="existSizeFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existSizePercent" key="existSizePercent" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Count" data-index="existCount" key="existCount" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existCountPercent" key="existCountPercent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    <lit-table id="tb-thread-usage" style="height: 60vh;display: none"hideDownload>
                        <lit-table-column width="1fr" title="Thread" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Size" data-index="existSizeFormat" key="existSizeFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existSizePercent" key="existSizePercent" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Count" data-index="existCount" key="existCount" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existCountPercent" key="existCountPercent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    <lit-table id="tb-so-usage" style="height:60vh;display: none"hideDownload>
                        <lit-table-column width="1fr" title="Library" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Size" data-index="existSizeFormat" key="existSizeFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existSizePercent" key="existSizePercent" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Count" data-index="existCount" key="existCount" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existCountPercent" key="existCountPercent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    <lit-table id="tb-function-usage" style="height:60vh;display: none"hideDownload>
                        <lit-table-column width="1fr" title="Function" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Size" data-index="existSizeFormat" key="existSizeFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existSizePercent" key="existSizePercent" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Exist Count" data-index="existCount" key="existCount" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="existCountPercent" key="existCountPercent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    </div>
        </div>
`;
  }
}

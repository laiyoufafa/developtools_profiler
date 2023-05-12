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
import { SelectionData, SelectionParam } from '../../../../bean/BoxSelection.js';
import { LitChartPie } from '../../../../../base-ui/chart/pie/LitChartPie.js';
import '../../../../../base-ui/chart/pie/LitChartPie.js';
import { queryHiPerfProcessCount, queryPerfProcess } from '../../../../database/SqlLite.js';
import { PerfThread } from '../../../../bean/PerfProfile.js';
import { LitProgressBar } from '../../../../../base-ui/progress-bar/LitProgressBar.js';
import { procedurePool } from '../../../../database/Procedure.js';
import { Utils } from '../../base/Utils.js';

@element('tabpane-perf-analysis')
export class TabPanePerfAnalysis extends BaseElement {
  private currentSelection: SelectionParam | any;
  private pie: LitChartPie | null | undefined;
  private processData!: Array<any>;
  private threadDataMap?: Map<number, any>;
  private pidData!: any[];
  private threadData!: any[];
  private soData!: any[];
  private functionData!: any[];
  private tableThread: LitTable | null | undefined;
  private tableProcess: LitTable | null | undefined;
  private tableSo: LitTable | null | undefined;
  private tableFunction: LitTable | null | undefined;
  private sumCount: any;
  private range: HTMLLabelElement | null | undefined;
  private processMap: Map<number, PerfThread> = new Map<number, PerfThread>();
  private back: HTMLDivElement | null | undefined;
  private tabName: HTMLDivElement | null | undefined;
  private progressEL: LitProgressBar | null | undefined;
  private processName: any;
  private threadName: any;
  private callChainMap!: Map<number, any>;
  private sortColumn: string = '';
  private sortType: number = 0;
  private allProcessCount!: any;
  private allThreadCount!: any;
  private allLibCount!: any;
  private allSymbolCount!: any;
  private currentLevel = -1;
  private currentLevelData!: Array<any>;
  set data(val: SelectionParam | any) {
    if (val == this.currentSelection) {
      return;
    }
    this.clearData();
    this.currentSelection = val;
    this.tableProcess!.style.display = 'grid';
    this.tableThread!.style.display = 'none';
    this.tableSo!.style.display = 'none';
    this.tableFunction!.style.display = 'none';
    this.back!.style.visibility = 'hidden';
    this.range!.textContent =
      'Selected range: ' + parseFloat(((val.rightNs - val.leftNs) / 1000000.0).toFixed(5)) + ' ms';
    if (!this.callChainMap){
      this.getCallChainDataFromWorker();
    }
    queryPerfProcess().then((result) => {
      let processes = result;
      for (let process of processes) {
        this.processMap.set(process.pid, process);
      }
    });
    this.getHiperfProcess(val);
  }
  initElements(): void {
    this.range = this.shadowRoot?.querySelector('#time-range');
    this.pie = this.shadowRoot!.querySelector<LitChartPie>('#chart-pie');
    this.tableProcess = this.shadowRoot!.querySelector<LitTable>('#tb-process-usage');
    this.tableSo = this.shadowRoot!.querySelector<LitTable>('#tb-so-usage');
    this.tableFunction = this.shadowRoot!.querySelector<LitTable>('#tb-function-usage');
    this.tableThread = this.shadowRoot!.querySelector<LitTable>('#tb-thread-usage');
    this.back = this.shadowRoot!.querySelector<HTMLDivElement>('.go-back');
    this.tabName = this.shadowRoot!.querySelector<HTMLDivElement>('.subheading');
    this.progressEL = this.shadowRoot?.querySelector('.progress') as LitProgressBar;
    this.getBack();
  }

  clearData() {
    this.pie!.dataSource = [];
    this.tableProcess!.recycleDataSource = [];
    this.tableThread!.recycleDataSource = [];
    this.tableSo!.recycleDataSource = [];
    this.tableFunction!.recycleDataSource = [];
  }

  getBack() {
    this.back!.addEventListener('click', () => {
      if (this.tabName!.textContent === 'Statistic By Thread Count') {
        this.tableProcess!.style.display = 'grid';
        this.tableThread!.style.display = 'none';
        this.tableThread!.setAttribute('hideDownload', '');
        this.tableProcess?.removeAttribute('hideDownload');
        this.back!.style.visibility = 'hidden';
        this.currentLevel = 0;
        this.currentLevelData = this.pidData;
        this.processPieChart(this.currentSelection);
      } else if (this.tabName!.textContent === 'Statistic By Library Count') {
        this.tableThread!.style.display = 'grid';
        this.tableSo!.style.display = 'none';
        this.tableSo!.setAttribute('hideDownload', '');
        this.tableThread?.removeAttribute('hideDownload');
        this.currentLevel = 1;
        this.currentLevelData = this.threadData;
        this.threadPieChart(this.currentSelection);
      } else if (this.tabName!.textContent === 'Statistic By Function Count') {
        this.tableSo!.style.display = 'grid';
        this.tableFunction!.style.display = 'none';
        this.tableFunction!.setAttribute('hideDownload', '');
        this.tableSo?.removeAttribute('hideDownload');
        this.currentLevel = 2;
        this.currentLevelData = this.soData;
        this.libraryPieChart(this.currentSelection);
      }
    });
  }
  processPieChart(val: any) {
    this.sumCount = this.allProcessCount.allCount;
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.pidData),
      angleField: 'count',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                                <div>ProcessName:${obj.obj.tableName}</div>
                                <div>Count:${obj.obj.countFormat}</div>
                                <div>Percent:${obj.obj.percent}%</div> 
                            </div>
                                `;
      },
      angleClick: (it) => {
        // @ts-ignore
        if (it.tableName != 'other') {
          this.clearData();
          this.back!.style.visibility = 'visible';
          this.tableProcess!.style.display = 'none';
          this.tableThread!.style.display = 'grid';
          this.tableProcess!.setAttribute('hideDownload', '');
          this.tableThread?.removeAttribute('hideDownload');
          this.getHiperfThread(it, val);
          // @ts-ignore
          this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = it.tableName;
          // @ts-ignore
          this.processName = it.tableName;
        }
      },
      hoverHandler: (data) => {
        if (data) {
          this.tableProcess!.setCurrentHover(data);
        } else {
          this.tableProcess!.mouseOut();
        }
      },
      interactions: [
        {
          type: 'element-active',
        },
      ],
    };
    this.tableProcess!.addEventListener('row-hover', (evt: any) => {
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
    this.tabName!.textContent = 'Statistic By Process Count';
    this.pidData.unshift(this.allProcessCount);
    this.tableProcess!.recycleDataSource = this.pidData;
    this.currentLevelData = JSON.parse(JSON.stringify(this.pidData));
    // @ts-ignore
    this.pidData.shift(this.allProcessCount);
    this.tableProcess?.reMeauseHeight();
    this.tableProcess!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
  }
  threadPieChart(val: any) {
    if (val.perfThread.length > 0 && val.perfProcess.length == 0) {
      this.back!.style.visibility = 'hidden';
      this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = '';
      this.tableThread!.style.display = 'grid';
    } else {
      this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = this.processName;
    }
    this.sumCount = this.allThreadCount.allCount;
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.threadData),
      angleField: 'count',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                                <div>ThreadName:${obj.obj.tableName}</div>
                                <div>Count:${obj.obj.countFormat}</div>
                                <div>Percent:${obj.obj.percent}%</div> 
                            </div>
                                `;
      },
      angleClick: (it) => {
        // @ts-ignore
        if (it.tableName != 'other') {
          this.clearData();
          this.back!.style.visibility = 'visible';
          this.tableThread!.style.display = 'none';
          this.tableSo!.style.display = 'grid';
          this.tableThread!.setAttribute('hideDownload', '');
          this.tableSo?.removeAttribute('hideDownload');
          this.getHiperfSo(it, val);
          this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent =
            // @ts-ignore
            this.processName + ' / ' + it.tableName;
          // @ts-ignore
          this.threadName = it.tableName;
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
    this.tableThread!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
    this.tabName!.textContent = 'Statistic By Thread Count';
    this.threadData.unshift(this.allThreadCount);
    this.tableThread!.recycleDataSource = this.threadData;
    this.currentLevelData = JSON.parse(JSON.stringify(this.threadData));
    // @ts-ignore
    this.threadData.shift(this.allThreadCount);
    this.tableThread?.reMeauseHeight();
  }
  libraryPieChart(val: any) {
    this.sumCount = this.allLibCount.allCount;
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.soData),
      angleField: 'count',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                                <div>Library:${obj.obj.tableName}</div>
                                <div>Count:${obj.obj.countFormat}</div>
                                <div>Percent:${obj.obj.percent}%</div> 
                            </div>
                                `;
      },
      angleClick: (it) => {
        // @ts-ignore
        if (it.tableName != 'other') {
          this.clearData();
          this.tableSo!.style.display = 'none';
          this.tableFunction!.style.display = 'grid';
          this.tableSo!.setAttribute('hideDownload', '');
          this.tableFunction?.removeAttribute('hideDownload');
          this.getHiperfFunction(it, val);
          this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent =
            // @ts-ignore
            this.processName + ' / ' + this.threadName + ' / ' + it.tableName;
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
    this.shadowRoot!.querySelector<HTMLDivElement>('.title')!.textContent = this.processName + ' / ' + this.threadName;
    this.tabName!.textContent = 'Statistic By Library Count';
    this.soData.unshift(this.allLibCount);
    this.tableSo!.recycleDataSource = this.soData;
    this.currentLevelData = JSON.parse(JSON.stringify(this.soData));
    // @ts-ignore
    this.soData.shift(this.allLibCount);
    this.tableSo?.reMeauseHeight();
    this.tableSo!.addEventListener('column-click', (evt) => {
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
        currentTable = this.tableProcess;
        break;
      case 1:
        currentTable = this.tableThread;
        break;
      case 2:
        currentTable = this.tableSo;
        break;
      case 3:
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
          arr.shift(this.allProcessCount);
          break;
        case 1:
          // @ts-ignore
          arr.shift(this.allThreadCount);
          break;
        case 2:
          // @ts-ignore
          arr.shift(this.allLibCount);
          break;
        case 3:
          // @ts-ignore
          arr.shift(this.allSymbolCount);
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
      } else if (column == 'countFormat') {
              currentTable!.recycleDataSource = arr.sort((a, b) => {
                return sort == 1 ? a.count - b.count : b.count - a.count;
              });
            } else if (column == 'percent') {
              currentTable!.recycleDataSource = arr.sort((a, b) => {
                return sort == 1 ? a.count - b.count : b.count - a.count;
              });
            }
      switch (this.currentLevel) {
        case 0:
          arr.unshift(this.allProcessCount);
          break;
        case 1:
          arr.unshift(this.allThreadCount);
          break;
        case 2:
          arr.unshift(this.allLibCount);
          break;
        case 3:
          arr.unshift(this.allSymbolCount);
          break;
      }
      currentTable!.recycleDataSource = arr;
    }
  }
  async getHiperfProcess(val: any) {
    this.progressEL!.loading = true;
    await queryHiPerfProcessCount(
      val.leftNs,
      val.rightNs,
      val.perfAll ? [] : val.perfCpus,
      val.perfThread,
      val.perfProcess
    ).then((result) => {
      this.processData = result;
      this.processData = JSON.parse(JSON.stringify(result));
      if (!this.processData || this.processData.length == 0) {
        return;
      }
      let allCount = 0;
      let pidMap = new Map<string, Array<any>>();
      if (val.perfThread.length > 0 && val.perfProcess.length == 0) {
        this.tableProcess!.style.display = 'none';
        this.getHiperfThread(this.processData[0], val);
      } else {
        for (let itemData of result) {
          allCount++;
          if (pidMap.has(itemData.pid)) {
            pidMap.get(itemData.pid)?.push(itemData);
          } else {
            let itemArray = new Array<any>();
            itemArray.push(itemData);
            pidMap.set(itemData.pid, itemArray);
          }
        }
        this.pidData = [];
        pidMap.forEach((value: Array<any>, key: string) => {
          let count = 0;
          let pName = '';
          for (let item of value) {
            let process = this.processMap.get(item.pid);
            pName = item.processName =
              process == null || process == undefined
                ? `Process(${item.pid})`
                : `${process!.processName || 'Process'}(${item.pid})`;
            count++;
          }
          const pidData = {
            tableName: pName,
            pid: key,
            percent: ((count / allCount) * 100).toFixed(2),
            countFormat: Utils.timeMsFormat2p(count),
            count: count,
          };
          this.pidData.push(pidData);
        });
        this.pidData.sort((a, b) => b.count - a.count);
        this.allProcessCount = this.totalCountData(allCount);
        this.currentLevel = 0;
        this.progressEL!.loading = false;
        this.processPieChart(val);
      }
    });
  }
  getHiperfThread(item: any, val: any) {
    this.progressEL!.loading = true;
    this.threadDataMap = new Map<number, any>();
    let threadMap = new Map<string, Array<any>>();
    let pid = item.pid;
    let allCount = 0;
    if (!this.processData || this.processData.length == 0) {
      return;
    }
    for (let itemData of this.processData) {
      if (itemData.pid !== pid) {
        continue;
      }
      allCount++;
      if (threadMap.has(itemData.tid)) {
        threadMap.get(itemData.tid)?.push(itemData);
      } else {
        let itemArray = new Array<any>();
        itemArray.push(itemData);
        threadMap.set(itemData.tid, itemArray);
      }
    }
    this.threadData = [];
    threadMap.forEach((value: Array<any>, key: string) => {
      let threadCount = 0;
      let tName = '';
      for (let item of value) {
        threadCount++;
        tName =
          item.threadName == null || item.threadName == undefined
            ? `Thread(${item.tid})`
            : `${item.threadName}(${item.tid})`;
      }
      const threadData = {
        pid: item.pid,
        tid: key,
        tableName: tName,
        countFormat: Utils.timeMsFormat2p(threadCount),
        count: threadCount,
        percent: ((threadCount / allCount) * 100).toFixed(2),
      };
      this.threadData.push(threadData);
    });
    this.allThreadCount = this.totalCountData(allCount);
    this.currentLevel = 1;
    this.threadData.sort((a, b) => b.count - a.count);
    this.progressEL!.loading = false;
    this.threadPieChart(val);
  }
  getHiperfSo(item: any, val: any) {
    this.progressEL!.loading = true;
    let parentCount = item.count;
    let tid = item.tid;
    let pid = item.pid;
    let allCount = 0;
    let libMap = new Map<number, Array<any>>();
    if (!this.processData || this.processData.length == 0) {
      return;
    }
    for (let itemData of this.processData) {
      if (itemData.pid !== pid || itemData.tid !== tid) {
        continue;
      }
      let lastLibCallChain = this.callChainMap!.get(itemData.callchain_id);
      if (!lastLibCallChain) {
        continue;
      }
      allCount++;
      if (libMap.has(lastLibCallChain.fileId)) {
        libMap.get(lastLibCallChain.fileId)?.push(lastLibCallChain);
      } else {
        let dataArray = new Array<any>();
        dataArray.push(lastLibCallChain);
        libMap.set(lastLibCallChain.fileId, dataArray);
      }
    }
    this.soData = [];
    libMap.forEach((libItems, libId) => {
      let libCount = 0;
      let libName = '';
      for (let libItem of libItems) {
        libCount++;
        libName = libItem.path;
      }
      let libPath = libName?.split('/');
      if (libPath) {
        libName = libPath[libPath.length - 1];
      }
      const libData = {
        pid: item.pid,
        tid: item.tid,
        percent: ((libCount / parentCount) * 100).toFixed(2),
        countFormat: Utils.timeMsFormat2p(libCount),
        count: libCount,
        tableName: libName,
        libId: libId,
      };
      this.soData.push(libData);
    });
    this.allLibCount = this.totalCountData(allCount);
    this.soData.sort((a, b) => b.count - a.count);
    this.currentLevel = 2;
    this.progressEL!.loading = false;
    this.libraryPieChart(val);
  }
  getHiperfFunction(item: any, val: any) {
    this.progressEL!.loading = true;
    this.shadowRoot!.querySelector<HTMLDivElement>('.subheading')!.textContent = 'Statistic By Function Count';
    let parentCount = item.count;
    let tid = item.tid;
    let pid = item.pid;
    let libId = item.libId;
    let allCount = 0;
    let symbolMap = new Map<number, Array<any>>();
    if (!this.processData || this.processData.length == 0) {
      return;
    }
    for (let itemData of this.processData) {
      if (itemData.pid !== pid || itemData.tid !== tid) {
        continue;
      }
      let dataCallChain = this.callChainMap!.get(itemData.callchain_id);
      if (!dataCallChain || dataCallChain.fileId !== libId) {
        continue;
      }
      allCount++;
      if (symbolMap.has(dataCallChain.symbolId)) {
        symbolMap.get(dataCallChain.symbolId)?.push(dataCallChain);
      } else {
        let dataArray = new Array<any>();
        dataArray.push(dataCallChain);
        symbolMap.set(dataCallChain.symbolId, dataArray);
      }
    }
    this.functionData = [];
    symbolMap.forEach((symbolItems, symbolId) => {
      let symbolCount = 0;
      let symbolName = '';
      for (let symbolItem of symbolItems) {
        symbolCount++;
        symbolName = symbolItem.name;
      }
      let symbolPath = symbolName?.split('/');
      if (symbolPath) {
        symbolName = symbolPath[symbolPath.length - 1];
      }
      const symbolData = {
        pid: item.pid,
        tid: item.tid,
        percent: ((symbolCount / parentCount) * 100).toFixed(2),
        countFormat: Utils.timeMsFormat2p(symbolCount),
        count: symbolCount,
        tableName: symbolName,
      };
      this.functionData.push(symbolData);
    });
    this.functionData.sort((a, b) => b.count - a.count);
    this.allSymbolCount = this.totalCountData(allCount);
    this.currentLevel = 3;
    this.progressEL!.loading = false;
    this.sumCount = this.allSymbolCount.allCount;
    this.pie!.config = {
      appendPadding: 0,
      data: this.getPieChartData(this.functionData),
      angleField: 'count',
      colorField: 'tableName',
      radius: 1,
      label: {
        type: 'outer',
      },
      tip: (obj) => {
        return `<div>
                    <div>Function:${obj.obj.tableName}</div>
                    <div>Count:${obj.obj.countFormat}</div>
                    <div>Percent:${obj.obj.percent}%</div>
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
    this.functionData.unshift(this.allSymbolCount);
    this.tableFunction!.recycleDataSource = this.functionData;
    this.currentLevelData = JSON.parse(JSON.stringify(this.functionData));
    // @ts-ignore
    this.functionData.shift(this.allSymbolCount);
    this.tableFunction?.reMeauseHeight();
    this.tableFunction!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByColumn(evt.detail.key, evt.detail.sort);
    });
  }
  totalCountData(count:any){
    let allCount;
    allCount = {
      countFormat: Utils.timeMsFormat2p(count),
      percent: ((count / count) * 100).toFixed(2),
      count: 0,
      allCount:count,
      pid: '',
    };
    return allCount;
  }

  getPieChartData(res: any[]) {
    if (res.length > 20) {
      let pieChartArr: any[] = [];
      let other: any = {
        tableName: 'other',
        count: 0,
        percent: 0,
        countFormat: 0,
      };
      for (let i = 0; i < res.length; i++) {
        if (i < 19) {
          pieChartArr.push(res[i]);
        } else {
          other.count += res[i].count;
          other.countFormat = Utils.timeMsFormat2p(other.count);
          other.percent = ((other.count / this.sumCount) * 100).toFixed(2);
        }
      }
      pieChartArr.push(other);
      return pieChartArr;
    }
    return res;
  }
  getCallChainDataFromWorker() {
    this.getDataByWorker([], (results: any) => {
      this.callChainMap = results;
    });
  }
  getDataByWorker(args: any[], handler: Function) {
    this.progressEL!.loading = true;
    procedurePool.submitWithName('logic0', 'perf-queryPerfAnalysisCallChain', args, undefined, (results: any) => {
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
                     <div class="table-box" style="height:auto;">
                    <lit-table id="tb-process-usage" style="height:60vh;display: none" >
                        <lit-table-column width="1fr" title="ProcessName" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Weight" data-index="countFormat" key="countFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="percent" key="percent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    <lit-table id="tb-thread-usage" style="height:60vh;display: none"hideDownload>
                        <lit-table-column width="1fr" title="ThreadName" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Weight" data-index="countFormat" key="countFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="percent" key="percent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    <lit-table id="tb-so-usage" style="height:60vh;display: none"hideDownload>
                        <lit-table-column width="1fr" title="Library" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Weight" data-index="countFormat" key="countFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="percent" key="percent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    <lit-table id="tb-function-usage" style="height:60vh;display: none"hideDownload>
                        <lit-table-column width="1fr" title="Function" data-index="tableName" key="tableName" align="flex-start"order></lit-table-column>
                        <lit-table-column width="1fr" title="Weight" data-index="countFormat" key="countFormat" align="flex-start" order></lit-table-column>
                        <lit-table-column width="1fr" title="%" data-index="percent" key="percent" align="flex-start"order></lit-table-column>
                    </lit-table>
                    </div>

        </div>
`;
  }
}

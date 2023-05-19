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
import '../../../../../base-ui/table/lit-table.js';
import { ConstructorItem, FileType } from '../../../../../js-heap/model/UiStruct.js';
import { HeapDataInterface } from '../../../../../js-heap/HeapDataInterface.js';
import { LitTableColumn } from '../../../../../base-ui/table/lit-table-column.js';
import '../../../../../base-ui/table/lit-table-column.js';
import { TabPaneJsMemoryFilter } from '../TabPaneJsMemoryFilter.js';
import '../TabPaneJsMemoryFilter.js';
import { SelectionParam } from '../../../../bean/BoxSelection.js';
import { SpJsMemoryChart } from '../../../chart/SpJsMemoryChart.js';
import { LitProgressBar } from '../../../../../base-ui/progress-bar/LitProgressBar.js';
import '../../../../../base-ui/progress-bar/LitProgressBar.js';
import '../../../../../base-ui/slicer/lit-slicer.js';
import { HeapSnapshotStruct } from '../../../../database/ui-worker/ProcedureWorkerHeapSnapshot.js';

@element('tabpane-summary')
export class TabPaneSummary extends BaseElement {
  private tbl: LitTable | null | undefined;
  private tbs: LitTable | null | undefined;
  private stackTable: LitTable | null | undefined;
  private summary: Array<ConstructorItem> = [];
  private retainsData: Array<ConstructorItem> = [];
  private stackData: Array<any> = [];
  private stackText: HTMLElement | undefined;
  static fileSize: number;
  private tabFilter: TabPaneJsMemoryFilter | undefined | null;
  private progressEL: LitProgressBar | null | undefined;
  private summaryFilter: Array<any> = [];
  private summaryData: Array<ConstructorItem> = [];
  private search: HTMLInputElement | null | undefined;
  private tbsTable: HTMLDivElement | null | undefined;
  private tblTable: HTMLDivElement | null | undefined;
  private rightTheadTable: HTMLDivElement | null | undefined;
  private leftTheadTable: HTMLDivElement | null | undefined;
  private leftArray: ConstructorItem[] = [];
  private rightArray: ConstructorItem[] = [];
  private stack: HTMLLIElement | null | undefined;
  private retainers: HTMLLIElement | null | undefined;

  set data(val: SelectionParam | any) {}

  initElements(): void {
    this.tbl = this.shadowRoot?.querySelector<LitTable>('#left');
    this.tbs = this.shadowRoot?.querySelector<LitTable>('#right');
    this.stackTable = this.shadowRoot?.querySelector<LitTable>('#stackTable');
    this.stackText = this.shadowRoot?.querySelector('.stackText') as HTMLElement;
    this.tabFilter = this.shadowRoot?.querySelector('#filter') as TabPaneJsMemoryFilter;
    this.progressEL = this.shadowRoot?.querySelector('.progress') as LitProgressBar;
    this.search = this.tabFilter?.shadowRoot?.querySelector('#filter-input') as HTMLInputElement;
    this.stack = this.shadowRoot?.querySelector('#stack') as HTMLLIElement;
    this.retainers = this.shadowRoot?.querySelector('#retainers') as HTMLLIElement;
    this.tblTable = this.tbl!.shadowRoot?.querySelector('.table') as HTMLDivElement;
    this.rightTheadTable = this.tbs!.shadowRoot?.querySelector('.thead') as HTMLDivElement;
    this.leftTheadTable = this.tbl!.shadowRoot?.querySelector('.thead') as HTMLDivElement;
    this.tbl!.addEventListener('row-click', (evt: any) => {
      this.rightTheadTable!.removeAttribute('sort');
      this.tbsTable = this.tbs!.shadowRoot?.querySelector('.table') as HTMLDivElement;
      this.tbsTable!.scrollTop = 0;
      let data = evt.detail.data as ConstructorItem;
      (data as any).isSelected = true;
      this.retainsData = [];
      this.retainsData = HeapDataInterface.getInstance().getRetains(data);
      this.retainsData.forEach((element: any) => {
        let shallow = Math.round((element.shallowSize / TabPaneSummary.fileSize) * 100) + '%';
        let retained = Math.round((element.retainedSize / TabPaneSummary.fileSize) * 100) + '%';
        element.shallowPercent = shallow;
        element.retainedPercent = retained;
        if (element.distance >= 100000000 || element.distance === -5) {
          element.distance = '-';
        }
        let nodeId = element.nodeName + ` @${element.id}`;
        element.objectName = element.edgeName + '\xa0' + 'in' + '\xa0' + nodeId;
      });
      if (this.retainsData.length > 0) {
        if (this.retainsData[0].distance > 1) {
          this.retainsData[0].getChildren();
        }
        let i = 0;
        let that = this;
        let retainsTable = function () {
          const getList = function (list: any) {
            list.forEach(function (row: any) {
              let shallow = Math.round((row.shallowSize / TabPaneSummary.fileSize) * 100) + '%';
              let retained = Math.round((row.retainedSize / TabPaneSummary.fileSize) * 100) + '%';
              row.shallowPercent = shallow;
              row.retainedPercent = retained;
              let nodeId = row.nodeName.concat(` @${row.id}`);
              row.objectName = row.edgeName + '\xa0' + 'in' + '\xa0' + nodeId;
              if (row.distance >= 100000000 || row.distance === -5) {
                row.distance = '-';
              }
              i++;
              if (i < that.retainsData[0].distance - 1 && list[0].distance != '-') {
                list[0].getChildren();
                if (row.hasNext) {
                  getList(row.children);
                }
              } else {
                return;
              }
            });
          };
          getList(that.retainsData[0].children);
        };
        retainsTable();
        this.tbs!.snapshotDataSource = this.retainsData;
      } else {
        this.tbs!.snapshotDataSource = [];
      }
      if (SpJsMemoryChart.file.file_name.includes('Timeline')) {
        this.stackData = HeapDataInterface.getInstance().getAllocationStackData(data);
        if (this.stackData.length > 0) {
          this.stackTable!.recycleDataSource = this.stackData;
          this.stackText!.textContent = '';
          this.stackText!.style.display = 'none';
          if (this.stack!.className == 'active') {
            this.stackTable!.style.display = 'grid';
            this.tbs!.style.display = 'none';
          }
        } else {
          this.stackText!.style.display = 'flex';
          this.stackTable!.recycleDataSource = [];
          this.stackTable!.style.display = 'none';
          if (this.retainers!.className == 'active') {
            this.stackText!.style.display = 'none';
          }
          if (this.retainsData === undefined || this.retainsData.length === 0) {
            this.stackText!.textContent = '';
          } else {
            this.stackText!.textContent =
              'Stack was not recorded for this object because it had been allocated before this profile recording started.';
          }
        }
      }
      new ResizeObserver(() => {
        this.tbs!.style.height = 'calc(100% - 30px)';
        this.tbs!.reMeauseHeight();
        this.stackTable!.style.height = 'calc(100% - 30px)';
        this.stackTable!.reMeauseHeight();
      }).observe(this.parentElement!);
      if ((evt.detail as any).callBack) {
        // @ts-ignore
        (evt.detail as any).callBack(true);
      }
    });

    this.tbs!.addEventListener('row-click', (evt: any) => {
      let data = evt.detail.data as ConstructorItem;
      (data as any).isSelected = true;
      if ((evt.detail as any).callBack) {
        // @ts-ignore
        (evt.detail as any).callBack(true);
      }
    });

    this.tbl!.addEventListener('icon-click', (evt: any) => {
      if (evt.detail.data.status) {
        evt.detail.data.getChildren();
        evt.detail.data.children.sort(function (a: ConstructorItem, b: ConstructorItem) {
          return b.retainedSize - a.retainedSize;
        });
        evt.detail.data.children.forEach((element: any) => {
          let shallow = Math.round((element.shallowSize / TabPaneSummary.fileSize) * 100) + '%';
          let retained = Math.round((element.retainedSize / TabPaneSummary.fileSize) * 100) + '%';
          element.shallowPercent = shallow;
          element.retainedPercent = retained;
          if (element.distance >= 100000000 || element.distance === -5) {
            element.distance = '-';
          }
          let nodeId = element.nodeName.concat(` @${element.id}`);
          element.objectName = nodeId;
          if (element.edgeName != '') {
            element.objectName = element.edgeName + '\xa0' + '::' + '\xa0' + nodeId;
          }
        });
      } else {
        evt.detail.data.status = true;
      }
      if (this.search!.value != '') {
        if (this.leftTheadTable!.hasAttribute('sort')) {
          this.tbl!.snapshotDataSource = this.leftArray;
        } else {
          this.tbl!.snapshotDataSource = this.summaryFilter;
        }
      } else {
        if (this.leftTheadTable!.hasAttribute('sort')) {
          this.tbl!.snapshotDataSource = this.leftArray;
        } else {
          this.tbl!.snapshotDataSource = this.summary;
        }
      }
      new ResizeObserver(() => {
        if (this.parentElement?.clientHeight !== 0) {
          this.tbl!.style.height = '100%';
          this.tbl!.reMeauseHeight();
        }
      }).observe(this.parentElement!);
    });
    this.tbs!.addEventListener('icon-click', (evt: any) => {
      let that = this;
      if (evt.detail.data.status) {
        evt.detail.data.getChildren();
        let i = 0;
        let retainsTable = function () {
          const getList = function (list: any) {
            list.forEach(function (row: any) {
              let shallow = Math.round((row.shallowSize / TabPaneSummary.fileSize) * 100) + '%';
              let retained = Math.round((row.retainedSize / TabPaneSummary.fileSize) * 100) + '%';
              row.shallowPercent = shallow;
              row.retainedPercent = retained;
              let nodeId = row.nodeName.concat(` @${row.id}`);
              row.objectName = row.edgeName + '\xa0' + 'in' + '\xa0' + nodeId;
              if (row.distance >= 100000000 || row.distance === -5) {
                row.distance = '-';
              }
              i++;
              if (i < evt.detail.data.distance - 1 && list[0].distance != '-') {
                list[0].getChildren();
                if (row.hasNext) {
                  getList(row.children);
                }
              } else {
                return;
              }
            });
          };
          getList(evt.detail.data.children);
        };
        retainsTable();
      } else {
        evt.detail.data.status = true;
      }
      if (this.rightTheadTable!.hasAttribute('sort')) {
        this.tbs!.snapshotDataSource = this.rightArray;
      } else {
        this.tbs!.snapshotDataSource = this.retainsData;
      }
      new ResizeObserver(() => {
        if (this.parentElement?.clientHeight !== 0) {
          this.tbs!.style.height = 'calc(100% - 30px)';
          this.tbs!.reMeauseHeight();
        }
      }).observe(this.parentElement!);
    });

    this.tbl!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByLeftTable(evt.detail.key, evt.detail.sort);
    });
    this.tbs!.addEventListener('column-click', (evt) => {
      // @ts-ignore
      this.sortByRightTable(evt.detail.key, evt.detail.sort);
    });
    this.classFilter();
  }

  setSnapshotData(
    data: HeapSnapshotStruct,
    dataList: Array<HeapSnapshotStruct>,
    scrollCallback: ((d: any, ds: any) => void) | undefined
  ) {
    if (scrollCallback) {
      scrollCallback(data, dataList);
    }
    this.summary = [];
    this.initSummaryData(data.id);
  }

  initSummaryData(fileId: number, maxNodeId?: number, minNodeId?: number) {
    this.clear();
    this.summary = [];
    this.progressEL!.loading = true;
    this.summary = HeapDataInterface.getInstance().getClassesListForSummary(fileId, minNodeId, maxNodeId);
    let dataList = HeapDataInterface.getInstance().getClassesListForSummary(SpJsMemoryChart.file.id);
    TabPaneSummary.fileSize = dataList.reduce((sum, e) => sum + e.shallowSize, 0);
    this.summary.forEach((element: any) => {
      if (element.childCount > 1) {
        let count = element.nodeName + ` ×${element.childCount}`;
        element.objectName = count;
      } else {
        element.objectName = element.nodeName;
      }
      let shallow = Math.round((element.shallowSize / TabPaneSummary.fileSize) * 100) + '%';
      let retained = Math.round((element.retainedSize / TabPaneSummary.fileSize) * 100) + '%';
      element.shallowPercent = shallow;
      element.retainedPercent = retained;
      if (element.distance >= 100000000 || element.distance === -5) {
        element.distance = '-';
      }
    });
    if (this.summary.length > 0) {
      this.summaryData = this.summary;
      this.tbl!.snapshotDataSource = this.summary;
      this.progressEL!.loading = false;
    } else {
      this.tbl!.snapshotDataSource = [];
      this.progressEL!.loading = false;
    }
    new ResizeObserver(() => {
      if (this.parentElement?.clientHeight !== 0) {
        this.tbl!.style.height = '100%';
        this.tbl!.reMeauseHeight();
      }
    }).observe(this.parentElement!);
    if (SpJsMemoryChart.file.file_name.includes('Timeline')) {
      this.retainers!.classList.add('active');
      this.stack!.style.display = 'flex';
      this.retainers!.style.pointerEvents = 'auto';
    } else {
      this.stack!.style.display = 'none';
      this.retainers!.classList.remove('active');
      this.retainers!.style.pointerEvents = 'none';
    }
    this.clickToggleTable();
  }

  sortByLeftTable(column: string, sort: number) {
    switch (sort) {
      case 0:
        if (this.search!.value === '') {
          this.tbl!.snapshotDataSource = this.summary;
        } else {
          this.tbl!.snapshotDataSource = this.summaryFilter;
        }
        break;
      default:
        if (this.search!.value === '') {
          this.leftArray = [...this.summary];
        } else {
          this.leftArray = [...this.summaryFilter];
        }
        switch (column) {
          case 'distance':
            this.tbl!.snapshotDataSource = this.leftArray.sort((a, b) => {
              return sort === 1 ? a.distance - b.distance : b.distance - a.distance;
            });
            this.leftArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1 ? a.distance - b.distance : b.distance - a.distance;
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbl!.snapshotDataSource = this.leftArray;
            break;
          case 'shallowSize':
            this.tbl!.snapshotDataSource = this.leftArray.sort((a, b) => {
              return sort === 1 ? a.shallowSize - b.shallowSize : b.shallowSize - a.shallowSize;
            });
            this.leftArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1 ? a.shallowSize - b.shallowSize : b.shallowSize - a.shallowSize;
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbl!.snapshotDataSource = this.leftArray;
            break;
          case 'retainedSize':
            this.tbl!.snapshotDataSource = this.leftArray.sort((a, b) => {
              return sort === 1 ? a.retainedSize - b.retainedSize : b.retainedSize - a.retainedSize;
            });
            this.leftArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1 ? a.retainedSize - b.retainedSize : b.retainedSize - a.retainedSize;
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbl!.snapshotDataSource = this.leftArray;
            break;
          case 'objectName':
            this.tbl!.snapshotDataSource = this.leftArray.sort((a, b) => {
              return sort === 1
                ? (a.objectName + '').localeCompare(b.objectName + '')
                : (b.objectName + '').localeCompare(a.objectName + '');
            });
            this.leftArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1
                      ? (a.objectName + '').localeCompare(b.objectName + '')
                      : (b.objectName + '').localeCompare(a.objectName + '');
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbl!.snapshotDataSource = this.leftArray;
            break;
        }
        break;
    }
  }

  sortByRightTable(column: string, sort: number) {
    switch (sort) {
      case 0:
        this.tbs!.snapshotDataSource = this.retainsData;
        break;
      default:
        this.rightArray = [...this.retainsData];
        switch (column) {
          case 'distance':
            this.tbs!.snapshotDataSource = this.rightArray.sort((a, b) => {
              return sort === 1 ? a.distance - b.distance : b.distance - a.distance;
            });
            this.rightArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1 ? a.distance - b.distance : b.distance - a.distance;
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbs!.snapshotDataSource = this.rightArray;
            break;
          case 'shallowSize':
            this.tbs!.snapshotDataSource = this.rightArray.sort((a, b) => {
              return sort === 1 ? a.shallowSize - b.shallowSize : b.shallowSize - a.shallowSize;
            });
            this.rightArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1 ? a.shallowSize - b.shallowSize : b.shallowSize - a.shallowSize;
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbs!.snapshotDataSource = this.rightArray;
            break;
          case 'retainedSize':
            this.tbs!.snapshotDataSource = this.rightArray.sort((a, b) => {
              return sort === 1 ? a.retainedSize - b.retainedSize : b.retainedSize - a.retainedSize;
            });
            this.rightArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1 ? a.retainedSize - b.retainedSize : b.retainedSize - a.retainedSize;
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbs!.snapshotDataSource = this.rightArray;
            break;
          case 'objectName':
            this.tbs!.snapshotDataSource = this.rightArray.sort((a, b) => {
              return sort === 1
                ? (a.objectName + '').localeCompare(b.objectName + '')
                : (b.objectName + '').localeCompare(a.objectName + '');
            });
            this.rightArray.forEach((list) => {
              let retainsTable = function () {
                const getList = function (list: any) {
                  list.sort((a: any, b: any) => {
                    return sort === 1
                      ? (a.objectName + '').localeCompare(b.objectName + '')
                      : (b.objectName + '').localeCompare(a.objectName + '');
                  });
                  list.forEach(function (row: any) {
                    if (row.children.length > 0) {
                      getList(row.children);
                    }
                  });
                };
                getList(list.children);
              };
              retainsTable();
            });
            this.tbs!.snapshotDataSource = this.rightArray;
            break;
        }
        break;
    }
  }

  clickToggleTable() {
    let lis = this.shadowRoot?.querySelectorAll('li') as any;
    let that = this;
    lis.forEach((li: any, i: any) => {
      lis[i].onclick = function () {
        for (let i = 0; i < lis.length; i++) {
          lis[i].className = '';
        }
        switch (li.textContent) {
          case 'Retainers':
            that.stackTable!.style.display = 'none';
            that.stackText!.style.display = 'none';
            that.tbs!.style.display = 'flex';
            that.tbs!.snapshotDataSource = that.retainsData;
            break;
          case 'Allocation stack':
            if (that.stackData.length > 0) {
              that.stackText!.style.display = 'none';
              that.stackTable!.style.display = 'flex';
              that.stackTable!.recycleDataSource = that.stackData;
            } else {
              that.stackText!.style.display = 'flex';
              if (that.retainsData === undefined || that.retainsData.length === 0) {
                that.stackText!.textContent = '';
              } else {
                that.stackText!.textContent =
                  'Stack was not recorded for this object because it had been allocated before this profile recording started.';
              }
            }
            that.tbs!.style.display = 'none';
            break;
        }
        this.className = 'active';
      };
    });
  }

  classFilter() {
    this.search!.addEventListener('keyup', () => {
      this.summaryFilter = [];
      this.summaryData.forEach((a: any, key: number) => {
        if (a.objectName.toLowerCase().includes(this.search!.value.toLowerCase())) {
          this.summaryFilter.push(a);
        } else {
        }
      });
      this.tbl!.snapshotDataSource = this.summaryFilter;
      let summaryTable = this.tbl!.shadowRoot?.querySelector('.table') as HTMLDivElement;
      summaryTable.scrollTop = 0;
    });
  }

  clear() {
    this.tbs!.snapshotDataSource = [];
    this.stackTable!.recycleDataSource = [];
    this.retainsData = [];
    this.stackText!.textContent = '';
    this.search!.value = '';
    this.stack!.classList.remove('active');
    this.tblTable!.scrollTop = 0;
    this.stackTable!.style.display = 'none';
    this.stackText!.style.display = 'none';
    this.tbs!.style.display = 'flex';
    this.rightTheadTable!.removeAttribute('sort');
    this.leftTheadTable!.removeAttribute('sort');
  }

  connectedCallback() {
    super.connectedCallback();
    let filterHeight = 0;
    new ResizeObserver((entries) => {
      let tabPaneFilter = this.shadowRoot!.querySelector('#filter') as HTMLElement;
      if (tabPaneFilter.clientHeight > 0) filterHeight = tabPaneFilter.clientHeight;
      if (this.parentElement!.clientHeight > filterHeight) {
        tabPaneFilter.style.display = 'flex';
      } else {
        tabPaneFilter.style.display = 'none';
      }
    }).observe(this.parentElement!);
  }

  initHtml(): string {
    return `
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px 0 10px;
            height: calc(100% - 10px - 31px);
        }
        .container {
            /* overflow: hidden; */
            width: 100%;
            height: 100%;
        }
        .container-left {
            height: 79.5vh;
            position: relative;
            float: left;
            max-width: 70%
        }
        .container-right {
            height: 70vh;
            box-sizing: border-box;
            overflow: hidden;
        }
        .left_table {
            position: absolute;
            top: 0;
            right: 5px;
            bottom: 0;
            left: 0;
        }
        .text{
            opacity: 0.9;
            font-family: Helvetica;
            font-size: 16px;
            color: #000000;
            line-height: 28px;
            font-weight: 400;
            margin-left: 70%;
        }
        ul{
            display: inline-flex;
            margin-top: 0px;
            width: 40%;
            position: absolute;
            padding-left: 5px;
        }
        li{
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
            opacity: 0.9;
            font-family: Helvetica;
            font-size: 16px;
            color: #000000;
            line-height: 28px;
            font-weight: 400;
            cursor: pointer;
        }
        .active{
            border-bottom:2px solid #6C9BFA;
        }
        .stackText{
            opacity: 0.9;
            font-family: Helvetica;
            font-size: 16px;
            color: #000000;
            line-height: 28px;
            font-weight: 400;
        }
        tab-pane-filter {
            border: solid rgb(216,216,216) 1px;
            float: left;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
        .progress{
            bottom: 33px;
            position: absolute;
            height: 1px;
            left: 0;
            right: 0;
        }
        selector{
            display: none;
        }
        .show{
            display: flex;
            flex: 1;
        }
        .retainers{
            height: 30px;
            /* background: #D8D8D8; */
            width: 100%;
            display: flex;
        }
        #right{
            height: calc(100% - 30px);
        }
    </style>
    <div style="display: flex;flex-direction: row;height: 100%;">
    <selector id='show_table' class="show">
        <lit-slicer style="width:100%">
        <div id="left_table" style="width: 65%">
            <lit-table id="left" style="height: auto" tree>
                <lit-table-column width="40%" title="Constructor" data-index="objectName" key="objectName" align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="2fr" title="Distance" data-index="distance" key="distance" align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="2fr" title="ShallowSize" data-index="shallowSize" key="shallowSize" align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="1fr" title="" data-index="shallowPercent" key="shallowPercent" align="flex-start">
                </lit-table-column>
                <lit-table-column width="2fr" title="RetainedSize" data-index="retainedSize" key="retainedSize" align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="1fr" title="" data-index="retainedPercent" key="retainedPercent" align="flex-start">
                </lit-table-column>
            </lit-table>
        </div>
        <lit-slicer-track ></lit-slicer-track>
        <div style="flex: 1;display: flex; flex-direction: row;">
            <div style="flex: 1;display: block;">
                <div class="retainers">
                    <ul>
                        <li href="#" id="retainers" style="width: 80px; text-align: center;" class="active">Retainers</li>
                        <li href="#" id="stack" style="width: 120px; text-align: center; display: none; padding-left: 10px;">Allocation stack</li>
                    </ul>
                </div>
                <lit-table id="right" tree>
                    <lit-table-column width="40%" title="Object" data-index="objectName" key="objectName" align="flex-start" order>
                    </lit-table-column>
                    <lit-table-column width="2fr" title="Distance" data-index="distance" key="distance" align="flex-start" order>
                    </lit-table-column>
                    <lit-table-column width="2fr" title="ShallowSize" data-index="shallowSize" key="shallowSize" align="flex-start" order>
                    </lit-table-column>
                    <lit-table-column width="1fr" title="" data-index="shallowPercent" key="shallowPercent" align="flex-start">
                    </lit-table-column>
                    <lit-table-column width="2fr" title="RetainedSize" data-index="retainedSize" key="retainedSize" align="flex-start" order>
                    </lit-table-column>
                    <lit-table-column width="1fr" title="" data-index="retainedPercent" key="retainedPercent" align="flex-start">
                    </lit-table-column>
                </lit-table>
                <text class="stackText" style="display: none;"></text>
                <lit-table id="stackTable" style="height: auto; display: none" hideDownload>
                    <lit-table-column width="100%" title="" data-index="name" key="name" align="flex-start" order>
                    </lit-table-column>
                </lit-table>
            </div>
        </div>
        </lit-slicer>
    </selector>
    <tab-pane-js-memory-filter id="filter" input inputLeftText></tab-pane-js-memory-filter>
    <lit-progress-bar class="progress"></lit-progress-bar>
    </div>
    `;
  }
}

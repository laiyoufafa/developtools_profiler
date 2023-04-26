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
import { LitProgressBar } from '../../../../../base-ui/progress-bar/LitProgressBar.js';
import { FrameChart } from '../../../chart/FrameChart.js';
import { DisassemblingWindow } from '../../../DisassemblingWindow.js';
import { SelectionParam } from '../../../../bean/BoxSelection.js';
import { ChartMode } from '../../../../bean/FrameChartStruct.js';
import { FilterData, TabPaneFilter } from '../TabPaneFilter.js';
import { procedurePool } from '../../../../database/Procedure.js';
import { FileMerageBean } from '../../../../database/logic-worker/ProcedureLogicWorkerFileSystem.js';

@element('tabpane-nm-calltree')
export class TabpaneNMCalltree extends BaseElement {
    private tbl: LitTable | null | undefined;
    private tbr: LitTable | null | undefined;
    private progressEL: LitProgressBar | null | undefined;
    private rightSource: Array<FileMerageBean> = [];
    private filter: any;
    private dataSource: any[] = [];
    private native_type: Array<string> = [
        'All Heap & Anonymous VM',
        'All Heap',
        'All Anonymous VM',
    ];
    private sortKey = 'heapSizeStr';
    private sortType = 0;
    private currentSelectedData: any = undefined;
    private frameChart: FrameChart | null | undefined;
    private isChartShow: boolean = false;
    private systmeRuleName = '/system/';
    private numRuleName = '/max/min/';
    private modal: DisassemblingWindow | null | undefined;
    private needShowMenu = true;
    private searchValue: string = '';
    private loadingList: number[] = [];
    private loadingPage: any;
    private currentSelection: SelectionParam | undefined;
    private filterAllocationType: string = '0';
    private filterNativeType: string = '0';
    private filterResponseType: number = -1;
    private filterResponseSelect: string = '0';
    private responseTypes: any[] = [];

    set data(val: SelectionParam | any) {
        if (val == this.currentSelection) {
            return;
        }
        this.searchValue = '';
        this.currentSelection = val;
        this.modal!.style.display = 'none';
        this.tbl!.style.visibility = 'visible';
        if (this.parentElement!.clientHeight > this.filter!.clientHeight) {
            this.filter!.style.display = 'flex';
        } else {
            this.filter!.style.display = 'none';
        }
        this.filter!.initializeFilterTree(
            true,
            true,
            val.nativeMemory.length > 0
        );
        this.filter!.filterValue = '';
        this.initFilterTypes();
        this.progressEL!.loading = true;
        this.loadingPage.style.visibility = 'visible';
        let types: Array<string | number> = [];
        if (val.nativeMemory.length > 0) {
            if (val.nativeMemory.indexOf(this.native_type[0]) != -1) {
                types.push("'AllocEvent'");
                types.push("'MmapEvent'");
            } else {
                if (val.nativeMemory.indexOf(this.native_type[1]) != -1) {
                    types.push("'AllocEvent'");
                }
                if (val.nativeMemory.indexOf(this.native_type[2]) != -1) {
                    types.push("'MmapEvent'");
                }
            }
        } else {
            if (val.nativeMemoryStatistic.indexOf(this.native_type[0]) != -1) {
                types.push(0);
                types.push(1);
            } else {
                if (
                    val.nativeMemoryStatistic.indexOf(this.native_type[1]) != -1
                ) {
                    types.push(0);
                }
                if (
                    val.nativeMemoryStatistic.indexOf(this.native_type[2]) != -1
                ) {
                    types.push(1);
                }
            }
        }

        this.getDataByWorkerQuery(
            {
                leftNs: val.leftNs,
                rightNs: val.rightNs,
                types,
            },
            (results: any[]) => {
                this.setLTableData(results);
                this.tbr!.recycleDataSource = [];
                this.frameChart!.mode = ChartMode.Byte;
                this.frameChart!.data = this.dataSource;
                this.frameChart?.updateCanvas(true, this.clientWidth);
                this.frameChart?.calculateChartData();
            }
        );
    }

    getParentTree(
        src: Array<FileMerageBean>,
        target: FileMerageBean,
        parents: Array<FileMerageBean>
    ): boolean {
        for (let call of src) {
            if (call.id == target.id) {
                parents.push(call);
                return true;
            } else {
                if (
                    this.getParentTree(
                        call.children as Array<FileMerageBean>,
                        target,
                        parents
                    )
                ) {
                    parents.push(call);
                    return true;
                }
            }
        }
        return false;
    }

    getChildTree(
        src: Array<FileMerageBean>,
        id: string,
        children: Array<FileMerageBean>
    ): boolean {
        for (let call of src) {
            if (call.id == id && call.children.length == 0) {
                children.push(call);
                return true;
            } else {
                if (
                    this.getChildTree(
                        call.children as Array<FileMerageBean>,
                        id,
                        children
                    )
                ) {
                    children.push(call);
                    return true;
                }
            }
        }
        return false;
    }

    setRightTableData(call: FileMerageBean) {
        let parents: Array<FileMerageBean> = [];
        let children: Array<FileMerageBean> = [];
        this.getParentTree(this.dataSource, call, parents);
        let maxId = call.id;
        let maxDur = 0;

        function findMaxStack(call: any) {
            if (call.children.length == 0) {
                if (call.heapSize > maxDur) {
                    maxDur = call.heapSize;
                    maxId = call.id;
                }
            } else {
                call.children.map((callChild: any) => {
                    findMaxStack(<FileMerageBean>callChild);
                });
            }
        }

        findMaxStack(call);
        this.getChildTree(
            call.children as Array<FileMerageBean>,
            maxId,
            children
        );
        let arr = parents.reverse().concat(children.reverse());
        for (let data of arr) {
            data.type =
                data.libName.endsWith('.so.1') ||
                data.libName.endsWith('.dll') ||
                data.libName.endsWith('.so')
                    ? 0
                    : 1;
        }
        let len = arr.length;
        this.rightSource = arr;
        this.tbr!.dataSource = len == 0 ? [] : arr;
    }

    showButtomMenu(isShow: boolean) {
        if (isShow) {
            this.filter.setAttribute('tree', '');
            this.filter.setAttribute('input', '');
            this.filter.setAttribute('inputLeftText', '');
            this.filter.setAttribute('first', '');
            this.filter.setAttribute('second', '');
            this.filter.showThird(true)
        } else {
            this.filter.removeAttribute('tree');
            this.filter.removeAttribute('input');
            this.filter.removeAttribute('inputLeftText');
            this.filter.removeAttribute('first');
            this.filter.removeAttribute('second');
            this.filter.showThird(false)
        }
    }

    initFilterTypes() {
        let filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter');
        if (this.currentSelection!.nativeMemory.length > 0) {
            procedurePool.submitWithName(
                'logic1',
                'native-memory-get-responseType',
                {},
                undefined,
                (res: any) => {
                    this.responseTypes = res;
                    let nullIndex = this.responseTypes.findIndex((item) => {
                        return item.key == 0;
                    });
                    if (nullIndex != -1) {
                        this.responseTypes.splice(nullIndex, 1);
                    }
                    filter!.setSelectList(
                        null,
                        null,
                        'Allocation Lifespan',
                        'Allocation Type',
                        this.responseTypes.map((item: any) => {
                            return item.value;
                        })
                    );
                    filter!.setFilterModuleSelect(
                        '#first-select',
                        'width',
                        '150px'
                    );
                    filter!.setFilterModuleSelect(
                        '#second-select',
                        'width',
                        '150px'
                    );
                    filter!.setFilterModuleSelect(
                        '#third-select',
                        'width',
                        '150px'
                    );
                    filter!.firstSelect = '0';
                    filter!.secondSelect = '0';
                    filter!.thirdSelect = '0';
                    this.filterAllocationType = '0';
                    this.filterNativeType = '0';
                    this.filterResponseSelect = '0';
                    this.filterResponseType = -1;
                }
            );
        } else {
            filter!.setSelectList(
                null,
                null,
                'Allocation Lifespan',
                'Allocation Type',
                undefined
            );
            filter!.setFilterModuleSelect('#first-select', 'width', '150px');
            filter!.setFilterModuleSelect('#second-select', 'width', '150px');
            filter!.firstSelect = '0';
            filter!.secondSelect = '0';
            this.filterAllocationType = '0';
            this.filterNativeType = '0';
        }
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>(
            '#tb-filesystem-calltree'
        );
        this.progressEL = this.shadowRoot?.querySelector(
            '.progress'
        ) as LitProgressBar;
        this.frameChart =
            this.shadowRoot?.querySelector<FrameChart>('#framechart');
        this.modal = this.shadowRoot?.querySelector<DisassemblingWindow>(
            'tab-native-data-modal'
        );
        this.loadingPage = this.shadowRoot?.querySelector('.loading');
        this.frameChart!.addChartClickListener((needShowMenu: boolean) => {
            this.parentElement!.scrollTo(0, 0);
            this.showButtomMenu(needShowMenu);
            this.needShowMenu = needShowMenu;
        });
        this.tbl!.rememberScrollTop = true;
        this.filter = this.shadowRoot?.querySelector<TabPaneFilter>('#filter');
        this.tbl!.addEventListener('row-click', (evt: any) => {
            // @ts-ignore
            let data = evt.detail.data as FileMerageBean;
            this.setRightTableData(data);
            data.isSelected = true;
            this.currentSelectedData = data;
            this.tbr?.clearAllSelection(data);
            this.tbr?.setCurrentSelection(data);
            // @ts-ignore
            if ((evt.detail as any).callBack) {
                // @ts-ignore
                (evt.detail as any).callBack(true);
            }
        });
        this.tbr = this.shadowRoot?.querySelector<LitTable>(
            '#tb-filesystem-list'
        );
        this.tbr!.addEventListener('row-click', (evt: any) => {
            // @ts-ignore
            let data = evt.detail.data as FileMerageBean;
            this.tbl?.clearAllSelection(data);
            (data as any).isSelected = true;
            this.tbl!.scrollToData(data);
            // @ts-ignore
            if ((evt.detail as any).callBack) {
                // @ts-ignore
                (evt.detail as any).callBack(true);
            }
        });
        this.modal!.setCloseListener(() => {
            this.modal!.style.display = 'none';
            this.tbl!.style.visibility = 'visible';
            this.shadowRoot!.querySelector<TabPaneFilter>(
                '#filter'
            )!.style.display = 'flex';
        });
        let filterFunc = (data: any) => {
            let args: any[] = [];
            if (data.type == 'check') {
                if (data.item.checked) {
                    args.push({
                        funcName: 'splitTree',
                        funcArgs: [
                            data.item.name,
                            data.item.select == '0',
                            data.item.type == 'symbol',
                        ],
                    });
                } else {
                    args.push({
                        funcName: 'resotreAllNode',
                        funcArgs: [[data.item.name]],
                    });
                    args.push({
                        funcName: 'resetAllNode',
                        funcArgs: [],
                    });
                    args.push({
                        funcName: 'clearSplitMapData',
                        funcArgs: [data.item.name],
                    });
                }
            } else if (data.type == 'select') {
                args.push({
                    funcName: 'resotreAllNode',
                    funcArgs: [[data.item.name]],
                });
                args.push({
                    funcName: 'clearSplitMapData',
                    funcArgs: [data.item.name],
                });
                args.push({
                    funcName: 'splitTree',
                    funcArgs: [
                        data.item.name,
                        data.item.select == '0',
                        data.item.type == 'symbol',
                    ],
                });
            } else if (data.type == 'button') {
                if (data.item == 'symbol') {
                    if (
                        this.currentSelectedData &&
                        !this.currentSelectedData.canCharge
                    ) {
                        return;
                    }
                    if (this.currentSelectedData != undefined) {
                        this.filter!.addDataMining(
                            { name: this.currentSelectedData.symbolName },
                            data.item
                        );
                        args.push({
                            funcName: 'splitTree',
                            funcArgs: [
                                this.currentSelectedData.symbolName,
                                false,
                                true,
                            ],
                        });
                    } else {
                        return;
                    }
                } else if (data.item == 'library') {
                    if (
                        this.currentSelectedData &&
                        !this.currentSelectedData.canCharge
                    ) {
                        return;
                    }
                    if (
                        this.currentSelectedData != undefined &&
                        this.currentSelectedData.libName != ''
                    ) {
                        this.filter!.addDataMining(
                            { name: this.currentSelectedData.libName },
                            data.item
                        );
                        args.push({
                            funcName: 'splitTree',
                            funcArgs: [
                                this.currentSelectedData.libName,
                                false,
                                false,
                            ],
                        });
                    } else {
                        return;
                    }
                } else if (data.item == 'restore') {
                    if (data.remove != undefined && data.remove.length > 0) {
                        let list = data.remove.map((item: any) => {
                            return item.name;
                        });
                        args.push({
                            funcName: 'resotreAllNode',
                            funcArgs: [list],
                        });
                        args.push({
                            funcName: 'resetAllNode',
                            funcArgs: [],
                        });
                        list.forEach((symbolName: string) => {
                            args.push({
                                funcName: 'clearSplitMapData',
                                funcArgs: [symbolName],
                            });
                        });
                    }
                }
            }
            this.getDataByWorker(args, (result: any[]) => {
                this.setLTableData(result);
                this.frameChart!.data = this.dataSource;
                if (this.isChartShow) this.frameChart?.calculateChartData();
                this.tbl!.move1px();
                if (this.currentSelectedData) {
                    this.currentSelectedData.isSelected = false;
                    this.tbl?.clearAllSelection(this.currentSelectedData);
                    this.tbr!.recycleDataSource = [];
                    this.currentSelectedData = undefined;
                }
            });
        };
        this.filter!.getDataLibrary(filterFunc);
        this.filter!.getDataMining(filterFunc);
        this.filter!.getCallTreeData((data: any) => {
            if (data.value == 0) {
                this.refreshAllNode({
                    ...this.filter!.getFilterTreeData(),
                    callTree: data.checks,
                });
            } else {
                let args: any[] = [];
                if (data.checks[1]) {
                    args.push({
                        funcName: 'hideSystemLibrary',
                        funcArgs: [],
                    });
                    args.push({
                        funcName: 'resetAllNode',
                        funcArgs: [],
                    });
                } else {
                    args.push({
                        funcName: 'resotreAllNode',
                        funcArgs: [[this.systmeRuleName]],
                    });
                    args.push({
                        funcName: 'resetAllNode',
                        funcArgs: [],
                    });
                    args.push({
                        funcName: 'clearSplitMapData',
                        funcArgs: [this.systmeRuleName],
                    });
                }
                this.getDataByWorker(args, (result: any[]) => {
                    this.setLTableData(result);
                    this.frameChart!.data = this.dataSource;
                    if (this.isChartShow) this.frameChart?.calculateChartData();
                });
            }
        });
        this.filter!.getCallTreeConstraintsData((data: any) => {
            let args: any[] = [
                {
                    funcName: 'resotreAllNode',
                    funcArgs: [[this.numRuleName]],
                },
                {
                    funcName: 'clearSplitMapData',
                    funcArgs: [this.numRuleName],
                },
            ];
            if (data.checked) {
                args.push({
                    funcName: 'hideNumMaxAndMin',
                    funcArgs: [parseInt(data.min), data.max],
                });
            }
            args.push({
                funcName: 'resetAllNode',
                funcArgs: [],
            });
            this.getDataByWorker(args, (result: any[]) => {
                this.setLTableData(result);
                this.frameChart!.data = this.dataSource;
                if (this.isChartShow) this.frameChart?.calculateChartData();
            });
        });
        this.filter!.getFilterData((data: FilterData) => {
            if (this.currentSelection!.nativeMemoryStatistic.length > 0) {
                this.filterResponseSelect = '';
            }
            if (
                this.filterAllocationType != data.firstSelect ||
                this.filterNativeType != data.secondSelect ||
                this.filterResponseSelect != data.thirdSelect
            ) {
                this.filterAllocationType = data.firstSelect || '0';
                this.filterNativeType = data.secondSelect || '0';
                this.filterResponseSelect = data.thirdSelect || "0'";
                let thirdIndex = parseInt(data.thirdSelect || '0');
                if (this.responseTypes.length > thirdIndex) {
                    this.filterResponseType =
                        this.responseTypes[thirdIndex].key || -1;
                }
                this.refreshAllNode(this.filter!.getFilterTreeData());
            } else if (this.searchValue != this.filter!.filterValue) {
                this.searchValue = this.filter!.filterValue;
                let args = [
                    {
                        funcName: 'setSearchValue',
                        funcArgs: [this.searchValue],
                    },
                    {
                        funcName: 'resetAllNode',
                        funcArgs: [],
                    },
                ];
                this.getDataByWorker(args, (result: any[]) => {
                    this.setLTableData(result);
                    this.frameChart!.data = this.dataSource;
                    this.switchFlameChart(data);
                });
            } else {
                this.switchFlameChart(data);
            }
        });
        this.tbl!.addEventListener('column-click', (evt) => {
            // @ts-ignore
            this.sortKey = evt.detail.key;
            // @ts-ignore
            this.sortType = evt.detail.sort;
            // @ts-ignore
            this.setLTableData(this.dataSource, true);
            this.frameChart!.data = this.dataSource;
        });
    }

    connectedCallback() {
        super.connectedCallback();
        let filterHeight = 0;
        new ResizeObserver((entries) => {
            let tabPaneFilter = this.shadowRoot!.querySelector(
                '#filter'
            ) as HTMLElement;
            if (tabPaneFilter.clientHeight > 0)
                filterHeight = tabPaneFilter.clientHeight;
            if (this.parentElement!.clientHeight > filterHeight) {
                tabPaneFilter.style.display = 'flex';
            } else {
                tabPaneFilter.style.display = 'none';
            }
            this.modal!.style.height = this.tbl!.clientHeight - 2 + 'px'; //2 is borderWidth
            if (this.tbl!.style.visibility == 'hidden') {
                tabPaneFilter.style.display = 'none';
            }
            if (this.parentElement?.clientHeight != 0) {
                if (this.isChartShow) {
                    this.frameChart?.updateCanvas(
                        false,
                        entries[0].contentRect.width
                    );
                    this.frameChart?.calculateChartData();
                }
                // @ts-ignore
                this.tbl?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 10 - 35 + 'px';
                this.tbl?.reMeauseHeight();
                // @ts-ignore
                this.tbr?.shadowRoot.querySelector('.table').style.height = this.parentElement.clientHeight - 45 - 21 + 'px';
                this.tbr?.reMeauseHeight();
                this.loadingPage.style.height =
                    this.parentElement!.clientHeight - 24 + 'px';
            }
        }).observe(this.parentElement!);
        this.parentElement!.onscroll = () => {
            this.frameChart!.tabPaneScrollTop = this.parentElement!.scrollTop;
        };
    }

    switchFlameChart(data: any) {
        let pageTab = this.shadowRoot?.querySelector('#show_table');
        let pageChart = this.shadowRoot?.querySelector('#show_chart');
        if (data.icon == 'block') {
            pageChart?.setAttribute('class', 'show');
            pageTab?.setAttribute('class', '');
            this.isChartShow = true;
            this.filter!.disabledMining = true;
            this.showButtomMenu(this.needShowMenu);
            this.frameChart!.data = this.dataSource;
            this.frameChart?.calculateChartData();
        } else if (data.icon == 'tree') {
            pageChart?.setAttribute('class', '');
            pageTab?.setAttribute('class', 'show');
            this.showButtomMenu(true);
            this.isChartShow = false;
            this.filter!.disabledMining = false;
            this.frameChart!.clearCanvas();
            this.tbl!.reMeauseHeight();
        }
    }

    refreshAllNode(filterData: any) {
        let args: any[] = [];
        let isTopDown: boolean = !filterData.callTree[0];
        let isHideSystemLibrary = filterData.callTree[1];
        let list = filterData.dataMining.concat(filterData.dataLibrary);
        let groupArgs = new Map<string, any>();
        groupArgs.set('filterAllocType', this.filterAllocationType);
        groupArgs.set('filterEventType', this.filterNativeType);
        groupArgs.set('filterResponseType', this.filterResponseType);
        groupArgs.set('leftNs', this.currentSelection?.leftNs || 0);
        groupArgs.set('rightNs', this.currentSelection?.rightNs || 0);
        groupArgs.set(
            'nativeHookType',
            this.currentSelection!.nativeMemory.length > 0
                ? 'native-hook'
                : 'native-hook-statistic'
        );
        args.push(
            {
                funcName: 'groupCallchainSample',
                funcArgs: [groupArgs],
            },
            {
                funcName: 'getCallChainsBySampleIds',
                funcArgs: [isTopDown],
            }
        );
        this.tbr!.recycleDataSource = [];
        if (isHideSystemLibrary) {
            args.push({
                funcName: 'hideSystemLibrary',
                funcArgs: [],
            });
        }
        if (filterData.callTreeConstraints.checked) {
            args.push({
                funcName: 'hideNumMaxAndMin',
                funcArgs: [
                    parseInt(filterData.callTreeConstraints.inputs[0]),
                    filterData.callTreeConstraints.inputs[1],
                ],
            });
        }
        args.push({
            funcName: 'splitAllProcess',
            funcArgs: [list],
        });
        args.push({
            funcName: 'resetAllNode',
            funcArgs: [],
        });
        this.getDataByWorker(args, (result: any[]) => {
            this.setLTableData(result);
            this.frameChart!.data = this.dataSource;
            if (this.isChartShow) this.frameChart?.calculateChartData();
        });
    }

    setLTableData(resultData: any[], sort?: boolean) {
        if (sort) {
            this.dataSource = this.sortTree(resultData);
        } else {
            if (resultData && resultData[0]) {
                this.dataSource =
                    this.currentSelection!.nativeMemory.length > 0
                        ? this.sortTree(resultData)
                        : this.sortTree(resultData[0].children || []);
            } else {
                this.dataSource = [];
            }
        }
        this.tbl!.recycleDataSource = this.dataSource;
    }

    sortTree(arr: Array<any>): Array<any> {
        let sortArr = arr.sort((a, b) => {
            if (
                this.sortKey == 'heapSizeStr' ||
                this.sortKey == 'heapPercent'
            ) {
                if (this.sortType == 0) {
                    return b.size - a.size;
                } else if (this.sortType == 1) {
                    return a.size - b.size;
                } else {
                    return b.size - a.size;
                }
            } else {
                if (this.sortType == 0) {
                    return b.count - a.count;
                } else if (this.sortType == 1) {
                    return a.count - b.count;
                } else {
                    return b.count - a.count;
                }
            }
        });
        sortArr.map((call) => {
            call.children = this.sortTree(call.children);
        });
        return sortArr;
    }

    getDataByWorker(args: any[], handler: Function) {
        this.loadingList.push(1);
        this.progressEL!.loading = true;
        this.loadingPage.style.visibility = 'visible';
        procedurePool.submitWithName(
            'logic1',
            'native-memory-calltree-action',
            args,
            undefined,
            (results: any) => {
                handler(results);
                this.loadingList.splice(0, 1);
                if (this.loadingList.length == 0) {
                    this.progressEL!.loading = false;
                    this.loadingPage.style.visibility = 'hidden';
                }
            }
        );
    }

    getDataByWorkerQuery(args: any, handler: Function) {
        this.loadingList.push(1);
        this.progressEL!.loading = true;
        this.loadingPage.style.visibility = 'visible';
        procedurePool.submitWithName(
            'logic1',
            this.currentSelection!.nativeMemory!.length > 0
                ? 'native-memory-queryCallchainsSamples'
                : 'native-memory-queryStatisticCallchainsSamples',
            args,
            undefined,
            (results: any) => {
                handler(results);
                this.loadingList.splice(0, 1);
                if (this.loadingList.length == 0) {
                    this.progressEL!.loading = false;
                    this.loadingPage.style.visibility = 'hidden';
                }
            }
        );
    }

    initHtml(): string {
        return `
        <style>
        :host{
            display: flex;
            flex-direction: column;
            padding: 10px 10px 0 10px;
        }
        tab-pane-filter {
            border: solid rgb(216,216,216) 1px;
            float: left;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
        selector{
            display: none;
        }
        .show{
            display: flex;
            flex: 1;
        }
        .progress{
            bottom: 33px;
            position: absolute;
            height: 1px;
            left: 0;
            right: 0;
        }
        .loading{
            bottom: 0;
            position: absolute;
            left: 0;
            right: 0;
            width:100%;
            background:transparent;
            z-index: 999999;
        }
    </style>
    <div style="display: flex;flex-direction: row">
    
    <selector id='show_table' class="show">
        <lit-slicer style="width:100%">
        <div id="left_table" style="width: 65%">
            <tab-native-data-modal id="modal"></tab-native-data-modal>
            <lit-table id="tb-filesystem-calltree" style="height: auto" tree>
                <lit-table-column width="60%" title="Symbol Name" data-index="symbolName" key="symbolName"  align="flex-start">
                </lit-table-column>
                <lit-table-column width="1fr" title="Size" data-index="heapSizeStr" key="heapSizeStr"  align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="1fr" title="%" data-index="heapPercent" key="heapPercent" align="flex-start"  order>
                </lit-table-column>
                <lit-table-column width="1fr" title="Count" data-index="countValue" key="countValue" align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="1fr" title="%" data-index="countPercent" key="countPercent" align="flex-start" order>
                </lit-table-column>
                <lit-table-column width="1fr" title="  " data-index="type" key="type"  align="flex-start" >
                    <template>
                        <img src="img/library.png" size="20" v-if=" type == 1 ">
                        <img src="img/function.png" size="20" v-if=" type == 0 ">
                        <div v-if=" type == - 1 "></div>
                    </template>
                </lit-table-column>
            </lit-table>
            
        </div>
        <lit-slicer-track ></lit-slicer-track>
        <lit-table id="tb-filesystem-list" no-head style="height: auto;border-left: 1px solid var(--dark-border1,#e2e2e2)" hideDownload>
            <span slot="head">Heaviest Stack Trace</span>
            <lit-table-column width="30px" title="" data-index="type" key="type"  align="flex-start" >
                <template>
                    <img src="img/library.png" size="20" v-if=" type == 1 ">
                    <img src="img/function.png" size="20" v-if=" type == 0 ">
                </template>
            </lit-table-column>
            <lit-table-column width="1fr" title="" data-index="symbolName" key="symbolName"  align="flex-start"></lit-table-column>
        </lit-table>
        </div>
        </lit-slicer>
     </selector>
     <tab-pane-filter id="filter" first second icon ></tab-pane-filter>
     <lit-progress-bar class="progress"></lit-progress-bar>
    <selector id='show_chart'>
        <tab-framechart id='framechart' style='width: 100%;height: auto'> </tab-framechart>
    </selector>  
    <div class="loading"></div>
    </div>`;
    }
}

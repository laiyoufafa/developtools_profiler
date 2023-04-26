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

import { BaseElement, element } from '../../../../base-ui/BaseElement.js';
import '../../../../base-ui/checkbox/LitCheckBox.js';
import { LitCheckBox } from '../../../../base-ui/checkbox/LitCheckBox.js';
import { TraceRow } from './TraceRow.js';
import { SpSystemTrace } from '../../SpSystemTrace.js';
import { LitSearch } from '../search/Search.js';
import { TraceSheet } from './TraceSheet.js';
import { CpuStruct } from '../../../database/ui-worker/ProcedureWorkerCPU.js';

@element('trace-row-config')
export class TraceRowConfig extends BaseElement {
    selectTypeList: Array<string> | undefined;
    private spSystemTrace: SpSystemTrace | null | undefined;
    private sceneTable: HTMLDivElement | null | undefined;
    private chartTable: HTMLDivElement | null | undefined;
    private inputElement: HTMLInputElement | null | undefined;
    private allTraceRowList: NodeListOf<TraceRow<any>> | undefined;
    private traceRowList: NodeListOf<TraceRow<any>> | undefined;
    private selectTypeMap: any = {};
    private selectTypeOption: any = {};
    private sceneRowList: any;

    get value() {
        return this.getAttribute('value') || '';
    }

    set value(value: string) {
        this.setAttribute('value', value);
    }

    static get observedAttributes() {
        return ['mode'];
    }

    init() {
        let sceneList = [
            {
                title: 'Frame timeline',
                name: 'janks',
            },
        ];
        this.sceneRowList = [];
        this.selectTypeList = [];
        this.sceneTable!.innerHTML = '';
        this.chartTable!.innerHTML = '';
        this.spSystemTrace =
            this.parentElement!.querySelector<SpSystemTrace>('sp-system-trace');
        this.allTraceRowList =
            this.spSystemTrace!.shadowRoot?.querySelector(
                'div[class=rows]'
            )!.querySelectorAll<TraceRow<any>>('trace-row');
        this.traceRowList = this.spSystemTrace!.shadowRoot?.querySelector(
            'div[class=rows-pane]'
        )!.querySelectorAll<TraceRow<any>>("trace-row[row-parent-id='']");
        if (this.traceRowList) {
            this.traceRowList!.forEach((it) => {
                this.initConfigChartTable(it);
            });
        }
        sceneList!.forEach((it) => {
            let sceneTraceRowList =
                this.spSystemTrace!.shadowRoot?.querySelector(
                    'div[class=rows]'
                )!.querySelector<TraceRow<any>>(
                    `trace-row[row-type=${it.name}]`
                );
            if (sceneTraceRowList) {
                this.initConfigSceneTable(it);
                let parentRow: any = {};
                let selectParentOption: any = {};
                let selectParentRow: any = {};
                this.allTraceRowList!.forEach((traceRow) => {
                    traceRow.setAttribute('scene', '');
                    if (traceRow.rowParentId == '') {
                        parentRow['parent'] = traceRow;
                    }
                    if (
                        traceRow.rowType == it.name &&
                        !selectParentRow[traceRow.rowParentId!]
                    ) {
                        selectParentOption[traceRow.rowParentId!] =
                            parentRow['parent'].name;
                        selectParentRow[traceRow.rowParentId!] =
                            parentRow['parent'];
                    }
                });
				 // @ts-ignore
                this.selectTypeMap[it.name] = Object.values(selectParentRow);
				 // @ts-ignore
                this.selectTypeOption[it.name] = Object.values(selectParentOption);
            } else {
                this.allTraceRowList!.forEach((traceRow) => {
                    traceRow.setAttribute('scene', '');
                });
            }
        });
    }

    initConfigSceneTable(item: any) {
        let div = document.createElement('div');
        div.className = 'scene-option-div';
        div.textContent = item.title;
        let optionCheckBox: LitCheckBox = new LitCheckBox();
        optionCheckBox.checked = false;
        optionCheckBox.style.justifySelf = 'center';
        optionCheckBox.style.height = '100%';
        optionCheckBox.title = item.title;
        optionCheckBox.setAttribute('rowType', item.name);
        optionCheckBox.addEventListener('change', (e) => {
            this.resetChartOption(item, optionCheckBox.checked);
            this.resetChartTable(item, optionCheckBox.checked);
        });
        this.sceneTable?.append(...[div, optionCheckBox]);
    }

    initConfigChartTable(row: TraceRow<any>) {
        let div = document.createElement('div');
        div.className = 'chart-option-div chart-item';
        div.textContent = row.name;
        div.title = row.name;
        let optionCheckBox: LitCheckBox = new LitCheckBox();
        optionCheckBox.checked = true;
        optionCheckBox.className = 'chart-config-check chart-item';
        optionCheckBox.style.height = '100%';
        optionCheckBox.style.justifySelf = 'center';
        optionCheckBox.title = row.name;
        optionCheckBox.setAttribute('rowType', row.rowType || '');
        optionCheckBox.addEventListener('change', (e) => {
            if (optionCheckBox.checked) {
                row.removeAttribute('row-hidden');
                row.setAttribute('scene', '');
                this.resetChildRowModel(row, true, false);
                if (this.spSystemTrace!.collectRows.length > 0) {
                    this.spSystemTrace!.collectRows.forEach((collectRow) => {
                        let isParentRow = row.folder
                            ? collectRow.rowParentId === row.rowId
                            : collectRow.rowId === row.rowId;
                        if (isParentRow) {
                            collectRow.removeAttribute('row-hidden');
                            collectRow.setAttribute('scene', '');
                        }
                    });
                }
            } else {
                if (row.rowParentId == '') {
                    row.expansion = false;
                }
                row.setAttribute('row-hidden', '');
                row.removeAttribute('scene');
                this.resetChildRowModel(row, false, true);
                if (this.spSystemTrace!.collectRows.length > 0) {
                    this.spSystemTrace!.collectRows.forEach((collectRow) => {
                        let isParentRow = row.folder
                            ? collectRow.rowParentId === row.rowId
                            : collectRow.rowId === row.rowId;
                        if (isParentRow) {
                            collectRow.removeAttribute('scene');
                            collectRow.setAttribute('row-hidden', '');
                        }
                    });
                }
            }
            this.refreshSystemPanel();
        });
        this.chartTable!.append(...[div, optionCheckBox]);
    }

    resetChartOption(item: any, isCheck: boolean) {
        if (isCheck) {
            this.selectTypeOption[item.name].forEach((selectName: any) => {
                this.selectTypeList!.push(selectName);
            });
        } else {
            this.selectTypeOption[item.name].forEach((name: any) => {
                let selectIndex = this.selectTypeList!.indexOf(name);
                delete this.selectTypeList![selectIndex];
            });
        }
        this.selectTypeList = this.selectTypeList!.filter(
            (selectName) => selectName != ''
        );
        this.shadowRoot!.querySelectorAll<LitCheckBox>('.chart-item').forEach(
            (litCheckBox: LitCheckBox) => {
                if (this.selectTypeList!.length > 0) {
                    litCheckBox.checked =
                        this.selectTypeList!.indexOf(litCheckBox.title) > -1;
                } else {
                    litCheckBox.checked = true;
                }
            }
        );
    }

    resetChartTable(item: any, isCheck: boolean) {
        let favoriteRowList =
            this.spSystemTrace?.favoriteRowsEL?.querySelectorAll<TraceRow<any>>(
                'trace-row'
            );
        if (this.selectTypeList!.length > 0) {
            this.traceRowList!.forEach((traceRow) => {
                if (this.selectTypeList!.indexOf(traceRow.name) > -1) {
                    traceRow.removeAttribute('row-hidden');
                    this.resetChildRowModel(traceRow, true, false);
                } else {
                    traceRow.setAttribute('row-hidden', '');
                    if (traceRow.expansion) {
                        traceRow.removeAttribute('expansion');
                    }
                    this.resetChildRowModel(traceRow, false, true);
                }
            });
            favoriteRowList!.forEach((traceRow) => {
                if (traceRow.rowType == item.name) {
                    traceRow.removeAttribute('row-hidden');
                    if (isCheck) {
                        traceRow.setAttribute('scene', '');
                    } else {
                        traceRow.removeAttribute('scene');
                    }
                } else {
                    traceRow.setAttribute('row-hidden', '');
                    if (isCheck) {
                        traceRow.removeAttribute('scene');
                    } else {
                        traceRow.setAttribute('scene', '');
                    }
                }
            });
        } else {
            this.traceRowList!.forEach((traceRow) => {
                traceRow.removeAttribute('row-hidden');
                this.resetChildRowModel(traceRow, true, false);
            });
            favoriteRowList!.forEach((traceRow) => {
                traceRow.removeAttribute('row-hidden');
                traceRow.setAttribute('scene', '');
            });
        }
        this.refreshSystemPanel();
    }

    resetChildRowModel(
        row: any,
        hasRowSceneModel: boolean,
        hasRowHidden: boolean = false
    ) {
        if (hasRowSceneModel) {
            row.setAttribute('scene', '');
        } else {
            row.removeAttribute('scene');
        }
        let sonRowList = this.spSystemTrace!.shadowRoot?.querySelector(
            'div[class=rows]'
        )!.querySelectorAll<TraceRow<any>>(
            `trace-row[row-parent-id='${row.rowId}']`
        );
        sonRowList!.forEach((sonRow) => {
            if (hasRowSceneModel) {
                sonRow.setAttribute('scene', '');
            } else {
                sonRow.removeAttribute('scene');
            }
            if (hasRowHidden) {
                sonRow.setAttribute('row-hidden', '');
            }
        });
    }

    refreshSystemPanel() {
        this.clearSearchAndFlag();
        this.spSystemTrace!.scrollToProcess('', '', '', false);
        this.spSystemTrace!.refreshFavoriteCanvas();
        this.spSystemTrace!.refreshCanvas(false);
    }

    clearSearchAndFlag() {
        let traceSheet = this.spSystemTrace!.shadowRoot?.querySelector(
            '.trace-sheet'
        ) as TraceSheet;
        if (traceSheet) {
            traceSheet!.setAttribute('mode', 'hidden');
        }
        let search = document
            .querySelector('sp-application')!
            .shadowRoot?.querySelector('#lit-search') as LitSearch;
        if (search) {
            search.clear();
        }
        let highlightRow = this.spSystemTrace!.shadowRoot?.querySelector<
            TraceRow<any>
        >('trace-row[highlight]');
        if (highlightRow) {
            highlightRow.highlight = false;
        }
        this.spSystemTrace!.timerShaftEL?.removeTriangle('inverted');
        CpuStruct.wakeupBean = undefined;
        this.spSystemTrace!.hoverFlag = undefined;
        this.spSystemTrace!.selectFlag = undefined;
    }

    initElements(): void {}

    connectedCallback() {
        this.sceneTable =
            this.shadowRoot!.querySelector<HTMLDivElement>('#scene-select');
        this.chartTable =
            this.shadowRoot!.querySelector<HTMLDivElement>('#chart-select');
        let bar = this.shadowRoot!.querySelector<HTMLDivElement>('.processBar');
        this.inputElement = this.shadowRoot!.querySelector('input');
        this.inputElement?.addEventListener('keyup', () => {
            this.shadowRoot!.querySelectorAll<HTMLElement>(
                '.chart-item'
            ).forEach((elementOption: HTMLElement) => {
                if (
                    elementOption.title!.indexOf(this.inputElement!.value) <= -1
                ) {
                    elementOption.style.display = 'none';
                } else {
                    elementOption.style.display = 'block';
                }
            });
            this.value = this.inputElement!.value;
        });
    }

    initHtml(): string {
        return `
            <style>
                :host([mode='hidden']){
                    visibility: hidden;
                }
                :host{
                    display: block;
                    visibility: visible;
                    background-color: #F6F6F6;
                }
                .config-title {
                    border-top: 1px solid var(--dark-border1,#D5D5D5);
                    background-color: #0A59F7;
                    display: flex;
                    height: 10%;
                    align-items: center;
                    padding: 0 20px 0 12px;
                }
                .config-scene {
                    height: 14%;
                }
                .config-chart {
                     height: 75%;
                }
                .title-text {
                    font-family: Helvetica-Bold;
                    font-size: 16px;
                    color: #FFFFFF;
                    text-align: left;
                    font-weight: 700;
                    margin-right: auto;
                }
                .config-close {
                    text-align: right;
                    cursor: pointer;
                    opacity: 1;
                }
                .config-close:hover {
                    opacity: 0.7;
                }
                .title_div{
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    padding-left: 15px;
                    padding-right: 15px;
                    border-bottom: 1px solid #e0e0e0;
                    background-color: #F6F6F6;
                }
                .search_bt{
                    height: 26px;
                    color: #ffffff;
                    cursor: pointer;
                    line-height: 40px;
                    text-align: center;
                    margin: auto;
                    width: 20vh;
                    background: #FFFFFF;
                    border: 1px solid rgba(0,0,0,0.6);
                    border-radius: 12px;
                }
                .config-select {
                    padding-top: 12px;
                    background: #FFFFFF;
                    overflow-y: scroll;
                    overflow-x: hidden;
                    border-radius: 5px;
                    border: solid 1px #e0e0e0;
                    display: grid;
                    padding-left: 40px;
                    grid-template-columns: auto auto;
                    grid-template-rows: repeat(auto-fit, 35px);
                }
                .config-img {
                    margin-right: 12px;
                } 
                .chart-option-div {
                    height: 35px;
                    line-height: 35px;
                }
                .scene-option-div {
                    height: 35px;
                    line-height: 35px;
                }
                input{
                    border: 0;
                    outline: none;
                    background-color: transparent;
                    cursor: pointer;
                    -webkit-user-select:none;
                    -moz-user-select:none;
                    user-select:none;
                    display: inline-flex;
                    color: var(--dark-color2,rgba(0,0,0,0.6));
                    width:100%;
                }
                .multipleSelect{
                    position: relative;
                    padding: 3px 6px;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    transition: all .3s;
                    outline: none;
                    font-size: 1rem;
                    -webkit-user-select:none ;
                    -moz-user-select:none;
                    user-select:none;
                    width: 250px;
                    border:1px solid var(--bark-prompt,#dcdcdc);
                    border-radius:16px;
                    background-color: #FFFFFF;
                    height: 50%;
                    color: #ffffff;
                    cursor: pointer;
                    line-height: 40px;
                    text-align: center;
                    margin: auto 4.2em auto auto;
                }
                .processBar {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 12px;
                    height: 100%;
                    z-index: 20;
                    opacity: 0;
                }
                .processBar:hover {
                    cursor: col-resize;
                }
            </style>
<!--            <div class="processBar"></div>-->
            <div class="config-title">
               <span class="title-text">Display Template</span>
               <lit-icon class="config-close" name="config-close" title="Config Close"></lit-icon>
            </div>
            <div class="config-scene">
                <div class="title_div" style="height: 43%">
                    <img class="config-img" title="Template Select" src="img/config_scene.png">
                    <div>Template Select</div>
                </div>
                <div class="config-select" id="scene-select" style="height: 45%;overflow: hidden;">
                </div>
            </div>
            <div class="config-chart">
                 <div class="title_div" style="height: 8%">
                    <img class="config-img" title="Timeline Details" src="img/config_chart.png">
                    <div>Timeline Details</div>
                    <div class="multipleSelect" tabindex="0">
                        <div class="multipleRoot" id="select" style="width:100%">
                            <input id="singleInput"/>
                        </div>
                        <lit-icon class="icon" name='search' color="#c3c3c3"></lit-icon>
                    </div>
                </div>
                <div class="config-select" id="chart-select" style="height: 91%">
                </div>
            </div>
`;
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
        if (name === 'mode' && newValue == '') {
            this.init();
        }
    }
}

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

import {LitTableColumn} from "./lit-table-column.js";
import {element} from "../BaseElement.js";
import "../utils/Template.js"
import {TableRowObject} from "./TableRowObject.js";

@element('lit-table')
export class LitTable extends HTMLElement {
    meauseRowElement: HTMLDivElement | undefined
    currentRecycleList: HTMLDivElement[] = []
    private st: HTMLSlotElement | null | undefined
    private tableElement: HTMLDivElement | null | undefined
    private theadElement: HTMLDivElement | null | undefined
    private columns: Array<Element> | null | undefined
    private tbodyElement: HTMLDivElement | undefined | null
    private tableColumns: NodeListOf<LitTableColumn> | undefined
    private colCount: number = 0
    private ds: Array<any> = []
    private recycleDs: Array<any> = []
    private gridTemplateColumns: any

    constructor() {
        super();
        const shadowRoot = this.attachShadow({mode: 'open'});
        shadowRoot.innerHTML = `
<style>
:host{ 
    display: grid;
    grid-template-columns: repeat(1,1fr);
    width: 100%;
    flex:1;
}
.tr{
    display: grid;
    width:100%;
}
.tr:nth-of-type(even){
}
.tr{
    background-color: var(--dark-background,#FFFFFF);
}
.tr:hover{
    background-color: var(--dark-background6,#DEEDFF);
}
.td{
    background-color: inherit;
    box-sizing: border-box;
    padding: 3px;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    width: 100%;
    height: auto;
}
.td-order{
}
.td-order:before{
    
}
:host([grid-line]) .td{
    border-left: 1px solid #f0f0f0;
}
:host([grid-line]) .td:last-of-type{
    border-right: 1px solid #f0f0f0;
}
.table{
     color: var(--dark-color2,#262626);
}
.thead{
    display: grid;
    position: sticky;
    top: 0;
    font-weight: bold;
    font-size: .9rem;
    color: var(--dark-color1,#000);
    z-index: 1;
}
.tbody{
    width: 100%;
    top: 0;
    left: 0;
    right:0;
    bottom:0;
    display: grid;
    grid-template-columns: 1fr;
    row-gap: 1px;
    column-gap: 1px;
    position: relative;
}
:host([grid-line])  .tbody{
    border-bottom: 1px solid #f0f0f0;
    background-color: #f0f0f0;
}
.th{
    display: grid;
}

.tree-icon{
    font-size: 1.2rem;
    width: 20px;
    height: 20px;
    padding-right: 5px;
    padding-left: 5px;
    cursor: pointer;
}
.tree-icon:hover{
    color: #42b983;
}
.row-checkbox,row-checkbox-all{
    
}
:host([no-head]) .thead{
    display: none;
}
.up-svg{
    position: absolute;
    right: 5px;
    top: 8px;
    bottom: 8px;
    width: 15px;
    height: 15px;
}
.down-svg{
    position: absolute;
    top: 8px;
    right: 5px;
    bottom: 8px;
    width: 15px;
    height: 15px;
}
</style>

<slot id="slot" style="display: none"></slot>
<div class="table" style="overflow:auto;">
    <div class="thead"></div>
    <div class="tbody"></div>
</div>
        `
    }

    static get observedAttributes() {
        return ['scroll-y', 'selectable', 'no-head', 'grid-line', 'defaultOrderColumn']
    }

    get selectable() {
        return this.hasAttribute('selectable');
    }

    set selectable(value) {
        if (value) {
            this.setAttribute('selectable', '');
        } else {
            this.removeAttribute('selectable');
        }
    }

    get scrollY() {
        return this.getAttribute('scroll-y') || 'auto';
    }

    set scrollY(value) {
        this.setAttribute('scroll-y', value);
    }

    get dataSource() {
        return this.ds || [];
    }

    set dataSource(value) {
        this.ds = value;
        if (this.hasAttribute('tree')) {
            this.renderTreeTable();
        } else {
            this.renderTable();
        }
    }

    get recycleDataSource() {
        return this.recycleDs || [];
    }

    set recycleDataSource(value) {
        if (this.hasAttribute('tree')) {
            this.recycleDs = this.meauseTreeRowElement(value)
        } else {
            this.recycleDs = this.meauseAllRowHeight(value)
        }
    }

    connectedCallback() {
        this.st = this.shadowRoot?.querySelector('#slot');
        this.tableElement = this.shadowRoot?.querySelector('.table');
        this.theadElement = this.shadowRoot?.querySelector('.thead');
        this.tbodyElement = this.shadowRoot?.querySelector('.tbody');
        this.tableColumns = this.querySelectorAll<LitTableColumn>('lit-table-column');
        this.colCount = this.tableColumns!.length;
        this.st?.addEventListener('slotchange', () => {
            this.theadElement!.innerHTML = '';
            setTimeout(() => {
                this.columns = this.st!.assignedElements();
                let rowElement = document.createElement('div');
                rowElement.classList.add('th');
                if (this.selectable) {
                    let box = document.createElement('div');
                    box.style.display = 'flex';
                    box.style.justifyContent = 'center';
                    box.style.alignItems = 'center';
                    box.style.gridArea = "_checkbox_";
                    box.classList.add('td');
                    box.style.backgroundColor = "#ffffff66";
                    let checkbox = document.createElement('lit-checkbox');
                    checkbox.classList.add('row-checkbox-all');
                    checkbox.onchange = (e: any) => {
                        this.shadowRoot!.querySelectorAll('.row-checkbox').forEach((a: any) => a.checked = e.detail.checked);
                        if (e.detail.checked) {
                            this.shadowRoot!.querySelectorAll('.tr').forEach(a => a.setAttribute('checked', ''));
                        } else {
                            this.shadowRoot!.querySelectorAll('.tr').forEach(a => a.removeAttribute('checked'));
                        }
                    }

                    box.appendChild(checkbox);
                    rowElement.appendChild(box);
                }

                let area: Array<any> = [], gridTemplateColumns: Array<any> = [];
                let resolvingArea = (columns: any, x: any, y: any) => {
                    columns.forEach((a: any, i: any) => {
                        if (!area[y]) area[y] = []
                        let key = a.getAttribute('key') || a.getAttribute('title')
                        if (a.tagName === 'LIT-TABLE-GROUP') {
                            let len = a.querySelectorAll('lit-table-column').length;
                            let children = [...a.children].filter(a => a.tagName !== 'TEMPLATE');
                            if (children.length > 0) {
                                resolvingArea(children, x, y + 1);
                            }
                            for (let j = 0; j < len; j++) {
                                area[y][x] = {x, y, t: key};
                                x++;
                            }
                            let h = document.createElement('div');
                            h.classList.add('td');
                            h.style.justifyContent = a.getAttribute('align')
                            h.style.borderBottom = '1px solid #f0f0f0'
                            h.style.gridArea = key;
                            h.innerText = a.title;
                            if (a.hasAttribute('fixed')) {
                                this.fixed(h, a.getAttribute('fixed'), "#42b983")
                            }
                            rowElement.append(h);
                        } else if (a.tagName === 'LIT-TABLE-COLUMN') {
                            area[y][x] = {x, y, t: key};
                            x++;
                            let h: any = document.createElement('div');
                            h.classList.add('td');
                            if (a.hasAttribute('order')) {
                                h.sortType = 0;
                                h.classList.add('td-order');
                                h.style.position = "relative"
                                let NS = "http://www.w3.org/2000/svg";
                                let upSvg: any = document.createElementNS(NS, "svg");
                                let upPath: any = document.createElementNS(NS, "path");
                                upSvg.setAttribute('fill', '#efefef');
                                upSvg.setAttribute('viewBox', '0 0 1024 1024');
                                upSvg.setAttribute('stroke', '#000000');
                                upSvg.classList.add('up-svg');
                                upPath.setAttribute("d", "M858.9 689L530.5 308.2c-9.4-10.9-27.5-10.9-37 0L165.1 689c-12.2 14.2-1.2 35 18.5 35h656.8c19.7 0 30.7-20.8 18.5-35z");
                                upSvg.appendChild(upPath);
                                let downSvg: any = document.createElementNS(NS, "svg");
                                let downPath: any = document.createElementNS(NS, "path");
                                downSvg.setAttribute('fill', '#efefef');
                                downSvg.setAttribute('viewBox', '0 0 1024 1024');
                                downSvg.setAttribute('stroke', '#efefef');
                                downSvg.classList.add('down-svg');
                                downPath.setAttribute("d", "M840.4 300H183.6c-19.7 0-30.7 20.8-18.5 35l328.4 380.8c9.4 10.9 27.5 10.9 37 0L858.9 335c12.2-14.2 1.2-35-18.5-35z");
                                downSvg.appendChild(downPath)
                                if (i == 0) {
                                    h.sortType = 0;
                                    upSvg.setAttribute('fill', '#fff');
                                    downSvg.setAttribute('fill', '#fff');
                                }
                                upSvg.style.display = 'none';
                                downSvg.style.display = 'none';
                                h.appendChild(upSvg);
                                h.appendChild(downSvg);
                                h.onclick = () => {
                                    this?.shadowRoot?.querySelectorAll('.td-order svg').forEach((it: any) => {
                                        it.setAttribute('fill', '#fff');
                                        it.setAttribute('fill', '#fff');
                                        it.sortType = 0;
                                    })
                                    if (h.sortType == undefined || h.sortType == null) {
                                        h.sortType = 0;
                                    } else if (h.sortType === 2) {
                                        h.sortType = 0;
                                    } else {
                                        h.sortType += 1;
                                    }
                                    switch (h.sortType) {
                                        case 1:
                                            upSvg.setAttribute('fill', '#333');
                                            downSvg.setAttribute('fill', '#fff');
                                            upSvg.style.display = 'block';
                                            downSvg.style.display = 'none';
                                            break;
                                        case 2:
                                            upSvg.setAttribute('fill', '#fff');
                                            downSvg.setAttribute('fill', '#333');
                                            upSvg.style.display = 'none';
                                            downSvg.style.display = 'block';
                                            break;
                                        default:
                                            upSvg.setAttribute('fill', "#fff");
                                            downSvg.setAttribute('fill', "#fff");
                                            upSvg.style.display = 'none';
                                            downSvg.style.display = 'none';
                                            break;
                                    }
                                    this.dispatchEvent(new CustomEvent("column-click", {
                                        detail: {
                                            sort: h.sortType, key: key
                                        }, composed: true
                                    }))
                                }
                            }
                            h.style.justifyContent = a.getAttribute('align')
                            gridTemplateColumns.push(a.getAttribute('width') || '1fr');
                            h.style.gridArea = key;
                            let titleLabel = document.createElement("label");
                            titleLabel.textContent = a.title;
                            h.appendChild(titleLabel);
                            if (a.hasAttribute('fixed')) {
                                this.fixed(h, a.getAttribute('fixed'), "#42b983")
                            }
                            rowElement.append(h);
                        }
                    })
                }
                resolvingArea(this.columns, 0, 0);
                area.forEach((rows, j, array) => {
                    for (let i = 0; i < this.colCount; i++) {
                        if (!rows[i]) rows[i] = array[j - 1][i];
                    }
                })
                this.gridTemplateColumns = gridTemplateColumns.join(' ');
                if (this.selectable) {
                    let s = area.map(a => '"_checkbox_ ' + (a.map((aa: any) => aa.t).join(' ')) + '"').join(' ');
                    rowElement.style.gridTemplateColumns = "60px " + gridTemplateColumns.join(' ');//`repeat(${this.colCount},1fr)`
                    rowElement.style.gridTemplateRows = `repeat(${area.length},1fr)`
                    rowElement.style.gridTemplateAreas = s
                } else {
                    let s = area.map(a => '"' + (a.map((aa: any) => aa.t).join(' ')) + '"').join(' ');
                    rowElement.style.gridTemplateColumns = gridTemplateColumns.join(' ');//`repeat(${this.colCount},1fr)`
                    rowElement.style.gridTemplateRows = `repeat(${area.length},1fr)`
                    rowElement.style.gridTemplateAreas = s
                }
                this.theadElement!.append(rowElement);
                if (this.hasAttribute('tree')) {
                    this.renderTreeTable();
                } else {
                    this.renderTable();
                }
            });

        });

        this.shadowRoot!.addEventListener("load", function (event) {
            console.log("DOM fully loaded and parsed");
        });
    }

    disconnectedCallback() {
    }

    adoptedCallback() {
        console.log('Custom square element moved to new page.');
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
    }

    fixed(td: HTMLElement, placement: string, bgColor: string) {
        td.style.position = 'sticky';
        if (placement === "left") {
            td.style.left = '0px';
            td.style.boxShadow = '3px 0px 5px #33333333'
        } else if (placement === "right") {
            td.style.right = '0px';
            td.style.boxShadow = '-3px 0px 5px #33333333'
        }
    }

    renderTable() {
        if (!this.columns) return;
        if (!this.ds) return;
        this.tbodyElement!.innerHTML = '';
        this.ds.forEach((rowData: any) => {
            let rowElement = document.createElement('div');
            rowElement.classList.add('tr');
            // @ts-ignore
            rowElement.data = rowData;
            let gridTemplateColumns: Array<any> = []
            if (this.selectable) {
                let box = document.createElement('div');
                box.style.display = 'flex';
                box.style.justifyContent = 'center';
                box.style.alignItems = 'center';
                box.classList.add('td');
                let checkbox = document.createElement('lit-checkbox');
                checkbox.classList.add('row-checkbox');
                checkbox.onchange = (e: any) => {
                    if (e.detail.checked) {
                        rowElement.setAttribute('checked', "");
                    } else {
                        rowElement.removeAttribute('checked');
                    }
                }
                box.appendChild(checkbox);
                rowElement.appendChild(box);
            }
            this.tableColumns!.forEach(cl => {
                let dataIndex = cl.getAttribute('data-index') || '1';
                gridTemplateColumns.push(cl.getAttribute('width') || '1fr')
                if (cl.template) {
                    // @ts-ignore
                    let cloneNode = cl.template.render(rowData).content.cloneNode(true);
                    let d = document.createElement('div');
                    d.classList.add('td');
                    d.style.wordBreak = 'break-all'
                    d.style.whiteSpace = 'pre-wrap'
                    d.style.justifyContent = cl.getAttribute('align') || ''
                    if (cl.hasAttribute('fixed')) {
                        this.fixed(d, cl.getAttribute('fixed') || '', "#ffffff")
                    }
                    d.append(cloneNode);
                    rowElement.append(d);
                } else {
                    let td = document.createElement('div');
                    td.classList.add('td');
                    td.style.wordBreak = 'break-all'
                    td.style.whiteSpace = 'pre-wrap'
                    td.title = rowData[dataIndex]
                    td.style.justifyContent = cl.getAttribute('align') || ''
                    if (cl.hasAttribute('fixed')) {
                        this.fixed(td, cl.getAttribute('fixed') || '', "#ffffff")
                    }
                    td.innerHTML = rowData[dataIndex];
                    rowElement.append(td);
                }

            })
            if (this.selectable) {
                rowElement.style.gridTemplateColumns = '60px ' + gridTemplateColumns.join(' ');
            } else {
                rowElement.style.gridTemplateColumns = gridTemplateColumns.join(' ');
            }
            rowElement.onclick = e => {
                this.dispatchEvent(new CustomEvent('row-click', {detail: rowData, composed: true}));
            }
            this.tbodyElement!.append(rowElement);
        })
    }

    renderTreeTable() {
        if (!this.columns) return;
        if (!this.ds) return;
        this.tbodyElement!.innerHTML = '';
        let ids = JSON.parse(this.getAttribute('tree') || `["id","pid"]`);
        let toTreeData = (data: any, id: any, pid: any) => {
            let cloneData = JSON.parse(JSON.stringify(data));
            return cloneData.filter((father: any) => {
                let branchArr = cloneData.filter((child: any) => father[id] == child[pid]);
                branchArr.length > 0 ? father['children'] = branchArr : '';
                return !father[pid];
            });
        }
        let treeData = toTreeData(this.ds, ids[0], ids[1]);//
        let offset = 30;
        let offsetVal = offset;
        const drawRow = (arr: any, parentNode: any) => {
            arr.forEach((rowData: any) => {
                let rowElement = document.createElement('div');
                rowElement.classList.add('tr');
                // @ts-ignore
                rowElement.data = rowData;
                let gridTemplateColumns: Array<any> = [];
                if (this.selectable) {
                    let box = document.createElement('div');
                    box.style.display = 'flex';
                    box.style.justifyContent = 'center';
                    box.style.alignItems = 'center';
                    box.classList.add('td');
                    let checkbox = document.createElement('lit-checkbox');
                    checkbox.classList.add('row-checkbox');
                    checkbox.onchange = (e: any) => {
                        if (e.detail.checked) {
                            rowElement.setAttribute('checked', "");
                        } else {
                            rowElement.removeAttribute('checked');
                        }
                        const changeChildNode = (rowElement: any, checked: any) => {
                            let id = rowElement.getAttribute('id');
                            let pid = rowElement.getAttribute('pid');
                            this.shadowRoot!.querySelectorAll(`div[pid=${id}]`).forEach(a => {
                                // @ts-ignore
                                a.querySelector('.row-checkbox')!.checked = checked;
                                if (checked) {
                                    a.setAttribute('checked', '');
                                } else {
                                    a.removeAttribute('checked');
                                }
                                changeChildNode(a, checked);
                            });
                        };
                        changeChildNode(rowElement, e.detail.checked);
                    }
                    box.appendChild(checkbox);
                    rowElement.appendChild(box);
                }
                this.tableColumns!.forEach((cl, index) => {
                    let dataIndex = cl.getAttribute('data-index');
                    gridTemplateColumns.push(cl.getAttribute('width') || '1fr')
                    let td;
                    if (cl.template) {
                        // @ts-ignore
                        let cloneNode = cl.template.render(rowData).content.cloneNode(true);
                        td = document.createElement('div');
                        td.classList.add('td');
                        td.style.wordBreak = 'break-all'
                        td.style.justifyContent = cl.getAttribute('align') || ''
                        if (cl.hasAttribute('fixed')) {
                            this.fixed(td, cl.getAttribute('fixed') || '', "#ffffff")
                        }
                        td.append(cloneNode);
                    } else {
                        td = document.createElement('div');
                        td.classList.add('td');
                        td.style.wordBreak = 'break-all'
                        td.style.justifyContent = cl.getAttribute('align') || ''
                        if (cl.hasAttribute('fixed')) {
                            this.fixed(td, cl.getAttribute('fixed') || '', "#ffffff")
                        }
                        // @ts-ignore
                        td.innerHTML = rowData[dataIndex];
                    }
                    if (index === 0) {
                        if (rowData.children && rowData.children.length > 0) {
                            let btn = document.createElement('lit-icon');
                            btn.classList.add('tree-icon');
                            // @ts-ignore
                            btn.name = 'minus-square';
                            td.insertBefore(btn, td.firstChild);
                            td.style.paddingLeft = (offsetVal - 30) + 'px';
                            btn.onclick = (e) => {
                                const foldNode = (rowElement: any) => {
                                    let id = rowElement.getAttribute('id');
                                    let pid = rowElement.getAttribute('pid');
                                    this.shadowRoot!.querySelectorAll(`div[pid=${id}]`).forEach(a => {
                                        let id = a.getAttribute('id');
                                        let pid = a.getAttribute('pid');
                                        (a as HTMLElement).style.display = 'none';
                                        foldNode(a);
                                    });
                                    if (rowElement.querySelector('.tree-icon')) {
                                        rowElement.querySelector('.tree-icon').name = 'plus-square';
                                    }
                                    rowElement.removeAttribute('expend');
                                };
                                const expendNode = (rowElement: any) => {
                                    let id = rowElement.getAttribute('id');
                                    let pid = rowElement.getAttribute('pid');
                                    this.shadowRoot!.querySelectorAll(`div[pid=${id}]`).forEach((a) => {
                                        let id = a.getAttribute('id');
                                        let pid = a.getAttribute('pid');
                                        (a as HTMLElement).style.display = '';
                                    });
                                    if (rowElement.querySelector('.tree-icon')) {
                                        rowElement.querySelector('.tree-icon').name = 'minus-square';
                                    }
                                    rowElement.setAttribute('expend', '');
                                }
                                if (rowElement.hasAttribute('expend')) {
                                    foldNode(rowElement);
                                } else {
                                    expendNode(rowElement);
                                }
                                e.stopPropagation();
                            };
                        } else {
                            td.style.paddingLeft = offsetVal + 'px';
                        }
                    }
                    rowElement.append(td);
                })
                if (this.selectable) {
                    rowElement.style.gridTemplateColumns = '60px ' + gridTemplateColumns.join(' ');
                } else {
                    rowElement.style.gridTemplateColumns = gridTemplateColumns.join(' ');
                }
                rowElement.onclick = e => {
                    this.dispatchEvent(new CustomEvent('row-click', {detail: rowData, composed: true}));
                }
                rowElement.style.cursor = 'pointer'
                parentNode.append(rowElement);
                rowElement.setAttribute('id', rowData[ids[0]]);
                rowElement.setAttribute('pid', rowData[ids[1]]);
                rowElement.setAttribute('expend', '');
                if (rowData.children && rowData.children.length > 0) {
                    offsetVal = offsetVal + offset;
                    drawRow(rowData.children, parentNode);
                    offsetVal = offsetVal - offset;
                }
            });
        };
        drawRow(treeData, this.tbodyElement);
    }

    getCheckRows() {
        // @ts-ignore
        return [...this.shadowRoot!.querySelectorAll('div[class=tr][checked]')].map(a => a.data).map(a => {
            delete a['children'];
            return a;
        });
    }

    deleteRowsCondition(fn: any) {
        this.shadowRoot!.querySelectorAll("div[class=tr]").forEach(tr => {
            // @ts-ignore
            if (fn(tr.data)) {
                tr.remove();
            }
        })
    }

    meauseElementHeight(rowData: any) {
        if (this.meauseRowElement == undefined) {
            this.meauseRowElement = this.createNewTableElement(rowData)
            this.meauseRowElement!.style.width = this.tableElement!.clientWidth + "px"
            this.meauseRowElement!.style.top = "-100px"
            this.meauseRowElement!.style.position = 'absolute'
            this.meauseRowElement!.style.left = "0px"
            this.tbodyElement?.append(this.meauseRowElement!)
        } else {
            this.meauseRowElement.childNodes.forEach((children: any, index: number) => {
                if (children.template) {
                    children.innerHTML = children.template.render(rowData).content.cloneNode(true).innerHTML
                } else {
                    children.innerHTML = rowData[children.dataIndex]
                }
            })
        }
        return this.meauseRowElement!.clientHeight
    }

    meauseTreeElementHeight(rowData: any, depth: number) {
        if (this.meauseRowElement == undefined) {
            this.meauseRowElement = this.createNewTreeTableElement(rowData)
            this.meauseRowElement!.style.width = this.tableElement!.clientWidth + "px"
            this.meauseRowElement!.style.top = "-100px"
            this.meauseRowElement!.style.position = 'absolute'
            this.meauseRowElement!.style.left = "0px"
            this.tbodyElement?.append(this.meauseRowElement!)
        } else {
            this.meauseRowElement.childNodes.forEach((children: any, index: number) => {
                if (index == 0) {
                    children.style.paddingLeft = depth * 30 + "px"
                }
                if (children.template) {
                    children.innerHTML = children.template.render(rowData.data).content.cloneNode(true).innerHTML
                } else {
                    children.innerHTML = rowData.data[children.dataIndex]
                }
            })
        }
        return this.meauseRowElement!.clientHeight
    }

    meauseAllRowHeight(list: any[]): TableRowObject[] {
        this.tbodyElement!.innerHTML = '';
        this.meauseRowElement = undefined
        this.tbodyElement && (this.tbodyElement.style.width = this.tableElement?.clientWidth + "px")
        this.currentRecycleList = []
        let headHeight = this.theadElement?.clientHeight || 0
        let totalHeight = headHeight
        let visibleObjects: TableRowObject[] = [];
        list.forEach((rowData, index) => {
            let height = this.meauseElementHeight(rowData);
            let tableRowObject = new TableRowObject();
            tableRowObject.height = height
            tableRowObject.top = totalHeight
            tableRowObject.data = rowData
            tableRowObject.rowIndex = index
            if (Math.max(totalHeight, this.tableElement!.scrollTop + headHeight) <= Math.min(totalHeight + height, this.tableElement!.scrollTop + this.tableElement!.clientHeight + headHeight)) {
                let newTableElement = this.createNewTableElement(tableRowObject);
                newTableElement.style.transform = `translateY(${totalHeight}px)`
                this.tbodyElement?.append(newTableElement)
                this.currentRecycleList.push(newTableElement)
            }
            totalHeight += height
            visibleObjects.push(tableRowObject)
        })
        this.tbodyElement && (this.tbodyElement.style.height = totalHeight + "px")
        this.tableElement && (this.tableElement.onscroll = (event) => {
            let top = this.tableElement!.scrollTop + headHeight;
            let skip = 0;
            for (let i = 0; i < visibleObjects.length; i++) {
                if (visibleObjects[i].top <= top && visibleObjects[i].top + visibleObjects[i].height >= top) {
                    skip = i
                    break;
                }
            }
            let reduce = this.currentRecycleList.map((item) => item.clientHeight).reduce((a, b) => a + b);
            while (reduce <= this.tableElement!.clientHeight) {
                let newTableElement = this.createNewTableElement(visibleObjects[skip].data);
                this.tbodyElement?.append(newTableElement)
                this.currentRecycleList.push(newTableElement)
                reduce += newTableElement.clientHeight
            }
            for (let i = 0; i < this.currentRecycleList.length; i++) {
                this.freshCurrentLine(this.currentRecycleList[i], visibleObjects[i + skip])
            }
        })
        return visibleObjects
    }

    meauseTreeRowElement(list: any[]): TableRowObject[] {
        this.meauseRowElement = undefined
        this.tbodyElement!.innerHTML = '';
        this.tbodyElement && (this.tbodyElement.style.width = this.tableElement?.clientWidth + "px")
        let headHeight = this.theadElement?.clientHeight || 0
        let totalHeight = headHeight
        let visibleObjects: TableRowObject[] = []
        this.currentRecycleList = []
        let resetAllHeight = (list: any[], depth: number, parentNode?: TableRowObject) => {
            list.forEach((item) => {
                let tableRowObject = new TableRowObject();
                tableRowObject.depth = depth
                tableRowObject.data = item
                tableRowObject.top = totalHeight//初始化高度
                tableRowObject.height = this.meauseTreeElementHeight(tableRowObject, depth)
                if (parentNode != undefined) {
                    parentNode.children.push(tableRowObject)
                }
                if (Math.max(totalHeight, this.tableElement!.scrollTop + headHeight) <= Math.min(totalHeight + tableRowObject.height, this.tableElement!.scrollTop + this.tableElement!.clientHeight + headHeight)) {
                    let newTableElement = this.createNewTreeTableElement(tableRowObject);
                    newTableElement.style.transform = `translateY(${totalHeight}px)`
                    this.tbodyElement?.append(newTableElement)
                    this.currentRecycleList.push(newTableElement)
                }
                totalHeight += tableRowObject.height
                visibleObjects.push(tableRowObject)
                if (item.children != undefined && item.children.length > 0) {
                    resetAllHeight(item.children, depth + 1, tableRowObject)
                }
            })
        }
        resetAllHeight(list, 0)
        console.log(visibleObjects);
        this.tbodyElement && (this.tbodyElement.style.height = totalHeight + "px")
        this.tableElement && (this.tableElement.onscroll = (event) => {
            let visibleObjects = this.recycleDs.filter((item) => {
                return !item.rowHidden
            })
            let top = this.tableElement!.scrollTop + headHeight;
            let skip = 0;
            for (let i = 0; i < visibleObjects.length; i++) {
                if (visibleObjects[i].top <= top && visibleObjects[i].top + visibleObjects[i].height >= top) {
                    skip = i
                    break;
                }
            }
            let reduce = this.currentRecycleList.map((item) => item.clientHeight).reduce((a, b) => a + b);
            while (reduce <= this.tableElement!.clientHeight) {
                let newTableElement = this.createNewTreeTableElement(visibleObjects[skip]);
                this.tbodyElement?.append(newTableElement)
                this.currentRecycleList.push(newTableElement)
                reduce += newTableElement.clientHeight
            }
            for (let i = 0; i < this.currentRecycleList.length; i++) {
                console.log(visibleObjects[i + skip]);
                this.freshCurrentLine(this.currentRecycleList[i], visibleObjects[i + skip])
            }
        })
        return visibleObjects
    }


    createNewTreeTableElement(rowData: TableRowObject): any {
        let newTableElement = document.createElement('div');
        newTableElement.classList.add('tr');
        let gridTemplateColumns: Array<any> = [];
        this?.columns?.forEach((column: any, index) => {
            let dataIndex = column.getAttribute('data-index') || '1';
            gridTemplateColumns.push(column.getAttribute('width') || '1fr')
            let td: any
            if (column.template) {
                td = column.template.render(rowData.data).content.cloneNode(true);
                td.template = column.template
            } else {
                td = document.createElement('div')
                td.classList.add('td');
                td.style.wordBreak = 'break-all'
                td.innerHTML = rowData.data[dataIndex];
                td.dataIndex = dataIndex
            }
            if (index === 0) {
                if (rowData.data.children && rowData.data.children.length > 0) {
                    let btn = this.createExpandBtn(rowData)
                    td.insertBefore(btn, td.firstChild);
                    td.style.paddingLeft = rowData.depth * 30 + 'px';
                } else {
                    td.style.paddingLeft = rowData.depth * 30 + 'px';
                }
            }
            newTableElement.append(td)
        })
        newTableElement.style.gridTemplateColumns = gridTemplateColumns.join(' ');
        newTableElement.style.position = 'absolute';
        newTableElement.style.top = '0px'
        newTableElement.style.left = '0px'
        return newTableElement
    }

    createExpandBtn(rowData: any) {
        let btn: any = document.createElement('lit-icon');
        btn.classList.add('tree-icon');
        // @ts-ignore
        if (rowData.expanded) {
            btn.name = 'minus-square';
        } else {
            btn.name = 'plus-square';
        }
        btn.onclick = (e: Event) => {
            const resetNodeHidden = (hidden: boolean, rowData: any) => {
                if (rowData.children.length > 0) {
                    if (hidden) {
                        rowData.children.forEach((child: any) => {
                            child.rowHidden = true
                            resetNodeHidden(hidden, child)
                        })
                    } else {
                        rowData.children.forEach((child: any) => {
                            child.rowHidden = !rowData.expanded
                            if (rowData.expanded) {
                                resetNodeHidden(hidden, child)
                            }
                        })
                    }
                }
            }
            const foldNode = () => {
                rowData.expanded = false
                resetNodeHidden(true, rowData)
            };
            const expendNode = () => {
                rowData.expanded = true
                resetNodeHidden(false, rowData)
            }
            if (rowData.expanded) {
                foldNode()
            } else {
                expendNode()
            }
            this.reMeauseHeight()
            e.stopPropagation();
        };
        return btn
    }

    reMeauseHeight() {
        let headHeight = this.theadElement?.clientHeight || 0
        let totalHeight = headHeight
        this.recycleDs.forEach((it) => {
            if (!it.rowHidden) {
                it.top = totalHeight
                totalHeight += it.height
            }
        })
        this.tbodyElement && (this.tbodyElement.style.height = totalHeight + "px")
        let visibleObjects = this.recycleDs.filter((item) => {
            return !item.rowHidden
        })
        let top = this.tableElement!.scrollTop + headHeight;
        let skip = 0;
        for (let i = 0; i < visibleObjects.length; i++) {
            if (visibleObjects[i].top <= top && visibleObjects[i].top + visibleObjects[i].height >= top) {
                skip = i
                break;
            }
        }
        let reduce = this.currentRecycleList.map((item) => item.clientHeight).reduce((a, b) => a + b);
        while (reduce <= this.tableElement!.clientHeight) {
            let newTableElement = this.createNewTreeTableElement(visibleObjects[skip]);
            this.tbodyElement?.append(newTableElement)
            this.currentRecycleList.push(newTableElement)
            reduce += newTableElement.clientHeight
        }
        for (let i = 0; i < this.currentRecycleList.length; i++) {
            this.freshCurrentLine(this.currentRecycleList[i], visibleObjects[i + skip])
        }
    }

    createNewTableElement(rowData: any): any {
        let newTableElement = document.createElement('div');
        newTableElement.classList.add('tr');
        let gridTemplateColumns: Array<any> = [];
        this?.columns?.forEach((column: any) => {
            let dataIndex = column.getAttribute('data-index') || '1';
            gridTemplateColumns.push(column.getAttribute('width') || '1fr')
            let td: any
            if (column.template) {
                td = column.template.render(rowData).content.cloneNode(true);
                td.template = column.template
            } else {
                td = document.createElement('div')
                td.classList.add('td');
                td.style.wordBreak = 'break-all'
                td.innerHTML = rowData[dataIndex];
                td.dataIndex = dataIndex
            }
            newTableElement.append(td)
        })
        newTableElement.style.gridTemplateColumns = gridTemplateColumns.join(' ');
        newTableElement.style.position = 'absolute';
        newTableElement.style.top = '0px'
        newTableElement.style.left = '0px'
        return newTableElement
    }

    freshCurrentLine(element: HTMLElement, rowObject: TableRowObject) {
        element.childNodes.forEach((child, index) => {
            if ((this.columns![index] as any).template) {
                (child as HTMLElement).innerHTML = (this.columns![index] as any).template.render(rowObject.data).content.cloneNode(true).innerHTML
            } else {
                let dataIndex = this.columns![index].getAttribute('data-index') || '1';
                (child as HTMLElement).innerHTML = rowObject.data[dataIndex]
            }
            if (rowObject.depth != -1 && index == 0) {
                if (rowObject.children && rowObject.children.length > 0) {
                    let btn = this.createExpandBtn(rowObject)
                    child.insertBefore(btn, child.firstChild);
                }
                (child as HTMLElement).style.paddingLeft = 30 * rowObject.depth + "px"
            }
        })
        element.style.transform = `translateY(${rowObject.top}px)`
    }
}

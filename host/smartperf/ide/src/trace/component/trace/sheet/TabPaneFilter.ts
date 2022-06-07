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

import {BaseElement, element} from "../../../../base-ui/BaseElement.js";
import "../../../../base-ui/select/LitSelect.js";
import "../../../../base-ui/select/LitSelectOption.js";
import '../../../../base-ui/icon/LitIcon.js'
import {LitIcon} from "../../../../base-ui/icon/LitIcon.js";
import {LitSelect} from "../../../../base-ui/select/LitSelect.js";
import "../../../../base-ui/popover/LitPopoverV.js"
import {LitCheckBox} from "../../../../base-ui/checkbox/LitCheckBox.js";

export interface FilterData {
    inputValue: string,
    firstSelect: string | null | undefined,
    secondSelect: string | null | undefined,
    mark: boolean | null | undefined,
    icon: string | null,
}

export interface MiningData {
    type: string,
    item: any | null | undefined,
    remove?: Array<any> | null | undefined,
}

@element('tab-pane-filter')
export class TabPaneFilter extends BaseElement {
    private filterInputEL: HTMLInputElement | null | undefined;
    private firstSelectEL: HTMLSelectElement | null | undefined;
    private secondSelectEL: HTMLSelectElement | null | undefined;
    private markButtonEL: HTMLButtonElement | null | undefined;
    private iconEL: LitIcon | null | undefined;
    private getFilter: ((e: FilterData) => void) | undefined;
    private getMining: ((e: MiningData) => void) | undefined;
    private getCallTree: ((e: any) => void) | undefined;
    private getCallTreeConstraints: ((e: any) => void) | undefined;

    private cutList: Array<any> | undefined;

    initElements(): void {
        this.cutList = [];
        this.filterInputEL = this.shadowRoot?.querySelector("#filter-input")
        this.markButtonEL = this.shadowRoot?.querySelector("#mark")
        this.iconEL = this.shadowRoot?.querySelector<LitIcon>("#icon")
        this.iconEL!.onclick = (e) => {
            if (this.iconEL!.name == "statistics") {
                this.iconEL!.name = "menu";
                this.iconEL!.size = 18;
                if (this.getFilter) {
                    this.getFilter({
                        inputValue: this.filterInputEL!.value,
                        firstSelect: this.firstSelectEL?.value,
                        secondSelect: this.secondSelectEL?.value,
                        mark: false,
                        icon: this.icon
                    })
                }
            } else if (this.iconEL!.name == "menu") {
                this.iconEL!.name = "statistics";
                this.iconEL!.size = 16;
                if (this.getFilter) {
                    this.getFilter({
                        inputValue: this.filterInputEL!.value,
                        firstSelect: this.firstSelectEL?.value,
                        secondSelect: this.secondSelectEL?.value,
                        mark: false,
                        icon: this.icon
                    })
                }
            }
        }

        this.markButtonEL!.onclick = (e) => {
            if (this.getFilter) {
                this.getFilter({
                    inputValue: this.filterInputEL!.value,
                    firstSelect: this.firstSelectEL?.value,
                    secondSelect: this.secondSelectEL?.value,
                    mark: true,
                    icon: this.icon
                })
            }
        }

        this.filterInputEL?.addEventListener("keydown", (event: any) => {
            if (event.keyCode == 13) {
                this.iconEL!.name = "menu"
                if (this.getFilter) {
                    this.getFilter({
                        inputValue: event.target.value,
                        firstSelect: this.firstSelectEL?.value,
                        secondSelect: this.secondSelectEL?.value,
                        mark: false,
                        icon: this.icon
                    })
                }
            }
        });

        this.setSelectList()

        this.initializeCallTree()

        this.initializeTreeConstraints()

        this.initializeMining()

        this.shadowRoot!.querySelectorAll<HTMLDivElement>(".mining-button").forEach((e, idx) => {
            e!.onclick = (ev) => {
                if (idx == 0) {
                    if (this.getMining) {
                        this.getMining({type: "button", item: "symbol"});
                    }
                } else if (idx == 1) {
                    if (this.getMining) {
                        this.getMining({type: "button", item: "library"});
                    }
                } else if (idx == 2) {
                    const restoreList = this.cutList!.filter(item => item.highlight === true)
                    const list = this.cutList!.filter(item => item.highlight === false)
                    this.cutList = list;
                    if (this.getMining) {
                        this.getMining({type: "button", item: "restore", remove: restoreList});
                    }
                    this.initializeMining();
                }
            }
        })
    }

    set firstSelect(value: string) {
        this.firstSelectEL!.value = value;
    }

    get firstSelect() {
        return this.firstSelectEL?.value || ""
    }

    set secondSelect(value: string) {
        this.secondSelectEL!.value = value;
    }

    get secondSelect() {
        return this.secondSelectEL?.value || ""
    }

    set filterValue(value: string) {
        this.filterInputEL!.value = value;
    }

    get filterValue() {
        return this.filterInputEL!.value
    }

    get inputPlaceholder() {
        return this.getAttribute("inputPlaceholder") || "Detail Filter";
    }

    get icon() {
        if (this.getAttribute("icon") != "false") {
            if (this.iconEL!.name == "statistics") {
                return "tree"
            } else if (this.iconEL!.name == "menu") {
                return "block"
            } else {
                return ""
            }
        } else {
            return "";
        }
    }

    set icon(value: string) {
        if (value == "block") {
            this.iconEL!.name = "menu";
            this.iconEL!.size = 18;
        } else if (value == "tree") {
            this.iconEL!.name = "statistics";
            this.iconEL!.size = 16;
        }
    }

    getCallTreeData(getCallTree: (v: any) => void) {
        this.getCallTree = getCallTree
    }

    getCallTreeConstraintsData(getCallTreeConstraints: (v: any) => void) {
        this.getCallTreeConstraints = getCallTreeConstraints
    }

    getFilterData(getFilter: (v: FilterData) => void) {
        this.getFilter = getFilter
    }

    setSelectList(firstList: Array<any> | null | undefined = ["All Allocations", "Created & Existing", "Created & Destroyed"],
                  secondList: Array<any> | null | undefined = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"]) {
        if (!firstList && !secondList) return;
        let sLE = this.shadowRoot?.querySelector("#load")
        let html = ``;
        if (firstList) {
            html += `<lit-select default-value="" id="first-select" class="spacing" placeholder="please choose">
            <lit-select-option value="Allocation Lifespan" disabled>Allocation Lifespan</lit-select-option>`
            firstList!.forEach((a, b) => {
                html += `<lit-select-option value="${b}">${a}</lit-select-option>`
            })
            html += `</lit-select>`
        }
        if (secondList) {
            html += `<lit-select default-value="" id="second-select" class="spacing" placeholder="please choose">
            <lit-select-option value="Allocation Type" disabled>Allocation Type</lit-select-option>`
            secondList!.forEach((a, b) => {
                html += `<lit-select-option value="${b}">${a}</lit-select-option>`
            })
            html += `</lit-select>`
        }
        if (!firstList) {
            this.secondSelectEL!.outerHTML = html;
        } else if (!secondList) {
            this.firstSelectEL!.outerHTML = html;
        } else {
            sLE!.innerHTML = html;
        }

        this.firstSelectEL = this.shadowRoot?.querySelector("#first-select")
        this.secondSelectEL = this.shadowRoot?.querySelector("#second-select")

        this.firstSelectEL!.onchange = (e) => {
            if (this.getFilter) {
                this.getFilter({
                    inputValue: this.filterInputEL!.value,
                    firstSelect: this.firstSelectEL?.value,
                    secondSelect: this.secondSelectEL?.value,
                    mark: false,
                    icon: this.icon
                })
            }
        }
        this.secondSelectEL!.onchange = (e) => {
            if (this.getFilter) {
                this.getFilter({
                    inputValue: this.filterInputEL!.value,
                    firstSelect: this.firstSelectEL?.value,
                    secondSelect: this.secondSelectEL?.value,
                    mark: false,
                    icon: this.icon
                })
            }
        }
    }

    initializeCallTree() {
        let row = this.shadowRoot!.querySelectorAll(".tree-check");
        row.forEach((e, idx) => {
            let check = e.querySelector<LitCheckBox>("lit-check-box")
            e.querySelector("div")!.onclick = (ev) => {
                if (this.getCallTree) {
                    if (idx == 0) {
                        this.getCallTree({
                            checks: [!check!.checked, row[1].querySelector<LitCheckBox>("lit-check-box")!.checked],
                            value: idx
                        });
                    } else {
                        this.getCallTree({
                            checks: [row[0].querySelector<LitCheckBox>("lit-check-box")!.checked, !check!.checked],
                            value: idx
                        });
                    }
                }
                check!.checked = !check!.checked;
            }
            check!.onchange = (ev: any) => {
                if (this.getCallTree) {
                    if (idx == 0) {
                        this.getCallTree({
                            checks: [ev.target.checked, row[1].querySelector<LitCheckBox>("lit-check-box")!.checked],
                            value: idx
                        });
                    } else {
                        this.getCallTree({
                            checks: [row[0].querySelector<LitCheckBox>("lit-check-box")!.checked, ev.target.checked],
                            value: idx
                        });
                    }
                }
            }
        })
    }

    initializeTreeConstraints() {
        let inputs = this.shadowRoot!.querySelectorAll<HTMLInputElement>(".constraints-input")
        let check = this.shadowRoot!.querySelector<LitCheckBox>("#constraints-check")
        check!.onchange = (ev: any) => {
            inputs.forEach((e: any, idx) => {
                if (inputs[idx].value == "") {
                    inputs[idx].value = idx == 0 ? "0" : "∞"
                }
                ev.target.checked ? e.removeAttribute("disabled") : e.setAttribute("disabled", '')
            })
            if (this.getCallTreeConstraints) {
                this.getCallTreeConstraints({
                    checked: ev.target.checked,
                    min: inputs[0].value,
                    max: inputs[1].value
                });
            }
        }
        inputs.forEach((e, idx) => {
            e.oninput = function () {
                // @ts-ignore
                this.value = this.value.replace(/\D/g, '');
            }
            e.addEventListener("keydown", (event: any) => {
                if (event.keyCode == "13") {
                    if (event?.target.value == "") {
                        inputs[idx].value = idx == 0 ? "0" : "∞"
                    }
                    if (this.getCallTreeConstraints) {
                        this.getCallTreeConstraints({
                            checked: check!.checked,
                            min: idx == 0 ? event?.target.value : inputs[0].value,
                            max: idx == 1 ? event?.target.value : inputs[1].value
                        });
                    }
                }
            })
        })
    }

    initializeMining() {
        let html = ``;
        this.cutList!.forEach((a, b) => {
            html += `<div style="display: flex;padding: 4px 7px;" class="mining-checked" ${a.highlight ? "highlight" : ""}>
                        <lit-check-box class="lit-check-box" not-close ${a.checked ? "checked" : ""} style="display: flex"></lit-check-box>
                        <div id="title" title="${a.name}">${a.name}</div>
                        <lit-select default-value="${a.select}" border='false' style="width: 100px;" ${a.highlight ? "highlight" : ""} adaptive-expansion not-close>    
                            <lit-select-option value="0">Charge</lit-select-option>`;
            html += `<lit-select-option value="1">Prune</lit-select-option>`;
            html += `</lit-select>
                       </div>`
        })

        this.shadowRoot!.querySelector<HTMLDivElement>("#mining-row")!.innerHTML = html;

        let row = this.shadowRoot!.querySelector("#mining-row")!.childNodes;
        row!.forEach((e: any, idx) => {
            e!.querySelector("#title")!.onclick = (ev: any) => {
                if (e.getAttribute("highlight") == "") {
                    e.removeAttribute("highlight")
                    e.querySelector("lit-select").removeAttribute("highlight");
                    this.cutList![idx].highlight = false;
                } else {
                    e.setAttribute("highlight", "");
                    e.querySelector("lit-select").setAttribute("highlight", "");
                    this.cutList![idx].highlight = true;
                }
            }
            // @ts-ignore
            e!.querySelector<LitSelect>("lit-select")!.onchange = (ev) => {
                // @ts-ignore
                this.cutList[idx].select = e!.querySelector<LitSelect>("lit-select")!.value;
                // @ts-ignore
                e!.querySelector<LitCheckBox>("lit-check-box")!.checked = true;
                this.cutList![idx].checked = true;
                if (this.getMining) {
                    this.getMining({type: "select", item: this.cutList![idx]});
                }
            }
            // @ts-ignore
            e!.querySelector<LitCheckBox>("lit-check-box")!.onchange = (ev) => {
                // @ts-ignore
                this.cutList[idx].checked = e!.querySelector<LitCheckBox>("lit-check-box")!.checked;
                if (this.getMining) {
                    this.getMining({type: "check", item: this.cutList![idx]});
                }
            }
        })
    }

    getDataMining(getMining: (v: MiningData) => void) {
        this.getMining = getMining
    }

    addDataMining(data: any, type: string) {
        let idx = this.cutList!.findIndex((e) => e.name == data.name)
        if (idx == -1) {
            this.cutList!.push({type: type, name: data.name, checked: true, select: "0", data: data, highlight: false});
        } else {
            this.cutList![idx] = {type: type, name: data.name, checked: true, select: "0", data: data, highlight: false}
        }
        this.initializeMining();
        return idx;
    }

    getFilterTreeData() {
        let row = this.shadowRoot!.querySelectorAll<LitCheckBox>(".tree-check lit-check-box");
        let inputs = this.shadowRoot!.querySelectorAll<HTMLInputElement>(".constraints-input")
        let check = this.shadowRoot!.querySelector<LitCheckBox>("#constraints-check")
        let data = {
            callTree: [row[0]!.checked, row[1]!.checked],
            callTreeConstraints: {
                checked: check!.checked,
                inputs: [inputs[0].value == "" ? "0" : inputs[0].value, inputs[1].value == "" ? "∞" : inputs[1].value]
            },
            dataMining: this.cutList,
        }
        return data;
    }

    initializeFilterTree(callTree: boolean = true, treeConstraints: boolean = true, mining: boolean = true) {
        if (callTree) {
            let row = this.shadowRoot!.querySelectorAll(".tree-check");
            row.forEach((e, idx) => {
                let check = e.querySelector<LitCheckBox>("lit-check-box")
                check!.checked = false
            })
        }
        if (treeConstraints) {
            let inputs = this.shadowRoot!.querySelectorAll<HTMLInputElement>(".constraints-input")
            if (inputs.length > 0) {
                inputs[0].value = "0";
                inputs[1].value = "∞";
            }
            let check = this.shadowRoot!.querySelector<LitCheckBox>("#constraints-check")
            check!.checked = false
        }
        if (mining) {
            this.cutList = [];
            this.initializeMining();
        }
    }

    initHtml(): string {
        return `
        <style>
        :host{
            height: 30px;
            background: var(--dark-background4,#F2F2F2);
            border-top: 1px solid var(--dark-border1,#c9d0da);display: flex;align-items: center;z-index: 2;
            margin-left: -10px;
        }

        .chosen-single {
            position: relative;
            display: block;
            overflow: hidden;
            text-decoration: none;
            white-space: nowrap;
            height: 34px;
            padding: 3px 6px;
            font-size: 14px;
            line-height: 1.42857143;
            color: #555;
            background-color: #fff;
            background-image: none;
            border: 1px solid #ccc;
            border-radius: 4px;
            transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
            box-shadow: inset 0 1px 1px rgba(0,0,0,.075);    
        }
        .disabled{
        color: rgba(0,0,0,0.4);
        }
        #filter-input{
        background: var(--dark-background4,#FFFFFF);
        border: 1px solid var(--dark-border,rgba(0,0,0,0.60));
        color: var(--dark-color2,#000000);
        border-radius: 8px;
        width: 200px;
        }
        #filter-input:focus{
            outline: none;
            box-shadow: 1px 1px 1px var(--dark-color,#bebebe);
        }
        #filter-input::-webkit-input-placeholder {
                color: var(--dark-color,#aab2bd);
            }
        .describe{
        font-size: 0.8rem;
        }

        #mark{
            border: 1px solid var(--bark-prompt,#999999);
            border-radius: 1px;
            background: var(--dark-background4,#F2F2F2);
            color: var(--dark-color2,rgba(0,0,0,0.9));
            transition: all 0.1s;
        }
        #mark:hover{
            background: var(--dark-background1,#dfdfdf);
        }
        #mark:active{
            background: var(--dark-background4,#F2F2F2);
            transition: all 0.05s;
        }
        #first-select{
        width: 200px;
        }
        #second-select{
        width: 200px;
        }
        .spacing{
        margin-left: 10px;
        }
        .max-spacing{
        margin-left: 15px;
        }

        :host(:not([inputLeftText])) .left-text{
            display: none;
        }
        :host(:not([input])) #filter-input{
            display: none;
        }
        :host(:not([mark])) #mark{
            display: none;
        }
        :host(:not([first])) #first-select{
            display: none;
        }
        :host(:not([second])) #second-select{
            display: none;
        }
        :host(:not([tree])) .tree{
            display: none;
        }
        :host(:not([icon])) #icon{
            display: none;
        }
        #icon[name="statistics"]{
            margin-left: 12px;
        }
        
        .constraints-input{
            background: var(--dark-border,#ffffff);
            color: var(--dark-color1,rgba(0,0,0,0.86));
            border: 1px solid var(--dark-border,rgba(0,0,0,0.60));
            border-radius: 10px;
            width: 40px;
            margin-left: 10px;
            outline: none;
        }
        .constraints-input[disabled]{
            background: var(--dark-background5,#ededed);
        }
        .mining-button{
            opacity: 0.9;
            font-size: 13px;
            color: #0A59F7;
            text-align: center;
            line-height: 16px;
            background: var(--dark-background3,#F4F3F4);
            border: 1px solid var(--dark-background8,#F4F3F4);
            border-radius: 16px;
            padding: 2px 8px;
        }
        
        #call-tree-popover[visible="true"] #call-tree{
            color: #0A59F7;
        }
        #tree-constraints-popover[visible="true"] #tree-constraints{
            color: #0A59F7;
        }
        #data-mining-popover[visible="true"] #data-mining{
            color: #0A59F7;
        }
        
        .mining-checked[highlight]{
            color: #FFFFFF;
            background: #0C65D1;
        }
        #title{
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            flex: 1;
            text-align: left;
        }
        #mining-row{
            background: var(--dark-background4,#F2F2F2);
            border-radius: 2px;
            height: 135px;
            width: 250px;
            overflow-y: auto;
        }
        .tree-check{
            margin-bottom: 5px;
            display: flex;
            align-content: center;
        }
</style>
    <lit-icon name="statistics" class="spacing" id="icon" size="16"></lit-icon>
    <span class="describe left-text spacing">Input Filter</span>
    <input id="filter-input" class="spacing" placeholder="${this.inputPlaceholder}"/>
    <button id="mark" class="spacing">Mark Snapshot</button>
    <div id="load" style="display: flex">
    
    </div>
       <lit-popover placement="topLeft" class="popover" haveRadio="true" trigger="click" id="call-tree-popover">
            <div slot="content">
                <div class="tree-check"><lit-check-box class="lit-check-box" not-close></lit-check-box><div>Invert</div></div>
                <div class="tree-check"><lit-check-box class="lit-check-box" not-close></lit-check-box><div>Hide System so</div></div>
            </div>
            <span class="describe tree max-spacing" id="call-tree">Options</span>
       </lit-popover>
       <lit-popover placement="topLeft" class="popover" haveRadio="true" trigger="click" id="tree-constraints-popover">
            <div slot="content" style="display: flex; align-items: flex-end">
                <lit-check-box id="constraints-check" not-close></lit-check-box>
                <input class="constraints-input" disabled value="0" not-close/>
                <lit-popover placement="topLeft" class="popover" haveRadio="true" not-close>
                    <div slot="content">
                        <div style="font-size: 0.7rem">Constraints：Only enabled with data and while stopped；</div>
                        <div style="font-size: 0.7rem">filters data to thresholds. </div>
                    </div>
                    <input class="constraints-input" disabled value="∞" not-close/>
                 </lit-popover>
            </div>
            <span class="describe tree max-spacing" id="tree-constraints">Sample Count Filter</span>
       </lit-popover>
       <lit-popover placement="topLeft" class="popover" haveRadio="true" trigger="click" id="data-mining-popover">
           <div slot="content">
                <div id="mining-row">
                
                </div>
                <div style="display: flex;justify-content: space-around; margin-top: 8px">
                    <div class="mining-button">Symbol</div>
                    <div class="mining-button">Library</div>
                    <div class="mining-button">Restore</div>
                </div>
           </div>
           <span class="describe tree max-spacing" id="data-mining">Data Mining</span>
       </lit-popover>
        `;
    }
}

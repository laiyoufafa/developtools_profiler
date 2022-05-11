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

export interface FilterData{
	inputValue:string,
	firstSelect:string|null|undefined,
	secondSelect:string|null|undefined,
	mark:boolean|null|undefined,
	icon:string|null,
}

@element('tab-pane-filter')
export class TabPaneFilter extends BaseElement {
	private filterInputEL: HTMLInputElement | null | undefined;
	private firstSelectEL: HTMLSelectElement | null | undefined;
	private secondSelectEL: HTMLSelectElement | null | undefined;
	private markButtonEL: HTMLButtonElement | null | undefined;
	private iconEL: LitIcon | null | undefined;
	private getFilter: ((e:FilterData)=>void) | undefined;

	initElements(): void {
		// this.firstSelectEL = this.shadowRoot?.querySelector("#first-select")
		// this.secondSelectEL = this.shadowRoot?.querySelector("#second-select")
		this.filterInputEL = this.shadowRoot?.querySelector("#filter-input")
		this.markButtonEL = this.shadowRoot?.querySelector("#mark")
		this.iconEL = this.shadowRoot?.querySelector<LitIcon>("#icon")

		this.iconEL!.onclick=(e)=>{
			if (this.iconEL!.name == "statistics") {
				this.iconEL!.name = "menu";
				this.iconEL!.size = 18;
				if (this.getFilter) {
					this.getFilter({
						inputValue:this.filterInputEL!.value,
						firstSelect:this.firstSelectEL?.value,
						secondSelect:this.secondSelectEL?.value,
						mark:false,
						icon:this.icon
					})
				}
			}else if (this.iconEL!.name == "menu") {
				this.iconEL!.name = "statistics";
				this.iconEL!.size = 16;
				if (this.getFilter) {
					this.getFilter({
						inputValue:this.filterInputEL!.value,
						firstSelect:this.firstSelectEL?.value,
						secondSelect:this.secondSelectEL?.value,
						mark:false,
						icon:this.icon
					})
				}
			}
		}

		this.markButtonEL!.onclick=(e)=>{
			if (this.getFilter) {
				this.getFilter({
					inputValue:this.filterInputEL!.value,
					firstSelect:this.firstSelectEL?.value,
					secondSelect:this.secondSelectEL?.value,
					mark:true,
					icon:this.icon
				})
			}
		}

		this.filterInputEL?.addEventListener("keydown", (event:any) => {
			if (event.keyCode == 13) {
				this.iconEL!.name="menu"
				if (this.getFilter) {
					this.getFilter({
						inputValue:event.target.value,
						firstSelect:this.firstSelectEL?.value,
						secondSelect:this.secondSelectEL?.value,
						mark:false,
						icon:this.icon
					})
				}
			}
		});

		// this.firstSelectEL!.onchange = (e)=>{
		// 	if (this.getFilter) {
		// 		this.getFilter({
		// 			inputValue:this.filterInputEL!.value,
		// 			firstSelect:this.firstSelectEL?.value,
		// 			secondSelect:this.secondSelectEL?.value,
		// 			mark:false
		// 		})
		// 	}
		// }
		// this.secondSelectEL!.onchange = (e)=>{
		// 	if (this.getFilter) {
		// 		this.getFilter({
		// 			inputValue:this.filterInputEL!.value,
		// 			firstSelect:this.firstSelectEL?.value,
		// 			secondSelect:this.secondSelectEL?.value,
		// 			mark:false
		// 		})
		// 	}
		// }
		this.setSelectList()
	}

	set firstSelect(value:string){
		this.firstSelectEL!.value = value;
	}

	get firstSelect(){
		return this.firstSelectEL?.value||""
	}

	set secondSelect(value:string){
		this.secondSelectEL!.value = value;
	}

	get secondSelect(){
		return this.secondSelectEL?.value||""
	}

	set filterValue(value:string){
		this.filterInputEL!.value = value;
	}
	get filterValue(){
		return this.filterInputEL!.value
	}

	get inputPlaceholder(){
		return this.getAttribute("inputPlaceholder") || "Detail Filter";
	}

	get icon(){
		if (this.getAttribute("icon") != "false") {
			if (this.iconEL!.name == "statistics") {
				return "tree"
			}else if (this.iconEL!.name == "menu") {
				return "block"
			}else {
				return ""
			}
		} else {
			return "";
		}
	}

	set icon(value:string){
		if (value == "block") {
			this.iconEL!.name = "menu";
			this.iconEL!.size = 18;
		}else if (value == "tree") {
			this.iconEL!.name = "statistics";
			this.iconEL!.size = 16;
		}
	}


	getFilterData(getFilter:(v:FilterData)=>void){
		this.getFilter = getFilter
	}

	setSelectList(firstList :Array<any>|null|undefined = [ "All Allocations" ,"Created & Existing" ,"Created & Destroyed" ],
				  secondList :Array<any>|null|undefined = ["All Heap & Anonymous VM", "All Heap", "All Anonymous VM"]){
		if (!firstList && !secondList) return;
		let sLE = this.shadowRoot?.querySelector("#load")
		let html = ``;
		if (firstList) {
			html += `<lit-select default-value="" id="first-select" class="spacing" placeholder="please choose">
    		<lit-select-option value="Allocation Lifespan" disabled>Allocation Lifespan</lit-select-option>`
			firstList!.forEach((a,b)=>{
				html+=`<lit-select-option value="${b}">${a}</lit-select-option>`
			})
			html+=`</lit-select>`
		}
		if (secondList) {
			html+=`<lit-select default-value="" id="second-select" class="spacing" placeholder="please choose">
    		<lit-select-option value="Allocation Type" disabled>Allocation Type</lit-select-option>`
			secondList!.forEach((a,b)=>{
				html+=`<lit-select-option value="${b}">${a}</lit-select-option>`
			})
			html+=`</lit-select>`
		}
		if (!firstList) {
			this.secondSelectEL!.outerHTML = html;
		} else if (!secondList) {
			this.firstSelectEL!.outerHTML = html;
		}else {
			sLE!.innerHTML=html;
		}

		this.firstSelectEL = this.shadowRoot?.querySelector("#first-select")
		this.secondSelectEL = this.shadowRoot?.querySelector("#second-select")

		this.firstSelectEL!.onchange = (e)=>{
			if (this.getFilter) {
				this.getFilter({
					inputValue:this.filterInputEL!.value,
					firstSelect:this.firstSelectEL?.value,
					secondSelect:this.secondSelectEL?.value,
					mark:false,
					icon:this.icon
				})
			}
		}
		this.secondSelectEL!.onchange = (e)=>{
			if (this.getFilter) {
				this.getFilter({
					inputValue:this.filterInputEL!.value,
					firstSelect:this.firstSelectEL?.value,
					secondSelect:this.secondSelectEL?.value,
					mark:false,
					icon:this.icon
				})
			}
		}

	}

	initHtml(): string {
		return `
<style>
:host{
    height: 30px;
    /*position: sticky;*/
    /*width: calc(100% - 10px);*/
    background: var(--dark-background4,#F2F2F2);
    /*bottom: 10px;*/
    /*bottom: 200px;*/
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

:host(:not([inputLeftText])) .describe{
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

</style>
<lit-icon name="statistics" class="spacing" id="icon" size="16"></lit-icon>
<span class="describe">Input Filter</span>
<input id="filter-input" class="spacing" placeholder="${this.inputPlaceholder}"/>
<button id="mark" class="spacing">Mark Snapshot</button>
<div id="load" style="display: flex">

</div>
<!--<lit-select default-value="Allocation Lifespan" id="first-select">-->
    <!--<lit-select-option value="Allocation Lifespan" disabled>Allocation Lifespan</lit-select-option>-->
    <!--<lit-select-option value="Created & Persistent">Created & Persistent</lit-select-option>-->
    <!--<lit-select-option value="Created…ersistent">Created…ersistent</lit-select-option>-->
    <!--<lit-select-option value="Created…estroyed">Created…estroyed</lit-select-option>-->
<!--</lit-select>-->
<!--<lit-select default-value="Allocation Type" id="second-select">-->
    <!--<lit-select-option value="Allocation Type" disabled>Allocation Type</lit-select-option>-->
    <!--<lit-select-option value="All Heap & Anoymous VM">All Heap & Anoymous VM</lit-select-option>-->
    <!--<lit-select-option value="All Heap Allocations">All Heap Allocations</lit-select-option>-->
    <!--<lit-select-option value="All VM Regions">All VM Regions</lit-select-option>-->
<!--</lit-select>-->
       <span class="describe tree spacing">Call Tree</span>
       <span class="describe tree spacing">Call Tree Constraints</span>
       <span class="describe tree spacing">Data Mining</span>
        `;
	}
}

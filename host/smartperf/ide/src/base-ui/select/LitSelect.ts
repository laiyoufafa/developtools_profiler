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

import {BaseElement, element} from "../BaseElement.js";

@element('lit-select')
export class LitSelect extends BaseElement {
	private focused:any;
	private inputElement:any;
	private clearElement:any;
	private iconElement:any;
	private searchElement:any;
	private multipleRootElement:any;
	static get observedAttributes() {
		return [
			'value',//默认值
			'default-value',//默认值
			'placeholder',//placeholder
			'disabled',
			'loading',//是否处于加载状态
			'allow-clear',//是否允许清除
			'show-search',//是否允许搜索
			'list-height',//设置弹窗滚动高度 默认256px
			'border',//是否显示边框
			'mode',// mode='multiple'多选
		];
	}
	initElements(): void {

	}

	get value() {
		return this.getAttribute('value') || this.defaultValue;
	}

	set value(value) {
		this.setAttribute('value', value);
	}

	get border() {
		return this.getAttribute('border') || 'true';
	}

	set border(value) {
		if (value) {
			this.setAttribute('border', 'true');
		} else {
			this.setAttribute('border', 'false');
		}
	}

	get listHeight() {
		return this.getAttribute('list-height') || '256px';
	}

	set listHeight(value) {
		this.setAttribute('list-height', value);
	}

	get defaultPlaceholder() {
		return this.getAttribute('placeholder') || '请选择';
	}

	get showSearch() {
		return this.hasAttribute('show-search');
	}

	set defaultValue(value) {
		this.setAttribute('default-value', value);
	}

	get defaultValue() {
		return this.getAttribute('default-value') || '';
	}

	set placeholder(value) {
		this.setAttribute('placeholder', value);
	}

	get placeholder() {
		return this.getAttribute('placeholder') || this.defaultPlaceholder;
	}

	get loading() {
		return this.hasAttribute('loading');
	}

	set loading(value) {
		if (value) {
			this.setAttribute('loading', '');
		} else {
			this.removeAttribute('loading')
		}
	}

	initHtml() {
		// super();
		// const shadowRoot = this.attachShadow({mode: 'open'});
		return`
        <style>
:host{ 
    display: inline-flex;
    position: relative;
    overflow: visible;
    cursor: pointer;
    transition: all .3s;
    border-radius: 2px;
    outline: none;
    -webkit-user-select:none ;
    -moz-user-select:none;
    user-select:none;
    /*width: 100%;*/
}
:host(:not([border])),
:host([border='true']){
    border: 1px solid var(--bark-prompt,#dcdcdc);
}
input{
    border: 0;
    outline: none;
    background-color: transparent;
    cursor: pointer;
    transition: all .3s;
    -webkit-user-select:none ;
    -moz-user-select:none;
    user-select:none;
    display: inline-flex;
    color: var(--dark-color2,rgba(0,0,0,0.9));
}
:host(:not([mode]))  input{
    width: 100%;
}
:host([mode])  input{
    padding: 6px 0px;
}
:host([mode])  .root{
    padding: 1px 8px;
}
.root{
    position: relative;
    padding: 3px 6px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    transition: all .3s;
    border-radius: 2px;
    background-color: var(--dark-background4,#fff);
    outline: none;
    font-size: 1rem;
    z-index: 2;
    -webkit-user-select:none ;
    -moz-user-select:none;
    user-select:none;
    width: 100%;
}
.body{
    max-height: ${this.listHeight};
    position: absolute;
    bottom: 100%;
    z-index: 99;
    padding-top: 5px;
    margin-top: 2px;
    background-color: var(--dark-background4,#fff);    
    width: 100%;
    transition: all 0.2s;
    transform: scaleY(.6);
    visibility: hidden;
    opacity: 0;
    transform-origin: bottom center;
    display: block;
    flex-direction: column;
    box-shadow: 0 5px 15px 0px #00000033;
    border-radius: 2px;
    overflow: auto;
}
.icon{
    pointer-events: none;
}
.noSelect{
  -moz-user-select:none;
  -ms-user-select:none;
  user-select:none;
  -khtml-user-select:none;
  -webkit-touch-callout:none;
  -webkit-user-select:none;
}

:host(:not([border]):not([disabled]):focus),
:host([border='true']:not([disabled]):focus),
:host(:not([border]):not([disabled]):hover),
:host([border='true']:not([disabled]):hover){
    border:1px solid var(--bark-prompt,#ccc)
}
:host(:not([disabled]):focus) .body,
:host(:not([disabled]):focus-within) .body{
    transform: scaleY(1);
    opacity: 1;
    z-index: 99;
    visibility: visible;
}
:host(:not([disabled]):focus)  input{
    color: var(--dark-color,#bebebe);
}
.multipleRoot input::-webkit-input-placeholder {
        color: var(--dark-color,#aab2bd);
    }
:host(:not([border])[disabled]) *,
:host([border='true'][disabled]) *{
    background-color: #f5f5f5;
    color: #b7b7b7;
    cursor: not-allowed;
}
:host([border='false'][disabled]) *{
    color: #b7b7b7;
    cursor: not-allowed;
}
:host([loading]) .loading{
    display: flex;
}
:host([loading]) .icon{
    display: none;
}
:host(:not([loading])) .loading{
    display: none;
}
:host(:not([loading])) .icon{
    display: flex;
}
:host(:not([allow-clear])) .clear{
    display: none;
}
.clear{
    display: none;
    color: #bfbfbf;
}
.clear:hover{
    color: #8c8c8c;
}
.search{
    display: none;
    color: #bfbfbf;
}
.multipleRoot{
    display: flex;
    flex-direction: column;
    flex-wrap: wrap;
    flex-flow: wrap;
    align-items: center;
}
.tag{
    display: inline-flex;
    align-items: center;
    background-color: #f5f5f5;
    padding: 1px 4px;
    height: auto;
    font-size: .75rem;
    font-weight: bold;
    color: #242424;
    overflow: auto;
    position: relative;
    margin-right: 4px;
    margin-top: 1px;
    margin-bottom: 1px;
}
.tag-close{
    font-size: .8rem;
    padding: 2px;
    margin-left: 0px;
    color: #999999;
}
.tag-close:hover{
    color: #333;
}

</style>
<div class="root noSelect" tabindex="0" hidefocus="true">
    <div class="multipleRoot"><input placeholder="${this.placeholder}" autocomplete="off" ${this.showSearch ? '' : 'readonly'} tabindex="0"></div><!--多选-->
    <lit-loading class="loading" size="12"></lit-loading>
    <lit-icon class="icon" name='down' color="#c3c3c3"></lit-icon>
    <lit-icon class="clear" name='close-circle-fill'></lit-icon>
    <lit-icon class="search" name='search'></lit-icon>
</div>
<div class="body">
    <slot></slot>
    <slot name="footer"></slot>
</div>
        `
	}

	isMultiple() {
		return this.hasAttribute('mode') && this.getAttribute('mode') === 'multiple'
	}

	newTag(value:any, text:any) {
		let tag:any = document.createElement('div');
		let icon:any = document.createElement('lit-icon');
		icon.classList.add('tag-close')
		icon.name = 'close'
		let span = document.createElement('span');
		tag.classList.add('tag');
		span.dataset['value'] = value;
		span.textContent = text;
		tag.append(span);
		tag.append(icon);
		icon.onclick = (ev:any) => {
			tag.parentElement.removeChild(tag);
			this.querySelector(`lit-select-option[value=${value}]`)!.removeAttribute('selected')
			if (this.shadowRoot!.querySelectorAll('.tag').length == 0) {
				this.inputElement.style.width = 'auto';
				this.inputElement.placeholder = this.defaultPlaceholder;
			}
			ev.stopPropagation();
		}
		tag.value = value;
		tag.dataset['value'] = value;
		tag.text = text;
		tag.dataset['text'] = text;
		return tag;
	}

	//当 custom element首次被插入文档DOM时，被调用。
	connectedCallback() {
		this.tabIndex = 0;//设置当前组件为可以获取焦点
		this.focused = false;
		this.inputElement = this.shadowRoot!.querySelector('input');
		this.clearElement = this.shadowRoot!.querySelector('.clear');
		this.iconElement = this.shadowRoot!.querySelector('.icon');
		this.searchElement = this.shadowRoot!.querySelector('.search');
		this.multipleRootElement = this.shadowRoot!.querySelector('.multipleRoot');
		//点击清理 清空input值，展示placeholder，
		this.clearElement.onclick = (ev:any) => {
			if (this.isMultiple()) {
				let delNodes:Array<any> = []
				this.multipleRootElement.childNodes.forEach((a:any) => {
					if (a.tagName === 'DIV') {
						delNodes.push(a);
					}
				})
				for (let i = 0; i < delNodes.length; i++) {
					delNodes[i].remove();
				}
				if (this.shadowRoot!.querySelectorAll('.tag').length == 0) {
					this.inputElement.style.width = 'auto';
					this.inputElement.placeholder = this.defaultPlaceholder;
				}
			}
			this.querySelectorAll('lit-select-option').forEach(a => a.removeAttribute('selected'));
			this.inputElement.value = ''
			this.clearElement.style.display = 'none';
			this.iconElement.style.display = 'flex';
			this.blur();
			ev.stopPropagation();//这里不会因为点击清理而触发 选择栏目显示或者隐藏
			this.dispatchEvent(new CustomEvent('onClear', {detail: ev}))//向外派发清理事件
		}
		//初始化时遍历所有的option节点
		this.initOptions();
		//当前控件点击时 如果时select本身 需要显示 或 隐藏选择栏目，通过this.focused变量控制（默认为false）
		this.onclick = (ev:any) => {
			if (ev.target.tagName === 'LIT-SELECT') {
				if (this.focused === false) {
					this.inputElement.focus();
					this.focused = true;
				} else {
					this.blur();
					this.focused = false;
				}
			}
		}
		this.onmouseover = this.onfocus = ev => {
			if (this.hasAttribute('allow-clear')) {
				if (this.inputElement.value.length > 0 || this.inputElement.placeholder !== this.defaultPlaceholder) {
					this.clearElement.style.display = 'flex'
					this.iconElement.style.display = 'none';
				} else {
					this.clearElement.style.display = 'none'
					this.iconElement.style.display = 'flex';
				}
			}
		}
		this.onmouseout = this.onblur = ev => {
			if (this.hasAttribute('allow-clear')) {
				this.clearElement.style.display = 'none';
				this.iconElement.style.display = 'flex';
			}
			this.focused = false;
		}
		//输入框获取焦点时，value值 暂存于 placeholder  然后value值清空，这样值会以placeholder形式灰色展示，鼠标位于第一个字符
		this.inputElement.onfocus = (ev:any) => {
			if (this.hasAttribute('disabled')) return;//如果控件处于disabled状态 直接忽略
			if (this.inputElement.value.length > 0) {
				this.inputElement.placeholder = this.inputElement.value;
				this.inputElement.value = ''
			}
			if (this.hasAttribute('show-search')) {//如果有show-search属性 需要显示放大镜，隐藏向下的箭头
				this.searchElement.style.display = 'flex';
				this.iconElement.style.display = 'none';
			}
			this.querySelectorAll('lit-select-option').forEach(a => {//input获取焦点时显示所有可选项，相当于清理了搜索结果
				// @ts-ignore
				a.style.display = 'flex';
			})
		}
		//当输入框失去焦点的时候 placeholder 的值 保存到value上，input显示值
		this.inputElement.onblur = (ev:any) => {
			if (this.hasAttribute('disabled')) return;//如果控件处于disabled状态 直接忽略
			if (this.isMultiple()) {
				if (this.hasAttribute('show-search')) {//如果有show-search属性 失去焦点需要 隐藏放大镜图标，显示默认的向下箭头图标
					this.searchElement.style.display = 'none';
					this.iconElement.style.display = 'flex';
				}
			} else {
				if (this.inputElement.placeholder !== this.defaultPlaceholder) {//如果placeholder为 请输入（默认值）不做处理
					this.inputElement.value = this.inputElement.placeholder; //placeholder 保存的值放入 value中
					this.inputElement.placeholder = this.defaultPlaceholder;//placeholder 值为 默认值（请输入）
				}
				if (this.hasAttribute('show-search')) {//如果有show-search属性 失去焦点需要 隐藏放大镜图标，显示默认的向下箭头图标
					this.searchElement.style.display = 'none';
					this.iconElement.style.display = 'flex';
				}
			}
		}
		//输入框每次文本变化 会匹配搜索的option 显示或者隐藏，达到搜索的效果
		this.inputElement.oninput = (ev:any) => {
			let els = [...this.querySelectorAll('lit-select-option')];
			if (!ev.target.value) {
				els.forEach((a:any) => a.style.display = 'flex');
			} else {
				els.forEach((a:any) => {
					let value = a.getAttribute('value');
					if (value.toLowerCase().indexOf(ev.target.value.toLowerCase()) !== -1 ||
						a.textContent.toLowerCase().indexOf(ev.target.value.toLowerCase()) !== -1) {
						a.style.display = 'flex';
					} else {
						a.style.display = 'none';
					}
				})
			}
		}
		//输入框按下回车键，自动输入当前搜索出来的第一行，及display!='none'的第一个，搜索会隐藏其他行
		this.inputElement.onkeydown = (ev:any) => {
			if (ev.key === 'Backspace') {
				if (this.isMultiple()) {
					let tag = this.multipleRootElement.lastElementChild.previousElementSibling;
					if (tag) {
						this.querySelector(`lit-select-option[value=${tag.value}]`)?.removeAttribute('selected');
						tag.remove()
						if (this.shadowRoot!.querySelectorAll('.tag').length == 0) {
							this.inputElement.style.width = 'auto';
							this.inputElement.placeholder = this.defaultPlaceholder;
						}
					}
				} else {
					this.clear();
					this.dispatchEvent(new CustomEvent('onClear', {detail: ev}))//向外派发清理事件
				}
			} else if (ev.key === 'Enter') {
				let filter = [...this.querySelectorAll('lit-select-option')].filter((a:any) => a.style.display !== 'none');
				if (filter.length > 0) {
					this.inputElement.value = filter[0].textContent;
					this.inputElement.placeholder = filter[0].textContent;
					this.blur();
					// @ts-ignore
					this.value=filter[0].getAttribute('value')
					this.dispatchEvent(new CustomEvent('change', {
						detail: {
							selected: true,
							value: filter[0].getAttribute('value'),
							text: filter[0].textContent
						}
					}));//向外层派发change事件，返回当前选中项
				}
			}
		}
	}

	initOptions(){
		this.querySelectorAll('lit-select-option').forEach(a => {
			//如果节点的值为 当前控件的默认值 defalut-value则 显示该值对应的option文本
			if (this.isMultiple()) {
				a.setAttribute('check', '');
				if (a.getAttribute('value') === this.defaultValue) {
					let tag = this.newTag(a.getAttribute('value'), a.textContent);
					this.multipleRootElement.insertBefore(tag, this.inputElement);
					this.inputElement.placeholder = '';
					this.inputElement.value = '';
					this.inputElement.style.width = '1px';
					a.setAttribute('selected', '');
				}
				// this.inputElement.focus();
			} else {
				if (a.getAttribute('value') === this.defaultValue) {
					this.inputElement.value = a.textContent;
					a.setAttribute('selected', '');
				}
			}
			//每个option设置onSelected事件 接受当前点击的option
			a.addEventListener('onSelected', (e:any) => {
				//所有option设置为未选中状态
				if (this.isMultiple()) {//多选
					if (a.hasAttribute('selected')) {
						let tag = this.shadowRoot!.querySelector(`div[data-value=${e.detail.value}]`);
						// @ts-ignore
						tag.parentElement!.removeChild(tag);
						e.detail.selected = false;
					} else {
						let tag = this.newTag(e.detail.value, e.detail.text);
						this.multipleRootElement.insertBefore(tag, this.inputElement);
						this.inputElement.placeholder = '';
						this.inputElement.value = '';
						this.inputElement.style.width = '1px';
					}
					if (this.shadowRoot!.querySelectorAll('.tag').length == 0) {
						this.inputElement.style.width = 'auto';
						this.inputElement.placeholder = this.defaultPlaceholder;
					}
					this.inputElement.focus();
				} else {//单选
					[...this.querySelectorAll('lit-select-option')].forEach(a => a.removeAttribute('selected'))
					this.blur();//失去焦点，隐藏选择栏目列表
					// @ts-ignore
					this.inputElement.value = e.detail.text;
				}
				//设置当前option为选择状态
				if (a.hasAttribute('selected')) {
					a.removeAttribute('selected')
				} else {
					a.setAttribute('selected', '')
				}
				//设置input的值为当前选择的文本
				// @ts-ignore
				this.value = e.detail.value;
				this.dispatchEvent(new CustomEvent('change', {detail: e.detail}));//向外层派发change事件，返回当前选中项
			})
		})
	}
	//js调用清理选项
	clear() {
		this.inputElement.value = '';
		this.inputElement.placeholder = this.defaultPlaceholder;
	}

	//重置为默认值
	reset() {
		this.querySelectorAll('lit-select-option').forEach(a => {
			//如果节点的值为 当前控件的默认值 defalut-value则 显示该值对应的option文本
			[...this.querySelectorAll('lit-select-option')].forEach(a => a.removeAttribute('selected'))
			if (a.getAttribute('value') === this.defaultValue) {
				this.inputElement.value = a.textContent;
				a.setAttribute('selected', '');
			}
		})
	}

	//当 custom element从文档DOM中删除时，被调用。
	disconnectedCallback() {

	}

	//当 custom element被移动到新的文档时，被调用。
	adoptedCallback() {
	}

	//当 custom element增加、删除、修改自身属性时，被调用。
	attributeChangedCallback(name:any, oldValue:any, newValue:any) {
		if (name === 'value' && this.inputElement) {
			if(newValue){
				[...this.querySelectorAll('lit-select-option')].forEach(a => {
					if (a.getAttribute('value') === newValue) {
						a.setAttribute('selected', '');
						this.inputElement.value = a.textContent;
					} else {
						a.removeAttribute('selected')
					}
				})
			}else{
				this.clear();
			}
		}
	}
	set dataSource(value:any){
		value.forEach((a:any)=>{
			let option = document.createElement('lit-select-option');
			option.setAttribute('value',a.key);
			option.textContent = a.val;
			this.append(option)
		})
		this.initOptions();
	}

}

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

import {BaseElement, element} from "../../../base-ui/BaseElement.js";

@element('trace-command')
export class SpTraceCommand extends BaseElement {
    private codeHl: HTMLTextAreaElement | undefined | null;
    private copyEl: HTMLElement | undefined | null;
    private codeCopyText: HTMLInputElement | undefined;

    get hdcCommon(): string {
        return this.codeHl!.textContent + "";
    }

    set hdcCommon(value: string) {
        this.codeHl!.textContent = value;
    }

    public connectedCallback() {
        this.codeHl = this.shadowRoot?.querySelector('#code-text') as HTMLTextAreaElement;
        this.copyEl = this.shadowRoot?.querySelector('#copy-image') as HTMLElement;
        this.codeHl.textContent = ""
        this.copyEl?.addEventListener('click', this.codeCopyEvent)
        this.codeHl.addEventListener('selectionchange', this.textSelectEvent)
    }

    public disconnectedCallback() {
        this.copyEl?.removeEventListener('click', this.codeCopyEvent)
    }

    codeCopyEvent = (event: any) => {
        this.codeHl?.select();
        document.execCommand('copy');
    }

    textSelectEvent = (event: any) => {
        this.copyEl!.style.backgroundColor = '#FFFFFF';
    }

    initElements(): void {
    }

    initHtml(): string {
        return `
<style>
:host{
    width: 100%;
    position: relative;
    background: var(--dark-background3,#FFFFFF);
    border-radius: 0px 16px 16px 0px;
}

#code-text{
    -webkit-appearance:none;
    opacity: 0.6;
    font-family: Helvetica;
    color: var(--dark-color,#000000);
    padding: 56px;
    font-size:1em;
    margin-left: 10px;
    line-height: 20px;
    font-weight: 400;
    border: none;
    outline:none; 
    resize:none; 
    z-index: 2;
    min-height: 560px;
    background: var(--dark-background3,#FFFFFF);
}

#copy-image{
    display: table-cell;
    white-space: nowrap;
    outline:0;
    float:right;
    z-index: 66;
    position: relative;
    top: 56px;
    right: 40px; 
}

#copy-button{
    -webkit-appearance:none;
    outline:0;    
    border: 0;
    background: var(--dark-background3,#FFFFFF);
    justify-content: end;
    z-index: 55;
    border-radius: 0px 16px 0px 0px;
}

#text-cmd{
    display: grid;
    justify-content: stretch;
    align-content:  stretch;
    font-size:16px;
    background: var(--dark-background3,#FFFFFF);
    border-radius: 0px 16px 0px 0px;

}

::-webkit-scrollbar
{
  width: 6px;
  height: 10px;
  background-color: var(--dark-background3,#FFFFFF);
}
 
::-webkit-scrollbar-track
{
  background-color: var(--dark-background3,#FFFFFF);
}
 
::-webkit-scrollbar-thumb
{
  border-radius: 6px;
  background-color: var(--dark-background7,#e7c9c9);
}
</style>
<div id="text-cmd">
    <button id="copy-button">
        <img id="copy-image" src="img/copy.png">
    </button>
    <textarea id="code-text" readonly></textarea>
</div>`;
    }
}

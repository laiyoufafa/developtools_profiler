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
import {Flag} from "./Flag.js";
import {ns2s} from "../TimerShaftElement.js";

@element('tabpane-flag')
export class TabPaneFlag extends BaseElement {
    private flagListIdx: number | null = null;

    initElements(): void {
        this.shadowRoot?.querySelector("#color-input")?.addEventListener("change", (event: any) => {
            if (this.flagListIdx != null) {
                document.dispatchEvent(new CustomEvent('flag-change', {
                    detail: {
                        type: "amend",
                        flagObj: {color: event?.target.value}
                    }
                }));
            }
        });
        this.shadowRoot?.querySelector("#text-input")?.addEventListener("keydown", (event: any) => {
            if (event.keyCode == "13") {
                if (this.flagListIdx != null) {
                    document.dispatchEvent(new CustomEvent('flag-change', {
                        detail: {
                            type: "amend",
                            flagObj: {text: event?.target.value}
                        }
                    }));
                }
            }
        });
        this.shadowRoot?.querySelector("#remove-flag")?.addEventListener("click", (event: any) => {
            if (this.flagListIdx != null) {
                document.dispatchEvent(new CustomEvent('flag-change', {detail: {type: "remove"}}));
                document.dispatchEvent(new CustomEvent('flag-draw'));
            }
        });
    }

    setFlagObj(flagObj: Flag, idx: number) {
        this.flagListIdx = idx;
        this.shadowRoot?.querySelector("#color-input")?.setAttribute("value", flagObj.color);
        (this.shadowRoot?.querySelector("#text-input") as HTMLInputElement).value = flagObj.text;
        (this.shadowRoot?.querySelector("#flag-time") as HTMLDivElement)!.innerHTML = ns2s(flagObj.time)
    }

    initHtml(): string {
        return `
<style>
:host{
    display: flex;
    flex-direction: column;
    padding: 10px 10px;
}
.notes-editor-panel{
display: flex;align-items: center
}
.flag-text{
font-size: 14px;color: #363636c7;font-weight: 300;
}
.flag-input{
    border-radius: 4px;
    border: 1px solid #dcdcdc;
    padding: 3px;
    margin: 0 10px;
}
.flag-input:focus{
    outline: none;
    box-shadow: 1px 1px 1px #bebebe;
}
.notes-editor-panel button {
    background: #262f3c;
    color: white;
    border-radius: 10px;
    font-size: 10px;
    height: 22px;
    line-height: 18px;
    min-width: 7em;
    margin: auto 0 auto 1rem;
    
    border: none;
    cursor: pointer;
    outline: inherit;
</style>
<div class="notes-editor-panel">
    <div class="flag-text">Annotation at <span id="flag-time"></span></div>
    <input style="flex: 1" class="flag-input" type="text" id="text-input"/>
    <span class="flag-text">Change color: <input type="color" id="color-input"/></span>
    <button id="remove-flag">Remove</button>
</div>
        `;
    }

}

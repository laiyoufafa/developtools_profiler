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

@element("sp-search")
export class SpSearch extends BaseElement {
    private search: HTMLInputElement | undefined | null;

    setPercent(name: string = "", value: number) {
        let searchHide = this.shadowRoot!.querySelector<HTMLElement>(".root")
        let searchIcon = this.shadowRoot!.querySelector<HTMLElement>("#search-icon")
        if (value > 0 && value <= 100) {
            searchHide!.style.display = "flex"
            searchHide!.style.backgroundColor = "var(--dark-background5,#e3e3e3)"
            searchIcon?.setAttribute('name', "cloud-sync");
            this.search!.setAttribute('placeholder', `${name}${value}%`);
            this.search!.setAttribute('readonly', "");
            this.search!.className = "readonly"
        } else if (value > 100) {
            searchHide!.style.display = "none" //flex
            searchHide!.style.backgroundColor = "var(--dark-background5,#fff)"
            searchIcon?.setAttribute('name', "search");
            this.search?.setAttribute('placeholder', `search`);
            this.search?.removeAttribute('readonly');
            this.search!.className = "write"
        } else if (value == -1) {
            searchHide!.style.display = "flex"
            searchHide!.style.backgroundColor = "var(--dark-background5,#e3e3e3)"
            searchIcon?.setAttribute('name', "cloud-sync");
            this.search!.setAttribute('placeholder', `${name}`);
            this.search!.setAttribute('readonly', "");
            this.search!.className = "readonly"
        } else {
            searchHide!.style.display = "none"
        }
    }


    initElements(): void {
        this.search = this.shadowRoot!.querySelector<HTMLInputElement>("input");
        this.search!.addEventListener("focus", (e) => {
            this.dispatchEvent(new CustomEvent("focus", {
                detail: {
                    value: this.search!.value
                }
            }));
        });
        this.search!.addEventListener("blur", (e) => {
            this.dispatchEvent(new CustomEvent("blur", {
                detail: {
                    value: this.search!.value
                }
            }));
        });
        this.search!.addEventListener("keypress", (e: KeyboardEvent) => {
            if (e.code == "Enter") {
                this.dispatchEvent(new CustomEvent("enter", {
                    detail: {
                        value: this.search!.value
                    }
                }));
            }
        });
    }

    initHtml(): string {
        return `
<style>
    :host{
    }
    .root{
    background-color: var(--dark-background5,#fff);    
    border-radius: 40px;
    padding: 3px 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid var(--dark-border,#c5c5c5);
    }
    .root input{
    outline: none;
    border: 0px;
    background-color: transparent;
    font-size: inherit;
    color: var(--dark-color,#666666);
    width: 30vw;
    height: auto;
    vertical-align:middle;
    line-height:inherit;
    height:inherit;
    padding: 6px 6px 6px 6px};
    max-height: inherit;
    box-sizing: border-box;

}
::placeholder {
  color: #b5b7ba;
  font-size: 1em;
}
.write::placeholder {
  color: #b5b7ba;
  font-size: 1em;
}
.readonly::placeholder {
  color: #4f7ab3;
  font-size: 1em;
}
</style>
<div class="root" style="display: none">
                <lit-icon id="search-icon" name="search" size="20" color="#aaaaaa"></lit-icon>
                <input class="readonly" placeholder="Search" readonly/>
            </div>
        `;
    }
}

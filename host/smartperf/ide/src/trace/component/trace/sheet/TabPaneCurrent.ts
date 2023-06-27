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
import { SlicesTime } from '../timer-shaft/SportRuler';

@element('tabpane-current')
export class TabPaneCurrent extends BaseElement {
  private slicestime: SlicesTime | null = null;
  initElements(): void {
    this.shadowRoot?.querySelector('#color-input')?.addEventListener('change', (event: any) => {
      if (this.slicestime) {
        this.slicestime.color = event?.target.value;
        document.dispatchEvent(new CustomEvent('slices-change', { detail: this.slicestime }));
      }
    });
    this.shadowRoot?.querySelector('#text-input')?.addEventListener('keyup', (event: any) => {
      event.stopPropagation();
      if (event.keyCode == '13') {
        if (this.slicestime) {
          window.publish(window.SmartEvent.UI.KeyboardEnable, {
            enable: true,
          });
          this.slicestime.text = event?.target.value;
          document.dispatchEvent(
            new CustomEvent('slices-change', {
              detail: this.slicestime,
            })
          );
        }
      }
    });
    this.shadowRoot?.querySelector('#text-input')?.addEventListener('blur', (event: any) => {
      (window as any).flagInputFocus = false;
      window.publish(window.SmartEvent.UI.KeyboardEnable, {
        enable: true,
      });
    });
    this.shadowRoot?.querySelector('#text-input')?.addEventListener('focus', (event: any) => {
      (window as any).flagInputFocus = true;
      window.publish(window.SmartEvent.UI.KeyboardEnable, {
        enable: false,
      });
    });
    this.shadowRoot?.querySelector('#remove')?.addEventListener('click', (event: any) => {
      if (this.slicestime) {
        this.slicestime.hidden = true;
        document.dispatchEvent(new CustomEvent('slices-change', { detail: this.slicestime }));
      }
    });
  }

  setCurrentSlicesTime(slicestime: SlicesTime) {
    this.slicestime = slicestime;
    this.shadowRoot!.querySelector<HTMLInputElement>('#color-input')!.value = this.slicestime.color;
    this.shadowRoot!.querySelector<HTMLInputElement>('#text-input')!.value = this.slicestime.text;
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
        .slices-text{
        font-size: 14px;color: var(--dark-color1,#363636c7);font-weight: 300;
        }
        .slices-input{
            border-radius: 4px;
            border: 1px solid var(--dark-border,#dcdcdc);
            color: var(--dark-color1,#212121);
            background: var(--dark-background5,#FFFFFF);
            padding: 3px;
            margin: 0 10px;
        }
        .slices-input:focus{
            outline: none;
            box-shadow: 1px 1px 1px var(--bark-prompt,#bebebe);
        }
        .notes-editor-panel button {
            background: var(--dark-border1,#262f3c);
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
            <div class="slices-text">Annotation at <span id="slices-time"></span></div>
            <input style="flex: 1" class="slices-input" type="text" id="text-input"/>
            <span class="slices-text">Change color: <input style="background: var(--dark-background5,#FFFFFF);" type="color" id="color-input"/></span>
            <button id="remove">Remove</button>
        </div>
        `;
  }
}

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
import "../../../base-ui/radiobox/LitRadioBox.js";
import {LitRadioBox} from "../../../base-ui/radiobox/LitRadioBox.js";
import "../../../base-ui/slider/LitSlider.js";
import {LitSlider} from "../../../base-ui/slider/LitSlider.js";

@element('record-setting')
export class SpRecordSetting extends BaseElement {
    private memoryBufferSlider: LitSlider | undefined;
    private maxDurationSliders: LitSlider | undefined;

    private radioBox: LitRadioBox | undefined

    get recordMod(): boolean {
        if (this.radioBox) {
            return this.radioBox.checked
        }
        return false;
    }

    get bufferSize(): number {
        let bufferSize = this.shadowRoot?.querySelector(".buffer-size") as HTMLElement
        return Number(bufferSize.getAttribute("percent"));
    }

    get maxDur(): number {
        let bufferSize = this.shadowRoot?.querySelector(".max-duration") as HTMLElement
        return Number(bufferSize.getAttribute("percent"));
    }

    initElements(): void {
        this.radioBox = this.shadowRoot?.querySelector("#litradio") as LitRadioBox
        this.memoryBufferSlider = this.shadowRoot?.querySelector<LitSlider>('#memory-buffer') as LitSlider;

        let sliderSize1 = this.memoryBufferSlider.sliderSize;

        this.maxDurationSliders = this.shadowRoot?.querySelector<LitSlider>('#max-duration') as LitSlider;
        let sliderSize2 = this.maxDurationSliders.sliderSize;
    }

    initHtml(): string {
        return `
<style>
:host{
    display: block;
    width: 100%;
    height: 100%;
    position: relative;
    background: background: var(--dark-background3,#FFFFFF);
    border-radius: 0px 16px 16px 0px;
}
.root {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    grid-template-rows: min-content min-content min-content;
    grid-gap: 50px;
    padding-top: 45px;
    padding-left: 41px;
    background: var(--dark-background3,#FFFFFF);
    font-size:16px;
    border-radius: 0px 16px 16px 0px;
    overflow-y: auto;
}
.record-mode{
    font-family: Helvetica-Bold;
    font-size: 16px;
    color: var(--dark-color1,#000000);
    line-height: 28px;
    font-weight: 700;
    margin-bottom: 16px;
}
.record{
    display:flex;
    flex-direction: column;
}

.buffer-size{
    height: min-content;
}

.max-duration{
    height: min-content;
}

#litradio{
    opacity: 0.9;
    font-family: Helvetica;
    font-size: 14px;
    color: var(--dark-color1,#000000);
    text-align: left;
    line-height: 16px;
    font-weight: 400;
}

</style>
<div class="root">
  <div class="record">
    <span class="record-mode">Record mode</span>
    <lit-radio name="Stop when full" dis="round" id="litradio" checked>Stop when full</lit-radio>
  </div>
  <div class="buffer-size">
    <span class="record-mode">In-memory buffer size</span> 
    <lit-slider id="memory-buffer" open dir="right" custom-slider></lit-slider>
  </div>
  <div class="max-duration">
    <span class="record-mode" >Max duration</span>  
    <lit-slider id="max-duration" open dir="right" ></lit-slider>
  </div>
</div>`;
    }
}

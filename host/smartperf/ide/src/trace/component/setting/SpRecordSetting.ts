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
import "../../../base-ui/popover/LitPopover.js"

@element('record-setting')
export class SpRecordSetting extends BaseElement {
    private memoryBufferSlider: LitSlider | undefined;
    private maxDurationSliders: LitSlider | undefined;
    private radioBox: LitRadioBox | undefined
    private bufferNumber: HTMLElement | undefined
    private durationNumber: HTMLElement | undefined

    get recordMod(): boolean {
        if (this.radioBox) {
            return this.radioBox.checked
        }
        return false;
    }

    get bufferSize(): number {
        if (this.bufferNumber?.hasAttribute('percent')) {
            return Number(this.bufferNumber!.getAttribute("percent"));
        }
        return 64
    }

    get maxDur(): number {
        if (this.durationNumber?.hasAttribute('percent')) {
            return Number(this.durationNumber!.getAttribute("percent"));
        }
        return 50
    }

    initElements(): void {
        this.bufferNumber = this.shadowRoot?.querySelector(".buffer-size") as HTMLElement
        this.durationNumber = this.shadowRoot?.querySelector(".max-duration") as HTMLElement
        let bu = this.shadowRoot?.querySelector('.record') as HTMLDivElement
        this.shadowRoot?.querySelectorAll<HTMLButtonElement>('.MenuButton').forEach(button => {

            button!.addEventListener('mouseenter', e => {
                button.style.backgroundColor = '#EFEFEF'
            })

            button!.addEventListener('mouseout', e => {
                button.style.backgroundColor = '#E4E3E9'
            })
        })

        this.radioBox = this.shadowRoot?.querySelector("#litradio") as LitRadioBox
        this.initLitSlider()
    }

    initLitSlider() {
        this.memoryBufferSlider = this.shadowRoot?.querySelector<LitSlider>('#memory-buffer') as LitSlider;
        this.memoryBufferSlider.sliderStyle = {
            minRange: 4,
            maxRange: 512,
            defaultValue: "64",
            resultUnit: "MB",
            stepSize: 2,
            lineColor: "var(--dark-color3,#46B1E3)",
            buttonColor: "#999999"
        };
        let bufferInput = this.shadowRoot?.querySelector('.memory_buffer_result') as HTMLInputElement;
        bufferInput.value = '          ' + this.memoryBufferSlider.sliderStyle.defaultValue + '          MB'
        this.memoryBufferSlider.addEventListener('input', evt => {
            bufferInput.value = '          ' + this.bufferSize + '          MB'
        })

        this.maxDurationSliders = this.shadowRoot?.querySelector<LitSlider>('#max-duration') as LitSlider;
        this.maxDurationSliders.sliderStyle = {
            minRange: 10,
            maxRange: 600,
            defaultValue: '00:00:50',
            resultUnit: "h:m:s",
            stepSize: 1,
            lineColor: "var(--dark-color4,#61CFBE)",
            buttonColor: "#999999"
        }

        let durationInput = this.shadowRoot?.querySelector('.max_duration_result') as HTMLInputElement;
        durationInput.value = '     ' + this.maxDurationSliders.sliderStyle.defaultValue + '     h:m:s'
        this.maxDurationSliders.addEventListener('input', evt => {
            durationInput.value = '     ' + this.maxDurationSliders!.formatSeconds(this.maxDur.toString()) + '     h:m:s'
        })
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
            grid-column: span 2 / auto;
        }
        .record{
            display:flex;
            flex-direction: column;
        }

        .buffer-size{
            height: min-content;
            display: grid;
            grid-template-rows: 1fr 1fr;
            grid-template-columns: 1fr min-content;
        }

        .max-duration{
            height: min-content;
            display: grid;
            grid-template-rows: 1fr 1fr;
            grid-template-columns: 1fr min-content;
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

        button{
            height: 25px;
            width: 100%;
            border: 0;
            text-align: left;
            padding-left: 20px;
            margin-top: 10px;
            background-color: #E4E3E9;
        }

        .line{
            border-top: 1px solid #C5C7CF;
            background: #E4E3E9;
            margin-top: 4px;
            display: inline-block;
            width: 100%;
            height: 1px;
            overflow: hidden;
            vertical-align: middle;
        }

        input{
            margin: 0 30px 0 0;
            height: 40px;
            background-color: var(--dark-background5,#F2F2F2);
            -webkit-appearance:none;
            outline:0;
            font-size:14px;
            border-radius:20px;
            border:1px solid var(--dark-border,#c8cccf);
            color:var(--dark-color,#6a6f77);
            text-align: left;
        }

        #memory-buffer, #max-duration {
            margin: 0 8px;
        }

        </style>
        <div class="root">
          <div class="record">
            <span class="record-mode">Record mode</span>
            <lit-radio name="Stop when full" dis="round" id="litradio" checked>Stop when full</lit-radio>
          </div>
          <div class="buffer-size">
            <span class="record-mode">In-memory buffer size</span>
            <lit-slider id="memory-buffer" defaultColor="var(--dark-color3,#46B1E3)" open dir="right">
            </lit-slider>
            <input class="memory_buffer_result" type="text" value='          64'>
          </div>
          <div class="max-duration">
            <span class="record-mode" >Max duration</span>
            <lit-slider id="max-duration" defaultColor="var(--dark-color4,#61CFBE)" open dir="right">
            </lit-slider>
            <input class="max_duration_result" type="text" value = '     00:00:50'>
          </div>
        </div>
        `;
    }
}
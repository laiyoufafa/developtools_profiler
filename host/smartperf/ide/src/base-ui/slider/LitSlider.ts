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

@element('lit-slider')
export class LitSlider extends BaseElement {
    private litSliderStyle: LitSliderStyle | undefined | null;
    private litSlider: HTMLInputElement | undefined | null;
    private litSliderCon: HTMLDivElement | undefined | null;
    private litResult: HTMLInputElement | undefined | null;
    private litSliderButton: HTMLDivElement | undefined | null;
    private slotEl: HTMLSlotElement | undefined | null;
    private currentValue: number = 0;
    private sliderLineHeight: string | undefined;
    private sliderButtonHeight: string | undefined;
    private sliderButtonWidth: string | undefined;
    private defaultTimeText: string | undefined | null;

    static get observedAttributes() {
        return ['percent', 'disabled-X', 'custom-slider', 'custom-line', 'custom-button']
    }

    get sliderStyle() {
        if (this.hasAttribute('custom-slider')) {
            this.defaultTimeText = "50";
            return {
                minRange: 0,
                maxRange: 1024,
                defaultValue: this.defaultTimeText,
                resultUnit: "MB",
                stepSize: 2,
                lineColor: "var(--dark-color3,#46B1E3)",
                buttonColor: "#999999"
            }
        } else {
            let defaultTime = "00:00:50";
            this.defaultTimeText = defaultTime.split(':')[2];
            return {
                minRange: 0,
                maxRange: 480,
                defaultValue: defaultTime,
                resultUnit: "h:m:s",
                stepSize: 1,
                lineColor: "var(--dark-color4,#61CFBE)",
                buttonColor: "#999999"
            }
        }

    }

    set sliderStyle(value) {
        this.litSliderStyle = value;
        this.litSliderStyle = this.sliderStyle;
        this.litSliderStyle.defaultValue = value.defaultValue
        if (this.hasAttribute('custom-slider')) {
            this.renderCustomSlider();
        } else {
            this.renderDefaultSlider();
        }
    }

    get disabledX() {
        return this.getAttribute('disabled-X') || '';
    }

    set disabledX(value: string) {
        if (value) {
            this.setAttribute('disabled-X', '');
        } else {
            this.removeAttribute('disabled-X');
        }
    }

    get customSlider() {
        return this.getAttribute('custom-slider') || '';
    }

    set customSlider(value: string) {
        if (value) {
            this.setAttribute('custom-slider', '');
        } else {
            this.removeAttribute('custom-slider');
        }
    }

    get customLine() {
        return this.getAttribute('custom-line') || '';
    }

    set customLine(value: string) {
        this.setAttribute('custom-line', value);
    }

    get customButton() {
        return this.getAttribute('custom-button') || '';
    }

    set customButton(value: string) {
        this.setAttribute('custom-button', value);
    }

    get percent() {
        return this.getAttribute('percent') || '';
    }

    set percent(value: string) {
        this.setAttribute('percent', value);
    }

    get resultUnit() {
        return this.getAttribute('resultUnit') || '';
    }

    set resultUnit(value: string) {
        this.setAttribute('resultUnit', value);
    }

    get sliderSize() {
        return this.currentValue;
    }

    initElements(): void {
    }

    initHtml(): string {
        this.litSliderStyle = this.sliderStyle;
        this.currentValue = Number(this.sliderStyle.defaultValue);
        let parentElement = this.parentNode as Element;
        if (parentElement) {
            parentElement.setAttribute('percent', this.defaultTimeText + "");
        }
        return `
        <style>
        :host{ 
            box-sizing:border-box; 
            display:flex;
            
        }
        :host([disabled]){ 
            opacity:0.8; 
            cursor:not-allowed; 
        }
        :host([disabled]) input[type="range"]{ 
            pointer-events:none;
        }
        #slider-con{ 
            cursor:pointer;
            display:flex;
            align-items:center;
            padding:5px 0; 
            width:80%;
            margin: 20px;
            grid-auto-flow: row dense;
            position: relative;
        }
        :host([showtips]){
            pointer-events:all;
        }
        
        #slider{
            background-color: var(--dark-background7,#D8D8D8);
            z-index: 5;
        }

        input[type="range"]{
            pointer-events:all;
            margin:0 -5px;
            width: 100%;
            -webkit-appearance: none;
            outline : 0;
            background: rgba(0,0,0,0.1);
            height: 10px;
            border-radius:2px;   
            background: -webkit-linear-gradient(right, ${this.litSliderStyle?.lineColor},${this.litSliderStyle?.lineColor} ) no-repeat;
            background-size: ${((Number(this.defaultTimeText) - this.litSliderStyle?.minRange) * 100 / (this.litSliderStyle?.maxRange - this.litSliderStyle?.minRange))}%;
        }

        input[type="range"]::-webkit-slider-runnable-track{
            display: flex;
            align-items: center;
            position: relative;
            height: ${this.sliderLineHeight ? this.sliderLineHeight : "10px"};
            border-radius:5px;
        }

        input[type="range"]::-webkit-slider-thumb{
            -webkit-appearance: none;
            /*border:2px solid #42b983;*/
            position: relative;
            width:${this.sliderButtonHeight ? this.sliderButtonHeight : "20px"};
            height:${this.sliderButtonWidth ? this.sliderButtonWidth : "20px"};
            margin-top: -4px;
            border-radius: 5px;
            background:${this.litSliderStyle?.buttonColor};
            transition:0.2s cubic-bezier(.12, .4, .29, 1.46);
        }
        
        input[type="range"]:focus{
            z-index:2;
        }

        input[type="range"]::-webkit-slider-thumb:active,
        input[type="range"]:focus::-webkit-slider-thumb{
            transform:scale(1.2);
            border: 2px solid;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            background: #fff;
        }

        :host(:focus-within) #slider-con,:host(:hover) #slider-con{
            z-index:10
        }
        
        #result{
            margin: 10px;
            width: 196px;
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
        
        #unitSpan{
            position: absolute;
            /*width: min-content;*/
            width: 50px;
            /*top: -50px;*/
            right: 0px;
            color: #adadad;
            display: table-cell;
            white-space: nowrap;
            padding: 7px 10px;
            font-size:14px;
        }
        
        </style>
        <slot id="slot"></slot>
        <div id='slider-con' dir="right">
            <input id="slider"
                value=${this.defaultTimeText}
                min=${this.litSliderStyle?.minRange}
                max=${this.litSliderStyle?.maxRange}
                step=${this.litSliderStyle?.stepSize} 
                type="range">
            <input id="result" type="text" value='     ${this.litSliderStyle?.defaultValue}'><span id="unitSpan" >${this.litSliderStyle?.resultUnit}</span>
        </div>
        `
    }

    connectedCallback() {
        this.slotEl = this.shadowRoot?.querySelector('#slot');
        this.litSlider = this.shadowRoot?.querySelector('#slider');
        this.litSliderCon = this.shadowRoot?.querySelector('#slider-con');
        this.litResult = this.shadowRoot?.querySelector('#result');
        this.litSlider?.addEventListener('input', this.inputChangeEvent)
        this.litSlider?.addEventListener('change', this.inputChangeEvent)
        this.slotEl?.addEventListener('slotchange', this.slotChangeEvent);
        this.litSlider?.addEventListener('click', this.sliderClickEvent);
        this.litSliderButton?.addEventListener('TouchEvent', this.sliderStartTouchEvent);
        this.litSliderStyle = this.sliderStyle;
    }

    slotChangeEvent = (event: any) => {
    }

    sliderClickEvent = (event: any) => {
    }

    inputChangeEvent = (event: any) => {
        if (this.litSlider) {
            this.currentValue = parseInt(this.litSlider?.value)
            let resultNumber = (this.currentValue - this.litSliderStyle!.minRange) * 100 / (this.litSliderStyle!.maxRange - this.litSliderStyle!.minRange);
            this.percent = Math.floor(resultNumber) + "%";
            this.litSliderCon?.style.setProperty('percent', this.currentValue + "%")
            let parentElement = this.parentNode as Element;
            parentElement.setAttribute('percent', this.currentValue + "");
            if (this.sliderStyle.resultUnit === 'MB') {
                this.litSlider!.style.backgroundSize = this.percent;
                this.litResult!.value = "     " + this.currentValue;
            } else if (this.sliderStyle.resultUnit === 'h:m:s') {
                this.litSlider!.style.backgroundSize = this.percent;
                let time = this.formatSeconds(this.litSlider?.value);
                this.litResult!.value = "     " + time;
            }
        }
    }

    sliderStartTouchEvent = (event: any) => {
    }

    sliderMoveTouchEvent = (event: any) => {
    }

    sliderEndTouchEvent = (event: any) => {
    }

    disconnectedCallback() {
        this.litSlider?.removeEventListener('input', this.inputChangeEvent);
        this.litSlider?.removeEventListener('change', this.inputChangeEvent)
        this.litSlider?.removeEventListener('click', this.sliderClickEvent);
        this.litSliderButton?.removeEventListener('TouchEvent', this.sliderStartTouchEvent);
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
        switch (name) {
            case "percent":
                if (newValue === null || newValue === "0%") {
                    let parentElement = this.parentNode as Element;
                    parentElement?.removeAttribute('percent');
                } else {
                    let parentElement = this.parentNode as Element;
                }
                break;
            default:
                break;
        }
    }

    renderCustomSlider() {
    }

    renderDefaultSlider() {
        if (!this.litSliderStyle) return;
    }

    formatSeconds(value: string) {
        let result = parseInt(value)
        let hours = Math.floor(result / 3600) < 10 ? '0' + Math.floor(result / 3600) : Math.floor(result / 3600);
        let minute = Math.floor((result / 60 % 60)) < 10 ? '0' + Math.floor((result / 60 % 60)) : Math.floor((result / 60 % 60));
        let second = Math.floor((result % 60)) < 10 ? '0' + Math.floor((result % 60)) : Math.floor((result % 60));
        let resultTime = '';
        if (hours === '00') {
            resultTime += `00:`;
        } else {
            resultTime += `${hours}:`;
        }
        if (minute === '00') {
            resultTime += `00:`;
        } else {
            resultTime += `${minute}:`;
        }
        resultTime += `${second}`;
        return resultTime;
    }
}

export interface LitSliderStyle {
    minRange: number
    maxRange: number
    defaultValue: string
    resultUnit: string
    stepSize?: number
    lineColor?: string
    buttonColor?: string
}

export interface LitSliderLineStyle {
    lineWith: number
    lineHeight: number
    border?: string
    borderRadiusValue?: number
    lineChangeColor?: string
}

export interface LitSliderButtonStyle {
    buttonWith: number
    buttonHeight: number
    border?: string
    borderRadiusValue?: number
    buttonChangeColor?: string
}

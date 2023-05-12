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

import { BaseElement, element } from '../../../base-ui/BaseElement.js';
import '../../../base-ui/select/LitAllocationSelect.js';

import '../../../base-ui/switch/lit-switch.js';
import { LitAllocationSelect } from '../../../base-ui/select/LitAllocationSelect.js';
import { SpRecordTrace } from '../SpRecordTrace.js';
import { Cmd } from '../../../command/Cmd.js';
import { CmdConstant } from '../../../command/CmdConstant.js';
import { HdcDeviceManager } from '../../../hdc/HdcDeviceManager.js';
import { LitRadioBox } from '../../../base-ui/radiobox/LitRadioBox.js';
import { SpCheckDesBox } from './SpCheckDesBox.js';

@element('sp-js-heap')
export class SpJsHeap extends BaseElement {
    private processInput: LitAllocationSelect | undefined | null;
    private spCheckDesBox: SpCheckDesBox | undefined | null;
    private radioBox: LitRadioBox | undefined | null;
    private interval: HTMLInputElement | undefined | null;

    get process(): string {
        if (this.processInput!.value.length > 0) {
            return this.processInput!.value;
        }
        return '';
    }

    get radioBoxType(): number {
        this.radioBox = this.shadowRoot?.querySelector(`lit-radio[checked]`);
        let type = this.radioBox?.getAttribute('type');
        return Number(type);
    }

    get grabNumeric(): boolean {
        if (this.radioBoxType === 0) {
            this.spCheckDesBox = this.shadowRoot?.querySelector('#snapshot');
            let isChecked = this.spCheckDesBox?.getAttribute('checked');
            return isChecked === 'true';
        } else {
            return false;
        }
    }

    get grabAllocations(): boolean {
        if (this.radioBoxType === 1) {
            this.spCheckDesBox = this.shadowRoot?.querySelector('#timeline');
            let isChecked = this.spCheckDesBox?.getAttribute('checked');
            return isChecked === 'true';
        } else {
            return false;
        }
    }

    get intervalValue(): number {
        if (this.radioBoxType === 0) {
            return Number(this.interval!.value);
        } else {
            return 0;
        }
    }

    initElements(): void {
        this.interval = this.shadowRoot?.querySelector('#interval');
        this.processInput = this.shadowRoot?.querySelector<LitAllocationSelect>('lit-allocation-select');
        let processInput = this.processInput?.shadowRoot?.querySelector('.multipleSelect') as HTMLDivElement;
        let processData: Array<string> = [];
        processInput!.addEventListener('mousedown', (ev) => {
            if (SpRecordTrace.serialNumber == '') {
                this.processInput!.processData = [];
                this.processInput!.initData();
            }
        });
        processInput!.addEventListener('mouseup', () => {
            if (SpRecordTrace.serialNumber == '') {
                this.processInput!.processData = [];
                this.processInput!.initData();
            } else {
                if (SpRecordTrace.isVscode) {
                    let cmd = Cmd.formatString(CmdConstant.CMD_GET_PROCESS_DEVICES, [SpRecordTrace.serialNumber]);
                    Cmd.execHdcCmd(cmd, (res: string) => {
                        processData = [];
                        let lineValues: string[] = res.replace(/\r\n/g, '\r').replace(/\n/g, '\r').split(/\r/);
                        for (let lineVal of lineValues) {
                            if (lineVal.indexOf('__progname') != -1 || lineVal.indexOf('PID CMD') != -1) {
                                continue;
                            }
                            let process: string[] = lineVal.trim().split(' ');
                            if (process.length == 2) {
                                let processId = process[0];
                                let processName = process[1];
                                processData.push(processName + '(' + processId + ')');
                            }
                        }
                        this.processInput!.processData = processData;
                        this.processInput!.initData();
                    });
                } else {
                    HdcDeviceManager.connect(SpRecordTrace.serialNumber).then((conn) => {
                        if (conn) {
                            HdcDeviceManager.shellResultAsString(CmdConstant.CMD_GET_PROCESS, false).then((res) => {
                                processData = [];
                                if (res) {
                                    let lineValues: string[] = res.replace(/\r\n/g, '\r').replace(/\n/g, '\r').split(/\r/);
                                    for (let lineVal of lineValues) {
                                        if (lineVal.indexOf('__progname') != -1 || lineVal.indexOf('PID CMD') != -1) {
                                            continue;
                                        }
                                        let process: string[] = lineVal.trim().split(' ');
                                        if (process.length == 2) {
                                            let processId = process[0];
                                            let processName = process[1];
                                            processData.push(processName + '(' + processId + ')');
                                        }
                                    }
                                }
                                this.processInput!.processData = processData;
                                this.processInput!.initData();
                            });
                        }
                    });
                }
            }
        });
        this.interval!.addEventListener('focusout', () => {
            if (this.interval!.value === '') {
                this.interval!.value = '10';
            }
        });
    }

    initHtml(): string {
        return `
        <style>
        :host{
            display: inline-block;
            width: 100%;
            height: 100%;
            background: var(--dark-background3,#FFFFFF);
            border-radius: 0px 16px 16px 0px;
        }
        .root {
            padding-top: 30px;
            padding-left: 54px;
            margin-right: 30px;
            font-size:16px;
            margin-bottom: 30px;
        }
        .config-div {
           width: 80%;
           display: flex;
           flex-direction: column;
           margin-top: 5vh;
           margin-bottom: 5vh;
           gap: 25px;
        }
        .title {
          opacity: 0.9;
          font-family: Helvetica-Bold;
          font-size: 18px;
          text-align: center;
          line-height: 40px;
          font-weight: 700;
          margin-right: 10px;
        }

        .des {
          color: #242424;
            font-family: Helvetica;
            font-size: 14px;
            text-align: left;
            line-height: 16px;
            font-weight: 400;
        }
        
        .select {
          border-radius: 15px;
        }
        input {
           width: 35%;
           height: 25px;
           border:0;
           outline:none;
           border-radius: 16px;
           text-indent:2%
        }
        input::-webkit-input-placeholder{
            color:var(--bark-prompt,#999999);
        }
       
        .inputstyle{
            background: var(--dark-background5,#FFFFFF);
            border: 1px solid var(--dark-background5,#999999);
            font-family: Helvetica;
            font-size: 14px;
            color: var(--dark-color1,#212121);
            text-align: left;
            line-height: 16px;
            font-weight: 400;
        }
        .inputstyle::-webkit-input-placeholder {
           background: var(--dark-background5,#FFFFFF);
        }
        
        .radio {
            font-family: Helvetica-Bold;
            font-size: 16px;
            color: #000000;
            line-height: 28px;
            font-weight: 700;
        }
        .unit {
            font-family: Helvetica;
            font-size: 14px;
            color: #000000;
            line-height: 28px;
            font-weight: 400;
        }
        </style>
        <div class="root">
            <div class="config-div">
                <div>
                    <span class="title">Process</span>
                    <span class="des">Record process</span>
                </div>
                <lit-allocation-select style="width: 100%;" rounded="" default-value="" class="select config" placement="bottom" ></lit-allocation-select>
            </div>
            <div class="config-div">
                <div>
                    <span class="title">Select profiling type</span>
                </div>
                <lit-radio dis="round" class="radio" name="litRadio" checked type="0">Heap snapshot</lit-radio>
                <div style="margin-left: 10px;">
                     <span class="des">Heap snapshot profiles show memory distribution among your page’s JavaScript objects and related DOM nodes.</span>
                    <div style="display: flex;margin-bottom: 12px;margin-top: 12px;"> 
                         <check-des-box checked="true" value ="lnclude numerical values in capture" id="snapshot">
                         </check-des-box>
                    </div>
                    <span class="des">Interval(Available on recent OpenHarmony 4.0)</span>
                    <div style="margin-top: 12px;">
                        <input class="inputstyle" type="text" id="interval" placeholder="" onkeyup="this.value=this.value.replace(/\\D/g,'').replace(/^0{1,}/g,'')" value="10">
                        <span class="unit">S</span>
                    </div>
                </div> 
                
                <lit-radio dis="round" name="litRadio" class="radio" type="1">Allocation insteumentation on timeline</lit-radio>
                <div style="margin-left: 10px;">
                    <span class="des">Allocation timelines show insturmented Javascript memory allocations over time. Once profile is recorded you can select a time interval to see objects that werre allocated within it and still alive by the end of recording. Use this profile type to isolate memory leaks.</span>
                    <div style="display: flex;margin-top: 12px;"> 
                    <check-des-box value ="record stack traces of allocations(extra performance overhead)" id="timeline">
                    </check-des-box>
                    </div>
                </div>
            </div>
        </div>
        `;
    }
}

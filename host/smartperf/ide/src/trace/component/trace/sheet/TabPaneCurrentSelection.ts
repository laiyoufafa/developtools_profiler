// @ts-nocheck
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
import {CpuStruct} from "../../../bean/CpuStruct.js";
import {LitTable} from "../../../../base-ui/table/lit-table.js";
import "../../../../base-ui/table/lit-table-column.js";

import {
    queryBinderArgsByArgset,
    queryWakeUpThread_WakeThread,
    queryWakeUpThread_WakeTime
} from "../../../database/SqlLite.js";
import {WakeupBean} from "../../../bean/WakeupBean.js";
import {ThreadStruct} from "../../../bean/ThreadStruct.js";
import {ProcessMemStruct} from "../../../bean/ProcessMemStruct.js";
import {FuncStruct} from "../../../bean/FuncStruct.js";
import {SpApplication} from "../../../SpApplication.js";

const STATUS_MAP: any = {
    D: "Uninterruptible Sleep",
    S: "Sleeping",
    R: "Runnable",
    "Running": "Running",
    "R+": "Runnable (Preempted)",
    DK: "Uninterruptible Sleep + Wake Kill",
    I: "Task Dead",
    T: "Stopped",
    t: "Traced",
    X: "Exit (Dead)",
    Z: "Exit (Zombie)",
    K: "Wake Kill",
    W: "Waking",
    P: "Parked",
    N: "No Load"
}
const INPUT_WORD = "This is the interval from when the task became eligible to run \n(e.g.because of notifying a wait queue it was a suspended on) to\n when it started running."

export function getTimeString(ns: number): string {
    let currentNs = ns
    let hour1 = 3600_000_000_000
    let minute1 = 60_000_000_000
    let second1 = 1_000_000_000; // 1 second
    let millisecond1 = 1_000_000; // 1 millisecond
    let microsecond1 = 1_000; // 1 microsecond
    let res = "";
    if (currentNs >= hour1) {
        res += Math.floor(currentNs / hour1) + "h ";
        currentNs = currentNs - Math.floor(currentNs / hour1) * hour1
    }
    if (currentNs >= minute1) {
        res += Math.floor(currentNs / minute1) + "m ";
        currentNs = currentNs - Math.floor(ns / minute1) * minute1
    }
    if (currentNs >= second1) {
        res += Math.floor(currentNs / second1) + "s ";
        currentNs = currentNs - Math.floor(currentNs / second1) * second1
    }
    if (currentNs >= millisecond1) {
        res += Math.floor(currentNs / millisecond1) + "ms ";
        currentNs = currentNs - Math.floor(currentNs / millisecond1) * millisecond1
    }
    if (currentNs >= microsecond1) {
        res += Math.floor(currentNs / microsecond1) + "μs ";
        currentNs = currentNs - Math.floor(currentNs / microsecond1) * microsecond1
    }
    if (currentNs > 0) {
        res += currentNs + "ns ";
    }
    return res
}

@element('tabpane-current-selection')
export class TabPaneCurrentSelection extends BaseElement {
    weakUpBean: WakeupBean | null | undefined;
    private tbl: LitTable | null | undefined;
    private tableObserver: MutationObserver | undefined
    // @ts-ignore
    private dpr: any = window.devicePixelRatio || window.webkitDevicePixelRatio || window.mozDevicePixelRatio || 1;

    set data(value: any) {
        this.setCpuData(value)
    }

    setCpuData(data: CpuStruct, callback: ((data: WakeupBean | null) => void) | undefined = undefined) {
        let leftTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#leftTitle");
        if (leftTitle) {
            leftTitle.innerText = "Slice Details"
        }
        let list: any[] = []
        let process = data.processName
        let processId = data.processId
        if (process == null || process == "") {
            process = data.name
            processId = data.tid
        }
        let state = ""
        if (data.end_state) {
            state = STATUS_MAP[data.end_state]
        } else if (data.end_state == "" || data.end_state == null) {
            state = ""
        } else {
            state = "Unknown State"
        }
        list.push({name: 'Process', value: `${process || 'Process'} [${processId}]`})
        list.push({name: 'Thread', value: `${data.name || 'Process'} [${data.tid}]`})
        list.push({name: 'CmdLine', value: data.processCmdLine})
        list.push({name: 'StartTime', value: getTimeString(data.startTime || 0)})
        list.push({name: 'Duration', value: getTimeString(data.dur || 0)})
        list.push({name: 'Prio', value: data.priority || 0})
        list.push({name: 'End State', value: state})
        this.queryWakeUpData(data).then((bean) => {
            if (callback) {
                callback(bean)
            }
            this.tbl!.dataSource = list
            let rightArea: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#right-table");
            let rightTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#rightTitle");
            let canvas = this.initCanvas();
            if (bean != null) {
                this.weakUpBean = bean;
                if (rightArea != null && rightArea) {
                    rightArea.style.visibility = "visible"
                }
                if (rightTitle != null && rightTitle) {
                    rightTitle.style.visibility = "visible"
                }
                this.drawRight(canvas, bean)
            } else {
                this.weakUpBean = null;
                if (rightArea != null && rightArea) {
                    rightArea.style.visibility = "hidden"
                }
                if (rightTitle != null && rightTitle) {
                    rightTitle.style.visibility = "hidden"
                }
            }
        })
    }

    setFunctionData(data: FuncStruct) {//方法信息
        this.initCanvas()
        let leftTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#leftTitle");
        let rightTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#rightTitle");
        if (rightTitle) {
            rightTitle.style.visibility = "hidden"
        }
        if (leftTitle) {
            leftTitle.innerText = "Slice Details"
        }
        let list: any[] = []
        list.push({name: 'Name', value: data.funName})
        list.push({name: 'StartTime', value: getTimeString(data.startTs || 0)})
        list.push({name: 'Duration', value: getTimeString(data.dur || 0)})
        if (FuncStruct.isBinder(data)) {
            if (data.argsetid != undefined) {
                queryBinderArgsByArgset(data.argsetid).then((argset) => {
                    argset.forEach((item) => {
                        list.push({name: item.keyName, value: item.strValue})
                    })

                });
            }
            list.push({name: 'depth', value: data.depth})
            list.push({name: 'arg_set_id', value: data.argsetid})
        }
        this.tbl!.dataSource = list

    }

    setMemData(data: ProcessMemStruct) {
        this.initCanvas()
        let leftTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#leftTitle");
        if (leftTitle) {
            leftTitle.innerText = "Counter Details"
        }
        let list: any[] = []
        list.push({name: 'Start time', value: getTimeString(data.startTime || 0)})
        list.push({name: 'Value', value: data.value})
        list.push({name: 'Delta', value: data.delta})
        list.push({name: 'Duration', value: getTimeString(data.duration || 0)})
        this.tbl!.dataSource = list

    }

    setThreadData(data: ThreadStruct) {
        this.initCanvas()
        let leftTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#leftTitle");
        let rightTitle: HTMLElement | null | undefined = this?.shadowRoot?.querySelector("#rightTitle");
        if (rightTitle) {
            rightTitle.style.visibility = "hidden"
        }
        if (leftTitle) {
            leftTitle.innerText = "Counter Details"
        }
        let list: any[] = []
        list.push({name: 'StartTime', value: getTimeString(data.startTime || 0)})
        list.push({name: 'Duration', value: getTimeString(data.dur || 0)})
        let state = ""
        if (data.state) {
            state = STATUS_MAP[data.state]
        } else if (data.state == "" || data.state == null) {
            state = ""
        } else {
            state = "Unknown State"
        }
        if ("Running" == state) {
            state = state + " on CPU " + data.cpu;
        }
        list.push({name: 'State', value: state})
        let processName = data.processName;
        if (processName == null || processName == "" || processName.toLowerCase() == "null") {
            processName = data.name;
        }
        list.push({name: 'Process', value: processName + " [" + data.pid + "] "})
        this.tbl!.dataSource = list
    }


    async queryWakeUpData(data: CpuStruct) {
        let wb: WakeupBean | null = null
        if (data.id == undefined || data.startTime == undefined) {
            return null
        }
        let wakeupTimes = await queryWakeUpThread_WakeTime(data.id, data.startTime)// 3,4835380000
        if (wakeupTimes != undefined && wakeupTimes.length > 0) {
            let wakeupTime = wakeupTimes[0]
            if (wakeupTime.wakeTs != undefined && wakeupTime.preRow != undefined && wakeupTime.wakeTs < wakeupTime.preRow) {
                return null
            }
            if (wakeupTime.wakeTs == undefined) {
                return null
            }
            let wakeupBeans = await queryWakeUpThread_WakeThread(wakeupTime.wakeTs)
            if (wakeupBeans != undefined && wakeupBeans.length > 0) {
                wb = wakeupBeans[0]
                if (wb != null) {
                    if (wakeupTime.wakeTs != undefined && wakeupTime.startTs != undefined) {
                        wb.wakeupTime = wakeupTime.wakeTs - wakeupTime.startTs
                    }
                    wb.schedulingLatency = data.startTime || 0 - (wb.wakeupTime || 0)
                    if (wb.process == null) {
                        wb.process = wb.thread;
                    }
                    if (wb.pid == undefined) {
                        wb.pid = wb.tid;
                    }
                    wb.schedulingDesc = INPUT_WORD
                }
            }
        }
        return wb
    }

    initCanvas(): HTMLCanvasElement | null {
        let canvas = this.shadowRoot!.querySelector("#rightDraw")
        let width = getComputedStyle(this.tbl!).getPropertyValue("width")
        let height = getComputedStyle(this.tbl!).getPropertyValue("height")
        if (canvas != null) {
            canvas.width = Math.round(Number(width.replace("px", "")) * this.dpr)
            canvas.height = Math.round(Number(height.replace("px", "")) * this.dpr)
            canvas.style.width = width
            canvas.style.height = height
        }
        SpApplication.skinChange = (val: boolean) => {
            this.drawRight(canvas, this.weakUpBean)
        }
        return canvas
    }

    drawRight(cavs: HTMLCanvasElement | null, wakeupBean: WakeupBean | null) {
        if (cavs == null) {
            return
        }
        let context = cavs.getContext("2d");
        if (context != null) {
            if (document.querySelector<SpApplication>("sp-application").dark) {
                context.strokeStyle = "#ffffff";
                context.fillStyle = "#ffffff";
            } else {
                context.strokeStyle = "#000000";
                context.fillStyle = "#000000";
            }
            context.lineWidth = this.dprToPx(2);
            context.moveTo(this.dprToPx(10), this.dprToPx(15))
            context.lineTo(this.dprToPx(10), this.dprToPx(125))
            context.stroke();
            //绘制菱形
            context.lineWidth = this.dprToPx(1);
            context.beginPath()
            context.moveTo(this.dprToPx(10), this.dprToPx(30))
            context.lineTo(this.dprToPx(4), this.dprToPx(40))
            context.lineTo(this.dprToPx(10), this.dprToPx(50))
            context.lineTo(this.dprToPx(16), this.dprToPx(40))
            context.lineTo(this.dprToPx(10), this.dprToPx(30))
            context.closePath()
            context.fill()
            context.font = this.dprToPx(12) + "px sans-serif";
            let strList = []
            strList.push("wakeup @ " + getTimeString(wakeupBean?.wakeupTime || 0) + " on CPU " + wakeupBean?.cpu + " by")
            strList.push("P:" + wakeupBean?.process + " [ " + wakeupBean?.pid + " ]")
            strList.push("F:" + wakeupBean?.thread + " [ " + wakeupBean?.tid + " ]")
            strList.forEach((str, index) => {
                if (context != null) {
                    context.fillText(str
                        , this.dprToPx(40), this.dprToPx(40 + 16 * index))
                }
            })
            context.lineWidth = this.dprToPx(2);
            context.lineJoin = "bevel"
            context.moveTo(this.dprToPx(10), this.dprToPx(95))
            context.lineTo(this.dprToPx(20), this.dprToPx(90))
            context.moveTo(this.dprToPx(10), this.dprToPx(95))
            context.lineTo(this.dprToPx(20), this.dprToPx(100))
            context.moveTo(this.dprToPx(10), this.dprToPx(95))
            context.lineTo(this.dprToPx(80), this.dprToPx(95))
            context.lineTo(this.dprToPx(70), this.dprToPx(90))
            context.moveTo(this.dprToPx(80), this.dprToPx(95))
            context.lineTo(this.dprToPx(70), this.dprToPx(100))
            context.stroke();
            context.font = this.dprToPx(12) + "px sans-serif";
            context.fillText("Scheduling latency:" + getTimeString(wakeupBean?.schedulingLatency || 0)
                , this.dprToPx(90), this.dprToPx(100))
            context.font = this.dprToPx(10) + "px sans-serif";
            INPUT_WORD.split("\n").forEach((str, index) => {
                context?.fillText(str
                    , this.dprToPx(90), this.dprToPx(120 + 12 * index))
            })

        }
    }

    dprToPx(num: number) {
        return Math.round(num * this.dpr)
    }

    initElements(): void {
        this.tbl = this.shadowRoot?.querySelector<LitTable>('#selectionTbl');
        this.tbl.addEventListener("column-click", ev => {
            console.log(ev.detail);
        })
        this.addTableObserver()
    }

    addTableObserver() {
        let MutationObserver = window.MutationObserver
        this.tableObserver = new MutationObserver((list) => {
            console.log(this.tbl);
            if (this.tbl) {
                let width = getComputedStyle(this.tbl).getPropertyValue("width")
                let height = getComputedStyle(this.tbl).getPropertyValue("height")
                console.log(width);
                console.log(height);
            }
        })
        let selector = this.shadowRoot?.querySelector(".left-table");
        this.tableObserver?.observe(selector!, {attributes: true, attributeFilter: ['style'], attributeOldValue: true})
    }

    initHtml(): string {
        return `
        <style>
            .current-title{
                width: 100%;
                display: flex;
                top: 0;
                background: var(--dark-background,#ffffff);
                position: sticky;
            }
            .current-title h2{
                width: 50%;
                padding: 0 10px;
                font-size: 16px;
                font-weight: 400;
                visibility: visible;
            }
            .bottom-scroll-area{
                display: flex;
                height: auto;
                overflow-y: auto;
            }
            .left-table{
                width: 50%;
                padding: 0 10px;
            }
            .right-table{
                width: 50%;
            }
        </style>
        <div style="width: 100%;height: auto;position: relative">
            <div class="current-title">
                <h2 id="leftTitle"></h2>
                <h2 id="rightTitle">Scheduling Latency</h2>
            </div>
            <div class="bottom-scroll-area">
                <div class="left-table">
                    <lit-table id="selectionTbl" no-head style="height: auto">
                        <lit-table-column title="name" data-index="name" key="name" align="flex-start"  width="180px">
                            <template><div>{{name}}</div></template>
                        </lit-table-column>
                        <lit-table-column title="value" data-index="value" key="value" align="flex-start" ></lit-table-column>
                    </lit-table>
                </div>
                <div class="right-table">
                    <canvas id="rightDraw" style="width: 100%;height: 100%;"></canvas>
                </div>
            </div>
        </div>
        `;
    }

}

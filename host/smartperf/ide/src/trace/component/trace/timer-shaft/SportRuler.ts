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

import {Graph} from "./Graph.js";
import {Rect} from "./Rect.js";
import {TimeRange} from "./RangeRuler.js";
import {Flag} from "./Flag.js";
import {LitTabs} from "../../../../base-ui/tabs/lit-tabs.js";
import {TraceSheet} from "../base/TraceSheet.js";

export class SportRuler extends Graph {
    public static rulerFlagObj: Flag | null = null;
    public flagListIdx: number | null = null
    public obj = [{x: 3}, {x: 2}];
    public flagList: Array<Flag> = [];

    lineColor: string | null = null;
    private rangeFlag = new Flag(0, 0, 0, 0, 0);
    private ruler_w = 1022;

    constructor(canvas: HTMLCanvasElement | undefined | null, c: CanvasRenderingContext2D, frame: Rect) {
        super(canvas, c, frame)
    }

    private _range: TimeRange = {} as TimeRange;

    get range(): TimeRange {
        return this._range;
    }

    set range(value: TimeRange) {
        this._range = value;
        this.draw()
    }

    modifyFlagList(type: string, flag: any = {}) {
        if (type == "amend") {
            console.log(this.flagListIdx);
            console.log(flag.text);
            if (flag.text && this.flagListIdx !== null) {
                this.flagList[this.flagListIdx].text = flag.text
            }
            if (flag.color && this.flagListIdx !== null) {
                this.flagList[this.flagListIdx].color = flag.color
            }
        } else if (type == "remove") {
            if (this.flagListIdx !== null) {
                this.flagList.splice(this.flagListIdx, 1)
            }
        }
        this.draw()
    }

    draw(): void {
        this.ruler_w = this.canvas!.offsetWidth
        this.c.clearRect(this.frame.x, this.frame.y, this.frame.width, this.frame.height)
        this.c.beginPath();
        this.lineColor = window.getComputedStyle(this.canvas!, null).getPropertyValue("color");
        this.c.strokeStyle = this.lineColor // "#dadada"
        this.c.lineWidth = 1;
        this.c.moveTo(this.frame.x, this.frame.y)
        this.c.lineTo(this.frame.x + this.frame.width, this.frame.y)
        this.c.stroke();
        this.c.closePath();
        this.c.beginPath();
        this.c.lineWidth = 3;
        this.c.strokeStyle = "#999999"
        this.c.moveTo(this.frame.x, this.frame.y)
        this.c.lineTo(this.frame.x, this.frame.y + this.frame.height)
        this.c.stroke();
        this.c.closePath();
        this.c.beginPath();
        this.c.lineWidth = 1;
        this.c.strokeStyle = this.lineColor; // "#999999"
        this.c.fillStyle = '#999999'
        this.c.font = '8px sans-serif'
        this.range.xs?.forEach((it, i) => {
            this.c.moveTo(it, this.frame.y)
            this.c.lineTo(it, this.frame.y + this.frame.height)
            this.c.fillText(`+${this.range.xsTxt[i]}`, it + 3, this.frame.y + 12)
        })

        this.c.stroke();
        this.c.closePath();
    }

    drawTheFlag(x: number, color: string = "#999999", isFill: boolean = false, text: string = "") {
        this.c.beginPath();
        this.c.fillStyle = color;
        this.c.strokeStyle = color;
        this.c.moveTo(x, 125);
        this.c.lineTo(x + 10, 125);
        this.c.lineTo(x + 10, 127);
        this.c.lineTo(x + 18, 127);
        this.c.lineTo(x + 18, 137);
        this.c.lineTo(x + 10, 137);
        this.c.lineTo(x + 10, 135);
        this.c.lineTo(x + 2, 135);
        this.c.lineTo(x + 2, 143);
        this.c.lineTo(x, 143);
        this.c.closePath()
        if (isFill) {
            this.c.fill()
        }
        this.c.stroke();


        if (text !== "") {
            this.c.font = "10px Microsoft YaHei"
            const {width} = this.c.measureText(text);
            this.c.fillStyle = 'rgba(255, 255, 255, 0.8)'; //
            this.c.fillRect(x + 21, 132, width + 4, 12);
            this.c.fillStyle = "black";
            this.c.fillText(text, x + 23, 142);
            this.c.stroke();
        }
    }

    randomRgbColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)]
        }
        return color;
    }

    mouseUp(ev: MouseEvent) {
        let search: HTMLDivElement | undefined | null = document.querySelector("sp-application")?.shadowRoot?.querySelector("div.search-container")
        let tabs: LitTabs | undefined | null = (ev?.target as any)?.shadowRoot?.querySelector("sp-system-trace")?.shadowRoot?.querySelector("trace-sheet")?.shadowRoot?.querySelector("#tabs")
        if (!tabs) {
            return;
        }
        let onResetSelected = ev.offsetY > search!.offsetHeight && ev.offsetY < (window.innerHeight - tabs!.offsetHeight)
        if (onResetSelected) {
            this.flagList.find((flagObj: Flag) => {
                if (flagObj.selected) {
                    flagObj.selected = false;
                    return true;
                }
            })
            SportRuler.rulerFlagObj = null;

            let x = ev.offsetX - (this.canvas?.offsetLeft || 0)
            let y = ev.offsetY - (this.canvas?.offsetTop || 0)
            if (y >= 123 && y < 142) {
                let onFlagRange = this.flagList.findIndex((flagObj: Flag, idx) => {
                    let flag_x = Math.round(this.ruler_w * (flagObj.time - this.range.startNS) / (this.range.endNS - this.range.startNS));
                    if (x >= flag_x && x <= flag_x + 18) {
                        flagObj.selected = true;
                        return true
                    }
                });
                if (onFlagRange == -1) {
                    let flagAtRulerTime = Math.round((this.range.endNS - this.range.startNS) * x / this.ruler_w)
                    if (flagAtRulerTime > 0 && (this.range.startNS + flagAtRulerTime) < this.range.endNS) {
                    }
                }
            }
            if (SportRuler.rulerFlagObj == null) {
                document.dispatchEvent(new CustomEvent('flag-draw'));
            }
        }
        if (onResetSelected) {
            this.draw()
        }
    }

    onFlagRangeEvent(flagObj: Flag, idx: number) {
        let traceSheet: TraceSheet | undefined | null = document.querySelector("sp-application")?.shadowRoot?.querySelector("sp-system-trace")?.shadowRoot?.querySelector("trace-sheet")
        SportRuler.rulerFlagObj = flagObj;
        this.flagListIdx = idx;

        traceSheet?.displayFlagData(flagObj, idx)
        document.dispatchEvent(new CustomEvent('flag-draw', {detail: {time: flagObj.time}}));
    }

    mouseMove(ev: MouseEvent) {
        let x = ev.offsetX - (this.canvas?.offsetLeft || 0)
        let y = ev.offsetY - (this.canvas?.offsetTop || 0)
        if (y >= 50 && y < 200) {
            this.draw()
            if (y >= 123 && y < 142 && x > 0) {
                let onFlagRange = this.flagList.findIndex((flagObj: Flag) => {
                    let flag_x = Math.round(this.ruler_w * (flagObj.time - this.range.startNS) / (this.range.endNS - this.range.startNS));
                    return (x >= flag_x && x <= flag_x + 18)
                });
                if (onFlagRange == -1) {
                    document.dispatchEvent(new CustomEvent('flag-draw', {detail: {x: x}}));
                } else {
                    document.dispatchEvent(new CustomEvent('flag-draw', {detail: {x: null}}));
                }
            } else {
                document.dispatchEvent(new CustomEvent('flag-draw', {detail: {x: null}}));
            }
        }
    }
}

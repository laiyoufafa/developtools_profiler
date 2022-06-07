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
import {ns2s, TimerShaftElement} from "../TimerShaftElement.js";
import {ColorUtils} from "../base/ColorUtils.js";
import {CpuStruct} from "../../../bean/CpuStruct.js";

const markPadding = 5;

export class Mark extends Graph {
    name: string | undefined
    inspectionFrame: Rect
    private _isHover: boolean = false

    constructor(canvas: HTMLCanvasElement | undefined | null, name: string, c: CanvasRenderingContext2D, frame: Rect) {
        super(canvas, c, frame);
        this.name = name;
        this.inspectionFrame = new Rect(frame.x - markPadding, frame.y, frame.width + markPadding * 2, frame.height)
    }

    get isHover(): boolean {
        return this._isHover;
    }

    set isHover(value: boolean) {
        this._isHover = value;
        if (value) {
            document.body.style.cursor = 'ew-resize'
        } else {
            document.body.style.cursor = 'default'
        }
    }

    draw(): void {
        this.c.beginPath();
        this.c.lineWidth = 7
        this.c.strokeStyle = '#999999'
        this.c.moveTo(this.frame.x, this.frame.y);
        this.c.lineTo(this.frame.x, this.frame.y + this.frame.height / 3)
        this.c.stroke();
        this.c.lineWidth = 1
        this.c.strokeStyle = '#999999'
        this.c.moveTo(this.frame.x, this.frame.y);
        this.c.lineTo(this.frame.x, this.frame.y + this.frame.height)
        this.c.stroke();
        this.c.closePath();
    }
}

export interface TimeRange {
    slicesTime: {
        color: string|null|undefined;
        startTime: number | null | undefined;
        endTime: number | null | undefined;
    };
    scale: number;
    totalNS: number
    startX: number
    endX: number
    startNS: number
    endNS: number
    xs: Array<number>
    xsTxt: Array<string>
}

export class RangeRuler extends Graph {
    public rangeRect: Rect
    public markA: Mark
    public markB: Mark
    public range: TimeRange;
    mouseDownOffsetX = 0
    mouseDownMovingMarkX = 0
    movingMark: Mark | undefined | null;
    isMouseDown: boolean = false;
    isMovingRange: boolean = false;
    isNewRange: boolean = false;
    markAX: number = 0;
    markBX: number = 0;
    isPress: boolean = false
    pressFrameId: number = -1
    currentDuration: number = 0
    centerXPercentage: number = 0;
    animaStartTime: number | undefined
    animTime: number = 250;
    p: number = 2000;
    private readonly notifyHandler: (r: TimeRange) => void;
    private scale: number = 0;
    //缩放级别
    private scales: Array<number> = [50, 100, 200, 500, 1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000,
        1_000_000, 2_000_000, 5_000_000, 10_000_000, 20_000_000, 50_000_000, 100_000_000, 200_000_000, 500_000_000,
        1_000_000_000, 2_000_000_000, 5_000_000_000, 10_000_000_000, 20_000_000_000, 50_000_000_000,
        100_000_000_000, 200_000_000_000, 500_000_000_000];
    private _cpuUsage: Array<{ cpu: number, ro: number, rate: number }> = []

    constructor(timerShaftEL: TimerShaftElement, frame: Rect, range: TimeRange, notifyHandler: (r: TimeRange) => void) {
        super(timerShaftEL.canvas, timerShaftEL.ctx!, frame)
        this.range = range;
        this.notifyHandler = notifyHandler;
        this.markA = new Mark(timerShaftEL.canvas, 'A', timerShaftEL.ctx!, new Rect(range.startX, frame.y, 1, frame.height))
        this.markB = new Mark(timerShaftEL.canvas, 'B', timerShaftEL.ctx!, new Rect(range.endX, frame.y, 1, frame.height))
        this.rangeRect = new Rect(range.startX, frame.y, range.endX - range.startX, frame.height)
    }

    set cpuUsage(value: Array<{ cpu: number, ro: number, rate: number }>) {
        this._cpuUsage = value
        this.draw();
    }

    drawCpuUsage() {
        let maxNum = Math.round(this._cpuUsage.length / 100)
        let miniHeight = Math.round(this.frame.height / CpuStruct.cpuCount);//每格高度
        let miniWidth = Math.ceil(this.frame.width / 100);//每格宽度
        for (let i = 0; i < this._cpuUsage.length; i++) {
            let it = this._cpuUsage[i]
            this.c.fillStyle = ColorUtils.MD_PALETTE[it.cpu]
            this.c.globalAlpha = it.rate
            this.c.fillRect(this.frame.x + miniWidth * it.ro, this.frame.y + it.cpu * miniHeight, miniWidth, miniHeight)
        }
    }

    draw(discardNotify: boolean = false): void {
        this.c.clearRect(this.frame.x - markPadding, this.frame.y, this.frame.width + markPadding * 2, this.frame.height)
        this.c.beginPath();
        if (this._cpuUsage.length > 0) {
            this.drawCpuUsage()
            this.c.globalAlpha = 0;
        } else {
            this.c.globalAlpha = 1;
        }
        //绘制选中区域
        this.c.fillStyle = window.getComputedStyle(this.canvas!, null).getPropertyValue("background-color");//"#ffffff"
        this.rangeRect.x = this.markA.frame.x < this.markB.frame.x ? this.markA.frame.x : this.markB.frame.x
        this.rangeRect.width = Math.abs(this.markB.frame.x - this.markA.frame.x)
        this.c.fillRect(this.rangeRect.x, this.rangeRect.y, this.rangeRect.width, this.rangeRect.height)
        this.c.globalAlpha = 1;
        this.c.globalAlpha = .5;
        this.c.fillStyle = "#999999"
        this.c.fillRect(this.frame.x, this.frame.y, this.rangeRect.x, this.rangeRect.height)
        this.c.fillRect(this.rangeRect.x + this.rangeRect.width, this.frame.y, this.frame.width - this.rangeRect.width, this.rangeRect.height)
        this.c.globalAlpha = 1;
        this.c.closePath();
        this.markA.draw();
        this.markB.draw();
        if (this.notifyHandler) {
            this.range.startX = this.rangeRect.x
            this.range.endX = this.rangeRect.x + this.rangeRect.width
            this.range.startNS = this.range.startX * this.range.totalNS / (this.canvas?.clientWidth || 0)
            this.range.endNS = this.range.endX * this.range.totalNS / (this.canvas?.clientWidth || 0)
            let l20 = (this.range.endNS - this.range.startNS) / 20;
            let min = 0;
            let max = 0;
            let weight = 0;
            for (let index = 0; index < this.scales.length; index++) {
                if (this.scales[index] > l20) {
                    if (index > 0) {
                        min = this.scales[index - 1];
                    } else {
                        min = 0;
                    }
                    max = this.scales[index];
                    weight = (l20 - min) * 1.0 / (max - min);
                    if (weight > 0.243) {
                        this.scale = max;
                    } else {
                        this.scale = min;
                    }
                    break;
                }
            }
            if (this.scale == 0) {
                this.scale = this.scales[0];
            }
            let tmpNs = 0;
            let yu = this.range.startNS % this.scale;
            let realW = (this.scale * this.frame.width) / (this.range.endNS - this.range.startNS);
            let startX = 0;
            if (this.range.xs) {
                this.range.xs.length = 0
            } else {
                this.range.xs = []
            }
            if (this.range.xsTxt) {
                this.range.xsTxt.length = 0
            } else {
                this.range.xsTxt = []
            }
            this.range.scale = this.scale;
            if (yu != 0) {
                let firstNodeWidth = ((this.scale - yu) / this.scale * realW);
                startX += firstNodeWidth;
                tmpNs += yu;
                this.range.xs.push(startX)
                this.range.xsTxt.push(ns2s(tmpNs))
            }
            while (tmpNs < this.range.endNS - this.range.startNS) {
                startX += realW;
                tmpNs += this.scale;
                this.range.xs.push(startX)
                this.range.xsTxt.push(ns2s(tmpNs))
            }
            if (!discardNotify) {
                this.notifyHandler(this.range)
            }
        }
    }

    mouseDown(ev: MouseEvent) {
        let x = ev.offsetX - (this.canvas?.offsetLeft || 0)
        let y = ev.offsetY - (this.canvas?.offsetTop || 0)
        this.isMouseDown = true;
        this.mouseDownOffsetX = x;
        if (this.markA.isHover) {
            this.movingMark = this.markA;
            this.mouseDownMovingMarkX = this.movingMark.frame.x || 0
        } else if (this.markB.isHover) {
            this.movingMark = this.markB;
            this.mouseDownMovingMarkX = this.movingMark.frame.x || 0
        } else {
            this.movingMark = null;
        }
        if (this.rangeRect.containsWithPadding(x, y, 5, 0)) {
            this.isMovingRange = true;
            this.markAX = this.markA.frame.x;
            this.markBX = this.markB.frame.x;
            document.body.style.cursor = "move"
        } else if (this.frame.containsWithMargin(x, y, 20, 0, 0, 0) && !this.rangeRect.containsWithMargin(x, y, 0, markPadding, 0, markPadding)) {
            this.isNewRange = true;
        }
    }

    mouseUp(ev: MouseEvent) {
        this.isMouseDown = false;
        this.isMovingRange = false;
        this.isNewRange = false;
        this.movingMark = null;
    }

    mouseMove(ev: MouseEvent) {
        let x = ev.offsetX - (this.canvas?.offsetLeft || 0);
        let y = ev.offsetY - (this.canvas?.offsetTop || 0)
        this.centerXPercentage = x / (this.canvas?.clientWidth || 0)
        if (this.centerXPercentage <= 0) {
            this.centerXPercentage = 0
        } else if (this.centerXPercentage >= 1) {
            this.centerXPercentage = 1
        }
        let maxX = this.canvas?.clientWidth || 0
        if (this.markA.inspectionFrame.contains(x, y)) {
            this.markA.isHover = true
        } else if (this.markB.inspectionFrame.contains(x, y)) {
            this.markB.isHover = true;
        } else {
            this.markA.isHover = false;
            this.markB.isHover = false;
        }
        if (this.movingMark) {
            let result = x - this.mouseDownOffsetX + this.mouseDownMovingMarkX;
            if (result >= 0 && result <= maxX) {
                this.movingMark.frame.x = result
            } else if (result < 0) {
                this.movingMark.frame.x = 0
            } else {
                this.movingMark.frame.x = maxX
            }
            this.movingMark.inspectionFrame.x = this.movingMark.frame.x - markPadding
            requestAnimationFrame(() => this.draw());
        } else if (this.rangeRect.containsWithPadding(x, y, markPadding, 0)) {
            document.body.style.cursor = "move"
        } else if (this.frame.containsWithMargin(x, y, 20, 0, 0, 0) && !this.rangeRect.containsWithMargin(x, y, 0, markPadding, 0, markPadding)) {
            document.body.style.cursor = "crosshair";
        }
        if (this.isMovingRange && this.isMouseDown) {
            let result = x - this.mouseDownOffsetX;
            let mA = result + this.markAX
            let mB = result + this.markBX
            if (mA >= 0 && mA <= maxX) {
                this.markA.frame.x = mA
            } else if (mA < 0) {
                this.markA.frame.x = 0
            } else {
                this.markA.frame.x = maxX
            }
            this.markA.inspectionFrame.x = this.markA.frame.x - markPadding
            if (mB >= 0 && mB <= maxX) {
                this.markB.frame.x = mB;
            } else if (mB < 0) {
                this.markB.frame.x = 0
            } else {
                this.markB.frame.x = maxX
            }
            this.markB.inspectionFrame.x = this.markB.frame.x - markPadding
            requestAnimationFrame(() => this.draw());
        } else if (this.isNewRange) {
            this.markA.frame.x = this.mouseDownOffsetX;
            this.markA.inspectionFrame.x = this.mouseDownOffsetX - markPadding;
            if (x >= 0 && x <= maxX) {
                this.markB.frame.x = x;
            } else if (x < 0) {
                this.markB.frame.x = 0;
            } else {
                this.markB.frame.x = maxX;
            }
            this.markB.inspectionFrame.x = this.markB.frame.x - markPadding;
            requestAnimationFrame(() => this.draw());
        }
    }

    mouseOut(ev: MouseEvent) {
        this.movingMark = null;
    }

    fillX() {
        if (this.range.startNS < 0) this.range.startNS = 0;
        if (this.range.endNS < 0) this.range.endNS = 0;
        if (this.range.endNS > this.range.totalNS) this.range.endNS = this.range.totalNS;
        if (this.range.startNS > this.range.totalNS) this.range.startNS = this.range.totalNS;
        this.range.startX = this.range.startNS * (this.canvas?.clientWidth || 0) / this.range.totalNS
        this.range.endX = this.range.endNS * (this.canvas?.clientWidth || 0) / this.range.totalNS
        this.markA.frame.x = this.range.startX
        this.markA.inspectionFrame.x = this.markA.frame.x - markPadding
        this.markB.frame.x = this.range.endX
        this.markB.inspectionFrame.x = this.markB.frame.x - markPadding
    }

    setRangeNS(startNS: number, endNS: number) {
        this.range.startNS = startNS
        this.range.endNS = endNS
        this.fillX()
        this.draw();
    }

    getRange(): TimeRange {
        return this.range;
    }

    keyPress(ev: KeyboardEvent) {
        if (this.animaStartTime === undefined) {
            this.animaStartTime = new Date().getTime();
        }
        let startTime = new Date().getTime();
        let duration = (startTime - this.animaStartTime);
        if (duration < this.animTime * 2) duration = duration + this.animTime
        this.currentDuration = duration
        if (this.isPress) return
        this.isPress = true
        switch (ev.key.toLocaleLowerCase()) {
            case "w":
                let animW = () => {
                    if (this.scale === 50) return;
                    this.range.startNS += (this.centerXPercentage * this.currentDuration * 2 * this.scale / this.p);
                    this.range.endNS -= ((1 - this.centerXPercentage) * this.currentDuration * 2 * this.scale / this.p);
                    this.fillX();
                    this.draw();
                    this.pressFrameId = requestAnimationFrame(animW)
                }
                this.pressFrameId = requestAnimationFrame(animW)
                break;
            case "s":
                let animS = () => {
                    if (this.range.startNS <= 0 && this.range.endNS >= this.range.totalNS) return;
                    this.range.startNS -= (this.centerXPercentage * this.currentDuration * 2 * this.scale / this.p);
                    this.range.endNS += ((1 - this.centerXPercentage) * this.currentDuration * 2 * this.scale / this.p);
                    this.fillX();
                    this.draw();
                    this.pressFrameId = requestAnimationFrame(animS)
                }
                this.pressFrameId = requestAnimationFrame(animS)
                break;
            case "a":
                let animA = () => {
                    if (this.range.startNS == 0) return;
                    let s = this.scale / this.p * this.currentDuration;
                    this.range.startNS -= s;
                    this.range.endNS -= s;
                    this.fillX();
                    this.draw();
                    this.pressFrameId = requestAnimationFrame(animA)
                }
                this.pressFrameId = requestAnimationFrame(animA)
                break;
            case "d":
                let animD = () => {
                    if (this.range.endNS >= this.range.totalNS) return;
                    this.range.startNS += this.scale / this.p * this.currentDuration;
                    this.range.endNS += this.scale / this.p * this.currentDuration;
                    this.fillX();
                    this.draw();
                    this.pressFrameId = requestAnimationFrame(animD)
                }
                this.pressFrameId = requestAnimationFrame(animD)
                break;
        }
    }

    keyUp(ev: KeyboardEvent) {
        this.animaStartTime = undefined;
        this.isPress = false
        if (this.pressFrameId != -1) {
            cancelAnimationFrame(this.pressFrameId)
        }
        let startTime = new Date().getTime();
        switch (ev.key) {
            case "w":
                let animW = () => {
                    if (this.scale === 50) return;
                    let dur = (new Date().getTime() - startTime);
                    this.range.startNS += (this.centerXPercentage * 100 * this.scale / this.p);
                    this.range.endNS -= ((1 - this.centerXPercentage) * 100 * this.scale / this.p);
                    this.fillX();
                    this.draw();
                    if (dur < 200) {
                        requestAnimationFrame(animW)
                    }
                }
                requestAnimationFrame(animW)
                break;
            case "s":
                let animS = () => {
                    if (this.range.startNS <= 0 && this.range.endNS >= this.range.totalNS) return;
                    let dur = (new Date().getTime() - startTime);
                    this.range.startNS -= (this.centerXPercentage * 100 * this.scale / this.p);
                    this.range.endNS += ((1 - this.centerXPercentage) * 100 * this.scale / this.p);
                    this.fillX();
                    this.draw();
                    if (dur < 200) {
                        requestAnimationFrame(animS)
                    }
                }
                requestAnimationFrame(animS)
                break;
            case "a":
                let animA = () => {
                    if (this.range.startNS <= 0) return
                    let dur = (new Date().getTime() - startTime);
                    let s = this.scale * 80 / this.p;
                    this.range.startNS -= s;
                    this.range.endNS -= s;
                    this.fillX();
                    this.draw();
                    if (dur < 200) {
                        requestAnimationFrame(animA)
                    }
                }
                animA();
                break;
            case "d":
                let animD = () => {
                    if (this.range.endNS >= this.range.totalNS) return;
                    let dur = (new Date().getTime() - startTime);
                    let s = this.scale * 80 / this.p;
                    this.range.startNS += s;
                    this.range.endNS += s;
                    this.fillX();
                    this.draw();
                    if (dur < 200) {
                        requestAnimationFrame(animD)
                    }
                }
                animD();
                break;
        }
    }
}

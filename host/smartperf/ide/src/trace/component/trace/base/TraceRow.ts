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
import {TimeRange} from "../timer-shaft/RangeRuler.js";
import '../../../../base-ui/icon/LitIcon.js'
import {Rect} from "../timer-shaft/Rect.js";
import {BaseStruct} from "../../../bean/BaseStruct.js";
import {SpSystemTrace} from "../../SpSystemTrace.js";
import {ns2x} from "../TimerShaftElement.js";
import {TraceRowObject} from "./TraceRowObject.js";
import {LitCheckBox} from "../../../../base-ui/checkbox/LitCheckBox.js";

export class RangeSelectStruct {
    startX: number | undefined
    endX: number | undefined
    startNS: number | undefined
    endNS: number | undefined
}

@element('trace-row')
export class TraceRow<T extends BaseStruct> extends BaseElement {
    static ROW_TYPE_CPU = "cpu"
    static ROW_TYPE_CPU_FREQ = "cpu-freq"
    static ROW_TYPE_FPS = "fps"
    static ROW_TYPE_PROCESS = "process"
    static ROW_TYPE_THREAD = "thread"
    static ROW_TYPE_MEM = "mem"
    static ROW_TYPE_HEAP = "heap"
    static ROW_TYPE_FUNC = "func"
    static range: TimeRange | undefined | null;
    static rangeSelectObject: RangeSelectStruct | undefined
    public obj: TraceRowObject<any> | undefined | null;
    public must: boolean = true;
    public dataList: undefined | Array<T>;
    public dataListCache: Array<T> = [];
    public c: CanvasRenderingContext2D | null = null;
    public describeEl: Element | null | undefined;
    public canvas: HTMLCanvasElement | null | undefined;
    public canvasContainer: HTMLDivElement | null | undefined;
    public tipEL: HTMLDivElement | null | undefined;
    public checkBoxEL: LitCheckBox | null | undefined;
    public onDrawHandler: ((ctx: CanvasRenderingContext2D) => void) | undefined | null
    public onThreadHandler: ((ctx: CanvasRenderingContext2D, useCache: boolean) => void) | undefined | null
    public onItemDrawHandler: ((ctx: CanvasRenderingContext2D, data: T, preData?: T, nextData?: T) => void) | undefined | null
    public supplier: (() => Promise<Array<T>>) | undefined | null
    // @ts-ignore
    offscreen: OffscreenCanvas | undefined;
    canvasWidth = 0
    canvasHeight = 0
    isHover: boolean = false;
    hoverX: number = 0;
    hoverY: number = 0;
    index: number = 0;
    dpr = window.devicePixelRatio || 1;
    private rootEL: HTMLDivElement | null | undefined;
    private nameEL: HTMLLabelElement | null | undefined;
    private isLoading: boolean = false

    constructor(args: { alpha: boolean, contextId: string, isOffScreen: boolean }) {
        super(args);
    }

    static get observedAttributes() {
        return ["folder", "name", "expansion", "children", "height", "row-type", "row-id", "row-parent-id", "sleeping",
            "check-type"
        ];
    }

    public _frame: Rect | undefined;

    get frame(): Rect {
        if (this._frame) {
            this._frame.width = (this.parentElement?.clientWidth || 0) - 248;
            this._frame.height = this.canvas?.clientHeight || 40;
            return this._frame;
        } else {
            this._frame = new Rect(0, 0, (this.parentElement?.clientWidth || 0) - 248, this.canvas?.clientHeight || 40);
            return this._frame;
        }
    }

    set frame(f: Rect) {
        this._frame = f;
    }

    private _rangeSelect: boolean = false;

    get rangeSelect(): boolean {
        return this._rangeSelect;
    }

    set rangeSelect(value: boolean) {
        this._rangeSelect = value;
    }

    get sleeping(): boolean {
        return this.hasAttribute("sleeping");
    }

    set sleeping(value: boolean) {
        if (value) {
            this.setAttribute("sleeping", "")
        } else {
            this.removeAttribute("sleeping")
            this.draw();
        }
    }

    get rowType(): string | undefined | null {
        return this.getAttribute("row-type");
    }

    set rowType(val) {
        this.setAttribute("row-type", val || "")
    }

    get rowId(): string | undefined | null {
        return this.getAttribute("row-id");
    }

    set rowId(val) {
        this.setAttribute("row-id", val || "")
    }

    get rowParentId(): string | undefined | null {
        return this.getAttribute("row-parent-id");
    }

    set rowParentId(val) {
        this.setAttribute("row-parent-id", val || "")
    }

    set rowHidden(val: boolean) {
        if (val) {
            this.setAttribute("row-hidden", "")
        } else {
            this.removeAttribute("row-hidden")
        }
    }

    get name(): string {
        return this.getAttribute("name") || ""
    }

    set name(value: string) {
        this.setAttribute("name", value)
    }

    get folder(): boolean {
        return this.hasAttribute("folder");
    }

    set folder(value: boolean) {
        if (value) {
            this.setAttribute("folder", '')
        } else {
            this.removeAttribute('folder')
        }
    }

    get expansion(): boolean {
        return this.hasAttribute("expansion")
    }

    set expansion(value) {
        if (value) {
            this.setAttribute("expansion", '');
        } else {
            this.removeAttribute('expansion')
        }
        this.parentElement?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${this.rowId}']`).forEach(it => {
            if (this.expansion) {
                it.rowHidden = false
            } else {
                it.rowHidden = true
            }
        })
        this.dispatchEvent(new CustomEvent("expansion-change", {
            detail: {
                expansion: this.expansion,
                rowType: this.rowType,
                rowId: this.rowId,
                rowParentId: this.rowParentId
            }
        }))
    }

    set tip(value: string) {
        if (this.tipEL) {
            this.tipEL.innerHTML = value;
        }
    }

    get checkType(): string {
        return this.getAttribute("check-type") || "";
    }

    set checkType(value: string) {
        this.setAttribute("check-type", value);
    }

    initElements(): void {
        this.rootEL = this.shadowRoot?.querySelector('.root')
        this.checkBoxEL = this.shadowRoot?.querySelector<LitCheckBox>('.lit-check-box')
        this.describeEl = this.shadowRoot?.querySelector('.describe')
        this.nameEL = this.shadowRoot?.querySelector('.name')
        this.canvas = this.shadowRoot?.querySelector('.panel')
        this.canvasContainer = this.shadowRoot?.querySelector('.panel-container')
        this.tipEL = this.shadowRoot?.querySelector('.tip')
        this.describeEl?.addEventListener('click', () => {
            if (this.folder) {
                this.expansion = !this.expansion
            }
        })
    }

    initCanvas() {
        if (this.canvas) {
            if (!this.args.isOffScreen) {
                this.c = this.canvas?.getContext(this.args.contextId, {alpha: this.args.alpha});
            }

            if (this.shadowRoot?.host.clientWidth == 0) {
                let preCanvas = this.previousElementSibling?.shadowRoot?.querySelector<HTMLCanvasElement>("canvas");
                if (preCanvas) {
                    this.canvasWidth = preCanvas.width;
                    this.canvasHeight = preCanvas.height;
                    this.canvas.width = preCanvas.width;
                    this.canvas.height = preCanvas.height;
                    this.canvas.style.width = preCanvas.style.width;
                    this.canvas.style.height = preCanvas.style.height;
                    if (this.c instanceof CanvasRenderingContext2D && !this.args.isOffScreen) {
                        this.c?.scale(this.dpr, this.dpr);
                        this.c?.translate(0, 0)
                    }
                }
            } else {
                let oldWidth = (this.shadowRoot!.host.clientWidth || 0) - (this.describeEl?.clientWidth || 248) - SpSystemTrace.scrollViewWidth - 1;
                let oldHeight = parseInt(this.getAttribute("height") || '40') //this.shadowRoot!.host.clientHeight || 0;
                this.rootEL!.style.height = `${this.getAttribute("height") || '40'}px`
                this.canvas.style.width = '100%';
                this.canvas.style.height = oldHeight + 'px';
                this.canvas.width = Math.floor(oldWidth * this.dpr);
                this.canvas.height = Math.floor(oldHeight * this.dpr);
                this.canvasWidth = this.canvas.width;
                this.canvasHeight = this.canvas.height;
                if (this.c instanceof CanvasRenderingContext2D && !this.args.isOffScreen) {
                    this.c?.scale(this.dpr, this.dpr);
                    this.c?.translate(0, 0)
                }
            }
            if (this.args.isOffScreen) {
                // @ts-ignore
                this.offscreen = this.canvas.transferControlToOffscreen();
            }
        }
    }

    updateWidth(width: number) {
        let dpr = window.devicePixelRatio || 1;
        this.canvasWidth = Math.round((width - (this.describeEl?.clientWidth || 248) - SpSystemTrace.scrollViewWidth) * dpr);
        this.canvasHeight = Math.round((this.shadowRoot!.host.clientHeight || 40) * dpr);
        if (this.args.isOffScreen) {
            this.draw(true);
            return;
        }
        this.canvas!.width = width - (this.describeEl?.clientWidth || 248) - SpSystemTrace.scrollViewWidth;
        this.canvas!.height = this.shadowRoot!.host.clientHeight || 40;
        let oldWidth = this.canvas!.width;
        let oldHeight = this.canvas!.height;
        this.canvas!.width = Math.round((oldWidth) * dpr);
        this.canvas!.height = Math.round(oldHeight * dpr);
        this.canvas!.style.width = oldWidth + 'px';
        this.canvas!.style.height = oldHeight + 'px';
        if (this.c instanceof CanvasRenderingContext2D) {
            this.c?.scale(dpr, dpr);
        }
    }

    connectedCallback() {
        this.checkBoxEL!.onchange = (ev: any) => {
            if (!ev.target.checked) {
                this.rangeSelect = false;
                this.draw();
            } else {
                this.rangeSelect = true;
                this.draw();
            }
        }
        this.initCanvas();
    }

    onMouseHover(x: number, y: number, tip: boolean = true): T | undefined | null {
        if (this.dataListCache && this.dataListCache.length > 0) {
            return this.dataListCache.find(it => (it.frame && Rect.contains(it.frame, x, y)));
        } else if (this.dataList && this.dataList.length > 0) {
            return this.dataList.find(it => (it.frame && Rect.contains(it.frame, x, y)));
        }
        if (this.tipEL) {
            this.tipEL.style.display = 'none';
        }
        return null;
    }

    setTipLeft(x: number, struct: any) {
        if (struct == null && this.tipEL) {
            this.tipEL.style.display = 'none';
            return
        }
        if (this.tipEL) {
            this.tipEL.style.display = 'flex';
            if (x + this.tipEL.clientWidth > (this.canvas?.style.width.replace("px", "") || 0)) {
                this.tipEL.style.transform = `translateX(${x - this.tipEL.clientWidth - 1}px)`;
            } else {
                this.tipEL.style.transform = `translateX(${x}px)`;
            }
        }
    }

    onMouseLeave(x: number, y: number) {
        if (this.tipEL) {
            this.tipEL.style.display = 'none';
        }
    }

    draw(useCache: boolean = false) {
        if (this.sleeping) {
            return;
        }
        if (!this.dataList) {
            if (this.supplier && !this.isLoading) {
                this.isLoading = true;
                if (this.supplier) {
                    let promise = this.supplier();
                    if (promise) {
                        promise.then(res => {
                            this.dataList = res
                            this.isLoading = false;
                            this.draw(false);
                        })
                    } else {
                        this.dataList = [];
                        this.isLoading = false;
                        this.draw(false);
                    }

                }
            }
            if (this.c && this.c instanceof CanvasRenderingContext2D) {
                this.c.clearRect(0, 0, this.canvas?.clientWidth || 0, this.canvas?.clientHeight || 0)
                this.c.beginPath();
                this.drawLines(this.c!);
                this.drawSelection(this.c!);
                this.c.fillStyle = window.getComputedStyle(this.nameEL!, null).getPropertyValue("color");
                this.c.fillText("Loading...", this.frame.x + 10, Math.ceil(this.frame.y + this.frame.height / 2))
                this.c.stroke();
                this.c.closePath();
            }
        } else {
            if (this.onDrawHandler && this.dataList && this.c instanceof CanvasRenderingContext2D) {
                if (this.c && TraceRow.range) {
                    this.clearCanvas(this.c!)
                    this.c.beginPath();
                    this.drawLines(this.c!);
                    this.onDrawHandler!(this.c!)
                    this.drawSelection(this.c!);
                    this.c.closePath();
                }
            } else if (this.onItemDrawHandler) {
                if (this.c && this.c instanceof CanvasRenderingContext2D && TraceRow.range) {
                    this.clearCanvas(this.c!)
                    this.c.beginPath();
                    this.drawLines(this.c!);
                    if (this.onItemDrawHandler && this.dataList) {
                        for (let i = 0; i < this.dataList.length; i++) {
                            let preData = this.dataList[i - 1] || undefined;
                            let data = this.dataList[i];
                            let nextData = this.dataList[i + 1] || undefined;
                            this.onItemDrawHandler(this.c!, data, preData, nextData)
                        }
                    }
                    this.drawSelection(this.c!);
                    this.c.closePath();
                }
            } else if (this.onThreadHandler) {
                this.onThreadHandler!(this.c!, useCache)
            }
        }
    }

    drawObject() {
        if (!this.obj) return;
        if (!this.obj.dataList) {
            if (this.obj.supplier && !this.obj.isLoading) {
                this.obj.isLoading = true;
                if (this.obj.supplier) {
                    let promise = this.obj.supplier();
                    if (promise) {
                        promise.then(res => {
                            console.log(res);
                            this.obj!.dataList = res
                            this.obj!.isLoading = false;
                            this.drawObject();
                        })
                    } else {
                        this.obj.dataList = [];
                        this.obj.isLoading = false;
                        this.drawObject();
                    }

                }
            }
            window.requestAnimationFrame(() => {
                if (this.c && this.c instanceof CanvasRenderingContext2D) {
                    this.c.clearRect(0, 0, this.canvas?.clientWidth || 0, this.canvas?.clientHeight || 0)
                    this.c.beginPath();
                    this.c.lineWidth = 1;
                    this.c.strokeStyle = "#dadada"
                    if (TraceRow.range) {
                        TraceRow.range.xs.forEach(it => {
                            // @ts-ignore
                            this.c!.moveTo(Math.floor(it), 0)
                            // @ts-ignore
                            this.c!.lineTo(Math.floor(it), this.shadowRoot?.host.clientHeight || 0)
                        })
                    }
                    this.drawSelection(this.c!);
                    this.c.fillStyle = window.getComputedStyle(this.nameEL!, null).getPropertyValue("color");
                    this.c.fillText("Loading...", this.frame.x + 10, Math.ceil(this.frame.y + this.frame.height / 2))
                    this.c.stroke();
                    this.c.closePath();
                }
            })
        } else {
            if (this.obj.onDrawHandler && this.obj.dataList) {
                window.requestAnimationFrame(() => {
                    if (this.c && TraceRow.range) {
                        this.clearCanvas(this.c!)
                        // @ts-ignore
                        this.c.beginPath();
                        this.drawLines(<CanvasRenderingContext2D>this.c!);
                        this.obj!.onDrawHandler!(this.c!)
                        this.drawSelection(<CanvasRenderingContext2D>this.c!);
                        // @ts-ignore
                        this.c.closePath();
                    }
                })
            } else if (this.obj.onThreadHandler) {
                this.obj.onThreadHandler(this, this.c!)
            }
        }
    }

    clearCanvas(ctx: CanvasRenderingContext2D) {
        if (ctx) {
            ctx.clearRect(0, 0, this.canvas?.clientWidth || 0, this.canvas?.clientHeight || 0)
        }
    }

    drawLines(ctx: CanvasRenderingContext2D) {
        if (ctx) {
            ctx.lineWidth = 1;
            ctx.strokeStyle = this.getLineColor();
            TraceRow.range?.xs.forEach(it => {
                ctx.moveTo(Math.floor(it), 0)
                ctx.lineTo(Math.floor(it), this.shadowRoot?.host.clientHeight || 0)
            })
            ctx.stroke();
        }
    }

    getLineColor() {
        return window.getComputedStyle(this.rootEL!, null).getPropertyValue("border-bottom-color")
    }

    drawSelection(ctx: CanvasRenderingContext2D) {
        if (this.rangeSelect) {
            TraceRow.rangeSelectObject!.startX = Math.floor(ns2x(TraceRow.rangeSelectObject!.startNS!, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS!, this.frame));
            TraceRow.rangeSelectObject!.endX = Math.floor(ns2x(TraceRow.rangeSelectObject!.endNS!, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS!, this.frame));
            if (ctx) {
                ctx.globalAlpha = 0.5
                ctx.fillStyle = "#666666"
                ctx.fillRect(TraceRow.rangeSelectObject!.startX!, this.frame.y, TraceRow.rangeSelectObject!.endX! - TraceRow.rangeSelectObject!.startX!, this.frame.height)
                ctx.globalAlpha = 1
            }
        }
    }

    isInTimeRange(startTime: number, duration: number): boolean {
        return ((startTime || 0) + (duration || 0) > (TraceRow.range?.startNS || 0) && (startTime || 0) < (TraceRow.range?.endNS || 0));
    }

    attributeChangedCallback(name: string, oldValue: string, newValue: string) {
        switch (name) {
            case "name":
                if (this.nameEL) {
                    this.nameEL.textContent = newValue;
                    this.nameEL.title = newValue;
                }
                break;
            case "height":
                if (newValue != oldValue) {
                    if (!this.args.isOffScreen) {
                        this.initCanvas();
                    }
                }
                break;
            case "check-type":
                if (newValue === "check") {
                    this.checkBoxEL?.setAttribute("checked", "");
                } else {
                    this.checkBoxEL?.removeAttribute("checked");
                }
                break;
        }
    }

    initHtml(): string {
        return `
<style>
*{
    box-sizing: border-box;
}
:host(:not([row-hidden])){
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    width: 100%;
}
:host([row-hidden]){
    width: 100%;
    display: none;
}
.root{
    height: 40px;
    width: 100%;
    display: grid;
    grid-template-rows: 100%;
    grid-template-columns: 248px 1fr;
    border-bottom: 1px solid var(--dark-border1,#dadada);
    box-sizing: border-box;
}
.describe{
    box-sizing: border-box;
    border-right: 1px solid var(--dark-border1,#c9d0da);
    background-color: transparent;
    align-items: center;
    position: relative;
}
.panel{
    width: 100%;
    height: 100%;
    overflow: visible;
    background-color: transparent;
}
.panel-container{
    width: 100%;
    height: 100%;
    position: relative;
    pointer-events: none;
}
.tip{
    position:absolute;
    top: 0;
    left: 0;
    height: 100%;
    background-color: white;
    border: 1px solid #f9f9f9;
    width: auto;
    font-size: 8px;
    color: #50809e;
    flex-direction: column;
    justify-content: center;
    align-items: flex-start;
    padding: 2px 10px;
    display: none;
    user-select: none;
}
.name{
    color: var(--dark-color1,#4b5766);
    margin-left: 10px;
    font-size: .9rem;
    font-weight: normal;
    width: 100%;
    max-height: 100%;
    text-align: left;
    overflow: hidden;
    user-select: none;
}
.icon{
    color: var(--dark-color1,#151515);
    margin-left: 10px;
}
.describe:hover {
    cursor: pointer;
}
:host([folder]) .describe:hover > .icon{
    color:#ecb93f;
    margin-left: 10px;
}
:host([folder]){
    background-color: var(--dark-background1,#f5fafb);
}
:host([folder]) .icon{
    display: flex;
}
:host(:not([folder])){
    background-color: var(--dark-background,#FFFFFF);
}
:host(:not([folder]):not([children])) {
}
:host(:not([folder]):not([children])) .icon{
    display: none;
}
:host(:not([folder])[children]) .icon{
    visibility: hidden;
    color:#fff
}

:host(:not([folder])[children]) .name{
}
:host([expansion]) {
    background-color: var(--bark-expansion,#0C65D1);
}
:host([expansion]) .name,:host([expansion]) .icon{
    color: #fff;
}
:host([expansion]) .describe{
    border-right: 0px;
}
:host([expansion]:not(sleeping)) .panel-container{
    display: none;
}
:host([expansion]) .children{
    flex-direction: column;
    width: 100%;
}
:host([expansion]) .icon{
    transform: rotateZ(0deg);
}
:host(:not([expansion])) .children{
    display: none;
    flex-direction: column;
    width: 100%;
}
:host(:not([expansion])) .icon{
    transform: rotateZ(-90deg);
}
:host([sleeping]) .describe{
    display: none;
}
:host([sleeping]) .panel-container{
    display: none;
}
:host([sleeping]) .children{
    display: none;
}
:host(:not([sleeping])) .describe{
    display: flex;;
}
:host(:not([sleeping])) .panel-container{
    display: flex;
}
:host(:not([sleeping])) .children{
    display: flex;
}

 :host([check-type]) .lit-check-box{
    display: none;
}
:host(:not([check-type])) .lit-check-box{
    display: none;
}
</style>
<div class="root">
    <div class="describe">
        <lit-icon class="icon" name="caret-down" size="13"></lit-icon>
        <label class="name"></label>
        <lit-check-box class="lit-check-box"></lit-check-box>
    </div>
    <div class="panel-container">
        <canvas class="panel"></canvas>
        <div class="tip"> 
            P:process [1573]<br>
            T:Thread [675]
        </div>
    </div>
</div>
        `;
    }

}

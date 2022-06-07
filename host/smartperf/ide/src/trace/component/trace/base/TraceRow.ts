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

import {element} from "../../../../base-ui/BaseElement.js";
import {TimeRange} from "../timer-shaft/RangeRuler.js";
import '../../../../base-ui/icon/LitIcon.js'
import {Rect} from "../timer-shaft/Rect.js";
import {BaseStruct} from "../../../bean/BaseStruct.js";
import {SpSystemTrace} from "../../SpSystemTrace.js";
import {ns2x} from "../TimerShaftElement.js";
import {TraceRowObject} from "./TraceRowObject.js";
import {LitCheckBox} from "../../../../base-ui/checkbox/LitCheckBox.js";
import {LitIcon} from "../../../../base-ui/icon/LitIcon";
import "../../../../base-ui/popover/LitPopoverV.js"
import {LitPopover} from "../../../../base-ui/popover/LitPopoverV.js";

export class RangeSelectStruct {
    startX: number | undefined
    endX: number | undefined
    startNS: number | undefined
    endNS: number | undefined
}

@element('trace-row')
export class TraceRow<T extends BaseStruct> extends HTMLElement {
    static ROW_TYPE_CPU = "cpu"
    static ROW_TYPE_CPU_FREQ = "cpu-freq"
    static ROW_TYPE_FPS = "fps"
    static ROW_TYPE_NATIVE_MEMORY = "native-memory"
    static ROW_TYPE_HIPERF = "hiperf"
    static ROW_TYPE_HIPERF_CPU = "hiperf-cpu"
    static ROW_TYPE_HIPERF_PROCESS = "hiperf-process"
    static ROW_TYPE_HIPERF_THREAD = "hiperf-thread"
    static ROW_TYPE_PROCESS = "process"
    static ROW_TYPE_THREAD = "thread"
    static ROW_TYPE_MEM = "mem"
    static ROW_TYPE_HEAP = "heap"
    static ROW_TYPE_FUNC = "func"
    static ROW_TYPE_MONITOR = "ability-monitor"
    static ROW_TYPE_CPU_ABILITY = "cpu-ability"
    static ROW_TYPE_MEMORY_ABILITY = "memory-ability"
    static ROW_TYPE_DISK_ABILITY = "disk-ability"
    static ROW_TYPE_NETWORK_ABILITY = "network-ability"
    static range: TimeRange | undefined | null;
    static rangeSelectObject: RangeSelectStruct | undefined
    public obj: TraceRowObject<any> | undefined | null;
    isHover: boolean = false;
    hoverX: number = 0;
    hoverY: number = 0;
    index: number = 0;
    public must: boolean = false;
    public isTransferCanvas = false;
    onComplete: Function | undefined;
    isComplete: boolean = false;
    public dataList: undefined | Array<T>;
    public describeEl: Element | null | undefined;
    public canvas: Array<HTMLCanvasElement> = [];
    public canvasContainer: HTMLDivElement | null | undefined;
    public tipEL: HTMLDivElement | null | undefined;
    public checkBoxEL: LitCheckBox | null | undefined;
    public collectEL: LitIcon | null | undefined;
    public onDrawHandler: ((ctx: CanvasRenderingContext2D) => void) | undefined | null
    public onThreadHandler: ((useCache: boolean) => void) | undefined | null
    public onDrawTypeChangeHandler: ((type: number) => void) | undefined | null
    public supplier: (() => Promise<Array<T>>) | undefined | null
    public favoriteChangeHandler: ((fav: TraceRow<any>) => void) | undefined | null
    public selectChangeHandler: ((list: Array<TraceRow<any>>) => void) | undefined | null
    dpr = window.devicePixelRatio || 1;
    // @ts-ignore
    offscreen: Array<OffscreenCanvas | undefined> = [];
    canvasWidth = 0
    canvasHeight = 0
    public _frame: Rect | undefined;
    public isLoading: boolean = false
    public readonly args: any;
    private rootEL: HTMLDivElement | null | undefined;
    private nameEL: HTMLLabelElement | null | undefined;
    private _rangeSelect: boolean = false;
    private _drawType: number = 0
    private folderIconEL: LitIcon | null | undefined;

    constructor(args: { canvasNumber: number, alpha: boolean, contextId: string, isOffScreen: boolean }) {
        super();
        this.args = args;
        this.attachShadow({mode: 'open'}).innerHTML = this.initHtml();
        this.initElements();
    }

    static get observedAttributes() {
        return ["folder", "name", "expansion", "children", "height", "row-type", "row-id", "row-parent-id", "sleeping",
            "check-type",
            "collect-type",
            "disabled-check"
        ];
    }

    get collect() {
        return this.hasAttribute("collect-type")
    }

    set collect(value) {
        if (value) {
            this.setAttribute("collect-type", "")
        } else {
            this.removeAttribute("collect-type")
        }
    }

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
            if (!it.collect) {
                it.rowHidden = !this.expansion;
            }
            if (it.folder && !value && it.expansion) {
                it.expansion = value;
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

    get frame(): Rect {
        let cHeight = 0;
        this.canvas.forEach(it => {
            cHeight += (it?.clientHeight || 40);
        })
        if (this._frame) {
            this._frame.width = (this.parentElement?.clientWidth || 0) - 248 - SpSystemTrace.scrollViewWidth;
            this._frame.height = cHeight;
            return this._frame;
        } else {
            this._frame = new Rect(0, 0, (this.parentElement?.clientWidth || 0) - 248 - SpSystemTrace.scrollViewWidth, cHeight);
            return this._frame;
        }
    }

    set frame(f: Rect) {
        this._frame = f;
    }

    get disabledCheck(): boolean {
        return this.hasAttribute("disabled-check");
    }

    set disabledCheck(value: boolean) {
        if (value) {
            this.setAttribute("disabled-check", '')
            this.checkBoxEL!.style.display = "none";
        } else {
            this.removeAttribute('disabled-check')
            this.checkBoxEL!.style.display = "flex";
        }
    }

    get checkType(): string {
        return this.getAttribute("check-type") || "";
    }

    set checkType(value: string) {
        if (!value || value.length == 0) {
            this.removeAttribute("check-type");
            return;
        }
        this.setAttribute("check-type", value);
        if (this.hasAttribute("disabled-check")) {
            this.checkBoxEL!.style.display = "none";
            return;
        }
        switch (value) {
            case "-1":
                this.checkBoxEL!.style.display = "none";
                this.rangeSelect = false;
                break;
            case "0":
                this.checkBoxEL!.style.display = "flex";
                this.checkBoxEL!.checked = false;
                this.checkBoxEL!.indeterminate = false;
                this.rangeSelect = false;
                break;
            case "1":
                this.checkBoxEL!.style.display = "flex";
                this.checkBoxEL!.checked = false
                this.checkBoxEL!.indeterminate = true;
                this.rangeSelect = false;
                break;
            case "2":
                this.rangeSelect = true;
                this.checkBoxEL!.style.display = "flex";
                this.checkBoxEL!.checked = true;
                this.checkBoxEL!.indeterminate = false;
                break;
        }
    }

    get drawType(): number {
        return this._drawType;
    }

    set drawType(value: number) {
        this._drawType = value;
        let radioList: NodeListOf<any> = this.shadowRoot!.querySelectorAll("input[type=radio][name=status]")
        if (radioList!.length > 0) {
            radioList[Number(value)].checked = true
        }
    }

    get highlight(): boolean {
        return this.hasAttribute("expansion");
    }

    set highlight(value: boolean) {
        if (value) {
            this.setAttribute("highlight", '')
        } else {
            this.removeAttribute('highlight')
        }
    }

    set folderPaddingLeft(value: number) {
        this.folderIconEL!.style.marginLeft = value + "px";
    }

    initElements(): void {
        this.rootEL = this.shadowRoot?.querySelector('.root')
        this.checkBoxEL = this.shadowRoot?.querySelector<LitCheckBox>('.lit-check-box')
        this.collectEL = this.shadowRoot?.querySelector<LitIcon>('.collect')
        this.describeEl = this.shadowRoot?.querySelector('.describe')
        this.folderIconEL = this.shadowRoot?.querySelector<LitIcon>('.icon')
        this.nameEL = this.shadowRoot?.querySelector('.name')
        this.canvasContainer = this.shadowRoot?.querySelector('.panel-container')
        this.tipEL = this.shadowRoot?.querySelector('.tip')
        let canvasNumber = this.args["canvasNumber"];
        for (let i = 0; i < canvasNumber; i++) {
            let canvas = document.createElement('canvas');
            canvas.className = "panel";
            this.canvas.push(canvas);
            this.canvasContainer!.appendChild(canvas);
        }
        this.describeEl?.addEventListener('click', () => {
            if (this.folder) {
                this.expansion = !this.expansion
            }
        })
    }

    initCanvas(list: Array<HTMLCanvasElement>): void {
        let timerShaftCanvas = this.parentElement!.parentElement!.querySelector("timer-shaft-element")!.shadowRoot!.querySelector<HTMLCanvasElement>("canvas");
        let tempHeight: number = 0;
        if (this.rowType == TraceRow.ROW_TYPE_FUNC) {
            tempHeight = 20;
        } else if (this.rowType == TraceRow.ROW_TYPE_THREAD) {
            tempHeight = 30;
        } else {
            tempHeight = 40;
        }
        list.forEach((canvas, i) => {
            // let oldWidth = (this.shadowRoot!.host.clientWidth || 0) - (this.describeEl?.clientWidth || 248) - SpSystemTrace.scrollViewWidth;
            this.rootEL!.style.height = `${this.getAttribute("height") || '40'}px`
            canvas.style.width = timerShaftCanvas!.style.width;
            canvas.style.height = tempHeight + 'px';
            // canvas.style.backgroundColor = `${randomRgbColor()}`
            this.canvasWidth = timerShaftCanvas!.width;
            this.canvasHeight = Math.ceil(tempHeight * this.dpr);
            canvas.width = this.canvasWidth;
            canvas.height = this.canvasHeight;
            // @ts-ignore
            this.offscreen.push(canvas!.transferControlToOffscreen());
        })
    }

    updateWidth(width: number) {
        let dpr = window.devicePixelRatio || 1;
        let tempHeight: number = 0;
        if (this.rowType == TraceRow.ROW_TYPE_FUNC) {
            tempHeight = 20;
        } else if (this.rowType == TraceRow.ROW_TYPE_THREAD) {
            tempHeight = 30;
        } else {
            tempHeight = 40;
        }
        let tempTop = 0;
        if (this.canvas.length > 1) {
            tempHeight = 20;
            tempTop = 10;
        }
        this.canvas.forEach(it => {
            this.canvasWidth = Math.ceil((width - (this.describeEl?.clientWidth || 248)) * dpr);
            this.canvasHeight = Math.ceil(tempHeight * this.dpr);
            it!.style.width = (width - (this.describeEl?.clientWidth || 248)) + 'px';
            if (this.args.isOffScreen) {
                this.draw(true);
            }
        })
    }

    connectedCallback() {
        this.checkBoxEL!.onchange = (ev: any) => {
            if (!ev.target.checked) {
                this.rangeSelect = false;
                this.checkType = "0"
                this.draw();
            } else {
                this.rangeSelect = true;
                this.checkType = "2"
                this.draw();
            }
            this.setCheckBox(ev.target.checked);
        }
        this.collectEL!.onclick = (e) => {
            this.collect = !this.collect;
            let spacer = this.parentElement!.previousElementSibling! as HTMLDivElement;
            if (this.collect) {
                spacer.style.height = `${spacer.offsetHeight + this.offsetHeight!}px`;
            } else {
                spacer.style.height = `${spacer.offsetHeight - this.offsetHeight!}px`;
                let parent = this.parentElement!.querySelector<TraceRow<any>>(`trace-row[row-id='${this.rowParentId}']`);
                if (parent) {
                    this.rowHidden = !parent.expansion;
                }
            }
            let collectList = this.parentElement!.querySelectorAll<TraceRow<any>>(`trace-row[collect-type]`);
            collectList.forEach((it, i) => {
                if (i == 0) {
                    it.style.top = `${spacer.offsetTop + 48}px`;
                } else {
                    it.style.top = `${collectList[i - 1].offsetTop + collectList[i - 1].offsetHeight}px`;
                }
            })
            this.favoriteChangeHandler?.(this)
        }
        this.initCanvas(this.canvas);
        let _this = this;
        let radioList = this.shadowRoot!.querySelectorAll("input[type=radio][name=status]")
        let popover = this.shadowRoot!.querySelector<LitPopover>(".popover")
        this.shadowRoot!.querySelector<HTMLDivElement>("#first-radio")!.onclick = (e) => {
            // @ts-ignore
            radioList[0]!.checked = true;
            // @ts-ignore
            popover!.visible = false
            setTimeout(() => {
                this.onDrawTypeChangeHandler?.(0);
            }, 300);
        }
        this.shadowRoot!.querySelector<HTMLDivElement>("#second-radio")!.onclick = (e) => {
            // @ts-ignore
            radioList[1]!.checked = true;
            // @ts-ignore
            popover!.visible = false
            setTimeout(() => {
                this.onDrawTypeChangeHandler?.(1);
            }, 300);
        }
    }

    setCheckBox(isCheck: boolean) {
        if (this.folder) {
            let allRow = this.parentElement?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${this.rowId}'][check-type]`)
            allRow!.forEach((ck) => {
                ck.setAttribute("check-type", isCheck ? "2" : "0")
                let allCheck: LitCheckBox | null | undefined = ck?.shadowRoot?.querySelector(".lit-check-box")
                allCheck!.checked = isCheck
            })
        } else if (this.rowParentId == "" && !this.folder) {
            this.selectChangeHandler?.([...this.parentElement!.querySelectorAll<TraceRow<any>>("trace-row[check-type='2']")])
            return;
        }
        let checkList = this.parentElement?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${this.folder ? this.rowId : this.rowParentId}'][check-type="2"]`)
        let unselectedList = this.parentElement?.querySelectorAll<TraceRow<any>>(`trace-row[row-parent-id='${this.folder ? this.rowId : this.rowParentId}'][check-type="0"]`)
        let parentRow = this.parentElement?.querySelector<TraceRow<any>>(`trace-row[row-id='${this.folder ? this.rowId : this.rowParentId}'][folder]`)
        let parentCheck: LitCheckBox | null | undefined = parentRow?.shadowRoot?.querySelector(".lit-check-box")

        if (unselectedList!.length == 0) {
            parentRow!.setAttribute("check-type", "2")
            parentCheck!.checked = true
            parentCheck!.indeterminate = false;
            checkList?.forEach((it) => {
                it.checkType = "2";
                it.rangeSelect = true;
                it.draw()
            })
        } else {
            parentRow!.setAttribute("check-type", "1")
            parentCheck!.checked = false
            parentCheck!.indeterminate = true;
            checkList?.forEach((it) => {
                it.checkType = "2";
                it.rangeSelect = true;
                it.draw()
            })
            unselectedList?.forEach((it) => {
                it.checkType = "0";
                it.rangeSelect = false;
                it.draw()
            })
        }

        if (checkList!.length == 0) {
            parentRow!.setAttribute("check-type", "0")
            parentCheck!.checked = false
            parentCheck!.indeterminate = false;
            unselectedList?.forEach((it) => {
                it.checkType = "0";
                it.rangeSelect = false;
                it.draw()
            })
        }
        this.selectChangeHandler?.([...this.parentElement!.querySelectorAll<TraceRow<any>>("trace-row[check-type='2']")])
    }

    onMouseHover(x: number, y: number, tip: boolean = true): T | undefined | null {
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
            if (x + this.tipEL.clientWidth > (this.canvas[0]!.clientWidth || 0)) {
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
        if (!this.isComplete) {
            if (this.supplier && !this.isLoading) {
                this.isLoading = true;
                this.must = true;
                if (this.supplier) {
                    let promise = this.supplier();
                    if (promise) {
                        promise.then(res => {
                            this.dataList = res
                            if (this.onComplete) {
                                this.onComplete();
                            }
                            this.isComplete = true;
                            this.isLoading = false;
                            this.draw(false);
                        })
                    } else {
                        this.isLoading = false;
                        this.draw(false);
                    }
                }
            }
        } else {
            if (this.onThreadHandler && this.dataList) {
                this.onThreadHandler!(useCache)
            }
        }
    }

    clearCanvas(ctx: CanvasRenderingContext2D) {
        if (ctx) {
            this.canvas.forEach(it => {
                ctx.clearRect(0, 0, it!.clientWidth || 0, it!.clientHeight || 0)
            })
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
            height: min-content;
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
            display: block;
        }
        .panel-container{
            width: 100%;
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
        :host([highlight]) .name{
            color: #4b5766;
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
            display: block;
        }
        :host(:not([sleeping])) .children{
            display: flex;
        }
        :host([folder]) .lit-check-box{
            display: none;
        }
        :host(:not([check-type])) .lit-check-box{
            display: none;
        }
        :host([collect-type]) {
            position:fixed;
            z-index:1000;
        }
        :host(:not([collect-type])) {
            position:static;
        }
        :host([collect-type]) .collect{
            display: block;
            color: #5291FF;
        }
        :host(:not([collect-type])) .collect{
            display: none;
            color: var(--dark-icon,#666666);
        }
        .collect{
            margin-right: 5px;
        }
        :host(:not([folder])) .describe:hover .collect{
            display: block;
        }
        :host([row-type="native-memory"]) .popover{
            display: flex;
        }
        .popover{
            color: var(--dark-color1,#4b5766);
            display: none;
            justify-content: center;
            align-items: center;
            margin-right: 5px;
        }
        .radio{
            margin-right: 10px;

        }
        #setting{
            color: var(--dark-color1,#606060);
        }
        :host([expansion]) #setting{
            color: #FFFFFF;
        }
        :host([highlight]) .flash{
            background-color: #ffe263;
        }

        </style>
        <div class="root">
            <div class="describe flash">
                <lit-icon class="icon" name="caret-down" size="13"></lit-icon>
                <label class="name"></label>
                <lit-icon class="collect" name="star-fill" size="17"></lit-icon>
                <lit-popover placement="bottomLeft" trigger="click" class="popover" haveRadio="true">
                    <div slot="content">
                        <div id="first-radio" style="margin-bottom: 5px">
                        <input class="radio" name="status" type="radio" value="0" />Current Bytes</div>
                        <div id="second-radio" style="margin-bottom: 5px">
                        <input class="radio" name="status" type="radio" value="1" />Native Memory Density</div>
                    </div>
                    <lit-icon name="setting" size="17" id="setting"></lit-icon>
                </lit-popover>
                <lit-check-box class="lit-check-box"></lit-check-box>
            </div>
            <div class="panel-container">
                <div class="tip">
                    P:process [1573]<br>
                    T:Thread [675]
                </div>
            </div>
        </div>
        `;
    }
}

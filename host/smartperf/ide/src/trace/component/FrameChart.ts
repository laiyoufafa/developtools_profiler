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

import {BaseElement, element} from "../../base-ui/BaseElement.js";
import {NativeHookCallInfo} from "../bean/NativeHook.js";
import {ChartMode, ChartStruct, Rect} from "../database/ProcedureWorkerCommon.js";
import {SpApplication} from "../SpApplication.js";
import {Utils} from "./trace/base/Utils.js";

const TAG: string = "FrameChart";
const scaleHeight = 30;
const depthHeight = 20;

@element('tab-framechart')
export class FrameChart extends BaseElement {
    private canvas: HTMLCanvasElement | undefined | null;
    private cavasContext: CanvasRenderingContext2D | undefined | null;
    private floatHint: HTMLDivElement | undefined | null;

    private rect: Rect = new Rect(0, 0, 0, 0);
    private _mode = ChartMode.Call;
    private startX = 0; // canvas start x coord
    private startY = 0; // canvas start y coord
    private canvasX = -1; // canvas current x
    private canvasY = -1; // canvas current y
    private lastCanvasXInScale = 0;
    private hintContent = ""; // float hoint inner html content

    private historyList: Array<Array<ChartStruct>> = [];
    private currentSize = 0;
    private currentCount = 0;
    private currentData: Array<ChartStruct> | undefined;
    private xPoint = 0; // x in rect
    private isFocusing = false;
    private canvasScrollTop = 0;
    private deltaXperent = 0;
    private _maxDepth = 0;

    static get observedAttributes() {
        return []
    }

    set selectTotalSize(size: number) {
    }

    set selectTotalCount(count: number) {
    }

    set data(val: Array<ChartStruct> | any) {
        this.currentData = val;
        this.deltaXperent = 0;
        this.xPoint = 0;
        this.caldrawArgs();
    }

    set maxDepth(depth: number) {
    }

    set tabPaneScrollTop(scrollTop: number) {
        this.canvasScrollTop = scrollTop;
    }

    /**
     * cal total count size and max Depth
     */
    private caldrawArgs() : void{
        this.currentCount = 0;
        this.currentSize = 0;
        for (let rootNode of this.currentData!){
            this.currentCount += rootNode.count;
            this.currentSize += rootNode.size;
            let depth = 0;
            this.calMaxDepth(rootNode,depth);
        }
        this.rect.height = ( this._maxDepth + 2) * 20 + scaleHeight; // 20px/depth and 30 is scale height
    }

    /**
     * cal max Depth
     * @param node every child node
     * @param depth current depth
     */
    private calMaxDepth(node: ChartStruct,depth : number) : void{
        depth ++;
        if (node.children && node.children.length > 0) {
            for (let children of node.children) {
                this.calMaxDepth(children,depth);
            }
        } else{
            this._maxDepth = Math.max(depth,this._maxDepth);
        }
    }


    /**
     * get chart mode
     */
    get mode() {
        return this._mode;
    }

    /**
     * set chart mode
     * @param mode chart format for data mode
     */
    set mode(mode: ChartMode) {
        this._mode = mode;
    }

    /**
     * calculate Data and draw chart
     */
    calculateChartData(): void {
        this.clearCanvas();
        this.cavasContext?.beginPath();
        this.drawScale();
        let x = this.xPoint;
        switch (this.mode) {
            case ChartMode.Byte:
                for (let node of this.currentData!) {
                    let width = Math.ceil(node.size / this.currentSize * this.rect!.width);
                    let height = depthHeight; // 20px / depth
                    // ensure the data for first depth frame
                    if (!node.frame) {
                        node.frame = new Rect(x, scaleHeight, width, height)
                    } else {
                        node.frame!.x = x;
                        node.frame!.y = scaleHeight;
                        node.frame!.width = width;
                        node.frame!.height = height;
                    }
                    x += width;
                    NativeHookCallInfo.draw(this.cavasContext!, node, node.size / this.currentSize);
                    this.drawFrameChart(node);
                }
                break;
            case ChartMode.Count:
                for (let node of this.currentData!) {
                    let width = Math.ceil(node.count / this.currentCount * this.rect!.width);
                    let height = depthHeight; // 20px / depth
                    // ensure the data for first depth frame
                    if (!node.frame) {
                        node.frame = new Rect(x, scaleHeight, width, height)
                    } else {
                        node.frame!.x = x;
                        node.frame!.y = scaleHeight;
                        node.frame!.width = width;
                        node.frame!.height = height;
                    }
                    x += width;
                    NativeHookCallInfo.draw(this.cavasContext!, node, node.count / this.currentCount);
                    this.drawFrameChart(node);
                }
                break;
        }
        this.cavasContext?.closePath();
    }

    /**
     * clear canvas all data
     */
    public clearCanvas() {
        this.cavasContext?.clearRect(0, 0, this.canvas!.width, this.canvas!.height);
    }

    /**
     * update canvas size
     */
    public updateCanvas(): void {
        if (this.canvas!.getBoundingClientRect()) {
            let box = this.canvas!.getBoundingClientRect();
            let D = document.documentElement;
            this.startX = box.left + Math.max(D.scrollLeft, document.body.scrollLeft) - D.clientLeft;
            this.startY = box.top + Math.max(D.scrollTop, document.body.scrollTop) - D.clientTop;
        }
        if (this.canvas instanceof HTMLCanvasElement) {
            this.canvas.style.width = 100 + "%";
            this.canvas.style.height = this.rect!.height + "px";
            this.canvas.width = this.canvas!.clientWidth;
            this.canvas.height = Math.ceil(this.rect!.height);
        }
        this.rect!.width = this.canvas!.clientWidth;

    }

    /**
     * draw top Scale Into 100 pieces
     */
    private drawScale(): void {
        let spApplication = <SpApplication>document.getElementsByTagName("sp-application")[0];
        // line
        this.cavasContext!.lineWidth = 0.5;
        this.cavasContext?.moveTo(0, 0);
        this.cavasContext?.lineTo(this.canvas!.width, 0);

        for (let i = 0; i <= 10; i++) {
            let startX = Math.floor(this.canvas!.width / 10 * i);
            for (let j = 0; j < 10; j++) {
                // children scale
                this.cavasContext!.lineWidth = 0.5;
                let startItemX = startX + Math.floor(this.canvas!.width / 100 * j);
                this.cavasContext?.moveTo(startItemX, 0);
                this.cavasContext?.lineTo(startItemX, 10);
            }
            if (i == 0) continue; // skip first Size is 0
            // long line every 10 count
            this.cavasContext!.lineWidth = 1;
            let sizeRatio = this.canvas!.width / this.rect.width; // scale ratio
            if (spApplication.dark) {
                this.cavasContext!.strokeStyle = "#888";
            } else {
                this.cavasContext!.strokeStyle = "#ddd";
            }
            this.cavasContext?.moveTo(startX, 0);
            this.cavasContext?.lineTo(startX, this.canvas!.height);
            if (spApplication.dark) {
                this.cavasContext!.fillStyle = "#fff";
            } else {
                this.cavasContext!.fillStyle = "#000";
            }
            let scale = '';
            if (this.mode == ChartMode.Byte) {
                scale = Utils.getByteWithUnit(this.currentSize * sizeRatio / 10 * i);
            } else {
                scale = (this.currentCount * sizeRatio / 10 * i).toFixed(0) + '';
            }
            this.cavasContext?.fillText(scale, startX + 5, depthHeight, 50); // 50 is Text max Size
            this.cavasContext?.stroke();
        }
    }

    /**
     * draw chart
     * @param node draw chart by every piece
     */
    private drawFrameChart(node: ChartStruct): void {
        if (node.children && node.children.length > 0) {
            for (let children of node.children) {
                children.parent = node;
                if (this.mode == ChartMode.Byte) {
                    NativeHookCallInfo.setFuncFrame(children, this.rect!, this.currentSize, this.mode);
                    NativeHookCallInfo.draw(this.cavasContext!, children, children.size / this.currentSize);
                } else {
                    NativeHookCallInfo.setFuncFrame(children, this.rect!, this.currentCount, this.mode);
                    NativeHookCallInfo.draw(this.cavasContext!, children, children.count / this.currentCount);
                }
                this.drawFrameChart(children);
            }
        }
    }

    /**
     * find target node from tree by mouse position
     *
     * @param nodes tree nodes
     * @param canvasX x coord of canvas
     * @param canvasY y coord of canvas
     * @returns target node
     */
    private searchData(nodes: Array<ChartStruct>, canvasX: number, canvasY: number): any {
        for (let node of nodes) {
            if (node.frame?.contains(canvasX, canvasY)) {
                return node;
            } else {
                let result = this.searchData(node.children, canvasX, canvasY);
                if (!result) continue; // if not found in this branch;search another branch
                return result;
            }
        }
        return null;
    }

    /**
     * show float hint and update position
     */
    private updateFloatHint(): void {
        this.floatHint!.innerHTML = this.hintContent;
        this.floatHint!.style.display = 'flex';
        let x = this.canvasX;
        let y = this.canvasY - this.canvasScrollTop;
        //right rect hint show left
        if (this.canvasX + this.floatHint!.clientWidth > (this.canvas?.clientWidth || 0)) {
            x -= this.floatHint!.clientWidth - 1;
        } else {
            x += 30;
        }
        //bottom rect hint show top
        if (this.canvasY + this.floatHint!.clientHeight > (this.canvas?.clientHeight || 0)) {
            y -= this.floatHint!?.clientHeight - 1;
            y += 30;
        } else {
            y -= 10;
        }
        this.floatHint!.style.transform = `translate(${x}px,${y}px)`;
    }

    /**
     * redraw Chart while click to scale chart
     * @param selectData select Rect data as array
     */
    private redrawChart(selectData: Array<ChartStruct>): void {
        this.currentData = selectData;
        this.currentSize = 0;
        this.currentCount = 0;
        if (selectData.length == 0) return;
        for (let data of selectData) {
            this.currentSize += data.size;
            this.currentCount += data.count;
        }
        this.calculateChartData();
    }

    /**
     * press w to zoom in, s to zoom out
     * @param index < 0 zoom out , > 0 zoom in
     */
    private scale(index: number): void {
        let newWidth = 0;
        // zoom in
        let deltaWidth = this.rect!.width * 0.1;
        if (index > 0) {
            newWidth = this.rect!.width + deltaWidth;
            // max scale
            let sizeRatio = this.canvas!.width / this.rect.width;
            if (this.mode == ChartMode.Byte) {
                if (Math.round(this.currentSize * sizeRatio) <= 10) {
                    newWidth = this.canvas!.width / (10 / this.currentSize);
                }
            } else {
                if (Math.round(this.currentCount * sizeRatio) <= 10) {
                    newWidth = this.canvas!.width / (10 / this.currentCount);
                }
            }
        } else { // zoom out
            newWidth = this.rect!.width - deltaWidth;
            // min scale
            if (newWidth < this.canvas!.clientWidth) {
                newWidth = this.canvas!.clientWidth;
                this.xPoint = 0;
                this.lastCanvasXInScale = 0
            }
        }
        // width not change
        if (newWidth == this.rect.width) return;
        this.translationByScale(index, deltaWidth, newWidth);
    }

    /**
     * translation after scale
     * @param index is zoom in
     * @param deltaWidth scale delta width
     * @param newWidth rect width after scale
     */
    private translationByScale(index: number, deltaWidth: number, newWidth: number): void {
        if (this.lastCanvasXInScale == 0) {
            this.lastCanvasXInScale = this.canvasX;
        }
        if (this.canvasX - this.lastCanvasXInScale != 0) {
            this.deltaXperent = (this.canvasX - this.lastCanvasXInScale) / this.rect.width;
        }
        let translationValue = deltaWidth * this.canvasX / this.canvas!.width * (1 - this.deltaXperent);
        if (index > 0) {
            this.xPoint -= translationValue;
        } else {
            this.xPoint += translationValue;
        }
        this.lastCanvasXInScale = this.canvasX;
        this.rect!.width = newWidth;
        this.translationDraw();
    }

    /**
     * press a/d to translate rect
     * @param index left or right
     */
    private translation(index: number): void {
        // let width = this.rect!.width;
        let offset = this.canvas!.width / 10;
        for (let i = 0; i < Math.abs(index); i++) {
            if (index < 0) {
                this.xPoint += offset;
            } else {
                this.xPoint -= offset;
            }
        }
        this.translationDraw();
    }

    /**
     * judge position ro fit canvas and draw
     */
    private translationDraw(): void {
        // rightad trans limit
        if (this.xPoint > 0) {
            this.xPoint = 0;
        }
        // left trans limit
        if (this.rect.width + this.xPoint < this.canvas!.width) {
            this.xPoint = this.canvas!.width - this.rect.width;
        }
        this.calculateChartData();
    }

    /**
     * canvas click
     * @param e MouseEvent
     */
    private onMouseClick(e: MouseEvent): void {
        if (e.button == 0) { // mouse left button
            if (ChartStruct.hoverFuncStruct && ChartStruct.hoverFuncStruct != ChartStruct.selectFuncStruct) {
                ChartStruct.selectFuncStruct = ChartStruct.hoverFuncStruct;
                this.historyList.push(this.currentData!);
                let selectData = new Array<ChartStruct>();
                selectData.push(ChartStruct.selectFuncStruct!);
                // reset scale and translation
                this.rect.width = this.canvas!.clientWidth;
                this.xPoint = 0;
                this.redrawChart(selectData);
            }
        } else if (e.button == 2) { // mouse right button
            ChartStruct.selectFuncStruct = undefined;
            if (this.historyList.length > 0) {
                // reset scale and translation
                this.rect.width = this.canvas!.clientWidth;
                this.xPoint = 0;
                this.redrawChart(this.historyList.pop()!);
            }
        }
    }

    /**
     * mouse on canvas move event
     */
    private onMouseMove(): void {
        let lastNode = ChartStruct.hoverFuncStruct;
        let searchResult = this.searchData(this.currentData!, this.canvasX, this.canvasY);
        if (searchResult) {
            ChartStruct.hoverFuncStruct = searchResult;
            // judge current node is hover redraw chart
            if (searchResult != lastNode) {
                let name = ChartStruct.hoverFuncStruct?.symbol;
                if (this.mode == ChartMode.Byte) {
                    let size = Utils.getByteWithUnit(ChartStruct.hoverFuncStruct!.size);
                    this.hintContent = `<span>Name: ${name} </span><span>Size: ${size}</span>`;
                } else {
                    let count = ChartStruct.hoverFuncStruct!.count;
                    this.hintContent = `<span>Name: ${name} </span><span>Count: ${count}</span>`;
                }
                this.calculateChartData();
            }
            // pervent float hint trigger onmousemove event.
            this.updateFloatHint();
        } else {
            if (this.floatHint) {
                this.floatHint.style.display = 'none';
            }
        }
    }

    initElements(): void {
        this.canvas = this.shadowRoot?.querySelector("#canvas");
        this.cavasContext = this.canvas?.getContext("2d");
        this.floatHint = this.shadowRoot?.querySelector('#float_hint');

        this.canvas!.oncontextmenu = () => {
            return false
        };
        this.canvas!.onmouseup = (e) => this.onMouseClick(e);

        this.canvas!.onmousemove = (e) => {
            this.canvasX = e.clientX - this.startX;
            this.canvasY = e.clientY - this.startY + this.canvasScrollTop;
            this.isFocusing = true;
            this.onMouseMove();
        };

        this.canvas!.onmouseleave = () => {
            ChartStruct.selectFuncStruct = undefined;
            this.isFocusing = false;
            if (this.floatHint) {
                this.floatHint.style.display = 'none';
            }
        };
        let lastPressTime = 0;
        document.addEventListener('keydown', (e) => {
            if (!this.isFocusing) return;
            if (Date.now() - lastPressTime < 100) return;
            lastPressTime = Date.now();
            switch (e.key.toLocaleLowerCase()) {
                case 'w':
                    this.scale(1);
                    break;
                case 's':
                    this.scale(-1);
                    break;
                case 'a':
                    this.translation(-1);
                    break;
                case 'd':
                    this.translation(1);
                    break;
            }
        })
    }

    initHtml(): string {
        return `
            <style>
            :host{
                display: flex;
                padding: 10px 10px;
            }
            .tip{
                position:absolute;
                left: 0;
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
            </style>
            <canvas id="canvas"></canvas>
            <div id ="float_hint" class="tip">
            </div>
            `;
    }
}


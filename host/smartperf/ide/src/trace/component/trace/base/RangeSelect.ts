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

import {RangeSelectStruct, TraceRow} from "./TraceRow.js";
import {Rect} from "../timer-shaft/Rect.js";

export class RangeSelect {
    rowsEL: HTMLDivElement | undefined | null;
    isMouseDown: boolean = false;
    public rangeTraceRow: Array<TraceRow<any>> | undefined
    public selectHandler: ((ds: Array<TraceRow<any>>) => void) | undefined;
    private rowA: TraceRow<any> | undefined
    private rowB: TraceRow<any> | undefined
    private startX: number = 0
    private endX: number = 0;
    private startY: number = 0
    private endY: number = 0;

    constructor() {
    }

    printFrame(div: HTMLElement) {
        console.log("offset frame:", div.offsetLeft, div.offsetTop, div.offsetWidth, div.offsetHeight)
        console.log("client frame:", div.clientLeft, div.clientTop, div.clientWidth, div.clientHeight)
        console.log("scroll frame:", div.scrollLeft, div.scrollTop, div.scrollWidth, div.scrollHeight)
    }

    printEventFrame(ev: MouseEvent) {
        console.log("mouse event normal:", ev.x, ev.y)
        console.log("mouse event offset:", ev.offsetX, ev.offsetY)
        console.log("mouse event client:", ev.clientX, ev.clientY)
        console.log("mouse event page  :", ev.pageX, ev.pageY)
        console.log("mouse event movement:", ev.movementX, ev.movementY)
        console.log("mouse event screen :", ev.screenX, ev.screenY)
    }

    isInRowsEl(ev: MouseEvent): boolean {
        return (ev.offsetY > this.rowsEL!.offsetTop! &&
            ev.offsetY < this.rowsEL!.offsetTop + this.rowsEL!.offsetHeight &&
            ev.offsetX > this.rowsEL!.offsetLeft! &&
            ev.offsetX < this.rowsEL!.offsetLeft + this.rowsEL!.offsetWidth
        )
    }

    mouseDown(ev: MouseEvent) {
        this.rangeTraceRow = [];
        if (this.isInRowsEl(ev)) {
            this.isMouseDown = true;
            this.startX = ev.offsetX - this.rowsEL!.offsetLeft!;
            this.startY = ev.offsetY - this.rowsEL!.offsetTop!;
        }
    }

    mouseUp(ev: MouseEvent) {
        if (this.isInRowsEl(ev) && this.isDrag()) {
            this.endX = ev.offsetX - this.rowsEL!.offsetLeft!;
            this.endY = ev.offsetY - this.rowsEL!.clientTop! + this.rowsEL!.offsetTop!;
            if (this.selectHandler) {
                this.selectHandler(this.rangeTraceRow || [])
            }
        }
        this.isMouseDown = false;
    }

    isDrag(): boolean {
        return this.startX != this.endX && this.startY != this.endY;
    }

    mouseMove(rows: Array<TraceRow<any>>, ev: MouseEvent) {
        if (!this.isMouseDown) return;
        this.endX = ev.offsetX - this.rowsEL!.offsetLeft!;
        this.endY = ev.offsetY - this.rowsEL!.offsetTop!;
        let scrollTop = this.rowsEL?.scrollTop || 0
        let xMin = this.startX < this.endX ? this.startX : this.endX;
        let xMax = this.startX > this.endX ? this.startX : this.endX;
        let yMin = this.startY < this.endY ? this.startY : this.endY;
        let yMax = this.startY > this.endY ? this.startY : this.endY;
        let rangeSelect: RangeSelectStruct | undefined;
        this.rangeTraceRow = rows.filter(it => {
            let rt = new Rect(xMin - (it.canvasContainer?.offsetLeft || 0), yMin - (it.canvasContainer?.offsetTop || 0) + scrollTop + this.rowsEL!.offsetTop, xMax - xMin, yMax - yMin);
            if (Rect.intersect(it.frame, rt)) {
                if (!rangeSelect) {
                    rangeSelect = new RangeSelectStruct();
                    let startX = Math.floor(rt.x <= 0 ? 0 : rt.x);
                    let endX = Math.floor((rt.x + rt.width) > it.frame.width ? it.frame.width : (rt.x + rt.width));
                    rangeSelect.startNS = Math.floor((TraceRow.range!.endNS - TraceRow.range!.startNS) * startX / it.frame.width + TraceRow.range!.startNS!);
                    rangeSelect.endNS = Math.floor((TraceRow.range!.endNS - TraceRow.range!.startNS) * endX / it.frame.width + TraceRow.range!.startNS!);
                }
                TraceRow.rangeSelectObject = rangeSelect;
                it.rangeSelect = true;
                return true
            } else {
                it.rangeSelect = false;
                return false;
            }
        })
    }
}

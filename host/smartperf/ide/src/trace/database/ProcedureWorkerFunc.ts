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

import {BaseStruct, ColorUtils, ns2x, Rect} from "./ProcedureWorkerCommon.js";

const padding = 1;
export function func(list: Array<any>, res: Set<any>, startNS: number, endNS: number, totalNS: number, frame: any) {
    res.clear();
    if (list) {
        for (let i = 0, len = list.length; i < len; i++) {
            let it = list[i];
            if ((it.startTs || 0) + (it.dur || 0) > (startNS || 0) && (it.startTs || 0) < (endNS || 0)) {
                FuncStruct.setFuncFrame(list[i], 10, startNS || 0, endNS || 0, totalNS || 0, frame)
                if (i > 0 && (list[i - 1].frame?.y || 0) == (list[i].frame?.y || 0) && ((list[i - 1].frame?.x || 0) == (list[i].frame?.x || 0) && (list[i - 1].frame?.width || 0) == (list[i].frame?.width || 0))) {
                    continue;
                } else {
                    res.add(list[i])
                }
            }
        }
    }
}

export class FuncStruct extends BaseStruct {
    argsetid: number | undefined // 53161
    depth: number | undefined // 0
    dur: number | undefined // 570000
    funName: string | undefined //"binder transaction"
    id: number | undefined // 92749
    is_main_thread: number | undefined // 0
    parent_id: number | undefined // null
    startTs: number | undefined // 9729867000
    threadName: string | undefined // "Thread-15"
    tid: number | undefined // 2785
    track_id: number | undefined // 414
    static hoverFuncStruct: FuncStruct | undefined;
    static selectFuncStruct: FuncStruct | undefined;

    static setFuncFrame(node: any, padding: number, startNS: number, endNS: number, totalNS: number, frame: any) {
        let x1: number, x2: number;
        if ((node.startTs || 0) < startNS) {
            x1 = 0;
        } else {
            x1 = ns2x((node.startTs || 0), startNS, endNS, totalNS, frame);
        }
        if ((node.startTs || 0) + (node.dur || 0) > endNS) {
            x2 = frame.width;
        } else {
            x2 = ns2x((node.startTs || 0) + (node.dur || 0), startNS, endNS, totalNS, frame);
        }
        let getV: number = x2 - x1 <= 1 ? 1 : x2 - x1;
        if (!node.frame) {
            node.frame = {};
        }
        node.frame.x = Math.floor(x1);
        node.frame.y = Math.ceil(frame.y + (node.depth || 0) * 20 + padding);
        node.frame.width = Math.floor(getV);
        node.frame.height = 20;
    }

    static draw(ctx: CanvasRenderingContext2D, data: FuncStruct) {
        if (data.frame) {
            if (data.dur == undefined || data.dur == null || data.dur == 0 || FuncStruct.isBinder(data)) {
            } else {
                let width = data.frame.width || 0;
                ctx.fillStyle = ColorUtils.FUNC_COLOR[data.depth || 0 % ColorUtils.FUNC_COLOR.length]
                let miniHeight = 20
                ctx.fillRect(data.frame.x, data.frame.y, data.frame.width, miniHeight - padding * 2)
                ctx.fillStyle = "#fff"
                if (data.frame.width > 10) {
                    FuncStruct.drawString(ctx, data.funName || '', 5, data.frame)
                }
                if (FuncStruct.isSelected(data)) {
                    ctx.strokeStyle = "#000"
                    ctx.lineWidth = 1
                    ctx.strokeRect(data.frame.x, data.frame.y, data.frame.width, miniHeight - padding * 2)
                }
            }
        }
    }

    static drawString(ctx: CanvasRenderingContext2D, str: string, textPadding: number, frame: Rect): boolean {
        let textMetrics = ctx.measureText(str);
        let charWidth = Math.round(textMetrics.width / str.length)
        if (textMetrics.width < frame.width - textPadding * 2) {
            let x2 = Math.floor(frame.width / 2 - textMetrics.width / 2 + frame.x + textPadding)
            ctx.fillText(str, x2, Math.floor(frame.y + frame.height / 2 + 2), frame.width - textPadding * 2)
            return true;
        }
        if (frame.width - textPadding * 2 > charWidth * 4) {
            let chatNum = (frame.width - textPadding * 2) / charWidth;
            let x1 = frame.x + textPadding
            ctx.fillText(str.substring(0, chatNum - 4) + '...', x1, Math.floor(frame.y + frame.height / 2 + 2), frame.width - textPadding * 2)
            return true;
        }
        return false;
    }

    static isSelected(data: FuncStruct): boolean {
        return (FuncStruct.selectFuncStruct != undefined &&
            FuncStruct.selectFuncStruct.startTs == data.startTs &&
            FuncStruct.selectFuncStruct.dur == data.dur &&
            FuncStruct.selectFuncStruct.funName == data.funName)
    }

    static isBinder(data: FuncStruct): boolean {
        if (data.funName != null &&
            (
                data.funName.toLowerCase().startsWith("binder transaction")
                || data.funName.toLowerCase().startsWith("binder async")
                || data.funName.toLowerCase().startsWith("binder reply")
            )
        ) {
            return true;
        } else {
            return false;
        }
    }
}


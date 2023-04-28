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

import { ColorUtils } from '../../component/trace/base/ColorUtils.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';
import {
    BaseStruct,
    isFrameContainPoint,
    ns2x,
    Rect,
    Render,
    RequestMessage,
} from './ProcedureWorkerCommon.js';

export class FuncRender extends Render {
    renderMainThread(
        req: {
            useCache: boolean;
            context: CanvasRenderingContext2D;
            type: string;
        },
        row: TraceRow<FuncStruct>
    ) {
        let list = row.dataList;
        let filter = row.dataListCache;
        func(
            list,
            filter,
            TraceRow.range!.startNS,
            TraceRow.range!.endNS,
            TraceRow.range!.totalNS,
            row.frame,
            req.useCache || !TraceRow.range!.refresh
        );
        req.context.beginPath();
        let find = false;
        for (let re of filter) {
            FuncStruct.draw(req.context, re);
            if (row.isHover) {
                if (re.dur == 0 || re.dur == null || re.dur == undefined) {
                    if (
                        re.frame &&
                        row.hoverX >= re.frame.x - 5 &&
                        row.hoverX <= re.frame.x + 5 &&
                        row.hoverY >= re.frame.y &&
                        row.hoverY <= re.frame.y + re.frame.height
                    ) {
                        FuncStruct.hoverFuncStruct = re;
                        find = true;
                    }
                } else {
                    if (
                        re.frame &&
                        isFrameContainPoint(re.frame, row.hoverX, row.hoverY)
                    ) {
                        FuncStruct.hoverFuncStruct = re;
                        find = true;
                    }
                }
            }
        }
        if (!find && row.isHover) FuncStruct.hoverFuncStruct = undefined;
        req.context.closePath();
    }

    render(req: RequestMessage, list: Array<any>, filter: Array<any>) {}
}

export function func(
    list: Array<any>,
    res: Array<any>,
    startNS: number,
    endNS: number,
    totalNS: number,
    frame: any,
    use: boolean
) {
    if (use && res.length > 0) {
        for (let i = 0, len = res.length; i < len; i++) {
            if (
                (res[i].startTs || 0) + (res[i].dur || 0) >= startNS &&
                (res[i].startTs || 0) <= endNS
            ) {
                FuncStruct.setFuncFrame(
                    res[i],
                    0,
                    startNS,
                    endNS,
                    totalNS,
                    frame
                );
            } else {
                res[i].frame = null;
            }
        }
        return;
    }
    res.length = 0;
    if (list) {
        let groups = list
            .filter(
                (it) =>
                    (it.startTs ?? 0) + (it.dur ?? 0) >= startNS &&
                    (it.startTs ?? 0) <= endNS
            )
            .map((it) => {
                FuncStruct.setFuncFrame(it, 0, startNS, endNS, totalNS, frame);
                return it;
            })
            .reduce((pre, current, index, arr) => {
                (pre[`${current.frame.x}-${current.depth}`] =
                    pre[`${current.frame.x}-${current.depth}`] || []).push(
                    current
                );
                return pre;
            }, {});
        Reflect.ownKeys(groups).map((kv) => {
            let arr = groups[kv].sort((a: any, b: any) => b.dur - a.dur);
            res.push(arr[0]);
        });
    }
}

export class FuncStruct extends BaseStruct {
    static hoverFuncStruct: FuncStruct | undefined;
    static selectFuncStruct: FuncStruct | undefined;
    argsetid: number | undefined; // 53161
    depth: number | undefined; // 0
    dur: number | undefined; // 570000
    flag: string | undefined; // 570000
    funName: string | undefined; //"binder transaction"
    id: number | undefined; // 92749
    is_main_thread: number | undefined; // 0
    parent_id: number | undefined; // null
    startTs: number | undefined; // 9729867000
    threadName: string | undefined; // "Thread-15"
    tid: number | undefined; // 2785
    identify: number | undefined;
    track_id: number | undefined; // 414
    textMetricsWidth: number | undefined;

    static setFuncFrame(
        node: any,
        padding: number,
        startNS: number,
        endNS: number,
        totalNS: number,
        frame: any
    ) {
        let x1: number, x2: number;
        if ((node.startTs || 0) > startNS && (node.startTs || 0) < endNS) {
            x1 = ns2x(node.startTs || 0, startNS, endNS, totalNS, frame);
        } else {
            x1 = 0;
        }
        if (
            (node.startTs || 0) + (node.dur || 0) > startNS &&
            (node.startTs || 0) + (node.dur || 0) < endNS
        ) {
            x2 = ns2x(
                (node.startTs || 0) + (node.dur || 0),
                startNS,
                endNS,
                totalNS,
                frame
            );
        } else {
            x2 = frame.width;
        }
        if (!node.frame) {
            node.frame = {};
        }
        let getV: number = x2 - x1 < 1 ? 1 : x2 - x1;
        node.frame.x = Math.floor(x1);
        node.frame.y = node.depth * 20;
        node.frame.width = Math.ceil(getV);
        node.frame.height = 20;
    }

    static draw(ctx: CanvasRenderingContext2D, data: FuncStruct) {
        if (data.frame) {
            let isBinder = FuncStruct.isBinder(data);
            if (data.dur == undefined || data.dur == null) {
            } else {
                ctx.globalAlpha = 1;
                ctx.fillStyle =
                    ColorUtils.FUNC_COLOR[
                        ColorUtils.hashFunc(
                            data.funName || '',
                            0,
                            ColorUtils.FUNC_COLOR.length
                        )
                    ];
                let textColor =
                    ColorUtils.FUNC_COLOR[
                        ColorUtils.hashFunc(
                            data.funName || '',
                            0,
                            ColorUtils.FUNC_COLOR.length
                        )
                    ];
                let miniHeight = 20;
                if (
                    FuncStruct.hoverFuncStruct &&
                    data.funName == FuncStruct.hoverFuncStruct.funName
                ) {
                    ctx.globalAlpha = 0.7;
                }
                ctx.fillRect(
                    data.frame.x,
                    data.frame.y,
                    data.frame.width,
                    miniHeight - padding * 2
                );
                if (data.frame.width > 10) {
                    ctx.strokeStyle = '#fff';
                    ctx.lineWidth = 1;
                    ctx.strokeRect(
                        data.frame.x,
                        data.frame.y,
                        data.frame.width,
                        miniHeight - padding * 2
                    );
                    ctx.fillStyle = ColorUtils.funcTextColor(textColor);
                    FuncStruct.drawString(
                        ctx,
                        `${data.funName || ''}`,
                        5,
                        data.frame,
                        data
                    );
                }
                if (FuncStruct.isSelected(data)) {
                    ctx.strokeStyle = '#000';
                    ctx.lineWidth = 2;
                    ctx.strokeRect(
                        data.frame.x,
                        data.frame.y + 1,
                        data.frame.width,
                        miniHeight - padding * 2 - 2
                    );
                }
            }
        }
    }

    static drawString(
        ctx: CanvasRenderingContext2D,
        str: string,
        textPadding: number,
        frame: Rect,
        func:FuncStruct
    ): boolean {
        if(func.textMetricsWidth === undefined){
            func.textMetricsWidth = ctx.measureText(str).width;
        }
        let charWidth = Math.round(func.textMetricsWidth / str.length);
        let fillTextWidth = frame.width - textPadding * 2;
        if (func.textMetricsWidth < fillTextWidth) {
            let x2 = Math.floor(
                frame.width / 2 -
                func.textMetricsWidth / 2 +
                frame.x +
                textPadding
            );
            ctx.fillText(
                str,
                x2,
                Math.floor(frame.y + frame.height / 2 + 2),
                fillTextWidth
            );
            return true;
        } else {
            if (fillTextWidth >= charWidth) {
                let chatNum = fillTextWidth / charWidth;
                let x1 = frame.x + textPadding;
                if (chatNum < 2) {
                    ctx.fillText(
                        str.substring(0, 1),
                        x1,
                        Math.floor(frame.y + frame.height / 2 + 2),
                        fillTextWidth
                    );
                } else {
                    ctx.fillText(
                        str.substring(0, chatNum - 1) + '...',
                        x1, Math.floor(frame.y + frame.height / 2 + 2),
                        fillTextWidth
                    );
                }
                return true;
            }
        }
        return false;
    }

    static isSelected(data: FuncStruct): boolean {
        return (
            FuncStruct.selectFuncStruct != undefined &&
            FuncStruct.selectFuncStruct.startTs == data.startTs &&
            FuncStruct.selectFuncStruct.depth == data.depth
        );
    }

    static isBinder(data: FuncStruct): boolean {
        if (
            data.funName != null &&
            (data.funName.toLowerCase().startsWith('binder transaction') ||
                data.funName.toLowerCase().startsWith('binder async') ||
                data.funName.toLowerCase().startsWith('binder reply'))
        ) {
            return true;
        } else {
            return false;
        }
    }

    static isBinderAsync(data: FuncStruct): boolean {
        if (
            data.funName != null &&
            data.funName.toLowerCase().includes('async')
        ) {
            return true;
        } else {
            return false;
        }
    }
}

const padding = 1;

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

import {ColorUtils} from "../../component/trace/base/ColorUtils.js";
import {TraceRow} from "../../component/trace/base/TraceRow.js";
import {BaseStruct, isFrameContainPoint, ns2x, Rect, Render, RequestMessage} from "./ProcedureWorkerCommon.js";

export class JankRender extends Render {
    renderMainThread(req: { useCache: boolean; context: CanvasRenderingContext2D; type: string }, row: TraceRow<JankStruct>) {
        let list = row.dataList;
        let filter = row.dataListCache;
        jank(list, filter, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS, row.frame, req.useCache || !TraceRow.range!.refresh);
        req.context.beginPath();
        let find = false;
        let nsScale = ((TraceRow.range!.endNS || 0) - (TraceRow.range!.startNS || 0)) / ( TraceRow.range!.totalNS * 9);
        for (let re of filter) {
            JankStruct.draw(req.context, re, nsScale)
            if (row.isHover) {
                if (re.dur == 0 || re.dur == null || re.dur == undefined) {
                    if (re.frame && row.hoverX >= re.frame.x - 5 && row.hoverX <= re.frame.x + 5 && row.hoverY >= re.frame.y && row.hoverY <= re.frame.y + re.frame.height) {
                        JankStruct.hoverJankStruct = re;
                        find = true;
                    }
                } else {
                    if (re.frame && isFrameContainPoint(re.frame, row.hoverX, row.hoverY)) {
                        JankStruct.hoverJankStruct = re;
                        find = true;
                    }
                }
            }
        }
        if (!find && row.isHover) JankStruct.hoverJankStruct = undefined;
        req.context.closePath();
    }

    render(req: RequestMessage, list: Array<any>, filter: Array<any>) {

    }
}

export function jank(list: Array<any>, res: Array<any>, startNS: number, endNS: number, totalNS: number, frame: any, use: boolean) {
    if (use && res.length > 0) {
        for (let i = 0, len = res.length; i < len; i++) {
            if ((res[i].ts || 0) + (res[i].dur || 0) >= startNS && (res[i].ts || 0) <= endNS) {
                JankStruct.setJankFrame(res[i], 0, startNS, endNS, totalNS, frame)
            } else {
                res[i].frame = null;
            }
        }
        return;
    }
    res.length = 0;
    if (list) {
        let groups = list.filter(it => (it.ts ?? 0) + (it.dur ?? 0) >= startNS && (it.ts ?? 0) <= endNS).map(it => {
            JankStruct.setJankFrame(it, 0, startNS, endNS, totalNS, frame)
            return it;
        }).reduce((pre, current, index, arr) => {
            (pre[`${current.frame.x}-${current.depth}`] = pre[`${current.frame.x}-${current.depth}`] || []).push(current);
            return pre;
        }, {});
        Reflect.ownKeys(groups).map((kv => {
            let arr = (groups[kv].sort((a: any, b: any) => b.dur - a.dur));
            res.push(arr[0]);
        }));
    }
}

export class JankStruct extends BaseStruct {
    static hoverJankStruct: JankStruct | undefined;
    static selectJankStruct: JankStruct | undefined;
    static selectJankStructList: Array<JankStruct> = new Array<JankStruct>();
    static delJankLineFlag: boolean = true;
    id: number | undefined // sliceid
    ts: number | undefined
    dur: number | undefined
    name: string | undefined
    depth: number | undefined
    jank_tag: boolean = false
    cmdline: string | undefined // process
    jank_type: string | undefined
    type: string | undefined
    pid: number | undefined
    frame_type: string | undefined; // app、renderService、frameTime
    app_dur: number | undefined;
    src_slice: string | undefined
    dst_slice: string | undefined
    rs_ts: number | undefined;
    rs_vsync: string | undefined
    rs_dur: number | undefined;
    rs_pid: number | undefined;
    rs_name: string |undefined
    gpu_dur:number | undefined;

    static setJankFrame(node: any, padding: number, startNS: number, endNS: number, totalNS: number, frame: any) {
        let x1: number, x2: number;
        if ((node.ts || 0) > startNS && (node.ts || 0) < endNS) {
            x1 = ns2x((node.ts || 0), startNS, endNS, totalNS, frame);
        } else {
            x1 = 0;
        }
        if ((node.ts || 0) + (node.dur || 0) > startNS && (node.ts || 0) + (node.dur || 0) < endNS) {
            x2 = ns2x((node.ts || 0) + (node.dur || 0), startNS, endNS, totalNS, frame);
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

    static draw(ctx: CanvasRenderingContext2D, data: JankStruct, nsScale: number) {
        if (data.frame) {
            if (data.dur == undefined || data.dur == null || data.dur == 0) {
            } else {
                ctx.globalAlpha = 1;
                ctx.fillStyle = ColorUtils.JANK_COLOR[0];
                if(data.jank_tag){
                    ctx.fillStyle = ColorUtils.JANK_COLOR[2];
                }
                let miniHeight = 20
                if (JankStruct.hoverJankStruct && data.name == JankStruct.hoverJankStruct.name
                    && JankStruct.hoverJankStruct.type == data.type && JankStruct.hoverJankStruct.pid == data.pid
                    && JankStruct.hoverJankStruct.frame_type == data.frame_type) {
                    ctx.globalAlpha = 0.7;
                }
                if (data.type == '0') {
                    ctx.fillStyle = ColorUtils.JANK_COLOR[0];
                    if(data.jank_tag){
                        ctx.fillStyle = ColorUtils.JANK_COLOR[2];
                    }
                    ctx.fillRect(data.frame.x, data.frame.y, data.frame.width, miniHeight - padding * 2);
                } else {
                    if(data.frame.width * nsScale < 1.5){
                        ctx.fillStyle = '#FFFFFF';
                        ctx.fillRect(data.frame.x, data.frame.y, data.frame.width * nsScale, miniHeight - padding * 2);
                        ctx.fillStyle = ColorUtils.JANK_COLOR[0];
                        if(data.jank_tag){
                            ctx.fillStyle = ColorUtils.JANK_COLOR[2];
                        }
                        ctx.fillRect(data.frame.x + data.frame.width * nsScale, data.frame.y, data.frame.width - (nsScale * 2), miniHeight - padding * 2);
                        ctx.fillStyle = '#FFFFFF'
                        ctx.fillRect(data.frame.x + data.frame.width * nsScale + data.frame.width - (nsScale * 2), data.frame.y, data.frame.width * nsScale, miniHeight - padding * 2);


                    } else {
                        ctx.fillStyle = ColorUtils.JANK_COLOR[0];
                        if(data.jank_tag){
                            ctx.fillStyle = ColorUtils.JANK_COLOR[2];
                        }
                        ctx.fillRect(data.frame.x, data.frame.y, data.frame.width, miniHeight - padding * 2);
                    }
                }

                if (data.frame.width > 10) {
                    ctx.fillStyle = "#fff"
                    JankStruct.drawString(ctx, `${data.name || ''}`, 5, data.frame)
                }
                if (JankStruct.isSelected(data)) {
                    ctx.strokeStyle = "#000"
                    ctx.lineWidth = 2
                    ctx.strokeRect(data.frame.x, data.frame.y + 1, data.frame.width, miniHeight - padding * 2 - 2)
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

    static isSelected(data: JankStruct): boolean {
        return (JankStruct.selectJankStruct != undefined &&
            JankStruct.selectJankStruct.ts == data.ts &&
            JankStruct.selectJankStruct.depth == data.depth
            && JankStruct.selectJankStruct.type == data.type
            && JankStruct.selectJankStruct.pid == data.pid
            && JankStruct.selectJankStruct.frame_type == data.frame_type)
    }

    static getStyleColor(jankTag: string | undefined){
        switch (jankTag){
            case 'No Jank' : return ColorUtils.JANK_COLOR[0];
                break;
            case 'Self Jank' : return ColorUtils.JANK_COLOR[1];
                break;
            case 'Other Jank' : return ColorUtils.JANK_COLOR[2];
                break;
            case 'Dropped Frame' : return ColorUtils.JANK_COLOR[3];
                break;
            case 'Buffer Stuffing' : return ColorUtils.JANK_COLOR[4];
                break;
            case 'SurfaceFlinger Stuffing' : return ColorUtils.JANK_COLOR[4];
                break;
            default: return ColorUtils.JANK_COLOR[5];
                break;
        }
    }
}

const padding = 1;

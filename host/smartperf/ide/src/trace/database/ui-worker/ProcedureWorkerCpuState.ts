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

import {
    BaseStruct,
    drawFlagLine,
    drawLines,
    drawLoading, drawSelection,
    drawWakeUp, ns2x, PerfRender, Render,
    RequestMessage
} from "./ProcedureWorkerCommon.js";
import {TraceRow} from "../../component/trace/base/TraceRow.js";
import {ColorUtils} from "../../component/trace/base/ColorUtils.js";
import {convertJSON} from "../logic-worker/ProcedureLogicWorkerCommon.js";

export class CpuStateRender extends PerfRender {

    renderMainThread(req: { useCache: boolean; context: CanvasRenderingContext2D; type: string; cpu: number }, row: TraceRow<CpuStateStruct>) {
        let list = row.dataList = convertJSON(row.dataList);
        let filter = row.dataListCache;
        let chartColor = ColorUtils.colorForTid(req.cpu);
        if(list && row.dataList2.length == 0){
            row.dataList2 = this.getList(list,TraceRow.range!.endNS,req.cpu)
        }
        this.cpuState(list, row.dataList2, req.type!, filter, req.cpu, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS, row.frame, req.useCache || !TraceRow.range!.refresh);
        req.context.beginPath();
        req.context.font = "11px sans-serif";
        req.context.fillStyle = chartColor;
        req.context.strokeStyle = chartColor;
        req.context.globalAlpha = 0.6;
        let path = new Path2D();
        let find = false;
        let offset = 3;
        for (let re of filter) {
            CpuStateStruct.draw(req.context, path, re);
            if (row.isHover) {
                if (re.frame && row.hoverX >= re.frame.x - offset && row.hoverX <= re.frame.x + re.frame.width + offset) {
                    CpuStateStruct.hoverStateStruct = re;
                    find = true;
                }
            }
        }
        if (!find && row.isHover) CpuStateStruct.hoverStateStruct = undefined;
        req.context.fill(path);
    }

    render(req: RequestMessage, list: Array<any>, filter: Array<any>, dataList2: Array<any>) {
        if (req.lazyRefresh) {
            this.cpuState(list, dataList2, req.type!, filter, req.params.cpu, req.startNS, req.endNS, req.totalNS, req.frame, req.useCache || !req.range.refresh);
        } else {
            if (!req.useCache) {
                this.cpuState(list, dataList2, req.type!, filter, req.params.cpu, req.startNS, req.endNS, req.totalNS, req.frame, false);
            }
        }
        CpuStateStruct.hoverStateStruct = undefined;
        if (req.canvas) {
            req.context.clearRect(0, 0, req.frame.width, req.frame.height);
            if (filter.length > 0 && !req.range.refresh && !req.useCache && req.lazyRefresh) {
                drawLoading(req.context, req.startNS, req.endNS, req.totalNS, req.frame, filter[0].startTs, filter[filter.length - 1].startTs + filter[filter.length - 1].dur)
            }
            req.context.beginPath();
            drawLines(req.context, req.xs, req.frame.height, req.lineColor);
            if (req.isHover) {
                let offset = 3;
                for (let re of filter) {
                    if (re.frame && req.hoverX >= re.frame.x - offset && req.hoverX <= re.frame.x + re.frame.width + offset) {
                        CpuStateStruct.hoverStateStruct = re;
                        break;
                    }
                }
            }
            CpuStateStruct.selectStateStruct = req.params.selectStateStruct;
            req.context.font = "11px sans-serif";
            req.context.fillStyle = req.chartColor;
            req.context.strokeStyle = req.chartColor;
            req.context.globalAlpha = 0.6;
            let path = new Path2D();
            for (let re of filter) {
                CpuStateStruct.draw(req.context, path, re);
            }
            req.context.fill(path);
            drawSelection(req.context, req.params);
            drawWakeUp(req.context, req.wakeupBean, req.startNS, req.endNS, req.totalNS, req.frame);
            drawFlagLine(req.context, req.flagMoveInfo, req.flagSelectedInfo, req.startNS, req.endNS, req.totalNS, req.frame, req.slicesTime);
        }
        let msg = {
            id: req.id,
            type: req.type,
            results: req.canvas ? undefined : filter,
            hover: CpuStateStruct.hoverStateStruct
        }
        self.postMessage(msg);
    }

    cpuState(arr: Array<any>, arr2: Array<any>, type: string, res: Array<any>, cpu: number, startNS: number, endNS: number, totalNS: number, frame: any, use: boolean) {
        if (use && res.length > 0) {
            for (let i = 0, len = res.length; i < len; i++) {
                if ((res[i].startTs || 0) + (res[i].dur || 0) >= startNS && (res[i].startTs || 0) <= endNS) {
                    CpuStateStruct.setFrame(res[i], 5, startNS, endNS, totalNS, frame)
                } else {
                    res[i].frame = null;
                }
            }
            return;
        }
        res.length = 0;
        if (arr) {
            let list: Array<any> = arr2;
            res.length = 0;
            let pns = (endNS - startNS) / frame.width;//每个像素多少ns
            let y = frame.y + 5;
            let height = frame.height - 10;
            let left = 0, right = 0;
            for (let i = 0, j = list.length - 1, ib = true, jb = true; i < list.length, j >= 0; i++, j--) {
                if (list[j].startTs <= endNS && jb) {
                    right = j;
                    jb = false;
                }
                if (list[i].startTs + list[i].dur >= startNS && ib) {
                    left = i;
                    ib = false;
                }
                if (!ib && !jb) {
                    break;
                }
            }
            let slice = list.slice(left, right + 1);
            let sum = 0;
            for (let i = 0; i < slice.length; i++) {
                if (!slice[i].frame) {
                    slice[i].frame = {};
                    slice[i].frame.y = y;
                    slice[i].frame.height = height;
                }
                if (slice[i].dur >= pns) {
                    slice[i].v = true;
                    CpuStateStruct.setFrame(slice[i], 5, startNS, endNS, totalNS, frame)
                } else {
                    if (i > 0) {
                        let c = slice[i].startTs - slice[i - 1].startTs - slice[i - 1].dur
                        if (c < pns && sum < pns) {
                            sum += c + slice[i - 1].dur;
                            slice[i].v = false;
                        } else {
                            slice[i].v = true;
                            CpuStateStruct.setFrame(slice[i], 5, startNS, endNS, totalNS, frame)
                            sum = 0;
                        }
                    }
                }
            }
            res.push(...slice.filter(it => it.v));
        }
    }

    getList(arr: Array<any>, endNS: number, cpu: number): Array<any> {
        let heights = [4, 12, 21, 30]
        for (let i = 0, len = arr.length; i < len; i++) {
            let it = arr[i];
            it.height = heights[it.value]
            it.cpu = cpu;
        }
        return arr;
    }

}

export class CpuStateStruct extends BaseStruct {
    static hoverStateStruct: CpuStateStruct | undefined;
    static selectStateStruct: CpuStateStruct | undefined;
    dur: number | undefined
    value: string | undefined
    startTs: number | undefined
    height: number | undefined
    cpu: number | undefined

    static draw(ctx: CanvasRenderingContext2D, path: Path2D, data: CpuStateStruct) {
        if (data.frame) {
            if (data.startTs === CpuStateStruct.hoverStateStruct?.startTs || data.startTs === CpuStateStruct.selectStateStruct?.startTs) {
                path.rect(data.frame.x, 35 - (data.height || 0), data.frame.width, data.height || 0)
                ctx.lineWidth = 1;
                ctx.globalAlpha = 1.0;
                ctx.beginPath()
                ctx.arc(data.frame.x, 35 - (data.height || 0), 3, 0, 2 * Math.PI, true)
                ctx.stroke()
                ctx.beginPath()
                ctx.moveTo(data.frame.x + 3, 35 - (data.height || 0));
                ctx.lineWidth = 3;
                ctx.lineTo(data.frame.x + data.frame.width, 35 - (data.height || 0))
                ctx.stroke();
                ctx.lineWidth = 1;
                ctx.globalAlpha = 0.6;
                ctx.fillRect(data.frame.x, 35 - (data.height || 0), data.frame.width, data.height || 0)
            } else {
                ctx.globalAlpha = 0.6;
                path.rect(data.frame.x, 35 - (data.height || 0), data.frame.width, data.height || 0)
            }
        }
    }

    static setCpuFrame(node: any, pns: number, startNS: number, endNS: number, frame: any) {
        if ((node.startTime || 0) < startNS) {
            node.frame.x = 0;
        } else {
            node.frame.x = Math.floor(((node.startTs || 0) - startNS) / pns);
        }
        if ((node.startTime || 0) + (node.dur || 0) > endNS) {
            node.frame.width = frame.width - node.frame.x;
        } else {
            node.frame.width = Math.ceil(((node.startTs || 0) + (node.dur || 0) - startNS) / pns - node.frame.x);
        }
        if (node.frame.width < 1) {
            node.frame.width = 1;
        }
    }
    static setFrame(node: any, padding: number, startNS: number, endNS: number, totalNS: number, frame: any) {
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
        node.frame.x = Math.ceil(x1);
        node.frame.y = frame.y + padding;
        node.frame.width = Math.floor(getV);
        node.frame.height = node.height;
    }
}

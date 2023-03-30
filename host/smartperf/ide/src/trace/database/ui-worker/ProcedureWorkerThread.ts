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
    BaseStruct, dataFilterHandler,
    isFrameContainPoint,
    Rect, Render,
    RequestMessage
} from "./ProcedureWorkerCommon.js";
import {TraceRow} from "../../component/trace/base/TraceRow.js";

export class ThreadRender extends Render {
    renderMainThread(req: {
        context: CanvasRenderingContext2D,
        useCache: boolean,
        type: string,
        translateY:number
    }, row: TraceRow<ThreadStruct>) {
        let list = row.dataList;
        let filter = row.dataListCache;
        // thread(list, filter, TraceRow.range!.startNS, TraceRow.range!.endNS, TraceRow.range!.totalNS, row.frame, req.useCache || !(TraceRow.range!.refresh));
        dataFilterHandler(list,filter,{
            startKey: "startTime",
            durKey: "dur",
            startNS: TraceRow.range?.startNS ?? 0,
            endNS: TraceRow.range?.endNS ?? 0,
            totalNS: TraceRow.range?.totalNS ?? 0,
            frame: row.frame,
            paddingTop: 5,
            useCache: req.useCache || !(TraceRow.range?.refresh ?? false)
        })
        req.context.beginPath();
        for (let re of filter) {
            re.translateY = req.translateY;
            ThreadStruct.draw(req.context, re)
            if (row.isHover && re.frame && isFrameContainPoint(re.frame!,row.hoverX,row.hoverY)) {
                ThreadStruct.hoverThreadStruct = re;
            }
        }
        req.context.closePath();
    }

    render(req: RequestMessage, list: Array<any>, filter: Array<any>) {

    }
}


const padding = 3;

export class ThreadStruct extends BaseStruct {
    static runningColor: string = "#467b3b";
    static rColor = "#a0b84d";
    static otherColor = "#673ab7";
    static uninterruptibleSleepColor = "#f19d38";
    static traceColor = "#0d47a1";
    static sColor = "#FBFBFB";
    static hoverThreadStruct: ThreadStruct | undefined;
    static selectThreadStruct: ThreadStruct | undefined;
    static statusMap: any = {
        "D": "Uninterruptible Sleep",
        "S": "Sleeping",
        "R": "Runnable",
        "Running": "Running",
        "R+": "Runnable (Preempted)",
        "DK": "Uninterruptible Sleep + Wake Kill",
        "I": "Task Dead",
        "T": "Traced",
        "t": "Traced",
        "X": "Exit (Dead)",
        "Z": "Exit (Zombie)",
        "K": "Wake Kill",
        "W": "Waking",
        "P": "Parked",
        "N": "No Load"
    }
    hasSched: number | undefined;// 14724852000
    pid: number | undefined// 2519
    processName: string | undefined //null
    threadName: string | undefined//"ACCS0"
    tid: number | undefined //2716
    upid: number | undefined // 1
    utid: number | undefined // 1
    cpu: number | undefined // null
    dur: number | undefined // 405000
    end_ts: number | undefined // null
    id: number | undefined // 1
    is_main_thread: number | undefined // 0
    name: string | undefined // "ACCS0"
    startTime: number | undefined // 58000
    start_ts: number | undefined // null
    state: string | undefined // "S"
    type: string | undefined // "thread"

    static draw(ctx: CanvasRenderingContext2D, data: ThreadStruct) {
        if (data.frame) {
            ctx.globalAlpha = 1
            let stateText = data.state || '';
            if ("S" == data.state) {
                ctx.fillStyle = ThreadStruct.sColor;
                ctx.globalAlpha = 0.2; // transparency
                ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2)
                ctx.globalAlpha = 1; // transparency
            } else if ("R" == data.state || "R+" == data.state) {
                ctx.fillStyle = ThreadStruct.rColor;
                ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2)
                ctx.fillStyle = "#fff";
                data.frame.width > 4 && ThreadStruct.drawString(ctx, ThreadStruct.getEndState(data.state || ''), 2, data.frame);
            } else if ("D" == data.state) {
                ctx.fillStyle = ThreadStruct.uninterruptibleSleepColor;
                ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2)
                ctx.fillStyle = "#fff";
                data.frame.width > 4 && ThreadStruct.drawString(ctx, ThreadStruct.getEndState(data.state || ''), 2, data.frame);
            } else if ("Running" == data.state) {
                ctx.fillStyle = ThreadStruct.runningColor;
                ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2)
                ctx.fillStyle = "#fff";
                data.frame.width > 4 && ThreadStruct.drawString(ctx, ThreadStruct.getEndState(data.state || ''), 2, data.frame);
            } else if ("T" == data.state || "t" == data.state) {
                ctx.fillStyle = ThreadStruct.traceColor;
                ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2)
                ctx.fillStyle = "#fff";
                ThreadStruct.drawString(ctx, ThreadStruct.getEndState(data.state || ''), 2, data.frame);
            } else {
                ctx.fillStyle = ThreadStruct.otherColor;
                ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2)
                ctx.fillStyle = "#fff";
                data.frame.width > 4 && ThreadStruct.drawString(ctx, ThreadStruct.getEndState(data.state || ''), 2, data.frame);
            }
            if (ThreadStruct.selectThreadStruct && ThreadStruct.equals(ThreadStruct.selectThreadStruct, data) && ThreadStruct.selectThreadStruct.state != "S") {
                ctx.strokeStyle = '#232c5d'
                ctx.lineWidth = 2
                ctx.strokeRect(data.frame.x, data.frame.y + padding, data.frame.width - 2, data.frame.height - padding * 2)
            }
        }
    }

    static drawString(ctx: CanvasRenderingContext2D, str: string, textPadding: number, frame: Rect) {
        let textMetrics = ctx.measureText(str);
        let charWidth = Math.round(textMetrics.width / str.length)
        if (textMetrics.width < frame.width - textPadding * 2) {
            let x2 = Math.floor(frame.width / 2 - textMetrics.width / 2 + frame.x + textPadding)
            ctx.textBaseline = "middle"
            ctx.font = "8px sans-serif";
            ctx.fillText(str, x2, Math.floor(frame.y + frame.height / 2), frame.width - textPadding * 2)
            return;
        }
        if (frame.width - textPadding * 2 > charWidth * 4) {
            let chatNum = (frame.width - textPadding * 2) / charWidth;
            let x1 = frame.x + textPadding
            ctx.textBaseline = "middle"
            ctx.font = "8px sans-serif";
            ctx.fillText(str.substring(0, chatNum - 4) + '...', x1, Math.floor(frame.y + frame.height / 2), frame.width - textPadding * 2)
            return;
        }
    }

    static getEndState(state: string): string {
        let statusMapElement = ThreadStruct.statusMap[state];
        if (statusMapElement) {
            return statusMapElement
        } else {
            if ("" == statusMapElement || statusMapElement == null) {
                return "";
            }
            return "Unknown State";
        }
    }

    static equals(d1: ThreadStruct, d2: ThreadStruct): boolean {
        if (d1 && d2 && d1.cpu == d2.cpu &&
            d1.tid == d2.tid &&
            d1.state == d2.state &&
            d1.startTime == d2.startTime &&
            d1.dur == d2.dur) {
            return true;
        } else {
            return false;
        }
    }
}
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

import {BaseStruct, ns2x, Rect} from "./ProcedureWorkerCommon.js";

const textPadding = 2;

export function fps(list: Array<any>, res: Set<any>, startNS: number, endNS: number, totalNS: number, frame: any) {
    res.clear();
    if (list) {
        for (let i = 0,len = list.length; i < len; i++) {
            let it = list[i];
            if ((it.fps || 0) > FpsStruct.maxFps) {
                FpsStruct.maxFps = it.fps || 0
            }
            if (i === list.length - 1) {
                it.dur = (endNS || 0) - (it.startNS || 0)
            } else {
                it.dur = (list[i + 1].startNS || 0) - (it.startNS || 0)
            }
            if ((it.startNS || 0) + (it.dur || 0) > (startNS) && (it.startNS || 0) < (endNS)) {
                FpsStruct.setFrame(list[i], 5, startNS, endNS, totalNS, frame)
                if (i > 0 && ((list[i - 1].frame?.x || 0) == (list[i].frame?.x || 0) && (list[i - 1].frame?.width || 0) == (list[i].frame?.width || 0))) {
                    continue;
                } else {
                    res.add(list[i])
                }
            }
        }
    }
}

export class FpsStruct extends BaseStruct{
    fps: number | undefined
    startNS: number | undefined = 0
    dur: number | undefined

    static maxFps: number = 0
    static maxFpsName: string = "0 FPS"
    static hoverFpsStruct:FpsStruct|undefined;
    static selectFpsStruct:FpsStruct|undefined;
    static draw(ctx: CanvasRenderingContext2D, data: FpsStruct) {
        if (data.frame) {
            let width = data.frame.width || 0;
            ctx.fillStyle= '#535da6'
            ctx.strokeStyle= '#535da6'
            if(data.startNS===FpsStruct.hoverFpsStruct?.startNS){
                ctx.lineWidth = 1;
                ctx.globalAlpha = 0.6;
                let drawHeight: number = ((data.fps || 0) * (data.frame.height || 0) * 1.0) / FpsStruct.maxFps;
                ctx.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight, width, drawHeight)
                ctx.beginPath()
                ctx.arc(data.frame.x,data.frame.y + data.frame.height - drawHeight,3,0,2*Math.PI,true)
                ctx.fill()
                ctx.globalAlpha = 1.0;
                ctx.stroke();
                ctx.beginPath()
                ctx.moveTo(data.frame.x+3,data.frame.y + data.frame.height - drawHeight);
                ctx.lineWidth = 3;
                ctx.lineTo(data.frame.x+width,data.frame.y + data.frame.height - drawHeight)
                ctx.stroke();
            }else{
                ctx.globalAlpha = 0.6;
                ctx.lineWidth = 1;
                let drawHeight: number = ((data.fps || 0) * (data.frame.height || 0) * 1.0) / FpsStruct.maxFps;
                ctx.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight, width, drawHeight)
            }
        }
        ctx.globalAlpha = 1.0;
        ctx.lineWidth = 1;
    }

    static setFrame(node: FpsStruct, padding: number, startNS: number, endNS: number, totalNS: number, frame: Rect) {
        let x1: number,x2:number;
        if ((node.startNS || 0) < startNS) {
            x1 = 0;
        } else {
            x1 = ns2x((node.startNS || 0), startNS, endNS, totalNS, frame);
        }
        if ((node.startNS || 0) + (node.dur || 0) > endNS) {
            x2 = frame.width;
        } else {
            x2 = ns2x((node.startNS || 0) + (node.dur || 0), startNS, endNS, totalNS, frame);
        }
        let getV: number = x2 - x1 <= 1 ? 1 : x2 - x1;
        let rectangle: Rect = new Rect(Math.floor(x1), Math.ceil(frame.y + padding), Math.ceil(getV), Math.floor(frame.height - padding * 2));
        node.frame = rectangle;
    }
}


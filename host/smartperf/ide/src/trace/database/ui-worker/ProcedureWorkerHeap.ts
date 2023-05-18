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
  dataFilterHandler,
  drawFlagLine,
  drawLines,
  drawLoading,
  drawSelection,
  drawWakeUp,
  isFrameContainPoint,
  ns2x,
  Rect,
  Render,
  RequestMessage,
} from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';
export class NativeMemoryRender extends Render {
  renderMainThread(req: any, row: TraceRow<any>) {}
}
export class HeapRender {
  renderMainThread(
    req: {
      context: CanvasRenderingContext2D;
      useCache: boolean;
      type: string;
    },
    row: TraceRow<HeapStruct>
  ) {
    let list = row.dataList;
    let filter = row.dataListCache;
    heap(
      list,
      filter,
      TraceRow.range?.startNS ?? 0,
      TraceRow.range?.endNS ?? 0,
      TraceRow.range?.totalNS ?? 0,
      row.frame,
      req.useCache || (TraceRow.range?.refresh ?? false)
    );
    req.context.beginPath();
    let find = false;
    for (let re of filter) {
      if (row.isHover && re.frame && !find && isFrameContainPoint(re.frame, row.hoverX, row.hoverY)) {
        HeapStruct.hoverHeapStruct = re;
        find = true;
      }
    }
    for (let re of filter) {
      HeapStruct.draw(req.context, re, row.drawType);
    }
    if (!find && row.isHover) HeapStruct.hoverHeapStruct = undefined;
    req.context.closePath();
  }

  render(req: RequestMessage, list: Array<any>, filter: Array<any>) {
    if (req.lazyRefresh) {
      heap(list, filter, req.startNS, req.endNS, req.totalNS, req.frame, req.useCache || !req.range.refresh);
    } else {
      if (!req.useCache) {
        heap(list, filter, req.startNS, req.endNS, req.totalNS, req.frame, false);
      }
    }
    if (req.canvas) {
      req.context.clearRect(0, 0, req.canvas.width, req.canvas.height);
      let arr = filter;
      if (arr.length > 0 && !req.range.refresh && !req.useCache && req.lazyRefresh) {
        drawLoading(
          req.context,
          req.startNS,
          req.endNS,
          req.totalNS,
          req.frame,
          arr[0].startTime,
          arr[arr.length - 1].startTime + arr[arr.length - 1].dur
        );
      }
      req.context.beginPath();
      drawLines(req.context, req.xs, req.frame.height, req.lineColor);
      HeapStruct.hoverHeapStruct = undefined;
      if (req.isHover) {
        for (let re of filter) {
          if (
            re.frame &&
            req.hoverX >= re.frame.x &&
            req.hoverX <= re.frame.x + re.frame.width &&
            req.hoverY >= re.frame.y &&
            req.hoverY <= re.frame.y + re.frame.height
          ) {
            HeapStruct.hoverHeapStruct = re;
            break;
          }
        }
      } else {
        HeapStruct.hoverHeapStruct = req.params.hoverHeapStruct;
      }
      for (let re of filter) {
        HeapStruct.draw(req.context, re, req.params.drawType);
      }
      drawSelection(req.context, req.params);
      drawWakeUp(req.context, req.wakeupBean, req.startNS, req.endNS, req.totalNS, req.frame);
      req.context.closePath();
      drawFlagLine(
        req.context,
        req.flagMoveInfo,
        req.flagSelectedInfo,
        req.startNS,
        req.endNS,
        req.totalNS,
        req.frame,
        req.slicesTime
      );
    }
    // @ts-ignore
    self.postMessage({
      id: req.id,
      type: req.type,
      results: req.canvas ? undefined : filter,
      hover: HeapStruct.hoverHeapStruct,
    });
  }
}
export function heap(
  list: Array<any>,
  res: Array<any>,
  startNS: number,
  endNS: number,
  totalNS: number,
  frame: any,
  use: boolean
) {
  if (use && res.length > 0) {
    for (let i = 0; i < res.length; i++) {
      let it = res[i];
      if ((it.startTime || 0) + (it.dur || 0) > (startNS || 0) && (it.startTime || 0) < (endNS || 0)) {
        HeapStruct.setFrame(res[i], 5, startNS || 0, endNS || 0, totalNS || 0, frame);
      } else {
        res[i].frame = null;
      }
    }
    return;
  }
  res.length = 0;
  for (let i = 0, len = list.length; i < len; i++) {
    let it = list[i];
    if ((it.startTime || 0) + (it.dur || 0) > (startNS || 0) && (it.startTime || 0) < (endNS || 0)) {
      HeapStruct.setFrame(it, 5, startNS || 0, endNS || 0, totalNS || 0, frame);
      if (i > 0) {
        let last = list[i - 1];
        if (last.frame?.x != it.frame.x || last.frame.width != it.frame.width) {
          res.push(it);
        }
      } else {
        res.push(it);
      }
    }
  }
}

export class HeapStruct extends BaseStruct {
  static hoverHeapStruct: HeapStruct | undefined;
  static selectHeapStruct: HeapStruct | undefined;
  startTime: number | undefined;
  endTime: number | undefined;
  dur: number | undefined;
  heapsize: number | undefined;
  density: number | undefined;
  maxHeapSize: number = 0;
  minHeapSize: number = 0;
  maxDensity: number = 0;
  minDensity: number = 0;

  static setFrame(node: HeapStruct, padding: number, startNS: number, endNS: number, totalNS: number, frame: Rect) {
    let x1: number, x2: number;
    if ((node.startTime || 0) < startNS) {
      x1 = 0;
    } else {
      x1 = ns2x(node.startTime || 0, startNS, endNS, totalNS, frame);
    }
    if ((node.startTime || 0) + (node.dur || 0) > endNS) {
      x2 = frame.width;
    } else {
      x2 = ns2x(
        // @ts-ignore
        node.startTime + node.dur,
        startNS,
        endNS,
        totalNS,
        frame
      );
    }
    let getV: number = x2 - x1 <= 1 ? 1 : x2 - x1;
    let rectangle: Rect = new Rect(
      Math.floor(x1),
      Math.ceil(frame.y + padding),
      Math.ceil(getV),
      Math.floor(frame.height - padding * 2)
    );
    node.frame = rectangle;
  }

  static draw(ctx: CanvasRenderingContext2D, data: HeapStruct, drawType: number) {
    if (data.frame) {
      let width = data.frame.width || 0;
      ctx.fillStyle = '#2db3aa';
      ctx.strokeStyle = '#2db3aa';
      let drawHeight: number = 0;
      if (drawType == 0) {
        if (data.minHeapSize < 0) {
          drawHeight = Math.ceil(
            (((data.heapsize || 0) - data.minHeapSize) * (data.frame.height || 0)) /
              (data.maxHeapSize - data.minHeapSize)
          );
        } else {
          drawHeight = Math.ceil(((data.heapsize || 0) * (data.frame.height || 0)) / data.maxHeapSize);
        }
      } else {
        if (data.minDensity < 0) {
          drawHeight = Math.ceil(
            (((data.density || 0) - data.minDensity) * (data.frame.height || 0)) / (data.maxDensity - data.minDensity)
          );
        } else {
          drawHeight = Math.ceil(((data.density || 0) * (data.frame.height || 0)) / data.maxDensity);
        }
      }
      if (data == HeapStruct.hoverHeapStruct || data == HeapStruct.selectHeapStruct) {
        ctx.lineWidth = 1;
        ctx.globalAlpha = 0.6;
        ctx.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight, width, drawHeight);
        ctx.beginPath();
        ctx.arc(data.frame.x, data.frame.y + data.frame.height - drawHeight, 3, 0, 2 * Math.PI, true);
        ctx.fill();
        ctx.globalAlpha = 1.0;
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(data.frame.x + 3, data.frame.y + data.frame.height - drawHeight);
        ctx.lineWidth = 3;
        ctx.lineTo(data.frame.x + width, data.frame.y + data.frame.height - drawHeight);
        ctx.stroke();
      } else {
        ctx.globalAlpha = 0.6;
        ctx.lineWidth = 1;
        ctx.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight, width, drawHeight);
      }
    }
    ctx.globalAlpha = 1.0;
    ctx.lineWidth = 1;
  }
}

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
import { BaseStruct, Rect, Render, isFrameContainPoint } from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';
export class HeapSnapshotRender extends Render {
  renderMainThread(
    req: {
      context: CanvasRenderingContext2D;
      useCache: boolean;
      type: string;
      traceRange: any;
    },
    row: TraceRow<HeapSnapshotStruct>
  ) {
    let list = row.dataList;
    let filter = row.dataListCache;
    if (filter.length == 0) {
      for (let file of list) {
        file.start_time = file.start_time - req.traceRange[0].start_ts;
        file.end_time = file.end_time - req.traceRange[0].start_ts;
      }
    }
    HeapSnapshot(
      list,
      filter,
      TraceRow.range?.startNS ?? 0,
      TraceRow.range?.endNS ?? 0,
      (TraceRow.range?.endNS ?? 0) - (TraceRow.range?.startNS! ?? 0),
      row.frame
    );
    req.context!.beginPath();
    for (let re of filter) {
      HeapSnapshotStruct.draw(req.context, re);
    }
    for (let re of filter) {
      if (re.frame && !isFrameContainPoint(re.frame, row.hoverX, row.hoverY)) {
        HeapSnapshotStruct.hoverSnapshotStruct = undefined;
      }
      if (re.frame && isFrameContainPoint(re.frame, row.hoverX, row.hoverY)) {
        HeapSnapshotStruct.hoverSnapshotStruct = re;
        break;
      }
    }
    req.context!.closePath();
  }
}
export function HeapSnapshot(
  list: Array<any>,
  filter: Array<any>,
  startNS: number,
  endNS: number,
  totalNS: number,
  frame: any
) {
  for (let i in list) {
    HeapSnapshotStruct.setFrame(list[i], startNS || 0, endNS || 0, totalNS || 0, frame);
  }
  filter.length = 0;
  for (let i = 0, len = list.length; i < len; i++) {
    if (list[i].frame) {
      filter.push(list[i]);
    }
  }
}
const padding = 3;
export class HeapSnapshotStruct extends BaseStruct {
  start_time: number = 0;
  end_time: number = 0;
  id: number = 0;
  pid: number = 0;
  file_name: string | undefined;
  textMetricsWidth: number | undefined;
  static hoverSnapshotStruct: HeapSnapshotStruct | undefined;
  static selectSnapshotStruct: HeapSnapshotStruct | undefined;

  static setFrame(node: any, startNS: number, endNS: number, totalNS: number, frame: Rect) {
    node.frame = null;
    if ((node.start_time - startNS || node.start_time - startNS === 0) && node.end_time - node.start_time) {
      let rectangle: Rect = new Rect(
        Math.floor(((node.start_time - startNS) / totalNS) * frame.width),
        0,
        Math.ceil(((node.end_time - node.start_time) / totalNS) * frame.width),
        40
      );
      node.frame = rectangle;
    }
  }

  static draw(ctx: CanvasRenderingContext2D, data: HeapSnapshotStruct): void {
    if (data.frame) {
      ctx.fillStyle = 'rgb(86,192,197)';
      ctx!.fillRect(data.frame!.x, data.frame!.y + padding, data.frame!.width, data.frame!.height - padding * 2);
      if (data.frame!.width > 7) {
        ctx.globalAlpha = 1.0;
        ctx.lineWidth = 1;
        ctx.fillStyle = '#fff';
        HeapSnapshotStruct.drawString(ctx, data.file_name || '', 2, data.frame!, data);
      }
      if (
        HeapSnapshotStruct.selectSnapshotStruct &&
        HeapSnapshotStruct.equals(HeapSnapshotStruct.selectSnapshotStruct, data)
      ) {
        ctx.strokeStyle = '#232c5d';
        ctx.lineWidth = 2;
        ctx.strokeRect(data.frame!.x, data.frame!.y + padding, data.frame!.width - 2, data.frame!.height - padding * 2);
      }
    }
  }

  static drawString(
    ctx: CanvasRenderingContext2D,
    str: string,
    textPadding: number,
    frame: Rect,
    data: HeapSnapshotStruct
  ) {
    if (data.textMetricsWidth === undefined) {
      data.textMetricsWidth = ctx.measureText(str).width;
    }
    let charWidth = Math.round(data.textMetricsWidth / str.length);
    let fillTextWidth = frame.width - textPadding * 2;
    if (data.textMetricsWidth < fillTextWidth) {
      let x2 = Math.floor(frame.width / 2 - data.textMetricsWidth / 2 + frame.x + textPadding);
      ctx.textBaseline = 'middle';
      ctx.font = '12px sans-serif';
      ctx.fillText(str, x2, Math.floor(frame.y + frame.height / 2), fillTextWidth);
    } else {
      if (fillTextWidth >= charWidth) {
        let chatNum = fillTextWidth / charWidth;
        let x1 = frame.x + textPadding;
        ctx.textBaseline = 'middle';
        ctx.font = '12px sans-serif';
        if (chatNum < 2) {
          ctx.fillText(str.substring(0, 1), x1, Math.floor(frame.y + frame.height / 2), fillTextWidth);
        } else {
          ctx.fillText(
            str.substring(0, chatNum - 1) + '...',
            x1,
            Math.floor(frame.y + frame.height / 2),
            fillTextWidth
          );
        }
      }
    }
  }

  static equals(d1: HeapSnapshotStruct, d2: HeapSnapshotStruct): boolean {
    return (
      d1 &&
      d2 &&
      d1.file_name == d2.file_name &&
      d1.id == d2.id &&
      d1.pid == d2.pid &&
      d1.start_time == d2.start_time &&
      d1.end_time == d2.end_time
    );
  }
}

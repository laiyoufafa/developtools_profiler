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
  isFrameContainPoint,
  Rect,
  Render,
  RequestMessage,
} from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';
import { Utils } from '../../component/trace/base/Utils.js';

export class ThreadRender extends Render {
  renderMainThread(
    req: {
      context: CanvasRenderingContext2D;
      useCache: boolean;
      type: string;
      translateY: number;
    },
    row: TraceRow<ThreadStruct>
  ) {
    let list = row.dataList;
    let filter = row.dataListCache;
    dataFilterHandler(list, filter, {
      startKey: 'startTime',
      durKey: 'dur',
      startNS: TraceRow.range?.startNS ?? 0,
      endNS: TraceRow.range?.endNS ?? 0,
      totalNS: TraceRow.range?.totalNS ?? 0,
      frame: row.frame,
      paddingTop: 5,
      useCache: req.useCache || !(TraceRow.range?.refresh ?? false),
    });
    req.context.beginPath();
    for (let re of filter) {
      re.translateY = req.translateY;
      ThreadStruct.draw(req.context, re);
      if (row.isHover && re.frame && isFrameContainPoint(re.frame!, row.hoverX, row.hoverY)) {
        ThreadStruct.hoverThreadStruct = re;
      }
    }
    req.context.closePath();
  }

  render(req: RequestMessage, list: Array<any>, filter: Array<any>) {}
}

const padding = 3;

export class ThreadStruct extends BaseStruct {
  static runningColor: string = '#467b3b';
  static rColor = '#a0b84d';
  static otherColor = '#673ab7';
  static uninterruptibleSleepColor = '#f19d38';
  static uninterruptibleSleepNonIOColor = '#795548';
  static traceColor = '#0d47a1';
  static sColor = '#FBFBFB';
  static hoverThreadStruct: ThreadStruct | undefined;
  static selectThreadStruct: ThreadStruct | undefined;

  hasSched: number | undefined; // 14724852000
  pid: number | undefined; // 2519
  processName: string | undefined; //null
  threadName: string | undefined; //"ACCS0"
  tid: number | undefined; //2716
  upid: number | undefined; // 1
  utid: number | undefined; // 1
  cpu: number | undefined; // null
  dur: number | undefined; // 405000
  argSetID: number | undefined; // 405000
  end_ts: number | undefined; // null
  id: number | undefined; // 1
  is_main_thread: number | undefined; // 0
  name: string | undefined; // "ACCS0"
  startTime: number | undefined; // 58000
  start_ts: number | undefined; // null
  state: string | undefined; // "S"
  type: string | undefined; // "thread"
  textMetricsWidth: number | undefined;

  static draw(ctx: CanvasRenderingContext2D, data: ThreadStruct) {
    if (data.frame) {
      ctx.globalAlpha = 1;
      let stateText = ThreadStruct.getEndState(data.state || '');
      ctx.fillStyle = Utils.getStateColor(data.state || '');
      if ('S' === data.state) {
        ctx.globalAlpha = 0.2;
      }
      ctx.fillRect(data.frame.x, data.frame.y + padding, data.frame.width, data.frame.height - padding * 2);
      ctx.fillStyle = '#fff';
      if ('S' !== data.state) {
        data.frame.width > 7 && ThreadStruct.drawString(ctx, stateText, 2, data.frame, data);
      }
      if (
        ThreadStruct.selectThreadStruct &&
        ThreadStruct.equals(ThreadStruct.selectThreadStruct, data) &&
        ThreadStruct.selectThreadStruct.state != 'S'
      ) {
        ctx.strokeStyle = '#232c5d';
        ctx.lineWidth = 2;
        ctx.strokeRect(data.frame.x, data.frame.y + padding, data.frame.width - 2, data.frame.height - padding * 2);
      }
    }
  }

  static drawString(ctx: CanvasRenderingContext2D, str: string, textPadding: number, frame: Rect, data: ThreadStruct) {
    if (data.textMetricsWidth === undefined) {
      data.textMetricsWidth = ctx.measureText(str).width;
    }
    let charWidth = Math.round(data.textMetricsWidth / str.length);
    let fillTextWidth = frame.width - textPadding * 2;
    if (data.textMetricsWidth < fillTextWidth) {
      let x2 = Math.floor(frame.width / 2 - data.textMetricsWidth / 2 + frame.x + textPadding);
      ctx.textBaseline = 'middle';
      ctx.font = '8px sans-serif';
      ctx.fillText(str, x2, Math.floor(frame.y + frame.height / 2), fillTextWidth);
    } else {
      if (fillTextWidth >= charWidth) {
        let chatNum = (frame.width - textPadding * 2) / charWidth;
        let x1 = frame.x + textPadding;
        ctx.textBaseline = 'middle';
        ctx.font = '8px sans-serif';
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

  static getEndState(state: string): string {
    let statusMapElement = Utils.getEndState(state);
    if (statusMapElement) {
      return statusMapElement;
    } else {
      if ('' == statusMapElement || statusMapElement == null) {
        return '';
      }
      return 'Unknown State';
    }
  }

  static equals(d1: ThreadStruct, d2: ThreadStruct): boolean {
    if (
      d1 &&
      d2 &&
      d1.cpu == d2.cpu &&
      d1.tid == d2.tid &&
      d1.state == d2.state &&
      d1.startTime == d2.startTime &&
      d1.dur == d2.dur
    ) {
      return true;
    } else {
      return false;
    }
  }
}

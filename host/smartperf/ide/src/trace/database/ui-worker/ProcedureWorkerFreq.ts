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
import { BaseStruct, dataFilterHandler, isFrameContainPoint, Render, RequestMessage } from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';

export class FreqRender extends Render {
  renderMainThread(
    req: {
      context: CanvasRenderingContext2D;
      useCache: boolean;
      type: string;
    },
    row: TraceRow<CpuFreqStruct>
  ) {
    let list = row.dataList;
    let filter = row.dataListCache;
    dataFilterHandler(list, filter, {
      startKey: 'startNS',
      durKey: 'dur',
      startNS: TraceRow.range?.startNS ?? 0,
      endNS: TraceRow.range?.endNS ?? 0,
      totalNS: TraceRow.range?.totalNS ?? 0,
      frame: row.frame,
      paddingTop: 5,
      useCache: req.useCache || !(TraceRow.range?.refresh ?? false),
    });
    req.context.beginPath();
    let find = false;
    for (let re of filter) {
      if (row.isHover && re.frame && isFrameContainPoint(re.frame, row.hoverX, row.hoverY)) {
        CpuFreqStruct.hoverCpuFreqStruct = re;
        find = true;
      }
      CpuFreqStruct.draw(req.context, re);
    }
    if (!find && row.isHover) CpuFreqStruct.hoverCpuFreqStruct = undefined;
    req.context.closePath();
    let s = CpuFreqStruct.maxFreqName;
    let textMetrics = req.context.measureText(s);
    req.context.globalAlpha = 0.8;
    req.context.fillStyle = '#f0f0f0';
    req.context.fillRect(0, 5, textMetrics.width + 8, 18);
    req.context.globalAlpha = 1;
    req.context.fillStyle = '#333';
    req.context.textBaseline = 'middle';
    req.context.fillText(s, 4, 5 + 9);
  }
}

export class CpuFreqStruct extends BaseStruct {
  static maxFreq: number = 0;
  static maxFreqName: string = '0 GHz';
  static hoverCpuFreqStruct: CpuFreqStruct | undefined;
  static selectCpuFreqStruct: CpuFreqStruct | undefined;
  cpu: number | undefined;
  value: number | undefined;
  startNS: number | undefined;
  dur: number | undefined; //自补充，数据库没有返回

  static draw(ctx: CanvasRenderingContext2D, data: CpuFreqStruct) {
    if (data.frame) {
      let width = data.frame.width || 0;
      let index = data.cpu || 0;
      index += 2;
      ctx.fillStyle = ColorUtils.colorForTid(index);
      ctx.strokeStyle = ColorUtils.colorForTid(index);
      if (data === CpuFreqStruct.hoverCpuFreqStruct || data === CpuFreqStruct.selectCpuFreqStruct) {
        ctx.lineWidth = 1;
        ctx.globalAlpha = 0.6;
        let drawHeight: number = Math.floor(
          ((data.value || 0) * (data.frame.height || 0) * 1.0) / CpuFreqStruct.maxFreq
        );
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
        let drawHeight: number = Math.floor(((data.value || 0) * (data.frame.height || 0)) / CpuFreqStruct.maxFreq);
        ctx.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight, width, drawHeight);
      }
    }
    ctx.globalAlpha = 1.0;
    ctx.lineWidth = 1;
  }
}

const textPadding = 2;

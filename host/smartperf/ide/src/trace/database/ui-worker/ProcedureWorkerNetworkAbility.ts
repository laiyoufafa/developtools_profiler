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
  Render,
  RequestMessage,
} from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';
export class NetworkAbilityRender extends Render {
  renderMainThread(
    req: {
      context: CanvasRenderingContext2D;
      useCache: boolean;
      type: string;
      maxNetworkRate: number;
      maxNetworkRateName: string;
    },
    row: TraceRow<NetworkAbilityMonitorStruct>
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
      NetworkAbilityMonitorStruct.draw(req.context, re, req.maxNetworkRate, row.isHover);
      if (row.isHover && re.frame && isFrameContainPoint(re.frame, row.hoverX, row.hoverY)) {
        NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = re;
        find = true;
      }
    }
    if (!find && row.isHover) NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct = undefined;
    req.context.closePath();
    let textMetrics = req.context.measureText(req.maxNetworkRateName);
    req.context.globalAlpha = 0.8;
    req.context.fillStyle = '#f0f0f0';
    req.context.fillRect(0, 5, textMetrics.width + 8, 18);
    req.context.globalAlpha = 1;
    req.context.fillStyle = '#333';
    req.context.textBaseline = 'middle';
    req.context.fillText(req.maxNetworkRateName, 4, 5 + 9);
  }

  render(req: RequestMessage, list: Array<any>, filter: Array<any>) {}
}

export class NetworkAbilityMonitorStruct extends BaseStruct {
  static maxNetworkRate: number = 0;
  static maxNetworkRateName: string = '0 KB/S';
  static hoverNetworkAbilityStruct: NetworkAbilityMonitorStruct | undefined;
  static selectNetworkAbilityStruct: NetworkAbilityMonitorStruct | undefined;
  value: number | undefined;
  startNS: number | undefined;

  static draw(
    context2D: CanvasRenderingContext2D,
    data: NetworkAbilityMonitorStruct,
    maxNetworkRate: number,
    isHover: boolean
  ) {
    if (data.frame) {
      let width = data.frame.width || 0;
      let index = 2;
      context2D.fillStyle = ColorUtils.colorForTid(index);
      context2D.strokeStyle = ColorUtils.colorForTid(index);
      if (data.startNS === NetworkAbilityMonitorStruct.hoverNetworkAbilityStruct?.startNS && isHover) {
        context2D.lineWidth = 1;
        context2D.globalAlpha = 0.6;
        let drawHeight: number = Math.floor(((data.value || 0) * (data.frame.height || 0) * 1.0) / maxNetworkRate);
        context2D.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight + 4, width, drawHeight);
        context2D.beginPath();
        context2D.arc(data.frame.x, data.frame.y + data.frame.height - drawHeight + 4, 3, 0, 2 * Math.PI, true);
        context2D.fill();
        context2D.globalAlpha = 1.0;
        context2D.stroke();
        context2D.beginPath();
        context2D.moveTo(data.frame.x + 3, data.frame.y + data.frame.height - drawHeight + 4);
        context2D.lineWidth = 3;
        context2D.lineTo(data.frame.x + width, data.frame.y + data.frame.height - drawHeight + 4);
        context2D.stroke();
      } else {
        context2D.globalAlpha = 0.6;
        context2D.lineWidth = 1;
        let drawHeight: number = Math.floor(((data.value || 0) * (data.frame.height || 0)) / maxNetworkRate);
        context2D.fillRect(data.frame.x, data.frame.y + data.frame.height - drawHeight + 4, width, drawHeight);
      }
    }
    context2D.globalAlpha = 1.0;
    context2D.lineWidth = 1;
  }
}

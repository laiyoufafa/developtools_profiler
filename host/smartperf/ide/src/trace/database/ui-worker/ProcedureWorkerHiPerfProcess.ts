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
  drawFlagLine,
  drawLines,
  drawLoading,
  drawSelection,
  hiPerf,
  HiPerfStruct,
  PerfRender,
  RequestMessage,
} from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';

export class HiperfProcessRender extends PerfRender {
  renderMainThread(hiPerfProcessReq: any, row: TraceRow<HiPerfProcessStruct>) {
    let list = row.dataList;
    let filter = row.dataListCache;
    let groupBy10MS = hiPerfProcessReq.scale > 30_000_000;
    if (list && row.dataList2.length == 0) {
      row.dataList2 = HiPerfProcessStruct.groupBy10MS(list, hiPerfProcessReq.intervalPerf);
    }
    hiPerf(
      list,
      row.dataList2,
      filter,
      TraceRow.range?.startNS ?? 0,
      TraceRow.range?.endNS ?? 0,
      row.frame,
      groupBy10MS,
      hiPerfProcessReq.useCache || (TraceRow.range?.refresh ?? false)
    );
    hiPerfProcessReq.context.beginPath();
    hiPerfProcessReq.context.fillStyle = ColorUtils.FUNC_COLOR[0];
    hiPerfProcessReq.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
    let path = new Path2D();
    let offset = groupBy10MS ? 0 : 3;
    let find = false;
    for (let re of filter) {
      HiPerfProcessStruct.draw(hiPerfProcessReq.context, path, re, groupBy10MS);
      if (row.isHover) {
        if (re.frame && row.hoverX >= re.frame.x - offset && row.hoverX <= re.frame.x + re.frame.width + offset) {
          HiPerfProcessStruct.hoverStruct = re;
          find = true;
        }
      }
    }
    if (!find && row.isHover) HiPerfProcessStruct.hoverStruct = undefined;
    groupBy10MS ? hiPerfProcessReq.context.fill(path) : hiPerfProcessReq.context.stroke(path);
    hiPerfProcessReq.context.closePath();
  }

  render(hiPerfProcessRequest: RequestMessage, list: Array<any>, filter: Array<any>, dataList2: Array<any>) {
    let groupBy10MS = hiPerfProcessRequest.scale > 100_000_000;
    if (hiPerfProcessRequest.lazyRefresh) {
      hiPerf(
        list,
        dataList2,
        filter,
        hiPerfProcessRequest.startNS,
        hiPerfProcessRequest.endNS,
        hiPerfProcessRequest.frame,
        groupBy10MS,
        hiPerfProcessRequest.useCache || !hiPerfProcessRequest.range.refresh
      );
    } else {
      if (!hiPerfProcessRequest.useCache) {
        hiPerf(
          list,
          dataList2,
          filter,
          hiPerfProcessRequest.startNS,
          hiPerfProcessRequest.endNS,
          hiPerfProcessRequest.frame,
          groupBy10MS,
          false
        );
      }
    }
    if (hiPerfProcessRequest.canvas) {
      hiPerfProcessRequest.context.clearRect(0, 0, hiPerfProcessRequest.frame.width, hiPerfProcessRequest.frame.height);
      let arr = filter;
      if (
        arr.length > 0 &&
        !hiPerfProcessRequest.range.refresh &&
        !hiPerfProcessRequest.useCache &&
        hiPerfProcessRequest.lazyRefresh
      ) {
        drawLoading(
          hiPerfProcessRequest.context,
          hiPerfProcessRequest.startNS,
          hiPerfProcessRequest.endNS,
          hiPerfProcessRequest.totalNS,
          hiPerfProcessRequest.frame,
          arr[0].startNS,
          arr[arr.length - 1].startNS + arr[arr.length - 1].dur
        );
      }
      drawLines(
        hiPerfProcessRequest.context,
        hiPerfProcessRequest.xs,
        hiPerfProcessRequest.frame.height,
        hiPerfProcessRequest.lineColor
      );
      hiPerfProcessRequest.context.stroke();
      hiPerfProcessRequest.context.beginPath();
      HiPerfProcessStruct.hoverStruct = undefined;
      hiPerfProcessRequest.context.fillStyle = ColorUtils.FUNC_COLOR[0];
      hiPerfProcessRequest.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
      if (hiPerfProcessRequest.isHover) {
        let offset = groupBy10MS ? 0 : 3;
        for (let re of filter) {
          if (
            re.frame &&
            hiPerfProcessRequest.hoverX >= re.frame.x - offset &&
            hiPerfProcessRequest.hoverX <= re.frame.x + re.frame.width + offset
          ) {
            HiPerfProcessStruct.hoverStruct = re;
            break;
          }
        }
      } else {
        HiPerfProcessStruct.hoverStruct = hiPerfProcessRequest.params.hoverStruct;
      }
      HiPerfProcessStruct.selectStruct = hiPerfProcessRequest.params.selectStruct;
      let path = new Path2D();
      for (let re of filter) {
        HiPerfProcessStruct.draw(hiPerfProcessRequest.context, path, re, groupBy10MS);
      }
      groupBy10MS ? hiPerfProcessRequest.context.fill(path) : hiPerfProcessRequest.context.stroke(path);
      hiPerfProcessRequest.context.closePath();
      drawSelection(hiPerfProcessRequest.context, hiPerfProcessRequest.params);
      drawFlagLine(
        hiPerfProcessRequest.context,
        hiPerfProcessRequest.flagMoveInfo,
        hiPerfProcessRequest.flagSelectedInfo,
        hiPerfProcessRequest.startNS,
        hiPerfProcessRequest.endNS,
        hiPerfProcessRequest.totalNS,
        hiPerfProcessRequest.frame,
        hiPerfProcessRequest.slicesTime
      );
    }
    // @ts-ignore
    self.postMessage({
      id: hiPerfProcessRequest.id,
      type: hiPerfProcessRequest.type,
      results: hiPerfProcessRequest.canvas ? undefined : filter,
      hover: HiPerfProcessStruct.hoverStruct,
    });
  }
}

export class HiPerfProcessStruct extends HiPerfStruct {
  static hoverStruct: HiPerfProcessStruct | undefined;
  static selectStruct: HiPerfProcessStruct | undefined;
}

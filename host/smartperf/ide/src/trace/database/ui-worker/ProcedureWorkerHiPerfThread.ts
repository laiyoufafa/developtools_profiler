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

export class HiperfThreadRender extends PerfRender {
  renderMainThread(hiPerfThreadReq: any, row: TraceRow<HiPerfThreadStruct>) {
    let list = row.dataList;
    let filter = row.dataListCache;
    let groupBy10MS = hiPerfThreadReq.scale > 30_000_000;
    if (list && row.dataList2.length == 0) {
      row.dataList2 = HiPerfThreadStruct.groupBy10MS(list, hiPerfThreadReq.intervalPerf);
    }
    hiPerf(
      list,
      row.dataList2,
      filter,
      TraceRow.range?.startNS ?? 0,
      TraceRow.range?.endNS ?? 0,
      row.frame,
      groupBy10MS,
      hiPerfThreadReq.useCache || (TraceRow.range?.refresh ?? false)
    );
    hiPerfThreadReq.context.beginPath();
    hiPerfThreadReq.context.fillStyle = ColorUtils.FUNC_COLOR[0];
    hiPerfThreadReq.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
    let path = new Path2D();
    let offset = groupBy10MS ? 0 : 3;
    let find = false;
    for (let re of filter) {
      HiPerfThreadStruct.draw(hiPerfThreadReq.context, path, re, groupBy10MS);
      if (row.isHover) {
        if (re.frame && row.hoverX >= re.frame.x - offset && row.hoverX <= re.frame.x + re.frame.width + offset) {
          HiPerfThreadStruct.hoverStruct = re;
          find = true;
        }
      }
    }
    if (!find && row.isHover) HiPerfThreadStruct.hoverStruct = undefined;
    groupBy10MS ? hiPerfThreadReq.context.fill(path) : hiPerfThreadReq.context.stroke(path);
    hiPerfThreadReq.context.closePath();
  }

  render(hiPerfThreadRequest: RequestMessage, list: Array<any>, filter: Array<any>, dataList2: Array<any>) {
    let groupBy10MS = hiPerfThreadRequest.scale > 100_000_000;
    if (hiPerfThreadRequest.lazyRefresh) {
      hiPerf(
        list,
        dataList2,
        filter,
        hiPerfThreadRequest.startNS,
        hiPerfThreadRequest.endNS,
        hiPerfThreadRequest.frame,
        groupBy10MS,
        hiPerfThreadRequest.useCache || !hiPerfThreadRequest.range.refresh
      );
    } else {
      if (!hiPerfThreadRequest.useCache) {
        hiPerf(
          list,
          dataList2,
          filter,
          hiPerfThreadRequest.startNS,
          hiPerfThreadRequest.endNS,
          hiPerfThreadRequest.frame,
          groupBy10MS,
          false
        );
      }
    }
    if (hiPerfThreadRequest.canvas) {
      hiPerfThreadRequest.context.clearRect(0, 0, hiPerfThreadRequest.frame.width, hiPerfThreadRequest.frame.height);
      let arr = filter;
      if (
        arr.length > 0 &&
        !hiPerfThreadRequest.range.refresh &&
        !hiPerfThreadRequest.useCache &&
        hiPerfThreadRequest.lazyRefresh
      ) {
        drawLoading(
          hiPerfThreadRequest.context,
          hiPerfThreadRequest.startNS,
          hiPerfThreadRequest.endNS,
          hiPerfThreadRequest.totalNS,
          hiPerfThreadRequest.frame,
          arr[0].startNS,
          arr[arr.length - 1].startNS + arr[arr.length - 1].dur
        );
      }
      drawLines(
        hiPerfThreadRequest.context,
        hiPerfThreadRequest.xs,
        hiPerfThreadRequest.frame.height,
        hiPerfThreadRequest.lineColor
      );
      hiPerfThreadRequest.context.stroke();
      hiPerfThreadRequest.context.beginPath();
      HiPerfThreadStruct.hoverStruct = undefined;
      hiPerfThreadRequest.context.fillStyle = ColorUtils.FUNC_COLOR[0];
      hiPerfThreadRequest.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
      if (hiPerfThreadRequest.isHover) {
        let offset = groupBy10MS ? 0 : 3;
        for (let re of filter) {
          if (
            re.frame &&
            hiPerfThreadRequest.hoverX >= re.frame.x - offset &&
            hiPerfThreadRequest.hoverX <= re.frame.x + re.frame.width + offset
          ) {
            HiPerfThreadStruct.hoverStruct = re;
            break;
          }
        }
      } else {
        HiPerfThreadStruct.hoverStruct = hiPerfThreadRequest.params.hoverStruct;
      }
      HiPerfThreadStruct.selectStruct = hiPerfThreadRequest.params.selectStruct;
      let path = new Path2D();
      for (let re of filter) {
        HiPerfThreadStruct.draw(hiPerfThreadRequest.context, path, re, groupBy10MS);
      }
      groupBy10MS ? hiPerfThreadRequest.context.fill(path) : hiPerfThreadRequest.context.stroke(path);
      hiPerfThreadRequest.context.stroke();
      hiPerfThreadRequest.context.closePath();
      drawSelection(hiPerfThreadRequest.context, hiPerfThreadRequest.params);
      drawFlagLine(
        hiPerfThreadRequest.context,
        hiPerfThreadRequest.flagMoveInfo,
        hiPerfThreadRequest.flagSelectedInfo,
        hiPerfThreadRequest.startNS,
        hiPerfThreadRequest.endNS,
        hiPerfThreadRequest.totalNS,
        hiPerfThreadRequest.frame,
        hiPerfThreadRequest.slicesTime
      );
    }
    // @ts-ignore
    self.postMessage({
      id: hiPerfThreadRequest.id,
      type: hiPerfThreadRequest.type,
      results: hiPerfThreadRequest.canvas ? undefined : filter,
      hover: HiPerfThreadStruct.hoverStruct,
    });
  }
}

export class HiPerfThreadStruct extends HiPerfStruct {
  static hoverStruct: HiPerfThreadStruct | undefined;
  static selectStruct: HiPerfThreadStruct | undefined;
}

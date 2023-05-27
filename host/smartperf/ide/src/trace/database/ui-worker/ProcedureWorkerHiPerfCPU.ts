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
  HiPerfStruct,
  hiPerf,
  PerfRender,
  RequestMessage,
} from './ProcedureWorkerCommon.js';
import { TraceRow } from '../../component/trace/base/TraceRow.js';
import { HiPerfThreadStruct } from './ProcedureWorkerHiPerfThread.js';

export class HiperfCpuRender extends PerfRender {
  renderMainThread(req: any, row: TraceRow<HiPerfCpuStruct>) {
    let list = row.dataList;
    let filter = row.dataListCache;
    let groupBy10MS = req.scale > 30_000_000;
    if (list && row.dataList2.length == 0) {
      row.dataList2 = HiPerfCpuStruct.groupBy10MS(list, req.intervalPerf, req.maxCpu);
    }
    hiPerf(
      list,
      row.dataList2,
      filter,
      TraceRow.range?.startNS ?? 0,
      TraceRow.range?.endNS ?? 0,
      row.frame,
      groupBy10MS,
      req.useCache || (TraceRow.range?.refresh ?? false)
    );
    req.context.beginPath();
    req.context.fillStyle = ColorUtils.FUNC_COLOR[0];
    req.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
    let path = new Path2D();
    let find = false;
    let offset = groupBy10MS ? 0 : 3;
    for (let re of filter) {
      if (
        row.isHover &&
        re.frame &&
        row.hoverX >= re.frame.x - offset &&
        row.hoverX <= re.frame.x + re.frame.width + offset
      ) {
        HiPerfCpuStruct.hoverStruct = re;
        find = true;
      }
      HiPerfCpuStruct.draw(req.context, path, re, groupBy10MS);
    }
    if (!find && row.isHover) HiPerfCpuStruct.hoverStruct = undefined;
    if (groupBy10MS) {
      req.context.fill(path);
    } else {
      req.context.stroke(path);
    }
    req.context.closePath();
  }

  render(hiPerfCpuRequest: RequestMessage, list: Array<any>, filter: Array<any>, dataList2: Array<any>) {
    let groupBy10MS = hiPerfCpuRequest.scale > 100_000_000;
    if (list && dataList2.length == 0) {
      dataList2 = HiPerfCpuStruct.groupBy10MS(list, hiPerfCpuRequest.intervalPerf, hiPerfCpuRequest.params.maxCpu);
    }
    if (hiPerfCpuRequest.lazyRefresh) {
      hiPerf(
        list,
        dataList2,
        filter,
        hiPerfCpuRequest.startNS,
        hiPerfCpuRequest.endNS,
        hiPerfCpuRequest.frame,
        groupBy10MS,
        hiPerfCpuRequest.useCache || !hiPerfCpuRequest.range.refresh
      );
    } else {
      if (!hiPerfCpuRequest.useCache) {
        hiPerf(
          list,
          dataList2,
          filter,
          hiPerfCpuRequest.startNS,
          hiPerfCpuRequest.endNS,
          hiPerfCpuRequest.frame,
          groupBy10MS,
          false
        );
      }
    }
    if (hiPerfCpuRequest.canvas) {
      hiPerfCpuRequest.context.clearRect(0, 0, hiPerfCpuRequest.frame.width, hiPerfCpuRequest.frame.height);
      let arr = filter;
      if (
        arr.length > 0 &&
        !hiPerfCpuRequest.range.refresh &&
        !hiPerfCpuRequest.useCache &&
        hiPerfCpuRequest.lazyRefresh
      ) {
        drawLoading(
          hiPerfCpuRequest.context,
          hiPerfCpuRequest.startNS,
          hiPerfCpuRequest.endNS,
          hiPerfCpuRequest.totalNS,
          hiPerfCpuRequest.frame,
          arr[0].startNS,
          arr[arr.length - 1].startNS + arr[arr.length - 1].dur
        );
      }
      drawLines(
        hiPerfCpuRequest.context,
        hiPerfCpuRequest.xs,
        hiPerfCpuRequest.frame.height,
        hiPerfCpuRequest.lineColor
      );
      hiPerfCpuRequest.context.stroke();
      hiPerfCpuRequest.context.beginPath();
      HiPerfCpuStruct.hoverStruct = undefined;
      if (hiPerfCpuRequest.isHover) {
        let offset = groupBy10MS ? 0 : 3;
        for (let re of filter) {
          if (
            re.frame &&
            hiPerfCpuRequest.hoverX >= re.frame.x - offset &&
            hiPerfCpuRequest.hoverX <= re.frame.x + re.frame.width + offset
          ) {
            HiPerfCpuStruct.hoverStruct = re;
            break;
          }
        }
      } else {
        HiPerfCpuStruct.hoverStruct = hiPerfCpuRequest.params.hoverStruct;
      }
      HiPerfCpuStruct.selectStruct = hiPerfCpuRequest.params.selectStruct;
      hiPerfCpuRequest.context.fillStyle = ColorUtils.FUNC_COLOR[0];
      hiPerfCpuRequest.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
      let path = new Path2D();
      for (let re of filter) {
        HiPerfCpuStruct.draw(hiPerfCpuRequest.context, path, re, groupBy10MS);
      }
      if (groupBy10MS) {
        hiPerfCpuRequest.context.fill(path);
      } else {
        hiPerfCpuRequest.context.stroke(path);
      }
      drawSelection(hiPerfCpuRequest.context, hiPerfCpuRequest.params);
      hiPerfCpuRequest.context.closePath();
      drawFlagLine(
        hiPerfCpuRequest.context,
        hiPerfCpuRequest.flagMoveInfo,
        hiPerfCpuRequest.flagSelectedInfo,
        hiPerfCpuRequest.startNS,
        hiPerfCpuRequest.endNS,
        hiPerfCpuRequest.totalNS,
        hiPerfCpuRequest.frame,
        hiPerfCpuRequest.slicesTime
      );
    }
    // @ts-ignore
    self.postMessage({
      id: hiPerfCpuRequest.id,
      type: hiPerfCpuRequest.type,
      results: hiPerfCpuRequest.canvas ? undefined : filter,
      hover: HiPerfCpuStruct.hoverStruct,
    });
  }
}

export class HiPerfCpuStruct extends HiPerfStruct {
  static hoverStruct: HiPerfCpuStruct | undefined;
  static selectStruct: HiPerfCpuStruct | undefined;

  cpu: number | undefined;
}

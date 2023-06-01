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
  PerfRender,
  RequestMessage,
} from './ProcedureWorkerCommon.js';

import { TraceRow } from '../../component/trace/base/TraceRow.js';

export class HiperfReportRender extends PerfRender {
  renderMainThread(hiPerfReportReq: any, row: TraceRow<HiPerfReportStruct>) {
    let list = row.dataList;
    let filter = row.dataListCache;
    let groupBy10MS = hiPerfReportReq.scale > 30_000_000;
    if (list && row.dataList2.length == 0) {
      row.dataList2 = HiPerfReportStruct.reportGroupBy10MS(list, hiPerfReportReq.intervalPerf);
    }
    HiPerfReport(
      list,
      row.dataList2,
      hiPerfReportReq.type!,
      filter,
      TraceRow.range?.startNS ?? 0,
      TraceRow.range?.endNS ?? 0,
      TraceRow.range?.totalNS ?? 0,
      row.frame,
      groupBy10MS,
      hiPerfReportReq.intervalPerf,
      hiPerfReportReq.useCache || (TraceRow.range?.refresh ?? false)
    );
    hiPerfReportReq.context.beginPath();
    hiPerfReportReq.context.fillStyle = ColorUtils.FUNC_COLOR[0];
    hiPerfReportReq.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
    let path = new Path2D();
    let offset = groupBy10MS ? 0 : 3;
    let find = false;
    for (let re of filter) {
      HiPerfReportStruct.draw(hiPerfReportReq.context, path, re, groupBy10MS);
      if (row.isHover) {
        if (re.frame && row.hoverX >= re.frame.x - offset && row.hoverX <= re.frame.x + re.frame.width + offset) {
          HiPerfReportStruct.hoverStruct = re;
          find = true;
        }
      }
    }
    if (!find && row.isHover) HiPerfReportStruct.hoverStruct = undefined;
    groupBy10MS ? hiPerfReportReq.context.fill(path) : hiPerfReportReq.context.stroke(path);
    hiPerfReportReq.context.closePath();
  }

  render(hiPerfReportRequest: RequestMessage, list: Array<any>, filter: Array<any>, dataList2: Array<any>) {
    let groupBy10MS = hiPerfReportRequest.scale > 100_000_000;
    if (hiPerfReportRequest.lazyRefresh) {
      HiPerfReport(
        list,
        dataList2,
        hiPerfReportRequest.type!,
        filter,
        hiPerfReportRequest.startNS,
        hiPerfReportRequest.endNS,
        hiPerfReportRequest.totalNS,
        hiPerfReportRequest.frame,
        groupBy10MS,
        hiPerfReportRequest.intervalPerf,
        hiPerfReportRequest.useCache || !hiPerfReportRequest.range.refresh
      );
    } else {
      if (!hiPerfReportRequest.useCache) {
        HiPerfReport(
          list,
          dataList2,
          hiPerfReportRequest.type!,
          filter,
          hiPerfReportRequest.startNS,
          hiPerfReportRequest.endNS,
          hiPerfReportRequest.totalNS,
          hiPerfReportRequest.frame,
          groupBy10MS,
          hiPerfReportRequest.intervalPerf,
          false
        );
      }
    }
    if (hiPerfReportRequest.canvas) {
      hiPerfReportRequest.context.clearRect(0, 0, hiPerfReportRequest.frame.width, hiPerfReportRequest.frame.height);
      let arr = filter;
      if (
        arr.length > 0 &&
        !hiPerfReportRequest.range.refresh &&
        !hiPerfReportRequest.useCache &&
        hiPerfReportRequest.lazyRefresh
      ) {
        drawLoading(
          hiPerfReportRequest.context,
          hiPerfReportRequest.startNS,
          hiPerfReportRequest.endNS,
          hiPerfReportRequest.totalNS,
          hiPerfReportRequest.frame,
          arr[0].startNS,
          arr[arr.length - 1].startNS + arr[arr.length - 1].dur
        );
      }
      drawLines(
        hiPerfReportRequest.context,
        hiPerfReportRequest.xs,
        hiPerfReportRequest.frame.height,
        hiPerfReportRequest.lineColor
      );
      hiPerfReportRequest.context.stroke();
      hiPerfReportRequest.context.beginPath();
      HiPerfReportStruct.hoverStruct = undefined;
      hiPerfReportRequest.context.fillStyle = ColorUtils.FUNC_COLOR[0];
      hiPerfReportRequest.context.strokeStyle = ColorUtils.FUNC_COLOR[0];
      if (hiPerfReportRequest.isHover) {
        let offset = groupBy10MS ? 0 : 3;
        for (let re of filter) {
          if (
            re.frame &&
            hiPerfReportRequest.hoverX >= re.frame.x - offset &&
            hiPerfReportRequest.hoverX <= re.frame.x + re.frame.width + offset
          ) {
            HiPerfReportStruct.hoverStruct = re;
            break;
          }
        }
      } else {
        HiPerfReportStruct.hoverStruct = hiPerfReportRequest.params.hoverStruct;
      }
      HiPerfReportStruct.selectStruct = hiPerfReportRequest.params.selectStruct;
      let path = new Path2D();
      for (let re of filter) {
        HiPerfReportStruct.draw(hiPerfReportRequest.context, path, re, groupBy10MS);
      }
      groupBy10MS ? hiPerfReportRequest.context.fill(path) : hiPerfReportRequest.context.stroke(path);
      hiPerfReportRequest.context.closePath();
      drawSelection(hiPerfReportRequest.context, hiPerfReportRequest.params);
      drawFlagLine(
        hiPerfReportRequest.context,
        hiPerfReportRequest.flagMoveInfo,
        hiPerfReportRequest.flagSelectedInfo,
        hiPerfReportRequest.startNS,
        hiPerfReportRequest.endNS,
        hiPerfReportRequest.totalNS,
        hiPerfReportRequest.frame,
        hiPerfReportRequest.slicesTime
      );
    }
    // @ts-ignore
    self.postMessage({
      id: hiPerfReportRequest.id,
      type: hiPerfReportRequest.type,
      results: hiPerfReportRequest.canvas ? undefined : filter,
      hover: HiPerfReportStruct.hoverStruct,
    });
  }
}

export function HiPerfReport(
  arr: Array<any>,
  arr2: any,
  type: string,
  res: Array<any>,
  startNS: number,
  endNS: number,
  totalNS: number,
  frame: any,
  groupBy10MS: boolean,
  intervalPerf: number,
  use: boolean
) {
  if (use && res.length > 0) {
    let pns = (endNS - startNS) / frame.width;
    let y = frame.y;
    for (let i = 0; i < res.length; i++) {
      let it = res[i];
      if ((it.startNS || 0) + (it.dur || 0) > startNS && (it.startNS || 0) < endNS) {
        if (!it.frame) {
          it.frame = {};
          it.frame.y = y;
        }
        it.frame.height = it.height;
        HiPerfReportStruct.setFrame(it, pns, startNS, endNS, frame);
      } else {
        it.frame = null;
      }
    }
    return;
  }
  res.length = 0;
  if (arr) {
    let list: Array<any> = groupBy10MS ? arr2 : arr;
    let pns = (endNS - startNS) / frame.width;
    let y = frame.y;
    list
      .filter((it) => (it.startNS || 0) + (it.dur || 0) > startNS && (it.startNS || 0) < endNS)
      .map((it) => {
        if (!it.frame) {
          it.frame = {};
          it.frame.y = y;
        }
        it.frame.height = it.height;
        HiPerfReportStruct.setFrame(it, pns, startNS, endNS, frame);
        return it;
      })
      .reduce((pre, current, index, arr) => {
        if (!pre[`${current.frame.x}`]) {
          pre[`${current.frame.x}`] = [];
          pre[`${current.frame.x}`].push(current);
          if (groupBy10MS) {
            res.push(current);
          } else {
            if (res.length == 0) {
              res.push(current);
            }
            if (res[res.length - 1] && Math.abs(current.frame.x - res[res.length - 1].frame.x) > 4) {
              res.push(current);
            }
          }
        }
        return pre;
      }, {});
  }
}

export class HiPerfReportStruct extends HiPerfStruct {
  static hoverStruct: HiPerfReportStruct | undefined;
  static selectStruct: HiPerfReportStruct | undefined;

  static reportGroupBy10MS(array: Array<any>, intervalPerf: number): Array<any> {
    let obj = array
      .map((it) => {
        it.timestamp_group = Math.trunc(it.startNS / 1_000_000_0) * 1_000_000_0;
        return it;
      })
      .reduce((pre, current) => {
        (pre[current['timestamp_group']] = pre[current['timestamp_group']] || []).push(current);
        return pre;
      }, {});
    let reportArr: any[] = [];
    let max = 0;
    for (let aKey in obj) {
      let sum = obj[aKey].reduce((pre: any, cur: any) => {
        return pre + cur.event_count;
      }, 0);
      if (sum > max) max = sum;
      let ns = parseInt(aKey);
      reportArr.push({
        startNS: ns,
        dur: 1_000_000_0,
        height: 0,
        sum: sum,
      });
    }
    reportArr.map((it) => {
      it.height = Math.floor((40 * it.sum) / max);
      return it;
    });
    return reportArr;
  }
}

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

import { SpApplication } from '../SpApplication.js';
import { BaseStruct } from './BaseStruct.js';
import { Rect } from '../component/trace/timer-shaft/Rect.js';
import { info, warn } from '../../log/Log.js';
import { drawString } from '../database/ui-worker/ProcedureWorkerCommon.js';

const padding: number = 1;
const lightBlue = {
  r: 82,
  g: 145,
  b: 255,
  a: 0.9,
};

export class ChartStruct extends BaseStruct {
  static hoverFuncStruct: ChartStruct | undefined;
  static selectFuncStruct: ChartStruct | undefined;
  static lastSelectFuncStruct: ChartStruct | undefined;
  needShow = false;
  isDraw = false;
  depth: number = 0;
  symbol: string = '';
  lib: string = '';
  size: number = 0;
  count: number = 0;
  dur: number = 0;
  drawSize: number = 0;
  drawCount: number = 0;
  drawDur: number = 0;
  parent: ChartStruct | undefined;
  children: Array<ChartStruct> = [];
  percent: number = 0;
  addr: string = '';
  isSearch: boolean = false;
  textMetricsWidth: number | undefined;
}

export enum ChartMode {
  Byte, // Native Memory
  Count, // Perf
  Duration, // eBpf
}

export function setFuncFrame(node: ChartStruct, canvas_frame: Rect, total: number, mode: ChartMode) {
  if (!node.frame) {
    node.frame = new Rect(0, 0, 0, 0);
  }
  // filter depth is 0
  if (node.parent) {
    let idx = node.parent.children.indexOf(node);
    if (idx == 0) {
      node.frame!.x = node.parent.frame!.x;
    } else {
      // set x by left frame. left frame is parent.children[idx - 1]
      node.frame.x = node.parent.children[idx - 1].frame!.x + node.parent.children[idx - 1].frame!.width;
    }
    switch (mode) {
      case ChartMode.Byte:
        node.frame!.width = Math.floor(((node.drawSize || node.size) / total) * canvas_frame.width);
        break;
      case ChartMode.Count:
        node.frame!.width = Math.floor(((node.drawCount || node.count) / total) * canvas_frame.width);
        break;
      case ChartMode.Duration:
        node.frame!.width = Math.floor(((node.drawDur || node.dur) / total) * canvas_frame.width);
        break;
      default:
        warn('not match ChartMode');
    }
    node.frame!.y = node.parent.frame!.y + 20;
    node.frame!.height = 20;
  }
}

/**
 * draw rect
 * @param frameChartBeanCanvasCtx CanvasRenderingContext2D
 * @param frameChartBeanData rect which is need draw
 * @param percent function size or count / total size or count
 */
export function draw(frameChartBeanCanvasCtx: CanvasRenderingContext2D, frameChartBeanData: ChartStruct) {
  let spApplication = <SpApplication>document.getElementsByTagName('sp-application')[0];
  if (frameChartBeanData.frame) {
    // draw rect
    let frameChartMiniHeight = 20;
    if (isSelected(frameChartBeanData)) {
      frameChartBeanCanvasCtx.fillStyle = `rgba(${lightBlue.r}, ${lightBlue.g}, ${lightBlue.b}, ${lightBlue.a})`;
    } else {
      let color = getHeatColor(frameChartBeanData.percent);
      frameChartBeanCanvasCtx.fillStyle = `rgba(${color.r}, ${color.g}, ${color.b}, 0.9)`;
    }
    frameChartBeanCanvasCtx.fillRect(frameChartBeanData.frame.x, frameChartBeanData.frame.y, frameChartBeanData.frame.width, frameChartMiniHeight - padding * 2);
    //draw border
    frameChartBeanCanvasCtx.lineWidth = 0.4;
    if (isHover(frameChartBeanData)) {
      if (spApplication.dark) {
        frameChartBeanCanvasCtx.strokeStyle = '#fff';
      } else {
        frameChartBeanCanvasCtx.strokeStyle = '#000';
      }
    } else {
      if (spApplication.dark) {
        frameChartBeanCanvasCtx.strokeStyle = '#000';
      } else {
        frameChartBeanCanvasCtx.strokeStyle = '#fff';
      }
      if (frameChartBeanData.isSearch) {
        frameChartBeanCanvasCtx.strokeStyle = `rgb(${lightBlue.r}, ${lightBlue.g}, ${lightBlue.b})`;
        frameChartBeanCanvasCtx.lineWidth = 1;
      }
    }
    frameChartBeanCanvasCtx.strokeRect(frameChartBeanData.frame.x, frameChartBeanData.frame.y, frameChartBeanData.frame.width, frameChartMiniHeight - padding * 2);

    //draw symbol name
    if (frameChartBeanData.frame.width > 10) {
      if (frameChartBeanData.percent > 0.6 || isSelected(frameChartBeanData)) {
        frameChartBeanCanvasCtx.fillStyle = '#fff';
      } else {
        frameChartBeanCanvasCtx.fillStyle = '#000';
      }
      drawString(frameChartBeanCanvasCtx, frameChartBeanData.symbol || '', 5, frameChartBeanData.frame, frameChartBeanData);
    }
    frameChartBeanData.isDraw = true;
  }
}

/**
 * get frame chart color by percent
 * @param widthPercentage proportion of function
 * @returns rbg
 */
function getHeatColor(widthPercentage: number) {
  return {
    r: Math.floor(245 + 10 * (1 - widthPercentage)),
    g: Math.floor(110 + 105 * (1 - widthPercentage)),
    b: 100,
  };
}

function isHover(data: ChartStruct): boolean {
  return ChartStruct.hoverFuncStruct == data;
}

function isSelected(data: ChartStruct): boolean {
  return ChartStruct.lastSelectFuncStruct == data;
}

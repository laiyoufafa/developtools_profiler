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
 * @param ctx CanvasRenderingContext2D
 * @param data rect which is need draw
 * @param percent function size or count / total size or count
 */
export function draw(ctx: CanvasRenderingContext2D, data: ChartStruct) {
  let spApplication = <SpApplication>document.getElementsByTagName('sp-application')[0];
  if (data.frame) {
    // draw rect
    let miniHeight = 20;
    if (isSelected(data)) {
      ctx.fillStyle = `rgba(${lightBlue.r}, ${lightBlue.g}, ${lightBlue.b}, ${lightBlue.a})`;
    } else {
      let color = getHeatColor(data.percent);
      ctx.fillStyle = `rgba(${color.r}, ${color.g}, ${color.b}, 0.9)`;
    }
    ctx.fillRect(data.frame.x, data.frame.y, data.frame.width, miniHeight - padding * 2);
    //draw border
    ctx.lineWidth = 0.4;
    if (isHover(data)) {
      if (spApplication.dark) {
        ctx.strokeStyle = '#fff';
      } else {
        ctx.strokeStyle = '#000';
      }
    } else {
      if (spApplication.dark) {
        ctx.strokeStyle = '#000';
      } else {
        ctx.strokeStyle = '#fff';
      }
      if (data.isSearch) {
        ctx.strokeStyle = `rgb(${lightBlue.r}, ${lightBlue.g}, ${lightBlue.b})`;
        ctx.lineWidth = 1;
      }
    }
    ctx.strokeRect(data.frame.x, data.frame.y, data.frame.width, miniHeight - padding * 2);

    //draw symbol name
    if (data.frame.width > 10) {
      if (data.percent > 0.6 || isSelected(data)) {
        ctx.fillStyle = '#fff';
      } else {
        ctx.fillStyle = '#000';
      }
      drawString(ctx, data.symbol || '', 5, data.frame, data);
    }
    data.isDraw = true;
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

/**
 * draw function string in rect
 * @param ctx CanvasRenderingContext2D
 * @param str function Name
 * @param textPadding textPadding
 * @param frame canvas area
 * @returns is draw
 */
function drawString(
  ctx: CanvasRenderingContext2D,
  str: string,
  textPadding: number,
  frame: Rect,
  struct: ChartStruct
): boolean {
  if (!struct.textMetricsWidth) {
    struct.textMetricsWidth = ctx.measureText(str).width;
  }

  let charWidth = Math.round(struct.textMetricsWidth / str.length);
  let fillTextWidth = frame.width - textPadding * 2;
  if (struct.textMetricsWidth < frame.width - textPadding * 2) {
    let x2 = Math.floor(frame.width / 2 - struct.textMetricsWidth / 2 + frame.x + textPadding);
    ctx.fillText(str, x2, Math.floor(frame.y + frame.height / 2 + 2), fillTextWidth);
    return true;
  } else {
    if (fillTextWidth >= charWidth) {
      let chatNum = fillTextWidth / charWidth;
      let x1 = frame.x + textPadding;
      if (chatNum < 2) {
        ctx.fillText(str.substring(0, 1), x1, Math.floor(frame.y + frame.height / 2 + 2), fillTextWidth);
      } else {
        ctx.fillText(
          str.substring(0, chatNum - 1) + '...',
          x1,
          Math.floor(frame.y + frame.height / 2 + 2),
          fillTextWidth
        );
      }
      return true;
    }
  }
  return false;
}

function isHover(data: ChartStruct): boolean {
  return ChartStruct.hoverFuncStruct == data;
}

function isSelected(data: ChartStruct): boolean {
  return ChartStruct.lastSelectFuncStruct == data;
}

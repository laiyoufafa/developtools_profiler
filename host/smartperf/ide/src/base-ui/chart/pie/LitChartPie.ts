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

import { resizeCanvas } from '../helper.js';
import { BaseElement, element } from '../../BaseElement.js';
import { LitChartPieConfig } from './LitChartPieConfig.js';
import { isPointIsCircle, pieChartColors, randomRgbColor } from './LitChartPieData.js';
import { Utils } from '../../../trace/component/trace/base/Utils.js';

interface Rectangle {
  x: number;
  y: number;
  w: number;
  h: number;
}

class Sector {
  id?: any;
  obj?: any;
  key: any;
  value: any;
  startAngle?: number;
  endAngle?: number;
  startDegree?: number;
  endDegree?: number;
  color?: string;
  percent?: number;
  hover?: boolean;
  ease?: {
    initVal?: number;
    step?: number;
    process?: boolean;
  };
}

@element('lit-chart-pie')
export class LitChartPie extends BaseElement {
  private eleShape: Element | null | undefined;
  private tipEL: HTMLDivElement | null | undefined;
  private labelsEL: HTMLDivElement | null | undefined;
  canvas: HTMLCanvasElement | undefined | null;
  ctx: CanvasRenderingContext2D | undefined | null;
  cfg: LitChartPieConfig | null | undefined;
  centerX: number | null | undefined;
  centerY: number | null | undefined;
  data: Sector[] = [];
  radius: number | undefined;
  private textRects: Rectangle[] = [];

  set config(cfg: LitChartPieConfig | null | undefined) {
    if (!cfg) return;
    this.cfg = cfg;
    (this.shadowRoot!.querySelector('#root') as HTMLDivElement).className =
      cfg && cfg.data.length > 0 ? 'bg_hasdata' : 'bg_nodata';
    this.measure();
    this.render();
  }

  set dataSource(arr: any[]) {
    if (this.cfg) {
      this.cfg.data = arr;
      this.measure();
      this.render();
    }
  }

  showHover() {
    let hasHover = false;
    this.data.forEach((it) => {
      it.hover = it.obj.isHover;
      if (it.hover) {
        hasHover = true;
      }
      this.updateHoverItemStatus(it);
      if (it.hover) {
        this.showTip(
          this.centerX || 0,
          this.centerY || 0,
          this.cfg!.tip ? this.cfg!.tip(it) : `${it.key}: ${it.value}`
        );
      }
    });
    if (!hasHover) {
      this.hideTip();
    }
    this.render();
  }

  measure() {
    if (!this.cfg) return;
    this.data = [];
    this.radius = (Math.min(this.clientHeight, this.clientWidth) * 0.65) / 2 - 10;
    let cfg = this.cfg!;
    let startAngle = 0;
    let startDegree = 0;
    let full = Math.PI / 180; //每度
    let fullDegree = 0; //每度
    let sum = this.cfg.data.reduce((previousValue, currentValue) => currentValue[cfg.angleField] + previousValue, 0);
    this.labelsEL!.textContent = '';
    let labelArray: string[] = [];
    this.cfg.data.forEach((it, index) => {
      let item: Sector = {
        id: `id-${Utils.uuid()}`,
        color: this.cfg!.label.color ? this.cfg!.label.color(it) : pieChartColors[index % pieChartColors.length],
        obj: it,
        key: it[cfg.colorField],
        value: it[cfg.angleField],
        startAngle: startAngle,
        endAngle: startAngle + full * ((it[cfg.angleField] / sum) * 360),
        startDegree: startDegree,
        endDegree: startDegree + fullDegree + (it[cfg.angleField] / sum) * 360,
        ease: {
          initVal: 0,
          step: (startAngle + full * ((it[cfg.angleField] / sum) * 360)) / startDegree,
          process: true,
        },
      };
      this.data.push(item);
      startAngle += full * ((it[cfg.angleField] / sum) * 360);
      startDegree += fullDegree + (it[cfg.angleField] / sum) * 360;
      labelArray.push(`<label class="label">
                    <div style="display: flex;flex-direction: row;margin-left: 5px;align-items: center;overflow: hidden;text-overflow: ellipsis" 
                        id="${item.id}">
                        <div class="tag" style="background-color: ${item.color}"></div>
                        <span class="name">${item.obj[cfg.colorField]}</span>
                    </div>
                </label>`);
    });
    this.labelsEL!.innerHTML = labelArray.join('');
  }

  get config(): LitChartPieConfig | null | undefined {
    return this.cfg;
  }

  connectedCallback() {
    super.connectedCallback();
    this.eleShape = this.shadowRoot!.querySelector<Element>('#shape');
    this.tipEL = this.shadowRoot!.querySelector<HTMLDivElement>('#tip');
    this.labelsEL = this.shadowRoot!.querySelector<HTMLDivElement>('#labels');
    this.canvas = this.shadowRoot!.querySelector<HTMLCanvasElement>('#canvas');
    this.ctx = this.canvas!.getContext('2d', { alpha: true });
    resizeCanvas(this.canvas!);
    this.radius = (Math.min(this.clientHeight, this.clientWidth) * 0.65) / 2 - 10;
    this.centerX = this.clientWidth / 2;
    this.centerY = this.clientHeight / 2 - 40;
    this.ctx?.translate(this.centerX, this.centerY);
    this.canvas!.onmouseout = (e) => {
      this.hideTip();
      this.data.forEach((it) => {
        it.hover = false;
        this.updateHoverItemStatus(it);
      });
      this.render();
    };
    //增加点击事件
    this.canvas!.onclick = (ev) => {
      let rect = this.getBoundingClientRect();
      let x = ev.pageX - rect.left - this.centerX!;
      let y = ev.pageY - rect.top - this.centerY!;
      if (isPointIsCircle(0, 0, x, y, this.radius!)) {
        let degree = this.computeDegree(x, y);
        this.data.forEach((it) => {
          if (degree >= it.startDegree! && degree <= it.endDegree!) {
            this.config?.angleClick?.(it.obj);
          }
        });
      }
    };
    this.canvas!.onmousemove = (ev) => {
      let rect = this.getBoundingClientRect();
      let x = ev.pageX - rect.left - this.centerX!;
      let y = ev.pageY - rect.top - this.centerY!;
      if (isPointIsCircle(0, 0, x, y, this.radius!)) {
        let degree = this.computeDegree(x, y);
        this.data.forEach((it) => {
          it.hover = degree >= it.startDegree! && degree <= it.endDegree!;
          this.updateHoverItemStatus(it);
          it.obj.isHover = it.hover;
          if (it.hover) {
            this.cfg!.hoverHandler?.(it.obj);
            this.showTip(
              ev.pageX - rect.left + 10,
              ev.pageY - this.offsetTop - 10,
              this.cfg!.tip ? this.cfg!.tip(it) : `${it.key}: ${it.value}`
            );
          }
        });
      } else {
        this.hideTip();
        this.data.forEach((it) => {
          it.hover = false;
          it.obj.isHover = false;
          this.updateHoverItemStatus(it);
        });
        this.cfg!.hoverHandler?.(undefined);
      }
      this.render();
    };
    this.render();
  }

  updateHoverItemStatus(item: any) {
    let label = this.shadowRoot!.querySelector(`#${item.id}`);
    if (label) {
      (label as HTMLLabelElement).style.boxShadow = item.hover ? '0 0 5px #22ffffff' : '';
    }
  }

  computeDegree(x: number, y: number) {
    let degree = (360 * Math.atan(y / x)) / (2 * Math.PI);
    if (x >= 0 && y >= 0) {
      degree = degree;
    } else if (x < 0 && y >= 0) {
      degree = 180 + degree;
    } else if (x < 0 && y < 0) {
      degree = 180 + degree;
    } else {
      degree = 270 + (90 + degree);
    }
    return degree;
  }

  initElements(): void {
    new ResizeObserver((entries, observer) => {
      entries.forEach((it) => {
        resizeCanvas(this.canvas!);
        this.centerX = this.clientWidth / 2;
        this.centerY = this.clientHeight / 2 - 40;
        this.ctx?.translate(this.centerX, this.centerY);
        this.measure();
        this.render();
      });
    }).observe(this);
  }

  render(ease: boolean = true) {
    if (!this.canvas || !this.cfg) return;
    if (this.radius! <= 0) return;
    this.ctx?.clearRect(0 - this.centerX!, 0 - this.centerY!, this.clientWidth, this.clientHeight);
    this.data.forEach((it) => {
      this.ctx!.beginPath();
      this.ctx!.fillStyle = it.color as string;
      this.ctx!.strokeStyle = this.data.length > 1 ? '#fff' : (it.color as string);
      this.ctx?.moveTo(0, 0);
      if (it.hover) {
        this.ctx!.lineWidth = 1;
        this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
      } else {
        this.ctx!.lineWidth = 1;
        if (ease) {
          if (it.ease!.initVal! < it.endAngle! - it.startAngle!) {
            it.ease!.process = true;
            this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.startAngle! + it.ease!.initVal!, false);
            it.ease!.initVal! += it.ease!.step!;
          } else {
            it.ease!.process = false;
            this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
          }
        } else {
          this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
        }
      }
      this.ctx?.lineTo(0, 0);
      this.ctx?.fill();
      this.ctx!.stroke();
      this.ctx?.closePath();
    });

    this.data
      .filter((it) => it.hover)
      .forEach((it) => {
        this.ctx!.beginPath();
        this.ctx!.fillStyle = it.color as string;
        this.ctx!.lineWidth = 1;
        this.ctx?.moveTo(0, 0);
        this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
        this.ctx?.lineTo(0, 0);
        this.ctx!.strokeStyle = this.data.length > 1 ? '#000' : (it.color as string);
        this.ctx!.stroke();
        this.ctx?.closePath();
      });

    this.textRects = [];
    if (this.cfg.showChartLine) {
      this.data.forEach((it) => {
        let text = `${it.value}`;
        let metrics = this.ctx!.measureText(text);
        let textWidth = metrics.width;
        let textHeight = metrics.fontBoundingBoxAscent + metrics.fontBoundingBoxDescent;
        this.ctx!.beginPath();
        this.ctx!.strokeStyle = it.color!;
        this.ctx!.fillStyle = '#595959';
        let deg = it.startDegree! + (it.endDegree! - it.startDegree!) / 2;
        let dep = 25;
        let x1 = 0 + this.radius! * Math.cos((deg * Math.PI) / 180);
        let y1 = 0 + this.radius! * Math.sin((deg * Math.PI) / 180);
        let x2 = 0 + (this.radius! + 13) * Math.cos((deg * Math.PI) / 180);
        let y2 = 0 + (this.radius! + 13) * Math.sin((deg * Math.PI) / 180);
        let x3 = 0 + (this.radius! + dep) * Math.cos((deg * Math.PI) / 180);
        let y3 = 0 + (this.radius! + dep) * Math.sin((deg * Math.PI) / 180);
        this.ctx!.moveTo(x1, y1);
        this.ctx!.lineTo(x2, y2);
        this.ctx!.stroke();
        let rect = this.correctRect({
          x: x3 - textWidth / 2,
          y: y3 + textHeight / 2,
          w: textWidth,
          h: textHeight,
        });
        this.ctx?.fillText(text, rect.x, rect.y);
        this.ctx?.closePath();
      });
    }
    if (this.data.filter((it) => it.ease!.process).length > 0) {
      requestAnimationFrame(() => this.render(ease));
    }
  }

  correctRect(rect: Rectangle): Rectangle {
    if (this.textRects.length == 0) {
      this.textRects.push(rect);
      return rect;
    } else {
      let rectangles = this.textRects.filter((it) => this.intersect(it, rect).cross);
      if (rectangles.length == 0) {
        this.textRects.push(rect);
        return rect;
      } else {
        let it = rectangles[0];
        let inter = this.intersect(it, rect);
        if (inter.direction == 'Right') {
          rect.x += inter.crossW;
        } else if (inter.direction == 'Bottom') {
          rect.y += inter.crossH;
        } else if (inter.direction == 'Left') {
          rect.x -= inter.crossW;
        } else if (inter.direction == 'Top') {
          rect.y -= inter.crossH;
        } else if (inter.direction == 'Right-Top') {
          rect.y -= inter.crossH;
        } else if (inter.direction == 'Right-Bottom') {
          rect.y += inter.crossH;
        } else if (inter.direction == 'Left-Top') {
          rect.y -= inter.crossH;
        } else if (inter.direction == 'Left-Bottom') {
          rect.y += inter.crossH;
        }
        this.textRects.push(rect);
        return rect;
      }
    }
  }

  intersect(
    r1: Rectangle,
    rect: Rectangle
  ): {
    cross: boolean;
    direction: string;
    crossW: number;
    crossH: number;
  } {
    let cross: boolean;
    let direction: string;
    let crossW: number;
    let crossH: number;
    let maxX = r1.x + r1.w > rect.x + rect.w ? r1.x + r1.w : rect.x + rect.w;
    let maxY = r1.y + r1.h > rect.y + rect.h ? r1.y + r1.h : rect.y + rect.h;
    let minX = r1.x < rect.x ? r1.x : rect.x;
    let minY = r1.y < rect.y ? r1.y : rect.y;
    if (maxX - minX < rect.w + r1.w && maxY - minY < r1.h + rect.h) {
      cross = true;
    } else {
      cross = false;
    }
    crossW = Math.abs(maxX - minX - (rect.w + r1.w));
    crossH = Math.abs(maxY - minY - (rect.y + r1.y));
    if (rect.x > r1.x) {
      //right
      if (rect.y > r1.y) {
        //bottom
        direction = 'Right-Bottom';
      } else if (rect.y == r1.y) {
        //middle
        direction = 'Right';
      } else {
        //top
        direction = 'Right-Top';
      }
    } else if (rect.x < r1.x) {
      //left
      if (rect.y > r1.y) {
        //bottom
        direction = 'Left-Bottom';
      } else if (rect.y == r1.y) {
        //middle
        direction = 'Left';
      } else {
        //top
        direction = 'Left-Top';
      }
    } else {
      if (rect.y > r1.y) {
        //bottom
        direction = 'Bottom';
      } else if (rect.y == r1.y) {
        //middle
        direction = 'Right'; //superposition default right
      } else {
        //top
        direction = 'Top';
      }
    }
    return {
      cross,
      direction,
      crossW,
      crossH,
    };
  }

  showTip(x: number, y: number, msg: string) {
    this.tipEL!.style.display = 'flex';
    this.tipEL!.style.top = `${y}px`;
    this.tipEL!.style.left = `${x}px`;
    this.tipEL!.innerHTML = msg;
  }

  hideTip() {
    this.tipEL!.style.display = 'none';
  }

  initHtml(): string {
    return `
        <style>   
        :host {
            display: flex;
            flex-direction: column;
            overflow: hidden;
            width: 100%;
            height: 100%;
        }
        .shape.active {
            animation: color 3.75 both;    
        }
        @keyframes color {
            0% { background-color: white; }
           100% { background-color: black; }    
        }
        #tip{
            background-color: #f5f5f4;
            border: 1px solid #fff;
            border-radius: 5px;
            color: #333322;
            font-size: 8pt;
            position: absolute;
            display: none;
            top: 0;
            left: 0;
            z-index: 99;
            pointer-events: none;
            user-select: none;
            padding: 5px 10px;
            box-shadow: 0 0 10px #22ffffff;
        }
        #root{
            position:relative;
        }
        .bg_nodata{
            background-repeat:no-repeat;
            background-position:center;
            background-image: url("img/pie_chart_no_data.png");
        }
        .bg_hasdata{
            background-repeat:no-repeat;
            background-position:center;
        }
        
        #labels{
            display: grid;
            grid-template-columns: auto auto auto auto auto;
            /*justify-content: center;*/
            /*align-items: center;*/
            width: 100%;
            height: 25%;
            box-sizing: border-box;
            position: absolute;
            bottom: 0px;
            left: 0;
            /*margin: 0px 10px;*/
            padding-left: 10px;
            padding-right: 10px;
            pointer-events: none    ;
        }
        .name{
            flex: 1;
            font-size: 9pt;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            /*color: #666;*/
            color: var(--dark-color1,#252525);
            pointer-events: painted;
        }
        .label{
            display: flex;
            align-items: center;
            max-lines: 1;
            white-space: nowrap;
            overflow: hidden;
            padding-right: 5px;
        }
        .tag{
            display: flex;
            align-items: center;
            justify-content: center;
            width: 10px;
            height: 10px;
            border-radius: 5px;
            margin-right: 5px;
        }
        </style>
        <div id="root">
            <div id="shape" class="shape active"></div>
            <canvas id="canvas" style="top: 0;left: 0;z-index: 21"></canvas>
            <div id="tip"></div>
            <div id="labels"></div>
        </div>`;
  }
}

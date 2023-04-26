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

import { BaseElement, element } from '../../BaseElement.js';
import { LitChartColumnConfig } from './LitChartColumnConfig.js';
import { resizeCanvas } from '../helper.js';

class Pillar {
    obj?: any;
    xLabel?: string;
    yLabel?: string;
    type?: string;
    root?: boolean;
    bgFrame?: {
        x: number;
        y: number;
        w: number;
        h: number;
    };
    frame?: {
        x: number;
        y: number;
        w: number;
        h: number;
    };
    height?: number;
    process?: boolean;
    heightStep?: number;
    centerX?: number;
    centerY?: number;
    color?: string;
    hover?: boolean;
}

interface RLine {
    label: string;
    y: number;
}

@element('lit-chart-column')
export class LitChartColumn extends BaseElement {
    private tipEL: HTMLDivElement | null | undefined;
    canvas: HTMLCanvasElement | undefined | null;
    ctx: CanvasRenderingContext2D | undefined | null;
    cfg: LitChartColumnConfig | null | undefined;
    offset?: { x: number | undefined; y: number | undefined };
    data: Pillar[] = [];
    rowLines: RLine[] = [];

    connectedCallback() {
        super.connectedCallback();
        this.tipEL = this.shadowRoot!.querySelector<HTMLDivElement>('#tip');
        this.canvas =
            this.shadowRoot!.querySelector<HTMLCanvasElement>('#canvas');
        this.ctx = this.canvas!.getContext('2d', { alpha: true });
        resizeCanvas(this.canvas!);
        this.offset = { x: 40, y: 20 };
        this.canvas!.onmouseout = (e) => {
            this.hideTip();
            this.data.forEach((it) => (it.hover = false));
            this.render();
        };
        this.canvas!.onmousemove = (ev) => {
            let rect = this.getBoundingClientRect();
            let x = ev.pageX - rect.left;
            let y = ev.pageY - rect.top;
            this.data.forEach((it) => {
                if (contains(it.bgFrame!, x, y)) {
                    it.hover = true;
                    this.cfg?.hoverHandler?.(it.obj.no);
                } else {
                    it.hover = false;
                }
            });
            let pillars = this.data.filter((it) => it.hover);
            if (this.cfg?.seriesField) {
                if (pillars.length > 0) {
                    let title = `<label>${this.cfg.xField}: ${pillars[0].xLabel}</label>`;
                    let msg = pillars
                        .map((it) => `<label>${it.type}: ${it.yLabel}</label>`)
                        .join('');
                    let sum = `<label>Total: ${pillars
                        .map((it) => it.obj[this.cfg?.yField!])
                        .reduce((pre, current) => pre + current, 0)}</label>`;
                    let innerHtml = `<div class="tip-content">${title}${msg}${sum}</div>`;
                    if (x >= this.clientWidth - this.tipEL!.clientWidth) {
                        this.showTip(
                            x - this.tipEL!.clientWidth - 10,
                            y - 20,
                            this.cfg!.tip ? this.cfg!.tip(pillars) : innerHtml
                        );
                    } else {
                        this.showTip(
                            x + 10,
                            y - 20,
                            this.cfg!.tip ? this.cfg!.tip(pillars) : innerHtml
                        );
                    }
                }
            } else {
                if (pillars.length > 0) {
                    let title = `<label>${pillars[0].xLabel}:${pillars[0].yLabel}</label>`;
                    let innerHtml = `<div class="tip-content">${title}</div>`;
                    if (x >= this.clientWidth - this.tipEL!.clientWidth) {
                        this.showTip(
                            x - this.tipEL!.clientWidth - 10,
                            y - 20,
                            this.cfg!.tip ? this.cfg!.tip(pillars) : innerHtml
                        );
                    } else {
                        this.showTip(
                            x + 10,
                            y - 20,
                            this.cfg!.tip ? this.cfg!.tip(pillars) : innerHtml
                        );
                    }
                }
            }

            if (this.data.filter((it) => it.process).length == 0) {
                this.render();
            }
        };
        this.render();
    }

    showHoverColumn(index: number) {
        this.data.forEach((it) => {
            if (it.obj.no === index) {
                it.hover = true;
            } else {
                it.hover = false;
            }
        });
        let pillars = this.data.filter((it) => it.hover);
        if (this.cfg?.seriesField) {
            if (pillars.length > 0) {
                let hoverData = pillars[0];
                let title = `<label>${this.cfg.xField}: ${pillars[0].xLabel}</label>`;
                let msg = pillars
                    .map((it) => `<label>${it.type}: ${it.yLabel}</label>`)
                    .join('');
                let sum = `<label>Total: ${pillars
                    .map((it) => it.obj[this.cfg?.yField!])
                    .reduce((pre, current) => pre + current, 0)}</label>`;
                let innerHtml = `<div class="tip-content">${title}${msg}${sum}</div>`;
                this.showTip(
                    this.clientWidth/2,
                    this.clientHeight/2,
                    this.cfg!.tip ? this.cfg!.tip(pillars) : innerHtml
                );
            }
        } else {
            if (pillars.length > 0) {
                let hoverData = pillars[0];
                let title = `<label>${pillars[0].xLabel}:${pillars[0].yLabel}</label>`;
                let innerHtml = `<div class="tip-content">${title}</div>`;
                this.showTip(
                    this.clientWidth/2,
                    this.clientHeight/2,
                    this.cfg!.tip ? this.cfg!.tip(pillars) : innerHtml
                );
            }
        }

        if (this.data.filter((it) => it.process).length == 0) {
            this.render();
        }
    }

    initElements(): void {
        new ResizeObserver((entries, observer) => {
            entries.forEach((it) => {
                resizeCanvas(this.canvas!);
                this.measure();
                this.render(false);
            });
        }).observe(this);
    }

    set config(cfg: LitChartColumnConfig | null | undefined) {
        if (!cfg) return;
        this.cfg = cfg;
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

    get dataSource() {
        return this.cfg?.data || [];
    }

    measure() {
        if (!this.cfg) return;
        this.data = [];
        this.rowLines = [];
        if (!this.cfg.seriesField) {
            let maxValue = Math.max(
                ...this.cfg.data.map((it) => it[this.cfg!.yField])
            );
            maxValue = Math.ceil(maxValue * 0.1) * 10;
            let partWidth =
                (this.clientWidth - this.offset!.x!) / this.cfg.data.length;
            let partHeight = this.clientHeight - this.offset!.y!;
            let gap = partHeight / 5;
            let valGap = maxValue / 5;
            for (let i = 0; i <= 5; i++) {
                this.rowLines.push({
                    y: gap * i,
                    label: `${maxValue - valGap * i} `,
                });
            }
            this.cfg?.data
                .sort((a, b) => b[this.cfg!.yField] - a[this.cfg!.yField])
                .forEach((it, i, array) => {
                    this.data.push({
                        color: this.cfg!.color(it),
                        obj: it,
                        root: true,
                        xLabel: it[this.cfg!.xField],
                        yLabel: it[this.cfg!.yField],
                        bgFrame: {
                            x: this.offset!.x! + partWidth * i,
                            y: 0,
                            w: partWidth,
                            h: partHeight,
                        },
                        centerX:
                            this.offset!.x! + partWidth * i + partWidth / 2,
                        centerY:
                            partHeight -
                            (it[this.cfg!.yField] * partHeight) / maxValue +
                            (it[this.cfg!.yField] * partHeight) / maxValue / 2,
                        frame: {
                            x: this.offset!.x! + partWidth * i + partWidth / 6,
                            y:
                                partHeight -
                                (it[this.cfg!.yField] * partHeight) / maxValue,
                            w: partWidth - partWidth / 3,
                            h: (it[this.cfg!.yField] * partHeight) / maxValue,
                        },
                        height: 0,
                        heightStep: Math.ceil(
                            (it[this.cfg!.yField] * partHeight) / maxValue / 60
                        ),
                        process: true,
                    });
                });
        } else {
            let reduceGroup = this.cfg.data.reduce(
                (pre, current, index, arr) => {
                    (pre[current[this.cfg!.xField]] =
                        pre[current[this.cfg!.xField]] || []).push(current);
                    return pre;
                },
                {}
            );
            let sums = Reflect.ownKeys(reduceGroup).map((k) =>
                (reduceGroup[k] as any[]).reduce(
                    (pre, current) => pre + current[this.cfg!.yField],
                    0
                )
            );
            let maxValue = Math.ceil(Math.max(...sums) * 0.1) * 10;
            let partWidth =
                (this.clientWidth - this.offset!.x!) /
                Reflect.ownKeys(reduceGroup).length;
            let partHeight = this.clientHeight - this.offset!.y!;
            let gap = partHeight / 5;
            let valGap = maxValue / 5;
            for (let i = 0; i <= 5; i++) {
                this.rowLines.push({
                    y: gap * i,
                    label: `${maxValue - valGap * i} `,
                });
            }
            Reflect.ownKeys(reduceGroup)
                .sort(
                    (b, a) =>
                        (reduceGroup[a] as any[]).reduce(
                            (pre, cur) =>
                                pre + (cur[this.cfg!.yField] as number),
                            0
                        ) -
                        (reduceGroup[b] as any[]).reduce(
                            (pre, cur) =>
                                pre + (cur[this.cfg!.yField] as number),
                            0
                        )
                )
                .forEach((key, i) => {
                    let elements = reduceGroup[key];
                    let initH = 0;
                    elements.forEach((it: any, y: number) => {
                        this.data.push({
                            color: this.cfg!.color(it),
                            obj: it,
                            root: y == 0,
                            type: it[this.cfg!.seriesField],
                            xLabel: it[this.cfg!.xField],
                            yLabel: it[this.cfg!.yField],
                            bgFrame: {
                                x: this.offset!.x! + partWidth * i,
                                y: 0,
                                w: partWidth,
                                h: partHeight,
                            },
                            centerX:
                                this.offset!.x! + partWidth * i + partWidth / 2,
                            centerY:
                                partHeight -
                                initH -
                                (it[this.cfg!.yField] * partHeight) / maxValue +
                                (it[this.cfg!.yField] * partHeight) /
                                    maxValue /
                                    2,
                            frame: {
                                x:
                                    this.offset!.x! +
                                    partWidth * i +
                                    partWidth / 6,
                                y:
                                    partHeight -
                                    (it[this.cfg!.yField] * partHeight) /
                                        maxValue -
                                    initH,
                                w: partWidth - partWidth / 3,
                                h:
                                    (it[this.cfg!.yField] * partHeight) /
                                    maxValue,
                            },
                            height: 0,
                            heightStep: Math.ceil(
                                (it[this.cfg!.yField] * partHeight) /
                                    maxValue /
                                    60
                            ),
                            process: true,
                        });
                        initH += (it[this.cfg!.yField] * partHeight) / maxValue;
                    });
                });
        }
    }

    get config(): LitChartColumnConfig | null | undefined {
        return this.cfg;
    }

    render(ease: boolean = true) {
        if (!this.canvas || !this.cfg) return;
        this.ctx!.clearRect(0, 0, this.clientWidth, this.clientHeight);
        this.drawLine(this.ctx!);
        this.data?.forEach((it) => this.drawColumn(this.ctx!, it, ease));
        if (ease) {
            if (this.data.filter((it) => it.process).length > 0) {
                requestAnimationFrame(() => this.render(ease));
            }
        }
    }

    drawLine(c: CanvasRenderingContext2D) {
        c.strokeStyle = '#dfdfdf';
        c.lineWidth = 1;
        c.beginPath();
        c.fillStyle = '#8c8c8c';
        this.rowLines.forEach((it, i) => {
            c.moveTo(this.offset!.x!, it.y);
            c.lineTo(this.clientWidth, it.y);
            if (i == 0) {
                c.fillText(
                    it.label,
                    this.offset!.x! - c.measureText(it.label).width - 2,
                    it.y + 11
                );
            } else {
                c.fillText(
                    it.label,
                    this.offset!.x! - c.measureText(it.label).width - 2,
                    it.y + 4
                );
            }
        });
        c.stroke();
        c.closePath();
    }

    drawColumn(c: CanvasRenderingContext2D, it: Pillar, ease: boolean) {
        if (it.hover) {
            c.globalAlpha = 0.2;
            c.fillStyle = '#999999';
            c.fillRect(
                it.bgFrame!.x,
                it.bgFrame!.y,
                it.bgFrame!.w,
                it.bgFrame!.h
            );
            c.globalAlpha = 1.0;
        }
        c.fillStyle = it.color || '#ff0000';
        if (ease) {
            if (it.height! < it.frame!.h) {
                it.process = true;
                c.fillRect(
                    it.frame!.x,
                    it.frame!.y + (it.frame!.h - it.height!),
                    it.frame!.w,
                    it.height!
                );
                it.height! += it.heightStep!;
            } else {
                c.fillRect(it.frame!.x, it.frame!.y, it.frame!.w, it.frame!.h);
                it.process = false;
            }
        } else {
            c.fillRect(it.frame!.x, it.frame!.y, it.frame!.w, it.frame!.h);
            it.process = false;
        }

        c.beginPath();
        c.strokeStyle = '#d8d8d8';
        c.moveTo(it.centerX!, it.frame!.y + it.frame!.h!);
        if (it.root) {
            c.lineTo(it.centerX!, it.frame!.y + it.frame!.h + 4);
        }
        let xMetrics = c.measureText(it.xLabel!);
        let xMetricsH =
            xMetrics.actualBoundingBoxAscent +
            xMetrics.actualBoundingBoxDescent;
        let yMetrics = c.measureText(it.yLabel!);
        let yMetricsH =
            yMetrics.fontBoundingBoxAscent + yMetrics.fontBoundingBoxDescent;
        c.fillStyle = '#8c8c8c';
        if (it.root) {
            c.fillText(
                it.xLabel!,
                it.centerX! - xMetrics.width / 2,
                it.frame!.y + it.frame!.h + 15
            );
        }
        c.fillStyle = '#fff';
        if (this.cfg?.label) {
            if (yMetricsH < it.frame!.h) {
				// @ts-ignore
                c.fillText(this.cfg!.label!.content ? this.cfg!.label!.content(it.obj) : it.yLabel!,
                    it.centerX! - yMetrics.width / 2,
                    it.centerY! + (it.frame!.h - it.height!) / 2
                );
            }
        }
        c.stroke();
        c.closePath();
    }

    beginPath(stroke: boolean, fill: boolean) {
        return (fn: (c: CanvasRenderingContext2D) => void) => {
            this.ctx!.beginPath();
            fn?.(this.ctx!);
            if (stroke) {
                this.ctx!.stroke();
            }
            if (fill) {
                this.ctx!.fill();
            }
            this.ctx!.closePath();
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
            width: 100%;
            height: 100%;
        }
        #tip{
            background-color: #f5f5f4;
            border: 1px solid #fff;
            border-radius: 5px;
            color: #333322;
            font-size: 8pt;
            position: absolute;
            min-width: max-content;
            display: none;
            top: 0;
            left: 0;
            pointer-events: none;
            user-select: none;
            padding: 5px 10px;
            box-shadow: 0 0 10px #22ffffff;
            /*transition: left;*/
            /*transition-duration: 0.3s;*/
        }
        #root{
            position:relative;
        }
        .tip-content{
            display: flex;
            flex-direction: column;
        }
        </style>
        <div id="root">
            <canvas id="canvas"></canvas>
            <div id="tip"></div>
        </div>`;
    }
}

function contains(
    rect: { x: number; y: number; w: number; h: number },
    x: number,
    y: number
): boolean {
    return (
        rect.x <= x &&
        x <= rect.x + rect.w &&
        rect.y <= y &&
        y <= rect.y + rect.h
    );
}

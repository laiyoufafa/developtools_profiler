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

import {resizeCanvas} from "../helper.js";
import {BaseElement, element} from "../../BaseElement.js";
import {LitChartPieConfig} from "./LitChartPieConfig.js";
import {isPointIsCircle, pieChartColors, randomRgbColor} from "./LitChartPieData.js";
import {Utils} from "../../../trace/component/trace/base/Utils.js";

interface Rectangle {
    x: number
    y: number
    w: number
    h: number
}

class Sector {
    id?:any
    obj?: any
    key: any
    value: any
    startAngle?: number
    endAngle?: number
    startDegree?: number
    endDegree?: number
    color?: string
    percent?: number
    hover?: boolean
    ease?: {
        initVal?: number
        step?: number
        process?: boolean
    }
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
    data: Sector[] = []
    radius: number | undefined;
    private textRects: Rectangle[] = []

    set config(cfg: LitChartPieConfig | null | undefined) {
        if (!cfg) return;
        this.cfg = cfg;
        (this.shadowRoot!.querySelector("#root") as HTMLDivElement).className = cfg && cfg.data.length > 0  ? 'bg_hasdata':'bg_nodata'
        this.measure();
        this.render();
    }

    set dataSource(arr:any[]){
        if(this.cfg){
            this.cfg.data = arr;
            this.measure();
            this.render();
        }
    }

    measure() {
        if (!this.cfg) return;
        this.data = []
        this.radius = Math.min(this.clientHeight, this.clientWidth) * 0.65 / 2 - 10
        let cfg = this.cfg!;
        let startAngle = 0
        let startDegree = 0;
        let full = Math.PI / 180;//每度
        let fullDegree = 0;//每度
        let sum = this.cfg.data.reduce((previousValue, currentValue) => currentValue[cfg.angleField] + previousValue, 0)
        this.labelsEL!.textContent = ""
        let labelArray: string[] = []
        this.cfg.data.forEach((it,index) => {
            let item: Sector = {
                id:`id-${Utils.uuid()}`,
                color: (this.cfg!.label.color)?(this.cfg!.label.color(it)):pieChartColors[index % pieChartColors.length],
                obj: it,
                key: it[cfg.colorField],
                value: it[cfg.angleField],
                startAngle: startAngle,
                endAngle: startAngle + full * (it[cfg.angleField] / sum * 360),
                startDegree: startDegree,
                endDegree: startDegree + fullDegree + (it[cfg.angleField] / sum * 360),
                ease: {
                    initVal: 0,
                    step: (startAngle + full * (it[cfg.angleField] / sum * 360)) / startDegree,
                    process: true,
                }
            }
            this.data.push(item)
            startAngle += full * (it[cfg.angleField] / sum * 360)
            startDegree += fullDegree + (it[cfg.angleField] / sum * 360)
            labelArray.push(`<label class="label">
                    <div style="display: flex;flex-direction: row;margin-left: 5px;align-items: center;overflow: hidden;text-overflow: ellipsis" 
                        id="${item.id}">
                        <div class="tag" style="background-color: ${item.color}"></div>
                        <span class="name">${item.obj[cfg.colorField]}</span>
                    </div>
                </label>`
            )
        })
        this.labelsEL!.innerHTML = labelArray.join("")
    }

    get config(): LitChartPieConfig | null | undefined {
        return this.cfg
    }

    connectedCallback() {
        super.connectedCallback();
        this.eleShape = this.shadowRoot!.querySelector<Element>('#shape');
        this.tipEL = this.shadowRoot!.querySelector<HTMLDivElement>("#tip");
        this.labelsEL = this.shadowRoot!.querySelector<HTMLDivElement>("#labels");
        this.canvas = this.shadowRoot!.querySelector<HTMLCanvasElement>("#canvas")
        this.ctx = this.canvas!.getContext('2d', {alpha: true});
        resizeCanvas(this.canvas!);
        this.radius = Math.min(this.clientHeight, this.clientWidth) * 0.65 / 2 - 10;
        this.centerX = this.clientWidth / 2
        this.centerY = this.clientHeight / 2 - 40;
        this.ctx?.translate(this.centerX, this.centerY)
        this.canvas!.onmouseout = (e) => {
            this.hideTip();
            this.data.forEach(it => {
                it.hover = false
                this.updateHoverItemStatus(it)
            });
            this.render();
        }
        //增加点击事件
        this.canvas!.onclick = (ev)=>{
            let rect = this.getBoundingClientRect()
            let x = ev.pageX - rect.left - this.centerX!;
            let y = ev.pageY - rect.top - this.centerY!;
            if (isPointIsCircle(0, 0, x, y, this.radius!)) {
                let degree = this.computeDegree(x,y);
                this.data.forEach(it => {
                    if (degree >= it.startDegree! && degree <= it.endDegree!) {
                        this.config?.angleClick?.(it.obj)
                    }
                })
            }
        }
        this.canvas!.onmousemove = (ev) => {
            let rect = this.getBoundingClientRect()
            let x = ev.pageX - rect.left - this.centerX!;
            let y = ev.pageY - rect.top - this.centerY!;
            if (isPointIsCircle(0, 0, x, y, this.radius!)) {
                let degree = this.computeDegree(x,y)
                this.data.forEach(it => {
                    it.hover = degree >= it.startDegree! && degree <= it.endDegree!
                    this.updateHoverItemStatus(it)
                    if (it.hover) {
                        this.showTip(ev.pageX - rect.left + 10, ev.pageY - this.offsetTop - 10, this.cfg!.tip?this.cfg!.tip(it):`${it.key}: ${it.value}`);
                    }
                })
            } else {
                this.hideTip();
                this.data.forEach(it => {
                    it.hover = false
                    this.updateHoverItemStatus(it)
                });
            }
            this.render();
        }
        this.render()
    }

    updateHoverItemStatus(item:any){
        let label = this.shadowRoot!.querySelector(`#${item.id}`);
        if(label){
            (label as HTMLLabelElement).style.boxShadow = item.hover ? '0 0 5px #22ffffff' : '';
        }
    }

    computeDegree(x:number,y:number){
        let degree = 360 * Math.atan(y / x) / (2 * Math.PI);
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
            entries.forEach(it => {
                resizeCanvas(this.canvas!);
                this.centerX = this.clientWidth / 2
                this.centerY = this.clientHeight / 2 - 40;
                this.ctx?.translate(this.centerX, this.centerY)
                this.measure();
                this.render();
            })
        }).observe(this)
    }

    render(ease: boolean = true) {
        if (!this.canvas || !this.cfg) return;
        if(this.radius!<=0) return;
        this.ctx?.clearRect(0 - this.centerX!, 0 - this.centerY!, this.clientWidth, this.clientHeight);
        this.data.forEach((it) => {
            this.ctx!.beginPath();
            this.ctx!.fillStyle = it.color as string;
            this.ctx!.strokeStyle = this.data.length > 1 ? "#fff" : it.color as string;
            this.ctx?.moveTo(0, 0)
            if (it.hover) {
                this.ctx!.lineWidth = 1;
                this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
            } else {
                this.ctx!.lineWidth = 1;
                if (ease) {
                    if (it.ease!.initVal! < it.endAngle! - it.startAngle!) {
                        it.ease!.process = true
                        this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.startAngle! + it.ease!.initVal!, false);
                        it.ease!.initVal! += it.ease!.step!
                    } else {
                        it.ease!.process = false
                        this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
                    }
                } else {
                    this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
                }
            }
            this.ctx?.lineTo(0, 0)
            this.ctx?.fill()
            this.ctx!.stroke();
            this.ctx?.closePath();
        })

        this.data.filter(it => it.hover).forEach(it => {
            this.ctx!.beginPath();
            this.ctx!.fillStyle = it.color as string;
            // this.ctx!.strokeStyle = "#fff";
            this.ctx!.lineWidth = 1;
            this.ctx?.moveTo(0, 0)
            this.ctx!.arc(0, 0, this.radius!, it.startAngle!, it.endAngle!, false);
            this.ctx?.lineTo(0, 0)
            // this.ctx!.strokeStyle = window.getComputedStyle(this.eleShape!).backgroundColor;;
            this.ctx!.strokeStyle = this.data.length > 1 ? "#000" : it.color as string;
            this.ctx!.stroke();
            this.ctx?.closePath();
        })

        this.textRects = []
        if(this.cfg.showChartLine){
            this.data.forEach(it => {
                let text = `${it.value}`;
                let metrics = this.ctx!.measureText(text);
                let textWidth = metrics.width;
                let textHeight = metrics.fontBoundingBoxAscent + metrics.fontBoundingBoxDescent;
                this.ctx!.beginPath();
                this.ctx!.strokeStyle = it.color!;
                this.ctx!.fillStyle = "#595959"
                let deg = it.startDegree! + (it.endDegree! - it.startDegree!) / 2;
                let dep = 25;
                let x1 = 0 + (this.radius!) * Math.cos(deg * Math.PI / 180);
                let y1 = 0 + (this.radius!) * Math.sin(deg * Math.PI / 180);
                let x2 = 0 + (this.radius! + 13) * Math.cos(deg * Math.PI / 180);
                let y2 = 0 + (this.radius! + 13) * Math.sin(deg * Math.PI / 180);
                let x3 = 0 + (this.radius! + dep) * Math.cos(deg * Math.PI / 180);
                let y3 = 0 + (this.radius! + dep) * Math.sin(deg * Math.PI / 180);
                this.ctx!.moveTo(x1, y1);
                this.ctx!.lineTo(x2, y2);
                this.ctx!.stroke();
                let rect = this.correctRect({
                    x: x3 - textWidth / 2,
                    y: y3 + textHeight / 2,
                    w: textWidth,
                    h: textHeight,
                })
                this.ctx?.fillText(text, rect.x, rect.y);
                this.ctx?.closePath();
            })
        }
        if (this.data.filter(it => it.ease!.process).length > 0) {
            requestAnimationFrame(() => this.render(ease));
        }
    }

    correctRect(rect: Rectangle): Rectangle {
        if (this.textRects.length == 0) {
            this.textRects.push(rect);
            return rect
        } else {
            let rectangles = this.textRects.filter(it => this.intersect(it, rect).cross);
            if (rectangles.length == 0) {
                this.textRects.push(rect);
                return rect;
            } else {
                let it = rectangles[0];
                let inter = this.intersect(it, rect);
                if (inter.direction == "Right") {
                    rect.x += inter.crossW;
                } else if (inter.direction == "Bottom") {
                    rect.y += inter.crossH;
                } else if (inter.direction == "Left") {
                    rect.x -= inter.crossW;
                } else if (inter.direction == "Top") {
                    rect.y -= inter.crossH;
                } else if (inter.direction == "Right-Top") {
                    rect.y -= inter.crossH;
                } else if (inter.direction == "Right-Bottom") {
                    rect.y += inter.crossH;
                } else if (inter.direction == "Left-Top") {
                    rect.y -= inter.crossH;
                } else if (inter.direction == "Left-Bottom") {
                    rect.y += inter.crossH;
                }
                this.textRects.push(rect);
                return rect;
            }
        }
    }

    intersect(r1: Rectangle, rect: Rectangle): {
        cross: boolean,
        direction: string,
        crossW: number,
        crossH: number,
    } {
        let cross: boolean
        let direction: string
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
        crossW = Math.abs(maxX - minX - (rect.w + r1.w))
        crossH = Math.abs(maxY - minY - (rect.y + r1.y))
        if (rect.x > r1.x) { //right
            if (rect.y > r1.y) { //bottom
                direction = "Right-Bottom"
            } else if (rect.y == r1.y) {//middle
                direction = "Right"
            } else { //top
                direction = "Right-Top"
            }
        } else if (rect.x < r1.x) { //left
            if (rect.y > r1.y) { //bottom
                direction = "Left-Bottom"
            } else if (rect.y == r1.y) {//middle
                direction = "Left"
            } else { //top
                direction = "Left-Top"
            }
        } else {
            if (rect.y > r1.y) { //bottom
                direction = "Bottom"
            } else if (rect.y == r1.y) {//middle
                direction = "Right" //superposition default right
            } else { //top
                direction = "Top"
            }
        }
        return {
            cross, direction, crossW, crossH
        }
    }

    showTip(x: number, y: number, msg: string) {
        this.tipEL!.style.display = "flex";
        this.tipEL!.style.top = `${y}px`;
        this.tipEL!.style.left = `${x}px`;
        this.tipEL!.innerHTML = msg;
    }

    hideTip() {
        this.tipEL!.style.display = "none";
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
            background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJQAAADPCAYAAAFkcsh2AAAAAXNSR0IArs4c6QAAAERlWElmTU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAAA6ABAAMAAAABAAEAAKACAAQAAAABAAAAlKADAAQAAAABAAAAzwAAAAD2dHVdAAAtN0lEQVR4Ae2dCbgeRZnvk5OTjQDZyAJkXwmEfd+SIyCKwx1GEDdGRxnkOl5QGRHuPPK4IG6go1x90EEcGWfGZQZ15jrKeBU8OWEJhCWBsATIHkJIICEhC9nI/f37dDX11df7199ykq7neb+qeuutt97699vV1dXV/fXqlRCeffbZv4a2Qv+eINqrd5jA4sWL1+/du/eQsDLDO+KII6rqVjGwYK+pkBS3tbV9dNq0aXcauQplWRQFCnr3vmf69OnnKd9mmHkUqS5wnGt0eMqEkWHkiY0hnrI4sHv37t2V1AAyz0jGw8xoTqqUVN5GF69IEqLlBUaG9LMm7cbq5i0uMyT/huEByREm7cZtFP7WZdL6fJuHzGl2PipdLGZRrfj8lQnlQXF7e/tUzzUCTnViXDUrnDNlypQXPGVhJ214lSrufT7nMcWBZX369Lm0SjSZcZZEMOZExe6J/nt456sgbbB7VaHMKEhzRuA+cxktZpk6ikOVGYEwpRy1UYC9zsiUcfEItOdV2Sek4gx4ZhgfRHoXdBukI78H2ghdBGnsvxyqGnXghYajQ7kRzFg/Ux1G4nmMZ+O4Rr6ba+RDEXrCHTbMUW0FKL0Epb+yeRUWOQo6Eeywhd106PnoKHHrROaNMm/IQcmZkZLdRyuy2Bhgxi4zoHkVOPtXWTWHWunIZG80XkfpN6i8naMz0EiS30R+sMknxbLoGxKylfj5WCU0VHHBNV1T3UyBhisutqkUua2HtZhG0QNu66GKaC1pWD3DVERWJ29o0NxhZGhJCBNZjRpbQ4q6zzXjVGECaXhY+kYajBJ1ceka6Cky54tfY6dbkxbNgOcW6YrsnfiBRYaBZD9XGmyecnnKW3Xemie4BU7F8U6+QonKKsYjW/i55577+ptvvnm9zVPatsItK/PZEIgE31JT5R4qC7viXghfJ+fpkK6sD0B2+D4ZTQs7bWZh6TRdqWiMK+/f4elfrWAmZIYPH37wiBEjXk8QC4oTjXrhhRfetnv37nuDGgUkkk6QSKNqHfLS2M4g9Tgj2gmubJVRaY1B4QYO4zApZL7TyZDR4SpPm0fXDIwLrrwVRqU1yGpsN+nck1pLj5c0hzUYwmUQFiddO4XKHEtZYQZJpwHFQ8pkrMa8JEZyhPZWoOnKFJDfgo4DLT23BEhZzCCZ1iCMX02lR4OK0YmwYcE2SDU/2w5Kf4rWkbpEM50xtjSGbqZTB9s80gc5+dBsG5WrTslQyRAmdbvEpnFdK3SjJcS8EGKQKUqM27jn9pYTbEkaW0xejcQGGrZv+PsiXIFWbOWYwlhHj6mXpUhn6+wsFTxHHzJkiOtsSTpWJAlY5ZkM0ljlGTV69OitHLKLLUVJyaoZY1IFp3yjk/eyZvCsGoNixizdvQQ3HmFKs/IAApXd46AxSDqqjBKT6cmDCKda6JS8CTTyCPVOMvk0sW2MkQ81yhQybZnJtOVJk0+KMSo1mmHGGP2xRhkhE0cdWlMeF2Pwa8wEUq0WxOkpy0oE9hsExlo9/a6f/gDxsdBnrDIlPw3d5POMrLJXQ7dDM6E/h/4eUvhSd1T9GzvJQ1wKdeepYO5af0b6cOhbkNaJ1aDCNOgGL/WWrLJH+7wPEmv28Y9Qf0jrgpOgnhEyDZ5ulxjxR+7Zs+cmLi0XUjYcWswg+WMGyW+7slnymY3KMar/nkvKOws3CiT6cZHeEaYYZFKvE8dd72zdiUi5yGBExQ1B1rtj6s/j8J5uG+GmI40CnT6gs9ut4OZp5E1kk85it1rsKmCoUc8///yROHDowqa0Y8gODNFpXVOIOpxVPVy5cuVhxiAafy2s1SIMkl7XNUxbVUZt27btRVNI40NIzzH5esRhhlUYFSaAIbNBLDDUMqwwY/HdihuJwCgMesBqsCIJYrqsuCHTrZNb2c77RyRgBUbBiT1Ngxp1SthHyTMK+J722wp9xBJlB4f1ZZUR3x8lk4fvGQV8utor6ImwGnlJcVKg3ihfZnSSbIryLQYt+/AF9WjsUD8TrEMGhU6CDmjwnOywU2Wpa+sPlg60N2hpjAbvaSKVH3JluLwsEg+DQjvmyjv51/26FU8rjYwe7U00magYmVP9smAYYDV4JsY+HFXH5TuoHOSWmzwgfVFPifcaRtoYI+fQiBkSdpPW8/+qdS5fn1CJNCKszTzQ60QwBmlsaw8xaLHVWCaDVC+XUVaD3ioMRj4hHvFmv2y6JZM5WYtROmzPqEWQOsaP3YXXzAapQmqjMMB1ah02M74JJe+QEa/NZYlVKdQoFHdZMl4SA05xeU7ee0KN3GjqVw0hjmxklrqbZVSnK4Fie9XXLQ7yjFX3mQx1gi0FpM0QYopTx9R9l4yan7pGpeAuxip7GNDcq+bAbPT+3tKSZ6wKaT3zeBSiw5u7h/pUmHAKXux4ZC5LcXrwp3tU7hlFhfPjhJ2yxDscR97L+pelF8LKDI9br/OU9oxiF9cfTEFcTE/k2O1xMnFlOPGUmPKdpiw4fDyjGWeYUTFKbceOEsvFx8GDW7bAKDZqrsqlrYZKIO9NBnCf4201gVFiRt0c2hWKTIO8zv57cZ8Ftl5vSLAZSttDBL1ZTeUxrkxR+TAgKpAyDRlBDFpaL4N06Ew7pl0ThxqlQlXAoElGsOD4Xzn9I9sOPXy2Aeydupgx5pc2r5Z0FDq2zkSjjDBz51+A3HtNPmvcr1+/CZMmTVqRpl5qo4wyDGsDvd3EiXXxm//mMF1g6pZxiUCJQIlAiUATEYibvjbFrHf4rSo+DzoO+ozPu9GPr/FjRbr50INKIyuewoe7o+73CUhLxyVQTTOQ21DQDmnVzTwFJdnrHP044SbyRlZFtrydvsEpk2wQIuc0voQUXQt9HnJ3pE31ZdzIlr2awu/5ApqPfx2STt2kPAcVHhJnCYW32EyFDe0t87CPMg+7GTqkgE7fOmzYsBtGjhy5pQBdiSrqAtTatWsHbdq06SEAOYrJ5wJijSYNCbwPetHUqVP/b9GNFQYUt69aHz46wsCF8LURJSpoNTVp8TKqbiy/f//+h06cOLHmVdiagOKe63m8ZUqspekKFyGm3Tl1DYA2FNBey9NIZqCWLl06fteuXQsBaHCeBlukzl3cpV+axZbUQDEQn8pywrwsykNkH4F3Ugi/WawnAcx7epNkQCJQeI6emwbPEVyFDNZPIXOUy4/KI/808ke65Vn1uPVrybOw+WXWETXZigyxQAGQrlhxg7CtuKYBGaB0lcz9MMc2JG86btEsEih7ETZjw3onom/GOi0jzgF7N2tp/+EaVHW/wFHtWwNI0t+Xxna6Dbl5ZGod71yVheTp/685k37uKqsAas2aNQcgpOdLiR11Fdl5GuunfBwYyJxGudoKAvnVQaaJCWx7Hzj8xjahAqjNmzdrf5LuDr2OSpCBbhNRxfYv8dMEgSE5APD2B7h1KK+YgyG31JVpVh7bLuRK/3HTfjBGZTjddlNZj94mGiX7QsxB6gKcWW5fGK/aKdvjeRRu9iFXICavhZQKkFC0IEa+ogjZqpkxvEJ3zFU0GJIBkC6XHQaSZMBGjtH9KBuhnyiTN1D/OLtuXMeRHSJZTulOUwfemdRZoTzxG4ZfRIy+KlDgVXlOXFurV68e3nvZsmVDduzYkWsMilPulHWS73B4JruGxGEmQ9wJdUCZg0AB9EwgpGkEvU+07dy586Y0wjXKdNj1naMcgARfS45n2rJRaUeHJ1YPkKQYvce0EWe6OfQsqvHH7RCd9nZHwV9Fump+1UhQoromoIIdJVFC9eYD0Mm0oanJMmgMwMy123SBtcsaldae3CUNaOxBOp90ZRyEzBDs0RX1CNlE3tuN0wD7Epvg4tP2q0SpCAE68gR0f4oOnQ4AFVdGR6UmtRoLzA24dwUk39vIqR2TbkbsGRIy2VwJgNr8cyIUu9evVqMB4DXa8KYMlq5O0h1WPkgi/yrywwNGAxK0+SONUXJxd71pHIt0HfUEiTa1xi4vckESO3LctEHSwZRwvQOz8ys8oHjsM7Tejbn66XDcyuJoVz4sz8GcJL4BPUymVh66vV3YHlD+l40q7pZrbSCifmcE32VrQE8dbNDp2PbUFVMI4k3dN/a2bMhYZRf3qDSnZU3fTlJnAUlra2/d6xkE4pZCjUzWmIaezFqnCHmNsb4er6NZdWrHtAFJdYPLr62IO2a92RVaZsslpHX/2PCxL84mOj6ffmlyGxvCPrQWCQZg3YPSc2I1RhTi9ps4ooMjiluB7d6IBzZFnVXeYB5IWQnOz3N5jj/SYiUmOWLe7LvFQVI/7Btxgaagh6KRjhNZ0F23+xfv+g+86yKbV+80oC+mzekNaGcvThHpMKb9VEAZYQALe87XSXmHkSkqBqj7AOqsovSF6QGgPrTzZliZy8sElKnMNOI60t8w+TrFc9A7uw66N3KKDcuqNxdQdiN42csc+UxjmV0/Jr2QMnOTHCOWroidLBMJy9NJV0vVDJStEtB+CWgX27wa0q9TN/cNOafUZq6+I9lUtqMGG4KqhQIVaPUTPBe7mivg/3H59cgDzB/ZaHE+cV3WsOoKVBQg/veDLsP7LkPmxCg5mw8Ar5H/KV5yG17ylF1WpksESgRKBEoESgRKBEoESgRKBPZbBAbR8y9Auhn+BHQ61Oxwe4QBsq/mkLhgFdGCNlTo2dsXoeXQg5DWx/8MUtDeR90eyfgBkMJ3u6Ne1/jxcOIz/LQd3UbGbL/WW4hheo38tST0jqd9K/Z5v/ASI+THhxKP9dNRoDpV3spqm2GeICA+CWlPuYIaFhBrlSHshIzxZgfd3V5Jr17fJv4BpKcjV/k8NzJ6R1EwBnL1mhvfCZS5T7nN02P3CXQHsv8O9ajwLctaeUUZSgT2MwTMONKUbrMiqocHs1iXOpX1pqOIhxJvgqcBXNt71pGfxxrUA3x45p7x48dvbIqhNNowoPhW8hhWO2+h8+93OvsAefvqt5L8OEemIgt4T0PXsaL524qCOmbqCtSSJUvG8d8XdwNO1ft5pk902N3ynAiUqauY+rrqXc6jp5/Y/KLTdQFK75DgPd9PYywdfQ4gp1mymYCy6in5JwA7D52pntU5dWOzhQIFQJ8CoO84LXaS73B4cdlagPL0AtQzABbpxXGNR5UVAhTjz+l8Gl1jTRGhZqCMEQD2CwBzx0RTnCmuGSiuXEs4dSbFtYrBFf9fGydLWWFAmXbYbDKz1ic3bUZZ1lh/UqQdekkgSS8yz2TVX6Q83r4IW++qRWcuj6LRW2i0yFsP3duZG+HCPcoCaBf7DvpZ+dTJzB7FqXYP2vOA9GSUVZyai6LKCuZ770uvWrVqYFa9mYDCk9ZwGp1Dx+7L2hB1IieR6NRsvGFh69at2wAr046W1EDhSbp90JqOxpyz6HimVy6oMzgGiYNjyupSBFivcrUekVZ5KqAAaRUdrVjfIa+3Ns3aT6r2uGfrDBNET6FznrA2wngM8uvohxkbw0QCXiJQgPRrlGnxrCrAn0Qnt1cVRDCYjHaEFaHHrIKGFdeVR/+0yJgYYoFCycfoxF/EaaE868CYyrC4NosuY+zdkqQzEigA6AfdnqRA5XiVuxwbWQ3ZBZGFzSsYBFg/ims+ch6FN+0BqEggXaUAoPO9iC2KK9EdeYV02y0yH/f1slAgAOkfsoAkY32QHktp+GJXDqBfcHmNzvO2/ktRbYYCRaevjKqQwD+B8s4EGZ2qYXOYNUn1GlGOk+hpUlWoAopz9bkqqWyMjqRxiAMRNn/R87mmB2wLfYRWARQTME38ptZqLY0dB1jrE/TMcconOPmmZfGqP7qNVwDFBGw5Hax4ld6tkDYvr0FX3EpjxWZ75A9Pq7vecthyrttGABSFugIOJT5bQlGzaFdBXB5dgf4wOYBMPa0Iq19PHkNQxVJ20BGWcX9nN2zNojttftY0YGifQmgAyIdDC1qAid0VHh8AhdHvjLCvw+e7Y0qEeCUbvYPwzucruUGuFXbBBMYoAUBdirF7BmO2ruJe8IBitXKKYcTEHsJ0OvMYhndOjaqHYfaN9YaY9utWhA3Poty7jQGgWaYhxuzguaEHFM/efm0Kk2I67Y1hKJ+XJGuX+/WqlmYwLLhXRGeU59mqCkvTnvGeI1B6YIji0YbnAUVmpmGkjengab5s2tm4xMOWZrw1LhVieOqVCMnnCbQR6j1RujjbxqrMABUll4bvnccYoMfcZt9SZD0ArlqaoZ53OlNWxL1iaNu0keQ9ofU4276uAn3e9q9CJTIy6eSRUG/GIo05u+OqIxecbpIj753O0hFXL2sZ4Ojpz+uqh+5g7Mmo54OSl0ddlrFirDhj0SQE9KXBNdAbUcKUVcyhyCd6Y5Qul48u4z0zKMv9zp+tV9+PervNKCqN3sOgARi9Aap6eECZ3ud92WqvaqC3yhKT6NLYU6v3RLYjj6prAJBh0GA6ostvxf0f/FHwvIsB6bPyGIJe4z26chXiPa4dDE+H1x0o0yhA6PKr+z8tBa82fGJ7aWY15W9aZVFJrWfVzXtCGj27YUCZxgFMT2r1HTsBYuZNHaQXwtO4tRIKDZR73kOhvodQF+8JbZj2Gg6UMQTA1LZZ0nmS9LHQIGgbZIfFALRZDOrkvXLZ+vKkmweUY+3Rfn45sXeltL0HgLRO1swwoGke5fYaYJ6At4P4AOJdTfQe1zTl+zUFKMDQ6dQlQIxVAHOMPAc6D54+cFXrkrRRXUS8pRFArWS23knHvauUrAaM6ZDGm75WLzSbPxq+ZxPxNPKdVnkzk6sLBQowXlHniNdZvRrHbL2DjiddpdqtOibZAchPmUwT4+W1AKWVyzmAstx0ADAOIS1Qst7crjA63BiQ9f9XkbdCrnyd8nPTArUbY+dCusk0QZfy2YAywTByxp3UGx9XlzYGqJz2E/cIxOnJW8aG2YUCykz6Aj0Y9ABk7xFox9izId1kFhlWoqwjrULaPxC7FqaVL1JOQN1B4xWL/Bh0BnRckQ1F6BoXwY9kY5cmprnW7yOVpihoY2vxD2j8lBSyRYsEU4McimdTZ3mOenmq6K6hVxv7r73bgzwaaqijyaU9NcijagJnQsWaVh4lSXVwpM9JRqeeQuyKZLdIMb90TutOxxShjTOhj/Sgs2L5pgjdRgeO9BulPaBo6G9MQT1j2tEnuc8sug10avlmXtF6bX0eUFz+7rCZ9UrToSEpdbsrCInV0H0aQnpbvrAA+N5pJ4Xm1FM6s3GqlDbQaNVycExdzfDzBD15tu8K8ugI6uBAXzWZACg+InyqYdYhfoAjHrfPvKJJnaIVjGwZ764AHW9mq1YlXbH7LgBqypQpi6pEC2BgsCaIZ2RUVbN3c2DUt5UZ2w3EBwwYcGKQIREAJSad+ojiAoPWlTRBzBSwY0emCtHC+tR2V3RxZMnOCRMmhHuUqnBO/lNk1XwFueZK3AgX9oyPAzULsB7PYj67g6e78hUepcK+ffue7ArlyWPcqjz1VIe63vwob323HmAdj85UE2vkXp4Y8gXYKqAmT578CA3VNEZooQ7jxroGZ8h7qwUZ5BNFscdbdweI2NOaTwQcFqasCigJcQrqGVzesFwLdXkr+/VqaT+2aQDrD1h6qlwV4N8IhV4tQ4FCeC/3OH9dpSkdY0I6sWgpOjM0urT2EvQfQR//5GjahYN8weEF2d5BKiTBhs/lsMeHFEWxdM8YtqQbJR/K11GlM6EHMbRCTibt6KVoPYrvFfd1fJXHGkNl3aHvlWCKoHlYzSCpnUaA5Lcjz9rBZNs8VxQ7NMQCpRoMbmmuQPchOjO0hRZnclCuSDPZTgRKHqW//4jp7wbKzoopb+Wir3DW/EsaA2PHKFuBXlbWe7g2ryencYCfM3h/IG0fEj3KKBo7duwGroQVN7Y0lmoSZ3S0Sozdn8kCkuxO7VF2J9lYpauSFsq0rNGjApPh2Yy7XVmNTu1RtmKOhuolPfm1q7REWmdEHpBkfC6PMr3Gs96FZwW7+w2/1WJOtfs5uDVdcGoCygCSY2JqqtY9xotO5AHBY7U2VAhQMmLp0qXj+XPo5bUaVGD967n031yUvsKAMgbxRtLx3BQ/UsDsWisYBxi9GeJvAtBnM8inEi0cKNMq49chgHU3+ZMML2O8EvnUj9y5ml3IQF238bJuQNmg4GUdvNL1LXgn2Py4NAPwEwB9TIzMTmSuYpD+YYxMYUUNAcq1Fm87FxAuo6PvJx7olitP2TzK9KzOC+TnQ/rznJ8Qh64ZGdkyLhEoESgRKBEoESgRKBEoESgRKBEoESgRKBEoESgRKBEoESgRKBEoESgKAe2K07dVWiFoSbuuy9q5Hqlj1C2QNl9pO9DtULPDoRgwJsKIayL4mdhpNomFKbwU5vcg7Y1aA5nN69oDqben5kLa0qh95ldCegtA7yAfDu2CtkBnQ6rnPig4Ct7HIe3O1S4+BVtvN6f7V9sKr4aWQDsh7a65HDoX0guJ2tYjmU5oGHQtJBtU1pDwP2hFnqQHjfIqhdu6I+/3m/zq/eNP+rzvEI/005/z4yhPvNEvv96PXb0+24uMLoGj7doXWIW3+mnjUSY/Af6RllyqZN49l79Bu0inrt5A0hG3gwHv5xbTvPVkTpGFVpmdlDcqrOyOKn6NXjGlRzYo6MFnP6gL+ntIdrlni2zRwVkG/ReUKUhhniAPUdBp80eoP2SDrlMrKtxJwfnQHRECex1+lF6dtmf6suf48Y3Efwt92s/b0SVkroR+ZzPTpvMC9c80oKMjmgxpbPiSnxfvOigqPETBe6Ad0EnQiVBciNK7h0oC9R+gF3wFalt5nW67fZ5OSY2PAvYHkNq9Cmr5IO97u2/lGOK8B6vlO1qrgZkH0lobLOuXCJQI9FgE6np/1CqosD9rMluITsGeU9gNeArbhvSfyPae+Scp0y1ZVFhJQepNbb6S5bTzOO08zia3x/mf5Ufd12+jGuvJ/H3GofRPm3wo+n0cxIs5iJkurHphNeFdzDwOleQXuxDQ16fuYuPyv/HekrlxSarX0uU90qFwnrexI1RrBn+O8zxIfFYtKHNQ56Pn5Bgd9XCo0OawRf+/+zMKb2Vv8xOhQi3MbHmHWr9+/UEbNmy4Fgw/BdD2ZcqDlQMAe+8aMofnxRkdm8J0W/oa5lBWm0ES+5ZAN7HL95+JdYPbsqHlHIoD28Yf+fwl8Y2gNj4NcoC8AvlUslH60OH+8b0t2lSHsg1RGlvnE/0d++vvccuanW8Jh1qzZs0BW7Zs+QpO8Uk5VB5QAHkudc/OU1d1mEfNYR41O6J+SzmUbSP91ugq5/oBaS3NNTU0zaG48zqYedDNgHAlgBRiB7ruR5dZGM4ELHX1GbAzIiq1rEPZ9tKH7dA1vDal9d2mhEIOZBbLeU30vcj/I6QF7EIDYOpt8JdRemhWxdRdS93REfV6hEM5tq/g7vEDzLt009Kw0BCH0suN9OgXHLBz6t0zLl1LuXRNytMOThU1F+uJDhVAQL++xqj1OeK6XxLr6lD6X0PWhjRx1KLgY5AWD3N97o56qQNO1YVTzUpd4S3B+0iGLUH0aIcy3cOhHuIjkBeMHz9+o+EVHdfFofz30/8fI5JGJjd0wuhwmUXnAS9uThTaHHWiJvb7hEOZTtNP7fY4j4n8K4ZXVFyoQ3G7fxwjQxfGHRRnIB3aSfnTONxxcXK1lNGG1mteoY3UWwGpsxz5CSHt7lMOZfXvSb4hejafx9xk8WpKFuJQuu3fvHmzHClpN0qFsRzAVTAGcBBHVBQUlEH/C+ieklHdeuRde/ZVh/KgAaefMFr9VUacQsXbQrkZmNy13YIzbaVKJmdSExzssXImOjQPejNDs6lE5UxaX0ol/JbQC28l948UOH2Y47iXqcpltfY49wi1ZMmSo5lw6xmYtoIVFXTwoxYXc7chh8XO4IMlCYrCbNinRygHj0WDBg06ha8Ybnf4qbK5RiiWAb62a9cufX6nSGeSwbM5+BrtnkllfUoh7DwJvbqUJQbkUs+5EpX1TIGZfDd0G8f4Q3nMzzRCrVu37sCNGzcu4ABp529dAwdWl54RtFX1QDhPw+hbjK7pKevKqe2F1/1phLIh+iU7Ht5jM5LSqR2KNaUzuMTdn6Sw6HLmQHO5c8z9jM6xJ+xy5oh0P3zF+U62CvZXhxIEa1hxn8GKu14dSAypLnksB1zSDGeS9ZYzFeHMuqTqPYnYgDNphCpDNwKH8cz1FS1SpwEk0aFwpqs5qHelUVZnmTNxhtegpTW2czz1NyToGJZQvr8V98WpnsOpEu/kYx2KidlncCa969YSgZFjCDSJy+AiHCvXXQj1+1FXD5AjA+X6e18tjJbBRwDcenOVeoSlhY44UCIdipHp4yjR263eO4QAvAbqgt6IU9iIMpx8JrYNpC3NiTIH6s7AKTujKlLeh7JFUeX7Mx/s72XJaFoUBqEOhRe+g4rftysB8mHQLGiA+DiWLhudxIUt29vtpUxrTrQHejilfCBG/zqoNz9gOAn6WbcHqE5TPSoLLr1ZMnp89erVw8MMr3IofakdsO8OE7Z5KNY8o4PYu63n4Ggi2wmlWu9BrpBA+30gvRr1MvRiRqXHIB91QhyYUdc+LQ62r0NzoWfo6AGsVXWFdbjKofjs/684QKmXE4xS6mjdpgMaIR4N67vianS18vUOtD8K0osKj0G70rSHfH8ufXqeWBWwfUYVcz9h0Hfbebxeg9VB0NmQhwvxkcyxq+bXFY7DvOkGRqcv1wM3jNSzugcxZCTx1Hq04ejsJN/h8KKynRR0uIXY/IwP4ErKxrnl+0KePm6hH49D+oeHzCcRa1RvY42q02AROBS3hGOZxQu4hgU6o2eBmpPFvbWb2x7070S/huhjUyh5FBn3tliTfj1b3CccyjgPmGifWmbnoU5YWMRqenD82owEzvQlk25UTMe0Gh0YQ4f1YuMjRbWP/n7okjOtRPcrcXopPxKqWA0mX/SzyjgTCi3D9i3QXJTqhPICeBwI6alDUc4kvTO5sl3pNcCPN0K14F8mefYBiN6Ve5X4VOLA+Y3xWWP0zKPOKTG65NCaqHsB+fXIak7Y0iMUdnqXLWwtcuTxUUiOaH8J+6m8lXTvIDE6hX17KllTnSUASOsdp1sOoJFGZ512fGYO6DlNutDRFVFZztRpypCVMy0z+VaI5TzCAHra2IOd9Rh5jPrEmPYnM0F/lwQ9h2IirlebekIYh/EasnUp053kOn+BcmsW49Exi7pboeByYNXX+pQmqV4g3ZC7VNOeHdN2pPPQh0wfBLH11iONPZdLb7u/i+CwejRSb510YqTItINzbeLkkDNo3jTU8MNi6mmZQyvmz5PWksPBlpxGRjmpZIIbF6u88CTOo/Yeg4Zji+csxFoLK2qnReE2OwovwN7ebTz0O88p6LFZnGkwxndAnjNxkLaTnkP8EnFooM5UORMyupR6gfwg8s/72fF+XFiEbo2O7mVrEO1qnaelRp4MnT6AwWlWGx2YmaFSjxKlbwMxeDaxeZN4NwfyPnhV8yJkzqZsL+RtkyGvN3I6icfC25C349Q1zvOU0YHOnu48pisVMSfnzHY6fBQdrCjYhzPt9DV4kZO+q+MPQYPhz4B0eTsT2kjZRvKaT2mr8zp4iQFZc9kaRp2jVIFYl82ectlK7GOCwBHtCPTI+VNCx1IVc7DlQBUvL+AU+oyhPjqlEWoRpO8dVF0yS+cBFSeA2aFyqIZMOp22WzYLKMdbxmk68HR7e/sG5pqaY+2vI48FSWyy737tUIwyO4BnCfQKaZ1Yo3GoSVAfAxvs9azTXUj+INILIb2NrAXEMjgIgE0fzSleg2/fMjtiPS9Lx/Qgejm0hv5pv5S22kwhrUl6EMjr0Yq5RQ/4fmI99fYw0Zzt57chr+UIBa0PPUX+1O5s+esjsEYj1LNQj3mSzoHUfGYlB1NLAgdBk6CKNSfKtGArvkgTY0VZwhyEZzv1XoFncNLKtHEmfcn3BPLexsMsjeyDsqvlUM9A57dA5zZhwxIWGjf7B2cM8RjXLnhaAjDLAG5xrfnnUKA2zagU6MNpXqNt41ABn8QZ8JVfi8wG0j11HcnuU640x26BHOoB6FO5NKSoBMiap+ilzVchPUfTqnTFPMVXo0XJE7jE+NnGRdgkj5iPXafEtLotpkxFmn/pjlChE52zye9XNzzcvHS1s0HqPzmIe+h8MBHtxiT+F8B05JdBL1E3aZ7irclII7KKWibQj/kYcyJ2xTmTnhvuyGB7hy/7PPUGkq4aaVsGgOIMeXTixImvtbPbbgdPiv8LvRdZul8CiDTzlMnUEbWco1h9CU3SP83B1MeTQwUcJiddnjNBj3WkaRft6YMd++wCJ/27Ux3V5FVn3w8VW+FQOq9JZwekXYwVk17yPT3MoX8aOaan7QgYZRrBHb19LWd6DF26s95nAv3ZO3To0DvVIc+h+KDnb2Fq6N+nA33UmzEv08nZOTpa1F2c7giHYIduQh7JYUfLVaEvt44cOXKLDPMcyrfwCy1naYEGcQeiUUk3BKNyqj0wZ73Qatihm5CTVMgB0c6DXJsGQ5U3kIndmj8HvhM4FFs476bg3gba0pCm6NNiGtpmLVDmahcHqNtlH93atqJX5F/07c1lY5Mq3WB/maXitnbFihVDt2/fvhbDvB2RTTKwkGY5MLoLfZQDlWrSndSo9KErOAGT5Gsppy1to9FfhXTUoqfedbFxAQPR8XY7FQDp+9VcGj5iC/TENB19GLu1Y6AQZxIGcib0rm8EHrTV23ImLTzr6UBLBbDYi6+8xzWqwqFUyAT9Z0Q/dgV7SH4bHdWbMnqzpWL0Lcj+3Bvtamh/BnUPpV9aIC7iG1k1mPJWVey5lEudHqxXhCqHUikv7l1O9PsKydbPzMHEA3Ak7QevV3i9XoqT9NKv/sho858m8Q9DTbMFE25i4PmlbHFD7FnMp4b1nlrwIqZbuUXyeoamD2aMqLc9tHMf7ZxV73Yy6N+ATVqc1WbAhgTa+x7zpqujGgsdoYwwFbWoaTbrG3bLxHSuC2P0DK3uzqRO087ulul8tyHa8Oc5k5ZFYNXVPvD+YZwzyaRYh0LBLi5/0/wD192F1vhdjE3bAXNWg82JHdEbbEtFc/6yiN4RWAFVzW0qhHNkcNibcaYrk6rGOpSpjCJ92OtfTL5ZMTZoKUCry9NxporNco2wifb7NqKdWtoAl/HQZGGFE3TWosuqewVzpuutfGQy0xnHQ+SrMPa7kdrqWABAD6G+XndvaS1fiOCxaYVbSO5J8MszNdiFU56CMy1I25dMDiWlfF9xHHusn8SxGrVtWO+1aYvMlLSdqqPcMnRPrKP+eqvehoMs5PJ4elJDYL4QRzqZeFeSrF2e2aFMZW154SD/mcnXI9aQbS3w1aOJrDp1q35Q1kotKv8gdmm0PSDEvmuYO38nhJ/Iyu1Q0oxTHYtTacdnmFGJjUcJaESiTC9QNOTuLcqOCL52bhba34h2GsXWyxhrwVrLQ88NHz78pBEjRuRe46rJoUyPWa/6MukbTL6WmM510blG371lMXklwuOyVOgBsnpF/yJuvn5Xq62FOJSMWLt27aBNmzbdjTPk2pVIh/TMaiL1i9p3VCs2ofWx8wlsPCa0sAcymVZ8mrnSrUWZXphDGYOYtA/mO9Z/JO/t9TH8qJgD9Cb0OHMlLaK2fMDWLP+917L9oR+3MyL9z6INLNyhjIHMrw4hfU/c2UynWmEpwJicKuaM1raS2amEW1PoVhzpb8Fea3qFh7o5lLEUh2rno563EX/M8Ii3cGDWcmBaYSnAMitVshOpjlSSLSKE8+iNnQ9z5/Zv9Tap7g5ld4C//HgPTnQlnXu7ze9JaQ7OXOzPNU9sdD+xdRkn7oVsM3m6UW031KFMp5YtWzaAf2y4mQMT+dTayLZazEHSC6GFbdwrun/Ytwn6BBPtnxatO42+pjiUbRiftB7FJP7HHKQLbH6rpjlYz2DrjBazT7f9X8aJvkK8p5m2Nd2h7M6vWrVq4LZt2/QQ8joO2kC7rIXS2nNvXjlvmlk4ji5n13E5u6tpRoQ03FIO5drHneJZ8D6Hc73TLWtWngPZsJcV7D5q5AGHO/h+wOenTJmyzi5rpXRLO5QLFGtcU3kwfT3Afoiyfm55o/Ic3PXYUO/HQvouwq2DBw++c/To0Vsb1bda2+lRDhXWWe4cj+Tgfhj6S+jwMJk68PTZn2lF6NWIh577sf1X/fr1+/WkSZNWFKG3WTp6vENFAecvrJ7HgTqfg6Z4bJRsVj76HkFfqicBvm598e4x0vOZ98yD/jB58uRNWdvtCfL7rEOlAR+n6MMSxhjuMkdywEdBI1kn89LEuqTpbd69vi7FXhqeRhFhtwY5falmjWjgwIEvjR07drsvX0YlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCJQIlAiUCdUCg0VuAJ9IHvbEyxuqLPm7139AfLN7+mjyMjn8R0ve2VkFpwiiE9PFdfaSt6aG9gRboL1ivgL4BLbHalVN/DZoJfdvil8l0CFyCWH+oJbCr5V8q03X3LSn939wZkL7+pv9MeQ3aAynoe1LzvFT3z8VEn4Lk8IOhd0B/A+kFxxchvQr+VUhOeCmkTxTqg2X6LqT+HUmj4GRoNfRNSC8X2B/wv4G85B6CksJtCMjuWZDsORO6CtLrT0Znkr2IVgW9hqWR6ChI3+38IHQepE9Xd0GboSmQTrapkI7VcdD/8vMPE8uuYyFhq3r6up5O0K9ARtdY0pdB74PmQjuhfSoIPH2F9qPQzdDt0PchgaNwGiSeLo920AEVfzgkh1JaANtBDnWFzSCtt48lO9TnC2g5iZwwTZDsXziCctgbfV4ae53qvQbCkE0dTsF0ny8nsIOcRPar/Hroe5AJnyBxjck4sfo4HtIopvb0Hc26hva6aq9Uri+tvAr9FHrQJyIv6CB/BHoRksMpvNIdBb86QxUErglrTMKKXd59lMnx5MA/hDSi/QjaBqUNWxzB3eTbfF4We40ajSgKLv6G313aq9d7SXRA/wQt9mkBsetwsIIwidT/hu6FuiBhKvvfAdU9GFDq3hAN/BaaDV0HqdP6luYwSCPIhZAAWwHdA+mDFLrkaTSSjUdDcoTfQK6jwUoMdyAxGfoqNB96GFI4AvospDM/b8hj73oa+z2kS6UuvTpZZN/7ITscSkZYPAvtgt4OabSUvHFGnRijoJGQsDoM0iVeL5aug6ZA10IK/bujfetXQKjT+vCpOuuelbC8YMCZRm6Qz6sl0rCvuYU9wknfu6G4M14yaUJee0egfBLUN6aRcZSZkVBiOhl1CTRBmB5sMn6sk1GOZgfjdDav0LQLbqHKW0TZx7BDI5GGfTnUTsgEHYSt0B7DKOMSgRKBEoESgRKBEoESgRQI/H+jqTosKu/LwQAAAABJRU5ErkJggg==);    
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

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

export function ns2s(ns: number): string {
    let second1 = 1_000_000_000; // 1 second
    let millisecond1 = 1_000_000; // 1 millisecond
    let microsecond1 = 1_000; // 1 microsecond
    let nanosecond1 = 1000.0;
    let res;
    if (ns >= second1) {
        res = (ns / 1000 / 1000 / 1000).toFixed(1) + " s";
    } else if (ns >= millisecond1) {
        res = (ns / 1000 / 1000).toFixed(1) + " ms";
    } else if (ns >= microsecond1) {
        res = (ns / 1000).toFixed(1) + " μs";
    } else if (ns > 0) {
        res = ns.toFixed(1) + " ns";
    } else {
        res = ns.toFixed(1) + " s";
    }
    return res;
}

export function ns2x(ns: number, startNS: number, endNS: number, duration: number, rect: any) {
    if (endNS == 0) {
        endNS = duration;
    }
    let xSize: number = (ns - startNS) * rect.width / (endNS - startNS);
    if (xSize < 0) {
        xSize = 0;
    } else if (xSize > rect.width) {
        xSize = rect.width;
    }
    return xSize;
}

export class Rect {
    x: number = 0
    y: number = 0
    width: number = 0
    height: number = 0

    constructor(x: number, y: number, width: number, height: number) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    contains(x: number, y: number): boolean {
        return this.x <= x && x <= this.x + this.width && this.y <= y && y <= this.y + this.height;
    }
    static contains(rect:Rect,x: number, y: number): boolean {
        return rect.x <= x && x <= rect.x + rect.width && rect.y <= y && y <= rect.y + rect.height;
    }

    containsWithPadding(x: number, y: number, paddingLeftRight: number, paddingTopBottom: number): boolean {
        return this.x + paddingLeftRight <= x
            && x <= this.x + this.width - paddingLeftRight
            && this.y + paddingTopBottom <= y
            && y <= this.y + this.height - paddingTopBottom;
    }
    static containsWithPadding(rect:Rect,x: number, y: number, paddingLeftRight: number, paddingTopBottom: number): boolean {
        return rect.x + paddingLeftRight <= x
            && x <= rect.x + rect.width - paddingLeftRight
            && rect.y + paddingTopBottom <= y
            && y <= rect.y + rect.height - paddingTopBottom;
    }

    containsWithMargin(x: number, y: number, t: number, r: number, b: number, l: number): boolean {
        return this.x - l <= x
            && x <= this.x + this.width + r
            && this.y - t <= y
            && y <= this.y + this.height + b;
    }
    static containsWithMargin(rect:Rect,x: number, y: number, t: number, r: number, b: number, l: number): boolean {
        return rect.x - l <= x
            && x <= rect.x + rect.width + r
            && rect.y - t <= y
            && y <= rect.y + rect.height + b;
    }

    /**
     * 判断是否相交
     * @param rect
     */
    intersect(rect: Rect): boolean {
        let maxX = this.x + this.width >= rect.x + rect.width ? this.x + this.width : rect.x + rect.width;
        let maxY = this.y + this.height >= rect.y + rect.height ? this.y + this.height : rect.y + rect.height;
        let minX = this.x <= rect.x ? this.x : rect.x;
        let minY = this.y <= rect.y ? this.y : rect.y;
        if (maxX - minX <= rect.width + this.width && maxY - minY <= this.height + rect.height){
            return true;
        }else{
            return false;
        }
    }
    static intersect(r1:Rect,rect: Rect): boolean {
        let maxX = r1.x + r1.width >= rect.x + rect.width ? r1.x + r1.width : rect.x + rect.width;
        let maxY = r1.y + r1.height >= rect.y + rect.height ? r1.y + r1.height : rect.y + rect.height;
        let minX = r1.x <= rect.x ? r1.x : rect.x;
        let minY = r1.y <= rect.y ? r1.y : rect.y;
        if (maxX - minX <= rect.width + r1.width && maxY - minY <= r1.height + rect.height){
            return true;
        }else{
            return false;
        }
    }
}

export class Point {
    x: number = 0
    y: number = 0

    constructor(x: number, y: number) {
        this.x = x;
        this.y = y;
    }
}

export class BaseStruct {
    frame: Rect | undefined
    isHover: boolean = false;
}


export class ColorUtils {
    // public static   GREY_COLOR:string = Color.getHSBColor(0, 0, 62); // grey
    public static   GREY_COLOR:string = "#f0f0f0"
    /**
     * Color array of all current columns
     */
    public static MD_PALETTE: Array<string> = [
        "#3391ff",// red
        "#0076ff",// pink
        "#66adff",// purple
        "#2db3aa",// deep purple
        "#008078",// indigo
        "#73e6de",// blue
        "#535da6",// light blue
        "#38428c", // cyan
        "#7a84cc",// teal
        "#ff9201",// green
        "#ff7500",// light green
        "#ffab40",// lime
        "#2db4e2",// amber 0xffc105
        "#0094c6", // orange
        "#7cdeff",// deep orange
        "#ffd44a", // brown
        "#fbbf00",// blue gray
        "#ffe593",// yellow 0xffec3d
    ];
    public static FUNC_COLOR:Array<string> = [
        "#3391ff", // purple
        "#2db4e2",
        "#2db3aa", // deep purple
        "#ffd44a",
        "#535da6", // indigo
        "#008078", // blue
        "#ff9201",
        "#38428c"];

    /**
     * Get the color value according to the length of the string
     *
     * @param str str
     * @param max max
     * @return int
     */
    public static hash(str: string, max: number): number {
        let colorA: number = 0x811c9dc5;
        let colorB: number = 0xfffffff;
        let colorC: number = 16777619;
        let colorD: number = 0xffffffff;
        let hash: number = colorA & colorB;

        for (let index: number = 0; index < str.length; index++) {
            hash ^= str.charCodeAt(index);
            hash = (hash * colorC) & colorD;
        }
        return Math.abs(hash) % max;
    }

    /**
     * Get color according to tid
     *
     * @param tid tid
     * @return Color
     */
    public static colorForTid(tid:number):string {
        let colorIdx:number = ColorUtils.hash(`${tid}`, ColorUtils.MD_PALETTE.length);
        return ColorUtils.MD_PALETTE[colorIdx];
    }

    public static formatNumberComma(str:number):string{
        let l = str.toString().split("").reverse();
        let t:string = "";
        for(let i = 0; i < l.length; i ++ )
        {
            t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
        }
        return t.split("").reverse().join("")
    }
}

export function drawLines(ctx: any,xs:Array<any>,height:number,lineColor:string) {
    if (ctx) {
        ctx.lineWidth = 1;
        ctx.strokeStyle = lineColor||"#dadada";
        xs?.forEach(it => {
            ctx.moveTo(Math.floor(it), 0)
            ctx.lineTo(Math.floor(it), height)
        })
        ctx.stroke();
    }
}


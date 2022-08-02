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

import {CpuStruct} from "../../../bean/CpuStruct.js";

export class ColorUtils {
    public static GREY_COLOR: string = "#f0f0f0"

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
        "#f90000",// blue gray
    ];
    public static FUNC_COLOR: Array<string> = [
        "#3391ff",
        "#f48fb1",
        "#2db4e2",
        "#2db3aa",
        "#81c784",
        "#ffab40",
        "#535da6",
        "#cddc39",
        "#0094c6",
        "#ff7043",
        "#008078",
        "#e65100",
        "#673ab7",
        "#ea80fc",
        "#7a84cc",
        "#7cdeff",
    ];

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

    public static colorForThread(thread: CpuStruct): string {
        if (thread == null) {
            return ColorUtils.GREY_COLOR;
        }
        let tid: number | undefined | null = (thread.processId || -1) >= 0 ? thread.processId : thread.tid;
        return ColorUtils.colorForTid(tid || 0);
    }

    public static colorForTid(tid: number): string {
        let colorIdx: number = ColorUtils.hash(`${tid}`, ColorUtils.MD_PALETTE.length);
        return ColorUtils.MD_PALETTE[colorIdx];
    }

    public static formatNumberComma(str: number): string {
        let l = str.toString().split("").reverse();
        let t: string = "";
        for (let i = 0; i < l.length; i++) {
            t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
        }
        return t.split("").reverse().join("")
    }
}
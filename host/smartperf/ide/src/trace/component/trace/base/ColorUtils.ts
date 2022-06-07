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
        "#3391ff",
        "#0076ff",
        "#66adff",
        "#2db3aa",
        "#008078",
        "#73e6de",
        "#535da6",
        "#38428c",
        "#7a84cc",
        "#ff9201",
        "#ff7500",
        "#ffab40",
        "#2db4e2",
        "#0094c6",
        "#7cdeff",
        "#ffd44a",
        "#fbbf00",
        "#ffe593",
    ];
    public static FUNC_COLOR: Array<string> = [
        "#3391ff",
        "#2db4e2",
        "#2db3aa",
        "#ffd44a",
        "#535da6",
        "#008078",
        "#ff9201",
        "#38428c"];

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
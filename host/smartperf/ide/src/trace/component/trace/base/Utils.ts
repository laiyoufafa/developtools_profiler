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

export class Utils {
    private static statusMap: Map<string, string> = new Map<string, string>();
    private static instance: Utils | null = null;

    constructor() {
        Utils.statusMap.set("D", "Uninterruptible Sleep");
        Utils.statusMap.set("S", "Sleeping");
        Utils.statusMap.set("R", "Runnable");
        Utils.statusMap.set("Running", "Running");
        Utils.statusMap.set("R+", "Runnable (Preempted)");
        Utils.statusMap.set("DK", "Uninterruptible Sleep + Wake Kill");
        Utils.statusMap.set("I", "Task Dead");
        Utils.statusMap.set("T", "Traced");
        Utils.statusMap.set("t", "Traced");
        Utils.statusMap.set("X", "Exit (Dead)");
        Utils.statusMap.set("Z", "Exit (Zombie)");
        Utils.statusMap.set("K", "Wake Kill");
        Utils.statusMap.set("W", "Waking");
        Utils.statusMap.set("P", "Parked");
        Utils.statusMap.set("N", "No Load");
    }

    public static getInstance(): Utils {
        if (Utils.instance == null) {
            Utils.instance = new Utils();
        }
        return Utils.instance
    }

    public static getEndState(state: string): string | null | undefined {
        if (Utils.getInstance().getStatusMap().has(state)) {
            return Utils.getInstance().getStatusMap().get(state);
        } else {
            if ("" == state || state == null) {
                return "";
            }
            return "Unknown State";
        }
    }

    public static getStateColor(state: string): string {
        if (state == "D" || state == "DK") {
            return "#f19b38"
        } else if (state == "R" || state == "R+") {
            return "#a0b84d"
        } else if (state == "I") {
            return "#673ab7"
        } else if (state == "Running") {
            return "#467b3b"
        } else if (state == "S") {
            return "#e0e0e0"
        } else {
            return "#ff6e40"
        }
    }

    public static getTimeString(ns: number): string {
        let currentNs = ns
        let hour1 = 3600_000_000_000
        let minute1 = 60_000_000_000
        let second1 = 1_000_000_000;
        let millisecond1 = 1_000_000;
        let microsecond1 = 1_000;
        let res = "";
        if (currentNs >= hour1) {
            res += Math.floor(currentNs / hour1) + "h ";
            currentNs = currentNs - Math.floor(currentNs / hour1) * hour1
        }
        if (currentNs >= minute1) {
            res += Math.floor(currentNs / minute1) + "m ";
            currentNs = currentNs - Math.floor(ns / minute1) * minute1
        }
        if (currentNs >= second1) {
            res += Math.floor(currentNs / second1) + "s ";
            currentNs = currentNs - Math.floor(currentNs / second1) * second1
        }
        if (currentNs >= millisecond1) {
            res += Math.floor(currentNs / millisecond1) + "ms ";
            currentNs = currentNs - Math.floor(currentNs / millisecond1) * millisecond1
        }
        if (currentNs >= microsecond1) {
            res += Math.floor(currentNs / microsecond1) + "μs ";
            currentNs = currentNs - Math.floor(currentNs / microsecond1) * microsecond1
        }
        if (currentNs > 0) {
            res += currentNs + "ns ";
        }
        if (res == "") {
            res = ns + "";
        }
        return res
    }

    public static getTimeStringHMS(ns: number): string {
        let currentNs = ns
        let hour1 = 3600_000_000_000
        let minute1 = 60_000_000_000
        let second1 = 1_000_000_000; // 1 second
        let millisecond1 = 1_000_000; // 1 millisecond
        let microsecond1 = 1_000; // 1 microsecond
        let res = "";
        if (currentNs >= hour1) {
            res += Math.floor(currentNs / hour1) + ":";
            currentNs = currentNs - Math.floor(currentNs / hour1) * hour1
        }
        if (currentNs >= minute1) {
            res += Math.floor(currentNs / minute1) + ":";
            currentNs = currentNs - Math.floor(ns / minute1) * minute1
        }
        if (currentNs >= second1) {
            res += Math.floor(currentNs / second1) + ":";
            currentNs = currentNs - Math.floor(currentNs / second1) * second1
        }
        if (currentNs >= millisecond1) {
            res += Math.floor(currentNs / millisecond1) + ".";
            currentNs = currentNs - Math.floor(currentNs / millisecond1) * millisecond1
        }
        if (currentNs >= microsecond1) {
            res += Math.floor(currentNs / microsecond1) + ".";
            currentNs = currentNs - Math.floor(currentNs / microsecond1) * microsecond1
        }
        if (currentNs > 0) {
            res += currentNs + "";
        }
        if (res == "") {
            res = ns + "";
        }
        return res
    }

    public static getByteWithUnit(bytes: number): string {
        if (bytes < 0) {
            return "-" + this.getByteWithUnit(Math.abs(bytes))
        }
        let currentBytes = bytes
        let kb1 = 1 << 10;
        let mb1 = 1 << 10 << 10;
        let gb1 = 1 << 10 << 10 << 10; // 1 gb
        let res = ""
        if (currentBytes > gb1) {
            res += (currentBytes / gb1).toFixed(2) + " Gb";
        } else if (currentBytes > mb1) {
            res += (currentBytes / mb1).toFixed(2) + " Mb";
        } else if (currentBytes > kb1) {
            res += (currentBytes / kb1).toFixed(2) + " Kb";
        } else {
            res += Math.round(currentBytes) + " byte";
        }
        return res
    }

    public static groupByMap(array: Array<any>, key: string) {
        let result = new Map();
        array.forEach(item => {
            let value = item[key];
            if (!result.has(value)) {
                result.set(value, [])
            }
            result.get(value).push(item);
        })
        return result;
    }

    public static groupBy(array: Array<any>, key: string) {
        return array.reduce((pre, current, index, arr) => {
            (pre[current[key]] = pre[current[key]] || []).push(current);
            return pre;
        }, {});
    }

    public static timeMsFormat2p(ns: number) {
        let currentNs = ns
        let hour1 = 3600_000
        let minute1 = 60_000
        let second1 = 1_000; // 1 second
        let res = ""
        if (currentNs >= hour1) {
            res += Math.floor(currentNs / hour1).toFixed(2) + "h"
            return res
        }
        if (currentNs >= minute1) {
            res += Math.floor(currentNs / minute1).toFixed(2) + "min"
            return res
        }
        if (currentNs >= second1) {
            res += Math.floor(currentNs / second1).toFixed(2) + "s"
            return res
        }
        if (currentNs > 0) {
            res += currentNs.toFixed(2) + "ms";
            return res
        }
        if (res == "") {
            res = "0s";
        }
        return res
    }

    public static uuid(): string {
        // @ts-ignore
        return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c => (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16));
    }

    public static MD5(uint8Array: any) {
        function md5cycle(x: any, k: any) {
            let a = x[0], b = x[1], c = x[2], d = x[3];

            a = ff(a, b, c, d, k[0], 7, -680876936);
            d = ff(d, a, b, c, k[1], 12, -389564586);
            c = ff(c, d, a, b, k[2], 17, 606105819);
            b = ff(b, c, d, a, k[3], 22, -1044525330);
            a = ff(a, b, c, d, k[4], 7, -176418897);
            d = ff(d, a, b, c, k[5], 12, 1200080426);
            c = ff(c, d, a, b, k[6], 17, -1473231341);
            b = ff(b, c, d, a, k[7], 22, -45705983);
            a = ff(a, b, c, d, k[8], 7, 1770035416);
            d = ff(d, a, b, c, k[9], 12, -1958414417);
            c = ff(c, d, a, b, k[10], 17, -42063);
            b = ff(b, c, d, a, k[11], 22, -1990404162);
            a = ff(a, b, c, d, k[12], 7, 1804603682);
            d = ff(d, a, b, c, k[13], 12, -40341101);
            c = ff(c, d, a, b, k[14], 17, -1502002290);
            b = ff(b, c, d, a, k[15], 22, 1236535329);

            a = gg(a, b, c, d, k[1], 5, -165796510);
            d = gg(d, a, b, c, k[6], 9, -1069501632);
            c = gg(c, d, a, b, k[11], 14, 643717713);
            b = gg(b, c, d, a, k[0], 20, -373897302);
            a = gg(a, b, c, d, k[5], 5, -701558691);
            d = gg(d, a, b, c, k[10], 9, 38016083);
            c = gg(c, d, a, b, k[15], 14, -660478335);
            b = gg(b, c, d, a, k[4], 20, -405537848);
            a = gg(a, b, c, d, k[9], 5, 568446438);
            d = gg(d, a, b, c, k[14], 9, -1019803690);
            c = gg(c, d, a, b, k[3], 14, -187363961);
            b = gg(b, c, d, a, k[8], 20, 1163531501);
            a = gg(a, b, c, d, k[13], 5, -1444681467);
            d = gg(d, a, b, c, k[2], 9, -51403784);
            c = gg(c, d, a, b, k[7], 14, 1735328473);
            b = gg(b, c, d, a, k[12], 20, -1926607734);

            a = hh(a, b, c, d, k[5], 4, -378558);
            d = hh(d, a, b, c, k[8], 11, -2022574463);
            c = hh(c, d, a, b, k[11], 16, 1839030562);
            b = hh(b, c, d, a, k[14], 23, -35309556);
            a = hh(a, b, c, d, k[1], 4, -1530992060);
            d = hh(d, a, b, c, k[4], 11, 1272893353);
            c = hh(c, d, a, b, k[7], 16, -155497632);
            b = hh(b, c, d, a, k[10], 23, -1094730640);
            a = hh(a, b, c, d, k[13], 4, 681279174);
            d = hh(d, a, b, c, k[0], 11, -358537222);
            c = hh(c, d, a, b, k[3], 16, -722521979);
            b = hh(b, c, d, a, k[6], 23, 76029189);
            a = hh(a, b, c, d, k[9], 4, -640364487);
            d = hh(d, a, b, c, k[12], 11, -421815835);
            c = hh(c, d, a, b, k[15], 16, 530742520);
            b = hh(b, c, d, a, k[2], 23, -995338651);

            a = ii(a, b, c, d, k[0], 6, -198630844);
            d = ii(d, a, b, c, k[7], 10, 1126891415);
            c = ii(c, d, a, b, k[14], 15, -1416354905);
            b = ii(b, c, d, a, k[5], 21, -57434055);
            a = ii(a, b, c, d, k[12], 6, 1700485571);
            d = ii(d, a, b, c, k[3], 10, -1894986606);
            c = ii(c, d, a, b, k[10], 15, -1051523);
            b = ii(b, c, d, a, k[1], 21, -2054922799);
            a = ii(a, b, c, d, k[8], 6, 1873313359);
            d = ii(d, a, b, c, k[15], 10, -30611744);
            c = ii(c, d, a, b, k[6], 15, -1560198380);
            b = ii(b, c, d, a, k[13], 21, 1309151649);
            a = ii(a, b, c, d, k[4], 6, -145523070);
            d = ii(d, a, b, c, k[11], 10, -1120210379);
            c = ii(c, d, a, b, k[2], 15, 718787259);
            b = ii(b, c, d, a, k[9], 21, -343485551);

            x[0] = add32(a, x[0]);
            x[1] = add32(b, x[1]);
            x[2] = add32(c, x[2]);
            x[3] = add32(d, x[3]);

        }

        function cmn(q: any, a: any, b: any, x: any, s: any, t: any) {
            a = add32(add32(a, q), add32(x, t));
            return add32((a << s) | (a >>> (32 - s)), b);
        }

        function ff(a: any, b: any, c: any, d: any, x: any, s: any, t: any) {
            return cmn((b & c) | ((~b) & d), a, b, x, s, t);
        }

        function gg(a: any, b: any, c: any, d: any, x: any, s: any, t: any) {
            return cmn((b & d) | (c & (~d)), a, b, x, s, t);
        }

        function hh(a: any, b: any, c: any, d: any, x: any, s: any, t: any) {
            return cmn(b ^ c ^ d, a, b, x, s, t);
        }

        function ii(a: any, b: any, c: any, d: any, x: any, s: any, t: any) {
            return cmn(c ^ (b | (~d)), a, b, x, s, t);
        }

        function md51(s: any) {
            let n = s.length,
                state = [1732584193, -271733879, -1732584194, 271733878], i;
            for (i = 64; i <= s.length; i += 64) {
                md5cycle(state, md5blk(s.subarray(i - 64, i)));
            }
            s = s.subarray(i - 64);
            let tail = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
            for (i = 0; i < s.length; i++)
                tail[i >> 2] |= s[i] << ((i % 4) << 3);
            tail[i >> 2] |= 0x80 << ((i % 4) << 3);
            if (i > 55) {
                md5cycle(state, tail);
                for (i = 0; i < 16; i++) tail[i] = 0;
            }
            tail[14] = n * 8;
            md5cycle(state, tail);
            return state;
        }

        /* there needs to be support for Unicode here,
         * unless we pretend that we can redefine the MD-5
         * algorithm for multi-byte characters (perhaps
         * by adding every four 16-bit characters and
         * shortening the sum to 32 bits). Otherwise
         * I suggest performing MD-5 as if every character
         * was two bytes--e.g., 0040 0025 = @%--but then
         * how will an ordinary MD-5 sum be matched?
         * There is no way to standardize text to something
         * like UTF-8 before transformation; speed cost is
         * utterly prohibitive. The JavaScript standard
         * itself needs to look at this: it should start
         * providing access to strings as preformed UTF-8
         * 8-bit unsigned value arrays.
         */
        function md5blk(s: any) { /* I figured global was faster.   */
            let md5blks = [], i; /* Andy King said do it this way. */
            for (i = 0; i < 64; i += 4) {
                md5blks[i >> 2] = s[i]
                    + (s[i + 1] << 8)
                    + (s[i + 2] << 16)
                    + (s[i + 3] << 24);
            }
            return md5blks;
        }

        let hex_chr = '0123456789abcdef'.split('');

        function rhex(n: any) {
            let s = '', j = 0;
            for (; j < 4; j++)
                s += hex_chr[(n >> (j * 8 + 4)) & 0x0F]
                    + hex_chr[(n >> (j * 8)) & 0x0F];
            return s;
        }

        function hex(x: any) {
            for (let i = 0; i < x.length; i++)
                x[i] = rhex(x[i]);
            return x.join('');
        }

        function md5(s: any) {
            return hex(md51(s));
        }

        function add32(a: any, b: any) {
            return (a + b) & 0xFFFFFFFF;
        }

        return md5(uint8Array);
    };

    public static getBinaryByteWithUnit(bytes: number): string {
        if (bytes == 0) {
            return "0Bytes"
        }
        let currentBytes = bytes
        let kib1 = 1024
        let mib1 = 1024 * 1024
        let gib1 = 1024 * 1024 * 1024;
        let res = ""
        if (currentBytes > gib1) {
            res += (currentBytes / gib1).toFixed(2) + "Gib";
        } else if (currentBytes > mib1) {
            res += (currentBytes / mib1).toFixed(2) + "Mib";
        } else if (currentBytes > kib1) {
            res += (currentBytes / kib1).toFixed(2) + "kib";
        } else {
            res += currentBytes.toFixed(2) + "Bytes";
        }
        return res
    }

    public static getTimeStampHMS(ns: number): string {
        let currentNs = ns
        let hour1 = 3600_000_000_000
        let minute1 = 60_000_000_000
        let second1 = 1_000_000_000; // 1 second
        let millisecond1 = 1_000_000; // 1 millisecond
        let microsecond1 = 1_000; // 1 microsecond
        let res = "";
        if (currentNs >= hour1) {
            res += this.getCompletionTime(Math.floor(currentNs / hour1), 2) + ":";
            currentNs = currentNs - Math.floor(currentNs / hour1) * hour1
        }
        if (currentNs >= minute1) {
            res += this.getCompletionTime(Math.floor(currentNs / minute1), 2) + ":";
            currentNs = currentNs - Math.floor(ns / minute1) * minute1
        }
        if (currentNs >= second1) {
            res += this.getCompletionTime(Math.floor(currentNs / second1), 2) + ":";
            currentNs = currentNs - Math.floor(currentNs / second1) * second1
        } else {
            res += '00:'
        }
        if (currentNs >= millisecond1) {
            res += this.getCompletionTime(Math.floor(currentNs / millisecond1), 3) + ".";
            currentNs = currentNs - Math.floor(currentNs / millisecond1) * millisecond1
        } else {
            res += "000."
        }
        if (currentNs >= microsecond1) {
            res += this.getCompletionTime(Math.floor(currentNs / microsecond1), 3) + ".";
            currentNs = currentNs - Math.floor(currentNs / microsecond1) * microsecond1
        } else {
            res += "000"
        }
        if (currentNs > 0) {
            res += this.getCompletionTime(currentNs, 3);
        }
        if (res == "") {
            res = ns + "";
        }
        return res
    }

    public static getDurString(ns: number): string {
        let currentNs = ns
        let minute1 = 60_000_000_000
        let second1 = 1_000_000_000;
        let millisecond1 = 1_000_000;
        let res = "";
        if (currentNs >= minute1) {
            res += Math.floor(currentNs / minute1) + ":";
            currentNs = currentNs - Math.floor(ns / minute1) * minute1
        }
        if (currentNs >= second1) {
            res += Math.floor(currentNs / second1) + ".";
            currentNs = currentNs - Math.floor(currentNs / second1) * second1;
            res += Math.floor(currentNs / millisecond1) + "s ";
            return res;
        }
        if (currentNs >= millisecond1) {
            res += Math.floor(currentNs / millisecond1) + "ms ";
            return res;
        }
        if (res == "") {
            res = ns + "";
        }
        return res
    }

    private static getCompletionTime(time: number, maxLength: number): string {
        if (maxLength == 2) {
            if (time.toString().length == 2) {
                return '' + time;
            } else {
                return '0' + time;
            }
        } else if (maxLength == 3) {
            if (time.toString().length == 3) {
                return time.toString();
            } else if (time.toString().length == 2) {
                return '0' + time;
            } else {
                return '00' + time;
            }
        } else {
            return '0'
        }
    }

    public getStatusMap(): Map<string, string> {
        return Utils.statusMap;
    }

}

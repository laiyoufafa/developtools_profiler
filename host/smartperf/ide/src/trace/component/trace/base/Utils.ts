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
        Utils.statusMap.set("T", "Stopped");
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

    /**
     * Get the last status description
     *
     * @param state state
     * @return String
     */
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

    public static getByteWithUnit(bytes: number): string {
        let currentBytes = bytes
        let kb1 = 1000
        let mb1 = 1000_000
        let gb1 = 1_000_000_000;
        let res = ""
        if (currentBytes > gb1) {
            res += (currentBytes / gb1).toFixed(2) + "Gb";
        } else if (currentBytes > mb1) {
            res += (currentBytes / mb1).toFixed(2) + "Mb";
        } else if (currentBytes > kb1) {
            res += (currentBytes / kb1).toFixed(2) + "kb";
        } else {
            res += currentBytes + "byte";
        }
        return res
    }

    public getStatusMap(): Map<string, string> {
        return Utils.statusMap;
    }
}

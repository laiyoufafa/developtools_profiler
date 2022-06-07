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

export const initTest = (metricData: Array<any>): ProcessInfoListItem => {
    let processInfoListItems: Array<ProcessInfoItem> = [];
    for (let index = 0; index < metricData.length; index++) {
        let eventName = metricData[index].event_name;
        let stat_type = metricData[index].stat_type;
        let count = metricData[index].count;
        let source = metricData[index].source;
        let serverity = metricData[index].serverity;

        let processInfoSource: ProcessInfoItem = {
            // @ts-ignore
            processName: eventName,
            threads: {
                // @ts-ignore
                threadName: stat_type,
                cpu: [{
                    cpu: eventName,
                    minFreq: stat_type,
                    maxFreq: count,
                    avgFrequency: source,
                    duration: serverity,
                }],
            }
        }
        processInfoListItems?.push(processInfoSource)
    }
    return {
        processInfo: processInfoListItems
    }
}

export const initCpuStrategyData = (metricData: Array<any>): ProcessInfoListItem => {
    let processInfoListItems: Array<ProcessInfoItem> = [];
    if (metricData.length == 10) {

    } else {

    }
    const splitChar: string = ','
    for (let sqlIndex = 0; sqlIndex < metricData.length; sqlIndex++) {
        if (metricData[sqlIndex].avg_frequency == null) {
            continue
        }
        let cpus = metricData[sqlIndex].cpu.split(splitChar);
        let minFrequencies = metricData[sqlIndex].min_freq.split(splitChar);
        let maxFrequencies = metricData[sqlIndex].max_freq.split(splitChar);
        let avgFrequencies = metricData[sqlIndex].avg_frequency.split(splitChar);
        let durations = metricData[sqlIndex].duration.split(splitChar);

        let arrayCpu = [];
        for (let index = 0; index < cpus.length; index++) {
            let cpuIndex: CpuItem = {
                cpu: cpus[index],
                minFreq: minFrequencies[index],
                maxFreq: maxFrequencies[index],
                avgFrequency: avgFrequencies[index],
                duration: durations[index],
            }
            arrayCpu.push(cpuIndex);
        }
        let processInfoSource: ProcessInfoItem = {
            processName: metricData[sqlIndex].process_name,
            threads: {
                threadName: metricData[sqlIndex].thread_name,
                cpu: arrayCpu,
            }
        }
        processInfoListItems?.push(processInfoSource)
    }
    return {
        processInfo: processInfoListItems
    };
}

export interface ProcessInfoListItem {
    processInfo: Array<ProcessInfoItem>
}

export interface ProcessInfoItem {
    processName: string;
    threads: ThreadsItem;
}

export interface ThreadsItem {
    threadName: string;
    cpu: Array<CpuItem>;
}

export interface CpuItem {
    cpu: string;
    minFreq: string;
    maxFreq: string;
    avgFrequency: string;
    duration: string;
}

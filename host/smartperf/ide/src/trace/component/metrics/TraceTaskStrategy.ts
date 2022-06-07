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

export const initTraceTaskStrategy = (metricData: Array<any>): ProcessListItem => {
    let statListItems: Array<ProcessItem> = []
    for (let sqlIndex = 0; sqlIndex < metricData.length; sqlIndex++) {
        let pidList = metricData[sqlIndex].pid;
        let processNameList = metricData[sqlIndex].process_name;
        let threadNameList = metricData[sqlIndex].thread_name;
        let value = ''
        if (threadNameList !== null) {
            let threadNames = threadNameList.split(',')
            for (const valueKey in threadNames) {
                value = '\"' + valueKey + '\"'
            }
        }
        let statListItem: ProcessItem = {
            pid: pidList,
            processName: processNameList,
            threadName: value
        }
        statListItems?.push(statListItem)
    }
    return {
        process: statListItems
    }
}

export interface ProcessListItem {
    process: Array<ProcessItem>
}

export interface ProcessItem {
    pid: string
    processName: string
    threadName: string
}

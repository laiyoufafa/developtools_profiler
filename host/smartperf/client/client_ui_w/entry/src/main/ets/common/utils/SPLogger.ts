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
// 全局TAG
const bastTag = 'SmartPerf';

// 控制所有
const isPrintLog = true;
// 控制debug
const isPrintDebugLog = true;
// 控制info
const isPrintInfoLog = true;
// 控制warn
const isPrintWarnLog = true;
// 控制error
const isPrintErrorLog = true;
/*
  全局Log封装
 */
export default class SPLogger {

    // debug debug日志
    static DEBUG(tag: string, msg: string) {
        if (isPrintLog && isPrintDebugLog) {
            console.debug(`${bastTag} tag: ${tag} --> ${msg}`)
        }
    }

    // info 级别日志
    static INFO(tag: string, msg: string) {
        if (isPrintLog && isPrintInfoLog) {
            console.info(`${bastTag} tag: ${tag} --> ${msg}`)
        }
    }

    // warn 级别日志
    static WARN(tag: string, msg: string) {
        if (isPrintLog && isPrintWarnLog) {
            console.warn(`${bastTag} tag: ${tag} --> ${msg}`)
        }
    }

    // error 级别日志
    static ERROR(tag: string, msg: string) {
        if (isPrintLog && isPrintErrorLog) {
            console.error(`${bastTag} tag: ${tag} --> ${msg}`)
        }
    }
}
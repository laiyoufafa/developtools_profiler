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

import { fileOpen,travelFile } from '../profiler/base/BaseProfilerUtils';


export function getCpuCoreInfo(): Array<String> {
    const basePath = "/sys/devices/system/cpu/cpufreq"
    let defaultPolicy = new Array(
        "policy0", "policy1", "policy2"
    )
    var supportPolicy = []
    let policyArr = travelFile(basePath,"policy")

    policyArr.forEach((policy)=>{
        defaultPolicy.forEach((defaultItem)=>{
            if (defaultItem == policy) {
                supportPolicy.push(defaultItem)
            }
        })
    })
    var coreArr = new Array()
    for (var index = 0; index < supportPolicy.length; index++) {
        const policy = supportPolicy[index];
        var affected_cpus = fileOpen(basePath + "/" + policy + "/affected_cpus")
        coreArr.push(affected_cpus.charAt(0))
    }
    return coreArr
}



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
import { getPidOfAbility } from '../utils/SystemUtils';
import SPLogger from '../utils/SPLogger'
import nativeProfiler from "libsmartperf.so"


const TAG = "NativeTaskFun"

export class NativeTaskFun {
    static initAllFun() {
        globalThis.CreateNativeFps = ((pkgName: string) => {
            let fpsStr: string = nativeProfiler.getFpsData(pkgName)
//            SPLogger.DEBUG(TAG, "nativeProfiler" + "--> fpsStr:" + fpsStr)
            return fpsStr
        })
        globalThis.CreateNativeRam = (() => {
            let ramStr: string = nativeProfiler.getRamData(globalThis.processPid)
//            SPLogger.DEBUG(TAG, "nativeProfiler" + "--> ramStr :" + ramStr)
            globalThis.ramArr.push(ramStr)
            return ramStr
        })

        globalThis.CheckDaemon = (() => {
            let status: string = nativeProfiler.checkDaemon()
//            SPLogger.DEBUG(TAG, "nativeProfiler" + "--> daemon status :" + status)
            return status
        })

        globalThis.checkAccess = ((path: string) => {
//            SPLogger.DEBUG("BaseProfilerUtils","native check path is start..."+ "path:" + path);
            let status: string = nativeProfiler.checkAccess(path)
//            SPLogger.DEBUG("BaseProfilerUtils","native check path is finish..."+ "path:" + path);
//            SPLogger.DEBUG(TAG, "nativeProfiler --> "+ path + " status :" + status)
            return status
        })

        let status = globalThis.CheckDaemon()
        SPLogger.DEBUG(TAG, "nativeProfiler" + "--> daemon status :" + status)
    }
}

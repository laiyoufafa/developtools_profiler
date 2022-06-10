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

import Ability from '@ohos.application.Ability'
import { initDb } from '../common/database/LocalRepository'
import { FloatWindowFun } from '../common/ui/floatwindow/FloatWindowFun'
import { NativeTaskFun } from "../common/profiler/NativeTaskFun"
import { NetWork } from '../common/profiler/item/NetWork';
import BundleManager from '../common/utils/BundleMangerUtils';
import SPLogger from '../common/utils/SPLogger'


var abilityWindowStage
const TAG = "MainAbility"

export default class MainAbility extends Ability {


    onCreate(want, launchParam) {
        globalThis.showFloatingWindow=false
        // Ability is creating, initialize resources for this ability
        BundleManager.getAppList().then(appList => {
            globalThis.appList = appList
            SPLogger.DEBUG(TAG,"appList-->" + JSON.stringify(appList))
            SPLogger.DEBUG(TAG,"appList-->-->-->-->-->-->-->-->-->-->-->-->-->-->-->-->-->-->")
            SPLogger.DEBUG(TAG,"globalThis.appList-->" + JSON.stringify(globalThis.appList))
        })
    }

    onDestroy() {
        // Ability is destroying, release resources for this ability
        SPLogger.DEBUG(TAG,"[MyApplication] MainAbility onDestroy")
    }

    onWindowStageCreate(windowStage) {
        console.info("zhaohui:-------------->onWindowStageCreate 1")
        globalThis.abilityContext = this.context
        console.info("zhaohui:-------------->onWindowStageCreate 2")
        // Main window is created, set main page for this ability
        SPLogger.DEBUG(TAG,"[MyApplication] MainAbility onWindowStageCreate")
        console.info("zhaohui:-------------->onWindowStageCreate 3")
        abilityWindowStage = windowStage;
        console.info("zhaohui:-------------->onWindowStageCreate 4")
        abilityWindowStage.setUIContent(this.context, "pages/LoginPage", null)
        console.info("zhaohui:-------------->onWindowStageCreate 5")
    }

    onWindowStageDestroy() {
        // Main window is destroyed, release UI related resources
        SPLogger.DEBUG(TAG,"[MyApplication] MainAbility onWindowStageDestroy")
    }

    onForeground() {
        initDb()
        // Ability has brought to foreground
        FloatWindowFun.initAllFun()
        NativeTaskFun.initAllFun()
        //check netWork
        NetWork.getInstance().init()
    }

    onBackground() {
        // Ability has back to background
        SPLogger.DEBUG(TAG,"[MyApplication] MainAbility onBackground")
    }
};

export function initTest() {

}




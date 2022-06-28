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
import { NetWork } from '../common/profiler/item/NetWork';
import { MainWorker } from '../common/profiler/MainWorkProfiler';
import BundleManager from '../common/utils/BundleMangerUtils';

var abilityWindowStage
export default class MainAbility extends Ability {

    onCreate(want, launchParam) {
        globalThis.showFloatingWindow=false
        BundleManager.getAppList().then(appList => {
            globalThis.appList = appList
        })
    }
    onDestroy() {}
    onWindowStageCreate(windowStage) {
        globalThis.abilityContext = this.context
        abilityWindowStage = windowStage;
        abilityWindowStage.setUIContent(this.context, "pages/LoginPage", null)
        globalThis.useDaemon = false
    }
    onWindowStageDestroy() {}
    onForeground() {
        initDb()
        FloatWindowFun.initAllFun()
        NetWork.getInstance().init()
        MainWorker.postMessage({ "testConnection": true })

    }
    onBackground() {}
};





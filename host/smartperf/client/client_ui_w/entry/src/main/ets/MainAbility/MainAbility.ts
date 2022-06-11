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
import wm from '@ohos.window'
import dataRdb from '@ohos.data.rdb'
import display from '@ohos.display'
import BundleManager from '../common/utils/BundleMangerUtils';
import { sql_t_general_info, dbVersion } from '../common/storage/ConstantsDb';
import { ReportItem, getReportListDb } from '../common/data/LocalCommon';
import SPLogger from '../common/utils/SPLogger'

var abilityWindowStage;
const TAG = "MainAbility"

export default class MainAbility extends Ability {
    floatingWindowOffsetX: number = 50
    floatingWindowOffsetY: number = 200
    titleWindowOffsetX: number = 300
    titleWindowOffsetY: number = 200
    lineChartWindowOffsetX: number= 700
    lineChartWindowOffsetY: number= 200
    windowWidth: number = 2560
    windowHeight: number = 1600

    onCreate(want, launchParam) {

        display.getDefaultDisplay().then((dp) => {
            this.windowWidth = dp.width;
            this.windowHeight = dp.height
        })

        globalThis.showFloatingWindow = false
        globalThis.abilityContext = this.context
        initApps()
        SPLogger.DEBUG(TAG, "[MyApplication] MainAbility initApp finished")
        initDb()
        SPLogger.DEBUG(TAG, "[MyApplication] MainAbility initDb finished")

    }

    onDestroy() {
        // Ability is destroying, release resources for this ability
        SPLogger.DEBUG(TAG, "[MyApplication] MainAbility onDestroy")
    }

    onWindowStageCreate(windowStage) {
        // Main window is created, set main page for this ability
        SPLogger.DEBUG(TAG, "[MyApplication] MainAbility onWindowStageCreate")
        abilityWindowStage = windowStage;
        abilityWindowStage.setUIContent(this.context, "pages/LoginPage", null)
    }

    onWindowStageDestroy() {
        // Main window is destroyed, release UI related resources
        SPLogger.DEBUG(TAG, "[MyApplication] MainAbility onWindowStageDestroy")
    }

    onForeground() {
        initDb()
        // Ability has brought to foreground
        globalThis.CreateFloatingWindow = (() => {
            //5.5SP2  2106 改成 8
            wm.create(globalThis.abilityContext, 'floatingWindow', 2106).then((floatWin) => {
                floatWin.moveTo(this.floatingWindowOffsetX, this.floatingWindowOffsetY).then(() => {
                    floatWin.resetSize(95, 95).then(() => {
                        floatWin.getProperties().then((property) => {
                            property.isTransparent = false
                        })
                        floatWin.loadContent('pages/FloatBall').then(() => {
                            floatWin.setBackgroundColor("#00000000").then(() => { //透明
                                console.log("xhq win setTransparent end.");

                                    floatWin.show().then(() => {
                                        globalThis.showFloatingWindow = true
                                    })

                            })
                        })
                    })
                })
            })
        })
        globalThis.MoveFloatingWindow = ((offsetX: number, offsetY: number) => {
            var xx = (this.floatingWindowOffsetX + offsetX * 2) < 0 ? 0 : ((this.floatingWindowOffsetX + offsetX * 2) > (this.windowWidth - 200) ? (this.windowWidth - 200) : (this.floatingWindowOffsetX + offsetX * 2))
            var yy = (this.floatingWindowOffsetY + offsetY * 2) < 0 ? 0 : ((this.floatingWindowOffsetY + offsetY * 2) > (this.windowHeight - 200) ? (this.windowHeight - 200) : (this.floatingWindowOffsetY + offsetY * 2))

            wm.find("floatingWindow").then((fltWin) => {
                fltWin.moveTo(xx, yy)
            })
        })

        globalThis.SetFloatingWindowPosition = ((offsetX: number, offsetY: number) => {
            this.floatingWindowOffsetX = (this.floatingWindowOffsetX + offsetX * 2) < 0 ? 0 : ((this.floatingWindowOffsetX + offsetX * 2) > (this.windowWidth - 200) ? (this.windowWidth - 200) : (this.floatingWindowOffsetX + offsetX * 2))
            this.floatingWindowOffsetY = (this.floatingWindowOffsetY + offsetY * 2) < 0 ? 0 : ((this.floatingWindowOffsetY + offsetY * 2) > (this.windowHeight - 200) ? (this.windowHeight - 200) : (this.floatingWindowOffsetY + offsetY * 2))
        })

        globalThis.DestroyFloatingWindow = (() => {
            wm.find("floatingWindow").then((fltWin) => {
                fltWin.destroy().then(() => {
                    globalThis.showFloatingWindow = false
                })
            })
        })

        globalThis.CreateTitleWindow = (() => {
            wm.create(globalThis.abilityContext, 'TitleWindow', 2106).then((floatWin) => {
                floatWin.moveTo(this.titleWindowOffsetX, this.titleWindowOffsetY).then(() => {
                    floatWin.resetSize(290, 330).then(() => {
                        floatWin.getProperties().then((property) => {
                            property.isTransparent = false
                        })
                        floatWin.loadContent('pages/TitleWindowPage').then(() => {
                            floatWin.setBackgroundColor("#00000000").then(() => { //透明
                                console.log("xhq win setTransparent end.");
                            })
                            floatWin.hide()
                            SPLogger.DEBUG(TAG, 'CreateTitleWindow Done');
                        })
                    })
                })
            })
        })

        globalThis.MoveTitleWindow = ((offsetX: number, offsetY: number) => {
            var xx = (this.titleWindowOffsetX + offsetX * 2) < 0 ? 0 : ((this.titleWindowOffsetX + offsetX * 2) > (this.windowWidth - 500) ? (this.windowWidth - 500) : (this.titleWindowOffsetX + offsetX * 2))
            var yy = (this.titleWindowOffsetY + offsetY * 2) < 0 ? 0 : ((this.titleWindowOffsetY + offsetY * 2) > (this.windowHeight - 330) ? (this.windowHeight - 330) : (this.titleWindowOffsetY + offsetY * 2))
            wm.find("TitleWindow").then((fltWin) => {
                fltWin.moveTo(xx, yy)
            })
        })

        globalThis.SetTitleWindowPosition = ((offsetX: number, offsetY: number) => {
            this.titleWindowOffsetX = (this.titleWindowOffsetX + offsetX * 2) < 0 ? 0 : ((this.titleWindowOffsetX + offsetX * 2) > (this.windowWidth - 500) ? (this.windowWidth - 500) : (this.titleWindowOffsetX + offsetX * 2))
            this.titleWindowOffsetY = (this.titleWindowOffsetY + offsetY * 2) < 0 ? 0 : ((this.titleWindowOffsetY + offsetY * 2) > (this.windowHeight - 330) ? (this.windowHeight - 330) : (this.titleWindowOffsetY + offsetY * 2))
        })


        globalThis.DestroyTitleWindow = (() => {
            wm.find("TitleWindow").then((fltWin) => {
                fltWin.destroy().then(() => {
                })
            })
        })

        globalThis.HideTitleWindow = (() => {
            wm.find("TitleWindow").then((fltWin) => {
                fltWin.hide()
            })
        })

        globalThis.ShowTitleWindow = (() => {
            wm.find("TitleWindow").then((fltWin) => {
                fltWin.show()
            })
        })
    }

    onBackground() {
        // Ability has back to background
        SPLogger.DEBUG(TAG, "[MyApplication] MainAbility onBackground")
    }
};

export function initApps() {
    BundleManager.getAppList().then(appList => {
        globalThis.appList = appList

    })
}

export function initDb() {

    const STORE_CONFIG = {
        name: "gp.db"
    }

    dataRdb.getRdbStore(globalThis.abilityContext, STORE_CONFIG, dbVersion)
        .then(rdbStore => {
            rdbStore.executeSql(sql_t_general_info, null).catch(err => {
                SPLogger.DEBUG(TAG, "--> createTable sql_t_general_info err:" + err)
            })
            SPLogger.DEBUG(TAG, "--> createTable start execute sql_t_general_info success:" + sql_t_general_info)
            return rdbStore
        })


    getReportListDb().then(res => {
        SPLogger.DEBUG(TAG, "getReportListDb" + "--> MainAbility createDd 2")
        globalThis.reportList = res
        let bundleNameArr = []
        for (let reportItemKey in globalThis.reportList) {
            bundleNameArr.push(globalThis.reportList[reportItemKey].packageName)
        }
        BundleManager.getIconByBundleName(bundleNameArr).then(map => {
            globalThis.iconMap = map
        })

        let resReport: Array<ReportItem> = res

        globalThis.sumTest = resReport.length
        globalThis.sumTestTime = 0

        let sumTestAppMap = new Map
        for (let resReportKey in resReport) {
            sumTestAppMap.set(resReport[resReportKey].appName, "")
            SPLogger.DEBUG(TAG, "getReportListDb success:" + resReport[resReportKey].testDuration)
            globalThis.sumTestTime += Number(resReport[resReportKey].testDuration).valueOf()
        }
        globalThis.sumTestApp = sumTestAppMap.size
        SPLogger.DEBUG(TAG, "getReportListDb success:" + globalThis.sumTest + " " + globalThis.sumTestTime + " " + globalThis.sumTestApp);
    }).catch(err => {
        SPLogger.ERROR(TAG, "console" + err);
    })
}



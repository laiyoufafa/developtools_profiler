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

import data_Preferences from '@ohos.data.preferences'

const TAG = "StorageUtils"

export default {
    async putSyncValue(key: string, value: string) {
        console.log(TAG + "--> putSyncValue  :start ")
        var path = globalThis.abilityContext.getDataBaseDir()
        console.info(TAG + "Get the preferences failed, path: " + path)
        data_Preferences.getPreferences(this.context, 'mystore', function (err, preferences) {
            if (err) {
                console.info(TAG + "Get the preferences failed, path: " + path + '/mystore')
                return;
            }
            preferences.put(key, value)
            preferences.flush()
            console.log(TAG + "--> putSyncValue  fileName:success ")
        })
        console.log(TAG + "--> putSyncValue  fileName: " + '/mystore' + "  key: " + key + "  value: " + value)
    },

    async getSyncValue(key: string, defaultValue: string) {

        var path = this.context.getDataBaseDir()
        data_Preferences.getPreferences(this.context, 'mystore', function (err, preferences) {
            if (err) {
                console.info("Get the preferences failed, path: " + path + '/mystore')
                return;
            }
            preferences.get(key, defaultValue, function (err, value) {
                if (err) {
                    console.info("Get the value of startup failed with err: " + err)
                    return
                }
                console.info("The value of startup is " + value)
            })
            preferences.flushSync()
        })
    },


    async deleteSyncValue(key: string) {

        var path = await globalThis.abilityContext.getFilesDir()
        let storage = data_Preferences.getStorageSync(path + '/mystore')
        storage.deleteSync(key)
        storage.flushSync()
        console.log(TAG + "--> deleteSyncValue  fileName: " + '/mystore' + "  key: " + key)
    },

    async clearSyncValue() {

        var path = await globalThis.abilityContext.getFilesDir()
        let storage = data_Preferences.getStorageSync(path + '/mystore')
        storage.clearSync()
        storage.flushSync()
        console.log(TAG + "--> clearSyncValue  fileName: " + '/mystore')
    }
}

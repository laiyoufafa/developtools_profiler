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
import window from '@ohos.window';
import rpc from '@ohos.rpc';
import featureAbility from '@ohos.ability.featureAbility';
import particleAbility from '@ohos.ability.particleAbility';


const TAG = "AbilityUtils"
let connectionNumber = -1
let proxyGlobal = null
/**
 * 启动ability  API8 写法 去除want 以及 featureAbility
 * @param bundleName
 * @param abilityName
 */
export function commonStartAbility(bundleName: string, abilityName: string, parameters?: { [key: string]: any }) {
    console.info(TAG + "Operation bundleName:" + bundleName + "Operation abilityName:" + abilityName);
    let str = {
        //    "want": {
        "bundleName": bundleName,
        "abilityName": abilityName,
        "parameters": parameters
        //    }
    }
    console.info(abilityName + ' Operation after. Cause:');
    globalThis.abilityContext.startAbility(str, (err, data) => {
        if (err) {
            console.error(abilityName + ' Operation failed. Cause:' + JSON.stringify(err));
            return;
        }
        console.info(abilityName + ' Operation successful. Data: ' + JSON.stringify(data))
    });
}


/**
 * 连接 service ability
 * @param bundleName
 * @param abilityName
 */
export function commonConnectAbility(bundleName: string, abilityName: string, parameters?: { [key: string]: any }, sendData?: number) {

    console.info(TAG + ' Operation before. Cause:');
    let want = {
        "bundleName": bundleName,
        "abilityName": abilityName,
        "parameters": parameters
    }
    let connection = {
        onConnect: function (elementName, proxy) {
            proxyGlobal = proxy
            console.log(TAG + 'onConnect SUCCESS,elementName1:' + elementName.abilityName);

            if (proxy == null) {
                console.log(' proxy == null');
                return;
            }
            let option = new rpc.MessageOption();
            let data = new rpc.MessageParcel();
            let reply = new rpc.MessageParcel();
            data.writeInt(sendData);
            data.writeInt(99);
            proxy.sendRequest(1, data, reply, option).then(function (result) {
                let msg = result.reply.readInt();
                console.log(TAG + ' connect commonConnectAbility result1: ' + msg);
            }).catch(function (e) {
                console.log("err:" + e)
            })
            let msg = reply.readInt();
            console.log(TAG + ' connect result1: ' + msg);
        },
        onDisconnect: function (elementName) {
            console.log(TAG + 'onDisConnect SUCCESS1');
        },
        onFailed: function () {
            console.log(TAG + 'onFailed1');
        }
    }
    connectionNumber = featureAbility.connectAbility(want, connection)
    console.info(TAG + ' Operation after. Cause:');
}

/**
 * send消息(不太好使)
 * @param data
 */
export function commonSendRequest(data: number) {
    console.info(TAG + ' commonSendRequest before. Cause:');
    let option = new rpc.MessageOption();
    if (proxyGlobal != null) {
        proxyGlobal.sendRequest(1, data, 99, option).then(function (result) {
            let msg = result.reply.readInt();
            console.log(TAG + ' commonSendRequest result1: ' + msg);
        }).catch(function (e) {
            console.log("err:" + e)
        })
    } else {
        console.log(TAG + 'proxy is null');
    }
    console.info(TAG + ' commonSendRequest after. Cause:');
}

/**
 * 断开 service ability
 */
export function commonDisConnectAbility() {

    console.info(TAG + ' Operation before. Cause:');
    if (connectionNumber != -1) {
        featureAbility.disconnectAbility(connectionNumber).then((data) => {
            console.info('Operation succeeded: ' + data);
        }).catch((error) => {
            console.error('Operation failed. Cause: ' + error);
        })
    }
    console.info(TAG + ' Operation after. Cause:');

}

/**
 * service ability 调用其他ability
 * @param bundleName
 * @param abilityName
 */
export function commonServiceAbility(bundleName: string, abilityName: string) {

    var str = {
        "want": {
            "deviceId": "",
            "bundleName": bundleName,
            "abilityName": abilityName,
            "uri": "",
            "type": "phone",
            "action": "",
            "parameters": {},
            "entities": []
        },
        "abilityStartSetting": {}
    };
    particleAbility.startAbility(str).then((data) => {
        console.info(abilityName + ' Operation succeeded: ' + data);
    }).catch((error) => {
        console.error(abilityName + ' Operation failed. Cause: ' + error);
    })

}





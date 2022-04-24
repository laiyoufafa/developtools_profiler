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

import net_socket from '@ohos.net.socket';

export let IPv4 = 1

export let IPv4BindAddr = {
    address: "127.0.0.1",
    family: IPv4,
    port: 8283
}

export let UdpSendAddress = {
    address: "127.0.0.1",
    family: IPv4,
    port: 8283
}

export class UDPOP {
    public udp = null
    public messageQueue: Array<String> = new Array()

    constructor() {
        this.udp = net_socket.constructUDPSocketInstance()
    }

    initUDP() {
        this.udp.bind(IPv4BindAddr)
        this.udp.on("message", this.SockOnMessage)
    }

    sendUDP(message) {
        if (this.udp != null) {
            this.udp.send({
                address: UdpSendAddress,
                data: message
            })
        }
        console.log("SockOnMessage send:")
    }

    closeUDP() {
        if (this.udp != null) {
            this.udp.close()
        }
    }

    checkUDP() {
        this.udp.getState(function (error, state) {
            if (error !== undefined) {
                console.log("udp get state error" + JSON.stringify(error))
            } else if (state !== undefined) {
                console.log("udp get state = " + JSON.stringify(state))
            } else {
                console.log("udp error and state both undefined")
            }
        })
    }

    SockOnMessage(data) {
        let buffer = data.message
        let dataView = new DataView(buffer)
        let str = ""
        for (let i = 0;i < dataView.byteLength; ++i) {
            str += String.fromCharCode(dataView.getUint8(i))
        }

        console.log("SockOnMessage recv:" + str)
        this.messageQueue.push(str)
        globalThis.ramArr.push(str)
    }
}
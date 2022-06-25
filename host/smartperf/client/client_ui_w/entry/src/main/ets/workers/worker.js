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
import worker from '@ohos.worker'; // 导入worker模块
import net_socket from '@ohos.net.socket';

let parentPort = worker.parentPort; // 获取parentPort属性

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

export let flagPackageNum = 0

export let udp = net_socket.constructUDPSocketInstance()
udp.bind(IPv4BindAddr, err => {
    if (err) {
        console.log('Worker socket bind fail');
        return;
    }
    console.log('Worker socket bind success');
    udp.getState((err, data) => {
        if (err) {
            console.log('Worker socket getState fail');
            return;
        }
        console.log('Worker socket getState success:' + JSON.stringify(data));
    })
})


parentPort.onexit = function (e) {
    console.log("Worker  onexit")
}

parentPort.onerror = function (e) {
    console.log("Worker onerror")
}

parentPort.onmessageerror = function (e) {
    console.log("Worker onmessageerror")
}

udp.on('listening', () => {
    console.log("Worker socket on listening success");
});
udp.on('close', () => {
    console.log("Worker socket on close success");
});

udp.on('error', err => {
    console.log("Worker socket on error, err:" + JSON.stringify(err))
});
parentPort.onmessage = function (e) {

    let socketCollectItems = e.data
    console.log("sub worker recv:" + JSON.stringify(e.data));

    let messageSetPkg = "set_pkgName::" + socketCollectItems.pkg
    udp.getState((err, data) => {
        if (err) {
            parentPort.postMessage("FPS$-1")
            console.log("Worker socket getState error", err);
        }
        console.log('Worker socket getState success:' + JSON.stringify(data));

        if (flagPackageNum < 2) {
            udp.send({
                address: UdpSendAddress,
                data: messageSetPkg
            })
        }
        flagPackageNum++

        if (socketCollectItems.fps) {
            let messageFps = "get_fps_and_jitters::0::0"
            if (socketCollectItems.is_video) {
                messageFps = "get_fps_and_jitters::1::0"
            } else if (socketCollectItems.is_camera) {
                messageFps = "get_fps_and_jitters::0::1"
            }
            udp.send({
                address: UdpSendAddress,
                data: messageFps
            })
            console.log("sub worker messageFps :" + messageFps);

        }
        if (socketCollectItems.ram) {
            let messageRam = "get_ram_info::" + socketCollectItems.pkg
            udp.send({
                address: UdpSendAddress,
                data: messageRam
            })
            console.log("sub worker messageRam :" + messageRam);
        }

        if (socketCollectItems.screen_capture) {
            udp.send({
                address: UdpSendAddress,
                data: "get_capture"
            })
            console.log("sub worker screen_capture :" + screen_capture);
        }

        if (socketCollectItems.temperature) {
            let messageTemperature = "get_temperature"
            udp.send({
                address: UdpSendAddress,
                data: messageTemperature
            })
            console.log("sub worker get_temperature :" + messageTemperature);
        }
        if (socketCollectItems.cpu_load) {
            let messageCpuLoad = "get_cpu_load"
            udp.send({
                address: UdpSendAddress,
                data: messageCpuLoad
            })
            console.log("sub worker messageCpuLoad :" + messageCpuLoad);
        }
        if (socketCollectItems.power) {
            let messagePower = "get_power"
            udp.send({
                address: UdpSendAddress,
                data: messagePower
            })
            console.log("sub worker messagePower :" + messagePower);
        }
        if (socketCollectItems.catch_trace_start) {
            let messageTrace = "catch_trace_start"
            udp.send({
                address: UdpSendAddress,
                data: messageTrace
            })
        }
        if (socketCollectItems.catch_trace_finish) {
            let messageTrace = "catch_trace_finish::" + socketCollectItems.traceName
            udp.send({
                address: UdpSendAddress,
                data: messageTrace
            })
        }


    })
}

udp.on("message", function (data) {
    let buffer = data.message
    let dataView = new DataView(buffer)
    let str = ""
    for (let i = 0;i < dataView.byteLength; ++i) {
        str += String.fromCharCode(dataView.getUint8(i))
    }
    try {
        if (contains(str, "pss", true)) {
            parentPort.postMessage("RAM$" + str)
        } else if (contains(str, "fps", true)) {
            if (str.indexOf("$$") != -1) {
                let arrStr = str.split("$$")
                if(arrStr.length > 0){
                    if(arrStr[0].indexOf("||") != -1 && arrStr[1].indexOf("||") != -1){
                        let fps = arrStr[0].split("||")
                        let fpsJitter = arrStr[1].split("||")
                        parentPort.postMessage("FPS$" + fps[1].toString() + "$" + fpsJitter[1].toString())
                    }
                }
            }
        } else if (contains(str, "cpu_load", true)) {
            if (str.indexOf("::") != -1) {
                let arrStr = str.split("::")
                if (arrStr.length > 0) {
                    let loadArrStr = arrStr[1]
                    console.log("SockOnMessage cpu_load:" + JSON.stringify(loadArrStr));
                    parentPort.postMessage("CPU_LOAD$" + loadArrStr)

                }
            }
        } else if (contains(str, "temperature", true)) {
            if (str.indexOf("::") != -1) {
                let arrStr = str.split("::")
                if (arrStr.length > 0) {
                    let tempArrStr = arrStr[1]
                    console.log("SockOnMessage tempArr:" + JSON.stringify(tempArrStr));
                    parentPort.postMessage("TEMP$" + tempArrStr)
                }
            }
        }
    } catch (e) {
        console.log("SockOnMessage recv callback err:" + e)
    }

})


function contains(string, substr, isIgnoreCase) {

    if (isIgnoreCase) {
        string = string.toLowerCase();
        substr = substr.toLowerCase();
    }
    var startChar = substr.substring(0, 1);
    var strLen = substr.length;

    for (var j = 0; j < string.length - strLen + 1; j++) {

        if (string.charAt(j) == startChar) //如果匹配起始字符,开始查找
        {
            if (string.substring(j, j + strLen) == substr) //如果从j开始的字符与str匹配，那ok
            {
                return true;
            }
        }
    }
    return false;
}


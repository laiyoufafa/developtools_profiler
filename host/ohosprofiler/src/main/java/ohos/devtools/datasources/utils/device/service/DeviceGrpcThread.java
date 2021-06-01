/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

package ohos.devtools.datasources.utils.device.service;

import ohos.devtools.datasources.transport.hdc.HdcCommandEnum;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.dao.DeviceUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DeviceGrpcThread
 *
 * @version 1.0
 * @date 2021/3/4 19:02
 **/
public class DeviceGrpcThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(DeviceGrpcThread.class);
    private final DeviceUtil sqlUtil = new DeviceUtil();
    private final HdcWrapper hdcHelper = HdcWrapper.getInstance();
    private DeviceIPPortInfo info;

    public DeviceGrpcThread(DeviceIPPortInfo info) {
        this.info = info;
    }

    /**
     * run
     */
    @Override
    public void run() {
        doGrpcRequest(info);
    }

    /**
     * doGrpcRequest
     *
     * @param info info
     */
    public void doGrpcRequest(DeviceIPPortInfo info) {
        String serialNumber = info.getDeviceID();
        String ip = info.getIp();
        int port = info.getPort();
        int forwardPort = info.getForwardPort();

        // 进行端口转发
        hdcForward(serialNumber, ip, forwardPort, port);

        // 1.根据serialNumber发送grpc请求
        LOGGER.debug("Device {} sent a grpc request", serialNumber);

        // 2.test: 自己造数据
        DeviceInfo deviceInfo = getDeviceInfo(info);

        // 返回数据后，先清空表数据，再插入。
        sqlUtil.insertDeviceInfo(deviceInfo);
    }

    private DeviceInfo getDeviceInfo(DeviceIPPortInfo info) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceID(info.getDeviceID());
        deviceInfo.setDeviceName(info.getDeviceName());
        deviceInfo.setRamInfo("8GB");
        deviceInfo.setRomInfo("128GB");
        return deviceInfo;
    }

    private void hdcForward(String serialNumber, String ip, int forwardPort, int port) {
        String cmdStr =
            HdcCommandEnum.HDC_STR.getHdcCommand() + " " + " -t " + serialNumber + " " + HdcCommandEnum.HDC_FPORT_STR
                .getHdcCommand() + " " + ip + ":" + forwardPort + " " + ip + ":" + port;
        LOGGER.debug("pushFile cmdStr = {}", cmdStr);
        hdcHelper.getHdcStringResult(cmdStr);
    }

}

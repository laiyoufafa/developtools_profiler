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

import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_FOR_PORT;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_FOR_PORT;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * device forwarding port class
 */
public class DeviceForwardPort {
    private static final Logger LOGGER = LogManager.getLogger(DeviceForwardPort.class);
    private static volatile DeviceForwardPort instance;

    /**
     * getInstance
     *
     * @return DeviceForwardPort
     */
    public static DeviceForwardPort getInstance() {
        if (instance == null) {
            synchronized (MultiDeviceManager.class) {
                if (instance == null) {
                    instance = new DeviceForwardPort();
                }
            }
        }
        return instance;
    }

    private DeviceForwardPort() {
    }

    /**
     * forward port to local
     *
     * @param info info
     * @return int
     */
    public int forwardDevicePort(DeviceIPPortInfo info) {
        String serialNumber = info.getDeviceID();
        while (true) {
            int forward = getForwardPort();
            ArrayList<String> cmdStr;
            if (IS_SUPPORT_NEW_HDC && info.getDeviceType() == LEAN_HOS_DEVICE) {
                cmdStr = conversionCommand(HDC_STD_FOR_PORT, serialNumber, String.valueOf(forward));
            } else {
                cmdStr = conversionCommand(HDC_FOR_PORT, serialNumber, String.valueOf(forward));
            }
            String res = HdcWrapper.getInstance().getHdcStringResult(cmdStr);
            if (!res.contains("cannot bind")) {
                return forward;
            }
        }
    }

    /**
     * getForwardPort
     *
     * @return int
     */
    public int getForwardPort() {
        int length = 55535;
        int off = 10000;
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            LOGGER.info("create Random NoSuchAlgorithmException ", noSuchAlgorithmException);
            return getForwardPort();
        }
        return secureRandom.nextInt(length) + off;
    }
}

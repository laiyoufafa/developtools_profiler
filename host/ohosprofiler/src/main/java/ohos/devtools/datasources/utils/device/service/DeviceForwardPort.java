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
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * 设备转发端口类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
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
     * 设置设备的IP和端口号信息
     *
     * @param info info
     * @return DeviceIPPortInfo
     */
    public DeviceIPPortInfo setDeviceIPPortInfo(DeviceIPPortInfo info) {
        String serialNumber = info.getDeviceID();
        while (true) {
            int forward = getForwardPort();
            String cmdStr =
                String.format(Locale.ENGLISH, HdcCommandEnum.HDC_FOR_PORT.getHdcCommand(), serialNumber, forward);
            String res = HdcWrapper.getInstance().getHdcStringResult(cmdStr);
            if (!res.contains("cannot bind")) {
                info.setForwardPort(forward);
                LOGGER.info("prot is {}", res);
                break;
            }
        }
        return info;
    }

    /**
     * getForwardPort
     *
     * @return int
     */
    public int getForwardPort() {
        int length = 55535;
        int off = 10000;
        SecureRandom secureRandom = null;
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            LOGGER.info("create Random has Execption {}", noSuchAlgorithmException.getMessage());
            return getForwardPort();
        }
        int anInt = secureRandom.nextInt(length) + off;
        return anInt;
    }
}

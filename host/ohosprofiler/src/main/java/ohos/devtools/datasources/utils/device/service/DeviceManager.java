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

import ohos.devtools.datasources.utils.common.util.PrintUtil;
import ohos.devtools.datasources.utils.device.dao.DeviceUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 设备管理类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public final class DeviceManager {
    private static final Logger LOGGER = LogManager.getLogger(DeviceManager.class);
    private DeviceIPPortInfo info;
    private final DeviceUtil sqlUtil = new DeviceUtil();
    private final ThreadPoolExecutor executor =
        new ThreadPoolExecutor(1, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * DeviceManager
     *
     * @param info info
     */
    public DeviceManager(DeviceIPPortInfo info) {
        this.info = info;
    }

    /**
     * init
     */
    public void init() {
        String deviceID = info.getDeviceID();
        if (!sqlUtil.hasDevice(deviceID)) {
            executor.execute(new DeviceGrpcThread(info));
        } else {
            PrintUtil.print(LOGGER, "The data is already in the device details table", 1);
        }
    }
}

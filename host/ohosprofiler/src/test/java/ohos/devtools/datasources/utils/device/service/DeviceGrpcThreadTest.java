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

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DeviceGrpcThreadTest
 *
 * @version 1.0
 * @date 2021/04/05 17:12
 **/
public class DeviceGrpcThreadTest {
    private DeviceGrpcThread deviceGrpcThread;
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * functional testing getDeviceGrpcThread
     *
     * @tc.name: getDeviceGrpcThread
     * @tc.number: OHOS_JAVA_device_DeviceGrpcThread_getDeviceGrpcThread_0001
     * @tc.desc: getDeviceGrpcThread
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Before
    public void getDeviceGrpcThread() {
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setPort(1);
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setDeviceName("");
        deviceIPPortInfo.setDeviceID("");
        deviceIPPortInfo.setDeviceType("");
        deviceGrpcThread = new DeviceGrpcThread(deviceIPPortInfo);
    }

    /**
     * functional testing run
     *
     * @tc.name: DeviceGrpcThread run
     * @tc.number: OHOS_JAVA_device_DeviceGrpcThread_run_0001
     * @tc.desc: DeviceGrpcThread run
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void run() {
        deviceGrpcThread.run();
        Assert.assertTrue(true);
    }
}
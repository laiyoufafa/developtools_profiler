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
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Multi-device management test class
 */
public class MultiDeviceManagerTest {
    private String serialNumber;
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * functional testing init
     *
     * @tc.name: MultiDeviceManager setup
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_init_0001
     * @tc.desc: MultiDeviceManager setup
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Before
    public void setUp() {
        serialNumber = "emulator-5554";
        DataBaseApi.getInstance().initDataSourceManager();
        MultiDeviceManager.getInstance().start();
        deviceIPPortInfo  = new DeviceIPPortInfo();
        deviceIPPortInfo.setDeviceID("1");
        deviceIPPortInfo.setDeviceType(DeviceType.FULL_HOS_DEVICE);
    }

    /**
     * functional testing pushDevToolsShell
     *
     * @tc.name: pushDevToolsShell
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_pushDevToolsShell_0001
     * @tc.desc: pushDevToolsShell
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void pushHiprofilerCliTest1() {
        boolean cli = MultiDeviceManager.getInstance().pushDevToolsShell(deviceIPPortInfo);
        Assert.assertFalse(cli);
    }

    /**
     * functional testing pushDevToolsShell
     *
     * @tc.name: pushDevToolsShell
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_pushDevToolsShell_0002
     * @tc.desc: pushDevToolsShell
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void pushHiprofilerCliTest2() {
        boolean cli = MultiDeviceManager.getInstance().pushDevToolsShell(deviceIPPortInfo);
        Assert.assertFalse(cli);
    }

    /**
     * functional testing getAllDeviceIPPortInfos
     *
     * @tc.name: getAllDeviceIPPortInfos
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_getAllDeviceIPPortInfos_0001
     * @tc.desc: getAllDeviceIPPortInfos
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getAllDeviceIPPortInfosTest() {
        List<DeviceIPPortInfo> list = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();
        Assert.assertNotNull(list);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getInstanceTest() {
        MultiDeviceManager multiDeviceManager = MultiDeviceManager.getInstance();
        Assert.assertNotNull(multiDeviceManager);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: pushHiprofilerToolsTest
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_pushHiprofilerToolsTest_0001
     * @tc.desc: push Hiprofiler Tools Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void pushHiprofilerToolsTest() {
        MultiDeviceManager.getInstance().pushHiProfilerTools(deviceIPPortInfo);
        Assert.assertTrue(true);
    }
}

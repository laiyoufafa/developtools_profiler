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
import ohos.devtools.datasources.utils.device.entity.DeviceInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 多设备管理测试类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class MultiDeviceManagerTest {
    private String serialNumber;

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
        MultiDeviceManager.getInstance().run();
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
        boolean cli = MultiDeviceManager.getInstance().pushDevToolsShell(serialNumber);
        Assert.assertTrue(cli);
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
        boolean cli = MultiDeviceManager.getInstance().pushDevToolsShell(serialNumber);
        Assert.assertTrue(cli);
    }

    /**
     * functional testing getDeviceIPPort
     *
     * @tc.name: getDeviceIPPort
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_getDeviceIPPort_0001
     * @tc.desc: getDeviceIPPort
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getDeviceIPPortTest() {
        ArrayList<ArrayList<String>> deviceIPPortList = MultiDeviceManager.getInstance().getDeviceIPPort(serialNumber);
        Assert.assertNotNull(deviceIPPortList);
    }

    /**
     * functional testing getDevicesInfo
     *
     * @tc.name: getDevicesInfo
     * @tc.number: OHOS_JAVA_device_MultiDeviceManager_getDevicesInfo_0001
     * @tc.desc: getDevicesInfo
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getDevicesInfoTest() {
        List<DeviceInfo> devicesInfo = MultiDeviceManager.getInstance().getDevicesInfo();
        Assert.assertNotNull(devicesInfo);
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
        List<DeviceIPPortInfo> list = MultiDeviceManager.getInstance().getAllDeviceIPPortInfos();
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

}

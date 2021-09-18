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

package ohos.devtools.datasources.utils.device.dao;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Util Test
 */
public class DeviceUtilTest {
    private ProcessInfo processInfo;
    private DeviceIPPortInfo deviceIPPortInfo;
    private ArrayList<DeviceIPPortInfo> deviceIPPortList;
    private DeviceDao deviceUtil;

    /**
     * functional testing init
     *
     * @tc.name: DeviceUtil init
     * @tc.number: OHOS_JAVA_device_DeviceUtil_init_0001
     * @tc.desc: DeviceUtil init
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Before
    public void init() {
        SessionManager.getInstance().setDevelopMode(true);
        // 应用初始化 Step1 初始化数据中心
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        deviceUtil = new DeviceDao();
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setDeviceID("1");
        deviceIPPortInfo.setPort(5001);
        deviceIPPortInfo.setForwardPort(5001);
        deviceIPPortInfo.setDeviceName("");
        deviceIPPortInfo.setDeviceType(DeviceType.FULL_HOS_DEVICE);
        deviceIPPortList = new ArrayList<>();
        deviceIPPortList.add(deviceIPPortInfo);
        processInfo = new ProcessInfo();
        processInfo.setDeviceId("1");
        processInfo.setProcessId(1);
        processInfo.setProcessName("com.go.maps");
    }

    /**
     * functional testing init
     *
     * @tc.name: deleteExceptDeviceIPPortTest
     * @tc.number: OHOS_JAVA_device_DeviceUtil_deleteExceptDeviceIPPortTest_0001
     * @tc.desc: delete Except Device IP Port Test
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void deleteExceptDeviceIPPortTest001() {
        deviceUtil.deleteExceptDeviceIPPort(deviceIPPortList);
        Assert.assertTrue(true);
    }

    /**
     * functional testing init
     *
     * @tc.name: deleteAllDeviceIPPortInfoTest
     * @tc.number: OHOS_JAVA_device_DeviceUtil_deleteAllDeviceIPPortInfoTest_0001
     * @tc.desc: delete All Device IP Port Info Test
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void deleteAllDeviceIPPortInfoTest001() {
        deviceUtil.deleteAllDeviceIPPortInfo();
        Assert.assertTrue(true);
    }

    /**
     * functional testing init
     *
     * @tc.name: hasDeviceTest
     * @tc.number: OHOS_JAVA_device_DeviceUtil_hasDeviceTest_0001
     * @tc.desc: has Device Test
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void hasDeviceTest001() {
        boolean res = deviceUtil.hasDeviceIPPort("123");
        Assert.assertFalse(res);
    }

    /**
     * functional testing init
     *
     * @tc.name: hasDeviceTest
     * @tc.number: OHOS_JAVA_device_DeviceUtil_hasDeviceIPPortTest_0001
     * @tc.desc: has Device IP Port Test
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void hasDeviceIPPortTest001() {
        boolean res = deviceUtil.hasDeviceIPPort("123");
        Assert.assertFalse(res);
    }

    /**
     * functional testing init
     *
     * @tc.name: getAllDeviceIPPortInfosTest
     * @tc.number: OHOS_JAVA_device_DeviceUtil_getAllDeviceIPPortInfosTest_0001
     * @tc.desc: get All Device IP Port Infos Test
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getAllDeviceIPPortInfosTest001() {
        List<DeviceIPPortInfo> allDeviceIPPortInfos = deviceUtil.getAllDeviceIPPortInfos();
        Assert.assertNotNull(allDeviceIPPortInfos);
    }
}
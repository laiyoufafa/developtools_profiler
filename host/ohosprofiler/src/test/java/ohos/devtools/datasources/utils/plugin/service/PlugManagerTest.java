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

package ohos.devtools.datasources.utils.plugin.service;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPlugin;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * PlugManagerTest
 *
 * @version 1.0
 * @date 2021/03/31 10:10
 **/
public class PlugManagerTest {
    private DeviceIPPortInfo deviceInfo;

    /**
     * functional testing init
     *
     * @tc.name: PlugManager init
     * @tc.number: OHOS_JAVA_plugin_PlugManager_init_0001
     * @tc.desc: PlugManager init
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Before
    public void setDeviceInfo() {
        SessionManager.getInstance().setDevelopMode(true);
        String serialNumber = "emulator-5554";
        deviceInfo = new DeviceIPPortInfo();
        deviceInfo.setDeviceID(serialNumber);
        DataBaseApi.getInstance().initDataSourceManager();
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: PlugManager getInstance
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getInstance_0001
     * @tc.desc: PlugManager getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getInstance01() {
        PlugManager plugManager = PlugManager.getInstance();
        Assert.assertNotNull(plugManager);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: PlugManager getInstance
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getInstance_0002
     * @tc.desc: PlugManager getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getInstance02() {
        PlugManager plugManager01 = PlugManager.getInstance();
        PlugManager plugManager02 = PlugManager.getInstance();
        Assert.assertEquals(plugManager01, plugManager02);
    }

    /**
     * functional testing insertPlugInfo
     *
     * @tc.name: PlugManager insertPlugInfo
     * @tc.number: OHOS_JAVA_plugin_PlugManager_insertPlugInfo_0001
     * @tc.desc: PlugManager insertPlugInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void insertPlugInfoTest() {
        HiProfilerPlugin hiProfilerPlugin =
            HiProfilerPlugin.builder().deviceId("dsf").version("dsfaf").sampleInterval(3).plugSha256("dshfjka")
                .status(1).id(3).name("tst").build();
        hiProfilerPlugin.setPath("C:\\ohoslibmemdataplugin.z.so");
        hiProfilerPlugin.toString();
        PlugManager.getInstance().insertPlugInfo(hiProfilerPlugin);
    }

    /**
     * functional testing selectPlugConfig
     *
     * @tc.name: PlugManager selectPlugConfig
     * @tc.number: OHOS_JAVA_plugin_PlugManager_selectPlugConfig_0001
     * @tc.desc: PlugManager selectPlugConfig
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void selectPlugConfigTest() {
        List<HiProfilerPlugin> list = PlugManager.getInstance().selectPlugConfig(deviceInfo.getDeviceID());
        Assert.assertNotNull(list);
    }

}

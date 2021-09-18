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
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.pluginconfig.CpuConfig;
import ohos.devtools.pluginconfig.MemoryConfig;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.memory.MemoryItemView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * PlugManagerTest
 */
public class PlugManagerTest {
    private DeviceIPPortInfo deviceInfo;
    private PluginConf pluginConf;

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
        pluginConf = new PluginConf("", "", null, false, null);
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
     * functional testing getPluginConfigTest
     *
     * @tc.name: PlugManager getPluginConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getPluginConfigTest_0001
     * @tc.desc: PlugManager getPluginConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getPluginConfigTest01() {
        List<PluginConf> list = PlugManager.getInstance().getPluginConfig(null, null);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getPluginConfigTest
     *
     * @tc.name: PlugManager getPluginConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getPluginConfigTest_0002
     * @tc.desc: PlugManager getPluginConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getPluginConfigTest02() {
        List<PluginConf> list = PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, null);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getPluginConfigTest
     *
     * @tc.name: PlugManager getPluginConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getPluginConfigTest_0003
     * @tc.desc: PlugManager getPluginConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getPluginConfigTest03() {
        List<PluginConf> list = PlugManager.getInstance().getPluginConfig(null, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getPluginConfigTest
     *
     * @tc.name: PlugManager getPluginConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getPluginConfigTest_0004
     * @tc.desc: PlugManager getPluginConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getPluginConfigTest04() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing getPluginConfigTest
     *
     * @tc.name: PlugManager getPluginConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getPluginConfigTest_0005
     * @tc.desc: PlugManager getPluginConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getPluginConfigTest05() {
        PlugManager.getInstance().clearPluginConfList();
        pluginConf.setPluginMode(PluginMode.OFFLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing loadingPlugsTest
     *
     * @tc.name: PlugManager loadingPlugsTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugsTest_0001
     * @tc.desc: PlugManager loadingPlugsTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugsTest01() {
        PlugManager.getInstance().loadingPlugs(null);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugsTest
     *
     * @tc.name: PlugManager loadingPlugsTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugsTest_0002
     * @tc.desc: PlugManager loadingPlugsTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugsTest02() {
        List<Class<? extends IPluginConfig>> pluginConfigs = new ArrayList<>();
        PlugManager.getInstance().loadingPlugs(pluginConfigs);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugsTest
     *
     * @tc.name: PlugManager loadingPlugsTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugsTest_0003
     * @tc.desc: PlugManager loadingPlugsTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugsTest03() {
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(CpuConfig.class);
        plugConfigList.add(MemoryConfig.class);
        PlugManager.getInstance().loadingPlugs(plugConfigList);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugsTest
     *
     * @tc.name: PlugManager loadingPlugsTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugsTest_0004
     * @tc.desc: PlugManager loadingPlugsTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugsTest04() {
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(null);
        PlugManager.getInstance().loadingPlugs(plugConfigList);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugsTest
     *
     * @tc.name: PlugManager loadingPlugsTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugsTest_0005
     * @tc.desc: PlugManager loadingPlugsTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugsTest05() {
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(null);
        PlugManager.getInstance().loadingPlugs(plugConfigList);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugTest
     *
     * @tc.name: PlugManager loadingPlugTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugTest_0001
     * @tc.desc: PlugManager loadingPlugTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugTest01() {
        PlugManager.getInstance().loadingPlug(null);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugTest
     *
     * @tc.name: PlugManager loadingPlugTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugTest_0002
     * @tc.desc: PlugManager loadingPlugTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugTest02() {
        PlugManager.getInstance().loadingPlug(MemoryConfig.class);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugTest
     *
     * @tc.name: PlugManager loadingPlugTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugTest_0003
     * @tc.desc: PlugManager loadingPlugTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugTest03() {
        PlugManager.getInstance().loadingPlug(IPluginConfig.class);
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugTest
     *
     * @tc.name: PlugManager loadingPlugTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugTest_0004
     * @tc.desc: PlugManager loadingPlugTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugTest04() {
        PlugManager.getInstance().loadingPlug(new CpuConfig().getClass());
        Assert.assertTrue(true);
    }

    /**
     * functional testing loadingPlugTest
     *
     * @tc.name: PlugManager loadingPlugTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_loadingPlugTest_0005
     * @tc.desc: PlugManager loadingPlugTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void loadingPlugTest05() {
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(null);
        PlugManager.getInstance().loadingPlug(plugConfigList.get(1));
        Assert.assertTrue(true);
    }

    /**
     * functional testing registerPluginTest
     *
     * @tc.name: PlugManager registerPluginTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_registerPluginTest_0001
     * @tc.desc: PlugManager registerPluginTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void registerPluginTest01() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing registerPluginTest
     *
     * @tc.name: PlugManager registerPluginTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_registerPluginTest_0002
     * @tc.desc: PlugManager registerPluginTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void registerPluginTest02() {
        PlugManager.getInstance().clearPluginConfList();
        pluginConf.setPluginMode(PluginMode.OFFLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing registerPluginTest
     *
     * @tc.name: PlugManager registerPluginTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_registerPluginTest_0003
     * @tc.desc: PlugManager registerPluginTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void registerPluginTest03() {
        PlugManager.getInstance().registerPlugin(null);
        Assert.assertTrue(true);
    }

    /**
     * functional testing registerPluginTest
     *
     * @tc.name: PlugManager registerPluginTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_registerPluginTest_0004
     * @tc.desc: PlugManager registerPluginTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void registerPluginTest04() {
        PlugManager.getInstance().clearPluginConfList();
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing registerPluginTest
     *
     * @tc.name: PlugManager registerPluginTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_registerPluginTest_0005
     * @tc.desc: PlugManager registerPluginTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void registerPluginTest05() {
        PlugManager.getInstance().clearPluginConfList();
        PlugManager.getInstance().registerPlugin(new PluginConf("pluginFileName", "", null, true, null));
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing addPluginStartSuccessTest
     *
     * @tc.name: PlugManager addPluginStartSuccessTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_addPluginStartSuccessTest_0001
     * @tc.desc: PlugManager addPluginStartSuccessTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void addPluginStartSuccessTest01() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(1L);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing addPluginStartSuccessTest
     *
     * @tc.name: PlugManager addPluginStartSuccessTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_addPluginStartSuccessTest_0002
     * @tc.desc: PlugManager addPluginStartSuccessTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void addPluginStartSuccessTest02() {
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        PlugManager.getInstance().addPluginStartSuccess(1L, null);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(1L);
        PluginConf pluginConfParam = list.get(0);
        Assert.assertNull(pluginConfParam);
    }

    /**
     * functional testing addPluginStartSuccessTest
     *
     * @tc.name: PlugManager addPluginStartSuccessTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_addPluginStartSuccessTest_0003
     * @tc.desc: PlugManager addPluginStartSuccessTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void addPluginStartSuccessTest03() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(0L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing addPluginStartSuccessTest
     *
     * @tc.name: PlugManager addPluginStartSuccessTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_addPluginStartSuccessTest_0004
     * @tc.desc: PlugManager addPluginStartSuccessTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void addPluginStartSuccessTest04() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(Long.MAX_VALUE, pluginConf);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(Long.MAX_VALUE);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing addPluginStartSuccessTest
     *
     * @tc.name: PlugManager addPluginStartSuccessTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_addPluginStartSuccessTest_0005
     * @tc.desc: PlugManager addPluginStartSuccessTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void addPluginStartSuccessTest05() {
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(1L);
        int num = list.size();
        Assert.assertEquals(2, num);
    }

    /**
     * functional testing getProfilerPlugConfigTest
     *
     * @tc.name: PlugManager getProfilerPlugConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerPlugConfigTest_0001
     * @tc.desc: PlugManager getProfilerPlugConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerPlugConfigTest01() {
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(0L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getProfilerPlugConfigTest
     *
     * @tc.name: PlugManager getProfilerPlugConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerPlugConfigTest_0002
     * @tc.desc: PlugManager getProfilerPlugConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerPlugConfigTest02() {
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(Long.MAX_VALUE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getProfilerPlugConfigTest
     *
     * @tc.name: PlugManager getProfilerPlugConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerPlugConfigTest_0003
     * @tc.desc: PlugManager getProfilerPlugConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerPlugConfigTest03() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(10L);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing getProfilerPlugConfigTest
     *
     * @tc.name: PlugManager getProfilerPlugConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerPlugConfigTest_0004
     * @tc.desc: PlugManager getProfilerPlugConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerPlugConfigTest04() {
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        PlugManager.getInstance().addPluginStartSuccess(10L, null);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(10L);
        PluginConf pluginConfParam = list.get(0);
        Assert.assertNull(pluginConfParam);
    }

    /**
     * functional testing getProfilerPlugConfigTest
     *
     * @tc.name: PlugManager getProfilerPlugConfigTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerPlugConfigTest_0005
     * @tc.desc: PlugManager getProfilerPlugConfigTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerPlugConfigTest05() {
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        PlugManager.getInstance().addPluginStartSuccess(10L, new PluginConf("pluginFileName", "", null, true, null));
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(10L);
        PluginConf pluginConfParam = list.get(0);
        Assert.assertNotNull(pluginConfParam);
    }

    /**
     * functional testing getProfilerMonitorItemListTest
     *
     * @tc.name: PlugManager getProfilerMonitorItemListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerMonitorItemListTest_0001
     * @tc.desc: PlugManager getProfilerMonitorItemListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerMonitorItemListTest01() {
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(0L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getProfilerMonitorItemListTest
     *
     * @tc.name: PlugManager getProfilerMonitorItemListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerMonitorItemListTest_0002
     * @tc.desc: PlugManager getProfilerMonitorItemListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerMonitorItemListTest02() {
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(Long.MAX_VALUE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getProfilerMonitorItemListTest
     *
     * @tc.name: PlugManager getProfilerMonitorItemListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerMonitorItemListTest_0003
     * @tc.desc: PlugManager getProfilerMonitorItemListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerMonitorItemListTest03() {
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        pluginConf.setMonitorItem(memoryItem);
        pluginConf.setChartPlugin(true);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(10L);
        ProfilerMonitorItem profilerMonitorItem = list.get(0);
        Assert.assertNotNull(profilerMonitorItem);
    }

    /**
     * functional testing getProfilerMonitorItemListTest
     *
     * @tc.name: PlugManager getProfilerMonitorItemListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerMonitorItemListTest_0004
     * @tc.desc: PlugManager getProfilerMonitorItemListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerMonitorItemListTest04() {
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        pluginConf.setChartPlugin(true);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(10L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing getProfilerMonitorItemListTest
     *
     * @tc.name: PlugManager getProfilerMonitorItemListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_getProfilerMonitorItemListTest_0005
     * @tc.desc: PlugManager getProfilerMonitorItemListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void getProfilerMonitorItemListTest05() {
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        pluginConf.setMonitorItem(memoryItem);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        ProfilerMonitorItem memoryItem2 = new ProfilerMonitorItem(10, "Memory", MemoryItemView.class);
        pluginConf.setMonitorItem(memoryItem2);
        pluginConf.setChartPlugin(true);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(10L);
        int num = list.size();
        Assert.assertEquals(2, num);
    }

    /**
     * functional testing clearProfilerMonitorItemMapTest
     *
     * @tc.name: PlugManager clearProfilerMonitorItemMapTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearProfilerMonitorItemMapTest_0001
     * @tc.desc: PlugManager clearProfilerMonitorItemMapTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearProfilerMonitorItemMapTest01() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(10L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearProfilerMonitorItemMapTest
     *
     * @tc.name: PlugManager clearProfilerMonitorItemMapTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearProfilerMonitorItemMapTest_0002
     * @tc.desc: PlugManager clearProfilerMonitorItemMapTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearProfilerMonitorItemMapTest02() {
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(10L);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing clearProfilerMonitorItemMapTest
     *
     * @tc.name: PlugManager clearProfilerMonitorItemMapTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearProfilerMonitorItemMapTest_0003
     * @tc.desc: PlugManager clearProfilerMonitorItemMapTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearProfilerMonitorItemMapTest03() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        List<PluginConf> list = PlugManager.getInstance().getProfilerPlugConfig(10L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearProfilerMonitorItemMapTest
     *
     * @tc.name: PlugManager clearProfilerMonitorItemMapTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearProfilerMonitorItemMapTest_0004
     * @tc.desc: PlugManager clearProfilerMonitorItemMapTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearProfilerMonitorItemMapTest04() {
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        pluginConf.setMonitorItem(memoryItem);
        pluginConf.setChartPlugin(true);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(10L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearProfilerMonitorItemMapTest
     *
     * @tc.name: PlugManager clearProfilerMonitorItemMapTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearProfilerMonitorItemMapTest_0005
     * @tc.desc: PlugManager clearProfilerMonitorItemMapTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearProfilerMonitorItemMapTest05() {
        pluginConf.setChartPlugin(true);
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        pluginConf.setMonitorItem(memoryItem);
        PlugManager.getInstance().addPluginStartSuccess(1L, pluginConf);
        PlugManager.getInstance().clearProfilerMonitorItemMap();
        pluginConf.setMonitorItem(memoryItem);
        PlugManager.getInstance().addPluginStartSuccess(10L, pluginConf);
        List<ProfilerMonitorItem> list = PlugManager.getInstance().getProfilerMonitorItemList(1L);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearPluginConfListTest
     *
     * @tc.name: PlugManager clearPluginConfListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearPluginConfListTest_0001
     * @tc.desc: PlugManager clearPluginConfListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearPluginConfListTest01() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        PlugManager.getInstance().clearPluginConfList();
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearPluginConfListTest
     *
     * @tc.name: PlugManager clearPluginConfListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearPluginConfListTest_0002
     * @tc.desc: PlugManager clearPluginConfListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearPluginConfListTest02() {
        PlugManager.getInstance().registerPlugin(pluginConf);
        PlugManager.getInstance().clearPluginConfList();
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * functional testing clearPluginConfListTest
     *
     * @tc.name: PlugManager clearPluginConfListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearPluginConfListTest_0003
     * @tc.desc: PlugManager clearPluginConfListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearPluginConfListTest03() {
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        PlugManager.getInstance().registerPlugin(pluginConf);
        PlugManager.getInstance().clearPluginConfList();
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearPluginConfListTest
     *
     * @tc.name: PlugManager clearPluginConfListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearPluginConfListTest_0004
     * @tc.desc: PlugManager clearPluginConfListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearPluginConfListTest04() {
        PluginConf pluginConfNew = new PluginConf("pluginFileName", "", null, true, null);
        pluginConfNew.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConfNew);
        PlugManager.getInstance().clearPluginConfList();
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.ONLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }

    /**
     * functional testing clearPluginConfListTest
     *
     * @tc.name: PlugManager clearPluginConfListTest
     * @tc.number: OHOS_JAVA_plugin_PlugManager_clearPluginConfListTest_0005
     * @tc.desc: PlugManager clearPluginConfListTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5SH
     */
    @Test
    public void clearPluginConfListTest05() {
        pluginConf.setPluginMode(PluginMode.OFFLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        PlugManager.getInstance().clearPluginConfList();
        pluginConf.setPluginMode(PluginMode.ONLINE);
        PlugManager.getInstance().registerPlugin(pluginConf);
        List<PluginConf> list =
            PlugManager.getInstance().getPluginConfig(DeviceType.FULL_HOS_DEVICE, PluginMode.OFFLINE);
        int num = list.size();
        Assert.assertEquals(0, num);
    }
}

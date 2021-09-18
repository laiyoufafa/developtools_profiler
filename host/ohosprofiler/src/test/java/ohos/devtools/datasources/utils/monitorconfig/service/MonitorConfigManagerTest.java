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

package ohos.devtools.datasources.utils.monitorconfig.service;

import com.alibaba.fastjson.JSONObject;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.monitorconfig.entity.MonitorInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitor Config Manager Test
 */
public class MonitorConfigManagerTest {
    private long localSessionId = 0;
    private JSONObject jsonObject;
    private DeviceIPPortInfo device;
    private ProcessInfo processInfo;
    private MonitorConfigManager monitorConfigManager;
    private LinkedList<MonitorInfo> monitorInfo;

    /**
     * functional testing setAnalyzeJson
     *
     * @tc.name: MonitorConfigManager setAnalyzeJson
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigManager_setAnalyzeJson_0001
     * @tc.desc: MonitorConfigManager setAnalyzeJson
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Before
    public void setAnalyzeJson() {
        SessionManager.getInstance().setDevelopMode(true);
        localSessionId = 1000L;
        jsonObject = new JSONObject();
        JSONObject memoryObject = new JSONObject();
        memoryObject.put("Select All", true);
        memoryObject.put("Java", true);
        memoryObject.put("Native", true);
        memoryObject.put("Graphics", true);
        memoryObject.put("Stack", true);
        memoryObject.put("Code", true);
        memoryObject.put("Others", true);
        JSONObject cPUObject = new JSONObject();
        cPUObject.put("Select All1", true);
        cPUObject.put("System Memory1", true);
        cPUObject.put("Gpu Memory1", true);
        cPUObject.put("BandWidth1", true);
        jsonObject.put("Memory", memoryObject);
        jsonObject.put("CPU", cPUObject);
        device = new DeviceIPPortInfo();
        device.setIp("");
        device.setForwardPort(5001);
        processInfo = new ProcessInfo();
        processInfo.setProcessId(1);
        processInfo.setProcessName("process");
        monitorConfigManager = MonitorConfigManager.getInstance();
        DataBaseApi.getInstance().initDataSourceManager();
        ConcurrentHashMap<Long, Map<String, LinkedList<String>>> dataMap = monitorConfigManager.dataMap;
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: MonitorConfigManager getInstance
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigManager_getInstance_0001
     * @tc.desc: MonitorConfigManager getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void getInstance01() {
        MonitorConfigManager configManager = MonitorConfigManager.getInstance();
        Assert.assertNotNull(configManager);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: MonitorConfigManager getInstance
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigManager_getInstance_0002
     * @tc.desc: MonitorConfigManager getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void getInstance02() {
        MonitorConfigManager configManager01 = MonitorConfigManager.getInstance();
        MonitorConfigManager configManager02 = MonitorConfigManager.getInstance();
        Assert.assertEquals(configManager01, configManager02);
    }

    /**
     * functional testing analyzeCharTarget
     *
     * @tc.name: MonitorConfigManager analyzeCharTarget
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigManager_analyzeCharTarget_0001
     * @tc.desc: MonitorConfigManager analyzeCharTarget
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void analyzeCharTargetTest1() {
        Map<String, LinkedList<String>> stringLinkedListMap =
            MonitorConfigManager.getInstance().analyzeCharTarget(localSessionId, jsonObject);
        Assert.assertNotNull(stringLinkedListMap);
    }

    /**
     * functional testing analyzeCharTarget
     *
     * @tc.name: MonitorConfigManager analyzeCharTarget
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigManager_analyzeCharTarget_0002
     * @tc.desc: MonitorConfigManager analyzeCharTarget
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void analyzeCharTargetTest3() {
        Map<String, LinkedList<String>> stringLinkedListMap =
            MonitorConfigManager.getInstance().analyzeCharTarget(localSessionId, jsonObject);
        Map<String, LinkedList<String>> stringLinkedListMap1 =
            MonitorConfigManager.getInstance().analyzeCharTarget(localSessionId, jsonObject);
        Assert.assertEquals(stringLinkedListMap, stringLinkedListMap1);
    }
}

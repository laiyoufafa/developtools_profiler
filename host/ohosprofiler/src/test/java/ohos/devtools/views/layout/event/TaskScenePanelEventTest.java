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

package ohos.devtools.views.layout.event;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.hoscomp.HosJLabel;
import ohos.devtools.views.layout.swing.TaskPanel;
import ohos.devtools.views.layout.swing.TaskScenePanel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;

/**
 * @Description TaskPanelEventTest
 * @Date 2021/4/3 14:35
 **/
public class TaskScenePanelEventTest {
    private TaskScenePanelEvent taskScenePanelEvent;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelEvent_getTaskScenePanelEvent_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void getTaskScenePanelEvent() {
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        taskScenePanelEvent = new TaskScenePanelEvent();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelEvent_obtainMap_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void obtainMap() {
        DeviceIPPortInfo deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setPort(5555);
        deviceIPPortInfo.setIp("");
        ProcessInfo processInfo = new ProcessInfo();
        HashMap<DeviceIPPortInfo, ProcessInfo> deviceIPPortInfoProcessInfoHashMap = new HashMap<>();
        deviceIPPortInfoProcessInfoHashMap.put(deviceIPPortInfo, processInfo);
        Constant.map.put("test", deviceIPPortInfoProcessInfoHashMap);
        TaskScenePanel taskScenePanel = new TaskScenePanel();
        taskScenePanel.addCheckBox();
        TaskPanel taskPanel = new TaskPanel();
        List<HosJLabel> hosJLabels = taskScenePanelEvent.obtainMap(taskScenePanel, taskPanel);
        Assert.isEmpty(hosJLabels);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelEvent_gain_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void gain() {
        JPanel taskPanel = new JPanel();
        String gain = taskScenePanelEvent.gain(taskPanel);
        org.junit.Assert.assertNotNull(gain);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelEvent_judgCompontent_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void judgCompontent() {
        JPanel jPanel = new JPanel();
        JPanel jPanel2 = new JPanel();
        jPanel.add(jPanel2);
        Component[] components = jPanel.getComponents();
        String test = taskScenePanelEvent.judgCompontent(components, "test");
        org.junit.Assert.assertNotNull(test);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelEvent_lastStep_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void lastStep() {
        TaskScenePanel taskScenePanel = new TaskScenePanel();
        TaskPanel taskPanel = new TaskPanel();
        taskScenePanelEvent.lastStep(taskScenePanel, taskPanel);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelEvent_listenerJPanelSouth_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void listenerJPanelSouth() {
        TaskScenePanel taskScenePanel = new TaskScenePanel();
        taskScenePanelEvent.listenerJPanelSouth(taskScenePanel);
    }
}
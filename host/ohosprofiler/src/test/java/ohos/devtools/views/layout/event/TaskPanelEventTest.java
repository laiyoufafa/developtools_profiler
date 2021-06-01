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

import ohos.devtools.views.layout.swing.TaskPanel;
import ohos.devtools.views.layout.swing.TaskScenePanel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;
import java.awt.Component;

/**
 * @Description TaskPanelEventTest
 * @Date 2021/4/3 14:35
 **/
public class TaskPanelEventTest {
    private TaskScenePanelEvent taskScenePanelEvent;
    private TaskScenePanel taskScenePanel;
    private TaskPanel taskPanel;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_getTaskScenePanel_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void getTaskScenePanelEvent() {
        taskScenePanelEvent = new TaskScenePanelEvent();
        taskScenePanel = new TaskScenePanel();
        taskPanel = new TaskPanel();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_clickClose_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void clickCloseTest() {
        taskScenePanelEvent.clickAddDevice(taskScenePanel, taskScenePanelEvent);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_checkBoxSelect_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void checkBoxSelectTest() {
        taskScenePanelEvent.checkBoxSelect(taskScenePanel);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_lastStep_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void lastStepTest() {
        taskScenePanelEvent.lastStep(taskScenePanel, taskPanel);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_listenerJPanelSouth_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void listenerJPanelSouthTest() {
        taskScenePanelEvent.listenerJPanelSouth(taskScenePanel);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_judgCompontent_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void judgCompontentTest() {
        taskScenePanelEvent.judgCompontent(new Component[] {new JPanel()}, "error");
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanelEvent_startTask_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void startTaskTest() {
        taskScenePanelEvent.startTask(taskScenePanel, taskPanel);
        Assert.assertTrue(true);
    }

}
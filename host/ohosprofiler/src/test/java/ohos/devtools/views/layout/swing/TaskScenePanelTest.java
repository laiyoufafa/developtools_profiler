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

package ohos.devtools.views.layout.swing;

import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.Constant;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;

/**
 * @Description TaskScenePanelTest
 * @Date 2021/4/3 10:40
 **/
public class TaskScenePanelTest {
    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanel_initObj_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void initObj() {
        // Application initialization Step1 Initialize the data center
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        Constant.jtasksTab = new JBTabbedPane();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanel_setBorderTopCenter_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setBorderTopCenterTest() {
        TaskScenePanel taskScenePanel = new TaskScenePanel(new TaskPanel(new JPanel(), new TaskPanelWelcome()));
        taskScenePanel.setBorderTopCenter();
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanel_addCheckBox_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void addCheckBoxTest() {
        TaskScenePanel taskScenePanel = new TaskScenePanel(new TaskPanel(new JPanel(), new TaskPanelWelcome()));
        taskScenePanel.addCheckBox();
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanel_setJPanelCenterRight_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setJPanelCenterRightTest() {
        TaskScenePanel taskScenePanel = new TaskScenePanel(new TaskPanel(new JPanel(), new TaskPanelWelcome()));
        taskScenePanel.setJPanelCenterRight();
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanel_setAttributes_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setAttributesTest() {
        TaskScenePanel taskScenePanel = new TaskScenePanel(new TaskPanel(new JPanel(), new TaskPanelWelcome()));
        taskScenePanel.setAttributes();
        Assert.assertTrue(true);
    }
}
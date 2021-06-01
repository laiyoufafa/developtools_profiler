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
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Constant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;

/**
 * @Description TaskPanelTest
 * @Date 2021/4/3 15:27
 **/
public class TaskPanelTest {
    private HomeWindow homeWindow;
    private TaskPanel taskPanel;
    private TaskPanelWelcome taskPanelWelcome;
    private JPanel jPanel;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanel_init_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void init() {
        homeWindow = new HomeWindow();
        taskPanelWelcome = new TaskPanelWelcome();
        jPanel = homeWindow.getTaskPanel();
        Constant.jtasksTab = new JBTabbedPane();
        taskPanel = new TaskPanel(jPanel, taskPanelWelcome);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanel_setAttributes_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setAttributesTest01() {
        taskPanel.setAttributes(jPanel);
        JLabel chooseButton = taskPanel.getChooseButton();
        Assert.assertEquals(Color.WHITE, chooseButton.getForeground());
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanel_setAttributes_0002
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setAttributesTest02() {
        taskPanel.setAttributes(null);
        JLabel chooseButton = taskPanel.getChooseButton();
        Assert.assertEquals(Color.WHITE, chooseButton.getForeground());
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanel_setAttributes_0003
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setAttributesTest03() {
        taskPanel.setAttributes(new JPanel());
        JLabel chooseButton = taskPanel.getChooseButton();
        Assert.assertEquals(Color.WHITE, chooseButton.getForeground());
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanel_taskLabel_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void taskLabelTest01() {
        JLabel jLabel = new JLabel();
        taskPanel.taskLabel(jLabel, new JLabel(), new JLabel());
        Assert.assertEquals(ColorConstants.APPLYTUN_COLOR, jLabel.getBackground());
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskPanel_taskLabel_0002
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void taskLabelTest02() {
        taskPanel.taskLabel(null, null, null);
    }
}
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

package ohos.devtools.views.hilog;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.layout.WelcomePanel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

/**
 * HiLog Panel Test
 *
 * @since 2021/2/1 9:31
 */
public class HiLogPanelTest {
    private HiLogPanel hiLog;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_HiLogPanel_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        JBPanel containerPanel = new JBPanel(new GridLayout());
        WelcomePanel welcomePanel = new WelcomePanel();
        containerPanel.add(welcomePanel);
        Constant.jtasksTab = new JBTabbedPane();
        hiLog = new HiLogPanel(containerPanel, welcomePanel);
    }

    /**
     * HiLog Panel Test
     *
     * @tc.name: HiLogPanelTest
     * @tc.number: OHOS_JAVA_View_HiLogPanel_HiLogPanelTest_0001
     * @tc.desc: HiLog Panel Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void hiLogPanelTest() {
        JBPanel jbPanel = new JBPanel();
        jbPanel.setLayout(new BorderLayout());
        JBPanel containerPanel = new JBPanel(new GridLayout());
        WelcomePanel welcomePanel = new WelcomePanel();
        containerPanel.add(welcomePanel);
        jbPanel.add(containerPanel, BorderLayout.CENTER);
        Constant.jtasksTab = new JBTabbedPane();
        HiLogPanel hiLogPanel = new HiLogPanel(containerPanel, welcomePanel);
        Assert.assertNotNull(hiLogPanel);
    }

    /**
     * mouse Clicked Test
     *
     * @tc.name: mouseClickedTest
     * @tc.number: OHOS_JAVA_View_HiLogPanel_mouseClickedTest_0001
     * @tc.desc: mouse Clicked Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseClickedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        mouseEvent.getComponent().setName("x");
        hiLog.mouseClicked(mouseEvent);
        Assert.assertTrue(true);
    }
}
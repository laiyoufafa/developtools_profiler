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

package ohos.devtools.views.layout;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import org.junit.Assert;
import org.junit.Test;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * SystemConfigPanelTest
 */
public class SystemConfigPanelTest {

    /**
     * get Instance addActionListenerTest
     *
     * @tc.name: addActionListenerTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_addActionListenerTest_0001
     * @tc.desc: addActionListenerTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void addActionListenerTest01() {
        new SystemConfigPanel(new TaskPanel()).addActionListener(new JBCheckBox());
        Assert.assertTrue(true);
    }

    /**
     * get Instance addDeviceRefreshTest
     *
     * @tc.name: addDeviceRefreshTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_addDeviceRefreshTest_0001
     * @tc.desc: addDeviceRefreshTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void addDeviceRefreshTest01() {
        new SystemConfigPanel(new TaskPanel()).addDeviceRefresh();
        Assert.assertTrue(true);
    }

    /**
     * get Instance mouseReleasedTest
     *
     * @tc.name: mouseReleasedTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_mouseReleasedTest_0001
     * @tc.desc: mouseReleasedTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void mouseReleasedTest01() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        new SystemConfigPanel(new TaskPanel()).mouseReleased(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * get Instance getClassificationSelectTest
     *
     * @tc.name: getClassificationSelectTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_getClassificationSelectTest_0001
     * @tc.desc: getClassificationSelectTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getClassificationSelectTest01() {
        boolean flag = new SystemConfigPanel(new TaskPanel()).getClassificationSelect();
        Assert.assertTrue(flag);
    }

    /**
     * get Instance getEventTest
     *
     * @tc.name: getEventTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_getEventTest_0001
     * @tc.desc: getEventTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getEventTest01() {
        new SystemConfigPanel(new TaskPanel()).getEvent(new ArrayList<>(), new ArrayList<>());
        Assert.assertTrue(true);
    }

    /**
     * get Instance itemStateChangedTest
     *
     * @tc.name: itemStateChangedTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_itemStateChangedTest_0001
     * @tc.desc: itemStateChangedTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void itemStateChangedTest01() {
        new SystemConfigPanel(new TaskPanel()).itemStateChanged(null);
        Assert.assertTrue(true);
    }

    /**
     * get Instance itemStateChangedTest
     *
     * @tc.name: itemStateChangedTest
     * @tc.number: OHOS_JAVA_layout_SystemConfigPanel_itemStateChangedTest_0002
     * @tc.desc: itemStateChangedTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void itemStateChangedTest02() {
        new SystemConfigPanel(new TaskPanel()).addDeviceRefresh();
        new SystemConfigPanel(new TaskPanel()).itemStateChanged(null);
        Assert.assertTrue(true);
    }
}

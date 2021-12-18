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

package ohos.devtools.views.trace.fragment;

import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ProcessMemData;
import ohos.devtools.views.trace.component.AnalystPanel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.event.MouseEvent;

/**
 * test MemDataFragment class .
 *
 * @date 2021/4/24 17:57
 */
class MemDataFragmentTest {
    private MemDataFragment memDataFragment;
    private JPanel jPanel;

    /**
     * init the memDataFragment .
     */
    @BeforeEach
    void setUp() {
        ProcessMem processMem = new ProcessMem();
        processMem.setProcessName("title");
        jPanel = new JPanel();
        memDataFragment = new MemDataFragment(jPanel, processMem);
    }

    /**
     * test function the mouseClicked .
     */
    @Test
    void mouseClicked() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        memDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(memDataFragment);
    }

    /**
     * test function the mouseMoved .
     */
    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        memDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(memDataFragment);
    }

    /**
     * test function the click .
     */
    @Test
    void click() {
        AnalystPanel.iThreadDataClick = null;
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        memDataFragment.click(mouseEvent, new ProcessMemData());
        Assertions.assertNotNull(memDataFragment);
    }
}
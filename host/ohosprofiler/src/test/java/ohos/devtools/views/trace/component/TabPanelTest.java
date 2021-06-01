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

package ohos.devtools.views.trace.component;

import org.junit.jupiter.api.Test;

import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test TabPanel class .
 *
 * @version 1.0
 * @date 2021/4/24 18:03
 **/
class TabPanelTest {
    /**
     * test function the mouseDragged .
     */
    @Test
    void mouseDragged() {
        TabPanel tabPanel = new TabPanel();
        MouseEvent mouseEvent = new MouseEvent(tabPanel, 1, 1, 1, 1, 1, 1, true, 1);
        tabPanel.mouseDragged(mouseEvent);
        assertEquals(300, tabPanel.getMHeight());
    }

    /**
     * test function the mouseMoved .
     */
    @Test
    void mouseMoved() {
        TabPanel tabPanel = new TabPanel();
        MouseEvent mouseEvent = new MouseEvent(tabPanel, 1, 1, 1, 1, 1, 1, true, 1);
        tabPanel.mouseMoved(mouseEvent);
        assertEquals(300, tabPanel.getMHeight());
    }

    /**
     * test function the hideInBottom .
     */
    @Test
    void hideInBottom() {
        TabPanel tabPanel = new TabPanel();
        tabPanel.hideInBottom();
        assertEquals(300, tabPanel.getMHeight());
    }

    /**
     * test get the MHeight .
     */
    @Test
    void getMHeight() {
        TabPanel tabPanel = new TabPanel();
        assertEquals(300, tabPanel.getMHeight());
    }

    /**
     * test get the MyHeight .
     */
    @Test
    void getMyHeight() {
        assertEquals(0, TabPanel.getMyHeight());
    }
}
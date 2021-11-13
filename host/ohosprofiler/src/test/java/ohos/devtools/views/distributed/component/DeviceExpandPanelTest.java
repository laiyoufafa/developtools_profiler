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

package ohos.devtools.views.distributed.component;

import ohos.devtools.views.trace.ExpandPanel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeviceExpandPanelTest {
    private static DeviceExpandPanel deviceExpandPanel;

    @BeforeAll
    static void createExpandPanel() {
        deviceExpandPanel = new DeviceExpandPanel("title", "flag");
    }

    @AfterAll
    static void end() {
        deviceExpandPanel = null;
    }

    @Test
    void getDbFlag() {
        assertEquals(deviceExpandPanel.getDbFlag(), "flag");
    }

    @Test
    void getTitle() {
        deviceExpandPanel.setTitle("title");
        assertEquals(deviceExpandPanel.getTitle(), "title");
    }

    @Test
    void setTitle() {
        deviceExpandPanel.setTitle("newTitle");
        assertEquals(deviceExpandPanel.getTitle(), "newTitle");
    }

    @Test
    void addRow() {
        deviceExpandPanel.addRow(new ExpandPanel("title"), "gapleft 5,pushx,growx");
        assertNotNull(deviceExpandPanel);
    }

    @Test
    void getContent() {
        assertNotNull(deviceExpandPanel.getContent());
    }

    @Test
    void refresh() {
        deviceExpandPanel.refresh(0, 0);
    }

    @Test
    void isCollapsed() {
        assertNotNull(deviceExpandPanel.isCollapsed());

    }
}
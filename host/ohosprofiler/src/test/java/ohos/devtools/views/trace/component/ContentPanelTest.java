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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * test the ContentPanel class
 *
 * @version 1.0
 * @date: 2021/4/24 18:03
 **/
class ContentPanelTest {
    private JFrame testFrame;

    /**
     * test function the refreshTab .
     */
    @Test
    void refreshTab() {
        ContentPanel contentPanel = new ContentPanel(new AnalystPanel());
        contentPanel.refreshTab();
        assertNotNull(contentPanel);
    }

    /**
     * test function the refresh .
     */
    @Test
    void refresh() {
        ContentPanel contentPanel = new ContentPanel(new AnalystPanel());
        contentPanel.refresh();
        assertNotNull(contentPanel);
    }

    /**
     * test function the rangeChange .
     */
    @Test
    void rangeChange() {
        ContentPanel contentPanel = new ContentPanel(new AnalystPanel());
        contentPanel.rangeChange(0L, 1000L);
        assertNotNull(contentPanel);
    }

    /**
     * test function the paintComponent .
     */
    @Test
    void paintComponent() {
        JPanel panel = new JPanel() {
            ContentPanel contentPanel = new ContentPanel(new AnalystPanel());

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                contentPanel.paintComponent(graphics);
            }
        };
        this.testFrame.add(panel);
        this.testFrame.setVisible(true);
        this.testFrame.repaint();
        assertNotNull(this.testFrame);
    }

    /**
     * init .
     */
    @BeforeEach
    void setUp() {
        if (this.testFrame == null) {
            this.testFrame = new JFrame();
        }
    }

    /**
     * destroy .
     */
    @AfterEach
    void tearDown() {
        if (this.testFrame != null) {
            this.testFrame.dispose();
            this.testFrame = null;
        }
    }
}
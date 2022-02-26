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

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * tab component
 *
 * @since 2021/04/20 12:12
 */
public class TabPanel extends JBTabbedPane {
    private static int barHeight;

    private final int iconWH = 20;
    private Rectangle topRect;
    private Rectangle bottomRect;
    private Image topImage = null;
    private Image bottomImage = null;
    private JBSplitter splitter;

    /**
     * structure function
     */
    public TabPanel() {
        setFont(Final.NORMAL_FONT);
        try {
            topImage = ImageIO.read(getClass().getResourceAsStream("/assets/top.png"));
            bottomImage = ImageIO.read(getClass().getResourceAsStream("/assets/bottom.png"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                super.mouseClicked(event);
                clickTop(event);
                clickBottom(event);
            }
        });
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (getTabCount() > 0) {
            Rectangle tabBounds = getUI().getTabBounds(this, 0);
            barHeight = tabBounds.height;
        }
        topRect = new Rectangle(getWidth() - 65, (barHeight - iconWH) / 2, iconWH, iconWH);
        bottomRect = new Rectangle(getWidth() - 35, (barHeight - iconWH) / 2, iconWH, iconWH);
        if (topImage != null) {
            graphics
                .drawImage(topImage, Utils.getX(topRect) + 2, Utils.getY(topRect) + 2, iconWH - 5, iconWH - 5, null);
        }
        if (bottomImage != null) {
            graphics
                .drawImage(bottomImage, Utils.getX(bottomRect) + 2, Utils.getY(bottomRect) + 2, iconWH - 5, iconWH - 5,
                    null);
        }
    }

    /**
     * Click to top
     *
     * @param event Mouse event
     */
    private void clickTop(final MouseEvent event) {
        if (topRect.contains(event.getPoint())) {
            if (SysAnalystPanel.getSplitter() != null) {
                this.setVisible(true);
                SysAnalystPanel.getSplitter().setProportion(0.05f);
            }
        }
    }

    /**
     * Click to bottom
     *
     * @param event Mouse event
     */
    private void clickBottom(final MouseEvent event) {
        if (bottomRect.contains(event.getPoint())) {
            hideInBottom();
        }
    }

    /**
     * Minimize the bottom tab
     */
    public void hideInBottom() {
        if (SysAnalystPanel.getSplitter() != null) {
            this.setVisible(true);
            SysAnalystPanel.getSplitter().setProportion(0.95f);
        }
    }

    /**
     * hide bottom tab
     */
    public void hidden() {
        if (SysAnalystPanel.getSplitter() != null) {
            this.setVisible(false);
            SysAnalystPanel.getSplitter().setProportion(1.0f);
        }
    }

    /**
     * display current panel
     */
    public void display() {
        if (SysAnalystPanel.getSplitter() != null) {
            this.setVisible(true);
            SysAnalystPanel.getSplitter().setProportion(0.6f);
        }
    }

    /**
     * display current panel
     *
     * @param proportion proportion
     */
    public void display(float proportion) {
        if (SysAnalystPanel.getSplitter() != null) {
            this.setVisible(true);
            SysAnalystPanel.getSplitter().setProportion(proportion);
        }
    }

}

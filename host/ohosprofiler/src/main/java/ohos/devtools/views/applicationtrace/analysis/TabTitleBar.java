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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * TabThreadStatesBean
 *
 * @since 2021/5/13 13:06
 */
public class TabTitleBar extends JPanel {
    private String title;
    private int activeIndex = 0;
    private Color backColor = JBColor.background();
    private Color focusColor = JBUI.CurrentTheme.Link.linkColor();

    /**
     * structure function
     *
     * @param title title
     */
    public TabTitleBar(@NotNull String title) {
        this.title = title;
        setLayout(new MigLayout("inset 0"));
        setBackground(backColor);
        JBLabel titleLabel = new JBLabel(title);
        add(titleLabel);
    }

    /**
     * add tab
     *
     * @param tabName tabName
     * @param currentIndex currentIndex
     */
    public void addTab(String tabName, int currentIndex) {
        add(new NormalTab(tabName, currentIndex));
    }

    private class NormalTab extends JPanel {
        private final int selectBorderHeight = 2;
        private Color mouseEnterColor = JBColor.background().darker();
        private int currentIndex = 0;
        private boolean isMouseEnter = false;

        /**
         * NormalTab
         *
         * @param tabTitle tabTitle
         * @param currentIndex currentIndex
         */
        public NormalTab(String tabTitle, int currentIndex) {
            setLayout(new MigLayout("inset 0"));
            JBLabel tabTitleLabel = new JBLabel(tabTitle);
            tabTitleLabel.setBorder(JBUI.Borders.empty(10, 15));
            add(tabTitleLabel);
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent event) {
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    activeIndex = currentIndex;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                }

                @Override
                public void mouseEntered(MouseEvent event) {
                    isMouseEnter = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    isMouseEnter = false;
                    repaint();
                }
            });
            this.currentIndex = currentIndex;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            graphics.setColor(backColor);
            graphics.fillRect(0, 0, getWidth(), getHeight());
            if (currentIndex == activeIndex) {
                graphics.setColor(focusColor);
                graphics.fillRect(0, getHeight() - selectBorderHeight, getWidth(), getHeight());
            }
            if (isMouseEnter) {
                graphics.setColor(mouseEnterColor);
                graphics.fillRect(0, 0, getWidth(), getHeight() - selectBorderHeight);
            }
        }
    }

}

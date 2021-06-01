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

import com.intellij.ui.components.JBBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Bottom scroll component
 *
 * @date 2021/04/20 12:24
 * @version 1.0.1
 */
public class BottomScrollPanel extends JBScrollPane {
    /**
     * box container
     */
    protected JBBox box = JBBox.createHorizontalBox();

    /**
     * Constructor
     */
    public BottomScrollPanel() {
        setViewportView(box);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent event) {
                super.mouseEntered(event);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /**
     * @date 2021/04/20 12:24
     * @version 1.0.1
     */
    public static class ChildPanel extends JBPanel {
        /**
         * line height
         */
        public int lineHeight;
        /**
         * line width
         */
        public int lineWidth;

        /**
         * construct with line width and line height
         *
         * @param lineWidth line width
         * @param lineHeight line height
         */
        public ChildPanel(final int lineWidth, final int lineHeight) {
            this.lineHeight = lineHeight;
            this.lineWidth = lineWidth;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension dimension = super.getMinimumSize();
            if (this.lineWidth != 0) {
                dimension.width = lineWidth;
            }
            dimension.height = lineHeight;
            return dimension;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension dimension = super.getPreferredSize();
            if (this.lineWidth != 0) {
                dimension.width = lineWidth;
            }
            dimension.height = lineHeight;
            return dimension;
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension dimension = super.getMaximumSize();
            if (this.lineWidth != 0) {
                dimension.width = lineWidth;
            }
            dimension.height = lineHeight;
            return dimension;
        }
    }

    /**
     * @date 2021/04/20 12:24
     * @version 1.0.1
     */
    public static class ChildLineComponent extends ChildPanel {
        private static final int DEFAULT_LINE_HEIGHT = 27;
        /**
         * Monitor whether the mouse is moved into the component
         */
        public boolean mouseIn;

        /**
         * Constructor
         */
        public ChildLineComponent() {
            super(0, DEFAULT_LINE_HEIGHT);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(final MouseEvent event) {
                    childMouseEntered(event);
                }

                @Override
                public void mouseExited(final MouseEvent event) {
                    childMouseExited(event);
                }

                @Override
                public void mouseClicked(final MouseEvent event) {
                    childMouseClicked(event);
                }
            });
        }

        /**
         * Custom mouse events
         *
         * @param event mouse event
         */
        public void childMouseEntered(final MouseEvent event) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mouseIn = true;
            repaint();
        }

        /**
         * Custom mouse events
         *
         * @param event mouse event
         */
        public void childMouseExited(final MouseEvent event) {
            mouseIn = false;
            repaint();
        }

        /**
         * Custom mouse events
         *
         * @param event mouse event
         */
        public void childMouseClicked(final MouseEvent event) { }
    }
}

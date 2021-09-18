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

package ohos.devtools.views.trace.fragment.graph;

import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.fragment.ruler.AbstractNode;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * Data line graphics
 *
 * @date 2021/04/22 12:25
 */
public abstract class AbstractGraph extends AbstractNode {
    /**
     * fragment
     */
    public AbstractDataFragment fragment;

    /**
     * root
     */
    public JComponent root;

    /**
     * rect
     */
    public Rectangle rect = new Rectangle(0, 0, 0, 0);

    /**
     * Control the focus blur event is triggered only once, the focus blur is initiated by mouseMove
     */
    public boolean flagFocus;

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    public abstract void draw(Graphics2D graphics);

    /**
     * Get focus event
     *
     * @param event event
     */
    public abstract void onFocus(MouseEvent event);

    /**
     * Loss of focus event
     *
     * @param event event
     */
    public abstract void onBlur(MouseEvent event);

    /**
     * Click event
     *
     * @param event event
     */
    public abstract void onClick(MouseEvent event);

    /**
     * Mouse movement event
     *
     * @param event event
     */
    public abstract void onMouseMove(MouseEvent event);

    /**
     * Whether the mouse is inside the graphics area
     *
     * @param event event
     * @return boolean boolean
     */
    public boolean edgeInspect(final MouseEvent event) {
        return rect.contains(event.getPoint());
    }

    /**
     * Redraw the current graph
     */
    public void repaint() {
        if (root != null) {
            root.repaint(rect);
        }
    }

    /**
     * Set rect object
     *
     * @param xAxis x coordinate
     * @param yAxis y coordinate
     * @param width width
     * @param height height
     */
    public void setRect(final double xAxis, final double yAxis, final double width, final double height) {
        if (rect == null) {
            rect = new Rectangle();
        }
        rect.setRect(xAxis, yAxis, width, height);
    }
}

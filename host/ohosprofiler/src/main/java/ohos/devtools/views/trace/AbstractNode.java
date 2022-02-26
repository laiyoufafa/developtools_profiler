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

package ohos.devtools.views.trace;

import com.intellij.ui.components.JBPanel;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * AbstractNode
 *
 * @since 2021/5/14 14:54
 */
public abstract class AbstractNode {
    /**
     * current Rectangle
     */
    protected Rectangle rect = new Rectangle();

    /**
     * boolean is Mouse In this node
     */
    protected boolean isMouseIn;

    /**
     * edge inspect
     *
     * @return return the current Rectangle
     */
    public Rectangle getRect() {
        return rect;
    }

    /**
     * set the Rectangle
     *
     * @param rect rect
     */
    public void setRect(Rectangle rect) {
        this.rect = rect;
    }

    /**
     * node draw function
     *
     * @param paint Graphics2D
     */
    public abstract void draw(Graphics2D paint);

    /**
     * get the string list
     *
     * @param time String
     * @return string list
     */
    public abstract List<String> getStringList(String time);

    /**
     * when the mouse move into this node
     *
     * @param point point
     * @param content content
     */
    public void moveIn(Point point, JBPanel content) {
        isMouseIn = true;
        content.repaint(getRect());
    }

    /**
     * when the mouse move out from this node
     *
     * @param point point
     * @param content content
     */
    public void moveOut(Point point, JBPanel content) {
        isMouseIn = false;
        content.repaint(getRect());
    }

    /**
     * when the node click
     *
     * @param event event
     */
    public void onClick(MouseEvent event) {
        event.getX();
    }
}

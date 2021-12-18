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

package ohos.devtools.views.trace.fragment.ruler;

import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * basic data
 *
 * @since 2021/04/22 12:25
 */
public abstract class AbstractFragment extends AbstractNode implements IFragment {
    private final Color lineColor = new Color(255, 255, 255, 80);
    private final Color textColor = new Color(0x999999);
    private Rectangle rect = new Rectangle(0, 0, 0, 0);
    private Rectangle descRect = new Rectangle(0, 0, 0, 0);
    private Rectangle dataRect = new Rectangle(0, 0, 0, 0);
    private JComponent root;

    /**
     * Gets the value of rect .
     *
     * @return the value of java.awt.Rectangle
     */
    public Rectangle getRect() {
        return rect;
    }

    /**
     * Sets the rect .You can use getRect() to get the value of rect
     *
     * @param rect rect
     */
    public void setRect(final Rectangle rect) {
        this.rect = rect;
    }

    /**
     * Gets the value of descRect .
     *
     * @return the value of java.awt.Rectangle
     */
    public Rectangle getDescRect() {
        return descRect;
    }

    /**
     * Sets the descRect .
     * <p>You can use getDescRect() to get the value of descRect</p>
     *
     * @param descRect descRect
     */
    public void setDescRect(final Rectangle descRect) {
        this.descRect = descRect;
    }

    /**
     * Gets the value of dataRect .
     *
     * @return the value of java.awt.Rectangle
     */
    public Rectangle getDataRect() {
        return dataRect;
    }

    /**
     * Sets the dataRect .
     * <p>You can use getDataRect() to get the value of dataRect</p>
     *
     * @param dataRect dataRect
     */
    public void setDataRect(final Rectangle dataRect) {
        this.dataRect = dataRect;
    }

    /**
     * Gets the value of root .
     *
     * @return the value of javax.swing.JComponent
     */
    public JComponent getRoot() {
        return root;
    }

    /**
     * Sets the root .
     * <p>You can use getRoot() to get the value of root</p>
     *
     * @param jComponent component
     */
    public void setRoot(final JComponent jComponent) {
        this.root = jComponent;
    }

    /**
     * Gets the value of lineColor .
     *
     * @return the value of java.awt.Color
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Gets the value of textColor .
     *
     * @return the value of java.awt.Color
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * repaint.
     */
    public void repaint() {
        if (root != null) {
            root.repaint(rect);
        }
    }

    /**
     * x to time.
     *
     * @param xValue x
     * @return long time
     */
    public long x2ns(final int xValue) {
        long ns = (xValue - Utils.getX(rect)) * AnalystPanel.getDURATION() / (root.getWidth() - Utils.getX(rect));
        return ns;
    }

    /**
     * time to x.
     *
     * @param ns time
     * @return int x
     */
    public int ns2x(final long ns) {
        long xValue = ns * (root.getWidth() - Utils.getX(rect)) / AnalystPanel.getDURATION();
        return (int) xValue + Utils.getX(rect);
    }

    /**
     * edge inspect
     *
     * @param event event
     * @return boolean boolean
     */
    public boolean edgeInspect(final MouseEvent event) {
        return event.getX() >= Utils.getX(rect) && event.getX() <= Utils.getX(rect) + rect.width
            && event.getY() >= Utils.getY(rect) && event.getY() <= Utils.getY(rect) + rect.height;
    }

    /**
     * edge inspect
     *
     * @param rectangle rectangle
     * @param event event
     * @return boolean boolean
     */
    public boolean edgeInspectRect(final Rectangle rectangle, final MouseEvent event) {
        return event.getX() >= Utils.getX(rectangle) && event.getX() <= Utils.getX(rectangle) + rectangle.width
            && event.getY() >= Utils.getY(rectangle) && event.getY() <= Utils.getY(rectangle) + rectangle.height;
    }

}

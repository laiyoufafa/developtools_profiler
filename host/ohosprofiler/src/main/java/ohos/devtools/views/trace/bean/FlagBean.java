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

package ohos.devtools.views.trace.bean;

import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * Little Red Flag Data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class FlagBean extends AbstractGraph {
    private long ns;

    private boolean visible;

    private String name;

    private long time;

    private Color color;

    /**
     * Gets the value of ns .
     *
     * @return the value of long
     */
    public long getNs() {
        return ns;
    }

    /**
     * Sets the ns .
     * <p>You can use getNs() to get the value of ns</p>
     *
     * @param ns Nanoseconds
     */
    public void setNs(final long ns) {
        this.ns = ns;
    }

    /**
     * Gets the value of visible .
     *
     * @return the value of boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visible .
     * <p>You can use getVisible() to get the value of visible</p>
     *
     * @param visible Whether to show
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets the value of name .
     *
     * @return the value of java.lang.String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name .
     * <p>You can use getName() to get the value of name</p>
     *
     * @param name name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the value of time .
     *
     * @return the value of long
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the time .
     * <p>You can use getTime() to get the value of time</p>
     *
     * @param time time
     */
    public void setTime(final long time) {
        this.time = time;
    }

    /**
     * Gets the value of color .
     *
     * @return the value of java.awt.Color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color .
     * <p>You can use getColor() to get the value of color</p>
     *
     * @param color color
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * remove listener
     */
    public void remove() {
        if (eventListener != null) {
            eventListener.delete(this);
        }
    }

    private final int defaultFuncColorIndex = 6;

    /**
     * no parameter structure
     */
    public FlagBean() {
        visible = false;
        color = ColorUtils.FUNC_COLOR[defaultFuncColorIndex];
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (name != null && !name.isEmpty()) {
            graphics.setColor(color);
            final int sx = 202;
            final int xy = 4;
            graphics.drawString(name, sx + rect.x + rect.width, rect.y + rect.height - xy);
        }
    }

    /**
     * Focus acquisition
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
    }

    /**
     * lose focus
     *
     * @param event event
     */
    @Override
    public void onBlur(final MouseEvent event) {
    }

    /**
     * Click event
     *
     * @param event event
     */
    @Override
    public void onClick(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.click(event, this);
        }
    }

    /**
     * Mouse movement event
     *
     * @param event event
     */
    @Override
    public void onMouseMove(final MouseEvent event) {
    }

    private IEventListener eventListener;

    /**
     * Set up the event listener
     *
     * @param eventListener eventListener
     */
    public void setEventListener(final IEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Event listener
     */
    public interface IEventListener {
        /**
         * Click event
         *
         * @param event event
         * @param data data
         */
        void click(MouseEvent event, FlagBean data);

        /**
         * Focus cancel event
         *
         * @param event event
         * @param data data
         */
        void blur(MouseEvent event, FlagBean data);

        /**
         * Focus acquisition event
         *
         * @param event event
         * @param data data
         */
        void focus(MouseEvent event, FlagBean data);

        /**
         * Mouse movement event
         *
         * @param event event
         * @param data data
         */
        void mouseMove(MouseEvent event, FlagBean data);

        /**
         * Delete event
         *
         * @param data data
         */
        void delete(FlagBean data);
    }

}

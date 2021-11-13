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

import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * AsyncEvent
 *
 * @since 2021/6/29 10:55
 */
public class AsyncEvent extends AbstractGraph {
    @DField(name = "id")
    private Integer id;
    @DField(name = "startTime")
    private Long startTime;
    @DField(name = "dur")
    private Long duration;
    @DField(name = "pid")
    private Integer pid;
    @DField(name = "name")
    private String name;
    @DField(name = "cookie")
    private Integer cookie;
    @DField(name = "depth")
    private Integer depth;
    private IEventListener eventListener;

    /**
     * Gets the value of depth .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getDepth() {
        return depth;
    }

    /**
     * Sets the depth .
     * <p>You can use getDepth() to get the value of depth</p>
     *
     * @param depth depth
     */
    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    /**
     * Gets the value of cookie .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getCookie() {
        return cookie;
    }

    /**
     * Sets the cookie .
     * <p>You can use getCookie() to get the value of cookie</p>
     *
     * @param cookie cookie
     */
    public void setCookie(Integer cookie) {
        this.cookie = cookie;
    }

    /**
     * Gets the value of id .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id</p>
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of java.lang.Long
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime</p>
     *
     * @param startTime startTime
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of java.lang.Long
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration</p>
     *
     * @param duration duration
     */
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * Gets the value of pid .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getPid() {
        return pid;
    }

    /**
     * Sets the pid .
     * <p>You can use getPid() to get the value of pid</p>
     *
     * @param pid pid
     */
    public void setPid(Integer pid) {
        this.pid = pid;
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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.setColor(ColorUtils.colorForTid(pid));
        Rectangle rectangle = new Rectangle(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
        graphics.fillRect(Utils.getX(rectangle), Utils.getY(rectangle), rectangle.width, rectangle.height);
        graphics.setColor(Color.WHITE);
        drawString(graphics, rectangle, name, Placement.CENTER_LINE);
    }

    @Override
    public void onFocus(MouseEvent event) {
    }

    @Override
    public void onBlur(MouseEvent event) {
    }

    @Override
    public void onClick(MouseEvent event) {
        if (this.eventListener != null) {
            eventListener.click(event, this);
        }
    }

    @Override
    public void onMouseMove(MouseEvent event) {
    }

    /**
     * Set up the event listener
     *
     * @param listener listener
     */
    public void setEventListener(final IEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * listener
     */
    public interface IEventListener {
        /**
         * Click event
         *
         * @param event event
         * @param data data
         */
        void click(MouseEvent event, AsyncEvent data);

        /**
         * Focus cancel event
         *
         * @param event event
         * @param data data
         */
        void blur(MouseEvent event, AsyncEvent data);

        /**
         * Focus acquisition event
         *
         * @param event event
         * @param data data
         */
        void focus(MouseEvent event, AsyncEvent data);

        /**
         * Mouse movement event
         *
         * @param event event
         * @param data data
         */
        void mouseMove(MouseEvent event, AsyncEvent data);
    }
}

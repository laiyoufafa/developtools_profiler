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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * Method entity class
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class FunctionBean extends AbstractGraph {
    private int tid;

    private String threadName;

    private int isMainThread;

    private int trackId;

    private long startTime;

    private long duration;

    private String funName;

    private int depth;

    private String category;

    private boolean isSelected; // Whether to be selected

    /**
     * Gets the value of tid .
     *
     * @return the value of int
     */
    public int getTid() {
        return tid;
    }

    /**
     * Sets the tid .
     * <p>You can use getTid() to get the value of tid</p>
     *
     * @param pTid pTid
     */
    public void setTid(final int pTid) {
        this.tid = pTid;
    }

    /**
     * Gets the value of threadName .
     *
     * @return the value of java.lang.String
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the threadName .
     * <p>You can use getThreadName() to get the value of threadName</p>
     *
     * @param name name
     */
    public void setThreadName(final String name) {
        this.threadName = name;
    }

    /**
     * Gets the value of isMainThread .
     *
     * @return the value of int
     */
    public int getIsMainThread() {
        return isMainThread;
    }

    /**
     * Sets the isMainThread .
     * <p>You can use getIsMainThread() to get the value of isMainThread</p>
     *
     * @param mainThread mainThread
     */
    public void setIsMainThread(final int mainThread) {
        this.isMainThread = mainThread;
    }

    /**
     * Gets the value of trackId .
     *
     * @return the value of int
     */
    public int getTrackId() {
        return trackId;
    }

    /**
     * Sets the trackId .
     * <p>You can use getTrackId() to get the value of trackId</p>
     *
     * @param id id
     */
    public void setTrackId(final int id) {
        this.trackId = id;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime</p>
     *
     * @param time time
     */
    public void setStartTime(final long time) {
        this.startTime = time;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of long
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration</p>
     *
     * @param dur dur
     */
    public void setDuration(final long dur) {
        this.duration = dur;
    }

    /**
     * Gets the value of funName .
     *
     * @return the value of java.lang.String
     */
    public String getFunName() {
        return funName;
    }

    /**
     * Sets the funName .
     * <p>You can use getFunName() to get the value of funName</p>
     *
     * @param name name
     */
    public void setFunName(final String name) {
        this.funName = name;
    }

    /**
     * Gets the value of depth .
     *
     * @return the value of int
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth .
     * <p>You can use getDepth() to get the value of depth</p>
     *
     * @param dep dep
     */
    public void setDepth(final int dep) {
        this.depth = dep;
    }

    /**
     * Gets the value of category .
     *
     * @return the value of java.lang.String
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category .
     * <p>You can use getCategory() to get the value of category</p>
     *
     * @param cate cate
     */
    public void setCategory(final String cate) {
        this.category = cate;
    }

    /**
     * Gets the value of isSelected .
     *
     * @return the value of boolean
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Sets the isSelected .
     * <p>You can use getSelected() to get the value of isSelected</p>
     *
     * @param selected selected
     */
    public void setSelected(final boolean selected) {
        this.isSelected = selected;
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (isSelected) {
            graphics.setColor(Color.black);
            graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
            graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
            graphics.fillRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
            graphics.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + 1, rect.getY() + 1, rect.getWidth() - 2, rect.getHeight() - 2);
            drawString(graphics, rectangle, funName, Placement.CENTER_LINE);
        } else {
            graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
            graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, funName, Placement.CENTER_LINE);
        }
    }

    /**
     * Focus acquisition event
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.focus(event, this);
        }
    }

    /**
     * Focus cancel event
     *
     * @param event event
     */
    @Override
    public void onBlur(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.blur(event, this);
        }
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
        if (edgeInspect(event)) {
            if (eventListener != null) {
                eventListener.mouseMove(event, this);
            }
        }
    }

    private IEventListener eventListener;

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
         * @param data  data
         */
        void click(MouseEvent event, FunctionBean data);

        /**
         * Focus cancel event
         *
         * @param event event
         * @param data  data
         */
        void blur(MouseEvent event, FunctionBean data);

        /**
         * Focus acquisition event
         *
         * @param event event
         * @param data  data
         */
        void focus(MouseEvent event, FunctionBean data);

        /**
         * Mouse movement event
         *
         * @param event event
         * @param data  data
         */
        void mouseMove(MouseEvent event, FunctionBean data);
    }

}

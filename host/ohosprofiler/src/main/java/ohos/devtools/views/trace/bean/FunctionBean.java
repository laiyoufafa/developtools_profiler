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

import com.intellij.ui.JBColor;
import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * Method entity class
 *
 * @since 2021/04/22 12:25
 */
public class FunctionBean extends AbstractGraph {
    @DField(name = "tid")
    private Integer tid;

    @DField(name = "threadName")
    private String threadName;

    @DField(name = "is_main_thread")
    private Integer isMainThread;

    @DField(name = "track_id")
    private Integer trackId;

    @DField(name = "startTs")
    private Long startTime;

    @DField(name = "dur")
    private Long duration;

    @DField(name = "funName")
    private String funName;

    @DField(name = "depth")
    private Integer depth;

    @DField(name = "argsetid")
    private Integer argSetId;

    private String category;

    private boolean isSelected; // Whether to be selected
    private IEventListener eventListener;

    /**
     * Gets the value of argSetId .
     *
     * @return the value of int
     */
    public Integer getArgSetId() {
        return argSetId;
    }

    /**
     * Sets the argSetId .You can use getArgSetId() to get the value of argSetId
     *
     * @param argSetId argSetId
     */
    public void setArgSetId(Integer argSetId) {
        this.argSetId = argSetId;
    }

    /**
     * Gets the value of tid .
     *
     * @return the value of int
     */
    public Integer getTid() {
        return tid;
    }

    /**
     * Sets the tid .You can use getTid() to get the value of tid
     *
     * @param pTid pTid
     */
    public void setTid(final Integer pTid) {
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
     * Sets the threadName .You can use getThreadName() to get the value of threadName
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
    public Integer getIsMainThread() {
        return isMainThread;
    }

    /**
     * Sets the isMainThread .You can use getIsMainThread() to get the value of isMainThread
     *
     * @param mainThread mainThread
     */
    public void setIsMainThread(final Integer mainThread) {
        this.isMainThread = mainThread;
    }

    /**
     * Gets the value of trackId .
     *
     * @return the value of int
     */
    public Integer getTrackId() {
        return trackId;
    }

    /**
     * Sets the trackId .You can use getTrackId() to get the value of trackId
     *
     * @param id id
     */
    public void setTrackId(final Integer id) {
        this.trackId = id;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of long
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .You can use getStartTime() to get the value of startTime
     *
     * @param time time
     */
    public void setStartTime(final Long time) {
        this.startTime = time;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of long
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration</p>
     *
     * @param dur dur
     */
    public void setDuration(final Long dur) {
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
    public Integer getDepth() {
        return depth;
    }

    /**
     * Sets the depth .
     * <p>You can use getDepth() to get the value of depth</p>
     *
     * @param dep dep
     */
    public void setDepth(final Integer dep) {
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
        if ((duration == null || duration == 0)) {
            if (isBinder()) {
                int pointX = Utils.getX(rect);
                int pointY = Utils.getY(rect);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                final int[] xArray;
                final int[] yArray;
                if (isSelected) {
                    xArray = new int[] {pointX, pointX + 8, pointX, pointX - 8};
                    yArray = new int[] {pointY - 2, pointY - 2 + 24, pointY - 2 + 16, pointY - 2 + 24};
                    graphics.setColor(JBColor.foreground().brighter());
                } else {
                    xArray = new int[] {pointX, pointX + 6, pointX, pointX - 6};
                    yArray = new int[] {pointY, pointY + 20, pointY + 14, pointY + 20};
                    graphics.setColor(JBColor.foreground().darker());
                }
                graphics.fillPolygon(xArray, yArray, xArray.length);
                rect.width = 8;
            }
        } else {
            if (isSelected) {
                graphics.setColor(Color.black);
                graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
                graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
                graphics.fillRect(Utils.getX(rect) + 1, Utils.getY(rect) + 1, rect.width - 2, rect.height - 2);
                graphics.setColor(Color.white);
                Rectangle rectangle = new Rectangle();
                rectangle.setRect(rect.getX() + 1, rect.getY() + 1, rect.getWidth() - 2, rect.getHeight() - 2);
                drawString(graphics, rectangle, funName, Placement.CENTER_LINE);
            } else {
                graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
                graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
                graphics.setColor(Color.white);
                drawString(graphics, rect, funName, Placement.CENTER_LINE);
            }
        }
    }

    /**
     * is binder
     *
     * @return the value of boolean
     */
    public boolean isBinder() {
        if (funName != null && (funName.toLowerCase(Locale.ENGLISH).startsWith("binder transaction") || funName
            .toLowerCase(Locale.ENGLISH).startsWith("binder async"))) {
            return true;
        } else {
            return false;
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
            AnalystPanel.clicked = true;
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
        void click(MouseEvent event, FunctionBean data);

        /**
         * Focus cancel event
         *
         * @param event event
         * @param data data
         */
        void blur(MouseEvent event, FunctionBean data);

        /**
         * Focus acquisition event
         *
         * @param event event
         * @param data data
         */
        void focus(MouseEvent event, FunctionBean data);

        /**
         * Mouse movement event
         *
         * @param event event
         * @param data data
         */
        void mouseMove(MouseEvent event, FunctionBean data);
    }
}

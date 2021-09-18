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
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * Thread data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 */
public class ThreadData extends AbstractGraph {
    private static Color runningColor = new Color(Final.RUNNING_COLOR);
    private static Color rColor = new Color(Final.R_COLOR);
    private static Color uninterruptibleSleepColor = new Color(Final.UNINTERRUPTIBLE_SLEEP_COLOR);
    private static Color sColor = new Color(Final.S_COLOR);
    private final int padding1 = 5;
    private final int padding2 = 10;
    private final float alpha2 = 0.02f;
    @DField(name = "upid")
    private int uPid;
    @DField(name = "utid")
    private int uTid;
    @DField(name = "pid")
    private int pid; // Process id
    @DField(name = "tid")
    private int tid; // Thread id
    @DField(name = "processName")
    private String processName;
    @DField(name = "threadName")
    private String threadName;
    @DField(name = "state")
    private String state;
    @DField(name = "startTime")
    private long startTime;
    @DField(name = "dur")
    private long duration;
    private boolean isSelected; // Whether to be selected
    @DField(name = "cpu")
    private int cpu;
    private IEventListener eventListener;

    /**
     * Gets the value of uPid .
     *
     * @return the value of uPid .
     */
    public int getuPid() {
        return uPid;
    }

    /**
     * Sets the uPid .
     * <p>You can use getuPid() to get the value of uPid.</p>
     *
     * @param param param.
     */
    public void setuPid(final int param) {
        this.uPid = param;
    }

    /**
     * Gets the value of uTid .
     *
     * @return the value of uTid .
     */
    public int getuTid() {
        return uTid;
    }

    /**
     * Sets the uTid .
     * <p>You can use getuTid() to get the value of uTid.</p>
     *
     * @param param .
     */
    public void setuTid(final int param) {
        this.uTid = param;
    }

    /**
     * Gets the value of pid .
     *
     * @return the value of pid .
     */
    public int getPid() {
        return pid;
    }

    /**
     * Sets the pid .
     * <p>You can use getPid() to get the value of pid.</p>
     *
     * @param param .
     */
    public void setPid(final int param) {
        this.pid = param;
    }

    /**
     * Gets the value of tid .
     *
     * @return the value of tid .
     */
    public int getTid() {
        return tid;
    }

    /**
     * Sets the tid .
     * <p>You can use getTid() to get the value of tid.</p>
     *
     * @param param .
     */
    public void setTid(final int param) {
        this.tid = param;
    }

    /**
     * Gets the value of processName .
     *
     * @return the value of processName .
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Sets the processName .
     * <p>You can use getProcessName() to get the value of processName.</p>
     *
     * @param param .
     */
    public void setProcessName(final String param) {
        this.processName = param;
    }

    /**
     * Gets the value of threadName .
     *
     * @return the value of threadName .
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the threadName .
     * <p>You can use getThreadName() to get the value of threadName.</p>
     *
     * @param param param
     */
    public void setThreadName(final String param) {
        this.threadName = param;
    }

    /**
     * Gets the value of state .
     *
     * @return the value of state .
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state .
     * <p>You can use getState() to get the value of state.</p>
     *
     * @param param param
     */
    public void setState(final String param) {
        this.state = param;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of startTime .
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime.</p>
     *
     * @param param param
     */
    public void setStartTime(final long param) {
        this.startTime = param;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of duration .
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration.</p>
     *
     * @param param param
     */
    public void setDuration(final long param) {
        this.duration = param;
    }

    /**
     * Gets the value of cpu .
     *
     * @return the value of cpu .
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * Sets the cpu .
     * <p>You can use getCpu() to get the value of cpu.</p>
     *
     * @param param param
     */
    public void setCpu(final int param) {
        this.cpu = param;
    }

    /**
     * Sets the isSelected .
     * <p>You can use getSelected() to get the value of isSelected.</p>
     *
     * @param param param
     */
    public void select(final boolean param) {
        isSelected = param;
    }

    /**
     * repaint.
     */
    public void repaint() {
        if (root != null) {
            root.repaint(Utils.getX(rect), Utils.getY(rect) - padding1, rect.width, rect.height + padding2);
        }
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (isSelected && !"S".equals(state)) {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect) - padding1, rect.width, rect.height + padding2);
            drawSelected(graphics);
        } else {
            drawUnSelected(graphics);
        }
    }

    private void drawSelected(final Graphics2D graphics) {
        if ("S".equals(state)) {
            graphics.setColor(sColor);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha2)); // transparency
            graphics.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // transparency
        } else if ("R".equals(state)) {
            graphics.setColor(rColor);
            graphics.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, Utils.getEndState(state), Placement.CENTER_LINE);
        } else if ("D".equals(state)) {
            graphics.setColor(uninterruptibleSleepColor);
            graphics.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, Utils.getEndState(state), Placement.CENTER_LINE);
        } else if ("Running".equals(state)) {
            graphics.setColor(runningColor);
            graphics.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            graphics.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + padding1, rect.getY(), rect.getWidth() - padding2, rect.getHeight());
            drawString(graphics, rectangle, Utils.getEndState(state), Placement.CENTER_LINE);
        } else {
            graphics.setColor(rColor);
            graphics.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            graphics.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + padding1, rect.getY(), rect.getWidth() - padding2, rect.getHeight());
            drawString(graphics, rectangle, Utils.getEndState(state), Placement.CENTER_LINE);
        }
    }

    private void drawUnSelected(final Graphics2D graphics) {
        if ("S".equals(state)) {
            graphics.setColor(sColor);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha2)); // transparency
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // transparency
        } else if ("R".equals(state)) {
            graphics.setColor(rColor);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, Utils.getEndState(state), Placement.CENTER_LINE);
        } else if ("D".equals(state)) {
            graphics.setColor(uninterruptibleSleepColor);
            graphics.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, Utils.getEndState(state), Placement.CENTER_LINE);
        } else if ("Running".equals(state)) {
            graphics.setColor(runningColor);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, Utils.getEndState(state), Placement.CENTER_LINE);
        } else {
            graphics.setColor(rColor);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(Color.white);
            drawString(graphics, rect, Utils.getEndState(state), Placement.CENTER_LINE);
        }
    }

    /**
     * Focus acquisition callback event
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
     * Focus cancel callback event
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
     * Click event callback
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
     * Mouse movement event callback
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
     * Set callback event listener
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
         * Mouse click event
         *
         * @param event event
         * @param data  data
         */
        void click(MouseEvent event, ThreadData data);

        /**
         * Mouse blur event
         *
         * @param event event
         * @param data  data
         */
        void blur(MouseEvent event, ThreadData data);

        /**
         * Mouse focus event
         *
         * @param event event
         * @param data  data
         */
        void focus(MouseEvent event, ThreadData data);

        /**
         * Mouse move event
         *
         * @param event event
         * @param data  data
         */
        void mouseMove(MouseEvent event, ThreadData data);
    }
}


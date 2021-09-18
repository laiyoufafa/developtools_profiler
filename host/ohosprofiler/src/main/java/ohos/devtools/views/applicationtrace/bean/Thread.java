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

package ohos.devtools.views.applicationtrace.bean;

import ohos.devtools.views.applicationtrace.AllData;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.TracePanel;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static ohos.devtools.views.trace.TracePanel.root;

/**
 * Thread
 *
 * @date: 2021/5/17 12:25
 */
public class Thread extends AbstractNode {
    private static Color runningColor = new Color(Final.RUNNING_COLOR);
    private static Color rColor = new Color(Final.R_COLOR);
    private static Color uninterruptibleSleepColor = new Color(Final.UNINTERRUPTIBLE_SLEEP_COLOR);
    private static Color exitColor = new Color(Final.EXIT_COLOR);
    private static Color sColor = new Color(Final.S_COLOR);
    private final Integer padding1 = 5;
    private final Integer padding2 = 10;
    private final float alpha2 = 0.02f;
    @DField(name = "upid") private Integer uPid;
    @DField(name = "utid") private Integer uTid;
    @DField(name = "pid") private Integer pid; // Process id
    @DField(name = "tid") private Integer tid; // Thread id
    @DField(name = "processName") private String processName;
    @DField(name = "threadName") private String threadName;
    @DField(name = "state") private String state;
    @DField(name = "startTime") private Long startTime;
    @DField(name = "dur") private Long duration;
    @DField(name = "cpu") private Integer cpu;
    private boolean isSelected; // Whether to be selected

    /**
     * Gets the value of uPid .
     *
     * @return the value of uPid .
     */
    public Integer getuPid() {
        return uPid;
    }

    /**
     * Sets the uPid .You can use getuPid() to get the value of uPid.
     *
     * @param param param.
     */
    public void setuPid(final Integer param) {
        this.uPid = param;
    }

    /**
     * Gets the value of uTid .
     *
     * @return the value of uTid .
     */
    public Integer getuTid() {
        return uTid;
    }

    /**
     * Sets the uTid . You can use getuTid() to get the value of uTid.</p>
     *
     * @param param param
     */
    public void setuTid(final Integer param) {
        this.uTid = param;
    }

    /**
     * Gets the value of pid .
     *
     * @return the value of pid .
     */
    public Integer getPid() {
        return pid;
    }

    /**
     * Sets the pid .
     * <p>You can use getPid() to get the value of pid.</p>
     *
     * @param param param
     */
    public void setPid(final Integer param) {
        this.pid = param;
    }

    /**
     * Gets the value of tid .
     *
     * @return the value of tid .
     */
    public Integer getTid() {
        return tid;
    }

    /**
     * Sets the tid .
     * <p>You can use getTid() to get the value of tid.</p>
     *
     * @param param param
     */
    public void setTid(final Integer param) {
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
     * @param param param
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
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime.</p>
     *
     * @param param param
     */
    public void setStartTime(final Long param) {
        this.startTime = param;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of duration .
     */
    public Long getDuration() {
        if (duration == -1) {
            duration = TracePanel.DURATION - startTime;
        }
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration.</p>
     *
     * @param param param
     */
    public void setDuration(final Long param) {
        this.duration = param;
    }

    /**
     * Gets the value of cpu .
     *
     * @return the value of cpu .
     */
    public Integer getCpu() {
        return cpu;
    }

    /**
     * Sets the cpu .
     * <p>You can use getCpu() to get the value of cpu.</p>
     *
     * @param param param
     */
    public void setCpu(final Integer param) {
        this.cpu = param;
    }

    /**
     * Sets the isSelected .You can use getSelected() to get the value of isSelected.
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
        root.repaint(Utils.getX(rect), Utils.getY(rect) - padding1, rect.width, rect.height + padding2);
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param paint graphics
     */
    @Override
    public void draw(Graphics2D paint) {
        if (isMouseIn) {
            Common.setAlpha(paint, 0.7F);
        } else {
            Common.setAlpha(paint, 1.0F);
        }
        paint.setFont(paint.getFont().deriveFont(9f));
        if (isSelected && !"S".equals(state)) {
            paint.setColor(Color.BLACK);
            paint.fillRect(Utils.getX(rect), Utils.getY(rect) - padding1, rect.width, rect.height + padding2);
            drawSelected(paint);
        } else {
            drawUnSelected(paint);
        }
        paint.setFont(paint.getFont().deriveFont(12f));
        Common.setAlpha(paint, 1.0F);
    }

    private void drawSelected(final Graphics2D paint) {
        if ("S".equals(state)) {
            paint.setColor(sColor);
            Common.setAlpha(paint, alpha2); // transparency
            paint.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            Common.setAlpha(paint, 1f); // transparency
        } else if ("R".equals(state) || "R+".equals(state)) {
            paint.setColor(rColor);
            paint.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            paint.setColor(Color.white);
            Common.drawStringCenter(paint, Utils.getEndState(state), rect);
        } else if ("D".equals(state)) {
            paint.setColor(uninterruptibleSleepColor);
            paint.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            paint.setColor(Color.white);
            Common.drawStringCenter(paint, Utils.getEndState(state), rect);
        } else if ("Running".equals(state)) {
            paint.setColor(runningColor);
            paint.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            paint.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + padding1, rect.getY(), rect.getWidth() - padding2, rect.getHeight());
            Common.drawStringCenter(paint, Utils.getEndState(state), rectangle);
        } else {
            paint.setColor(exitColor);
            paint.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            paint.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + padding1, rect.getY(), rect.getWidth() - padding2, rect.getHeight());
            Common.drawStringCenter(paint, Utils.getEndState(state), rectangle);
        }
    }

    private void drawUnSelected(final Graphics2D paint) {
        if ("S".equals(state)) {
            paint.setColor(sColor);
            Common.setAlpha(paint, alpha2); // transparency
            paint.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            Common.setAlpha(paint, 1f); // transparency
        } else if ("R".equals(state)) {
            paint.setColor(rColor);
            paint.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            paint.setColor(Color.white);
            Common.drawStringCenter(paint, Utils.getEndState(state), rect);
        } else if ("D".equals(state)) {
            paint.setColor(uninterruptibleSleepColor);
            paint.fillRect(Utils.getX(rect) + padding1, Utils.getY(rect), rect.width - padding2, rect.height);
            paint.setColor(Color.white);
            Common.drawStringCenter(paint, Utils.getEndState(state), rect);
        } else if ("Running".equals(state)) {
            paint.setColor(runningColor);
            paint.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            paint.setColor(Color.white);
            Common.drawStringCenter(paint, Utils.getEndState(state), rect);
        } else {
            paint.setColor(exitColor);
            paint.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            paint.setColor(Color.white);
            Common.drawStringCenter(paint, Utils.getEndState(state), rect);
        }
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(time,
            "Thread: " + (threadName == null || threadName.isEmpty() ? AllData.threadNames.get(tid) : threadName),
            Utils.getEndState(state), TimeUtils.getTimeWithUnit(duration));
    }

    @Override
    public void onClick(MouseEvent event) {
    }
}

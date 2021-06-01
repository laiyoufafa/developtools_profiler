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

import ohos.devtools.views.trace.fragment.CpuDataFragment;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Final;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * cpu data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class CpuData extends AbstractGraph {
    private int cpu;

    /**
     * get the number of cpu .
     *
     * @return int Returns the number of cpu
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * set the value of cpu .
     *
     * @param cpu Set the number of cpu
     */
    public void setCpu(final int cpu) {
        this.cpu = cpu;
    }

    private String name;

    /**
     * get the name .
     *
     * @return String Get the name
     */
    public String getName() {
        return name;
    }

    /**
     * set the name .
     *
     * @param name Set name
     */
    public void setName(final String name) {
        this.name = name;
    }

    private ArrayList<Integer> stats = new ArrayList<>();

    /**
     * get the stats .
     *
     * @return java.util.ArrayList
     */
    public java.util.ArrayList<Integer> getStats() {
        return stats;
    }

    /**
     * set the stats .
     *
     * @param stats stats
     */
    public void setStats(final java.util.ArrayList<Integer> stats) {
        this.stats = stats;
    }

    private String endState;

    /**
     * get the endState .
     *
     * @return String endState
     */
    public String getEndState() {
        return endState;
    }

    /**
     * get the endState .
     *
     * @param endState endState
     */
    public void setEndState(final String endState) {
        this.endState = endState;
    }

    private int priority;

    /**
     * get the priority .
     *
     * @return int priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * set the priority .
     *
     * @param priority priority
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    private int schedId;

    /**
     * get the schedId .
     *
     * @return int
     */
    public int getSchedId() {
        return schedId;
    }

    /**
     * set the schedId .
     *
     * @param schedId schedId
     */
    public void setSchedId(final int schedId) {
        this.schedId = schedId;
    }

    private long startTime;

    /**
     * get the startTime .
     *
     * @return long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * set the startTime .
     *
     * @param startTime startTime
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    private long duration;

    /**
     * get the duration .
     *
     * @return long
     */
    public long getDuration() {
        return duration;
    }

    /**
     * set the duration .
     *
     * @param duration duration
     */
    public void setDuration(final long duration) {
        this.duration = duration;
    }

    private String type;

    /**
     * get the type .
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * set the type .
     *
     * @param type type
     */
    public void setType(final String type) {
        this.type = type;
    }

    private int id;

    /**
     * get the id .
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * set the id .
     *
     * @param id id
     */
    public void setId(final int id) {
        this.id = id;
    }

    private int tid;

    /**
     * get the tid .
     *
     * @return int
     */
    public int getTid() {
        return tid;
    }

    /**
     * set the tid .
     *
     * @param tid tid
     */
    public void setTid(final int tid) {
        this.tid = tid;
    }

    private String processCmdLine;

    /**
     * get the processCmdLine .
     *
     * @return String
     */
    public String getProcessCmdLine() {
        return processCmdLine;
    }

    /**
     * set the processCmdLine .
     *
     * @param processCmdLine processCmdLine
     */
    public void setProcessCmdLine(final String processCmdLine) {
        this.processCmdLine = processCmdLine;
    }

    private String processName;

    /**
     * get the processName .
     *
     * @return String
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * set the processName .
     *
     * @param processName processName
     */
    public void setProcessName(final String processName) {
        this.processName = processName;
    }

    private int processId;

    /**
     * get the processId .
     *
     * @return int
     */
    public int getProcessId() {
        return processId;
    }

    /**
     * set the processId .
     *
     * @param processId processId
     */
    public void setProcessId(final int processId) {
        this.processId = processId;
    }

    /**
     * ui control extension field.
     */
    public CpuData() {
    }

    private double chartWidth;

    private double chartNum;

    private String str;

    private Rectangle2D bounds;

    private javax.swing.JComponent root;

    /**
     * Get rootcomponent
     *
     * @return javax.swing.JComponent
     */
    public javax.swing.JComponent getRoot() {
        return root;
    }

    /**
     * Set to get rootcomponent
     *
     * @param root root
     */
    public void setRoot(final javax.swing.JComponent root) {
        this.root = root;
    }

    private final int padding1 = 2;

    private final int padding2 = 4;

    private final float alpha90 = .9f;

    private final float alpha60 = .6f;

    private final int strOffsetY = 16;

    private final int processNameControl = -2;

    /**
     * Draw graphics based on attributes
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (isSelected) {
            drawSelect(graphics);
        } else {
            drawNoSelect(graphics);
        }
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        graphics.setColor(Color.white);
        int nameControl = processNameControl;
        if (processName != null && !processName.isEmpty()) {
            nameControl = processName.lastIndexOf("/");
        }
        Rectangle rectangle = new Rectangle();
        if (nameControl >= 0) {
            rectangle.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            drawString(graphics, rectangle, processName.substring(nameControl + 1) + "[" + processId + "]",
                Placement.CENTER);
        } else if (nameControl == -1) {
            rectangle.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            drawString(graphics, rectangle, processName + "[" + processId + "]", Placement.CENTER);
        } else {
            rectangle.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            drawString(graphics, rectangle, name + "[" + processId + "]", Placement.CENTER);
        }
        graphics.setFont(Final.SMALL_FONT);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha90));
        rectangle.setRect(rect.getX(), rect.getY() + strOffsetY, rect.getWidth(), rect.getHeight());
        drawString(graphics, rectangle, name + "[" + tid + "]", Placement.CENTER);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        graphics.setFont(Final.NORMAL_FONT);
    }

    private void drawSelect(final Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect((int) rect.getX(), (int) (rect.getY() - padding1), rect.width, rect.height + padding2);
        if (CpuDataFragment.focusCpuData != null) {
            if (CpuDataFragment.focusCpuData.processId != processId) {
                graphics.setColor(Color.GRAY);
            } else {
                if (CpuDataFragment.focusCpuData.hashCode() != this.hashCode()) {
                    graphics.setComposite(AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha60));
                }
                graphics.setColor(ColorUtils.colorForTid(processId > 0 ? processId : tid));
            }
        } else {
            graphics.setColor(ColorUtils.colorForTid(processId > 0 ? processId : tid));
        }
        graphics.fillRect((int) (rect.getX() + padding1), (int) rect.getY(), rect.width - padding2, rect.height);
    }

    private void drawNoSelect(final Graphics2D graphics) {
        if (CpuDataFragment.focusCpuData != null) {
            if (CpuDataFragment.focusCpuData.processId != processId) {
                graphics.setColor(Color.GRAY);
            } else {
                if (CpuDataFragment.focusCpuData.hashCode() != this.hashCode()) {
                    graphics.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha60));
                }
                graphics.setColor(ColorUtils.colorForTid(processId > 0 ? processId : tid));
            }
        } else {
            graphics.setColor(ColorUtils.colorForTid(processId > 0 ? processId : tid));
        }
        graphics.fillRect((int) rect.getX(), (int) rect.getY(), rect.width, rect.height);
    }

    private boolean isSelected; // Whether to be selected

    /**
     * Set selected state
     *
     * @param isSelected isSelected
     */
    public void select(final boolean isSelected) {
        this.isSelected = isSelected;
    }

    /**
     * Redraw the current page
     */
    public void repaint() {
        if (root != null) {
            root.repaint(rect.x, rect.y - padding1, rect.width, rect.height + padding2);
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
     * Focus loss event
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
     * Set callback event listener
     *
     * @param eventListener eventListener
     */
    public void setEventListener(final IEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Listener
     */
    public interface IEventListener {
        /**
         * Mouse click event
         *
         * @param event event
         * @param data  data
         */
        void click(MouseEvent event, CpuData data);

        /**
         * Mouse blur event
         *
         * @param event event
         * @param data  data
         */
        void blur(MouseEvent event, CpuData data);

        /**
         * Mouse focus event
         *
         * @param event event
         * @param data  data
         */
        void focus(MouseEvent event, CpuData data);

        /**
         * Mouse move event
         *
         * @param event event
         * @param data  data
         */
        void mouseMove(MouseEvent event, CpuData data);
    }
}

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

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * Process data
 *
 * @since 2021/04/22 12:25
 */
public class ProcessData extends AbstractGraph {
    @DField(name = "id")
    private int id;

    @DField(name = "utid")
    private int utid;

    @DField(name = "cpu")
    private int cpu;

    @DField(name = "startTime")
    private long startTime;

    @DField(name = "dur")
    private long duration;

    @DField(name = "state")
    private String state;

    @DField(name = "tid")
    private int tid;

    @DField(name = "pid")
    private int pid;

    @DField(name = "process")
    private String process;

    @DField(name = "thread")
    private String thread;

    /**
     * Gets the value of id .
     *
     * @return the value of int
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id</p>
     *
     * @param id id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Gets the value of utid .
     *
     * @return the value of int
     */
    public int getUtid() {
        return utid;
    }

    /**
     * Sets the utid .
     * <p>You can use getUtid() to get the value of utid</p>
     *
     * @param id id
     */
    public void setUtid(final int id) {
        this.utid = id;
    }

    /**
     * Gets the value of cpu .
     *
     * @return the value of int
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * Sets the cpu .
     * <p>You can use getCpu() to get the value of cpu</p>
     *
     * @param cpu cpu
     */
    public void setCpu(final int cpu) {
        this.cpu = cpu;
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
     * Gets the value of state .
     *
     * @return the value of java.lang.String
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state .
     * <p>You can use getState() to get the value of state</p>
     *
     * @param state state
     */
    public void setState(final String state) {
        this.state = state;
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
     * Gets the value of process .
     *
     * @return the value of process .
     */
    public String getProcess() {
        return process;
    }

    /**
     * Sets the process .
     * <p>You can use getProcess() to get the value of process.</p>
     *
     * @param param .
     */
    public void setProcess(final String param) {
        this.process = param;
    }

    /**
     * Gets the value of thread .
     *
     * @return the value of thread .
     */
    public String getThread() {
        return thread;
    }

    /**
     * Sets the thread .
     * <p>You can use getThread() to get the value of thread.</p>
     *
     * @param param .
     */
    public void setThread(final String param) {
        this.thread = param;
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
    }

    /**
     * Get focus event
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
    }

    /**
     * Focus cancel callback event
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
    }

    /**
     * Mouse movement event
     *
     * @param event event
     */
    @Override
    public void onMouseMove(final MouseEvent event) {
    }
}

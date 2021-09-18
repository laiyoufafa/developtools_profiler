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
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * cpu data
 *
 * @version 1.0
 * @date: 2021/5/14 15:52
 */
public class Cpu extends AbstractNode {
    private ArrayList<Integer> stats = new ArrayList<>();
    @DField(name = "cpu")
    private Integer cpu;
    @DField(name = "name")
    private String name;
    @DField(name = "end_state")
    private String endState;
    @DField(name = "schedId")
    private Integer schedId;
    @DField(name = "type")
    private String type;
    @DField(name = "tid")
    private Integer tid;
    @DField(name = "processCmdLine")
    private String processCmdLine;
    @DField(name = "processName")
    private String processName;
    @DField(name = "processId")
    private Integer processId;
    @DField(name = "id")
    private Integer id;
    @DField(name = "priority")
    private Integer priority;
    @DField(name = "startTime")
    private long startTime;
    @DField(name = "dur")
    private long duration;

    /**
     * get the number of cpu .
     *
     * @return Integer Returns the number of cpu
     */
    public Integer getCpu() {
        return cpu;
    }

    /**
     * set the value of cpu .
     *
     * @param cpu Set the number of cpu
     */
    public void setCpu(final Integer cpu) {
        this.cpu = cpu;
    }

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

    /**
     * get the stats .
     *
     * @return java.util.ArrayList
     */
    public ArrayList<Integer> getStats() {
        return stats;
    }

    /**
     * set the stats .
     *
     * @param stats stats
     */
    public void setStats(final ArrayList<Integer> stats) {
        this.stats = stats;
    }

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

    /**
     * get the priority .
     *
     * @return Integer priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * set the priority .
     *
     * @param priority priority
     */
    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    /**
     * get the schedId .
     *
     * @return Integer
     */
    public Integer getSchedId() {
        return schedId;
    }

    /**
     * set the schedId .
     *
     * @param schedId schedId
     */
    public void setSchedId(final Integer schedId) {
        this.schedId = schedId;
    }

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

    /**
     * get the id .
     *
     * @return Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * set the id .
     *
     * @param id id
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * get the tid .
     *
     * @return Integer
     */
    public Integer getTid() {
        return tid;
    }

    /**
     * set the tid .
     *
     * @param tid tid
     */
    public void setTid(final Integer tid) {
        this.tid = tid;
    }

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

    /**
     * get the processId .
     *
     * @return Integer
     */
    public Integer getProcessId() {
        return processId;
    }

    /**
     * set the processId .
     *
     * @param processId processId
     */
    public void setProcessId(final Integer processId) {
        this.processId = processId;
    }

    @Override
    public void draw(Graphics2D paint) {
        if (isMouseIn) {
            Common.setAlpha(paint, 0.7F);
        } else {
            Common.setAlpha(paint, 1.0F);
        }
        paint.setColor(ColorUtils.colorForTid(processId > 0 ? processId : tid));
        paint.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
        Common.setAlpha(paint, 1.0F);
        paint.setColor(JBColor.foreground().brighter());
        Common.drawStringCenter(paint, getName(), rect);
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(time, "Thread: " + name,
            "Process: " + ((processName == null || processName.isBlank()) ? name : processName),
            "Duration: " + TimeUtils.getTimeString(duration), "CPU: " + cpu);
    }
}

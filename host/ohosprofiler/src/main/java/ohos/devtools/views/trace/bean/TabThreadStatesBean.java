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

/**
 * TabThreadStatesBean
 *
 * @since 2021/5/13 13:06
 */
public class TabThreadStatesBean {
    /**
     * process name
     */
    @DField(name = "process")
    private String process;

    /**
     * process id
     */
    @DField(name = "pid")
    private Integer pid;

    /**
     * thread name
     */
    @DField(name = "thread")
    private String thread;

    /**
     * thread id
     */
    @DField(name = "tid")
    private Integer tid;

    /**
     * thread state
     */
    @DField(name = "state")
    private String state;

    /**
     * total wall duration
     */
    @DField(name = "wallDuration")
    private Long wallDuration;

    /**
     * avg wall duration
     */
    @DField(name = "avgDuration")
    private Double avgDuration;

    /**
     * count
     */
    @DField(name = "occurrences")
    private Integer occurrences;

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
     * @param param .
     */
    public void setPid(final Integer param) {
        this.pid = param;
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
     * @param param .
     */
    public void setTid(final Integer param) {
        this.tid = param;
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
     * @param param .
     */
    public void setState(final String param) {
        this.state = param;
    }

    /**
     * Gets the value of allDuration .
     *
     * @return the value of allDuration .
     */
    public Long getWallDuration() {
        return wallDuration;
    }

    /**
     * Sets the allDuration .
     * <p>You can use getAllDuration() to get the value of allDuration.</p>
     *
     * @param param .
     */
    public void setWallDuration(final long param) {
        this.wallDuration = param;
    }

    /**
     * Gets the value of avgDuration .
     *
     * @return the value of avgDuration .
     */
    public Double getAvgDuration() {
        return avgDuration;
    }

    /**
     * Sets the avgDuration .
     * <p>You can use getAvgDuration() to get the value of avgDuration.</p>
     *
     * @param param .
     */
    public void setAvgDuration(final double param) {
        this.avgDuration = param;
    }

    /**
     * Gets the value of occurrences .
     *
     * @return the value of occurrences .
     */
    public Integer getOccurrences() {
        return occurrences;
    }

    /**
     * Sets the occurrences .
     * <p>You can use getOccurrences() to get the value of occurrences.</p>
     *
     * @param param .
     */
    public void setOccurrences(final int param) {
        this.occurrences = param;
    }
}

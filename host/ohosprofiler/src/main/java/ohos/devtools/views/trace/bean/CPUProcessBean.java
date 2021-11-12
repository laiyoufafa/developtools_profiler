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

/**
 * cpu process data
 *
 * @since 2021/04/22 12:25
 */
public class CPUProcessBean {
    private long avgDuration;

    private long wallDuration;

    private String pid;

    private String occurrences;

    private String process;

    /**
     * Construction method with parameters
     *
     * @param mAvgDuration mAvgDuration
     * @param mWallDuration mWallDuration
     * @param mPid mPid
     * @param mOccurrences mOccurrences
     * @param mProcess mProcess
     */
    public CPUProcessBean(final long mAvgDuration, final long mWallDuration, final String mPid,
        final String mOccurrences, final String mProcess) {
        this.avgDuration = mAvgDuration;
        this.wallDuration = mWallDuration;
        this.pid = mPid;
        this.occurrences = mOccurrences;
        this.process = mProcess;
    }

    /**
     * Gets the value of avgDuration .
     *
     * @return the value of long
     */
    public long getAvgDuration() {
        return avgDuration;
    }

    /**
     * Sets the avgDuration .
     * <p>You can use getAvgDuration() to get the value of avgDuration</p>
     *
     * @param avgDuration avgDuration
     */
    public void setAvgDuration(final long avgDuration) {
        this.avgDuration = avgDuration;
    }

    /**
     * Gets the value of wallDuration .
     *
     * @return the value of long
     */
    public long getWallDuration() {
        return wallDuration;
    }

    /**
     * Sets the wallDuration .
     * <p>You can use getWallDuration() to get the value of wallDuration</p>
     *
     * @param wallDuration wallDuration
     */
    public void setWallDuration(final long wallDuration) {
        this.wallDuration = wallDuration;
    }

    /**
     * Gets the value of pid .
     *
     * @return the value of java.lang.String
     */
    public String getPid() {
        return pid;
    }

    /**
     * Sets the pid .
     * <p>You can use getPid() to get the value of pid</p>
     *
     * @param pid pid
     */
    public void setPid(final String pid) {
        this.pid = pid;
    }

    /**
     * Gets the value of occurrences .
     *
     * @return the value of java.lang.String
     */
    public String getOccurrences() {
        return occurrences;
    }

    /**
     * Sets the occurrences .
     * <p>You can use getOccurrences() to get the value of occurrences</p>
     *
     * @param occurrences occurrences
     */
    public void setOccurrences(final String occurrences) {
        this.occurrences = occurrences;
    }

    /**
     * Gets the value of process .
     *
     * @return the value of java.lang.String
     */
    public String getProcess() {
        return process;
    }

    /**
     * Sets the process .
     * <p>You can use getProcess() to get the value of process</p>
     *
     * @param process process
     */
    public void setProcess(final String process) {
        this.process = process;
    }
}

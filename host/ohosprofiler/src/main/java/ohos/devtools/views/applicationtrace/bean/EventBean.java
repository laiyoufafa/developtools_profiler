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

/**
 * EventBean
 *
 * @since 2021/5/27 12:01
 */
public class EventBean {
    private long startTime;
    private String name;
    private long wallDuration;
    private long selfTime;
    private long cpuDuration;
    private long cpuSelfTime;

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
     * @param param .
     */
    public void setStartTime(final long param) {
        this.startTime = param;
    }

    /**
     * Gets the value of name .
     *
     * @return the value of name .
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name .
     * <p>You can use getName() to get the value of name.</p>
     *
     * @param param .
     */
    public void setName(final String param) {
        this.name = param;
    }

    /**
     * Gets the value of wallDuration .
     *
     * @return the value of wallDuration .
     */
    public long getWallDuration() {
        return wallDuration;
    }

    /**
     * Sets the wallDuration .
     * <p>You can use getWallDuration() to get the value of wallDuration.</p>
     *
     * @param param .
     */
    public void setWallDuration(final long param) {
        this.wallDuration = param;
    }

    /**
     * Gets the value of selfTime .
     *
     * @return the value of selfTime .
     */
    public long getSelfTime() {
        return selfTime;
    }

    /**
     * Sets the selfTime .
     * <p>You can use getSelfTime() to get the value of selfTime.</p>
     *
     * @param param .
     */
    public void setSelfTime(final long param) {
        this.selfTime = param;
    }

    /**
     * Gets the value of cpuDuration .
     *
     * @return the value of cpuDuration .
     */
    public long getCpuDuration() {
        return cpuDuration;
    }

    /**
     * Sets the cpuDuration .
     * <p>You can use getCpuDuration() to get the value of cpuDuration.</p>
     *
     * @param param .
     */
    public void setCpuDuration(final long param) {
        this.cpuDuration = param;
    }

    /**
     * Gets the value of cpuSelfTime .
     *
     * @return the value of cpuSelfTime .
     */
    public long getCpuSelfTime() {
        return cpuSelfTime;
    }

    /**
     * Sets the cpuSelfTime .
     * <p>You can use getCpuSelfTime() to get the value of cpuSelfTime.</p>
     *
     * @param param .
     */
    public void setCpuSelfTime(final long param) {
        this.cpuSelfTime = param;
    }
}

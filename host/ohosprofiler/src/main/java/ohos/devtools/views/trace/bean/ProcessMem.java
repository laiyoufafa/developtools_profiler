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
 * Process memory directory
 *
 * @since 2021/04/22 12:25
 */
public class ProcessMem {
    @DField(name = "trackId")
    private int trackId;

    @DField(name = "processName")
    private String processName;

    @DField(name = "pid")
    private int pid;

    @DField(name = "upid")
    private int upid;

    @DField(name = "trackName")
    private String trackName;

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
     * @param track track
     */
    public void setTrackId(final int track) {
        this.trackId = track;
    }

    /**
     * Gets the value of processName .
     *
     * @return the value of java.lang.String
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Sets the processName .
     * <p>You can use getProcessName() to get the value of processName</p>
     *
     * @param name name
     */
    public void setProcessName(final String name) {
        this.processName = name;
    }

    /**
     * Gets the value of pid .
     *
     * @return the value of int
     */
    public int getPid() {
        return pid;
    }

    /**
     * Sets the pid .
     * <p>You can use getPid() to get the value of pid</p>
     *
     * @param id id
     */
    public void setPid(final int id) {
        this.pid = id;
    }

    /**
     * Gets the value of upid .
     *
     * @return the value of int
     */
    public int getUpid() {
        return upid;
    }

    /**
     * Sets the upid .
     * <p>You can use getUpid() to get the value of upid</p>
     *
     * @param id id
     */
    public void setUpid(final int id) {
        this.upid = id;
    }

    /**
     * Gets the value of trackName .
     *
     * @return the value of java.lang.String
     */
    public String getTrackName() {
        return trackName;
    }

    /**
     * Sets the trackName .
     * <p>You can use getTrackName() to get the value of trackName</p>
     *
     * @param name name
     */
    public void setTrackName(final String name) {
        this.trackName = name;
    }
}

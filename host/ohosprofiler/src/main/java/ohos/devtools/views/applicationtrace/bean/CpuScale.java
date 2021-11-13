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

import ohos.devtools.views.trace.DField;

/**
 * cpu scale
 *
 * @since 2021/5/27 12:22
 */
public class CpuScale {
    @DField(name = "data")
    private byte[] data;
    @DField(name = "id")
    private long id;
    @DField(name = "session")
    private long session;
    @DField(name = "sessionId")
    private long sessionId;
    @DField(name = "timeStamp")
    private long timeStamp;
    private double scale;
    private long startNs;
    private long endNs;

    /**
     * Gets the value of data .
     *
     * @return the value of data .
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the data .
     * <p>You can use getData() to get the value of data.</p>
     *
     * @param param .
     */
    public void setData(final byte[] param) {
        this.data = param;
    }

    /**
     * Gets the value of id .
     *
     * @return the value of id .
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id.</p>
     *
     * @param param .
     */
    public void setId(final long param) {
        this.id = param;
    }

    /**
     * Gets the value of session .
     *
     * @return the value of session .
     */
    public long getSession() {
        return session;
    }

    /**
     * Sets the session .
     * <p>You can use getSession() to get the value of session.</p>
     *
     * @param param .
     */
    public void setSession(final long param) {
        this.session = param;
    }

    /**
     * Gets the value of sessionId .
     *
     * @return the value of sessionId .
     */
    public long getSessionId() {
        return sessionId;
    }

    /**
     * Sets the sessionId .
     * <p>You can use getSessionId() to get the value of sessionId.</p>
     *
     * @param param .
     */
    public void setSessionId(final long param) {
        this.sessionId = param;
    }

    /**
     * Gets the value of timeStamp .
     *
     * @return the value of timeStamp .
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the timeStamp .
     * <p>You can use getTimeStamp() to get the value of timeStamp.</p>
     *
     * @param param .
     */
    public void setTimeStamp(final long param) {
        this.timeStamp = param;
    }

    /**
     * Gets the value of scale .
     *
     * @return the value of scale .
     */
    public double getScale() {
        return scale;
    }

    /**
     * Sets the scale .
     * <p>You can use getScale() to get the value of scale.</p>
     *
     * @param param .
     */
    public void setScale(final double param) {
        this.scale = param;
    }

    /**
     * Gets the value of startNs .
     *
     * @return the value of startNs .
     */
    public long getStartNs() {
        return startNs;
    }

    /**
     * Sets the startNs .
     * <p>You can use getStartNs() to get the value of startNs.</p>
     *
     * @param param .
     */
    public void setStartNs(final long param) {
        this.startNs = param;
    }

    /**
     * Gets the value of endNs .
     *
     * @return the value of endNs .
     */
    public long getEndNs() {
        return endNs;
    }

    /**
     * Sets the endNs .
     * <p>You can use getEndNs() to get the value of endNs.</p>
     *
     * @param param .
     */
    public void setEndNs(final long param) {
        this.endNs = param;
    }
}

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

package ohos.devtools.samplecode.datasources.databases.datatable.enties;

import java.util.Objects;

/**
 * Sample data
 *
 * @since 2021/11/24
 */
public class Sampledata<T> {
    private long localSessionId;
    private int sessionId;
    private long timeStamp;
    private int intData;
    private double doubleData;

    /**
     * Get session
     *
     * @return long
     */
    public long getSession() {
        return localSessionId;
    }

    /**
     * Get sessionId
     *
     * @return int
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Save session
     *
     * @param localSessionId Local sessionId
     */
    public void setSession(long localSessionId) {
        this.localSessionId = localSessionId;
    }

    /**
     * Set sessionId
     *
     * @param sessionId sessionId
     */
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * get local sessionId
     *
     * @return long
     */
    public long getLocalSessionId() {
        return localSessionId;
    }

    /**
     * Set local sessionId
     *
     * @param localSessionId Local sessionId
     */
    public void setLocalSessionId(long localSessionId) {
        this.localSessionId = localSessionId;
    }

    /**
     * Get time stamp
     *
     * @return long
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Set time stamp
     *
     * @param timeStamp Time stamp
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * get IntData
     *
     * @return double
     */
    public int getIntData() {
        return intData;
    }

    /**
     * set IntData
     *
     * @param intData intData
     */
    public void setIntData(int intData) {
        this.intData = intData;
    }

    /**
     * get DoubleData
     *
     * @return double
     */
    public double getDoubleData() {
        return doubleData;
    }

    /**
     * set DoubleData
     *
     * @param doubleData doubleData
     */
    public void setDoubleData(double doubleData) {
        this.doubleData = doubleData;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localSessionId, sessionId, timeStamp, intData, doubleData);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Sampledata{"
            + "localSessionId="
            + localSessionId
            + ", sessionId="
            + sessionId
            + ", timeStamp="
            + timeStamp
            + ", intData="
            + intData
            + ", doubleData="
            + doubleData
            + '}';
    }
}

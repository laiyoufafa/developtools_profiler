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

package ohos.devtools.datasources.databases.datatable.enties;

import java.io.Serializable;

/**
 * cpu data
 */
public class CpuData <T> implements Serializable {
    private static final long serialVersionUID = -8106428244173195592L;
    long localSessionId;
    int sessionId;
    long timeStamp;
    T data;

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
     * Get data
     *
     * @return T
     */
    public T getData() {
        return data;
    }

    /**
     * Set data
     *
     * @param data data
     */
    public void setData(T data) {
        this.data = data;
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
}

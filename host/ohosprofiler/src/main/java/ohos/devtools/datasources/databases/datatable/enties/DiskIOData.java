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
import java.math.BigDecimal;
import java.util.Objects;

/**
 * The DiskIOData class that receives the data
 *
 * @since 2021/10/22 15:43
 */
public class DiskIOData implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3804318833005799410L;

    private long localSessionId;
    private int sessionId;
    private long timeStamp;
    private BigDecimal readSectorsKb;
    private BigDecimal writeSectorsKb;

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
     * get ReadSectorsKb
     *
     * @return double
     */
    public BigDecimal getReadSectorsKb() {
        return readSectorsKb;
    }

    /**
     * set ReadSectorsKb
     *
     * @param readSectorsKb readSectorsKb
     */
    public void setReadSectorsKb(BigDecimal readSectorsKb) {
        this.readSectorsKb = readSectorsKb;
    }

    /**
     * get WriteSectorsKb
     *
     * @return int
     */
    public BigDecimal getWriteSectorsKb() {
        return writeSectorsKb;
    }

    /**
     * set WriteSectorsKb
     *
     * @param writeSectorsKb writeSectorsKb
     */
    public void setWriteSectorsKb(BigDecimal writeSectorsKb) {
        this.writeSectorsKb = writeSectorsKb;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localSessionId, sessionId, timeStamp, readSectorsKb, writeSectorsKb);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "DiskIOData{"
            + "localSessionId=" + localSessionId
            + ", sessionId=" + sessionId
            + ", timeStamp=" + timeStamp
            + ", readSectorsKb=" + readSectorsKb
            + ", writeSectorsKb=" + writeSectorsKb
            + '}';
    }
}

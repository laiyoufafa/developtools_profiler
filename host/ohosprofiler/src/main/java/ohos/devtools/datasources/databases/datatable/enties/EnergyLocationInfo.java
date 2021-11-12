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
import java.util.Objects;

/**
 * energy location inf0
 *
 * @since 2021/10/22 15:43
 */
public class EnergyLocationInfo implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8106428244173195592L;

    private long sessionId;
    private long eventId;
    private long timestamp;
    private String priority;
    private int energyUsage;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getEnergyUsage() {
        return energyUsage;
    }

    public void setEnergyUsage(int energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, eventId, timestamp, priority, energyUsage);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "EnergyLocationInfo{"
            + "sessionId=" + sessionId
            + ", eventId=" + eventId
            + ", timestamp=" + timestamp
            + ", priority='" + priority + '\''
            + ", energyUsage=" + energyUsage
            + '}';
    }
}

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

package ohos.devtools.services.ability;

import java.io.Serializable;
import java.util.Objects;

/**
 * AbilityEventInfo
 *
 * @since: 2021/8/20
 */
public class AbilityEventInfo implements Serializable {
    private static final long serialVersionUID = -5808117329320192029L;

    long sessionId;
    long timeStamp;
    EventType eventType;
    int keyType;
    boolean isDown;

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public int getKeyType() {
        return keyType;
    }

    public void setKeyType(int keyType) {
        this.keyType = keyType;
    }

    public boolean isDown() {
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AbilityEventInfo that = (AbilityEventInfo) object;
        if (sessionId == that.sessionId && timeStamp == that.timeStamp && keyType == that.keyType) {
            return isDown == that.isDown && eventType == that.eventType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, timeStamp, eventType, keyType, isDown);
    }

    @Override
    public String toString() {
        return "AbilityEventInfo{"
            + "sessionId="
            + sessionId
            + ", timeStamp="
            + timeStamp
            + ", keyType="
            + keyType
            + ", isDown="
            + isDown
            + '}';
    }
}

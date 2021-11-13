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
 * AbilitySliceData
 *
 * @since: 2021/9/20
 */
public class AbilityActivityInfo implements Serializable {
    private static final long serialVersionUID = -511400279730697290L;

    long sessionId;
    int lifeCycleId;
    long timeStamp;
    String abilityStateName;
    int abilityState;
    boolean isStartStatus;

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getLifeCycleId() {
        return lifeCycleId;
    }

    public void setLifeCycleId(int lifeCycleId) {
        this.lifeCycleId = lifeCycleId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAbilityStateName() {
        return abilityStateName;
    }

    public void setAbilityStateName(String abilityStateName) {
        this.abilityStateName = abilityStateName;
    }

    public int getAbilityState() {
        return abilityState;
    }

    public void setAbilityState(int abilityState) {
        this.abilityState = abilityState;
    }

    /**
     * isStartStatus
     *
     * @return boolean
     */
    public boolean isStartStatus() {
        return abilityState != 4 && abilityState != 6;
    }

    @Override
    public String toString() {
        return "AbilityActivityInfo{" + "sessionId=" + sessionId + ", lifeCycleId=" + lifeCycleId + ", timeStamp="
            + timeStamp + ", abilityStateName='" + abilityStateName + '\'' + ", abilityState=" + abilityState
            + ", isEndStatus=" + isStartStatus + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AbilityActivityInfo that = (AbilityActivityInfo) object;
        if (sessionId == that.sessionId && lifeCycleId == that.lifeCycleId && timeStamp == that.timeStamp
            && abilityState == that.abilityState) {
            return isStartStatus == that.isStartStatus && Objects.equals(abilityStateName, that.abilityStateName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, lifeCycleId, timeStamp, abilityStateName, abilityState, isStartStatus);
    }
}

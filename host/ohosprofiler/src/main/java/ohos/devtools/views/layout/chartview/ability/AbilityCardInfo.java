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

package ohos.devtools.views.layout.chartview.ability;

import java.util.Objects;

/**
 * AbilityCard Info
 *
 * @since 2021/10/25
 */
public class AbilityCardInfo {
    private long sessionId;
    private String applicationName;
    private long startTime;
    private long endTime;
    private AbilityCardStatus abilityCardStatus;

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public AbilityCardStatus getAbilityCardStatus() {
        return abilityCardStatus;
    }

    public void setAbilityCardStatus(AbilityCardStatus abilityCardStatus) {
        this.abilityCardStatus = abilityCardStatus;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, applicationName, startTime, endTime, abilityCardStatus);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "AbilityCardInfo{" + "sessionId=" + sessionId + ", applicationName='" + applicationName + '\''
            + ", startTime=" + startTime + ", endTime=" + endTime + ", abilityCardStatus=" + abilityCardStatus + '}';
    }
}

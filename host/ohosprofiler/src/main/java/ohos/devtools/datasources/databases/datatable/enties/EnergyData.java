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
 * energy data
 *
 * @since 2021/10/22 16:00
 */
public class EnergyData implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8106428244173195592L;

    private long sessionId;
    private long eventId;
    private String systemEvent;
    private String description;
    private String callStack;
    private String endCallStack;
    private Long startTimeStamp;
    private Long endTimeStamp;
    private Long triggerTimeNs;
    private String provider;
    private String priority;
    private Long minInterval;
    private Long FestInterval;
    private String workNetworkType;
    private String workCharging;
    private String workStorage;
    private String workDeepIdle;
    private String workBattery;
    private String workPersisted;
    private String workRepeatCounter;
    private String workRepeatCycleTime;
    private String workDelay;
    private String workResult;
    private String startType;
    private String endType;
    private String eventType;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getStartType() {
        return startType;
    }

    public void setStartType(String startType) {
        this.startType = startType;
    }

    public String getEndType() {
        return endType;
    }

    public void setEndType(String endType) {
        this.endType = endType;
    }

    public String getEndCallStack() {
        return endCallStack;
    }

    public void setEndCallStack(String endCallStack) {
        this.endCallStack = endCallStack;
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

    public String getSystemEvent() {
        return systemEvent;
    }

    public void setSystemEvent(String systemEvent) {
        this.systemEvent = systemEvent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCallStack() {
        return callStack;
    }

    public void setCallStack(String callStack) {
        this.callStack = callStack;
    }

    public Long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(Long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public Long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(Long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public Long getTriggerTimeNs() {
        return triggerTimeNs;
    }

    public void setTriggerTimeNs(Long triggerTimeNs) {
        this.triggerTimeNs = triggerTimeNs;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getMinInterval() {
        return minInterval;
    }

    public void setMinInterval(Long minInterval) {
        this.minInterval = minInterval;
    }

    public Long getFestInterval() {
        return FestInterval;
    }

    public void setFestInterval(Long festInterval) {
        FestInterval = festInterval;
    }

    public String getWorkNetworkType() {
        return workNetworkType;
    }

    public void setWorkNetworkType(String workNetworkType) {
        this.workNetworkType = workNetworkType;
    }

    public String getWorkCharging() {
        return workCharging;
    }

    public void setWorkCharging(String workCharging) {
        this.workCharging = workCharging;
    }

    public String getWorkStorage() {
        return workStorage;
    }

    public void setWorkStorage(String workStorage) {
        this.workStorage = workStorage;
    }

    public String getWorkDeepIdle() {
        return workDeepIdle;
    }

    public void setWorkDeepIdle(String workDeepIdle) {
        this.workDeepIdle = workDeepIdle;
    }

    public String getWorkBattery() {
        return workBattery;
    }

    public void setWorkBattery(String workBattery) {
        this.workBattery = workBattery;
    }

    public String getWorkPersisted() {
        return workPersisted;
    }

    public void setWorkPersisted(String workPersisted) {
        this.workPersisted = workPersisted;
    }

    public String getWorkRepeatCounter() {
        return workRepeatCounter;
    }

    public void setWorkRepeatCounter(String workRepeatCounter) {
        this.workRepeatCounter = workRepeatCounter;
    }

    public String getWorkRepeatCycleTime() {
        return workRepeatCycleTime;
    }

    public void setWorkRepeatCycleTime(String workRepeatCycleTime) {
        this.workRepeatCycleTime = workRepeatCycleTime;
    }

    public String getWorkDelay() {
        return workDelay;
    }

    public void setWorkDelay(String workDelay) {
        this.workDelay = workDelay;
    }

    public String getWorkResult() {
        return workResult;
    }

    public void setWorkResult(String workResult) {
        this.workResult = workResult;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(sessionId, eventId, systemEvent, description, callStack, endCallStack, startTimeStamp, endTimeStamp,
                triggerTimeNs, provider, priority, minInterval, FestInterval, workNetworkType, workCharging,
                workStorage, workDeepIdle, workBattery, workPersisted, workRepeatCounter, workRepeatCycleTime,
                workDelay, workResult, startType, endType, eventType);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "EnergyData{"
            + "sessionId=" + sessionId
            + ", endType='" + endType + '\''
            + '}';
    }
}

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

package ohos.devtools.views.trace.metrics.bean;

import ohos.devtools.views.trace.DField;

import java.util.Objects;

/**
 * Distribute Term entity
 */
public class DistributeTerm {
    @DField(name = "threadId")
    private String threadId;

    @DField(name = "threadName")
    private String threadName;

    @DField(name = "processId")
    private String processId;

    @DField(name = "process_name")
    private String processName;

    @DField(name = "funName")
    private String funName;

    @DField(name = "dur")
    private String dur;

    @DField(name = "ts")
    private String time;

    @DField(name = "chainId")
    private String chainId;

    @DField(name = "spanId")
    private int spanId;

    @DField(name = "parentSpanId")
    private int parentSpanId;

    @DField(name = "flag")
    private String flag;

    @DField(name = "trace_name")
    private String traceName;

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getFunName() {
        return funName;
    }

    public void setFunName(String funName) {
        this.funName = funName;
    }

    public String getDur() {
        return dur;
    }

    public void setDur(String dur) {
        this.dur = dur;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public int getSpanId() {
        return spanId;
    }

    public void setSpanId(int spanId) {
        this.spanId = spanId;
    }

    public int getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(int parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getTraceName() {
        return traceName;
    }

    public void setTraceName(String traceName) {
        this.traceName = traceName;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DistributeTerm that = (DistributeTerm) obj;
        return spanId == that.spanId && parentSpanId == that.parentSpanId && Objects.equals(threadId, that.threadId)
            && Objects.equals(threadName, that.threadName) && Objects.equals(processId, that.processId) && Objects
            .equals(processName, that.processName) && Objects.equals(funName, that.funName) && Objects
            .equals(dur, that.dur) && Objects.equals(time, that.time) && Objects.equals(chainId, that.chainId)
            && Objects.equals(flag, that.flag) && Objects.equals(traceName, that.traceName);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(threadId, threadName, processId, processName, funName, dur, time, chainId, spanId, parentSpanId, flag,
                traceName);
    }

    @Override
    public String toString() {
        return "DistributeTerm{" + "threadId='" + threadId + '\'' + ", threadName='" + threadName + '\''
            + ", processId='" + processId + '\'' + ", processName='" + processName + '\'' + ", funName='" + funName
            + '\'' + ", dur=" + dur + ", time=" + time + ", chainId='" + chainId + '\'' + ", spanId=" + spanId
            + ", parentSpanId=" + parentSpanId + ", flag='" + flag + '\'' + ", traceName='" + traceName + '\'' + '}';
    }
}

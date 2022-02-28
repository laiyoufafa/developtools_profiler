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
 * Sys Calls Top
 *
 * @since 2021/5/19 16:39
 */
public class SysCallsTop {
    @DField(name = "tid")
    private int tid;
    @DField(name = "pid")
    private int pid;
    @DField(name = "minDur")
    private int minDur;
    @DField(name = "avgDur")
    private int avgDur;
    @DField(name = "maxDur")
    private int maxDur;
    @DField(name = "funName")
    private String funName;
    @DField(name = "process_name")
    private String processName;
    @DField(name = "thread_name")
    private String threadName;

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getMinDur() {
        return minDur;
    }

    public void setMinDur(int minDur) {
        this.minDur = minDur;
    }

    public int getAvgDur() {
        return avgDur;
    }

    public void setAvgDur(int avgDur) {
        this.avgDur = avgDur;
    }

    public int getMaxDur() {
        return maxDur;
    }

    public void setMaxDur(int maxDur) {
        this.maxDur = maxDur;
    }

    public String getFunName() {
        return funName;
    }

    public void setFunName(String funName) {
        this.funName = funName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SysCallsTop that = (SysCallsTop) obj;
        return tid == that.tid && pid == that.pid && minDur == that.minDur && avgDur == that.avgDur
            && maxDur == that.maxDur && Objects.equals(funName, that.funName) && Objects
            .equals(processName, that.processName) && Objects.equals(threadName, that.threadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, pid, minDur, avgDur, maxDur, funName, processName, threadName);
    }

    @Override
    public String toString() {
        return "SysCallsTop{" + "tid=" + tid + ", pid=" + pid + ", minDur=" + minDur + ", avgDur=" + avgDur
            + ", maxDur=" + maxDur + ", funName='" + funName + '\'' + ", processName='" + processName + '\''
            + ", threadName='" + threadName + '\'' + '}';
    }
}

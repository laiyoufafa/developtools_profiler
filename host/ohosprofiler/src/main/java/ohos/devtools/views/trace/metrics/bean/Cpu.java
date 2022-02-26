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

/**
 * cpu data
 *
 * @since 2021/5/19 16:39
 */
public class Cpu {
    @DField(name = "tid")
    private Integer tid;
    @DField(name = "pid")
    private Integer pid;
    @DField(name = "cpu")
    private String cpu;
    @DField(name = "duration")
    private String duration;
    @DField(name = "min_freq")
    private String minFreq;
    @DField(name = "max_freq")
    private String maxFreq;
    @DField(name = "avg_frequency")
    private String avgFrequency;
    @DField(name = "process_name")
    private String processName;
    @DField(name = "thread_name")
    private String threadName;

    public Integer getTid() {
        return tid;
    }

    public void setTid(Integer tid) {
        this.tid = tid;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMinFreq() {
        return minFreq;
    }

    public void setMinFreq(String minFreq) {
        this.minFreq = minFreq;
    }

    public String getMaxFreq() {
        return maxFreq;
    }

    public void setMaxFreq(String maxFreq) {
        this.maxFreq = maxFreq;
    }

    public String getAvgFrequency() {
        return avgFrequency;
    }

    public void setAvgFrequency(String avgFrequency) {
        this.avgFrequency = avgFrequency;
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
    public String toString() {
        return "Cpu{" + "tid=" + tid + ", pid=" + pid + ", cpu='" + cpu + '\'' + ", duration='" + duration + '\''
            + ", minFreq='" + minFreq + '\'' + ", maxFreq='" + maxFreq + '\'' + ", avgFrequency='" + avgFrequency + '\''
            + ", processName='" + processName + '\'' + ", threadName='" + threadName + '\'' + '}';
    }
}

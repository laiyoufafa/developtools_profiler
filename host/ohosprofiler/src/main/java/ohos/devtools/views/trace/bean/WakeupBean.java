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

package ohos.devtools.views.trace.bean;

/**
 * Wake up data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class WakeupBean {
    private long wakeupTime;

    private int wakeupCpu;

    private String wakeupProcess;

    private String wakeupPid;

    private String wakeupThread;

    private String wakeupTid;

    private long schedulingLatency;

    private String schedulingDesc;

    /**
     * get wakeup time.
     *
     * @return wakeup time
     */
    public long getWakeupTime() {
        return wakeupTime;
    }

    /**
     * set wakeup time.
     *
     * @param wt wt
     */
    public void setWakeupTime(final long wt) {
        this.wakeupTime = wt;
    }

    /**
     * get wakeup cpu.
     *
     * @return wakeup cpu
     */
    public int getWakeupCpu() {
        return wakeupCpu;
    }

    /**
     * set wakeup cpu.
     *
     * @param cpu cpu
     */
    public void setWakeupCpu(final int cpu) {
        this.wakeupCpu = cpu;
    }

    /**
     * get wakeup process.
     *
     * @return wakeup process
     */
    public String getWakeupProcess() {
        return wakeupProcess;
    }

    /**
     * set wakeup process.
     *
     * @param process process
     */
    public void setWakeupProcess(final String process) {
        this.wakeupProcess = process;
    }

    /**
     * get wakeup pId.
     *
     * @return wakeup pId
     */
    public String getWakeupPid() {
        return wakeupPid;
    }

    /**
     * set wakeup pId.
     *
     * @param pid pid
     */
    public void setWakeupPid(final String pid) {
        this.wakeupPid = pid;
    }

    /**
     * get wakeup thread.
     *
     * @return wakeup thread
     */
    public String getWakeupThread() {
        return wakeupThread;
    }

    /**
     * set wakeup thread.
     *
     * @param thread thread
     */
    public void setWakeupThread(final String thread) {
        this.wakeupThread = thread;
    }

    /**
     * get wakeup tId.
     *
     * @return wakeup thread id
     */
    public String getWakeupTid() {
        return wakeupTid;
    }

    /**
     * set wakeup tId.
     *
     * @param tid tid
     */
    public void setWakeupTid(final String tid) {
        this.wakeupTid = tid;
    }

    /**
     * get scheduling Latency.
     *
     * @return scheduling Latency
     */
    public long getSchedulingLatency() {
        return schedulingLatency;
    }

    /**
     * set scheduling Latency.
     *
     * @param latency latency
     */
    public void setSchedulingLatency(final long latency) {
        this.schedulingLatency = latency;
    }

    /**
     * get scheduling Desc.
     *
     * @return scheduling Desc
     */
    public String getSchedulingDesc() {
        return schedulingDesc;
    }

    /**
     * set scheduling Desc.
     *
     * @param desc desc
     */
    public void setSchedulingDesc(final String desc) {
        this.schedulingDesc = desc;
    }
}

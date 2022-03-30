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

package ohos.devtools.datasources.utils.monitorconfig.entity;

import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.services.hiperf.HiPerfCommand;
import ohos.devtools.services.hiperf.PerfCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Perf config Struct.
 *
 * @since 2021/11/02
 */
public class PerfConfig<T> {
    private static final int DEFAULT_FREQUENCY = 1000;
    private static final int DEFAULT_CPU_PERCENT = 100;
    private static final int DEFAULT_PERIOD = 1;
    private static final int DEFAULT_MEMORY = 256;
    private static final String SOFT_EVENT_KEY = "software";
    private static final String HARD_EVENT_KEY = "hardware";

    /**
     * record assign cpu num such as 0,1,2;
     */
    private List<T> cpuList = new ArrayList<>();

    /**
     * Set the max percent of cpu time used for recording. percent is in range [1-100]
     */
    private int cpuPercent = DEFAULT_CPU_PERCENT;

    /**
     * Set event sampling frequency. default is 4000 samples every second.
     */
    private int frequency = DEFAULT_FREQUENCY;

    /**
     * event type Default is cpu cycles
     */
    private List<T> eventList = new ArrayList<>();

    /**
     * Set event sampling period for trace point events. recording one sample when <num> events happened.
     */
    private int period = DEFAULT_PERIOD;

    /**
     * off cpu
     */
    private boolean isOffCpu = true;

    /**
     * Don't trace child processes.
     */
    private boolean isInherit = false;

    /**
     * Setup and enable call stack (stack chain/backtrace) recording,related with enum CallStack,
     */
    private int callStack = 0;

    /**
     * taken branch stack sampling related with enum Branch
     */
    private int branch = 0;

    /**
     * used to receiving record data from kernel, must be a power of two, rang[2,1024], default is 256.
     */
    private int mmapPages = DEFAULT_MEMORY;

    /**
     * Set the clock id to use for the various time fields in the perf_event_type records.related enum Clock
     */
    private int clockId = 0;

    /**
     * get Cpu list
     *
     * @return cpu list
     */
    public List<T> getCpuList() {
        return cpuList;
    }

    /**
     * set Cpu List
     *
     * @param cpuList Cpu list
     */
    public void setCpuList(List<T> cpuList) {
        this.cpuList = cpuList;
    }

    /**
     * get set Cpu Percent
     *
     * @return cpu percent
     */
    public int getCpuPercent() {
        return cpuPercent;
    }

    /**
     * set cpu percent
     *
     * @param cpuPercent percent
     */
    public void setCpuPercent(int cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    /**
     * get frequency
     *
     * @return frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * set frequency
     *
     * @param frequency frequency
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * get Event list
     *
     * @return event list
     */
    public List<T> getEventList() {
        return eventList;
    }

    /**
     * set event list
     *
     * @param eventList event list
     */
    public void setEventList(List<T> eventList) {
        this.eventList = eventList;
    }

    /**
     * get sample period
     *
     * @return period
     */
    public int getPeriod() {
        return period;
    }

    /**
     * set sample period
     *
     * @param period period
     */
    public void setPeriod(int period) {
        this.period = period;
    }

    /**
     * adjust is off cpu
     *
     * @return is off cpu
     */
    public boolean isOffCpu() {
        return isOffCpu;
    }

    /**
     * set off cpu
     *
     * @param offCpu off cpu
     */
    public void setOffCpu(boolean offCpu) {
        this.isOffCpu = offCpu;
    }

    /**
     * get Call stack type index of enum @CallStack
     *
     * @return enum CallStack index
     */
    public int getCallStack() {
        return callStack;
    }

    /**
     * set enum CallStack index
     *
     * @param callStack index
     */
    public void setCallStack(int callStack) {
        this.callStack = callStack;
    }

    /**
     * get enum Branch index
     *
     * @return index of enum Branch
     */
    public int getBranch() {
        return branch;
    }

    /**
     * set enmu Branch index
     *
     * @param branch index
     */
    public void setBranch(int branch) {
        this.branch = branch;
    }

    /**
     * get memory map pages
     *
     * @return memory
     */
    public int getMmapPages() {
        return mmapPages;
    }

    /**
     * set memory map pages
     *
     * @param mmapPages memory
     */
    public void setMmapPages(int mmapPages) {
        this.mmapPages = mmapPages;
    }

    /**
     * is No inherit
     *
     * @return is no Inherit
     */
    public boolean isInherit() {
        return isInherit;
    }

    /**
     * set no Inherit
     *
     * @param inherit noInherit
     */
    public void setInherit(boolean inherit) {
        this.isInherit = inherit;
    }

    /**
     * get Clock id
     *
     * @return index of enum Clock
     */
    public int getClockId() {
        return clockId;
    }

    /**
     * set Clock id
     *
     * @param clockId index of clock
     */
    public void setClockId(int clockId) {
        this.clockId = clockId;
    }

    /**
     * use to -j
     *
     * @since 2021/5/19 16:39
     */
    public enum Branch {
        /**
         * none
         */
        NONE("none"),
        /**
         * any type of branch
         */
        ANY("any"),
        /**
         * any function call or system call
         */
        ANY_CALL("any_call"),
        /**
         * any function return or system call return
         */
        ANY_RET("any_ret"),
        /**
         * any indirect branch
         */
        IND_CALL("ind_call"),
        /**
         * direct calls, including far (to/from kernel) calls
         */
        CALL("call"),
        /**
         * only when the branch target is at the user level
         */
        USER("u"),
        /**
         * only when the branch target is in the kernel
         */
        KERNEL("k");
        private String name;

        Branch(String name) {
            this.name = name;
        }

        /**
         * get Name
         *
         * @return name
         */
        public String getName() {
            return name;
        }
    }

    /**
     * use to --call-stack
     *
     * @since 2021/5/19 16:39
     */
    public enum CallStack {
        /**
         * none
         */
        NONE(0, "none"),
        /**
         * frame pointer
         */
        FP(1, "fp"),
        /**
         * DWARF's CFI - Call Frame Information
         * 'dwarf,size' set sample stack size,
         * size should be in 8~65528 and 8 byte aligned.
         */
        DWARF(2, "dwarf");
        private String name;
        private int index;

        CallStack(int index, String name) {
            this.name = name;
        }

        /**
         * get Name
         *
         * @return name
         */
        public String getName() {
            return name;
        }

        /**
         * get index
         *
         * @return index
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * use --clock_id
     *
     * @since 2021/5/19 16:39
     */
    public enum Clock {
        MONOTONIC("monotonic"), MONOTONIC_RAW("monitonic_row"), REALTIME("realtime"), BOOTTIME("boottime"),
        PERF("perf");
        private String name;

        Clock(String name) {
            this.name = name;
        }

        /**
         * getName
         *
         * @return name
         */
        public String getName() {
            return name;
        }
    }

    /**
     * get device cpu count
     *
     * @param isLeakOhos isLeakOhos
     * @param deviceId device id
     * @return cpu count of device
     */
    public int getCpuCount(boolean isLeakOhos, String deviceId) {
        ArrayList<String> cmd = HdcWrapper.getInstance().generateDeviceCmdHead(isLeakOhos, deviceId);
        cmd.add("ls");
        cmd.add("/sys/devices/system/cpu/");
        cmd.add("|");
        cmd.add("grep");
        cmd.add("cpu");
        cmd.add("|");
        cmd.add("wc");
        cmd.add("-l");
        try {
            String count = HdcWrapper.getInstance().execCmdBy(cmd).strip();
            return Integer.parseInt(count);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * get device support Events
     *
     * @param isLeakOhos isLeakOhos
     * @param deviceId deviceId
     * @return Map type is key , events is value
     */
    public Map<String, List<String>> getSupportEvents(boolean isLeakOhos, String deviceId) {
        PerfCommand perfCommand = new HiPerfCommand(isLeakOhos, deviceId);
        return perfCommand.getSupportEvents();
    }

    /**
     * Filter Software and hardware Events
     *
     * @param isLeakOhos isLeakOhos
     * @param deviceId deviceId
     * @return sw, hw event list
     */
    public List<String> getSoftHardWareEvents(boolean isLeakOhos, String deviceId) {
        List<String> shEvents = new ArrayList<>();
        Map<String, List<String>> events = getSupportEvents(isLeakOhos, deviceId);
        if (events.containsKey(HARD_EVENT_KEY)) {
            shEvents.addAll(events.get(HARD_EVENT_KEY));
        }
        if (events.containsKey(SOFT_EVENT_KEY)) {
            shEvents.addAll(events.get(SOFT_EVENT_KEY));
        }
        return shEvents;
    }
}

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

package ohos.devtools.views.common;

/**
 * SystemTunningProbesCheckbox
 *
 * @version 1.0
 * @date 2021/04/14 20:13
 **/
public final class SystemTunningProbesCheckbox {
    private static final String TRACE_EVENT = "           ftrace_events:";

    /**
     * SYSTEM_TUNNING_PROBES_NO_SELECT_HEAD
     */
    public static final String SYSTEM_TUNNING_PROBES_NO_SELECT_HEAD =
        "hdc shell hiprofiler_cmd \\\n" + "   -c -  \\\n" + "<<EOF\n";

    /**
     * SYSTEM_TUNNING_PROBES_NO_SELECT_END
     */
    public static final String SYSTEM_TUNNING_PROBES_NO_SELECT_END = "\n" + "EOF";

    /**
     * SYSTEM_TUNNING_PROBES_DATA_SOURCE
     */
    public static final String SYSTEM_TUNNING_PROBES_DATA_SOURCE =
        "data_sources: {\n" + "   config {\n" + "       name: \"linux.process_stats\"\n" + "       target_buffer: 1\n"
            + "       process_stats_config {\n" + "           scan_all_processes_on_start: true\n" + "       }\n"
            + "   }\n" + "}";

    /**
     * SCHED_SWITCH
     */
    public static final String SCHED_SWITCH = "\"sched/sched_switch\"\n";

    /**
     * SCHED_WAKEUP
     */
    public static final String SCHED_WAKEUP = "\"sched/sched_wakeup\"\n";

    /**
     * SCHED_WAKEUP_NEW
     */
    public static final String SCHED_WAKEUP_NEW = "\"sched/sched_wakeup_new\"\n";

    /**
     * SCHED_WAKING
     */
    public static final String SCHED_WAKING = "\"sched/sched_waking\"\n";

    /**
     * SCHED_PROCESS_EXIT
     */
    public static final String SCHED_PROCESS_EXIT = "\"sched/sched_process_exit\"\n";

    /**
     * SCHED_PROCESS_FREE
     */
    public static final String SCHED_PROCESS_FREE = "\"sched/sched_process_free\"\n";

    /**
     * TASH_NEWTASK
     */
    public static final String TASH_NEWTASK = "\"task/task_newtask\"\n";

    /**
     * TASK_RENAME
     */
    public static final String TASK_RENAME = "\"task/task_rename\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SCHEDULING_DETAIL_FTRACE
     */
    public static final String SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SCHEDULING_DETAIL_FTRACE =
        TRACE_EVENT + SCHED_SWITCH + TRACE_EVENT + SCHED_WAKEUP + TRACE_EVENT + SCHED_WAKEUP_NEW + TRACE_EVENT
            + SCHED_WAKING + TRACE_EVENT + SCHED_PROCESS_EXIT + TRACE_EVENT + SCHED_PROCESS_FREE + TRACE_EVENT
            + TASH_NEWTASK + TRACE_EVENT + TASK_RENAME;

    /**
     * CPU_FREQUENCY
     */
    public static final String CPU_FREQUENCY = "\"power/cpu_frequency\"\n";

    /**
     * CPU_IDLE
     */
    public static final String CPU_IDLE = "\"power/cpu_idle\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_CPU_FREQUENCY_FTRACE
     */
    public static final String SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_CPU_FREQUENCY_FTRACE =
        TRACE_EVENT + CPU_FREQUENCY + TRACE_EVENT + CPU_IDLE;

    /**
     * SYS_ENTER
     */
    public static final String SYS_ENTER = "\"raw_syscalls/sys_enter\"\n";

    /**
     * SYS_EXIT
     */
    public static final String SYS_EXIT = "\"raw_syscalls/sys_exit\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SYSCALLS_FTRACE
     */
    public static final String SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SYSCALLS_FTRACE =
        TRACE_EVENT + SYS_ENTER + TRACE_EVENT + SYS_EXIT;

    /**
     * REGULATOR_SET_VOLTAGE
     */
    public static final String REGULATOR_SET_VOLTAGE = "\"regulator/regulator_set_voltage\"\n";

    /**
     * REQULATOR_SET_VOLTAGE_COMPLETE
     */
    public static final String REQULATOR_SET_VOLTAGE_COMPLETE = "\"regulator/regulator_set_voltage_complete\"\n";

    /**
     * CLOCK_ENABLE
     */
    public static final String CLOCK_ENABLE = "\"power/clock_enable\"\n";

    /**
     * CLOCK_DISABLE
     */
    public static final String CLOCK_DISABLE = "\"power/clock_disable\"\n";

    /**
     * CLOCK_SET_RATE
     */
    public static final String CLOCK_SET_RATE = "\"power/clock_set_rate\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_BOARD_VOLTAGES_FTRACE
     */
    public static final String SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_BOARD_VOLTAGES_FTRACE =
        TRACE_EVENT + REGULATOR_SET_VOLTAGE + TRACE_EVENT + REQULATOR_SET_VOLTAGE_COMPLETE + TRACE_EVENT + CLOCK_ENABLE
            + TRACE_EVENT + CLOCK_DISABLE + TRACE_EVENT + CLOCK_SET_RATE;

    /**
     * MM_EVENT_RECORD
     */
    public static final String MM_EVENT_RECORD = "\"mm_event/mm_event_record\"\n";

    /**
     * RSS_STAT
     */
    public static final String RSS_STAT = "\"kmem/rss_stat\"\n";

    /**
     * ION_STAT
     */
    public static final String ION_STAT = "\"ion/ion_stat\"\n";

    /**
     * ION_HEAP_GROW
     */
    public static final String ION_HEAP_GROW = "\"kmem/ion_heap_grow\"\n";

    /**
     * ION_HEAP_SHRINK
     */
    public static final String ION_HEAP_SHRINK = "\"kmem/ion_heap_shrink\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_HIGH_FREQUENCY_FTRACE
     */
    public static final String SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_HIGH_FREQUENCY_FTRACE =
        TRACE_EVENT + MM_EVENT_RECORD + TRACE_EVENT + RSS_STAT + TRACE_EVENT + ION_STAT + TRACE_EVENT + ION_HEAP_GROW
            + TRACE_EVENT + ION_HEAP_SHRINK;

    /**
     * LOWMEMORY_KILL
     */
    public static final String LOWMEMORY_KILL = "\"lowmemorykiller/lowmemory_kill\"\n";

    /**
     * OOM_SCORE_ADJ_UPDATE
     */
    public static final String OOM_SCORE_ADJ_UPDATE = "\"oom/oom_score_adj_update\"\n";

    /**
     * PRINT
     */
    public static final String PRINT = "\"ftrace/print\"\n";

    /**
     * LMKD
     */
    public static final String LMKD = "\"lmkd\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_LOW_MEMORY_FTRACE
     */
    public static final String SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_LOW_MEMORY_FTRACE =
        TRACE_EVENT + LOWMEMORY_KILL + TRACE_EVENT + OOM_SCORE_ADJ_UPDATE + TRACE_EVENT + PRINT + TRACE_EVENT + LMKD;

    /**
     * SUSPEND_RESUME
     */
    public static final String SUSPEND_RESUME = "\"power/suspend_resume\"\n";

    /**
     * SYSTEM_TUNNING_PROBES_CONTAIN_SHARE_FTRACE_LABLE
     */
    public static final String SYSTEM_TUNNING_PROBES_CONTAIN_SHARE_FTRACE_LABLE = TRACE_EVENT + SUSPEND_RESUME;

    /**
     * SYSTEM_TUNNING_PROBES_KIND_FTRACE_EVENT
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_FTRACE_EVENT = "ftrace_events";

    /**
     * SYSTEM_TUNNING_PROBES_KIND_ATRACE_APPS
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_ATRACE_APPS = "atrace_apps";

    /**
     * SYSTEM_TUNNING_PROBES_KIND_IN_MEMORY
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_IN_MEMORY = "inMemoryValue";

    /**
     * SYSTEM_TUNNING_PROBES_KIND_MAX_DURATION
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_MAX_DURATION = "maxDuration";

    /**
     * SYSTEM_TUNNING_PROBES_KIND_MAX_FILE_SIZE
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_MAX_FILE_SIZE = "maxFileSize";

    /**
     * SYSTEM_TUNNING_PROBES_KIND_FLUSH_ON_DISK
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_FLUSH_ON_DISK = "flushOnDisk";

    /**
     * SYSTEM_TUNNING_PROBES_KIND_RECORD_STATE
     */
    public static final String SYSTEM_TUNNING_PROBES_KIND_RECORD_STATE = "recordState";

    /**
     * PERFETTO_DURATION_MS_ONE
     */
    public static final String PERFETTO_DURATION_MS_ONE = "duration_ms:";

    /**
     * PERFETTO_DURATION_MS_TWO
     */
    public static final String PERFETTO_DURATION_MS_TWO =
        "write_into_file: true\n" + "file_write_period_ms: 1000\n" + "buffers {\n" + "   size_kb:";

    /**
     * PERFETTO_DURATION_MS_THREE
     */
    public static final String PERFETTO_DURATION_MS_THREE =
        "}\n" + "\n" + "data_sources {\n" + "   config {\n" + "       name: \"linux.ftrace\"\n"
            + "       target_buffer: 0\n" + "       ftrace_config {\n" + "           buffer_size_kb: 400\n";

    /**
     * PERFETTO_DURATION_MS_FOUT
     */
    public static final String PERFETTO_DURATION_MS_FOUT = "        }\n" + "   }\n" + "}";

    /**
     * SECOND_TO_MS
     */
    public static final int SECOND_TO_MS = 1000;

    /**
     * MINUTE_TO_S
     */
    public static final int MINUTE_TO_S = 60;

    /**
     * MINUTE_TO_S
     */
    public static final int HOUR_TO_MINUTE = 60;

    /**
     * MINUTE_TO_S
     */
    public static final int DAY_TO_HOUR = 24;

    /**
     * 时间换算单位
     */
    public static final int TIME_CONVERSION_UNIT = 60;

    /**
     * MEMORY_MB_TO_KB
     */
    public static final int MEMORY_MB_TO_KB = 1024;

    /**
     * MEMORY_MB_TO_KB
     */
    public static final int ANALYSIS_TO_DB_TIME_COMSUMING = 8;

    /**
     * CHEDULING_DETAIL_BYTRACE 从bytrace中获取数据
     */
    public static final String SCHEDULING_DETAIL_BYTRACE_ONE_AND_SIX = "sched;freq";

    /**
     * CPU_FREQUENCY_BYTRACE 从bytrace中获取数据
     */
    public static final String CPU_FREQUENCY_BYTRACE_ONE_AND_TWO_AND_FOUR = "idle";

    /**
     * BOARD_VOLTAGES_BYTRACE 从bytrace中获取数据
     */
    public static final String BOARD_VOLTAGES_BYTRACE = "regulators";

    /**
     * HIGH_FREQUENCY_BYTRACE 从bytrace中获取数据
     */
    public static final String HIGH_FREQUENCY_BYTRACE = "memory";

    /**
     * LOW_MEMORY_BYTRACE 从bytrace中获取数据
     */
    public static final String LOW_MEMORY_BYTRACE = "memreclaim";

    private SystemTunningProbesCheckbox() {
    }
}

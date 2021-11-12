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

package ohos.devtools.views.trace;

/**
 * sql text file path
 *
 * @since 2021/5/27 12:03
 */
public enum Sql {
    QUERY_TOTAL_TIME("QueryTotalTime"),
    QUERY_PROCESS("QueryProcess"),
    QUERY_VSYNC_APP("QueryVSYNCAPP"),
    QUERY_THREAD_DATA("QueryThreadData"),
    QUERY_THREADS_BY_PID("QueryThreadsByPid"),
    GET_FUN_DATA_BY_TID("GetFunDataByTid"),
    QUERY_CPU_DATA("QueryCpuData"),
    QUERY_CPU_DATA_LIMIT("QueryCpuDataLimit"),
    QUERY_CPU_DATA_COUNT("QueryCpuDataCount"),
    GET_THREAD_FUNC_BY_NAME("GetThreadFuncByName"),
    QUERY_CPU_FREQ_DATA("QueryCpuFreqData"),
    QUERY_CPU_MAX("QueryCpuMax"),
    QUERY_CPU_MAX_FREQ("QueryCpuMaxFreq"),
    QUERY_PREF_TOTAL_TIME("QueryPrefTotalTime"),
    QUERY_PERF_FUNC("QueryPerfFunc"),
    QUERY_PERF_FILES("QueryPerfFiles"),
    QUERY_PERF_THREAD("QueryPerfThread"),
    QUERY_CPU_SCALE("QueryCpuScale"),

    SYS_GET_TAB_COUNTERS("GetTabCounters"),
    SYS_GET_TAB_PROCESS_BY_CPU("GetTabCpuByProcess"),
    SYS_GET_TAB_THREAD_BY_CPU("GetTabCpuByThread"),
    SYS_GET_TAB_THREAD_STATES("GetTabThreadStates"),
    SYS_GET_TAB_SLICES("GetTabSlices"),
    SYS_GET_WAKEUP_TIME("QueryWakeUpThread_WakeTime"),
    SYS_GET_WAKEUP_THREAD("QueryWakeUpThread_WakeThread"),
    SYS_GET_FUN_DATA_BY_TID("GetFunDataByTid"),
    SYS_GET_PROCESS_MEM_DATA("QueryProcessMemData"),
    SYS_GET_PROCESS_MEM("QueryProcessMem"),
    SYS_GET_CPU_UTILIZATION_RATE("GetCpuUtilizationRate"),
    SYS_QUERY_THREAD_DATA("QueryThreadData"),
    SYS_QUERY_PROCESS_DATA("QueryProcessData"),
    SYS_QUERY_PROCESS_DATA_LIMIT("QueryProcessDataLimit"),
    SYS_QUERY_PROCESS_DATA_COUNT("QueryProcessDataCount"),
    SYS_QUERY_PROCESS_THREADS("QueryProcessThreads"),
    SYS_QUERY_PROCESS_THREADS_NORDER("QueryProcessThreadsNOrder"),
    SYS_QUERY_PROCESS("QueryProcess"),
    SYS_QUERY_PROCESS_NORDER("QueryProcessNOrder"),
    SYS_QUERY_CPU_FREQ_DATA("QueryCpuFreqData"),
    SYS_QUERY_CPU_MAX_FREQ("QueryCpuMaxFreq"),
    SYS_QUERY_CPU_DATA("QueryCpuData"),
    SYS_QUERY_CPU_DATA_COUNT("QueryCpuDataCount"),
    SYS_QUERY_CPU_DATA_LIMIT("QueryCpuDataLimit"),
    SYS_QUERY_CPU_MAX("QueryCpuMax"),
    SYS_QUERY_CPU_FREQ("QueryCpuFreq"),
    SYS_QUERY_TOTAL_TIME("QueryTotalTime"),
    SYS_GET_ASYNC_EVENTS("GetAsyncEvents"),
    SYS_QUERY_CLOCK_LIST("QueryClockList"),
    SYS_QUERY_CLOCK_FREQUENCY("QueryClockFrequency"),
    SYS_QUERY_CLOCK_STATE("QueryClockState"),
    SYS_QUERY_SCREEN_STATE("QueryScreenState"),
    SYS_QUERY_LOGS("QueryLogs"),
    SYS_QUERY_LOGS_COUNT("QueryLogsCount"),

    DISTRIBUTED_QUERY_TOTAL_TIME("DistributedQueryTotalTime"),
    DISTRIBUTED_QUERY_THREADS_BY_PID("DistributedQueryThreadsByPid"),
    DISTRIBUTED_GET_FUN_DATA_BY_TID("DistributedGetFunDataByTid"),
    DISTRIBUTED_SET_TRACE_RANGE_START_TIME("DistributedSetTraceRangeStartTime"),
    DISTRIBUTED_CPU_VIEWS("DistributedCpuViews"),
    DISTRIBUTED_TRACE_CPU("DistributedTraceCpu"),
    DISTRIBUTED_TRACE_MEM("DistributedTraceMem"),
    DISTRIBUTED_TRACE_MEM_UNAGG("DistributedTraceMemUnagg"),
    DISTRIBUTED_TRACE_METADATA("DistributedTraceMetadata"),
    DISTRIBUTED_TRACE_STATS("DistributedTraceStats"),
    DISTRIBUTED_TRACE_TASK_NAMES("DistributedTraceTaskNames");
    private final String name;

    Sql(String name) {
        this.name = name;
    }

    /**
     * get name
     *
     * @return name name
     */
    public String getName() {
        return name;
    }
}

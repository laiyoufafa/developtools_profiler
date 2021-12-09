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

#ifndef TRACE_DATA_CACHE_BASE_H
#define TRACE_DATA_CACHE_BASE_H


#include <array>
#include <deque>
#include <limits>
#include <map>
#include <stdexcept>
#include <string>
#include <vector>
#include "trace_stdtype.h"
namespace SysTuning {
namespace TraceStreamer {
using namespace TraceStdtype;
class TraceDataCacheBase {
public:
    TraceDataCacheBase();
    TraceDataCacheBase(const TraceDataCacheBase&) = delete;
    TraceDataCacheBase& operator=(const TraceDataCacheBase&) = delete;
    virtual ~TraceDataCacheBase() = default;

public:
    size_t ThreadSize() const
    {
        return internalThreadsData_.size();
    }
    size_t ProcessSize() const
    {
        return internalProcessesData_.size();
    }

    size_t DataDictSize() const
    {
        return dataDict_.Size();
    }
    void UpdateTraceRange()
    {
        metaData_.SetTraceDuration((traceEndTime_ - traceStartTime_) / SEC_TO_NS);
    }
    DataIndex GetDataIndex(std::string_view str);
    std::map<uint64_t, std::string> statusString_ = {{TASK_RUNNABLE, "R"},
                                                     {TASK_INTERRUPTIBLE, "S"},
                                                     {TASK_UNINTERRUPTIBLE, "D"},
                                                     {TASK_RUNNING, "Running"},
                                                     {TASK_INTERRUPTED, "I"},
                                                     {TASK_EXIT_DEAD, "X"},
                                                     {TASK_ZOMBIE, "Z"},
                                                     {TASK_KILLED, "I"},
                                                     {TASK_WAKEKILL, "R"},
                                                     {TASK_INVALID, "U"},
                                                     {TASK_CLONE, "I"},
                                                     {TASK_DK, "DK"},
                                                     {TASK_TRACED_KILL, "TK"},
                                                     {TASK_FOREGROUND, "R+"},
                                                     {TASK_MAX, "S"}};
    uint64_t traceStartTime_ = std::numeric_limits<uint64_t>::max();
    uint64_t traceEndTime_ = 0;

    Raw rawData_;
    ThreadState threadStateData_;
    Instants instantsData_;

    Filter filterData_;
    ProcessMeasureFilter processMeasureFilterData_;
    ClockEventData clockEventFilterData_;
    ClkEventData clkEventFilterData_;
    ProcessMeasureFilter processFilterData_;
    ThreadMeasureFilter threadMeasureFilterData_;
    ThreadMeasureFilter threadFilterData_;
    DataDict dataDict_;

    SchedSlice schedSliceData_;
    CallStack internalSlicesData_;
    LogInfo hilogData_;

    std::deque<Process> internalProcessesData_ = {};
    std::deque<Thread> internalThreadsData_ = {};

    Measure measureData_;
    CpuMeasureFilter cpuMeasureData_;

    StatAndInfo stat_;
    MetaData metaData_;
    SymbolsData symbolsData_;
    SysCall sysCallData_;
    ArgSet argSet_;
    DataType dataType_;
    SysMeasureFilter sysEvent_;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // TRACE_DATA_CACHE_BASE_H

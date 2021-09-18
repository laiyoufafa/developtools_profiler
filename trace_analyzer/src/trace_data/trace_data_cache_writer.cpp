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

#include "trace_data_cache_writer.h"
#include <array>
#include <deque>
#include <limits>
#include <map>
#include <optional>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>
#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
using namespace TraceStdtype;
TraceDataCacheWriter::~TraceDataCacheWriter() {}
InternalPid TraceDataCacheWriter::GetProcessInternalPid(uint32_t pid)
{
    internalProcessesData_.emplace_back(pid);
    return static_cast<InternalPid>(internalProcessesData_.size() - 1);
}
Process* TraceDataCacheWriter::GetProcessData(InternalPid internalPid)
{
    TS_ASSERT(internalPid < internalProcessesData_.size());
    return &internalProcessesData_[internalPid];
}

InternalTid TraceDataCacheWriter::NewInternalThread(uint32_t tid)
{
    internalThreadsData_.emplace_back(tid);
    return static_cast<InternalTid>(internalThreadsData_.size() - 1);
}
Thread* TraceDataCacheWriter::GetThreadData(InternalTid internalTid)
{
    TS_ASSERT(internalTid < internalThreadsData_.size());
    return &internalThreadsData_[internalTid];
}

void TraceDataCacheWriter::UpdateTraceTime(uint64_t timestamp)
{
    traceStartTime_ = std::min(traceStartTime_, timestamp);
    traceEndTime_ = std::max(traceEndTime_, timestamp);
}

CallStack* TraceDataCacheWriter::GetInternalSlicesData()
{
    return &internalSlicesData_;
}

Filter* TraceDataCacheWriter::GetFilterData()
{
    return &filterData_;
}

Raw* TraceDataCacheWriter::GetRawData()
{
    return &rawData_;
}

Measure* TraceDataCacheWriter::GetMeasureData()
{
    return &measureData_;
}

ThreadState* TraceDataCacheWriter::GetThreadStateData()
{
    return &threadStateData_;
}

SchedSlice* TraceDataCacheWriter::GetSchedSliceData()
{
    return &schedSliceData_;
}

CpuMeasureFilter* TraceDataCacheWriter::GetCpuMeasuresData()
{
    return &cpuMeasureData_;
}

ThreadMeasureFilter* TraceDataCacheWriter::GetThreadMeasureFilterData()
{
    return &threadMeasureFilterData_;
}

ThreadMeasureFilter* TraceDataCacheWriter::GetThreadFilterData()
{
    return &threadFilterData_;
}

Instants* TraceDataCacheWriter::GetInstantsData()
{
    return &instantsData_;
}

ProcessMeasureFilter* TraceDataCacheWriter::GetProcessFilterData()
{
    return &processFilterData_;
}

ProcessMeasureFilter* TraceDataCacheWriter::GetProcessMeasureFilterData()
{
    return &processMeasureFilterData_;
}

ClockEventData* TraceDataCacheWriter::GetClockEventFilterData()
{
    return &clockEventFilterData_;
}
StatAndInfo* TraceDataCacheWriter::GetStatAndInfo()
{
    return &stat_;
}

MetaData* TraceDataCacheWriter::GetMetaData()
{
    return &metaData_;
}

SymbolsData* TraceDataCacheWriter::GetSymbolsData()
{
    return &symbolsData_;
}
} // namespace TraceStreamer
} // namespace SysTuning

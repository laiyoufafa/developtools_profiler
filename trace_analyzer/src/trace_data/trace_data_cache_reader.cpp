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

#include "trace_data_cache_reader.h"
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
#include "trace_stdtype.h"

namespace SysTuning {
namespace TraceStreamer {
using namespace TraceStdtype;
TraceDataCacheReader::~TraceDataCacheReader() {}
const std::string& TraceDataCacheReader::GetDataFromDict(DataIndex id) const
{
    return dataDict_.GetDataFromDict(id);
}
const Process& TraceDataCacheReader::GetConstProcessData(InternalPid internalPid) const
{
    TUNING_ASSERT(internalPid < internalProcessesData_.size());
    return internalProcessesData_[internalPid];
}
const Thread& TraceDataCacheReader::GetConstThreadData(InternalTid internalTid) const
{
    TUNING_ASSERT(internalTid < internalThreadsData_.size());
    return internalThreadsData_[internalTid];
}
const InternalSlices& TraceDataCacheReader::GetConstInternalSlicesData() const
{
    return internalSlicesData_;
}
const Filter& TraceDataCacheReader::GetConstFilterData() const
{
    return filterData_;
}
const Raw& TraceDataCacheReader::GetConstRawTableData() const
{
    return rawData_;
}
const Counter& TraceDataCacheReader::GetConstCounterData() const
{
    return counterData_;
}

const ThreadCounterFilter* TraceDataCacheReader::ThreadCounterFilterData() const
{
    return &threadCounterFilterData_;
}
const ThreadState& TraceDataCacheReader::GetConstThreadStateData() const
{
    return threadStateData_;
}
const SchedSlice& TraceDataCacheReader::GetConstSchedSliceData() const
{
    return schedSliceData_;
}
const CpuCounter& TraceDataCacheReader::GetConstCpuCounterData() const
{
    return cpuCounterData_;
}
const ThreadCounterFilter& TraceDataCacheReader::GetConstThreadFilterData() const
{
    return threadFilterData_;
}
const Instants& TraceDataCacheReader::GetConstInstantsData() const
{
    return instantsData_;
}
const ProcessCounterFilter& TraceDataCacheReader::GetConstProcessFilterData() const
{
    return processFilterData_;
}
const ProcessCounterFilter& TraceDataCacheReader::ProcessCounterFilterData() const
{
    return processCounterFilterData_;
}
const std::string& TraceDataCacheReader::GetConstSchedStateData(uint64_t rowId) const
{
    TUNING_ASSERT(statusString_.find(rowId) != statusString_.end());
    return statusString_.at(rowId);
}
uint64_t TraceDataCacheReader::BoundStartTime() const
{
    return boundtimeStart_;
}
uint64_t TraceDataCacheReader::BoundEndTime() const
{
    return boundtimeEnd_;
}
} // namespace TraceStreamer
} // namespace SysTuning

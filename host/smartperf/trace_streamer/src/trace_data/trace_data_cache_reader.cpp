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
#include "log.h"
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
    TS_ASSERT(internalPid < internalProcessesData_.size());
    return internalProcessesData_[internalPid];
}
const Thread& TraceDataCacheReader::GetConstThreadData(InternalTid internalTid) const
{
    TS_ASSERT(internalTid < internalThreadsData_.size());
    return internalThreadsData_[internalTid];
}
const CallStack& TraceDataCacheReader::GetConstInternalSlicesData() const
{
    return callstackData_;
}
const CallStack& TraceDataCacheReader::GetConstIrqData() const
{
    return irqData_;
}
const Filter& TraceDataCacheReader::GetConstFilterData() const
{
    return filterData_;
}
const Raw& TraceDataCacheReader::GetConstRawTableData() const
{
    return rawData_;
}
const Measure& TraceDataCacheReader::GetConstMeasureData() const
{
    return measureData_;
}

const ThreadMeasureFilter& TraceDataCacheReader::GetConstThreadMeasureFilterData() const
{
    return threadMeasureFilterData_;
}
const ThreadState& TraceDataCacheReader::GetConstThreadStateData() const
{
    return threadStateData_;
}
const SchedSlice& TraceDataCacheReader::GetConstSchedSliceData() const
{
    return schedSliceData_;
}
const CpuMeasureFilter& TraceDataCacheReader::GetConstCpuMeasureData() const
{
    return cpuMeasureData_;
}
const ThreadMeasureFilter& TraceDataCacheReader::GetConstThreadFilterData() const
{
    return threadFilterData_;
}
const Instants& TraceDataCacheReader::GetConstInstantsData() const
{
    return instantsData_;
}
const ProcessMeasureFilter& TraceDataCacheReader::GetConstProcessFilterData() const
{
    return processFilterData_;
}
const ProcessMeasureFilter& TraceDataCacheReader::GetConstProcessMeasureFilterData() const
{
    return processMeasureFilterData_;
}

const ClockEventData& TraceDataCacheReader::GetConstClockEventFilterData() const
{
    return clockEventFilterData_;
}
const ClkEventData& TraceDataCacheReader::GetConstClkEventFilterData() const
{
    return clkEventFilterData_;
}
const std::string& TraceDataCacheReader::GetConstSchedStateData(uint64_t rowId) const
{
    TS_ASSERT(statusString_.find(rowId) != statusString_.end());
    return statusString_.at(rowId);
}
uint64_t TraceDataCacheReader::TraceStartTime() const
{
    return traceStartTime_;
}
uint64_t TraceDataCacheReader::TraceEndTime() const
{
    return traceEndTime_;
}

const StatAndInfo& TraceDataCacheReader::GetConstStatAndInfo() const
{
    return stat_;
}
const MetaData& TraceDataCacheReader::GetConstMetaData() const
{
    return metaData_;
}

const SymbolsData& TraceDataCacheReader::GetConstSymbolsData() const
{
    return symbolsData_;
}

const LogInfo& TraceDataCacheReader::GetConstHilogData() const
{
    return hilogData_;
}

const HeapInfo& TraceDataCacheReader::GetConstHeapData() const
{
    return heapData_;
}

const HeapFrameInfo& TraceDataCacheReader::GetConstHeapFrameData() const
{
    return heapFrameData_;
}

const Hidump& TraceDataCacheReader::GetConstHidumpData() const
{
    return hidumpData_;
}

const SysCall& TraceDataCacheReader::GetConstSysCallData() const
{
    return sysCallData_;
}
const ArgSet& TraceDataCacheReader::GetConstArgSetData() const
{
    return argSet_;
}

const DataType& TraceDataCacheReader::GetConstDataTypeData() const
{
    return dataType_;
}

const SysMeasureFilter& TraceDataCacheReader::GetConstSysMeasureFilterData() const
{
    return sysEvent_;
}
} // namespace TraceStreamer
} // namespace SysTuning

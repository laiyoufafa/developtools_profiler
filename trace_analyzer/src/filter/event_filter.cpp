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

#include "event_filter.h"

#include "filter_filter.h"
#include "process_filter.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
EventFilter::EventFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter),
      prevTimestamp_(0),
      idleStringId_(traceDataCache_->GetDataIndex("idle")),
      filterTypeStringId_(traceDataCache_->GetDataIndex("cpu_measure_filter"))
{
}

EventFilter::~EventFilter() = default;

void EventFilter::UpdateSchedSwitch(uint32_t cpu,
                                    uint64_t timestamp,
                                    uint32_t prevPid,
                                    uint32_t nextPid,
                                    std::string_view nextComm)
{
    UNUSED(prevPid);
    if (!UpdateTimestamp(timestamp)) {
        TUNING_LOGD("sched_switch event out of order by %.4f ms, skipping", (prevTimestamp_ - timestamp) / 1e6);
        return;
    }

    TUNING_ASSERT(cpu < MAX_CPUS);

    UpdateDuration(cpu, timestamp);

    DataIndex nameIndex;
    if (nextPid == 0) {
        nameIndex = idleStringId_;
    } else {
        nameIndex = traceDataCache_->GetDataIndex(nextComm);
    }

    auto internaltid = streamFilters_->processFilter_->SetThreadPid(timestamp, nextPid, nameIndex);

    auto* slices = traceDataCache_->GetSlicesData();
    auto* pendingSlice = &pendingSchedPerCpu_[cpu];
    pendingSlice->storageIndex = slices->AppendSliceData(cpu, timestamp, 0, internaltid);
    pendingSlice->pid = nextPid;
}

uint32_t EventFilter::GetOrCreatCpuCounterFilter(DataIndex name, uint32_t cpu)
{
    auto cpuCounter = traceDataCache_->GetCpuCountersData();
    auto filterId = FindFilterId(name, cpu);
    if (filterId == INVALID_UINT32) {
        std::string nameStr = traceDataCache_->GetDataFromDict(name);
        filterId = streamFilters_->filterFilter_->AddFilter("cpu_counter_track", nameStr);
        cpuCounter->AppendCpuCounter(filterId, filterTypeStringId_, name, cpu);
        AddFilterId(name, cpu, filterId);
    }

    return filterId;
}

void EventFilter::AddFilterId(DataIndex name, uint32_t cpu, uint32_t filterId)
{
    cpuNameFilters_[std::make_pair(cpu, name)] = filterId;
}

uint32_t EventFilter::FindFilterId(DataIndex name, uint32_t cpu) const
{
    auto it = cpuNameFilters_.find(std::make_pair(cpu, name));
    if (it != cpuNameFilters_.end()) {
        return it->second;
    }

    return INVALID_UINT32;
}

void EventFilter::BinderTransaction(const BinderParamter& binderParamter) const
{
    UNUSED(binderParamter);
}

void EventFilter::BinderTransactionReceived(uint64_t ts, uint32_t tid, int32_t transactionId) const
{
    UNUSED(ts);
    UNUSED(tid);
    UNUSED(transactionId);
}

void EventFilter::UpdateDuration(uint32_t cpu, uint64_t timestamp)
{
    auto* pendingSlice = &pendingSchedPerCpu_[cpu];
    if (pendingSlice->storageIndex >= std::numeric_limits<size_t>::max()) {
        return;
    }

    auto* slices = traceDataCache_->GetSchedSliceData();
    size_t idx = pendingSlice->storageIndex;
    uint64_t duration = timestamp - slices->TimeStamData()[idx];
    slices->SetDuration(idx, duration);
}

bool EventFilter::UpdateTimestamp(uint64_t timestamp)
{
    if (timestamp < prevTimestamp_) {
        return false;
    }

    prevTimestamp_ = timestamp;
    return true;
}
} // namespace TraceStreamer
} // namespace SysTuning

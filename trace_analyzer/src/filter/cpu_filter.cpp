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

#include "cpu_filter.h"

namespace SysTuning {
namespace TraceStreamer {
CpuFilter::CpuFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter) : FilterBase(dataCache, filter) {}
CpuFilter::~CpuFilter() = default;
uint64_t CpuFilter::InsertSwitchEvent(uint64_t ts, uint64_t cpu, uint64_t prevPid, uint64_t prevPior,
    uint64_t prevState, uint64_t nextPid, uint64_t nextPior)
{
    auto index = traceDataCache_->GetSchedSliceData()->AppendSchedSlice(ts, 0, cpu, nextPid, 0, nextPior);

    auto prevTidOnCpu = cpuToRowSched_.find(cpu);
    if (prevTidOnCpu != cpuToRowSched_.end()) {
        traceDataCache_->GetSchedSliceData()->Update(prevTidOnCpu->second, ts, prevState, prevPior);
        cpuToRowSched_.at(cpu) = index;
    } else {
        cpuToRowSched_.insert(std::make_pair(cpu, index));
    }

    if (nextPid) {
        auto lastRow = RowOfUidUThreadState(nextPid);
        if (lastRow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
        }
        index = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, 0, cpu, nextPid, TASK_RUNNING);
        FilterUidRow(nextPid, index, TASK_RUNNING);
        if (cpuToRowThreadState_.find(cpu) == cpuToRowThreadState_.end()) {
            cpuToRowThreadState_.insert(std::make_pair(cpu, index));
            cpuToUtidThreadState_.insert(std::make_pair(cpu, nextPid));
        } else {
            cpuToRowThreadState_.at(cpu) = index;
            cpuToUtidThreadState_.at(cpu) = nextPid;
        }
    }

    if (prevPid) {
        auto lastRow = RowOfUidUThreadState(prevPid);
        auto lastState = StateOfUidThreadState(prevPid);
        if (lastRow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
            auto temp = traceDataCache_->GetThreadStateData()->AppendThreadState(
                ts, static_cast<uint64_t>(-1), static_cast<uint64_t>(-1), prevPid, prevState);
            FilterUidRow(prevPid, temp, prevState);
        }
        UNUSED(lastState);
    }
    return 0;
}
uint64_t CpuFilter::FilterUidRow(uint64_t uid, uint64_t row, uint64_t state)
{
    if (uidToRowThreadState_.find(uid) != uidToRowThreadState_.end()) {
        uidToRowThreadState_.at(uid) = TPthread {row, state};
    } else {
        uidToRowThreadState_.insert(std::make_pair(uid, TPthread {row, state}));
    }
    return 0;
}

uint64_t CpuFilter::RowOfUidUThreadState(uint64_t uid) const
{
    auto row = uidToRowThreadState_.find(uid);
    if (row != uidToRowThreadState_.end()) {
        return (*row).second.row_;
    }
    return INVALID_UINT64;
}

uint64_t CpuFilter::StateOfUidThreadState(uint64_t uid) const
{
    auto row = uidToRowThreadState_.find(uid);
    if (row != uidToRowThreadState_.end()) {
        return (*row).second.state_;
    }
    return TASK_INVALID;
}

uint64_t CpuFilter::InsertWakeingEvent(uint64_t ts, uint64_t internalTid)
{
    uint64_t lastrow = RowOfUidUThreadState(internalTid);
    auto lastState = StateOfUidThreadState(internalTid);
    if (lastrow != INVALID_UINT64) {
        if (lastState != TASK_RUNNING) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastrow, ts);
        }
    }
    if (lastState != TASK_RUNNING) {
        auto index = traceDataCache_->GetThreadStateData()->AppendThreadState(
            ts, static_cast<uint64_t>(-1), static_cast<uint64_t>(-1), internalTid, TASK_RUNNABLE);
        FilterUidRow(internalTid, index, TASK_RUNNABLE);
    }
    return 0;
}

uint64_t CpuFilter::FindUtidInThreadStateTableByCpu(uint64_t cpu) const
{
    auto row = cpuToUtidThreadState_.find(cpu);
    if (row != cpuToUtidThreadState_.end()) {
        return (*row).second;
    }
    return INVALID_UINT64;
}
} // namespace TraceStreamer
} // namespace SysTuning

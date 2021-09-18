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
void CpuFilter::InsertSwitchEvent(uint64_t ts,
                                  uint64_t cpu,
                                  uint64_t prevPid,
                                  uint64_t prevPior,
                                  uint64_t prevState,
                                  uint64_t nextPid,
                                  uint64_t nextPior)
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
        CheckWakeingEvent(nextPid);
        auto lastRow = RowOfInternalTidThreadState(nextPid);
        if (lastRow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
        }
        index =
            traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_UINT64, cpu, nextPid, TASK_RUNNING);
        FilterInternalTidRow(nextPid, index, TASK_RUNNING);
        if (cpuToRowThreadState_.find(cpu) == cpuToRowThreadState_.end()) {
            cpuToRowThreadState_.insert(std::make_pair(cpu, index));
        } else {
            cpuToRowThreadState_.at(cpu) = index;
        }
    }

    if (prevPid) {
        CheckWakeingEvent(nextPid);
        auto lastRow = RowOfInternalTidThreadState(prevPid);
        if (lastRow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
            auto temp = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_UINT64, INVALID_UINT64,
                                                                                 prevPid, prevState);
            FilterInternalTidRow(prevPid, temp, prevState);
        }
    }
}
bool CpuFilter::InsertProcessExitEvent(uint64_t ts, uint64_t cpu, uint64_t pid)
{
    UNUSED(cpu);
    auto lastRow = RowOfInternalTidThreadState(pid);
    if (lastRow != INVALID_UINT64) {
        traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
        return true;
    }
    return false;
}
uint64_t CpuFilter::FilterInternalTidRow(uint64_t uid, uint64_t row, uint64_t state)
{
    if (internalTidToRowThreadState_.find(uid) != internalTidToRowThreadState_.end()) {
        internalTidToRowThreadState_.at(uid) = TPthread{row, state};
    } else {
        internalTidToRowThreadState_.insert(std::make_pair(uid, TPthread{row, state}));
    }
    return 0;
}

uint64_t CpuFilter::RowOfInternalTidThreadState(uint64_t uid) const
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        return (*row).second.row_;
    }
    return INVALID_UINT64;
}

uint64_t CpuFilter::StateOfInternalTidThreadState(uint64_t uid) const
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        return (*row).second.state_;
    }
    return TASK_INVALID;
}

void CpuFilter::InsertWakeingEvent(uint64_t ts, uint64_t internalTid)
{
    /* repeated wakeup msg may come, we only record last wakeupmsg, and
the wakeup will only insert to DataCache when a sched_switch comes
*/
    if (lastWakeUpMsg.find(internalTid) != lastWakeUpMsg.end()) {
        lastWakeUpMsg.at(internalTid) = ts;
    } else {
        lastWakeUpMsg.insert(std::make_pair(internalTid, ts));
    }
}

void CpuFilter::CheckWakeingEvent(uint64_t internalTid)
{
    if (lastWakeUpMsg.find(internalTid) == lastWakeUpMsg.end()) {
        return;
    }
    auto ts = lastWakeUpMsg.at(internalTid);
    lastWakeUpMsg.erase(internalTid);
    uint64_t lastrow = RowOfInternalTidThreadState(internalTid);
    auto lastState = StateOfInternalTidThreadState(internalTid);
    if (lastState == TASK_RUNNING) {
        return;
    }
    if (lastrow != INVALID_UINT64) {
        traceDataCache_->GetThreadStateData()->UpdateDuration(lastrow, ts);
    }
    auto index = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_UINT64, INVALID_UINT64,
                                                                          internalTid, TASK_RUNNABLE);
    FilterInternalTidRow(internalTid, index, TASK_RUNNABLE);
}
} // namespace TraceStreamer
} // namespace SysTuning

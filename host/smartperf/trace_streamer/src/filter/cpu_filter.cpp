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
#include "args_filter.h"
#include "process_filter.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
CpuFilter::CpuFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter) : FilterBase(dataCache, filter) {}
CpuFilter::~CpuFilter() = default;
void CpuFilter::InsertSwitchEvent(uint64_t ts,
                                  uint64_t cpu,
                                  uint32_t prevPid,
                                  uint64_t prevPior,
                                  uint64_t prevState,
                                  uint32_t nextPid,
                                  uint64_t nextPior)
{
    auto index = traceDataCache_->GetSchedSliceData()->AppendSchedSlice(ts, 0, cpu, nextPid, 0, nextPior);
    auto prevTidOnCpu = cpuToRowSched_.find(cpu);
    if (prevTidOnCpu != cpuToRowSched_.end()) {
        if (prevState == TASK_UNINTERRUPTIBLE || prevState == TASK_DK) {
            if (!pidToSchedSliceRow.count(prevPid)) {
                pidToSchedSliceRow.insert(std::make_pair(prevPid, prevTidOnCpu->second.row));
            } else {
                pidToSchedSliceRow.at(prevPid) = prevTidOnCpu->second.row;
            }
        }
        traceDataCache_->GetSchedSliceData()->Update(prevTidOnCpu->second.row, ts, prevState, prevPior);
        cpuToRowSched_.at(cpu).row = index;
    } else {
        cpuToRowSched_.insert(std::make_pair(cpu, RowPos{nextPid, index}));
    }
    if (nextPid) {
        CheckWakeupEvent(nextPid);
        auto lastRow = RowOfInternalTidInStateTable(nextPid);
        if (lastRow != INVALID_UINT64) {
            // check if there are wakeup or waking events before
            lastRow = RowOfInternalTidInStateTable(nextPid);
            traceDataCache_->GetThreadStateData()->UpdateDuration(static_cast<TableRowId>(lastRow), ts);
        }
        index = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_TIME, cpu, nextPid, TASK_RUNNING);
        RemberInternalTidInStateTable(nextPid, index, TASK_RUNNING);
        if (cpuToRowThreadState_.find(cpu) == cpuToRowThreadState_.end()) {
            cpuToRowThreadState_.insert(std::make_pair(cpu, index));
        } else {
            if (traceDataCache_->GetThreadStateData()->ItidsData()[cpuToRowThreadState_.at(cpu)] != prevPid) {
                if (!traceDataCache_->GetThreadStateData()->End(static_cast<TableRowId>(cpuToRowThreadState_.at(cpu)),
                                                                ts)) {
                    ClearInternalTidInStateTable(
                        traceDataCache_->GetThreadStateData()->ItidsData()[cpuToRowThreadState_.at(cpu)]);
                }
            }
            cpuToRowThreadState_.at(cpu) = index;
        }
    }

    if (prevPid) {
        auto lastRow = RowOfInternalTidInStateTable(prevPid);
        if (lastRow != INVALID_UINT64) {
            CheckWakeupEvent(prevPid);
            lastRow = RowOfInternalTidInStateTable(prevPid);
            traceDataCache_->GetThreadStateData()->UpdateDuration(static_cast<TableRowId>(lastRow), ts);
            streamFilters_->processFilter_->AddCpuStateCount(prevPid);
            auto thread = traceDataCache_->GetThreadData(prevPid);
            if (thread && !thread->switchCount_){
                thread->switchCount_ = 1;
            }
        }
        auto temp = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_TIME, INVALID_CPU,
                                                                             prevPid, prevState);
        RemberInternalTidInStateTable(prevPid, temp, prevState);
    }
}

bool CpuFilter::InsertBlockedReasonEvent(uint64_t ts, uint64_t cpu, uint32_t iTid, bool iowait, DataIndex caller, uint32_t delay)
{
    if (!pidToSchedSliceRow.count(iTid)) {
        return false;
    }
    auto row = pidToSchedSliceRow.at(iTid);
    // ArgSet
    ArgsSet args;
    args.AppendArg(ioWait_, BASE_DATA_TYPE_INT, iowait);
    args.AppendArg(caller_, BASE_DATA_TYPE_STRING, caller);
    args.AppendArg(delay_, BASE_DATA_TYPE_INT, delay);
    auto argSetId = streamFilters_->argsFilter_->NewArgs(args);

    traceDataCache_->GetSchedSliceData()->UpdateArg(row, argSetId);
    pidToSchedSliceRow.erase(iTid);
    return true;
}
bool CpuFilter::InsertProcessExitEvent(uint64_t ts, uint64_t cpu, uint32_t pid)
{
    UNUSED(cpu);
    auto thread = traceDataCache_->GetThreadData(static_cast<InternalTid>(pid));
    if (thread) {
        thread->endT_ = ts;
        return true;
    }
    return false;
}

bool CpuFilter::InsertProcessFreeEvent(uint64_t ts, uint32_t pid)
{
    auto thread = traceDataCache_->GetThreadData(static_cast<InternalTid>(pid));
    if (thread) {
        thread->endT_ = ts;
        return true;
    }
    return false;
}

void CpuFilter::Finish() const
{
    auto size = traceDataCache_->ThreadSize();
    for (auto i = 0; i < size; i++) {
        auto thread = traceDataCache_->GetThreadData(i);
        if (thread->internalPid_ != INVALID_UINT32) {
            traceDataCache_->GetProcessData(thread->internalPid_)->threadCount_++;
            traceDataCache_->GetProcessData(thread->internalPid_)->cpuStatesCount_ += thread->cpuStatesCount_;
            traceDataCache_->GetProcessData(thread->internalPid_)->sliceSize_ += thread->sliceSize_;
            traceDataCache_->GetProcessData(thread->internalPid_)->switchCount_ += thread->switchCount_;
            continue;
        }
        auto ipid = traceDataCache_->AppendNewProcessData(
            thread->tid_, traceDataCache_->GetDataFromDict(thread->nameIndex_), thread->startT_);
        thread->internalPid_ = ipid;
        traceDataCache_->GetProcessData(thread->internalPid_)->threadCount_++;
        traceDataCache_->GetProcessData(thread->internalPid_)->cpuStatesCount_ += thread->cpuStatesCount_;
        traceDataCache_->GetProcessData(thread->internalPid_)->sliceSize_ += thread->sliceSize_;
        traceDataCache_->GetProcessData(thread->internalPid_)->switchCount_ += thread->switchCount_;
    }
    auto threadState = traceDataCache_->GetConstThreadStateData();
    size = threadState.Size();
    auto rowData = threadState.ItidsData();
    for (auto i = 0; i < size; i++) {
        auto thread = traceDataCache_->GetThreadData(rowData[i]);
        if (thread->internalPid_ == INVALID_UINT32) {
            continue;
        }
        auto process = traceDataCache_->GetProcessData(thread->internalPid_);
        traceDataCache_->GetThreadStateData()->UpdateTidAndPid(i, thread->tid_, process->pid_);
    }
    auto slice = traceDataCache_->GetConstSchedSliceData();
    size = slice.Size();
    for (auto i = 0; i < size; i++) {
        traceDataCache_->GetSchedSliceData()->AppendInternalPid(
            traceDataCache_->GetThreadData(slice.InternalTidsData()[i])->internalPid_);
    }
}
void CpuFilter::Clear()
{
    cpuToRowThreadState_.clear();
    cpuToRowSched_.clear();
    lastWakeUpMsg_.clear();
    internalTidToRowThreadState_.clear();
}
void CpuFilter::InsertWakeupEvent(uint64_t ts, uint32_t internalTid, bool isWaking)
{
    if (!isWaking && !toRunnableTid_.count(internalTid)) {
        toRunnableTid_[internalTid] = ts;
    }
}
uint64_t CpuFilter::RemberInternalTidInStateTable(uint32_t uid, uint64_t row, uint64_t state)
{
    if (internalTidToRowThreadState_.find(uid) != internalTidToRowThreadState_.end()) {
        internalTidToRowThreadState_.at(uid) = TPthread{row, state};
    } else {
        internalTidToRowThreadState_.insert(std::make_pair(uid, TPthread{row, state}));
    }
    return 0;
}
uint64_t CpuFilter::RowOfInternalTidInStateTable(uint32_t uid) const
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        return (*row).second.row_;
    }
    return INVALID_UINT64;
}
void CpuFilter::ClearInternalTidInStateTable(uint32_t uid)
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        internalTidToRowThreadState_.erase(row);
    }
}
uint64_t CpuFilter::StateOfInternalTidInStateTable(uint32_t uid) const
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        return (*row).second.state_;
    }
    return TASK_INVALID;
}

void CpuFilter::CheckWakeupEvent(uint32_t internalTid)
{
    if (toRunnableTid_.count(internalTid)) {
        uint64_t lastrow = RowOfInternalTidInStateTable(internalTid);
        auto lastState = StateOfInternalTidInStateTable(internalTid);
        if (lastState == TASK_RUNNING) {
            toRunnableTid_.erase(internalTid);
            return;
        }
        if (lastrow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(static_cast<TableRowId>(lastrow),
                                                                  toRunnableTid_.at(internalTid));
        }
        auto index = traceDataCache_->GetThreadStateData()->AppendThreadState(
            toRunnableTid_.at(internalTid), INVALID_TIME, INVALID_CPU, internalTid, TASK_RUNNABLE);
        RemberInternalTidInStateTable(internalTid, index, TASK_RUNNABLE);
        toRunnableTid_.erase(internalTid);
    }
    return;
}
} // namespace TraceStreamer
} // namespace SysTuning

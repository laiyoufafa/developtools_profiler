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
    auto switchEvent = std::make_unique<TSSwitchEvent>(ts, cpu, prevPid, prevPior, prevState, nextPid, nextPior);
    auto cpuEvent = std::make_unique<TSCpuEvent>();
    cpuEvent->type_ = TS_EVENT_THREAD_SWITCH;
    cpuEvent->switchEvent_ = std::move(switchEvent);
    tsCpuEventQueue_.insert(std::make_pair(ts, std::move(cpuEvent)));
    MaybeDealEvent();
}
bool CpuFilter::InsertProcessExitEvent(uint64_t ts, uint64_t cpu, uint64_t pid)
{
    auto processExitEvent = std::make_unique<TSProcessExitEvent>(ts, cpu, pid);
    auto cpuEvent = std::make_unique<TSCpuEvent>();
    cpuEvent->type_ = TS_EVENT_PROCESS_EXIT;
    cpuEvent->processExitEvent_ = std::move(processExitEvent);
    tsCpuEventQueue_.insert(std::make_pair(ts,
        std::move(cpuEvent)));
    MaybeDealEvent();
    return true;
}

bool CpuFilter::InsertProcessFreeEvent(uint64_t ts, uint64_t pid)
{
    auto processExitEvent = std::make_unique<TSProcessExitEvent>(ts, 0, pid);
    auto cpuEvent = std::make_unique<TSCpuEvent>();
    cpuEvent->type_ = TS_EVENT_PROCESS_FREE;
    cpuEvent->processExitEvent_ = std::move(processExitEvent);
    tsCpuEventQueue_.insert(std::make_pair(ts,
        std::move(cpuEvent)));
    MaybeDealEvent();
    return true;
}
void CpuFilter::InsertWakeupEvent(uint64_t ts, uint64_t internalTid)
{
    auto wakeupEvent = std::make_unique<TSWakeupEvent>(TSWakeupEvent(ts, internalTid));
    auto cpuEvent = std::make_unique<TSCpuEvent>();
    cpuEvent->type_ = TS_EVENT_THREAD_WAKING;
    cpuEvent->wakeupEvent_ = std::move(wakeupEvent);
    tsCpuEventQueue_.insert(std::make_pair(ts,
        std::move(cpuEvent)));
    MaybeDealEvent();
}
uint64_t CpuFilter::RemberInternalTidInStateTable(uint64_t uid, uint64_t row, uint64_t state)
{
    if (internalTidToRowThreadState_.find(uid) != internalTidToRowThreadState_.end()) {
        internalTidToRowThreadState_.at(uid) = TPthread{row, state};
    } else {
        internalTidToRowThreadState_.insert(std::make_pair(uid, TPthread{row, state}));
    }
    return 0;
}

void CpuFilter::MaybeDealEvent()
{
    if (tsCpuEventQueue_.size() > MAX_CACHE_SIZE) {
        DealEvent(tsCpuEventQueue_.begin()->second.get());
        tsCpuEventQueue_.erase(tsCpuEventQueue_.begin());
    }
}

void CpuFilter::DealEvent(const TSCpuEvent* event)
{
    switch (static_cast<size_t>(event->type_)) {
        case TS_EVENT_THREAD_SWITCH:
            ExecInsertSwitchEvent(event->switchEvent_->ts_, event->switchEvent_->cpu_, event->switchEvent_->prevPid_,
                                  event->switchEvent_->prevPior_, event->switchEvent_->prevState_,
                                  event->switchEvent_->nextPid_, event->switchEvent_->nextPior_);
            break;
        case TS_EVENT_THREAD_WAKING:
            ExecInsertWakeupEvent(event->wakeupEvent_->ts_, event->wakeupEvent_->pid_);
            break;
        case TS_EVENT_PROCESS_EXIT:
        case TS_EVENT_PROCESS_FREE:
            ExecInsertProcessExitEvent(event->processExitEvent_->ts_, event->processExitEvent_->cpu_,
                                       event->processExitEvent_->pid_);
            break;
        default:
            break;
    }
}
void CpuFilter::FinishCpuEvent()
{
    for (auto it = tsCpuEventQueue_.begin(); it != tsCpuEventQueue_.end(); it++) {
        DealEvent(it->second.get());
    }
    tsCpuEventQueue_.clear();
}
void CpuFilter::ExecInsertSwitchEvent(uint64_t ts,
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
        CheckWakeupEvent(nextPid);
        auto lastRow = RowOfInternalTidInStateTable(nextPid);
        if (lastRow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
        }
        index =
            traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_UINT64, cpu, nextPid, TASK_RUNNING);
        RemberInternalTidInStateTable(nextPid, index, TASK_RUNNING);
        if (cpuToRowThreadState_.find(cpu) == cpuToRowThreadState_.end()) {
            cpuToRowThreadState_.insert(std::make_pair(cpu, index));
        } else {
            cpuToRowThreadState_.at(cpu) = index;
        }
    }

    if (prevPid) {
        CheckWakeupEvent(prevPid);
        auto lastRow = RowOfInternalTidInStateTable(prevPid);
        if (lastRow != INVALID_UINT64) {
            traceDataCache_->GetThreadStateData()->UpdateDuration(lastRow, ts);
        }
        auto temp = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_UINT64, INVALID_UINT64,
                                                                             prevPid, prevState);
        RemberInternalTidInStateTable(prevPid, temp, prevState);
    }
}
void CpuFilter::ExecInsertWakeupEvent(uint64_t ts, uint64_t internalTid)
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
bool CpuFilter::ExecInsertProcessExitEvent(uint64_t ts, uint64_t cpu, uint64_t pid)
{
    UNUSED(cpu);
    auto thread = traceDataCache_->GetThreadData(static_cast<InternalTid>(pid));
    if (thread) {
        thread->endT_ = ts;
        return true;
    }
    return false;
}
uint64_t CpuFilter::RowOfInternalTidInStateTable(uint64_t uid) const
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        return (*row).second.row_;
    }
    return INVALID_UINT64;
}

uint64_t CpuFilter::StateOfInternalTidInStateTable(uint64_t uid) const
{
    auto row = internalTidToRowThreadState_.find(uid);
    if (row != internalTidToRowThreadState_.end()) {
        return (*row).second.state_;
    }
    return TASK_INVALID;
}

void CpuFilter::CheckWakeupEvent(uint64_t internalTid)
{
    if (lastWakeUpMsg.find(internalTid) == lastWakeUpMsg.end()) {
        return;
    }
    auto ts = lastWakeUpMsg.at(internalTid);
    lastWakeUpMsg.erase(internalTid);
    uint64_t lastrow = RowOfInternalTidInStateTable(internalTid);
    auto lastState = StateOfInternalTidInStateTable(internalTid);
    if (lastState == TASK_RUNNING) {
        return;
    }
    if (lastrow != INVALID_UINT64) {
        traceDataCache_->GetThreadStateData()->UpdateDuration(lastrow, ts);
    }
    auto index = traceDataCache_->GetThreadStateData()->AppendThreadState(ts, INVALID_UINT64, INVALID_UINT64,
                                                                          internalTid, TASK_RUNNABLE);
    RemberInternalTidInStateTable(internalTid, index, TASK_RUNNABLE);
}
} // namespace TraceStreamer
} // namespace SysTuning

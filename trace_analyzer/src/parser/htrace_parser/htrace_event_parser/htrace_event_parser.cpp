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
#include "htrace_event_parser.h"
#include <sstream>
#include <string>
#include "clock_filter.h"
#include "cpu_filter.h"
#include "log.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "stat_filter.h"
#include "symbols_filter.h"
#include "thread_state.h"
#include "types/plugins/ftrace_data/binder.pb.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceEventParser::HtraceEventParser(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : EventParserBase(dataCache, filter), workQueueId_(dataCache->dataDict_.GetStringIndex("workqueue"))
{
    eventToFunctionMap_ = {{config_.eventNameMap_.at(TRACE_EVENT_SCHED_SWITCH),
                            std::bind(&HtraceEventParser::SchedSwitchEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_TASK_RENAME),
                            std::bind(&HtraceEventParser::TaskRenameEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_TASK_NEWTASK),
                            std::bind(&HtraceEventParser::TaskNewtaskEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_TRACING_MARK_WRITE),
                            std::bind(&HtraceEventParser::TracingMarkWriteEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_SCHED_WAKEUP),
                            std::bind(&HtraceEventParser::SchedWakeupEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_SCHED_WAKEUP_NEW),
                            std::bind(&HtraceEventParser::SchedWakeupNewEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_PROCESS_EXIT),
                            std::bind(&HtraceEventParser::ProcessExitEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_SCHED_WAKING),
                            std::bind(&HtraceEventParser::SchedWakingEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_CPU_IDLE),
                            std::bind(&HtraceEventParser::CpuIdleEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_CPU_FREQUENCY),
                            std::bind(&HtraceEventParser::CpuFrequencyEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_WORKQUEUE_EXECUTE_START),
                            std::bind(&HtraceEventParser::WorkqueueExecuteStartEvent, this, std::placeholders::_1)},
                           {config_.eventNameMap_.at(TRACE_EVENT_WORKQUEUE_EXECUTE_END),
                            std::bind(&HtraceEventParser::WorkqueueExecuteEndEvent, this, std::placeholders::_1)}};
}

HtraceEventParser::~HtraceEventParser() = default;
void HtraceEventParser::ParseDataItem(const FtraceCpuDetailMsg* cpuDetail, BuiltinClocks clock)
{
    eventCpu_ = cpuDetail->cpu();
    auto events = cpuDetail->event();
    if (events.size()) {
        if (cpuDetail->overwrite()) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_LOST);
        }
        // parser cpu event
        for (auto i = 0; i < events.size(); i++) {
            auto event = cpuDetail->event(i);
            eventTimestamp_ = event.timestamp();
            eventTimestamp_ = streamFilters_->clockFilter_->ToPrimaryTraceTime(clock, eventTimestamp_);
            traceDataCache_->UpdateTraceTime(eventTimestamp_);
            if (event.tgid() != INVALID_INT32) {
                eventPid_ = event.tgid();
                streamFilters_->processFilter_->GetOrCreateThreadWithPid(eventPid_, eventPid_);
                streamFilters_->processFilter_->UpdateOrCreateThreadWithPidAndName(eventPid_, eventPid_, "name");
            }

            if (event.has_binder_transaction_format()) {
                InvokeFunc(TRACE_EVENT_BINDER_TRANSACTION, event.binder_transaction_format());
            } else if (event.has_binder_transaction_received_format()) {
                InvokeFunc(TRACE_EVENT_BINDER_TRANSACTION_RECEIVED, event.binder_transaction_received_format());
            } else if (event.has_sched_switch_format()) {
                InvokeFunc(TRACE_EVENT_SCHED_SWITCH, event.sched_switch_format());
            } else if (event.has_task_rename_format()) {
                InvokeFunc(TRACE_EVENT_TASK_RENAME, event.task_rename_format());
            } else if (event.has_task_newtask_format()) {
                InvokeFunc(TRACE_EVENT_TASK_NEWTASK, event.task_newtask_format());
            } else if (event.has_sched_wakeup_format()) {
                InvokeFunc(TRACE_EVENT_SCHED_WAKEUP, event.sched_wakeup_format());
            } else if (event.has_sched_wakeup_new_format()) {
                InvokeFunc(TRACE_EVENT_SCHED_WAKEUP, event.sched_wakeup_new_format());
            } else if (event.has_sched_process_exit_format()) {
                InvokeFunc(TRACE_EVENT_PROCESS_EXIT, event.sched_process_exit_format());
            } else if (event.has_sched_waking_format()) {
                InvokeFunc(TRACE_EVENT_SCHED_WAKING, event.sched_waking_format());
            } else if (event.has_cpu_idle_format()) {
                InvokeFunc(TRACE_EVENT_CPU_IDLE, event.cpu_idle_format());
            } else if (event.has_cpu_frequency_format()) {
                InvokeFunc(TRACE_EVENT_CPU_FREQUENCY, event.cpu_frequency_format());
            } else if (event.has_workqueue_execute_start_format()) {
                InvokeFunc(TRACE_EVENT_WORKQUEUE_EXECUTE_START, event.workqueue_execute_start_format());
            } else if (event.has_workqueue_execute_end_format()) {
                InvokeFunc(TRACE_EVENT_WORKQUEUE_EXECUTE_END, event.workqueue_execute_end_format());
            }
        }
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_LOST);
    }
}
bool HtraceEventParser::SchedSwitchEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const SchedSwitchFormat&>(event);
    uint32_t prevPrioValue = msg.prev_prio();
    uint32_t nextPrioValue = msg.next_prio();
    uint32_t prevPidValue = msg.prev_pid();
    uint32_t nextPidValue = msg.next_pid();

    std::string prevCommStr = msg.prev_comm();
    std::string nextCommStr = msg.next_comm();
    auto prevState = msg.prev_state();

    auto nextInternalTid =
        streamFilters_->processFilter_->UpdateOrCreateThreadWithName(eventTimestamp_, nextPidValue, nextCommStr);
    auto uprevtid =
        streamFilters_->processFilter_->UpdateOrCreateThreadWithName(eventTimestamp_, prevPidValue, prevCommStr);
    streamFilters_->cpuFilter_->InsertSwitchEvent(eventTimestamp_, eventCpu_, uprevtid,
                                                  static_cast<uint64_t>(prevPrioValue), prevState, nextInternalTid,
                                                  static_cast<uint64_t>(nextPrioValue));
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_SWITCH, STAT_EVENT_RECEIVED);
    return true;
}
bool HtraceEventParser::ProcessExitEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const SchedProcessExitFormat&>(event);
    uint32_t pidValue = msg.pid();
    std::string commStr = msg.comm();
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_PROCESS_EXIT, STAT_EVENT_RECEIVED);
    auto iTid = streamFilters_->processFilter_->UpdateOrCreateThreadWithName(eventTimestamp_, pidValue, commStr);
    if (streamFilters_->cpuFilter_->InsertProcessExitEvent(eventTimestamp_, eventCpu_, iTid)) {
        return true;
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_PROCESS_EXIT, STAT_EVENT_NOTMATCH);
        return false;
    }
}
bool HtraceEventParser::TaskRenameEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const TaskRenameFormat&>(event);
    auto prevCommStr = msg.newcomm();
    auto pidValue = msg.pid();
    streamFilters_->processFilter_->UpdateOrCreateProcessWithName(pidValue, prevCommStr);
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TASK_RENAME, STAT_EVENT_RECEIVED);
    return true;
}
bool HtraceEventParser::TaskNewtaskEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const TaskNewtaskFormat&>(event);
    auto commonStr = msg.comm();
    auto pidValue = msg.pid();

    uint32_t ftracePid = 0;
    if (eventPid_ != INVALID_UINT32) {
        ftracePid = eventPid_;
    }

    static const uint32_t threadPid = 2;
    static const uint32_t cloneThread = 0x00010000;
    auto cloneFlags = msg.clone_flags();
    if ((cloneFlags & cloneThread) == 0 && ftracePid != threadPid) {
        streamFilters_->processFilter_->UpdateOrCreateProcessWithName(pidValue, commonStr);
    } else if (ftracePid == threadPid) {
        streamFilters_->processFilter_->GetOrCreateThreadWithPid(pidValue, threadPid);
    }
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TASK_NEWTASK, STAT_EVENT_RECEIVED);
    return true;
}
bool HtraceEventParser::TracingMarkWriteEvent(const google::protobuf::MessageLite& event) const
{
    UNUSED(event);
    TS_LOGE("TracingMarkWriteEvent");
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool HtraceEventParser::SchedWakeupEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const SchedWakeupFormat&>(event);
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_wakeup"));
    auto instants = traceDataCache_->GetInstantsData();

    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(eventTimestamp_, msg.pid());
    instants->AppendInstantEventData(eventTimestamp_, name, internalTid);

    std::optional<uint32_t> targetCpu = msg.target_cpu();
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, eventTimestamp_, RAW_SCHED_WAKEUP, targetCpu.value(),
                                                     internalTid);
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP, STAT_EVENT_RECEIVED);
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP, STAT_EVENT_DATA_INVALID);
    }
    return true;
}
bool HtraceEventParser::SchedWakeupNewEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const SchedWakeupNewFormat&>(event);
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_wakeup_new"));
    auto instants = traceDataCache_->GetInstantsData();

    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(eventTimestamp_, msg.pid());
    instants->AppendInstantEventData(eventTimestamp_, name, internalTid);

    std::optional<uint32_t> targetCpu = msg.target_cpu();
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, eventTimestamp_, RAW_SCHED_WAKEUP, targetCpu.value(),
                                                     internalTid);
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP_NEW, STAT_EVENT_RECEIVED);
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP_NEW, STAT_EVENT_DATA_INVALID);
    }
    return true;
}
bool HtraceEventParser::SchedWakingEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const SchedWakingFormat&>(event);
    std::optional<uint32_t> wakePidValue = msg.pid();
    if (!wakePidValue.has_value()) {
        TS_LOGD("Failed to convert wake_pid");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKING, STAT_EVENT_DATA_INVALID);
        return false;
    }
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_waking"));
    auto instants = traceDataCache_->GetInstantsData();
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(eventTimestamp_, eventPid_);
    instants->AppendInstantEventData(eventTimestamp_, name, internalTid);
    streamFilters_->cpuFilter_->InsertWakeingEvent(eventTimestamp_, internalTid);
    std::optional<uint32_t> targetCpu = msg.target_cpu();
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, eventTimestamp_, RAW_SCHED_WAKING, targetCpu.value(),
                                                     internalTid);
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKING, STAT_EVENT_RECEIVED);
    }
    return true;
}
bool HtraceEventParser::CpuIdleEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const CpuIdleFormat&>(event);
    std::optional<uint32_t> eventCpuValue = msg.cpu_id();
    std::optional<uint64_t> newStateValue = msg.state();
    if (!eventCpuValue.has_value()) {
        TS_LOGW("Failed to convert event cpu");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_IDLE, STAT_EVENT_DATA_INVALID);
        return false;
    }
    if (!newStateValue.has_value()) {
        TS_LOGW("Failed to convert state");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_IDLE, STAT_EVENT_DATA_INVALID);
        return false;
    }
    auto cpuIdleNameIndex = traceDataCache_->GetDataIndex(std::string_view("cpu_idle"));

    streamFilters_->cpuMeasureFilter_->AppendNewMeasureData(eventCpuValue.value(), cpuIdleNameIndex, eventTimestamp_,
                                                            newStateValue.value());

    // Add cpu_idle event to raw_data_table
    traceDataCache_->GetRawData()->AppendRawData(0, eventTimestamp_, RAW_CPU_IDLE, eventCpuValue.value(), 0);
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_IDLE, STAT_EVENT_RECEIVED);
    return true;
}
bool HtraceEventParser::CpuFrequencyEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const CpuFrequencyFormat&>(event);
    std::optional<uint64_t> newStateValue = msg.state();
    std::optional<uint32_t> eventCpuValue = msg.cpu_id();

    if (!newStateValue.has_value()) {
        TS_LOGW("Failed to convert state");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_FREQUENCY, STAT_EVENT_DATA_INVALID);
        return false;
    }
    if (!eventCpuValue.has_value()) {
        TS_LOGW("Failed to convert event cpu");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_FREQUENCY, STAT_EVENT_DATA_INVALID);
        return false;
    }
    TS_LOGE("CPU FREQ");
    auto cpuidleNameIndex = traceDataCache_->GetDataIndex(std::string_view("cpu_frequency"));

    streamFilters_->cpuMeasureFilter_->AppendNewMeasureData(eventCpuValue.value(), cpuidleNameIndex, eventTimestamp_,
                                                            newStateValue.value());
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_FREQUENCY, STAT_EVENT_RECEIVED);
    return true;
}
bool HtraceEventParser::WorkqueueExecuteStartEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const WorkqueueExecuteStartFormat&>(event);
    auto result = streamFilters_->sliceFilter_->BeginSlice(eventTimestamp_, eventPid_, eventPid_, workQueueId_,
                                                           streamFilters_->symbolsFilter_->GetFunc(msg.function()));
    if (result) {
        traceDataCache_->GetInternalSlicesData()->AppendDistributeInfo();
    }
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_START, STAT_EVENT_RECEIVED);
    return true;
}
bool HtraceEventParser::WorkqueueExecuteEndEvent(const google::protobuf::MessageLite& event) const
{
    const auto msg = static_cast<const WorkqueueExecuteEndFormat&>(event);
    if (streamFilters_->sliceFilter_->EndSlice(eventTimestamp_, eventPid_, eventPid_)) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_END, STAT_EVENT_NOTMATCH);
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_END, STAT_EVENT_RECEIVED);
    }
    return true;
}

bool HtraceEventParser::InvokeFunc(const SupportedTraceEventType& eventType,
                                   const google::protobuf::MessageLite& msgBase)
{
    auto eventName = config_.eventNameMap_.find(eventType);
    if (eventName == config_.eventNameMap_.end()) {
        // log warn
        streamFilters_->statFilter_->IncreaseStat(eventType, STAT_EVENT_NOTSUPPORTED);
        return false;
    }
    auto it = eventToFunctionMap_.find(eventName->second);
    if (it == eventToFunctionMap_.end()) {
        // log warn
        streamFilters_->statFilter_->IncreaseStat(eventType, STAT_EVENT_NOTSUPPORTED);
        return false;
    }
    it->second(msgBase);
    return true;
}
} // namespace TraceStreamer
} // namespace SysTuning

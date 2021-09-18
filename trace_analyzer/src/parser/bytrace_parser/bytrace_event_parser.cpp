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

#include "bytrace_event_parser.h"
#include <regex>
#include <string>
#include <unordered_map>

#include "common.h"
#include "cpu_filter.h"
#include "filter_filter.h"
#include "measure_filter.h"
#include "parting_string.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "stat_filter.h"
#include "string_to_numerical.h"
#include "thread_state.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
std::string GetFunctionName(const std::string_view& text, const std::string_view& delimiter)
{
    std::string str("");
    if (delimiter.empty()) {
        return str;
    }

    std::size_t foundIndex = text.find(delimiter);
    if (foundIndex != std::string::npos) {
        std::size_t funIndex = foundIndex + delimiter.size();
        str = std::string(text.substr(funIndex, text.size() - funIndex));
    }
    return str;
}
} // namespace

BytraceEventParser::BytraceEventParser(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : EventParserBase(dataCache, filter),
      ioWaitId_(const_cast<TraceDataCache*>(dataCache)->GetDataIndex("io_wait")),
      workQueueId_(const_cast<TraceDataCache*>(dataCache)->GetDataIndex("workqueue")),
      schedWakeupId_(const_cast<TraceDataCache*>(dataCache)->GetDataIndex("sched_wakeup")),
      schedBlockedReasonId_(const_cast<TraceDataCache*>(dataCache)->GetDataIndex("sched_blocked_reason")),
      pointLength_(1),
      maxPointLength_(2),
      byHex_(16)
{
    eventToFunctionMap_ = {
        {config_.eventNameMap_.at(TRACE_EVENT_SCHED_SWITCH),
         bind(&BytraceEventParser::SchedSwitchEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_TASK_RENAME),
         bind(&BytraceEventParser::TaskRenameEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_TASK_NEWTASK),
         bind(&BytraceEventParser::TaskNewtaskEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_TRACING_MARK_WRITE),
         bind(&BytraceEventParser::TracingMarkWriteEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_SCHED_WAKEUP),
         bind(&BytraceEventParser::SchedWakeupEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_SCHED_WAKING),
         bind(&BytraceEventParser::SchedWakingEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_CPU_IDLE),
         bind(&BytraceEventParser::CpuIdleEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_CPU_FREQUENCY),
         bind(&BytraceEventParser::CpuFrequencyEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_WORKQUEUE_EXECUTE_START),
         bind(&BytraceEventParser::WorkqueueExecuteStartEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_WORKQUEUE_EXECUTE_END),
         bind(&BytraceEventParser::WorkqueueExecuteEndEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_CLOCK_SET_RATE),
         bind(&BytraceEventParser::SetRateEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_CLOCK_ENABLE),
         bind(&BytraceEventParser::ClockEnableEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_CLOCK_DISABLE),
         bind(&BytraceEventParser::ClockDisableEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_REGULATOR_SET_VOLTAGE),
         bind(&BytraceEventParser::RegulatorSetVoltageEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_REGULATOR_SET_VOLTAGE_COMPLETE),
         bind(&BytraceEventParser::RegulatorSetVoltageCompleteEvent, this, std::placeholders::_1,
              std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_REGULATOR_DISABLE),
         bind(&BytraceEventParser::RegulatorDisableEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_REGULATOR_DISABLE_COMPLETE),
         bind(&BytraceEventParser::RegulatorDisableCompleteEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_IPI_ENTRY),
         bind(&BytraceEventParser::IpiEntryEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_IPI_EXIT),
         bind(&BytraceEventParser::IpiExitEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_IRQ_HANDLER_ENTRY),
         bind(&BytraceEventParser::IrqHandlerEntryEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_IRQ_HANDLER_EXIT),
         bind(&BytraceEventParser::IrqHandlerExitEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_SOFTIRQ_RAISE),
         bind(&BytraceEventParser::SoftIrqRaiseEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_SOFTIRQ_ENTRY),
         bind(&BytraceEventParser::SoftIrqEntryEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_SOFTIRQ_EXIT),
         bind(&BytraceEventParser::SoftIrqExitEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_BINDER_TRANSACTION_ALLOC_BUF),
         bind(&BytraceEventParser::BinderTransactionAllocBufEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_SCHED_WAKEUP_NEW),
         bind(&BytraceEventParser::SchedWakeupEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_PROCESS_EXIT),
         bind(&BytraceEventParser::ProcessExitEvent, this, std::placeholders::_1, std::placeholders::_2)}};
}

bool BytraceEventParser::SchedSwitchEvent(const ArgsMap& args, const BytraceLine& line) const
{
    if (args.empty() || args.size() < MIN_SCHED_ARGS_COUNT) {
        TS_LOGD("Failed to parse sched_switch event, no args or args size < 6");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_SWITCH, STAT_EVENT_DATA_INVALID);
        return false;
    }
    auto prevCommStr = std::string_view(args.at("prev_comm"));
    auto nextCommStr = std::string_view(args.at("next_comm"));
    auto prevPrioValue = base::StrToInt32(args.at("prev_prio"));
    auto nextPrioValue = base::StrToInt32(args.at("next_prio"));
    auto prevPidValue = base::StrToUInt32(args.at("prev_pid"));
    auto nextPidValue = base::StrToUInt32(args.at("next_pid"));
    if (!(!prevCommStr.empty() && prevPidValue.has_value() && prevPrioValue.has_value() && nextPidValue.has_value() &&
          nextPrioValue.has_value())) {
        TS_LOGD("Failed to parse sched_switch event");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_SWITCH, STAT_EVENT_DATA_INVALID);
        return false;
    }

    auto prevStateStr = args.at("prev_state");
    uint64_t prevState = ThreadState(prevStateStr.c_str()).State();
    // traceDataCache_->GetThreadStateDleata()->State
    auto nextInternalTid =
        streamFilters_->processFilter_->UpdateOrCreateThreadWithName(line.ts, nextPidValue.value(), nextCommStr);
    auto uprevtid =
        streamFilters_->processFilter_->UpdateOrCreateThreadWithName(line.ts, prevPidValue.value(), prevCommStr);
    streamFilters_->cpuFilter_->InsertSwitchEvent(line.ts, line.cpu, uprevtid,
                                                  static_cast<uint64_t>(prevPrioValue.value()), prevState,
                                                  nextInternalTid, static_cast<uint64_t>(nextPrioValue.value()));
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_SWITCH, STAT_EVENT_RECEIVED);
    return true;
}

bool BytraceEventParser::TaskRenameEvent(const ArgsMap& args, const BytraceLine& line) const
{
    auto prevCommStr = std::string_view(args.at("newcomm"));
    auto pidValue = base::StrToUInt32(args.at("pid"));
    streamFilters_->processFilter_->UpdateOrCreateThreadWithName(line.ts, pidValue.value(), prevCommStr);
    return true;
}

bool BytraceEventParser::TaskNewtaskEvent(const ArgsMap& args, const BytraceLine& line) const
{
    auto commonStr = std::string_view(args.at("comm"));
    auto pidValue = base::StrToUInt32(args.at("pid"));

    uint32_t ftracePid = 0;
    if (!line.tGidStr.empty() && line.tGidStr != "-----") {
        std::optional<uint32_t> tgid = base::StrToUInt32(line.tGidStr);
        if (tgid) {
            ftracePid = tgid.value();
        }
    }

    static const uint32_t threadPid = 2;
    static const uint32_t cloneThread = 0x00010000;
    auto cloneFlags = base::StrToUInt64(args.at("clone_flags"), byHex_).value();
    if ((cloneFlags & cloneThread) == 0 && ftracePid != threadPid) {
        streamFilters_->processFilter_->UpdateOrCreateProcessWithName(static_cast<uint32_t>(pidValue.value()),
                                                                      commonStr);
    } else if (ftracePid == threadPid) {
        streamFilters_->processFilter_->GetOrCreateThreadWithPid(static_cast<uint32_t>(pidValue.value()), threadPid);
    }
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TASK_NEWTASK, STAT_EVENT_RECEIVED);
    return true;
}

bool BytraceEventParser::TracingMarkWriteEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    ParsePrintEvent(line.ts, line.pid, line.argsStr.c_str());
    return true;
}

bool BytraceEventParser::SchedWakeupEvent(const ArgsMap& args, const BytraceLine& line) const
{
    if (args.size() < MIN_SCHED_WAKEUP_ARGS_COUNT) {
        TS_LOGD("Failed to parse SchedWakeupEvent event, no args or args size < 2");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP, STAT_EVENT_DATA_INVALID);
        return false;
    }
    std::optional<uint32_t> wakePidValue = base::StrToUInt32(args.at("pid"));
    if (!wakePidValue.has_value()) {
        TS_LOGD("Failed to convert wake_pid");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP, STAT_EVENT_DATA_INVALID);
        return false;
    }
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_wakeup"));
    auto instants = traceDataCache_->GetInstantsData();
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(line.ts, wakePidValue.value());
    instants->AppendInstantEventData(line.ts, name, internalTid);
    streamFilters_->cpuFilter_->InsertWakeingEvent(line.ts, internalTid);
    std::optional<uint32_t> targetCpu = base::StrToUInt32(args.at("target_cpu"));
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, line.ts, RAW_SCHED_WAKEUP, targetCpu.value(), internalTid);
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKEUP, STAT_EVENT_RECEIVED);
    }
    return true;
}

bool BytraceEventParser::SchedWakingEvent(const ArgsMap& args, const BytraceLine& line) const
{
    std::optional<uint32_t> wakePidValue = base::StrToUInt32(args.at("pid"));
    if (!wakePidValue.has_value()) {
        TS_LOGD("Failed to convert wake_pid");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKING, STAT_EVENT_DATA_INVALID);
        return false;
    }
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_waking"));
    auto instants = traceDataCache_->GetInstantsData();
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(line.ts, line.pid);
    instants->AppendInstantEventData(line.ts, name, internalTid);
    std::optional<uint32_t> targetCpu = base::StrToUInt32(args.at("target_cpu"));
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, line.ts, RAW_SCHED_WAKING, targetCpu.value(), internalTid);
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKING, STAT_EVENT_RECEIVED);
    }
    return true;
}

bool BytraceEventParser::CpuIdleEvent(const ArgsMap& args, const BytraceLine& line) const
{
    std::optional<uint32_t> eventCpuValue = base::StrToUInt32(args.at("cpu_id"));
    std::optional<int64_t> newStateValue = base::StrToInt64(args.at("state"));
    if (!eventCpuValue.has_value()) {
        TS_LOGD("Failed to convert event cpu");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_IDLE, STAT_EVENT_DATA_INVALID);
        return false;
    }
    if (!newStateValue.has_value()) {
        TS_LOGD("Failed to convert state");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_IDLE, STAT_EVENT_DATA_INVALID);
        return false;
    }
    auto cpuIdleNameIndex = traceDataCache_->GetDataIndex(line.eventName.c_str());
    streamFilters_->cpuMeasureFilter_->AppendNewMeasureData(eventCpuValue.value(), cpuIdleNameIndex, line.ts,
                                                            newStateValue.value());
    // Add cpu_idle event to raw_data_table
    traceDataCache_->GetRawData()->AppendRawData(0, line.ts, RAW_CPU_IDLE, eventCpuValue.value(), 0);
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_IDLE, STAT_EVENT_RECEIVED);
    return true;
}

bool BytraceEventParser::CpuFrequencyEvent(const ArgsMap& args, const BytraceLine& line) const
{
    std::optional<uint32_t> eventCpuValue = base::StrToUInt32(args.at("cpu_id"));
    std::optional<int64_t> newStateValue = base::StrToInt64(args.at("state"));

    if (!newStateValue.has_value()) {
        TS_LOGD("Failed to convert state");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_FREQUENCY, STAT_EVENT_DATA_INVALID);
        return false;
    }
    if (!eventCpuValue.has_value()) {
        TS_LOGD("Failed to convert event cpu");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CPU_FREQUENCY, STAT_EVENT_DATA_INVALID);
        return false;
    }

    auto cpuidleNameIndex = traceDataCache_->GetDataIndex(line.eventName.c_str());
    streamFilters_->cpuMeasureFilter_->AppendNewMeasureData(eventCpuValue.value(), cpuidleNameIndex, line.ts,
                                                            newStateValue.value());
    return true;
}

bool BytraceEventParser::WorkqueueExecuteStartEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    auto splitStr = GetFunctionName(line.argsStr, "function ");
    auto splitStrIndex = traceDataCache_->GetDataIndex(splitStr);
    bool result = streamFilters_->sliceFilter_->BeginSlice(line.ts, line.pid, line.pid, workQueueId_, splitStrIndex);
    if (result) {
        traceDataCache_->GetInternalSlicesData()->AppendDistributeInfo();
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_START, STAT_EVENT_RECEIVED);
        return true;
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_START, STAT_EVENT_DATA_LOST);
        return false;
    }
}

bool BytraceEventParser::WorkqueueExecuteEndEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    if (streamFilters_->sliceFilter_->EndSlice(line.ts, line.pid, line.pid)) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_END, STAT_EVENT_RECEIVED);
        return true;
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_WORKQUEUE_EXECUTE_END, STAT_EVENT_NOTMATCH);
        return false;
    }
}

bool BytraceEventParser::ProcessExitEvent(const ArgsMap& args, const BytraceLine& line) const
{
    auto comm = std::string_view(args.at("comm"));
    auto pid = base::StrToUInt32(args.at("pid"));
    if (!pid.has_value()) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_PROCESS_EXIT, STAT_EVENT_DATA_INVALID);
        return false;
    }
    auto itid = streamFilters_->processFilter_->UpdateOrCreateThreadWithName(line.ts, pid.value(), comm);
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_PROCESS_EXIT, STAT_EVENT_RECEIVED);
    if (streamFilters_->cpuFilter_->InsertProcessExitEvent(line.ts, line.cpu, itid)) {
        return true;
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_PROCESS_EXIT, STAT_EVENT_NOTMATCH);
        return false;
    }
}

bool BytraceEventParser::SetRateEvent(const ArgsMap& args, const BytraceLine& line) const
{
    auto name = std::string_view(args.at("name"));
    auto state = base::StrToInt64(args.at("state"));
    auto cpu = base::StrToUInt64(args.at("cpu_id"));
    DataIndex nameIndex = traceDataCache_->GetDataIndex(name);
    streamFilters_->clockRateFilter_->AppendNewMeasureData(cpu.value(), nameIndex, line.ts, state.value());
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CLOCK_SET_RATE, STAT_EVENT_RECEIVED);
    return true;
}

bool BytraceEventParser::ClockEnableEvent(const ArgsMap& args, const BytraceLine& line) const
{
    auto name = std::string_view(args.at("name"));
    auto state = base::StrToInt64(args.at("state"));
    auto cpuId = base::StrToUInt64(args.at("cpu_id"));
    DataIndex nameIndex = traceDataCache_->GetDataIndex(name);
    streamFilters_->clockEnableFilter_->AppendNewMeasureData(cpuId.value(), nameIndex, line.ts, state.value());
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CLOCK_ENABLE, STAT_EVENT_RECEIVED);
    return true;
}
bool BytraceEventParser::ClockDisableEvent(const ArgsMap& args, const BytraceLine& line) const
{
    auto name = std::string_view(args.at("name"));
    auto state = base::StrToInt64(args.at("state"));
    auto cpuId = base::StrToUInt64(args.at("cpu_id"));
    DataIndex nameIndex = traceDataCache_->GetDataIndex(name);
    streamFilters_->clockDisableFilter_->AppendNewMeasureData(cpuId.value(), nameIndex, line.ts, state.value());
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CLOCK_ENABLE, STAT_EVENT_RECEIVED);
    return true;
}

bool BytraceEventParser::RegulatorSetVoltageEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_SET_VOLTAGE, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_SET_VOLTAGE, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::RegulatorSetVoltageCompleteEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_SET_VOLTAGE_COMPLETE, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_SET_VOLTAGE_COMPLETE,
                                                    STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::RegulatorDisableEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_DISABLE, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_DISABLE, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::RegulatorDisableCompleteEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_DISABLE_COMPLETE, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_REGULATOR_DISABLE_COMPLETE, STAT_EVENT_NOTSUPPORTED);
    return true;
}

bool BytraceEventParser::IpiEntryEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IPI_ENTRY, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IPI_ENTRY, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::IpiExitEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IPI_EXIT, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IPI_EXIT, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::IrqHandlerEntryEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_ENTRY, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_ENTRY, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::IrqHandlerExitEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_EXIT, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_EXIT, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::SoftIrqRaiseEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}
bool BytraceEventParser::SoftIrqEntryEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}
bool BytraceEventParser::SoftIrqExitEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}
bool BytraceEventParser::BinderTransactionAllocBufEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}
bool BytraceEventParser::ParseDataItem(const BytraceLine& line, const ArgsMap& args, uint32_t tgid) const
{
    traceDataCache_->UpdateTraceTime(line.ts);
    if (tgid) {
        streamFilters_->processFilter_->GetOrCreateThreadWithPid(line.pid, tgid);
        streamFilters_->processFilter_->UpdateOrCreateThreadWithPidAndName(line.pid, tgid, line.task);
    }

    auto it = eventToFunctionMap_.find(line.eventName);
    if (it != eventToFunctionMap_.end()) {
        return it->second(args, line);
    }
    TS_LOGW("UnRecognizable event name:%s", line.eventName.c_str());
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_NOTSUPPORTED);
    return false;
}

void BytraceEventParser::ParsePrintEvent(uint64_t ts, uint32_t pid, std::string_view event) const
{
    TracePoint point;
    if (GetTracePoint(event, point) == SUCCESS) {
        ParseTracePoint(ts, pid, point);
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_RECEIVED);
    } else {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_DATA_INVALID);
    }
}

void BytraceEventParser::ParseTracePoint(uint64_t ts, uint32_t pid, TracePoint point) const
{
    switch (point.phase_) {
        case 'B': {
            if (streamFilters_->sliceFilter_->BeginSlice(ts, pid, point.tgid_, 0,
                                                         traceDataCache_->GetDataIndex(point.name_))) {
                // add distributed data
                traceDataCache_->GetInternalSlicesData()->AppendDistributeInfo(
                    point.chainId_, point.spanId_, point.parentSpanId_, point.flag_, point.args_);
            } else {
                streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_DATA_LOST);
            }
            break;
        }
        case 'E': {
            streamFilters_->sliceFilter_->EndSlice(ts, pid, point.tgid_);
            break;
        }
        case 'S': {
            auto cookie = static_cast<int64_t>(point.value_);
            streamFilters_->sliceFilter_->StartAsyncSlice(ts, pid, point.tgid_, cookie,
                                                          traceDataCache_->GetDataIndex(point.name_));
            break;
        }
        case 'F': {
            auto cookie = static_cast<int64_t>(point.value_);
            streamFilters_->sliceFilter_->FinishAsyncSlice(ts, pid, point.tgid_, cookie,
                                                           traceDataCache_->GetDataIndex(point.name_));
            break;
        }
        case 'C': {
            DataIndex nameIndex = traceDataCache_->GetDataIndex(point.name_);
            uint32_t internalPid = streamFilters_->processFilter_->GetInternalPid(point.tgid_);
            if (internalPid != INVALID_ID) {
                streamFilters_->processMeasureFilter_->AppendNewMeasureData(internalPid, nameIndex, ts, point.value_);
            } else {
                streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_DATA_INVALID);
            }
            break;
        }
        default:
            TS_LOGD("point missing!");
            break;
    }
}

ParseResult BytraceEventParser::CheckTracePoint(std::string_view pointStr) const
{
    if (pointStr.size() == 0) {
        TS_LOGD("get trace point data size is 0!");
        return ERROR;
    }

    std::string clockSyncSts = "trace_event_clock_sync";
    if (pointStr.compare(0, clockSyncSts.length(), clockSyncSts.c_str()) == 0) {
        TS_LOGD("skip trace point ：%s!", clockSyncSts.c_str());
        return ERROR;
    }

    if (pointStr.find_first_of('B') != 0 && pointStr.find_first_of('E') != 0 && pointStr.find_first_of('C') != 0 &&
        pointStr.find_first_of('S') != 0 && pointStr.find_first_of('F') != 0) {
        TS_LOGD("trace point not supported : [%c] !", pointStr[0]);
        return ERROR;
    }

    if (pointStr.find_first_of('E') != 0 && pointStr.size() == 1) {
        TS_LOGD("point string size error!");
        return ERROR;
    }

    if (pointStr.size() >= maxPointLength_) {
        if ((pointStr[1] != '|') && (pointStr[1] != '\n')) {
            TS_LOGD("not support data formart!");
            return ERROR;
        }
    }

    return SUCCESS;
}

uint32_t BytraceEventParser::GetThreadGroupId(std::string_view pointStr, size_t& length) const
{
    for (size_t i = maxPointLength_; i < pointStr.size(); i++) {
        if (pointStr[i] == '|' || pointStr[i] == '\n') {
            break;
        }

        if (pointStr[i] < '0' || pointStr[i] > '9') {
            return ERROR;
        }

        length++;
    }

    std::string str(pointStr.data() + maxPointLength_, length);
    return base::StrToUInt32(str).value_or(0);
}

std::string_view BytraceEventParser::GetPointNameForBegin(std::string_view pointStr, size_t tGidlength) const
{
    size_t index = maxPointLength_ + tGidlength + pointLength_;

    size_t length = pointStr.size() - index - ((pointStr.back() == '\n') ? 1 : 0);
    std::string_view name = std::string_view(pointStr.data() + index, length);
    return name;
}

ParseResult BytraceEventParser::HandlerB(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const
{
    outPoint.name_ = GetPointNameForBegin(pointStr, tGidlength);
    if (outPoint.name_.empty()) {
        TS_LOGD("point name is empty!");
        return ERROR;
    }
    // Use $# to differentiate distributed data
    if (outPoint.name_.find("$#") == std::string::npos) {
        return SUCCESS;
    }
    // Resolve distributed calls
    const std::regex distributeMatcher = std::regex(R"((?:^\[([a-z0-9]+),(\d+),(\d+)\]:?([CS]?)\$#)?(.*)\$#(.*)$)");
    std::smatch matcheLine;
    bool matched = std::regex_match(outPoint.name_, matcheLine, distributeMatcher);
    if (matched) {
        size_t index = 0;
        outPoint.chainId_ = matcheLine[++index].str();
        outPoint.spanId_ = matcheLine[++index].str();
        outPoint.parentSpanId_ = matcheLine[++index].str();
        outPoint.flag_ = matcheLine[++index].str();
        outPoint.name_ = matcheLine[++index].str();
        outPoint.args_ = matcheLine[++index].str();
    }
    return SUCCESS;
}

ParseResult BytraceEventParser::HandlerE(void) const
{
    return SUCCESS;
}

size_t BytraceEventParser::GetNameLength(std::string_view pointStr, size_t nameIndex) const
{
    size_t namelength = 0;
    for (size_t i = nameIndex; i < pointStr.size(); i++) {
        if (pointStr[i] == '|') {
            namelength = i - nameIndex;
            break;
        }
    }
    return namelength;
}

size_t BytraceEventParser::GetValueLength(std::string_view pointStr, size_t valueIndex) const
{
    size_t valuePipe = pointStr.find('|', valueIndex);
    size_t valueLen = pointStr.size() - valueIndex;
    if (valuePipe != std::string_view::npos) {
        valueLen = valuePipe - valueIndex;
    }

    if (valueLen == 0) {
        return 0;
    }

    if (pointStr[valueIndex + valueLen - pointLength_] == '\n') {
        valueLen--;
    }

    return valueLen;
}

ParseResult BytraceEventParser::HandlerCSF(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const
{
    // point name
    size_t nameIndex = maxPointLength_ + tGidlength + pointLength_;
    size_t namelength = GetNameLength(pointStr, nameIndex);
    if (namelength == 0) {
        TS_LOGD("point name length is error!");
        return ERROR;
    }
    outPoint.name_ = std::string_view(pointStr.data() + nameIndex, namelength);

    // point value
    size_t valueIndex = nameIndex + namelength + pointLength_;
    size_t valueLen = GetValueLength(pointStr, valueIndex);
    if (valueLen == 0) {
        TS_LOGD("point value length is error!");
        return ERROR;
    }

    std::string valueStr(pointStr.data() + valueIndex, valueLen);
    if (!base::StrToUInt64(valueStr).has_value()) {
        TS_LOGD("point value is error!");
        return ERROR;
    }
    outPoint.value_ = base::StrToUInt64(valueStr).value();

    size_t valuePipe = pointStr.find('|', valueIndex);
    if (valuePipe != std::string_view::npos) {
        size_t groupLen = pointStr.size() - valuePipe - pointLength_;
        if (groupLen == 0) {
            return ERROR;
        }

        if (pointStr[pointStr.size() - pointLength_] == '\n') {
            groupLen--;
        }

        outPoint.categoryGroup_ = std::string_view(pointStr.data() + valuePipe + 1, groupLen);
    }

    return SUCCESS;
}

ParseResult BytraceEventParser::GetTracePoint(std::string_view pointStr, TracePoint& outPoint) const
{
    if (CheckTracePoint(pointStr) != SUCCESS) {
        return ERROR;
    }

    size_t tGidlength = 0;

    outPoint.phase_ = pointStr.front();
    outPoint.tgid_ = GetThreadGroupId(pointStr, tGidlength);

    ParseResult ret = ERROR;
    switch (outPoint.phase_) {
        case 'B': {
            ret = HandlerB(pointStr, outPoint, tGidlength);
            break;
        }
        case 'E': {
            ret = HandlerE();
            break;
        }
        case 'S':
        case 'F':
        case 'C': {
            ret = HandlerCSF(pointStr, outPoint, tGidlength);
            break;
        }
        default:
            return ERROR;
    }
    return ret;
}
} // namespace TraceStreamer
} // namespace SysTuning

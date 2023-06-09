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
#include "binder_filter.h"
#include "cpu_filter.h"
#include "filter_filter.h"
#include "irq_filter.h"
#include "measure_filter.h"
#include "parting_string.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "stat_filter.h"
#include "string_to_numerical.h"
#include "thread_state.h"
#include "ts_common.h"
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
      printEventParser_(traceDataCache_, streamFilters_)
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
        {config_.eventNameMap_.at(TRACE_EVENT_BINDER_TRANSACTION),
         bind(&BytraceEventParser::BinderTransaction, this, std::placeholders::_1, std::placeholders::_2)},
        {config_.eventNameMap_.at(TRACE_EVENT_BINDER_TRANSACTION_RECEIVED),
         bind(&BytraceEventParser::BinderTransactionReceived, this, std::placeholders::_1, std::placeholders::_2)},
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
    auto threadState = ThreadState(prevStateStr.c_str());
    uint64_t prevState = threadState.State();
    if (!threadState.IsValid()) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_SWITCH, STAT_EVENT_DATA_INVALID);
    }
    auto nextInternalTid = 0;
    auto uprevtid = 0;
    if (streamFilters_->processFilter_->isThreadNameEmpty(nextPidValue.value())) {
        nextInternalTid =
            streamFilters_->processFilter_->UpdateOrCreateThreadWithName(line.ts, nextPidValue.value(), nextCommStr);
    } else {
        nextInternalTid = streamFilters_->processFilter_->UpdateOrCreateThread(line.ts, nextPidValue.value());
    }
    if (streamFilters_->processFilter_->isThreadNameEmpty(prevPidValue.value())) {
        uprevtid =
            streamFilters_->processFilter_->UpdateOrCreateThreadWithName(line.ts, prevPidValue.value(), prevCommStr);
    } else {
        uprevtid = streamFilters_->processFilter_->UpdateOrCreateThread(line.ts, prevPidValue.value());
    }
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
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TASK_NEWTASK, STAT_EVENT_RECEIVED);
    // the clone flag from txt trace from kernel original is HEX, but when it is converted from proto
    // based trace, it will be OCT number, it is not stable, so we decide to ignore it
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TASK_NEWTASK, STAT_EVENT_NOTSUPPORTED);
    return true;
}

bool BytraceEventParser::TracingMarkWriteEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    printEventParser_.ParsePrintEvent(line.ts, line.pid, line.argsStr.c_str());
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
    auto wakePidStr = std::string_view(args.at("comm"));
    if (!wakePidValue.has_value()) {
        TS_LOGD("Failed to convert wake_pid");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SCHED_WAKING, STAT_EVENT_DATA_INVALID);
        return false;
    }
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_waking"));
    auto instants = traceDataCache_->GetInstantsData();
    DataIndex wakePidStrIndex = traceDataCache_->GetDataIndex(wakePidStr);
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThreadWithNameIndex(line.ts, 
                                                                                                wakePidValue.value(),
                                                                                                wakePidStrIndex);
    streamFilters_->cpuFilter_->InsertWakeupEvent(line.ts, internalTid);
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
    bool result = streamFilters_->sliceFilter_->BeginSlice(line.ts, line.pid, 0, workQueueId_, splitStrIndex);
    traceDataCache_->GetInternalSlicesData()->AppendDistributeInfo();
    if (result) {
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
    if (streamFilters_->sliceFilter_->EndSlice(line.ts, line.pid, 0)) {
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
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CLOCK_DISABLE, STAT_EVENT_RECEIVED);
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
    auto name = std::string_view(args.at("name"));
    streamFilters_->irqFilter_->IrqHandlerEntry(line.ts, line.cpu, traceDataCache_->GetDataIndex(name));
    return true;
}
bool BytraceEventParser::IrqHandlerExitEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_EXIT, STAT_EVENT_RECEIVED);
    uint32_t ret = (args.at("ret") == "handled") ? 1 : 0;
    streamFilters_->irqFilter_->IrqHandlerExit(line.ts, line.cpu, ret);
    return true;
}
bool BytraceEventParser::SoftIrqRaiseEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_SOFTIRQ_RAISE, STAT_EVENT_RECEIVED);
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_SOFTIRQ_RAISE, STAT_EVENT_NOTSUPPORTED);
    return true;
}
bool BytraceEventParser::SoftIrqEntryEvent(const ArgsMap& args, const BytraceLine& line) const
{
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_SOFTIRQ_ENTRY, STAT_EVENT_RECEIVED);
    auto vec = base::StrToUInt32(args.at("vec"));
    streamFilters_->irqFilter_->SoftIrqEntry(line.ts, line.cpu, vec.value());
    return true;
}
bool BytraceEventParser::SoftIrqExitEvent(const ArgsMap& args, const BytraceLine& line) const
{
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_SOFTIRQ_EXIT, STAT_EVENT_RECEIVED);
    auto vec = base::StrToUInt32(args.at("vec"));
    streamFilters_->irqFilter_->SoftIrqExit(line.ts, line.cpu, vec.value());
    return true;
}

bool BytraceEventParser::BinderTransaction(const ArgsMap& args, const BytraceLine& line) const
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_BINDER_TRANSACTION, STAT_EVENT_RECEIVED);
    auto transactionId = base::StrToInt64(args.at("transaction"));
    auto destNode = base::StrToUInt32(args.at("dest_node"));
    auto destProc = base::StrToUInt32(args.at("dest_proc"));
    auto destThread = base::StrToUInt32(args.at("dest_thread"));
    auto isReply = base::StrToUInt32(args.at("reply"));
    auto flags = base::StrToUInt32(args.at("flags"), base::INTEGER_RADIX_TYPE_HEX);
    auto codeStr = base::StrToUInt32(args.at("code"), base::INTEGER_RADIX_TYPE_HEX);
    TS_LOGD("ts:%lu, pid:%u, destNode:%u, destTgid:%u, destTid:%u, transactionId:%lu, isReply:%u flags:%u, code:%u",
            line.ts, line.pid, destNode.value(), destProc.value(), destThread.value(), transactionId.value(),
            isReply.value(), flags.value(), codeStr.value());
    streamFilters_->binderFilter_->SendTraction(line.ts, line.pid, transactionId.value(), destNode.value(),
                                                destProc.value(), destThread.value(), isReply.value(), flags.value(),
                                                codeStr.value());
    return true;
}
bool BytraceEventParser::BinderTransactionReceived(const ArgsMap& args, const BytraceLine& line) const
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_BINDER_TRANSACTION_RECEIVED, STAT_EVENT_RECEIVED);
    auto transactionId = base::StrToInt64(args.at("transaction"));
    streamFilters_->binderFilter_->ReceiveTraction(line.ts, line.pid, transactionId.value());
    TS_LOGD("ts:%lu, pid:%u, transactionId:%lu", line.ts, line.pid, transactionId.value());
    return true;
}
bool BytraceEventParser::BinderTransactionAllocBufEvent(const ArgsMap& args, const BytraceLine& line) const
{
    UNUSED(args);
    UNUSED(line);
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_BINDER_TRANSACTION_ALLOC_BUF, STAT_EVENT_RECEIVED);
    auto dataSize = base::StrToUInt64(args.at("data_size"));
    auto offsetsSize = base::StrToUInt64(args.at("offsets_size"));
    streamFilters_->binderFilter_->TransactionAllocBuf(line.ts, line.pid, dataSize.value(), offsetsSize.value());
    TS_LOGD("dataSize:%lu, offsetSize:%lu", dataSize.value(), offsetsSize.value());
    return true;
}
bool BytraceEventParser::ParseDataItem(const BytraceLine& line, const ArgsMap& args, uint32_t tgid) const
{
    traceDataCache_->UpdateTraceTime(line.ts);
    if (tgid) {
        streamFilters_->processFilter_->UpdateOrCreateThreadWithPidAndName(line.pid, tgid, line.task);
    } else {
        // When tgid is zero, only use tid create thread
        streamFilters_->processFilter_->GetOrCreateThreadWithPid(line.pid, tgid);
    }

    auto it = eventToFunctionMap_.find(line.eventName);
    if (it != eventToFunctionMap_.end()) {
        return it->second(args, line);
    }
    TS_LOGW("UnRecognizable event name:%s", line.eventName.c_str());
    traceDataCache_->GetStatAndInfo()->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_NOTSUPPORTED);
    return false;
}
} // namespace TraceStreamer
} // namespace SysTuning

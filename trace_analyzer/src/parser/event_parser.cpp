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

#include "event_parser.h"
#include <string>
#include <unordered_map>

#include "cpu_filter.h"
#include "event_filter.h"
#include "filter_filter.h"
#include "measure_filter.h"
#include "parting_string.h"
#include "process_filter.h"
#include "slice_filter.h"
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

EventParser::EventParser(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : streamFilters_(filter),
      ioWaitId_(dataCache->GetDataIndex("io_wait")),
      workQueueId_(dataCache->GetDataIndex("workqueue")),
      schedWakeupId_(dataCache->GetDataIndex("sched_wakeup")),
      schedBlockedReasonId_(dataCache->GetDataIndex("sched_blocked_reason")),
      binderId_(dataCache->GetDataIndex("binder")),
      pointLength_(1),
      maxPointLength_(2),
      byHex_(16),
      traceDataCache_(dataCache)
{
    eventToFunction_ = {
        {"binder_transaction",
         bind(&EventParser::BinderTransactionEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"binder_transaction_received",
         bind(&EventParser::BinderTransactionReceivedEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"binder_lock", bind(&EventParser::BinderLockEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"binder_locked", bind(&EventParser::BinderLockedEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"binder_unlock", bind(&EventParser::BinderUnLockEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"binder_transaction_alloc_buf",
         bind(&EventParser::BinderTransactionAllocBufEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"sched_switch", bind(&EventParser::SchedSwitchEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"task_rename", bind(&EventParser::TaskRenameEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"task_newtask", bind(&EventParser::TaskNewtaskEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"tracing_mark_write",
         bind(&EventParser::TracingMarkWriteEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"0", bind(&EventParser::TracingMarkWriteEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"print", bind(&EventParser::TracingMarkWriteEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"sched_wakeup", bind(&EventParser::SchedWakeupEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"sched_waking", bind(&EventParser::SchedWakingEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"cpu_idle", bind(&EventParser::CpuIdleEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"cpu_frequency", bind(&EventParser::CpuFrequencyEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"workqueue_execute_start",
         bind(&EventParser::WorkqueueExecuteStartEvent, this, std::placeholders::_1, std::placeholders::_2)},
        {"workqueue_execute_end",
         bind(&EventParser::WorkqueueExecuteEndEvent, this, std::placeholders::_1, std::placeholders::_2)}
    };
}

bool EventParser::BinderTransactionEvent(ArgsMap args, const BytraceLine line) const
{
    auto transId = base::StrToInt32(args["transaction"]);
    if (!transId.has_value()) {
        TUNING_LOGD("Failed to convert transaction id");
        return false;
    }
    auto destNode = base::StrToInt32(args["dest_node"]);
    if (!destNode.has_value()) {
        TUNING_LOGD("Failed to convert dest_node");
        return false;
    }
    auto destTgid = base::StrToInt32(args["dest_proc"]);
    if (!destTgid.has_value()) {
        TUNING_LOGD("Failed to convert dest_tgid");
        return false;
    }
    auto destTid = base::StrToInt32(args["dest_thread"]);
    if (!destTid.has_value()) {
        TUNING_LOGD("Failed to convert dest_tid");
        return false;
    }

    auto isReply = (base::StrToInt32(args["reply"]).value() == 1);
    uint32_t flags = base::StrToUInt32(args["flags"], byHex_).value();

    std::string codeStr = args["code"] + " Java Layer Dependent";
    DataIndex code = traceDataCache_->GetDataIndex(std::string_view(codeStr));

    struct BinderParamter binderParamter;
    binderParamter.ts = line.ts;
    binderParamter.tid = line.pid;
    binderParamter.transactionId = transId.value();
    binderParamter.destNode = destNode.value();
    binderParamter.destTgid = destTgid.value();
    binderParamter.destTid = destTid.value();
    binderParamter.isReply = isReply;
    binderParamter.flags = flags;
    binderParamter.code = code;
    binderParamter.nameIndex = traceDataCache_->GetDataIndex(line.eventName.c_str());

    streamFilters_->eventFilter_->BinderTransaction(binderParamter);
    return true;
}

bool EventParser::BinderTransactionReceivedEvent(ArgsMap args, const BytraceLine line) const
{
    auto transId = base::StrToInt32(args["transaction"]);
    if (!transId.has_value()) {
        TUNING_LOGD("Failed to convert transaction id");
        return false;
    }

    streamFilters_->eventFilter_->BinderTransactionReceived(line.ts, line.pid, transId.value());
    return true;
}

bool EventParser::BinderLockEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}

bool EventParser::BinderLockedEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}

bool EventParser::BinderUnLockEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(args);
    UNUSED(line);
    return true;
}

bool EventParser::BinderTransactionAllocBufEvent(ArgsMap args, const BytraceLine line) const
{
    auto dataSize = base::StrToUInt64(args["data_size"]);
    if (!dataSize.has_value()) {
        TUNING_LOGD("Failed to parse data_size");
        return false;
    }
    auto offsetsSize = base::StrToUInt64(args["offsets_size"]);
    if (!offsetsSize.has_value()) {
        TUNING_LOGD("Failed to parse offsets_size");
        return false;
    }

    UNUSED(line);
    return true;
}

bool EventParser::SchedSwitchEvent(ArgsMap args, const BytraceLine line) const
{
    auto prevCommStr = std::string_view(args["prev_comm"]);
    auto nextCommStr = std::string_view(args["next_comm"]);
    auto prevPrioValue = base::StrToInt32(args["prev_prio"]);
    auto nextPrioValue = base::StrToInt32(args["next_prio"]);
    auto prevPidValue = base::StrToUInt32(args["prev_pid"]);
    auto nextPidValue = base::StrToUInt32(args["next_pid"]);
    if (!(prevPidValue.has_value() && prevPrioValue.has_value() && nextPidValue.has_value() &&
        nextPrioValue.has_value())) {
        TUNING_LOGD("Failed to parse sched_switch event");
        return false;
    }

    auto prevStateStr = args["prev_state"];
    uint64_t prevState = ThreadState(prevStateStr.c_str()).State();
    streamFilters_->eventFilter_->UpdateSchedSwitch(line.cpu, line.ts, prevPidValue.value(),
                                            nextPidValue.value(), nextCommStr);
    auto unexttid = streamFilters_->processFilter_->UpdateThreadByName(line.ts, nextPidValue.value(), nextCommStr);
    auto uprevtid = streamFilters_->processFilter_->UpdateThreadByName(line.ts, prevPidValue.value(), prevCommStr);
    streamFilters_->cpuFilter_->InsertSwitchEvent(line.ts, line.cpu, uprevtid,
            static_cast<uint64_t>(prevPrioValue.value()), prevState, unexttid,
            static_cast<uint64_t>(nextPrioValue.value()));
    return true;
}

bool EventParser::TaskRenameEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(line);
    auto prevCommStr = std::string_view(args["newcomm"]);
    auto pidValue = base::StrToUInt32(args["pid"]);
    streamFilters_->processFilter_->UpdateProcess(pidValue.value(), prevCommStr);
    return true;
}

bool EventParser::TaskNewtaskEvent(ArgsMap args, const BytraceLine line) const
{
    auto commonStr = std::string_view(args["comm"]);
    auto pidValue = base::StrToUInt32(args["pid"]);

    uint32_t ftracePid = 0;
    if (!line.tGidStr.empty() && line.tGidStr != "-----") {
        std::optional<uint32_t> tgid = base::StrToUInt32(line.tGidStr);
        if (tgid) {
            ftracePid = tgid.value();
        }
    }

    static const uint32_t threadPid = 2;
    static const uint32_t cloneThread = 0x00010000;
    auto cloneFlags = base::StrToUInt64(args["clone_flags"], byHex_).value();
    if ((cloneFlags & cloneThread) == 0 && ftracePid != threadPid) {
        streamFilters_->processFilter_->UpdateProcess(static_cast<uint32_t>(pidValue.value()), commonStr);
    } else if (ftracePid == threadPid) {
        streamFilters_->processFilter_->SetThreadPid(static_cast<uint32_t>(pidValue.value()), threadPid);
    }
    return true;
}

bool EventParser::TracingMarkWriteEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(args);
    ParsePrintEvent(line.ts, line.pid, line.argsStr.c_str());
    return true;
}

bool EventParser::SchedWakeupEvent(ArgsMap args, const BytraceLine line) const
{
    std::optional<uint32_t> wakePidValue = base::StrToUInt32(args["pid"]);
    if (!wakePidValue.has_value()) {
        TUNING_LOGD("Failed to convert wake_pid");
        return false;
    }
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_wakeup"));
    auto* instants = traceDataCache_->GetInstantsData();
    InternalTid internalTid = streamFilters_->processFilter_->SetThreadPid(line.ts, line.pid, 0);
    instants->AppendInstantEventData(line.ts, name, internalTid);
    std::optional<uint32_t> targetCpu = base::StrToUInt32(args["target_cpu"]);
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, line.ts, RAW_SCHED_WAKEUP, targetCpu.value(), internalTid);
    }
    return true;
}

bool EventParser::SchedWakingEvent(ArgsMap args, const BytraceLine line) const
{
    std::optional<uint32_t> wakePidValue = base::StrToUInt32(args["pid"]);
    if (!wakePidValue.has_value()) {
        TUNING_LOGD("Failed to convert wake_pid");
        return false;
    }
    DataIndex name = traceDataCache_->GetDataIndex(std::string_view("sched_waking"));
    auto* instants = traceDataCache_->GetInstantsData();
    InternalTid internalTid = streamFilters_->processFilter_->SetThreadPid(line.ts, line.pid, 0);
    instants->AppendInstantEventData(line.ts, name, internalTid);
    streamFilters_->cpuFilter_->InsertWakeingEvent(line.ts, internalTid);
    std::optional<uint32_t> targetCpu = base::StrToUInt32(args["target_cpu"]);
    if (targetCpu.has_value()) {
        traceDataCache_->GetRawData()->AppendRawData(0, line.ts, RAW_SCHED_WAKING, targetCpu.value(), internalTid);
    }
    return true;
}

bool EventParser::CpuIdleEvent(ArgsMap args, const BytraceLine line) const
{
    std::optional<uint32_t> eventCpuValue = base::StrToUInt32(args["cpu_id"]);
    std::optional<double> newStateValue = base::StrToDouble(args["state"]);
    if (!eventCpuValue.has_value()) {
        TUNING_LOGD("Failed to convert event cpu");
        return false;
    }
    if (!newStateValue.has_value()) {
        TUNING_LOGD("Failed to convert state");
        return false;
    }
    auto cpuIdleNameIndex = traceDataCache_->GetDataIndex(line.eventName.c_str());
    uint32_t filterId =
        streamFilters_->cpuCounterFilter_->GetOrCreateCertainFilterId(eventCpuValue.value(), cpuIdleNameIndex);
    traceDataCache_->GetCounterData()->AppendCounterData(0, line.ts, newStateValue.value(), filterId);
    traceDataCache_->GetRawData()->AppendRawData(0, line.ts, RAW_CPU_IDLE, eventCpuValue.value(), 0);
    return true;
}

bool EventParser::CpuFrequencyEvent(ArgsMap args, const BytraceLine line) const
{
    std::optional<double> newStateValue = base::StrToDouble(args["state"]);
    std::optional<uint32_t> eventCpuValue = base::StrToUInt32(args["cpu_id"]);

    if (!newStateValue.has_value()) {
        TUNING_LOGD("Failed to convert state");
        return false;
    }
    if (!eventCpuValue.has_value()) {
        TUNING_LOGD("Failed to convert event cpu");
        return false;
    }

    auto cpuidleNameIndex = traceDataCache_->GetDataIndex(line.eventName.c_str());
    uint32_t filterId =
        streamFilters_->cpuCounterFilter_->GetOrCreateCertainFilterId(eventCpuValue.value(), cpuidleNameIndex);
    traceDataCache_->GetCounterData()->AppendCounterData(0, line.ts, newStateValue.value(), filterId);
    return true;
}

bool EventParser::WorkqueueExecuteStartEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(args);
    auto splitStr = GetFunctionName(line.argsStr, "function ");
    DataIndex nameIndex = traceDataCache_->GetDataIndex(std::string_view(splitStr));
    streamFilters_->sliceFilter_->BeginSlice(line.ts, line.pid, line.pid, workQueueId_, nameIndex);
    return true;
}

bool EventParser::WorkqueueExecuteEndEvent(ArgsMap args, const BytraceLine line) const
{
    UNUSED(args);
    streamFilters_->sliceFilter_->EndSlice(line.ts, line.pid, line.pid);
    return true;
}

bool EventParser::ParseLine(const BytraceLine& line) const
{
    ArgsMap args;
    traceDataCache_->UpdateBoundTime(line.ts);
    if (!line.tGidStr.empty() && line.tGidStr != "-----") {
        std::optional<uint32_t> tgid = base::StrToUInt32(line.tGidStr);
        if (tgid) {
            streamFilters_->processFilter_->SetThreadPid(line.pid, tgid.value());
            streamFilters_->processFilter_->SetThreadName(line.pid, tgid.value_or(0), std::string_view(line.task));
        }
    }

    for (base::PartingString ss(line.argsStr, ' '); ss.Next();) {
        std::string key;
        std::string value;
        if (!(std::string(ss.GetCur()).find("=") != std::string::npos)) {
            key = "name";
            value = ss.GetCur();
            args.emplace(std::move(key), std::move(value));
            continue;
        }
        for (base::PartingString inner(ss.GetCur(), '='); inner.Next();) {
            if (key.empty()) {
                key = inner.GetCur();
            } else {
                value = inner.GetCur();
            }
        }
        args.emplace(std::move(key), std::move(value));
    }

    auto it = eventToFunction_.find(line.eventName);
    if (it != eventToFunction_.end()) {
        return it->second(args, line);
    }
    return true;
}

void EventParser::ParsePrintEvent(uint64_t ts, uint32_t pid, std::string_view event) const
{
    TracePoint point;
    if (GetTracePoint(event, point) == SUCCESS) {
        ParseTracePoint(ts, pid, point);
    }
}

void EventParser::ParseTracePoint(uint64_t ts, uint32_t pid, TracePoint point) const
{
    switch (point.phase_) {
        case 'B': {
            DataIndex nameIndex = traceDataCache_->GetDataIndex(point.name_);
            streamFilters_->sliceFilter_->BeginSlice(ts, pid, point.tgid_, 0, nameIndex);
            break;
        }
        case 'E': {
            streamFilters_->sliceFilter_->EndSlice(ts, pid, point.tgid_);
            break;
        }
        case 'S': {
            DataIndex nameIndex = traceDataCache_->GetDataIndex(point.name_);
            auto cookie = static_cast<int64_t>(point.value_);
            streamFilters_->sliceFilter_->AsyncBeginSlice(ts, pid, point.tgid_, cookie, nameIndex);
            break;
        }
        case 'F': {
            auto cookie = static_cast<int64_t>(point.value_);
            streamFilters_->sliceFilter_->AsyncEndSlice(ts, pid, cookie);
            break;
        }
        case 'C': {
            if (point.name_ == "ScreenState") {
                return;
            }

            DataIndex nameIndex = traceDataCache_->GetDataIndex(point.name_);
            uint32_t internalPid = streamFilters_->processFilter_->UpdateProcess(point.tgid_, point.name_);
            auto filterId = streamFilters_->processCounterFilter_->GetOrCreateCertainFilterId(internalPid, nameIndex);
            traceDataCache_->GetCounterData()->AppendCounterData(0, ts, point.value_, filterId);
            break;
        }
        default:
            TUNING_LOGD("point missing!");
            break;
    }
}

ParseResult EventParser::CheckTracePoint(std::string_view pointStr) const
{
    if (pointStr.size() == 0) {
        TUNING_LOGD("get trace point data size is 0!");
        return ERROR;
    }

    std::string clockSyncSts = "trace_event_clock_sync";
    if (pointStr.compare(0, clockSyncSts.length(), clockSyncSts.c_str()) == 0) {
        TUNING_LOGD("skip trace point ：%s!", clockSyncSts.c_str());
        return ERROR;
    }

    if (pointStr.find_first_of('B') != 0 && pointStr.find_first_of('E') != 0 && pointStr.find_first_of('C') != 0 &&
        pointStr.find_first_of('S') != 0 && pointStr.find_first_of('F') != 0) {
        TUNING_LOGD("trace point not supported : [%c] !", pointStr[0]);
        return ERROR;
    }

    if (pointStr.find_first_of('E') != 0 && pointStr.size() == 1) {
        TUNING_LOGD("point string size error!");
        return ERROR;
    }

    if (pointStr.size() >= maxPointLength_) {
        if ((pointStr[1] != '|') && (pointStr[1] != '\n')) {
            TUNING_LOGD("not support data formart!");
            return ERROR;
        }
    }

    return SUCCESS;
}

uint32_t EventParser::GetThreadGroupId(std::string_view pointStr, size_t& length) const
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

std::string_view EventParser::GetPointNameForBegin(std::string_view pointStr, size_t tGidlength) const
{
    size_t index = maxPointLength_ + tGidlength + pointLength_;
    size_t length = pointStr.size() - index - ((pointStr.back() == '\n') ? 1 : 0);
    std::string_view name = std::string_view(pointStr.data() + index, length);
    return name;
}

ParseResult EventParser::HandlerB(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const
{
    outPoint.name_ = GetPointNameForBegin(pointStr, tGidlength);
    if (outPoint.name_.empty()) {
        TUNING_LOGD("point name is empty!");
        return ERROR;
    }
    return SUCCESS;
}

ParseResult EventParser::HandlerE(void) const
{
    return SUCCESS;
}

size_t EventParser::GetNameLength(std::string_view pointStr, size_t nameIndex) const
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

size_t EventParser::GetValueLength(std::string_view pointStr, size_t valueIndex) const
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

ParseResult EventParser::HandlerCSF(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const
{
    // point name
    size_t nameIndex = maxPointLength_ + tGidlength + pointLength_;
    size_t namelength = GetNameLength(pointStr, nameIndex);
    if (namelength == 0) {
        TUNING_LOGD("point name length is error!");
        return ERROR;
    }
    outPoint.name_ = std::string_view(pointStr.data() + nameIndex, namelength);

    // point value
    size_t valueIndex = nameIndex + namelength + pointLength_;
    size_t valueLen = GetValueLength(pointStr, valueIndex);
    if (valueLen == 0) {
        TUNING_LOGD("point value length is error!");
        return ERROR;
    }

    std::string valueStr(pointStr.data() + valueIndex, valueLen);
    if (!base::StrToDouble(valueStr).has_value()) {
        TUNING_LOGD("point value is error!");
        return ERROR;
    }
    outPoint.value_ = base::StrToDouble(valueStr).value();

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

ParseResult EventParser::GetTracePoint(std::string_view pointStr, TracePoint& outPoint) const
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

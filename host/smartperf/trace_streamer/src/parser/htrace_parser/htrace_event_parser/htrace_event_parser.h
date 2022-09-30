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
#ifndef HTRACE_EVENT_PARSER_H
#define HTRACE_EVENT_PARSER_H
#include <cstdint>
#include <functional>
#include <limits>
#include <stdexcept>
#include <string>
#include <unordered_set>
#include <vector>

#include "event_parser_base.h"
#include "google/protobuf/message_lite.h"
#include "log.h"
#include "print_event_parser.h"
#include "trace_data/trace_data_cache.h"
#include "trace_plugin_result.pb.h"
#include "trace_streamer_filters.h"
#include "ts_common.h"

namespace SysTuning {
namespace TraceStreamer {
using namespace google::protobuf;
class HtraceEventParser : private EventParserBase {
public:
    HtraceEventParser(TraceDataCache* dataCache, const TraceStreamerFilters* filter);
    ~HtraceEventParser();
    void ParseDataItem(const FtraceCpuDetailMsg* cpuDetail, BuiltinClocks clock);
    void FilterAllEvents();
private:
    void DealEvent(const FtraceEvent& event);
    bool BinderTractionEvent(const MessageLite& event) const;
    bool BinderTractionReceivedEvent(const MessageLite& event) const;
    bool BinderTractionAllocBufEvent(const MessageLite& event) const;
    bool BinderTractionLockEvent(const MessageLite& event) const;
    bool BinderTractionLockedEvent(const MessageLite& event) const;
    bool BinderTractionUnLockEvent(const MessageLite& event) const;
    bool SchedSwitchEvent(const MessageLite& event);
    bool ProcessExitEvent(const MessageLite& event) const;
    bool ProcessFreeEvent(const MessageLite& event) const;
    bool TaskRenameEvent(const MessageLite& event) const;
    bool TaskNewtaskEvent(const MessageLite& event) const;
    bool ParsePrintEvent(const MessageLite& event);
    bool SchedWakeupEvent(const MessageLite& event) const;
    bool SchedWakeupNewEvent(const MessageLite& event) const;
    bool SchedWakingEvent(const MessageLite& event) const;
    bool CpuIdleEvent(const MessageLite& event) const;
    bool CpuFrequencyEvent(const MessageLite& event) const;
    bool SuspendResumeEvent(const MessageLite& event) const;
    bool WorkqueueExecuteStartEvent(const MessageLite& event) const;
    bool WorkqueueExecuteEndEvent(const MessageLite& event) const;
    bool ClockSetRateEvent(const MessageLite& event) const;
    bool ClockEnableEvent(const MessageLite& event) const;
    bool ClockDisableEvent(const MessageLite& event) const;
    bool ClkSetRateEvent(const MessageLite& event) const;
    bool ClkEnableEvent(const MessageLite& event) const;
    bool ClkDisableEvent(const MessageLite& event) const;
    bool IrqHandlerEntryEvent(const MessageLite& event) const;
    bool IrqHandlerExitEvent(const MessageLite& event) const;
    bool SoftIrqEntryEvent(const MessageLite& event) const;
    bool SoftIrqRaiseEvent(const MessageLite& event) const;
    bool SoftIrqExitEvent(const MessageLite& event) const;
    bool SysEnterEvent(const MessageLite& event) const;
    bool SysExitEvent(const MessageLite& event) const;
    bool OomScoreAdjUpdate(const MessageLite& event) const;
    bool SignalGenerateEvent(const MessageLite& event) const;
    bool SignalDeleverEvent(const MessageLite& event) const;
    bool InvokeFunc(const SupportedTraceEventType& eventType, const MessageLite& msgBase);
    struct EventInfo {
        uint64_t eventTimestamp;
        uint32_t eventCpu;
        uint32_t eventPid;
        uint32_t eventTid;
        FtraceEvent cpuDetail;
    };
    using FuncCall = std::function<bool(const MessageLite& event)>;
    uint32_t eventCpu_ = INVALID_UINT32;
    uint64_t eventTimestamp_ = INVALID_UINT64;
    uint32_t eventPid_ = INVALID_UINT32;
    uint32_t eventTid_ = INVALID_UINT32;
    std::map<std::string, FuncCall> eventToFunctionMap_ = {};
    std::unordered_set<uint32_t> tids_ = {};
    std::unordered_set<uint32_t> pids_ = {};
    DataIndex workQueueId_ = 0;
    PrintEventParser printEventParser_;
    uint64_t lastOverwrite_ = 0;
    uint64_t ftraceStartTime_ = std::numeric_limits<uint64_t>::max();
    uint64_t ftraceEndTime_ = 0;
    std::vector<EventInfo> eventList_ = {};
    const DataIndex signalGenerateId_ = traceDataCache_->GetDataIndex("signal_generate");
    const DataIndex signalDeliverId_ = traceDataCache_->GetDataIndex("signal_deliver");
    const DataIndex schedWakeupName_ = traceDataCache_->GetDataIndex("sched_wakeup");
    const DataIndex schedWakingName_ = traceDataCache_->GetDataIndex("sched_waking");
    const DataIndex schedWakeupNewName_ = traceDataCache_->GetDataIndex("sched_wakeup_new");
    const DataIndex cpuIdleName_ = traceDataCache_->GetDataIndex("cpu_idle");
    const DataIndex cpuFrequencyName_ = traceDataCache_->GetDataIndex("cpu_frequency");
    const DataIndex sysEnterName_ = traceDataCache_->GetDataIndex("sys_enter");
    const DataIndex sysExitName_ = traceDataCache_->GetDataIndex("sys_exit");
    const DataIndex oomScoreAdjName_ = traceDataCache_->GetDataIndex("oom_score_adj");
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_EVENT_PARSER_H_

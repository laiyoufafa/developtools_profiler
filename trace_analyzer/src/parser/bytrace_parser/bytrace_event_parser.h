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

#ifndef SRC_BYTRACE_EVENT_PARSER_H
#define SRC_BYTRACE_EVENT_PARSER_H

#include <functional>

#include "common_types.h"
#include "event_parser_base.h"
#include "trace_data_cache.h"
#include "trace_streamer_cfg.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
using ArgsMap = std::unordered_map<std::string, std::string>;
class BytraceEventParser : private EventParserBase {
public:
    BytraceEventParser(TraceDataCache* dataCache, const TraceStreamerFilters* filter);
    bool ParseDataItem(const BytraceLine& line, const ArgsMap& args, uint32_t tgid) const;

private:
    using FuncCall = std::function<bool(const ArgsMap& args, const BytraceLine line)>;
    bool SchedSwitchEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool TaskRenameEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool TaskNewtaskEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool TracingMarkWriteEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool SchedWakeupEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool SchedWakingEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool CpuIdleEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool CpuFrequencyEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool WorkqueueExecuteStartEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool WorkqueueExecuteEndEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool ProcessExitEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool SetRateEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool ClockEnableEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool ClockDisableEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool RegulatorSetVoltageEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool RegulatorSetVoltageCompleteEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool RegulatorDisableEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool RegulatorDisableCompleteEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool IpiEntryEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool IpiExitEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool IrqHandlerEntryEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool IrqHandlerExitEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool SoftIrqRaiseEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool SoftIrqEntryEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool SoftIrqExitEvent(const ArgsMap& args, const BytraceLine& line) const;
    bool BinderTransactionAllocBufEvent(const ArgsMap& args, const BytraceLine& line) const;

    void ParsePrintEvent(uint64_t ts, uint32_t pid, std::string_view event) const;
    void ParseTracePoint(uint64_t ts, uint32_t pid, TracePoint point) const;
    ParseResult GetTracePoint(std::string_view str, TracePoint& out) const;
    ParseResult CheckTracePoint(std::string_view pointStr) const;
    uint32_t GetThreadGroupId(std::string_view pointStr, size_t& length) const;
    std::string_view GetPointNameForBegin(std::string_view pointStr, size_t tGidlength) const;
    ParseResult HandlerB(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const;
    ParseResult HandlerE(void) const;
    ParseResult HandlerCSF(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const;
    size_t GetNameLength(std::string_view pointStr, size_t nameIndex) const;
    size_t GetValueLength(std::string_view pointStr, size_t valueIndex) const;

private:
    const DataIndex ioWaitId_;
    const DataIndex workQueueId_;
    const DataIndex schedWakeupId_;
    const DataIndex schedBlockedReasonId_;
    const uint32_t pointLength_;
    const uint32_t maxPointLength_;
    const int byHex_;
    std::map<std::string, FuncCall> eventToFunctionMap_{};
    const unsigned int MIN_SCHED_ARGS_COUNT = 6;
    const unsigned int MIN_SCHED_WAKEUP_ARGS_COUNT = 2;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // SRC_BYTRACE_EVENT_PARSER_H

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

#ifndef SRC_EVENT_PARSER_H
#define SRC_EVENT_PARSER_H

#include <functional>

#include "common_types.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
class EventParser {
public:
    explicit EventParser(TraceDataCache*, const TraceStreamerFilters*);
    bool ParseLine(const BytraceLine&) const;

private:
    const TraceStreamerFilters *streamFilters_;
    const DataIndex ioWaitId_;
    const DataIndex workQueueId_;
    const DataIndex schedWakeupId_;
    const DataIndex schedBlockedReasonId_;
    const DataIndex binderId_;
    const uint32_t pointLength_;
    const uint32_t maxPointLength_;
    const int byHex_;

    using ArgsMap = std::unordered_map<std::string, std::string>;
    using FuncCall = std::function<bool(ArgsMap args, const BytraceLine line)>;
    std::map<std::string, FuncCall> eventToFunction_ {};

    bool BinderTransactionEvent(ArgsMap args, const BytraceLine line) const;
    bool BinderTransactionReceivedEvent(ArgsMap args, const BytraceLine line) const;
    bool BinderLockEvent(ArgsMap args, const BytraceLine line) const;
    bool BinderLockedEvent(ArgsMap args, const BytraceLine line) const;
    bool BinderUnLockEvent(ArgsMap args, const BytraceLine line) const;
    bool BinderTransactionAllocBufEvent(ArgsMap args, const BytraceLine line) const;
    bool SchedSwitchEvent(ArgsMap args, const BytraceLine line) const;
    bool TaskRenameEvent(ArgsMap args, const BytraceLine line) const;
    bool TaskNewtaskEvent(ArgsMap args, const BytraceLine line) const;
    bool TracingMarkWriteEvent(ArgsMap args, const BytraceLine line) const;
    bool SchedWakeupEvent(ArgsMap args, const BytraceLine line) const;
    bool SchedWakingEvent(ArgsMap args, const BytraceLine line) const;
    bool CpuIdleEvent(ArgsMap args, const BytraceLine line) const;
    bool CpuFrequencyEvent(ArgsMap args, const BytraceLine line) const;
    bool WorkqueueExecuteStartEvent(ArgsMap args, const BytraceLine line) const;
    bool WorkqueueExecuteEndEvent(ArgsMap args, const BytraceLine line) const;

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
    TraceDataCache *traceDataCache_;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // _EVENT_PARSER_H_

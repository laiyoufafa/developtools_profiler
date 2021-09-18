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
#include <map>
#include <stdexcept>
#include <string>

#include "base/common.h"
#include "event_parser_base.h"
#include "google/protobuf/message_lite.h"
#include "log.h"
#include "trace_data/trace_data_cache.h"
#include "trace_streamer_filters.h"
#include "types/plugins/ftrace_data/trace_plugin_result.pb.h"

namespace SysTuning {
namespace TraceStreamer {
class HtraceEventParser : private EventParserBase {
public:
    HtraceEventParser(TraceDataCache* dataCache, const TraceStreamerFilters* filter);
    ~HtraceEventParser();
    void ParseDataItem(const FtraceCpuDetailMsg* cpuDetail, BuiltinClocks clock);

private:
    bool SchedSwitchEvent(const google::protobuf::MessageLite& event) const;
    bool ProcessExitEvent(const google::protobuf::MessageLite& event) const;
    bool TaskRenameEvent(const google::protobuf::MessageLite& event) const;
    bool TaskNewtaskEvent(const google::protobuf::MessageLite& event) const;
    bool TracingMarkWriteEvent(const google::protobuf::MessageLite& event) const;
    bool SchedWakeupEvent(const google::protobuf::MessageLite& event) const;
    bool SchedWakeupNewEvent(const google::protobuf::MessageLite& event) const;
    bool SchedWakingEvent(const google::protobuf::MessageLite& event) const;
    bool CpuIdleEvent(const google::protobuf::MessageLite& event) const;
    bool CpuFrequencyEvent(const google::protobuf::MessageLite& event) const;
    bool WorkqueueExecuteStartEvent(const google::protobuf::MessageLite& event) const;
    bool WorkqueueExecuteEndEvent(const google::protobuf::MessageLite& event) const;
    bool InvokeFunc(const SupportedTraceEventType& eventType, const google::protobuf::MessageLite& msgBase);
    using FuncCall = std::function<bool(const google::protobuf::MessageLite& event)>;
    uint32_t eventCpu_ = INVALID_UINT32;
    uint64_t eventTimestamp_ = INVALID_UINT64;
    uint32_t eventPid_ = INVALID_UINT32;
    std::map<std::string, FuncCall> eventToFunctionMap_{};
    DataIndex workQueueId_ = 0;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_EVENT_PARSER_H_

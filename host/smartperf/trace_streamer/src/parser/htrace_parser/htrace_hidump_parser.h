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
#ifndef HTRACE_HIDUMP_PARSER_H
#define HTRACE_HIDUMP_PARSER_H
#include <cstdint>
#include <map>
#include <string>
#include "trace_data/trace_data_cache.h"
#include "trace_streamer_config.h"
#include "trace_streamer_filters.h"


namespace SysTuning {
namespace TraceStreamer {
class HtraceHidumpParser {
public:
    HtraceHidumpParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx);
    ~HtraceHidumpParser();
    void Parse(HidumpInfo& tracePacket);
    void Finish();

private:
    const TraceStreamerFilters* streamFilters_;
    TraceDataCache* traceDataCache_;
    TraceStreamerConfig config_ = {};
    uint64_t traceStartTime_ = std::numeric_limits<uint64_t>::max();
    uint64_t traceEndTime_ = 0;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_HIDUMP_PARSER_H

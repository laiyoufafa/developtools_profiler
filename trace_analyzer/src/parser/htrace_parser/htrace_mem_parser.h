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
#ifndef HTRACE_MEM_PARSER_H
#define HTRACE_MEM_PARSER_H
#include <cstdint>
#include <limits>
#include <map>
#include <stdexcept>
#include <string>
#include "trace_data/trace_data_cache.h"
#include "trace_streamer_cfg.h"
#include "trace_streamer_filters.h"
#include "types/plugins/memory_data/memory_plugin_result.pb.h"

namespace SysTuning {
namespace TraceStreamer {
class HtraceMemParser {
public:
    HtraceMemParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx);
    ~HtraceMemParser();
    void Parse(const MemoryData& tracePacket, uint64_t, BuiltinClocks clock) const;

private:
    const TraceStreamerFilters* streamFilters_;
    TraceDataCache* traceDataCache_;
    TraceStreamConfig config_;
    std::map<MemInfoType, DataIndex> memNameDictMap_{};
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_MEM_PARSER_H

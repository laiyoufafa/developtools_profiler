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
#ifndef HTRACE_NATIVE_HOOK_PARSER_H
#define HTRACE_NATIVE_HOOK_PARSER_H
#include <cstdint>
#include <map>
#include <string>
#include "double_map.h"
#include "htrace_event_parser.h"
#include "htrace_plugin_time.h"
#include "native_hook_result.pb.h"
#include "trace_streamer_config.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
class HtraceNativeHookParser : public HtracePluginTimeParser {
public:
    HtraceNativeHookParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx);
    ~HtraceNativeHookParser();
    void SortNativeHookData(BatchNativeHookData& tracePacket);
    void FinishParseNativeHookData();
    void Finish();

private:
    void MaybeParseNativeHookData();
    void ParseNativeHookData(const uint64_t timeStamp, const NativeHookData* nativeHookData);
    void ParseNativeHookFrame(const RepeatedPtrField< ::Frame >& frameInfo);
    void MaybeUpdateCurrentSizeDur(uint64_t row, uint64_t timeStamp, bool isMalloc);
    uint64_t eventId_ = 0;
    DoubleMap<uint32_t, uint64_t, uint64_t> addrToAllocEventRow_;
    DoubleMap<uint32_t, uint64_t, uint64_t> addrToMmapEventRow_;
    uint64_t lastMallocEventRaw_ = INVALID_UINT64;
    uint64_t lastMmapEventRaw_ = INVALID_UINT64;
    std::multimap<uint64_t, std::unique_ptr<NativeHookData>> tsNativeHookQueue_;
    const size_t MAX_CACHE_SIZE = 200000;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_NATIVE_HOOK_PARSER_H

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
#ifndef HTRACE_JS_MEMORY_PARSER_H
#define HTRACE_JS_MEMORY_PARSER_H
#include <cstdint>
#include <string>
#include <vector>
#include "common_types.h"
#include "htrace_plugin_time_parser.h"
#include "json.hpp"
#include "trace_streamer_config.h"
#include "trace_streamer_filters.h"
using json = nlohmann::json;

namespace SysTuning {
namespace TraceStreamer {
class HtraceJSMemoryParser : public HtracePluginTimeParser {
public:
    HtraceJSMemoryParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx);
    ~HtraceJSMemoryParser();
    void ParseJSMemoryConfig(ProtoReader::BytesView tracePacket);
    void Parse(ProtoReader::BytesView tracePacket, uint64_t ts);
    void Finish();

private:
    void ParseTimeLine(int fileId, const std::string& jsonString);
    void ParseSnapshot(int fileId, const std::string& jsonString);
    void ParserJSSnapInfo(int fileId, const json& jMessage);
    void ParseNodes(int fileId, const json& jMessage);
    void ParseEdges(int fileId, const json& jMessage);
    void ParseLocation(int fileId, const json& jMessage);
    void ParseSample(int fileId, const json& jMessage);
    void ParseString(int fileId, const json& jMessage);
    void ParseTraceFuncInfo(int fileId, const json& jMessage);
    void ParseTraceNode(int fileId, const json& jMessage);
    void ParserSnapInfo(int fileId, const std::string& key, const std::vector<std::vector<std::string>>& types);
    int32_t type_ = 0;
    int32_t pid_ = 0;
    const std::string snapshotEnd_ = "{\"id\":1,\"result\":{}}";
    const std::string timeLineEnd_ = "{\"id\":2,\"result\":{}}";
    uint64_t startTime_ = std::numeric_limits<uint64_t>::max();
    bool isFirst_ = true;
    std::string jsMemoryString_ = "";
    int fileId_ = 0;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_JS_MEMORY_PARSER_H

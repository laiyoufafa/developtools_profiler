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

#ifndef BYTRACE_PARSER_H
#define BYTRACE_PARSER_H

#include <regex>

#include "event_parser.h"
#include "log.h"
#include "string_to_numerical.h"
#include "trace_data/trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
class BytraceParser {
public:
    explicit BytraceParser(TraceDataCache*, const TraceStreamerFilters*);
    ~BytraceParser();

    bool Parse(std::unique_ptr<uint8_t[]>, size_t size);

private:
    enum BytraceParserState { IDLE, RUNNING, END };

    BytraceParserState parserState_ = IDLE;
    bool isByTrace_ = false;
    std::deque<uint8_t> packagesBuffer_ = {0};
    std::unique_ptr<EventParser> eventParser_;
    const std::regex bytraceMatcher_ =
        std::regex(R"(-(\d+)\s+\(?\s*(\d+|-+)?\)?\s?\[(\d+)\]\s*)"
        R"([a-zA-Z0-9.]{0,5}\s+(\d+\.\d+):\s+(\S+):)");

    const std::string script_ = R"(</script>)";

    inline static bool IsNotSpace(char c)
    {
        return !std::isspace(c);
    }
    inline static bool IsTraceData(const std::string& buffer)
    {
        return ((buffer.compare(0, std::string("#").length(), "#") == 0) ||
                buffer.find("TASK-PID") != std::string::npos);
    }

    ParseResult LineParser(const std::string& buffer, BytraceLine& line) const;
    std::string StrTrim(const std::string& input) const;
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // _BYTRACE_PARSER_H_

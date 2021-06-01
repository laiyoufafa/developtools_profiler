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

#include "bytrace_parser.h"

namespace SysTuning {
namespace TraceStreamer {
BytraceParser::BytraceParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : eventParser_(std::make_unique<EventParser>(dataCache, ctx))
{
}

BytraceParser::~BytraceParser() = default;

bool BytraceParser::Parse(std::unique_ptr<uint8_t[]> bufferStr, size_t size)
{
    if (parserState_ == END) {
        return true;
    }

    parserState_ = RUNNING;
    packagesBuffer_.insert(packagesBuffer_.end(), &bufferStr[0], &bufferStr[size]);
    auto packagesBegin = packagesBuffer_.begin();

    while (1) {
        auto packagesLine = std::find(packagesBegin, packagesBuffer_.end(), '\n');
        if (packagesLine == packagesBuffer_.end()) {
            break;
        }

        std::string bufferLine(packagesBegin, packagesLine);

        if (IsTraceData(bufferLine)) {
            isByTrace_ = true;
            goto NEXT_LINE;
        }

        if (isByTrace_) {
            if (bufferLine.find(script_.c_str()) != std::string::npos) {
                parserState_ = END;
                break;
            } else if (!bufferLine.empty() && (bufferLine.compare(0, std::string("#").length(), "#") != 0)) {
                BytraceLine line;
                if (LineParser(bufferLine, line) == SUCCESS) {
                    eventParser_->ParseLine(std::move(line));
                }
            }
        }

    NEXT_LINE:
        packagesBegin = packagesLine + 1;
        continue;
    }

    if (parserState_ == END) {
        packagesBuffer_.clear();
    } else {
        packagesBuffer_.erase(packagesBuffer_.begin(), packagesBegin);
    }
    return true;
}

ParseResult BytraceParser::LineParser(const std::string& buffer, BytraceLine& line) const
{
    std::smatch matcheLine;
    bool matched = std::regex_search(buffer, matcheLine, bytraceMatcher_);
    if (!matched) {
        TUNING_LOGD("Not support this event (line: %s)", buffer.c_str());
        return ERROR;
    }

    size_t index = 0;
    std::string pidStr = matcheLine[++index].str();
    std::optional<uint32_t> optionalPid = base::StrToUInt32(pidStr);
    if (!optionalPid.has_value()) {
        TUNING_LOGD("Illegal pid: %s", pidStr.c_str());
        return ERROR;
    }

    std::string tGidStr = matcheLine[++index].str();
    std::string cpuStr = matcheLine[++index].str();
    std::optional<uint32_t> optionalCpu = base::StrToUInt32(cpuStr);
    if (!optionalCpu.has_value()) {
        TUNING_LOGD("Illegal cpu %s", cpuStr.c_str());
        return ERROR;
    }
    std::string timeStr = matcheLine[++index].str();
    std::optional<double> optionalTime = base::StrToDouble(timeStr);
    if (!optionalTime.has_value()) {
        TUNING_LOGD("Illegal ts %s", timeStr.c_str());
        return ERROR;
    }
    std::string eventName = matcheLine[++index].str();

    line.pid = optionalPid.value();
    line.cpu = optionalCpu.value();
    line.ts = static_cast<uint64_t>(optionalTime.value() * 1e9);
    line.task = StrTrim(matcheLine.prefix());
    line.tGidStr = tGidStr;
    line.eventName = eventName;
    line.argsStr = StrTrim(matcheLine.suffix());

    return SUCCESS;
}

// Remove space at the beginning and end of the string
std::string BytraceParser::StrTrim(const std::string& input) const
{
    std::string str = input;
    auto posBegin = std::find_if(str.begin(), str.end(), IsNotSpace);
    str.erase(str.begin(), posBegin);

    auto posEnd = std::find_if(str.rbegin(), str.rend(), IsNotSpace);
    str.erase(posEnd.base(), str.end());

    return str;
}
} // namespace TraceStreamer
} // namespace SysTuning

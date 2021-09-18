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
#include <unistd.h>
#include "parting_string.h"
#include "stat_filter.h"
namespace SysTuning {
namespace TraceStreamer {
BytraceParser::BytraceParser(TraceDataCache* dataCache, const TraceStreamerFilters* filters)
    : ParserBase(filters),
      eventParser_(std::make_unique<BytraceEventParser>(dataCache, filters)),
      dataSegArray(new DataSegment[MAX_SEG_ARRAY_SIZE])
{
}

BytraceParser::~BytraceParser() = default;

void BytraceParser::WaitForParserEnd()
{
    if (matchLineThreadStarted || parsingDataItemThreadStarted_) {
        toExit_ = true;
        while (!exited_) {
            usleep(sleepDur_ * sleepDur_);
        }
    }
}
void BytraceParser::ParseTraceDataSegment(std::unique_ptr<uint8_t[]> bufferStr, size_t size)
{
    if (isParsingOver_) {
        return;
    }
    packagesBuffer_.insert(packagesBuffer_.end(), &bufferStr[0], &bufferStr[size]);
    auto packagesBegin = packagesBuffer_.begin();

    while (1) {
        auto packagesLine = std::find(packagesBegin, packagesBuffer_.end(), '\n');
        if (packagesLine == packagesBuffer_.end()) {
            break;
        }

        std::string bufferLine(packagesBegin, packagesLine);

        if (IsTraceComment(bufferLine)) {
            traceCommentLines_++;
            goto NEXT_LINE;
        }
        if (bufferLine.empty()) {
            parsedTraceInvalidLines_++;
            goto NEXT_LINE;
        }

        if (bufferLine.find(script_.c_str()) != std::string::npos) {
            isParsingOver_ = true;
            break;
        }
        ParseTraceDataItem(bufferLine);

    NEXT_LINE:
        packagesBegin = packagesLine + 1;
        continue;
    }

    if (isParsingOver_) {
        packagesBuffer_.clear();
    } else {
        packagesBuffer_.erase(packagesBuffer_.begin(), packagesBegin);
    }
    return;
}

void BytraceParser::ParseTraceDataItem(const std::string& buffer)
{
    while (!toExit_) {
        int head = seprateHead_;
        if (dataSegArray[head].status.load() != Status_Init) {
            TS_LOGD("seprateHead_:\t%d, matchHead_:\t%d, parseHead_:\t%d\n", seprateHead_, matchHead_, parseHead_);
            usleep(sleepDur_);
            continue;
        }
        dataSegArray[head].seg = std::move(buffer);
        dataSegArray[head].status = Status_Seprated;
        seprateHead_ = (seprateHead_ + 1) % MAX_SEG_ARRAY_SIZE;
        break;
    }
    if (!matchLineThreadStarted) {
        matchLineThreadStarted = true;
        int tmp = maxThread_;
        while (tmp--) {
            parserThreadCount_++;
            std::thread MatchLineThread(&BytraceParser::MatchLine, this);
            MatchLineThread.detach();
            fprintf(stdout, "parser Thread:%d/%d start working ...\n", maxThread_ - tmp, maxThread_);
        }
    }
    return;
}
int BytraceParser::GetNextSegment()
{
    int head;
    dataSegMux_.lock();
    head = matchHead_;
    DataSegment& seg = dataSegArray[head];
    if (seg.status.load() != Status_Seprated) {
        if (toExit_) {
            parserThreadCount_--;
            fprintf(stdout, "exiting parser, parserThread Count:%d\n", parserThreadCount_);
            dataSegMux_.unlock();
            if (!parserThreadCount_ && !parsingDataItemThreadStarted_) {
                exited_ = true;
            }
            return ERROR_CODE_EXIT;
        }
        if (seg.status == Status_Parsing) {
            matchHead_ = (matchHead_ + 1) % MAX_SEG_ARRAY_SIZE;
            dataSegMux_.unlock();
            return ERROR_CODE_NODATA;
        }
        dataSegMux_.unlock();
        TS_LOGD("MatchLine watting:\t%d, matchHead_:\t%d, parseHead_:\t%d\n", seprateHead_, matchHead_, parseHead_);
        usleep(sleepDur_);
        return ERROR_CODE_NODATA;
    }
    matchHead_ = (matchHead_ + 1) % MAX_SEG_ARRAY_SIZE;
    seg.status = Status_Parsing;
    dataSegMux_.unlock();
    return head;
}

void BytraceParser::GetDataSegAttr(DataSegment& seg, const std::smatch& matcheLine) const
{
    size_t index = 0;
    std::string pidStr = matcheLine[++index].str();
    std::optional<uint32_t> optionalPid = base::StrToUInt32(pidStr);
    if (!optionalPid.has_value()) {
        TS_LOGD("Illegal pid: %s", pidStr.c_str());
        seg.status = Status_Invalid;
        return;
    }

    std::string tGidStr = matcheLine[++index].str();
    std::string cpuStr = matcheLine[++index].str();
    std::optional<uint32_t> optionalCpu = base::StrToUInt32(cpuStr);
    if (!optionalCpu.has_value()) {
        TS_LOGD("Illegal cpu %s", cpuStr.c_str());
        seg.status = Status_Invalid;
        return;
    }
    std::string timeStr = matcheLine[++index].str();
    std::optional<double> optionalTime = base::StrToDouble(timeStr);
    if (!optionalTime.has_value()) {
        TS_LOGD("Illegal ts %s", timeStr.c_str());
        seg.status = Status_Invalid;
        return;
    }
    std::string eventName = matcheLine[++index].str();
    seg.bufLine.task = StrTrim(matcheLine.prefix());
    seg.bufLine.argsStr = StrTrim(matcheLine.suffix());
    seg.bufLine.pid = optionalPid.value();
    seg.bufLine.cpu = optionalCpu.value();
    seg.bufLine.ts = static_cast<uint64_t>(optionalTime.value() * 1e9);
    seg.bufLine.tGidStr = tGidStr;
    seg.bufLine.eventName = eventName;
    GetDataSegArgs(seg);
    seg.status = Status_Parsed;
}

void BytraceParser::GetDataSegArgs(DataSegment& seg) const
{
    seg.args.clear();
    if (!seg.bufLine.tGidStr.empty() && seg.bufLine.tGidStr != "-----") {
        seg.tgid = base::StrToUInt32(seg.bufLine.tGidStr).value_or(0);
    }

    for (base::PartingString ss(seg.bufLine.argsStr, ' '); ss.Next();) {
        std::string key;
        std::string value;
        if (!(std::string(ss.GetCur()).find("=") != std::string::npos)) {
            key = "name";
            value = ss.GetCur();
            seg.args.emplace(std::move(key), std::move(value));
            continue;
        }
        for (base::PartingString inner(ss.GetCur(), '='); inner.Next();) {
            if (key.empty()) {
                key = inner.GetCur();
            } else {
                value = inner.GetCur();
            }
        }
        seg.args.emplace(std::move(key), std::move(value));
    }
}
void BytraceParser::MatchLine()
{
    while (1) {
        int head = GetNextSegment();
        if (head < 0) {
            if (head == ERROR_CODE_NODATA) {
                continue;
            }
            matchLineThreadStarted = false;
            if (!parsingDataItemThreadStarted_) {
                exited_ = true;
            }
            return;
        }
        DataSegment& seg = dataSegArray[head];
        std::smatch matcheLine;
        if (!std::regex_search(seg.seg, matcheLine, bytraceMatcher_)) {
            TS_LOGD("Not support this event (line: %s)", seg.seg.c_str());
            seg.status = Status_Invalid;
            parsedTraceInvalidLines_++;
            continue;
        } else {
            parsedTraceValidLines_++;
        }
        GetDataSegAttr(seg, matcheLine);
        if (!parsingDataItemThreadStarted_) {
            parsingDataItemThreadStarted_ = true;
            std::thread ParserThread(&BytraceParser::ParseLine, this);
            ParserThread.detach();
        }
    }
}
void BytraceParser::ParseLine()
{
    while (1) {
        DataSegment& seg = dataSegArray[parseHead_];
        if (seg.status.load() == Status_Invalid) {
            seg.status = Status_Init;
            parseHead_ = (parseHead_ + 1) % MAX_SEG_ARRAY_SIZE;
            streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_INVALID);
            continue;
        }
        if (seg.status.load() != Status_Parsed) {
            if (toExit_ && !parserThreadCount_) {
                fprintf(stdout, "exiting ParseLine Thread\n");
                exited_ = true;
                parsingDataItemThreadStarted_ = false;
                return;
            }
            usleep(sleepDur_);
            continue;
        }
        BytraceLine line = seg.bufLine;
        uint32_t tgid = seg.tgid;
        eventParser_->ParseDataItem(line, seg.args, tgid);
        parseHead_ = (parseHead_ + 1) % MAX_SEG_ARRAY_SIZE;
        seg.status = Status_Init;
    }
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

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
#include "binder_filter.h"
#include "cpu_filter.h"
#include "parting_string.h"
#include "stat_filter.h"
namespace SysTuning {
namespace TraceStreamer {
BytraceParser::BytraceParser(TraceDataCache* dataCache, const TraceStreamerFilters* filters)
    : ParserBase(filters),
      eventParser_(std::make_unique<BytraceEventParser>(dataCache, filters)),
      dataSegArray(new DataSegment[MAX_SEG_ARRAY_SIZE])
{
#ifdef SUPPORTTHREAD
    noThread_ = false;
#endif
}

BytraceParser::~BytraceParser() = default;

void BytraceParser::WaitForParserEnd()
{
    if (parseThreadStarted_ || filterThreadStarted_) {
        toExit_ = true;
        while (!exited_) {
            usleep(sleepDur_ * sleepDur_);
        }
    }
    streamFilters_->cpuFilter_->FinishCpuEvent();
    streamFilters_->binderFilter_->FinishBinderEvent();
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
    int head = rawDataHead_;
    while (!toExit_) {
        if (dataSegArray[head].status.load() != TS_PARSE_STATUS_INIT) {
            TS_LOGD("rawDataHead_:\t%d, parseHead_:\t%d, filterHead_:\t%d\n", rawDataHead_, parseHead_, filterHead_);
            usleep(sleepDur_);
            continue;
        }
        dataSegArray[head].seg = std::move(buffer);
        dataSegArray[head].status = TS_PARSE_STATUS_SEPRATED;
        if (!noThread_) {
            rawDataHead_ = (rawDataHead_ + 1) % MAX_SEG_ARRAY_SIZE;
        }
        break;
    }
    if (!parseThreadStarted_ && !noThread_) {
        parseThreadStarted_ = true;
        int tmp = maxThread_;
        while (tmp--) {
            parserThreadCount_++;
            std::thread MatchLineThread(&BytraceParser::ParseThread, this);
            MatchLineThread.detach();
            TS_LOGI("parser Thread:%d/%d start working ...\n", maxThread_ - tmp, maxThread_);
        }
    }
    if (noThread_) {
        ParserData(dataSegArray[head]);
    }
    return;
}
int BytraceParser::GetNextSegment()
{
    int head;
    dataSegMux_.lock();
    head = parseHead_;
    DataSegment& seg = dataSegArray[head];
    if (seg.status.load() != TS_PARSE_STATUS_SEPRATED) {
        if (toExit_) {
            parserThreadCount_--;
            TS_LOGI("exiting parser, parserThread Count:%d\n", parserThreadCount_);
            dataSegMux_.unlock();
            if (!parserThreadCount_ && !filterThreadStarted_) {
                exited_ = true;
            }
            return ERROR_CODE_EXIT;
        }
        if (seg.status == TS_PARSE_STATUS_PARSING) {
            dataSegMux_.unlock();
            usleep(sleepDur_);
            return ERROR_CODE_NODATA;
        }
        dataSegMux_.unlock();
        TS_LOGD("ParseThread watting:\t%d, parseHead_:\t%d, filterHead_:\t%d\n", rawDataHead_, parseHead_, filterHead_);
        usleep(sleepDur_);
        return ERROR_CODE_NODATA;
    }
    parseHead_ = (parseHead_ + 1) % MAX_SEG_ARRAY_SIZE;
    seg.status = TS_PARSE_STATUS_PARSING;
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
        seg.status = TS_PARSE_STATUS_INVALID;
        return;
    }

    std::string tGidStr = matcheLine[++index].str();
    std::string cpuStr = matcheLine[++index].str();
    std::optional<uint32_t> optionalCpu = base::StrToUInt32(cpuStr);
    if (!optionalCpu.has_value()) {
        TS_LOGD("Illegal cpu %s", cpuStr.c_str());
        seg.status = TS_PARSE_STATUS_INVALID;
        return;
    }
    std::string timeStr = matcheLine[++index].str();
    std::optional<double> optionalTime = base::StrToDouble(timeStr);
    if (!optionalTime.has_value()) {
        TS_LOGD("Illegal ts %s", timeStr.c_str());
        seg.status = TS_PARSE_STATUS_INVALID;
        return;
    }
    std::string eventName = matcheLine[++index].str();
    seg.bufLine.task = StrTrim(matcheLine.prefix());
    if (seg.bufLine.task == "<...>") {
        seg.bufLine.task = "";
    }
    seg.bufLine.argsStr = StrTrim(matcheLine.suffix());
    seg.bufLine.pid = optionalPid.value();
    seg.bufLine.cpu = optionalCpu.value();
    seg.bufLine.ts = static_cast<uint64_t>(optionalTime.value() * 1e9);
    seg.bufLine.tGidStr = tGidStr;
    seg.bufLine.eventName = eventName;
    GetDataSegArgs(seg);
    seg.status = TS_PARSE_STATUS_PARSED;
}

void BytraceParser::GetDataSegArgs(DataSegment& seg) const
{
    seg.args.clear();
    if (seg.bufLine.tGidStr != "-----") {
        seg.tgid = base::StrToUInt32(seg.bufLine.tGidStr).value_or(0);
    } else {
        seg.tgid = 0;
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
void BytraceParser::ParseThread()
{
    while (1) {
        int head = GetNextSegment();
        if (head < 0) {
            if (head == ERROR_CODE_NODATA) {
                continue;
            }
            if (!filterThreadStarted_) {
                exited_ = true;
            }
            return;
        }
        DataSegment& seg = dataSegArray[head];
        ParserData(seg);
    }
}

void BytraceParser::ParserData(DataSegment& seg)
{
    std::smatch matcheLine;
    if (!std::regex_search(seg.seg, matcheLine, bytraceMatcher_)) {
        TS_LOGD("Not support this event (line: %s)", seg.seg.c_str());
        seg.status = TS_PARSE_STATUS_INVALID;
        parsedTraceInvalidLines_++;
        FilterData(seg);
        return;
    } else {
        parsedTraceValidLines_++;
    }
    GetDataSegAttr(seg, matcheLine);
    if (!filterThreadStarted_ && !noThread_) {
        filterThreadStarted_ = true;
        std::thread ParserThread(&BytraceParser::FilterThread, this);
        ParserThread.detach();
    }
    if (noThread_) {
        FilterData(seg);
    }
}
void BytraceParser::FilterThread()
{
    while (1) {
        DataSegment& seg = dataSegArray[filterHead_];
        if (!FilterData(seg)) {
            return;
        }
    }
}
bool BytraceParser::FilterData(DataSegment& seg)
{
    if (seg.status.load() == TS_PARSE_STATUS_INVALID) {
        seg.status = TS_PARSE_STATUS_INIT;
        if (!noThread_) {
            filterHead_ = (filterHead_ + 1) % MAX_SEG_ARRAY_SIZE;
        }
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_INVALID);
        return true;
    }
    if (seg.status.load() != TS_PARSE_STATUS_PARSED) {
        if (toExit_ && !parserThreadCount_) {
            TS_LOGI("exiting FilterThread Thread\n");
            exited_ = true;
            filterThreadStarted_ = false;
            return false;
        }
        if (!noThread_) { // wasm do not allow thread
            usleep(sleepDur_);
        }
        return true;
    }
    BytraceLine line = seg.bufLine;
    uint32_t tgid = seg.tgid;
    eventParser_->ParseDataItem(line, seg.args, tgid);
    if (!noThread_) {
        filterHead_ = (filterHead_ + 1) % MAX_SEG_ARRAY_SIZE;
    }
    seg.status = TS_PARSE_STATUS_INIT;
    return true;
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

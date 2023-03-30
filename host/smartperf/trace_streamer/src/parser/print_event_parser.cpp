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
#include "print_event_parser.h"
#include "frame_filter.h"
#include "stat_filter.h"
#include "string_to_numerical.h"
namespace SysTuning {
namespace TraceStreamer {
PrintEventParser::PrintEventParser(TraceDataCache *dataCache, const TraceStreamerFilters *filter)
    : EventParserBase(dataCache, filter), pointLength_(1), maxPointLength_(2)
{
    eventToFrameFunctionMap_ = {
        {recvievVsync_, bind(&PrintEventParser::ReciveVsync, this, std::placeholders::_1, std::placeholders::_2,
                             std::placeholders::_3)},
        {onVsyncEvent_, bind(&PrintEventParser::ReciveOnVsync, this, std::placeholders::_1, std::placeholders::_2,
                             std::placeholders::_3)},
        {rsOnVsyncEvent_, bind(&PrintEventParser::RSReciveOnVsync, this, std::placeholders::_1, std::placeholders::_2,
                               std::placeholders::_3)},
        {marshRwTransactionData_, bind(&PrintEventParser::OnRwTransaction, this, std::placeholders::_1,
                                       std::placeholders::_2, std::placeholders::_3)},
        {rsMainThreadProcessCmd_, bind(&PrintEventParser::OnMainThreadProcessCmd, this, std::placeholders::_1,
                                       std::placeholders::_2, std::placeholders::_3)}};
}

bool PrintEventParser::ParsePrintEvent(const std::string& comm, uint64_t ts, uint32_t pid, std::string_view event, const BytraceLine& line)
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_RECEIVED);
    TracePoint point;
    if (GetTracePoint(event, point) != SUCCESS) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_DATA_INVALID);
        return false;
    }
    if (point.tgid_) {
        streamFilters_->processFilter_->GetOrCreateInternalPid(ts, point.tgid_);
    }
    switch (point.phase_) {
        case 'B': {
            uint32_t index = streamFilters_->sliceFilter_->BeginSlice(comm, ts, pid, point.tgid_, INVALID_DATAINDEX,
                                                                      traceDataCache_->GetDataIndex(point.name_));
            if (index != INVALID_UINT32) {
                // add distributed data
                traceDataCache_->GetInternalSlicesData()->SetDistributeInfo(
                    index, point.chainId_, point.spanId_, point.parentSpanId_, point.flag_, point.args_);
                if (pid == point.tgid_) {
                    HandleFrameSliceBeginEvent(point.funcPrefixId_, index, point.funcArgs_, line);
                }
            } else {
                streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_DATA_LOST);
            }
            break;
        }
        case 'E': {
            uint32_t index = streamFilters_->sliceFilter_->EndSlice(ts, pid, point.tgid_);
            if (pid == point.tgid_) {
                HandleFrameSliceEndEvent(ts, point.tgid_, pid, index);
            }
            break;
        }
        case 'S': {
            auto cookie = static_cast<uint64_t>(point.value_);
            auto index = streamFilters_->sliceFilter_->StartAsyncSlice(ts, pid, point.tgid_, cookie,
                                                                       traceDataCache_->GetDataIndex(point.name_));
            if (point.name_ == onFrameQueeuStartEvent_) {
                OnFrameQueueStart(ts, index, point.tgid_);
            }
            break;
        }
        case 'F': {
            auto cookie = static_cast<uint64_t>(point.value_);
            auto index = streamFilters_->sliceFilter_->FinishAsyncSlice(ts, pid, point.tgid_, cookie,
                                                                        traceDataCache_->GetDataIndex(point.name_));
            HandleFrameQueueEndEvent(ts, point.tgid_, point.tgid_, index);
            break;
        }
        case 'C': {
            DataIndex nameIndex = traceDataCache_->GetDataIndex(point.name_);
            uint32_t internalPid = streamFilters_->processFilter_->GetInternalPid(point.tgid_);
            if (internalPid != INVALID_ID) {
                streamFilters_->processMeasureFilter_->AppendNewMeasureData(internalPid, nameIndex, ts, point.value_);
                streamFilters_->processFilter_->AddProcessMemory(internalPid);
            } else {
                streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_TRACING_MARK_WRITE, STAT_EVENT_DATA_INVALID);
            }
            break;
        }
        default:
            TS_LOGD("point missing!");
            return false;
    }
    return true;
}

void PrintEventParser::Finish()
{
    eventToFrameFunctionMap_.clear();
    frameCallIds_.clear();
    vsyncSliceIds_.clear();
    onVsyncCallIds_.clear();
    streamFilters_->frameFilter_->Finish();
}
ParseResult PrintEventParser::CheckTracePoint(std::string_view pointStr) const
{
    if (pointStr.size() == 0) {
        TS_LOGD("get trace point data size is 0!");
        return ERROR;
    }

    std::string clockSyncSts = "trace_event_clock_sync";
    if (pointStr.compare(0, clockSyncSts.length(), clockSyncSts.c_str()) == 0) {
        TS_LOGD("skip trace point :%s!", clockSyncSts.c_str());
        return ERROR;
    }

    if (pointStr.find_first_of('B') != 0 && pointStr.find_first_of('E') != 0 && pointStr.find_first_of('C') != 0 &&
        pointStr.find_first_of('S') != 0 && pointStr.find_first_of('F') != 0) {
        TS_LOGD("trace point not supported : [%c] !", pointStr[0]);
        return ERROR;
    }

    if (pointStr.find_first_of('E') != 0 && pointStr.size() == 1) {
        TS_LOGD("point string size error!");
        return ERROR;
    }

    if (pointStr.size() >= maxPointLength_) {
        if ((pointStr[1] != '|') && (pointStr[1] != '\n')) {
            TS_LOGD("not support data formart!");
            return ERROR;
        }
    }

    return SUCCESS;
}

std::string_view PrintEventParser::GetPointNameForBegin(std::string_view pointStr, size_t tGidlength) const
{
    size_t index = maxPointLength_ + tGidlength + pointLength_;

    size_t length = pointStr.size() - index - ((pointStr.back() == '\n') ? 1 : 0);
    std::string_view name = std::string_view(pointStr.data() + index, length);
    return name;
}

ParseResult PrintEventParser::HandlerB(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const
{
    outPoint.name_ = GetPointNameForBegin(pointStr, tGidlength);
    if (outPoint.name_.empty()) {
        TS_LOGD("point name is empty!");
        return ERROR;
    }
    // Use $# to differentiate distributed data
    if (outPoint.name_.find("$#") == std::string::npos) {
        auto space = outPoint.name_.find(' ');
        if (space != std::string::npos) {
            outPoint.funcPrefix_ = outPoint.name_.substr(0, space);
            outPoint.funcPrefixId_ = traceDataCache_->GetDataIndex(outPoint.funcPrefix_);
            outPoint.funcArgs_ =  outPoint.name_.substr(space + 1, -1);
        } else {
            outPoint.funcPrefixId_ = traceDataCache_->GetDataIndex(outPoint.name_);
        }
        return SUCCESS;
    }
    // Resolve distributed calls
    // the normal data mybe like:
    // system-1298 ( 1298) [001] ...1 174330.287420: tracing_mark_write: B|1298|[8b00e96b2,2,1]:C$#decodeFrame$#"
    //    "{\"Process\":\"DecodeVideoFrame\",\"frameTimestamp\":37313484466} \
    //        system - 1298(1298)[001]... 1 174330.287622 : tracing_mark_write : E | 1298 \n
    const std::regex distributeMatcher =
        std::regex(R"((?:^\[([a-z0-9]+),(\d+),(\d+)\]:?([CS]?)\$#)?(.*)\$#(.*)$)");
    std::smatch matcheLine;
    bool matched = std::regex_match(outPoint.name_, matcheLine, distributeMatcher);
    if (matched) {
        size_t index = 0;
        outPoint.chainId_ = matcheLine[++index].str();
        outPoint.spanId_ = matcheLine[++index].str();
        outPoint.parentSpanId_ = matcheLine[++index].str();
        outPoint.flag_ = matcheLine[++index].str();
        outPoint.name_ = matcheLine[++index].str();
        outPoint.args_ = matcheLine[++index].str();
    }
    return SUCCESS;
}

void PrintEventParser::HandleFrameSliceBeginEvent(DataIndex eventName, size_t callStackRow, std::string& args, const BytraceLine& line)
{
    auto it = eventToFrameFunctionMap_.find(eventName);
    if (it != eventToFrameFunctionMap_.end()) {
        it->second(callStackRow, args, line);
    }
}
bool PrintEventParser::ReciveVsync(size_t callStackRow, std::string& args, const BytraceLine &line)
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_VSYNC, STAT_EVENT_RECEIVED);
    // args is like "dataCount:24bytes now:211306766162 expectedEnd:211323423844 vsyncId:3179"
    TS_LOGD("ts:%lu tid:%d, %s callStackRow:%lu",line.ts, line.pid, args.c_str(), callStackRow);
    std::sregex_iterator it(args.begin(), args.end(), recvVsyncPattern_);
    std::sregex_iterator end;
    uint64_t now = INVALID_UINT64;
    uint64_t expectEnd = INVALID_UINT64;
    uint32_t vsyncId = INVALID_UINT32;
    while (it != end) {
        std::smatch match = *it;
        std::string key = match.str(1);
        std::string value = match.str(2);
        if (key == "now") {
            now = base::StrToUInt64(value).value();
        } else if (key == "expectedEnd") {
            expectEnd = base::StrToUInt64(value).value();
        } else if (key == "vsyncId") {
            vsyncId = base::StrToUInt64(value).value();
        }
        ++it;
    }
    auto iTid = streamFilters_->processFilter_->GetInternalTid(line.pid);
    auto iPid = streamFilters_->processFilter_->GetInternalPid(line.tgid);
    streamFilters_->frameFilter_->BeginVsyncEvent(line.ts, iPid, iTid, now, expectEnd, vsyncId, callStackRow);
    vsyncSliceIds_.push_back(callStackRow);
    return true;
}
bool PrintEventParser::ReciveOnVsync(size_t callStackRow, std::string& args, const BytraceLine &line)
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_ONVSYNC, STAT_EVENT_RECEIVED);
    TS_LOGD("ts:%lu tid:%d, %s callStackRow:%lu",line.ts, line.pid, args.c_str(), callStackRow);
    std::sregex_iterator it(args.begin(), args.end(), recvVsyncPattern_);
    std::sregex_iterator end;
    uint64_t now = INVALID_UINT64;
    while (it != end) {
        std::smatch match = *it;
        std::string key = match.str(1);
        std::string value = match.str(2);
        if (key == "now") {
            now = base::StrToUInt64(value).value();
        }
        ++it;
    }
    auto iTid = streamFilters_->processFilter_->GetInternalTid(line.pid);
    if (streamFilters_->frameFilter_->BeginOnvsyncEvent(line.ts, iTid, now, callStackRow)) {
        onVsyncCallIds_.push_back(callStackRow);
    }
    return true;
}
bool PrintEventParser::RSReciveOnVsync(size_t callStackRow, std::string& args, const BytraceLine &line)
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_ONVSYNC, STAT_EVENT_RECEIVED);
    TS_LOGD("ts:%lu tid:%d, %s callStackRow:%lu",line.ts, line.pid, args.c_str(), callStackRow);
    auto iTid = streamFilters_->processFilter_->GetInternalTid(line.pid);
    (void)streamFilters_->frameFilter_->MarkRSOnvsyncEvent(line.ts, iTid);
    return true;
}
bool PrintEventParser::OnRwTransaction(size_t callStackRow, std::string& args, const BytraceLine &line)
{
    // H:MarshRSTransactionData cmdCount:20 transactionFlag:[3799,8] isUni:1
    TS_LOGD("ts:%lu tid:%d, %s callStackRow:%lu",line.ts, line.pid, args.c_str(), callStackRow);
    std::smatch match;
    if (std::regex_search(args, match, transFlagPattern_)) {
        std::string flag2 = match.str(2);
        auto iTid = streamFilters_->processFilter_->GetInternalTid(line.pid);
        return streamFilters_->frameFilter_->BeginRSTransactionData(line.ts, iTid, base::StrToUInt32(flag2).value());
    }
    return true;
}
bool PrintEventParser::OnMainThreadProcessCmd(size_t callStackRow, std::string& args, const BytraceLine &line)
{
    TS_LOGD("ts:%lu tid:%d, %s callStackRow:%lu",line.ts, line.pid, args.c_str(), callStackRow);
    std::sregex_iterator it(args.begin(), args.end(), mainProcessCmdPattern);
    std::sregex_iterator end;
    std::vector<FrameFilter::FrameMap> frames;
    while (it != end) {
        std::smatch match = *it;
        std::string value1 = match.str(1);
        std::string value2 = match.str(2);
        frames.push_back({streamFilters_->processFilter_->GetInternalTid(base::StrToUInt32(value1).value()),
                          base::StrToUInt32(value2).value()});
        ++it;
    }
    auto iTid = streamFilters_->processFilter_->GetInternalTid(line.pid);
    return streamFilters_->frameFilter_->BeginProcessCommandUni(line.ts, iTid, frames, callStackRow);
}
bool PrintEventParser::OnFrameQueueStart(uint64_t ts, size_t callStackRow, uint64_t pid)
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_FRAMEQUEUE, STAT_EVENT_RECEIVED);
    TS_LOGD("ts:%llu tid:%llu, callStackRow:%zu", ts, pid, callStackRow);
    auto iTid = streamFilters_->processFilter_->GetInternalTid(pid);
    if (streamFilters_->frameFilter_->StartFrameQueue(ts, iTid)) {
        frameCallIds_.push_back(callStackRow);
    }
    return true;
}
void PrintEventParser::HandleFrameSliceEndEvent(uint64_t ts, uint64_t pid, uint64_t tid, size_t callStackRow) {
    // it can be frame or slice
    auto iTid = streamFilters_->processFilter_->GetInternalTid(tid);
    auto pos = std::find(vsyncSliceIds_.begin(), vsyncSliceIds_.end(), callStackRow);
    if (pos != vsyncSliceIds_.end()) {
        TS_LOGD("ts:%llu, RenderSliceEnd:%llu, callStackRow:%zu", ts, tid, callStackRow);
        if (!streamFilters_->frameFilter_->EndVsyncEvent(ts, iTid)) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_VSYNC, STAT_EVENT_NOTMATCH);
            TS_LOGW("ts:%llu, RenderSliceEnd:%llu, callStackRow:%zu failed", ts, tid, callStackRow);
        }
        vsyncSliceIds_.erase(pos);
    } else {
        auto pos = std::find(onVsyncCallIds_.begin(), onVsyncCallIds_.end(), callStackRow);
        if (pos != onVsyncCallIds_.end()) {
            TS_LOGD("ts:%lu, VsyncSliceEnd:%d, callStackRow:%lu", ts, tid, callStackRow);
            if (!streamFilters_->frameFilter_->EndOnVsyncEvent(ts, iTid)) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_ONVSYNC, STAT_EVENT_NOTMATCH);
                TS_LOGW("ts:%llu, VsyncSliceEnd:%llu, callStackRow:%zu failed", ts, tid, callStackRow);
            }
            onVsyncCallIds_.erase(pos);
        }
    }
    return;
}

void PrintEventParser::HandleFrameQueueEndEvent(uint64_t ts, uint64_t pid, uint64_t tid, size_t callStackRow) {
    // it can be frame or slice
    auto iTid = streamFilters_->processFilter_->GetInternalTid(tid);
    auto pos = std::find(frameCallIds_.begin(), frameCallIds_.end(), callStackRow);
    if (pos != frameCallIds_.end()) {
        TS_LOGD("ts:%llu, frameSliceEnd:%llu", ts, tid);
        if (!streamFilters_->frameFilter_->EndFrameQueue(ts, iTid)) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_FRAMEQUEUE, STAT_EVENT_NOTMATCH);
            TS_LOGW("ts:%llu, frameSliceEnd:%llu failed", ts, tid);
        }
        frameCallIds_.erase(pos);
    }
    return;
}
ParseResult PrintEventParser::HandlerE(void)
{
    return SUCCESS;
}

size_t PrintEventParser::GetNameLength(std::string_view pointStr, size_t nameIndex)
{
    size_t namelength = 0;
    for (size_t i = nameIndex; i < pointStr.size(); i++) {
        if (pointStr[i] == ' ') {
            namelength = i - nameIndex;
        }
        if (pointStr[i] == '|') {
            namelength = i - nameIndex;
            break;
        }
    }
    return namelength;
}

size_t PrintEventParser::GetValueLength(std::string_view pointStr, size_t valueIndex) const
{
    size_t valuePipe = pointStr.find('|', valueIndex);
    size_t valueLen = pointStr.size() - valueIndex;
    if (valuePipe != std::string_view::npos) {
        valueLen = valuePipe - valueIndex;
    }

    if (valueLen == 0) {
        return 0;
    }

    if (pointStr[valueIndex + valueLen - pointLength_] == '\n') {
        valueLen--;
    }

    return valueLen;
}

ParseResult PrintEventParser::HandlerCSF(std::string_view pointStr, TracePoint& outPoint, size_t tGidlength) const
{
    // point name
    size_t nameIndex = maxPointLength_ + tGidlength + pointLength_;
    size_t namelength = GetNameLength(pointStr, nameIndex);
    if (namelength == 0) {
        TS_LOGD("point name length is error!");
        return ERROR;
    }
    outPoint.name_ = std::string_view(pointStr.data() + nameIndex, namelength);

    // point value
    size_t valueIndex = nameIndex + namelength + pointLength_;
    size_t valueLen = GetValueLength(pointStr, valueIndex);
    if (valueLen == 0) {
        TS_LOGD("point value length is error!");
        return ERROR;
    }

    std::string valueStr(pointStr.data() + valueIndex, valueLen);
    if (!base::StrToUInt64(valueStr).has_value()) {
        TS_LOGD("point value is error!");
        return ERROR;
    }
    outPoint.value_ = base::StrToUInt64(valueStr).value();

    size_t valuePipe = pointStr.find('|', valueIndex);
    if (valuePipe != std::string_view::npos) {
        size_t groupLen = pointStr.size() - valuePipe - pointLength_;
        if (groupLen == 0) {
            return ERROR;
        }

        if (pointStr[pointStr.size() - pointLength_] == '\n') {
            groupLen--;
        }

        outPoint.categoryGroup_ = std::string_view(pointStr.data() + valuePipe + 1, groupLen);
    }

    return SUCCESS;
}

ParseResult PrintEventParser::GetTracePoint(std::string_view pointStr, TracePoint& outPoint) const
{
    if (CheckTracePoint(pointStr) != SUCCESS) {
        return ERROR;
    }

    size_t tGidlength = 0;
    // we may get wrong format data like tracing_mark_write: E
    // while the format data must be E|call-tid
    // please use a regular-format to get all the data
    outPoint.phase_ = pointStr.front();
    outPoint.tgid_ = GetThreadGroupId(pointStr, tGidlength);

    ParseResult ret = ERROR;
    switch (outPoint.phase_) {
        case 'B': {
            ret = HandlerB(pointStr, outPoint, tGidlength);
            break;
        }
        case 'E': {
            ret = HandlerE();
            break;
        }
        case 'S':
        case 'F':
        case 'C': {
            ret = HandlerCSF(pointStr, outPoint, tGidlength);
            break;
        }
        default:
            return ERROR;
    }
    return ret;
}

uint32_t PrintEventParser::GetThreadGroupId(std::string_view pointStr, size_t& length) const
{
    for (size_t i = maxPointLength_; i < pointStr.size(); i++) {
        if (pointStr[i] == '|' || pointStr[i] == '\n') {
            break;
        }

        if (pointStr[i] < '0' || pointStr[i] > '9') {
            return ERROR;
        }

        length++;
    }

    std::string str(pointStr.data() + maxPointLength_, length);
    return base::StrToUInt32(str).value_or(0);
}
} // namespace TraceStreamer
} // namespace SysTuning

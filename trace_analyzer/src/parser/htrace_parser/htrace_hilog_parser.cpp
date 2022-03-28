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
#include "htrace_hilog_parser.h"
#include "clock_filter.h"
#include "htrace_event_parser.h"
#include "process_filter.h"
#include "stat_filter.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceHiLogParser::HtraceHiLogParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : streamFilters_(ctx), traceDataCache_(dataCache)
{
    if (!traceDataCache_) {
        TS_LOGE("traceDataCache_ should not be null");
        return;
    }
    if (!streamFilters_) {
        TS_LOGE("streamFilters_ should not be null");
        return;
    }
}

HtraceHiLogParser::~HtraceHiLogParser()
{
    TS_LOGI("hilog ts MIN:%llu, MAX:%llu",
            static_cast<unsigned long long>(traceStartTime_), static_cast<unsigned long long>(traceEndTime_));
}
void HtraceHiLogParser::Parse(HilogInfo& tracePacket)
{
    if (!tracePacket.info_size()) {
        return;
    }
    if (!traceDataCache_) {
        TS_LOGE("traceDataCache_ should not be null");
        return;
    }
    if (!streamFilters_) {
        TS_LOGE("streamFilters_ should not be null");
        return;
    }
    for (int i = 0; i < tracePacket.info_size(); i++) {
        auto hilogLine = tracePacket.mutable_info(i);
        uint64_t curLineSeq = hilogLine->id();
        streamFilters_->statFilter_->IncreaseStat(TRACE_HILOG, STAT_EVENT_RECEIVED);
        if (curLineSeq < lastLineSeq_ + 1) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_HILOG, STAT_EVENT_NOTMATCH);
        } else if (curLineSeq > lastLineSeq_ + 1) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_HILOG, STAT_EVENT_DATA_LOST);
        }
        lastLineSeq_ = curLineSeq;
        auto logData = traceDataCache_->GetDataIndex(hilogLine->context().c_str());
        auto logDetails = hilogLine->detail();

        auto timeStamp = logDetails.tv_nsec() + logDetails.tv_sec() * SEC_TO_NS;
        auto newTimeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, timeStamp);
        if (newTimeStamp != timeStamp) { // record the time only when the time is valid
            traceStartTime_ = std::min(traceStartTime_, newTimeStamp);
            traceEndTime_ = std::max(traceEndTime_, newTimeStamp);
        }

        streamFilters_->processFilter_->GetOrCreateThreadWithPid(logDetails.tid(), logDetails.pid());
        auto iter = logLevelString_.find(logDetails.level());
        if (iter == logLevelString_.end()) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_HILOG, STAT_EVENT_DATA_INVALID);
            TS_LOGD("log level do not exit!!!");
            continue;
        }
        DataIndex levelData = traceDataCache_->dataDict_.GetStringIndex(iter->second.c_str());
        DataIndex logTag = traceDataCache_->dataDict_.GetStringIndex(logDetails.tag().c_str());
        traceDataCache_->GetHilogData()->AppendNewLogInfo(curLineSeq, newTimeStamp, logDetails.pid(), logDetails.tid(),
                                                          levelData, logTag, logData, timeStamp);
    }
}
void HtraceHiLogParser::Finish()
{
    traceDataCache_->MixTraceTime(traceStartTime_, traceEndTime_);
}
} // namespace TraceStreamer
} // namespace SysTuning

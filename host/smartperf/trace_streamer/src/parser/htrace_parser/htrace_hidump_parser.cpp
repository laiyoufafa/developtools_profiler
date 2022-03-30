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
#include "clock_filter.h"
#include "hidump_plugin_result.pb.h"
#include "htrace_event_parser.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "htrace_hidump_parser.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceHidumpParser::HtraceHidumpParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
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

HtraceHidumpParser::~HtraceHidumpParser()
{
    TS_LOGI("FPS data ts MIN:%llu, MAX:%llu",
            static_cast<unsigned long long>(traceStartTime_), static_cast<unsigned long long>(traceEndTime_));
}
void HtraceHidumpParser::Parse(HidumpInfo& tracePacket)
{
    if (!tracePacket.fps_event_size()) {
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
    for (int i = 0; i < tracePacket.fps_event_size(); i++) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_HIDUMP_FPS, STAT_EVENT_RECEIVED);
        auto hidumpData = tracePacket.mutable_fps_event(i);
        auto timeStamp = hidumpData->time().tv_nsec() + hidumpData->time().tv_sec() * SEC_TO_NS;
        auto newTimeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(hidumpData->id(), timeStamp);
        if (newTimeStamp != timeStamp) { // record the time only when the time is valid
            traceStartTime_ = std::min(traceStartTime_, newTimeStamp);
            traceEndTime_ = std::max(traceEndTime_, newTimeStamp);
        } else {
            streamFilters_->statFilter_->IncreaseStat(TRACE_HIDUMP_FPS, STAT_EVENT_DATA_INVALID);
        }
        auto fps = hidumpData->fps();
        traceDataCache_->GetHidumpData()->AppendNewHidumpInfo(newTimeStamp, fps);
    }
}
void HtraceHidumpParser::Finish()
{
    traceDataCache_->MixTraceTime(traceStartTime_, traceEndTime_);
}
} // namespace TraceStreamer
} // namespace SysTuning

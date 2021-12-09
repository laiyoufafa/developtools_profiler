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
#include "htrace_clock_detail_parser.h"
#include "clock_filter.h"
#include "htrace_event_parser.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "symbols_filter.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceClockDetailParser::HtraceClockDetailParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : streamFilters_(ctx), traceDataCache_(dataCache)
{
    for (auto i = 0; i < MEM_MAX; i++) {
        memNameDictMap_.insert(std::make_pair(static_cast<MemInfoType>(i),
            traceDataCache_->GetDataIndex(config_.memNameMap_.at(static_cast<MemInfoType>(i)))));
    }
}

HtraceClockDetailParser::~HtraceClockDetailParser() = default;
void HtraceClockDetailParser::Parse(TracePluginResult& tracePacket) const
{
    if (!tracePacket.clocks_detail_size()) {
        return;
    }
    std::vector<SnapShot> snapShot;
    TS_LOGI("got clock snapshot");
    for (int i = 0; i < tracePacket.clocks_detail_size(); i++) {
        auto clockInfo = tracePacket.mutable_clocks_detail(i);
        TS_LOGI("clockid:%d, ts:%llu", clockInfo->id(),
                static_cast<unsigned long long>(clockInfo->time().tv_nsec() + clockInfo->time().tv_sec() * SEC_TO_NS));
        snapShot.push_back(SnapShot{static_cast<ClockId>(clockInfo->id()),
                                    clockInfo->time().tv_nsec() + clockInfo->time().tv_sec() * SEC_TO_NS});
        streamFilters_->clockFilter_->AddClockSnapshot(snapShot);
    }
    streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_CLOCK_SYNC, STAT_EVENT_RECEIVED);
}
} // namespace TraceStreamer
} // namespace SysTuning

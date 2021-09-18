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
#include "htrace_mem_parser.h"
#include "clock_filter.h"
#include "htrace_event_parser.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "symbols_filter.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceMemParser::HtraceMemParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : streamFilters_(ctx), traceDataCache_(dataCache)
{
    if (!traceDataCache_) {
        TS_LOGF("traceDataCache_ should not be null");
        return;
    }
    for (auto i = 0; i < MEM_MAX; i++) {
        memNameDictMap_.insert(
            std::make_pair(static_cast<MemInfoType>(i),
                           traceDataCache_->GetDataIndex(config_.memNameMap_.at(static_cast<MemInfoType>(i)))));
    }
}

HtraceMemParser::~HtraceMemParser() = default;
void HtraceMemParser::Parse(const MemoryData& tracePacket, uint64_t timeStamp, BuiltinClocks clock) const
{
    if (!tracePacket.processesinfo_size()) {
        return;
    }

    if (!streamFilters_) {
        TS_LOGF("streamFilters_ should not be null");
        return;
    }

    timeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(clock, timeStamp);
    for (int i = 0; i < tracePacket.processesinfo_size(); i++) {
        auto memInfo = tracePacket.processesinfo(i);
        auto ipid = streamFilters_->processFilter_->UpdateOrCreateProcessWithName(memInfo.pid(), memInfo.name());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_SIZE), timeStamp,
                                                                    memInfo.vm_size_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_RSS), timeStamp,
                                                                    memInfo.vm_rss_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_ANON), timeStamp,
                                                                    memInfo.rss_anon_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_RSS_FILE), timeStamp,
                                                                    memInfo.rss_file_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_RSS_SHMEM), timeStamp,
                                                                    memInfo.rss_shmem_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_SWAP), timeStamp,
                                                                    memInfo.vm_swap_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_LOCKED), timeStamp,
                                                                    memInfo.vm_locked_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_HWM), timeStamp,
                                                                    memInfo.vm_hwm_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_OOM_SCORE_ADJ),
                                                                    timeStamp, memInfo.oom_score_adj());
        streamFilters_->statFilter_->IncreaseStat(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    }
}
} // namespace TraceStreamer
} // namespace SysTuning

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
#include "htrace_event_parser.h"
#include "native_hook_result.pb.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "htrace_native_hook_parser.h"
namespace SysTuning {
namespace TraceStreamer {
uint64_t HtraceNativeHookParser::eventId_ = 0;
HtraceNativeHookParser::HtraceNativeHookParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : streamFilters_(ctx), traceDataCache_(dataCache), addrToAllocEventRow_(INVALID_UINT64)
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

HtraceNativeHookParser::~HtraceNativeHookParser()
{
    TS_LOGI("native hook data ts MIN:%llu, MAX:%llu",
            static_cast<unsigned long long>(traceStartTime_), static_cast<unsigned long long>(traceEndTime_));
}
void HtraceNativeHookParser::Parse(BatchNativeHookData& tracePacket)
{
    if (!tracePacket.events_size()) {
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
    for (auto i = 0; i < tracePacket.events_size(); i++) {
        auto nativeHookData = tracePacket.mutable_events(i);
        auto timeStamp = nativeHookData->tv_nsec() + nativeHookData->tv_sec() * SEC_TO_NS;
        auto newTimeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, timeStamp);
        if (newTimeStamp != timeStamp) { // record the time only when the time is valid
            traceStartTime_ = std::min(traceStartTime_, newTimeStamp);
            traceEndTime_ = std::max(traceEndTime_, newTimeStamp);
        }
        // kAllocEvent = 3  kFreeEvent = 4  EVENT_NOT_SET = 0
        auto eventCase = nativeHookData->event_case();
        if (eventCase == NativeHookData::kAllocEvent) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MALLOC, STAT_EVENT_RECEIVED);
            if (newTimeStamp == timeStamp) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MALLOC, STAT_EVENT_DATA_INVALID);
            }
            auto allocEvent = nativeHookData->alloc_event();
            DataIndex allocIndex = traceDataCache_->dataDict_.GetStringIndex("AllocEvent");
            auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(allocEvent.tid(), allocEvent.pid());
            auto ipid = streamFilters_->processFilter_->GetInternalPid(allocEvent.pid());
            auto row = traceDataCache_->GetHeapData()->AppendNewHeapInfo(eventId_, ipid, itid, allocIndex, newTimeStamp,
                                                                         0, 0, allocEvent.addr(), allocEvent.size(),
                                                                         allocEvent.size(), 0);
            addrToAllocEventRow_.Insert(ipid, allocEvent.addr(), static_cast<uint64_t>(row));
            traceDataCache_->GetHeapData()->UpdateCurrentSizeDur(row, newTimeStamp);
            for (auto depth = 0; depth < allocEvent.frame_info_size(); depth++) {
                auto allocEventFrame = allocEvent.frame_info(depth);
                DataIndex symbolNameIndex =
                    traceDataCache_->dataDict_.GetStringIndex(allocEventFrame.symbol_name().c_str());
                DataIndex filePathIndex =
                    traceDataCache_->dataDict_.GetStringIndex(allocEventFrame.file_path().c_str());
                traceDataCache_->GetHeapFrameData()->AppendNewHeapFrameInfo(
                    eventId_, depth, allocEventFrame.ip(), allocEventFrame.sp(), symbolNameIndex, filePathIndex,
                    allocEventFrame.offset(), allocEventFrame.symbol_offset());
            }
            eventId_++;
        } else if (eventCase == NativeHookData::kFreeEvent) {
            streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_RECEIVED);
            if (newTimeStamp == timeStamp) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_DATA_INVALID);
            }
            auto freeEvent = nativeHookData->free_event();
            DataIndex freeIndex = traceDataCache_->dataDict_.GetStringIndex("FreeEvent");
            auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(freeEvent.tid(), freeEvent.pid());
            auto ipid = streamFilters_->processFilter_->GetInternalPid(freeEvent.pid());
            int64_t freeHeapSize = 0;
            auto row = addrToAllocEventRow_.Find(ipid, freeEvent.addr());
            if (row != INVALID_UINT64 && newTimeStamp > traceDataCache_->GetHeapData()->TimeStamData()[row]) {
                addrToAllocEventRow_.Erase(ipid, freeEvent.addr());
                traceDataCache_->GetHeapData()->UpdateHeapDuration(row, newTimeStamp);
                freeHeapSize = traceDataCache_->GetHeapData()->HeapSizes()[row];
            } else if (row == INVALID_UINT64) {
                TS_LOGW("func addr:%lu is empty", freeEvent.addr());
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_DATA_INVALID);
            }
            row = traceDataCache_->GetHeapData()->AppendNewHeapInfo(eventId_, ipid, itid, freeIndex, newTimeStamp, 0, 0,
                                                                    freeEvent.addr(), freeHeapSize, -freeHeapSize, 0);
            traceDataCache_->GetHeapData()->UpdateCurrentSizeDur(row, newTimeStamp);
            for (auto depth = 0; depth < freeEvent.frame_info_size(); depth++) {
                auto freeEventFrame = freeEvent.frame_info(depth);
                DataIndex symbolNameIndex =
                    traceDataCache_->dataDict_.GetStringIndex(freeEventFrame.symbol_name().c_str());
                DataIndex filePathIndex = traceDataCache_->dataDict_.GetStringIndex(freeEventFrame.file_path().c_str());
                traceDataCache_->GetHeapFrameData()->AppendNewHeapFrameInfo(
                    eventId_, depth, freeEventFrame.ip(), freeEventFrame.sp(), symbolNameIndex, filePathIndex,
                    freeEventFrame.offset(), freeEventFrame.symbol_offset());
            }
            eventId_++;
        } else {
            TS_LOGE("An unknown type of data was received!");
        }
    }
}
void HtraceNativeHookParser::Finish()
{
    traceDataCache_->MixTraceTime(traceStartTime_, traceEndTime_);
}
} // namespace TraceStreamer
} // namespace SysTuning

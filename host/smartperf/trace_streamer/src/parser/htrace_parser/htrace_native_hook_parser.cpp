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
#include "htrace_native_hook_parser.h"
#include "clock_filter.h"
#include "process_filter.h"
#include "stat_filter.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceNativeHookParser::HtraceNativeHookParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : HtracePluginTimeParser(dataCache, ctx), addrToAllocEventRow_(INVALID_UINT64), addrToMmapEventRow_(INVALID_UINT64)
{
}

HtraceNativeHookParser::~HtraceNativeHookParser()
{
    TS_LOGI("native hook data ts MIN:%llu, MAX:%llu", static_cast<unsigned long long>(GetPluginStartTime()),
            static_cast<unsigned long long>(GetPluginEndTime()));
}
// In order to improve the accuracy of data, it is necessary to sort the original data.
// Data sorting will be reduced by 5% to 10% Speed of parsing data.
void HtraceNativeHookParser::SortNativeHookData(BatchNativeHookData& tracePacket)
{
    for (auto i = 0; i < tracePacket.events_size(); i++) {
        auto nativeHookData = std::make_unique<NativeHookData>(*tracePacket.mutable_events(i));
        auto timeStamp = nativeHookData->tv_nsec() + nativeHookData->tv_sec() * SEC_TO_NS;
        tsNativeHookQueue_.insert(std::make_pair(timeStamp, std::move(nativeHookData)));
        MaybeParseNativeHookData();
    }
    return;
}
void HtraceNativeHookParser::MaybeParseNativeHookData()
{
    if (tsNativeHookQueue_.size() > MAX_CACHE_SIZE) {
        ParseNativeHookData(tsNativeHookQueue_.begin()->first, tsNativeHookQueue_.begin()->second.get());
        tsNativeHookQueue_.erase(tsNativeHookQueue_.begin());
    }
}
void HtraceNativeHookParser::FinishParseNativeHookData()
{
    for (auto it = tsNativeHookQueue_.begin(); it != tsNativeHookQueue_.end(); it++) {
        ParseNativeHookData(it->first, it->second.get());
    }
    tsNativeHookQueue_.clear();
}

void HtraceNativeHookParser::ParseNativeHookData(const uint64_t timeStamp, const NativeHookData* nativeHookData)
{
    auto newTimeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, timeStamp);
    UpdatePluginTimeRange(TS_CLOCK_REALTIME, timeStamp, newTimeStamp);
    // kAllocEvent = 3  kFreeEvent = 4 kMmapEvent = 5 kMunmapEvent = 6, EVENT_NOT_SET = 0
    auto eventCase = nativeHookData->event_case();
    switch (eventCase) {
        case NativeHookData::kAllocEvent: {
            streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MALLOC, STAT_EVENT_RECEIVED);
            if (newTimeStamp == timeStamp) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MALLOC, STAT_EVENT_DATA_INVALID);
            }
            auto allocEvent = nativeHookData->alloc_event();
            auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(allocEvent.tid(), allocEvent.pid());
            auto ipid = streamFilters_->processFilter_->GetInternalPid(allocEvent.pid());
            auto row = traceDataCache_->GetHeapData()->AppendNewNativeHookData(
                eventId_, ipid, itid, allocEvent.GetTypeName(), INVALID_UINT64, newTimeStamp, 0, 0, allocEvent.addr(),
                allocEvent.size(), allocEvent.size());
            addrToAllocEventRow_.Insert(ipid, allocEvent.addr(), static_cast<uint64_t>(row));
            MaybeUpdateCurrentSizeDur(row, newTimeStamp, true);
            ParseNativeHookFrame(allocEvent.frame_info());
            eventId_++;
            break;
        }
        case NativeHookData::kFreeEvent: {
            streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_RECEIVED);
            if (newTimeStamp == timeStamp) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_DATA_INVALID);
            }
            auto freeEvent = nativeHookData->free_event();
            auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(freeEvent.tid(), freeEvent.pid());
            auto ipid = streamFilters_->processFilter_->GetInternalPid(freeEvent.pid());
            int64_t freeHeapSize = 0;
            auto row = addrToAllocEventRow_.Find(ipid, freeEvent.addr());
            if (row != INVALID_UINT64 && newTimeStamp > traceDataCache_->GetHeapData()->TimeStamData()[row]) {
                addrToAllocEventRow_.Erase(ipid, freeEvent.addr());
                traceDataCache_->GetHeapData()->UpdateHeapDuration(row, newTimeStamp);
                freeHeapSize = traceDataCache_->GetHeapData()->MemSizes()[row];
            } else if (row == INVALID_UINT64) {
                TS_LOGW("func addr:%lu is empty", freeEvent.addr());
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_DATA_INVALID);
            }
            row = traceDataCache_->GetHeapData()->AppendNewNativeHookData(
                eventId_, ipid, itid, freeEvent.GetTypeName(), INVALID_UINT64, newTimeStamp, 0, 0, freeEvent.addr(),
                freeHeapSize, (-1) * freeHeapSize);
            if (freeHeapSize != 0) {
                MaybeUpdateCurrentSizeDur(row, newTimeStamp, true);
            }
            ParseNativeHookFrame(freeEvent.frame_info());
            eventId_++;
            break;
        }
        case NativeHookData::kMmapEvent: {
            streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MMAP, STAT_EVENT_RECEIVED);
            if (newTimeStamp == timeStamp) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MMAP, STAT_EVENT_DATA_INVALID);
            }
            auto mMapEvent = nativeHookData->mmap_event();
            auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(mMapEvent.tid(), mMapEvent.pid());
            auto ipid = streamFilters_->processFilter_->GetInternalPid(mMapEvent.pid());
            auto subType = traceDataCache_->dataDict_.GetStringIndex(mMapEvent.type());
            auto row = traceDataCache_->GetHeapData()->AppendNewNativeHookData(
                eventId_, ipid, itid, mMapEvent.GetTypeName(), subType, newTimeStamp, 0, 0, mMapEvent.addr(),
                mMapEvent.size(), mMapEvent.size());
            addrToMmapEventRow_.Insert(ipid, mMapEvent.addr(), static_cast<uint64_t>(row));
            MaybeUpdateCurrentSizeDur(row, newTimeStamp, false);
            ParseNativeHookFrame(mMapEvent.frame_info());
            eventId_++;
            break;
        }
        case NativeHookData::kMunmapEvent: {
            streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MUNMAP, STAT_EVENT_RECEIVED);
            if (newTimeStamp == timeStamp) {
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MUNMAP, STAT_EVENT_DATA_INVALID);
            }
            auto mUnMapEvent = nativeHookData->munmap_event();
            auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(mUnMapEvent.tid(), mUnMapEvent.pid());
            auto ipid = streamFilters_->processFilter_->GetInternalPid(mUnMapEvent.pid());
            auto row = addrToMmapEventRow_.Find(ipid, mUnMapEvent.addr());
            int64_t effectiveMUnMapSize = 0;
            if (row != INVALID_UINT64 && newTimeStamp > traceDataCache_->GetHeapData()->TimeStamData()[row]) {
                addrToMmapEventRow_.Erase(ipid, mUnMapEvent.addr());
                traceDataCache_->GetHeapData()->UpdateHeapDuration(row, newTimeStamp);
                effectiveMUnMapSize = static_cast<int64_t>(mUnMapEvent.size());
            } else if (row == INVALID_UINT64) {
                TS_LOGW("func addr:%lu is empty", mUnMapEvent.addr());
                streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MUNMAP, STAT_EVENT_DATA_INVALID);
            }
            row = traceDataCache_->GetHeapData()->AppendNewNativeHookData(
                eventId_, ipid, itid, mUnMapEvent.GetTypeName(), INVALID_UINT64, newTimeStamp, 0, 0, mUnMapEvent.addr(),
                mUnMapEvent.size(), (-1) * effectiveMUnMapSize);
            if (effectiveMUnMapSize != 0) {
                MaybeUpdateCurrentSizeDur(row, newTimeStamp, false);
            }
            ParseNativeHookFrame(mUnMapEvent.frame_info());
            eventId_++;
            break;
        }
        default:
            TS_LOGE("An unknown type of data was received!");
            break;
    }
}
void HtraceNativeHookParser::MaybeUpdateCurrentSizeDur(uint64_t row, uint64_t timeStamp, bool isMalloc)
{
    auto& lastAnyEventRaw = isMalloc ? lastMallocEventRaw_ : lastMmapEventRaw_;
    if (lastAnyEventRaw != INVALID_UINT64) {
        traceDataCache_->GetHeapData()->UpdateCurrentSizeDur(lastAnyEventRaw, timeStamp);
    }
    lastAnyEventRaw = row;
}
void HtraceNativeHookParser::ParseNativeHookFrame(const RepeatedPtrField<::Frame>& repeatedFrame)
{
    // the callstack form nativehook of sourcedata is reverse order
    // we need to show the last frame firstly
    auto depth = 0;
    for (auto i = repeatedFrame.size() - 1; i >= 0; i--) {
        auto frame = repeatedFrame.Get(i);
        DataIndex symbolNameIndex = traceDataCache_->dataDict_.GetStringIndex(frame.symbol_name().c_str());
        DataIndex filePathIndex = traceDataCache_->dataDict_.GetStringIndex(frame.file_path().c_str());
        traceDataCache_->GetHeapFrameData()->AppendNewNativeHookFrame(eventId_, depth, frame.ip(), frame.sp(),
                                                                      symbolNameIndex, filePathIndex, frame.offset(),
                                                                      frame.symbol_offset());
        depth++;
    }
}
void HtraceNativeHookParser::Finish()
{
    traceDataCache_->MixTraceTime(GetPluginStartTime(), GetPluginEndTime());
}
} // namespace TraceStreamer
} // namespace SysTuning

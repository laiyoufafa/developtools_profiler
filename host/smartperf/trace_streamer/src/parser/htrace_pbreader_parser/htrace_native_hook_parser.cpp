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
    : HtracePluginTimeParser(dataCache, ctx)
{
    nativeHookFilter_ = std::make_unique<NativeHookFilter>(dataCache, ctx);
}

HtraceNativeHookParser::~HtraceNativeHookParser()
{
    TS_LOGI("native hook data ts MIN:%llu, MAX:%llu", static_cast<unsigned long long>(GetPluginStartTime()),
            static_cast<unsigned long long>(GetPluginEndTime()));
    TS_LOGI("native real ts MIN:%llu, MAX:%llu", static_cast<unsigned long long>(MinTs()),
            static_cast<unsigned long long>(MaxTs()));
}

void HtraceNativeHookParser::ParseStackMap(const ProtoReader::BytesView& bytesView)
{
    ProtoReader::StackMap_Reader stackMapReader(bytesView);
    auto stackId = stackMapReader.id();
    bool parseError = false;
    // stores frames info. if offlineSymbolization is true, storing ips data, else storing FrameMap id.
    std::vector<uint64_t> frames;
    if (stackMapReader.has_frame_map_id()) {
        auto itor = stackMapReader.frame_map_id(&parseError);
        if (parseError) {
            TS_LOGE("Parse packed varInt in ParseStackMap function failed!!!");
            return;
        }
        while (itor) {
            frames.emplace_back(*itor);
            itor++;
        }
    } else if (stackMapReader.has_ip()) {
        auto itor = stackMapReader.ip(&parseError); // packedrepeated 取数据可能会有问题。
        if (parseError) {
            TS_LOGE("Parse packed varInt in ParseStackMap function failed!!!");
            return;
        }
        while (itor) {
            frames.emplace_back(*itor);
            itor++;
        }
    }
    nativeHookFilter_->AppendStackMaps(stackId, frames);
    return;
}

void HtraceNativeHookParser::ParseFrameMap(std::unique_ptr<NativeHookMetaData>& nativeHookMetaData)
{
    segs_.emplace_back(nativeHookMetaData->seg_);
    ProtoReader::FrameMap_Reader frameMapReader(nativeHookMetaData->reader_->frame_map());
    // when callstack is compressed, Frame message only has ip data area.
    nativeHookFilter_->AppendFrameMaps(frameMapReader.id(), frameMapReader.frame());
}
void HtraceNativeHookParser::ParseTagEvent(const ProtoReader::BytesView& bytesView)
{
    ProtoReader::MemTagEvent_Reader memTagEventReader(bytesView);
    auto tagIndex = traceDataCache_->dataDict_.GetStringIndex(memTagEventReader.tag().ToStdString());
    traceDataCache_->GetNativeHookData()->UpdateAddrToMemMapSubType(memTagEventReader.addr(), tagIndex);
}
void HtraceNativeHookParser::ParseFileEvent(const ProtoReader::BytesView& bytesView)
{
    ProtoReader::FilePathMap_Reader filePathMapReader(bytesView);
    auto id = filePathMapReader.id();
    auto nameIndex = traceDataCache_->dataDict_.GetStringIndex(filePathMapReader.name().ToStdString());
    nativeHookFilter_->AppendFilePathMaps(id, nameIndex);
}
void HtraceNativeHookParser::ParseSymbolEvent(const ProtoReader::BytesView& bytesView)
{
    ProtoReader::SymbolMap_Reader symbolMapReader(bytesView);
    auto id = symbolMapReader.id();
    auto nameIndex = traceDataCache_->dataDict_.GetStringIndex(symbolMapReader.name().ToStdString());
    nativeHookFilter_->AppendSymbolMap(id, nameIndex);
}
void HtraceNativeHookParser::ParseThreadEvent(const ProtoReader::BytesView& bytesView)
{
    ProtoReader::ThreadNameMap_Reader threadNameMapReader(bytesView);
    auto id = threadNameMapReader.id();
    auto nameIndex = traceDataCache_->dataDict_.GetStringIndex(threadNameMapReader.name().ToStdString());
    nativeHookFilter_->AppendThreadNameMap(id, nameIndex);
}

void HtraceNativeHookParser::ParseNativeHookEvent(SupportedTraceEventType type,
                                                  uint64_t timeStamp,
                                                  const ProtoReader::BytesView& bytesView)
{
    if (type != TRACE_NATIVE_HOOK_MALLOC and type != TRACE_NATIVE_HOOK_FREE and type != TRACE_NATIVE_HOOK_MMAP and
        type != TRACE_NATIVE_HOOK_MUNMAP and type != TRACE_NATIVE_HOOK_RECORD_STATISTICS) {
        TS_LOGE("unsupported native hook event!!!");
        return;
    }
    uint64_t newTimeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, timeStamp);
    UpdatePluginTimeRange(TS_CLOCK_REALTIME, timeStamp, newTimeStamp);
    streamFilters_->statFilter_->IncreaseStat(type, STAT_EVENT_RECEIVED);
    if (newTimeStamp == timeStamp) {
        streamFilters_->statFilter_->IncreaseStat(type, STAT_EVENT_DATA_INVALID);
    }
    nativeHookFilter_->ParseEvent(type, newTimeStamp, bytesView);
}
void HtraceNativeHookParser::ParseOneNativeHookData(
    std::multimap<uint64_t, std::unique_ptr<NativeHookMetaData>>::iterator itor)
{
    if (!tsNativeHookQueue_.size()) {
        return;
    }
    auto nativeHookDataReader = itor->second->reader_.get();
    if (nativeHookDataReader->has_maps_info()) {
        nativeHookFilter_->GetOfflineSymbolizationObj()->ParseMaps(itor->second);
    } else if (nativeHookDataReader->has_symbol_tab()) {
        nativeHookFilter_->GetOfflineSymbolizationObj()->ParseSymbolTables(itor->second);
    } else if (nativeHookDataReader->has_stack_map()) {
        ParseStackMap(nativeHookDataReader->stack_map());
    } else if (nativeHookDataReader->has_frame_map()) {
        ParseFrameMap(itor->second);
    } else if (nativeHookDataReader->has_tag_event()) {
        ParseTagEvent(nativeHookDataReader->tag_event());
    } else if (nativeHookDataReader->has_file_path()) {
        ParseFileEvent(nativeHookDataReader->file_path());
    } else if (nativeHookDataReader->has_symbol_name()) {
        ParseSymbolEvent(nativeHookDataReader->symbol_name());
    } else if (nativeHookDataReader->has_thread_name_map()) {
        ParseThreadEvent(nativeHookDataReader->thread_name_map());
    } else if (nativeHookDataReader->has_alloc_event()) {
        ParseNativeHookEvent(TRACE_NATIVE_HOOK_MALLOC, itor->first, nativeHookDataReader->alloc_event());
    } else if (nativeHookDataReader->has_free_event()) {
        ParseNativeHookEvent(TRACE_NATIVE_HOOK_FREE, itor->first, nativeHookDataReader->free_event());
    } else if (nativeHookDataReader->has_mmap_event()) {
        ParseNativeHookEvent(TRACE_NATIVE_HOOK_MMAP, itor->first, nativeHookDataReader->mmap_event());
    } else if (nativeHookDataReader->has_munmap_event()) {
        ParseNativeHookEvent(TRACE_NATIVE_HOOK_MUNMAP, itor->first, nativeHookDataReader->munmap_event());
    } else if (nativeHookDataReader->has_statistics_event()) {
        ParseNativeHookEvent(TRACE_NATIVE_HOOK_RECORD_STATISTICS, itor->first,
                             nativeHookDataReader->statistics_event());
    }
}

void HtraceNativeHookParser::MaybeParseNativeHookData()
{
    if (tsNativeHookQueue_.size() > MAX_CACHE_SIZE) {
        ParseOneNativeHookData(tsNativeHookQueue_.begin());
        tsNativeHookQueue_.erase(tsNativeHookQueue_.begin());
    }
}
// In order to improve the accuracy of data, it is necessary to sort the original data.
// Data sorting will be reduced by 5% to 10% Speed of parsing data.
void HtraceNativeHookParser::SortNativeHookData(HtraceDataSegment& dataSeg)
{
    auto batchNativeHookDataReader = ProtoReader::BatchNativeHookData_Reader(dataSeg.protoData);
    for (auto itor = batchNativeHookDataReader.events(); itor; itor++) {
        auto nativeHookDataReader = std::make_unique<ProtoReader::NativeHookData_Reader>(itor->ToBytes());
        auto timeStamp = nativeHookDataReader->tv_nsec() + nativeHookDataReader->tv_sec() * SEC_TO_NS;
        auto nativeHookMetaData = std::make_unique<NativeHookMetaData>(dataSeg.seg, std::move(nativeHookDataReader));
        tsNativeHookQueue_.insert(std::make_pair(timeStamp, std::move(nativeHookMetaData)));
        MaybeParseNativeHookData();
    }
    return;
}
void HtraceNativeHookParser::ParseConfigInfo(HtraceDataSegment& dataSeg)
{
    nativeHookFilter_->ParseConfigInfo(dataSeg.protoData);
}
void HtraceNativeHookParser::FinishParseNativeHookData()
{
    for (auto it = tsNativeHookQueue_.begin(); it != tsNativeHookQueue_.end(); it++) {
        ParseOneNativeHookData(it);
    }
    tsNativeHookQueue_.clear();
    nativeHookFilter_->FinishParseNativeHookData();
}
void HtraceNativeHookParser::Finish()
{
    if (GetPluginStartTime() != GetPluginEndTime()) {
        traceDataCache_->MixTraceTime(GetPluginStartTime(), GetPluginEndTime());
    } else {
        traceDataCache_->MixTraceTime(GetPluginStartTime(), GetPluginStartTime() + 1);
    }
}
} // namespace TraceStreamer
} // namespace SysTuning

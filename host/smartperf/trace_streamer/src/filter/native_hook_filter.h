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
#ifndef NATIVE_HOOK_FILTER_H
#define NATIVE_HOOK_FILTER_H
#include <map>
#include <queue>
#include <set>
#include <unordered_map>
#include "double_map.h"
#include "filter_base.h"
#include "native_hook_config.pbreader.h"
#include "native_hook_result.pbreader.h"
#include "numerical_to_string.h"
#include "offline_symbolization_filter.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"
#include "triple_map.h"

namespace SysTuning {
namespace TraceStreamer {
class NativeHookFrameInfo {
public:
    NativeHookFrameInfo()
        : ip_(INVALID_UINT64),
          sp_(INVALID_UINT64),
          symbolIndex_(INVALID_UINT64),
          filePathIndex_(INVALID_UINT64),
          offset_(INVALID_UINT64),
          symbolOffset_(INVALID_UINT64)
    {
    }
    NativeHookFrameInfo(uint64_t ip,
                        uint64_t sp,
                        uint64_t symbolIndex,
                        uint64_t filePathIndex,
                        uint64_t offset,
                        uint64_t symbolOffset)
        : ip_(ip),
          sp_(sp),
          symbolIndex_(symbolIndex),
          filePathIndex_(filePathIndex),
          offset_(offset),
          symbolOffset_(symbolOffset)
    {
    }
    ~NativeHookFrameInfo() {}
    uint64_t ip_;
    uint64_t sp_;
    uint64_t symbolIndex_;
    uint64_t filePathIndex_;
    uint64_t offset_;
    uint64_t symbolOffset_;
};

class NativeHookFilter : private FilterBase {
public:
    NativeHookFilter(TraceDataCache*, const TraceStreamerFilters*);
    NativeHookFilter(const NativeHookFilter&) = delete;
    NativeHookFilter& operator=(const NativeHookFilter&) = delete;
    ~NativeHookFilter() override;

public:
    void ParseConfigInfo(ProtoReader::BytesView& protoData);
    void AppendStackMaps(uint32_t stackid, std::vector<uint64_t>& frames);
    void AppendFrameMaps(uint32_t id, const ProtoReader::BytesView& bytesView);
    void AppendFilePathMaps(uint32_t id, uint64_t fileIndex);
    void AppendSymbolMap(uint32_t id, uint64_t symbolIndex);
    void AppendThreadNameMap(uint32_t id, uint64_t threadNameIndex);
    void ParseEvent(SupportedTraceEventType type, uint64_t timeStamp, const ProtoReader::BytesView& bytesView);
    void ParseStatisticEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView);
    void ParseAllocEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView);
    void ParseFreeEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView);
    void ParseMmapEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView);
    void ParseMunmapEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView);
    void FinishParseNativeHookData();
    std::shared_ptr<OfflineSymbolizationFilter> GetOfflineSymbolizationObj()
    {
        return offlineSymbolization_;
    }
    template <class T1, class T2>
    void UpdateMap(std::unordered_map<T1, T2>& sourceMap, T1 key, T2 value);

private:
    void MaybeUpdateCurrentSizeDur(uint64_t row, uint64_t timeStamp, bool isMalloc);
    void UpdateThreadNameWithNativeHookData() const;
    void GetCallIdToLastLibId();
    void GetNativeHookFrameVaddrs();
    void UpdateSymbolIdByOffline();
    void ParseFramesInOfflineSymbolizationMode();
    void ParseFramesInCallStackCompressedMode();
    void ParseFramesWithOutCallStackCompressedMode();
    void ParseNativeHookFrame();
    void CompressStackAndFrames(ProtoReader::RepeatedDataAreaIterator<ProtoReader::BytesView> frames);
    std::unique_ptr<NativeHookFrameInfo> ParseFrame(const ProtoReader::DataArea& frame);

private:
    // stores frames info. if offlineSymbolization is true, the second storing ips data, else storing FrameMap id.
    std::map<uint64_t, std::shared_ptr<ProtoReader::RepeatedDataAreaIterator<ProtoReader::BytesView>>> rowToFrames_ =
        {};
    std::unordered_map<uint32_t, std::shared_ptr<const ProtoReader::BytesView>> frameIdToFrameBytes_ = {};
    std::unordered_map<uint64_t, std::vector<uint64_t>> stackHashValueToFramesHashMap_ = {};
    std::unordered_map<uint64_t, std::unique_ptr<NativeHookFrameInfo>> frameHashToFrameInfoMap_ = {};
    std::unordered_map<uint32_t, uint64_t> threadNameIdToThreadNameIndex_ = {};
    std::map<uint32_t, std::vector<uint64_t>> stackIdToFrames_ = {};
    std::unordered_map<uint32_t, uint64_t> callIdToLastCallerPathIndex_ = {};
    std::unordered_map<uint64_t, std::string> functionNameIndexToVaddr_ = {};
    std::unordered_map<uint32_t, uint64_t> symbolIdToSymbolIndex_ = {};
    std::map<uint32_t, uint64_t> callChainIdToStackHashValueMap_ = {};
    std::unordered_map<uint64_t, uint32_t> stackHashValueToCallChainIdMap_ = {};
    std::unordered_map<uint32_t, uint32_t> itidToThreadNameId_ = {};
    std::unordered_map<uint32_t, uint64_t> fileIdToFileIndex_ = {};
    std::set<DataIndex> invalidLibPathIndexs_ = {};
    std::deque<std::string> vaddrs_ = {};
    std::shared_ptr<OfflineSymbolizationFilter> offlineSymbolization_;
    DoubleMap<uint32_t, uint64_t, uint64_t> addrToAllocEventRow_;
    DoubleMap<uint32_t, uint64_t, uint64_t> addrToMmapEventRow_;
    uint64_t lastMallocEventRaw_ = INVALID_UINT64;
    uint64_t lastMmapEventRaw_ = INVALID_UINT64;
    std::hash<std::string_view> hashFun_;
    uint32_t pid_ = INVALID_UINT32;
    bool isOfflineSymbolizationMode_ = false;
    bool isCallStackCompressedMode_ = false;
    bool isStringCompressedMode_ = false;
    bool isStatisticMode_ = false;
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // NATIVE_HOOK_FILTER_H

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

#include "native_hook_filter.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "string_to_numerical.h"
namespace SysTuning {
namespace TraceStreamer {
NativeHookFilter::NativeHookFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter), addrToAllocEventRow_(INVALID_UINT64), addrToMmapEventRow_(INVALID_UINT64)
{
    invalidLibPathIndexs_.insert(traceDataCache_->dataDict_.GetStringIndex("/system/lib/libc++.so"));
    invalidLibPathIndexs_.insert(traceDataCache_->dataDict_.GetStringIndex("/system/lib64/libc++.so"));
    invalidLibPathIndexs_.insert(traceDataCache_->dataDict_.GetStringIndex("/system/lib/ld-musl-aarch64.so.1"));
    invalidLibPathIndexs_.insert(traceDataCache_->dataDict_.GetStringIndex("/system/lib/ld-musl-arm.so.1"));
    offlineSymbolization_ = std::make_shared<OfflineSymbolizationFilter>(dataCache, filter);
}
NativeHookFilter::~NativeHookFilter() = default;

void NativeHookFilter::ParseConfigInfo(ProtoReader::BytesView& protoData)
{
    auto configReader = ProtoReader::NativeHookConfig_Reader(protoData);
    if (configReader.has_statistics_interval()) {
        isStatisticMode_ = true;
        isCallStackCompressedMode_ = true;
        isStringCompressedMode_ = true;
    }
    if (configReader.has_offline_symbolization()) {
        isOfflineSymbolizationMode_ = true;
        isCallStackCompressedMode_ = true;
        isStringCompressedMode_ = true;
        return;
    }
    if (configReader.has_callframe_compressed()) {
        isCallStackCompressedMode_ = true;
        isStringCompressedMode_ = true;
        return;
    }
    if (configReader.has_string_compressed()) {
        isStringCompressedMode_ = true;
        return;
    }
    return;
}
void NativeHookFilter::AppendStackMaps(uint32_t stackid, std::vector<uint64_t>& frames)
{
    stackIdToFrames_.emplace(std::make_pair(stackid, std::move(frames)));
}
void NativeHookFilter::AppendFrameMaps(uint32_t id, const ProtoReader::BytesView& bytesView)
{
    auto frames = std::make_shared<const ProtoReader::BytesView>(bytesView);
    frameIdToFrameBytes_.emplace(std::make_pair(id, frames));
}
void NativeHookFilter::AppendFilePathMaps(uint32_t id, uint64_t fileIndex)
{
    fileIdToFileIndex_.emplace(id, fileIndex);
}
void NativeHookFilter::AppendSymbolMap(uint32_t id, uint64_t symbolIndex)
{
    symbolIdToSymbolIndex_.emplace(id, symbolIndex);
}
void NativeHookFilter::AppendThreadNameMap(uint32_t id, uint64_t threadNameIndex)
{
    threadNameIdToThreadNameIndex_.emplace(id, threadNameIndex);
}

void NativeHookFilter::ParseEvent(SupportedTraceEventType type,
                                  uint64_t timeStamp,
                                  const ProtoReader::BytesView& bytesView)
{
    switch (type) {
        case TRACE_NATIVE_HOOK_MALLOC:
            ParseAllocEvent(timeStamp, bytesView);
            break;
        case TRACE_NATIVE_HOOK_FREE:
            ParseFreeEvent(timeStamp, bytesView);
            break;
        case TRACE_NATIVE_HOOK_MMAP:
            ParseMmapEvent(timeStamp, bytesView);
            break;
        case TRACE_NATIVE_HOOK_MUNMAP:
            ParseMunmapEvent(timeStamp, bytesView);
            break;
        case TRACE_NATIVE_HOOK_RECORD_STATISTICS:
            ParseStatisticEvent(timeStamp, bytesView);
            break;
        default:
            TS_LOGE("unsupported native hook events!");
    }
}

void NativeHookFilter::ParseStatisticEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView)
{
    ProtoReader::RecordStatisticsEvent_Reader reader(bytesView);
    if (pid_ == INVALID_UINT32) {
        pid_ = reader.pid();
    }
    auto ipid = streamFilters_->processFilter_->GetOrCreateInternalPid(timeStamp, reader.pid());
    traceDataCache_->GetNativeHookStatisticsData()->AppendNewNativeHookStatistic(
        ipid, timeStamp, reader.callstack_id(), reader.type(), reader.apply_count(), reader.release_count(),
        reader.apply_size(), reader.release_size());
}

template <class T1, class T2>
void NativeHookFilter::UpdateMap(std::unordered_map<T1, T2>& sourceMap, T1 key, T2 value)
{
    auto itor = sourceMap.find(key);
    if (itor != sourceMap.end()) {
        itor->second = value;
    } else {
        sourceMap.insert(std::make_pair(key, value));
    }
}
std::unique_ptr<NativeHookFrameInfo> NativeHookFilter::ParseFrame(const ProtoReader::DataArea& frame)
{
    ProtoReader::Frame_Reader reader(frame.Data(), frame.Size());
    uint64_t symbolIndex = INVALID_UINT64;
    uint64_t filePathIndex = INVALID_UINT64;
    if (isStringCompressedMode_) {
        if (!symbolIdToSymbolIndex_.count(reader.symbol_name_id())) {
            TS_LOGE("Native hook ParseFrame find symbol id failed!!!");
            return nullptr;
        }
        symbolIndex = symbolIdToSymbolIndex_.at(reader.symbol_name_id());

        if (!fileIdToFileIndex_.count(reader.file_path_id())) {
            TS_LOGE("Native hook ParseFrame find file path id failed!!!");
            return nullptr;
        }
        filePathIndex = fileIdToFileIndex_.at(reader.file_path_id());
    } else {
        symbolIndex = traceDataCache_->dataDict_.GetStringIndex(reader.symbol_name().ToStdString());
        filePathIndex = traceDataCache_->dataDict_.GetStringIndex(reader.file_path().ToStdString());
    }
    auto frameInfo = std::make_unique<NativeHookFrameInfo>(reader.ip(), reader.sp(), symbolIndex, filePathIndex,
                                                           reader.offset(), reader.symbol_offset());
    return std::move(frameInfo);
}

void NativeHookFilter::CompressStackAndFrames(ProtoReader::RepeatedDataAreaIterator<ProtoReader::BytesView> frames)
{
    std::vector<uint64_t> framesHash;
    uint64_t frameHash = INVALID_UINT64;
    std::string framesHashStr = "";
    for (auto itor = frames; itor; itor++) {
        std::string_view frameStr(reinterpret_cast<const char*>(itor->Data()), itor->Size());
        auto frameHash = hashFun_(frameStr);
        if (!frameHashToFrameInfoMap_.count(frameHash)) {
            // the frame compression is completed and the frame is parsed.
            auto frameInfo = ParseFrame(itor.GetDataArea());
            frameHashToFrameInfoMap_.emplace(std::make_pair(frameHash, std::move(frameInfo)));
        }
        framesHash.emplace_back(frameHash);
        framesHashStr.append("+");
        framesHashStr.append(std::to_string(frameHash));
    }
    auto stackHashValue = hashFun_(framesHashStr);
    uint32_t callChainId = INVALID_UINT32;
    if (!stackHashValueToCallChainIdMap_.count(stackHashValue)) {
        callChainId = callChainIdToStackHashValueMap_.size() + 1;
        callChainIdToStackHashValueMap_.emplace(std::make_pair(callChainId, stackHashValue));
        stackHashValueToCallChainIdMap_.emplace(std::make_pair(stackHashValue, callChainId));
        stackHashValueToFramesHashMap_.emplace(std::make_pair(stackHashValue, std::move(framesHash)));
    } else {
        callChainId = stackHashValueToCallChainIdMap_[stackHashValue];
    }
    // When compressing the call stack, update the callChainId of the nativeHook table
    auto row = traceDataCache_->GetNativeHookData()->Size() - 1;
    traceDataCache_->GetNativeHookData()->UpdateCallChainId(row, callChainId);
}
void NativeHookFilter::ParseAllocEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView)
{
    ProtoReader::AllocEvent_Reader allocEventReader(bytesView);
    uint32_t callChainId = INVALID_UINT32;
    // compressed call stack
    if (allocEventReader.has_stack_id()) {
        callChainId = allocEventReader.stack_id();
    }
    if (pid_ == INVALID_UINT32) {
        pid_ = allocEventReader.pid();
    }
    auto itid =
        streamFilters_->processFilter_->GetOrCreateThreadWithPid(allocEventReader.tid(), allocEventReader.pid());
    auto ipid = streamFilters_->processFilter_->GetInternalPid(allocEventReader.pid());
    if (allocEventReader.has_thread_name_id()) {
        UpdateMap(itidToThreadNameId_, itid, allocEventReader.thread_name_id());
    }
    auto row = traceDataCache_->GetNativeHookData()->AppendNewNativeHookData(
        callChainId, ipid, itid, "AllocEvent", INVALID_UINT64, timeStamp, 0, 0, allocEventReader.addr(),
        allocEventReader.size());
    addrToAllocEventRow_.Insert(ipid, allocEventReader.addr(), static_cast<uint64_t>(row));
    if (allocEventReader.size() != 0) {
        MaybeUpdateCurrentSizeDur(row, timeStamp, true);
    }
    // Uncompressed call stack
    if (allocEventReader.has_frame_info()) {
        CompressStackAndFrames(allocEventReader.frame_info());
    }
}

void NativeHookFilter::ParseFreeEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView)
{
    ProtoReader::FreeEvent_Reader freeEventReader(bytesView);
    uint32_t callChainId = INVALID_UINT32;
    if (freeEventReader.has_stack_id()) {
        callChainId = freeEventReader.stack_id();
    }
    if (pid_ == INVALID_UINT32) {
        pid_ = freeEventReader.pid();
    }
    auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(freeEventReader.tid(), freeEventReader.pid());
    auto ipid = streamFilters_->processFilter_->GetInternalPid(freeEventReader.pid());
    if (freeEventReader.thread_name_id() != 0) {
        UpdateMap(itidToThreadNameId_, itid, freeEventReader.thread_name_id());
    }
    int64_t freeHeapSize = 0;
    // Find a matching malloc event, and if the matching fails, do not write to the database
    auto row = addrToAllocEventRow_.Find(ipid, freeEventReader.addr());
    if (row != INVALID_UINT64 && timeStamp > traceDataCache_->GetNativeHookData()->TimeStampData()[row]) {
        addrToAllocEventRow_.Erase(ipid, freeEventReader.addr());
        traceDataCache_->GetNativeHookData()->UpdateEndTimeStampAndDuration(row, timeStamp);
        freeHeapSize = traceDataCache_->GetNativeHookData()->MemSizes()[row];
    } else if (row == INVALID_UINT64) {
        TS_LOGD("func addr:%lu is empty", freeEventReader.addr());
        streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_FREE, STAT_EVENT_DATA_INVALID);
        return;
    }

    row = traceDataCache_->GetNativeHookData()->AppendNewNativeHookData(
        callChainId, ipid, itid, "FreeEvent", INVALID_UINT64, timeStamp, 0, 0, freeEventReader.addr(), freeHeapSize);
    if (freeHeapSize != 0) {
        MaybeUpdateCurrentSizeDur(row, timeStamp, true);
    }
    // Uncompressed call stack
    if (freeEventReader.has_frame_info()) {
        CompressStackAndFrames(freeEventReader.frame_info());
    }
}

void NativeHookFilter::ParseMmapEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView)
{
    ProtoReader::MmapEvent_Reader mMapEventReader(bytesView);
    uint32_t callChainId = INVALID_UINT32;
    if (mMapEventReader.has_stack_id()) {
        callChainId = mMapEventReader.stack_id();
    }
    if (pid_ == INVALID_UINT32) {
        pid_ = mMapEventReader.pid();
    }
    auto itid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(mMapEventReader.tid(), mMapEventReader.pid());
    auto ipid = streamFilters_->processFilter_->GetInternalPid(mMapEventReader.pid());
    // Update the mapping of tid to thread name id.
    if (mMapEventReader.thread_name_id() != 0) {
        UpdateMap(itidToThreadNameId_, itid, mMapEventReader.thread_name_id());
    }
    // Gets the index of the mmap event's label in the data dictionary
    DataIndex subType = INVALID_UINT64;
    if (mMapEventReader.has_type()) {
        subType = traceDataCache_->dataDict_.GetStringIndex(mMapEventReader.type().ToStdString());
        // Establish a mapping of addr and size to the mmap tag index.
        traceDataCache_->GetNativeHookData()->UpdateAddrToMemMapSubType(mMapEventReader.addr(), subType);
    }
    auto row = traceDataCache_->GetNativeHookData()->AppendNewNativeHookData(
        callChainId, ipid, itid, "MmapEvent", subType, timeStamp, 0, 0, mMapEventReader.addr(), mMapEventReader.size());
    addrToMmapEventRow_.Insert(ipid, mMapEventReader.addr(), static_cast<uint64_t>(row));
    // update currentSizeDur.
    if (mMapEventReader.size()) {
        MaybeUpdateCurrentSizeDur(row, timeStamp, false);
    }
    // Uncompressed call stack
    if (mMapEventReader.has_frame_info()) {
        CompressStackAndFrames(mMapEventReader.frame_info());
    }
}

void NativeHookFilter::ParseMunmapEvent(uint64_t timeStamp, const ProtoReader::BytesView& bytesView)
{
    ProtoReader::MunmapEvent_Reader mUnmapEventReader(bytesView);
    uint32_t callChainId = INVALID_UINT32;
    if (mUnmapEventReader.has_stack_id()) {
        callChainId = mUnmapEventReader.stack_id();
    }
    if (pid_ == INVALID_UINT32) {
        pid_ = mUnmapEventReader.pid();
    }
    auto itid =
        streamFilters_->processFilter_->GetOrCreateThreadWithPid(mUnmapEventReader.tid(), mUnmapEventReader.pid());
    auto ipid = streamFilters_->processFilter_->GetInternalPid(mUnmapEventReader.pid());
    if (mUnmapEventReader.thread_name_id() != 0) {
        UpdateMap(itidToThreadNameId_, itid, mUnmapEventReader.thread_name_id());
    }
    // Query for MMAP events that match the current data. If there are no matching MMAP events, the current data is not
    // written to the database.
    auto row = addrToMmapEventRow_.Find(ipid, mUnmapEventReader.addr());
    if (row != INVALID_UINT64 && timeStamp > traceDataCache_->GetNativeHookData()->TimeStampData()[row]) {
        addrToMmapEventRow_.Erase(ipid, mUnmapEventReader.addr());
        traceDataCache_->GetNativeHookData()->UpdateEndTimeStampAndDuration(row, timeStamp);
    } else if (row == INVALID_UINT64) {
        TS_LOGD("func addr:%lu is empty", mUnmapEventReader.addr());
        streamFilters_->statFilter_->IncreaseStat(TRACE_NATIVE_HOOK_MUNMAP, STAT_EVENT_DATA_INVALID);
        return;
    }
    row = traceDataCache_->GetNativeHookData()->AppendNewNativeHookData(
        callChainId, ipid, itid, "MunmapEvent", INVALID_UINT64, timeStamp, 0, 0, mUnmapEventReader.addr(),
        mUnmapEventReader.size());
    if (mUnmapEventReader.size() != 0) {
        MaybeUpdateCurrentSizeDur(row, timeStamp, false);
    }
    // Uncompressed call stack
    if (mUnmapEventReader.has_frame_info()) {
        CompressStackAndFrames(mUnmapEventReader.frame_info());
    }
}

void NativeHookFilter::MaybeUpdateCurrentSizeDur(uint64_t row, uint64_t timeStamp, bool isMalloc)
{
    auto& lastAnyEventRaw = isMalloc ? lastMallocEventRaw_ : lastMmapEventRaw_;
    if (lastAnyEventRaw != INVALID_UINT64) {
        traceDataCache_->GetNativeHookData()->UpdateCurrentSizeDur(lastAnyEventRaw, timeStamp);
    }
    lastAnyEventRaw = row;
}
void NativeHookFilter::UpdateSymbolIdByOffline()
{
    auto size = traceDataCache_->GetNativeHookFrameData()->Size();
    for (auto i = 0; i < size; ++i) {
        if (traceDataCache_->GetNativeHookFrameData()->SymbolNames()[i] == INVALID_UINT64) {
            auto filePathIndex = traceDataCache_->GetNativeHookFrameData()->FilePaths()[i];
            auto filePathStr = traceDataCache_->dataDict_.GetDataFromDict(filePathIndex);
            auto vaddrStr = traceDataCache_->GetNativeHookFrameData()->Vaddrs()[i];
            traceDataCache_->GetNativeHookFrameData()->UpdateSymbolId(
                i, traceDataCache_->dataDict_.GetStringIndex(filePathStr + "+" + vaddrStr));
        }
    }
}
void NativeHookFilter::ParseFramesInOfflineSymbolizationMode()
{
    for (auto stackIdToFramesItor = stackIdToFrames_.begin(); stackIdToFramesItor != stackIdToFrames_.end();
         stackIdToFramesItor++) {
        auto framesInfo = offlineSymbolization_->Parse(pid_, stackIdToFramesItor->second);
        uint64_t depth = 0;
        uint64_t filePathIndex = INVALID_UINT64;
        for (auto itor = framesInfo->rbegin(); itor != framesInfo->rend(); itor++) {
            // Note that the filePathId here is provided for the end side. Not a true TS internal index dictionary.
            auto frameInfo = itor->get();
            if (fileIdToFileIndex_.count(frameInfo->filePathId_)) {
                filePathIndex = fileIdToFileIndex_.at(frameInfo->filePathId_);
            } else {
                filePathIndex = INVALID_UINT64;
            }
            std::string vaddr = base::Uint64ToHexText(frameInfo->symVaddr_);
            traceDataCache_->GetNativeHookFrameData()->AppendNewNativeHookFrame(
                stackIdToFramesItor->first, depth, frameInfo->ip_, INVALID_UINT64, frameInfo->symbolIndex_,
                filePathIndex, frameInfo->offset_, frameInfo->symbolOffset_, vaddr);
            depth++;
        }
    }
    UpdateSymbolIdByOffline();
}

void NativeHookFilter::GetNativeHookFrameVaddrs()
{
    auto size = traceDataCache_->GetNativeHookFrameData()->Size();
    // Traverse every piece of native_hook frame data
    for (auto i = 0; i < size; i++) {
        auto symbolOffset = traceDataCache_->GetNativeHookFrameData()->SymbolOffsets()[i];
        // When the symbol offset is not 0, vaddr=offset+symbol offset
        if (symbolOffset) {
            auto fileOffset = traceDataCache_->GetNativeHookFrameData()->Offsets()[i];
            auto vaddr = base::Uint64ToHexText(fileOffset + symbolOffset);
            vaddrs_.emplace_back(vaddr);
            continue;
        }
        // When the symbol offset is 0, vaddr takes the string after the plus sign in the function name
        auto functionNameIndex = traceDataCache_->GetNativeHookFrameData()->SymbolNames()[i];
        std::string vaddr = "";
        auto itor = functionNameIndexToVaddr_.find(functionNameIndex);
        if (itor == functionNameIndexToVaddr_.end()) {
            auto functionName = traceDataCache_->dataDict_.GetDataFromDict(functionNameIndex);
            auto pos = functionName.rfind("+");
            if (pos != functionName.npos && pos != functionName.length() - 1) {
                vaddr = functionName.substr(++pos);
            }
            // Vaddr keeps "" when lookup failed
            functionNameIndexToVaddr_.emplace(std::make_pair(functionNameIndex, vaddr));
        } else {
            vaddr = itor->second;
        }
        vaddrs_.emplace_back(vaddr);
    }
}
void NativeHookFilter::ParseFramesInCallStackCompressedMode()
{
    for (auto stackIdToFramesItor = stackIdToFrames_.begin(); stackIdToFramesItor != stackIdToFrames_.end();
         stackIdToFramesItor++) {
        auto frameIds = stackIdToFramesItor->second;
        uint64_t depth = 0;
        for (auto frameIdsItor = frameIds.crbegin(); frameIdsItor != frameIds.crend(); frameIdsItor++) {
            if (!frameIdToFrameBytes_.count(*frameIdsItor)) {
                TS_LOGE("Can not find Frame by frame_map_id!!!");
                continue;
            }
            ProtoReader::Frame_Reader reader(*(frameIdToFrameBytes_.at(*frameIdsItor)));

            if (!reader.has_file_path_id() or !reader.has_symbol_name_id()) {
                TS_LOGE("Data exception, frames should has fil_path_id and symbol_name_id");
                continue;
            }
            if (!fileIdToFileIndex_.count(reader.file_path_id())) {
                TS_LOGE("Data exception, can not find fil_path_id!!!");
                continue;
            }
            auto& filePathIndex = fileIdToFileIndex_.at(reader.file_path_id());
            if (!symbolIdToSymbolIndex_.count(reader.symbol_name_id())) {
                TS_LOGE("Data exception, can not find symbol_name_id!!!");
                continue;
            }
            auto& symbolIndex = symbolIdToSymbolIndex_.at(reader.symbol_name_id());
            traceDataCache_->GetNativeHookFrameData()->AppendNewNativeHookFrame(
                stackIdToFramesItor->first, depth, reader.ip(), reader.sp(), symbolIndex, filePathIndex,
                reader.offset(), reader.symbol_offset());
            depth++;
        }
    }
}

void NativeHookFilter::ParseFramesWithOutCallStackCompressedMode()
{
    for (auto itor = callChainIdToStackHashValueMap_.begin(); itor != callChainIdToStackHashValueMap_.end(); itor++) {
        auto callChainId = itor->first;
        if (!stackHashValueToFramesHashMap_.count(itor->second)) {
            continue;
        }
        auto& framesHash = stackHashValueToFramesHashMap_.at(itor->second);
        uint64_t depth = 0;
        for (auto frameHashValueVectorItor = framesHash.crbegin(); frameHashValueVectorItor != framesHash.crend();
             frameHashValueVectorItor++) {
            if (!frameHashToFrameInfoMap_.count(*frameHashValueVectorItor)) {
                TS_LOGE("find matching frameInfo failed!!!!");
                return;
            }
            auto& frameInfo = frameHashToFrameInfoMap_.at(*frameHashValueVectorItor);
            traceDataCache_->GetNativeHookFrameData()->AppendNewNativeHookFrame(
                callChainId, depth, frameInfo->ip_, frameInfo->sp_, frameInfo->symbolIndex_, frameInfo->filePathIndex_,
                frameInfo->offset_, frameInfo->symbolOffset_);
            depth++;
        }
    }
}
void NativeHookFilter::ParseNativeHookFrame()
{
    // when isOfflineSymbolizationMode is true, the isCallStackCompressedMode is true too.
    if (isOfflineSymbolizationMode_) {
        ParseFramesInOfflineSymbolizationMode();
        return;
    }
    // isOfflineSymbolizationMode is false, but isCallStackCompressedMode is true.
    if (isCallStackCompressedMode_) {
        ParseFramesInCallStackCompressedMode();
    } else {
        ParseFramesWithOutCallStackCompressedMode();
    }
    GetNativeHookFrameVaddrs();
    traceDataCache_->GetNativeHookFrameData()->UpdateVaddrs(vaddrs_);
    return;
}
void NativeHookFilter::UpdateThreadNameWithNativeHookData() const
{
    if (itidToThreadNameId_.empty() || threadNameIdToThreadNameIndex_.empty()) {
        return;
    }
    for (auto itor = itidToThreadNameId_.begin(); itor != itidToThreadNameId_.end(); ++itor) {
        auto thread = traceDataCache_->GetThreadData(itor->first);
        if (!thread->nameIndex_) {
            auto threadNameMapItor = threadNameIdToThreadNameIndex_.find(itor->second);
            if (threadNameMapItor != threadNameIdToThreadNameIndex_.end()) {
                thread->nameIndex_ = threadNameMapItor->second;
            }
        }
    }
}
void NativeHookFilter::FinishParseNativeHookData()
{
    ParseNativeHookFrame();
    traceDataCache_->GetNativeHookData()->UpdateMemMapSubType();
    // update last lib id
    GetCallIdToLastLibId();
    if (callIdToLastCallerPathIndex_.size()) {
        traceDataCache_->GetNativeHookData()->UpdateLastCallerPathIndexs(callIdToLastCallerPathIndex_);
    }
    UpdateThreadNameWithNativeHookData();

    threadNameIdToThreadNameIndex_.clear();
    callIdToLastCallerPathIndex_.clear();
    functionNameIndexToVaddr_.clear();
    vaddrs_.clear();
    rowToFrames_.clear();
    frameIdToFrameBytes_.clear();
    stackHashValueToFramesHashMap_.clear();
    frameHashToFrameInfoMap_.clear();
    stackIdToFrames_.clear();
    symbolIdToSymbolIndex_.clear();
    callChainIdToStackHashValueMap_.clear();
    stackHashValueToCallChainIdMap_.clear();
    itidToThreadNameId_.clear();
    fileIdToFileIndex_.clear();
    invalidLibPathIndexs_.clear();
    addrToAllocEventRow_.Clear();
    addrToMmapEventRow_.Clear();
}
void NativeHookFilter::GetCallIdToLastLibId()
{
    auto size = static_cast<int64_t>(traceDataCache_->GetNativeHookFrameData()->Size());
    uint32_t lastCallChainId = INVALID_UINT32;
    bool foundLast = false;
    for (auto i = size - 1; i > -1; i--) {
        auto callChainId = traceDataCache_->GetNativeHookFrameData()->CallChainIds()[i];
        if (callChainId == lastCallChainId) {
            if (foundLast) {
                continue;
            }
        }
        if (callChainId != lastCallChainId) {
            lastCallChainId = callChainId;
            foundLast = false;
        }
        auto filePathIndex = traceDataCache_->GetNativeHookFrameData()->FilePaths()[i];
        if (!traceDataCache_->GetNativeHookFrameData()->Depths()[i]) {
            callIdToLastCallerPathIndex_.insert(std::make_pair(callChainId, filePathIndex));
            foundLast = true;
            continue;
        }

        auto lower = std::lower_bound(invalidLibPathIndexs_.begin(), invalidLibPathIndexs_.end(), filePathIndex);
        if (lower == invalidLibPathIndexs_.end() || *lower != filePathIndex) { // found
            auto filePath = traceDataCache_->dataDict_.GetDataFromDict(filePathIndex);
            auto ret = filePath.find("libc++_shared.so");
            if (ret == filePath.npos) {
                callIdToLastCallerPathIndex_.insert(std::make_pair(callChainId, filePathIndex));
                foundLast = true;
            }
        }
    }
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "slice_filter.h"
#include <cstdint>
#include <limits>
#include <optional>

#include "args_filter.h"
#include "log.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "string_to_numerical.h"
#include "ts_common.h"

namespace SysTuning {
namespace TraceStreamer {
using namespace SysTuning::base;
SliceFilter::SliceFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter), asyncEventMap_(INVALID_UINT64)
{
}

SliceFilter::~SliceFilter() = default;

bool SliceFilter::BeginSlice(const std::string& comm,
                             uint64_t timestamp,
                             uint32_t pid,
                             uint32_t threadGroupId,
                             DataIndex cat,
                             DataIndex nameIndex)
{
    InternalTid internalTid = INVALID_UTID;
    if (threadGroupId > 0) {
        internalTid = streamFilters_->processFilter_->UpdateOrCreateThreadWithPidAndName(pid, threadGroupId, comm);
        pidTothreadGroupId_[pid] = threadGroupId;
    } else {
        internalTid = streamFilters_->processFilter_->UpdateOrCreateThreadWithName(timestamp, pid, comm);
    }
    // make a SliceData DataItem, {timestamp, dur, internalTid, cat, nameIndex}
    struct SliceData sliceData = {timestamp, -1, internalTid, cat, nameIndex};
    return BeginSliceInternal(sliceData);
}

void SliceFilter::IrqHandlerEntry(uint64_t timestamp, uint32_t cpu, DataIndex catalog, DataIndex nameIndex)
{
    struct SliceData sliceData = {timestamp, 0, cpu, catalog, nameIndex};
    auto slices = traceDataCache_->GetIrqData();
    size_t index = slices->AppendInternalSlice(
        sliceData.timestamp, sliceData.duration, sliceData.internalTid, sliceData.cat,
        GetNameASCIISumNoNum(traceDataCache_->GetDataFromDict(sliceData.name)), sliceData.name, 0, std::nullopt);
    if (irqEventMap_.count(cpu)) {
        // not match
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_ENTRY, STAT_EVENT_DATA_LOST);
        irqEventMap_.at(cpu) = {timestamp, index};
    } else {
        irqEventMap_[cpu] = {timestamp, index};
    }
    slices->AppendDistributeInfo();
    return;
}

void SliceFilter::IrqHandlerExit(uint64_t timestamp, uint32_t cpu, ArgsSet args)
{
    if (!irqEventMap_.count(cpu)) {
        // not match
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_IRQ_HANDLER_EXIT, STAT_EVENT_NOTMATCH);
        return;
    }
    uint32_t argSetId = INVALID_UINT32;
    auto slices = traceDataCache_->GetIrqData();
    argSetId = streamFilters_->argsFilter_->NewArgs(args);
    slices->SetDurationAndArg(irqEventMap_.at(cpu).row, timestamp, argSetId);
    irqEventMap_.erase(cpu);
    return;
}

void SliceFilter::SoftIrqEntry(uint64_t timestamp, uint32_t cpu, DataIndex catalog, DataIndex nameIndex)
{
    struct SliceData sliceData = {timestamp, 0, cpu, catalog, nameIndex};
    auto slices = traceDataCache_->GetIrqData();
    size_t index = slices->AppendInternalSlice(
        sliceData.timestamp, sliceData.duration, sliceData.internalTid, sliceData.cat,
        GetNameASCIISumNoNum(traceDataCache_->GetDataFromDict(sliceData.name)), sliceData.name, 0, std::nullopt);
    if (softIrqEventMap_.count(cpu)) {
        // not match
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SOFTIRQ_ENTRY, STAT_EVENT_DATA_LOST);
        softIrqEventMap_.at(cpu) = {timestamp, index};
    } else {
        softIrqEventMap_[cpu] = {timestamp, index};
    }
    slices->AppendDistributeInfo();
    return;
}

void SliceFilter::SoftIrqExit(uint64_t timestamp, uint32_t cpu, ArgsSet args)
{
    if (!softIrqEventMap_.count(cpu)) {
        // not match
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_SOFTIRQ_EXIT, STAT_EVENT_DATA_LOST);
        return;
    }
    uint32_t argSetId = INVALID_UINT32;
    auto slices = traceDataCache_->GetIrqData();
    argSetId = streamFilters_->argsFilter_->NewArgs(args);
    slices->SetDurationAndArg(softIrqEventMap_.at(cpu).row, timestamp, argSetId);
    softIrqEventMap_.erase(cpu);
    return;
}

void SliceFilter::RememberSliceData(InternalTid internalTid,
                                    std::unordered_map<InternalTid, StackOfSlices>& stackMap,
                                    SliceData& slice,
                                    uint32_t depth,
                                    uint64_t index)
{
    if (stackMap.find(internalTid) == stackMap.end()) {
        auto& sliceStack = stackMap[internalTid]; // this can be a empty call, but it does not matter
        slice.depth = depth;
        slice.index = index;
        sliceStack.push_back(slice);
    } else {
        auto& sliceStack = stackMap.at(internalTid); // this can be a empty call, but it does not matter
        slice.depth = depth;
        slice.index = index;
        sliceStack.push_back(slice);
    }
}
size_t SliceFilter::BeginAsyncBinder(uint64_t timestamp, uint32_t pid, DataIndex cat, DataIndex nameIndex, ArgsSet args)
{
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);
    // the call stack belongs to thread, so we keep a call-tree for the thread
    uint8_t depth = CurrentDepth(internalTid);
    struct SliceData sliceData = {timestamp, 0, internalTid, cat, nameIndex};
    auto slices = traceDataCache_->GetInternalSlicesData();
    size_t index = slices->AppendInternalSlice(
        sliceData.timestamp, sliceData.duration, sliceData.internalTid, sliceData.cat,
        GetNameASCIISumNoNum(traceDataCache_->GetDataFromDict(sliceData.name)), sliceData.name, depth, std::nullopt);

    sliceData.index = index;
    uint32_t argSetId = INVALID_INT32;
    if (args.valuesMap_.size()) {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        sliceData.argSetId = argSetId;
        slices->AppendArgSet(argSetId);
        sliceRowToArgsSetIdForBinderEvents_.emplace(index, argSetId);
    } else {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        sliceData.argSetId = argSetId;
        slices->AppendArgSet(argSetId);
        sliceRowToArgsSetIdForBinderEvents_.emplace(index, argSetId);
    }
    return index;
}

uint8_t SliceFilter::CurrentDepth(InternalTid internalTid)
{
    if (depthHolder_.find(internalTid) == depthHolder_.end()) {
        return 0;
    }
    auto& depthMap = depthHolder_.at(internalTid);
    auto depthSize = depthMap.size();
    auto lastIndex = 0;
    for (int32_t i = depthSize - 1; i >= 0; i--) {
        if (depthMap.at(i)) {
            return i;
        }
    }
    return 0;
}
uint8_t SliceFilter::UpdateDepth(bool increase, InternalTid internalTid, int32_t depth)
{
    if (increase) {
        if (depthHolder_.find(internalTid) == depthHolder_.end()) {
            StackOnDepth tmp;
            tmp.insert(std::make_pair(0, true));
            depthHolder_.insert(std::make_pair(internalTid, tmp));
            return 0;
        }
        auto& depthMap = depthHolder_.at(internalTid);
        auto depthSize = depthMap.size();
        auto lastIndex = 0;
        for (int32_t i = depthSize - 1; i >= 0; i--) {
            if (depthMap.at(i) && (i == depthSize - 1)) {
                depthMap.insert(std::make_pair(depthSize, true));
                return depthSize;
            }
            if (depthMap.at(i)) {
                break;
            }
            lastIndex = i;
        }

        if (!depthMap.at(lastIndex)) {
            depthMap.at(lastIndex) = true;
            return lastIndex;
        }
    } else {
        if (depthHolder_.find(internalTid) == depthHolder_.end()) {
            TS_LOGE("internalTid not found");
            return 0;
        }
        auto& depthMap = depthHolder_.at(internalTid);
        if (depthMap.find(depth) == depthMap.end()) {
            return 0;
        }
        depthMap.at(depth) = false;
    }
    return depth;
}

void SliceFilter::CloseUnMatchedSlice(int64_t ts, StackOfSlices& stack, InternalTid itid)
{
    auto slices = traceDataCache_->GetInternalSlicesData();
    bool incomplete = false;
    for (int i = stack.size() - 1; i >= 0; i--) {
        uint32_t sliceIdx = stack[i].index;
        int64_t startTs = slices->TimeStamData()[sliceIdx];
        int64_t dur = slices->DursData()[sliceIdx];
        int64_t endTs = startTs + dur;
        if (dur == -1) {
            incomplete = true;
            continue;
        }
        if (incomplete) {
            if (ts <= endTs) {
                continue;
            }
            for (int j = stack.size() - 1; j > i; --j) {
                uint32_t childIdx = stack[i].index;
                slices->SetDur(childIdx, endTs - slices->TimeStamData()[childIdx]);
                UpdateDepth(false, stack.back().internalTid, stack.back().depth);
                stack.pop_back();
            }
            UpdateDepth(false, stack.back().internalTid, stack.back().depth);
            stack.pop_back();
            incomplete = false;
            continue;
        }
        if (endTs <= ts) {
            UpdateDepth(false, stack.back().internalTid, stack.back().depth);
            stack.pop_back();
        }
    }
}
int32_t SliceFilter::MatchingIncompleteSliceIndex(const StackOfSlices& stack, DataIndex name, DataIndex category)
{
    auto slices = traceDataCache_->GetInternalSlicesData();
    for (int i = stack.size() - 1; i >= 0; i--) {
        uint32_t sliceIdx = stack[i].index;
        if (slices->DursData()[sliceIdx] != -1) {
            continue;
        }
        const DataIndex& categoryLast = slices->CatsData()[sliceIdx];
        if (category != INVALID_UINT64 && (categoryLast != INVALID_UINT64 || category != categoryLast)) {
            continue;
        }
        const DataIndex& nameLast = slices->NamesData()[sliceIdx];
        if (name != INVALID_UINT64 && nameLast != INVALID_UINT64 && name != nameLast) {
            continue;
        }
        return static_cast<int32_t>(i);
    }
    return -1;
}

size_t SliceFilter::BeginBinder(uint64_t timestamp, uint32_t pid, DataIndex cat, DataIndex nameIndex, ArgsSet args)
{
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);
    uint8_t depth = UpdateDepth(true, internalTid);
    struct SliceData sliceData = {timestamp, -1, internalTid, cat, nameIndex, depth, 0};
    // keep slice of thread
    auto& sliceStack = binderStackMap_[sliceData.internalTid];
    CloseUnMatchedSlice(timestamp, sliceStack, internalTid);
    auto slices = traceDataCache_->GetInternalSlicesData();
    uint32_t argSetId = INVALID_INT32;
    if (args.valuesMap_.size()) {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        slices->AppendArgSet(argSetId);
        sliceRowToArgsSetIdForBinderEvents_.emplace(pid, argSetId);
    } else {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        slices->AppendArgSet(argSetId);
        sliceRowToArgsSetIdForBinderEvents_.emplace(pid, argSetId);
    }
    size_t index = slices->AppendInternalSlice(
        sliceData.timestamp, sliceData.duration, sliceData.internalTid, sliceData.cat,
        GetNameASCIISumNoNum(traceDataCache_->GetDataFromDict(sliceData.name)), sliceData.name, depth, std::nullopt);
    argsToSliceRow_[argSetId] = static_cast<uint32_t>(index);
    sliceData.argSetId = index;
    RememberSliceData(sliceData.internalTid, binderStackMap_, sliceData, depth, index);
    return index;
}

bool SliceFilter::EndBinder(uint64_t timestamp, uint32_t pid, DataIndex category, DataIndex name, ArgsSet args)
{
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);
    auto& stack = binderStackMap_[internalTid];
    CloseUnMatchedSlice(timestamp, stack, internalTid);
    if (stack.empty()) {
        TS_LOGE("a slice end do not match a slice start event");
        callEventDisMatchCount++;
        return false;
    }
    auto stackIdx = MatchingIncompleteSliceIndex(stack, INVALID_UINT64, INVALID_UINT64);
    if (stackIdx < 0) {
        TS_LOGE("MatchingIncompleteSliceIndex failed");
        return false;
    }
    auto lastRow = stack[stackIdx].index;
    auto slices = traceDataCache_->GetInternalSlicesData();
    slices->SetDuration(lastRow, timestamp);

    auto argSize = sliceRowToArgsSetIdForBinderEvents_.count(lastRow);
#ifdef BINDER_ASYNC
    if (!argSize) {
        TS_LOGE("args not found for %d", lastRow);
        return false;
    }
    streamFilters_->argsFilter_->AppendArgs(args, sliceRowToArgsSetIdForBinderEvents_.at(lastRow));
    argsToSliceRow_.erase(itor->second);

    sliceRowToArgsSetIdForBinderEvents_.erase(itor);
#endif
    if (stackIdx == stack.size() - 1) {
        auto depthTemp = UpdateDepth(false, internalTid, stack.back().depth);
        UNUSED(depthTemp);
        stack.pop_back();
    }
    return true;
}
uint32_t SliceFilter::AddArgs(uint32_t tid, DataIndex key1, DataIndex key2, ArgsSet& args)
{
    InternalTid internalTid = streamFilters_->processFilter_->GetInternalTid(tid);
    auto& stack = binderStackMap_[internalTid];
    auto idx = MatchingIncompleteSliceIndex(stack, key1, key2);
    if (idx < 0) {
        return INVALID_UINT32;
    }
#ifdef BINDER_ASYNC
    auto argSize = sliceRowToArgsSetIdForBinderEvents_.count(tid);
    if (!argSize) {
        return INVALID_UINT32;
    }
    auto it = sliceRowToArgsSetIdForBinderEvents_.equal_range(tid);
#ifdef IS_WASM
    auto itorTmp = it.first;
    auto itor = itorTmp;
    while (itorTmp != it.second) {
        itor = itorTmp;
        itorTmp++;
    }
#else
    auto itor = it.first;
#endif
#endif
    streamFilters_->argsFilter_->AppendArgs(args, stack[idx].argSetId);
    return argsToSliceRow_[stack[idx].argSetId];
}

void SliceFilter::StartAsyncSlice(uint64_t timestamp,
                                  uint32_t pid,
                                  uint32_t threadGroupId,
                                  int64_t cookie,
                                  DataIndex nameIndex)
{
    UNUSED(pid);
    InternalPid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, threadGroupId);

    auto lastFilterId = asyncEventMap_.Find(internalTid, cookie, nameIndex);
    auto slices = traceDataCache_->GetInternalSlicesData();
    if (lastFilterId != INVALID_UINT64) {
        asyncEventDisMatchCount++;
        return;
    }
    asyncEventSize_++;
    // a pid, cookie and function name determain a callstack
    asyncEventMap_.Insert(internalTid, cookie, nameIndex, asyncEventSize_);
    // the IDE need a depth to paint call slice in different position of the canvas, the depth of async call
    // do not mean the parent-to-child relationship, it is different from no-async call
    uint8_t depth = 0;
    size_t index = slices->AppendInternalAsyncSlice(timestamp, -1, internalTid, INVALID_UINT64,
                                                    GetNameASCIISumNoNum(traceDataCache_->GetDataFromDict(nameIndex)),
                                                    nameIndex, depth, cookie, std::nullopt);
    asyncEventFilterMap_.insert(std::make_pair(asyncEventSize_, AsyncEvent{timestamp, index}));
}

void SliceFilter::FinishAsyncSlice(uint64_t timestamp,
                                   uint32_t pid,
                                   uint32_t threadGroupId,
                                   int64_t cookie,
                                   DataIndex nameIndex)
{
    UNUSED(pid);
    InternalPid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, threadGroupId);
    auto lastFilterId = asyncEventMap_.Find(internalTid, cookie, nameIndex);
    auto slices = traceDataCache_->GetInternalSlicesData();
    if (lastFilterId == INVALID_UINT64) { // if failed
        asyncEventDisMatchCount++;
        return;
    }
    if (asyncEventFilterMap_.find(lastFilterId) == asyncEventFilterMap_.end()) {
        TS_LOGE("logic error");
        asyncEventDisMatchCount++;
        return;
    }
    // update timestamp
    asyncEventFilterMap_.at(lastFilterId).timestamp = timestamp;
    slices->SetDuration(asyncEventFilterMap_.at(lastFilterId).row, timestamp);
    asyncEventFilterMap_.erase(lastFilterId);
    asyncEventMap_.Erase(internalTid, cookie, nameIndex);
}

bool SliceFilter::BeginSliceInternal(const SliceData& sliceData)
{
    auto& sliceStack = sliceStackMap_[sliceData.internalTid]; // this can be a empty call, but it does not matter
    CloseUnMatchedSlice(sliceData.timestamp, sliceStack, sliceData.internalTid);
    // the call stack belongs to thread, so we keep a call-tree for the thread
    uint8_t depth = UpdateDepth(true, sliceData.internalTid);
    auto slices = traceDataCache_->GetInternalSlicesData();

    if (sliceStack.size() >= std::numeric_limits<uint8_t>::max()) {
        TS_LOGE("stack depth out of range.");
    }
    const uint8_t depthTemp = static_cast<uint8_t>(sliceStack.size());
    std::optional<uint64_t> parentId = std::nullopt;
    if (depthTemp != 0) {
        auto lastSlice = sliceStack.back();
        parentId = std::make_optional(slices->IdsData()[lastSlice.index]); // get the depth here
    }

    size_t index = slices->AppendInternalSlice(
        sliceData.timestamp, sliceData.duration, sliceData.internalTid, sliceData.cat,
        GetNameASCIISumNoNum(traceDataCache_->GetDataFromDict(sliceData.name)), sliceData.name, depth, parentId);
    struct SliceData sliceDataTmp = {
        sliceData.timestamp, -1, sliceData.internalTid, sliceData.cat, sliceData.name, depth, 0};
    RememberSliceData(sliceData.internalTid, sliceStackMap_, sliceDataTmp, depth, index);
    return true;
}

bool SliceFilter::EndSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId)
{
    InternalTid internalTid = INVALID_UTID;
    if (threadGroupId) {
        auto actThreadGroupIdIter = pidTothreadGroupId_.find(pid);
        if (actThreadGroupIdIter == pidTothreadGroupId_.end()) {
            callEventDisMatchCount++;
            return false;
        }
        uint32_t actThreadGroupId = actThreadGroupIdIter->second;
        if (threadGroupId != actThreadGroupId) {
            TS_LOGD("pid %u mismatched thread group id %u", pid, actThreadGroupId);
        }
        internalTid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(pid, actThreadGroupId);
    } else {
        internalTid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(pid, 0);
    }

    auto& stack = sliceStackMap_[internalTid];
    CloseUnMatchedSlice(timestamp, stack, internalTid);
    if (stack.empty()) {
        TS_LOGE("a slice end do not match a slice start event");
        callEventDisMatchCount++;
        return false;
    }
    auto stackIdx = MatchingIncompleteSliceIndex(stack, INVALID_UINT64, INVALID_UINT64);
    if (stackIdx == -1) {
        TS_LOGE("MatchingIncompleteSliceIndex failed");
        return false;
    }
    auto slices = traceDataCache_->GetInternalSlicesData();
    auto stackData = stack[stackIdx];
    slices->SetDuration(stackData.index, timestamp);
    if (stackIdx == stack.size() - 1) {
        auto tmp = UpdateDepth(false, internalTid, stack.back().depth);
        UNUSED(tmp);
        stack.pop_back();
    }
    return true;
}

uint64_t SliceFilter::GenHashByStack(const StackOfSlices& sliceStack) const
{
    std::string hashStr;
    const auto& sliceSet = traceDataCache_->GetConstInternalSlicesData();
    for (size_t i = 0; i < sliceStack.size(); i++) {
        size_t index = sliceStack[i].index;
        hashStr += "cat";
        hashStr += std::to_string(sliceSet.CatsData()[index]);
        hashStr += "name";
        hashStr += std::to_string(sliceSet.NamesData()[index]);
    }

    const uint64_t stackHashMask = uint64_t(-1) >> 1;
    return (std::hash<std::string>{}(hashStr)) & stackHashMask;
}
void SliceFilter::Clear()
{
    asyncEventMap_.Clear();
    asyncNoEndingEventMap_.clear();
    irqEventMap_.clear();
    softIrqEventMap_.clear();
    asyncEventFilterMap_.clear();
    sliceStackMap_.clear();
    depthHolder_.clear();
    pidTothreadGroupId_.clear();
    sliceRowToArgsSetIdForBinderEvents_.clear();
    argsToSliceRow_.clear();
    argsSet_.clear();
}
} // namespace TraceStreamer
} // namespace SysTuning

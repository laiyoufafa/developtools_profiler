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
#include "ts_common.h"

namespace SysTuning {
namespace TraceStreamer {
SliceFilter::SliceFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter), asyncEventMap_(INVALID_UINT64)
{
}

SliceFilter::~SliceFilter() = default;

bool SliceFilter::BeginSlice(uint64_t timestamp,
                             uint32_t pid,
                             uint32_t threadGroupId,
                             DataIndex cat,
                             DataIndex nameIndex)
{
    InternalTid internalTid = INVALID_UTID;
    if (threadGroupId > 0) {
        internalTid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(pid, threadGroupId);
        pidTothreadGroupId_[pid] = threadGroupId;
    } else {
        internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);
    }
    // make a SliceData DataItem, {timestamp, dur, internalTid, cat, nameIndex}
    struct SliceData sliceData = {timestamp, 0, internalTid, cat, nameIndex};
    return BeginSliceInternal(sliceData);
}

void SliceFilter::IrqHandlerEntry(uint64_t timestamp, uint32_t cpu, DataIndex catalog, DataIndex nameIndex)
{
    struct SliceData sliceData = {timestamp, 0, cpu, catalog, nameIndex};
    auto slices = traceDataCache_->GetIrqData();
    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
                                               sliceData.cat, sliceData.name, 0, std::nullopt);
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
    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
                                               sliceData.cat, sliceData.name, 0, std::nullopt);
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

size_t SliceFilter::BeginAsyncBinder(uint64_t timestamp, uint32_t pid, DataIndex cat, DataIndex nameIndex, ArgsSet args)
{
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);
    struct SliceData sliceData = {timestamp, 0, internalTid, cat, nameIndex};
    auto slices = traceDataCache_->GetInternalSlicesData();

    auto sliceStack = &sliceStackMap_[sliceData.internalTid];
    if (sliceStack->size() >= std::numeric_limits<uint8_t>::max()) {
        TS_LOGW("stack depth out of range.");
    }
    const uint8_t depth = static_cast<uint8_t>(sliceStack->size());
    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
                                               sliceData.cat, sliceData.name, depth, std::nullopt);

    uint32_t argSetId = INVALID_INT32;
    if (args.valuesMap_.size()) {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        slices->AppendArgSet(argSetId);
        binderQueue_[pid] = argSetId;
    } else {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        slices->AppendArgSet(argSetId);
        binderQueue_[pid] = argSetId;
    }
    argsToSliceQueue_[argSetId] = static_cast<uint32_t>(index);
    return index;
}
size_t SliceFilter::BeginBinder(uint64_t timestamp, uint32_t pid, DataIndex cat, DataIndex nameIndex,
    ArgsSet args)
{
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);
    struct SliceData sliceData = {timestamp, 0, internalTid, cat, nameIndex};
    auto slices = traceDataCache_->GetInternalSlicesData();

    auto sliceStack = &sliceStackMap_[sliceData.internalTid];
    if (sliceStack->size() >= std::numeric_limits<uint8_t>::max()) {
        TS_LOGW("stack depth out of range.");
    }
    const uint8_t depth = static_cast<uint8_t>(sliceStack->size());
    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
                                               sliceData.cat, sliceData.name, depth, std::nullopt);

    sliceStack->push_back(index);

    uint32_t argSetId = INVALID_INT32;
    if (args.valuesMap_.size()) {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        slices->AppendArgSet(argSetId);
        binderQueue_[pid] = argSetId;
    } else {
        argSetId = streamFilters_->argsFilter_->NewArgs(args);
        slices->AppendArgSet(argSetId);
        binderQueue_[pid] = argSetId;
    }
    argsToSliceQueue_[argSetId] = static_cast<uint32_t>(index);
    return index;
}

uint32_t SliceFilter::AddArgs(uint32_t tid, DataIndex key1, DataIndex key2, ArgsSet &args)
{
    if (!binderQueue_.count(tid)) {
        return INVALID_UINT32;
    }
    streamFilters_->argsFilter_->AppendArgs(args, binderQueue_[tid]);
    return argsToSliceQueue_[binderQueue_[tid]];
}
bool SliceFilter::EndBinder(uint64_t timestamp, uint32_t pid, DataIndex category, DataIndex name, ArgsSet args)
{
    if (!binderQueue_.count(pid)) {
        return false;
    }
    auto lastRow = argsToSliceQueue_[binderQueue_[pid]];
    auto slices = traceDataCache_->GetInternalSlicesData();
    slices->SetDuration(lastRow, timestamp);
    streamFilters_->argsFilter_->AppendArgs(args, binderQueue_[pid]);
    argsToSliceQueue_.erase(binderQueue_[pid]);

    binderQueue_.erase(pid);
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(timestamp, pid);

    const auto& stack = sliceStackMap_[internalTid];
    if (stack.empty()) {
        TS_LOGE("a slice end do not match a slice start event");
        callEventDisMatchCount++;
        return false;
    }
    sliceStackMap_[internalTid].pop_back();
    return true;
}

void SliceFilter::StartAsyncSlice(uint64_t timestamp,
                                  uint32_t pid,
                                  uint32_t threadGroupId,
                                  int64_t cookie,
                                  DataIndex nameIndex)
{
    InternalPid internalPid = streamFilters_->processFilter_->GetOrCreateInternalPid(timestamp, threadGroupId);
    auto lastFilterId = asyncEventMap_.Find(internalPid, cookie, nameIndex);
    auto slices = traceDataCache_->GetInternalSlicesData();
    if (lastFilterId != INVALID_UINT64) {
        asyncEventDisMatchCount++;
        FinishAsyncSlice(timestamp, pid, threadGroupId, cookie, nameIndex);
    }
    asyncEventSize_++;
    // a pid, cookie and function name determain a callstack
    asyncEventMap_.Insert(internalPid, cookie, nameIndex, asyncEventSize_);
    // the IDE need a depth to paint call slice in different position of the canvas, the depth of async call
    // do not mean the parent-to-child relationship, it is different from no-async call
    uint8_t depth = 0;
    if (asyncNoEndingEventMap_.find(internalPid) == asyncNoEndingEventMap_.end()) {
        depth = 0;
        asyncNoEndingEventMap_.insert(std::make_pair(internalPid, 1));
    } else {
        depth = asyncNoEndingEventMap_.at(internalPid);
        asyncNoEndingEventMap_.at(internalPid)++;
    }
    size_t index = slices->AppendInternalAsyncSlice(timestamp, 0, internalPid, INVALID_UINT64, nameIndex, depth, cookie,
                                                    std::nullopt);
    asyncEventFilterMap_.insert(std::make_pair(asyncEventSize_, AsyncEvent{timestamp, index}));
}

void SliceFilter::FinishAsyncSlice(uint64_t timestamp,
                                   uint32_t pid,
                                   uint32_t threadGroupId,
                                   int64_t cookie,
                                   DataIndex nameIndex)
{
    UNUSED(pid);
    InternalPid internalPid = streamFilters_->processFilter_->GetOrCreateInternalPid(timestamp, threadGroupId);
    auto lastFilterId = asyncEventMap_.Find(internalPid, cookie, nameIndex);
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
    asyncEventMap_.Erase(internalPid, cookie, nameIndex);

    if (asyncNoEndingEventMap_.find(internalPid) == asyncNoEndingEventMap_.end()) {
        asyncNoEndingEventMap_.insert(std::make_pair(internalPid, 1));
    } else {
        asyncNoEndingEventMap_.at(internalPid)--;
        if (!asyncNoEndingEventMap_.at(internalPid)) {
            asyncNoEndingEventMap_.erase(internalPid);
        }
    }
}

bool SliceFilter::BeginSliceInternal(const SliceData& sliceData)
{
    // the call stack belongs to thread, so we keep a call-tree for the thread
    auto sliceStack = &sliceStackMap_[sliceData.internalTid]; // this can be a empty call, but it does not matter
    auto slices = traceDataCache_->GetInternalSlicesData();
    if (sliceStack->size() >= std::numeric_limits<uint8_t>::max()) {
        TS_LOGW("stack depth out of range.");
    }
    const uint8_t depth = static_cast<uint8_t>(sliceStack->size());
    std::optional<uint64_t> parentId = std::nullopt;
    if (depth != 0) {
        size_t lastDepth = sliceStack->back();
        parentId = std::make_optional(slices->IdsData()[lastDepth]); // get the depth here
    }

    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
                                               sliceData.cat, sliceData.name, depth, parentId);
    sliceStack->push_back(index);
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

    const auto& stack = sliceStackMap_[internalTid];
    if (stack.empty()) {
        TS_LOGW("a slice end do not match a slice start event");
        callEventDisMatchCount++;
        return false;
    }

    auto slices = traceDataCache_->GetInternalSlicesData();
    size_t index = stack.back();
    slices->SetDuration(index, timestamp);
    sliceStackMap_[internalTid].pop_back();
    // update dur of parent slice maybe
    auto parentId = slices->ParentIdData()[index];
    if (parentId.has_value()) {
        slices->SetDuration(parentId.value(), timestamp);
    }
    return true;
}

uint64_t SliceFilter::GenHashByStack(const StackOfSlices& sliceStack) const
{
    std::string hashStr;
    const auto& sliceSet = traceDataCache_->GetConstInternalSlicesData();
    for (size_t i = 0; i < sliceStack.size(); i++) {
        size_t index = sliceStack[i];
        hashStr += "cat";
        hashStr += std::to_string(sliceSet.CatsData()[index]);
        hashStr += "name";
        hashStr += std::to_string(sliceSet.NamesData()[index]);
    }

    const uint64_t stackHashMask = uint64_t(-1) >> 1;
    return (std::hash<std::string>{}(hashStr)) & stackHashMask;
}
} // namespace TraceStreamer
} // namespace SysTuning

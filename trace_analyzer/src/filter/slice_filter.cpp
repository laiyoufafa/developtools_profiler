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

#include "common.h"
#include "log.h"
#include "measure_filter.h"
#include "process_filter.h"

namespace SysTuning {
namespace TraceStreamer {
SliceFilter::SliceFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter), asyncEventMap_(INVALID_UINT64), asyncEventSize_(0)
{
}

SliceFilter::~SliceFilter() = default;

bool SliceFilter::BeginSlice(uint64_t timestamp,
                             uint32_t pid,
                             uint32_t threadGroupId,
                             DataIndex cat,
                             DataIndex nameIndex)
{
    InternalTid internalTid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(pid, threadGroupId);
    pidTothreadGroupId_[pid] = threadGroupId;
    struct SliceData sliceData = {timestamp, 0, internalTid, cat, nameIndex};
    return BeginSliceInternal(sliceData);
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
    asyncEventMap_.Insert(internalPid, cookie, nameIndex, asyncEventSize_);
    size_t index =
        slices->AppendInternalAsyncSlice(timestamp, 0, internalPid, INVALID_UINT64, nameIndex, 0, cookie, std::nullopt);
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
}

bool SliceFilter::BeginSliceInternal(const SliceData& sliceData)
{
    auto sliceStack = &sliceStackMap_[sliceData.internalTid];
    auto slices = traceDataCache_->GetInternalSlicesData();
    if (sliceStack->size() >= std::numeric_limits<uint8_t>::max()) {
        TS_LOGE("stack depth out of range.");
        return false;
    }
    const uint8_t depth = static_cast<uint8_t>(sliceStack->size());
    std::optional<uint64_t> parentId = std::nullopt;
    if (depth != 0) {
        size_t lastDepth = sliceStack->back();
        parentId = std::make_optional(slices->IdsData()[lastDepth]);
    }

    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
                                               sliceData.cat, sliceData.name, depth, parentId);
    sliceStack->push_back(index);
    return true;
}

bool SliceFilter::EndSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId)
{
    auto actThreadGroupIdIter = pidTothreadGroupId_.find(pid);
    if (actThreadGroupIdIter == pidTothreadGroupId_.end()) {
        callEventDisMatchCount++;
        return false;
    }

    uint32_t actThreadGroupId = actThreadGroupIdIter->second;
    if (threadGroupId != 0 && threadGroupId != actThreadGroupId) {
        TS_LOGD("pid %u mismatched thread group id %u", pid, actThreadGroupId);
    }

    InternalTid internalTid = streamFilters_->processFilter_->GetOrCreateThreadWithPid(pid, actThreadGroupId);

    const auto& stack = sliceStackMap_[internalTid];
    if (stack.empty()) {
        TS_LOGW("workqueue_execute_end do not match a workqueue_execute_start event");
        callEventDisMatchCount++;
        return false;
    }

    auto slices = traceDataCache_->GetInternalSlicesData();
    size_t index = stack.back();
    slices->SetDuration(index, timestamp);

    sliceStackMap_[internalTid].pop_back();
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

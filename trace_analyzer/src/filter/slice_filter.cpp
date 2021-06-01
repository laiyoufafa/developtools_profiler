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
#include "measure_filter.h"
#include "process_filter.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
SliceFilter::SliceFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter) {}

SliceFilter::~SliceFilter() = default;

void SliceFilter::BeginSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId, DataIndex cat, DataIndex name)
{
    InternalTid internalTid = streamFilters_->processFilter_->SetThreadPid(pid, threadGroupId);
    pidTothreadGroupId_[pid] = threadGroupId;
    uint32_t filterId = streamFilters_->threadFilter_->GetOrCreateCertainFilterId(internalTid, 0);
    struct SliceData sliceData = {timestamp, 0, internalTid, filterId, cat, name};
    FinishedSliceStack(sliceData.timestamp, sliceStackMap_[sliceData.internalTid]);
    BeginSliceInternal(sliceData);
}

void SliceFilter::AsyncBeginSlice(uint64_t timestamp, uint32_t pid,
                                  uint32_t threadGroupId, int64_t cookie, DataIndex name)
{
    UNUSED(timestamp);

    InternalTid internalTid = streamFilters_->processFilter_->SetThreadPid(pid, threadGroupId);
    auto filterId = streamFilters_->processFilterFilter_->GetOrCreateCertainFilterIdByCookie(internalTid, name, cookie);
    UNUSED(filterId);
}

void SliceFilter::AsyncEndSlice(uint64_t timestamp, uint32_t pid, int64_t cookie)
{
    UNUSED(timestamp);
    UNUSED(pid);
    UNUSED(cookie);
}

void SliceFilter::BeginSliceInternal(const SliceData& sliceData)
{
    auto* sliceStack = &sliceStackMap_[sliceData.internalTid];
    auto* slices = traceDataCache_->GetInternalSlicesData();
    const uint8_t depth = static_cast<uint8_t>(sliceStack->size());
    if (depth >= std::numeric_limits<uint8_t>::max()) {
        TUNING_LOGF("stack depth out of range.");
        return;
    }

    uint64_t parentStackId = 0;
    std::optional<uint64_t> parentId = std::nullopt;
    if (depth != 0) {
        size_t lastDepth = sliceStack->back();
        parentStackId = slices->StackIdsData()[lastDepth];
        parentId = std::make_optional(slices->IdsData()[lastDepth]);
    }

    size_t index = slices->AppendInternalSlice(sliceData.timestamp, sliceData.duration, sliceData.internalTid,
        sliceData.filterId, sliceData.cat, sliceData.name, depth, 0, parentStackId, parentId);
    sliceStack->push_back(index);
    slices->SetStackId(index, GenHashByStack(*sliceStack));
}

void SliceFilter::EndSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId)
{
    auto actThreadGroupIdIter = pidTothreadGroupId_.find(pid);
    if (actThreadGroupIdIter == pidTothreadGroupId_.end()) {
        return;
    }

    uint32_t actThreadGroupId = actThreadGroupIdIter->second;
    if (threadGroupId != 0 && threadGroupId != actThreadGroupId) {
        TUNING_LOGD("pid %u mismatched thread group id %u", pid, actThreadGroupId);
    }

    InternalTid internalTid = streamFilters_->processFilter_->SetThreadPid(pid, actThreadGroupId);

    FinishedSliceStack(timestamp, sliceStackMap_[internalTid]);

    const auto& stack = sliceStackMap_[internalTid];
    if (stack.empty()) {
        return;
    }

    auto* slices = traceDataCache_->GetInternalSlicesData();
    size_t index = stack.back();
    slices->SetDuration(index, timestamp - slices->TimeStamData()[index]);

    sliceStackMap_[internalTid].pop_back();
}

void SliceFilter::FinishedSliceStack(uint64_t timestamp, StackOfSlices& sliceStack)
{
    const auto& slices = traceDataCache_->GetConstInternalSlicesData();
    for (int i = static_cast<int>(sliceStack.size()) - 1; i >= 0; i--) {
        size_t index = sliceStack[static_cast<size_t>(i)];
        uint64_t during = slices.DursData()[index];
        if (during == 0) {
            continue;
        }

        uint64_t endT = slices.TimeStamData()[index] + during;
        if (timestamp >= endT) {
            sliceStack.pop_back();
        }
    }
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
    return (std::hash<std::string> {}(hashStr)) & stackHashMask;
}
} // namespace TraceStreamer
} // namespace SysTuning

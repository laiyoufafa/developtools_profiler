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

#ifndef SLICE_FILTER_H
#define SLICE_FILTER_H

#include <cstdint>

#include "filter_base.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
class TraceDataCache;
class TraceStreamerFilters;

struct SliceData {
    uint64_t timestamp;
    uint64_t duration;
    InternalTid internalTid;
    uint32_t filterId;
    DataIndex cat;
    DataIndex name;
};

class SliceFilter : private FilterBase {
public:
    explicit SliceFilter(TraceDataCache*, const TraceStreamerFilters*);
    ~SliceFilter() override;

    void BeginSlice(uint64_t timestamp, uint32_t ftraceTid, uint32_t atraceTid, DataIndex cat, DataIndex name);
    void EndSlice(uint64_t timestamp, uint32_t ftraceTid, uint32_t atraceTid);
    void AsyncBeginSlice(uint64_t timestamp, uint32_t ftraceTid, uint32_t atraceTid, int64_t cookie, DataIndex name);
    void AsyncEndSlice(uint64_t timestamp, uint32_t pid, int64_t cookie);

private:
    using StackOfSlices = std::vector<size_t>;

    uint64_t GenHashByStack(const StackOfSlices&) const;
    void BeginSliceInternal(const SliceData& sliceData);
    void FinishedSliceStack(uint64_t endTs, StackOfSlices&);

    std::unordered_map<InternalTid, StackOfSlices> sliceStackMap_ = {};
    std::unordered_map<uint32_t, uint32_t> pidTothreadGroupId_ = {};
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // SLICE_FILTER_H

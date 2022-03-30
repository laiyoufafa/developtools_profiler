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
#include <vector>
#include "args_set.h"
#include "filter_base.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"
#include "triple_map.h"
#include "ts_common.h"

namespace SysTuning {
namespace TraceStreamer {
struct SliceData {
    uint64_t timestamp;
    uint64_t duration;
    InternalTid internalTid;
    DataIndex cat;
    DataIndex name;
};
struct AsyncEvent {
    uint64_t timestamp;
    size_t row;
};
class SliceFilter : private FilterBase {
public:
    SliceFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter);
    ~SliceFilter() override;

    bool BeginSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId, DataIndex cat, DataIndex nameIndex);
    size_t BeginBinder(uint64_t timestamp, uint32_t pid, DataIndex cat, DataIndex nameIndex, ArgsSet args = ArgsSet());
    size_t BeginAsyncBinder(uint64_t timestamp, uint32_t pid, DataIndex cat, DataIndex nameIndex,
                            ArgsSet args = ArgsSet());
    bool EndBinder(uint64_t timestamp, uint32_t pid, DataIndex category = {}, DataIndex name = {}, ArgsSet args = {});
    bool EndSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId);
    void StartAsyncSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId, int64_t cookie, DataIndex nameIndex);
    void
        FinishAsyncSlice(uint64_t timestamp, uint32_t pid, uint32_t threadGroupId, int64_t cookie, DataIndex nameIndex);
    uint32_t AddArgs(FilterId, DataIndex key1, DataIndex key2, ArgsSet& args);
    void IrqHandlerEntry(uint64_t timestamp, uint32_t cpu, DataIndex catalog, DataIndex nameIndex);
    void IrqHandlerExit(uint64_t timestamp, uint32_t cpu, ArgsSet args);
    void SoftIrqEntry(uint64_t timestamp, uint32_t cpu, DataIndex catalog, DataIndex nameIndex);
    void SoftIrqExit(uint64_t timestamp, uint32_t cpu, ArgsSet args);
private:
    using StackOfSlices = std::vector<size_t>;
    uint64_t GenHashByStack(const StackOfSlices& sliceStack) const;
    bool BeginSliceInternal(const SliceData& sliceData);
private:
    // The parameter list is tid, cookid, functionName, asyncCallId.
    TripleMap<uint32_t, uint64_t, DataIndex, uint64_t> asyncEventMap_;
    // this is only used to calc the layer of the async event in same time range
    std::map<uint32_t, uint8_t> asyncNoEndingEventMap_ = {};
    //  irq map, key1 is cpu, key2
    struct IrqRecords {
        uint64_t ts;
        size_t row;
    };
    std::unordered_map<uint32_t, IrqRecords> irqEventMap_ = {};
    //  irq map, key1 is cpu, key2
    std::unordered_map<uint32_t, IrqRecords> softIrqEventMap_ = {};
    std::map<uint64_t, AsyncEvent> asyncEventFilterMap_ = {};
    std::unordered_map<InternalTid, StackOfSlices> sliceStackMap_ = {};
    std::unordered_map<InternalTid, StackOfSlices> binderStackMap_ = {};
    std::unordered_map<uint32_t, uint32_t> pidTothreadGroupId_ = {};
    uint64_t asyncEventSize_ = 0;
    uint64_t asyncEventDisMatchCount = 0;
    uint64_t callEventDisMatchCount = 0;
    std::unordered_map<uint32_t, uint32_t> binderQueue_ = {};
    std::unordered_map<uint32_t, uint32_t> argsToSliceQueue_ = {};
    struct SliceInfo {
        uint32_t row;
        ArgsSet args_tracker;
    };
    std::unordered_map<FilterId, std::vector<SliceInfo>> argsSet_ = {};
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // SLICE_FILTER_H

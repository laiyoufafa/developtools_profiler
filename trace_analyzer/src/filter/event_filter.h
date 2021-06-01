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

#ifndef EVENT_FILTER_H
#define EVENT_FILTER_H

#include <array>
#include <limits>
#include <string_view>
#include "filter_base.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
class TraceStreamerFilters;
constexpr size_t MAX_CPUS = 64;
constexpr uint32_t INVALID_UINT32 = std::numeric_limits<uint32_t>::max();

struct BinderParamter {
    uint64_t ts;
    uint32_t tid;
    int32_t transactionId;
    int32_t destNode;
    int32_t destTgid;
    int32_t destTid;
    bool isReply;
    uint32_t flags;
    DataIndex code;
    DataIndex nameIndex;
};

class EventFilter : private FilterBase {
public:
    explicit EventFilter(TraceDataCache*, const TraceStreamerFilters*);
    ~EventFilter() override;

    virtual void UpdateSchedSwitch(uint32_t cpu,
                                 uint64_t timestamp,
                                 uint32_t prevPid,
                                 uint32_t nextPid,
                                 std::string_view nextComm);

    uint32_t GetOrCreatCpuCounterFilter(DataIndex name, uint32_t cpu);
    void BinderTransaction(const BinderParamter& binderParamter) const;
    void BinderTransactionReceived(uint64_t ts, uint32_t tid, int32_t transactionId) const;

private:
    void AddFilterId(DataIndex name, uint32_t cpu, uint32_t filterId);
    uint32_t FindFilterId(DataIndex name, uint32_t cpu) const;
    void UpdateDuration(uint32_t cpu, uint64_t timeStamp);
    bool UpdateTimestamp(uint64_t timeStamp);

    struct PendingSchedSlice {
        size_t storageIndex = std::numeric_limits<size_t>::max();
        uint32_t pid = 0;
    };

    uint64_t prevTimestamp_;
    DataIndex const idleStringId_;
    DataIndex const filterTypeStringId_;
    std::array<PendingSchedSlice, MAX_CPUS> pendingSchedPerCpu_ {};
    std::map<std::pair<uint32_t, DataIndex>, uint32_t> cpuNameFilters_ {};
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // EVENT_FILTER_H

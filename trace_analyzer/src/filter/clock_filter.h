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

#ifndef CLOCK_FILTER_H
#define CLOCK_FILTER_H

#include <map>
#include <string>
#include <unordered_map>
#include <vector>
#include "filter_base.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
enum BuiltinClocks {
    REALTIME = 1,
    REALTIME_COARSE = 2,
    MONOTONIC = 3,
    MONOTONIC_COARSE = 4,
    MONOTONIC_RAW = 5,
    BOOTTIME = 6,
};

class ClockFilter : private FilterBase {
public:
    using ClockId = uint32_t;
    using ConvertClockMap = std::map<uint64_t, int64_t>;

    class SnapShot {
    public:
        ClockId clockId;
        uint64_t ts;
    };

    explicit ClockFilter(TraceDataCache*, const TraceStreamerFilters*);
    ~ClockFilter() override;

    uint64_t ToPrimaryTraceTime(ClockId srcClockId, uint64_t srcTs) const;
    uint64_t Convert(ClockId srcClockId, uint64_t srcTs, ClockId desClockId) const;

    void SetPrimaryClock(ClockId primary)
    {
        primaryClock_ = primary;
    }

    void AddClockSnapshot(const std::vector<SnapShot>& snapShot);

private:
    std::string GenClockKey(ClockId srcClockId, ClockId desClockId) const;
    void AddConvertClockMap(ClockId srcClockId, ClockId dstClockId, uint64_t srcTs, uint64_t dstTs);

private:
    std::unordered_map<std::string, ConvertClockMap> clockMaps_ = {};

    ClockId primaryClock_;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // CLOCK_FILTER_H

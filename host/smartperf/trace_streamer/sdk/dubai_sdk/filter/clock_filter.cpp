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

#include "clock_filter.h"
#include <algorithm>
#include <map>
#include "string_help.h"

namespace SysTuning {
namespace TraceStreamer {
ClockFilter::ClockFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
{
    profilerSDKTraceFileHeader_ = std::make_unique<base::ProfilerTraceFileHeader>().get();
}
ClockFilter::~ClockFilter() {}

std::string ClockFilter::GenClockKey(ClockId srcClockId, ClockId desClockId)
{
    std::string ret;
    ret += std::to_string(srcClockId);
    ret += ",";
    ret += std::to_string(desClockId);
    return ret;
}

uint64_t ClockFilter::ToPrimaryTraceTime(ClockId srcClockId, uint64_t srcTs) const
{
    if (srcClockId == primaryClock_) {
        return srcTs;
    }
    return Convert(srcClockId, srcTs, primaryClock_);
}

uint64_t ClockFilter::Convert(ClockId srcClockId, uint64_t srcTs, ClockId desClockId) const
{
    std::string&& clockKey = GenClockKey(srcClockId, desClockId);
    auto keyIt = clockMaps_.find(clockKey);
    if (keyIt == clockMaps_.end()) {
        return srcTs;
    }

    auto tsIt = keyIt->second.upper_bound(srcTs);
    if (tsIt != keyIt->second.begin()) {
        tsIt--;
    }

    if (tsIt->second >= 0) {
        return srcTs + static_cast<uint64_t>(tsIt->second);
    } else {
        return srcTs - static_cast<uint64_t>(0 - tsIt->second);
    }
}

void ClockFilter::AddConvertClockMap(ClockId srcClockId, ClockId dstClockId, uint64_t srcTs, uint64_t dstTs)
{
    std::string&& clockKey = GenClockKey(srcClockId, dstClockId);
    auto keyIt = clockMaps_.find(clockKey);
    if (keyIt == clockMaps_.end()) {
        ConvertClockMap newConvertMap = {{srcTs, dstTs - srcTs}};
        clockMaps_[clockKey] = newConvertMap;
    } else {
        clockMaps_[clockKey].insert(std::make_pair(srcTs, dstTs - srcTs));
    }
}
void ClockFilter::AddClockSnapshot(const std::vector<SnapShot>& snapShot)
{
    ClockId srcId, desId;
    for (srcId = 0; srcId < snapShot.size() - 1; ++srcId) {
        for (desId = srcId + 1; desId < snapShot.size(); ++desId) {
            ClockId srcClockId = snapShot[srcId].clockId;
            ClockId desClockId = snapShot[desId].clockId;
            uint64_t srcTs = snapShot[srcId].ts;
            uint64_t desTs = snapShot[desId].ts;

            AddConvertClockMap(srcClockId, desClockId, srcTs, desTs);
            AddConvertClockMap(desClockId, srcClockId, desTs, srcTs);
        }
    }
    hasInitSnapShot_ = true;
}

int32_t ClockFilter::InitSnapShotTimeRange(const uint8_t* data, int32_t len)
{
    std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(len);
    std::copy(data, data + len, buf.get());
    profilerSDKTraceFileHeader_ = reinterpret_cast<base::ProfilerTraceFileHeader*>(buf.get());

    if (HasInitSnapShot()) {
        TS_LOGE("SDK already has clock snapshot!!!");
        return -1;
    }
    if (!profilerSDKTraceFileHeader_->data.boottime) {
        TS_LOGE("SDK Profiler header has no clock snapshot!!!");
        return -1;
    }

    std::vector<SnapShot> snapShot;

    TS_LOGE("SDK clockid: TS_CLOCK_BOOTTIME, ts:%llu", profilerSDKTraceFileHeader_->data.boottime);
    if (profilerSDKTraceFileHeader_->data.boottime) {
        snapShot.push_back(SnapShot{TS_CLOCK_BOOTTIME, profilerSDKTraceFileHeader_->data.boottime});
    }

    TS_LOGE("SDK clockid: TS_CLOCK_REALTIME, ts:%llu", profilerSDKTraceFileHeader_->data.realtime);
    if (profilerSDKTraceFileHeader_->data.realtime) {
        snapShot.push_back(SnapShot{TS_CLOCK_REALTIME, profilerSDKTraceFileHeader_->data.realtime});
    }

    TS_LOGE("SDK clockid: TS_CLOCK_REALTIME_COARSE, ts:%llu", profilerSDKTraceFileHeader_->data.realtimeCoarse);
    if (profilerSDKTraceFileHeader_->data.realtimeCoarse) {
        snapShot.push_back(SnapShot{TS_CLOCK_REALTIME_COARSE, profilerSDKTraceFileHeader_->data.realtimeCoarse});
    }

    TS_LOGE("SDK clockid: TS_MONOTONIC, ts:%llu", profilerSDKTraceFileHeader_->data.monotonic);
    if (profilerSDKTraceFileHeader_->data.monotonic) {
        snapShot.push_back(SnapShot{TS_MONOTONIC, profilerSDKTraceFileHeader_->data.monotonic});
    }

    TS_LOGE("SDK clockid: TS_MONOTONIC_COARSE, ts:%llu", profilerSDKTraceFileHeader_->data.monotonicCoarse);
    if (profilerSDKTraceFileHeader_->data.monotonicCoarse) {
        snapShot.push_back(SnapShot{TS_MONOTONIC_COARSE, profilerSDKTraceFileHeader_->data.monotonicCoarse});
    }

    TS_LOGE("SDK clockid: TS_MONOTONIC_RAW, ts:%llu", profilerSDKTraceFileHeader_->data.monotonicRaw);
    if (profilerSDKTraceFileHeader_->data.monotonicRaw) {
        snapShot.push_back(SnapShot{TS_MONOTONIC_RAW, profilerSDKTraceFileHeader_->data.monotonicRaw});
    }

    if (snapShot.size()) {
        AddClockSnapshot(snapShot);
    }
    return 0;
}
} // namespace TraceStreamer
} // namespace SysTuning

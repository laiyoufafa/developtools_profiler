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
#include "htrace_plugin_time.h"
namespace SysTuning {
namespace TraceStreamer {
HtracePluginTime::HtracePluginTime(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : streamFilters_(ctx), traceDataCache_(dataCache)
{
    if (!streamFilters_) {
        TS_LOGF("streamFilters_ should not be null");
        return;
    }
    if (!traceDataCache_) {
        TS_LOGF("traceDataCache_ should not be null");
        return;
    }
}
void HtracePluginTime::UpdatePluginTimeRange(ClockId clockId, uint64_t asyncTimestamp, uint64_t syncTimestamp)
{
    if (clockId == streamFilters_->clockFilter_->GetPrimaryClock()) {
        syncHtracePluginStartTime_ = std::min(syncHtracePluginStartTime_, syncTimestamp);
        syncHtracePluginEndTime_ = std::max(syncHtracePluginEndTime_, syncTimestamp);
        return;
    }
    if (syncTimestamp != asyncTimestamp) {
        syncHtracePluginStartTime_ = std::min(syncHtracePluginStartTime_, syncTimestamp);
        syncHtracePluginEndTime_ = std::max(syncHtracePluginEndTime_, syncTimestamp);
    } else {
        asyncHtracePluginStartTime_ = std::min(asyncHtracePluginStartTime_, syncTimestamp);
        asyncHtracePluginEndTime_ = std::max(asyncHtracePluginEndTime_, syncTimestamp);
    }
}
uint64_t HtracePluginTime::GetPluginStartTime()
{
    if (syncHtracePluginStartTime_ != std::numeric_limits<uint64_t>::max()) {
        return syncHtracePluginStartTime_;
    } else if (asyncHtracePluginStartTime_ != std::numeric_limits<uint64_t>::max()) {
        return asyncHtracePluginStartTime_;
    }
    return std::numeric_limits<uint64_t>::max();
}

uint64_t HtracePluginTime::GetPluginEndTime()
{
    if (syncHtracePluginEndTime_ != 0) {
        return syncHtracePluginEndTime_;
    } else if (asyncHtracePluginEndTime_ != 0) {
        return asyncHtracePluginEndTime_;
    }
    return 0;
}
} // namespace TraceStreamer
} // namespace SysTuning

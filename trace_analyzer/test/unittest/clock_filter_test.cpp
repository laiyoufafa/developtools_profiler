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

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "clock_filter.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
namespace SysTuning {
namespace TraceStreamer {
class ClockFilterTest : public ::testing::Test {
public:
    void SetUp()
    {
        streamFilters_.clockFilter_ = std::make_unique<ClockFilter>(&traceDataCache_, &streamFilters_);
    }

    void TearDown() {}

public:
    SysTuning::TraceStreamer::TraceStreamerFilters streamFilters_;
    SysTuning::TraceStreamer::TraceDataCache traceDataCache_;
};
HWTEST_F(ClockFilterTest, ConvertTimestamp, TestSize.Level1)
{
    TS_LOGI("test2-1");
    std::vector<SnapShot> snapShot0;
    snapShot0.push_back({TS_CLOCK_BOOTTIME, 100});
    snapShot0.push_back({TS_MONOTONIC, 200});
    snapShot0.push_back({TS_CLOCK_REALTIME, 300});
    snapShot0.push_back({TS_CLOCK_REALTIME_COARSE, 400});
    streamFilters_.clockFilter_->AddClockSnapshot(snapShot0);

    std::vector<SnapShot> snapShot1;
    snapShot1.push_back({TS_CLOCK_BOOTTIME, 200});
    snapShot1.push_back({TS_MONOTONIC, 350});
    snapShot1.push_back({TS_CLOCK_REALTIME, 400});
    snapShot1.push_back({TS_CLOCK_REALTIME_COARSE, 800});
    streamFilters_.clockFilter_->AddClockSnapshot(snapShot1);

    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_CLOCK_BOOTTIME, 150, TS_MONOTONIC), static_cast<uint64_t>(250));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_CLOCK_BOOTTIME, 200, TS_MONOTONIC), static_cast<uint64_t>(350));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_CLOCK_BOOTTIME, 101, TS_CLOCK_REALTIME),
              static_cast<uint64_t>(301));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_CLOCK_BOOTTIME, 102, TS_CLOCK_REALTIME_COARSE),
              static_cast<uint64_t>(402));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_MONOTONIC, 101, TS_CLOCK_REALTIME), static_cast<uint64_t>(201));

    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_CLOCK_REALTIME, 351, TS_MONOTONIC), static_cast<uint64_t>(251));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_CLOCK_REALTIME, 401, TS_MONOTONIC), static_cast<uint64_t>(351));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_MONOTONIC, 150, TS_CLOCK_BOOTTIME), static_cast<uint64_t>(50));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_MONOTONIC, 250, TS_CLOCK_BOOTTIME), static_cast<uint64_t>(150));
    EXPECT_EQ(streamFilters_.clockFilter_->Convert(TS_MONOTONIC, 351, TS_CLOCK_BOOTTIME), static_cast<uint64_t>(201));
}

HWTEST_F(ClockFilterTest, ConvertToPrimary, TestSize.Level1)
{
    TS_LOGI("test2-2");
    std::vector<SnapShot> snapShot0;
    snapShot0.push_back({TS_CLOCK_BOOTTIME, 100});
    snapShot0.push_back({TS_CLOCK_REALTIME, 200});
    snapShot0.push_back({CLOCK_REALTIME, 300});
    snapShot0.push_back({TS_CLOCK_REALTIME_COARSE, 400});
    streamFilters_.clockFilter_->AddClockSnapshot(snapShot0);

    std::vector<SnapShot> snapShot1;
    snapShot1.push_back({TS_CLOCK_BOOTTIME, 200});
    snapShot1.push_back({TS_CLOCK_REALTIME, 350});
    snapShot1.push_back({CLOCK_REALTIME, 400});
    snapShot1.push_back({TS_CLOCK_REALTIME_COARSE, 800});
    streamFilters_.clockFilter_->AddClockSnapshot(snapShot1);

    streamFilters_.clockFilter_->SetPrimaryClock(CLOCK_REALTIME);

    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_BOOTTIME, 150), static_cast<uint64_t>(350));
    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_BOOTTIME, 101), static_cast<uint64_t>(301));
    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, 101), static_cast<uint64_t>(201));
    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, 351), static_cast<uint64_t>(401));
    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME_COARSE, 350),
              static_cast<uint64_t>(250));
    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME_COARSE, 420),
              static_cast<uint64_t>(320));
    EXPECT_EQ(streamFilters_.clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME_COARSE, 801),
              static_cast<uint64_t>(401));
}
} // namespace TraceStreamer
} // namespace SysTuning

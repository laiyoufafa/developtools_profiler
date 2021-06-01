/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "src/trace_analyzer/tracker/track_tracker.h"

using namespace testing::ext;

class TrackTrackerTest : public ::testing::Test {
protected:
    void SetUp() override
    {
        context_.storage = std::make_unique<SysTuning::trace_analyzer::TraceStorage>();
        context_.trackTracker_ = std::make_unique<SysTuning::trace_analyzer::TrackTracker>(&context_);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(TrackTrackerTest, AddCpuCounterTrack, TestSize.Level1)
{
    uint32_t trackId = context_.trackTracker_->AddTrack("cpu_counter_track", "cpu1");
    EXPECT_EQ(trackId, 0);

    trackId = context_.trackTracker_->AddTrack("cpu_counter_track", "cpu2");
    EXPECT_EQ(trackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);
}

HWTEST_F(TrackTrackerTest, AddThreadTrack, TestSize.Level1)
{
    uint32_t threadTrackId = context_.trackTracker_->AddTrack("thread_counter_track", "threadCount1");
    EXPECT_EQ(threadTrackId, 0);

    threadTrackId = context_.trackTracker_->AddTrack("thread_counter_track", "threadCount2");
    EXPECT_EQ(threadTrackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);

    threadTrackId = context_.trackTracker_->AddTrack("thread_track", "thread1");
    EXPECT_EQ(threadTrackId, 2);

    threadTrackId = context_.trackTracker_->AddTrack("thread_track", "thread2");
    EXPECT_EQ(threadTrackId, 3);

    EXPECT_EQ(trackTable->count(), 4);
}

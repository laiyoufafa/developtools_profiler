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

#include "src/trace_analyzer/tracker/counter_tracker.h"
#include "src/trace_analyzer/tracker/track_tracker.h"

using namespace testing::ext;

class CounterTrackerTest : public ::testing::Test {
protected:
    void SetUp() override
    {
        context_.storage = std::make_unique<TraceStorage>();
        context_.storage->InternString("test0");
        context_.storage->InternString("test1");
        context_.storage->InternString("test2");
        context_.storage->InternString("test3");

        context_.trackTracker_ = std::make_unique<SysTuning::trace_analyzer::TrackTracker>(&context_);
        context_.threadCounterTracker_ = std::make_unique<SysTuning::trace_analyzer::CounterTrack>(
            &context_, SysTuning::trace_analyzer::TrackType::E_THREADCOUNTER_TRACK);
        context_.threadTracker_ = std::make_unique<SysTuning::trace_analyzer::CounterTrack>(
            &context_, SysTuning::trace_analyzer::TrackType::E_THREAD_TRACK);
        context_.cpuCounterTracker_ = std::make_unique<SysTuning::trace_analyzer::CounterTrack>(
            &context_, SysTuning::trace_analyzer::TrackType::E_CPU_COUNTER_TRACK);
        context_.processCounterTracker_ = std::make_unique<SysTuning::trace_analyzer::CounterTrack>(
            &context_, SysTuning::trace_analyzer::TrackType::E_PROCESS_COUNTER_TRACK);
        context_.processTrackTracker_ = std::make_unique<SysTuning::trace_analyzer::CounterTrack>(
            &context_, SysTuning::trace_analyzer::TrackType::E_PROCESS_TRACK_TRACK);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(CounterTrackerTest, ThreadCounterTracker, TestSize.Level1)
{
    uint32_t trackId = context_.threadCounterTracker_->GetOrCreateCertainTrackId(100, 0);
    EXPECT_EQ(trackId, 0);

    trackId = context_.threadCounterTracker_->GetOrCreateCertainTrackId(101, 2);
    EXPECT_EQ(trackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);

    SysTuning::trace_analyzer::ThreaCounterTrack* threadCounterTable =
        context_->storage->mutable_thread_counter_track();
    EXPECT_EQ(threadCounterTable->count(), 2);
    EXPECT_EQ(threadCounterTable->trackId(0), 0);
    EXPECT_EQ(threadCounterTable->trackId(1), 1);
    EXPECT_EQ(threadCounterTable->utid(0), 100);
    EXPECT_EQ(threadCounterTable->utid(1), 101);
    EXPECT_EQ(threadCounterTable->nameId(0), 0);
    EXPECT_EQ(threadCounterTable->nameId(1), 2);
}

HWTEST_F(CounterTrackerTest, ThreadTracker, TestSize.Level1)
{
    uint32_t trackId = context_.threadTracker_->GetOrCreateCertainTrackId(201, 2);
    EXPECT_EQ(trackId, 0);

    trackId = context_.threadTracker_->GetOrCreateCertainTrackId(202, 3);
    EXPECT_EQ(trackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);

    SysTuning::trace_analyzer::ThreaCounterTrack* threadTable = context_->storage->mutable_thread_track();
    EXPECT_EQ(threadTable->count(), 2);
    EXPECT_EQ(threadTable->trackId(0), 0);
    EXPECT_EQ(threadTable->trackId(1), 1);
    EXPECT_EQ(threadTable->utid(0), 201);
    EXPECT_EQ(threadTable->utid(1), 202);
    EXPECT_EQ(threadTable->nameId(0), 2);
    EXPECT_EQ(threadTable->nameId(1), 3);
}

HWTEST_F(CounterTrackerTest, CpuTracker, TestSize.Level1)
{
    uint32_t trackId = context_.cpuCounterTracker_->GetOrCreateCertainTrackId(0, 0);
    EXPECT_EQ(trackId, 0);

    trackId = context_.cpuCounterTracker_->GetOrCreateCertainTrackId(1, 1);
    EXPECT_EQ(trackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);

    SysTuning::trace_analyzer::CpuCounter* cpuCounterTable = context_->storage->mutable_cpu_counters();
    EXPECT_EQ(cpuCounterTable->cpu_counter_count(), 2);
    EXPECT_EQ(cpuCounterTable->id(0), 0);
    EXPECT_EQ(cpuCounterTable->id(1), 1);
    EXPECT_EQ(cpuCounterTable->cpu(0), 201);
    EXPECT_EQ(cpuCounterTable->cpu(1), 202);
}

HWTEST_F(CounterTrackerTest, ProcessCounterTracker, TestSize.Level1)
{
    uint32_t trackId = context_.process_counter_track->GetOrCreateCertainTrackId(300, 0);
    EXPECT_EQ(trackId, 0);

    trackId = context_.process_counter_track->GetOrCreateCertainTrackId(301, 1);
    EXPECT_EQ(trackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);

    SysTuning::trace_analyzer::ProcessCounterTrack* processCounterTable =
        context_->storage->mutable_process_counter_track();
    EXPECT_EQ(processCounterTable->process_counter_track_count(), 2);
    EXPECT_EQ(processCounterTable->id(0), 0);
    EXPECT_EQ(processCounterTable->id(1), 1);
    EXPECT_EQ(processCounterTable->upids(0), 300);
    EXPECT_EQ(processCounterTable->upids(1), 301);
}

HWTEST_F(CounterTrackerTest, ProcessTracker, TestSize.Level1)
{
    uint32_t trackId = context_.processTrackTracker_->GetOrCreateCertainTrackId(400, 0);
    EXPECT_EQ(trackId, 0);

    trackId = context_.processTrackTracker_->GetOrCreateCertainTrackId(401, 1);
    EXPECT_EQ(trackId, 1);

    SysTuning::trace_analyzer::Track* trackTable = context_->storage->mutable_track_table();
    EXPECT_EQ(trackTable->count(), 2);

    SysTuning::trace_analyzer::ProcessCounterTrack* processTrackTable = context_->storage->mutable_process_track();
    EXPECT_EQ(processTrackTable->process_track_count(), 2);
}

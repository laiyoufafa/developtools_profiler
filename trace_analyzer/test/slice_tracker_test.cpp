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
#include "src/trace_analyzer/tracker/process_tracker.h"
#include "src/trace_analyzer/tracker/slice_tracker.h"
#include "src/trace_analyzer/tracker/track_tracker.h"

using namespace testing::ext;

class SliceTrackerTest : public ::testing::Test {
protected:
    void SetUp() override
    {
        context_.storage = std::make_unique<SysTuning::trace_analyzer::TraceStorage>();
        context_.storage->InternString("category0");
        context_.storage->InternString("category1");
        context_.storage->InternString("name0");
        context_.storage->InternString("name1");

        context_.trackTracker_ = std::make_unique<SysTuning::trace_analyzer::TrackTracker>(&context_);
        context_.processTracker_ = std::make_unique<SysTuning::trace_analyzer::ProcessTracker>(&context_);
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
        context_.sliceTracker_ = std::make_unique<SysTuning::trace_analyzer::SliceTracker>(&context_);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(SliceTrackerTest, SliceTest, TestSize.Level1)
{
    context_.sliceTracker_->BeginSlice(100000, 200, 200, 0, 2);
    context_.sliceTracker_->EndSlice(105000, 200, 200);

    SysTuning::trace_analyzer::Slices* slices = context_->storage->mutable_slices();
    EXPECT_GT(slices->slice_count(), 0);
}

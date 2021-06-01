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

#include "src/trace_analyzer/tracker/cpu_tracker.h"

using namespace testing::ext;

class EventTrackerTest : public ::testing::Test {
protected:
    void SetUp() override
    {
        context_.storage = std::make_unique<SysTuning::trace_analyzer::TraceStorage>();
        context_.storage->InternString("process0");
        context_.storage->InternString("process1");
        context_.storage->InternString("process2");
        context_.storage->InternString("process3");
        context_.storage->InternString("thread4");
        context_.storage->InternString("thread5");
        context_.storage->InternString("thread6");

        context_.trackTracker_ = std::make_unique<SysTuning::trace_analyzer::TrackTracker>(&context_);
        context_.process_tracker = std::make_unique<SysTuning::trace_analyzer::ProcessTracker>(&context_);
        context_.eventTracker_ = std::make_unique<SysTuning::trace_analyzer::EventTracker>(&context_);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(EventTrackerTest, PushSchedSwitch, TestSize.Level1)
{
    context_.eventTracker_->PushSchedSwitch(0, 100000, 101, 200, "threadTest0");
    context_.eventTracker_->PushSchedSwitch(0, 110000, 200, 201, "threadTest1");
    context_.eventTracker_->PushSchedSwitch(0, 120000, 201, 203, "threadTest2");
    context_.eventTracker_->PushSchedSwitch(0, 140000, 203, 205, "threadTest3");
    context_.eventTracker_->PushSchedSwitch(0, 170000, 205, 207, "threadTest4");
    context_.eventTracker_->PushSchedSwitch(0, 200000, 207, 209, "threadTest5");

    SysTuning::trace_analyzer::Slices* slices = context_->storage->mutable_slices();

    EXPECT_GT(slices->slice_count(), 6);
}

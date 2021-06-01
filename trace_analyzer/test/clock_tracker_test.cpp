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

#include "src/trace_analyzer/tracker/clock_tracker.h"

using namespace testing::ext;

class ClockTrackerTest : public ::testing::Test {
protected:
    void SetUp() override
    {
        context_.storage = std::make_unique<SysTuning::trace_analyzer::TraceStorage>();
        context_.clockTracker_ = std::make_unique<SysTuning::trace_analyzer::ClockTracker>(&context_);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(ClockTrackerTest, ConvertTimestamp, TestSize.Level1)
{
    std::vector<SysTuning::trace_analyzer::SnapShot> snapShot0;
    SysTuning::trace_analyzer::SnapShot a{1, 100};
    SysTuning::trace_analyzer::SnapShot b{2, 200};
    SysTuning::trace_analyzer::SnapShot c{3, 300};
    SysTuning::trace_analyzer::SnapShot d{4, 400};
    snapShot0.push_back(a);
    snapShot0.push_back(b);
    snapShot0.push_back(c);
    snapShot0.push_back(d);
    context_.clockTracker_->AddClockSnapshot(snapShot0);

    std::vector<SysTuning::trace_analyzer::SnapShot> snapShot1;
    SysTuning::trace_analyzer::SnapShot a1{1, 200};
    SysTuning::trace_analyzer::SnapShot b1{2, 350};
    SysTuning::trace_analyzer::SnapShot c1{3, 400};
    SysTuning::trace_analyzer::SnapShot d1{4, 800};
    snapShot1.push_back(a1);
    snapShot1.push_back(b1);
    snapShot1.push_back(c1);
    snapShot1.push_back(d1);
    context_.clockTracker_->AddClockSnapshot(snapShot1);

    EXPECT_EQ(context_.clockTracker_->Convert(1, 150, 2), 250);
    EXPECT_EQ(context_.clockTracker_->Convert(1, 200, 2), 350);
    EXPECT_EQ(context_.clockTracker_->Convert(1, 101, 3), 301);
    EXPECT_EQ(context_.clockTracker_->Convert(1, 102, 4), 402);
    EXPECT_EQ(context_.clockTracker_->Convert(2, 101, 3), 201);

    EXPECT_EQ(context_.clockTracker_->Convert(3, 351, 2), 251);
    EXPECT_EQ(context_.clockTracker_->Convert(3, 401, 2), 351);
    EXPECT_EQ(context_.clockTracker_->Convert(2, 150, 1), 50);
    EXPECT_EQ(context_.clockTracker_->Convert(2, 250, 1), 150);
    EXPECT_EQ(context_.clockTracker_->Convert(2, 351, 1), 201);
}

HWTEST_F(ClockTrackerTest, ConvertToPrimary, TestSize.Level1)
{
    std::vector<SysTuning::trace_analyzer::SnapShot> snapShot0;
    SysTuning::trace_analyzer::SnapShot a{1, 100};
    SysTuning::trace_analyzer::SnapShot b{2, 200};
    SysTuning::trace_analyzer::SnapShot c{3, 300};
    SysTuning::trace_analyzer::SnapShot d{4, 400};
    snapShot0.push_back(a);
    snapShot0.push_back(b);
    snapShot0.push_back(c);
    snapShot0.push_back(d);
    context_.clockTracker_->AddClockSnapshot(snapShot0);

    std::vector<SysTuning::trace_analyzer::SnapShot> snapShot1;
    SysTuning::trace_analyzer::SnapShot a1{1, 200};
    SysTuning::trace_analyzer::SnapShot b1{2, 350};
    SysTuning::trace_analyzer::SnapShot c1{3, 400};
    SysTuning::trace_analyzer::SnapShot d1{4, 800};
    snapShot1.push_back(a1);
    snapShot1.push_back(b1);
    snapShot1.push_back(c1);
    snapShot1.push_back(d1);
    context_.clockTracker_->AddClockSnapshot(snapShot1);

    context_.clockTracker_->SetPrimaryClock(3);

    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(1, 150), 350);
    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(1, 101), 301);
    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(2, 101), 201);
    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(2, 351), 401);
    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(4, 350), 250);
    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(4, 420), 320);
    EXPECT_EQ(context_.clockTracker_->ToPrimaryTraceTime(4, 801), 401);
}

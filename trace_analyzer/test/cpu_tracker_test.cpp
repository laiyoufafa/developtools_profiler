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

class CpuTrackerTest : public ::testing::Test {
protected:
    void SetUp() override
    {
        context_.storage = std::make_unique<SysTuning::trace_analyzer::TraceStorage>();
        context_.cpuTracker_ = std::make_unique<SysTuning::trace_analyzer::CpuTracker>(&context_);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(CpuTrackerTest, GenThreadStateTable, TestSize.Level1)
{
    context_.cpuTracker_->InsertWakeingEvent(10000, 200);
    context_.cpuTracker_->InsertSwitchEvent(10500, 0, 105, 120, 1, 200, 120);
    context_.cpuTracker_->InsertSwitchEvent(10700, 0, 200, 120, 1, 210, 120);
    context_.cpuTracker_->InsertWakeingEvent(11000, 200);
    context_.cpuTracker_->InsertSwitchEvent(11100, 0, 110, 120, 1, 200, 120);
    context_.cpuTracker_->InsertSwitchEvent(11500, 0, 200, 120, 1, 210, 120);
    context_.cpuTracker_->InsertWakeingEvent(12000, 200);
    context_.cpuTracker_->InsertSwitchEvent(12600, 0, 120, 120, 1, 200, 120);
    context_.cpuTracker_->InsertSwitchEvent(12800, 0, 200, 120, 16, 210, 120);

    SysTuning::trace_analyzer::ThreadStateStorage* threadStateTable = context_->storage->mutable_ThreadStateObj();

    EXPECT_GT(threadStateTable->thread_state_count(), 6);
    EXPECT_EQ(context_.cpuTracker_->FindUtidInThreadStateTableByCpu(0), 210);
}

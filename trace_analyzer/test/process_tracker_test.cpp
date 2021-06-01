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

#include "src/trace_analyzer/tracker/process_tracker.h"

using namespace testing::ext;

class ProcessTrackerTest : public ::testing::Test {
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

        context_.processTracker_ = std::make_unique<SysTuning::trace_analyzer::ProcessTracker>(&context_);
    }

    void TearDown() override {}

private:
    SysTuning::trace_analyzer::TraceAnalyzerContext context_;
};

HWTEST_F(ProcessTrackerTest, UpdateThread, TestSize.Level1)
{
    uint32_t utid0 = context_.processTracker_->UpdateThread(1000, 101, 4);
    EXPECT_EQ(utid0, 1);

    uint32_t utid1 = context_.processTracker_->UpdateThread(100, 95);
    EXPECT_EQ(utid1, 2);

    SysTuning::trace_analyzer::Thread* thread = context_->storage->GetMutableThread(utid0);
    EXPECT_EQ(thread->tid, 101);
    EXPECT_EQ(thread->start_ns, 10000);

    thread = context_->storage->GetMutableThread(utid0);
    EXPECT_EQ(thread->tid, 100);
    EXPECT_EQ(thread->upid, 1);
}

HWTEST_F(ProcessTrackerTest, UpdateProcess, TestSize.Level1)
{
    uint32_t upid0 = context_.processTracker_->UpdateProcess(1000);
    EXPECT_EQ(upid0, 1);

    SysTuning::trace_analyzer::Process* process = context_->storage->GetMutableProcess(upid0);
    EXPECT_EQ(process->pid, 1000);

    process = context_->storage->GetMutableProcess(upid1);
    EXPECT_EQ(process->pid, 1001);
}

HWTEST_F(ProcessTrackerTest, Update, TestSize.Level1)
{
    uint32_t upid0 = context_.processTracker_->UpdateProcess(2000);
    EXPECT_EQ(upid0, 1);

    SysTuning::trace_analyzer::Process* process = context_->storage->GetMutableProcess(upid0);
    EXPECT_EQ(process->pid, 2000);

    SysTuning::trace_analyzer::Thread* thread = context_->storage->GetMutableThread(1);
    EXPECT_EQ(thread->tid, 2000);

    SysTuning::trace_analyzer::Thread* thread = context_->storage->GetMutableThread(2);
    EXPECT_EQ(thread->tid, 2001);
}

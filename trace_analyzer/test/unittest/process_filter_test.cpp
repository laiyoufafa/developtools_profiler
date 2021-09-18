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

#include "process_filter.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
namespace SysTuning {
namespace TraceStreamer {
class ProcessFilterTest : public ::testing::Test {
public:
    void SetUp()
    {
        streamFilters_.processFilter_ = std::make_unique<ProcessFilter>(&traceDataCache_, &streamFilters_);
    }

    void TearDown() {}

public:
    TraceStreamerFilters streamFilters_;
    TraceDataCache traceDataCache_;
};

HWTEST_F(ProcessFilterTest, UpdateOrCreateThread, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iTid0 = streamFilters_.processFilter_->UpdateOrCreateThread(168758662877000, 2716);
    EXPECT_TRUE(iTid0 == 1);

    uint32_t iTid1 = streamFilters_.processFilter_->UpdateOrCreateThread(2716, 2519);
    EXPECT_TRUE(iTid1 == 2);

    Thread* thread = traceDataCache_.GetThreadData(iTid0);
    EXPECT_TRUE(thread->tid_ == 2716);
    EXPECT_TRUE(thread->startT_ == 168758662877000);

    thread = traceDataCache_.GetThreadData(iTid1);
    EXPECT_TRUE(thread->tid_ == 2519);
    EXPECT_TRUE(thread->internalPid_ == 0);
}

HWTEST_F(ProcessFilterTest, UpdateOrCreateProcessWithName, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iPid0 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8629, "RenderThread");
    EXPECT_TRUE(iPid0 == 1);

    uint32_t iPid1 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8709, "RenderThread");
    EXPECT_TRUE(iPid1 == 2);

    Process* process = traceDataCache_.GetProcessData(iPid0);
    EXPECT_TRUE(process->pid_ == 8629);

    process = traceDataCache_.GetProcessData(iPid1);
    EXPECT_TRUE(process->pid_ == 8709);
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateProcessWithName2, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iPid0 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8629, "RenderThread");
    EXPECT_TRUE(iPid0 == 1);
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateProcessWithName3, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iPid0 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8629, "RenderThread");
    EXPECT_TRUE(iPid0 == 1);

    uint32_t iPid1 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8709, "RenderThread");
    EXPECT_TRUE(iPid1 == 2);
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateProcessWithName4, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iPid0 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8629, "RenderThread");
    EXPECT_TRUE(iPid0 == 1);

    Process* process = traceDataCache_.GetProcessData(iPid0);
    EXPECT_TRUE(process->pid_ == 8629);
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateProcessWithName5, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iPid0 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8629, "RenderThread");
    EXPECT_TRUE(iPid0 == 1);

    uint32_t iPid1 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(8709, "RenderThread");
    EXPECT_TRUE(iPid1 == 2);

    uint32_t iPid2 = streamFilters_.processFilter_->UpdateOrCreateProcessWithName(87091, "RenderThread");
    EXPECT_TRUE(iPid2 == 3);

    Process* process = traceDataCache_.GetProcessData(iPid0);
    EXPECT_TRUE(process->pid_ == 8629);

    process = traceDataCache_.GetProcessData(iPid1);
    EXPECT_TRUE(process->pid_ == 8709);

    process = traceDataCache_.GetProcessData(iPid2);
    EXPECT_TRUE(process->pid_ == 87091);
}

HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithName, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iTid0 =streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758662957020, 123, "RenderThread");
    EXPECT_TRUE(iTid0 == 1);
    uint32_t iTid1 = streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758663957020, 2519,
        "RenderThread2");
    EXPECT_TRUE(iTid1 == 2);

    Thread* thread = traceDataCache_.GetThreadData(iTid0);
    EXPECT_TRUE(thread->tid_ == 123);
    EXPECT_TRUE(thread->startT_ == 168758662957020);
    EXPECT_TRUE(thread->nameIndex_ == traceDataCache_.GetDataIndex("RenderThread"));

    thread = traceDataCache_.GetThreadData(iTid1);
    EXPECT_TRUE(thread->tid_ == 2519);
    EXPECT_TRUE(thread->internalPid_ == 0);
    EXPECT_TRUE(thread->nameIndex_ == traceDataCache_.GetDataIndex("RenderThread2"));
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithName2, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iTid0 =streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758662957020, 123, "RenderThread");
    EXPECT_TRUE(iTid0 == 1);
}

HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithName3, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iTid0 =streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758662957020, 123, "RenderThread2");
    EXPECT_TRUE(iTid0 == 1);
    Thread* thread = traceDataCache_.GetThreadData(iTid0);
    EXPECT_TRUE(thread->tid_ == 123);
    EXPECT_TRUE(thread->startT_ == 168758662957020);
    EXPECT_TRUE(thread->nameIndex_ == traceDataCache_.GetDataIndex("RenderThread2"));
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithName4, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iTid0 =streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758662957020, 123, "RenderThread");
    EXPECT_TRUE(iTid0 == 1);
    uint32_t iTid1 = streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758663957020, 2519,
        "RenderThread2");
    EXPECT_TRUE(iTid1 == 2);
    auto thread = traceDataCache_.GetThreadData(iTid1);
    EXPECT_TRUE(thread->tid_ == 2519);
    EXPECT_TRUE(thread->internalPid_ == 0);
    EXPECT_TRUE(thread->nameIndex_ == traceDataCache_.GetDataIndex("RenderThread2"));
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithName5, TestSize.Level1)
{
    TS_LOGI("test10-1");
    uint32_t iTid0 =streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758662957020, 123, "RenderThread");
    EXPECT_TRUE(iTid0 == 1);
    uint32_t iTid1 = streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758663957020, 2519,
        "RenderThread2");
    EXPECT_TRUE(iTid1 == 2);
    uint32_t iTid2 = streamFilters_.processFilter_->UpdateOrCreateThreadWithName(168758663957020, 25191,
        "RenderThread3");
    EXPECT_TRUE(iTid2 == 3);
    auto thread = traceDataCache_.GetThreadData(iTid2);
    EXPECT_TRUE(thread->tid_ == 25191);
    EXPECT_TRUE(thread->internalPid_ == 0);
    EXPECT_TRUE(thread->nameIndex_ == traceDataCache_.GetDataIndex("RenderThread3"));
}

HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithPidAndName, TestSize.Level1)
{
    TS_LOGI("test10-1");
    streamFilters_.processFilter_->UpdateOrCreateThreadWithPidAndName(869, 123, "RenderThread");
    auto itid = streamFilters_.processFilter_->GetInternalTid(869);
    EXPECT_TRUE(itid != INVALID_ID);

    Thread* thread = traceDataCache_.GetThreadData(itid);
    EXPECT_TRUE(thread->nameIndex_ == traceDataCache_.GetDataIndex("RenderThread"));
}
HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithPidAndName2, TestSize.Level1)
{
    TS_LOGI("test10-1");
    streamFilters_.processFilter_->UpdateOrCreateThreadWithPidAndName(869, 123, "RenderThread");
    auto itid = streamFilters_.processFilter_->GetInternalTid(969);
    EXPECT_TRUE(itid == INVALID_ID);
}

HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithPidAndName3, TestSize.Level1)
{
    TS_LOGI("test10-1");
    streamFilters_.processFilter_->UpdateOrCreateThreadWithPidAndName(869, 123, "RenderThread");
    auto itid = streamFilters_.processFilter_->GetInternalPid(123);
    EXPECT_TRUE(itid != INVALID_ID);
}

HWTEST_F(ProcessFilterTest, UpdateOrCreateThreadWithPidAndName4, TestSize.Level1)
{
    TS_LOGI("test10-1");
    streamFilters_.processFilter_->UpdateOrCreateThreadWithPidAndName(869, 123, "RenderThread");
    auto itid = streamFilters_.processFilter_->GetInternalPid(124);
    EXPECT_TRUE(itid == INVALID_ID);
}
}
}

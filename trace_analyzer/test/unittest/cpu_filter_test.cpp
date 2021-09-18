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

#include "common.h"
#include "cpu_filter.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
namespace SysTuning {
namespace TraceStreamer {
class CpuFilterTest : public ::testing::Test {
public:
    void SetUp()
    {
        streamFilters_.cpuFilter_ = std::make_unique<CpuFilter>(&traceDataCache_, &streamFilters_);
    }

    void TearDown() {}

public:
    TraceStreamerFilters streamFilters_;
    TraceDataCache traceDataCache_;
};

HWTEST_F(CpuFilterTest, CpufilterTest1, TestSize.Level1)
{
    TS_LOGI("test3-1");
    /* InsertWakeingEvent ts, internalTid */
    /* InsertSwitchEvent                         ts,             cpu, prevPid, prevPior, prevState, nextPid, nextPior */
    streamFilters_.cpuFilter_->InsertWakeingEvent(168758662877000, 1);  // 1st waking

    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(1) == INVALID_UINT64);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(1) == TASK_INVALID);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 0); // 0 thread state only

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758662919000, 0, 1, 120, TASK_INTERRUPTIBLE, 2, 124); // 1st switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(1) == TASK_INVALID);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(2) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(2) == 0);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 1); // 1 thread state

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663017000, 0, 0, 120, TASK_RUNNABLE, 4, 120);  // 2nd switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(4) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(4) == 1);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 2); // 2 thread state

    streamFilters_.cpuFilter_->InsertWakeingEvent(168758663078000, 0);  // 2nd waking

    streamFilters_.cpuFilter_->InsertWakeingEvent(168758663092000, 0);  // 3rd waking

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663107000, 0, 2, 124, TASK_RUNNABLE, 5, 98); // 3rd switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(5) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(2) == TASK_RUNNABLE);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(5) == 2);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(2) == 3);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 4); // 4 thread state

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663126000, 0, 5, 98, TASK_INTERRUPTIBLE, 2, 124);  // 4th switch
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(2) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(5) == TASK_INTERRUPTIBLE);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(2) == 4);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(5) == 5);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 6); // 6 thread state

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663136000, 3, 5, 120, TASK_RUNNABLE, 6, 120);  // 5th switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(6) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(6) == 6);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 8); // 8 thread state

    // after 3rd switch
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->DursData()[0] == 168758663107000 - 168758662919000);
    // after 4th switch
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->DursData()[2] == 168758663126000 - 168758663107000);
    // after 5th switch
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->DursData()[5] == 168758663136000 - 168758663126000);
}

HWTEST_F(CpuFilterTest, GenThreadStateTable, TestSize.Level1)
{
    TS_LOGI("test3-1");
    /* InsertWakeingEvent ts, internalTid */
    /* InsertSwitchEvent                         ts,             cpu, prevPid, prevPior, prevState, nextPid, nextPior */
    streamFilters_.cpuFilter_->InsertWakeingEvent(168758662877000, 1);  // 1st waking

    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(1) == INVALID_UINT64);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(1) == TASK_INVALID);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 0); // 0 thread state only

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758662919000, 0, 1, 120, TASK_INTERRUPTIBLE, 2, 124); // 1st switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(1) == TASK_INVALID);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(2) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(2) == 0);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 1); // 1 thread state

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663017000, 0, 0, 120, TASK_RUNNABLE, 4, 120);  // 2nd switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(4) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(4) == 1);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 2); // 2 thread state

    streamFilters_.cpuFilter_->InsertWakeingEvent(168758663078000, 0);  // 2nd waking

    streamFilters_.cpuFilter_->InsertWakeingEvent(168758663092000, 0);  // 3rd waking

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663107000, 0, 2, 124, TASK_RUNNABLE, 5, 98); // 3rd switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(5) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(2) == TASK_RUNNABLE);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(5) == 2);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(2) == 3);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 4); // 4 thread state

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663126000, 0, 5, 98, TASK_INTERRUPTIBLE, 2, 124);  // 4th switch
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(2) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(5) == TASK_INTERRUPTIBLE);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(2) == 4);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(5) == 5);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 6); // 6 thread state

    streamFilters_.cpuFilter_->InsertSwitchEvent(168758663136000, 3, 5, 120, TASK_RUNNABLE, 6, 120);  // 5th switch

    EXPECT_TRUE(streamFilters_.cpuFilter_->StateOfInternalTidThreadState(6) == TASK_RUNNING);
    EXPECT_TRUE(streamFilters_.cpuFilter_->RowOfInternalTidThreadState(6) == 6);
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->Size() == 8); // 8 thread state

    // after 3rd switch
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->DursData()[0] == 168758663107000 - 168758662919000);
    // after 4th switch
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->DursData()[2] == 168758663126000 - 168758663107000);
    // after 5th switch
    EXPECT_TRUE(traceDataCache_.GetThreadStateData()->DursData()[5] == 168758663136000 - 168758663126000);
}
}
}

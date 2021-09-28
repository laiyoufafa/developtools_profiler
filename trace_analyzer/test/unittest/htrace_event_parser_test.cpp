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

#include <fcntl.h>
#include <unordered_map>

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "htrace_cpu_detail_parser.h"
#include "parser/common_types.h"
#include "trace_streamer_selector.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
namespace SysTuning {
namespace TraceStreamer {
const uint64_t TIMESTAMP = 1616439852302;
const std::string THREAD_NAME_01 = "ACCS0";
const std::string THREAD_NAME_02 = "HeapTaskDaemon";
const uint32_t PRIORITY_01 = 120;
const uint32_t PRIORITY_02 = 124;
const uint32_t PID_01 = 2716;
const uint32_t PID_02 = 2532;
class HtraceEventParserTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }

    void TearDown() {}
public:
    SysTuning::TraceStreamer::TraceStreamerSelector stream_;
};

// schedSwitch event
HWTEST_F(HtraceEventParserTest, ParseDataItem01, TestSize.Level1)
{
    SchedSwitchFormat* event = new SchedSwitchFormat();
    event->set_prev_prio(PRIORITY_01);
    event->set_next_prio(PRIORITY_02);
    event->set_prev_pid(PID_01);
    event->set_next_pid(PID_02);
    event->set_prev_comm(THREAD_NAME_01);
    event->set_next_comm(THREAD_NAME_02);
    event->set_prev_state(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->unsafe_arena_set_allocated_sched_switch_format(event);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    EXPECT_TRUE(1);
    auto realTimeStamp = stream_.traceDataCache_->GetConstSchedSliceData().TimeStamData()[0];
    EXPECT_TRUE(TIMESTAMP == realTimeStamp);
    auto realCpu = stream_.traceDataCache_->GetConstSchedSliceData().CpusData()[0];
    EXPECT_TRUE(0 == realCpu);
}

// FtraceCpuDetailMsg has no ftrace event
HWTEST_F(HtraceEventParserTest, ParseDataItem02, TestSize.Level1)
{
    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);

    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_OTHER, STAT_EVENT_DATA_LOST);
    EXPECT_TRUE(1 == eventCount);
}

HWTEST_F(HtraceEventParserTest, ParseDataItem03, TestSize.Level1)
{
    SchedSwitchFormat* event = new SchedSwitchFormat();
    event->set_prev_prio(PRIORITY_01);
    event->set_next_prio(PRIORITY_02);
    event->set_prev_pid(PID_01);
    event->set_next_pid(PID_02);
    event->set_prev_comm(THREAD_NAME_01);
    event->set_next_comm(THREAD_NAME_02);
    event->set_prev_state(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(1);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_sched_switch_format(event);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_OTHER, STAT_EVENT_DATA_LOST);
    EXPECT_TRUE(1 == eventCount);
}

// task_rename event
HWTEST_F(HtraceEventParserTest, ParseDataItem04, TestSize.Level1)
{
    TaskRenameFormat* taskRenameEvent = new TaskRenameFormat();
    taskRenameEvent->set_pid(PID_01);
    taskRenameEvent->set_oldcomm(THREAD_NAME_01);
    taskRenameEvent->set_newcomm(THREAD_NAME_02);
    taskRenameEvent->set_oom_score_adj(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_task_rename_format(taskRenameEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_TASK_RENAME, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

// task_newtask event
HWTEST_F(HtraceEventParserTest, ParseDataItem05, TestSize.Level1)
{
    TaskNewtaskFormat* newTaskEvent = new TaskNewtaskFormat();
    newTaskEvent->set_pid(PID_01);
    newTaskEvent->set_comm(THREAD_NAME_01);
    newTaskEvent->set_clone_flags(0);
    newTaskEvent->set_oom_score_adj(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_task_newtask_format(newTaskEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_TASK_NEWTASK, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

// sched_wakeup event
HWTEST_F(HtraceEventParserTest, ParseDataItem06, TestSize.Level1)
{
    SchedWakeupFormat* wakeupEvent = new SchedWakeupFormat();
    wakeupEvent->set_comm(THREAD_NAME_01);
    wakeupEvent->set_pid(PRIORITY_02);
    wakeupEvent->set_prio(PID_01);
    wakeupEvent->set_target_cpu(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_sched_wakeup_format(wakeupEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_SCHED_WAKEUP, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

// sched_waking event
HWTEST_F(HtraceEventParserTest, ParseDataItem07, TestSize.Level1)
{
    SchedWakingFormat* wakingEvent = new SchedWakingFormat();
    wakingEvent->set_comm(THREAD_NAME_01);
    wakingEvent->set_pid(PRIORITY_02);
    wakingEvent->set_prio(PID_01);
    wakingEvent->set_target_cpu(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_sched_waking_format(wakingEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_SCHED_WAKING, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

HWTEST_F(HtraceEventParserTest, ParseDataItem08, TestSize.Level1)
{
    CpuIdleFormat* cpuIdleEvent = new CpuIdleFormat();
    cpuIdleEvent->set_cpu_id(0);
    cpuIdleEvent->set_state(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_cpu_idle_format(cpuIdleEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_CPU_IDLE, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

// CpuFrequency event
HWTEST_F(HtraceEventParserTest, ParseDataItem09, TestSize.Level1)
{
    CpuFrequencyFormat* cpuFrequencyEvent = new CpuFrequencyFormat();
    cpuFrequencyEvent->set_cpu_id(0);
    cpuFrequencyEvent->set_state(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(2);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_cpu_frequency_format(cpuFrequencyEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_CPU_FREQUENCY, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

// WorkqueueExecuteStart event
HWTEST_F(HtraceEventParserTest, ParseDataItem10, TestSize.Level1)
{
    WorkqueueExecuteStartFormat* workqueueExecuteStartEvent = new WorkqueueExecuteStartFormat();
    workqueueExecuteStartEvent->set_work(0);
    workqueueExecuteStartEvent->set_function(1);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_workqueue_execute_start_format(workqueueExecuteStartEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_WORKQUEUE_EXECUTE_START,
                                                                           STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

// WorkqueueExecuteEnd event
HWTEST_F(HtraceEventParserTest, ParseDataItem11, TestSize.Level1)
{
    WorkqueueExecuteEndFormat* workqueueExecuteEndEvent = new WorkqueueExecuteEndFormat();
    workqueueExecuteEndEvent->set_work(0);

    FtraceCpuDetailMsg ftraceCpuDetail;
    ftraceCpuDetail.set_cpu(0);
    ftraceCpuDetail.set_overwrite(0);
    auto ftraceEvent = ftraceCpuDetail.add_event();

    ftraceEvent->set_timestamp(TIMESTAMP);
    ftraceEvent->set_tgid(1);
    ftraceEvent->set_comm(THREAD_NAME_02);
    ftraceEvent->set_allocated_workqueue_execute_end_format(workqueueExecuteEndEvent);

    HtraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    eventParser.ParseDataItem(&ftraceCpuDetail, TS_CLOCK_BOOTTIME);
    auto eventCount =
        stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_EVENT_WORKQUEUE_EXECUTE_END, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}
} // namespace TraceStreamer
} // namespace SysTuning

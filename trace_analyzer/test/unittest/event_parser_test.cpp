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

#include "parser/bytrace_parser/bytrace_event_parser.h"
#include "parser/bytrace_parser/bytrace_parser.h"
#include "parser/common_types.h"
#include "trace_streamer_selector.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
namespace SysTuning {
namespace TraceStreamer {
const uint32_t G_BUF_SIZE = 1024;
// TestSuite:
class EventParserTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }

    void TearDown() {}

public:
    TraceStreamerSelector stream_{};
    const char* dbPath = "/data/resource/out.db";
};

HWTEST_F(EventParserTest, ParseLine, TestSize.Level1)
{
    TS_LOGI("test4-1");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.cpu = 0;
    bytraceLine.task = "ACCS0-2716";
    bytraceLine.pidStr = "12";
    bytraceLine.tGidStr = "12";
    bytraceLine.eventName = "sched_switch";
    ArgsMap args;
    args.insert(std::make_pair("prev_comm", "ACCS0"));
    args.insert(std::make_pair("prev_pid", "2716"));
    args.insert(std::make_pair("prev_prio", "120"));
    args.insert(std::make_pair("prev_state", "R"));
    args.insert(std::make_pair("next_comm", "kworker/0:0"));
    args.insert(std::make_pair("next_pid", "8326"));
    args.insert(std::make_pair("next_prio", "120"));
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.ParseDataItem(bytraceLine, args, 2519);

    EXPECT_EQ(result, true);
}
HWTEST_F(EventParserTest, ParseLineNotEnoughArgs, TestSize.Level1)
{
    TS_LOGI("test4-1");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.cpu = 0;
    bytraceLine.task = "ACCS0-2716";
    bytraceLine.pidStr = "12";
    bytraceLine.tGidStr = "12";
    bytraceLine.eventName = "sched_switch";
    ArgsMap args;
    args.insert(std::make_pair("prev_prio", "120"));
    args.insert(std::make_pair("prev_state", "R"));
    args.insert(std::make_pair("next_comm", "kworker/0:0"));
    args.insert(std::make_pair("next_pid", "8326"));
    args.insert(std::make_pair("next_prio", "120"));
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.ParseDataItem(bytraceLine, args, 2519);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, ParseLineUnCognizableEventname, TestSize.Level1)
{
    TS_LOGI("test4-2");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.cpu = 0;
    bytraceLine.task = "ACCS0-2716";
    bytraceLine.pidStr = "12";
    bytraceLine.tGidStr = "12";
    bytraceLine.eventName = "ThisEventNameDoNotExist"; // UnRecognizable event name
    ArgsMap args;
    args.insert(std::make_pair("prev_comm", "ACCS0"));
    args.insert(std::make_pair("prev_pid", "2716"));
    args.insert(std::make_pair("prev_prio", "120"));
    args.insert(std::make_pair("prev_state", "R"));
    args.insert(std::make_pair("next_comm", "kworker/0:0"));
    args.insert(std::make_pair("next_pid", "8326"));
    args.insert(std::make_pair("next_prio", "120"));
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.ParseDataItem(bytraceLine, args, 2519);

    EXPECT_EQ(result, false);
}
HWTEST_F(EventParserTest, ParseLineNoArgs, TestSize.Level1)
{
    TS_LOGI("test4-4");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.cpu = 0;
    bytraceLine.task = "ACCS0-2716";
    bytraceLine.pidStr = "12";
    bytraceLine.tGidStr = "12";
    bytraceLine.eventName = "sched_switch";
    ArgsMap args;
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.ParseDataItem(bytraceLine, args, 2519);

    EXPECT_EQ(result, false);
}
HWTEST_F(EventParserTest, ParseLineNoArgs2, TestSize.Level1)
{
    TS_LOGI("test4-4");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.cpu = 0;
    bytraceLine.task = "ACCS0-2716";
    bytraceLine.pidStr = "12";
    bytraceLine.tGidStr = "12";
    bytraceLine.eventName = "sched_wakeup";
    ArgsMap args;
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.ParseDataItem(bytraceLine, args, 2519);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, TracingMarkWriteC, TestSize.Level1)
{
    TS_LOGI("test4-7");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "ACCS0-2716  ( 2519) [000] ...1 174330.284808: tracing_mark_write: C|2519|Heap size (KB)|2906\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TracingMarkWriteBE, TestSize.Level1)
{
    TS_LOGI("test4-8");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "system-1298 ( 1298) [001] ...1 174330.287420: tracing_mark_write: B|1298|Choreographer#doFrame\n \
             system - 1298(1298)[001]... 1 174330.287622 : tracing_mark_write : E | 1298\n" // E | 1298 wrong format
    });
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    EXPECT_EQ(bytraceParser.ParsedTraceInvalidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TracingMarkWriteSF, TestSize.Level1)
{
    TS_LOGI("test4-9");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"system-1298 ( 1298) [001] ...1 174330.287478: tracing_mark_write: S|1298|animator:\
            translateX|18888109\n system-1298(1298)[001]... 1 174330.287514 : tracing_mark_write : \
            F | 1298 | animator : translateX | 18888109\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    EXPECT_EQ(bytraceParser.ParsedTraceInvalidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TracingMarkWriteErrorPoint, TestSize.Level1)
{
    TS_LOGI("test4-10");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: G|1298|animator: \
            translateX|18888109\n system-1298(1298)[001]... 1 174330.287514 : tracing_mark_write : \
            F | 1298 | animator : translateX | 18888109\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    EXPECT_EQ(bytraceParser.ParsedTraceInvalidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, CpuIdle, TestSize.Level1)
{
    TS_LOGI("test4-14");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"<idle>-0     (-----) [003] d..2 174330.280761: cpu_idle: state=2 cpu_id=3\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, IrqHandlerEntry, TestSize.Level1)
{
    TS_LOGI("test4-15");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "ACCS0-2716  ( 2519) [000] d.h1 174330.280362: irq_handler_entry: irq=19 name=408000.qcom,cpu-bwmon\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, IrqHandlerExit, TestSize.Level1)
{
    TS_LOGI("test4-16");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "ACCS0-2716  ( 2519) [000] d.h1 174330.280382: irq_handler_exit: irq=19 ret=handled\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, SchedWaking, TestSize.Level1)
{
    TS_LOGI("test4-17");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"ACCS0-2716  ( 2519) [000] d..5 174330.280567: sched_waking: \
            comm=Binder:924_6 pid=1332 prio=120 target_cpu=000\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();
    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, SchedWakeup, TestSize.Level1)
{
    TS_LOGI("test4-18");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"ACCS0-2716  ( 2519) [000] d..6 174330.280575: sched_wakeup: \
            comm=Binder:924_6 pid=1332 prio=120 target_cpu=000\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TraceEventClockSync, TestSize.Level1)
{
    TS_LOGI("test4-19");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"athread-12728 (12728) [003] ...1 174330.280300: tracing_mark_write: \
            trace_event_clock_sync:parent_ts=23139.998047\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TracingMarkWriteErrorPoint2, TestSize.Level1)
{
    TS_LOGI("test4-23");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: \
            G|1298|animator:translateX|18888109 system - 1298(1298)[001]... 1 174330.287514 : \
            tracing_mark_write : F | 1298 | animator : translateX | 18888109 \n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, SchedSwitch, TestSize.Level1)
{
    TS_LOGI("test4-27");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "ACCS0-2716  ( 2519) [000] d..3 174330.289220: sched_switch: prev_comm=ACCS0 prev_pid=2716 prev_prio=120 \
            prev_state=R+ ==> next_comm=Binder:924_6 next_pid=1332 next_prio=120\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TaskRename, TestSize.Level1)
{
    TS_LOGI("test4-28");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"<...>-2093  (-----) [001] ...2 174332.792290: task_rename: pid=12729 oldcomm=perfd \
            newcomm=POSIX timer 249 oom_score_adj=-1000\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, TaskNewtask, TestSize.Level1)
{
    TS_LOGI("test4-29");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"<...>-2     (-----) [003] ...1 174332.825588: task_newtask: pid=12730 \
            comm=kthreadd clone_flags=800711 oom_score_adj=0\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, WorkqueueExecuteStart, TestSize.Level1)
{
    TS_LOGI("test4-30");
    std::unique_ptr<uint8_t[]> buf(
        new uint8_t[G_BUF_SIZE]{"<...>-12180 (-----) [001] ...1 174332.827595: workqueue_execute_start: \
            work struct 0000000000000000: function pm_runtime_work\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, WorkqueueExecuteEnd, TestSize.Level1)
{
    TS_LOGI("test4-31");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "<...>-12180 (-----) [001] ...1 174332.828056: workqueue_execute_end: work struct 0000000000000000\n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}
HWTEST_F(EventParserTest, Distribute, TestSize.Level1)
{
    TS_LOGI("test4-31");
    std::unique_ptr<uint8_t[]> buf(new uint8_t[G_BUF_SIZE]{
        "system-1298 ( 1298) [001] ...1 174330.287420: tracing_mark_write: B|1298|[8b00e96b2,2,1]:C$#decodeFrame$#"
        "{\"Process\":\"DecodeVideoFrame\",\"frameTimestamp\":37313484466} \
            system - 1298(1298)[001]... 1 174330.287622 : tracing_mark_write : E | 1298 \n"});
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), G_BUF_SIZE);
    bytraceParser.WaitForParserEnd();

    EXPECT_EQ(bytraceParser.ParsedTraceValidLines(), static_cast<const unsigned int>(1));
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
}

HWTEST_F(EventParserTest, SchedSwitchEvent, TestSize.Level1)
{
    TS_LOGI("test4-32");
    BytraceLine bytraceLine;
    static std::unordered_map<std::string, std::string> args{{"prev_comm", "ACCS0"}, {"next_comm", "HeapTaskDaemon"},
                                                             {"prev_prio", "120"},   {"next_prio", "124"},
                                                             {"prev_pid", "2716"},   {"next_pid", "2532"},
                                                             {"prev_state", "S"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.SchedSwitchEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, SchedSwitchEventAbnormal, TestSize.Level1)
{
    TS_LOGI("test4-33");
    BytraceLine bytraceLine;
    static std::unordered_map<std::string, std::string> args{{"prev_comm", "ACCS0"}, {"next_comm", "HeapTaskDaemon"},
                                                             {"prev_prio", ""},      {"next_prio", ""},
                                                             {"prev_pid", ""},       {"next_pid", ""},
                                                             {"prev_state", "S"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.SchedSwitchEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, TaskRenameEvent, TestSize.Level1)
{
    TS_LOGI("test4-34");
    BytraceLine bytraceLine;
    static std::unordered_map<std::string, std::string> args{{"newcomm", "POSIX"}, {"pid", "8542"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.TaskRenameEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, TaskNewtaskEvent, TestSize.Level1)
{
    TS_LOGI("test4-35");
    BytraceLine bytraceLine;
    bytraceLine.tGidStr = "12";
    static std::unordered_map<std::string, std::string> args{{"comm", "POSIX"}, {"pid", "8542"}, {"clone_flags", "1"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.TaskNewtaskEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, TracingMarkWriteEvent, TestSize.Level1)
{
    TS_LOGI("test4-36");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.argsStr = "vec=9 [action=RCU]";
    static std::unordered_map<std::string, std::string> args{};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.TracingMarkWriteEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, SchedWakeupEvent, TestSize.Level1)
{
    TS_LOGI("test4-37");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    static std::unordered_map<std::string, std::string> args{{"pid", "1200"}, {"target_cpu", "1"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.SchedWakeupEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, SchedWakeupEventAbromal, TestSize.Level1)
{
    TS_LOGI("test4-38");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    static std::unordered_map<std::string, std::string> args{{"pid", ""}, {"target_cpu", "1"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.SchedWakeupEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, SchedWakingEvent, TestSize.Level1)
{
    TS_LOGI("test4-39");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    static std::unordered_map<std::string, std::string> args{{"pid", "1200"}, {"target_cpu", "1"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.SchedWakingEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, SchedWakingEventAbnormal, TestSize.Level1)
{
    TS_LOGI("test4-40");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    static std::unordered_map<std::string, std::string> args{{"pid", ""}, {"target_cpu", "1"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.SchedWakingEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, CpuIdleEvent, TestSize.Level1)
{
    TS_LOGI("test4-41");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.eventName = "POSIX";
    static std::unordered_map<std::string, std::string> args{{"cpu_id", "3"}, {"state", "4294967295"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CpuIdleEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, CpuIdleEventAbnormal1, TestSize.Level1)
{
    TS_LOGI("test4-42");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.eventName = "POSIX";
    static std::unordered_map<std::string, std::string> args{{"cpu_id", ""}, {"state", "4294967295"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CpuIdleEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, CpuIdleEventAbnormal2, TestSize.Level1)
{
    TS_LOGI("test4-43");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.eventName = "POSIX";
    static std::unordered_map<std::string, std::string> args{{"cpu_id", "1"}, {"state", ""}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CpuIdleEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, CpuFrequencyEvent, TestSize.Level1)
{
    TS_LOGI("test4-44");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.eventName = "POSIX";
    static std::unordered_map<std::string, std::string> args{{"cpu_id", "3"}, {"state", "4294967295"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CpuFrequencyEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, CpuFrequencyEventAbnormal1, TestSize.Level1)
{
    TS_LOGI("test4-45");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.eventName = "POSIX";
    static std::unordered_map<std::string, std::string> args{{"cpu_id", ""}, {"state", "4294967295"}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CpuFrequencyEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, CpuFrequencyEventAbnormal2, TestSize.Level1)
{
    TS_LOGI("test4-46");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.eventName = "POSIX";
    static std::unordered_map<std::string, std::string> args{{"cpu_id", "3"}, {"state", ""}};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CpuFrequencyEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, WorkqueueExecuteStartEvent, TestSize.Level1)
{
    TS_LOGI("test4-47");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1355;
    bytraceLine.argsStr = "vec=9 [action=RCU]";
    static std::unordered_map<std::string, std::string> args{};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.WorkqueueExecuteStartEvent(args, bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, WorkqueueExecuteEndEvent, TestSize.Level1)
{
    TS_LOGI("test4-48");
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1355;
    static std::unordered_map<std::string, std::string> args{};
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.WorkqueueExecuteEndEvent(args, bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, CheckTracePoint, TestSize.Level1)
{
    TS_LOGI("test4-49");
    std::string str("B|924|FullSuspendCheck");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CheckTracePoint(str);

    EXPECT_TRUE(result == SUCCESS);
}

HWTEST_F(EventParserTest, CheckTracePointAbnormal1, TestSize.Level1)
{
    TS_LOGI("test4-50");
    std::string str("");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CheckTracePoint(str);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, CheckTracePointAbnormal2, TestSize.Level1)
{
    TS_LOGI("test4-51");
    std::string str("trace_event_clock_sync");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CheckTracePoint(str);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, CheckTracePointAbnormal3, TestSize.Level1)
{
    TS_LOGI("test4-52");
    std::string str("BECSF|924|FullSuspendCheck");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CheckTracePoint(str);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, CheckTracePointAbnormal4, TestSize.Level1)
{
    TS_LOGI("test4-53");
    std::string str("X");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CheckTracePoint(str);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, CheckTracePointAbnormal5, TestSize.Level1)
{
    TS_LOGI("test4-54");
    std::string str("B&924|FullSuspendCheck");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.CheckTracePoint(str);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, GetTracePoint, TestSize.Level1)
{
    TS_LOGI("test4-55");
    TracePoint point;
    std::string str("B|924|SuspendThreadByThreadId suspended Binder:924_8 id=39");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.GetTracePoint(str, point);

    EXPECT_TRUE(result == SUCCESS);
}

HWTEST_F(EventParserTest, GetTracePointAbnormal1, TestSize.Level1)
{
    TS_LOGI("test4-56");
    TracePoint point;
    std::string str("");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.GetTracePoint(str, point);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, GetTracePointAbnormal2, TestSize.Level1)
{
    TS_LOGI("test4-57");
    TracePoint point;
    std::string str("X|924|SuspendThreadByThreadId suspended Binder:924_8 id=39");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.GetTracePoint(str, point);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, GetThreadGroupId, TestSize.Level1)
{
    TS_LOGI("test4-58");
    size_t length{0};
    std::string str("E|924");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.GetThreadGroupId(str, length);

    EXPECT_TRUE(result == 924);
}

HWTEST_F(EventParserTest, GetThreadGroupIdAbnormal, TestSize.Level1)
{
    TS_LOGI("test4-59");
    size_t length{0};
    std::string str("E|abc");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.GetThreadGroupId(str, length);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, HandlerB, TestSize.Level1)
{
    TS_LOGI("test4-60");
    size_t length{3};
    TracePoint outPoint;
    std::string str("B|924|HIDL::ISensors::batch::client");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.HandlerB(str, outPoint, length);

    EXPECT_TRUE(result == SUCCESS);
}

HWTEST_F(EventParserTest, HandlerBAbnormal, TestSize.Level1)
{
    TS_LOGI("test4-61");
    size_t length{3};
    TracePoint outPoint;
    std::string str("B|924|");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.HandlerB(str, outPoint, length);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, HandlerCsf, TestSize.Level1)
{
    TS_LOGI("test4-62");
    size_t length{4};
    TracePoint outPoint;
    std::string str("C|2519|Heap size (KB)|2363");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.HandlerCSF(str, outPoint, length);

    EXPECT_TRUE(result == SUCCESS);
}

HWTEST_F(EventParserTest, HandlerCsfAbnormal1, TestSize.Level1)
{
    TS_LOGI("test4-63");
    size_t length{4};
    TracePoint outPoint;
    std::string str("");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.HandlerCSF(str, outPoint, length);

    EXPECT_TRUE(result == ERROR);
}

HWTEST_F(EventParserTest, HandlerCsfAbnormal2, TestSize.Level1)
{
    TS_LOGI("test4-64");
    size_t length{4};
    TracePoint outPoint;
    std::string str("C|2519|Heap size (KB)|");
    BytraceEventParser eventParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    int result = eventParser.HandlerCSF(str, outPoint, length);

    EXPECT_TRUE(result == ERROR);
}
} // namespace TraceStreamer
} // namespace SysTuning

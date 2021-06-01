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

#include <fcntl.h>

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "bytrace_parser.h"
#include "event_parser.h"

namespace SysTuning {
namespace trace_analyzer {
// TestSuite:
class EventParserTest : public ::testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
    void SetUp() override {}

    void TearDown() override {}
};

HWTEST_F(EventParserTest, ParseLine)
{
    BytraceLine bytraceLine;
    bytraceLine.ts = 1616439852302;
    bytraceLine.pid = 1;
    bytraceLine.cpu = 0;
    bytraceLine.task = "ACCS0-2716";
    bytraceLine.pidStr = "12";
    bytraceLine.tGidStr = "12";
    bytraceLine.tGidStr = "softirq_raise";
    bytraceLine.argsStr = "vec=9 [action=RCU]";

    TraceAnalyzerContext ctx;
    EventParser eventParser(ctx);

    int result = eventParser.ParseLine(bytraceLine);

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, ParseLine_error)
{
    BytraceLine bytraceLine;
    TraceAnalyzerContext ctx;
    EventParser eventParser(ctx);

    int result = eventParser.ParseLine(bytraceLine);

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, trace_event_clock_sync)
{
    std::string str =
        "atrace-12728 (12728) [003] ...1 174330.280300: tracing_mark_write: trace_event_clock_sync: "
        "parent_ts=23139.998047  ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, trace_event_clock_sync)
{
    std::string str =
        "atrace-12728 (12728) [003] ...1 174330.280300: tracing_mark_write: trace_event_clock_sync: "
        "parent_ts=23139.998047  ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_C)
{
    std::string str = "ACCS0-2716  ( 2519) [000] ...1 174330.284808: tracing_mark_write: C|2519|Heap size (KB)|2906";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, tracing_mark_write_BE)
{
    std::string str = "system-1298 ( 1298) [001] ...1 174330.287420: tracing_mark_write: B|1298|Choreographer#doFrame
        system - 1298(1298)[001]... 1 174330.287622 : tracing_mark_write : E | 1298 ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, tracing_mark_write_SF)
{
    std::string str =
        "system-1298 ( 1298) [001] ...1 174330.287478: tracing_mark_write: S|1298|animator:translateX|18888109
        system -
        1298(1298)[001]... 1 174330.287514 : tracing_mark_write : F | 1298 | animator : translateX | 18888109 ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_point)
{
    std::string str =
        "system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: G|1298|animator:translateX|18888109
        system -
        1298(1298)[001]... 1 174330.287514 : tracing_mark_write : F | 1298 | animator : translateX | 18888109 ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_length)
{
    std::string str = "system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: B|2";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_formart)
{
    std::string str = "system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: B2ggg";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_formart1)
{
    std::string str = "system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write:aaaaffff B2ggg";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, cpu_idle)
{
    std::string str = "<idle>-0     (-----) [003] d..2 174330.280761: cpu_idle: state=2 cpu_id=3  ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, irq_handler_entry)
{
    std::string str =
        "ACCS0-2716  ( 2519) [000] d.h1 174330.280362: irq_handler_entry: irq=19 name=408000.qcom,cpu-bwmon";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, irq_handler_exit)
{
    std::string str = "ACCS0-2716  ( 2519) [000] d.h1 174330.280382: irq_handler_exit: irq=19 ret=handled";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, sched_waking)
{
    std::string str =
        "ACCS0-2716  ( 2519) [000] d..5 174330.280567: sched_waking: comm=Binder:924_6 pid=1332 prio=120 "
        "target_cpu=000";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, sched_wakeup)
{
    std::string str =
        "ACCS0-2716  ( 2519) [000] d..6 174330.280575: sched_wakeup: comm=Binder:924_6 pid=1332 prio=120 "
        "target_cpu=000";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, trace_event_clock_sync)
{
    std::string str =
        " atrace-12728 (12728) [003] ...1 174330.280300: tracing_mark_write: trace_event_clock_sync: "
        "parent_ts=23139.998047  ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_C)
{
    std::string str = "  ACCS0-2716  ( 2519) [000] ...1 174330.284808: tracing_mark_write: C|2519|Heap size (KB)|2906";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, tracing_mark_write_BE)
{
    std::string str =
        "system-1298  ( 1298) [001] ...1 174330.287420: tracing_mark_write: B|1298|Choreographer#doFrame
        system - 1298(1298)[001]... 1 174330.287622 : tracing_mark_write : E | 1298 ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, tracing_mark_write_SF)
{
    std::string str =
        "system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: S|1298|animator:translateX|18888109
        system - 1298(1298)[001]... 1 174330.287514 : tracing_mark_write : F | 1298 | animator : translateX |
        18888109 ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_point)
{
    std::string str =
        "system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: G|1298|animator:translateX|18888109
        system - 1298(1298)[001]... 1 174330.287514 : tracing_mark_write : F |
        1298 | animator : translateX | 18888109 ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_length)
{
    std::string str = "   system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: B|2";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_formart)
{
    std::string str = "   system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write: B2ggg";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, tracing_mark_write_error_formart1)
{
    std::string str = "   system-1298  ( 1298) [001] ...1 174330.287478: tracing_mark_write:aaaaffff B2ggg";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(EventParserTest, binder_transaction)
{
    std::string str =
        "   ACCS0-2716  ( 2519) [000] ...1 174330.280558: binder_transaction: transaction=28562407 dest_node=4336 "
        "dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, binder_lock)
{
    std::string str = "   <...>-1332  (-----) [000] ...1 174330.289231: binder_lock: transaction=28562426";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, binder_locked)
{
    std::string str = "   <...>-1332  (-----) [000] ...1 174330.289232: binder_locked: transaction=28562426";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, binder_unlock)
{
    std::string str = "   <...>-1332  (-----) [000] ...1 174330.289233: binder_unlock: transaction=28562426";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, binder_transaction_alloc_buf)
{
    std::string str =
        "    ACCS0-2716  ( 2519) [000] ...1 174330.290071: binder_transaction_alloc_buf: transaction=28562428 "
        "data_size=80 offsets_size=0";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, sched_switch)
{
    std::string str =
        "  ACCS0-2716  ( 2519) [000] d..3 174330.289220: sched_switch: prev_comm=ACCS0 prev_pid=2716 prev_prio=120 "
        "prev_state=R+ ==> next_comm=Binder:924_6 next_pid=1332 next_prio=120";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, task_rename)
{
    std::string str =
        "<...>-2093  (-----) [001] ...2 174332.792290: task_rename: pid=12729 oldcomm=perfd newcomm=POSIX timer 249"
        "oom_score_adj=-1000";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, task_newtask)
{
    std::string str =
        "<...>-2     (-----) [003] ...1 174332.825588: task_newtask: pid=12730 comm=kthreadd clone_flags=800711"
        "oom_score_adj=0";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, workqueue_execute_start)
{
    std::string str =
        " <...>-12180 (-----) [001] ...1 174332.827595: workqueue_execute_start: work struct 0000000000000000: "
        "function pm_runtime_work";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(EventParserTest, workqueue_execute_end)
{
    std::string str =
        " <...>-12180 (-----) [001] ...1 174332.828056: workqueue_execute_end: work struct 0000000000000000";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}
} // namespace trace_analyzer
} // namespace SysTuning

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

static std::string g_str = "TRACE:
    #tracer : nop
    #
    #entries - in - buffer / entries - written : 1585855 / 1585855 #P : 4
    #
    #_-- -- -= > irqs - off
    #/ _ -- -- => need - resched
    #| / _ -- -=> hardirq / softirq
    #|| / _ -- => preempt - depth
    #|| | / delay
    #TASK - PID TGID CPU # || || TIMESTAMP FUNCTION
    #| | | | || || | |
    atrace - 12728(12728)[003]... 1 174330.280300 : tracing_mark_write : trace_event_clock_sync
    : parent_ts = 23139.998047 atrace - 12728(12728)[003]... 1 174330.280316 : tracing_mark_write
    : trace_event_clock_sync : realtime_ts = 1616439852302 ACCS0 - 2716(2519)[000] d.h1 174330.280362
    : irq_handler_entry : irq = 19 name = 408000.qcom, cpu - bwmon ACCS0 -
    2716(2519)[000] d.h1 174330.280382 : irq_handler_exit
    : irq = 19 ret = handled atrace - 12728(12728)[003] d..3 174330.280387 : sched_switch
    : prev_comm = atrace prev_pid = 12728 prev_prio = 120 prev_state =
    S == > next_comm = swapper / 3 next_pid = 0 next_prio = 120 < idle > -0(-----)[003] d..4 174330.280435
    : sched_waking : comm = rcu_preempt pid = 7 prio = 98 target_cpu = 000 ACCS0 -
    2716(2519)[000] d..1 174330.280449 : ipi_entry
    : (Rescheduling interrupts)<idle> - 0(-----)[003] d..2 174330.280450 : softirq_raise
    : vec = 9 [action = RCU]<idle> - 0(-----)[003] d..3 174330.280453 : sched_waking
    : comm = ksoftirqd / 3 pid = 29 prio = 120 target_cpu = 003 ACCS0 - 2716(2519)[000] dnh2 174330.280459
    : sched_wakeup : comm = rcu_preempt pid = 7 prio = 98 target_cpu = 000 ACCS0 -
    2716(2519)[000] dn .1 174330.280461 : ipi_exit
    : (Rescheduling interrupts)<idle> - 0(-----)[003] dn .4 174330.280468 : sched_wakeup
    : comm = ksoftirqd / 3 pid = 29 prio = 120 target_cpu = 003 ACCS0 - 2716(2519)[000] d..3 174330.280472
    : sched_switch
    : prev_comm = ACCS0 prev_pid = 2716 prev_prio = 120 prev_state = R ==
    > next_comm = rcu_preempt next_pid = 7 next_prio = 98 rcu_preempt - 7(7)[000] d..3 174330.280485
    : sched_switch
    : prev_comm = rcu_preempt prev_pid = 7 prev_prio = 98 prev_state = S ==
    > next_comm = ACCS0 next_pid = 2716 next_prio = 120 < idle > -0(-----)[003] d..3 174330.280494
    : sched_switch
    : prev_comm = swapper / 3 prev_pid = 0 prev_prio = 120 prev_state =
    R == > next_comm = ksoftirqd / 3 next_pid = 29 next_prio = 120 ksoftirqd / 3 - 29(29)[003]..s1 174330.280504
    : softirq_entry : vec = 9 [action = RCU] ksoftirqd / 3 - 29(29)[003] d.s3 174330.280510 : sched_waking
    : comm = rcu_preempt pid = 7 prio = 98 target_cpu = 000 ACCS0 - 2716(2519)[000] d..1 174330.280519 : ipi_entry
    : (Rescheduling interrupts)ksoftirqd / 3 - 29(29)[003]..s1 174330.280521 : softirq_exit
    : vec = 9 [action = RCU] ACCS0 - 2716(2519)[000] dnh2 174330.280523 : sched_wakeup
    : comm = rcu_preempt pid = 7 prio = 98 target_cpu = 000 ACCS0 - 2716(2519)[000] dn .1 174330.280524 : ipi_exit
    : (Rescheduling interrupts) ";

namespace SysTuning {
namespace trace_analyzer {
// TestSuite:
class BytraceParserTest : public ::testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
    void SetUp() override {}

    void TearDown() override {}
};

HWTEST_F(BytraceParserTest, Parse)
{
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(g_str.c_str(), g_str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(BytraceParserTest, Parse_no_data1)
{
    std::string str;
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(BytraceParserTest, Parse_no_data2)
{
    std::string str = " ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(BytraceParserTest, Parse_error_data)
{
    std::string str = "1111111111111111111111111111111111111111111111";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(BytraceParserTest, Parse_error_data1)
{
    std::string str =
        "TRACE:
        # tracer: nop
        # ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}

HWTEST_F(BytraceParserTest, Parse_error_data2)
{
    std::string str = "\nafafda ";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, false);
}
} // namespace trace_analyzer
}

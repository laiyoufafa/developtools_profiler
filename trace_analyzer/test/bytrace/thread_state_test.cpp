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

namespace SysTuning {
namespace trace_analyzer {
// TestSuite:
class ThreadStateTest : public ::testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
    void SetUp() override {}

    void TearDown() override {}
};

HWTEST_F(ThreadStateTest, ThreadState)
{
    std::string str =
        "<...>-968   (-----) [003] d..3 174332.828058: sched_switch: prev_comm=anim prev_pid=968 prev_prio=116 "
        "prev_state=S ==> next_comm=display next_pid=966 next_prio=117";
    ThreadState threadState(str);

    uint32_t result = threadState.State();

    EXPECT_EQ(result, 0x10);
}

HWTEST_F(ThreadStateTest, ThreadState1)
{
    std::string str =
        "<...>-968   (-----) [003] d..3 174332.828058: sched_switch: prev_comm=anim prev_pid=968 prev_prio=116 "
        "prev_state=S ==> next_comm=display next_pid=966 next_prio=117";
    TraceAnalyzerContext ctx;
    BytraceParser bytraceParser(ctx);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}

HWTEST_F(ThreadStateTest, ThreadState2)
{
    std::string str =
        "< ... > -968(-----)[003] d..3 174332.828058 : sched_switch
        : prev_comm = anim prev_pid = 968 prev_prio = 116 prev_state =
        S == > next_comm = display next_pid = 966 next_prio =
        117 < ... > -1823(-----)[002] dn .4 174332.828061 : sched_wakeup : comm = system pid =
        1298 prio = 110 target_cpu =
        002 < ... > -966(-----)[003]... 1 174332.828062 : tracing_mark_write : B | 924 |
        com.server.wm.RootWindowContainer$MyHandler : #1 < ... > -1823(-----)[002] dn .2 174332.828064
        : ipi_entry : (Rescheduling interrupts)<...> - 966(-----)[003] d..3 174332.828064 : sched_waking : comm =
        Binder : 924_11 pid = 3672 prio = 120 target_cpu =
        002 < ... > -12180(-----)[001] d..3 174332.828065 : sched_switch : prev_comm =
        kworker / 1 : 0 prev_pid = 12180 prev_prio = 120 prev_state =
        S == > next_comm = Binder : 924_C next_pid = 2183 next_prio =
        120 < ... > -1823(-----)[002] dnh3 174332.828067 : sched_blocked_reason
        : pid = 569 iowait = 0 caller = rpm_resume + 0x138 / 0x63c < ... > -1823(-----)[002] dnh3 174332.828069
        : sched_wakeup : comm = HwBinder : 508_1 pid = 569 prio = 112 target_cpu =
        002 < ... > -1823(-----)[002] dn .2 174332.828070 : ipi_exit
        : (Rescheduling interrupts)<...> - 1823(-----)[002] dn .2 174332.828073 : ipi_entry
        : (Single function call interrupts)<...> -
        1823(-----)[002] dnh3 174332.828074 : sched_waking : comm = kschedfreq : 2 pid = 12391 prio = 49 target_cpu =
        002 < ... > -964(-----)[000] d..1 174332.828075 : ipi_entry
        : (Rescheduling interrupts)<...> - 2183(-----)[001] d..3 174332.828080 : sched_waking : comm =
        PowerManagerSer pid = 987 prio = 116 target_cpu =
        001 < ... > -1823(-----)[002] dnh4 174332.828080 : sched_wakeup
        : comm = kschedfreq : 2 pid = 12391 prio = 49 target_cpu = 002 < ... > -964(-----)[000] d.h2 174332.828082
        : sched_blocked_reason
        : pid = 3672 iowait = 0 caller = __fdget_pos + 0x5c / 0x9c < ... > -1823(-----)[002] dn .2 174332.828082
        : ipi_exit : (Single function call interrupts)<...> - 2183(-----)[001] dn .4 174332.828086 : sched_wakeup
        : comm = PowerManagerSer pid = 987 prio = 116 target_cpu = 001 < ... > -1823(-----)[002] d..3 174332.828086
        : sched_switch : prev_comm = Binder : 924_A prev_pid = 1823 prev_prio = 110 prev_state =
        R + == > next_comm = kschedfreq : 2 next_pid = 12391 next_prio = 49 ";
    TraceAnalyzerContext context;
    BytraceParser bytraceParser(context);

    int result = bytraceParser.Parse(str.c_str(), str.size());

    EXPECT_EQ(result, true);
}
} // namespace trace_analyzer
} // namespace SysTuning

/* THIS FILE IS GENERATE BY ftrace_cpp_generator.py, PLEASE DON'T EDIT IT!
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
#include <sstream>
#include "event_formatter.h"

FTRACE_NS_BEGIN
namespace {
REGISTER_FTRACE_EVENT_FORMATTER(
    bputs,
    [](const FtraceEvent& event) -> bool { return event.has_bputs_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.bputs_format();
        std::stringstream sout;
        sout << "bputs:";
        sout << " ip=" << msg.ip();
        sout << " str=" << msg.str();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    branch,
    [](const FtraceEvent& event) -> bool { return event.has_branch_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.branch_format();
        std::stringstream sout;
        sout << "branch:";
        sout << " line=" << msg.line();
        sout << " func=" << msg.func();
        sout << " file=" << msg.file();
        sout << " correct=" << msg.correct();
        sout << " constant=" << msg.constant();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    context_switch,
    [](const FtraceEvent& event) -> bool { return event.has_context_switch_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.context_switch_format();
        std::stringstream sout;
        sout << "context_switch:";
        sout << " prev_pid=" << msg.prev_pid();
        sout << " next_pid=" << msg.next_pid();
        sout << " next_cpu=" << msg.next_cpu();
        sout << " prev_prio=" << msg.prev_prio();
        sout << " prev_state=" << msg.prev_state();
        sout << " next_prio=" << msg.next_prio();
        sout << " next_state=" << msg.next_state();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    funcgraph_entry,
    [](const FtraceEvent& event) -> bool { return event.has_funcgraph_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.funcgraph_entry_format();
        std::stringstream sout;
        sout << "funcgraph_entry:";
        sout << " func=" << msg.func();
        sout << " depth=" << msg.depth();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    funcgraph_exit,
    [](const FtraceEvent& event) -> bool { return event.has_funcgraph_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.funcgraph_exit_format();
        std::stringstream sout;
        sout << "funcgraph_exit:";
        sout << " func=" << msg.func();
        sout << " calltime=" << msg.calltime();
        sout << " rettime=" << msg.rettime();
        sout << " overrun=" << msg.overrun();
        sout << " depth=" << msg.depth();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    function,
    [](const FtraceEvent& event) -> bool { return event.has_function_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.function_format();
        std::stringstream sout;
        sout << "function:";
        sout << " ip=" << msg.ip();
        sout << " parent_ip=" << msg.parent_ip();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kernel_stack,
    [](const FtraceEvent& event) -> bool { return event.has_kernel_stack_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kernel_stack_format();
        std::stringstream sout;
        sout << "kernel_stack:";
        sout << " size=" << msg.size();
        sout << " caller=" << msg.caller();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mmiotrace_map,
    [](const FtraceEvent& event) -> bool { return event.has_mmiotrace_map_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mmiotrace_map_format();
        std::stringstream sout;
        sout << "mmiotrace_map:";
        sout << " phys=" << msg.phys();
        sout << " virt=" << msg.virt();
        sout << " len=" << msg.len();
        sout << " map_id=" << msg.map_id();
        sout << " opcode=" << msg.opcode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mmiotrace_rw,
    [](const FtraceEvent& event) -> bool { return event.has_mmiotrace_rw_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mmiotrace_rw_format();
        std::stringstream sout;
        sout << "mmiotrace_rw:";
        sout << " phys=" << msg.phys();
        sout << " value=" << msg.value();
        sout << " pc=" << msg.pc();
        sout << " map_id=" << msg.map_id();
        sout << " opcode=" << msg.opcode();
        sout << " width=" << msg.width();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    print,
    [](const FtraceEvent& event) -> bool { return event.has_print_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.print_format();
        std::stringstream sout;
        sout << "print:";
        sout << " ip=" << msg.ip();
        sout << " buf=" << msg.buf();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    user_stack,
    [](const FtraceEvent& event) -> bool { return event.has_user_stack_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.user_stack_format();
        std::stringstream sout;
        sout << "user_stack:";
        sout << " tgid=" << msg.tgid();
        sout << " caller=" << msg.caller();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    wakeup,
    [](const FtraceEvent& event) -> bool { return event.has_wakeup_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.wakeup_format();
        std::stringstream sout;
        sout << "wakeup:";
        sout << " prev_pid=" << msg.prev_pid();
        sout << " next_pid=" << msg.next_pid();
        sout << " next_cpu=" << msg.next_cpu();
        sout << " prev_prio=" << msg.prev_prio();
        sout << " prev_state=" << msg.prev_state();
        sout << " next_prio=" << msg.next_prio();
        sout << " next_state=" << msg.next_state();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

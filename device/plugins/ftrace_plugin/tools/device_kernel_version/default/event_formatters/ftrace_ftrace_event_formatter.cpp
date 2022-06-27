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
#include <cinttypes>

#include "event_formatter.h"
#include "logging.h"
#include "trace_events.h"

FTRACE_NS_BEGIN
namespace {
const int BUFFER_SIZE = 512;

REGISTER_FTRACE_EVENT_FORMATTER(
    bputs,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_bputs_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.bputs_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "bputs: %ps: %s", (void*)msg.ip(), msg.str().c_str());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    branch,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_branch_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.branch_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "branch: %u:%s:%s (%u)%s", msg.line(), msg.func().c_str(),
                           msg.file().c_str(), msg.correct(), msg.constant() ? " CONSTANT" : "");
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    context_switch,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_context_switch_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.context_switch_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "context_switch: %u:%u:%u  ==> %u:%u:%u [%03u]", msg.prev_pid(),
                           msg.prev_prio(), msg.prev_state(), msg.next_pid(), msg.next_prio(), msg.next_state(),
                           msg.next_cpu());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    funcgraph_entry,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_funcgraph_entry_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.funcgraph_entry_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "funcgraph_entry: --> %ps (%d)", (void*)msg.func(), msg.depth());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    funcgraph_exit,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_funcgraph_exit_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.funcgraph_exit_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer),
                           "funcgraph_exit: <-- %ps (%d) (start: %" PRIu64 "  end: %" PRIu64 ") over: %d",
                           (void*)msg.func(), msg.depth(), msg.calltime(), msg.rettime(), msg.depth());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    function,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_function_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.function_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "function:  %ps <-- %ps", (void*)msg.ip(), (void*)msg.parent_ip());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kernel_stack,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_kernel_stack_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.kernel_stack_format();
        char buffer[BUFFER_SIZE];
        int len =
            snprintf(buffer, sizeof(buffer), "kernel_stack: size=%d, caller=%" PRIu64 "", msg.size(), msg.caller());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mmiotrace_map,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_mmiotrace_map_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.mmiotrace_map_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "mmiotrace_map: %lx %" PRIu64 " %" PRIu64 " %d %x",
                           (unsigned long)msg.phys(), msg.virt(), msg.len(), msg.map_id(), msg.opcode());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mmiotrace_rw,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_mmiotrace_rw_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.mmiotrace_rw_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "mmiotrace_rw: %lx %" PRIu64 " %" PRIu64 " %d %x %x",
                           (unsigned long)msg.phys(), msg.value(), msg.pc(), msg.map_id(), msg.opcode(), msg.width());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    print,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_print_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.print_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "print: %s", msg.buf().c_str());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    user_stack,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_user_stack_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.user_stack_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "user_stack: tgid=%d, caller=%s", msg.tgid(), msg.caller().c_str());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    wakeup,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_wakeup_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.wakeup_format();
        char buffer[BUFFER_SIZE];
        int len =
            snprintf(buffer, sizeof(buffer), "wakeup: %u:%u:%u  ==+ %u:%u:%u [%03u]", msg.prev_pid(), msg.prev_prio(),
                     msg.prev_state(), msg.next_pid(), msg.next_prio(), msg.next_state(), msg.next_cpu());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });
} // namespace
FTRACE_NS_END

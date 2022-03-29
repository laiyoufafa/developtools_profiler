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
    irq_handler_entry,
    [](const FtraceEvent& event) -> bool { return event.has_irq_handler_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.irq_handler_entry_format();
        std::stringstream sout;
        sout << "irq_handler_entry:";
        sout << " irq=" << msg.irq();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    irq_handler_exit,
    [](const FtraceEvent& event) -> bool { return event.has_irq_handler_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.irq_handler_exit_format();
        std::stringstream sout;
        sout << "irq_handler_exit:";
        sout << " irq=" << msg.irq();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    softirq_entry,
    [](const FtraceEvent& event) -> bool { return event.has_softirq_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.softirq_entry_format();
        std::stringstream sout;
        sout << "softirq_entry:";
        sout << " vec=" << msg.vec();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    softirq_exit,
    [](const FtraceEvent& event) -> bool { return event.has_softirq_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.softirq_exit_format();
        std::stringstream sout;
        sout << "softirq_exit:";
        sout << " vec=" << msg.vec();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    softirq_raise,
    [](const FtraceEvent& event) -> bool { return event.has_softirq_raise_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.softirq_raise_format();
        std::stringstream sout;
        sout << "softirq_raise:";
        sout << " vec=" << msg.vec();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

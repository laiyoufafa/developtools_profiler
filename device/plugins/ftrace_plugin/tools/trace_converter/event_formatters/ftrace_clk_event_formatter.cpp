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
    clk_disable,
    [](const FtraceEvent& event) -> bool { return event.has_clk_disable_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_disable_format();
        std::stringstream sout;
        sout << "clk_disable:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_disable_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_disable_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_disable_complete_format();
        std::stringstream sout;
        sout << "clk_disable_complete:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_enable,
    [](const FtraceEvent& event) -> bool { return event.has_clk_enable_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_enable_format();
        std::stringstream sout;
        sout << "clk_enable:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_enable_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_enable_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_enable_complete_format();
        std::stringstream sout;
        sout << "clk_enable_complete:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_prepare,
    [](const FtraceEvent& event) -> bool { return event.has_clk_prepare_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_prepare_format();
        std::stringstream sout;
        sout << "clk_prepare:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_prepare_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_prepare_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_prepare_complete_format();
        std::stringstream sout;
        sout << "clk_prepare_complete:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_set_parent,
    [](const FtraceEvent& event) -> bool { return event.has_clk_set_parent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_set_parent_format();
        std::stringstream sout;
        sout << "clk_set_parent:";
        sout << " name=" << msg.name();
        sout << " pname=" << msg.pname();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_set_parent_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_set_parent_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_set_parent_complete_format();
        std::stringstream sout;
        sout << "clk_set_parent_complete:";
        sout << " name=" << msg.name();
        sout << " pname=" << msg.pname();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_set_phase,
    [](const FtraceEvent& event) -> bool { return event.has_clk_set_phase_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_set_phase_format();
        std::stringstream sout;
        sout << "clk_set_phase:";
        sout << " name=" << msg.name();
        sout << " phase=" << msg.phase();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_set_phase_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_set_phase_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_set_phase_complete_format();
        std::stringstream sout;
        sout << "clk_set_phase_complete:";
        sout << " name=" << msg.name();
        sout << " phase=" << msg.phase();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_set_rate,
    [](const FtraceEvent& event) -> bool { return event.has_clk_set_rate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_set_rate_format();
        std::stringstream sout;
        sout << "clk_set_rate:";
        sout << " name=" << msg.name();
        sout << " rate=" << msg.rate();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_set_rate_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_set_rate_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_set_rate_complete_format();
        std::stringstream sout;
        sout << "clk_set_rate_complete:";
        sout << " name=" << msg.name();
        sout << " rate=" << msg.rate();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_unprepare,
    [](const FtraceEvent& event) -> bool { return event.has_clk_unprepare_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_unprepare_format();
        std::stringstream sout;
        sout << "clk_unprepare:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clk_unprepare_complete,
    [](const FtraceEvent& event) -> bool { return event.has_clk_unprepare_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clk_unprepare_complete_format();
        std::stringstream sout;
        sout << "clk_unprepare_complete:";
        sout << " name=" << msg.name();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

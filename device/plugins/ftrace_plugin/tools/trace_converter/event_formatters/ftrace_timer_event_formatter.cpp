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
    hrtimer_cancel,
    [](const FtraceEvent& event) -> bool { return event.has_hrtimer_cancel_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_cancel_format();
        std::stringstream sout;
        sout << "hrtimer_cancel:";
        sout << " hrtimer=" << msg.hrtimer();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_expire_entry,
    [](const FtraceEvent& event) -> bool { return event.has_hrtimer_expire_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_expire_entry_format();
        std::stringstream sout;
        sout << "hrtimer_expire_entry:";
        sout << " hrtimer=" << msg.hrtimer();
        sout << " now=" << msg.now();
        sout << " function=" << msg.function();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_expire_exit,
    [](const FtraceEvent& event) -> bool { return event.has_hrtimer_expire_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_expire_exit_format();
        std::stringstream sout;
        sout << "hrtimer_expire_exit:";
        sout << " hrtimer=" << msg.hrtimer();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_init,
    [](const FtraceEvent& event) -> bool { return event.has_hrtimer_init_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_init_format();
        std::stringstream sout;
        sout << "hrtimer_init:";
        sout << " hrtimer=" << msg.hrtimer();
        sout << " clockid=" << msg.clockid();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_start,
    [](const FtraceEvent& event) -> bool { return event.has_hrtimer_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_start_format();
        std::stringstream sout;
        sout << "hrtimer_start:";
        sout << " hrtimer=" << msg.hrtimer();
        sout << " function=" << msg.function();
        sout << " expires=" << msg.expires();
        sout << " softexpires=" << msg.softexpires();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    itimer_expire,
    [](const FtraceEvent& event) -> bool { return event.has_itimer_expire_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.itimer_expire_format();
        std::stringstream sout;
        sout << "itimer_expire:";
        sout << " which=" << msg.which();
        sout << " pid=" << msg.pid();
        sout << " now=" << msg.now();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    itimer_state,
    [](const FtraceEvent& event) -> bool { return event.has_itimer_state_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.itimer_state_format();
        std::stringstream sout;
        sout << "itimer_state:";
        sout << " which=" << msg.which();
        sout << " expires=" << msg.expires();
        sout << " value_sec=" << msg.value_sec();
        sout << " value_usec=" << msg.value_usec();
        sout << " interval_sec=" << msg.interval_sec();
        sout << " interval_usec=" << msg.interval_usec();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_cancel,
    [](const FtraceEvent& event) -> bool { return event.has_timer_cancel_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.timer_cancel_format();
        std::stringstream sout;
        sout << "timer_cancel:";
        sout << " timer=" << msg.timer();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_expire_entry,
    [](const FtraceEvent& event) -> bool { return event.has_timer_expire_entry_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.timer_expire_entry_format();
        std::stringstream sout;
        sout << "timer_expire_entry:";
        sout << " timer=" << msg.timer();
        sout << " now=" << msg.now();
        sout << " function=" << msg.function();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_expire_exit,
    [](const FtraceEvent& event) -> bool { return event.has_timer_expire_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.timer_expire_exit_format();
        std::stringstream sout;
        sout << "timer_expire_exit:";
        sout << " timer=" << msg.timer();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_init,
    [](const FtraceEvent& event) -> bool { return event.has_timer_init_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.timer_init_format();
        std::stringstream sout;
        sout << "timer_init:";
        sout << " timer=" << msg.timer();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_start,
    [](const FtraceEvent& event) -> bool { return event.has_timer_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.timer_start_format();
        std::stringstream sout;
        sout << "timer_start:";
        sout << " timer=" << msg.timer();
        sout << " function=" << msg.function();
        sout << " expires=" << msg.expires();
        sout << " now=" << msg.now();
        sout << " flags=" << msg.flags();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

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
#include "hrtimer.h"
#include "logging.h"
#include "trace_events.h"

FTRACE_NS_BEGIN
namespace {
const int BUFFER_SIZE = 512;

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_cancel,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_hrtimer_cancel_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_cancel_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "hrtimer_cancel: hrtimer=%" PRIu64 "", msg.hrtimer());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_expire_entry,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_hrtimer_expire_entry_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_expire_entry_format();
        char buffer[BUFFER_SIZE];
        int len =
            snprintf(buffer, sizeof(buffer), "hrtimer_expire_entry: hrtimer=%" PRIu64 " function=%" PRIu64 " now=%llu",
                     msg.hrtimer(), msg.function(), (unsigned long long)msg.now());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_expire_exit,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_hrtimer_expire_exit_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_expire_exit_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "hrtimer_expire_exit: hrtimer=%" PRIu64 "", msg.hrtimer());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_init,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_hrtimer_init_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_init_format();
        char buffer[BUFFER_SIZE];
        int len =
            snprintf(buffer, sizeof(buffer), "hrtimer_init: hrtimer=%" PRIu64 " clockid=%s mode=%s", msg.hrtimer(),
                     __print_symbolic(msg.clockid(), {0, "CLOCK_REALTIME"}, {1, "CLOCK_MONOTONIC"},
                                      {7, "CLOCK_BOOTTIME"}, {11, "CLOCK_TAI"}),
                     __print_symbolic(msg.mode(), {HRTIMER_MODE_ABS, "ABS"}, {HRTIMER_MODE_REL, "REL"},
                                      {HRTIMER_MODE_ABS_PINNED, "ABS|PINNED"}, {HRTIMER_MODE_REL_PINNED, "REL|PINNED"},
                                      {HRTIMER_MODE_ABS_SOFT, "ABS|SOFT"}, {HRTIMER_MODE_REL_SOFT, "REL|SOFT"},
                                      {HRTIMER_MODE_ABS_PINNED_SOFT, "ABS|PINNED|SOFT"},
                                      {HRTIMER_MODE_REL_PINNED_SOFT, "REL|PINNED|SOFT"}));
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    hrtimer_start,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_hrtimer_start_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.hrtimer_start_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(
            buffer, sizeof(buffer),
            "hrtimer_start: hrtimer=%" PRIu64 " function=%" PRIu64 " expires=%llu softexpires=%llu mode=%s",
            msg.hrtimer(), msg.function(), (unsigned long long)msg.expires(), (unsigned long long)msg.softexpires(),
            __print_symbolic(msg.mode(), {HRTIMER_MODE_ABS, "ABS"}, {HRTIMER_MODE_REL, "REL"},
                             {HRTIMER_MODE_ABS_PINNED, "ABS|PINNED"}, {HRTIMER_MODE_REL_PINNED, "REL|PINNED"},
                             {HRTIMER_MODE_ABS_SOFT, "ABS|SOFT"}, {HRTIMER_MODE_REL_SOFT, "REL|SOFT"},
                             {HRTIMER_MODE_ABS_PINNED_SOFT, "ABS|PINNED|SOFT"},
                             {HRTIMER_MODE_REL_PINNED_SOFT, "REL|PINNED|SOFT"}));
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    itimer_expire,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_itimer_expire_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.itimer_expire_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "itimer_expire: which=%d pid=%d now=%" PRIu64 "", msg.which(),
                           (int)msg.pid(), msg.now());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    itimer_state,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_itimer_state_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.itimer_state_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer),
                           "itimer_state: which=%d expires=%" PRIu64 " it_value=%" PRIu64 "it_interval=%" PRIu64 "",
                           msg.which(), msg.expires(), msg.value_sec(), msg.interval_sec());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_cancel,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_timer_cancel_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.timer_cancel_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "timer_cancel: timer=%" PRIu64 "", msg.timer());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_expire_entry,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_timer_expire_entry_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.timer_expire_entry_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer),
                           "timer_expire_entry: timer=%" PRIu64 " function=%" PRIu64 " now=%" PRIu64 "", msg.timer(),
                           msg.function(), msg.now());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_expire_exit,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_timer_expire_exit_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.timer_expire_exit_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "timer_expire_exit: timer=%" PRIu64 "", msg.timer());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_init,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_timer_init_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.timer_init_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer), "timer_init: timer=%" PRIu64 "", msg.timer());
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    timer_start,
    [](const ForStandard::FtraceEvent& event) -> bool { return event.has_timer_start_format(); },
    [](const ForStandard::FtraceEvent& event) -> std::string {
        auto msg = event.timer_start_format();
        char buffer[BUFFER_SIZE];
        int len = snprintf(buffer, sizeof(buffer),
                           "timer_start: timer=%" PRIu64 " function=%" PRIu64 " expires=%" PRIu64 " [timeout=%" PRIu64
                           "] cpu=%u idx=%u flags=%s",
                           msg.timer(), msg.function(), msg.expires(), msg.expires() - msg.now(),
                           msg.flags() & 0x0003FFFF, msg.flags() >> 22,
                           __print_flags(msg.flags() & (0x00040000 | 0x00080000 | 0x00100000 | 0x00200000), "|",
                                         {0x00040000, "M"}, {0x00080000, "D"}, {0x00100000, "P"}, {0x00200000, "I"}));
        if (len >= BUFFER_SIZE - 1) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of print event msg had be cut off in outfile");
        }
        return std::string(buffer);
    });
} // namespace
FTRACE_NS_END

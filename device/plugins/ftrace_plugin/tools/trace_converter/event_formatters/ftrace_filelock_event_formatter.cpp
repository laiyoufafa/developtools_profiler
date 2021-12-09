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
    break_lease_block,
    [](const FtraceEvent& event) -> bool { return event.has_break_lease_block_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.break_lease_block_format();
        std::stringstream sout;
        sout << "break_lease_block:";
        sout << " fl=" << msg.fl();
        sout << " i_ino=" << msg.i_ino();
        sout << " s_dev=" << msg.s_dev();
        sout << " fl_next=" << msg.fl_next();
        sout << " fl_owner=" << msg.fl_owner();
        sout << " fl_flags=" << msg.fl_flags();
        sout << " fl_type=" << msg.fl_type();
        sout << " fl_break_time=" << msg.fl_break_time();
        sout << " fl_downgrade_time=" << msg.fl_downgrade_time();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    break_lease_noblock,
    [](const FtraceEvent& event) -> bool { return event.has_break_lease_noblock_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.break_lease_noblock_format();
        std::stringstream sout;
        sout << "break_lease_noblock:";
        sout << " fl=" << msg.fl();
        sout << " i_ino=" << msg.i_ino();
        sout << " s_dev=" << msg.s_dev();
        sout << " fl_next=" << msg.fl_next();
        sout << " fl_owner=" << msg.fl_owner();
        sout << " fl_flags=" << msg.fl_flags();
        sout << " fl_type=" << msg.fl_type();
        sout << " fl_break_time=" << msg.fl_break_time();
        sout << " fl_downgrade_time=" << msg.fl_downgrade_time();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    break_lease_unblock,
    [](const FtraceEvent& event) -> bool { return event.has_break_lease_unblock_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.break_lease_unblock_format();
        std::stringstream sout;
        sout << "break_lease_unblock:";
        sout << " fl=" << msg.fl();
        sout << " i_ino=" << msg.i_ino();
        sout << " s_dev=" << msg.s_dev();
        sout << " fl_next=" << msg.fl_next();
        sout << " fl_owner=" << msg.fl_owner();
        sout << " fl_flags=" << msg.fl_flags();
        sout << " fl_type=" << msg.fl_type();
        sout << " fl_break_time=" << msg.fl_break_time();
        sout << " fl_downgrade_time=" << msg.fl_downgrade_time();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    generic_add_lease,
    [](const FtraceEvent& event) -> bool { return event.has_generic_add_lease_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.generic_add_lease_format();
        std::stringstream sout;
        sout << "generic_add_lease:";
        sout << " i_ino=" << msg.i_ino();
        sout << " wcount=" << msg.wcount();
        sout << " dcount=" << msg.dcount();
        sout << " icount=" << msg.icount();
        sout << " s_dev=" << msg.s_dev();
        sout << " fl_owner=" << msg.fl_owner();
        sout << " fl_flags=" << msg.fl_flags();
        sout << " fl_type=" << msg.fl_type();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    generic_delete_lease,
    [](const FtraceEvent& event) -> bool { return event.has_generic_delete_lease_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.generic_delete_lease_format();
        std::stringstream sout;
        sout << "generic_delete_lease:";
        sout << " fl=" << msg.fl();
        sout << " i_ino=" << msg.i_ino();
        sout << " s_dev=" << msg.s_dev();
        sout << " fl_next=" << msg.fl_next();
        sout << " fl_owner=" << msg.fl_owner();
        sout << " fl_flags=" << msg.fl_flags();
        sout << " fl_type=" << msg.fl_type();
        sout << " fl_break_time=" << msg.fl_break_time();
        sout << " fl_downgrade_time=" << msg.fl_downgrade_time();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    time_out_leases,
    [](const FtraceEvent& event) -> bool { return event.has_time_out_leases_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.time_out_leases_format();
        std::stringstream sout;
        sout << "time_out_leases:";
        sout << " fl=" << msg.fl();
        sout << " i_ino=" << msg.i_ino();
        sout << " s_dev=" << msg.s_dev();
        sout << " fl_next=" << msg.fl_next();
        sout << " fl_owner=" << msg.fl_owner();
        sout << " fl_flags=" << msg.fl_flags();
        sout << " fl_type=" << msg.fl_type();
        sout << " fl_break_time=" << msg.fl_break_time();
        sout << " fl_downgrade_time=" << msg.fl_downgrade_time();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

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
    balance_dirty_pages,
    [](const FtraceEvent& event) -> bool { return event.has_balance_dirty_pages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.balance_dirty_pages_format();
        std::stringstream sout;
        sout << "balance_dirty_pages:";
        sout << " bdi=" << msg.bdi();
        sout << " limit=" << msg.limit();
        sout << " setpoint=" << msg.setpoint();
        sout << " dirty=" << msg.dirty();
        sout << " bdi_setpoint=" << msg.bdi_setpoint();
        sout << " bdi_dirty=" << msg.bdi_dirty();
        sout << " dirty_ratelimit=" << msg.dirty_ratelimit();
        sout << " task_ratelimit=" << msg.task_ratelimit();
        sout << " dirtied=" << msg.dirtied();
        sout << " dirtied_pause=" << msg.dirtied_pause();
        sout << " paused=" << msg.paused();
        sout << " pause=" << msg.pause();
        sout << " period=" << msg.period();
        sout << " think=" << msg.think();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    bdi_dirty_ratelimit,
    [](const FtraceEvent& event) -> bool { return event.has_bdi_dirty_ratelimit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.bdi_dirty_ratelimit_format();
        std::stringstream sout;
        sout << "bdi_dirty_ratelimit:";
        sout << " bdi=" << msg.bdi();
        sout << " write_bw=" << msg.write_bw();
        sout << " avg_write_bw=" << msg.avg_write_bw();
        sout << " dirty_rate=" << msg.dirty_rate();
        sout << " dirty_ratelimit=" << msg.dirty_ratelimit();
        sout << " task_ratelimit=" << msg.task_ratelimit();
        sout << " balanced_dirty_ratelimit=" << msg.balanced_dirty_ratelimit();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    global_dirty_state,
    [](const FtraceEvent& event) -> bool { return event.has_global_dirty_state_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.global_dirty_state_format();
        std::stringstream sout;
        sout << "global_dirty_state:";
        sout << " nr_dirty=" << msg.nr_dirty();
        sout << " nr_writeback=" << msg.nr_writeback();
        sout << " nr_unstable=" << msg.nr_unstable();
        sout << " background_thresh=" << msg.background_thresh();
        sout << " dirty_thresh=" << msg.dirty_thresh();
        sout << " dirty_limit=" << msg.dirty_limit();
        sout << " nr_dirtied=" << msg.nr_dirtied();
        sout << " nr_written=" << msg.nr_written();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    wbc_writepage,
    [](const FtraceEvent& event) -> bool { return event.has_wbc_writepage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.wbc_writepage_format();
        std::stringstream sout;
        sout << "wbc_writepage:";
        sout << " name=" << msg.name();
        sout << " nr_to_write=" << msg.nr_to_write();
        sout << " pages_skipped=" << msg.pages_skipped();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " for_background=" << msg.for_background();
        sout << " for_reclaim=" << msg.for_reclaim();
        sout << " range_cyclic=" << msg.range_cyclic();
        sout << " range_start=" << msg.range_start();
        sout << " range_end=" << msg.range_end();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_bdi_register,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_bdi_register_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_bdi_register_format();
        std::stringstream sout;
        sout << "writeback_bdi_register:";
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_congestion_wait,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_congestion_wait_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_congestion_wait_format();
        std::stringstream sout;
        sout << "writeback_congestion_wait:";
        sout << " usec_timeout=" << msg.usec_timeout();
        sout << " usec_delayed=" << msg.usec_delayed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_dirty_inode,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_dirty_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_dirty_inode_format();
        std::stringstream sout;
        sout << "writeback_dirty_inode:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_dirty_inode_enqueue,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_dirty_inode_enqueue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_dirty_inode_enqueue_format();
        std::stringstream sout;
        sout << "writeback_dirty_inode_enqueue:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " mode=" << msg.mode();
        sout << " dirtied_when=" << msg.dirtied_when();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_dirty_inode_start,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_dirty_inode_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_dirty_inode_start_format();
        std::stringstream sout;
        sout << "writeback_dirty_inode_start:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_dirty_page,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_dirty_page_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_dirty_page_format();
        std::stringstream sout;
        sout << "writeback_dirty_page:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " index=" << msg.index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_exec,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_exec_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_exec_format();
        std::stringstream sout;
        sout << "writeback_exec:";
        sout << " name=" << msg.name();
        sout << " nr_pages=" << msg.nr_pages();
        sout << " sb_dev=" << msg.sb_dev();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " range_cyclic=" << msg.range_cyclic();
        sout << " for_background=" << msg.for_background();
        sout << " reason=" << msg.reason();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_lazytime,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_lazytime_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_lazytime_format();
        std::stringstream sout;
        sout << "writeback_lazytime:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " mode=" << msg.mode();
        sout << " dirtied_when=" << msg.dirtied_when();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_lazytime_iput,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_lazytime_iput_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_lazytime_iput_format();
        std::stringstream sout;
        sout << "writeback_lazytime_iput:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " mode=" << msg.mode();
        sout << " dirtied_when=" << msg.dirtied_when();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_mark_inode_dirty,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_mark_inode_dirty_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_mark_inode_dirty_format();
        std::stringstream sout;
        sout << "writeback_mark_inode_dirty:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_pages_written,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_pages_written_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_pages_written_format();
        std::stringstream sout;
        sout << "writeback_pages_written:";
        sout << " pages=" << msg.pages();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_queue,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_queue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_queue_format();
        std::stringstream sout;
        sout << "writeback_queue:";
        sout << " name=" << msg.name();
        sout << " nr_pages=" << msg.nr_pages();
        sout << " sb_dev=" << msg.sb_dev();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " range_cyclic=" << msg.range_cyclic();
        sout << " for_background=" << msg.for_background();
        sout << " reason=" << msg.reason();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_queue_io,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_queue_io_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_queue_io_format();
        std::stringstream sout;
        sout << "writeback_queue_io:";
        sout << " name=" << msg.name();
        sout << " older=" << msg.older();
        sout << " age=" << msg.age();
        sout << " moved=" << msg.moved();
        sout << " reason=" << msg.reason();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_sb_inodes_requeue,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_sb_inodes_requeue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_sb_inodes_requeue_format();
        std::stringstream sout;
        sout << "writeback_sb_inodes_requeue:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " dirtied_when=" << msg.dirtied_when();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_single_inode,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_single_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_single_inode_format();
        std::stringstream sout;
        sout << "writeback_single_inode:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " dirtied_when=" << msg.dirtied_when();
        sout << " writeback_index=" << msg.writeback_index();
        sout << " nr_to_write=" << msg.nr_to_write();
        sout << " wrote=" << msg.wrote();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_single_inode_start,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_single_inode_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_single_inode_start_format();
        std::stringstream sout;
        sout << "writeback_single_inode_start:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " state=" << msg.state();
        sout << " dirtied_when=" << msg.dirtied_when();
        sout << " writeback_index=" << msg.writeback_index();
        sout << " nr_to_write=" << msg.nr_to_write();
        sout << " wrote=" << msg.wrote();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_start,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_start_format();
        std::stringstream sout;
        sout << "writeback_start:";
        sout << " name=" << msg.name();
        sout << " nr_pages=" << msg.nr_pages();
        sout << " sb_dev=" << msg.sb_dev();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " range_cyclic=" << msg.range_cyclic();
        sout << " for_background=" << msg.for_background();
        sout << " reason=" << msg.reason();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_wait,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_wait_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_wait_format();
        std::stringstream sout;
        sout << "writeback_wait:";
        sout << " name=" << msg.name();
        sout << " nr_pages=" << msg.nr_pages();
        sout << " sb_dev=" << msg.sb_dev();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " range_cyclic=" << msg.range_cyclic();
        sout << " for_background=" << msg.for_background();
        sout << " reason=" << msg.reason();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_wait_iff_congested,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_wait_iff_congested_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_wait_iff_congested_format();
        std::stringstream sout;
        sout << "writeback_wait_iff_congested:";
        sout << " usec_timeout=" << msg.usec_timeout();
        sout << " usec_delayed=" << msg.usec_delayed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_wake_background,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_wake_background_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_wake_background_format();
        std::stringstream sout;
        sout << "writeback_wake_background:";
        sout << " name=" << msg.name();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_write_inode,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_write_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_write_inode_format();
        std::stringstream sout;
        sout << "writeback_write_inode:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_write_inode_start,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_write_inode_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_write_inode_start_format();
        std::stringstream sout;
        sout << "writeback_write_inode_start:";
        sout << " name=" << msg.name();
        sout << " ino=" << msg.ino();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    writeback_written,
    [](const FtraceEvent& event) -> bool { return event.has_writeback_written_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.writeback_written_format();
        std::stringstream sout;
        sout << "writeback_written:";
        sout << " name=" << msg.name();
        sout << " nr_pages=" << msg.nr_pages();
        sout << " sb_dev=" << msg.sb_dev();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " range_cyclic=" << msg.range_cyclic();
        sout << " for_background=" << msg.for_background();
        sout << " reason=" << msg.reason();
        sout << " cgroup_ino=" << msg.cgroup_ino();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

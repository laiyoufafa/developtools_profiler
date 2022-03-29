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
    block_bio_backmerge,
    [](const FtraceEvent& event) -> bool { return event.has_block_bio_backmerge_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_bio_backmerge_format();
        std::stringstream sout;
        sout << "block_bio_backmerge:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_bio_bounce,
    [](const FtraceEvent& event) -> bool { return event.has_block_bio_bounce_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_bio_bounce_format();
        std::stringstream sout;
        sout << "block_bio_bounce:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_bio_complete,
    [](const FtraceEvent& event) -> bool { return event.has_block_bio_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_bio_complete_format();
        std::stringstream sout;
        sout << "block_bio_complete:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " error=" << msg.error();
        sout << " rwbs=" << msg.rwbs();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_bio_frontmerge,
    [](const FtraceEvent& event) -> bool { return event.has_block_bio_frontmerge_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_bio_frontmerge_format();
        std::stringstream sout;
        sout << "block_bio_frontmerge:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_bio_queue,
    [](const FtraceEvent& event) -> bool { return event.has_block_bio_queue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_bio_queue_format();
        std::stringstream sout;
        sout << "block_bio_queue:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_bio_remap,
    [](const FtraceEvent& event) -> bool { return event.has_block_bio_remap_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_bio_remap_format();
        std::stringstream sout;
        sout << "block_bio_remap:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " old_dev=" << msg.old_dev();
        sout << " old_sector=" << msg.old_sector();
        sout << " rwbs=" << msg.rwbs();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_dirty_buffer,
    [](const FtraceEvent& event) -> bool { return event.has_block_dirty_buffer_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_dirty_buffer_format();
        std::stringstream sout;
        sout << "block_dirty_buffer:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " size=" << msg.size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_getrq,
    [](const FtraceEvent& event) -> bool { return event.has_block_getrq_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_getrq_format();
        std::stringstream sout;
        sout << "block_getrq:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_plug,
    [](const FtraceEvent& event) -> bool { return event.has_block_plug_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_plug_format();
        std::stringstream sout;
        sout << "block_plug:";
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_rq_complete,
    [](const FtraceEvent& event) -> bool { return event.has_block_rq_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_rq_complete_format();
        std::stringstream sout;
        sout << "block_rq_complete:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " error=" << msg.error();
        sout << " rwbs=" << msg.rwbs();
        sout << " cmd=" << msg.cmd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_rq_insert,
    [](const FtraceEvent& event) -> bool { return event.has_block_rq_insert_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_rq_insert_format();
        std::stringstream sout;
        sout << "block_rq_insert:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " bytes=" << msg.bytes();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        sout << " cmd=" << msg.cmd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_rq_issue,
    [](const FtraceEvent& event) -> bool { return event.has_block_rq_issue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_rq_issue_format();
        std::stringstream sout;
        sout << "block_rq_issue:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " bytes=" << msg.bytes();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        sout << " cmd=" << msg.cmd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_rq_remap,
    [](const FtraceEvent& event) -> bool { return event.has_block_rq_remap_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_rq_remap_format();
        std::stringstream sout;
        sout << "block_rq_remap:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " old_dev=" << msg.old_dev();
        sout << " old_sector=" << msg.old_sector();
        sout << " nr_bios=" << msg.nr_bios();
        sout << " rwbs=" << msg.rwbs();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_rq_requeue,
    [](const FtraceEvent& event) -> bool { return event.has_block_rq_requeue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_rq_requeue_format();
        std::stringstream sout;
        sout << "block_rq_requeue:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " cmd=" << msg.cmd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_sleeprq,
    [](const FtraceEvent& event) -> bool { return event.has_block_sleeprq_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_sleeprq_format();
        std::stringstream sout;
        sout << "block_sleeprq:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " nr_sector=" << msg.nr_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_split,
    [](const FtraceEvent& event) -> bool { return event.has_block_split_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_split_format();
        std::stringstream sout;
        sout << "block_split:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " new_sector=" << msg.new_sector();
        sout << " rwbs=" << msg.rwbs();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_touch_buffer,
    [](const FtraceEvent& event) -> bool { return event.has_block_touch_buffer_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_touch_buffer_format();
        std::stringstream sout;
        sout << "block_touch_buffer:";
        sout << " dev=" << msg.dev();
        sout << " sector=" << msg.sector();
        sout << " size=" << msg.size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    block_unplug,
    [](const FtraceEvent& event) -> bool { return event.has_block_unplug_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.block_unplug_format();
        std::stringstream sout;
        sout << "block_unplug:";
        sout << " nr_rq=" << msg.nr_rq();
        sout << " comm=" << msg.comm();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

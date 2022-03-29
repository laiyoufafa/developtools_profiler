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
    mm_compaction_begin,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_begin_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_begin_format();
        std::stringstream sout;
        sout << "mm_compaction_begin:";
        sout << " zone_start=" << msg.zone_start();
        sout << " migrate_pfn=" << msg.migrate_pfn();
        sout << " free_pfn=" << msg.free_pfn();
        sout << " zone_end=" << msg.zone_end();
        sout << " sync=" << msg.sync();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_defer_compaction,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_defer_compaction_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_defer_compaction_format();
        std::stringstream sout;
        sout << "mm_compaction_defer_compaction:";
        sout << " nid=" << msg.nid();
        sout << " idx=" << msg.idx();
        sout << " order=" << msg.order();
        sout << " considered=" << msg.considered();
        sout << " defer_shift=" << msg.defer_shift();
        sout << " order_failed=" << msg.order_failed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_defer_reset,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_defer_reset_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_defer_reset_format();
        std::stringstream sout;
        sout << "mm_compaction_defer_reset:";
        sout << " nid=" << msg.nid();
        sout << " idx=" << msg.idx();
        sout << " order=" << msg.order();
        sout << " considered=" << msg.considered();
        sout << " defer_shift=" << msg.defer_shift();
        sout << " order_failed=" << msg.order_failed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_deferred,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_deferred_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_deferred_format();
        std::stringstream sout;
        sout << "mm_compaction_deferred:";
        sout << " nid=" << msg.nid();
        sout << " idx=" << msg.idx();
        sout << " order=" << msg.order();
        sout << " considered=" << msg.considered();
        sout << " defer_shift=" << msg.defer_shift();
        sout << " order_failed=" << msg.order_failed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_end,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_end_format();
        std::stringstream sout;
        sout << "mm_compaction_end:";
        sout << " zone_start=" << msg.zone_start();
        sout << " migrate_pfn=" << msg.migrate_pfn();
        sout << " free_pfn=" << msg.free_pfn();
        sout << " zone_end=" << msg.zone_end();
        sout << " sync=" << msg.sync();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_finished,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_finished_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_finished_format();
        std::stringstream sout;
        sout << "mm_compaction_finished:";
        sout << " nid=" << msg.nid();
        sout << " idx=" << msg.idx();
        sout << " order=" << msg.order();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_isolate_freepages,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_isolate_freepages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_isolate_freepages_format();
        std::stringstream sout;
        sout << "mm_compaction_isolate_freepages:";
        sout << " start_pfn=" << msg.start_pfn();
        sout << " end_pfn=" << msg.end_pfn();
        sout << " nr_scanned=" << msg.nr_scanned();
        sout << " nr_taken=" << msg.nr_taken();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_isolate_migratepages,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_isolate_migratepages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_isolate_migratepages_format();
        std::stringstream sout;
        sout << "mm_compaction_isolate_migratepages:";
        sout << " start_pfn=" << msg.start_pfn();
        sout << " end_pfn=" << msg.end_pfn();
        sout << " nr_scanned=" << msg.nr_scanned();
        sout << " nr_taken=" << msg.nr_taken();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_migratepages,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_migratepages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_migratepages_format();
        std::stringstream sout;
        sout << "mm_compaction_migratepages:";
        sout << " nr_migrated=" << msg.nr_migrated();
        sout << " nr_failed=" << msg.nr_failed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_suitable,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_suitable_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_suitable_format();
        std::stringstream sout;
        sout << "mm_compaction_suitable:";
        sout << " nid=" << msg.nid();
        sout << " idx=" << msg.idx();
        sout << " order=" << msg.order();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_compaction_try_to_compact_pages,
    [](const FtraceEvent& event) -> bool { return event.has_mm_compaction_try_to_compact_pages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_compaction_try_to_compact_pages_format();
        std::stringstream sout;
        sout << "mm_compaction_try_to_compact_pages:";
        sout << " order=" << msg.order();
        sout << " gfp_mask=" << msg.gfp_mask();
        sout << " prio=" << msg.prio();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

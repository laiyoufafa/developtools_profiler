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
    mm_shrink_slab_end,
    [](const FtraceEvent& event) -> bool { return event.has_mm_shrink_slab_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_shrink_slab_end_format();
        std::stringstream sout;
        sout << "mm_shrink_slab_end:";
        sout << " shr=" << msg.shr();
        sout << " nid=" << msg.nid();
        sout << " shrink=" << msg.shrink();
        sout << " unused_scan=" << msg.unused_scan();
        sout << " new_scan=" << msg.new_scan();
        sout << " retval=" << msg.retval();
        sout << " total_scan=" << msg.total_scan();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_shrink_slab_start,
    [](const FtraceEvent& event) -> bool { return event.has_mm_shrink_slab_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_shrink_slab_start_format();
        std::stringstream sout;
        sout << "mm_shrink_slab_start:";
        sout << " shr=" << msg.shr();
        sout << " shrink=" << msg.shrink();
        sout << " nid=" << msg.nid();
        sout << " nr_objects_to_shrink=" << msg.nr_objects_to_shrink();
        sout << " gfp_flags=" << msg.gfp_flags();
        sout << " cache_items=" << msg.cache_items();
        sout << " delta=" << msg.delta();
        sout << " total_scan=" << msg.total_scan();
        sout << " priority=" << msg.priority();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_direct_reclaim_begin,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_direct_reclaim_begin_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_direct_reclaim_begin_format();
        std::stringstream sout;
        sout << "mm_vmscan_direct_reclaim_begin:";
        sout << " order=" << msg.order();
        sout << " may_writepage=" << msg.may_writepage();
        sout << " gfp_flags=" << msg.gfp_flags();
        sout << " classzone_idx=" << msg.classzone_idx();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_direct_reclaim_end,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_direct_reclaim_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_direct_reclaim_end_format();
        std::stringstream sout;
        sout << "mm_vmscan_direct_reclaim_end:";
        sout << " nr_reclaimed=" << msg.nr_reclaimed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_kswapd_sleep,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_kswapd_sleep_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_kswapd_sleep_format();
        std::stringstream sout;
        sout << "mm_vmscan_kswapd_sleep:";
        sout << " nid=" << msg.nid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_kswapd_wake,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_kswapd_wake_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_kswapd_wake_format();
        std::stringstream sout;
        sout << "mm_vmscan_kswapd_wake:";
        sout << " nid=" << msg.nid();
        sout << " zid=" << msg.zid();
        sout << " order=" << msg.order();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_lru_isolate,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_lru_isolate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_lru_isolate_format();
        std::stringstream sout;
        sout << "mm_vmscan_lru_isolate:";
        sout << " classzone_idx=" << msg.classzone_idx();
        sout << " order=" << msg.order();
        sout << " nr_requested=" << msg.nr_requested();
        sout << " nr_scanned=" << msg.nr_scanned();
        sout << " nr_skipped=" << msg.nr_skipped();
        sout << " nr_taken=" << msg.nr_taken();
        sout << " isolate_mode=" << msg.isolate_mode();
        sout << " lru=" << msg.lru();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_lru_shrink_inactive,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_lru_shrink_inactive_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_lru_shrink_inactive_format();
        std::stringstream sout;
        sout << "mm_vmscan_lru_shrink_inactive:";
        sout << " nid=" << msg.nid();
        sout << " nr_scanned=" << msg.nr_scanned();
        sout << " nr_reclaimed=" << msg.nr_reclaimed();
        sout << " nr_dirty=" << msg.nr_dirty();
        sout << " nr_writeback=" << msg.nr_writeback();
        sout << " nr_congested=" << msg.nr_congested();
        sout << " nr_immediate=" << msg.nr_immediate();
        sout << " nr_activate=" << msg.nr_activate();
        sout << " nr_ref_keep=" << msg.nr_ref_keep();
        sout << " nr_unmap_fail=" << msg.nr_unmap_fail();
        sout << " priority=" << msg.priority();
        sout << " reclaim_flags=" << msg.reclaim_flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_wakeup_kswapd,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_wakeup_kswapd_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_wakeup_kswapd_format();
        std::stringstream sout;
        sout << "mm_vmscan_wakeup_kswapd:";
        sout << " nid=" << msg.nid();
        sout << " zid=" << msg.zid();
        sout << " order=" << msg.order();
        sout << " gfp_flags=" << msg.gfp_flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_vmscan_writepage,
    [](const FtraceEvent& event) -> bool { return event.has_mm_vmscan_writepage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_vmscan_writepage_format();
        std::stringstream sout;
        sout << "mm_vmscan_writepage:";
        sout << " pfn=" << msg.pfn();
        sout << " reclaim_flags=" << msg.reclaim_flags();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

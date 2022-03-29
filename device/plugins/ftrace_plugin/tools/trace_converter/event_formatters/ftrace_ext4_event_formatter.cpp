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
    ext4_alloc_da_blocks,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_alloc_da_blocks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_alloc_da_blocks_format();
        std::stringstream sout;
        sout << "ext4_alloc_da_blocks:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " data_blocks=" << msg.data_blocks();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_allocate_blocks,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_allocate_blocks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_allocate_blocks_format();
        std::stringstream sout;
        sout << "ext4_allocate_blocks:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " block=" << msg.block();
        sout << " len=" << msg.len();
        sout << " logical=" << msg.logical();
        sout << " lleft=" << msg.lleft();
        sout << " lright=" << msg.lright();
        sout << " goal=" << msg.goal();
        sout << " pleft=" << msg.pleft();
        sout << " pright=" << msg.pright();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_allocate_inode,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_allocate_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_allocate_inode_format();
        std::stringstream sout;
        sout << "ext4_allocate_inode:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " dir=" << msg.dir();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_begin_ordered_truncate,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_begin_ordered_truncate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_begin_ordered_truncate_format();
        std::stringstream sout;
        sout << "ext4_begin_ordered_truncate:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " new_size=" << msg.new_size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_collapse_range,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_collapse_range_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_collapse_range_format();
        std::stringstream sout;
        sout << "ext4_collapse_range:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " offset=" << msg.offset();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_release_space,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_release_space_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_release_space_format();
        std::stringstream sout;
        sout << "ext4_da_release_space:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " i_blocks=" << msg.i_blocks();
        sout << " freed_blocks=" << msg.freed_blocks();
        sout << " reserved_data_blocks=" << msg.reserved_data_blocks();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_reserve_space,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_reserve_space_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_reserve_space_format();
        std::stringstream sout;
        sout << "ext4_da_reserve_space:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " i_blocks=" << msg.i_blocks();
        sout << " reserved_data_blocks=" << msg.reserved_data_blocks();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_update_reserve_space,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_update_reserve_space_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_update_reserve_space_format();
        std::stringstream sout;
        sout << "ext4_da_update_reserve_space:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " i_blocks=" << msg.i_blocks();
        sout << " used_blocks=" << msg.used_blocks();
        sout << " reserved_data_blocks=" << msg.reserved_data_blocks();
        sout << " quota_claim=" << msg.quota_claim();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_write_begin,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_write_begin_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_write_begin_format();
        std::stringstream sout;
        sout << "ext4_da_write_begin:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_write_end,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_write_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_write_end_format();
        std::stringstream sout;
        sout << "ext4_da_write_end:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " copied=" << msg.copied();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_write_pages,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_write_pages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_write_pages_format();
        std::stringstream sout;
        sout << "ext4_da_write_pages:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " first_page=" << msg.first_page();
        sout << " nr_to_write=" << msg.nr_to_write();
        sout << " sync_mode=" << msg.sync_mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_da_write_pages_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_da_write_pages_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_da_write_pages_extent_format();
        std::stringstream sout;
        sout << "ext4_da_write_pages_extent:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_direct_IO_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_direct_io_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_direct_io_enter_format();
        std::stringstream sout;
        sout << "ext4_direct_IO_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " rw=" << msg.rw();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_direct_IO_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_direct_io_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_direct_io_exit_format();
        std::stringstream sout;
        sout << "ext4_direct_IO_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " rw=" << msg.rw();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_discard_blocks,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_discard_blocks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_discard_blocks_format();
        std::stringstream sout;
        sout << "ext4_discard_blocks:";
        sout << " dev=" << msg.dev();
        sout << " blk=" << msg.blk();
        sout << " count=" << msg.count();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_discard_preallocations,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_discard_preallocations_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_discard_preallocations_format();
        std::stringstream sout;
        sout << "ext4_discard_preallocations:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_drop_inode,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_drop_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_drop_inode_format();
        std::stringstream sout;
        sout << "ext4_drop_inode:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " drop=" << msg.drop();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_cache_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_cache_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_cache_extent_format();
        std::stringstream sout;
        sout << "ext4_es_cache_extent:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " pblk=" << msg.pblk();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_find_delayed_extent_range_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_find_delayed_extent_range_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_find_delayed_extent_range_enter_format();
        std::stringstream sout;
        sout << "ext4_es_find_delayed_extent_range_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_find_delayed_extent_range_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_find_delayed_extent_range_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_find_delayed_extent_range_exit_format();
        std::stringstream sout;
        sout << "ext4_es_find_delayed_extent_range_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " pblk=" << msg.pblk();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_insert_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_insert_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_insert_extent_format();
        std::stringstream sout;
        sout << "ext4_es_insert_extent:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " pblk=" << msg.pblk();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_lookup_extent_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_lookup_extent_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_lookup_extent_enter_format();
        std::stringstream sout;
        sout << "ext4_es_lookup_extent_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_lookup_extent_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_lookup_extent_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_lookup_extent_exit_format();
        std::stringstream sout;
        sout << "ext4_es_lookup_extent_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " pblk=" << msg.pblk();
        sout << " status=" << msg.status();
        sout << " found=" << msg.found();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_remove_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_remove_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_remove_extent_format();
        std::stringstream sout;
        sout << "ext4_es_remove_extent:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_shrink,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_shrink_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_shrink_format();
        std::stringstream sout;
        sout << "ext4_es_shrink:";
        sout << " dev=" << msg.dev();
        sout << " nr_shrunk=" << msg.nr_shrunk();
        sout << " scan_time=" << msg.scan_time();
        sout << " nr_skipped=" << msg.nr_skipped();
        sout << " retried=" << msg.retried();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_shrink_count,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_shrink_count_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_shrink_count_format();
        std::stringstream sout;
        sout << "ext4_es_shrink_count:";
        sout << " dev=" << msg.dev();
        sout << " nr_to_scan=" << msg.nr_to_scan();
        sout << " cache_cnt=" << msg.cache_cnt();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_shrink_scan_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_shrink_scan_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_shrink_scan_enter_format();
        std::stringstream sout;
        sout << "ext4_es_shrink_scan_enter:";
        sout << " dev=" << msg.dev();
        sout << " nr_to_scan=" << msg.nr_to_scan();
        sout << " cache_cnt=" << msg.cache_cnt();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_es_shrink_scan_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_es_shrink_scan_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_es_shrink_scan_exit_format();
        std::stringstream sout;
        sout << "ext4_es_shrink_scan_exit:";
        sout << " dev=" << msg.dev();
        sout << " nr_shrunk=" << msg.nr_shrunk();
        sout << " cache_cnt=" << msg.cache_cnt();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_evict_inode,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_evict_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_evict_inode_format();
        std::stringstream sout;
        sout << "ext4_evict_inode:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " nlink=" << msg.nlink();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_convert_to_initialized_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_convert_to_initialized_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_convert_to_initialized_enter_format();
        std::stringstream sout;
        sout << "ext4_ext_convert_to_initialized_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " m_lblk=" << msg.m_lblk();
        sout << " m_len=" << msg.m_len();
        sout << " u_lblk=" << msg.u_lblk();
        sout << " u_len=" << msg.u_len();
        sout << " u_pblk=" << msg.u_pblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_convert_to_initialized_fastpath,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_convert_to_initialized_fastpath_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_convert_to_initialized_fastpath_format();
        std::stringstream sout;
        sout << "ext4_ext_convert_to_initialized_fastpath:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " m_lblk=" << msg.m_lblk();
        sout << " m_len=" << msg.m_len();
        sout << " u_lblk=" << msg.u_lblk();
        sout << " u_len=" << msg.u_len();
        sout << " u_pblk=" << msg.u_pblk();
        sout << " i_lblk=" << msg.i_lblk();
        sout << " i_len=" << msg.i_len();
        sout << " i_pblk=" << msg.i_pblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_handle_unwritten_extents,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_handle_unwritten_extents_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_handle_unwritten_extents_format();
        std::stringstream sout;
        sout << "ext4_ext_handle_unwritten_extents:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " flags=" << msg.flags();
        sout << " lblk=" << msg.lblk();
        sout << " pblk=" << msg.pblk();
        sout << " len=" << msg.len();
        sout << " allocated=" << msg.allocated();
        sout << " newblk=" << msg.newblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_in_cache,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_in_cache_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_in_cache_format();
        std::stringstream sout;
        sout << "ext4_ext_in_cache:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_load_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_load_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_load_extent_format();
        std::stringstream sout;
        sout << "ext4_ext_load_extent:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pblk=" << msg.pblk();
        sout << " lblk=" << msg.lblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_map_blocks_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_map_blocks_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_map_blocks_enter_format();
        std::stringstream sout;
        sout << "ext4_ext_map_blocks_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_map_blocks_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_map_blocks_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_map_blocks_exit_format();
        std::stringstream sout;
        sout << "ext4_ext_map_blocks_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " flags=" << msg.flags();
        sout << " pblk=" << msg.pblk();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " mflags=" << msg.mflags();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_put_in_cache,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_put_in_cache_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_put_in_cache_format();
        std::stringstream sout;
        sout << "ext4_ext_put_in_cache:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " start=" << msg.start();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_remove_space,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_remove_space_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_remove_space_format();
        std::stringstream sout;
        sout << "ext4_ext_remove_space:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " start=" << msg.start();
        sout << " end=" << msg.end();
        sout << " depth=" << msg.depth();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_remove_space_done,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_remove_space_done_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_remove_space_done_format();
        std::stringstream sout;
        sout << "ext4_ext_remove_space_done:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " start=" << msg.start();
        sout << " end=" << msg.end();
        sout << " depth=" << msg.depth();
        sout << " partial=" << msg.partial();
        sout << " eh_entries=" << msg.eh_entries();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_rm_idx,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_rm_idx_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_rm_idx_format();
        std::stringstream sout;
        sout << "ext4_ext_rm_idx:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pblk=" << msg.pblk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_rm_leaf,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_rm_leaf_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_rm_leaf_format();
        std::stringstream sout;
        sout << "ext4_ext_rm_leaf:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " partial=" << msg.partial();
        sout << " start=" << msg.start();
        sout << " ee_lblk=" << msg.ee_lblk();
        sout << " ee_pblk=" << msg.ee_pblk();
        sout << " ee_len=" << msg.ee_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ext_show_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ext_show_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ext_show_extent_format();
        std::stringstream sout;
        sout << "ext4_ext_show_extent:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pblk=" << msg.pblk();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_fallocate_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_fallocate_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_fallocate_enter_format();
        std::stringstream sout;
        sout << "ext4_fallocate_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " offset=" << msg.offset();
        sout << " len=" << msg.len();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_fallocate_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_fallocate_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_fallocate_exit_format();
        std::stringstream sout;
        sout << "ext4_fallocate_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " blocks=" << msg.blocks();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_find_delalloc_range,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_find_delalloc_range_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_find_delalloc_range_format();
        std::stringstream sout;
        sout << "ext4_find_delalloc_range:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " from=" << msg.from();
        sout << " to=" << msg.to();
        sout << " reverse=" << msg.reverse();
        sout << " found=" << msg.found();
        sout << " found_blk=" << msg.found_blk();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_forget,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_forget_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_forget_format();
        std::stringstream sout;
        sout << "ext4_forget:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " block=" << msg.block();
        sout << " is_metadata=" << msg.is_metadata();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_free_blocks,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_free_blocks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_free_blocks_format();
        std::stringstream sout;
        sout << "ext4_free_blocks:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " block=" << msg.block();
        sout << " count=" << msg.count();
        sout << " flags=" << msg.flags();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_free_inode,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_free_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_free_inode_format();
        std::stringstream sout;
        sout << "ext4_free_inode:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " uid=" << msg.uid();
        sout << " gid=" << msg.gid();
        sout << " blocks=" << msg.blocks();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_get_implied_cluster_alloc_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_get_implied_cluster_alloc_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_get_implied_cluster_alloc_exit_format();
        std::stringstream sout;
        sout << "ext4_get_implied_cluster_alloc_exit:";
        sout << " dev=" << msg.dev();
        sout << " flags=" << msg.flags();
        sout << " lblk=" << msg.lblk();
        sout << " pblk=" << msg.pblk();
        sout << " len=" << msg.len();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_get_reserved_cluster_alloc,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_get_reserved_cluster_alloc_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_get_reserved_cluster_alloc_format();
        std::stringstream sout;
        sout << "ext4_get_reserved_cluster_alloc:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ind_map_blocks_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ind_map_blocks_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ind_map_blocks_enter_format();
        std::stringstream sout;
        sout << "ext4_ind_map_blocks_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_ind_map_blocks_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_ind_map_blocks_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_ind_map_blocks_exit_format();
        std::stringstream sout;
        sout << "ext4_ind_map_blocks_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " flags=" << msg.flags();
        sout << " pblk=" << msg.pblk();
        sout << " lblk=" << msg.lblk();
        sout << " len=" << msg.len();
        sout << " mflags=" << msg.mflags();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_insert_range,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_insert_range_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_insert_range_format();
        std::stringstream sout;
        sout << "ext4_insert_range:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " offset=" << msg.offset();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_invalidatepage,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_invalidatepage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_invalidatepage_format();
        std::stringstream sout;
        sout << "ext4_invalidatepage:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " index=" << msg.index();
        sout << " offset=" << msg.offset();
        sout << " length=" << msg.length();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_journal_start,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_journal_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_journal_start_format();
        std::stringstream sout;
        sout << "ext4_journal_start:";
        sout << " dev=" << msg.dev();
        sout << " ip=" << msg.ip();
        sout << " blocks=" << msg.blocks();
        sout << " rsv_blocks=" << msg.rsv_blocks();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_journal_start_reserved,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_journal_start_reserved_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_journal_start_reserved_format();
        std::stringstream sout;
        sout << "ext4_journal_start_reserved:";
        sout << " dev=" << msg.dev();
        sout << " ip=" << msg.ip();
        sout << " blocks=" << msg.blocks();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_journalled_invalidatepage,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_journalled_invalidatepage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_journalled_invalidatepage_format();
        std::stringstream sout;
        sout << "ext4_journalled_invalidatepage:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " index=" << msg.index();
        sout << " offset=" << msg.offset();
        sout << " length=" << msg.length();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_journalled_write_end,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_journalled_write_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_journalled_write_end_format();
        std::stringstream sout;
        sout << "ext4_journalled_write_end:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " copied=" << msg.copied();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_load_inode,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_load_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_load_inode_format();
        std::stringstream sout;
        sout << "ext4_load_inode:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_load_inode_bitmap,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_load_inode_bitmap_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_load_inode_bitmap_format();
        std::stringstream sout;
        sout << "ext4_load_inode_bitmap:";
        sout << " dev=" << msg.dev();
        sout << " group=" << msg.group();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mark_inode_dirty,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mark_inode_dirty_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mark_inode_dirty_format();
        std::stringstream sout;
        sout << "ext4_mark_inode_dirty:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " ip=" << msg.ip();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_bitmap_load,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_bitmap_load_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_bitmap_load_format();
        std::stringstream sout;
        sout << "ext4_mb_bitmap_load:";
        sout << " dev=" << msg.dev();
        sout << " group=" << msg.group();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_buddy_bitmap_load,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_buddy_bitmap_load_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_buddy_bitmap_load_format();
        std::stringstream sout;
        sout << "ext4_mb_buddy_bitmap_load:";
        sout << " dev=" << msg.dev();
        sout << " group=" << msg.group();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_discard_preallocations,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_discard_preallocations_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_discard_preallocations_format();
        std::stringstream sout;
        sout << "ext4_mb_discard_preallocations:";
        sout << " dev=" << msg.dev();
        sout << " needed=" << msg.needed();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_new_group_pa,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_new_group_pa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_new_group_pa_format();
        std::stringstream sout;
        sout << "ext4_mb_new_group_pa:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pa_pstart=" << msg.pa_pstart();
        sout << " pa_lstart=" << msg.pa_lstart();
        sout << " pa_len=" << msg.pa_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_new_inode_pa,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_new_inode_pa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_new_inode_pa_format();
        std::stringstream sout;
        sout << "ext4_mb_new_inode_pa:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pa_pstart=" << msg.pa_pstart();
        sout << " pa_lstart=" << msg.pa_lstart();
        sout << " pa_len=" << msg.pa_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_release_group_pa,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_release_group_pa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_release_group_pa_format();
        std::stringstream sout;
        sout << "ext4_mb_release_group_pa:";
        sout << " dev=" << msg.dev();
        sout << " pa_pstart=" << msg.pa_pstart();
        sout << " pa_len=" << msg.pa_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mb_release_inode_pa,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mb_release_inode_pa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mb_release_inode_pa_format();
        std::stringstream sout;
        sout << "ext4_mb_release_inode_pa:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " block=" << msg.block();
        sout << " count=" << msg.count();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mballoc_alloc,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mballoc_alloc_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mballoc_alloc_format();
        std::stringstream sout;
        sout << "ext4_mballoc_alloc:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " orig_logical=" << msg.orig_logical();
        sout << " orig_start=" << msg.orig_start();
        sout << " orig_group=" << msg.orig_group();
        sout << " orig_len=" << msg.orig_len();
        sout << " goal_logical=" << msg.goal_logical();
        sout << " goal_start=" << msg.goal_start();
        sout << " goal_group=" << msg.goal_group();
        sout << " goal_len=" << msg.goal_len();
        sout << " result_logical=" << msg.result_logical();
        sout << " result_start=" << msg.result_start();
        sout << " result_group=" << msg.result_group();
        sout << " result_len=" << msg.result_len();
        sout << " found=" << msg.found();
        sout << " groups=" << msg.groups();
        sout << " buddy=" << msg.buddy();
        sout << " flags=" << msg.flags();
        sout << " tail=" << msg.tail();
        sout << " cr=" << msg.cr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mballoc_discard,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mballoc_discard_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mballoc_discard_format();
        std::stringstream sout;
        sout << "ext4_mballoc_discard:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " result_start=" << msg.result_start();
        sout << " result_group=" << msg.result_group();
        sout << " result_len=" << msg.result_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mballoc_free,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mballoc_free_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mballoc_free_format();
        std::stringstream sout;
        sout << "ext4_mballoc_free:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " result_start=" << msg.result_start();
        sout << " result_group=" << msg.result_group();
        sout << " result_len=" << msg.result_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_mballoc_prealloc,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_mballoc_prealloc_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_mballoc_prealloc_format();
        std::stringstream sout;
        sout << "ext4_mballoc_prealloc:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " orig_logical=" << msg.orig_logical();
        sout << " orig_start=" << msg.orig_start();
        sout << " orig_group=" << msg.orig_group();
        sout << " orig_len=" << msg.orig_len();
        sout << " result_logical=" << msg.result_logical();
        sout << " result_start=" << msg.result_start();
        sout << " result_group=" << msg.result_group();
        sout << " result_len=" << msg.result_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_other_inode_update_time,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_other_inode_update_time_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_other_inode_update_time_format();
        std::stringstream sout;
        sout << "ext4_other_inode_update_time:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " orig_ino=" << msg.orig_ino();
        sout << " uid=" << msg.uid();
        sout << " gid=" << msg.gid();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_punch_hole,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_punch_hole_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_punch_hole_format();
        std::stringstream sout;
        sout << "ext4_punch_hole:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " offset=" << msg.offset();
        sout << " len=" << msg.len();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_read_block_bitmap_load,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_read_block_bitmap_load_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_read_block_bitmap_load_format();
        std::stringstream sout;
        sout << "ext4_read_block_bitmap_load:";
        sout << " dev=" << msg.dev();
        sout << " group=" << msg.group();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_readpage,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_readpage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_readpage_format();
        std::stringstream sout;
        sout << "ext4_readpage:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " index=" << msg.index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_releasepage,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_releasepage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_releasepage_format();
        std::stringstream sout;
        sout << "ext4_releasepage:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " index=" << msg.index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_remove_blocks,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_remove_blocks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_remove_blocks_format();
        std::stringstream sout;
        sout << "ext4_remove_blocks:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " from=" << msg.from();
        sout << " to=" << msg.to();
        sout << " partial=" << msg.partial();
        sout << " ee_pblk=" << msg.ee_pblk();
        sout << " ee_lblk=" << msg.ee_lblk();
        sout << " ee_len=" << msg.ee_len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_request_blocks,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_request_blocks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_request_blocks_format();
        std::stringstream sout;
        sout << "ext4_request_blocks:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " len=" << msg.len();
        sout << " logical=" << msg.logical();
        sout << " lleft=" << msg.lleft();
        sout << " lright=" << msg.lright();
        sout << " goal=" << msg.goal();
        sout << " pleft=" << msg.pleft();
        sout << " pright=" << msg.pright();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_request_inode,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_request_inode_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_request_inode_format();
        std::stringstream sout;
        sout << "ext4_request_inode:";
        sout << " dev=" << msg.dev();
        sout << " dir=" << msg.dir();
        sout << " mode=" << msg.mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_sync_file_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_sync_file_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_sync_file_enter_format();
        std::stringstream sout;
        sout << "ext4_sync_file_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " parent=" << msg.parent();
        sout << " datasync=" << msg.datasync();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_sync_file_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_sync_file_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_sync_file_exit_format();
        std::stringstream sout;
        sout << "ext4_sync_file_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_sync_fs,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_sync_fs_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_sync_fs_format();
        std::stringstream sout;
        sout << "ext4_sync_fs:";
        sout << " dev=" << msg.dev();
        sout << " wait=" << msg.wait();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_trim_all_free,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_trim_all_free_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_trim_all_free_format();
        std::stringstream sout;
        sout << "ext4_trim_all_free:";
        sout << " dev_major=" << msg.dev_major();
        sout << " dev_minor=" << msg.dev_minor();
        sout << " group=" << msg.group();
        sout << " start=" << msg.start();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_trim_extent,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_trim_extent_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_trim_extent_format();
        std::stringstream sout;
        sout << "ext4_trim_extent:";
        sout << " dev_major=" << msg.dev_major();
        sout << " dev_minor=" << msg.dev_minor();
        sout << " group=" << msg.group();
        sout << " start=" << msg.start();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_truncate_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_truncate_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_truncate_enter_format();
        std::stringstream sout;
        sout << "ext4_truncate_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " blocks=" << msg.blocks();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_truncate_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_truncate_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_truncate_exit_format();
        std::stringstream sout;
        sout << "ext4_truncate_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " blocks=" << msg.blocks();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_unlink_enter,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_unlink_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_unlink_enter_format();
        std::stringstream sout;
        sout << "ext4_unlink_enter:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " parent=" << msg.parent();
        sout << " size=" << msg.size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_unlink_exit,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_unlink_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_unlink_exit_format();
        std::stringstream sout;
        sout << "ext4_unlink_exit:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_write_begin,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_write_begin_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_write_begin_format();
        std::stringstream sout;
        sout << "ext4_write_begin:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_write_end,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_write_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_write_end_format();
        std::stringstream sout;
        sout << "ext4_write_end:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " pos=" << msg.pos();
        sout << " len=" << msg.len();
        sout << " copied=" << msg.copied();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_writepage,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_writepage_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_writepage_format();
        std::stringstream sout;
        sout << "ext4_writepage:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " index=" << msg.index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_writepages,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_writepages_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_writepages_format();
        std::stringstream sout;
        sout << "ext4_writepages:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " nr_to_write=" << msg.nr_to_write();
        sout << " pages_skipped=" << msg.pages_skipped();
        sout << " range_start=" << msg.range_start();
        sout << " range_end=" << msg.range_end();
        sout << " writeback_index=" << msg.writeback_index();
        sout << " sync_mode=" << msg.sync_mode();
        sout << " for_kupdate=" << msg.for_kupdate();
        sout << " range_cyclic=" << msg.range_cyclic();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_writepages_result,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_writepages_result_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_writepages_result_format();
        std::stringstream sout;
        sout << "ext4_writepages_result:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " ret=" << msg.ret();
        sout << " pages_written=" << msg.pages_written();
        sout << " pages_skipped=" << msg.pages_skipped();
        sout << " writeback_index=" << msg.writeback_index();
        sout << " sync_mode=" << msg.sync_mode();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    ext4_zero_range,
    [](const FtraceEvent& event) -> bool { return event.has_ext4_zero_range_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.ext4_zero_range_format();
        std::stringstream sout;
        sout << "ext4_zero_range:";
        sout << " dev=" << msg.dev();
        sout << " ino=" << msg.ino();
        sout << " offset=" << msg.offset();
        sout << " len=" << msg.len();
        sout << " mode=" << msg.mode();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

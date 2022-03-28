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
    binder_alloc_lru_end,
    [](const FtraceEvent& event) -> bool { return event.has_binder_alloc_lru_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_alloc_lru_end_format();
        std::stringstream sout;
        sout << "binder_alloc_lru_end:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_alloc_lru_start,
    [](const FtraceEvent& event) -> bool { return event.has_binder_alloc_lru_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_alloc_lru_start_format();
        std::stringstream sout;
        sout << "binder_alloc_lru_start:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_alloc_page_end,
    [](const FtraceEvent& event) -> bool { return event.has_binder_alloc_page_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_alloc_page_end_format();
        std::stringstream sout;
        sout << "binder_alloc_page_end:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_alloc_page_start,
    [](const FtraceEvent& event) -> bool { return event.has_binder_alloc_page_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_alloc_page_start_format();
        std::stringstream sout;
        sout << "binder_alloc_page_start:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_command,
    [](const FtraceEvent& event) -> bool { return event.has_binder_command_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_command_format();
        std::stringstream sout;
        sout << "binder_command:";
        sout << " cmd=" << msg.cmd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_free_lru_end,
    [](const FtraceEvent& event) -> bool { return event.has_binder_free_lru_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_free_lru_end_format();
        std::stringstream sout;
        sout << "binder_free_lru_end:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_free_lru_start,
    [](const FtraceEvent& event) -> bool { return event.has_binder_free_lru_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_free_lru_start_format();
        std::stringstream sout;
        sout << "binder_free_lru_start:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_ioctl,
    [](const FtraceEvent& event) -> bool { return event.has_binder_ioctl_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_ioctl_format();
        std::stringstream sout;
        sout << "binder_ioctl:";
        sout << " cmd=" << msg.cmd();
        sout << " arg=" << msg.arg();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_ioctl_done,
    [](const FtraceEvent& event) -> bool { return event.has_binder_ioctl_done_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_ioctl_done_format();
        std::stringstream sout;
        sout << "binder_ioctl_done:";
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_lock,
    [](const FtraceEvent& event) -> bool { return event.has_binder_lock_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_lock_format();
        std::stringstream sout;
        sout << "binder_lock:";
        sout << " tag=" << msg.tag();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_locked,
    [](const FtraceEvent& event) -> bool { return event.has_binder_locked_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_locked_format();
        std::stringstream sout;
        sout << "binder_locked:";
        sout << " tag=" << msg.tag();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_read_done,
    [](const FtraceEvent& event) -> bool { return event.has_binder_read_done_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_read_done_format();
        std::stringstream sout;
        sout << "binder_read_done:";
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_return,
    [](const FtraceEvent& event) -> bool { return event.has_binder_return_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_return_format();
        std::stringstream sout;
        sout << "binder_return:";
        sout << " cmd=" << msg.cmd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_format();
        std::stringstream sout;
        sout << "binder_transaction:";
        sout << " debug_id=" << msg.debug_id();
        sout << " target_node=" << msg.target_node();
        sout << " to_proc=" << msg.to_proc();
        sout << " to_thread=" << msg.to_thread();
        sout << " reply=" << msg.reply();
        sout << " code=" << msg.code();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_alloc_buf,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_alloc_buf_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_alloc_buf_format();
        std::stringstream sout;
        sout << "binder_transaction_alloc_buf:";
        sout << " debug_id=" << msg.debug_id();
        sout << " data_size=" << msg.data_size();
        sout << " offsets_size=" << msg.offsets_size();
        sout << " extra_buffers_size=" << msg.extra_buffers_size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_buffer_release,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_buffer_release_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_buffer_release_format();
        std::stringstream sout;
        sout << "binder_transaction_buffer_release:";
        sout << " debug_id=" << msg.debug_id();
        sout << " data_size=" << msg.data_size();
        sout << " offsets_size=" << msg.offsets_size();
        sout << " extra_buffers_size=" << msg.extra_buffers_size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_failed_buffer_release,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_failed_buffer_release_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_failed_buffer_release_format();
        std::stringstream sout;
        sout << "binder_transaction_failed_buffer_release:";
        sout << " debug_id=" << msg.debug_id();
        sout << " data_size=" << msg.data_size();
        sout << " offsets_size=" << msg.offsets_size();
        sout << " extra_buffers_size=" << msg.extra_buffers_size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_fd,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_fd_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_fd_format();
        std::stringstream sout;
        sout << "binder_transaction_fd:";
        sout << " debug_id=" << msg.debug_id();
        sout << " src_fd=" << msg.src_fd();
        sout << " dest_fd=" << msg.dest_fd();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_node_to_ref,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_node_to_ref_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_node_to_ref_format();
        std::stringstream sout;
        sout << "binder_transaction_node_to_ref:";
        sout << " debug_id=" << msg.debug_id();
        sout << " node_debug_id=" << msg.node_debug_id();
        sout << " node_ptr=" << msg.node_ptr();
        sout << " ref_debug_id=" << msg.ref_debug_id();
        sout << " ref_desc=" << msg.ref_desc();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_received,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_received_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_received_format();
        std::stringstream sout;
        sout << "binder_transaction_received:";
        sout << " debug_id=" << msg.debug_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_ref_to_node,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_ref_to_node_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_ref_to_node_format();
        std::stringstream sout;
        sout << "binder_transaction_ref_to_node:";
        sout << " debug_id=" << msg.debug_id();
        sout << " ref_debug_id=" << msg.ref_debug_id();
        sout << " ref_desc=" << msg.ref_desc();
        sout << " node_debug_id=" << msg.node_debug_id();
        sout << " node_ptr=" << msg.node_ptr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_transaction_ref_to_ref,
    [](const FtraceEvent& event) -> bool { return event.has_binder_transaction_ref_to_ref_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_transaction_ref_to_ref_format();
        std::stringstream sout;
        sout << "binder_transaction_ref_to_ref:";
        sout << " debug_id=" << msg.debug_id();
        sout << " node_debug_id=" << msg.node_debug_id();
        sout << " src_ref_debug_id=" << msg.src_ref_debug_id();
        sout << " src_ref_desc=" << msg.src_ref_desc();
        sout << " dest_ref_debug_id=" << msg.dest_ref_debug_id();
        sout << " dest_ref_desc=" << msg.dest_ref_desc();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_unlock,
    [](const FtraceEvent& event) -> bool { return event.has_binder_unlock_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_unlock_format();
        std::stringstream sout;
        sout << "binder_unlock:";
        sout << " tag=" << msg.tag();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_unmap_kernel_end,
    [](const FtraceEvent& event) -> bool { return event.has_binder_unmap_kernel_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_unmap_kernel_end_format();
        std::stringstream sout;
        sout << "binder_unmap_kernel_end:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_unmap_kernel_start,
    [](const FtraceEvent& event) -> bool { return event.has_binder_unmap_kernel_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_unmap_kernel_start_format();
        std::stringstream sout;
        sout << "binder_unmap_kernel_start:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_unmap_user_end,
    [](const FtraceEvent& event) -> bool { return event.has_binder_unmap_user_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_unmap_user_end_format();
        std::stringstream sout;
        sout << "binder_unmap_user_end:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_unmap_user_start,
    [](const FtraceEvent& event) -> bool { return event.has_binder_unmap_user_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_unmap_user_start_format();
        std::stringstream sout;
        sout << "binder_unmap_user_start:";
        sout << " proc=" << msg.proc();
        sout << " page_index=" << msg.page_index();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_update_page_range,
    [](const FtraceEvent& event) -> bool { return event.has_binder_update_page_range_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_update_page_range_format();
        std::stringstream sout;
        sout << "binder_update_page_range:";
        sout << " proc=" << msg.proc();
        sout << " allocate=" << msg.allocate();
        sout << " offset=" << msg.offset();
        sout << " size=" << msg.size();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_wait_for_work,
    [](const FtraceEvent& event) -> bool { return event.has_binder_wait_for_work_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_wait_for_work_format();
        std::stringstream sout;
        sout << "binder_wait_for_work:";
        sout << " proc_work=" << msg.proc_work();
        sout << " transaction_stack=" << msg.transaction_stack();
        sout << " thread_todo=" << msg.thread_todo();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    binder_write_done,
    [](const FtraceEvent& event) -> bool { return event.has_binder_write_done_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.binder_write_done_format();
        std::stringstream sout;
        sout << "binder_write_done:";
        sout << " ret=" << msg.ret();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

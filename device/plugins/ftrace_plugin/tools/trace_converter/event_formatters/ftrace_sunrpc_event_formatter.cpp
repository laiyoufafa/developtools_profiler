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
    rpc_bind_status,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_bind_status_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_bind_status_format();
        std::stringstream sout;
        sout << "rpc_bind_status:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_call_status,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_call_status_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_call_status_format();
        std::stringstream sout;
        sout << "rpc_call_status:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_connect_status,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_connect_status_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_connect_status_format();
        std::stringstream sout;
        sout << "rpc_connect_status:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " status=" << msg.status();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_socket_close,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_socket_close_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_socket_close_format();
        std::stringstream sout;
        sout << "rpc_socket_close:";
        sout << " socket_state=" << msg.socket_state();
        sout << " sock_state=" << msg.sock_state();
        sout << " ino=" << msg.ino();
        sout << " dstaddr=" << msg.dstaddr();
        sout << " dstport=" << msg.dstport();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_socket_connect,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_socket_connect_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_socket_connect_format();
        std::stringstream sout;
        sout << "rpc_socket_connect:";
        sout << " error=" << msg.error();
        sout << " socket_state=" << msg.socket_state();
        sout << " sock_state=" << msg.sock_state();
        sout << " ino=" << msg.ino();
        sout << " dstaddr=" << msg.dstaddr();
        sout << " dstport=" << msg.dstport();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_socket_error,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_socket_error_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_socket_error_format();
        std::stringstream sout;
        sout << "rpc_socket_error:";
        sout << " error=" << msg.error();
        sout << " socket_state=" << msg.socket_state();
        sout << " sock_state=" << msg.sock_state();
        sout << " ino=" << msg.ino();
        sout << " dstaddr=" << msg.dstaddr();
        sout << " dstport=" << msg.dstport();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_socket_reset_connection,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_socket_reset_connection_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_socket_reset_connection_format();
        std::stringstream sout;
        sout << "rpc_socket_reset_connection:";
        sout << " error=" << msg.error();
        sout << " socket_state=" << msg.socket_state();
        sout << " sock_state=" << msg.sock_state();
        sout << " ino=" << msg.ino();
        sout << " dstaddr=" << msg.dstaddr();
        sout << " dstport=" << msg.dstport();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_socket_shutdown,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_socket_shutdown_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_socket_shutdown_format();
        std::stringstream sout;
        sout << "rpc_socket_shutdown:";
        sout << " socket_state=" << msg.socket_state();
        sout << " sock_state=" << msg.sock_state();
        sout << " ino=" << msg.ino();
        sout << " dstaddr=" << msg.dstaddr();
        sout << " dstport=" << msg.dstport();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_socket_state_change,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_socket_state_change_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_socket_state_change_format();
        std::stringstream sout;
        sout << "rpc_socket_state_change:";
        sout << " socket_state=" << msg.socket_state();
        sout << " sock_state=" << msg.sock_state();
        sout << " ino=" << msg.ino();
        sout << " dstaddr=" << msg.dstaddr();
        sout << " dstport=" << msg.dstport();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_task_begin,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_task_begin_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_task_begin_format();
        std::stringstream sout;
        sout << "rpc_task_begin:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " action=" << msg.action();
        sout << " runstate=" << msg.runstate();
        sout << " status=" << msg.status();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_task_complete,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_task_complete_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_task_complete_format();
        std::stringstream sout;
        sout << "rpc_task_complete:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " action=" << msg.action();
        sout << " runstate=" << msg.runstate();
        sout << " status=" << msg.status();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_task_run_action,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_task_run_action_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_task_run_action_format();
        std::stringstream sout;
        sout << "rpc_task_run_action:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " action=" << msg.action();
        sout << " runstate=" << msg.runstate();
        sout << " status=" << msg.status();
        sout << " flags=" << msg.flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_task_sleep,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_task_sleep_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_task_sleep_format();
        std::stringstream sout;
        sout << "rpc_task_sleep:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " timeout=" << msg.timeout();
        sout << " runstate=" << msg.runstate();
        sout << " status=" << msg.status();
        sout << " flags=" << msg.flags();
        sout << " q_name=" << msg.q_name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    rpc_task_wakeup,
    [](const FtraceEvent& event) -> bool { return event.has_rpc_task_wakeup_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.rpc_task_wakeup_format();
        std::stringstream sout;
        sout << "rpc_task_wakeup:";
        sout << " task_id=" << msg.task_id();
        sout << " client_id=" << msg.client_id();
        sout << " timeout=" << msg.timeout();
        sout << " runstate=" << msg.runstate();
        sout << " status=" << msg.status();
        sout << " flags=" << msg.flags();
        sout << " q_name=" << msg.q_name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_handle_xprt,
    [](const FtraceEvent& event) -> bool { return event.has_svc_handle_xprt_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_handle_xprt_format();
        std::stringstream sout;
        sout << "svc_handle_xprt:";
        sout << " xprt=" << msg.xprt();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        sout << " addr=" << msg.addr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_process,
    [](const FtraceEvent& event) -> bool { return event.has_svc_process_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_process_format();
        std::stringstream sout;
        sout << "svc_process:";
        sout << " xid=" << msg.xid();
        sout << " vers=" << msg.vers();
        sout << " proc=" << msg.proc();
        sout << " service=" << msg.service();
        sout << " addr=" << msg.addr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_recv,
    [](const FtraceEvent& event) -> bool { return event.has_svc_recv_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_recv_format();
        std::stringstream sout;
        sout << "svc_recv:";
        sout << " xid=" << msg.xid();
        sout << " len=" << msg.len();
        sout << " flags=" << msg.flags();
        sout << " addr=" << msg.addr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_send,
    [](const FtraceEvent& event) -> bool { return event.has_svc_send_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_send_format();
        std::stringstream sout;
        sout << "svc_send:";
        sout << " xid=" << msg.xid();
        sout << " status=" << msg.status();
        sout << " flags=" << msg.flags();
        sout << " addr=" << msg.addr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_wake_up,
    [](const FtraceEvent& event) -> bool { return event.has_svc_wake_up_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_wake_up_format();
        std::stringstream sout;
        sout << "svc_wake_up:";
        sout << " pid=" << msg.pid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_xprt_dequeue,
    [](const FtraceEvent& event) -> bool { return event.has_svc_xprt_dequeue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_xprt_dequeue_format();
        std::stringstream sout;
        sout << "svc_xprt_dequeue:";
        sout << " xprt=" << msg.xprt();
        sout << " flags=" << msg.flags();
        sout << " wakeup=" << msg.wakeup();
        sout << " addr=" << msg.addr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    svc_xprt_do_enqueue,
    [](const FtraceEvent& event) -> bool { return event.has_svc_xprt_do_enqueue_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.svc_xprt_do_enqueue_format();
        std::stringstream sout;
        sout << "svc_xprt_do_enqueue:";
        sout << " xprt=" << msg.xprt();
        sout << " pid=" << msg.pid();
        sout << " flags=" << msg.flags();
        sout << " addr=" << msg.addr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    xprt_complete_rqst,
    [](const FtraceEvent& event) -> bool { return event.has_xprt_complete_rqst_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.xprt_complete_rqst_format();
        std::stringstream sout;
        sout << "xprt_complete_rqst:";
        sout << " xid=" << msg.xid();
        sout << " status=" << msg.status();
        sout << " addr=" << msg.addr();
        sout << " port=" << msg.port();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    xprt_lookup_rqst,
    [](const FtraceEvent& event) -> bool { return event.has_xprt_lookup_rqst_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.xprt_lookup_rqst_format();
        std::stringstream sout;
        sout << "xprt_lookup_rqst:";
        sout << " xid=" << msg.xid();
        sout << " status=" << msg.status();
        sout << " addr=" << msg.addr();
        sout << " port=" << msg.port();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    xprt_transmit,
    [](const FtraceEvent& event) -> bool { return event.has_xprt_transmit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.xprt_transmit_format();
        std::stringstream sout;
        sout << "xprt_transmit:";
        sout << " xid=" << msg.xid();
        sout << " status=" << msg.status();
        sout << " addr=" << msg.addr();
        sout << " port=" << msg.port();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    xs_tcp_data_ready,
    [](const FtraceEvent& event) -> bool { return event.has_xs_tcp_data_ready_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.xs_tcp_data_ready_format();
        std::stringstream sout;
        sout << "xs_tcp_data_ready:";
        sout << " err=" << msg.err();
        sout << " total=" << msg.total();
        sout << " addr=" << msg.addr();
        sout << " port=" << msg.port();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    xs_tcp_data_recv,
    [](const FtraceEvent& event) -> bool { return event.has_xs_tcp_data_recv_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.xs_tcp_data_recv_format();
        std::stringstream sout;
        sout << "xs_tcp_data_recv:";
        sout << " addr=" << msg.addr();
        sout << " port=" << msg.port();
        sout << " xid=" << msg.xid();
        sout << " flags=" << msg.flags();
        sout << " copied=" << msg.copied();
        sout << " reclen=" << msg.reclen();
        sout << " offset=" << msg.offset();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

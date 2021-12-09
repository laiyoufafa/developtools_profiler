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
    sched_kthread_stop,
    [](const FtraceEvent& event) -> bool { return event.has_sched_kthread_stop_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_kthread_stop_format();
        std::stringstream sout;
        sout << "sched_kthread_stop:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_kthread_stop_ret,
    [](const FtraceEvent& event) -> bool { return event.has_sched_kthread_stop_ret_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_kthread_stop_ret_format();
        std::stringstream sout;
        sout << "sched_kthread_stop_ret:";
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_migrate_task,
    [](const FtraceEvent& event) -> bool { return event.has_sched_migrate_task_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_migrate_task_format();
        std::stringstream sout;
        sout << "sched_migrate_task:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        sout << " orig_cpu=" << msg.orig_cpu();
        sout << " dest_cpu=" << msg.dest_cpu();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_move_numa,
    [](const FtraceEvent& event) -> bool { return event.has_sched_move_numa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_move_numa_format();
        std::stringstream sout;
        sout << "sched_move_numa:";
        sout << " pid=" << msg.pid();
        sout << " tgid=" << msg.tgid();
        sout << " ngid=" << msg.ngid();
        sout << " src_cpu=" << msg.src_cpu();
        sout << " src_nid=" << msg.src_nid();
        sout << " dst_cpu=" << msg.dst_cpu();
        sout << " dst_nid=" << msg.dst_nid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_pi_setprio,
    [](const FtraceEvent& event) -> bool { return event.has_sched_pi_setprio_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_pi_setprio_format();
        std::stringstream sout;
        sout << "sched_pi_setprio:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " oldprio=" << msg.oldprio();
        sout << " newprio=" << msg.newprio();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_process_exec,
    [](const FtraceEvent& event) -> bool { return event.has_sched_process_exec_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_process_exec_format();
        std::stringstream sout;
        sout << "sched_process_exec:";
        sout << " filename=" << msg.filename();
        sout << " pid=" << msg.pid();
        sout << " old_pid=" << msg.old_pid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_process_exit,
    [](const FtraceEvent& event) -> bool { return event.has_sched_process_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_process_exit_format();
        std::stringstream sout;
        sout << "sched_process_exit:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_process_fork,
    [](const FtraceEvent& event) -> bool { return event.has_sched_process_fork_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_process_fork_format();
        std::stringstream sout;
        sout << "sched_process_fork:";
        sout << " parent_comm=" << msg.parent_comm();
        sout << " parent_pid=" << msg.parent_pid();
        sout << " child_comm=" << msg.child_comm();
        sout << " child_pid=" << msg.child_pid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_process_free,
    [](const FtraceEvent& event) -> bool { return event.has_sched_process_free_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_process_free_format();
        std::stringstream sout;
        sout << "sched_process_free:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_process_wait,
    [](const FtraceEvent& event) -> bool { return event.has_sched_process_wait_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_process_wait_format();
        std::stringstream sout;
        sout << "sched_process_wait:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_stat_blocked,
    [](const FtraceEvent& event) -> bool { return event.has_sched_stat_blocked_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_stat_blocked_format();
        std::stringstream sout;
        sout << "sched_stat_blocked:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " delay=" << msg.delay();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_stat_iowait,
    [](const FtraceEvent& event) -> bool { return event.has_sched_stat_iowait_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_stat_iowait_format();
        std::stringstream sout;
        sout << "sched_stat_iowait:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " delay=" << msg.delay();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_stat_runtime,
    [](const FtraceEvent& event) -> bool { return event.has_sched_stat_runtime_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_stat_runtime_format();
        std::stringstream sout;
        sout << "sched_stat_runtime:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " runtime=" << msg.runtime();
        sout << " vruntime=" << msg.vruntime();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_stat_sleep,
    [](const FtraceEvent& event) -> bool { return event.has_sched_stat_sleep_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_stat_sleep_format();
        std::stringstream sout;
        sout << "sched_stat_sleep:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " delay=" << msg.delay();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_stat_wait,
    [](const FtraceEvent& event) -> bool { return event.has_sched_stat_wait_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_stat_wait_format();
        std::stringstream sout;
        sout << "sched_stat_wait:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " delay=" << msg.delay();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_stick_numa,
    [](const FtraceEvent& event) -> bool { return event.has_sched_stick_numa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_stick_numa_format();
        std::stringstream sout;
        sout << "sched_stick_numa:";
        sout << " pid=" << msg.pid();
        sout << " tgid=" << msg.tgid();
        sout << " ngid=" << msg.ngid();
        sout << " src_cpu=" << msg.src_cpu();
        sout << " src_nid=" << msg.src_nid();
        sout << " dst_cpu=" << msg.dst_cpu();
        sout << " dst_nid=" << msg.dst_nid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_swap_numa,
    [](const FtraceEvent& event) -> bool { return event.has_sched_swap_numa_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_swap_numa_format();
        std::stringstream sout;
        sout << "sched_swap_numa:";
        sout << " src_pid=" << msg.src_pid();
        sout << " src_tgid=" << msg.src_tgid();
        sout << " src_ngid=" << msg.src_ngid();
        sout << " src_cpu=" << msg.src_cpu();
        sout << " src_nid=" << msg.src_nid();
        sout << " dst_pid=" << msg.dst_pid();
        sout << " dst_tgid=" << msg.dst_tgid();
        sout << " dst_ngid=" << msg.dst_ngid();
        sout << " dst_cpu=" << msg.dst_cpu();
        sout << " dst_nid=" << msg.dst_nid();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_switch,
    [](const FtraceEvent& event) -> bool { return event.has_sched_switch_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_switch_format();
        std::stringstream sout;
        sout << "sched_switch:";
        sout << " prev_comm=" << msg.prev_comm();
        sout << " prev_pid=" << msg.prev_pid();
        sout << " prev_prio=" << msg.prev_prio();
        sout << " prev_state=" << msg.prev_state();
        sout << " next_comm=" << msg.next_comm();
        sout << " next_pid=" << msg.next_pid();
        sout << " next_prio=" << msg.next_prio();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_wait_task,
    [](const FtraceEvent& event) -> bool { return event.has_sched_wait_task_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_wait_task_format();
        std::stringstream sout;
        sout << "sched_wait_task:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_wake_idle_without_ipi,
    [](const FtraceEvent& event) -> bool { return event.has_sched_wake_idle_without_ipi_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_wake_idle_without_ipi_format();
        std::stringstream sout;
        sout << "sched_wake_idle_without_ipi:";
        sout << " cpu=" << msg.cpu();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_wakeup,
    [](const FtraceEvent& event) -> bool { return event.has_sched_wakeup_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_wakeup_format();
        std::stringstream sout;
        sout << "sched_wakeup:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        sout << " success=" << msg.success();
        sout << " target_cpu=" << msg.target_cpu();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_wakeup_new,
    [](const FtraceEvent& event) -> bool { return event.has_sched_wakeup_new_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_wakeup_new_format();
        std::stringstream sout;
        sout << "sched_wakeup_new:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        sout << " success=" << msg.success();
        sout << " target_cpu=" << msg.target_cpu();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    sched_waking,
    [](const FtraceEvent& event) -> bool { return event.has_sched_waking_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.sched_waking_format();
        std::stringstream sout;
        sout << "sched_waking:";
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " prio=" << msg.prio();
        sout << " success=" << msg.success();
        sout << " target_cpu=" << msg.target_cpu();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

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
    cgroup_attach_task,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_attach_task_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_attach_task_format();
        std::stringstream sout;
        sout << "cgroup_attach_task:";
        sout << " dst_root=" << msg.dst_root();
        sout << " dst_id=" << msg.dst_id();
        sout << " dst_level=" << msg.dst_level();
        sout << " pid=" << msg.pid();
        sout << " dst_path=" << msg.dst_path();
        sout << " comm=" << msg.comm();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_destroy_root,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_destroy_root_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_destroy_root_format();
        std::stringstream sout;
        sout << "cgroup_destroy_root:";
        sout << " root=" << msg.root();
        sout << " ss_mask=" << msg.ss_mask();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_mkdir,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_mkdir_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_mkdir_format();
        std::stringstream sout;
        sout << "cgroup_mkdir:";
        sout << " root=" << msg.root();
        sout << " id=" << msg.id();
        sout << " level=" << msg.level();
        sout << " path=" << msg.path();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_release,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_release_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_release_format();
        std::stringstream sout;
        sout << "cgroup_release:";
        sout << " root=" << msg.root();
        sout << " id=" << msg.id();
        sout << " level=" << msg.level();
        sout << " path=" << msg.path();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_remount,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_remount_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_remount_format();
        std::stringstream sout;
        sout << "cgroup_remount:";
        sout << " root=" << msg.root();
        sout << " ss_mask=" << msg.ss_mask();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_rename,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_rename_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_rename_format();
        std::stringstream sout;
        sout << "cgroup_rename:";
        sout << " root=" << msg.root();
        sout << " id=" << msg.id();
        sout << " level=" << msg.level();
        sout << " path=" << msg.path();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_rmdir,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_rmdir_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_rmdir_format();
        std::stringstream sout;
        sout << "cgroup_rmdir:";
        sout << " root=" << msg.root();
        sout << " id=" << msg.id();
        sout << " level=" << msg.level();
        sout << " path=" << msg.path();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_setup_root,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_setup_root_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_setup_root_format();
        std::stringstream sout;
        sout << "cgroup_setup_root:";
        sout << " root=" << msg.root();
        sout << " ss_mask=" << msg.ss_mask();
        sout << " name=" << msg.name();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cgroup_transfer_tasks,
    [](const FtraceEvent& event) -> bool { return event.has_cgroup_transfer_tasks_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cgroup_transfer_tasks_format();
        std::stringstream sout;
        sout << "cgroup_transfer_tasks:";
        sout << " dst_root=" << msg.dst_root();
        sout << " dst_id=" << msg.dst_id();
        sout << " dst_level=" << msg.dst_level();
        sout << " pid=" << msg.pid();
        sout << " dst_path=" << msg.dst_path();
        sout << " comm=" << msg.comm();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

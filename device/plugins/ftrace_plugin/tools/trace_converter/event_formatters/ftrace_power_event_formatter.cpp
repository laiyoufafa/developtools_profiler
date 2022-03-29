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
    clock_disable,
    [](const FtraceEvent& event) -> bool { return event.has_clock_disable_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clock_disable_format();
        std::stringstream sout;
        sout << "clock_disable:";
        sout << " name=" << msg.name();
        sout << " state=" << msg.state();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clock_enable,
    [](const FtraceEvent& event) -> bool { return event.has_clock_enable_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clock_enable_format();
        std::stringstream sout;
        sout << "clock_enable:";
        sout << " name=" << msg.name();
        sout << " state=" << msg.state();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    clock_set_rate,
    [](const FtraceEvent& event) -> bool { return event.has_clock_set_rate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.clock_set_rate_format();
        std::stringstream sout;
        sout << "clock_set_rate:";
        sout << " name=" << msg.name();
        sout << " state=" << msg.state();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cpu_frequency,
    [](const FtraceEvent& event) -> bool { return event.has_cpu_frequency_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cpu_frequency_format();
        std::stringstream sout;
        sout << "cpu_frequency:";
        sout << " state=" << msg.state();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cpu_frequency_limits,
    [](const FtraceEvent& event) -> bool { return event.has_cpu_frequency_limits_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cpu_frequency_limits_format();
        std::stringstream sout;
        sout << "cpu_frequency_limits:";
        sout << " min_freq=" << msg.min_freq();
        sout << " max_freq=" << msg.max_freq();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cpu_idle,
    [](const FtraceEvent& event) -> bool { return event.has_cpu_idle_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cpu_idle_format();
        std::stringstream sout;
        sout << "cpu_idle:";
        sout << " state=" << msg.state();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dev_pm_qos_add_request,
    [](const FtraceEvent& event) -> bool { return event.has_dev_pm_qos_add_request_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dev_pm_qos_add_request_format();
        std::stringstream sout;
        sout << "dev_pm_qos_add_request:";
        sout << " name=" << msg.name();
        sout << " type=" << msg.type();
        sout << " new_value=" << msg.new_value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dev_pm_qos_remove_request,
    [](const FtraceEvent& event) -> bool { return event.has_dev_pm_qos_remove_request_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dev_pm_qos_remove_request_format();
        std::stringstream sout;
        sout << "dev_pm_qos_remove_request:";
        sout << " name=" << msg.name();
        sout << " type=" << msg.type();
        sout << " new_value=" << msg.new_value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dev_pm_qos_update_request,
    [](const FtraceEvent& event) -> bool { return event.has_dev_pm_qos_update_request_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dev_pm_qos_update_request_format();
        std::stringstream sout;
        sout << "dev_pm_qos_update_request:";
        sout << " name=" << msg.name();
        sout << " type=" << msg.type();
        sout << " new_value=" << msg.new_value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    device_pm_callback_end,
    [](const FtraceEvent& event) -> bool { return event.has_device_pm_callback_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.device_pm_callback_end_format();
        std::stringstream sout;
        sout << "device_pm_callback_end:";
        sout << " device=" << msg.device();
        sout << " driver=" << msg.driver();
        sout << " error=" << msg.error();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    device_pm_callback_start,
    [](const FtraceEvent& event) -> bool { return event.has_device_pm_callback_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.device_pm_callback_start_format();
        std::stringstream sout;
        sout << "device_pm_callback_start:";
        sout << " device=" << msg.device();
        sout << " driver=" << msg.driver();
        sout << " parent=" << msg.parent();
        sout << " pm_ops=" << msg.pm_ops();
        sout << " event=" << msg.event();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pm_qos_add_request,
    [](const FtraceEvent& event) -> bool { return event.has_pm_qos_add_request_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pm_qos_add_request_format();
        std::stringstream sout;
        sout << "pm_qos_add_request:";
        sout << " pm_qos_class=" << msg.pm_qos_class();
        sout << " value=" << msg.value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pm_qos_remove_request,
    [](const FtraceEvent& event) -> bool { return event.has_pm_qos_remove_request_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pm_qos_remove_request_format();
        std::stringstream sout;
        sout << "pm_qos_remove_request:";
        sout << " pm_qos_class=" << msg.pm_qos_class();
        sout << " value=" << msg.value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pm_qos_update_flags,
    [](const FtraceEvent& event) -> bool { return event.has_pm_qos_update_flags_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pm_qos_update_flags_format();
        std::stringstream sout;
        sout << "pm_qos_update_flags:";
        sout << " action=" << msg.action();
        sout << " prev_value=" << msg.prev_value();
        sout << " curr_value=" << msg.curr_value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pm_qos_update_request,
    [](const FtraceEvent& event) -> bool { return event.has_pm_qos_update_request_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pm_qos_update_request_format();
        std::stringstream sout;
        sout << "pm_qos_update_request:";
        sout << " pm_qos_class=" << msg.pm_qos_class();
        sout << " value=" << msg.value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pm_qos_update_request_timeout,
    [](const FtraceEvent& event) -> bool { return event.has_pm_qos_update_request_timeout_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pm_qos_update_request_timeout_format();
        std::stringstream sout;
        sout << "pm_qos_update_request_timeout:";
        sout << " pm_qos_class=" << msg.pm_qos_class();
        sout << " value=" << msg.value();
        sout << " timeout_us=" << msg.timeout_us();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pm_qos_update_target,
    [](const FtraceEvent& event) -> bool { return event.has_pm_qos_update_target_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pm_qos_update_target_format();
        std::stringstream sout;
        sout << "pm_qos_update_target:";
        sout << " action=" << msg.action();
        sout << " prev_value=" << msg.prev_value();
        sout << " curr_value=" << msg.curr_value();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    power_domain_target,
    [](const FtraceEvent& event) -> bool { return event.has_power_domain_target_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.power_domain_target_format();
        std::stringstream sout;
        sout << "power_domain_target:";
        sout << " name=" << msg.name();
        sout << " state=" << msg.state();
        sout << " cpu_id=" << msg.cpu_id();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    pstate_sample,
    [](const FtraceEvent& event) -> bool { return event.has_pstate_sample_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.pstate_sample_format();
        std::stringstream sout;
        sout << "pstate_sample:";
        sout << " core_busy=" << msg.core_busy();
        sout << " scaled_busy=" << msg.scaled_busy();
        sout << " from=" << msg.from();
        sout << " to=" << msg.to();
        sout << " mperf=" << msg.mperf();
        sout << " aperf=" << msg.aperf();
        sout << " tsc=" << msg.tsc();
        sout << " freq=" << msg.freq();
        sout << " io_boost=" << msg.io_boost();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    suspend_resume,
    [](const FtraceEvent& event) -> bool { return event.has_suspend_resume_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.suspend_resume_format();
        std::stringstream sout;
        sout << "suspend_resume:";
        sout << " action=" << msg.action();
        sout << " val=" << msg.val();
        sout << " start=" << msg.start();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    wakeup_source_activate,
    [](const FtraceEvent& event) -> bool { return event.has_wakeup_source_activate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.wakeup_source_activate_format();
        std::stringstream sout;
        sout << "wakeup_source_activate:";
        sout << " name=" << msg.name();
        sout << " state=" << msg.state();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    wakeup_source_deactivate,
    [](const FtraceEvent& event) -> bool { return event.has_wakeup_source_deactivate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.wakeup_source_deactivate_format();
        std::stringstream sout;
        sout << "wakeup_source_deactivate:";
        sout << " name=" << msg.name();
        sout << " state=" << msg.state();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

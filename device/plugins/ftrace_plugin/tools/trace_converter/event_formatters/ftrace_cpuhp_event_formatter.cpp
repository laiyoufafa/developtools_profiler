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
    cpuhp_enter,
    [](const FtraceEvent& event) -> bool { return event.has_cpuhp_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cpuhp_enter_format();
        std::stringstream sout;
        sout << "cpuhp_enter:";
        sout << " cpu=" << msg.cpu();
        sout << " target=" << msg.target();
        sout << " idx=" << msg.idx();
        sout << " fun=" << msg.fun();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cpuhp_exit,
    [](const FtraceEvent& event) -> bool { return event.has_cpuhp_exit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cpuhp_exit_format();
        std::stringstream sout;
        sout << "cpuhp_exit:";
        sout << " cpu=" << msg.cpu();
        sout << " state=" << msg.state();
        sout << " idx=" << msg.idx();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    cpuhp_multi_enter,
    [](const FtraceEvent& event) -> bool { return event.has_cpuhp_multi_enter_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.cpuhp_multi_enter_format();
        std::stringstream sout;
        sout << "cpuhp_multi_enter:";
        sout << " cpu=" << msg.cpu();
        sout << " target=" << msg.target();
        sout << " idx=" << msg.idx();
        sout << " fun=" << msg.fun();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

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
    signal_deliver,
    [](const FtraceEvent& event) -> bool { return event.has_signal_deliver_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.signal_deliver_format();
        std::stringstream sout;
        sout << "signal_deliver:";
        sout << " sig=" << msg.sig();
        sout << " error_code=" << msg.error_code();
        sout << " code=" << msg.code();
        sout << " sig_handler=" << msg.sig_handler();
        sout << " sig_flags=" << msg.sig_flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    signal_generate,
    [](const FtraceEvent& event) -> bool { return event.has_signal_generate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.signal_generate_format();
        std::stringstream sout;
        sout << "signal_generate:";
        sout << " sig=" << msg.sig();
        sout << " error_code=" << msg.error_code();
        sout << " code=" << msg.code();
        sout << " comm=" << msg.comm();
        sout << " pid=" << msg.pid();
        sout << " group=" << msg.group();
        sout << " result=" << msg.result();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

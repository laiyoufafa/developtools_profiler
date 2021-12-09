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
    i2c_read,
    [](const FtraceEvent& event) -> bool { return event.has_i2c_read_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.i2c_read_format();
        std::stringstream sout;
        sout << "i2c_read:";
        sout << " adapter_nr=" << msg.adapter_nr();
        sout << " msg_nr=" << msg.msg_nr();
        sout << " addr=" << msg.addr();
        sout << " flags=" << msg.flags();
        sout << " len=" << msg.len();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    i2c_reply,
    [](const FtraceEvent& event) -> bool { return event.has_i2c_reply_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.i2c_reply_format();
        std::stringstream sout;
        sout << "i2c_reply:";
        sout << " adapter_nr=" << msg.adapter_nr();
        sout << " msg_nr=" << msg.msg_nr();
        sout << " addr=" << msg.addr();
        sout << " flags=" << msg.flags();
        sout << " len=" << msg.len();
        sout << " buf=" << msg.buf();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    i2c_result,
    [](const FtraceEvent& event) -> bool { return event.has_i2c_result_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.i2c_result_format();
        std::stringstream sout;
        sout << "i2c_result:";
        sout << " adapter_nr=" << msg.adapter_nr();
        sout << " nr_msgs=" << msg.nr_msgs();
        sout << " ret=" << msg.ret();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    i2c_write,
    [](const FtraceEvent& event) -> bool { return event.has_i2c_write_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.i2c_write_format();
        std::stringstream sout;
        sout << "i2c_write:";
        sout << " adapter_nr=" << msg.adapter_nr();
        sout << " msg_nr=" << msg.msg_nr();
        sout << " addr=" << msg.addr();
        sout << " flags=" << msg.flags();
        sout << " len=" << msg.len();
        sout << " buf=" << msg.buf();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

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
    mm_lru_activate,
    [](const FtraceEvent& event) -> bool { return event.has_mm_lru_activate_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_lru_activate_format();
        std::stringstream sout;
        sout << "mm_lru_activate:";
        sout << " page=" << msg.page();
        sout << " pfn=" << msg.pfn();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_lru_insertion,
    [](const FtraceEvent& event) -> bool { return event.has_mm_lru_insertion_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_lru_insertion_format();
        std::stringstream sout;
        sout << "mm_lru_insertion:";
        sout << " page=" << msg.page();
        sout << " pfn=" << msg.pfn();
        sout << " lru=" << msg.lru();
        sout << " flags=" << msg.flags();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

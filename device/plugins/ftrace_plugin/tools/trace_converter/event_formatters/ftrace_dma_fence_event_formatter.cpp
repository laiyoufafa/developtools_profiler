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
    dma_fence_destroy,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_destroy_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_destroy_format();
        std::stringstream sout;
        sout << "dma_fence_destroy:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dma_fence_emit,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_emit_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_emit_format();
        std::stringstream sout;
        sout << "dma_fence_emit:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dma_fence_enable_signal,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_enable_signal_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_enable_signal_format();
        std::stringstream sout;
        sout << "dma_fence_enable_signal:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dma_fence_init,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_init_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_init_format();
        std::stringstream sout;
        sout << "dma_fence_init:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dma_fence_signaled,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_signaled_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_signaled_format();
        std::stringstream sout;
        sout << "dma_fence_signaled:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dma_fence_wait_end,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_wait_end_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_wait_end_format();
        std::stringstream sout;
        sout << "dma_fence_wait_end:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    dma_fence_wait_start,
    [](const FtraceEvent& event) -> bool { return event.has_dma_fence_wait_start_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.dma_fence_wait_start_format();
        std::stringstream sout;
        sout << "dma_fence_wait_start:";
        sout << " driver=" << msg.driver();
        sout << " timeline=" << msg.timeline();
        sout << " context=" << msg.context();
        sout << " seqno=" << msg.seqno();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

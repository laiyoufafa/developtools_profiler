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
    kfree,
    [](const FtraceEvent& event) -> bool { return event.has_kfree_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kfree_format();
        std::stringstream sout;
        sout << "kfree:";
        sout << " call_site=" << msg.call_site();
        sout << " ptr=" << msg.ptr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kmalloc,
    [](const FtraceEvent& event) -> bool { return event.has_kmalloc_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kmalloc_format();
        std::stringstream sout;
        sout << "kmalloc:";
        sout << " call_site=" << msg.call_site();
        sout << " ptr=" << msg.ptr();
        sout << " bytes_req=" << msg.bytes_req();
        sout << " bytes_alloc=" << msg.bytes_alloc();
        sout << " gfp_flags=" << msg.gfp_flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kmalloc_node,
    [](const FtraceEvent& event) -> bool { return event.has_kmalloc_node_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kmalloc_node_format();
        std::stringstream sout;
        sout << "kmalloc_node:";
        sout << " call_site=" << msg.call_site();
        sout << " ptr=" << msg.ptr();
        sout << " bytes_req=" << msg.bytes_req();
        sout << " bytes_alloc=" << msg.bytes_alloc();
        sout << " gfp_flags=" << msg.gfp_flags();
        sout << " node=" << msg.node();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kmem_cache_alloc,
    [](const FtraceEvent& event) -> bool { return event.has_kmem_cache_alloc_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kmem_cache_alloc_format();
        std::stringstream sout;
        sout << "kmem_cache_alloc:";
        sout << " call_site=" << msg.call_site();
        sout << " ptr=" << msg.ptr();
        sout << " bytes_req=" << msg.bytes_req();
        sout << " bytes_alloc=" << msg.bytes_alloc();
        sout << " gfp_flags=" << msg.gfp_flags();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kmem_cache_alloc_node,
    [](const FtraceEvent& event) -> bool { return event.has_kmem_cache_alloc_node_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kmem_cache_alloc_node_format();
        std::stringstream sout;
        sout << "kmem_cache_alloc_node:";
        sout << " call_site=" << msg.call_site();
        sout << " ptr=" << msg.ptr();
        sout << " bytes_req=" << msg.bytes_req();
        sout << " bytes_alloc=" << msg.bytes_alloc();
        sout << " gfp_flags=" << msg.gfp_flags();
        sout << " node=" << msg.node();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    kmem_cache_free,
    [](const FtraceEvent& event) -> bool { return event.has_kmem_cache_free_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.kmem_cache_free_format();
        std::stringstream sout;
        sout << "kmem_cache_free:";
        sout << " call_site=" << msg.call_site();
        sout << " ptr=" << msg.ptr();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_page_alloc,
    [](const FtraceEvent& event) -> bool { return event.has_mm_page_alloc_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_page_alloc_format();
        std::stringstream sout;
        sout << "mm_page_alloc:";
        sout << " pfn=" << msg.pfn();
        sout << " order=" << msg.order();
        sout << " gfp_flags=" << msg.gfp_flags();
        sout << " migratetype=" << msg.migratetype();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_page_alloc_extfrag,
    [](const FtraceEvent& event) -> bool { return event.has_mm_page_alloc_extfrag_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_page_alloc_extfrag_format();
        std::stringstream sout;
        sout << "mm_page_alloc_extfrag:";
        sout << " pfn=" << msg.pfn();
        sout << " alloc_order=" << msg.alloc_order();
        sout << " fallback_order=" << msg.fallback_order();
        sout << " alloc_migratetype=" << msg.alloc_migratetype();
        sout << " fallback_migratetype=" << msg.fallback_migratetype();
        sout << " change_ownership=" << msg.change_ownership();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_page_alloc_zone_locked,
    [](const FtraceEvent& event) -> bool { return event.has_mm_page_alloc_zone_locked_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_page_alloc_zone_locked_format();
        std::stringstream sout;
        sout << "mm_page_alloc_zone_locked:";
        sout << " pfn=" << msg.pfn();
        sout << " order=" << msg.order();
        sout << " migratetype=" << msg.migratetype();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_page_free,
    [](const FtraceEvent& event) -> bool { return event.has_mm_page_free_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_page_free_format();
        std::stringstream sout;
        sout << "mm_page_free:";
        sout << " pfn=" << msg.pfn();
        sout << " order=" << msg.order();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_page_free_batched,
    [](const FtraceEvent& event) -> bool { return event.has_mm_page_free_batched_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_page_free_batched_format();
        std::stringstream sout;
        sout << "mm_page_free_batched:";
        sout << " pfn=" << msg.pfn();
        return sout.str();
    });

REGISTER_FTRACE_EVENT_FORMATTER(
    mm_page_pcpu_drain,
    [](const FtraceEvent& event) -> bool { return event.has_mm_page_pcpu_drain_format(); },
    [](const FtraceEvent& event) -> std::string {
        auto msg = event.mm_page_pcpu_drain_format();
        std::stringstream sout;
        sout << "mm_page_pcpu_drain:";
        sout << " pfn=" << msg.pfn();
        sout << " order=" << msg.order();
        sout << " migratetype=" << msg.migratetype();
        return sout.str();
    });
} // namespace
FTRACE_NS_END

/*
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

#ifndef PROCESS_FILTER_H
#define PROCESS_FILTER_H

#include <tuple>

#include "filter_base.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
class ProcessFilter : private FilterBase {
public:
    explicit ProcessFilter(TraceDataCache*, const TraceStreamerFilters*);
    ~ProcessFilter() override;

    uint32_t SetThreadPid(uint64_t timestamp, uint32_t tid, DataIndex threadNameIndex);
    uint32_t UpdateThreadByName(uint64_t timestamp, uint32_t tid, std::string_view name);

    uint32_t SetThreadPid(uint32_t tid, uint32_t tgid);
    void SetThreadName(uint32_t tid, uint32_t pid, std::string_view name);
    uint32_t UpdateProcess(uint32_t pid, std::string_view name);
    std::tuple<uint32_t, TraceStdtype::Process*> CreateProcessMaybe(uint32_t pid, uint64_t start_ns);

private:
    std::tuple<uint32_t, TraceStdtype::Thread*> NewThread(uint32_t tid);
    std::tuple<uint32_t, TraceStdtype::Process*> NewProcess(uint32_t pid);

    InternalTid GetInternalTid(uint32_t tid) const;
    InternalTid GetInternalPid(uint32_t pid) const;
    InternalTid GetItidExact(uint32_t tid, uint32_t pid) const;
    std::multimap<uint32_t, uint32_t> tidMappingSet_ = {};
    std::map<uint32_t, uint32_t> pidMappingSet_ = {};
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // PROCESS_FILTER_H

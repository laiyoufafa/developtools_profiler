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

#include "process_filter.h"
#include <cinttypes>
#include <limits>
#include <string_view>
#include <utility>

using CustomPair = std::pair<uint32_t, uint32_t>;
namespace SysTuning {
namespace TraceStreamer {
namespace {
    const uint32_t INVALID_ID = std::numeric_limits<uint32_t>::max();
}
ProcessFilter::ProcessFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    :FilterBase(dataCache, filter)
{
    tidMappingSet_.insert(CustomPair(0, 0));
    pidMappingSet_.insert(CustomPair(0, 0));
}

ProcessFilter::~ProcessFilter() {}

uint32_t ProcessFilter::UpdateThreadByName(uint64_t timeStamp, uint32_t tid, std::string_view name)
{
    DataIndex nameIndex = traceDataCache_->GetDataIndex(name);
    return SetThreadPid(timeStamp, tid, nameIndex);
}

uint32_t ProcessFilter::SetThreadPid(uint64_t timeStamp, uint32_t tid, size_t threadNameIndex)
{
    TraceStdtype::Thread* thread = nullptr;
    uint32_t internalTid = GetInternalTid(tid);
    if (internalTid != INVALID_ID) {
        if (threadNameIndex) {
            thread = traceDataCache_->GetThreadData(internalTid);
            thread->nameIndex_ = threadNameIndex;
        }
    } else {
        std::tie(internalTid, thread) = NewThread(tid);
        thread->nameIndex_ = threadNameIndex;
        if (timeStamp) {
            thread->startT_ = timeStamp;
        }
    }

    return internalTid;
}

void ProcessFilter::SetThreadName(uint32_t tid, uint32_t pid, std::string_view name)
{
    uint32_t internalTid = SetThreadPid(tid, pid);
    auto* thread = traceDataCache_->GetThreadData(internalTid);
    auto nameIndex = traceDataCache_->GetDataIndex(name);
    thread->nameIndex_ = nameIndex;
}

uint32_t ProcessFilter::SetThreadPid(uint32_t tid, uint32_t pid)
{
    TraceStdtype::Thread* thread = nullptr;
    uint32_t internalTid = GetItidExact(tid, pid);
    if (internalTid != INVALID_ID) {
        thread = traceDataCache_->GetThreadData(internalTid);
    } else {
        std::tie(internalTid, thread) = NewThread(tid);
    }

    if (!thread->internalPid_) {
        std::tie(thread->internalPid_, std::ignore) = CreateProcessMaybe(pid, thread->startT_);
    }

    return internalTid;
}

uint32_t ProcessFilter::UpdateProcess(uint32_t pid, std::string_view name)
{
    uint32_t internalPid = 0;
    TraceStdtype::Process* process = nullptr;
    std::tie(internalPid, process) = CreateProcessMaybe(pid, 0);
    if (process) {
        process->cmdLine_ = std::string(name);
    }
    SetThreadPid(pid, pid);
    return internalPid;
}

uint32_t ProcessFilter::GetItidExact(uint32_t tid, uint32_t pid) const
{
    uint32_t internalTid = INVALID_ID;
    auto tidsPair = tidMappingSet_.equal_range(tid);
    for (auto it = tidsPair.first; it != tidsPair.second; it++) {
        uint32_t iterUtid = it->second;
        auto* iterThread = traceDataCache_->GetThreadData(iterUtid);
        if (!iterThread->internalPid_) {
            internalTid = iterUtid;
            break;
        }

        const auto& iterProcess = traceDataCache_->GetConstProcessData(iterThread->internalPid_);
        if (iterProcess.pid_ == pid) {
            internalTid = iterUtid;
            break;
        }
    }

    return internalTid;
}

uint32_t ProcessFilter::GetInternalTid(uint32_t tid) const
{
    auto itRange = tidMappingSet_.equal_range(tid);
    if (itRange.first != itRange.second) {
        auto internalTid = std::prev(itRange.second)->second;
        return internalTid;
    }
    return INVALID_ID;
}

uint32_t ProcessFilter::GetInternalPid(uint32_t pid) const
{
    auto it = pidMappingSet_.find(pid);
    if (it != pidMappingSet_.end()) {
        return it->second;
    }
    return INVALID_ID;
}

std::tuple<uint32_t, TraceStdtype::Thread*> ProcessFilter::NewThread(uint32_t tid)
{
    uint32_t internalTid = traceDataCache_->GetInternalThread(tid);
    tidMappingSet_.emplace(tid, internalTid);
    auto* thread = traceDataCache_->GetThreadData(internalTid);

    return std::make_tuple(internalTid, thread);
}

std::tuple<uint32_t, TraceStdtype::Process*> ProcessFilter::NewProcess(uint32_t pid)
{
    uint32_t internalPid = traceDataCache_->GetProcessInternalPid(pid);
    pidMappingSet_.emplace(pid, internalPid);
    auto* process = traceDataCache_->GetProcessData(internalPid);

    return std::make_tuple(internalPid, process);
}

std::tuple<uint32_t, TraceStdtype::Process*> ProcessFilter::CreateProcessMaybe(uint32_t pid, uint64_t startT)
{
    uint32_t internalPid = INVALID_ID;
    TraceStdtype::Process* process = nullptr;
    auto it = pidMappingSet_.find(pid);
    if (it != pidMappingSet_.end()) {
        internalPid = it->second;
        process = traceDataCache_->GetProcessData(internalPid);
    } else {
        std::tie(internalPid, process) = NewProcess(pid);
    }

    if (process->startT_ == 0) {
        process->startT_ = startT;
    }

    return std::make_tuple(internalPid, process);
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "trace_stdtype.h"

namespace SysTuning {
namespace TraceStdtype {
size_t ThreadState::AppendThreadState(uint64_t ts, uint64_t dur,
    uint64_t cpu, uint64_t internalTid, uint64_t state)
{
    internalTids_.emplace_back(internalTid);
    states_.emplace_back(state);
    timeStamps_.emplace_back(ts);
    durs_.emplace_back(dur);
    cpus_.emplace_back(cpu);
    return Size() - 1;
}

void ThreadState::SetDuration(size_t index, uint64_t duration)
{
    durs_[index] = duration;
}

uint64_t ThreadState::UpdateDuration(size_t index, uint64_t timestamp)
{
    durs_[index] = timestamp - timeStamps_[index];
    return internalTids_[index];
}

void ThreadState::UpdateState(size_t index, uint64_t state)
{
    states_[index] = state;
}
void ThreadState::UpdateDuration(size_t index, uint64_t timestamp, uint64_t state)
{
    durs_[index] = timestamp - timeStamps_[index];
    states_[index] = state;
}

uint64_t ThreadState::UpdateDuration(size_t index, uint64_t timestamp, uint64_t cpu, uint64_t state)
{
    cpus_[index] = cpu;
    durs_[index] = timestamp - timeStamps_[index];
    states_[index] = state;
    return internalTids_[index];
}

size_t SchedSlice::AppendSchedSlice(uint64_t ts, uint64_t dur, uint64_t cpu,
    uint64_t internalTid, uint64_t endState, uint64_t priority)
{
    internalTids_.emplace_back(internalTid);
    endStates_.emplace_back(endState);
    priority_.emplace_back(priority);
    timeStamps_.emplace_back(ts);
    durs_.emplace_back(dur);
    cpus_.emplace_back(cpu);
    return Size() - 1;
}

void SchedSlice::SetDuration(size_t index, uint64_t duration)
{
    durs_[index] = duration;
}

void SchedSlice::Update(uint64_t index, uint64_t ts, uint64_t state, uint64_t pior)
{
    durs_[index] = ts - timeStamps_[index];
    endStates_[index] = state;
    priority_[index] = pior;
}


size_t Slices::AppendSliceData(uint32_t cpu, uint64_t startT, uint64_t durationNs, InternalTid internalTid)
{
    durs_.emplace_back(durationNs);
    internalTids_.emplace_back(internalTid);
    cpus_.emplace_back(cpu);
    timeStamps_.emplace_back(startT);
    return Size() - 1;
}

size_t InternalSlices::AppendInternalSlice(uint64_t startT, uint64_t durationNs,
    InternalTid internalTid, uint32_t filterId, DataIndex cat, DataIndex name,
    uint8_t depth, uint64_t stackId, uint64_t parentStackId, std::optional<uint64_t> parentId)
{
    AppendCommonInfo(startT, durationNs, internalTid, filterId);
    AppendCallStack(cat, name, depth, stackId,
                    parentStackId, parentId);
    ids_.emplace_back(ids_.size());
    return Size() - 1;
}

void InternalSlices::AppendCommonInfo(uint64_t startT, uint64_t durationNs, InternalTid internalTid, uint32_t filterId)
{
    timeStamps_.emplace_back(startT);
    durs_.emplace_back(durationNs);
    internalTids_.emplace_back(internalTid);
    filterIds_.emplace_back(filterId);
}
void InternalSlices::AppendCallStack(DataIndex cat, DataIndex name, uint8_t depth, uint64_t stackId,
    uint64_t parentStackId, std::optional<uint64_t> parentId)
{
    stackIds_.emplace_back(stackId);
    parentStackIds_.emplace_back(parentStackId);
    parentIds_.emplace_back(parentId);
    cats_.emplace_back(cat);
    names_.emplace_back(name);
    depths_.emplace_back(depth);
}
size_t Filter::AppendNewFilterData(std::string type, std::string name, uint32_t sourceArgSetId)
{
    nameDeque_.emplace_back(name);
    sourceArgSetId_.emplace_back(sourceArgSetId);
    ids_.emplace_back(Size());
    typeDeque_.emplace_back(type);
    return Size() - 1;
}

size_t Counter::AppendCounterData(uint32_t type, uint64_t timestamp, double value, uint32_t filterId)
{
    valuesDeque_.emplace_back(value);
    filterIdDeque_.emplace_back(filterId);
    typeDeque_.emplace_back(type);
    timeStamps_.emplace_back(timestamp);
    return Size() - 1;
}

size_t Raw::AppendRawData(uint32_t id, uint64_t timestamp, uint32_t name, uint32_t cpu, uint32_t internalTid)
{
    ids_.emplace_back(id);
    timeStamps_.emplace_back(timestamp);
    nameDeque_.emplace_back(name);
    cpuDeque_.emplace_back(cpu);
    utidDeque_.emplace_back(internalTid);
    return Size() - 1;
}

size_t ThreadCounterFilter::AppendNewData(uint64_t filterId, uint32_t nameIndex, uint64_t internalTid)
{
    filterId_.emplace_back(filterId);
    nameIndex_.emplace_back(nameIndex);
    internalTids_.emplace_back(internalTid);
    return Size() - 1;
}

size_t Instants::AppendInstantEventData(uint64_t timestamp, DataIndex nameIndex, int64_t internalTid)
{
    internalTids_.emplace_back(internalTid);
    timeStamps_.emplace_back(timestamp);
    NameIndexs_.emplace_back(nameIndex);
    return Size() - 1;
}

size_t ProcessCounterFilter::AppendProcessCounterFilterData(uint32_t id, DataIndex name, uint32_t internalPid)
{
    internalPids_.emplace_back(internalPid);
    ids_.emplace_back(id);
    names_.emplace_back(name);
    return Size() - 1;
}

size_t ProcessCounterFilter::AppendProcessFilterData(uint32_t id, DataIndex name, uint32_t internalPid)
{
    internalPids_.emplace_back(internalPid);
    ids_.emplace_back(id);
    names_.emplace_back(name);
    return Size() - 1;
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#ifndef TRACE_STDTYPE_H
#define TRACE_STDTYPE_H

#include <array>
#include <deque>
#include <limits>
#include <map>
#include <optional>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

#include "log.h"
#include "src/base/common.h"

namespace SysTuning {
namespace TraceStdtype {
enum EndState {
    TASK_RUNNABLE = 0,        // R
    TASK_INTERRUPTIBLE = 1,   // S
    TASK_UNINTERRUPTIBLE = 2, // D
    TASK_RUNNING = 3,         // Running
    TASK_INTERRUPTED = 4,     // I
    TASK_EXIT_DEAD = 16,      // X
    TASK_ZOMBIE = 32,         // Z
    TASK_CLONE = 64,          // I
    TASK_KILLED = 128,        // K
    TASK_DK = 130,            // DK
    TASK_WAKEKILL = 256,      // W
    TASK_INVALID = 9999,
    TASK_FOREGROUND = 2048 // R+
};

class CacheBase {
public:
    size_t Size() const
    {
        return std::max(timeStamps_.size(), ids_.size());
    }
    const std::deque<uint32_t>& IdsData() const
    {
        return ids_;
    }
    const std::deque<uint64_t>& TimeStamData() const
    {
        return timeStamps_;
    }
    const std::deque<InternalTid>& InternalTidsData() const
    {
        return internalTids_;
    }
public:
    std::deque<InternalTid> internalTids_;
    std::deque<uint64_t> timeStamps_;
    std::deque<uint32_t> ids_;
};

class CpuCacheBase {
public:
    const std::deque<uint64_t>& DursData() const
    {
        return durs_;
    }

    const std::deque<uint64_t>& CpusData() const
    {
        return cpus_;
    }
public:
    std::deque<uint64_t> durs_;
    std::deque<uint64_t> cpus_;
};
class Thread {
public:
    explicit Thread(uint32_t t) : tid_(t) {}
    InternalPid internalPid_ = 0;
    uint32_t tid_ = 0;
    DataIndex nameIndex_ = 0;
    InternalTime startT_ = 0;
    InternalTime endT_ = 0;
};

class Process {
public:
    explicit Process(uint32_t p) : pid_(p) {}
    std::string cmdLine_ = "";
    InternalTime startT_ = 0;
    uint32_t pid_ = 0;
};

class ThreadState : public CacheBase, public CpuCacheBase {
public:
    size_t AppendThreadState(uint64_t ts, uint64_t dur, uint64_t cpu, uint64_t internalTid, uint64_t state);
    void SetDuration(size_t index, uint64_t duration);
    uint64_t UpdateDuration(size_t index, uint64_t timestamp);
    void UpdateState(size_t index, uint64_t state);
    void UpdateDuration(size_t index, uint64_t timestamp, uint64_t state);
    uint64_t UpdateDuration(size_t index, uint64_t timestamp, uint64_t cpu, uint64_t state);
    const std::deque<DataIndex>& StatesData() const
{
        return states_;
    }

private:
    std::deque<DataIndex> states_;
};

class SchedSlice : public CacheBase, public CpuCacheBase {
public:
    size_t AppendSchedSlice(uint64_t ts, uint64_t dur, uint64_t cpu,
            uint64_t internalTid, uint64_t endState, uint64_t priority);
    void SetDuration(size_t index, uint64_t duration);
    void Update(uint64_t index, uint64_t ts, uint64_t state, uint64_t pior);

    const std::deque<uint64_t>& EndStatesData() const
    {
        return endStates_;
    }

    const std::deque<uint64_t>& PriorityData() const
    {
        return priority_;
    }

private:
    std::deque<uint64_t> endStates_;
    std::deque<uint64_t> priority_;
};

class Slices : public CacheBase, public CpuCacheBase {
public:
    size_t AppendSliceData(uint32_t cpu, uint64_t startT, uint64_t durationNs, InternalTid internalTid);

    void SetDuration(size_t index, uint64_t durationNs)
    {
        durs_[index] = durationNs;
    }
};

class InternalSlices : public CacheBase, public CpuCacheBase {
public:
    size_t AppendInternalSlice(uint64_t startT,
                    uint64_t durationNs,
                    InternalTid internalTid,
                    uint32_t filterId,
                    DataIndex cat,
                    DataIndex name,
                    uint8_t depth,
                    uint64_t stackId,
                    uint64_t parentStackId,
                    std::optional<uint64_t> parentId);
    void SetDuration(size_t index, uint64_t durationNs)
    {
        durs_[index] = durationNs;
    }

    void SetStackId(size_t index, uint64_t stackId)
    {
        stackIds_[index] = stackId;
    }
    const std::deque<std::optional<uint64_t>>& ParentIdData() const
    {
        return parentIds_;
    }
    const std::deque<uint32_t>& FilterIdData() const
    {
        return filterIds_;
    }
    const std::deque<DataIndex>& CatsData() const
    {
        return cats_;
    }
    const std::deque<uint64_t>& StackIdsData() const
    {
        return stackIds_;
    }
    const std::deque<uint64_t>& ParentStackIds() const
    {
        return parentStackIds_;
    }
    const std::deque<DataIndex>& NamesData() const
    {
        return names_;
    }
    const std::deque<uint8_t>& Depths() const
    {
        return depths_;
    }
private:
    void AppendCommonInfo(uint64_t startT, uint64_t durationNs, InternalTid internalTid, uint32_t filterId);
    void AppendCallStack(DataIndex cat, DataIndex name, uint8_t depth, uint64_t stackId,
                        uint64_t parentStackId, std::optional<uint64_t> parentId);
private:
    std::deque<std::optional<uint64_t>> parentIds_;
    std::deque<uint32_t> filterIds_;
    std::deque<DataIndex> cats_;
    std::deque<uint64_t> stackIds_;
    std::deque<uint64_t> parentStackIds_;
    std::deque<DataIndex> names_;
    std::deque<uint8_t> depths_;
};

class Filter : public CacheBase {
public:
    size_t AppendNewFilterData(std::string type, std::string name, uint32_t sourceArgSetId);
    const std::deque<std::string>& NameData() const
    {
        return nameDeque_;
    }
    const std::deque<std::string>& TypeData() const
    {
        return typeDeque_;
    }
    const std::deque<uint32_t>& SourceArgSetIdData() const
    {
        return sourceArgSetId_;
    }

private:
    std::deque<std::string> nameDeque_;
    std::deque<std::string> typeDeque_;
    std::deque<uint32_t> sourceArgSetId_;
};

class Counter : public CacheBase {
public:
    size_t AppendCounterData(uint32_t type, uint64_t timestamp, double value, uint32_t filterId);
    const std::deque<uint32_t>& TypeData() const
    {
        return typeDeque_;
    }
    const std::deque<int64_t>& ValuesData() const
    {
        return valuesDeque_;
    }
    const std::deque<uint32_t>& FilterIdData() const
    {
        return filterIdDeque_;
    }

private:
    std::deque<uint32_t> typeDeque_;
    std::deque<int64_t> valuesDeque_;
    std::deque<uint32_t> filterIdDeque_;
};

class Raw : public CacheBase {
public:
    size_t AppendRawData(uint32_t id, uint64_t timestamp, uint32_t name, uint32_t cpu, uint32_t internalTid);
    const std::deque<uint32_t>& NameData() const
    {
        return nameDeque_;
    }
    const std::deque<uint32_t>& CpuData() const
    {
        return cpuDeque_;
    }
    const std::deque<uint32_t>& InternalTidData() const
    {
        return utidDeque_;
    }

private:
    std::deque<uint32_t> nameDeque_;
    std::deque<uint32_t> cpuDeque_;
    std::deque<uint32_t> utidDeque_;
};

class ThreadCounterFilter {
public:
    size_t AppendNewData(uint64_t filterId, uint32_t nameIndex, uint64_t internalTid);
    size_t Size() const
    {
        return filterId_.size();
    }
    const std::deque<uint64_t>& FilterIdData() const
    {
        return filterId_;
    }
    const std::deque<uint64_t>& InternalTidData() const
    {
        return internalTids_;
    }
    const std::deque<uint32_t>& NameIndexData() const
    {
        return nameIndex_;
    }

private:
    std::deque<uint64_t> filterId_;
    std::deque<uint64_t> internalTids_;
    std::deque<uint32_t> nameIndex_;
};

class CpuCounter : public CacheBase {
public:
    inline size_t AppendCpuCounter(uint64_t filterId,
                                DataIndex name,
                                uint64_t cpu,
                                uint64_t sourceArgSetId = 0,
                                DataIndex unit = 0)
    {
        ids_.emplace_back(filterId);
        unit_.emplace_back(unit);
        cpu_.emplace_back(cpu);
        name_.emplace_back(name);
        sourceArgSetId_.emplace_back(sourceArgSetId);
        return Size() - 1;
    }

    const std::deque<uint64_t>& CpuData() const
    {
        return cpu_;
    }

    const std::deque<DataIndex>& TypeData() const
    {
        return type_;
    }

    const std::deque<DataIndex>& UnitData() const
    {
        return unit_;
    }

    const std::deque<DataIndex>& NameData() const
    {
        return name_;
    }

    const std::deque<uint64_t>& SourceArgSetIdData() const
    {
        return sourceArgSetId_;
    }

private:
    std::deque<uint64_t> cpu_;
    std::deque<DataIndex> type_;
    std::deque<DataIndex> unit_;
    std::deque<DataIndex> name_;
    std::deque<uint64_t> sourceArgSetId_;
};

class Instants : public CacheBase {
public:
    size_t AppendInstantEventData(uint64_t timestamp, DataIndex nameIndex, int64_t internalTid);

    const std::deque<DataIndex>& NameIndexsData() const
    {
        return NameIndexs_;
    }

private:
    std::deque<DataIndex> NameIndexs_;
};

class ProcessCounterFilter : public CacheBase {
public:
    size_t AppendProcessCounterFilterData(uint32_t id, DataIndex name, uint32_t internalPid);
    size_t AppendProcessFilterData(uint32_t id, DataIndex name, uint32_t internalPid);

    const std::deque<uint32_t>& UpidsData() const
    {
        return internalPids_;
    }

    const std::deque<DataIndex>& NamesData() const
    {
        return names_;
    }

private:
    std::deque<uint32_t> internalPids_;
    std::deque<DataIndex> names_;
};
class DataDict {
public:
    std::deque<std::string> dataDict_;
    std::unordered_map<uint64_t, DataIndex> dataDictInnerMap_;
public:
    size_t Size() const
    {
        return dataDict_.size();
    }
    DataIndex GetStringIndex(std::string_view str)
    {
        auto hashValue = hashFun(str);
        auto itor = dataDictInnerMap_.find(hashValue);
        if (itor != dataDictInnerMap_.end()) {
            TUNING_ASSERT(std::string_view(dataDict_[itor->second]) == str);
            return itor->second;
        }
        dataDict_.emplace_back(std::string(str));
        DataIndex stringIdentity = dataDict_.size() - 1;
        dataDictInnerMap_.emplace(hashValue, stringIdentity);
        return stringIdentity;
    }
    const std::string& GetDataFromDict(DataIndex id) const
    {
        TUNING_ASSERT(id < dataDict_.size());
        return dataDict_[id];
    }
private:
    std::hash<std::string_view> hashFun;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // TRACE_STDTYPE_H

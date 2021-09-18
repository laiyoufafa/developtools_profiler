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
#include <sstream>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

#include "base/common.h"
#include "cfg/trace_streamer_cfg.h"
#include "log.h"

namespace SysTuning {
namespace TraceStdtype {
using namespace SysTuning::TraceCfg;
class CacheBase {
public:
    virtual ~CacheBase() = default;
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
    virtual ~CpuCacheBase() = default;
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
    size_t AppendSchedSlice(uint64_t ts,
                            uint64_t dur,
                            uint64_t cpu,
                            uint64_t internalTid,
                            uint64_t endState,
                            uint64_t priority);
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

class CallStack : public CacheBase, public CpuCacheBase {
public:
    size_t AppendInternalAsyncSlice(uint64_t startT,
                                    uint64_t durationNs,
                                    InternalTid internalTid,
                                    DataIndex cat,
                                    DataIndex name,
                                    uint8_t depth,
                                    uint64_t cookid,
                                    const std::optional<uint64_t>& parentId);
    size_t AppendInternalSlice(uint64_t startT,
                               uint64_t durationNs,
                               InternalTid internalTid,
                               DataIndex cat,
                               DataIndex name,
                               uint8_t depth,
                               const std::optional<uint64_t>& parentId);
    void AppendDistributeInfo(const std::string& chainId,
                              const std::string& spanId,
                              const std::string& parentSpanId,
                              const std::string& flag,
                              const std::string& args);
    void AppendDistributeInfo();
    void SetDuration(size_t index, uint64_t timestamp);
    void SetTimeStamp(size_t index, uint64_t timestamp);

    const std::deque<std::optional<uint64_t>>& ParentIdData() const;
    const std::deque<DataIndex>& CatsData() const;
    const std::deque<DataIndex>& NamesData() const;
    const std::deque<uint8_t>& Depths() const;
    const std::deque<uint64_t>& Cookies() const;
    const std::deque<uint64_t>& CallIds() const;
    const std::deque<std::string>& ChainIds() const;
    const std::deque<std::string>& SpanIds() const;
    const std::deque<std::string>& ParentSpanIds() const;
    const std::deque<std::string>& Flags() const;
    const std::deque<std::string>& ArgsData() const;

private:
    void AppendCommonInfo(uint64_t startT, uint64_t durationNs, InternalTid internalTid);
    void AppendCallStack(DataIndex cat, DataIndex name, uint8_t depth, std::optional<uint64_t> parentId);

private:
    std::deque<std::optional<uint64_t>> parentIds_;
    std::deque<DataIndex> cats_;
    std::deque<uint64_t> cookies_;
    std::deque<uint64_t> callIds_;
    std::deque<DataIndex> names_;
    std::deque<uint8_t> depths_;

    std::deque<std::string> chainIds_;
    std::deque<std::string> spanIds_;
    std::deque<std::string> parentSpanIds_;
    std::deque<std::string> flags_;
    std::deque<std::string> args_;
};

class Filter : public CacheBase {
public:
    size_t AppendNewFilterData(std::string type, std::string name, uint64_t sourceArgSetId);
    const std::deque<std::string>& NameData() const
    {
        return nameDeque_;
    }
    const std::deque<std::string>& TypeData() const
    {
        return typeDeque_;
    }
    const std::deque<uint64_t>& SourceArgSetIdData() const
    {
        return sourceArgSetId_;
    }

private:
    std::deque<std::string> nameDeque_;
    std::deque<std::string> typeDeque_;
    std::deque<uint64_t> sourceArgSetId_;
};

class Measure : public CacheBase {
public:
    size_t AppendMeasureData(uint32_t type, uint64_t timestamp, int64_t value, uint32_t filterId);
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
        return itidDeque_;
    }

private:
    std::deque<uint32_t> nameDeque_;
    std::deque<uint32_t> cpuDeque_;
    std::deque<uint32_t> itidDeque_;
};

class ThreadMeasureFilter {
public:
    size_t AppendNewFilter(uint64_t filterId, uint32_t nameIndex, uint64_t internalTid);
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

class CpuMeasureFilter : public CacheBase {
public:
    inline size_t AppendNewFilter(uint64_t filterId, DataIndex name, uint64_t cpu)
    {
        ids_.emplace_back(filterId);
        cpu_.emplace_back(cpu);
        name_.emplace_back(name);
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

    const std::deque<DataIndex>& NameData() const
    {
        return name_;
    }

private:
    std::deque<uint64_t> cpu_;
    std::deque<DataIndex> type_;
    std::deque<DataIndex> name_;
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

class ProcessMeasureFilter : public CacheBase {
public:
    size_t AppendNewFilter(uint64_t id, DataIndex name, uint32_t internalPid);

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
class ClockEventData : public CacheBase {
public:
    size_t AppendNewFilter(uint64_t id, DataIndex type, DataIndex name, uint64_t cpu);

    const std::deque<uint64_t>& CpusData() const
    {
        return cpus_;
    }

    const std::deque<DataIndex>& NamesData() const
    {
        return names_;
    }
    const std::deque<DataIndex>& TypesData() const
    {
        return types_;
    }

private:
    std::deque<uint64_t> cpus_; // in clock_set_rate event, it save cpu
    std::deque<DataIndex> names_;
    std::deque<DataIndex> types_;
};
class StatAndInfo {
public:
    StatAndInfo();
    ~StatAndInfo() = default;
    void IncreaseStat(SupportedTraceEventType eventType, StatType type);
    const uint32_t& GetValue(SupportedTraceEventType eventType, StatType type) const;
    const std::string& GetEvent(SupportedTraceEventType eventType) const;
    const std::string& GetStat(StatType type) const;
    const std::string& GetSeverityDesc(SupportedTraceEventType eventType, StatType type) const;
    const StatSeverityLevel& GetSeverity(SupportedTraceEventType eventType, StatType type) const;

private:
    uint32_t statCount_[TRACE_EVENT_MAX][STAT_EVENT_MAX];
    std::string event_[TRACE_EVENT_MAX];
    std::string stat_[STAT_EVENT_MAX];
    std::string statSeverityDesc_[TRACE_EVENT_MAX][STAT_EVENT_MAX];
    StatSeverityLevel statSeverity_[TRACE_EVENT_MAX][STAT_EVENT_MAX];
    TraceStreamConfig config_;
};
class SymbolsData {
public:
    SymbolsData() = default;
    ~SymbolsData() = default;
    uint64_t Size() const;
    void InsertSymbol(const DataIndex& name, const uint64_t& addr);
    const std::deque<DataIndex>& GetConstFuncNames() const;
    const std::deque<uint64_t>& GetConstAddrs() const;

private:
    std::deque<uint64_t> addrs_;
    std::deque<DataIndex> funcName_;
};
class MetaData {
public:
    MetaData();
    ~MetaData() = default;
    void SetTraceType(const std::string& traceType);
    void SetSourceFileName(const std::string& fileName);
    void SetOutputFileName(const std::string& fileName);
    void SetParserToolVersion(const std::string& version);
    void SetParserToolPublishDateTime(const std::string& datetime);
    void SetTraceDataSize(uint64_t dataSize);
    const std::string& Value(uint64_t row) const;
    const std::string& Name(uint64_t row) const;

private:
    const std::string METADATA_ITEM_DATASIZE_COLNAME = "datasize";
    const std::string METADATA_ITEM_PARSETOOL_NAME_COLNAME = "parse_tool";
    const std::string METADATA_ITEM_PARSERTOOL_VERSION_COLNAME = "tool_version";
    const std::string METADATA_ITEM_PARSERTOOL_PUBLISH_DATETIME_COLNAME = "tool_publish_time";
    const std::string METADATA_ITEM_SOURCE_FILENAME_COLNAME = "source_name";
    const std::string METADATA_ITEM_OUTPUT_FILENAME_COLNAME = "output_name";
    const std::string METADATA_ITEM_PARSERTIME_COLNAME = "runtime";
    const std::string METADATA_ITEM_SOURCE_DATETYPE_COLNAME = "source_type";

    std::deque<std::string> columnNames_;
    std::deque<std::string> values_;
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
    DataIndex GetStringIndex(std::string_view str);
    const std::string& GetDataFromDict(DataIndex id) const
    {
        TS_ASSERT(id < dataDict_.size());
        return dataDict_[id];
    }

private:
    std::hash<std::string_view> hashFun;
};
} // namespace TraceStdtype
} // namespace SysTuning

#endif // TRACE_STDTYPE_H

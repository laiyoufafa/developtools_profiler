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
#include <mutex>
#include <optional>
#include <sstream>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

#include "cfg/trace_streamer_config.h"
#include "double_map.h"
#include "log.h"
#include "ts_common.h"

namespace SysTuning {
namespace TraceStdtype {
using namespace SysTuning::TraceCfg;
using namespace SysTuning::TraceStreamer;
class CacheBase {
public:
    size_t Size() const
    {
        return std::max(timeStamps_.size(), ids_.size());
    }
    const std::deque<uint64_t>& IdsData() const
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
    virtual void Clear()
    {
        internalTids_.clear();
        timeStamps_.clear();
        ids_.clear();
    }

public:
    std::deque<InternalTid> internalTids_ = {};
    std::deque<uint64_t> timeStamps_ = {};
    std::deque<uint64_t> ids_ = {};
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
    virtual void Clear()
    {
        durs_.clear();
        cpus_.clear();
    }
    void SetDur(uint64_t index, uint64_t dur)
    {
        durs_[index] = dur;
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

class ThreadState {
public:
    TableRowId
        AppendThreadState(InternalTime ts, InternalTime dur, InternalCpu cpu, TableRowId idTid, TableRowId idState);
    void SetDuration(TableRowId index, InternalTime dur);
    TableRowId UpdateDuration(TableRowId index, InternalTime ts);
    void UpdateState(TableRowId index, TableRowId idState);
    void UpdateDuration(TableRowId index, InternalTime ts, TableRowId idState);
    TableRowId UpdateDuration(TableRowId index, InternalTime ts, InternalCpu cpu, TableRowId idState);
    struct ColumnData {
        InternalTime timeStamp;
        InternalTime duration;
        TableRowId idTid;
        TableRowId idState;
        InternalCpu cpu;
    };
    const std::deque<ColumnData>& RowData() const
    {
        return rowDatas_;
    }
    void Clear()
    {
        rowDatas_.clear();
    }
    size_t Size()
    {
        return rowDatas_.size();
    }

private:
    std::deque<ColumnData> rowDatas_ = {};
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
    const std::deque<uint64_t>& TsEndData() const
    {
        return tsEnds_;
    }
    const std::deque<InternalPid>& InternalPidsData() const
    {
        return internalPids_;
    }
    void AppendInternalPid(InternalPid ipid)
    {
        internalPids_.emplace_back(ipid);
    }
    virtual void Clear() override
    {
        CacheBase::Clear();
        CpuCacheBase::Clear();
        endStates_.clear();
        priority_.clear();
        internalPids_.clear();
        tsEnds_.clear();
    }

private:
    std::deque<InternalPid> internalPids_ = {};
    std::deque<uint64_t> tsEnds_ = {};
    std::deque<uint64_t> endStates_ = {};
    std::deque<uint64_t> priority_ = {};
};

class CallStack : public CacheBase, public CpuCacheBase {
public:
    size_t AppendInternalAsyncSlice(uint64_t startT,
                                    int32_t durationNs,
                                    InternalTid internalTid,
                                    DataIndex cat,
                                    uint16_t nameIdentify,
                                    DataIndex name,
                                    uint8_t depth,
                                    uint64_t cookid,
                                    const std::optional<uint64_t>& parentId);
    size_t AppendInternalSlice(uint64_t startT,
                               int32_t durationNs,
                               InternalTid internalTid,
                               DataIndex cat,
                               uint16_t nameIdentify,
                               DataIndex name,
                               uint8_t depth,
                               const std::optional<uint64_t>& parentId);
    void AppendDistributeInfo(const std::string& chainId,
                              const std::string& spanId,
                              const std::string& parentSpanId,
                              const std::string& flag,
                              const std::string& args);
    void AppendArgSet(uint32_t argSetId);
    void AppendDistributeInfo();
    void SetDuration(size_t index, uint64_t timestamp);
    void SetDurationAndArg(size_t index, uint64_t timestamp, uint32_t argSetId);
    void SetTimeStamp(size_t index, uint64_t timestamp);
    virtual void Clear() override
    {
        CacheBase::Clear();
        CpuCacheBase::Clear();
        cats_.clear();
        cookies_.clear();
        callIds_.clear();
        names_.clear();
        depths_.clear();
        chainIds_.clear();
        spanIds_.clear();
        parentSpanIds_.clear();
        flags_.clear();
        args_.clear();
        argSet_.clear();
    }

    const std::deque<std::optional<uint64_t>>& ParentIdData() const;
    const std::deque<DataIndex>& CatsData() const;
    const std::deque<DataIndex>& NamesData() const;
    const std::deque<uint8_t>& Depths() const;
    const std::deque<uint64_t>& Cookies() const;
    const std::deque<uint64_t>& CallIds() const;
    const std::deque<uint16_t>& IdentifysData() const;
    const std::deque<std::string>& ChainIds() const;
    const std::deque<std::string>& SpanIds() const;
    const std::deque<std::string>& ParentSpanIds() const;
    const std::deque<std::string>& Flags() const;
    const std::deque<std::string>& ArgsData() const;
    const std::deque<uint32_t>& ArgSetIdsData() const;

private:
    void AppendCommonInfo(uint64_t startT, int32_t durationNs, InternalTid internalTid);
    void AppendCallStack(DataIndex cat, DataIndex name, uint8_t depth, std::optional<uint64_t> parentId);

private:
    std::deque<std::optional<uint64_t>> parentIds_;
    std::deque<DataIndex> cats_ = {};
    std::deque<uint64_t> cookies_ = {};
    std::deque<uint64_t> callIds_ = {};
    std::deque<uint16_t> identifys_ = {};
    std::deque<DataIndex> names_ = {};
    std::deque<uint8_t> depths_ = {};

    std::deque<std::string> chainIds_ = {};
    std::deque<std::string> spanIds_ = {};
    std::deque<std::string> parentSpanIds_ = {};
    std::deque<std::string> flags_ = {};
    std::deque<std::string> args_ = {};
    std::deque<uint32_t> argSet_ = {};
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
    virtual void Clear() override
    {
        CacheBase::Clear();
        nameDeque_.clear();
        typeDeque_.clear();
        sourceArgSetId_.clear();
    }

private:
    std::deque<std::string> nameDeque_ = {};
    std::deque<std::string> typeDeque_ = {};
    std::deque<uint64_t> sourceArgSetId_ = {};
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
    virtual void Clear() override
    {
        CacheBase::Clear();
        typeDeque_.clear();
        valuesDeque_.clear();
        filterIdDeque_.clear();
    }

private:
    std::deque<uint32_t> typeDeque_ = {};
    std::deque<int64_t> valuesDeque_ = {};
    std::deque<uint32_t> filterIdDeque_ = {};
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
    virtual void Clear() override
    {
        CacheBase::Clear();
        nameDeque_.clear();
        cpuDeque_.clear();
        itidDeque_.clear();
    }

private:
    std::deque<uint32_t> nameDeque_ = {};
    std::deque<uint32_t> cpuDeque_ = {};
    std::deque<uint32_t> itidDeque_ = {};
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
    void Clear()
    {
        filterId_.clear();
        internalTids_.clear();
        nameIndex_.clear();
    }

private:
    std::deque<uint64_t> filterId_ = {};
    std::deque<uint64_t> internalTids_ = {};
    std::deque<uint32_t> nameIndex_ = {};
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
    virtual void Clear() override
    {
        CacheBase::Clear();
        cpu_.clear();
        type_.clear();
        name_.clear();
    }

private:
    std::deque<uint64_t> cpu_ = {};
    std::deque<DataIndex> type_ = {};
    std::deque<DataIndex> name_ = {};
};

class Instants : public CacheBase {
public:
    size_t AppendInstantEventData(uint64_t timestamp, DataIndex nameIndex, int64_t internalTid, int64_t wakeupFromInternalPid);

    const std::deque<DataIndex>& NameIndexsData() const
    {
        return NameIndexs_;
    }
    const std::deque<int64_t>& WakeupFromPidsData() const
    {
        return wakeupFromInternalPids_;
    }
    virtual void Clear() override
    {
        CacheBase::Clear();
        NameIndexs_.clear();
        wakeupFromInternalPids_.clear();
    }

private:
    std::deque<DataIndex> NameIndexs_;
    std::deque<int64_t> wakeupFromInternalPids_;
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
    virtual void Clear() override
    {
        CacheBase::Clear();
        internalPids_.clear();
        names_.clear();
    }

private:
    std::deque<uint32_t> internalPids_ = {};
    std::deque<DataIndex> names_ = {};
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
    virtual void Clear() override
    {
        CacheBase::Clear();
        cpus_.clear();
        names_.clear();
        types_.clear();
    }

private:
    std::deque<uint64_t> cpus_ = {}; // in clock_set_rate event, it save cpu
    std::deque<DataIndex> names_ = {};
    std::deque<DataIndex> types_ = {};
};
class ClkEventData : public CacheBase {
public:
    size_t AppendNewFilter(uint64_t id, uint64_t rate, DataIndex name, uint64_t cpu);

    const std::deque<DataIndex>& NamesData() const
    {
        return names_;
    }
    const std::deque<uint64_t>& RatesData() const
    {
        return rates_;
    }
    const std::deque<uint64_t>& CpusData() const
    {
        return cpus_;
    }
    virtual void Clear() override
    {
        CacheBase::Clear();
        names_.clear();
        rates_.clear();
        cpus_.clear();
    }

private:
    std::deque<DataIndex> names_;
    std::deque<uint64_t> rates_;
    std::deque<uint64_t> cpus_;
};
class SysCall : public CacheBase {
public:
    size_t AppendSysCallData(int64_t sysCallNum, DataIndex type, uint64_t ipid, uint64_t timestamp, int64_t ret);
    const std::deque<int64_t>& SysCallsData() const
    {
        return sysCallNums_;
    }
    const std::deque<DataIndex>& TypesData() const
    {
        return types_;
    }
    const std::deque<uint64_t>& IpidsData() const
    {
        return ipids_;
    }
    const std::deque<uint64_t>& RetsData() const
    {
        return rets_;
    }
    virtual void Clear() override
    {
        CacheBase::Clear();
        sysCallNums_.clear();
        types_.clear();
        ipids_.clear();
        rets_.clear();
    }

private:
    std::deque<int64_t> sysCallNums_ = {};
    std::deque<DataIndex> types_ = {};
    std::deque<uint64_t> ipids_ = {};
    std::deque<uint64_t> rets_ = {};
};
class ArgSet : public CacheBase {
public:
    size_t AppendNewArg(DataIndex nameId, BaseDataType dataType, int64_t value, size_t argSet);
    const std::deque<BaseDataType>& DataTypes() const;
    const std::deque<int64_t>& ValuesData() const;
    const std::deque<uint64_t>& ArgsData() const;
    const std::deque<DataIndex>& NamesData() const;

    virtual void Clear() override
    {
        CacheBase::Clear();
        names_.clear();
        dataTypes_.clear();
        values_.clear();
        argset_.clear();
    }

private:
    std::deque<uint64_t> names_ = {};
    std::deque<BaseDataType> dataTypes_ = {};
    std::deque<int64_t> values_ = {};
    std::deque<uint64_t> argset_ = {};
};
class SysMeasureFilter : public CacheBase {
public:
    size_t AppendNewFilter(uint64_t filterId, DataIndex type, DataIndex nameId);
    const std::deque<DataIndex>& NamesData() const;
    const std::deque<DataIndex>& TypesData() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        types_.clear();
        names_.clear();
    }

private:
    std::deque<DataIndex> types_ = {};
    std::deque<DataIndex> names_ = {};
};
class DataType : public CacheBase {
public:
    size_t AppendNewDataType(BaseDataType dataType, DataIndex dataDescIndex);
    const std::deque<BaseDataType>& DataTypes() const;
    const std::deque<DataIndex>& DataDesc() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        dataTypes_.clear();
        descs_.clear();
    }

private:
    std::deque<BaseDataType> dataTypes_ = {};
    std::deque<DataIndex> descs_ = {};
};
class LogInfo : public CacheBase {
public:
    size_t AppendNewLogInfo(uint64_t seq,
                            uint64_t timestamp,
                            uint32_t pid,
                            uint32_t tid,
                            DataIndex level,
                            DataIndex tag,
                            DataIndex context,
                            uint64_t originTs);
    const std::deque<uint64_t>& HilogLineSeqs() const;
    const std::deque<uint32_t>& Pids() const;
    const std::deque<uint32_t>& Tids() const;
    const std::deque<DataIndex>& Levels() const;
    const std::deque<DataIndex>& Tags() const;
    const std::deque<DataIndex>& Contexts() const;
    const std::deque<uint64_t>& OriginTimeStamData() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        hilogLineSeqs_.clear();
        pids_.clear();
        levels_.clear();
        tags_.clear();
        contexts_.clear();
        originTs_.clear();
    }

private:
    std::deque<uint64_t> hilogLineSeqs_ = {};
    std::deque<uint32_t> pids_ = {};
    std::deque<uint32_t> tids_ = {};
    std::deque<DataIndex> levels_ = {};
    std::deque<DataIndex> tags_ = {};
    std::deque<DataIndex> contexts_ = {};
    std::deque<uint64_t> originTs_ = {};
};

class NativeHook : public CacheBase {
public:
    size_t AppendNewNativeHookData(uint64_t eventId,
                                   uint32_t ipid,
                                   uint32_t itid,
                                   std::string eventType,
                                   DataIndex subType,
                                   uint64_t timestamp,
                                   uint64_t endTimestamp,
                                   uint64_t duration,
                                   uint64_t addr,
                                   int64_t memSize,
                                   int64_t allMemSize);
    void UpdateHeapDuration(size_t row, uint64_t endTimestamp);
    void UpdateCurrentSizeDur(size_t row, uint64_t timeStamp);
    void UpdateMemMapSubType();
    void UpdateAddrToMemMapSubType(uint64_t, int64_t, uint64_t);
    const std::deque<uint64_t>& EventIds() const;
    const std::deque<uint32_t>& Ipids() const;
    const std::deque<uint32_t>& Itids() const;
    const std::deque<std::string>& EventTypes() const;
    const std::deque<DataIndex>& SubTypes() const;
    const std::deque<uint64_t>& EndTimeStamps() const;
    const std::deque<uint64_t>& Durations() const;
    const std::deque<uint64_t>& Addrs() const;
    const std::deque<int64_t>& MemSizes() const;
    const std::deque<int64_t>& AllMemSizes() const;
    const std::deque<uint64_t>& CurrentSizeDurs() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        eventIds_.clear();
        ipids_.clear();
        itids_.clear();
        eventTypes_.clear();
        subTypes_.clear();
        endTimestamps_.clear();
        durations_.clear();
        addrs_.clear();
        memSizes_.clear();
        allMemSizes_.clear();
        currentSizeDurs_.clear();
    }

private:
    std::deque<uint64_t> eventIds_ = {};
    std::deque<uint32_t> ipids_ = {};
    std::deque<uint32_t> itids_ = {};
    std::deque<std::string> eventTypes_ = {};
    std::deque<DataIndex> subTypes_ = {};
    std::deque<uint64_t> endTimestamps_ = {};
    std::deque<uint64_t> durations_ = {};
    std::deque<uint64_t> addrs_ = {};
    std::deque<int64_t> memSizes_ = {};
    std::deque<int64_t> allMemSizes_ = {};
    std::deque<uint64_t> currentSizeDurs_ = {};
    DoubleMap<uint64_t, int64_t, uint64_t> addrToMmapTag_ = INVALID_UINT64;
    int64_t countHeapSizes_ = 0;
    int64_t countMmapSizes_ = 0;
    const std::string ALLOC_EVET = "AllocEvent";
    const std::string FREE_EVENT = "FreeEvent";
    const std::string MMAP_EVENT = "MmapEvent";
    const std::string MUNMAP_EVENT = "MunmapEvent";
};

class NativeHookFrame {
public:
    size_t AppendNewNativeHookFrame(uint64_t eventId,
                                    uint64_t depth,
                                    uint64_t ip,
                                    uint64_t sp,
                                    DataIndex symbolName,
                                    DataIndex filePath,
                                    uint64_t offset,
                                    uint64_t symbolOffset);
    void UpdateSymbolIdToNameMap(uint64_t, uint64_t);
    void UpdateFilePathIdToNameMap(uint64_t, uint64_t);
    void UpdateSymbolId();
    void UpdateFileId();
    const std::deque<uint64_t>& EventIds() const;
    const std::deque<uint64_t>& Depths() const;
    const std::deque<uint64_t>& Ips() const;
    const std::deque<uint64_t>& Sps() const;
    const std::deque<DataIndex>& SymbolNames() const;
    const std::deque<DataIndex>& FilePaths() const;
    const std::deque<uint64_t>& Offsets() const;
    const std::deque<uint64_t>& SymbolOffsets() const;
    size_t Size() const
    {
        return eventIds_.size();
    }
    void Clear()
    {
        eventIds_.clear();
        depths_.clear();
        ips_.clear();
        sps_.clear();
        symbolNames_.clear();
        filePaths_.clear();
        offsets_.clear();
        symbolOffsets_.clear();
    }

private:
    std::deque<uint64_t> eventIds_ = {};
    std::deque<uint64_t> depths_ = {};
    std::deque<uint64_t> ips_ = {};
    std::deque<uint64_t> sps_ = {};
    std::deque<DataIndex> symbolNames_ = {};
    std::deque<DataIndex> filePaths_ = {};
    std::deque<uint64_t> offsets_ = {};
    std::deque<uint64_t> symbolOffsets_ = {};
    std::map<uint32_t, uint64_t> filePathIdToFilePathName_;
    std::map<uint32_t, uint64_t> symbolIdToSymbolName_;
};

class Hidump : public CacheBase {
public:
    size_t AppendNewHidumpInfo(uint64_t timestamp, uint32_t fps);
    const std::deque<uint32_t>& Fpss() const;

private:
    std::deque<uint32_t> fpss_ = {};
};

class PerfCallChain : public CacheBase {
public:
    size_t AppendNewPerfCallChain(uint64_t sampleId,
                                  uint64_t callchainId,
                                  uint64_t vaddrInFile,
                                  uint64_t fileId,
                                  uint64_t symbolId);
    const std::deque<uint64_t>& SampleIds() const;
    const std::deque<uint64_t>& CallchainIds() const;
    const std::deque<uint64_t>& VaddrInFiles() const;
    const std::deque<uint64_t>& FileIds() const;
    const std::deque<uint64_t>& SymbolIds() const;
    const std::deque<std::string>& Names() const;
    void SetName(uint64_t index, const std::string& name);

private:
    std::deque<uint64_t> sampleIds_ = {};
    std::deque<uint64_t> callchainIds_ = {};
    std::deque<uint64_t> vaddrInFiles_ = {};
    std::deque<uint64_t> fileIds_ = {};
    std::deque<uint64_t> symbolIds_ = {};
    std::deque<std::string> names_ = {};
};

class PerfFiles : public CacheBase {
public:
    size_t AppendNewPerfFiles(uint64_t fileIds, uint32_t serial, DataIndex symbols, DataIndex filePath);
    const std::deque<uint64_t>& FileIds() const;
    const std::deque<DataIndex>& Symbols() const;
    const std::deque<DataIndex>& FilePaths() const;
    const std::deque<uint32_t>& Serials() const;

private:
    std::deque<uint64_t> fileIds_ = {};
    std::deque<uint32_t> serials_ = {};
    std::deque<DataIndex> symbols_ = {};
    std::deque<DataIndex> filePaths_ = {};
};

class PerfSample : public CacheBase {
public:
    size_t AppendNewPerfSample(uint64_t sampleId,
                               uint64_t timestamp,
                               uint64_t tid,
                               uint64_t eventCount,
                               uint64_t eventTypeId,
                               uint64_t timestampTrace,
                               uint64_t cpuId,
                               uint64_t threadState);
    const std::deque<uint64_t>& SampleIds() const;
    const std::deque<uint64_t>& Tids() const;
    const std::deque<uint64_t>& EventCounts() const;
    const std::deque<uint64_t>& EventTypeIds() const;
    const std::deque<uint64_t>& TimestampTraces() const;
    const std::deque<uint64_t>& CpuIds() const;
    const std::deque<DataIndex>& ThreadStates() const;
private:
    std::deque<uint64_t> sampleIds_ = {};
    std::deque<uint64_t> tids_ = {};
    std::deque<uint64_t> eventCounts_ = {};
    std::deque<uint64_t> eventTypeIds_ = {};
    std::deque<uint64_t> timestampTraces_ = {};
    std::deque<uint64_t> cpuIds_ = {};
    std::deque<DataIndex> threadStates_ = {};
};

class PerfThread : public CacheBase {
public:
    size_t AppendNewPerfThread(uint64_t pid, uint64_t tid, DataIndex threadName);
    const std::deque<uint64_t>& Pids() const;
    const std::deque<uint64_t>& Tids() const;
    const std::deque<DataIndex>& ThreadNames() const;
private:
    std::deque<uint64_t> tids_ = {};
    std::deque<uint64_t> pids_ = {};
    std::deque<DataIndex> threadNames_ = {};
};

class PerfReport : public CacheBase {
public:
    size_t AppendNewPerfReport(DataIndex type, DataIndex value);
    const std::deque<DataIndex>& Types() const;
    const std::deque<DataIndex>& Values() const;
private:
    std::deque<DataIndex> types_ = {};
    std::deque<DataIndex> values_ = {};
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
    TraceStreamerConfig config_;
};
class SymbolsData {
public:
    SymbolsData() = default;
    ~SymbolsData() = default;
    uint64_t Size() const;
    void InsertSymbol(const DataIndex& name, const uint64_t& addr);
    const std::deque<DataIndex>& GetConstFuncNames() const;
    const std::deque<uint64_t>& GetConstAddrs() const;
    void Clear()
    {
        addrs_.clear();
        funcName_.clear();
    }

private:
    std::deque<uint64_t> addrs_ = {};
    std::deque<DataIndex> funcName_ = {};
};
class DiskIOData : public CacheBase {
public:
    DiskIOData() = default;
    ~DiskIOData() = default;
    void AppendNewData(uint64_t ts,
                       uint64_t dur,
                       uint64_t rd,
                       uint64_t wr,
                       uint64_t rdPerSec,
                       uint64_t wrPerSec,
                       double rdCountPerSec,
                       double wrCountPerSec,
                       uint64_t rdCount,
                       uint64_t wrCount);
    const std::deque<uint64_t>& Durs() const;
    const std::deque<uint64_t>& RdDatas() const;
    const std::deque<uint64_t>& WrDatas() const;
    const std::deque<double>& RdSpeedDatas() const;
    const std::deque<double>& WrSpeedDatas() const;
    const std::deque<double>& RdCountPerSecDatas() const;
    const std::deque<double>& WrCountPerSecDatas() const;
    const std::deque<uint64_t>& RdCountDatas() const;
    const std::deque<uint64_t>& WrCountDatas() const;

private:
    std::deque<uint64_t> durs_ = {};
    std::deque<uint64_t> rdDatas_ = {};
    std::deque<uint64_t> wrDatas_ = {};
    std::deque<double> wrPerSec_ = {};
    std::deque<double> rdPerSec_ = {};
    std::deque<double> wrCountPerSec_ = {};
    std::deque<double> rdCountPerSec_ = {};
    std::deque<uint64_t> rdCountDatas_ = {};
    std::deque<uint64_t> wrCountDatas_ = {};
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
    void SetTraceDuration(uint64_t dur);
    const std::string& Value(uint64_t row) const;
    const std::string& Name(uint64_t row) const;
    void Clear()
    {
        columnNames_.clear();
        values_.clear();
    }

private:
    const std::string METADATA_ITEM_DATASIZE_COLNAME = "datasize";
    const std::string METADATA_ITEM_PARSETOOL_NAME_COLNAME = "parse_tool";
    const std::string METADATA_ITEM_PARSERTOOL_VERSION_COLNAME = "tool_version";
    const std::string METADATA_ITEM_PARSERTOOL_PUBLISH_DATETIME_COLNAME = "tool_publish_time";
    const std::string METADATA_ITEM_SOURCE_FILENAME_COLNAME = "source_name";
    const std::string METADATA_ITEM_OUTPUT_FILENAME_COLNAME = "output_name";
    const std::string METADATA_ITEM_PARSERTIME_COLNAME = "runtime";
    const std::string METADATA_ITEM_TRACE_DURATION_COLNAME = "trace_duration";
    const std::string METADATA_ITEM_SOURCE_DATETYPE_COLNAME = "source_type";

    std::deque<std::string> columnNames_ = {};
    std::deque<std::string> values_ = {};
};
class DataDict {
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
    void Finish()
    {
        std::string::size_type pos(0);
        for (auto i = 0; i < dataDict_.size(); i++) {
            while ((pos = dataDict_[i].find("\"")) != std::string::npos){
                dataDict_[i].replace(pos, 1, "\'");
            }
        }
    }
    void Clear()
    {
        dataDict_.clear();
    }

public:
    std::deque<std::string> dataDict_;
    std::unordered_map<uint64_t, DataIndex> dataDictInnerMap_;

private:
    std::hash<std::string_view> hashFun;
    std::mutex mutex_;
};
class NetDetailData : public CacheBase {
public:
    size_t AppendNewNetData(uint64_t newTimeStamp,
                            uint64_t tx,
                            uint64_t rx,
                            uint64_t dur,
                            double rxSpeed,
                            double txSpeed,
                            uint64_t packetIn,
                            double packetInSec,
                            uint64_t packetOut,
                            double packetOutSec,
                            const std::string& netType);
    const std::deque<uint64_t>& Durs() const;
    const std::deque<double>& RxSpeed() const;
    const std::deque<double>& TxSpeed() const;
    const std::deque<std::string>& NetTypes() const;
    const std::deque<uint64_t>& RxDatas() const;
    const std::deque<uint64_t>& TxDatas() const;
    const std::deque<uint64_t>& PacketIn() const;
    const std::deque<double>& PacketInSec() const;
    const std::deque<uint64_t>& PacketOut() const;
    const std::deque<double>& PacketOutSec() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        durs_.clear();
        rxSpeeds_.clear();
        txSpeeds_.clear();
        netTypes_.clear();
        packetIn_.clear();
        packetInSec_.clear();
        packetOut_.clear();
        packetOutSec_.clear();
    }

private:
    std::deque<uint64_t> rxs_ = {};
    std::deque<uint64_t> txs_ = {};
    std::deque<uint64_t> durs_ = {};
    std::deque<double> rxSpeeds_ = {};
    std::deque<double> txSpeeds_ = {};
    std::deque<uint64_t> packetIn_ = {};
    std::deque<double> packetInSec_ = {};
    std::deque<uint64_t> packetOut_ = {};
    std::deque<double> packetOutSec_ = {};
    std::deque<std::string> netTypes_ = {};
};
class LiveProcessDetailData : public CacheBase {
public:
    size_t AppendNewData(uint64_t newTimeStamp,
                         uint64_t dur,
                         int32_t processID,
                         std::string processName,
                         int32_t parentProcessID,
                         int32_t uid,
                         std::string userName,
                         double cpuUsage,
                         int32_t pssInfo,
                         uint64_t cpuTime,
                         int32_t threads,
                         int64_t diskWrites,
                         int64_t diskReads);
    const std::deque<uint64_t>& Durs() const;
    const std::deque<int32_t>& ProcessID() const;
    const std::deque<std::string>& ProcessName() const;
    const std::deque<int32_t>& ParentProcessID() const;
    const std::deque<int32_t>& Uid() const;
    const std::deque<std::string>& UserName() const;
    const std::deque<double>& CpuUsage() const;
    const std::deque<int32_t>& PssInfo() const;
    const std::deque<int32_t>& Threads() const;
    const std::deque<int64_t>& DiskWrites() const;
    const std::deque<int64_t>& DiskReads() const;
    const std::deque<uint64_t>& CpuTimes() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        durs_.clear();
        processID_.clear();
        processName_.clear();
        parentProcessID_.clear();
        uid_.clear();
        userName_.clear();
        cpuUsage_.clear();
        pssInfo_.clear();
        threads_.clear();
        diskWrites_.clear();
        diskReads_.clear();
    }

private:
    std::deque<uint64_t> durs_ = {};
    std::deque<int32_t> processID_ = {};
    std::deque<std::string> processName_ = {};
    std::deque<int32_t> parentProcessID_ = {};
    std::deque<int32_t> uid_ = {};
    std::deque<std::string> userName_ = {};
    std::deque<double> cpuUsage_ = {};
    std::deque<int32_t> pssInfo_ = {};
    std::deque<int32_t> threads_ = {};
    std::deque<int64_t> diskWrites_ = {};
    std::deque<int64_t> diskReads_ = {};
    std::deque<uint64_t> cpuTimes_ = {};
};
class CpuUsageDetailData : public CacheBase {
public:
    size_t AppendNewData(uint64_t newTimeStamp,
                         uint64_t dur,
                         double totalLoad,
                         double userLoad,
                         double systemLoad,
                         int64_t threads);
    const std::deque<uint64_t>& Durs() const;
    const std::deque<double>& TotalLoad() const;
    const std::deque<double>& UserLoad() const;
    const std::deque<double>& SystemLoad() const;
    const std::deque<int64_t>& Threads() const;
    virtual void Clear() override
    {
        CacheBase::Clear();
        durs_.clear();
        totalLoad_.clear();
        userLoad_.clear();
        systemLoad_.clear();
        threads_.clear();
    }

private:
    std::deque<uint64_t> durs_ = {};
    std::deque<double> totalLoad_ = {};
    std::deque<double> userLoad_ = {};
    std::deque<double> systemLoad_ = {};
    std::deque<int64_t> threads_ = {};
};
} // namespace TraceStdtype
} // namespace SysTuning

#endif // TRACE_STDTYPE_H

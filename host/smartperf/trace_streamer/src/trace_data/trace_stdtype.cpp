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
#include <algorithm>
#include <ctime>
namespace SysTuning {
namespace TraceStdtype {
TableRowId ThreadState::AppendThreadState(InternalTime ts,
                                          InternalTime dur,
                                          InternalCpu cpu,
                                          TableRowId idTid,
                                          TableRowId idState)
{
    ColumnData& row = rowDatas_.emplace_back();
    row.timeStamp = ts;
    row.duration = dur;
    row.cpu = cpu;
    row.idTid = idTid;
    row.idState = idState;

    return rowDatas_.size() - 1;
}

void ThreadState::SetDuration(TableRowId index, InternalTime dur)
{
    rowDatas_[index].duration = dur;
}

TableRowId ThreadState::UpdateDuration(TableRowId index, InternalTime ts)
{
    if (rowDatas_[index].duration == INVALID_TIME) {
        rowDatas_[index].duration = ts - rowDatas_[index].timeStamp;
    }
    return rowDatas_[index].idTid;
}

void ThreadState::UpdateState(TableRowId index, TableRowId idState)
{
    rowDatas_[index].idState = idState;
}

void ThreadState::UpdateDuration(TableRowId index, InternalTime ts, TableRowId idState)
{
    rowDatas_[index].duration = ts - rowDatas_[index].timeStamp;
    rowDatas_[index].idState = idState;
}

TableRowId ThreadState::UpdateDuration(TableRowId index, InternalTime ts, InternalCpu cpu, TableRowId idState)
{
    rowDatas_[index].cpu = cpu;
    rowDatas_[index].duration = ts - rowDatas_[index].timeStamp;
    rowDatas_[index].idState = idState;
    return rowDatas_[index].idTid;
}

size_t SchedSlice::AppendSchedSlice(uint64_t ts,
                                    uint64_t dur,
                                    uint64_t cpu,
                                    uint64_t internalTid,
                                    uint64_t endState,
                                    uint64_t priority)
{
    timeStamps_.emplace_back(ts);
    durs_.emplace_back(dur);
    cpus_.emplace_back(cpu);
    internalTids_.emplace_back(internalTid);
    endStates_.emplace_back(endState);
    priority_.emplace_back(priority);
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

size_t CallStack::AppendInternalAsyncSlice(uint64_t startT,
                                           uint64_t durationNs,
                                           InternalTid internalTid,
                                           DataIndex cat,
                                           DataIndex name,
                                           uint8_t depth,
                                           uint64_t cookid,
                                           const std::optional<uint64_t>& parentId)
{
    AppendCommonInfo(startT, durationNs, internalTid);
    AppendCallStack(cat, name, depth, parentId);
    AppendDistributeInfo();
    cookies_.emplace_back(cookid);
    ids_.emplace_back(ids_.size());
    return Size() - 1;
}
size_t CallStack::AppendInternalSlice(uint64_t startT,
                                      uint64_t durationNs,
                                      InternalTid internalTid,
                                      DataIndex cat,
                                      DataIndex name,
                                      uint8_t depth,
                                      const std::optional<uint64_t>& parentId)
{
    AppendCommonInfo(startT, durationNs, internalTid);
    AppendCallStack(cat, name, depth, parentId);
    ids_.emplace_back(ids_.size());
    cookies_.emplace_back(INVALID_UINT64);
    return Size() - 1;
}

void CallStack::AppendCommonInfo(uint64_t startT, uint64_t durationNs, InternalTid internalTid)
{
    timeStamps_.emplace_back(startT);
    durs_.emplace_back(durationNs);
    callIds_.emplace_back(internalTid);
}
void CallStack::AppendCallStack(DataIndex cat, DataIndex name, uint8_t depth, std::optional<uint64_t> parentId)
{
    parentIds_.emplace_back(parentId);
    cats_.emplace_back(cat);
    names_.emplace_back(name);
    depths_.emplace_back(depth);
}
void CallStack::AppendDistributeInfo(const std::string& chainId,
                                     const std::string& spanId,
                                     const std::string& parentSpanId,
                                     const std::string& flag,
                                     const std::string& args)
{
    chainIds_.emplace_back(chainId);
    spanIds_.emplace_back(spanId);
    parentSpanIds_.emplace_back(parentSpanId);
    flags_.emplace_back(flag);
    args_.emplace_back(args);
    argSet_.emplace_back(INVALID_UINT32);
}
void CallStack::AppendDistributeInfo()
{
    chainIds_.emplace_back("");
    spanIds_.emplace_back("");
    parentSpanIds_.emplace_back("");
    flags_.emplace_back("");
    args_.emplace_back("");
    argSet_.emplace_back(INVALID_UINT32);
}
void CallStack::AppendArgSet(uint32_t argSetId)
{
    chainIds_.emplace_back("");
    spanIds_.emplace_back("");
    parentSpanIds_.emplace_back("");
    flags_.emplace_back("");
    args_.emplace_back("");
    argSet_.emplace_back(argSetId);
}
void CallStack::SetDuration(size_t index, uint64_t timestamp)
{
    durs_[index] = timestamp - timeStamps_[index];
}

void CallStack::SetDurationAndArg(size_t index, uint64_t timestamp, uint32_t argSetId)
{
    SetTimeStamp(index, timestamp);
    argSet_[index] = argSetId;
}
void CallStack::SetTimeStamp(size_t index, uint64_t timestamp)
{
    timeStamps_[index] = timestamp;
}

const std::deque<std::optional<uint64_t>>& CallStack::ParentIdData() const
{
    return parentIds_;
}
const std::deque<DataIndex>& CallStack::CatsData() const
{
    return cats_;
}
const std::deque<DataIndex>& CallStack::NamesData() const
{
    return names_;
}
const std::deque<uint8_t>& CallStack::Depths() const
{
    return depths_;
}
const std::deque<uint64_t>& CallStack::Cookies() const
{
    return cookies_;
}
const std::deque<uint64_t>& CallStack::CallIds() const
{
    return callIds_;
}
const std::deque<std::string>& CallStack::ChainIds() const
{
    return chainIds_;
}
const std::deque<std::string>& CallStack::SpanIds() const
{
    return spanIds_;
}
const std::deque<std::string>& CallStack::ParentSpanIds() const
{
    return parentSpanIds_;
}
const std::deque<std::string>& CallStack::Flags() const
{
    return flags_;
}
const std::deque<std::string>& CallStack::ArgsData() const
{
    return args_;
}
const std::deque<uint32_t>& CallStack::ArgSetIdsData() const
{
    return argSet_;
}

size_t ArgSet::AppendNewArg(DataIndex nameId, BaseDataType dataType, int64_t value, size_t argSet)
{
    dataTypes_.emplace_back(dataType);
    argset_.emplace_back(argSet);
    ids_.emplace_back(Size());
    values_.emplace_back(value);
    names_.emplace_back(nameId);
    return Size() - 1;
}
const std::deque<BaseDataType>& ArgSet::DataTypes() const
{
    return dataTypes_;
}
const std::deque<int64_t>& ArgSet::ValuesData() const
{
    return values_;
}
const std::deque<uint64_t>& ArgSet::ArgsData() const
{
    return argset_;
}
const std::deque<DataIndex>& ArgSet::NamesData() const
{
    return names_;
}

size_t SysMeasureFilter::AppendNewFilter(uint64_t filterId, DataIndex type, DataIndex nameId)
{
    ids_.emplace_back(filterId);
    names_.emplace_back(nameId);
    types_.emplace_back(type);
    return ids_.size() - 1;
}
const std::deque<DataIndex>& SysMeasureFilter::NamesData() const
{
    return names_;
}

const std::deque<DataIndex>& SysMeasureFilter::TypesData() const
{
    return types_;
}
size_t DataType::AppendNewDataType(BaseDataType dataType, DataIndex dataDescIndex)
{
    ids_.emplace_back(Size());
    dataTypes_.emplace_back(dataType);
    descs_.emplace_back(dataDescIndex);
    return Size() - 1;
}

const std::deque<BaseDataType>& DataType::DataTypes() const
{
    return dataTypes_;
}
const std::deque<DataIndex>& DataType::DataDesc() const
{
    return descs_;
}
size_t Filter::AppendNewFilterData(std::string type, std::string name, uint64_t sourceArgSetId)
{
    nameDeque_.emplace_back(name);
    sourceArgSetId_.emplace_back(sourceArgSetId);
    ids_.emplace_back(Size());
    typeDeque_.emplace_back(type);
    return Size() - 1;
}

size_t Measure::AppendMeasureData(uint32_t type, uint64_t timestamp, int64_t value, uint32_t filterId)
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
    itidDeque_.emplace_back(internalTid);
    return Size() - 1;
}

size_t ThreadMeasureFilter::AppendNewFilter(uint64_t filterId, uint32_t nameIndex, uint64_t internalTid)
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
size_t LogInfo::AppendNewLogInfo(uint64_t seq,
                                 uint64_t timestamp,
                                 uint32_t pid,
                                 uint32_t tid,
                                 DataIndex level,
                                 DataIndex tag,
                                 DataIndex context,
                                 uint64_t originTs)
{
    hilogLineSeqs_.emplace_back(seq);
    timeStamps_.emplace_back(timestamp);
    pids_.emplace_back(pid);
    tids_.emplace_back(tid);
    levels_.emplace_back(level);
    tags_.emplace_back(tag);
    contexts_.emplace_back(context);
    originTs_.emplace_back(originTs);
    return Size() - 1;
}
const std::deque<uint64_t>& LogInfo::HilogLineSeqs() const
{
    return hilogLineSeqs_;
}
const std::deque<uint32_t>& LogInfo::Pids() const
{
    return pids_;
}
const std::deque<uint32_t>& LogInfo::Tids() const
{
    return tids_;
}
const std::deque<DataIndex>& LogInfo::Levels() const
{
    return levels_;
}
const std::deque<DataIndex>& LogInfo::Tags() const
{
    return tags_;
}
const std::deque<DataIndex>& LogInfo::Contexts() const
{
    return contexts_;
}
const std::deque<uint64_t>& LogInfo::OriginTimeStamData() const
{
    return originTs_;
}

size_t NativeHook::AppendNewNativeHookData(uint64_t eventId,
                                           uint32_t ipid,
                                           uint32_t itid,
                                           std::string eventType,
                                           DataIndex subType,
                                           uint64_t timestamp,
                                           uint64_t endTimestamp,
                                           uint64_t duration,
                                           uint64_t addr,
                                           int64_t memSize,
                                           int64_t curMemSize)
{
    eventIds_.emplace_back(eventId);
    ipids_.emplace_back(ipid);
    itids_.emplace_back(itid);
    eventTypes_.emplace_back(eventType);
    subTypes_.emplace_back(subType);
    timeStamps_.emplace_back(timestamp);
    endTimestamps_.emplace_back(endTimestamp);
    durations_.emplace_back(duration);
    addrs_.emplace_back(addr);
    memSizes_.emplace_back(memSize);
    if (eventType == ALLOC_EVET || eventType == FREE_EVENT) {
        countHeapSizes_ += curMemSize;
        allMemSizes_.emplace_back(countHeapSizes_);
    } else if (eventType == MMAP_EVENT || eventType == MUNMAP_EVENT) {
        countMmapSizes_ += curMemSize;
        allMemSizes_.emplace_back(countMmapSizes_);
    }
    currentSizeDurs_.emplace_back(0);
    return Size() - 1;
}
void NativeHook::UpdateHeapDuration(size_t row, uint64_t endTimestamp)
{
    endTimestamps_[row] = endTimestamp;
    durations_[row] = endTimestamp - timeStamps_[row];
}
void NativeHook::UpdateCurrentSizeDur(size_t row, uint64_t timeStamp)
{
    currentSizeDurs_[row] = timeStamp - timeStamps_[row];
}
const std::deque<uint64_t>& NativeHook::EventIds() const
{
    return eventIds_;
}
const std::deque<uint32_t>& NativeHook::Ipids() const
{
    return ipids_;
}
const std::deque<uint32_t>& NativeHook::Itids() const
{
    return itids_;
}
const std::deque<std::string>& NativeHook::EventTypes() const
{
    return eventTypes_;
}
const std::deque<DataIndex>& NativeHook::SubTypes() const
{
    return subTypes_;
}
const std::deque<uint64_t>& NativeHook::EndTimeStamps() const
{
    return endTimestamps_;
}
const std::deque<uint64_t>& NativeHook::Durations() const
{
    return durations_;
}
const std::deque<uint64_t>& NativeHook::Addrs() const
{
    return addrs_;
}
const std::deque<int64_t>& NativeHook::MemSizes() const
{
    return memSizes_;
}
const std::deque<int64_t>& NativeHook::AllMemSizes() const
{
    return allMemSizes_;
}
const std::deque<uint64_t>& NativeHook::CurrentSizeDurs() const
{
    return currentSizeDurs_;
}

size_t NativeHookFrame::AppendNewNativeHookFrame(uint64_t eventId,
                                                 uint64_t depth,
                                                 uint64_t ip,
                                                 uint64_t sp,
                                                 DataIndex symbolName,
                                                 DataIndex filePath,
                                                 uint64_t offset,
                                                 uint64_t symbolOffset)
{
    eventIds_.emplace_back(eventId);
    depths_.emplace_back(depth);
    ips_.emplace_back(ip);
    sps_.emplace_back(sp);
    symbolNames_.emplace_back(symbolName);
    filePaths_.emplace_back(filePath);
    offsets_.emplace_back(offset);
    symbolOffsets_.emplace_back(symbolOffset);
    return Size() - 1;
}
const std::deque<uint64_t>& NativeHookFrame::EventIds() const
{
    return eventIds_;
}
const std::deque<uint64_t>& NativeHookFrame::Depths() const
{
    return depths_;
}
const std::deque<uint64_t>& NativeHookFrame::Ips() const
{
    return ips_;
}
const std::deque<uint64_t>& NativeHookFrame::Sps() const
{
    return sps_;
}
const std::deque<DataIndex>& NativeHookFrame::SymbolNames() const
{
    return symbolNames_;
}
const std::deque<DataIndex>& NativeHookFrame::FilePaths() const
{
    return filePaths_;
}
const std::deque<uint64_t>& NativeHookFrame::Offsets() const
{
    return offsets_;
}
const std::deque<uint64_t>& NativeHookFrame::SymbolOffsets() const
{
    return symbolOffsets_;
}

size_t Hidump::AppendNewHidumpInfo(uint64_t timestamp, uint32_t fps)
{
    timeStamps_.emplace_back(timestamp);
    fpss_.emplace_back(fps);
    return Size() - 1;
}
const std::deque<uint32_t>& Hidump::Fpss() const
{
    return fpss_;
}

size_t PerfCallChain::AppendNewPerfCallChain(uint64_t sampleId,
                                             uint64_t callchainId,
                                             uint64_t vaddrInFile,
                                             uint64_t fileId,
                                             uint64_t symbolId)
{
    ids_.emplace_back(Size());
    sampleIds_.emplace_back(sampleId);
    callchainIds_.emplace_back(callchainId);
    vaddrInFiles_.emplace_back(vaddrInFile);
    fileIds_.emplace_back(fileId);
    symbolIds_.emplace_back(symbolId);
    names_.emplace_back("");
    return Size() - 1;
}
const std::deque<uint64_t>& PerfCallChain::SampleIds() const
{
    return sampleIds_;
}
const std::deque<uint64_t>& PerfCallChain::CallchainIds() const
{
    return callchainIds_;
}
const std::deque<uint64_t>& PerfCallChain::VaddrInFiles() const
{
    return vaddrInFiles_;
}
const std::deque<uint64_t>& PerfCallChain::FileIds() const
{
    return fileIds_;
}
const std::deque<uint64_t>& PerfCallChain::SymbolIds() const
{
    return symbolIds_;
}

const std::deque<std::string>& PerfCallChain::Names() const
{
    return names_;
}
void PerfCallChain::SetName(uint64_t index, const std::string& name)
{
    names_[index] = name;
}
size_t PerfFiles::AppendNewPerfFiles(uint64_t fileIds, uint32_t serial, DataIndex symbols, DataIndex filePath)
{
    ids_.emplace_back(Size());
    fileIds_.emplace_back(fileIds);
    serials_.emplace_back(serial);
    symbols_.emplace_back(symbols);
    filePaths_.emplace_back(filePath);
    return Size() - 1;
}
const std::deque<uint64_t>& PerfFiles::FileIds() const
{
    return fileIds_;
}

const std::deque<uint32_t>& PerfFiles::Serials() const
{
    return serials_;
}
const std::deque<DataIndex>& PerfFiles::Symbols() const
{
    return symbols_;
}
const std::deque<DataIndex>& PerfFiles::FilePaths() const
{
    return filePaths_;
}

size_t PerfSample::AppendNewPerfSample(uint64_t sampleId,
                                       uint64_t timestamp,
                                       uint64_t tid,
                                       uint64_t eventCount,
                                       uint64_t eventTypeId,
                                       uint64_t timestampTrace,
                                       uint64_t cpuId,
                                       uint64_t threadState)
{
    ids_.emplace_back(Size());
    sampleIds_.emplace_back(sampleId);
    timeStamps_.emplace_back(timestamp);
    tids_.emplace_back(tid);
    eventCounts_.emplace_back(eventCount);
    eventTypeIds_.emplace_back(eventTypeId);
    timestampTraces_.emplace_back(timestampTrace);
    cpuIds_.emplace_back(cpuId);
    threadStates_.emplace_back(threadState);
    return Size() - 1;
}
const std::deque<uint64_t>& PerfSample::SampleIds() const
{
    return sampleIds_;
}
const std::deque<uint64_t>& PerfSample::Tids() const
{
    return tids_;
}
const std::deque<uint64_t>& PerfSample::EventCounts() const
{
    return eventCounts_;
}
const std::deque<uint64_t>& PerfSample::EventTypeIds() const
{
    return eventTypeIds_;
}
const std::deque<uint64_t>& PerfSample::TimestampTraces() const
{
    return timestampTraces_;
}
const std::deque<uint64_t>& PerfSample::CpuIds() const
{
    return cpuIds_;
}
const std::deque<DataIndex>& PerfSample::ThreadStates() const
{
    return threadStates_;
}

size_t PerfThread::AppendNewPerfThread(uint64_t pid, uint64_t tid, DataIndex threadName)
{
    ids_.emplace_back(Size());
    pids_.emplace_back(pid);
    tids_.emplace_back(tid);
    threadNames_.emplace_back(threadName);
    return Size() - 1;
}
const std::deque<uint64_t>& PerfThread::Pids() const
{
    return pids_;
}
const std::deque<uint64_t>& PerfThread::Tids() const
{
    return tids_;
}
const std::deque<DataIndex>& PerfThread::ThreadNames() const
{
    return threadNames_;
}
size_t PerfReport::AppendNewPerfReport(DataIndex type, DataIndex value)
{
    ids_.emplace_back(Size());
    types_.emplace_back(type);
    values_.emplace_back(value);
    return Size() - 1;
}
const std::deque<DataIndex>& PerfReport::Types() const
{
    return types_;
}
const std::deque<DataIndex>& PerfReport::Values() const
{
    return values_;
}
size_t ProcessMeasureFilter::AppendNewFilter(uint64_t id, DataIndex name, uint32_t internalPid)
{
    internalPids_.emplace_back(internalPid);
    ids_.emplace_back(id);
    names_.emplace_back(name);
    return Size() - 1;
}
size_t ClockEventData::AppendNewFilter(uint64_t id, DataIndex type, DataIndex name, uint64_t cpu)
{
    cpus_.emplace_back(cpu);
    ids_.emplace_back(id);
    types_.emplace_back(type);
    names_.emplace_back(name);
    return Size() - 1;
}
size_t ClkEventData::AppendNewFilter(uint64_t id, uint64_t rate, DataIndex name, uint64_t cpu)
{
    ids_.emplace_back(id);
    rates_.emplace_back(rate);
    names_.emplace_back(name);
    cpus_.emplace_back(cpu);
    return Size() - 1;
}
size_t SysCall::AppendSysCallData(int64_t sysCallNum, DataIndex type, uint64_t ipid, uint64_t timestamp, int64_t ret)
{
    sysCallNums_.emplace_back(sysCallNum);
    types_.emplace_back(type);
    ipids_.emplace_back(ipid);
    timeStamps_.emplace_back(timestamp);
    rets_.emplace_back(ret);
    return Size() - 1;
}
StatAndInfo::StatAndInfo()
{
    // sched_switch_received | sched_switch_not_match | sched_switch_not_not_supported etc.
    for (int i = TRACE_EVENT_START; i < TRACE_EVENT_MAX; i++) {
        event_[i] = config_.eventNameMap_.at(static_cast<SupportedTraceEventType>(i));
    }
    for (int j = STAT_EVENT_START; j < STAT_EVENT_MAX; j++) {
        stat_[j] = config_.eventErrorDescMap_.at(static_cast<StatType>(j));
    }

    for (int i = TRACE_EVENT_START; i < TRACE_EVENT_MAX; i++) {
        for (int j = STAT_EVENT_START; j < STAT_EVENT_MAX; j++) {
            statSeverity_[i][j] = config_.eventParserStatSeverityDescMap_.at(static_cast<SupportedTraceEventType>(i))
                                      .at(static_cast<StatType>(j));
        }
    }

    for (int i = TRACE_EVENT_START; i < TRACE_EVENT_MAX; i++) {
        for (int j = STAT_EVENT_START; j < STAT_EVENT_MAX; j++) {
            statSeverityDesc_[i][j] = config_.serverityLevelDescMap_.at(statSeverity_[i][j]);
        }
    }

    for (int i = TRACE_EVENT_START; i < TRACE_EVENT_MAX; i++) {
        for (int j = STAT_EVENT_START; j < STAT_EVENT_MAX; j++) {
            statCount_[i][j] = 0;
        }
    }
}
void StatAndInfo::IncreaseStat(SupportedTraceEventType eventType, StatType type)
{
    statCount_[eventType][type]++;
}
const uint32_t& StatAndInfo::GetValue(SupportedTraceEventType eventType, StatType type) const
{
    return statCount_[eventType][type];
}
const std::string& StatAndInfo::GetEvent(SupportedTraceEventType eventType) const
{
    return event_[eventType];
}
const std::string& StatAndInfo::GetStat(StatType type) const
{
    return stat_[type];
}
const std::string& StatAndInfo::GetSeverityDesc(SupportedTraceEventType eventType, StatType type) const
{
    return statSeverityDesc_[eventType][type];
}
const StatSeverityLevel& StatAndInfo::GetSeverity(SupportedTraceEventType eventType, StatType type) const
{
    return statSeverity_[eventType][type];
}
uint64_t SymbolsData::Size() const
{
    return addrs_.size();
}
void SymbolsData::InsertSymbol(const DataIndex& name, const uint64_t& addr)
{
    addrs_.emplace_back(addr);
    funcName_.emplace_back(name);
}
const std::deque<DataIndex>& SymbolsData::GetConstFuncNames() const
{
    return funcName_;
}
const std::deque<uint64_t>& SymbolsData::GetConstAddrs() const
{
    return addrs_;
}
MetaData::MetaData()
{
    columnNames_.resize(METADATA_ITEM_MAX);
    values_.resize(METADATA_ITEM_MAX);
    columnNames_[METADATA_ITEM_DATASIZE] = METADATA_ITEM_DATASIZE_COLNAME;
    columnNames_[METADATA_ITEM_PARSETOOL_NAME] = METADATA_ITEM_PARSETOOL_NAME_COLNAME;
    columnNames_[METADATA_ITEM_PARSERTOOL_VERSION] = METADATA_ITEM_PARSERTOOL_VERSION_COLNAME;
    columnNames_[METADATA_ITEM_PARSERTOOL_PUBLISH_DATETIME] = METADATA_ITEM_PARSERTOOL_PUBLISH_DATETIME_COLNAME;
    columnNames_[METADATA_ITEM_SOURCE_FILENAME] = METADATA_ITEM_SOURCE_FILENAME_COLNAME;
    columnNames_[METADATA_ITEM_OUTPUT_FILENAME] = METADATA_ITEM_OUTPUT_FILENAME_COLNAME;
    columnNames_[METADATA_ITEM_PARSERTIME] = METADATA_ITEM_PARSERTIME_COLNAME;
    columnNames_[METADATA_ITEM_TRACE_DURATION] = METADATA_ITEM_TRACE_DURATION_COLNAME;
    columnNames_[METADATA_ITEM_SOURCE_DATETYPE] = METADATA_ITEM_SOURCE_DATETYPE_COLNAME;
    values_[METADATA_ITEM_PARSETOOL_NAME] = "trace_streamer";
}
void MetaData::SetTraceType(const std::string& traceType)
{
    values_[METADATA_ITEM_SOURCE_DATETYPE] = traceType;
}
void MetaData::SetSourceFileName(const std::string& fileName)
{
    MetaData::values_[METADATA_ITEM_SOURCE_FILENAME] = fileName;
}
void MetaData::SetOutputFileName(const std::string& fileName)
{
    MetaData::values_[METADATA_ITEM_OUTPUT_FILENAME] = fileName;
}
void MetaData::SetParserToolVersion(const std::string& version)
{
    values_[METADATA_ITEM_PARSERTOOL_VERSION] = version;
}
void MetaData::SetParserToolPublishDateTime(const std::string& datetime)
{
    values_[METADATA_ITEM_PARSERTOOL_PUBLISH_DATETIME] = datetime;
}
void MetaData::SetTraceDataSize(uint64_t dataSize)
{
    std::stringstream ss;
    ss << dataSize;
    values_[METADATA_ITEM_DATASIZE] = ss.str();
    // 	Function 'time' may return error. It is not allowed to do anything that might fail inside the constructor.
    time_t rawtime;
    struct tm* timeinfo = nullptr;
    void(time(&rawtime));
    timeinfo = localtime(&rawtime);
    values_[METADATA_ITEM_PARSERTIME] = asctime(timeinfo);
    // sometimes there will be a extra \n at last
    values_[METADATA_ITEM_PARSERTIME].pop_back();
}
void MetaData::SetTraceDuration(uint64_t dur)
{
    values_[METADATA_ITEM_TRACE_DURATION] = std::to_string(dur) + " s";
}
const std::string& MetaData::Value(uint64_t row) const
{
    return values_[row];
}
const std::string& MetaData::Name(uint64_t row) const
{
    return columnNames_[row];
}
DataIndex DataDict::GetStringIndex(std::string_view str)
{
    auto hashValue = hashFun(str);
    auto itor = dataDictInnerMap_.find(hashValue);
    if (itor != dataDictInnerMap_.end()) {
        return itor->second;
    }
    dataDict_.emplace_back(std::string(str));
    DataIndex stringIdentity = dataDict_.size() - 1;
    dataDictInnerMap_.emplace(hashValue, stringIdentity);
    return stringIdentity;
}
size_t CpuUsageDetailData::AppendNewData(uint64_t newTimeStamp,
                                         uint64_t dur,
                                         double totalLoad,
                                         double userLoad,
                                         double systemLoad,
                                         int64_t threads)
{
    timeStamps_.emplace_back(newTimeStamp);
    durs_.emplace_back(dur);
    totalLoad_.emplace_back(totalLoad);
    userLoad_.emplace_back(userLoad);
    systemLoad_.emplace_back(systemLoad);
    threads_.emplace_back(threads);
    return Size() - 1;
}
const std::deque<uint64_t>& CpuUsageDetailData::Durs() const
{
    return durs_;
}
const std::deque<double>& CpuUsageDetailData::TotalLoad() const
{
    return totalLoad_;
}
const std::deque<double>& CpuUsageDetailData::UserLoad() const
{
    return userLoad_;
}
const std::deque<double>& CpuUsageDetailData::SystemLoad() const
{
    return systemLoad_;
}
const std::deque<int64_t>& CpuUsageDetailData::Threads() const
{
    return threads_;
}
size_t LiveProcessDetailData::AppendNewData(uint64_t newTimeStamp,
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
                                            int64_t diskReads)
{
    timeStamps_.emplace_back(newTimeStamp);
    durs_.emplace_back(dur);
    processID_.emplace_back(processID);
    processName_.emplace_back(processName);
    parentProcessID_.emplace_back(parentProcessID);
    uid_.emplace_back(uid);
    userName_.emplace_back(userName);
    cpuUsage_.emplace_back(cpuUsage);
    pssInfo_.emplace_back(pssInfo);
    threads_.emplace_back(threads);
    diskWrites_.emplace_back(diskWrites);
    diskReads_.emplace_back(diskReads);
    cpuTimes_.emplace_back(cpuTime);
    return Size() - 1;
}
const std::deque<uint64_t>& LiveProcessDetailData::Durs() const
{
    return durs_;
}
const std::deque<int32_t>& LiveProcessDetailData::ProcessID() const
{
    return processID_;
}
const std::deque<std::string>& LiveProcessDetailData::ProcessName() const
{
    return processName_;
}
const std::deque<int32_t>& LiveProcessDetailData::ParentProcessID() const
{
    return parentProcessID_;
}
const std::deque<int32_t>& LiveProcessDetailData::Uid() const
{
    return uid_;
}
const std::deque<std::string>& LiveProcessDetailData::UserName() const
{
    return userName_;
}
const std::deque<double>& LiveProcessDetailData::CpuUsage() const
{
    return cpuUsage_;
}
const std::deque<int32_t>& LiveProcessDetailData::PssInfo() const
{
    return pssInfo_;
}
const std::deque<int32_t>& LiveProcessDetailData::Threads() const
{
    return threads_;
}
const std::deque<int64_t>& LiveProcessDetailData::DiskWrites() const
{
    return diskWrites_;
}
const std::deque<int64_t>& LiveProcessDetailData::DiskReads() const
{
    return diskReads_;
}

const std::deque<uint64_t>& LiveProcessDetailData::CpuTimes() const
{
    return cpuTimes_;
}

size_t NetDetailData::AppendNewNetData(uint64_t newTimeStamp,
                                       uint64_t tx,
                                       uint64_t rx,
                                       uint64_t dur,
                                       double rxSpeed,
                                       double txSpeed,
                                       uint64_t packetIn,
                                       double packetInSec,
                                       uint64_t packetOut,
                                       double packetOutSec,
                                       const std::string& netType)
{
    timeStamps_.emplace_back(newTimeStamp);
    txs_.emplace_back(tx);
    rxs_.emplace_back(rx);
    durs_.emplace_back(dur);
    txSpeeds_.emplace_back(txSpeed);
    rxSpeeds_.emplace_back(rxSpeed);
    netTypes_.emplace_back(netType);
    packetIn_.emplace_back(packetIn);
    packetInSec_.emplace_back(packetInSec);
    packetOut_.emplace_back(packetOut);
    packetOutSec_.emplace_back(packetOutSec);

    return Size() - 1;
}
const std::deque<uint64_t>& NetDetailData::Durs() const
{
    return durs_;
}
const std::deque<double>& NetDetailData::RxSpeed() const
{
    return rxSpeeds_;
}
const std::deque<double>& NetDetailData::TxSpeed() const
{
    return txSpeeds_;
}
const std::deque<std::string>& NetDetailData::NetTypes() const
{
    return netTypes_;
}
const std::deque<uint64_t>& NetDetailData::RxDatas() const
{
    return rxs_;
}
const std::deque<uint64_t>& NetDetailData::TxDatas() const
{
    return txs_;
}
const std::deque<uint64_t>& NetDetailData::PacketIn() const
{
    return packetIn_;
}
const std::deque<double>& NetDetailData::PacketInSec() const
{
    return packetInSec_;
}
const std::deque<uint64_t>& NetDetailData::PacketOut() const
{
    return packetOut_;
}
const std::deque<double>& NetDetailData::PacketOutSec() const
{
    return packetOutSec_;
}

void DiskIOData::AppendNewData(uint64_t ts,
                               uint64_t dur,
                               uint64_t rd,
                               uint64_t wr,
                               uint64_t rdPerSec,
                               uint64_t wrPerSec,
                               double rdCountPerSec,
                               double wrCountPerSec,
                               uint64_t rdCount,
                               uint64_t wrCount)
{
    timeStamps_.emplace_back(ts);
    durs_.emplace_back(dur);
    rdDatas_.emplace_back(rd);
    wrDatas_.emplace_back(wr);
    rdPerSec_.emplace_back(rdPerSec);
    wrPerSec_.emplace_back(wrPerSec);
    rdCountPerSec_.emplace_back(rdCountPerSec);
    wrCountPerSec_.emplace_back(wrCountPerSec);
    rdCountDatas_.emplace_back(rdCount);
    wrCountDatas_.emplace_back(wrCount);
}
const std::deque<uint64_t>& DiskIOData::Durs() const
{
    return durs_;
}
const std::deque<uint64_t>& DiskIOData::RdDatas() const
{
    return rdDatas_;
}
const std::deque<uint64_t>& DiskIOData::WrDatas() const
{
    return wrDatas_;
}
const std::deque<double>& DiskIOData::RdSpeedDatas() const
{
    return rdPerSec_;
}
const std::deque<double>& DiskIOData::WrSpeedDatas() const
{
    return wrPerSec_;
}

const std::deque<double>& DiskIOData::RdCountPerSecDatas() const
{
    return rdCountPerSec_;
}
const std::deque<double>& DiskIOData::WrCountPerSecDatas() const
{
    return wrCountPerSec_;
}
const std::deque<uint64_t>& DiskIOData::RdCountDatas() const
{
    return rdCountDatas_;
}
const std::deque<uint64_t>& DiskIOData::WrCountDatas() const
{
    return wrCountDatas_;
}
} // namespace TraceStdtype
} // namespace SysTuning

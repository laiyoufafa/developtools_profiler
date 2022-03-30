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
#include <ctime>
namespace SysTuning {
namespace TraceStdtype {
size_t ThreadState::AppendThreadState(uint64_t ts, uint64_t dur, uint64_t cpu, uint64_t internalTid, uint64_t state)
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
    if (durs_[index] == INVALID_UINT64) {
        durs_[index] = timestamp - timeStamps_[index];
    }
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

size_t HeapInfo::AppendNewHeapInfo(uint64_t eventId,
                                   uint32_t ipid,
                                   uint32_t itid,
                                   DataIndex eventType,
                                   uint64_t timestamp,
                                   uint64_t endTimestamp,
                                   uint64_t duration,
                                   uint64_t addr,
                                   int64_t heapSize,
                                   int64_t allHeapSize,
                                   uint64_t currentSizeDur)
{
    eventIds_.emplace_back(eventId);
    ipids_.emplace_back(ipid);
    itids_.emplace_back(itid);
    eventTypes_.emplace_back(eventType);
    timeStamps_.emplace_back(timestamp);
    endTimestamps_.emplace_back(endTimestamp);
    durations_.emplace_back(duration);
    addrs_.emplace_back(addr);
    heapSizes_.emplace_back(heapSize);
    countHeapSizes_ += allHeapSize;
    allHeapSizes_.emplace_back(countHeapSizes_);
    currentSizeDurs_.emplace_back(currentSizeDur);
    return Size() - 1;
}
void HeapInfo::UpdateHeapDuration(size_t row, uint64_t endTimestamp)
{
    endTimestamps_[row] = endTimestamp;
    durations_[row] = endTimestamp - timeStamps_[row];
}
void HeapInfo::UpdateCurrentSizeDur(size_t row, uint64_t nextStartTime)
{
    if (row == 0) {
        return;
    }
    if (nextStartTime > timeStamps_[--row]) {
        currentSizeDurs_[row] = nextStartTime - timeStamps_[row];
    }
}
const std::deque<uint64_t>& HeapInfo::EventIds() const
{
    return eventIds_;
}
const std::deque<uint32_t>& HeapInfo::Ipids() const
{
    return ipids_;
}
const std::deque<uint32_t>& HeapInfo::Itids() const
{
    return itids_;
}
const std::deque<DataIndex>& HeapInfo::EventTypes() const
{
    return eventTypes_;
}
const std::deque<uint64_t>& HeapInfo::EndTimeStamps() const
{
    return endTimestamps_;
}
const std::deque<uint64_t>& HeapInfo::Durations() const
{
    return durations_;
}
const std::deque<uint64_t>& HeapInfo::Addrs() const
{
    return addrs_;
}
const std::deque<int64_t>& HeapInfo::HeapSizes() const
{
    return heapSizes_;
}
const std::deque<int64_t>& HeapInfo::AllHeapSizes() const
{
    return allHeapSizes_;
}
const std::deque<uint64_t>& HeapInfo::CurrentSizeDurs() const
{
    return currentSizeDurs_;
}

size_t HeapFrameInfo::AppendNewHeapFrameInfo(uint64_t eventId,
                                             uint64_t depth,
                                             DataIndex ip,
                                             DataIndex sp,
                                             DataIndex symbolName,
                                             DataIndex filePath,
                                             DataIndex offset,
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
const std::deque<uint64_t>& HeapFrameInfo::EventIds() const
{
    return eventIds_;
}
const std::deque<uint64_t>& HeapFrameInfo::Depths() const
{
    return depths_;
}
const std::deque<uint64_t>& HeapFrameInfo::Ips() const
{
    return ips_;
}
const std::deque<uint64_t>& HeapFrameInfo::Sps() const
{
    return sps_;
}
const std::deque<DataIndex>& HeapFrameInfo::SymbolNames() const
{
    return symbolNames_;
}
const std::deque<DataIndex>& HeapFrameInfo::FilePaths() const
{
    return filePaths_;
}
const std::deque<uint64_t>& HeapFrameInfo::Offsets() const
{
    return offsets_;
}
const std::deque<uint64_t>& HeapFrameInfo::SymbolOffsets() const
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
        TS_ASSERT(std::string_view(dataDict_[itor->second]) == str);
        return itor->second;
    }
    dataDict_.emplace_back(std::string(str));
    DataIndex stringIdentity = dataDict_.size() - 1;
    dataDictInnerMap_.emplace(hashValue, stringIdentity);
    return stringIdentity;
}
} // namespace TraceStdtype
} // namespace SysTuning

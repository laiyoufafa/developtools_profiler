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
#include "htrace_mem_parser.h"
#include "clock_filter.h"
#include "htrace_event_parser.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "stat_filter.h"
#include "symbols_filter.h"
#include "system_event_measure_filter.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceMemParser::HtraceMemParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : streamFilters_(ctx), traceDataCache_(dataCache)
{
    if (!traceDataCache_) {
        TS_LOGE("traceDataCache_ should not be null");
        return;
    }
    if (!streamFilters_) {
        TS_LOGE("streamFilters_ should not be null");
        return;
    }
    for (auto i = 0; i < MEM_MAX; i++) {
        memNameDictMap_.insert(
            std::make_pair(static_cast<MemInfoType>(i),
                           traceDataCache_->GetDataIndex(config_.memNameMap_.at(static_cast<MemInfoType>(i)))));
    }
    for (auto i = 0; i < SysMeminfoType::MEMINFO_CMA_FREE + 1; i++) {
        sysMemNameDictMap_.insert(
            std::make_pair(static_cast<SysMeminfoType>(i),
                           traceDataCache_->GetDataIndex(config_.sysMemNameMap_.at(static_cast<SysMeminfoType>(i)))));
    }
    for (auto i = 0; i < SysVMeminfoType::VMEMINFO_WORKINGSET_RESTORE + 1; i++) {
        sysVMemNameDictMap_.insert(std::make_pair(
            static_cast<SysVMeminfoType>(i),
            traceDataCache_->GetDataIndex(config_.sysVirtualMemNameMap_.at(static_cast<SysVMeminfoType>(i)))));
    }
}

HtraceMemParser::~HtraceMemParser()
{
    TS_LOGI("mem ts MIN:%llu, MAX:%llu", static_cast<unsigned long long>(traceStartTime_),
            static_cast<unsigned long long>(traceEndTime_));
}
void HtraceMemParser::Parse(const MemoryData& tracePacket, uint64_t timeStamp, BuiltinClocks clock)
{
    if (!traceDataCache_) {
        TS_LOGE("traceDataCache_ should not be null");
        return;
    }
    if (!streamFilters_) {
        TS_LOGE("streamFilters_ should not be null");
        return;
    }
    auto newTimeStamp = streamFilters_->clockFilter_->ToPrimaryTraceTime(clock, timeStamp);
    if (newTimeStamp != timeStamp) { // record the time only when the time is valid
        traceStartTime_ = std::min(traceStartTime_, newTimeStamp);
        traceEndTime_ = std::max(traceEndTime_, newTimeStamp);
    }
    if (tracePacket.processesinfo_size()) {
        ParseProcessInfo(tracePacket, newTimeStamp);
    }
    if (tracePacket.meminfo_size()) {
        ParseMemInfo(tracePacket, newTimeStamp);
    }
    if (tracePacket.vmeminfo_size()) {
        ParseVMemInfo(tracePacket, newTimeStamp);
    }
}
void HtraceMemParser::ParseProcessInfo(const MemoryData& tracePacket, uint64_t timeStamp) const
{
    if (tracePacket.processesinfo_size()) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    }
    for (int i = 0; i < tracePacket.processesinfo_size(); i++) {
        auto memInfo = tracePacket.processesinfo(i);
        auto ipid = streamFilters_->processFilter_->UpdateOrCreateProcessWithName(memInfo.pid(), memInfo.name());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_SIZE), timeStamp,
                                                                    memInfo.vm_size_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_RSS), timeStamp,
                                                                    memInfo.vm_rss_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_ANON), timeStamp,
                                                                    memInfo.rss_anon_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_RSS_FILE), timeStamp,
                                                                    memInfo.rss_file_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_RSS_SHMEM), timeStamp,
                                                                    memInfo.rss_shmem_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_SWAP), timeStamp,
                                                                    memInfo.vm_swap_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_LOCKED), timeStamp,
                                                                    memInfo.vm_locked_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_VM_HWM), timeStamp,
                                                                    memInfo.vm_hwm_kb());
        streamFilters_->processMeasureFilter_->AppendNewMeasureData(ipid, memNameDictMap_.at(MEM_OOM_SCORE_ADJ),
                                                                    timeStamp, memInfo.oom_score_adj());
    }
}

void HtraceMemParser::ParseMemInfoEasy(const MemoryData& tracePacket, uint64_t timeStamp) const
{
    if (tracePacket.meminfo_size()) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_MEMORY, STAT_EVENT_RECEIVED);
    }
    for (int i = 0; i < tracePacket.meminfo_size(); i++) {
        auto memInfo = tracePacket.meminfo(i);
        if (config_.sysMemNameMap_.find(memInfo.key()) != config_.sysMemNameMap_.end()) {
            streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(sysMemNameDictMap_.at(memInfo.key()),
                                                                             timeStamp, memInfo.value());
        } else {
            streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_MEMORY, STAT_EVENT_DATA_INVALID);
        }
    }
}

void HtraceMemParser::ParseVMemInfoEasy(const MemoryData& tracePacket, uint64_t timeStamp) const
{
    traceDataCache_->UpdateTraceTime(timeStamp);
    if (tracePacket.vmeminfo_size()) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_VIRTUAL_MEMORY, STAT_EVENT_RECEIVED);
    }
    for (int i = 0; i < tracePacket.vmeminfo_size(); i++) {
        auto memInfo = tracePacket.vmeminfo(i);
        if (config_.sysVirtualMemNameMap_.find(memInfo.key()) != config_.sysVirtualMemNameMap_.end()) {
            streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(sysVMemNameDictMap_.at(memInfo.key()),
                                                                             timeStamp, memInfo.value());
        } else {
            streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_VIRTUAL_MEMORY, STAT_EVENT_DATA_INVALID);
        }
    }
}

void HtraceMemParser::ParseMemInfo(const MemoryData& tracePacket, uint64_t timeStamp) const
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_MEMORY, STAT_EVENT_RECEIVED);
    for (int i = 0; i < tracePacket.meminfo_size(); i++) {
        auto vMemInfo = tracePacket.meminfo(i);
        switch (static_cast<int32_t>(vMemInfo.key())) {
            case SysMeminfoType::MEMINFO_UNSPECIFIED:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_UNSPECIFIED), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_MEM_TOTAL:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_MEM_TOTAL), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_MEM_FREE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_MEM_FREE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_MEM_AVAILABLE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_MEM_AVAILABLE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_BUFFERS:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_BUFFERS), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_CACHED:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_CACHED), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SWAP_CACHED:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SWAP_CACHED), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_ACTIVE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_ACTIVE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_INACTIVE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_INACTIVE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_ACTIVE_ANON:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_ACTIVE_ANON), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_INACTIVE_ANON:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_INACTIVE_ANON), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_ACTIVE_FILE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_ACTIVE_FILE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_INACTIVE_FILE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_INACTIVE_FILE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_UNEVICTABLE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_UNEVICTABLE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_MLOCKED:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_MLOCKED), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SWAP_TOTAL:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SWAP_TOTAL), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SWAP_FREE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SWAP_FREE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_DIRTY:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_DIRTY), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_WRITEBACK:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_WRITEBACK), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_ANON_PAGES:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_ANON_PAGES), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_MAPPED:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_MAPPED), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SHMEM:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SHMEM), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SLAB:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SLAB), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SLAB_RECLAIMABLE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SLAB_RECLAIMABLE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_SLAB_UNRECLAIMABLE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_SLAB_UNRECLAIMABLE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_KERNEL_STACK:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_KERNEL_STACK), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_PAGE_TABLES:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_PAGE_TABLES), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_COMMIT_LIMIT:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_COMMIT_LIMIT), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_COMMITED_AS:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_COMMITED_AS), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_VMALLOC_TOTAL:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_VMALLOC_TOTAL), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_VMALLOC_USED:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_VMALLOC_USED), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_VMALLOC_CHUNK:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_VMALLOC_CHUNK), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_CMA_TOTAL:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_CMA_TOTAL), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType::MEMINFO_CMA_FREE:
                streamFilters_->sysEventMemMeasureFilter_->AppendNewMeasureData(
                    sysMemNameDictMap_.at(SysMeminfoType::MEMINFO_CMA_FREE), timeStamp, vMemInfo.value());
                break;
            case SysMeminfoType_INT_MIN_SENTINEL_DO_NOT_USE_:
            case SysMeminfoType_INT_MAX_SENTINEL_DO_NOT_USE_:
            default:
                streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_MEMORY, STAT_EVENT_DATA_INVALID);
                break;
        }
    }
}
void HtraceMemParser::ParseVMemInfo(const MemoryData& tracePacket, uint64_t timeStamp) const
{
    streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_VIRTUAL_MEMORY, STAT_EVENT_RECEIVED);
    for (int i = 0; i < tracePacket.vmeminfo_size(); i++) {
        auto vMemInfo = tracePacket.vmeminfo(i);
        switch (static_cast<int32_t>(vMemInfo.key())) {
            case SysVMeminfoType::VMEMINFO_UNSPECIFIED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNSPECIFIED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_FREE_PAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_FREE_PAGES), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ALLOC_BATCH:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ALLOC_BATCH), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_INACTIVE_ANON:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_INACTIVE_ANON), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ACTIVE_ANON:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ACTIVE_ANON), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_INACTIVE_FILE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_INACTIVE_FILE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ACTIVE_FILE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ACTIVE_FILE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_UNEVICTABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_UNEVICTABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_MLOCK:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_MLOCK), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ANON_PAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ANON_PAGES), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_MAPPED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_MAPPED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_FILE_PAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_FILE_PAGES), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_DIRTY:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_DIRTY), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_WRITEBACK:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_WRITEBACK), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SLAB_RECLAIMABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SLAB_RECLAIMABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SLAB_UNRECLAIMABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SLAB_UNRECLAIMABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_PAGE_TABLE_PAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_PAGE_TABLE_PAGES), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_KERNEL_STACK:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_KERNEL_STACK), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_OVERHEAD:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_OVERHEAD), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_UNSTABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_UNSTABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_BOUNCE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_BOUNCE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_VMSCAN_WRITE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_VMSCAN_WRITE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_VMSCAN_IMMEDIATE_RECLAIM:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_VMSCAN_IMMEDIATE_RECLAIM), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_WRITEBACK_TEMP:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_WRITEBACK_TEMP), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ISOLATED_ANON:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ISOLATED_ANON), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ISOLATED_FILE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ISOLATED_FILE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SHMEM:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SHMEM), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_DIRTIED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_DIRTIED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_WRITTEN:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_WRITTEN), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_PAGES_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_PAGES_SCANNED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_WORKINGSET_REFAULT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_WORKINGSET_REFAULT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_WORKINGSET_ACTIVATE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_WORKINGSET_ACTIVATE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_WORKINGSET_NODERECLAIM:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_WORKINGSET_NODERECLAIM), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ANON_TRANSPARENT_HUGEPAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ANON_TRANSPARENT_HUGEPAGES), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_FREE_CMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_FREE_CMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SWAPCACHE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SWAPCACHE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_DIRTY_THRESHOLD:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_DIRTY_THRESHOLD), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_DIRTY_BACKGROUND_THRESHOLD:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_DIRTY_BACKGROUND_THRESHOLD), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGPGIN:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGPGIN), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGPGOUT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGPGOUT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGPGOUTCLEAN:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGPGOUTCLEAN), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PSWPIN:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PSWPIN), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PSWPOUT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PSWPOUT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGALLOC_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGALLOC_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGALLOC_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGALLOC_NORMAL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGALLOC_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGALLOC_MOVABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGFREE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGFREE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGACTIVATE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGACTIVATE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGDEACTIVATE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGDEACTIVATE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGFAULT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGFAULT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGMAJFAULT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGMAJFAULT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGREFILL_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGREFILL_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGREFILL_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGREFILL_NORMAL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGREFILL_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGREFILL_MOVABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD_NORMAL), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD_MOVABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT_NORMAL), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT_MOVABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD_NORMAL), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD_MOVABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_NORMAL), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_MOVABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_THROTTLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT_THROTTLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGINODESTEAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGINODESTEAL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_SLABS_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_SLABS_SCANNED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_KSWAPD_INODESTEAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_KSWAPD_INODESTEAL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_KSWAPD_LOW_WMARK_HIT_QUICKLY:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_KSWAPD_LOW_WMARK_HIT_QUICKLY), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_KSWAPD_HIGH_WMARK_HIT_QUICKLY:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_KSWAPD_HIGH_WMARK_HIT_QUICKLY), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PAGEOUTRUN:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PAGEOUTRUN), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_ALLOCSTALL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_ALLOCSTALL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGROTATED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGROTATED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_DROP_PAGECACHE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_DROP_PAGECACHE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_DROP_SLAB:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_DROP_SLAB), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGMIGRATE_SUCCESS:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGMIGRATE_SUCCESS), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGMIGRATE_FAIL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGMIGRATE_FAIL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_MIGRATE_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_MIGRATE_SCANNED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_FREE_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_FREE_SCANNED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_ISOLATED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_ISOLATED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_STALL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_STALL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_FAIL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_FAIL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_SUCCESS:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_SUCCESS), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_DAEMON_WAKE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_DAEMON_WAKE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_CULLED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_CULLED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_SCANNED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_RESCUED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_RESCUED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_MLOCKED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_MLOCKED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_MUNLOCKED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_MUNLOCKED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_CLEARED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_CLEARED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_STRANDED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_UNEVICTABLE_PGS_STRANDED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZSPAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZSPAGES), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ION_HEAP:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ION_HEAP), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_GPU_HEAP:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_GPU_HEAP), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_ALLOCSTALL_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_ALLOCSTALL_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_ALLOCSTALL_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_ALLOCSTALL_MOVABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_ALLOCSTALL_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_ALLOCSTALL_NORMAL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_DAEMON_FREE_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_DAEMON_FREE_SCANNED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_COMPACT_DAEMON_MIGRATE_SCANNED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_COMPACT_DAEMON_MIGRATE_SCANNED), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_FASTRPC:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_FASTRPC), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_INDIRECTLY_RECLAIMABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_INDIRECTLY_RECLAIMABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ION_HEAP_POOL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ION_HEAP_POOL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_KERNEL_MISC_RECLAIMABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_KERNEL_MISC_RECLAIMABLE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SHADOW_CALL_STACK_BYTES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SHADOW_CALL_STACK_BYTES), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SHMEM_HUGEPAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SHMEM_HUGEPAGES), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_SHMEM_PMDMAPPED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_SHMEM_PMDMAPPED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_UNRECLAIMABLE_PAGES:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_UNRECLAIMABLE_PAGES), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZONE_ACTIVE_ANON:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZONE_ACTIVE_ANON), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZONE_ACTIVE_FILE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZONE_ACTIVE_FILE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZONE_INACTIVE_ANON:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZONE_INACTIVE_ANON), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZONE_INACTIVE_FILE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZONE_INACTIVE_FILE), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZONE_UNEVICTABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZONE_UNEVICTABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_NR_ZONE_WRITE_PENDING:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_NR_ZONE_WRITE_PENDING), timeStamp,
                    vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_OOM_KILL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_OOM_KILL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGLAZYFREE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGLAZYFREE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGLAZYFREED:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGLAZYFREED), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGREFILL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGREFILL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_DIRECT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSCAN_KSWAPD), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSKIP_DMA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSKIP_DMA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSKIP_MOVABLE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSKIP_MOVABLE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSKIP_NORMAL:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSKIP_NORMAL), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_DIRECT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_PGSTEAL_KSWAPD), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_SWAP_RA:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_SWAP_RA), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_SWAP_RA_HIT:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_SWAP_RA_HIT), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType::VMEMINFO_WORKINGSET_RESTORE:
                streamFilters_->sysEventVMemMeasureFilter_->AppendNewMeasureData(
                    sysVMemNameDictMap_.at(SysVMeminfoType::VMEMINFO_WORKINGSET_RESTORE), timeStamp, vMemInfo.value());
                break;
            case SysVMeminfoType_INT_MIN_SENTINEL_DO_NOT_USE_:
            case SysVMeminfoType_INT_MAX_SENTINEL_DO_NOT_USE_:
            default:
                streamFilters_->statFilter_->IncreaseStat(TRACE_SYS_VIRTUAL_MEMORY, STAT_EVENT_DATA_INVALID);
        }
    }
}
void HtraceMemParser::Finish()
{
    traceDataCache_->MixTraceTime(traceStartTime_, traceEndTime_);
}
} // namespace TraceStreamer
} // namespace SysTuning

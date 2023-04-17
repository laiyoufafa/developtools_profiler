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
#include "stack_preprocess.h"

#include <elf.h>
#include <unistd.h>

#include "logging.h"
#include "plugin_service_types.pb.h"
#include "elf_parser.h"
#include "utilities.h"

static std::atomic<uint64_t> timeCost = 0;
static std::atomic<uint64_t> unwindTimes = 0;

constexpr static uint32_t MAX_BUFFER_SIZE = 10 * 1024 * 1024;
constexpr static uint32_t MAX_MATCH_CNT = 1000;
constexpr static uint32_t MAX_MATCH_INTERVAL = 2000;
constexpr static uint32_t LOG_PRINT_TIMES = 10000;
constexpr static uint32_t FUNCTION_MAP_LOG_PRINT = 100;
constexpr static uint32_t FILE_MAP_LOG_PRINT = 10;
constexpr static uint32_t MAX_BATCH_CNT = 5;
constexpr static uint32_t LONG_TIME_THRESHOLD = 1000000;
// dlopen function call frame index for fp mode
constexpr static uint32_t DLOPEN_FRAME_INDEX = 4;

using namespace OHOS::Developtools::NativeDaemon;

StackPreprocess::StackPreprocess(const StackDataRepeaterPtr& dataRepeater, const NativeHookConfig& hookConfig)
    : dataRepeater_(dataRepeater), buffer_(new (std::nothrow) uint8_t[MAX_BUFFER_SIZE]),
      hookConfig_(hookConfig), fpHookData_(nullptr, nullptr)
{
    runtime_instance = std::make_shared<VirtualRuntime>(hookConfig_);

    if (hookConfig_.malloc_free_matching_interval() > MAX_MATCH_INTERVAL) {
        HILOG_INFO(LOG_CORE, "Not support set %d", hookConfig_.malloc_free_matching_interval());
        hookConfig_.set_malloc_free_matching_interval(MAX_MATCH_INTERVAL);
    }

    if (hookConfig_.malloc_free_matching_cnt() > MAX_MATCH_CNT) {
        HILOG_INFO(LOG_CORE, "Not support set %d", hookConfig_.malloc_free_matching_cnt());
        hookConfig_.set_malloc_free_matching_cnt(MAX_MATCH_CNT);
    }
    HILOG_INFO(LOG_CORE, "malloc_free_matching_interval = %d malloc_free_matching_cnt = %d\n",
        hookConfig_.malloc_free_matching_interval(), hookConfig_.malloc_free_matching_cnt());

    if (hookConfig_.statistics_interval() > 0) {
        statisticsInterval_ = std::chrono::seconds(hookConfig_.statistics_interval());
    }
    HILOG_INFO(LOG_CORE, "statistics_interval = %d statisticsInterval_ = %lld \n",
        hookConfig_.statistics_interval(), statisticsInterval_.count());
    // create file
    if (hookConfig_.save_file()) {
        if (hookConfig_.file_name() != "") {
            HILOG_DEBUG(LOG_CORE, "save file name = %s", hookConfig_.file_name().c_str());
            FILE *fp = fopen(hookConfig_.file_name().c_str(), "wb+");
            if (fp) {
                fpHookData_.reset();
                fpHookData_ = std::unique_ptr<FILE, decltype(&fclose)>(fp, fclose);
            } else {
                fpHookData_.reset();
            }
        } else {
            HILOG_WARN(LOG_CORE, "If you need to save the file, please set the file_name");
        }
    }
    DlopenRangePreprocess();
    dlopenFrameIdx_ = hookConfig_.fp_unwind() ? DLOPEN_FRAME_INDEX : DLOPEN_FRAME_INDEX + FILTER_STACK_DEPTH;
#if defined(__arm__)
    u64regs_.resize(PERF_REG_ARM_MAX);
#else
    u64regs_.resize(PERF_REG_ARM64_MAX);
#endif
    callFrames_.reserve(hookConfig_.max_stack_depth());
}

StackPreprocess::StackPreprocess(bool fpUnwind): fpHookData_(nullptr, nullptr)
{
    DlopenRangePreprocess();
    dlopenFrameIdx_ = fpUnwind ? DLOPEN_FRAME_INDEX : DLOPEN_FRAME_INDEX + FILTER_STACK_DEPTH;
}

StackPreprocess::~StackPreprocess()
{
    isStopTakeData_ = true;
    if (dataRepeater_) {
        dataRepeater_->Close();
    }
    if (thread_.joinable()) {
        thread_.join();
    }
    runtime_instance = nullptr;
    fpHookData_ = nullptr;
}

void StackPreprocess::SetWriter(const std::shared_ptr<BufferWriter>& writer)
{
    writer_ = writer;
}

bool StackPreprocess::StartTakeResults()
{
    CHECK_NOTNULL(dataRepeater_, false, "data repeater null");

    std::thread demuxer(&StackPreprocess::TakeResults, this);
    CHECK_TRUE(demuxer.get_id() != std::thread::id(), false, "demuxer thread invalid");

    thread_ = std::move(demuxer);
    isStopTakeData_ = false;
    return true;
}

bool StackPreprocess::StopTakeResults()
{
    HILOG_INFO(LOG_CORE, "start StopTakeResults");
    CHECK_NOTNULL(dataRepeater_, false, "data repeater null");
    CHECK_TRUE(thread_.get_id() != std::thread::id(), false, "thread invalid");

    isStopTakeData_ = true;
    dataRepeater_->PutRawStack(nullptr, false);
    HILOG_INFO(LOG_CORE, "Wait thread join");

    if (thread_.joinable()) {
        thread_.join();
    }
    HILOG_INFO(LOG_CORE, "Wait thread join success");
    return true;
}

void StackPreprocess::TakeResults()
{
    if (!dataRepeater_) {
        return;
    }

    size_t minStackDepth = hookConfig_.max_stack_depth() > MIN_STACK_DEPTH
        ? MIN_STACK_DEPTH : hookConfig_.max_stack_depth();
    if (hookConfig_.blocked()) {
        minStackDepth = static_cast<size_t>(hookConfig_.max_stack_depth());
    }
    minStackDepth += FILTER_STACK_DEPTH;
    HILOG_INFO(LOG_CORE, "TakeResults thread %d, start!", gettid());
    while (1) {
        BatchNativeHookData stackData;
        RawStackPtr batchRawStack[MAX_BATCH_CNT] = {nullptr};
        auto result = dataRepeater_->TakeRawData(hookConfig_.malloc_free_matching_interval(),
            MAX_BATCH_CNT, batchRawStack);
        if (!result || isStopTakeData_) {
            break;
        }
        for (unsigned int i = 0; i < MAX_BATCH_CNT; i++) {
            auto rawData = batchRawStack[i];
            if (!rawData || isStopTakeData_) {
                break;
            }

            if (!rawData->reportFlag) {
                ignoreCnts_++;
                if (ignoreCnts_ % LOG_PRINT_TIMES == 0) {
                    HILOG_INFO(LOG_CORE, "ignoreCnts_ = %d quene size = %zu\n", ignoreCnts_, dataRepeater_->Size());
                }
                continue;
            }
            eventCnts_++;
            if (eventCnts_ % LOG_PRINT_TIMES == 0) {
                HILOG_INFO(LOG_CORE, "eventCnts_ = %d quene size = %zu\n", eventCnts_, dataRepeater_->Size());
            }
            callFrames_.clear();
            if (hookConfig_.fp_unwind()) {
                uint64_t* fpIp = reinterpret_cast<uint64_t *>(rawData->data);
                for (uint8_t idx = 0; idx < rawData->fpDepth ; ++idx) {
                    if (fpIp[idx] == 0) {
                        break;
                    }
                    callFrames_.emplace_back(fpIp[idx]);
                }
            } else {
#if defined(__arm__)
                uint32_t *regAddrArm = reinterpret_cast<uint32_t *>(rawData->data);
                u64regs_.assign(regAddrArm, regAddrArm + PERF_REG_ARM_MAX);
#else
                if (memcpy_s(u64regs_.data(), sizeof(uint64_t) * PERF_REG_ARM64_MAX, rawData->data,
                    sizeof(uint64_t) * PERF_REG_ARM64_MAX) != EOK) {
                    HILOG_ERROR(LOG_CORE, "memcpy_s regs failed");
                }
#endif
            }
#ifdef PERFORMANCE_DEBUG
            struct timespec start = {};
            clock_gettime(CLOCK_REALTIME, &start);
            size_t realFrameDepth = callFrames_.size();
#endif
            size_t stackDepth = ((size_t)hookConfig_.max_stack_depth() > MAX_CALL_FRAME_UNWIND_SIZE)
                        ? MAX_CALL_FRAME_UNWIND_SIZE
                        : hookConfig_.max_stack_depth() + FILTER_STACK_DEPTH;
            if (rawData->reduceStackFlag) {
                stackDepth = minStackDepth;
            }
            bool ret = runtime_instance->UnwindStack(u64regs_, rawData->stackData, rawData->stackSize,
                rawData->stackConext->pid, rawData->stackConext->tid, callFrames_, stackDepth);
            if (!ret) {
                HILOG_ERROR(LOG_CORE, "unwind fatal error");
                continue;
            }
            if (!isDlopenRangeValid_) {
                runtime_instance->CalcDlopenIpRange(libcSoPath_, dlopenIpMax_, dlopenIpMin_);
                isDlopenRangeValid_ = true;
            }
            if (rawData->stackConext->type == MMAP_MSG) {
                // if mmap msg trigger by dlopen, update maps voluntarily
                if (callFrames_.size() > dlopenFrameIdx_) {
                    // for dlopen mmap framme
                    if (callFrames_[dlopenFrameIdx_].ip_ >= dlopenIpMin_ &&
                            callFrames_[dlopenFrameIdx_].ip_ < dlopenIpMax_) {
                        HILOG_DEBUG(LOG_CORE, "mmap msg trigger by dlopen, update maps voluntarily");
                        runtime_instance->UpdateMaps(rawData->stackConext->pid, rawData->stackConext->tid);
                        flushBasicData_ = hookConfig_.offline_symbolization() ? true : false;
                    }
                }
            }
            if (hookConfig_.save_file() && hookConfig_.file_name() != "") {
                WriteFrames(rawData, callFrames_);
            } else if (!hookConfig_.save_file()) {
                SetHookData(rawData, callFrames_, stackData);
            }
#ifdef PERFORMANCE_DEBUG
            struct timespec end = {};
            clock_gettime(CLOCK_REALTIME, &end);
            uint64_t curTimeCost = (end.tv_sec - start.tv_sec) * MAX_MATCH_CNT * MAX_MATCH_CNT * MAX_MATCH_CNT +
                (end.tv_nsec - start.tv_nsec);
            if (curTimeCost >= LONG_TIME_THRESHOLD) {
                HILOG_ERROR(LOG_CORE, "bigTimeCost %" PRIu64 " event=%d, realFrameDepth=%zu, "
                    "callFramesDepth=%zu\n",
                    curTimeCost, rawData->stackConext->type, realFrameDepth, callFrames_.size());
            }
            timeCost += curTimeCost;
            unwindTimes++;
            if (unwindTimes % LOG_PRINT_TIMES == 0) {
                HILOG_ERROR(LOG_CORE, "unwindTimes %" PRIu64" cost time = %" PRIu64" mean cost = %" PRIu64"\n",
                    unwindTimes.load(), timeCost.load(), timeCost.load() / unwindTimes.load());
            }
#endif
        }
        FlushData(stackData);

        // interval reporting statistics
        if (hookConfig_.statistics_interval() > 0) {
            auto currentTime = std::chrono::steady_clock::now();
            auto elapsedTime = std::chrono::duration_cast<std::chrono::seconds>(currentTime - lastStatisticsTime_);
            if (elapsedTime >= statisticsInterval_) {
                lastStatisticsTime_ = currentTime;
                FlushRecordStatistics();
            }
        }
    }
    HILOG_INFO(LOG_CORE, "TakeResults thread %d, exit!", gettid());
}

inline void StackPreprocess::FillOfflineCallStack(std::vector<CallFrame>& callFrames, size_t idx)
{
    for (; idx < callFrames.size(); ++idx) {
        callStack_.push_back(callFrames[idx].ip_);
    }
}

inline void StackPreprocess::FillCallStack(std::vector<CallFrame>& callFrames,
    BatchNativeHookData& batchNativeHookData, size_t idx)
{
    for (; idx < callFrames.size(); ++idx) {
        ReportFrameMap(callFrames[idx], batchNativeHookData);
        // for call stack id
        callStack_.push_back(callFrames[idx].callFrameId_);
    }
}

/**
 * @return '0' is invalid stack id, '> 0' is valid stack id
 */
inline uint32_t StackPreprocess::SetCallStackMap(BatchNativeHookData& batchNativeHookData)
{
    auto hookData = batchNativeHookData.add_events();
    StackMap* stackmap = hookData->mutable_stack_map();
    uint32_t stackId = callStackMap_.size() + 1;
    stackmap->set_id(stackId);
    // offline symbolization use ip, other use frame_map_id
    if (hookConfig_.offline_symbolization()) {
        for (size_t i = 0; i < callStack_.size(); i++) {
            stackmap->add_ip(callStack_[i]);
        }
    } else {
        for (size_t i = 0; i < callStack_.size(); i++) {
            stackmap->add_frame_map_id(callStack_[i]);
        }
    }
    callStackMap_[callStack_] = stackId;
    return stackId;
}

/**
 * @return '0' is invalid stack id, '> 0' is valid stack id
 */
inline uint32_t StackPreprocess::GetCallStackId(const RawStackPtr& rawStack,
    std::vector<CallFrame>& callFrames,
    BatchNativeHookData& batchNativeHookData)
{
    // ignore the first two frame if dwarf unwind
    size_t idx = hookConfig_.fp_unwind() ? 0 : FILTER_STACK_DEPTH;
    // if free_stack_report or munmap_stack_report is false, don't need to record.
    if ((rawStack->stackConext->type == FREE_MSG) && !hookConfig_.free_stack_report()) {
        return 0;
    } else if ((rawStack->stackConext->type == MUNMAP_MSG) && !hookConfig_.munmap_stack_report()) {
        return 0;
    }
    callStack_.clear();
    callStack_.reserve(callFrames.size());
    if (!hookConfig_.offline_symbolization()) {
        FillCallStack(callFrames, batchNativeHookData, idx);
    } else {
        FillOfflineCallStack(callFrames, idx);
    }
    // return call stack id
    auto itStack = callStackMap_.find(callStack_);
    if (itStack != callStackMap_.end()) {
        return itStack->second;
    } else {
        return SetCallStackMap(batchNativeHookData);
    }
}

template <typename T>
void StackPreprocess::SetEventFrame(const RawStackPtr& rawStack,
    std::vector<CallFrame>& callFrames,
    BatchNativeHookData& batchNativeHookData,
    T* event, uint32_t stackMapId)
{
    // ignore the first two frame if dwarf unwind
    size_t idx = hookConfig_.fp_unwind() ? 0 : FILTER_STACK_DEPTH;
    event->set_pid(rawStack->stackConext->pid);
    event->set_tid(rawStack->stackConext->tid);
    event->set_addr((uint64_t)rawStack->stackConext->addr);

    if (hookConfig_.callframe_compress() && stackMapId != 0) {
        event->set_stack_id(stackMapId);
    } else if (hookConfig_.string_compressed()) {
        for (; idx < callFrames.size(); ++idx) {
            ReportSymbolNameMap(callFrames[idx], batchNativeHookData);
            ReportFilePathMap(callFrames[idx], batchNativeHookData);
            Frame* frame = event->add_frame_info();
            SetFrameInfo(*frame, callFrames[idx]);
        }
    } else {
        for (; idx < callFrames.size(); ++idx) {
            Frame* frame = event->add_frame_info();
            SetFrameInfo(*frame, callFrames[idx]);
        }
    }
}

void StackPreprocess::SetAllocStatisticsFrame(const RawStackPtr& rawStack,
    std::vector<CallFrame>& callFrames,
    BatchNativeHookData& batchNativeHookData)
{
    // ignore the first two frame if dwarf unwind
    size_t idx = hookConfig_.fp_unwind() ? 0 : FILTER_STACK_DEPTH;
    callStack_.clear();
    callStack_.reserve(callFrames.size());
    if (!hookConfig_.offline_symbolization()) {
        FillCallStack(callFrames, batchNativeHookData, idx);
    } else {
        FillOfflineCallStack(callFrames, idx);
    }
    // by call stack id set alloc statistics data.
    auto itStack = callStackMap_.find(callStack_);
    if (itStack != callStackMap_.end()) {
        SetAllocStatisticsData(rawStack, itStack->second, true);
    } else {
        auto stackId = SetCallStackMap(batchNativeHookData);
        SetAllocStatisticsData(rawStack, stackId);
    }
}

void StackPreprocess::SetHookData(RawStackPtr rawStack,
    std::vector<CallFrame>& callFrames, BatchNativeHookData& batchNativeHookData)
{
    if (hookConfig_.offline_symbolization() && flushBasicData_) {
        SetMapsInfo(-1, rawStack);
        flushBasicData_ = false;
    }

    // statistical reporting must is compressed and accurate.
    if (hookConfig_.statistics_interval() > 0) {
        if (rawStack->stackConext->type == FREE_MSG || rawStack->stackConext->type == MUNMAP_MSG) {
            SetFreeStatisticsData((uint64_t)rawStack->stackConext->addr);
        } else if (rawStack->stackConext->type == MALLOC_MSG || rawStack->stackConext->type == MMAP_MSG) {
            SetAllocStatisticsFrame(rawStack, callFrames, batchNativeHookData);
        }
        return;
    }

    uint32_t stackMapId = 0;
    if (hookConfig_.callframe_compress() &&
        !(rawStack->stackConext->type == MEMORY_TAG || rawStack->stackConext->type == PR_SET_VMA_MSG)) {
        stackMapId = GetCallStackId(rawStack, callFrames, batchNativeHookData);
    }
    NativeHookData* hookData = batchNativeHookData.add_events();
    hookData->set_tv_sec(rawStack->stackConext->ts.tv_sec);
    hookData->set_tv_nsec(rawStack->stackConext->ts.tv_nsec);

    if (rawStack->stackConext->type == MALLOC_MSG) {
        AllocEvent* allocEvent = hookData->mutable_alloc_event();
        allocEvent->set_size(static_cast<uint64_t>(rawStack->stackConext->mallocSize));
        std::string name = rawStack->stackConext->tname;
        if (!name.empty()) {
            allocEvent->set_thread_name_id(GetThreadIdx(name, batchNativeHookData));
        }
        SetEventFrame(rawStack, callFrames, batchNativeHookData, allocEvent, stackMapId);
    } else if (rawStack->stackConext->type == FREE_MSG) {
        FreeEvent* freeEvent = hookData->mutable_free_event();
        std::string name = rawStack->stackConext->tname;
        if (!name.empty()) {
            freeEvent->set_thread_name_id(GetThreadIdx(name, batchNativeHookData));
        }
        SetEventFrame(rawStack, callFrames, batchNativeHookData, freeEvent, stackMapId);
    } else if (rawStack->stackConext->type == MMAP_MSG) {
        MmapEvent* mmapEvent = hookData->mutable_mmap_event();
        mmapEvent->set_size(static_cast<uint64_t>(rawStack->stackConext->mallocSize));
        std::string name = rawStack->stackConext->tname;
        if (name == "ArkJs") {
            mmapEvent->set_type("ArkJsGlobalHandle");
        } else if (name.find("/") != std::string::npos) {
            name.erase(0, 1); // remove first '/'
            mmapEvent->set_type("FilePage:" + name);
        } else if (!name.empty()) {
            mmapEvent->set_thread_name_id(GetThreadIdx(name, batchNativeHookData));
        }
        SetEventFrame(rawStack, callFrames, batchNativeHookData, mmapEvent, stackMapId);
    } else if (rawStack->stackConext->type == MUNMAP_MSG) {
        MunmapEvent* munmapEvent = hookData->mutable_munmap_event();
        munmapEvent->set_size(static_cast<uint64_t>(rawStack->stackConext->mallocSize));
        std::string name = rawStack->stackConext->tname;
        if (!name.empty()) {
            munmapEvent->set_thread_name_id(GetThreadIdx(name, batchNativeHookData));
        }
        SetEventFrame(rawStack, callFrames, batchNativeHookData, munmapEvent, stackMapId);
    } else if (rawStack->stackConext->type == PR_SET_VMA_MSG) {
        MemTagEvent* tagEvent = hookData->mutable_tag_event();
        std::string name = "Anonymous:";
        tagEvent->set_tag(name + rawStack->stackConext->tname);
        tagEvent->set_size(rawStack->stackConext->mallocSize);
        tagEvent->set_addr((uint64_t)rawStack->stackConext->addr);
    }
}

inline bool StackPreprocess::SetFreeStatisticsData(uint64_t addr)
{
    // through the addr lookup record
    auto addrIter = allocAddrMap_.find(addr);
    if (addrIter != allocAddrMap_.end()) {
        auto& record = addrIter->second.second;
        ++record->releaseCount;
        record->releaseSize += addrIter->second.first;
        statisticsPeriodData_[record->callstackId] = record;
        allocAddrMap_.erase(addr);
        return true;
    }
    return false;
}
inline void StackPreprocess::SetAllocStatisticsData(const RawStackPtr& rawStack, size_t stackId, bool isExists)
{
    // if the record exists, it is updated.Otherwise Add
    if (isExists) {
        auto recordIter = recordStatisticsMap_.find(stackId);
        if (recordIter != recordStatisticsMap_.end()) {
            auto& record = recordIter->second;
            ++record.applyCount;
            record.applySize += rawStack->stackConext->mallocSize;
            allocAddrMap_[(uint64_t)rawStack->stackConext->addr] =
                std::pair(rawStack->stackConext->mallocSize, &recordIter->second);
            statisticsPeriodData_[stackId] = &recordIter->second;
        }
    } else {
        RecordStatistic record;
        record.pid = rawStack->stackConext->pid;
        record.callstackId = stackId;
        record.applyCount = 1;
        record.applySize = rawStack->stackConext->mallocSize;
        record.type = (rawStack->stackConext->type == MALLOC_MSG ?
            RecordStatisticsEvent::MALLOC : RecordStatisticsEvent::MMAP);
        auto [recordIter, stat] = recordStatisticsMap_.emplace(stackId, record);
        allocAddrMap_[(uint64_t)rawStack->stackConext->addr] =
            std::pair(rawStack->stackConext->mallocSize, &recordIter->second);
        statisticsPeriodData_[stackId] = &recordIter->second;
    }
}
uint32_t StackPreprocess::GetThreadIdx(std::string threadName, BatchNativeHookData& batchNativeHookData)
{
    auto it = threadMap_.find(threadName);
    if (it != threadMap_.end()) {
        return it->second;
    } else {
        auto hookData = batchNativeHookData.add_events();
        auto* thread = hookData->mutable_thread_name_map();
        thread->set_id(threadMap_.size() + 1);
        thread->set_name(threadName);
        threadMap_[threadName] = threadMap_.size() + 1;

        HILOG_INFO(LOG_CORE, "threadName = %s, functionMap_.size() = %zu\n", threadName.c_str(), threadMap_.size());
        return threadMap_.size();
    }
}

void StackPreprocess::WriteFrames(RawStackPtr rawStack, const std::vector<CallFrame>& callFrames)
{
    CHECK_TRUE(fpHookData_.get() != nullptr, NO_RETVAL, "fpHookData_ is nullptr, please check file_name(%s)",
        hookConfig_.file_name().c_str());
    if (rawStack->stackConext->type == PR_SET_VMA_MSG) {
        fprintf(fpHookData_.get(), "prctl;%u;%u;%" PRId64 ";%ld;0x%" PRIx64 ":tag:%s\n",
            rawStack->stackConext->pid, rawStack->stackConext->tid,
            (int64_t)rawStack->stackConext->ts.tv_sec, rawStack->stackConext->ts.tv_nsec,
            (uint64_t)rawStack->stackConext->addr, rawStack->stackConext->tname);
        return;
    }
    std::string tag = "";
    switch (rawStack->stackConext->type) {
        case FREE_MSG:
            tag = "free";
            break;
        case MALLOC_MSG:
            tag = "malloc";
            break;
        case MMAP_MSG:
            tag = "mmap";
            break;
        case MUNMAP_MSG:
            tag = "munmap";
            break;
        default:
            break;
    }

    fprintf(fpHookData_.get(), "%s;%u;%u;%" PRId64 ";%ld;0x%" PRIx64 ";%zu\n", tag.c_str(),
        rawStack->stackConext->pid, rawStack->stackConext->tid, (int64_t)rawStack->stackConext->ts.tv_sec,
        rawStack->stackConext->ts.tv_nsec, (uint64_t)rawStack->stackConext->addr, rawStack->stackConext->mallocSize);

    for (size_t idx = 0; idx < callFrames.size(); ++idx) {
        (void)fprintf(fpHookData_.get(), "0x%" PRIx64 ";0x%" PRIx64 ";%s;%s;0x%" PRIx64 ";%" PRIu64 "\n",
            callFrames[idx].ip_, callFrames[idx].sp_, std::string(callFrames[idx].symbolName_).c_str(),
            std::string(callFrames[idx].filePath_).c_str(), callFrames[idx].offset_, callFrames[idx].symbolOffset_);
    }
}

inline void StackPreprocess::SetFrameInfo(Frame& frame, CallFrame& callFrame)
{
    frame.set_ip(callFrame.ip_);
    if (hookConfig_.offline_symbolization()) {
        return;
    }
    frame.set_sp(callFrame.sp_);
    frame.set_offset(callFrame.offset_);
    frame.set_symbol_offset(callFrame.symbolOffset_);

    if (callFrame.symbolNameId_ != 0 && callFrame.filePathId_ != 0) {
        frame.set_symbol_name_id(callFrame.symbolNameId_);
        frame.set_file_path_id(callFrame.filePathId_);
    } else {
        frame.set_symbol_name(std::string(callFrame.symbolName_));
        frame.set_file_path(std::string(callFrame.filePath_));
    }
}

inline void StackPreprocess::ReportSymbolNameMap(CallFrame& callFrame, BatchNativeHookData& batchNativeHookData)
{
    if (callFrame.needReport_ & SYMBOL_NAME_ID_REPORT) {
        auto hookData = batchNativeHookData.add_events();
        SymbolMap* symbolMap = hookData->mutable_symbol_name();
        symbolMap->set_id(callFrame.symbolNameId_);
        symbolMap->set_name(std::string(callFrame.symbolName_));
    }
}

inline void StackPreprocess::ReportFilePathMap(CallFrame& callFrame, BatchNativeHookData& batchNativeHookData)
{
    if (callFrame.needReport_ & FILE_PATH_ID_REPORT) {
        auto hookData = batchNativeHookData.add_events();
        FilePathMap* filepathMap = hookData->mutable_file_path();
        filepathMap->set_id(callFrame.filePathId_);
        filepathMap->set_name(std::string(callFrame.filePath_));
    }
}

inline void StackPreprocess::ReportFrameMap(CallFrame& callFrame, BatchNativeHookData& batchNativeHookData)
{
    if (callFrame.needReport_ & CALL_FRAME_REPORT) {
        ReportSymbolNameMap(callFrame, batchNativeHookData);
        ReportFilePathMap(callFrame, batchNativeHookData);
        auto hookData = batchNativeHookData.add_events();
        FrameMap* frameMap = hookData->mutable_frame_map();
        Frame* frame = frameMap->mutable_frame();
        SetFrameInfo(*frame, callFrame);
        frameMap->set_id(callFrame.callFrameId_);
    }
}

const std::string StackPreprocess::SearchLibcSoPath()
{
    std::string mapContent = ReadFileToString("/proc/self/maps");
    std::istringstream s(mapContent);
    std::string line;
    if (mapContent.size() > 0) {
        while (std::getline(s, line)) {
            std::vector<std::string> map = StringSplit(line, " ");
            for (const auto& item : map) {
                if (item.find("ld-musl") != std::string::npos) {
                    return item;
                }
            }
        }
    }
    return "";
}

void StackPreprocess::DlopenRangePreprocess()
{
    libcSoPath_ = SearchLibcSoPath();
    if (libcSoPath_.empty()) {
        HILOG_ERROR(LOG_CORE, "DlopenRangePreprocess search libc so path failed");
        return;
    }
    using OHOS::Developtools::NativeDaemon::ELF::ElfFile;
    std::unique_ptr<ElfFile> elfPtr = ElfFile::MakeUnique(libcSoPath_);
    if (elfPtr == nullptr) {
        HILOG_ERROR(LOG_CORE, "DlopenRangePreprocess elfPtr is nullptr");
        return;
    }

    std::string symSecName;
    std::string strSecName;
    if (elfPtr->shdrs_.find(".symtab") != elfPtr->shdrs_.end()) {
        symSecName = ".symtab";
        strSecName = ".strtab";
    } else if (elfPtr->shdrs_.find(".dynsym") != elfPtr->shdrs_.end()) {
        symSecName = ".dynsym";
        strSecName = ".dynstr";
    } else {
        HILOG_ERROR(LOG_CORE, "DlopenRangePreprocess get symbol failed");
        return;
    }
    const auto &sym = elfPtr->shdrs_[static_cast<const std::string>(symSecName)];
    const uint8_t* symData = elfPtr->GetSectionData(sym->secIndex_);
    const auto &str = elfPtr->shdrs_[static_cast<const std::string>(strSecName)];
    const uint8_t* strData = elfPtr->GetSectionData(str->secIndex_);

    std::string strTable(reinterpret_cast<char*>(const_cast<uint8_t*>(strData)), str->secSize_);
    std::pair<uint64_t, uint64_t> symbolRange;
    const std::string dlopenStr = "dlopen";
    size_t pos = 0;
    while (true) {
        if (pos = strTable.find(dlopenStr, pos); pos != std::string::npos) {
            std::string strTemp(strTable.c_str() + pos);
            if (strTemp.compare(dlopenStr) != 0) {
                pos += strTemp.size() + 1;
                continue;
            }
            HILOG_INFO(LOG_CORE, "DlopenRangePreprocess st_name = %zu", pos);
            if (sym->secEntrySize_ == sizeof(Elf32_Sym)) {
                symbolRange = elfPtr->GetSymbolRange<Elf32_Sym>(const_cast<uint8_t*>(symData), sym->secSize_, pos);
            } else {
                symbolRange = elfPtr->GetSymbolRange<Elf64_Sym>(const_cast<uint8_t*>(symData), sym->secSize_, pos);
            }
        }
        break;
    }

    uint64_t textVaddr = 0;
    uint64_t textOffset = 0;
    if (auto text = elfPtr->shdrs_.find(".text"); text != elfPtr->shdrs_.end()) {
        textVaddr = elfPtr->shdrs_[".text"]->secVaddr_;
        textOffset = elfPtr->shdrs_[".text"]->fileOffset_;
    }

    dlopenIpMax_ = symbolRange.first + symbolRange.second + textOffset - textVaddr;
    dlopenIpMin_ = symbolRange.first + textOffset - textVaddr;
    HILOG_INFO(LOG_CORE, "DlopenRangePreprocess st_value = 0x%" PRIx64 ", st_size = 0x%" PRIx64 ","
        "textOffset = 0x%" PRIx64 ", textVaddr = 0x%" PRIx64 ", dlopenIpMax_ = 0x%" PRIx64 ","
        "dlopenIpMin_ = 0x%" PRIx64 "", symbolRange.first, symbolRange.second, textOffset, textVaddr,
        dlopenIpMax_, dlopenIpMin_);
}

void StackPreprocess::SetMapsInfo(pid_t pid, RawStackPtr rawStack)
{
    if (rawStack == nullptr) {
        runtime_instance->UpdateMaps(pid, pid);
    }

    uint32_t tempFilePathId = 0;
    for (auto& item : runtime_instance->GetProcessMaps()) {
        if (item.isReported) {
            continue;
        }
        item.isReported = true;

        BatchNativeHookData stackData;
        if (tempFilePathId != item.filePathId_) {
            tempFilePathId = item.filePathId_;
            ElfSymbolTable symbolInfo;
            GetSymbols(item.name_, symbolInfo);
            if (symbolInfo.symEntSize == 0) {
                continue;
            }

            NativeHookData* hookData = stackData.add_events();
            FilePathMap* filepathMap = hookData->mutable_file_path();
            filepathMap->set_id(item.filePathId_);
            filepathMap->set_name(item.name_);
            SetSymbolInfo(item.filePathId_, symbolInfo, stackData);
        }

        NativeHookData* hookData = stackData.add_events();
        MapsInfo* map = hookData->mutable_maps_info();
        map->set_pid(rawStack == nullptr ? pid : rawStack->stackConext->pid);
        map->set_start(item.begin_);
        map->set_end(item.end_);
        map->set_offset(item.pageoffset_);
        map->set_file_path_id(item.filePathId_);
        FlushData(stackData);
    }
}

void StackPreprocess::SetSymbolInfo(uint32_t filePathId, ElfSymbolTable& symbolInfo,
    BatchNativeHookData& batchNativeHookData)
{
    if (symbolInfo.symEntSize == 0) {
        HILOG_ERROR(LOG_CORE, "SetSymbolInfo get symbolInfo failed");
        return;
    }
    NativeHookData* hookData = batchNativeHookData.add_events();
    SymbolTable* symTable = hookData->mutable_symbol_tab();
    symTable->set_file_path_id(filePathId);
    symTable->set_text_exec_vaddr(symbolInfo.textVaddr);
    symTable->set_text_exec_vaddr_file_offset(symbolInfo.textOffset);
    symTable->set_sym_entry_size(symbolInfo.symEntSize);
    symTable->set_sym_table(symbolInfo.symTable.data(), symbolInfo.symTable.size());
    symTable->set_str_table(symbolInfo.strTable.data(), symbolInfo.strTable.size());
}

void StackPreprocess::OfflineSymbolizationPreprocess(pid_t pid)
{
    SetMapsInfo(pid, nullptr);
    flushBasicData_ = false;
    runtime_instance->ClearMaps();
}

void StackPreprocess::FlushData(BatchNativeHookData& stackData)
{
    if (stackData.events().size() > 0) {
        size_t length = stackData.ByteSizeLong();
        if (length < MAX_BUFFER_SIZE) {
            stackData.SerializeToArray(buffer_.get(), length);
            Flush(buffer_.get(), length);
        } else {
            HILOG_ERROR(LOG_CORE, "the data is larger than MAX_BUFFER_SIZE, flush failed");
        }
    }
}

void StackPreprocess::Flush(const uint8_t* src, size_t size)
{
    if (src == nullptr) {
        HILOG_ERROR(LOG_CORE, "Flush src is nullptr");
        return;
    }
    ProfilerPluginData pluginData;
    pluginData.set_name("nativehook");
    pluginData.set_version("1.01");
    pluginData.set_status(0);
    pluginData.set_data(src, size);
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    pluginData.set_clock_id(ProfilerPluginData::CLOCKID_REALTIME);
    pluginData.set_tv_sec(ts.tv_sec);
    pluginData.set_tv_nsec(ts.tv_nsec);
    writer_->WriteMessage(pluginData, "nativehook");
    writer_->Flush();
}

void StackPreprocess::GetSymbols(const std::string& filePath, ElfSymbolTable& symbols)
{
    using OHOS::Developtools::NativeDaemon::ELF::ElfFile;
    std::unique_ptr<ElfFile> elfPtr = ElfFile::MakeUnique(filePath);
    if (elfPtr == nullptr) {
        HILOG_ERROR(LOG_CORE, "GetSymbols elfPtr is nullptr");
        return;
    }

    symbols.textVaddr = (std::numeric_limits<uint64_t>::max)();
    for (auto &item : elfPtr->phdrs_) {
        if ((item->type_ == PT_LOAD) && (item->flags_ & PF_X)) {
            // find the min addr
            if (symbols.textVaddr != (std::min)(symbols.textVaddr, item->vaddr_)) {
                symbols.textVaddr = (std::min)(symbols.textVaddr, item->vaddr_);
                symbols.textOffset = item->offset_;
            }
        }
    }
    if (symbols.textVaddr == (std::numeric_limits<uint64_t>::max)()) {
        HILOG_ERROR(LOG_CORE, "GetSymbols get textVaddr failed");
        return;
    }

    std::string symSecName;
    std::string strSecName;
    if (elfPtr->shdrs_.find(".symtab") != elfPtr->shdrs_.end()) {
        symSecName = ".symtab";
        strSecName = ".strtab";
    } else if (elfPtr->shdrs_.find(".dynsym") != elfPtr->shdrs_.end()) {
        symSecName = ".dynsym";
        strSecName = ".dynstr";
    } else {
        return;
    }
    const auto &sym = elfPtr->shdrs_[static_cast<const std::string>(symSecName)];
    const uint8_t* symData = elfPtr->GetSectionData(sym->secIndex_);
    const auto &str = elfPtr->shdrs_[static_cast<const std::string>(strSecName)];
    const uint8_t* strData = elfPtr->GetSectionData(str->secIndex_);

    if (sym->secSize_ == 0 || str->secSize_ == 0) {
        HILOG_ERROR(LOG_CORE, "GetSymbols get section size failed, \
            sym size: %" PRIu64 ", str size: %" PRIu64 "", sym->secSize_, str->secSize_);
        return;
    }
    symbols.symEntSize = sym->secEntrySize_;
    symbols.symTable.resize(sym->secSize_);
    std::copy(symData, symData + sym->secSize_, symbols.symTable.data());
    symbols.strTable.resize(str->secSize_);
    std::copy(strData, strData + str->secSize_, symbols.strTable.data());
}

bool StackPreprocess::FlushRecordStatistics()
{
    if (statisticsPeriodData_.empty()) {
        return false;
    }
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    BatchNativeHookData statisticsData;
    for (auto [addr, statistics] : statisticsPeriodData_) {
        NativeHookData* hookData = statisticsData.add_events();
        hookData->set_tv_sec(ts.tv_sec);
        hookData->set_tv_nsec(ts.tv_nsec);
        RecordStatisticsEvent* recordEvent = hookData->mutable_statistics_event();
        recordEvent->set_pid(statistics->pid);
        recordEvent->set_callstack_id(statistics->callstackId);
        recordEvent->set_type(statistics->type);
        recordEvent->set_apply_count(statistics->applyCount);
        recordEvent->set_release_count(statistics->releaseCount);
        recordEvent->set_apply_size(statistics->applySize);
        recordEvent->set_release_size(statistics->releaseSize);
    }
    FlushData(statisticsData);
    statisticsPeriodData_.clear();

    return true;
}

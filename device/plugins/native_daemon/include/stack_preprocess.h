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
#ifndef STACK_PREPROCESS_H
#define STACK_PREPROCESS_H

#include <chrono>
#include <thread>
#include <unordered_map>
#include <list>
#include <algorithm>

#include "logging.h"
#include "nocopyable.h"
#include "stack_data_repeater.h"
#include "buffer_writer.h"
#include "virtual_runtime.h"
#include "hook_common.h"
#include "native_hook_config.pb.h"
#include "native_hook_result.pb.h"

class StackPreprocess {
public:
    struct RecordStatistic {
        uint32_t pid {0};
        uint32_t callstackId {0};
        RecordStatisticsEvent::MemoryType type {RecordStatisticsEvent::MALLOC};
        uint64_t applyCount {0};
        uint64_t releaseCount {0};
        uint64_t applySize {0};
        uint64_t releaseSize {0};
    };

    explicit StackPreprocess(const StackDataRepeaterPtr& dataRepeater, const NativeHookConfig& hookConfig);
    ~StackPreprocess();
    void SetWriter(const std::shared_ptr<BufferWriter>& writer);
    bool StartTakeResults();
    bool StopTakeResults();
    void OfflineSymbolizationPreprocess(pid_t pid);
    bool FlushRecordStatistics();
    void SetSerializeMode(bool protobufSerialize);

private:
    using CallFrame = OHOS::Developtools::NativeDaemon::CallFrame;
    struct ElfSymbolTable {
        uint64_t textVaddr;
        uint32_t textOffset;
        uint32_t symEntSize;
        std::vector<uint8_t> strTable;
        std::vector<uint8_t> symTable;
    };

    enum RecordStatisticsLimit : std::size_t {
        STATISTICS_MAP_SZIE = 2048,
        STATISTICS_PERIOD_DATA_SIZE = 256,
        ALLOC_ADDRMAMP_SIZE = 2048,
    };

private:
    void TakeResults();
    void SetHookData(RawStackPtr RawStack, std::vector<CallFrame>& callsFrames,
        BatchNativeHookData& batchNativeHookData);
    void WriteFrames(RawStackPtr RawStack, const std::vector<CallFrame>& callsFrames);
    void SetFrameInfo(Frame& frame, CallFrame& callFrame);
    void ReportSymbolNameMap(CallFrame& callFrame, BatchNativeHookData& batchNativeHookData);
    void ReportFilePathMap(CallFrame& callFrame, BatchNativeHookData& batchNativeHookData);
    void ReportFrameMap(CallFrame& callFrame, BatchNativeHookData& batchNativeHookData);
    uint32_t GetThreadIdx(std::string threadName, BatchNativeHookData& batchNativeHookData);
    void SetMapsInfo(pid_t pid, RawStackPtr rawStack);
    void SetSymbolInfo(uint32_t filePathId, ElfSymbolTable& symbolInfo,
        BatchNativeHookData& batchNativeHookData);
    void FlushData(BatchNativeHookData& stackData);
    void Flush(const uint8_t* src, size_t size);
    void GetSymbols(const std::string& filePath, ElfSymbolTable& symbols);
    void DlopenRangePreprocess();
    const std::string SearchLibcSoPath();

    void FillOfflineCallStack(std::vector<CallFrame>& callsFrames, size_t idx);
    void FillCallStack(std::vector<CallFrame>& callsFrames,
        BatchNativeHookData& batchNativeHookData, size_t idx);
    uint32_t SetCallStackMap(BatchNativeHookData& batchNativeHookData);
    uint32_t GetCallStackId(const RawStackPtr& rawStack, std::vector<CallFrame>& callsFrames,
        BatchNativeHookData& batchNativeHookData);
    template <typename T>
    void SetEventFrame(const RawStackPtr& rawStack, std::vector<CallFrame>& callsFrames,
        BatchNativeHookData& batchNativeHookData, T* event, uint32_t stackId);
    void SetAllocStatisticsFrame(const RawStackPtr& rawStack, std::vector<CallFrame>& callsFrames,
        BatchNativeHookData& batchNativeHookData);
    bool SetFreeStatisticsData(uint64_t addr);
    void SetAllocStatisticsData(const RawStackPtr& rawStack, size_t stackId, bool isExists = false);
private:
    std::shared_ptr<BufferWriter> writer_ = nullptr;
    StackDataRepeaterPtr dataRepeater_ = nullptr;
    std::thread thread_ {};
    std::unique_ptr<uint8_t[]> buffer_;
    bool isStopTakeData_ = false;
    std::shared_ptr<OHOS::Developtools::NativeDaemon::VirtualRuntime> runtime_instance;
    DISALLOW_COPY_AND_MOVE(StackPreprocess);
    std::unordered_map<std::string, uint32_t> threadMap_;
    NativeHookConfig hookConfig_;
    std::unique_ptr<FILE, decltype(&fclose)> fpHookData_;
    uint32_t ignoreCnts_ = 0;
    uint32_t eventCnts_ = 0;
    bool flushBasicData_ {true};
    std::string libcSoPath_;
    uint32_t dlopenFrameIdx_ {0};
    uint64_t dlopenIpMax_ {0};
    uint64_t dlopenIpMin_ {0};
    std::vector<u64> u64regs_;
    std::vector<CallFrame> callsFrames_;
    std::vector<uint64_t> callStack_;
    // Key is callStack_, value is call stack id
    std::map<std::vector<uint64_t>, uint32_t> callStackMap_;
    std::chrono::seconds statisticsInterval_;
    std::chrono::steady_clock::time_point lastStatisticsTime_;
    // Key is call stack id, value is recordstatistic data
    std::unordered_map<uint32_t, RecordStatistic> recordStatisticsMap_ {STATISTICS_MAP_SZIE};
    // Key is call stack id, value is recordstatistic data pointer
    std::unordered_map<uint32_t, RecordStatistic*> statisticsPeriodData_ {STATISTICS_PERIOD_DATA_SIZE};
    // Key is alloc or mmap address, value first is mallocsize, second is recordstatistic data pointer
    std::unordered_map<uint64_t, std::pair<uint64_t, RecordStatistic*>> allocAddrMap_ {ALLOC_ADDRMAMP_SIZE};
    bool isProtobufSerialize_ = true;
    bool isDlopenRangeValid_ = false;
};

#endif // STACK_PREPROCESS_H
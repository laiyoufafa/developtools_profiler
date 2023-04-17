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
#include "hook_standalone.h"
#include <functional>
#include <linux/types.h>
#include "utilities.h"
#include "hook_common.h"
#include "hook_service.h"
#include "logging.h"
#include "share_memory_allocator.h"
#include "event_notifier.h"
#include "epoll_event_poller.h"
#include "virtual_runtime.h"
#include "stack_preprocess.h"

using namespace OHOS::Developtools::NativeDaemon;
namespace OHOS {
namespace Developtools {
namespace Profiler {
namespace Hook {
const int DEFAULT_EVENT_POLLING_INTERVAL = 5000;
const int MOVE_BIT_16 = 16;
const int MOVE_BIT_32 = 32;
std::string g_smbName = "hooknativesmb";
std::shared_ptr<VirtualRuntime> g_runtimeInstance;
std::unique_ptr<EpollEventPoller> g_eventPoller_;
std::shared_ptr<ShareMemoryBlock> g_shareMemoryBlock;
std::shared_ptr<EventNotifier> g_eventNotifier;
std::shared_ptr<HookService> g_hookService;
std::shared_ptr<StackPreprocess> g_stackPreprocess;
uint32_t g_maxStackDepth;
bool g_unwindErrorFlag = false;
bool g_fpUnwind = false;
std::unique_ptr<FILE, decltype(&fclose)> g_fpHookFile(nullptr, nullptr);
std::string g_libcSoPath;
uint32_t g_dlopenFrameIdx;
uint64_t g_dlopenIpMax;
uint64_t g_dlopenIpMin;
bool g_isDlopenRangeValid = false;


void WriteFrames(BaseStackRawData *data, const std::vector<CallFrame>& callFrames)
{
    if (data->type == MALLOC_MSG) {
        fprintf(g_fpHookFile.get(), "malloc;%" PRId64 ";%ld;0x%" PRIx64 ";%zu\n",
                (int64_t)data->ts.tv_sec, data->ts.tv_nsec, (uint64_t)data->addr, data->mallocSize);
    } else if (data->type == FREE_MSG) {
        fprintf(g_fpHookFile.get(), "free;%" PRId64 ";%ld;0x%" PRIx64 "\n",
                (int64_t)data->ts.tv_sec, data->ts.tv_nsec, (uint64_t)data->addr);
    } else if (data->type == MMAP_MSG) {
        fprintf(g_fpHookFile.get(), "mmap;%" PRId64 ";%ld;0x%" PRIx64 ";%zu\n",
                (int64_t)data->ts.tv_sec, data->ts.tv_nsec, (uint64_t)data->addr, data->mallocSize);
    } else if (data->type == MUNMAP_MSG) {
        fprintf(g_fpHookFile.get(), "munmap;%" PRId64 ";%ld;0x%" PRIx64 ";%zu\n",
                (int64_t)data->ts.tv_sec, data->ts.tv_nsec, (uint64_t)data->addr, data->mallocSize);
    }  else if (data->type == PR_SET_VMA_MSG) {
        fprintf(g_fpHookFile.get(), "prctl;%" PRId64 ";%ld;0x%" PRIx64 ";%zu;%s\n",
                (int64_t)data->ts.tv_sec, data->ts.tv_nsec, (uint64_t)data->addr, data->mallocSize, data->tname);
    } else {
        return;
    }

    for (size_t idx = 0; idx < callFrames.size(); ++idx) {
        auto item = callFrames[idx];
        (void)fprintf(g_fpHookFile.get(), "0x%" PRIx64 ";0x%" PRIx64 ";%s;%s;0x%" PRIx64 ";%" PRIu64 "\n",
                      item.ip_, item.sp_, std::string(item.symbolName_).c_str(),
                      std::string(item.filePath_).c_str(), item.offset_, item.symbolOffset_);
    }
    fflush(g_fpHookFile.get());
}

void ReadShareMemory(uint64_t duration, const std::string& performance_filename)
{
    CHECK_NOTNULL(g_shareMemoryBlock, NO_RETVAL, "smb is null!");
    uint64_t value = g_eventNotifier->Take();

    static bool first_flag = true;
    static bool end_flag = false;
    static uint64_t times = 0;

    struct timespec first_time;
    struct timespec begin_time;
    struct timespec end_time;
    std::vector<u64> u64regs;
    std::vector<CallFrame> callFrames;
    uint64_t total_time = 0;
#if defined(__arm__)
    u64regs.resize(PERF_REG_ARM_MAX);
#else
    u64regs.resize(PERF_REG_ARM64_MAX);
#endif
    callFrames.reserve(g_maxStackDepth);
    auto rawData = std::make_unique<StandaloneRawStack>();
    uint16_t  rawRealSize = 0;
    while (true) {
        bool ret = g_shareMemoryBlock->TakeData([&](const int8_t data[], uint32_t size) -> bool {
            if (size < sizeof(BaseStackRawData)) {
                HILOG_ERROR(LOG_CORE, "stack data invalid!");
                return false;
            }
            rawData->stackConext = reinterpret_cast<BaseStackRawData *>(const_cast<int8_t *>(data));
            rawData->data = const_cast<int8_t *>(data) + sizeof(BaseStackRawData);

            callFrames.clear();
            if (g_fpUnwind) {
                rawData->fpDepth = (size - sizeof(BaseStackRawData)) / sizeof(uint64_t);
                uint64_t* fpIp = reinterpret_cast<uint64_t *>(rawData->data);
                for (int idx = 0; idx < rawData->fpDepth; ++idx) {
                    if (fpIp[idx] == 0) {
                        break;
                    }
                    callFrames.push_back(fpIp[idx]);
                }
            } else {
                rawRealSize = sizeof(BaseStackRawData) + MAX_REG_SIZE * sizeof(char);
                rawData->stackSize = size - rawRealSize;
                if (rawData->stackSize > 0) {
                    rawData->stackData = reinterpret_cast<uint8_t *>(const_cast<int8_t *>(data)) + rawRealSize;
                }
#if defined(__arm__)
                uint32_t *regAddrArm = reinterpret_cast<uint32_t *>(rawData->data);
                u64regs.assign(regAddrArm, regAddrArm + PERF_REG_ARM_MAX);
#else
                if (memcpy_s(u64regs.data(), sizeof(uint64_t) * PERF_REG_ARM64_MAX, rawData->data,
                    sizeof(uint64_t) * PERF_REG_ARM64_MAX) != EOK) {
                    HILOG_ERROR(LOG_CORE, "memcpy_s regs failed");
                }
#endif
            }

            if (!end_flag && duration != 0) {
                clock_gettime(CLOCK_REALTIME, &begin_time);
                if (first_flag) {
                    first_flag = false;
                    first_time = begin_time;
                }
            }

            bool ret = g_runtimeInstance->UnwindStack(u64regs, rawData->stackData, rawData->stackSize,
                rawData->stackConext->pid, rawData->stackConext->tid, callFrames,
                (g_maxStackDepth > 0) ? g_maxStackDepth + FILTER_STACK_DEPTH : MAX_CALL_FRAME_UNWIND_SIZE);
            if (!ret) {
                HILOG_ERROR(LOG_CORE, "unwind fatal error");
                return false;
            }
            if (!g_isDlopenRangeValid) {
                g_runtimeInstance->CalcDlopenIpRange(g_libcSoPath, g_dlopenIpMax, g_dlopenIpMin);
                g_isDlopenRangeValid = true;
            }
            if (rawData->stackConext->type == MMAP_MSG) {
                // if mmap msg trigger by dlopen, update maps voluntarily
                if (callFrames.size() > g_dlopenFrameIdx) {
                    // for dlopen mmap framme
                    if (callFrames[g_dlopenFrameIdx].ip_ >= g_dlopenIpMin &&
                            callFrames[g_dlopenFrameIdx].ip_ < g_dlopenIpMax) {
                        HILOG_DEBUG(LOG_CORE, "mmap msg trigger by dlopen, update maps voluntarily");
                        g_runtimeInstance->UpdateMaps(rawData->stackConext->pid, rawData->stackConext->tid);
                    }
                }
            }

            if (!end_flag && duration != 0) {
                clock_gettime(CLOCK_REALTIME, &end_time);
                total_time += (end_time.tv_sec - begin_time.tv_sec) * 1000000000LLU +
                    (end_time.tv_nsec - begin_time.tv_nsec);
                ++times;
                if (end_time.tv_sec - first_time.tv_sec >= static_cast<long>(duration)) {
                    end_flag = true;
                    FILE *fp = fopen(performance_filename.c_str(), "a");
                    if (fp) {
                        time_t now = time(NULL);
                        struct tm now_tm;
                        localtime_r(&now, &now_tm);
                        fprintf(fp, "Current time: %04d-%02d-%02d %02d:%02d:%02d\n", now_tm.tm_year + 1900,
                                now_tm.tm_mon + 1, now_tm.tm_mday, now_tm.tm_hour, now_tm.tm_min, now_tm.tm_sec);
                        fprintf(fp, "Total durations: %" PRIu64 " nanoseconds\n", total_time);
                        fprintf(fp, "Total times: %" PRIu64 "\n", times);
                        fprintf(fp, "Average unwinding stack time: %.2f  nanoseconds\n\n",
                                (double)(total_time) / times);
                        fclose(fp);
                        printf("Performance data has already been generated.\n");
                    }
                }
            }

            WriteFrames(rawData->stackConext, callFrames);

            return true;
        });
        if (!ret) {
            break;
        }
    }
}

bool StartHook(HookData& hookData)
{
    g_runtimeInstance = std::make_shared<VirtualRuntime>();
    g_stackPreprocess = std::make_shared<StackPreprocess>(hookData.fpUnwind);
    g_libcSoPath = g_stackPreprocess->GetLibcSoPath();
    g_dlopenFrameIdx = g_stackPreprocess->GetDlopenFrameIdx();
    g_dlopenIpMax = g_stackPreprocess->GetDlopenIpMax();
    g_dlopenIpMin = g_stackPreprocess->GetDlopenIpMin();
    FILE *fp = fopen(hookData.fileName.c_str(), "wb+");
    if (fp != nullptr) {
        g_fpHookFile.reset();
        g_fpHookFile = std::unique_ptr<FILE, decltype(&fclose)>(fp, fclose);
    }
    if (g_fpHookFile == nullptr) {
        printf("create file failed!\n");
        return false;
    }
    // create smb and eventNotifier
    g_shareMemoryBlock = ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal(g_smbName, hookData.smbSize);
    g_eventNotifier = EventNotifier::Create(0, EventNotifier::NONBLOCK);
    // start event poller task
    g_eventPoller_ = std::make_unique<EpollEventPoller>(DEFAULT_EVENT_POLLING_INTERVAL);
    g_eventPoller_->Init();
    g_eventPoller_->Start();
    g_eventPoller_->AddFileDescriptor(g_eventNotifier->GetFd(),
                                      std::bind(ReadShareMemory, hookData.duration, hookData.performance_filename));

    HILOG_INFO(LOG_CORE, "hookservice smbFd = %d, eventFd = %d\n", g_shareMemoryBlock->GetfileDescriptor(),
               g_eventNotifier->GetFd());

    // hook config |F F            F F              F F F F       F F F F      F F F F|
    //              stack depth    malloctype       filtersize    sharememory  size
#if defined(__arm__)
    hookData.fpUnwind = false;
#endif
    if (hookData.maxStackDepth < DLOPEN_MIN_UNWIND_DEPTH) {
        // set default max depth
        hookData.maxStackDepth = DLOPEN_MIN_UNWIND_DEPTH;
    }
    uint64_t hookConfig = (uint8_t)hookData.maxStackDepth;
    const int moveBit8 = 8;
    hookConfig <<= moveBit8;

    hookConfig |= hookData.mallocDisable ? MALLOCDISABLE : 0;
    hookConfig |= hookData.mmapDisable ? MMAPDISABLE : 0;
    hookConfig |= hookData.freemsgstack ? FREEMSGSTACK : 0;
    hookConfig |= hookData.munmapmsgstack ? MUNMAPMSGSTACK : 0;
    hookConfig |= hookData.fpUnwind ? FPUNWIND : 0;

    hookConfig <<= MOVE_BIT_16;
    hookConfig |= hookData.filterSize;
    hookConfig <<= MOVE_BIT_32;
    hookConfig |= hookData.smbSize;

    HILOG_INFO(LOG_CORE, "hookConfig = 0x%" PRIx64 ",  hookservice smbFd = %d, eventFd = %d\n",
        hookConfig, g_shareMemoryBlock->GetfileDescriptor(), g_eventNotifier->GetFd());

    g_hookService = std::make_shared<HookService>(g_shareMemoryBlock->GetfileDescriptor(),
        g_eventNotifier->GetFd(), hookData.pid, hookData.processName, hookConfig);

    g_maxStackDepth = hookData.maxStackDepth;
    g_fpUnwind = hookData.fpUnwind;
    return true;
}
} // namespace Hook
} // namespace Profiler
} // namespace Developtools
} // namespace OHOS

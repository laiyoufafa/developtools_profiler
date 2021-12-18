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
#include "hook_service.h"
#include "logging.h"
#include "share_memory_allocator.h"
#include "event_notifier.h"
#include "epoll_event_poller.h"
#include "virtual_runtime.h"

namespace OHOS {
namespace Developtools {
namespace Profiler {
namespace Hook {
const int DEFAULT_EVENT_POLLING_INTERVAL = 5000;
std::string g_smbName = "hooknativesmb";
std::shared_ptr<OHOS::Developtools::NativeDaemon::VirtualRuntime> g_runtimeInstance;
std::unique_ptr<EpollEventPoller> g_eventPoller_;
std::shared_ptr<ShareMemoryBlock> g_shareMemoryBlock;
std::shared_ptr<EventNotifier> g_eventNotifier;
std::shared_ptr<HookService> g_hookService;
uint32_t g_maxStackDepth;
std::unique_ptr<FILE, decltype(&fclose)> g_fpHookFile(nullptr, nullptr);

void writeFrames(int type, const struct timespec& ts, void* addr, uint32_t mallocSize,
    const std::vector<OHOS::Developtools::NativeDaemon::CallFrame>& callsFrames)
{
    if (type == 0) {
        fprintf(g_fpHookFile.get(), "malloc;%" PRId64 ";%ld;0x%" PRIx64 ";%u\n", ts.tv_sec, ts.tv_nsec,
            (uint64_t)addr, mallocSize);
    } else if (type == 1) {
        fprintf(g_fpHookFile.get(), "free;%" PRId64 ";%ld;0x%" PRIx64 "\n", ts.tv_sec, ts.tv_nsec,
            (uint64_t)addr);
    } else {
        return;
    }

    for (size_t idx = 0, size = callsFrames.size(); idx < size; ++idx) {
        (void)fprintf(g_fpHookFile.get(), "0x%" PRIx64 ";0x%" PRIx64 ";%s;%s;0x%" PRIx64 ";%" PRIu64 "\n",
                      callsFrames[idx].ip_, callsFrames[idx].sp_, callsFrames[idx].symbolName_.c_str(),
                      callsFrames[idx].filePath_.c_str(), callsFrames[idx].offset_, callsFrames[idx].symbolOffset_);
    }
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
    uint64_t total_time = 0;

    while (true) {
        bool ret = g_shareMemoryBlock->TakeData([&](const int8_t data[], uint32_t size) -> bool {
            std::vector<u64> u64regs;
            uint32_t *regAddr = nullptr;
            uint32_t stackSize;
            pid_t tid;
            pid_t pid;
            void *addr = nullptr;
            int8_t* tmp = const_cast<int8_t *>(data);

            struct timespec ts = {};
            if (memcpy_s(&ts, sizeof(ts), data, sizeof(ts)) != EOK) {
                HILOG_ERROR(LOG_CORE, "memcpy_s ts failed");
            }
            uint32_t type = *(reinterpret_cast<uint32_t *>(tmp + sizeof(ts)));
            uint32_t mallocSize = *(reinterpret_cast<uint32_t *>(tmp + sizeof(ts) + sizeof(type)));
            addr = *(reinterpret_cast<void **>(tmp + sizeof(ts) + sizeof(type) + sizeof(mallocSize)));
            stackSize = *(reinterpret_cast<uint32_t *>(tmp + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *)));
            std::unique_ptr<uint8_t[]> stackData = std::make_unique<uint8_t[]>(stackSize);
            if (memcpy_s(stackData.get(), stackSize, tmp + sizeof(stackSize) + sizeof(ts) + sizeof(type)
                + sizeof(mallocSize) + sizeof(void *), stackSize) != EOK) {
                HILOG_ERROR(LOG_CORE, "memcpy_s data failed");
            }
            tid = *(reinterpret_cast<pid_t *>(tmp + sizeof(stackSize) + stackSize + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *)));
            pid = *(reinterpret_cast<pid_t *>(tmp + sizeof(stackSize) + stackSize + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *) + sizeof(tid)));
            regAddr = reinterpret_cast<uint32_t *>(tmp + sizeof(tid) + sizeof(pid) + sizeof(stackSize)
                + stackSize + sizeof(ts) + sizeof(type) + sizeof(mallocSize) + sizeof(void *));

            int reg_count = (size - sizeof(tid) - sizeof(pid) - sizeof(stackSize) - stackSize
                - sizeof(ts) - sizeof(type) - sizeof(mallocSize) - sizeof(void *))
                / sizeof(uint32_t);
            if (reg_count <= 0) {
                HILOG_ERROR(LOG_CORE, "data error size = %u", size);
            }
            for (int idx = 0; idx < reg_count; ++idx) {
                u64regs.push_back(*regAddr++);
            }

            if (!end_flag && duration != 0) {
                clock_gettime(CLOCK_REALTIME, &begin_time);
                if (first_flag) {
                    first_flag = false;
                    first_time = begin_time;
                }
            }

            std::vector<OHOS::Developtools::NativeDaemon::CallFrame> callsFrames;
            g_runtimeInstance->UnwindStack(u64regs, stackData.get(), stackSize, pid, tid, callsFrames,
                (g_maxStackDepth > 0) ? g_maxStackDepth : OHOS::Developtools::NativeDaemon::MAX_CALL_FRAME_UNWIND_SIZE);

            if (!end_flag && duration != 0) {
                clock_gettime(CLOCK_REALTIME, &end_time);
                total_time += (end_time.tv_sec - begin_time.tv_sec) * 1000000000LLU +
                    (end_time.tv_nsec - begin_time.tv_nsec);
                ++times;
                if (end_time.tv_sec - first_time.tv_sec >= duration) {
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

            writeFrames(type, ts, addr, mallocSize, callsFrames);
            return true;
        });
        if (!ret) {
            break;
        }
    }
}

bool StartHook(HookData& hookData)
{
    g_runtimeInstance = std::make_shared<OHOS::Developtools::NativeDaemon::VirtualRuntime>();
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

    // start service init socket
    g_hookService = std::make_shared<HookService>(g_shareMemoryBlock->GetfileDescriptor(),
        g_eventNotifier->GetFd(), hookData.filterSize, hookData.smbSize, hookData.pid, hookData.processName);

    g_maxStackDepth = hookData.maxStackDepth;
    return true;
}
} // namespace Hook
} // namespace Profiler
} // namespace Developtools
} // namespace OHOS
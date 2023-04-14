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

#ifndef HOOK_COMMON_H
#define HOOK_COMMON_H

#if HAVE_LIBUNWIND
// for libunwind.h empty struct has size 0 in c, size 1 in c++
#define UNW_EMPTY_STRUCT uint8_t unused
#include <libunwind.h>
#endif

#include "register.h"
#include "utilities.h"

#define MAX_THREAD_NAME (32)
#define MAX_UNWIND_DEPTH (100)

namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
const int STACK_DATA_SIZE = 40000;
const int SPEED_UP_THRESHOLD = STACK_DATA_SIZE / 2;
const int SLOW_DOWN_THRESHOLD = STACK_DATA_SIZE / 4;
const int32_t MIN_STACK_DEPTH = 6;
// filter two layers of dwarf stack in libnative_hook.z.so
const size_t FILTER_STACK_DEPTH = 2;
const size_t MAX_CALL_FRAME_UNWIND_SIZE = MAX_UNWIND_DEPTH + FILTER_STACK_DEPTH;
// dlopen function minimum stack depth
const int32_t DLOPEN_MIN_UNWIND_DEPTH = 5;
}
}
}

constexpr size_t MAX_REG_SIZE = sizeof(uint64_t)
    * OHOS::Developtools::NativeDaemon::PERF_REG_ARM64_MAX;

enum {
    MALLOCDISABLE = (1u << 0),
    MMAPDISABLE = (1u << 1),
    FREEMSGSTACK = (1u << 2),
    MUNMAPMSGSTACK = (1u << 3),
    FPUNWIND = (1u << 4),
    BLOCKED = (1u << 5),
    MEMTRACE_ENABLE = (1u << 6),
};

enum {
    MALLOC_MSG = 0,
    FREE_MSG,
    MMAP_MSG,
    MUNMAP_MSG,
    MEMORY_TAG,
    PR_SET_VMA_MSG,
};

struct alignas(8) BaseStackRawData { // 8 is 8 bit
    char tname[MAX_THREAD_NAME];
    struct timespec ts;
    void* addr;
    size_t mallocSize;
    uint32_t pid;
    uint32_t tid;
    uint32_t type;
};

struct alignas(8) StackRawData: public BaseStackRawData { // 8 is 8 bit
    union {
        char regs[MAX_REG_SIZE];
        uint64_t ip[MAX_UNWIND_DEPTH] = {0};
    };
};

typedef struct {
    uint32_t filterSize_;
    bool mallocDisable_;
    bool mmapDisable_;
    bool freeStackData_;
    bool munmapStackData_;
    uint8_t maxStackDepth_;
    bool fpunwind_;
    bool isBlocked;
    bool memtraceEnable;
} ClientConfig;

struct StandaloneRawStack {
    BaseStackRawData* stackConext; // points to the foundation type data
    uint8_t* stackData;
    int8_t* data; // fp mode data is ip, dwarf mode data is regs
    uint32_t stackSize;
    uint8_t fpDepth; // fp mode fpDepth is ip depth, dwarf mode is invalid
};

#endif // HOOK_SERVICE_H

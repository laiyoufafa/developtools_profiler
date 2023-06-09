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

#include <atomic>
#include <climits>
#include <dlfcn.h>
#include <fcntl.h>
#include <string>
#include <sys/time.h>

#include "hook_common.h"
#include "hook_socket_client.h"
#include "musl_preinit_common.h"
#include "stack_writer.h"
#include "runtime_stack_range.h"
#include "register.h"
#include "virtual_runtime.h"
#include "get_thread_id.h"
#include "hook_client.h"

static __thread bool ohos_malloc_hook_enable_hook_flag = true;

namespace {
using OHOS::Developtools::NativeDaemon::buildArchType;
std::shared_ptr<HookSocketClient> g_hookClient;
std::recursive_mutex g_ClientMutex;
std::atomic<const MallocDispatchType*> g_dispatch {nullptr};

const MallocDispatchType* GetDispatch()
{
    return g_dispatch.load(std::memory_order_relaxed);
}

bool InititalizeIPC()
{
    return true;
}
void FinalizeIPC() { }
} // namespace

bool ohos_malloc_hook_on_start(void)
{
    std::lock_guard<std::recursive_mutex> guard(g_ClientMutex);

    if (g_hookClient == nullptr) {
        g_hookClient = std::make_shared<HookSocketClient>(getpid());
    }
    return true;
}

bool ohos_malloc_hook_on_end(void)
{
    std::lock_guard<std::recursive_mutex> guard(g_ClientMutex);
    g_hookClient = nullptr;

    return true;
}

void* hook_malloc(void* (*fn)(size_t), size_t size)
{
    void* ret = nullptr;
    if (fn) {
        ret = fn(size);
    }

    if (g_hookClient == nullptr) {
        return ret;
    }
    if ((size < g_hookClient->GetFilterSize()) || g_hookClient->GetMallocDisable() ) {
        return ret;
    }

    int regCount = OHOS::Developtools::NativeDaemon::RegisterGetCount();
    if (regCount <= 0) {
        return ret;
    }
    uint32_t* regs = new (std::nothrow) uint32_t[regCount];
    if (!regs) {
        HILOG_ERROR(LOG_CORE, "new regs failed");
        return ret;
    }

#if defined(__arm__)
    asm volatile(
      "mov r3, r13\n"
      "mov r4, r15\n"
      "stmia %[base], {r3-r4}\n"
      : [ base ] "+r"(regs)
      :
      : "r3", "r4", "memory");
#endif
    const char* stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
    char* stackendptr = nullptr;
    GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
    int stackSize = stackendptr - stackptr;
    pid_t pid = getpid();
    pid_t tid = get_thread_id();

    struct timespec ts = {};
    clock_gettime(CLOCK_REALTIME, &ts);

    uint32_t type = MALLOC_MSG;

    size_t metaSize = sizeof(ts) + sizeof(type) + sizeof(size) + sizeof(void *)
        + sizeof(stackSize) + stackSize + sizeof(pid_t) + sizeof(pid_t) + regCount * sizeof(uint32_t);
    std::unique_ptr<uint8_t[]> buffer = std::make_unique<uint8_t[]>(metaSize);
    size_t totalSize = metaSize;

    if (memcpy_s(buffer.get(), totalSize, &ts, sizeof(ts)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s ts failed");
    }
    metaSize = sizeof(ts);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &type, sizeof(type)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s type failed");
    }
    metaSize += sizeof(type);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &size, sizeof(size)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s size failed");
    }
    metaSize += sizeof(size);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &ret, sizeof(void *)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s ret failed");
    }
    metaSize += sizeof(void *);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &stackSize, sizeof(stackSize)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackSize failed");
    }
    metaSize += sizeof(stackSize);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, stackptr, stackSize) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += stackSize;
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &pid, sizeof(pid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &tid, sizeof(tid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s tid failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, regs, regCount * sizeof(uint32_t)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s regs failed");
    }
    metaSize += regCount * sizeof(uint32_t);
    delete[] regs;

    std::lock_guard<std::recursive_mutex> guard(g_ClientMutex);
    if (g_hookClient != nullptr) {
        g_hookClient->SendStack(buffer.get(), metaSize);
    }
    return ret;
}

void* hook_valloc(void* (*fn)(size_t), size_t size)
{
    void* pRet = nullptr;
    if (fn) {
        pRet = fn(size);
    }
    return pRet;
}

void* hook_calloc(void* (*fn)(size_t, size_t), size_t number, size_t size)
{
    void* pRet = nullptr;
    if (fn) {
        pRet = fn(number, size);
    }
    return pRet;
}

void* hook_memalign(void* (*fn)(size_t, size_t), size_t align, size_t bytes)
{
    void* pRet = nullptr;
    if (fn) {
        pRet = fn(align, bytes);
    }
    return pRet;
}

void* hook_realloc(void* (*fn)(void *, size_t), void* ptr, size_t size)
{
    void* pRet = nullptr;
    if (fn) {
        pRet = fn(ptr, size);
    }

    return pRet;
}

size_t hook_malloc_usable_size(size_t (*fn)(void*), void* ptr)
{
    size_t ret = 0;
    if (fn) {
        ret = fn(ptr);
    }

    return ret;
}

void hook_free(void (*free_func)(void*), void *p)
{
    if (free_func) {
        free_func(p);
    }

    if (g_hookClient == nullptr) {
        return;
    }

    if (g_hookClient->GetMallocDisable()) {
        return;
    }

    int regCount = OHOS::Developtools::NativeDaemon::RegisterGetCount();
    if (regCount <= 0) {
        return;
    }
    uint32_t* regs = new (std::nothrow) uint32_t[regCount];
    if (!regs) {
        HILOG_ERROR(LOG_CORE, "new regs failed");
        return;
    }
#if defined(__arm__)
    asm volatile(
      "mov r3, r13\n"
      "mov r4, r15\n"
      "stmia %[base], {r3-r4}\n"
      : [ base ] "+r"(regs)
      :
      : "r3", "r4", "memory");
#endif
    const char* stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
    char* stackendptr = nullptr;
    GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
    int stackSize = stackendptr - stackptr;
    pid_t tid = get_thread_id();
    pid_t pid = getpid();
    uint32_t type = FREE_MSG;
    struct timespec ts = {};
    clock_gettime(CLOCK_REALTIME, &ts);

    size_t metaSize = sizeof(ts) + sizeof(type) + sizeof(uint32_t) + sizeof(void *)
        + sizeof(stackSize) + stackSize + sizeof(pid_t) + sizeof(pid_t) + regCount * sizeof(uint32_t);
    std::unique_ptr<uint8_t[]> buffer = std::make_unique<uint8_t[]>(metaSize);
    int totalSize = metaSize;

    if (memcpy_s(buffer.get(), totalSize, &ts, sizeof(ts)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s ts failed");
    }
    metaSize = sizeof(ts);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &type, sizeof(type)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s type failed");
    }
    metaSize += sizeof(type);
    if (memset_s(buffer.get() + metaSize, totalSize - metaSize, 0, sizeof(uint32_t)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memset_s data failed");
    }
    metaSize += sizeof(uint32_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &p, sizeof(void *)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s ptr failed");
    }
    metaSize += sizeof(void *);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &stackSize, sizeof(stackSize)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackSize failed");
    }
    metaSize += sizeof(stackSize);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, stackptr, stackSize) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += stackSize;
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &pid, sizeof(pid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s pid failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &tid, sizeof(tid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s tid failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, regs, regCount * sizeof(uint32_t)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s regs failed");
    }
    metaSize += regCount * sizeof(uint32_t);

    delete[] regs;
    std::lock_guard<std::recursive_mutex> guard(g_ClientMutex);
    if (g_hookClient != nullptr) {
        g_hookClient->SendStack(buffer.get(), metaSize);
    }
}

void* hook_mmap(void*(*fn)(void*, size_t, int, int, int, off_t),
    void* addr, size_t length, int prot, int flags, int fd, off_t offset)
{
    void* ret = nullptr;
    if (fn) {
        ret = fn(addr, length, prot, flags, fd, offset);
    }

    if (g_hookClient == nullptr) {
        return ret;
    }

    if (g_hookClient->GetMmapDisable()) {
        return ret;
    }

    int regCount = OHOS::Developtools::NativeDaemon::RegisterGetCount();
    if (regCount <= 0) {
        return ret;
    }
    uint32_t* regs = new (std::nothrow) uint32_t[regCount];
    if (!regs) {
        HILOG_ERROR(LOG_CORE, "new regs failed");
        return ret;
    }

#if defined(__arm__)
    asm volatile(
      "mov r3, r13\n"
      "mov r4, r15\n"
      "stmia %[base], {r3-r4}\n"
      : [ base ] "+r"(regs)
      :
      : "r3", "r4", "memory");
#endif
    const char* stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
    char* stackendptr = nullptr;
    GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
    int stackSize = stackendptr - stackptr;
    pid_t pid = getpid();
    pid_t tid = get_thread_id();

    struct timespec ts = {};
    clock_gettime(CLOCK_REALTIME, &ts);

    uint32_t type = MMAP_MSG;

    size_t metaSize = sizeof(ts) + sizeof(type) + sizeof(length) + sizeof(void *)
        + sizeof(stackSize) + stackSize + sizeof(pid_t) + sizeof(pid_t) + regCount * sizeof(uint32_t);
    std::unique_ptr<uint8_t[]> buffer = std::make_unique<uint8_t[]>(metaSize);
    size_t totalSize = metaSize;

    if (memcpy_s(buffer.get(), totalSize, &ts, sizeof(ts)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s ts failed");
    }
    metaSize = sizeof(ts);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &type, sizeof(type)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s type failed");
    }
    metaSize += sizeof(type);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &length, sizeof(length)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s length failed");
    }
    metaSize += sizeof(length);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &ret, sizeof(void *)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s addr failed");
    }
    metaSize += sizeof(ret);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &stackSize, sizeof(stackSize)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackSize failed");
    }
    metaSize += sizeof(stackSize);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, stackptr, stackSize) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += stackSize;
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &pid, sizeof(pid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &tid, sizeof(tid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s tid failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, regs, regCount * sizeof(uint32_t)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s regs failed");
    }
    metaSize += regCount * sizeof(uint32_t);
    delete[] regs;

    std::lock_guard<std::recursive_mutex> guard(g_ClientMutex);
    if (g_hookClient != nullptr) {
        g_hookClient->SendStack(buffer.get(), metaSize);
    }
    return ret;
}

int hook_munmap(int(*fn)(void*, size_t), void* addr, size_t length)
{
    int ret = -1;
    if (fn) {
        ret = fn(addr, length);
    }

    if (g_hookClient == nullptr) {
        return ret;
    }

    if (g_hookClient->GetMmapDisable()) {
        return ret;
    }

    int regCount = OHOS::Developtools::NativeDaemon::RegisterGetCount();
    if (regCount <= 0) {
        return ret;
    }
    uint32_t* regs = new (std::nothrow) uint32_t[regCount];
    if (!regs) {
        HILOG_ERROR(LOG_CORE, "new regs failed");
        return ret;
    }

#if defined(__arm__)
    asm volatile(
      "mov r3, r13\n"
      "mov r4, r15\n"
      "stmia %[base], {r3-r4}\n"
      : [ base ] "+r"(regs)
      :
      : "r3", "r4", "memory");
#endif
    const char* stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
    char* stackendptr = nullptr;
    GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
    int stackSize = stackendptr - stackptr;
    pid_t pid = getpid();
    pid_t tid = get_thread_id();

    struct timespec ts = {};
    clock_gettime(CLOCK_REALTIME, &ts);

    uint32_t type = MUNMAP_MSG;

    size_t metaSize = sizeof(ts) + sizeof(type) + sizeof(length) + sizeof(void *)
        + sizeof(stackSize) + stackSize + sizeof(pid_t) + sizeof(pid_t) + regCount * sizeof(uint32_t);

    std::unique_ptr<uint8_t[]> buffer = std::make_unique<uint8_t[]>(metaSize);
    size_t totalSize = metaSize;

    if (memcpy_s(buffer.get(), totalSize, &ts, sizeof(ts)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s ts failed");
    }
    metaSize = sizeof(ts);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &type, sizeof(type)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s type failed");
    }
    metaSize += sizeof(type);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &length, sizeof(length)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s length failed");
    }
    metaSize += sizeof(length);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &addr, sizeof(void *)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s addr failed");
    }
    metaSize += sizeof(addr);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &stackSize, sizeof(stackSize)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackSize failed");
    }
    metaSize += sizeof(stackSize);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, stackptr, stackSize) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += stackSize;
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &pid, sizeof(pid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s stackptr failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, &tid, sizeof(tid)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s tid failed");
    }
    metaSize += sizeof(pid_t);
    if (memcpy_s(buffer.get() + metaSize, totalSize - metaSize, regs, regCount * sizeof(uint32_t)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s regs failed");
    }
    metaSize += regCount * sizeof(uint32_t);
    delete[] regs;

    std::lock_guard<std::recursive_mutex> guard(g_ClientMutex);
    if (g_hookClient != nullptr) {
        g_hookClient->SendStack(buffer.get(), metaSize);
    }
    return ret;
}

bool ohos_malloc_hook_initialize(const MallocDispatchType*malloc_dispatch, bool*, const char*)
{
    g_dispatch.store(malloc_dispatch);
    InititalizeIPC();
    return true;
}
void ohos_malloc_hook_finalize(void)
{
    FinalizeIPC();
}

void* ohos_malloc_hook_malloc(size_t size)
{
    __set_hook_flag(false);
    void* ret = hook_malloc(GetDispatch()->malloc, size);
    __set_hook_flag(true);
    return ret;
}

void* ohos_malloc_hook_realloc(void* ptr, size_t size)
{
    __set_hook_flag(false);
    void* ret = hook_realloc(GetDispatch()->realloc, ptr, size);
    __set_hook_flag(true);
    return ret;
}

void* ohos_malloc_hook_calloc(size_t number, size_t size)
{
    __set_hook_flag(false);
    void* ret = hook_calloc(GetDispatch()->calloc, number, size);
    __set_hook_flag(true);
    return ret;
}

void* ohos_malloc_hook_valloc(size_t size)
{
    __set_hook_flag(false);
    void* ret = hook_valloc(GetDispatch()->valloc, size);
    __set_hook_flag(true);
    return ret;
}

void ohos_malloc_hook_free(void* p)
{
    __set_hook_flag(false);
    hook_free(GetDispatch()->free, p);
    __set_hook_flag(true);
}

void* ohos_malloc_hook_memalign(size_t alignment, size_t bytes)
{
    __set_hook_flag(false);
    void* ret = hook_memalign(GetDispatch()->memalign, alignment, bytes);
    __set_hook_flag(true);
    return ret;
}

size_t ohos_malloc_hook_malloc_usable_size(void* mem)
{
    __set_hook_flag(false);
    size_t ret = hook_malloc_usable_size(GetDispatch()->malloc_usable_size, mem);
    __set_hook_flag(true);
    return ret;
}

bool ohos_malloc_hook_get_hook_flag(void)
{
    return ohos_malloc_hook_enable_hook_flag;
}

bool ohos_malloc_hook_set_hook_flag(bool flag)
{
    bool before_lag = ohos_malloc_hook_enable_hook_flag;
    ohos_malloc_hook_enable_hook_flag = flag;
    return before_lag;
}

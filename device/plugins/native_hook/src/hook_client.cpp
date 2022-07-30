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
#include <pthread.h>
#include <sys/prctl.h>

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
static std::atomic<uint64_t> timeCost = 0;
static std::atomic<uint64_t> mallocTimes = 0;
static std::atomic<uint64_t> dataCounts = 0;
using OHOS::Developtools::NativeDaemon::buildArchType;
static std::shared_ptr<HookSocketClient> g_hookClient;
std::recursive_timed_mutex g_ClientMutex;
std::atomic<const MallocDispatchType*> g_dispatch {nullptr};
constexpr int TIMEOUT_MSEC = 2000;
constexpr int PRINT_INTERVAL = 5000;
constexpr uint64_t S_TO_NS = 1000 * 1000 * 1000;
const MallocDispatchType* GetDispatch()
{
    return g_dispatch.load(std::memory_order_relaxed);
}

bool InititalizeIPC()
{
    return true;
}
void FinalizeIPC() {}
}  // namespace

bool ohos_malloc_hook_on_start(void)
{
    std::lock_guard<std::recursive_timed_mutex> guard(g_ClientMutex);

    if (g_hookClient == nullptr) {
        g_hookClient = std::make_shared<HookSocketClient>(getpid());
    }
    GetMainThreadRuntimeStackRange();
    return true;
}

bool ohos_malloc_hook_on_end(void)
{
    std::lock_guard<std::recursive_timed_mutex> guard(g_ClientMutex);
    g_hookClient = nullptr;

    return true;
}

static void inline __attribute__((always_inline)) FpUnwind(int max_depth, uint64_t *ip, int stackSize)
{
    void **startfp = (void **)__builtin_frame_address(0);
    void **fp = startfp;
    for (int i = 0; i < max_depth; i++) {
        ip[i] = *(unsigned long *)(fp + 1);
        void **next_fp = (void **)*fp;
        if (next_fp <= fp) {
            break;
        }
        if (((next_fp - startfp) * sizeof(void *)) > stackSize) {
            break;
        }
        fp = next_fp;
    }
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
    if ((size < g_hookClient->GetFilterSize()) || g_hookClient->GetMallocDisable()) {
        return ret;
    }

#ifdef PERFORMANCE_DEBUG
    struct timespec start = {};
    clock_gettime(CLOCK_REALTIME, &start);
#endif
    StackRawData rawdata = {{{0}}};
    const char* stackptr = nullptr;
    const char* stackendptr = nullptr;
    int stackSize = 0;
    clock_gettime(CLOCK_REALTIME, &rawdata.ts);

    if (g_hookClient->GetFpunwind()) {
        stackptr = reinterpret_cast<const char*>(__builtin_frame_address(0));
        GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
        stackSize = stackendptr - stackptr;
        FpUnwind(g_hookClient->GetMaxStackDepth(), rawdata.ip, stackSize);
        stackSize = 0;
    } else {
        uint64_t* regs = reinterpret_cast<uint64_t*>(&(rawdata.regs));
#if defined(__arm__)
        asm volatile(
            "mov r3, r13\n"
            "mov r4, r15\n"
            "stmia %[base], {r3-r4}\n"
            : [ base ] "+r"(regs)
            :
            : "r3", "r4", "memory");
#elif defined(__aarch64__)
        asm volatile(
            "1:\n"
            "stp x28, x29, [%[base], #224]\n"
            "str x30, [%[base], #240]\n"
            "mov x12, sp\n"
            "adr x13, 1b\n"
            "stp x12, x13, [%[base], #248]\n"
            : [ base ] "+r"(regs)
            :
            : "x12", "x13", "memory");
        stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
        GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
        stackSize = stackendptr - stackptr;
#endif
    }
    rawdata.type = MALLOC_MSG;
    rawdata.pid = getpid();
    rawdata.tid = get_thread_id();
    rawdata.mallocSize = size;
    rawdata.addr = ret;
    prctl(PR_GET_NAME, rawdata.tname);

    std::unique_lock<std::recursive_timed_mutex> lck(g_ClientMutex, std::defer_lock);
    std::chrono::time_point<std::chrono::steady_clock> timeout =
        std::chrono::steady_clock::now() + std::chrono::milliseconds(TIMEOUT_MSEC);
    if (!lck.try_lock_until(timeout)) {
        HILOG_ERROR(LOG_CORE, "lock hook_malloc failed!");
        return ret;
    }
    if (g_hookClient != nullptr) {
        g_hookClient->SendStackWithPayload(&rawdata, sizeof(rawdata), stackptr, stackSize);
    }
#ifdef PERFORMANCE_DEBUG
    struct timespec end = {};
    clock_gettime(CLOCK_REALTIME, &end);
    timeCost += (end.tv_sec - start.tv_sec) * S_TO_NS + (end.tv_nsec - start.tv_nsec);
    mallocTimes++;
    dataCounts += stackSize;
    if (mallocTimes % PRINT_INTERVAL == 0) {
        HILOG_ERROR(LOG_CORE,
            "mallocTimes %" PRIu64" cost time = %" PRIu64" copy data bytes = %" PRIu64" mean cost = %" PRIu64"\n",
            mallocTimes.load(), timeCost.load(), dataCounts.load(), timeCost.load() / mallocTimes.load());
    }
#endif
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

void* hook_realloc(void* (*fn)(void*, size_t), void* ptr, size_t size)
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

void hook_free(void (*free_func)(void*), void* p)
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

    StackRawData rawdata = {{{0}}};
    const char* stackptr = nullptr;
    const char* stackendptr = nullptr;
    int stackSize = 0;
    clock_gettime(CLOCK_REALTIME, &rawdata.ts);

    if (g_hookClient->GetFreeStackData()) {
        if (g_hookClient->GetFpunwind()) {
            stackptr = reinterpret_cast<const char*>(__builtin_frame_address(0));
            GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
            stackSize = stackendptr - stackptr;
            FpUnwind(g_hookClient->GetMaxStackDepth(), rawdata.ip, stackSize);
            stackSize = 0;
        } else {
            uint64_t* regs = reinterpret_cast<uint64_t*>(&(rawdata.regs));
#if defined(__arm__)
            asm volatile(
                "mov r3, r13\n"
                "mov r4, r15\n"
                "stmia %[base], {r3-r4}\n"
                : [ base ] "+r"(regs)
                :
                : "r3", "r4", "memory");
#elif defined(__aarch64__)
            asm volatile(
                "1:\n"
                "stp x28, x29, [%[base], #224]\n"
                "str x30, [%[base], #240]\n"
                "mov x12, sp\n"
                "adr x13, 1b\n"
                "stp x12, x13, [%[base], #248]\n"
                : [ base ] "+r"(regs)
                :
                : "x12", "x13", "memory");
#endif
            stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
            GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
            stackSize = stackendptr - stackptr;
        }
    }

    rawdata.type = FREE_MSG;
    rawdata.pid = getpid();
    rawdata.tid = get_thread_id();
    rawdata.mallocSize = 0;
    rawdata.addr = p;
    prctl(PR_GET_NAME, rawdata.tname);

    std::unique_lock<std::recursive_timed_mutex> lck(g_ClientMutex, std::defer_lock);
    std::chrono::time_point<std::chrono::steady_clock> timeout =
        std::chrono::steady_clock::now() + std::chrono::milliseconds(TIMEOUT_MSEC);
    if (!lck.try_lock_until(timeout)) {
        HILOG_ERROR(LOG_CORE, "lock hook_free failed!");
        return;
    }
    if (g_hookClient != nullptr) {
        g_hookClient->SendStackWithPayload(&rawdata, sizeof(rawdata), stackptr, stackSize);
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

    StackRawData rawdata = {{{0}}};
    const char* stackptr = nullptr;
    const char* stackendptr = nullptr;
    int stackSize = 0;
    clock_gettime(CLOCK_REALTIME, &rawdata.ts);

    if (g_hookClient->GetFpunwind()) {
        stackptr = reinterpret_cast<const char*>(__builtin_frame_address(0));
        GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
        stackSize = stackendptr - stackptr;
        FpUnwind(g_hookClient->GetMaxStackDepth(), rawdata.ip, stackSize);
        stackSize = 0;
    } else {
        uint64_t* regs = reinterpret_cast<uint64_t*>(&(rawdata.regs));
#if defined(__arm__)
        asm volatile(
            "mov r3, r13\n"
            "mov r4, r15\n"
            "stmia %[base], {r3-r4}\n"
            : [ base ] "+r"(regs)
            :
            : "r3", "r4", "memory");
#elif defined(__aarch64__)
        asm volatile(
            "1:\n"
            "stp x28, x29, [%[base], #224]\n"
            "str x30, [%[base], #240]\n"
            "mov x12, sp\n"
            "adr x13, 1b\n"
            "stp x12, x13, [%[base], #248]\n"
            : [ base ] "+r"(regs)
            :
            : "x12", "x13", "memory");
#endif
        stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
        GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
        stackSize = stackendptr - stackptr;
    }

    rawdata.type = MMAP_MSG;
    rawdata.pid = getpid();
    rawdata.tid = get_thread_id();
    rawdata.mallocSize = length;
    rawdata.addr = ret;
    prctl(PR_GET_NAME, rawdata.tname);

    std::unique_lock<std::recursive_timed_mutex> lck(g_ClientMutex, std::defer_lock);
    std::chrono::time_point<std::chrono::steady_clock> timeout =
        std::chrono::steady_clock::now() + std::chrono::milliseconds(TIMEOUT_MSEC);
    if (!lck.try_lock_until(timeout)) {
        HILOG_ERROR(LOG_CORE, "lock hook_mmap failed!");
        return ret;
    }
    if (g_hookClient != nullptr) {
        g_hookClient->SendStackWithPayload(&rawdata, sizeof(rawdata), stackptr, stackSize);
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

    int stackSize = 0;
    StackRawData rawdata = {{{0}}};
    const char* stackptr = nullptr;
    const char* stackendptr = nullptr;
    clock_gettime(CLOCK_REALTIME, &rawdata.ts);

    if (g_hookClient->GetMunmapStackData()) {
        if (g_hookClient->GetFpunwind()) {
            stackptr = reinterpret_cast<const char*>(__builtin_frame_address(0));
            GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
            stackSize = stackendptr - stackptr;
            FpUnwind(g_hookClient->GetMaxStackDepth(), rawdata.ip, stackSize);
            stackSize = 0;
        } else {
            uint64_t* regs = reinterpret_cast<uint64_t*>(&(rawdata.regs));
#if defined(__arm__)
            asm volatile(
                "mov r3, r13\n"
                "mov r4, r15\n"
                "stmia %[base], {r3-r4}\n"
                : [ base ] "+r"(regs)
                :
                : "r3", "r4", "memory");
#elif defined(__aarch64__)
            asm volatile(
                "1:\n"
                "stp x28, x29, [%[base], #224]\n"
                "str x30, [%[base], #240]\n"
                "mov x12, sp\n"
                "adr x13, 1b\n"
                "stp x12, x13, [%[base], #248]\n"
                : [ base ] "+r"(regs)
                :
                : "x12", "x13", "memory");
#endif
            stackptr = reinterpret_cast<const char*>(regs[RegisterGetSP(buildArchType)]);
            GetRuntimeStackEnd(stackptr, &stackendptr);  // stack end pointer
            stackSize = stackendptr - stackptr;
        }
    }

    rawdata.type = MUNMAP_MSG;
    rawdata.pid = getpid();
    rawdata.tid = get_thread_id();
    rawdata.mallocSize = length;
    rawdata.addr = addr;
    prctl(PR_GET_NAME, rawdata.tname);

    std::unique_lock<std::recursive_timed_mutex> lck(g_ClientMutex, std::defer_lock);
    std::chrono::time_point<std::chrono::steady_clock> timeout =
        std::chrono::steady_clock::now() + std::chrono::milliseconds(TIMEOUT_MSEC);
    if (!lck.try_lock_until(timeout)) {
        HILOG_ERROR(LOG_CORE, "lock hook_munmap failed!");
        return ret;
    }
    if (g_hookClient != nullptr) {
        g_hookClient->SendStackWithPayload(&rawdata, sizeof(rawdata), stackptr, stackSize);
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

void* ohos_malloc_hook_mmap(void* addr, size_t length, int prot, int flags, int fd, off_t offset)
{
    __set_hook_flag(false);
    void* ret = hook_mmap(GetDispatch()->mmap, addr, length, prot, flags, fd, offset);
    __set_hook_flag(true);
    return ret;
}

int ohos_malloc_hook_munmap(void* addr, size_t length)
{
    __set_hook_flag(false);
    int ret = hook_munmap(GetDispatch()->munmap, addr, length);
    __set_hook_flag(true);
    return ret;
}

void ohos_malloc_hook_memtag(void* addr, size_t size, char* tag, size_t tagLen)
{
    __set_hook_flag(false);

    if (g_hookClient == nullptr) {
        return;
    }
    StackRawData rawdata = {{{0}}};
    clock_gettime(CLOCK_REALTIME, &rawdata.ts);
    rawdata.type = MEMORY_TAG;
    rawdata.pid = getpid();
    rawdata.tid = get_thread_id();
    rawdata.mallocSize = size;
    rawdata.addr = addr;

    if (memcpy_s(rawdata.tname, sizeof(rawdata.tname), tag, tagLen) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s tag failed");
    }
    rawdata.tname[sizeof(rawdata.tname) - 1] = '\0';

    std::unique_lock<std::recursive_timed_mutex> lck(g_ClientMutex, std::defer_lock);
    std::chrono::time_point<std::chrono::steady_clock> timeout =
        std::chrono::steady_clock::now() + std::chrono::milliseconds(TIMEOUT_MSEC);
    if (!lck.try_lock_until(timeout)) {
        HILOG_ERROR(LOG_CORE, "lock failed!");
        return;
    }
    if (g_hookClient != nullptr) {
        g_hookClient->SendStack(&rawdata, sizeof(rawdata));
    }
    __set_hook_flag(true);
}

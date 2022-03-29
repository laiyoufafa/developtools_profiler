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
#ifndef HIPERF_CALLSTACK_H
#define HIPERF_CALLSTACK_H

#if HAVE_LIBUNWIND
// for libunwind.h empty struct has size 0 in c, size 1 in c++
#define UNW_EMPTY_STRUCT uint8_t unused;
#include <libunwind.h>
#endif

#include <map>
#include <string>
#include <vector>

#if is_double_framework
#if defined(__linux__)
#if !is_mingw
#include <sys/mman.h>
#endif
#include <unwindstack/MachineArm.h>
#include <unwindstack/Maps.h>
#include <unwindstack/Regs.h>
#include <unwindstack/RegsArm.h>
#include <unwindstack/Unwinder.h>
#include <unwindstack/UserArm.h>
#endif
#endif

#include "virtual_thread.h"
#include "hashlistpp.h"
#include "perf_event_record.h"
namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
const int MAX_CALL_FRAME_EXPEND_CYCLE = 20;
const size_t MAX_CALL_FRAME_EXPEND_CACHE_SIZE = 20;
const size_t MAX_CALL_FRAME_UNWIND_SIZE = 30;
// if ip is 0 , 1 both not usefule
const uint64_t BAD_IP_ADDRESS = 2;

struct UnwindInfo {
    const VirtualThread &thread;
    const std::vector<u64> &regs;
    const std::vector<char> &stack;
};

#if is_double_framework
#if defined(__linux__)
class UnwindMaps : public ::unwindstack::Maps {
public:
    void UpdateMaps(const std::vector<MemMapItem> &maps)
    {
        maps_.clear();
        for (auto it = maps.begin(); it != maps.end();) {
            const MemMapItem &map = *it;
            // Add an entry.
            maps_.emplace_back(CreateMapInfo(map));
            ++it;
        }

        std::sort(maps_.begin(), maps_.end(), [](const auto &m1, const auto &m2) {
            if (m1 == nullptr || m2 == nullptr) {
                return m1 != nullptr;
            }
            return m1->start < m2->start;
        });
        maps_.resize(maps.size());
    }
    unwindstack::MapInfo *CreateMapInfo(const MemMapItem &map)
    {
        return new unwindstack::MapInfo(nullptr, map.begin_, map.end_, map.pageoffset_,
            PROT_READ | PROT_EXEC | map.flags, map.name_.c_str());
    }
};
#endif
#endif

class CallStack {
public:
    CallStack()
    {
#if is_double_framework
#if defined(__linux__)
        unwindstack::Elf::SetCachingEnabled(true);
#endif
#endif
    }
    ~CallStack();
    bool UnwindCallStack(const VirtualThread &thread, std::vector<u64> regs,
        const std::vector<char>& stack, std::vector<CallFrame> &,
        size_t maxStackLevel = MAX_CALL_FRAME_UNWIND_SIZE);

private:
    // we have a cache for all thread
    // std::map<pid_t, std::vector<std::vector<CallFrame>>> cachedCallFramesMap_;
    std::map<pid_t, HashList<uint64_t, std::vector<CallFrame>>> cachedCallFramesMap_;
    bool GetIpSP(uint64_t &ip, uint64_t &sp, const std::vector<u64> &regs) const;
#if is_double_framework
#if defined(__linux__)
    std::unordered_map<pid_t, UnwindMaps> cachedMaps_;
    bool UnwindCallStackExternal(const VirtualThread &thread, std::vector<u64> regs,
        const std::vector<char> stack, std::vector<CallFrame> &callStack, size_t maxStackLevel);
    bool UnwindStep(unwindstack::Unwinder &unwinder, std::vector<CallFrame> &callStack);
#endif
#endif
#if HAVE_LIBUNWIND
    static bool ReadVirtualThreadMemory(UnwindInfo &unwindInfoPtr, unw_word_t addr,
        unw_word_t *data);
    static const std::string GetUnwErrorName(int error);
    static void dumpUDI(unw_dyn_info_t &di);
    static bool fillUDI(unw_dyn_info_t &di, SymbolsFile &symbolsFile, MemMapItem &mmap,
        const VirtualThread &thread);
    static int FindProcInfo(unw_addr_space_t as, unw_word_t ip, unw_proc_info_t *pi,
        int need_unwind_info, void *arg);
    static int AccessMem(unw_addr_space_t as, unw_word_t addr, unw_word_t *valuePoint,
        int writeOperation, void *arg);
    static int AccessReg(unw_addr_space_t as, unw_regnum_t regnum, unw_word_t *valuePoint,
        int writeOperation, void *arg);
    static void PutUnwindInfo(unw_addr_space_t as, unw_proc_info_t *pi, void *arg);
    static int AccessFpreg(unw_addr_space_t as, unw_regnum_t num, unw_fpreg_t *val,
        int writeOperation, void *arg);
    static int GetDynInfoListAaddr(unw_addr_space_t as, unw_word_t *dil_vaddr, void *arg);
    static int Resume(unw_addr_space_t as, unw_cursor_t *cu, void *arg);
    static int getProcName(unw_addr_space_t as, unw_word_t addr, char *bufp, size_t buf_len,
        unw_word_t *offp, void *arg);
    static int FindUnwindTable(SymbolsFile *symbolsFile, MemMapItem &mmap,
        UnwindInfo *unwindInfoPtr, unw_addr_space_t as, unw_word_t ip, unw_proc_info_t *pi,
        unw_dyn_info_t &di, int need_unwind_info, void *arg);
    static unw_accessors_t accessors_;
    void UnwindStep(unw_cursor_t &c, std::vector<CallFrame> &callFrames, size_t maxStackLevel);
    std::map<pid_t, unw_addr_space_t> unwindAddrSpaceMap_;
#endif
    FRIEND_TEST(CallStackTest, LibUnwindEmptyFunc);
    FRIEND_TEST(CallStackTest, GetUnwErrorName);
    FRIEND_TEST(CallStackTest, UnwindCallStack);
};
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
#endif
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
#define HILOG_TAG "CallStack"

#include "callstack.h"

#include <string>
#if HAVE_LIBUNWIND
#include <libunwind.h>
extern "C" {
#include <libunwind_i.h>
}
#endif

#include "register.h"
#ifdef target_cpu_arm
// reg size is int (unw_word_t)
#define UNW_WORD_PFLAG "x"
#else
// reg size is long (unw_word_t)
#define UNW_WORD_PFLAG "zx"
#endif
namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
#if HAVE_LIBUNWIND
const std::map<unw_error_t, const std::string> UNW_ERROR_MAP = {
    {UNW_ESUCCESS, std::to_string(UNW_ESUCCESS)},
    {UNW_EUNSPEC, std::to_string(UNW_EUNSPEC)},
    {UNW_ENOMEM, std::to_string(UNW_ENOMEM)},
    {UNW_EBADREG, std::to_string(UNW_EBADREG)},
    {UNW_EREADONLYREG, std::to_string(UNW_EREADONLYREG)},
    {UNW_ESTOPUNWIND, std::to_string(UNW_ESTOPUNWIND)},
    {UNW_EINVALIDIP, std::to_string(UNW_EINVALIDIP)},
    {UNW_EBADFRAME, std::to_string(UNW_EBADFRAME)},
    {UNW_EINVAL, std::to_string(UNW_EINVAL)},
    {UNW_EBADVERSION, std::to_string(UNW_EBADVERSION)},
    {UNW_ENOINFO, std::to_string(UNW_ENOINFO)},
};
const std::string CallStack::GetUnwErrorName(int error)
{
    if (UNW_ERROR_MAP.count(static_cast<unw_error_t>(-error)) > 0) {
        return UNW_ERROR_MAP.at(static_cast<unw_error_t>(-error));
    } else {
        return "UNKNOW_UNW_ERROR";
    }
}

void CallStack::dumpUDI(unw_dyn_info_t &di)
{
    HLOGM("unwind_table info: ");
    HLOGM(" di.start_ip:            0x%016" UNW_WORD_PFLAG "", di.start_ip);
    HLOGM(" di.end_ip:              0x%016" UNW_WORD_PFLAG "", di.end_ip);
    HLOGM(" di.u.rti.segbase:       0x%016" UNW_WORD_PFLAG "", di.u.rti.segbase);
    HLOGM(" di.u.rti.table_data:    0x%016" UNW_WORD_PFLAG "", di.u.rti.table_data);
    HLOGM(" di.u.rti.table_len:     0x%016" UNW_WORD_PFLAG "", di.u.rti.table_len);
}

bool CallStack::fillUDI(unw_dyn_info_t &di, SymbolsFile &symbolsFile, MemMapItem &mmap,
    const VirtualThread &thread)
{
    uint64_t fdeTableElfOffset, fdeTableSize, ehFrameHdrElfOffset;
    uint64_t SectionVaddr, SectionSize, SectionFileOffset;
    di.start_ip = mmap.begin_;
    di.end_ip = mmap.end_;
#ifndef target_cpu_arm
    if ((UNW_INFO_FORMAT_REMOTE_TABLE == di.format) &&
        symbolsFile.GetHDRSectionInfo(ehFrameHdrElfOffset, fdeTableElfOffset, fdeTableSize)) {
        /*
            unw_word_t name_ptr;        // addr. of table name (e.g., library name)
            unw_word_t segbase;         // segment base
            unw_word_t table_len;       // must be a multiple of sizeof(unw_word_t)!
            unw_word_t table_data;
        */
        /*
            all the rti addr is offset of the elf file
            begin - page offset = elf file base addr in vaddr user space
            begin - page offset + elf offset = vaddr in real word.(for this thread)
        */

        // segbase is file offset .
        /*
            00200000-00344000 r--p 00000000 08:02 46404365
            00344000-005c4000 r-xp 00143000 08:02 46404365

            LOAD           0x00000000001439c0 0x00000000003449c0 0x00000000003449c0
                            0x000000000027f3c0 0x000000000027f3c0  R E    0x1000

            GNU_EH_FRAME   0x00000000000f3248 0x00000000002f3248 0x00000000002f3248
                            0x000000000000bb04 0x000000000000bb04  R      0x4

        */
        MemMapItem ehFrameMmap;
        if (!thread.FindMapByFileInfo(mmap.name_, ehFrameHdrElfOffset, ehFrameMmap)) {
            HLOGE("no ehframe mmap found.");
            return false;
        }

        di.u.rti.segbase = ehFrameMmap.begin_ + ehFrameHdrElfOffset - ehFrameMmap.pageoffset_;
        di.u.rti.table_data = ehFrameMmap.begin_ + fdeTableElfOffset - ehFrameMmap.pageoffset_;
        di.u.rti.table_len = fdeTableSize / sizeof(unw_word_t);

        HLOGM(" map pageoffset:         0x%016" PRIx64 "", mmap.pageoffset_);
        HLOGM(" ehFrameHdrElfOffset:    0x%016" PRIx64 "", ehFrameHdrElfOffset);
        HLOGM(" fdeTableElfOffset:      0x%016" PRIx64 "", fdeTableElfOffset);
        HLOGM(" fdeTableSize:           0x%016" PRIx64 "", fdeTableSize);
        return true;
    }
#else
    if ((UNW_INFO_FORMAT_ARM_EXIDX == di.format) &&
        symbolsFile.GetSectionInfo(ARM_EXIDX, SectionVaddr, SectionSize, SectionFileOffset)) {
        MemMapItem targetMmap;
        if (!thread.FindMapByFileInfo(mmap.name_, SectionFileOffset, targetMmap)) {
            HLOGE("no debug mmap found.");
            return false;
        }
        HLOGM(" begin: %" PRIx64 " offset:%" PRIx64 "", targetMmap.begin_, targetMmap.pageoffset_);

        di.u.rti.table_data = targetMmap.begin_ + SectionFileOffset - targetMmap.pageoffset_;
        di.u.rti.table_len = SectionSize;
        HLOGM(" SectionName:           %s", std::string(ARM_EXIDX).c_str());
        HLOGM(" SectionVaddrt:         0x%016" PRIx64 "", SectionVaddr);
        HLOGM(" SectionFileOffset      0x%016" PRIx64 "", SectionFileOffset);
        HLOGM(" SectionSize:           0x%016" PRIx64 "", SectionSize);

        // GetSectionInfo return true, but SectionVaddr || SectionSize is 0 ???
        HLOG_ASSERT(SectionVaddr != 0 && SectionSize != 0);
        return true;
    }
#endif
    return false;
}

/*
    https://www.nongnu.org/libunwind/man/libunwind-dynamic(3).html
*/
int CallStack::FindUnwindTable(SymbolsFile *symbolsFile, MemMapItem &mmap,
    UnwindInfo *unwindInfoPtr, unw_addr_space_t as, unw_word_t ip, unw_proc_info_t *pi,
    unw_dyn_info_t &di, int need_unwind_info, void *arg)
{
    HLOGM("try seach debug info at %s", symbolsFile->filePath_.c_str());
    if (fillUDI(di, *symbolsFile, mmap, unwindInfoPtr->thread)) {
        dumpUDI(di);
        /*
            we dont use dwarf_search_unwind_table
            because in arm it will search two function:
            1 arm_search_unwind_table first
            2 dwarf_search_unwind_table

            see libunwind_i.h for arm
            define tdep_search_unwind_table UNW_OBJ(search_unwind_table)

        */
        int ret = static_cast<unw_error_t>(
            tdep_search_unwind_table(as, ip, &di, pi, need_unwind_info, arg));

        HLOGM("search_unwind_table ret %d:%s", ret, GetUnwErrorName(ret).c_str());

        if (UNW_ESUCCESS != ret) {
            if (UNW_ENOINFO != ret) {
                HLOGW("search_unwind_table ret error %d:%s", ret, GetUnwErrorName(ret).c_str());
            }
            return -UNW_EUNSPEC;
        } else {
            return UNW_ESUCCESS;
        }
    } else {
        HLOGW("no debug info found for thread %d:%s", unwindInfoPtr->thread.tid_,
            unwindInfoPtr->thread.name_.c_str());
        return -UNW_EUNSPEC;
    }
}

int CallStack::FindProcInfo(unw_addr_space_t as, unw_word_t ip, unw_proc_info_t *pi,
    int need_unwind_info, void *arg)
{
    UnwindInfo *unwindInfoPtr = static_cast<UnwindInfo *>(arg);
    MemMapItem mmap;
    unw_dyn_info_t di;
    if (memset_s(&di, sizeof(di), 0, sizeof(di)) != EOK) {
        HLOGE("memset_s failed.");
    }
    HLOGM("need_unwind_info ret %d ip %" UNW_WORD_PFLAG "", need_unwind_info, ip);

    if (unwindInfoPtr->thread.FindMapByAddr(ip, mmap)) {
#ifdef target_cpu_arm
        // arm use .ARM.exidx , not use ehframe
        di.format = UNW_INFO_FORMAT_ARM_EXIDX;
#else
        // otherwise we use EH FRAME
        di.format = UNW_INFO_FORMAT_REMOTE_TABLE;
#endif
        SymbolsFile *symbolsFile = unwindInfoPtr->thread.FindSymbolsFileByMap(mmap);
        if (symbolsFile != nullptr) {
            return FindUnwindTable(symbolsFile, mmap, unwindInfoPtr, as, ip, pi, di,
                need_unwind_info, arg);
        } else {
            HLOGW("no symbols file found for thread %d:%s", unwindInfoPtr->thread.tid_,
                unwindInfoPtr->thread.name_.c_str());
        }
    } else {
        HLOGE("ip 0x%016" UNW_WORD_PFLAG " not found in thread %d:%s", ip,
            unwindInfoPtr->thread.tid_, unwindInfoPtr->thread.name_.c_str());
    }

    return -UNW_EUNSPEC;
}

bool CallStack::ReadVirtualThreadMemory(UnwindInfo &unwindInfoPtr, unw_word_t addr,
    unw_word_t *data)
{
    return unwindInfoPtr.thread.ReadRoMemory(addr, (uint8_t *)data, sizeof(unw_word_t));
}

int CallStack::AccessMem([[maybe_unused]] unw_addr_space_t as, unw_word_t addr,
    unw_word_t *valuePoint, int writeOperation, void *arg)
{
    UnwindInfo *unwindInfoPtr = static_cast<UnwindInfo *>(arg);
    uint64_t stackPoint;
    uint64_t stackEnd = 0;
    size_t stackOffset = 0;
    *valuePoint = 0;
    HLOGDUMMY("try access addr 0x%" UNW_WORD_PFLAG " ", addr);

    HLOG_ASSERT(writeOperation == 0);

    if (!RegisterGetSPValue(stackPoint, unwindInfoPtr->regs.data(), unwindInfoPtr->regs.size())) {
        HLOGE("RegisterGetSPValue failed");
        return -UNW_EUNSPEC;
    }

    stackEnd = stackPoint + unwindInfoPtr->stack.size();

    /* Check overflow. */
    if (addr + sizeof(unw_word_t) < addr) {
        HLOGE("address overfolw at 0x%" UNW_WORD_PFLAG " increase 0x%zu", addr, sizeof(unw_word_t));
        return -UNW_EUNSPEC;
    }

    if (addr < stackPoint || addr + sizeof(unw_word_t) >= stackEnd) {
        if (ReadVirtualThreadMemory(*unwindInfoPtr, addr, valuePoint)) {
            HLOGM("access_mem addr %p get val 0x%" UNW_WORD_PFLAG ", from mmap",
                reinterpret_cast<void *>(addr), *valuePoint);
        } else {
            HLOGW("access_mem addr %p failed, from mmap, ", reinterpret_cast<void *>(addr));
            HLOGW("stack range 0x%" PRIx64 " -  0x%" PRIx64 "(0x%" PRIx64 ")", stackPoint, stackEnd,
                stackEnd - stackPoint);
            return -UNW_EUNSPEC;
        }
    } else {
        stackOffset = addr - stackPoint;
        *valuePoint = *(unw_word_t *)&unwindInfoPtr->stack[stackOffset];
        HLOGM("access_mem addr %p val %" UNW_WORD_PFLAG ", from stack offset %zu",
            reinterpret_cast<void *>(addr), *valuePoint, stackOffset);
    }

    return UNW_ESUCCESS;
}

int CallStack::AccessReg([[maybe_unused]] unw_addr_space_t as, unw_regnum_t regnum,
    unw_word_t *valuePoint, int writeOperation, void *arg)
{
    UnwindInfo *unwindInfoPtr = static_cast<UnwindInfo *>(arg);
    uint64_t val;
    size_t perfRegIndex = LibunwindRegIdToPrefReg(regnum);

    /* Don't support write, I suspect we don't need it. */
    if (writeOperation) {
        HLOGE("access_reg %d", regnum);
        return -UNW_EINVAL;
    }

    if (unwindInfoPtr->regs.empty()) {
        return -UNW_EUNSPEC;
    }

    if (!RegisterGetValue(val, unwindInfoPtr->regs.data(), perfRegIndex, unwindInfoPtr->regs.size())) {
        HLOGE("can't read reg %zu", perfRegIndex);
        return -UNW_EUNSPEC;
    }

    *valuePoint = (unw_word_t)val;
    HLOGM("reg %d:%s, val 0x%" UNW_WORD_PFLAG "", regnum, RegisterGetName(perfRegIndex).c_str(),
        *valuePoint);
    return UNW_ESUCCESS;
}

void CallStack::PutUnwindInfo([[maybe_unused]] unw_addr_space_t as,
    [[maybe_unused]] unw_proc_info_t *pi, [[maybe_unused]] void *arg)
{
    HLOGV("enter");
}

int CallStack::AccessFpreg([[maybe_unused]] unw_addr_space_t as, [[maybe_unused]] unw_regnum_t num,
    [[maybe_unused]] unw_fpreg_t *val, [[maybe_unused]] int writeOperation,
    [[maybe_unused]] void *arg)
{
    HLOGV("enter");
    return -UNW_EINVAL;
}

int CallStack::GetDynInfoListAaddr([[maybe_unused]] unw_addr_space_t as,
    [[maybe_unused]] unw_word_t *dil_vaddr, [[maybe_unused]] void *arg)
{
    return -UNW_ENOINFO;
}

int CallStack::Resume([[maybe_unused]] unw_addr_space_t as, [[maybe_unused]] unw_cursor_t *cu,
    [[maybe_unused]] void *arg)
{
    HLOGV("enter");
    return -UNW_EINVAL;
}

int CallStack::getProcName([[maybe_unused]] unw_addr_space_t as, [[maybe_unused]] unw_word_t addr,
    [[maybe_unused]] char *bufp, [[maybe_unused]] size_t buf_len, [[maybe_unused]] unw_word_t *offp,
    [[maybe_unused]] void *arg)
{
    HLOGV("enter");
    return -UNW_EINVAL;
}
unw_accessors_t CallStack::accessors_ = {
    .find_proc_info = FindProcInfo,
    .put_unwind_info = PutUnwindInfo,
    .get_dyn_info_list_addr = GetDynInfoListAaddr,
    .access_mem = AccessMem,
    .access_reg = AccessReg,
    .access_fpreg = AccessFpreg,
    .resume = Resume,
    .get_proc_name = getProcName,
};

void CallStack::UnwindStep(unw_cursor_t &c, std::vector<CallFrame> &callStack, size_t maxStackLevel)
{
    while (callStack.size() < maxStackLevel) {
        int ret = unw_step(&c);
        if (ret > 0) {
            unw_word_t ip, sp;
            unw_get_reg(&c, UNW_REG_IP, &ip);
            unw_get_reg(&c, UNW_REG_SP, &sp);

            if (ip == 0) {
                HLOGD("ip == 0 something is wrong. break");
                break;
            }

            /*
             * Decrement the IP for any non-activation frames.
             * this is required to properly find the srcline
             * for caller frames.
             * See also the documentation for dwfl_frame_pc(),
             * which this code tries to replicate.
             */
            if (unw_is_signal_frame(&c) <= 0) {
                --ip;
            }
            HLOGV("unwind:%zu: ip 0x%" UNW_WORD_PFLAG " sp 0x%" UNW_WORD_PFLAG "", callStack.size(),
                ip, sp);
            if (callStack.back().ip_ == ip && callStack.back().sp_ == sp) {
                HLOGW("we found a same frame, stop here");
                break;
            }
            callStack.emplace_back(ip, sp);
        } else {
            HLOGV("no more frame step found. ret %d:%s", ret, GetUnwErrorName(ret).c_str());
            break;
        }
    }
}
#endif

bool CallStack::GetIpSP(uint64_t &ip, uint64_t &sp, const std::vector<u64> &regs) const
{
    if (regs.size() > 0) {
        if (!RegisterGetSPValue(sp, regs.data(), regs.size())) {
            HLOGW("unable get sp");
            return false;
        }
        if (!RegisterGetIPValue(ip, regs.data(), regs.size())) {
            HLOGW("unable get ip");
            return false;
        }
        if (ip != 0) {
            return true;
        }
    } else {
        HLOGW("reg size is 0");
        return false;
    }
    return false;
}

#if is_double_framework
#if defined(__linux__)
bool CallStack::UnwindStep(unwindstack::Unwinder &unwinder, std::vector<CallFrame> &callStack)
{
    unwinder.SetResolveNames(false);
    unwinder.Unwind();
    for (auto &frame : unwinder.frames()) {
        // Unwinding in arm architecture can return 0 pc address.
        if (frame.pc == 0u or frame.map_start == 0u) {
            HLOGV("unwind failed: pc 0x%" PRIx64 " map_start 0x%" PRIx64 "", frame.pc,
                frame.map_start);
            break;
        }
        HLOGV("unwind:%zu: ip 0x%" PRIx64 " sp 0x%" PRIx64 "", callStack.size(), frame.pc,
            frame.sp);
        callStack.emplace_back(frame.pc, frame.sp);
    }
    return true;
}

bool CallStack::UnwindCallStackExternal(const VirtualThread &thread, std::vector<u64> regs,
    const std::vector<char> stack, std::vector<CallFrame> &callStack, size_t maxStackLevel)
{
    uint64_t stack_addr = 0u;
    uint64_t ip = 0u, sp = 0u;

    if (regs.size() != unwindstack::ARM_REG_LAST) {
        HLOGW("reg size is not enough");
        return false;
    } else if (GetIpSP(ip, sp, regs)) {
        HLOGV("unwind:%zu: ip 0x%" PRIx64 " sp 0x%" PRIx64 "", 0u, ip, sp);
        stack_addr = sp;
    } else {
        HLOGW("reg ip sp get failed");
        return false;
    }

    UnwindMaps cached_map;
    cached_map.UpdateMaps(thread.GetMaps());
    std::shared_ptr<unwindstack::MemoryOfflineBuffer> stack_memory(
        new unwindstack::MemoryOfflineBuffer(reinterpret_cast<const uint8_t *>(stack.data()),
            stack_addr, stack_addr + stack.size()));

    unwindstack::arm_user_regs arm_user_regs;
    if (memset_s(&arm_user_regs, sizeof(arm_user_regs), 0, sizeof(arm_user_regs)) != EOK) {
        HLOGE("reg ip sp get failed");
    }
    static_assert(static_cast<int>(unwindstack::ARM_REG_R0) == static_cast<int>(PERF_REG_ARM_R0),
        "");
    static_assert(static_cast<int>(unwindstack::ARM_REG_LAST) == static_cast<int>(PERF_REG_ARM_MAX),
        "");
    for (size_t i = unwindstack::ARM_REG_R0; i < unwindstack::ARM_REG_LAST; ++i) {
        arm_user_regs.regs[i] = static_cast<uint32_t>(regs[i]);
    }

    std::unique_ptr<unwindstack::Regs> unwind_regs(unwindstack::RegsArm::Read(&arm_user_regs));
    if (!unwind_regs) {
        return false;
    }
    unwindstack::Unwinder unwinder(MAX_CALL_FRAME_UNWIND_SIZE, &cached_map, unwind_regs.get(),
        stack_memory);
    UnwindStep(unwinder, callStack);
    if (callStack.size() == 0) {
        callStack.emplace_back(ip, sp);
    }
    return true;
}
#endif
#endif

bool CallStack::UnwindCallStack(const VirtualThread &thread, std::vector<u64> regs,
    const std::vector<char>& stack, std::vector<CallFrame> &callStack, size_t maxStackLevel)
{
    HLOGV("enter");
#if is_double_framework
    return UnwindCallStackExternal(thread, regs, stack, callStack, maxStackLevel);
#endif
#if HAVE_LIBUNWIND
    unw_addr_space_t addr_space;
    UnwindInfo unwindInfo = {.thread = thread, .regs = regs, .stack = stack};
    unw_cursor_t c;
    uint64_t ip, sp;

    if (regs.size() > 0) {
        if (!RegisterGetSPValue(sp, regs.data(), regs.size())) {
            HLOGW("unable get sp");
            return false;
        }
        if (!RegisterGetIPValue(ip, regs.data(), regs.size())) {
            HLOGW("unable get ip");
            return false;
        }
        if (ip != 0) {
            HLOGV("unwind:%zu: ip 0x%" PRIx64 " sp 0x%" PRIx64 "", callStack.size(), ip, sp);
            callStack.emplace_back(ip, sp);
        }
    } else {
        HLOGW("reg size is 0");
        return false;
    }

    /*
     * If we need more than one entry, do the DWARF
     * unwind itself.
     */
    if (maxStackLevel - 1 > 0) {
        if (unwindAddrSpaceMap_.count(thread.tid_) == 0) {
            addr_space = unw_create_addr_space(&accessors_, 0);
            if (!addr_space) {
                HLOGE("Can't create unwind vaddress space.");
                return false;
            }
            unwindAddrSpaceMap_.emplace(thread.tid_, addr_space);
            unw_set_caching_policy(addr_space, UNW_CACHE_GLOBAL);
        } else {
            addr_space = unwindAddrSpaceMap_.at(thread.tid_);
        }

        int ret = unw_init_remote(&c, addr_space, &unwindInfo);
        if (ret) {
            HLOGE("unwind error %d:%s see unw_error_t.", ret, GetUnwErrorName(ret).c_str());
        } else {
            UnwindStep(c, callStack, maxStackLevel);
        }
    }
#endif
    return true;
}

/*
we should have CallStack cache for each thread

0. A -> B -> C -> E -> F
1.           C -> E -> F
2.      B -> C
3. A -> B -> C
4.      B -> G -> H
5.      J -> C

0 is our cache
1 2 3... is from record

use expendLimit to setup how may frame match is needs

*/
CallStack::~CallStack()
{
#if HAVE_LIBUNWIND
    for (auto &pair : unwindAddrSpaceMap_) {
        unw_destroy_addr_space(pair.second);
    }
#endif
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
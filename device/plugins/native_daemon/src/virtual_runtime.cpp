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
#define HILOG_TAG "Runtime"

#include "virtual_runtime.h"

#include <cinttypes>
#include <iostream>
#include <sstream>
#include <unistd.h>
#if !is_mingw
#include <sys/mman.h>
#endif

#include "register.h"
#include "symbols_file.h"
#include "utilities.h"

using namespace std::chrono;
namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
namespace {
std::atomic<uint64_t> callStackErrCnt = 0;
constexpr uint32_t CALL_STACK_ERROR_TIMES = 10;
}
// we unable to access 'swapper' from /proc/0/
void VirtualRuntime::ClearMaps()
{
    processMemMaps_.clear();
}

VirtualRuntime::VirtualRuntime(bool offlineSymbolization)
{
    threadMapsLock_ = PTHREAD_MUTEX_INITIALIZER;
    threadMemMapsLock_ = PTHREAD_MUTEX_INITIALIZER;
    if (!offlineSymbolization) {
        userSymbolCache_.reserve(USER_SYMBOL_CACHE_LIMIT);
    }
}
VirtualRuntime::~VirtualRuntime()
{
    HILOG_INFO(LOG_CORE, "%s:%d UserSymbolCache size = %zu", __func__, __LINE__, userSymbolCache_.size());
    HILOG_INFO(LOG_CORE, "Total number of call stack errors: %" PRIu64 "", callStackErrCnt.load());
    ClearMaps();
}
std::string VirtualRuntime::ReadThreadName(pid_t tid)
{
    std::string comm = ReadFileToString(StringPrintf("/proc/%d/comm", tid)).c_str();
    comm.erase(std::remove(comm.begin(), comm.end(), '\r'), comm.end());
    comm.erase(std::remove(comm.begin(), comm.end(), '\n'), comm.end());
    return comm;
}

VirtualThread &VirtualRuntime::UpdateThread(pid_t pid, pid_t tid, const std::string name)
{
#ifdef HIPERF_DEBUG_TIME
    const auto startTime = steady_clock::now();
#endif
    VirtualThread &thread = GetThread(pid, tid);
    if (!name.empty()) {
        thread.name_ = name;
    }
#ifdef HIPERF_DEBUG_TIME
    updateThreadTimes_ += duration_cast<microseconds>(steady_clock::now() - startTime);
#endif
    return thread;
}

VirtualThread &VirtualRuntime::CreateThread(pid_t pid, pid_t tid)
{
    // make a new one
    userSpaceThreadMap_.emplace(std::piecewise_construct, std::forward_as_tuple(tid),
                                std::forward_as_tuple(pid, tid, symbolsFiles_, this));
    VirtualThread& thr = userSpaceThreadMap_.at(tid);
    return thr;
}

VirtualThread &VirtualRuntime::GetThread(pid_t pid, pid_t tid)
{
    HLOGV("find thread %u:%u", pid, tid);
    pthread_mutex_lock(&threadMapsLock_);
    auto it = userSpaceThreadMap_.find(tid);
    if (it == userSpaceThreadMap_.end()) {
        // we also need thread
        VirtualThread& thr = CreateThread(pid, tid);
        pthread_mutex_unlock(&threadMapsLock_);
        return thr;
    } else {
        VirtualThread& thr = it->second;
        pthread_mutex_unlock(&threadMapsLock_);
        return thr;
    }
}

void VirtualRuntime::MakeCallFrame(Symbol &symbol, CallFrame &callFrame)
{
    callFrame.vaddrInFile_ = symbol.funcVaddr_;
    callFrame.symbolName_ = symbol.symbolName_;
    callFrame.symbolIndex_ = symbol.index_;
    callFrame.filePath_ = symbol.module_.empty() ? symbol.comm_ : symbol.module_;
    callFrame.symbolOffset_ = symbol.offset_;
    callFrame.callFrameId_ = symbol.symbolId_;
    callFrame.isReported_ = symbol.isReported_;
    if (symbol.funcVaddr_ != 0) {
        callFrame.offset_ = symbol.funcVaddr_;
    } else {
        callFrame.offset_ = callFrame.ip_;
    }
}

bool VirtualRuntime::GetSymbolName(pid_t pid, pid_t tid, std::vector<CallFrame>& callsFrames, int offset, bool first)
{
#ifdef HIPERF_DEBUG_TIME
    const auto startTime = steady_clock::now();
#endif
    // Symbolic the Call Stack
    HLOGV("total %zu frames", callsFrames.size());

    perf_callchain_context perfCallchainContext = PERF_CONTEXT_MAX;
    for (auto callFrameIt = callsFrames.begin() + offset; callFrameIt != callsFrames.end(); ++callFrameIt) {
        auto &callFrame = callFrameIt.operator*();
        if (callFrame.ip_ >= PERF_CONTEXT_MAX) {
            // dont care, this is not issue.
            HLOGV("%s", UpdatePerfContext(callFrame.ip_, perfCallchainContext).c_str());
            continue;
        }
        auto symbol = GetSymbol(callFrame.ip_, pid, tid,
            perfCallchainContext);
        if (symbol.isValid()) {
            MakeCallFrame(symbol, callFrame);
        } else {
#ifdef TRY_UNWIND_TWICE
            if (first) {
                if (failedIPs_.find(callFrame.ip_) == failedIPs_.end()) {
                    return false;
                } else {
                    callsFrames.erase(callFrameIt, callsFrames.end());
                    return true;
                }
            } else {
                failedIPs_.insert(callFrame.ip_);
                callsFrames.erase(callFrameIt, callsFrames.end());
                return true;
            }
#else
            ++callStackErrCnt;
            if (callStackErrCnt.load() % CALL_STACK_ERROR_TIMES == 0) {
                HILOG_DEBUG(LOG_CORE, "number of call stack errors: %" PRIu64 "", callStackErrCnt.load());
            }
            callsFrames.erase(callFrameIt, callsFrames.end());
            return true;
#endif
        }
        int index = callFrameIt - callsFrames.begin();
        HLOGV(" (%u)unwind symbol: %*s%s", index, index, "", callFrame.ToSymbolString().c_str());
    }
#ifdef HIPERF_DEBUG_TIME
    auto usedTime = duration_cast<microseconds>(steady_clock::now() - startTime);
    if (usedTime.count() != 0) {
        HLOGV("cost %0.3f ms to symbolic ", usedTime.count() / MS_DUARTION);
    }
    symbolicRecordTimes_ += usedTime;
#endif
    return true;
}

void VirtualRuntime::UpdateMaps(pid_t pid, pid_t tid)
{
    auto &thread = UpdateThread(pid, tid);
    if (thread.ParseMap(processMemMaps_, true)) {
        HILOG_DEBUG(LOG_CORE, "voluntarily update maps succeed");
    } else {
        HILOG_DEBUG(LOG_CORE, "voluntarily update maps ignore");
    }
}

bool VirtualRuntime::UnwindStack(std::vector<u64>& regs,
                                 const u8* stack_addr,
                                 int stack_size,
                                 pid_t pid,
                                 pid_t tid,
                                 std::vector<CallFrame>& callsFrames,
                                 size_t maxStackLevel,
                                 bool offline_symbolization)
{
#ifdef HIPERF_DEBUG_TIME
    const auto startTime = steady_clock::now();
#endif
    // if we have userstack ?
    int offset = 0;
    auto &thread = UpdateThread(pid, tid);
    if (stack_size > 0) {
        callstack_.UnwindCallStack(thread, &regs[0], regs.size(), stack_addr, stack_size, callsFrames, maxStackLevel);
        if (callsFrames.size() <= FILTER_STACK_DEPTH) {
            callsFrames.clear();
            return false;
        }
        // Do not symbolize the first two frame, cause the two frame implement by tool itself
        offset = FILTER_STACK_DEPTH;
#ifdef HIPERF_DEBUG_TIME
        unwindCallStackTimes_ += duration_cast<microseconds>(steady_clock::now() - startTime);
#endif
    }
#ifdef HIPERF_DEBUG_TIME
    unwindFromRecordTimes_ += duration_cast<microseconds>(steady_clock::now() - startTime);
#endif
    if (offline_symbolization) {
        return true;
    }
    if (!GetSymbolName(pid, tid, callsFrames, offset, true)) {
#ifdef TRY_UNWIND_TWICE
        HLOGD("clear and unwind one more time");
        if (!thread.ParseMap(processMemMaps_, true)) {
            GetSymbolName(pid, tid, callsFrames, offset, false);
            return false;
        }
        if (stack_size > 0) {
            callsFrames.clear();
            callstack_.UnwindCallStack(thread, &regs[0], regs.size(), stack_addr,
                stack_size, callsFrames, maxStackLevel);
        }
        if (callsFrames.size() <= FILTER_STACK_DEPTH) {
            callsFrames.clear();
            return false;
        }
        if (!GetSymbolName(pid, tid, callsFrames, offset, false)) {
            return false;
        }
#endif
    }
    return true;
}

bool VirtualRuntime::IsSymbolExist(std::string fileName)
{
    if (symbolsFiles_.find(fileName) != symbolsFiles_.end()) {
        HLOGV("already have '%s'", fileName.c_str());
        return true;
    }
    return false;
}

bool VirtualRuntime::DelSymbolFile(const std::string& fileName)
{
    auto search = symbolsFiles_.find(fileName);
    if ( search != symbolsFiles_.end()) {
        symbolsFiles_.erase(search);
        return true;
    }
    return false;
}

void VirtualRuntime::UpdateSymbols(std::string fileName)
{
    HLOGD("try to find symbols for file: %s", fileName.c_str());
#ifdef HIPERF_DEBUG_TIME
    const auto startTime = steady_clock::now();
#endif
    if (symbolsFiles_.find(fileName) != symbolsFiles_.end()) {
        HLOGV("already have '%s'", fileName.c_str());
        return;
    }

    // found it by name
    auto symbolsFile = SymbolsFile::CreateSymbolsFile(fileName);

    // set sybol path If it exists
    if (symbolsPaths_.size() > 0) {
        symbolsFile->setSymbolsFilePath(symbolsPaths_); // also load from search path
    }
    if (loadSymboleWhenNeeded_) {
        // load it when we need it
        symbolsFiles_[symbolsFile->filePath_] = std::move(symbolsFile);
    } else if (symbolsFile->LoadSymbols()) {
        symbolsFiles_[symbolsFile->filePath_] = std::move(symbolsFile);
    } else {
        HLOGW("symbols file for '%s' not found.", fileName.c_str());
    }
#ifdef HIPERF_DEBUG_TIME
    auto usedTime = duration_cast<microseconds>(steady_clock::now() - startTime);
    if (usedTime.count() != 0) {
        HLOGV("cost %0.3f ms to load '%s'", usedTime.count() / MS_DUARTION, fileName.c_str());
    }
    updateSymbolsTimes_ += usedTime;
#endif
}

const Symbol VirtualRuntime::GetKernelSymbol(uint64_t ip, const std::vector<MemMapItem> &memMaps,
                                             const VirtualThread &thread)
{
    Symbol vaddrSymbol(ip, thread.name_);
    for (auto &map : memMaps) {
        if (ip > map.begin_ && ip < map.end_) {
            HLOGM("found addr 0x%" PRIx64 " in kernel map 0x%" PRIx64 " - 0x%" PRIx64 " from %s",
                  ip, map.begin_, map.end_, map.name_.c_str());
            vaddrSymbol.module_ = map.name_;
            // found symbols by file name
            auto search = symbolsFiles_.find(map.name_);
            if (search != symbolsFiles_.end()) {
                auto& symbolsFile = search->second;
                vaddrSymbol.fileVaddr_ =
                        symbolsFile->GetVaddrInSymbols(ip, map.begin_, map.pageoffset_);
                HLOGV("found symbol vaddr 0x%" PRIx64 " for runtime vaddr 0x%" PRIx64
                        " at '%s'",
                        vaddrSymbol.fileVaddr_, ip, map.name_.c_str());
                if (!symbolsFile->SymbolsLoaded()) {
                    symbolsFile->LoadSymbols();
                }
                Symbol foundSymbols = symbolsFile->GetSymbolWithVaddr(vaddrSymbol.fileVaddr_);
                foundSymbols.taskVaddr_ = ip;
                if (!foundSymbols.isValid()) {
                    HLOGW("addr 0x%" PRIx64 " vaddr  0x%" PRIx64 " NOT found in symbol file %s",
                            ip, vaddrSymbol.fileVaddr_, map.name_.c_str());
                    return vaddrSymbol;
                } else {
                    return foundSymbols;
                }
            }
            HLOGW("addr 0x%" PRIx64 " in map but NOT found the symbol file %s", ip,
                  map.name_.c_str());
        } else {
            HLOGM("addr 0x%" PRIx64 " not in map 0x%" PRIx64 " - 0x%" PRIx64 " from %s", ip,
                  map.begin_, map.end_, map.name_.c_str());
        }
    }
    return vaddrSymbol;
}

const Symbol VirtualRuntime::GetUserSymbol(uint64_t ip, const VirtualThread &thread)
{
    Symbol vaddrSymbol(ip, thread.name_);
    const MemMapItem *mmap = thread.FindMapByAddr(ip);
    if (mmap != nullptr) {
        SymbolsFile *symbolsFile = thread.FindSymbolsFileByMap(*mmap);
        if (symbolsFile != nullptr) {
            vaddrSymbol.fileVaddr_ =
                symbolsFile->GetVaddrInSymbols(ip, mmap->begin_, mmap->pageoffset_);
            vaddrSymbol.module_ = mmap->nameHold_;
            vaddrSymbol.symbolName_ = vaddrSymbol.Name();
            HLOGV("found symbol vaddr 0x%" PRIx64 " for runtime vaddr 0x%" PRIx64 " at '%s'",
                  vaddrSymbol.fileVaddr_, ip, mmap->name_.c_str());
            if (!symbolsFile->SymbolsLoaded()) {
                symbolsFile->LoadSymbols();
            }
            Symbol foundSymbols = symbolsFile->GetSymbolWithVaddr(vaddrSymbol.fileVaddr_);
            foundSymbols.taskVaddr_ = ip;
            foundSymbols.symbolName_ = foundSymbols.Name();
            if (!foundSymbols.isValid()) {
                HLOGW("addr 0x%" PRIx64 " vaddr  0x%" PRIx64 " NOT found in symbol file %s", ip,
                      vaddrSymbol.fileVaddr_, mmap->name_.c_str());
                vaddrSymbol.filePathId_ = mmap->filePathId_;
                return vaddrSymbol;
            } else {
                foundSymbols.filePathId_ = mmap->filePathId_;
                return foundSymbols;
            }
        } else {
            HLOGW("addr 0x%" PRIx64 " in map but NOT found the symbol file %s", ip,
                  mmap->name_.c_str());
        }
    } else {
        HLOGW("ReportVaddrMapMiss");
#ifdef HIPERF_DEBUG
        thread.ReportVaddrMapMiss(ip);
#endif
    }
    return vaddrSymbol;
}

bool VirtualRuntime::GetSymbolCache(uint64_t ip, Symbol &symbol, const VirtualThread &thread)
{
    const MemMapItem *mmap = thread.FindMapByAddr(ip);
    if (mmap != nullptr) {
        auto foundSymbolIter = userSymbolCache_.find(std::pair(ip, mmap->filePathId_));
        if (foundSymbolIter != userSymbolCache_.end()) {
            symbol = foundSymbolIter->second;
            symbol.isReported_ = true;
            return true;
        }
    }
    return false;
}

void VirtualRuntime::UpdateSymbolCache(uint64_t ip, Symbol &symbol,
    HashList<uint64_t, Symbol> &cache)
{
    // review change to LRU for memmory
    HLOG_ASSERT_MESSAGE(cache.count(ip) == 0, "already have cached ip 0x%" PRIx64 "", ip);
    cache[ip] = symbol;
}

const Symbol VirtualRuntime::GetSymbol(uint64_t ip, pid_t pid, pid_t tid,
                                       const perf_callchain_context &context)
{
    HLOGM("try find tid %u ip 0x%" PRIx64 " in %zu symbolsFiles ", tid, ip, symbolsFiles_.size());
    Symbol symbol;
    if (GetSymbolCache(ip, symbol, GetThread(pid, tid))) {
        return symbol;
    }
    if (context == PERF_CONTEXT_USER or (context == PERF_CONTEXT_MAX and !symbol.isValid())) {
        // check userspace memmap
        symbol = GetUserSymbol(ip, GetThread(pid, tid));
        if (symbol.isValid()) {
            HLOGM("GetUserSymbol valid tid = %d ip = 0x%" PRIx64 "", tid, ip);
            symbol.symbolId_ = userSymbolCache_.size() + 1;
            userSymbolCache_[std::pair(ip, symbol.filePathId_)] = symbol;
        } else {
            HLOGM("GetUserSymbol invalid!");
        }
    }

    return symbol;
}

bool VirtualRuntime::SetSymbolsPaths(const std::vector<std::string> &symbolsPaths)
{
    std::unique_ptr<SymbolsFile> symbolsFile = SymbolsFile::CreateSymbolsFile(SYMBOL_UNKNOW_FILE);
    // we need check if the path is accessable
    bool accessable = symbolsFile->setSymbolsFilePath(symbolsPaths);
    if (accessable) {
        symbolsPaths_ = symbolsPaths;
    } else {
        if (!symbolsPaths.empty()) {
            printf("some symbols path unable access\n");
        }
    }
    return accessable;
}
void VirtualRuntime::CalculationDlopenRange(std::string& muslPath, uint64_t& max, uint64_t& min)
{
    auto iter = std::find_if(processMemMaps_.begin(), processMemMaps_.end(), [&](MemMapItem& map) {
        if (map.name_ == muslPath && (map.type_ & PROT_EXEC)) {
            return true;
        }
        return false;
    });

    if (iter == processMemMaps_.end()) {
        HILOG_INFO(LOG_CORE, "find musl failed!");
        return;
    }
    max = max + iter->begin_ - iter->pageoffset_;
    min = min + iter->begin_ - iter->pageoffset_;
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
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
#include <cstdio>
#define HILOG_TAG "RuntimeThread"

#include "virtual_thread.h"

#include <cinttypes>
#include <iostream>
#include <sstream>
#if !is_mingw
#include <sys/mman.h>
#endif

#include "symbols.h"
#include "utilities.h"
#include "virtual_runtime.h"
namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
VirtualThread::VirtualThread(pid_t pid,
                             pid_t tid,
                             const std::set<std::unique_ptr<SymbolsFile>, CCompareSymbolsFile>& symbolsFiles,
                             VirtualRuntime* runtime,
                             bool parseFlag)
    : pid_(pid), tid_(tid), symbolsFiles_(symbolsFiles), virtualruntime_(runtime)
{
    if (parseFlag) {
        pthread_mutex_lock(&virtualruntime_->threadMemMapsLock_);
        if (virtualruntime_->processMemMaps_.size() == 0) {
            this->ParseMap(virtualruntime_->processMemMaps_);
        }
        pthread_mutex_unlock(&virtualruntime_->threadMemMapsLock_);
    }

    memMaps_ = &virtualruntime_->processMemMaps_;
    this->name_ = ReadThreadName(pid);
    reg_nr = RegisterGetCount();
    if (reg_nr <= 0) {
        HLOGE("Getting register count failed");
        reg_nr = 0;
        user_regs = nullptr;
    } else if (reg_nr != std::numeric_limits<size_t>::max()) {
        user_regs = new (std::nothrow) u64[reg_nr];
        if (!user_regs) {
            HLOGM("new regs failed");
        }
        if (memset_s(user_regs, sizeof(u64) * reg_nr, 0, sizeof(u64) * reg_nr) != EOK) {
            HLOGM("memset_s regs failed");
        }
    } else {
        reg_nr = 0;
        user_regs = nullptr;
    }
    HLOGM("%d %d map from parent size is %zu", pid, tid, memMaps_->size());
}

std::string VirtualThread::ReadThreadName(pid_t tid)
{
    std::string comm = ReadFileToString(StringPrintf("/proc/%d/comm", tid)).c_str();
    comm.erase(std::remove(comm.begin(), comm.end(), '\r'), comm.end());
    comm.erase(std::remove(comm.begin(), comm.end(), '\n'), comm.end());
    return comm;
}

bool VirtualThread::FindMapByAddr(uint64_t addr, MemMapItem &outMap) const
{
    HLOGM("try found vaddr 0x%" PRIx64 " in maps %zu ", addr, memMaps_->size());
    for (auto &map : *memMaps_) {
        if (addr >= map.begin_ && addr < map.end_) {
            outMap = map;
            HLOGMMM("found vaddr 0x%" PRIx64 " in map fileoffset 0x%" PRIx64 " (0x%" PRIx64
                    " - 0x%" PRIx64 " pageoffset 0x%" PRIx64 ")  from %s",
                addr, outMap.FileOffsetFromAddr(addr), map.begin_, map.end_, map.pageoffset_,
                map.name_.c_str());
            return true;
        }
    }
    HLOGM("NOT found vaddr 0x%" PRIx64 " in maps %zu ", addr, memMaps_->size());
    return false;
}

bool VirtualThread::FindMapByFileInfo(const std::string name, uint64_t offset,
    MemMapItem& outMap) const
{
    for (auto &map : *memMaps_) {
        if (name != map.name_) {
            continue;
        }
        // check begin and length
        if (offset >= map.pageoffset_ && (offset - map.pageoffset_) < (map.end_ - map.begin_)) {
            outMap = map;
            HLOGMMM("found fileoffset 0x%" PRIx64 " in map (0x%" PRIx64 " - 0x%" PRIx64
                    " pageoffset 0x%" PRIx64 ")  from %s",
                offset, map.begin_, map.end_, map.pageoffset_, map.name_.c_str());
            return true;
        } else {
            HLOGM("* fail to found fileoffset 0x%" PRIx64 " in map (0x%" PRIx64 " - 0x%" PRIx64
                    " pageoffset 0x%" PRIx64 ")  from %s",
                offset, map.begin_, map.end_, map.pageoffset_, map.name_.c_str());
        }
    }
    HLOGM("NOT found offset 0x%" PRIx64 " in maps %zu ", offset, memMaps_->size());
    return false;
}

SymbolsFile *VirtualThread::FindSymbolsFileByMap(const MemMapItem &inMap) const
{
    for (auto &symbolsFile : symbolsFiles_) {
        if (symbolsFile->filePath_ == inMap.name_) {
            HLOGM("found symbol for map '%s'", inMap.name_.c_str());
            if (!symbolsFile->Loaded()) {
                symbolsFile->LoadSymbols();
            }
            return symbolsFile.get();
        }
    }

    if (find(missedSymbolFile_.begin(), missedSymbolFile_.end(), inMap.name_) ==
        missedSymbolFile_.end()) {
        missedSymbolFile_.emplace_back(inMap.name_);
        HLOGW("NOT found symbol for map '%s'", inMap.name_.c_str());
        for (auto &file : symbolsFiles_) {
            HLOGW(" we have '%s'", file->filePath_.c_str());
        }
    }

    return nullptr;
}

bool VirtualThread::ReadRoMemory(uint64_t addr, uint8_t *data, size_t size) const
{
    MemMapItem map {};
    if (FindMapByAddr(addr, map)) {
        // found symbols by file name
        SymbolsFile *symbolsFile = FindSymbolsFileByMap(map);
        if (symbolsFile != nullptr) {
            HLOGM("read vaddr from addr is 0x%" PRIx64 " at '%s'", addr - map.begin_,
                map.name_.c_str());
            if (size == symbolsFile->ReadRoMemory(map.FileOffsetFromAddr(addr), data, size)) {
                return true;
            } else {
                return false;
            }
        } else {
            HLOGW("found addr %" PRIx64 " in map but not loaded symbole %s", addr,
                map.name_.c_str());
        }
    } else {
        HLOGW("have not found addr %" PRIx64 " in any map", addr);
    }
    return false;
}

bool VirtualThread::IsLegalFileName(const std::string &fileName)
{
    if (fileName.empty() or StringStartsWith(fileName, "/dev/") or
        fileName.find(':') != std::string::npos or fileName.front() == '[' or
        fileName.back() == ']' or StringEndsWith(fileName, ".ttf") or
        fileName == MMAP_ANONYMOUS_OHOS_NAME) {
        return false;
    }
    return true;
}

#if is_mingw
void VirtualThread::ParseMap()
{
    // only linux support read maps in runtime
    return;
}
#else
constexpr const int MMAP_LINE_MIN_TOKEN = 5;
constexpr const int MMAP_LINE_TOKEN_INDEX_FLAG = 1;
constexpr const int MMAP_LINE_TOKEN_INDEX_OFFSET = 2;
constexpr const int MMAP_LINE_TOKEN_INDEX_MM = 3;
constexpr const int MMAP_LINE_TOKEN_INDEX_INODE = 4;
constexpr const int MMAP_LINE_TOKEN_INDEX_NAME = 5;
constexpr const int MMAP_LINE_MAX_TOKEN = 6;

void VirtualThread::ParseMap(std::vector<MemMapItem>& memMaps)
{
    std::string mapPath = StringPrintf("/proc/%d/maps", pid_);
    std::string mapContent = ReadFileToString(mapPath);
    if (mapContent.size() > 0) {
        std::istringstream s(mapContent);
        std::string line;
        while (std::getline(s, line)) {
            HLOGM("map line: %s", line.c_str());
            // b0023000-b0024000 r--p 00000000 b3:05 959        /system/lib/libdl.so
            // 0                 1    2        3     4          5
            MemMapItem memMapItem;
            std::vector<std::string> mapTokens = StringSplit(line, " ");

            if (mapTokens.size() < MMAP_LINE_MIN_TOKEN) {
                // maybe file name is empty
                continue;
            }

            // b0023000-b0024000
            constexpr const int MMAP_ADDR_RANGE_TOKEN = 2;
            std::vector<std::string> addrRanges = StringSplit(mapTokens[0], "-");
            if (addrRanges.size() != MMAP_ADDR_RANGE_TOKEN) {
                continue;
            }

            // b0023000 / b0024000
            try {
                memMapItem.begin_ = std::stoull(addrRanges[0], nullptr, NUMBER_FORMAT_HEX_BASE);
                memMapItem.end_ = std::stoull(addrRanges[1], nullptr, NUMBER_FORMAT_HEX_BASE);
            } catch (...) {
                // next line
                continue;
            }

            constexpr const int MMAP_PROT_CHARS = 4;
            int index = 0;
            // rwxp
            memMapItem.type_ = 0;
            if (mapTokens[MMAP_LINE_TOKEN_INDEX_FLAG].size() != MMAP_PROT_CHARS) {
                continue;
            }
            if (mapTokens[MMAP_LINE_TOKEN_INDEX_FLAG][index++] == 'r') {
                memMapItem.type_ |= PROT_READ;
            }
            if (mapTokens[MMAP_LINE_TOKEN_INDEX_FLAG][index++] == 'w') {
                memMapItem.type_ |= PROT_WRITE;
            }
            if (mapTokens[MMAP_LINE_TOKEN_INDEX_FLAG][index++] == 'x') {
                memMapItem.type_ |= PROT_EXEC;
            }

            if ((memMapItem.type_ & PROT_EXEC) || (memMapItem.type_ & PROT_READ)) {
                /*
                we need record the read hen exec map address
                callstackk need r map to check the ehframe addrssss
                Section Headers:
                [Nr] Name              Type             Address           Offset
                    Size              EntSize          Flags  Link  Info  Align

                [12] .eh_frame_hdr     PROGBITS         00000000002929a0  000929a0
                    00000000000071fc  0000000000000000   A       0     0     4
                [13] .eh_frame         PROGBITS         0000000000299ba0  00099ba0
                    000000000002a8f4  0000000000000000   A       0     0     8
                [14] .text             PROGBITS         00000000002c5000  000c5000
                    00000000001caa4a  0000000000000000  AX       0     0     16

                00200000-002c5000 r--p 00000000 08:02 46400311
                002c5000-00490000 r-xp 000c5000 08:02 46400311
                */
            } else {
                continue;
            }

            // MAP_PRIVATE or MAP_SHARED
            constexpr const int MAP_FLAG_ATTR_INDEX = 3;
            if (mapTokens[MMAP_LINE_TOKEN_INDEX_FLAG][MAP_FLAG_ATTR_INDEX] == 'p') {
                memMapItem.flags = MAP_PRIVATE;
            } else if (mapTokens[MMAP_LINE_TOKEN_INDEX_FLAG][MAP_FLAG_ATTR_INDEX] == 's') {
                memMapItem.flags = MAP_SHARED;
            }

            try {
                // 00000000
                memMapItem.pageoffset_ = std::stoull(mapTokens[MMAP_LINE_TOKEN_INDEX_OFFSET],
                    nullptr, NUMBER_FORMAT_HEX_BASE);

                // major:minor
                std::vector<std::string> mm = StringSplit(mapTokens[MMAP_LINE_TOKEN_INDEX_MM], ":");

                // b3:05
                memMapItem.major_ = std::stoull(mm.at(0), nullptr, NUMBER_FORMAT_HEX_BASE);
                memMapItem.minor_ = std::stoull(mm.at(1), nullptr, NUMBER_FORMAT_HEX_BASE);

                // 959
                memMapItem.inode = std::stoull(mapTokens[MMAP_LINE_TOKEN_INDEX_INODE], nullptr,
                    NUMBER_FORMAT_HEX_BASE);
            } catch (...) {
                // next line
                continue;
            }

            // system/lib/libdl.so
            if (mapTokens.size() == MMAP_LINE_MAX_TOKEN) {
                memMapItem.name_ = mapTokens[MMAP_LINE_TOKEN_INDEX_NAME];
            }
            if (!IsLegalFileName(memMapItem.name_)) {
                continue;
            }

            memMaps.push_back(memMapItem);
            virtualruntime_->UpdateSymbols(memMapItem.name_);
            pid_t pid = getpid();
            HLOGD("%d %d memMap add '%s'", pid_, tid_, memMapItem.name_.c_str());
        }
    }
}
#endif

void VirtualThread::CreateMapItem(const std::string filename, uint64_t begin, uint64_t len,
    uint64_t offset)
{
    MemMapItem &map = memMaps_->emplace_back();
    map.name_ = filename;
    map.begin_ = begin;
    map.end_ = begin + len;
    map.pageoffset_ = offset;
    HLOGD(" %u:%u create a new map(total %zu) at '%s' (0x%" PRIx64 "-0x%" PRIx64 ")@0x%" PRIx64 " ",
        pid_, tid_, memMaps_->size(), map.name_.c_str(), map.begin_, map.end_, map.pageoffset_);
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
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
#ifndef HIPERF_VIRTUAL_THREAD_H
#define HIPERF_VIRTUAL_THREAD_H
#include <assert.h>
#include <cinttypes>
#include <functional>
#include <pthread.h>
#include <set>
#include <vector>
#include "debug_logger.h"
#include "logging.h"
#include "register.h"
#include "symbols.h"
#include "utilities.h"

namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
/*
03284000-03289000 r--p 00000000 b3:05 289        /system/bin/sh
032b7000-032b9000 rw-p 00000000 00:00 0
aff60000-aff96000 r--p 00000000 b3:05 923        /system/lib/libc++.so
affeb000-affed000 rw-p 00000000 00:00 0
b0023000-b0024000 r--p 00000000 b3:05 959        /system/lib/libdl.so
*/
const std::string MMAP_NAME_HEAP = "[heap]";
const std::string MMAP_NAME_ANON = "[anon]";

class MemMapItem {
public:
    uint64_t begin_ = 0;
    uint64_t end_ = 0;
    uint16_t type_ = 0;
    uint16_t flags = 0;
    uint64_t pageoffset_ = 0;
    uint64_t major_ = 0;
    uint64_t minor_ = 0;
    ino_t inode = 0;
    std::string name_;

    MemMapItem() {}
    MemMapItem(uint64_t begin, uint64_t end, uint64_t offset, const std::string &name)
        : begin_(begin), end_(end), pageoffset_(offset), name_(name)
    {
    }

    // use for find
    bool operator==(const std::string &name) const
    {
        return name_ == name;
    }
    uint64_t FileOffsetFromAddr(uint64_t addr)
    {
        // real vaddr - real map begin = addr offset in section
        // section offset + page off set = file offset
        return addr - begin_ + pageoffset_;
    }
    // debug only
    const std::string ToString() const
    {
        std::stringstream sstream;
        sstream << "begin 0x" << std::hex << begin_;
        sstream << "end 0x" << std::hex << end_;
        sstream << "type 0x" << std::hex << type_;
        sstream << "flags 0x" << std::hex << flags;
        sstream << "pageoffset 0x" << std::hex << pageoffset_;
        sstream << " " << name_;
        return sstream.str();
    }
    bool operator < (const MemMapItem& right) const
    {
        return name_ < right.name_;
    }
};

class VirtualRuntime;

class VirtualThread {
public:
    VirtualThread(const VirtualThread &) = delete;
    VirtualThread &operator=(const VirtualThread &) = delete;

    VirtualThread(pid_t pid,
                  pid_t tid,
                  const std::set<std::unique_ptr<SymbolsFile>, CCompareSymbolsFile>& symbolsFiles,
                  VirtualRuntime* runtime,
                  bool parseFlag = true);

    virtual ~VirtualThread()
    {
        if (user_regs != nullptr) {
            delete []user_regs;
            user_regs = nullptr;
        }
    }

    std::string ReadThreadName(pid_t tid);

    pid_t pid_;
    pid_t tid_;
    std::string name_;

    std::vector<MemMapItem> *GetMaps() const
    {
        return memMaps_;
    }

    void ParseMap(std::vector<MemMapItem> &memMaps);
    void CreateMapItem(const std::string filename, uint64_t begin, uint64_t len, uint64_t offset);
    bool FindMapByAddr(uint64_t addr, MemMapItem &outMap) const;
    bool FindMapByFileInfo(const std::string name, uint64_t offset, MemMapItem& outMap) const;
    SymbolsFile *FindSymbolsFileByMap(const MemMapItem &inMap) const;
    bool ReadRoMemory(uint64_t addr, uint8_t *data, size_t size) const;
public:
    u64* user_regs;
    u64 reg_nr;
private:
    const std::set<std::unique_ptr<SymbolsFile>, CCompareSymbolsFile> & symbolsFiles_;
    bool IsLegalFileName(const std::string &filename);

    // thread must use ref from process
    std::vector<MemMapItem>* memMaps_;

    mutable std::map<uint64_t, uint8_t> cachedMemory_;
    mutable std::vector<std::string> missedSymbolFile_;
    VirtualRuntime* virtualruntime_;

    friend class VirtualRuntime;
    FRIEND_TEST(VirtualThreadTest, ReadRoMemory);
};
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
#endif
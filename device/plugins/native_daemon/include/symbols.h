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

#ifndef HIPERF_SYMBOLS_H
#define HIPERF_SYMBOLS_H

#include <cinttypes>
#include <iomanip>
#include <sstream>
#include <string>
#include <gtest/gtest.h>
#include "perf_file_format.h"
#include "utilities.h"

namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
constexpr const char KERNEL_MMAP_NAME[] = "[kernel.kallsyms]";
constexpr const char KERNEL_MODULES_EXT_NAME[] = ".ko";
constexpr const char KERNEL_ELF_NAME[] = "vmlinux";
constexpr const char MMAP_ANONYMOUS_NAME[] = "[anon]";
constexpr const char MMAP_ANONYMOUS_OHOS_NAME[] = "//anon";

const std::string NOTE_GNU_BUILD_ID = ".note.gnu.build-id";
const std::string EH_FRAME_HR = ".eh_frame_hdr";
const std::string EH_FRAME = ".eh_frame";
const std::string ARM_EXIDX = ".ARM.exidx";
const std::string SYMTAB = ".symtab";
const std::string DYNSYM = ".dynsym";

const int MAX_SYMBOLS_TYPE_NAME_LEN = 10;

struct Symbol {
    uint64_t vaddr_ = 0;
    uint64_t ipVaddr_ = 0;
    uint64_t offset_ = 0;
    uint64_t len_ = 0;
    int32_t index_ = -1;
    std::string name_ = "";
    std::string module_ = "";      // maybe empty
    std::string demangle_ = "";    // demangle string
    mutable bool matched_ = false; // if some callstack match this
    int32_t hit_ = 0;

    Symbol(uint64_t vaddr, uint64_t len, const std::string name, const std::string module)
        : vaddr_(vaddr),
          ipVaddr_(vaddr),
          offset_(ipVaddr_ - vaddr_),
          len_(len),
          name_(name),
          module_(module) {};
    Symbol(uint64_t vaddr, const std::string name, const std::string module)
        : Symbol(vaddr, 0, name, module) {};
    Symbol(uint64_t addr = 0) : Symbol(addr, std::string(), std::string()) {};

    bool same(const Symbol &b) const
    {
        return (vaddr_ == b.vaddr_ and demangle_ == b.demangle_);
    }

    bool operator==(const Symbol &b) const
    {
        return same(b);
    }

    bool operator!=(const Symbol &b) const
    {
        return !same(b);
    }

    bool isValid() const
    {
        return !module_.empty();
    }

    void Matched() const
    {
        matched_ = true;
    }

    void setIpVAddress(uint64_t vaddr)
    {
        ipVaddr_ = vaddr;
        offset_ = ipVaddr_ - vaddr_;
    }

    std::string GetName() const
    {
        return demangle_.empty() ? name_ : demangle_;
    }

    std::string GetNameOrModuleVaddr(const std::string &comm) const
    {
        if (!demangle_.empty()) {
            return demangle_;
        } else if (!name_.empty()) {
            return name_;
        } else {
            std::stringstream sstream;
            sstream << (module_.empty() ? comm : module_) << "+0x" << std::hex << ipVaddr_;
            return sstream.str();
        }
    }

    std::string Name() const
    {
        std::stringstream sstream;
        if (!demangle_.empty()) {
            sstream << demangle_;
        } else {
            sstream << name_;
        }
        if (ipVaddr_ > vaddr_) {
            sstream << "(+0x" << std::hex << (ipVaddr_ - vaddr_) << ")";
        } else if (ipVaddr_ < vaddr_) {
            sstream << "(-0x" << std::hex << (vaddr_ - ipVaddr_) << ") should not happend.";
        }
        return sstream.str();
    };

    std::string Addr() const
    {
        std::stringstream sstream;
        sstream << "0x" << std::setfill('0') << std::setw(sizeof(ipVaddr_) * BYTE_PRINT_WIDTH)
                << std::hex << ipVaddr_;
        return sstream.str();
    };
    std::string Len() const
    {
        std::stringstream sstream;
        sstream << std::setfill('0') << std::setw(sizeof(len_)) << len_;
        return sstream.str();
    };
    std::string ToString() const
    {
        std::stringstream sstream;
        sstream << Addr() << " " << Name();
        return sstream.str();
    };
    std::string ToDebugString() const
    {
        std::stringstream sstream;
        sstream << Addr() << "|";
        sstream << Len() << "|";
        sstream << demangle_ << "|";
        sstream << name_ << "|";
        sstream << (matched_ ? "matched" : "");

        return sstream.str();
    };

    bool contain(uint64_t addr) const
    {
        if (len_ == 0) {
            return vaddr_ <= addr;
        } else {
            return vaddr_ <= addr and (vaddr_ + len_) > addr;
        }
    }

    // The range [first, last) must be partitioned with respect to the expression !(value < element)
    // or !comp(value, element)
    static bool ValueLessThanElem(uint64_t vaddr, const Symbol &a)
    {
        return vaddr < a.vaddr_;
    }
    static bool CompareLT(const Symbol &a, const Symbol &b)
    {
        return a.vaddr_ < b.vaddr_; // we should use vaddr to sort
    };
};

enum SymbolsFileType {
    SYMBOL_KERNEL_FILE,
    SYMBOL_KERNEL_MODULE_FILE,
    SYMBOL_ELF_FILE,
    SYMBOL_JAVA_FILE,
    SYMBOL_JS_FILE,
    SYMBOL_UNKNOW_FILE,
};

class SymbolsFile {
public:
    SymbolsFileType symbolFileType_;
    std::string filePath_ = "";

    // [14] .text             PROGBITS         00000000002c5000  000c5000
    // min exec addr , general it point to .text
    // we make a default value for min compare
    static const uint64_t maxVaddr = std::numeric_limits<uint64_t>::max();

    uint64_t textExecVaddr_ = maxVaddr;
    uint64_t textExecVaddrFileOffset_ = 0;
    uint64_t textExecVaddrRange_ = maxVaddr;

    SymbolsFile(SymbolsFileType symbolType, const std::string path)
        : symbolFileType_(symbolType), filePath_(path) {};
    virtual ~SymbolsFile();

    // create the symbols file object
    static std::unique_ptr<SymbolsFile> CreateSymbolsFile(SymbolsFileType = SYMBOL_UNKNOW_FILE,
        const std::string symbolFilePath = std::string());
    static std::unique_ptr<SymbolsFile> CreateSymbolsFile(const std::string &symbolFilePath);

    // set symbols path
    bool setSymbolsFilePath(const std::string &symbolsSearchPath)
    {
        std::vector<std::string> symbolsSearchPaths = {symbolsSearchPath};
        return setSymbolsFilePath(symbolsSearchPaths);
    };
    bool setSymbolsFilePath(const std::vector<std::string> &);

    // load symbol from file
    virtual bool LoadSymbols([[maybe_unused]] const std::string &symbolFilePath = std::string())
    {
        HLOGV("virtual dummy function called");
        loaded_ = true;
        return false;
    };

    // get the build if from symbols
    const std::string GetBuildId() const;

    // get the symbols vector
    const std::vector<Symbol> &GetSymbols();

    // get vaddr(in symbol) from ip(real addr , after mmap reloc)
    virtual uint64_t GetVaddrInSymbols(uint64_t ip, uint64_t mapStart, uint64_t mapOffset) const;

    // get symbols from vaddr
    const Symbol GetSymbolWithVaddr(uint64_t vaddr) const;

    // read the .text section and .eh_frame section (RO) memory from elf mmap
    // unwind use this to check the DWARF and so on
    virtual size_t ReadRoMemory(uint64_t, uint8_t *, size_t) const
    {
        HLOGV("virtual dummy function called");
        return 0; // default not support
    }

    // get the section info , like .ARM.exidx
    virtual bool GetSectionInfo([[maybe_unused]] const std::string &name,
        [[maybe_unused]] uint64_t &sectionVaddr, [[maybe_unused]] uint64_t &sectionSize,
        [[maybe_unused]] uint64_t &sectionFileOffset) const
    {
        HLOGV("virtual dummy function called");
        return false;
    }
#ifndef __arm__
    // get hdr info for unwind , need provide the fde table location and entry count
    virtual bool GetHDRSectionInfo([[maybe_unused]] uint64_t &ehFrameHdrElfOffset,
        [[maybe_unused]] uint64_t &fdeTableElfOffset, [[maybe_unused]] uint64_t &fdeTableSize) const
    {
        HLOGV("virtual dummy function called");
        return false;
    }
#endif
    // load from symbols from the perf.data format
    static std::unique_ptr<SymbolsFile> LoadSymbolsFromSaved(const SymbolFileStruct &);
    // save the symbols to perf.data format
    const SymbolFileStruct exportSymbolToFileFormat(bool onlyMatched = true);

    bool Loaded()
    {
        return loaded_;
    }

protected:
    bool loaded_ = false;
    const std::string FindSymbolFile(const std::vector<std::string> &,
        std::string symboleFilePath = std::string()) const;

    std::string SearchReadableFile(const std::vector<std::string> &searchPaths,
        const std::string &filePath) const;
    bool UpdateBuildIdIfMatch(std::string buildId);
    std::string buildId_;
    std::vector<std::string> symbolsFileSearchPaths_;
    std::vector<Symbol> symbols_ {};
    void AdjustSymbols();
    bool CheckPathReadable(const std::string &path) const;

    FRIEND_TEST(SymbolsTest, FindSymbolFile);
    FRIEND_TEST(SymbolsTest, UpdateBuildIdIfMatch);
    FRIEND_TEST(SymbolsTest, exportSymbolToFileFormat);
    FRIEND_TEST(SymbolsTest, exportSymbolToFileFormatMatched);
    friend class VirtualRuntimeTest;
};

class CCompareSymbolsFile {
public:
    bool operator() (const std::unique_ptr<SymbolsFile>& left, const std::unique_ptr<SymbolsFile>& right) const
    {
        return left->filePath_ < right->filePath_;
    }
};
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
#endif
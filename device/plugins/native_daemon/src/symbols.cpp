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

#define HILOG_TAG "Symbols"

#include "symbols.h"

#include <algorithm>
#include <chrono>
#include <cxxabi.h>
#include <fcntl.h>
#include <fstream>

#if is_mingw
#include <memoryapi.h>
#else
#include <sys/mman.h>
#include <sys/stat.h>
#endif

#include <cstdlib>
#include <unistd.h>

#include "elf_parser.h"
#include "dwarf_encoding.h"
#include "utilities.h"

using namespace OHOS::Developtools::NativeDaemon::ELF;
using namespace std::chrono;

namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
const std::string SymbolsFile::GetBuildId() const
{
    return buildId_;
}

bool SymbolsFile::UpdateBuildIdIfMatch(std::string buildId)
{
    /*
        here we have two case
        1 buildId_ is empty
            a) always return match
        2 buildId_ is not empty
            a) really check if the same one
    */

    if (buildId_.empty()) {
        // we have new empty build
        if (buildId.empty()) {
            // both empty , no build id provided
            HLOGD("build id is empty.");
            return true;
        } else {
            buildId_ = buildId;
            HLOGD("new buildId %s", buildId_.c_str());
            return true;
        }
    } else {
        // we already have a build id
        // so this is not the first time load symbol
        // we need check if it match
        HLOGV("expected buildid: %s vs %s", buildId_.c_str(), buildId.c_str());

        if (buildId_ != buildId) {
            HLOGW("id not match");
            return false;
        } else {
            HLOGD("id match");
            return true;
        }
    }
}

std::string SymbolsFile::SearchReadableFile(const std::vector<std::string> &searchPaths,
    const std::string &filePath) const
{
    if (filePath.empty()) {
        HLOGW("nothing to found");
        return filePath;
    }
    if (CheckPathReadable(filePath)) {
        // found direct folder
        HLOGD("find %s in current work dir", filePath.c_str());
        return filePath;
    }
    for (auto searchPath : searchPaths) {
        if (searchPath.back() != PATH_SEPARATOR) {
            searchPath += PATH_SEPARATOR;
        }
        std::string PossibleFilePath = searchPath + filePath;
        if (CheckPathReadable(PossibleFilePath)) {
            return PossibleFilePath;
        }
        HLOGW("have not found '%s' in search paths.", filePath.c_str());
    }
    return std::string();
}

const std::string SymbolsFile::FindSymbolFile(
    const std::vector<std::string> &symbolsFileSearchPaths, std::string symboleFilePath) const
{
    /*
        this function do 2 things:
        find by name:
            1 find dso path
            2 find search path
                a) search path + dso path
                b) search path + dso name

        show we should return filePath_ as default ?
    */
    if (symboleFilePath.empty()) {
        symboleFilePath = filePath_;
        HLOGD("use default filename: %s ", symboleFilePath.c_str());
    }
    symboleFilePath = PlatformPathConvert(symboleFilePath);
    std::string foundPath = SearchReadableFile(symbolsFileSearchPaths, symboleFilePath);
    if (foundPath.empty()) {
        if (symboleFilePath.find(PATH_SEPARATOR) != std::string::npos) {
            // found it again with base name , split it and get last name
            foundPath = SearchReadableFile(symbolsFileSearchPaths,
                StringSplit(symboleFilePath, PATH_SEPARATOR_STR).back());
        }
    }
    return foundPath;
}

class ElfFileSymbols : public SymbolsFile {
public:
    explicit ElfFileSymbols(const std::string symbolFilePath,
        const SymbolsFileType symbolsFileType = SYMBOL_ELF_FILE)
        : SymbolsFile(symbolsFileType, symbolFilePath)
    {
    }

    virtual ~ElfFileSymbols()
    {
        if (mmap_ != MMAP_FAILED) {
            if (munmap(mmap_, mmapSize_) != 0) {
                HLOGE("munmap failed with %p", munmap);
            }
        }
    }

    bool LoadSymbols(const std::string &symbolFilePath) override
    {
        loaded_ = true;
        std::string findSymbolFilePath = FindSymbolFile(symbolsFileSearchPaths_, symbolFilePath);
        if (findSymbolFilePath.empty()) {
            HLOGW("elf found failed (belong to %s)", filePath_.c_str());
            return false;
        }
        if (LoadElfSymbols(findSymbolFilePath)) {
            return true;
        } else {
            HLOGW("elf open failed with '%s'", findSymbolFilePath.c_str());
            return false;
        }
        return false;
    }

    size_t ReadRoMemory(uint64_t addr, uint8_t *data, size_t size) const override
    {
        size_t readSize = 0;

        if (mmap_ != MMAP_FAILED) {
            if ((addr + size) <= mmapSize_) {
                std::copy_n(static_cast<uint8_t *>(mmap_) + addr, size, data);
                readSize = size;
            } else {
                HLOGW("read out of range.");
                HLOGW("try read 0x%" PRIx64 "(elf offset)+%zu max is 0x%" PRIx64 "", addr, size,
                    mmapSize_);
            }
        } else {
            if (readFd_ != nullptr) {
                if (fseek(readFd_.get(), addr, SEEK_SET) != 0) {
                    return 0;
                }
                if (fread(data, size, 1u, readFd_.get())) {
                    readSize = size;
                } else {
                    HLOGEP("read at %" PRIx64 " failed for %s", addr, filePath_.c_str());
                }
            }
            HLOGM("no mmap files loaded");
        }

        HLOGM("read %zu/%zu bytes at %" PRIx64 "(elf offset)", readSize, size, addr);

        return readSize;
    }

protected:
    std::string CovertByteBufferToHexString(const unsigned char *buffer, size_t size) const
    {
        std::string descString;
        size_t i = 0;
        while (i < size) {
            descString.append(ToHex(buffer[i]));
            i++; // move to next char
        }
        return descString;
    }

    std::string ElfGetBuildId(const unsigned char *buffer, size_t size) const
    {
        const unsigned char *end = buffer + size;
        HLOGV("size:%zu", size);

        /*
        Note Section
        A vendor or system engineer might need to mark an object file with special
        information that other programs can check for conformance or compatibility. Sections
        of type SHT_NOTE and program header elements of type PT_NOTE can be used for this
        purpose.

        The note information in sections and program header elements holds any number of
        entries, as shown in the following figure. For 64–bit objects and 32–bit objects,
        each entry is an array of 4-byte words in the format of the target processor. Labels
        are shown in Figure 12-6 to help explain note information organization, but are not
        part of the specification.

        Figure 12-5 Note Information

        image:ELF note section information.
        namesz and name
        The first namesz bytes in name contain a null-terminated character representation of
        the entry's owner or originator. No formal mechanism exists for avoiding name
        conflicts. By convention, vendors use their own name, such as “XYZ Computer
        Company,” as the identifier. If no name is present, namesz contains the value zero.
        Padding is present, if necessary, to ensure 4-byte alignment for the descriptor.
        Such padding is not included in namesz.

        descsz and desc
        The first descsz bytes in desc hold the note descriptor. If no descriptor is
        present, descsz contains the value zero. Padding is present, if necessary, to ensure
        4-byte alignment for the next note entry. Such padding is not included in descsz.

        type
        Provides the interpretation of the descriptor. Each originator controls its own
        types. Multiple interpretations of a single type value can exist. A program must
        recognize both the name and the type to understand a descriptor. Types currently
        must be nonnegative.

        The note segment that is shown in the following figure holds two entries.
        */

        // namesz + descsz + type
        static constexpr const int ELF_NOTE_SECTION_LENS = sizeof(uint32_t) * 3;

        while (end - buffer >= ELF_NOTE_SECTION_LENS) {
            uint32_t namesz;
            uint32_t descsz;
            uint32_t type;
            CopyFromBufferAndMove(buffer, namesz);
            CopyFromBufferAndMove(buffer, descsz);
            CopyFromBufferAndMove(buffer, type);

            // to ensure 4-byte alignment for the descriptor.
            constexpr const int ELF_NOTE_SECTION_NAME_ALIGN = 4;

            namesz = RoundUp(namesz, ELF_NOTE_SECTION_NAME_ALIGN);
            descsz = RoundUp(descsz, ELF_NOTE_SECTION_NAME_ALIGN);
            HLOGM("namesz:%u descsz:%u type:%u", namesz, descsz, type);

            // size enough ?
            if (buffer >= end) {
                return std::string();
            }
            if (type == NT_GNU_BUILD_ID) {
                char name[namesz + 1];
                CopyBytesFromBufferAndMove(buffer, &name[0], namesz);
                name[namesz] = 0;
                HLOGM("found buildid name:%s", name);
                if (strcmp(name, ELF_NOTE_GNU) == 0) {
                    std::string descString = CovertByteBufferToHexString(buffer, descsz);
                    HLOGD("found buildid:%s", descString.c_str());
                    return descString;
                } else {
                    // next
                    buffer += descsz;
                }
            } else {
                // next
                buffer += namesz + descsz;
            }
        }
        return std::string(); // found nothing
    }

private:
    bool EhFrameHDRValid_ {false};
    uint64_t ehFrameHDRElfOffset_ {0};
    uint64_t ehFrameHDRFdeCount_ {0};
    uint64_t ehFrameHDRFdeTableItemSize_ {0};
    uint64_t ehFrameHDRFdeTableElfOffset_ {0};
    OHOS::UniqueFd fd_ {-1};
    std::unique_ptr<FILE, decltype(&fclose)> readFd_ {nullptr, &fclose};
    struct ShdrInfo {
        uint64_t sectionVaddr_;
        uint64_t sectionSize_;
        uint64_t sectionFileOffset_;
        ShdrInfo(uint64_t sectionVaddr, uint64_t sectionSize, uint64_t sectionFileOffset)
            : sectionVaddr_(sectionVaddr),
              sectionSize_(sectionSize),
              sectionFileOffset_(sectionFileOffset)
        {
        }
    };
    std::map<const std::string, ShdrInfo> shdrMap_;
    void *mmap_ {MMAP_FAILED};
    uint64_t mmapSize_ = {0};

    std::string GetReadableName(std::string name) const
    {
        int status = 0;
        char *demanle = abi::__cxa_demangle(name.c_str(), 0, 0, &status);
        const std::string readableNameString((status == 0) ? demanle : name);
        free(static_cast<void *>(demanle));
        return readableNameString;
    }

    const std::string ElfStTypeName(unsigned char stt) const
    {
        switch (stt) {
            case STT_FUNC:
                return "function";
            case STT_GNU_IFUNC:
                return "gun_func";
            case STT_OBJECT:
                return "  object";
            default:
                return "  unknow";
        }
    }

    bool GetSectionInfo(const std::string &name, uint64_t &sectionVaddr, uint64_t &sectionSize,
        uint64_t &sectionFileOffset) const override
    {
        HLOGM("Section '%s' found in %zu", name.c_str(), shdrMap_.size());
        if (shdrMap_.count(name) > 0) {
            HLOGM("Section '%s' found", name.c_str());
            const auto &shdrInfo = shdrMap_.at(name);
            sectionVaddr = shdrInfo.sectionVaddr_;
            sectionSize = shdrInfo.sectionSize_;
            sectionFileOffset = shdrInfo.sectionFileOffset_;
            HLOGM("Get Section '%s' %" PRIx64 " - %" PRIx64 "", name.c_str(), sectionVaddr,
                sectionSize);
            return true;
        } else {
            HLOGW("Section '%s' not found", name.c_str());
            return false;
        }
    }

#ifndef __arm__
    bool GetHDRSectionInfo(uint64_t &ehFrameHdrElfOffset, uint64_t &fdeTableElfOffset,
        uint64_t &fdeTableSize) const override
    {
        if (EhFrameHDRValid_) {
            ehFrameHdrElfOffset = ehFrameHDRElfOffset_;
            fdeTableElfOffset = ehFrameHDRFdeTableElfOffset_;
            fdeTableSize = ehFrameHDRFdeCount_ * ehFrameHDRFdeTableItemSize_;
            return true;
        } else {
            HLOGW("!EhFrameHDRValid_");
            return false;
        }
    }
#endif

    void DumpEhFrameHDR() const
    {
        HLOGD("  ehFrameHDRElfOffset_:          0x%" PRIx64 "", ehFrameHDRElfOffset_);
        HLOGD("  ehFrameHDRFdeCount_:           0x%" PRIx64 "", ehFrameHDRFdeCount_);
        HLOGD("  ehFrameHDRFdeTableElfOffset_:  0x%" PRIx64 "", ehFrameHDRFdeTableElfOffset_);
        HLOGD("  ehFrameHDRFdeTableItemSize_:   0x%" PRIx64 "", ehFrameHDRFdeTableItemSize_);
    }

    bool LoadEhFrameHDR(const unsigned char *buffer, size_t bufferSize, uint64_t shdrOffset)
    {
        eh_frame_hdr *ehFrameHdr = (eh_frame_hdr *)buffer;
        const uint8_t *dataPtr = ehFrameHdr->encode_data;
        DwarfEncoding dwEhFramePtr(ehFrameHdr->eh_frame_ptr_enc, dataPtr);
        DwarfEncoding dwFdeCount(ehFrameHdr->fde_count_enc, dataPtr);
        DwarfEncoding dwTable(ehFrameHdr->table_enc, dataPtr);
        DwarfEncoding dwTableValue(ehFrameHdr->table_enc, dataPtr);

        HLOGD("eh_frame_hdr:");
        HexDump(ehFrameHdr, BITS_OF_FOUR_BYTE, bufferSize);
        unsigned char version = ehFrameHdr->version;
        HLOGD("  version:           %02x:%s", version, (version == 1) ? "valid" : "invalid");
        HLOGD("  eh_frame_ptr_enc:  %s", dwEhFramePtr.ToString().c_str());
        HLOGD("  fde_count_enc:     %s", dwFdeCount.ToString().c_str());
        HLOGD("  table_enc:         %s", dwTable.ToString().c_str());
        HLOGD("  table_enc:         %s", dwTable.ToString().c_str());
        HLOGD("  table_value_enc:   %s", dwTableValue.ToString().c_str());
        HLOGD("  table_offset_in_hdr:   %zu", dwTable.GetData() - buffer);

        if (version != 1) {
            HLOGD("eh_frame_hdr version is invalid");
            return false;
        }
        EhFrameHDRValid_ = true;
        ehFrameHDRElfOffset_ = shdrOffset;
        ehFrameHDRFdeTableElfOffset_ = dwTable.GetData() - buffer + shdrOffset;
        DumpEhFrameHDR();
        return true;
    }

    void LoadFileToMemory(const std::string &loadElfPath)
    {
#ifndef USE_MMAP
        FILE* fp = fopen(loadElfPath.c_str(), "rb");
        if (fp != nullptr) {
            readFd_.reset(fp);
        } else {
            readFd_.reset();
        }
        return;
#else
#if is_mingw
        fd_ = OHOS::UniqueFd(open(loadElfPath.c_str(), O_RDONLY | O_BINARY));
#else
        fd_ = OHOS::UniqueFd(open(loadElfPath.c_str(), O_RDONLY));
#endif
        if (fd_ != -1) {
            struct stat sb = {};

            if (fstat(fd_, &sb) == -1) {
                HLOGE("unable to check the file size");
            } else {
                HLOGV("file stat size %" PRIu64 "", sb.st_size);

                // unmap it first
                if (mmap_ != MMAP_FAILED) {
                    munmap(mmap_, mmapSize_);
                }

                mmap_ = mmap(0, sb.st_size, PROT_READ, MAP_PRIVATE, fd_, 0);
                if (mmap_ == MMAP_FAILED) {
                    HLOGE("unable to map the file size %" PRIu64 " ", sb.st_size);
                    mmapSize_ = 0;
                } else {
                    mmapSize_ = sb.st_size;
                    HLOGD("mmap build with size %" PRIu64 " ", mmapSize_);
                }
            }
        } else {
            HLOGD("elf file open failed with %s by %s", loadElfPath.c_str(), strerror(errno));
            return;
        }
#endif
    }

    bool ParseSymTab(const std::unique_ptr<ElfFile> &elfFile,
        const std::unique_ptr<ELF::SectionHeader> &shdr, std::vector<Symbol> &symbolsTable) const
    {
        HLOGV("ParseSymTable");
        if (!elfFile->ParseSymTable(shdr.get())) {
            return false;
        }

        HLOGV("Symbol Table:%s", shdr->secTypeName_.c_str());
        HLOGM("%*s|%16s|%4s|%s", MAX_SYMBOLS_TYPE_NAME_LEN, "type", "addr", "size", "name");

        for (const std::unique_ptr<ElfSymbol> &symbol : elfFile->symTable_->symbols_) {
            if (ELF64_ST_TYPE(symbol->symInfo_) == STT_FUNC or
                ELF64_ST_TYPE(symbol->symInfo_) == STT_GNU_IFUNC) {
                /*
                    name|            addr|size|name
                function|00000000c0102b8c|  56|__lookup_processor_type
                function|00000000c0102bd4|   0|__error_p
                function|00000000c0008224|  64|__vet_atags
                function|00000000c0008144| 128|__fixup_smp
                function|00000000c00081d0|  64|__fixup_pv_table
                function|00000000c000808c| 168|__create_page_tables
                function|00000000c0b002e0|  68|__mmap_switched
                function|00000000c0102acc|  20|__enable_mmu
                object|00000000c0102ac0|   0|__secondary_data
                function|00000000c0102ae0|  20|__do_fixup_smp_on_up
                */

                std::string name = elfFile->GetStrPtr(shdr->link_, symbol->nameIndex_);
                uint64_t base = symbol->symValue_;
                uint64_t len = symbol->symSize_;
                std::string type = ElfStTypeName(ELF64_ST_TYPE(symbol->symInfo_));

                HLOGM("%10s|%016" PRIx64 "|%4" PRIu64 "|%s", type.c_str(), base, len,
                    GetReadableName(name).c_str());

                if (base == 0) {
                    continue; // we dont need 0 addr symbol
                }
                Symbol symbol(base, len, name, filePath_); // we use full name here
                symbol.demangle_ = GetReadableName(name);
                symbolsTable.emplace_back(symbol);
            } else {
                continue;
            }
        } // for symbols
        return true;
    }
    bool ParseShdr(const std::unique_ptr<ElfFile> elfFile, std::vector<Symbol> &symbolsTable,
        std::vector<Symbol> &dynamicSymbolsTable, std::string &buildIdFound)
    {
        // walkthough
        for (const auto &shdrPair : elfFile->shdrs_) {
            const auto &shdr = shdrPair.second;
            const char *sh_name =
                elfFile->GetStrPtr(elfFile->ehdr_->shdrStrTabIdx_, shdr->nameIndex_);
            const unsigned char *data = elfFile->GetSectionData(shdr->secIndex_);

            if (sh_name == nullptr || data == nullptr) {
                HLOGE("name %p or data %p get failed.", sh_name, data);
                return false;
            }

            HLOGVVV("shdr name '%s' vaddr 0x%" PRIx64 " offset 0x%" PRIx64 " size 0x%" PRIx64
                    " type 0x%" PRIx64 "(%s) index %u link 0x%u entry 0x%" PRIx64 "",
                sh_name, shdr->secVaddr_, shdr->fileOffset_, shdr->secSize_, shdr->secType_,
                shdr->secTypeName_.c_str(), shdr->secIndex_, shdr->link_, shdr->secEntrySize_);

            shdrMap_.emplace(sh_name, ShdrInfo(shdr->secVaddr_, shdr->secSize_, shdr->fileOffset_));

            switch (shdr->secType_) {
                case SHT_SYMTAB:
                    if (!ParseSymTab(elfFile, shdr, symbolsTable)) {
                        return false;
                    }
                    break;
                case SHT_DYNSYM:
                    if (!ParseSymTab(elfFile, shdr, dynamicSymbolsTable)) {
                        return false;
                    }
                    break;
                case SHT_NOTE:
                    // notes
                    if (buildIdFound.empty()) {
                        // we use our function, not from gelf_getnote
                        HLOGM("found NOTE_GNU_BUILD_ID size:  %" PRIu64 "", shdr->secSize_);

                        // there will be a log of note sh , we just need the right one
                        buildIdFound = ElfGetBuildId(data, shdr->secSize_);
                    }
                    break;
#ifndef __arm__
                case SHT_PROGBITS:
                    if (EH_FRAME_HR == sh_name) {
                        LoadEhFrameHDR(data, shdr->secSize_, shdr->fileOffset_);
                    }
                    break;
#endif
                default:
                    HLOGM("skip shdr.sh_type %" PRIx64 "", shdr->secType_);
                    break;
            } // for shdr
        }     // for each shdrs_
        return true;
    }

    std::unique_ptr<ElfFile> LoadElfFile(std::string &elfPath)
    {
        HLOGD("try load elf %s", elfPath.c_str());
        if (elfPath.empty()) {
            elfPath = filePath_;
            HLOGD("use default elf path %s\n", elfPath.c_str());
        }
        return ElfFile::MakeUnique(elfPath);
    }

    void UpdateSymbols(std::vector<Symbol> &symbolsTable, std::vector<Symbol> &dynamicSymbolsTable,
        const std::string &elfPath)
    {
        symbols_.clear();
        HLOGD("%zu symbols loadded from symbolsTable.", symbolsTable.size());
        HLOGD("%zu symbols loadded from dynamicSymbolsTable.", dynamicSymbolsTable.size());

        if (symbolsTable.size() > dynamicSymbolsTable.size()) {
            symbols_ = symbolsTable;
        } else {
            symbols_ = dynamicSymbolsTable;
        }
        AdjustSymbols();
        HLOGD("%zu symbols loadded from elf '%s'.", symbols_.size(), elfPath.c_str());
        if (buildId_.empty()) {
            HLOGD("buildId not found from elf '%s'.", elfPath.c_str());
            // dont failed. some time the lib have not got the build id
            // buildId not found from elf '/system/bin/ld-musl-arm.so.1'.
        }
    }

    bool LoadElfSymbols(std::string elfPath)
    {
#ifdef HIPERF_DEBUG_TIME
        const auto startTime = steady_clock::now();
#endif
        std::unique_ptr<ElfFile> elfFile = LoadElfFile(elfPath);
        if (elfFile == nullptr) {
            HLOGD("elf load failed");
            return false;
        } else {
            HLOGD("loaded elf %s", elfPath.c_str());
        }
        // we prepare two table here
        // only one we will push in to symbols_
        // or both drop if build id is not same
        std::vector<Symbol> symbolsTable, dynamicSymbolsTable;
        std::string buildIdFound;
        for (auto &phdr : elfFile->phdrs_) {
            if ((phdr->type_ == PT_LOAD) && (phdr->flags_ & PF_X)) {
                // find the min addr
                if (textExecVaddr_ != std::min(textExecVaddr_, phdr->vaddr_)) {
                    textExecVaddr_ = std::min(textExecVaddr_, phdr->vaddr_);
                    textExecVaddrFileOffset_ = phdr->offset_;
                }
            }
        }

        HLOGD("textExecVaddr_ 0x%016" PRIx64 " file offset 0x%016" PRIx64 "", textExecVaddr_,
            textExecVaddrFileOffset_);

        if (!ParseShdr(std::move(elfFile), symbolsTable, dynamicSymbolsTable, buildIdFound)) {
            return false;
        }

        if (UpdateBuildIdIfMatch(buildIdFound)) {
            UpdateSymbols(symbolsTable, dynamicSymbolsTable, elfPath);
        } else {
            HLOGW("symbols will not update for '%s' because buildId is not match.",
                elfPath.c_str());
            // this mean failed . we dont goon for this.
            return false;
        }

        // mmap it for later use
        LoadFileToMemory(elfPath);
#ifdef HIPERF_DEBUG_TIME
        auto usedTime = duration_cast<microseconds>(steady_clock::now() - startTime);
        if (usedTime.count() != 0) {
            HLOGV("cost %0.3f ms to load symbols '%s'",
                usedTime.count() / static_cast<double>(milliseconds::duration::period::den),
                elfPath.c_str());
        }
#endif
        return true;
    }

    uint64_t GetVaddrInSymbols(uint64_t ip, uint64_t mapStart,
        uint64_t mapPageOffset) const override
    {
        /*
            00200000-002c5000 r--p 00000000 08:02 46400311
            002c5000-00490000 r-xp 000c5000 08:02 4640031

            [14] .text             PROGBITS         00000000002c5000  000c5000

            if ip is 0x46e6ab
            1. find the map range is 002c5000-00490000
            2. ip - map start(002c5000) = map section offset
            3. map section offset + map page offset(000c5000) = elf file offset
            4. elf file offset - exec file offset(000c5000)
                = ip offset (ip always in exec file offset)
            5. ip offset + exec begin vaddr(2c5000) = virtual ip in elf
        */
        uint64_t vaddr = ip - mapStart + mapPageOffset - textExecVaddrFileOffset_ + textExecVaddr_;
        HLOGM(" ip :0x%016" PRIx64 " -> elf offset :0x%016" PRIx64 " -> vaddr :0x%016" PRIx64 " ",
            ip, ip - mapStart + mapPageOffset, vaddr);
        HLOGM("(minExecAddrFileOffset_ is 0x%" PRIx64 " textExecVaddr_ is 0x%" PRIx64 ")",
            textExecVaddrFileOffset_, textExecVaddr_);
        return vaddr;
    }
};

class KernelSymbols : public ElfFileSymbols {
public:
    explicit KernelSymbols(const std::string symbolFilePath)
        : ElfFileSymbols(symbolFilePath, SYMBOL_KERNEL_FILE)
    {
    }

    static constexpr const int KSYM_MIN_TOKENS = 3;

    bool ParseKallsymsLine()
    {
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
        const auto startTime = steady_clock::now();
        std::chrono::microseconds parseLineTime = std::chrono::microseconds::zero();
        std::chrono::microseconds sscanfTime = std::chrono::microseconds::zero();
        std::chrono::microseconds newTime = std::chrono::microseconds::zero();
        std::chrono::microseconds readFileTime = std::chrono::microseconds::zero();
#endif
        size_t lines = 0;
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
        const auto eachFileStartTime = steady_clock::now();
#endif
        std::string kallsym;
        if (!ReadFileToString("/proc/kallsyms", kallsym)) {
            HLOGW("/proc/kallsyms load failed.");
            return false;
        }
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
        // any way we finish the line scan
        readFileTime += duration_cast<milliseconds>(steady_clock::now() - eachFileStartTime);
#endif
        char *lineBegin = kallsym.data();
        char *dataEnd = lineBegin + kallsym.size();
        while (lineBegin < dataEnd) {
            char *lineEnd = strchr(lineBegin, '\n');
            if (lineEnd != nullptr) {
                *lineEnd = '\0';
            }
            size_t lineSize = (lineEnd != nullptr) ? (lineEnd - lineBegin) : (dataEnd - lineBegin);

#ifdef HIPERF_DEBUG_SYMBOLS_TIME
            const auto eachLineStartTime = steady_clock::now();
#endif
            lines++;
            uint64_t addr = 0;
            char type = '\0';

            char nameRaw[lineSize];
            char moduleRaw[lineSize];
            int ret = sscanf_s(lineBegin, "%" PRIx64 " %c %s%s", &addr, &type, sizeof(type),
                nameRaw, sizeof(nameRaw), moduleRaw, sizeof(moduleRaw));

            lineBegin = lineEnd + 1;
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
            // any way we finish the line scan
            sscanfTime += duration_cast<milliseconds>(steady_clock::now() - eachLineStartTime);
#endif
            if (ret >= KSYM_MIN_TOKENS) {
                if (ret == KSYM_MIN_TOKENS) {
                    moduleRaw[0] = '\0';
                }
                HLOGM(" 0x%016" PRIx64 " %c '%s' '%s'", addr, type, nameRaw, moduleRaw);
            } else {
                HLOGW("unknow line %d: '%s'", ret, lineBegin);
                continue;
            }
            std::string name = nameRaw;
            std::string module = moduleRaw;

            /*
            T
            The symbol is in the text (code) section.

            W
            The symbol is a weak symbol that has not been specifically
            tagged as a weak object symbol. When a weak defined symbol is
            linked with a normal defined symbol, the normal defined symbol
            is used with no error. When a weak undefined symbol is linked
            and the symbol is not defined, the value of the weak symbol
            becomes zero with no error.
            */
            if (addr != 0 && strchr("TtWw", type)) {
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
                const auto eachNewSymbolTime = steady_clock::now();
#endif
                // we only need text symbols
                symbols_.emplace_back(addr, name, module.empty() ? filePath_ : module);
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
                newTime += duration_cast<milliseconds>(steady_clock::now() - eachNewSymbolTime);
#endif
            }
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
            parseLineTime += duration_cast<milliseconds>(steady_clock::now() - eachLineStartTime);
#endif
        }
#ifdef HIPERF_DEBUG_SYMBOLS_TIME
        std::chrono::microseconds usedTime =
            duration_cast<milliseconds>(steady_clock::now() - startTime);
        printf("parse kernel symbols use : %0.3f ms\n", usedTime.count() / MS_DUARTION);
        printf("parse line use : %0.3f ms\n", parseLineTime.count() / MS_DUARTION);
        printf("sscanf line use : %0.3f ms\n", sscanfTime.count() / MS_DUARTION);
        printf("new symbols use : %0.3f ms\n", newTime.count() / MS_DUARTION);
        printf("read file use : %0.3f ms\n", readFileTime.count() / MS_DUARTION);
#endif
        HLOGD("%zu line processed(%zu symbols)", lines, symbols_.size());
        return true;
    }

    const std::string KPTR_RESTRICT = "/proc/sys/kernel/kptr_restrict";

    bool LoadKernelSyms()
    {
        HLOGD("try read /proc/kallsyms");
        if (access("/proc/kallsyms", R_OK) != 0) {
            printf("No vmlinux path is given, and kallsyms cannot be opened\n");
            return false;
        }

        if (ReadFileToString(KPTR_RESTRICT).front() != '0') {
            printf("/proc/sys/kernel/kptr_restrict is NOT 0\n");
            if (!WriteStringToFile(KPTR_RESTRICT, "0")) {
                printf("/proc/sys/kernel/kptr_restrict write failed and we cant not change it.\n");
            }
        }

        // getline end
        if (!ParseKallsymsLine()) {
            return false;
        }

        if (symbols_.empty()) {
            printf("The symbol table addresses in /proc/kallsyms are all 0.\n"
                   "Please check the value of /proc/sys/kernel/kptr_restrict, it "
                   "should be 0.\n"
                   "Or provide a separate vmlinux path.\n");

            if (buildId_.size() != 0) {
                // but we got the buildid , so we make a dummpy symbols
                HLOGD("kallsyms not found. but we have the buildid");
                return true;
            } else {
                // we got nothing
                return false;
            }
        } else {
            AdjustSymbols();
            printf("%zu symbols_ loadded from kallsyms.\n", symbols_.size());
            return true;
        }
    }
    virtual bool LoadSymbols(const std::string &symbolFilePath) override
    {
        loaded_ = true;
        HLOGV("KernelSymbols try read '%s' search paths size %zu", symbolFilePath.c_str(),
            symbolsFileSearchPaths_.size());

        if (symbolsFileSearchPaths_.size() == 0) {
            // try read
            HLOGD("try read /sys/kernel/notes");
            std::string notes = ReadFileToString("/sys/kernel/notes");
            if (notes.empty()) {
                printf("notes cannot be opened, unable get buildid\n");
                return false;
            } else {
                HLOGD("kernel notes size: %zu", notes.size());
                buildId_ = ElfGetBuildId((const unsigned char *)notes.data(), notes.size());
            }

            const auto startTime = std::chrono::steady_clock::now();
            if (!LoadKernelSyms()) {
                printf("parse kalsyms failed.\n");
                return false;
            } else {
                const auto thisTime = std::chrono::steady_clock::now();
                const auto usedTimeMsTick =
                    std::chrono::duration_cast<std::chrono::milliseconds>(thisTime - startTime);
                printf("Load kernel symbols (total %" PRId64 " ms)\n", (int64_t)usedTimeMsTick.count());
                // load complete
                return true;
            }
        } // no search path

        // try vmlinux
        return ElfFileSymbols::LoadSymbols(KERNEL_ELF_NAME);
    }
    virtual uint64_t GetVaddrInSymbols(uint64_t ip, uint64_t mapStart, uint64_t) const override
    {
        // ip is vaddr in /proc/kallsyms
        return ip;
    }
    ~KernelSymbols() override {}
};

class KernelModuleSymbols : public ElfFileSymbols {
public:
    explicit KernelModuleSymbols(const std::string symbolFilePath) : ElfFileSymbols(symbolFilePath)
    {
        HLOGV("create %s", symbolFilePath.c_str());
        symbolFileType_ = SYMBOL_KERNEL_MODULE_FILE;
        module_ = symbolFilePath;
    }
    virtual bool LoadSymbols(const std::string &symbolFilePath) override
    {
        loaded_ = true;
        if (module_ == filePath_) {
            // file name sitll not convert to ko file path
            // this is in record mode
            HLOGV("find ko name %s", module_.c_str());
            for (const std::string &path : kernelModulePaths) {
                if (access(path.c_str(), R_OK) == 0) {
                    std::string koPath = path + module_ + KERNEL_MODULES_EXT_NAME;
                    HLOGV("found ko in %s", koPath.c_str());
                    if (access(koPath.c_str(), R_OK) == 0) {
                        // create symbol
                        filePath_ = koPath;
                        break; // find next ko
                    }
                }
            }
            LoadBuildId();
        } else {
            HLOGV("we have file path, load with %s", filePath_.c_str());
            return ElfFileSymbols::LoadSymbols(filePath_);
        }
        return false;
    }
    virtual uint64_t GetVaddrInSymbols(uint64_t ip, uint64_t mapStart, uint64_t) const override
    {
        return ip - mapStart;
    }

private:
    bool LoadBuildId()
    {
        std::string sysFile = "/sys/module/" + module_ + "/notes/.note.gnu.build-id";
        std::string buildIdRaw = ReadFileToString(sysFile);
        if (!buildIdRaw.empty()) {
            buildId_ = ElfGetBuildId((const unsigned char *)buildIdRaw.data(), buildIdRaw.size());
            HLOGD("kerne module %s(%s) build id %s", module_.c_str(), filePath_.c_str(),
                buildId_.c_str());
            return buildId_.empty() ? false : true;
        }
        return false;
    }

    const std::vector<std::string> kernelModulePaths = {"/vendor/modules/"};
    std::string module_ = "";
};

class JavaFileSymbols : public ElfFileSymbols {
public:
    explicit JavaFileSymbols(const std::string symbolFilePath) : ElfFileSymbols(symbolFilePath)
    {
        symbolFileType_ = SYMBOL_KERNEL_FILE;
    }
    virtual bool LoadSymbols(const std::string &symbolFilePath) override
    {
        loaded_ = true;
        return false;
    }
    ~JavaFileSymbols() override {}

    virtual uint64_t GetVaddrInSymbols(uint64_t ip, uint64_t mapStart,
        uint64_t mapPageOffset) const override
    {
        // this is different with elf
        // elf use  ip - mapStart + mapPageOffset - minExecAddrFileOffset_ + textExecVaddr_
        return ip - mapStart + mapPageOffset;
    }
};

class JSFileSymbols : public ElfFileSymbols {
public:
    explicit JSFileSymbols(const std::string symbolFilePath) : ElfFileSymbols(symbolFilePath)
    {
        symbolFileType_ = SYMBOL_KERNEL_FILE;
    }
    virtual bool LoadSymbols(const std::string &symbolFilePath) override
    {
        loaded_ = true;
        return false;
    }
    ~JSFileSymbols() override {}
};

class UnknowFileSymbols : public SymbolsFile {
public:
    explicit UnknowFileSymbols(const std::string symbolFilePath)
        : SymbolsFile(SYMBOL_UNKNOW_FILE, symbolFilePath)
    {
    }
    virtual bool LoadSymbols(const std::string &symbolFilePath) override
    {
        loaded_ = true;
        return false;
    }
    ~UnknowFileSymbols() override {}
};

SymbolsFile::~SymbolsFile() {}

std::unique_ptr<SymbolsFile> SymbolsFile::CreateSymbolsFile(SymbolsFileType symbolType,
    const std::string symbolFilePath)
{
    switch (symbolType) {
        case SYMBOL_KERNEL_FILE:
            return std::make_unique<KernelSymbols>(
                symbolFilePath.empty() ? KERNEL_MMAP_NAME : symbolFilePath);
        case SYMBOL_KERNEL_MODULE_FILE:
            return std::make_unique<KernelModuleSymbols>(symbolFilePath);
        case SYMBOL_ELF_FILE:
            return std::make_unique<ElfFileSymbols>(symbolFilePath);
        case SYMBOL_JAVA_FILE:
            return std::make_unique<JavaFileSymbols>(symbolFilePath);
        case SYMBOL_JS_FILE:
            return std::make_unique<JSFileSymbols>(symbolFilePath);
        default:
            return std::make_unique<SymbolsFile>(SYMBOL_UNKNOW_FILE, symbolFilePath);
    }
}

std::unique_ptr<SymbolsFile> SymbolsFile::CreateSymbolsFile(const std::string &symbolFilePath)
{
    // we need check file name here
    if (symbolFilePath == KERNEL_MMAP_NAME) {
        return SymbolsFile::CreateSymbolsFile(SYMBOL_KERNEL_FILE, symbolFilePath);
    } else if (StringEndsWith(symbolFilePath, KERNEL_MODULES_EXT_NAME)) {
        return SymbolsFile::CreateSymbolsFile(SYMBOL_KERNEL_MODULE_FILE, symbolFilePath);
    } else {
        // default is elf
        return SymbolsFile::CreateSymbolsFile(SYMBOL_ELF_FILE, symbolFilePath);
    }
}

void SymbolsFile::AdjustSymbols()
{
    if (symbols_.size() <= 1u) {
        return;
    }

    // order
    sort(symbols_.begin(), symbols_.end(), Symbol::CompareLT);
    HLOGV("sort completed");

    size_t fullSize = symbols_.size();
    size_t erased = 0;
    // Check for duplicate values

    auto last = std::unique(symbols_.begin(), symbols_.end());
    symbols_.erase(last, symbols_.end());
    erased = fullSize - symbols_.size();
    HLOGV("uniqued completed");
    auto it = symbols_.begin();
    while (it != symbols_.end()) {
        it->index_ = it - symbols_.begin();
        it++;
    }
    HLOGV("indexed completed");

    HLOG_ASSERT(symbols_.size() != 0);

    if (textExecVaddrRange_ == maxVaddr) {
        textExecVaddrRange_ = symbols_.back().ipVaddr_ - symbols_.front().ipVaddr_;
    }

    HLOGDDD("%zu symbols after adjust (%zu erased) 0x%016" PRIx64 " - 0x%016" PRIx64
            " @0x%016" PRIx64 " ",
        symbols_.size(), erased, symbols_.front().ipVaddr_, symbols_.back().ipVaddr_,
        textExecVaddrFileOffset_);
}

const std::vector<Symbol> &SymbolsFile::GetSymbols()
{
    return symbols_;
}

const Symbol SymbolsFile::GetSymbolWithVaddr(uint64_t vaddr) const
{
#ifdef HIPERF_DEBUG_TIME
    const auto startTime = steady_clock::now();
#endif
    Symbol symbol;
    // it should be already order from small to large
    if (symbols_.size() > 0u) {
        auto found =
            std::upper_bound(symbols_.begin(), symbols_.end(), vaddr, Symbol::ValueLessThanElem);
        /*
        if data is { 1, 2, 4, 5, 5, 6 };
        upper_bound for each val :
            0 < 1 at index 0
            1 < 2 at index 1
            2 < 4 at index 2
            3 < 4 at index 2
            4 < 5 at index 3
            5 < 6 at index 5
            6 < not found
        */
        if (found != symbols_.begin()) {
            found = std::prev(found);
            if (found->contain(vaddr)) {
                found->Matched();
                symbol = *found;
                HLOGV("found '%s' for vaddr 0x%016" PRIx64 "", found->ToString().c_str(), vaddr);
            }
        }
    }

    if (!symbol.isValid()) {
        HLOGV("NOT found vaddr 0x%" PRIx64 " in symbole file %s(%zu)", vaddr, filePath_.c_str(),
            symbols_.size());
    }

    symbol.setIpVAddress(vaddr); // update vaddr again for return new one
#ifdef HIPERF_DEBUG_TIME
    auto usedTime = duration_cast<milliseconds>(steady_clock::now() - startTime);
    if (usedTime > 1ms) {
        HLOGW("cost %" PRId64 "ms to search ", usedTime.count());
    }
#endif
    return symbol;
}

bool SymbolsFile::CheckPathReadable(const std::string &path) const
{
    if (access(path.c_str(), R_OK) == 0) {
        return true;
    } else {
        HLOGM("'%s' is unable read", path.c_str());
        return false;
    }
}

bool SymbolsFile::setSymbolsFilePath(const std::vector<std::string> &symbolsSearchPaths)
{
    symbolsFileSearchPaths_.clear();
    for (auto &symbolsSearchPath : symbolsSearchPaths) {
        if (CheckPathReadable(symbolsSearchPath)) {
            symbolsFileSearchPaths_.emplace_back(symbolsSearchPath);
            HLOGV("'%s' is add to symbolsSearchPath", symbolsSearchPath.c_str());
        }
    }
    return (symbolsFileSearchPaths_.size() > 0);
}

std::unique_ptr<SymbolsFile> SymbolsFile::LoadSymbolsFromSaved(
    const SymbolFileStruct &symbolFileStruct)
{
    auto symbolsFile = CreateSymbolsFile(symbolFileStruct.filePath_);
    symbolsFile->filePath_ = symbolFileStruct.filePath_;
    symbolsFile->symbolFileType_ = (SymbolsFileType)symbolFileStruct.symbolType_;
    symbolsFile->textExecVaddr_ = symbolFileStruct.textExecVaddr_;
    symbolsFile->textExecVaddrFileOffset_ = symbolFileStruct.textExecVaddrFileOffset_;
    symbolsFile->buildId_ = symbolFileStruct.buildId_;
    for (auto &symbolStruct : symbolFileStruct.symbolStructs_) {
        symbolsFile->symbols_.emplace_back(symbolStruct.vaddr_, symbolStruct.len_,
            symbolStruct.symbolName_, symbolFileStruct.filePath_);
    }
    symbolsFile->AdjustSymbols(); // reorder
    HLOGV("load %zu symbol from SymbolFileStruct for file '%s'", symbolsFile->symbols_.size(),
        symbolsFile->filePath_.c_str());
    return symbolsFile;
}

const SymbolFileStruct SymbolsFile::exportSymbolToFileFormat(bool onlyMatched)
{
    SymbolFileStruct symbolFileStruct;

    symbolFileStruct.filePath_ = filePath_;
    symbolFileStruct.symbolType_ = symbolFileType_;
    symbolFileStruct.textExecVaddr_ = textExecVaddr_;
    symbolFileStruct.textExecVaddrFileOffset_ = textExecVaddrFileOffset_;
    symbolFileStruct.buildId_ = buildId_;

    auto symbols = GetSymbols();

    for (auto &symbol : symbols) {
        if (onlyMatched && !symbol.matched_) {
            continue;
        }
        auto &symbolStruct = symbolFileStruct.symbolStructs_.emplace_back();
        symbolStruct.vaddr_ = symbol.vaddr_;
        symbolStruct.len_ = symbol.len_;
        symbolStruct.symbolName_ = symbol.GetName();
    }
    HLOGV("export %zu symbol to SymbolFileStruct from %s", symbolFileStruct.symbolStructs_.size(),
        filePath_.c_str());
    return symbolFileStruct;
}

uint64_t SymbolsFile::GetVaddrInSymbols(uint64_t ip, uint64_t, uint64_t) const
{
    // no convert
    return ip;
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS

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

#include <elf_parser.h>

using namespace OHOS::Developtools::NativeDaemon::ELF;
namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
std::unique_ptr<ElfSymbol> ElfSymbol::MakeUnique(char * const symBuf, const std::size_t bufSize)
{
    std::unique_ptr<ElfSymbol> sym {new (std::nothrow) ElfSymbol()};
    if (sym == nullptr) {
        HLOGE("Error in ElfSymbol::MakeUnique(): ElfSymbol::ElfSymbol() failed");
        return nullptr;
    }
    if (!sym->Init(symBuf, bufSize)) {
        HLOGE("ElfSymbol::Init(symBuf, bufSize) failed");
        DumpSymBuf(symBuf, bufSize);
        return nullptr;
    }
    return sym;
}

bool ElfSymbol::ParseElf32Symbol(char * const symBuf)
{
    uint32_t *u4Buf = reinterpret_cast<uint32_t *>(symBuf);
    constexpr int nameOffset {0};
    nameIndex_ = u4Buf[nameOffset];
    constexpr int valueOffset {1};
    symValue_ = u4Buf[valueOffset];
    constexpr int sizeOffset {2};
    symSize_ = u4Buf[sizeOffset];
    constexpr int infoOffset {12};
    symInfo_ = symBuf[infoOffset];
    constexpr int otherInfoOffset {13};
    symOtherInfo_ = symBuf[otherInfoOffset];
    uint16_t *u2Buf = reinterpret_cast<uint16_t *>(symBuf);
    constexpr int secOffset {7};
    secIndex_ = u2Buf[secOffset];
    return true;
}

bool ElfSymbol::ParseElf64Symbol(char * const symBuf)
{
    uint32_t *u4Buf = reinterpret_cast<uint32_t *>(symBuf);
    constexpr int nameOffset {0};
    nameIndex_ = u4Buf[nameOffset];
    constexpr int infoOffset {4};
    symInfo_ = symBuf[infoOffset];
    constexpr int otherInfoOffset {5};
    symOtherInfo_ = symBuf[otherInfoOffset];
    uint16_t *u2Buf = reinterpret_cast<uint16_t *>(symBuf);
    constexpr int secOffset {3};
    secIndex_ = u2Buf[secOffset];
    uint64_t *u8Buf = reinterpret_cast<uint64_t *>(symBuf);
    constexpr int valueOffset {1};
    symValue_ = u8Buf[valueOffset];
    constexpr int sizeOffset {2};
    symSize_ = u8Buf[sizeOffset];
    return true;
}

std::unique_ptr<SymbolTable> SymbolTable::MakeUnique(const std::string &symNamesStr,
    const char * const secBuf, const uint64_t secSize, const uint64_t entrySize)
{
    std::unique_ptr<SymbolTable> symTable {new (std::nothrow) SymbolTable(symNamesStr)};
    if (symNamesStr.empty()) {
        HLOGE("symNamesStr is empty");
    }
    if (symTable == nullptr) {
        HLOGE("Error in SymbleTable::MakeUnique(): SymbleTable::SymbolTable() failed");
        return nullptr;
    }
    char *symBuf = const_cast<char *>(secBuf);
    for (uint64_t curPos = 0; curPos < secSize; curPos += entrySize) {
        symBuf = const_cast<char *>(secBuf + curPos);
        if ((curPos + entrySize) >= secSize) {
            break;
        }
        std::unique_ptr<ElfSymbol> sym = ElfSymbol::MakeUnique(symBuf, entrySize);
        if (sym == nullptr) {
            HLOGE("Error in SymbolTable::MakeUnique(): ElfSymbol::MakeUnique() failed");
            return nullptr;
        }
        symTable->symbols_.emplace_back(std::move(sym));
    }
    return symTable;
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
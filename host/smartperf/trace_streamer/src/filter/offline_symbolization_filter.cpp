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

#include "offline_symbolization_filter.h"
namespace SysTuning {
namespace TraceStreamer {
OfflineSymbolizationFilter::OfflineSymbolizationFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter),
      symbolTablePtrAndStValueToSymAddr_(nullptr),
      filePathIdAndStValueToSymAddr_(nullptr)
{
}
std::shared_ptr<std::vector<std::shared_ptr<FrameInfo>>> OfflineSymbolizationFilter::OfflineSymbolization(
    const std::shared_ptr<std::vector<uint64_t>> ips)
{
    auto result = std::make_shared<std::vector<std::shared_ptr<FrameInfo>>>();
    for (auto itor = ips->begin(); itor != ips->end(); itor++) {
        auto frameInfo = OfflineSymbolization(*itor);
        // If the IP in the middle of the call stack cannot be symbolized, the remaining IP is discarded
        if (!frameInfo) {
            break;
        }
        result->emplace_back(frameInfo);
    }
    return result;
}
template <class T>
void OfflineSymbolizationFilter::UpdateFrameInfo(T* elfSym,
                                                 uint32_t& symbolStart,
                                                 uint64_t symVaddr,
                                                 uint64_t ip,
                                                 FrameInfo* frameInfo)
{
    if (elfSym->st_value + elfSym->st_size >= symVaddr) {
        symbolStart = elfSym->st_name;
        frameInfo->offset_ = elfSym->st_value != 0 ? elfSym->st_value : ip;
        frameInfo->symbolOffset_ = symVaddr - elfSym->st_value;
    }
}
std::shared_ptr<FrameInfo> OfflineSymbolizationFilter::OfflineSymbolization(uint64_t ip)
{
    if (ipToFrameInfo_.count(ip)) {
        return ipToFrameInfo_.at(ip);
    }
    // start symbolization
    std::shared_ptr<FrameInfo> frameInfo = std::make_shared<FrameInfo>();
    frameInfo->ip_ = ip;
    uint64_t vmStart = INVALID_UINT64;
    uint64_t vmOffset = INVALID_UINT64;
    auto endItor = startAddrToMapsInfoMap_.upper_bound(ip);
    auto length = std::distance(startAddrToMapsInfoMap_.begin(), endItor);
    if (length > 0) {
        endItor--;
        // Follow the rules of front closing and rear opening, [start, end)
        if (ip < endItor->second->end()) {
            vmStart = endItor->second->start();
            vmOffset = endItor->second->offset();
            frameInfo->filePathId_ = endItor->second->file_path_id();
        }
    }
    if (frameInfo->filePathId_ == INVALID_UINT32) {
        // find matching MapsInfo failed!!!
        TS_LOGD("find matching Maps Info failed, ip = %lu", ip);
        return nullptr;
    }
    // find SymbolTable by filePathId
    auto itor = filePathIdToSymbolTableMap_.find(frameInfo->filePathId_);
    if (itor == filePathIdToSymbolTableMap_.end()) {
        // find matching SymbolTable failed, but filePathId is availiable
        ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
        TS_LOGD("find matching filePathId failed, ip = %lu, filePathId = %u", ip, frameInfo->filePathId_);
        return frameInfo;
    }
    auto symbolTable = itor->second;
    // calculate symVaddr = ip - vmStart + vmOffset + phdrVaddr - phdrOffset
    uint64_t symVaddr =
        ip - vmStart + vmOffset + symbolTable->text_exec_vaddr() - symbolTable->text_exec_vaddr_file_offset();
    frameInfo->symVaddr_ = symVaddr;

    // pase sym_table to Elf32_Sym or Elf64_Sym array decided by sym_entry_size.
    auto symEntLen = symbolTable->sym_entry_size();
    auto startValueToSymAddrMap = symbolTablePtrAndStValueToSymAddr_.Find(symbolTable);
    if (!startValueToSymAddrMap) {
        // find matching SymbolTable failed, but symVaddr is availiable
        ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
        // find symbolTable failed!!!
        TS_LOGD("find symbolTalbe failed!!!");
        return frameInfo;
    }
    // Traverse array, st_value <= symVaddr and symVaddr <= st_value + st_size.  then you can get st_name
    auto end = startValueToSymAddrMap->upper_bound(symVaddr);
    length = std::distance(startValueToSymAddrMap->begin(), end);
    uint32_t symbolStart = INVALID_UINT32;
    if (length > 0) {
        end--;
        if (symEntLen == ELF32_SYM) {
            UpdateFrameInfo(reinterpret_cast<const Elf32_Sym*>(end->second), symbolStart, symVaddr, ip,
                            frameInfo.get());
        } else {
            UpdateFrameInfo(reinterpret_cast<const Elf64_Sym*>(end->second), symbolStart, symVaddr, ip,
                            frameInfo.get());
        }
    }

    if (symbolStart == INVALID_UINT32 || symbolStart >= symbolTable->str_table().Size()) {
        // find symbolStart failed, but some data is availiable.
        frameInfo->offset_ = ip;
        frameInfo->symbolOffset_ = 0;
        ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
        TS_LOGD("symbolStart is %lu invaliable!!!", symbolStart);
        return frameInfo;
    }

    auto mangle = reinterpret_cast<const char*>(symbolTable->str_table().Data() + symbolStart);
    auto demangle = GetDemangleSymbolIndex(mangle);
    frameInfo->symbolIndex_ = traceDataCache_->GetDataIndex(demangle);
    ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
    return frameInfo;
}

void OfflineSymbolizationFilter::OfflineSymbolization(const std::set<uint64_t>& ips)
{
    for (auto ip : ips) {
        // start symbolization
        std::shared_ptr<FrameInfo> frameInfo = std::make_shared<FrameInfo>();
        frameInfo->ip_ = ip;
        uint64_t vmStart = INVALID_UINT64;
        uint64_t vmOffset = INVALID_UINT64;
        auto endItor = startAddrToMapsInfoMap_.upper_bound(ip);
        auto length = std::distance(startAddrToMapsInfoMap_.begin(), endItor);
        if (length > 0) {
            endItor--;
            // Follow the rules of front closing and rear opening, [start, end)
            if (ip < endItor->second->end()) {
                vmStart = endItor->second->start();
                vmOffset = endItor->second->offset();
                frameInfo->filePathId_ = endItor->second->file_path_id();
            }
        }
        if (frameInfo->filePathId_ == INVALID_UINT32) {
            // find matching MapsInfo failed!!!
            TS_LOGD("find matching Maps Info failed, ip = %lu", ip);
            continue;
        }
        if (!filePathIdToImportSymbolTableMap_.count(frameInfo->filePathId_)) {
            TS_LOGD("can not find matching symbol table!");
            continue;
        }
        auto& symbolTable = filePathIdToImportSymbolTableMap_.at(frameInfo->filePathId_);
        // Calculate virtual address
        uint64_t symVaddr = ip - vmStart + vmOffset + symbolTable->textVaddr - symbolTable->textOffset;
        // pase sym_table to Elf32_Sym or Elf64_Sym array decided by sym_entry_size.
        auto symEntLen = symbolTable->symEntSize;
        auto startValueToSymAddrMap = filePathIdAndStValueToSymAddr_.Find(frameInfo->filePathId_);
        if (!startValueToSymAddrMap) {
            // find matching SymbolTable failed, but symVaddr is availiable
            ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
            // find symbolTable failed!!!
            TS_LOGD("find symbolTalbe failed!!!");
            continue;
        }
        // Traverse array, st_value <= symVaddr and symVaddr <= st_value + st_size.  then you can get st_name
        auto end = startValueToSymAddrMap->upper_bound(symVaddr);
        length = std::distance(startValueToSymAddrMap->begin(), end);
        uint32_t symbolStart = INVALID_UINT32;
        if (length > 0) {
            end--;
            if (symEntLen == ELF32_SYM) {
                UpdateFrameInfo(reinterpret_cast<const Elf32_Sym*>(end->second), symbolStart, symVaddr, ip,
                                frameInfo.get());
            } else {
                UpdateFrameInfo(reinterpret_cast<const Elf64_Sym*>(end->second), symbolStart, symVaddr, ip,
                                frameInfo.get());
            }
        }
        if (symbolStart == INVALID_UINT32 || symbolStart >= symbolTable->strTable.size()) {
            // find symbolStart failed, but some data is availiable.
            frameInfo->offset_ = ip;
            frameInfo->symbolOffset_ = 0;
            ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
            TS_LOGD("symbolStart is : %u invaliable!!!", symbolStart);
            continue;
        }
        auto mangle = symbolTable->strTable.c_str() + symbolStart;
        auto demangle = GetDemangleSymbolIndex(mangle);
        frameInfo->symbolIndex_ = traceDataCache_->GetDataIndex(demangle);
        ipToFrameInfo_.insert(std::make_pair(ip, frameInfo));
    }
}
} // namespace TraceStreamer
} // namespace SysTuning
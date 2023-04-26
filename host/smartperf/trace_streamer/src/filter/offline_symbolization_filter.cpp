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
      ipidAndStartAddrToMapsInfoMap_(nullptr),
      symbolTablePtrAndStValueToSymAddr_(nullptr),
      ipidAndIpToFrameInfo_(nullptr)
{
}
OfflineSymbolizationFilter::~OfflineSymbolizationFilter()
{
    ipidAndStartAddrToMapsInfoMap_.Clear();
    symbolTablePtrAndStValueToSymAddr_.Clear();
    filePathIdToSymbolTableMap_.clear();
    ipidAndIpToFrameInfo_.Clear();
}
void OfflineSymbolizationFilter::ParseMaps(std::unique_ptr<NativeHookMetaData>& nativeHookMetaData)
{
    segs_.emplace_back(nativeHookMetaData->seg_);
    auto reader = std::make_shared<ProtoReader::MapsInfo_Reader>(nativeHookMetaData->reader_->maps_info());
    auto ipid = streamFilters_->processFilter_->GetOrCreateInternalPid(0, reader->pid());
    // The temporary variable startTime here is to solve the problem of parsing errors under the window platform
    uint64_t startTime = reader->start();
    ipidAndStartAddrToMapsInfoMap_.Insert(ipid, startTime, std::move(reader));
}
void OfflineSymbolizationFilter::ParseSymbolTables(std::unique_ptr<NativeHookMetaData>& nativeHookMetaData)
{
    segs_.emplace_back(nativeHookMetaData->seg_);
    auto reader = std::make_shared<ProtoReader::SymbolTable_Reader>(nativeHookMetaData->reader_->symbol_tab());
    filePathIdToSymbolTableMap_.insert(std::make_pair(reader->file_path_id(), reader));
    auto symEntrySize = reader->sym_entry_size();
    auto symTable = reader->sym_table();
    auto size = symTable.Size() / symEntrySize;
    if (symEntrySize == ELF32_SYM) {
        auto firstSymbolAddr = reinterpret_cast<const Elf32_Sym*>(symTable.Data());
        for (auto i = 0; i < size; i++) {
            auto symAddr = firstSymbolAddr + i;
            if ((symAddr->st_info & STT_FUNC) && symAddr->st_value) {
                symbolTablePtrAndStValueToSymAddr_.Insert(reader, symAddr->st_value,
                                                          reinterpret_cast<const uint8_t*>(symAddr));
            }
        }
    } else {
        auto firstSymbolAddr = reinterpret_cast<const Elf64_Sym*>(symTable.Data());
        for (auto i = 0; i < size; i++) {
            auto symAddr = firstSymbolAddr + i;
            if ((symAddr->st_info & STT_FUNC) && symAddr->st_value) {
                symbolTablePtrAndStValueToSymAddr_.Insert(reader, symAddr->st_value,
                                                          reinterpret_cast<const uint8_t*>(symAddr));
            }
        }
    }
}
std::shared_ptr<std::vector<std::shared_ptr<FrameInfo>>>
    OfflineSymbolizationFilter::Parse(uint32_t pid, const std::vector<uint64_t>& ips)
{
    auto result = std::make_shared<std::vector<std::shared_ptr<FrameInfo>>>();
    for (auto itor = ips.begin(); itor != ips.end(); itor++) {
        auto frameInfo = Parse(pid, *itor);
        // If the IP in the middle of the call stack cannot be symbolized, the remaining IP is discarded
        if (!frameInfo) {
            break;
        }
        result->emplace_back(frameInfo);
    }
    return result;
}
std::shared_ptr<FrameInfo> OfflineSymbolizationFilter::Parse(uint32_t pid, uint64_t ip)
{
    auto ipid = streamFilters_->processFilter_->GetOrCreateInternalPid(0, pid);
    auto result = ipidAndIpToFrameInfo_.Find(ipid, ip);
    if (result) {
        return result;
    }
    // start symbolization
    std::shared_ptr<FrameInfo> frameInfo = std::make_shared<FrameInfo>();
    frameInfo->ip_ = ip;
    auto startAddrToMapsInfoMap = ipidAndStartAddrToMapsInfoMap_.Find(ipid);
    if (!startAddrToMapsInfoMap) {
        // can not find pid
        TS_LOGD("find matching ipid failed, pid = %u, ip = %lu", pid, ip);
        return nullptr;
    }
    uint64_t vmStart = INVALID_UINT64;
    uint64_t vmOffset = INVALID_UINT64;
    auto endItor = startAddrToMapsInfoMap->upper_bound(ip);
    auto length = std::distance(startAddrToMapsInfoMap->begin(), endItor);
    if (length > 0) {
        endItor--;
        if (ip <= endItor->second->end()) {
            vmStart = endItor->second->start();
            vmOffset = endItor->second->offset();
            frameInfo->filePathId_ = endItor->second->file_path_id();
        }
    }
    if (frameInfo->filePathId_ == INVALID_UINT32) {
        // find matching MapsInfo failed!!!
        TS_LOGD("find matching Maps Info failed, pid = %u, ip = %lu", pid, ip);
        return nullptr;
    }
    // find SymbolTable by filePathId
    auto itor = filePathIdToSymbolTableMap_.find(frameInfo->filePathId_);
    if (itor == filePathIdToSymbolTableMap_.end()) {
        // find matching SymbolTable failed, but filePathId is availiable
        ipidAndIpToFrameInfo_.Insert(ipid, ip, frameInfo);
        TS_LOGD("find matching filePathId failed, pid = %u, ip = %lu, filePathId = %u", pid, ip,
                frameInfo->filePathId_);
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
            auto elf32Sym = reinterpret_cast<const Elf32_Sym*>(end->second);
            if (end->first + elf32Sym->st_size >= symVaddr) {
                symbolStart = elf32Sym->st_name;
                frameInfo->offset_ = elf32Sym->st_value != 0 ? elf32Sym->st_value : ip;
                frameInfo->symbolOffset_ = symVaddr - elf32Sym->st_value;
            }
        } else {
            auto elf64Sym = reinterpret_cast<const Elf64_Sym*>(end->second);
            if (elf64Sym->st_value + elf64Sym->st_size >= symVaddr) {
                symbolStart = elf64Sym->st_name;
                frameInfo->offset_ = elf64Sym->st_value != 0 ? elf64Sym->st_value : ip;
                frameInfo->symbolOffset_ = symVaddr - elf64Sym->st_value;
            }
        }
    }

    if (symbolStart == INVALID_UINT32 || symbolStart >= symbolTable->str_table().Size()) {
        // find symbolStart failed, but some data is availiable.
        frameInfo->offset_ = ip;
        frameInfo->symbolOffset_ = 0;
        ipidAndIpToFrameInfo_.Insert(ipid, ip, frameInfo);
        TS_LOGD("symbolStart is invaliable!!!");
        return frameInfo;
    }

    auto originSymbolName = reinterpret_cast<const char*>(symbolTable->str_table().Data() + symbolStart);
    int status = 0;
    auto symbolName = abi::__cxa_demangle(originSymbolName, nullptr, nullptr, &status);
    if (status) { // status != 0 failed
        frameInfo->symbolIndex_ = traceDataCache_->GetDataIndex(originSymbolName);
    } else {
        frameInfo->symbolIndex_ = traceDataCache_->GetDataIndex(symbolName);
    }
    ipidAndIpToFrameInfo_.Insert(ipid, ip, frameInfo);
    return frameInfo;
}
} // namespace TraceStreamer
} // namespace SysTuning

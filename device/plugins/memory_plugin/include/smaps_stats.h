/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef SMAPS_STATS_H
#define SMAPS_STATS_H

#include "logging.h"
#include <cinttypes>
#include <cstdio>
#include <fstream>
#include <inttypes.h>
#include <iostream>
#include <memory>
#include <string>
#include <sys/mman.h>

struct stats_t {
    int pss;
    int swappablePss;
    int rss;
    int privateDirty;
    int sharedDirty;
    int privateClean;
    int sharedClean;
    int swappedOut;
    int swappedOutPss;
};

enum NumType {
    FIFTH_FIELD = 5,
    HEX_BASE = 16,
    DEC_BASE = 10,
};

struct MapPiecesInfo_t {
    uint64_t start_addr;
    uint64_t end_addr;

    std::string name;
};

struct MemUsageInfo_t {
    uint64_t vss;
    uint64_t rss;
    uint64_t pss;
    uint64_t uss;

    uint64_t swap;
    uint64_t swap_pss;

    uint64_t private_clean;
    uint64_t private_dirty;
    uint64_t shared_clean;
    uint64_t shared_dirty;
};

enum vmemifoType {
    VMHEAP_NULL = -2,
    VMHEAP_NEEDFIX = -1,
    VMHEAP_UNKNOWN,
    VMHEAP_DALVIK,
    VMHEAP_NATIVE,

    VMHEAP_DALVIK_OTHER,
    VMHEAP_STACK,
    VMHEAP_CURSOR,
    VMHEAP_ASHMEM,
    VMHEAP_GL_DEV,
    VMHEAP_UNKNOWN_DEV,
    VMHEAP_SO,
    VMHEAP_JAR,
    VMHEAP_TTF,
    VMHEAP_DEX,
    VMHEAP_OAT,
    VMHEAP_ART,
    VMHEAP_UNKNOWN_MAP,
    VMHEAP_GRAPHICS,
    VMHEAP_GL,
    VMHEAP_OTHER_MEMTRACK,

    // Dalvik extra sections (heap).
    VMHEAP_DALVIK_NORMAL,
    VMHEAP_DALVIK_LARGE,
    VMHEAP_DALVIK_ZYGOTE,
    VMHEAP_DALVIK_NON_MOVING,

    // Dalvik other extra sections.
    VMHEAP_DALVIK_OTHER_LINEARALLOC,
    VMHEAP_DALVIK_OTHER_ACCOUNTING,
    VMHEAP_DALVIK_OTHER_ZYGOTE_CODE_CACHE,
    VMHEAP_DALVIK_OTHER_APP_CODE_CACHE,
    VMHEAP_DALVIK_OTHER_COMPILER_METADATA,
    VMHEAP_DALVIK_OTHER_INDIRECT_REFERENCE_TABLE,

    // Boot vdex / app dex / app vdex
    VMHEAP_DEX_BOOT_VDEX,
    VMHEAP_DEX_APP_DEX,
    VMHEAP_DEX_APP_VDEX,

    // App art, boot art.
    VMHEAP_ART_APP,
    VMHEAP_ART_BOOT,

    _NUM_HEAP,
    _NUM_EXCLUSIVE_HEAP = VMHEAP_OTHER_MEMTRACK + 1,
    _NUM_CORE_HEAP = VMHEAP_NATIVE + 1
};

enum OpsType {
    OPS_START = 1,
    OPS_END,
};

struct vMeminfoAreaMapping {
    int ops;
    const char* heapstr;
    int heapid[2];
};

constexpr vMeminfoAreaMapping vmaMemheap[] = {
    {OpsType::OPS_START, "[heap]", {vmemifoType::VMHEAP_NATIVE, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START, "[stack", {vmemifoType::VMHEAP_STACK, vmemifoType::VMHEAP_NULL}},
};

// [anon:
constexpr vMeminfoAreaMapping vmaMemanon[] = {
    {OpsType::OPS_START, "[anon:libc_malloc]", {vmemifoType::VMHEAP_NATIVE, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START, "[anon:scudo:", {vmemifoType::VMHEAP_NATIVE, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START, "[anon:GWP-ASan", {vmemifoType::VMHEAP_NATIVE, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START, "[anon:stack_and_tls:", {vmemifoType::VMHEAP_STACK, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START,
     "[anon:dalvik-LinearAlloc",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_LINEARALLOC}},
    {OpsType::OPS_START, "[anon:dalvik-alloc space", {vmemifoType::VMHEAP_DALVIK, vmemifoType::VMHEAP_DALVIK_NORMAL}},
    {OpsType::OPS_START, "[anon:dalvik-main space", {vmemifoType::VMHEAP_DALVIK, vmemifoType::VMHEAP_DALVIK_NORMAL}},
    {OpsType::OPS_START,
     "[anon:dalvik-large object space",
     {vmemifoType::VMHEAP_DALVIK, vmemifoType::VMHEAP_DALVIK_LARGE}},
    {OpsType::OPS_START,
     "[anon:dalvik-free list large object space",
     {vmemifoType::VMHEAP_DALVIK, vmemifoType::VMHEAP_DALVIK_LARGE}},
    {OpsType::OPS_START,
     "[anon:dalvik-non moving space",
     {vmemifoType::VMHEAP_DALVIK, vmemifoType::VMHEAP_DALVIK_NON_MOVING}},
    {OpsType::OPS_START, "[anon:dalvik-zygote space", {vmemifoType::VMHEAP_DALVIK, vmemifoType::VMHEAP_DALVIK_ZYGOTE}},
    {OpsType::OPS_START,
     "[anon:dalvik-indirect ref",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_INDIRECT_REFERENCE_TABLE}},
    {OpsType::OPS_START,
     "[anon:dalvik-jit-code-cache",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_APP_CODE_CACHE}},
    {OpsType::OPS_START,
     "[anon:dalvik-data-code-cache",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_APP_CODE_CACHE}},
    {OpsType::OPS_START,
     "[anon:dalvik-CompilerMetadata",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_COMPILER_METADATA}},
    {OpsType::OPS_START,
     "[anon:dalvik-",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_ACCOUNTING}},
    {OpsType::OPS_START, "[anon:", {vmemifoType::VMHEAP_UNKNOWN, vmemifoType::VMHEAP_NULL}},
};

constexpr vMeminfoAreaMapping vmaMemfd[] = {
    {OpsType::OPS_START,
     "/memfd:jit-cache",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_APP_CODE_CACHE}},
    {OpsType::OPS_START,
     "/memfd:jit-zygote-cache",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_ZYGOTE_CODE_CACHE}},
};
// dev
constexpr vMeminfoAreaMapping vmaMemdev[] = {
    {OpsType::OPS_START, "/dev/kgsl-3d0", {vmemifoType::VMHEAP_GL_DEV, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START, "/dev/ashmem/CursorWindow", {vmemifoType::VMHEAP_CURSOR, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START,
     "/dev/ashmem/jit-zygote-cache",
     {vmemifoType::VMHEAP_DALVIK_OTHER, vmemifoType::VMHEAP_DALVIK_OTHER_ZYGOTE_CODE_CACHE}},
    {OpsType::OPS_START, "/dev/ashmem", {vmemifoType::VMHEAP_ASHMEM, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_START, "/dev/", {vmemifoType::VMHEAP_UNKNOWN_DEV, vmemifoType::VMHEAP_NULL}},
};

constexpr vMeminfoAreaMapping vmaMemsuffix[] = {
    {OpsType::OPS_END, ".so", {vmemifoType::VMHEAP_SO, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_END, ".jar", {vmemifoType::VMHEAP_JAR, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_END, ".ttf", {vmemifoType::VMHEAP_TTF, vmemifoType::VMHEAP_NULL}},
    {OpsType::OPS_END, ".oat", {vmemifoType::VMHEAP_OAT, vmemifoType::VMHEAP_NULL}},

    {OpsType::OPS_END, ".odex", {vmemifoType::VMHEAP_DEX, vmemifoType::VMHEAP_DEX_APP_DEX}},

    {OpsType::OPS_END, ".vdex", {vmemifoType::VMHEAP_DEX, vmemifoType::VMHEAP_NEEDFIX}},
    {OpsType::OPS_END, ".art", {vmemifoType::VMHEAP_ART, vmemifoType::VMHEAP_NEEDFIX}},
    {OpsType::OPS_END, ".art]", {vmemifoType::VMHEAP_ART, vmemifoType::VMHEAP_NEEDFIX}},
};

class SmapsStats {
public:
    SmapsStats() {}
    SmapsStats(const std::string path) : testpath_(path){};
    ~SmapsStats() {}
    bool ParseMaps(int pid);
    int GetProcessJavaHeap();
    int GetProcessNativeHeap();
    int GetProcessCode();
    int GetProcessStack();
    int GetProcessGraphics();
    int GetProcessPrivateOther();
    int GetProcessSystem();

private:
    stats_t stats_[_NUM_HEAP] = {{0}};
    bool lastline_ = false;
    std::string testpath_;

    int GetTotalPrivateClean();
    int GetTotalPrivateDirty();
    int GetPrivate(int type);
    int GetTotalPss();
    int GetTotalSwappedOutPss();
    void ReviseStatsData();

    bool ReadVmemareasFile(const std::string& path);
    bool ParseMapHead(std::string& line, MapPiecesInfo_t& head);
    bool SetMapAddrInfo(std::string& line, MapPiecesInfo_t& head);
    bool GetMemUsageField(std::string& line, MemUsageInfo_t& memusage);
    void CollectVmemAreasData(const MapPiecesInfo_t& mempic,
                              const MemUsageInfo_t& memusage,
                              uint64_t& prevEnd,
                              int& prevHeap);
    bool GetVmaIndex(std::string name, uint32_t namesz, int32_t heapIndex[2], bool& swappable);
    uint64_t GetSwapablepssValue(const MemUsageInfo_t& memusage, bool swappable);
    void SetVmemAreasData(int index, uint64_t swapablePss, const MemUsageInfo_t& usage);
    void HeapIndexFix(std::string name, const char* key, int32_t heapIndex[2]);
    bool GetVMAStuId(int ops,
                     std::string name,
                     const vMeminfoAreaMapping* vma,
                     int count,
                     int32_t heapIndex[2],
                     bool& swappable);
};

#endif
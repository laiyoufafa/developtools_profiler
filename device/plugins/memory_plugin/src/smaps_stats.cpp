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
#include "smaps_stats.h"

#include "securec.h"

namespace {
bool MatchHead(const std::string& name, const char* str)
{
    return strncmp(name.c_str(), str, strlen(str)) == 0;
}

bool MatchTail(const std::string& name, std::string str)
{
    int index = name.size() - str.size();
    if (index < 0) {
        return false;
    }
    return (name.substr(index) == str);
}
} // namespace
bool SmapsStats::ParseMaps(int pid)
{
    std::string smaps_path = std::string("/proc/") + std::to_string(pid) + std::string("/smaps");
    if (testpath_.size() > 0) {
        smaps_path = testpath_ + std::to_string(pid) + std::string("/smaps");
    }
    HILOG_INFO(LOG_CORE, "smaps path:%s", smaps_path.c_str());
    ReadVmemareasFile(smaps_path);
    ReviseStatsData();
    return true;
}

bool SmapsStats::ReadVmemareasFile(const std::string& path)
{
    bool findMapHead = false;
    MapPiecesInfo_t mappic = {0};
    MemUsageInfo_t memusage = {0};
    uint64_t prevEnd = 0;
    int prevHeap = 0;
    std::ifstream input(path, std::ios::in);
    if (input.fail()) {
        HILOG_ERROR(LOG_CORE, "open %s failed, errno = %d", path.c_str(), errno);
        return false;
    }
    do {
        if (!input.good()) {
            return false;
        }
        std::string line;
        getline(input, line);
        line += '\n';
        if (!findMapHead) {
            // 00400000-00409000 r-xp 00000000 fc:00 426998  /usr/lib/gvfs/gvfsd-http
            ParseMapHead(line, mappic);
            findMapHead = true;
            continue;
        }
        if (findMapHead && GetMemUsageField(line, memusage)) {
            if (!lastline_) {
                continue;
            }
        }
        CollectVmemAreasData(mappic, memusage, prevEnd, prevHeap);
        findMapHead = false;
        lastline_ = false;
    } while (!input.eof());
    input.close();

    return true;
}

bool SmapsStats::GetVMAStuId(int ops,
                             std::string name,
                             const vMeminfoAreaMapping* vma,
                             int count,
                             int32_t heapIndex[2],
                             bool& swappable)
{
    for (int i = 0; i < count; i++) {
        if (ops == OPS_START) {
            if (MatchHead(name, vma[i].heapstr)) {
                heapIndex[0] = vma[i].heapid[0];
                heapIndex[1] = vma[i].heapid[1];
                swappable = false;
                return true;
            }
        } else if (ops == OPS_END) {
            if (MatchTail(name, vma[i].heapstr)) {
                if (vma[i].heapid[1] == VMHEAP_NEEDFIX) {
                    HeapIndexFix(name, vma[i].heapstr, heapIndex);
                } else {
                    heapIndex[0] = vma[i].heapid[0];
                    heapIndex[1] = vma[i].heapid[1];
                }
                swappable = true;
                return true;
            }
        }
    }
    return false;
}

bool SmapsStats::GetVmaIndex(std::string name, uint32_t namesz, int32_t heapIndex[2], bool& swappable)
{
    switch (name[0]) {
        case '[':
            if (MatchHead(name, "[heap]") || MatchHead(name, "[stack")) {
                int count = sizeof(vmaMemheap) / sizeof(vmaMemheap[0]);
                return GetVMAStuId(OPS_START, name, vmaMemheap, count, heapIndex, swappable);
            } else if (MatchHead(name, "[anon:")) {
                if (MatchHead(name, "[anon:dalvik-")) {
                    int count = sizeof(vmaMemsuffix) / sizeof(vmaMemsuffix[0]);
                    if (GetVMAStuId(OPS_END, name, vmaMemsuffix, count, heapIndex, swappable)) {
                        return true;
                    }
                }
                int count = sizeof(vmaMemanon) / sizeof(vmaMemanon[0]);
                return GetVMAStuId(OPS_START, name, vmaMemanon, count, heapIndex, swappable);
            }
            break;
        case '/':
            if (MatchHead(name, "/memfd:")) {
                int count = sizeof(vmaMemfd) / sizeof(vmaMemfd[0]);
                return GetVMAStuId(OPS_START, name, vmaMemfd, count, heapIndex, swappable);
            } else if (MatchHead(name, "/dev/")) {
                int count = sizeof(vmaMemdev) / sizeof(vmaMemdev[0]);
                return GetVMAStuId(OPS_START, name, vmaMemdev, count, heapIndex, swappable);
            } else {
                int count = sizeof(vmaMemsuffix) / sizeof(vmaMemsuffix[0]);
                return GetVMAStuId(OPS_END, name, vmaMemsuffix, count, heapIndex, swappable);
            }
            break;
        default:
            int count = sizeof(vmaMemsuffix) / sizeof(vmaMemsuffix[0]);
            return GetVMAStuId(OPS_END, name, vmaMemsuffix, count, heapIndex, swappable);
            break;
    }
    if (namesz > strlen(".dex") && strstr(name.c_str(), ".dex") != nullptr) {
        heapIndex[0] = VMHEAP_DEX;
        heapIndex[1] = VMHEAP_DEX_APP_DEX;
        swappable = true;
        return true;
    }
    return false;
}

void SmapsStats::CollectVmemAreasData(const MapPiecesInfo_t& mempic,
                                      const MemUsageInfo_t& memusage,
                                      uint64_t& prevEnd,
                                      int& prevHeap)
{
    std::string name;
    int32_t heapIndex[2] = {VMHEAP_UNKNOWN, VMHEAP_NULL};
    bool swappable = false;
    uint64_t swapablePss = 0;

    if (MatchTail(mempic.name, " (deleted)")) {
        name = mempic.name.substr(0, mempic.name.size() - strlen(" (deleted)"));
    } else {
        name = mempic.name;
    }
    uint32_t namesz = name.size();
    if (!GetVmaIndex(name, namesz, heapIndex, swappable)) {
        if (namesz > 0) {
            heapIndex[0] = VMHEAP_UNKNOWN_MAP;
        } else if (mempic.start_addr == prevEnd && prevHeap == VMHEAP_SO) {
            // bss section of a shared library
            heapIndex[0] = VMHEAP_SO;
        }
    }
    prevEnd = mempic.end_addr;
    prevHeap = heapIndex[0];
    swapablePss = GetSwapablepssValue(memusage, swappable);
    SetVmemAreasData(heapIndex[0], swapablePss, memusage);
    if ((heapIndex[1] != VMHEAP_NULL) && (heapIndex[1] != VMHEAP_NEEDFIX)) {
        SetVmemAreasData(heapIndex[1], swapablePss, memusage);
    }
}

void SmapsStats::ReviseStatsData()
{
    // Summary data to VMHEAP_UNKNOWN
    for (int i = _NUM_CORE_HEAP; i < _NUM_EXCLUSIVE_HEAP; i++) {
        stats_[VMHEAP_UNKNOWN].pss += stats_[i].pss;
        stats_[VMHEAP_UNKNOWN].swappablePss += stats_[i].swappablePss;
        stats_[VMHEAP_UNKNOWN].rss += stats_[i].rss;
        stats_[VMHEAP_UNKNOWN].privateDirty += stats_[i].privateDirty;
        stats_[VMHEAP_UNKNOWN].sharedDirty += stats_[i].sharedDirty;
        stats_[VMHEAP_UNKNOWN].privateClean += stats_[i].privateClean;
        stats_[VMHEAP_UNKNOWN].sharedClean += stats_[i].sharedClean;
        stats_[VMHEAP_UNKNOWN].swappedOut += stats_[i].swappedOut;
        stats_[VMHEAP_UNKNOWN].swappedOutPss += stats_[i].swappedOutPss;
    }
}

bool SmapsStats::SetMapAddrInfo(std::string& line, MapPiecesInfo_t& head)
{
    const char* pStr = line.c_str();
    char* end = nullptr;
    // start_addr
    head.start_addr = strtoull(pStr, &end, HEX_BASE);
    if (end == pStr || *end != '-') {
        return false;
    }
    pStr = end + 1;
    // end_addr
    head.end_addr = strtoull(pStr, &end, HEX_BASE);
    if (end == pStr) {
        return false;
    }
    return true;
}

bool SmapsStats::ParseMapHead(std::string& line, MapPiecesInfo_t& head)
{
    if (!SetMapAddrInfo(line, head)) {
        return false;
    }
    size_t newlineops = 0;
    size_t wordsz = 0;
    std::string newline = line;
    for (int i = 0; i < FIFTH_FIELD; i++) {
        std::string word = newline;
        wordsz = word.find(" ");
        if (wordsz == std::string::npos) {
            return false;
        }
        word = newline.substr(0, wordsz);

        newlineops = newline.find_first_not_of(" ", wordsz);
        newline = newline.substr(newlineops);
    }
    head.name = newline.substr(0, newline.size() - 1);
    return true;
}

bool SmapsStats::GetMemUsageField(std::string& line, MemUsageInfo_t& memusage)
{
    char field[64];
    int len;
    const char* pLine = line.c_str();

    int ret = sscanf_s(pLine, "%63s %n", field, sizeof(field), &len);
    if (ret == 1 && *field && field[strlen(field) - 1] == ':') {
        const char* c = pLine + len;
        std::string strfield(field);
        switch (field[0]) {
            case 'P':
                if (MatchHead(strfield, "Pss:")) {
                    memusage.pss = strtoull(c, nullptr, DEC_BASE);
                } else if (MatchHead(strfield, "Private_Clean:")) {
                    uint64_t prcl = strtoull(c, nullptr, DEC_BASE);
                    memusage.private_clean = prcl;
                    memusage.uss += prcl;
                } else if (MatchHead(strfield, "Private_Dirty:")) {
                    uint64_t prdi = strtoull(c, nullptr, DEC_BASE);
                    memusage.private_dirty = prdi;
                    memusage.uss += prdi;
                }
                break;
            case 'S':
                if (MatchHead(strfield, "Size:")) {
                    memusage.vss = strtoull(c, nullptr, DEC_BASE);
                } else if (MatchHead(strfield, "Shared_Clean:")) {
                    memusage.shared_clean = strtoull(c, nullptr, DEC_BASE);
                } else if (MatchHead(strfield, "Shared_Dirty:")) {
                    memusage.shared_dirty = strtoull(c, nullptr, DEC_BASE);
                } else if (MatchHead(strfield, "Swap:")) {
                    memusage.swap = strtoull(c, nullptr, DEC_BASE);
                } else if (MatchHead(strfield, "SwapPss:")) {
                    memusage.swap_pss = strtoull(c, nullptr, DEC_BASE);
                }
                break;
            case 'R':
                if (MatchHead(strfield, "Rss:")) {
                    memusage.rss = strtoull(c, nullptr, DEC_BASE);
                }
                break;
            case 'V':
                if (MatchHead(strfield, "VmFlags:")) {
                    lastline_ = true;
                }
                break;
            default:
                break;
        }
        return true;
    }

    return false;
}

uint64_t SmapsStats::GetSwapablepssValue(const MemUsageInfo_t& memusage, bool swappable)
{
    const MemUsageInfo_t& usage = memusage;
    uint64_t swapablePss = 0;

    if (swappable && (usage.pss > 0)) {
        float sharing_proportion = 0.0f;
        if ((usage.shared_clean > 0) || (usage.shared_dirty > 0)) {
            sharing_proportion = (usage.pss - usage.uss) / (usage.shared_clean + usage.shared_dirty);
        }
        swapablePss = (sharing_proportion * usage.shared_clean) + usage.private_clean;
    }
    return swapablePss;
}

void SmapsStats::SetVmemAreasData(int index, uint64_t swapablePss, const MemUsageInfo_t& usage)
{
    stats_[index].pss += usage.pss;
    stats_[index].swappablePss += swapablePss;
    stats_[index].rss += usage.rss;
    stats_[index].privateDirty += usage.private_dirty;
    stats_[index].sharedDirty += usage.shared_dirty;
    stats_[index].privateClean += usage.private_clean;
    stats_[index].sharedClean += usage.shared_clean;
    stats_[index].swappedOut += usage.swap;
    stats_[index].swappedOutPss += usage.swap_pss;
}

void SmapsStats::HeapIndexFix(std::string name, const char* key, int32_t heapIndex[2])
{
    if (!strncmp(key, ".vdex", sizeof(".vdex"))) {
        if ((strstr(name.c_str(), "@boot") != nullptr) || (strstr(name.c_str(), "/boot") != nullptr) ||
            (strstr(name.c_str(), "/apex") != nullptr)) {
            heapIndex[0] = VMHEAP_DEX;
            heapIndex[1] = VMHEAP_DEX_BOOT_VDEX;
        } else {
            heapIndex[0] = VMHEAP_DEX;
            heapIndex[1] = VMHEAP_DEX_APP_VDEX;
        }
    } else if (!strncmp(key, ".art", sizeof(".art")) || !strncmp(key, ".art]", sizeof(".art]"))) {
        if ((strstr(name.c_str(), "@boot") != nullptr) || (strstr(name.c_str(), "/boot") != nullptr) ||
            (strstr(name.c_str(), "/apex") != nullptr)) {
            heapIndex[0] = VMHEAP_ART;
            heapIndex[1] = VMHEAP_ART_BOOT;
        } else {
            heapIndex[0] = VMHEAP_ART;
            heapIndex[1] = VMHEAP_ART_APP;
        }
    }
}

int SmapsStats::GetProcessJavaHeap()
{
    return stats_[VMHEAP_DALVIK].privateDirty + GetPrivate(VMHEAP_ART);
}

int SmapsStats::GetProcessNativeHeap()
{
    return stats_[VMHEAP_NATIVE].privateDirty;
}

int SmapsStats::GetProcessCode()
{
    return GetPrivate(VMHEAP_SO) + GetPrivate(VMHEAP_JAR) + GetPrivate(VMHEAP_TTF) +
           GetPrivate(VMHEAP_DEX) + GetPrivate(VMHEAP_OAT) +
           GetPrivate(VMHEAP_DALVIK_OTHER_ZYGOTE_CODE_CACHE) +
           GetPrivate(VMHEAP_DALVIK_OTHER_APP_CODE_CACHE);
}

int SmapsStats::GetProcessStack()
{
    return stats_[VMHEAP_STACK].privateDirty;
}

int SmapsStats::GetProcessGraphics()
{
    return GetPrivate(VMHEAP_GL_DEV) + GetPrivate(VMHEAP_GRAPHICS) + GetPrivate(VMHEAP_GL);
}

int SmapsStats::GetProcessPrivateOther()
{
    return GetTotalPrivateClean() + GetTotalPrivateDirty() - GetProcessJavaHeap() - GetProcessNativeHeap() -
           GetProcessCode() - GetProcessStack() - GetProcessGraphics();
}

int SmapsStats::GetProcessSystem()
{
    return GetTotalPss() - GetTotalPrivateClean() - GetTotalPrivateDirty();
}

int SmapsStats::GetTotalPrivateClean()
{
    return stats_[VMHEAP_UNKNOWN].privateClean + stats_[VMHEAP_NATIVE].privateClean +
           stats_[VMHEAP_DALVIK].privateClean;
}

int SmapsStats::GetTotalPrivateDirty()
{
    return stats_[VMHEAP_UNKNOWN].privateDirty + stats_[VMHEAP_NATIVE].privateDirty +
           stats_[VMHEAP_DALVIK].privateDirty;
}

int SmapsStats::GetPrivate(int type)
{
    return stats_[type].privateDirty + stats_[type].privateClean;
}

int SmapsStats::GetTotalPss()
{
    return stats_[VMHEAP_UNKNOWN].pss + stats_[VMHEAP_NATIVE].pss + stats_[VMHEAP_DALVIK].pss + GetTotalSwappedOutPss();
}

int SmapsStats::GetTotalSwappedOutPss()
{
    return stats_[VMHEAP_UNKNOWN].swappedOutPss + stats_[VMHEAP_NATIVE].swappedOutPss +
           stats_[VMHEAP_DALVIK].swappedOutPss;
}

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

#ifndef MEMORY_DATA_PLUGIN_H
#define MEMORY_DATA_PLUGIN_H

#include <algorithm>
#include <dirent.h>
#include <fcntl.h>
#include <inttypes.h>
#include <iomanip>
#include <string>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <unordered_map>
#include <utility>

#include "logging.h"
#include "memory_plugin_config.pb.h"
#include "memory_plugin_result.pb.h"
#include "smaps_stats.h"

struct Proto2StrMapping {
    int protobufid;
    const char* procstr;
};

constexpr Proto2StrMapping meminfoMapping[] = {
    {SysMeminfoType::MEMINFO_UNSPECIFIED, "MemUnspecified"},
    {SysMeminfoType::MEMINFO_MEM_TOTAL, "MemTotal"},
    {SysMeminfoType::MEMINFO_MEM_FREE, "MemFree"},
    {SysMeminfoType::MEMINFO_MEM_AVAILABLE, "MemAvailable"},
    {SysMeminfoType::MEMINFO_BUFFERS, "Buffers"},
    {SysMeminfoType::MEMINFO_CACHED, "Cached"},
    {SysMeminfoType::MEMINFO_SWAP_CACHED, "SwapCached"},
    {SysMeminfoType::MEMINFO_ACTIVE, "Active"},
    {SysMeminfoType::MEMINFO_INACTIVE, "Inactive"},
    {SysMeminfoType::MEMINFO_ACTIVE_ANON, "Active(anon)"},
    {SysMeminfoType::MEMINFO_INACTIVE_ANON, "Inactive(anon)"},
    {SysMeminfoType::MEMINFO_ACTIVE_FILE, "Active(file)"},
    {SysMeminfoType::MEMINFO_INACTIVE_FILE, "Inactive(file)"},
    {SysMeminfoType::MEMINFO_UNEVICTABLE, "Unevictable"},
    {SysMeminfoType::MEMINFO_MLOCKED, "Mlocked"},
    {SysMeminfoType::MEMINFO_SWAP_TOTAL, "SwapTotal"},
    {SysMeminfoType::MEMINFO_SWAP_FREE, "SwapFree"},
    {SysMeminfoType::MEMINFO_DIRTY, "Dirty"},
    {SysMeminfoType::MEMINFO_WRITEBACK, "Writeback"},
    {SysMeminfoType::MEMINFO_ANON_PAGES, "AnonPages"},
    {SysMeminfoType::MEMINFO_MAPPED, "Mapped"},
    {SysMeminfoType::MEMINFO_SHMEM, "Shmem"},
};

struct ProcStatusMapping {
    int procid;
    const char* procstr;
};

enum StatusType {
    PRO_TGID = 1,
    PRO_NAME,
    PRO_VMSIZE,
    PRO_VMRSS,
    PRO_RSSANON,
    PRO_RSSFILE,
    PRO_RSSSHMEM,
    PRO_VMSWAP,
    PRO_VMLCK,
    PRO_VMHWM,
};

constexpr ProcStatusMapping procStatusMapping[] = {
    {StatusType::PRO_TGID, "Tgid"},         {StatusType::PRO_NAME, "Name"},       {StatusType::PRO_VMSIZE, "VmSize"},
    {StatusType::PRO_VMRSS, "VmRSS"},       {StatusType::PRO_RSSANON, "RssAnon"}, {StatusType::PRO_RSSFILE, "RssFile"},
    {StatusType::PRO_RSSSHMEM, "RssShmem"}, {StatusType::PRO_VMSWAP, "VmSwap"},   {StatusType::PRO_VMLCK, "VmLck"},
    {StatusType::PRO_VMHWM, "VmHWM"},
};

enum ErrorType {
    RET_NULL_ADDR,
    RET_IVALID_PID,
    RET_TGID_VALUE_NULL,
    RET_FAIL = -1,
    RET_SUCC = 0,
};

enum FileType {
    FILE_STATUS = 0,
    FILE_OOM,
    FILE_SMAPS,
};

struct ProcfdMapping {
    int procid;
    const char* file;
};

constexpr ProcfdMapping procfdMapping[] = {
    {FileType::FILE_STATUS, "status"},
    {FileType::FILE_OOM, "oom_score_adj"},
    {FileType::FILE_SMAPS, "smaps"},
};

class MemoryDataPlugin {
public:
    MemoryDataPlugin();
    ~MemoryDataPlugin();
    int Start(const uint8_t* configData, uint32_t configSize);
    int Report(uint8_t* configData, uint32_t configSize);
    int Stop();
    void SetPath(char* path)
    {
        testpath_ = path;
    };
    void WriteProcesseList(MemoryData& data);
    void WriteProcinfoByPidfds(ProcessMemoryInfo* processinfo, int32_t pid);
    DIR* OpenDestDir(const char* dirPath);
    int32_t GetValidPid(DIR* dirp);
    // for test change static
    int ParseNumber(std::string line);

private:
    /* data */
    MemoryConfig protoConfig_;

    std::unique_ptr<uint8_t[]> buffer_;

    int meminfoFd_;
    int vmstatFd_;
    std::map<std::string, int> meminfoCounters_;

    void InitProto2StrVector();
    std::vector<const char*> meminfoStrList_;
    // SmapsStats *
    void WriteVmstat(MemoryData& data);
    void WriteMeminfo(MemoryData& data);

    std::unordered_map<int32_t, std::vector<int>> pidFds_;
    std::vector<int32_t> seenPids_;
    char* testpath_;
    int32_t err_;
    int32_t ReadFile(int fd);
    std::vector<int> OpenProcPidFiles(int32_t pid);
    int32_t ReadProcPidFile(int32_t pid, const char* pFileName);
    void WriteProcessInfo(MemoryData& data, int32_t pid);
    void SetEmptyProcessInfo(ProcessMemoryInfo* processinfo);
    void WriteOomInfo(ProcessMemoryInfo* processinfo, int32_t pid);
    void WriteProcess(ProcessMemoryInfo* processinfo, const char* pFile, uint32_t fileLen, int32_t pid);
    void WriteAppsummary(ProcessMemoryInfo* processinfo, SmapsStats& smapInfo);
    void SetProcessInfo(ProcessMemoryInfo* processinfo, int key, const char* word);

    bool BufnCmp(const char* src, int srcLen, const char* key, int keyLen);
    bool addPidBySort(int32_t pid);
    int GetProcStatusId(const char* src, int srcLen);

    bool ParseMemInfo(const char* data, ProcessMemoryInfo* memoryInfo);
    bool GetMemInfoByDumpsys(uint32_t pid, ProcessMemoryInfo* memoryInfo);
};

#endif

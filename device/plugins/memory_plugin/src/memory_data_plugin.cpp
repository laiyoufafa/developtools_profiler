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
#include "memory_data_plugin.h"

#include <sstream>

#include "buffer_splitter.h"
#include "securec.h"
#include "smaps_stats.h"

namespace {
static const char* CMD_FORMAT = "dumpsys meminfo --local ";
constexpr size_t READ_BUFFER_SIZE = 1024 * 16;
static const int BUF_MAX_LEN = 2048;
} // namespace

MemoryDataPlugin::MemoryDataPlugin()
    : buffer_(new (std::nothrow) uint8_t[READ_BUFFER_SIZE]), meminfoFd_(-1), vmstatFd_(-1), err_(-1)
{
    InitProto2StrVector();
    SetPath(const_cast<char*>("/proc"));
}

MemoryDataPlugin::~MemoryDataPlugin()
{
    HILOG_INFO(LOG_CORE, "plugin:~MemoryDataPlugin!");

    buffer_ = nullptr;

    if (meminfoFd_ > 0) {
        close(meminfoFd_);
        meminfoFd_ = -1;
    }
    if (vmstatFd_ > 0) {
        close(vmstatFd_);
        vmstatFd_ = -1;
    }
    for (auto it = pidFds_.begin(); it != pidFds_.end(); it++) {
        for (int i = FILE_STATUS; i <= FILE_SMAPS; i++) {
            if (it->second[i] != -1) {
                close(it->second[i]);
            }
        }
    }
    return;
}

void MemoryDataPlugin::InitProto2StrVector()
{
    int maxprotobufid = 0;
    for (unsigned int i = 0; i < sizeof(meminfoMapping) / sizeof(meminfoMapping[0]); i++) {
        maxprotobufid = std::max(meminfoMapping[i].protobufid, maxprotobufid);
    }
    meminfoStrList_.resize(maxprotobufid + 1);

    for (unsigned int i = 0; i < sizeof(meminfoMapping) / sizeof(meminfoMapping[0]); i++) {
        meminfoStrList_[meminfoMapping[i].protobufid] = meminfoMapping[i].procstr;
    }
    return;
}

int MemoryDataPlugin::Start(const uint8_t* configData, uint32_t configSize)
{
    if (buffer_ == nullptr) {
        HILOG_ERROR(LOG_CORE, "buffer_ = null");
        return RET_FAIL;
    }

    if (protoConfig_.ParseFromArray(configData, configSize) <= 0) {
        HILOG_ERROR(LOG_CORE, "plugin:ParseFromArray failed");
        return RET_FAIL;
    }

    if (protoConfig_.report_sysmem_mem_info()) {
        char fileName[PATH_MAX + 1] = {0};
        char realPath[PATH_MAX + 1] = {0};
        if (snprintf_s(fileName, sizeof(fileName), sizeof(fileName) - 1, "%s/meminfo", testpath_) < 0) {
            HILOG_ERROR(LOG_CORE, "snprintf_s error");
        }
        if (realpath(fileName, realPath) == nullptr) {
            HILOG_ERROR(LOG_CORE, "plugin:realpath failed, errno=%d", errno);
            return RET_FAIL;
        }
        meminfoFd_ = open(realPath, O_RDONLY | O_CLOEXEC);
        if (meminfoFd_ == -1) {
            HILOG_ERROR(LOG_CORE, "plugin:open failed, fileName, errno=%d", errno);
            return RET_FAIL;
        }
    }
    if (protoConfig_.report_sysmem_vmem_info()) {
        vmstatFd_ = open("/proc/vmstat", O_RDONLY | O_CLOEXEC);
        if (vmstatFd_ == -1) {
            HILOG_ERROR(LOG_CORE, "plugin:Failed to open(/proc/vmstat), errno=%d", errno);
            return RET_FAIL;
        }
    }

    if (protoConfig_.sys_meminfo_counters().size() > 0) {
        for (int i = 0; i < protoConfig_.sys_meminfo_counters().size(); i++) {
            if (meminfoStrList_[protoConfig_.sys_meminfo_counters(i)]) {
                meminfoCounters_.emplace(meminfoStrList_[protoConfig_.sys_meminfo_counters(i)],
                                         protoConfig_.sys_meminfo_counters(i));
            }
        }
    }

    if (protoConfig_.pid().size() > 0) {
        for (int i = 0; i < protoConfig_.pid().size(); i++) {
            int32_t pid = protoConfig_.pid(i);
            pidFds_.emplace(pid, OpenProcPidFiles(pid));
        }
    }

    HILOG_INFO(LOG_CORE, "plugin:start success!");
    return RET_SUCC;
}

void MemoryDataPlugin::WriteMeminfo(MemoryData& data)
{
    int readsize = ReadFile(meminfoFd_);
    if (readsize == RET_FAIL) {
        HILOG_ERROR(LOG_CORE, "%s:read meminfoFd fail!", __func__);
        return;
    }
    BufferSplitter totalbuffer((const char*)buffer_.get(), readsize);

    do {
        if (!totalbuffer.NextWord(':')) {
            continue;
        }
        const_cast<char *>(totalbuffer.CurWord())[totalbuffer.CurWordSize()] = '\0';
        auto it = meminfoCounters_.find(totalbuffer.CurWord());
        if (it == meminfoCounters_.end()) {
            continue;
        }

        int counter_id = it->second;
        if (!totalbuffer.NextWord(' ')) {
            continue;
        }
        auto value = static_cast<uint64_t>(strtoll(totalbuffer.CurWord(), nullptr, DEC_BASE));
        auto* meminfo = data.add_meminfo();

        meminfo->set_key(static_cast<SysMeminfoType>(counter_id));
        meminfo->set_value(value);
    } while (totalbuffer.NextLine());

    return;
}

void MemoryDataPlugin::WriteVmstat(MemoryData& data)
{
    return;
}

void MemoryDataPlugin::WriteAppsummary(ProcessMemoryInfo* processinfo, SmapsStats& smapInfo)
{
    processinfo->mutable_memsummary()->set_java_heap(smapInfo.GetProcessJavaHeap());
    processinfo->mutable_memsummary()->set_native_heap(smapInfo.GetProcessNativeHeap());
    processinfo->mutable_memsummary()->set_code(smapInfo.GetProcessCode());
    processinfo->mutable_memsummary()->set_stack(smapInfo.GetProcessStack());
    processinfo->mutable_memsummary()->set_graphics(smapInfo.GetProcessGraphics());
    processinfo->mutable_memsummary()->set_private_other(smapInfo.GetProcessPrivateOther());
    processinfo->mutable_memsummary()->set_system(smapInfo.GetProcessSystem());
}

int MemoryDataPlugin::ParseNumber(std::string line)
{
    return atoi(line.substr(line.find_first_of("01234567890")).c_str());
}

bool MemoryDataPlugin::ParseMemInfo(const char* data, ProcessMemoryInfo* memoryInfo)
{
    bool ready = false;
    bool done = false;
    std::istringstream ss(data);
    std::string line;

    while (std::getline(ss, line)) {
        std::string s(line);
        if (s.find("App Summary") != s.npos) {
            ready = true;
            continue;
        }

        if (ready) {
            if (s.find("Java Heap:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_java_heap(ParseNumber(s));
                continue;
            }
            if (s.find("Native Heap:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_native_heap(ParseNumber(s));
                continue;
            }
            if (s.find("Code:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_code(ParseNumber(s));
                continue;
            }
            if (s.find("Stack:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_stack(ParseNumber(s));
                continue;
            }
            if (s.find("Graphics:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_graphics(ParseNumber(s));
                continue;
            }
            if (s.find("Private Other:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_private_other(ParseNumber(s));
                continue;
            }
            if (s.find("System:") != s.npos) {
                memoryInfo->mutable_memsummary()->set_system(ParseNumber(s));
                done = true;
                break;
            }
        }
    }
    return done;
}

bool MemoryDataPlugin::GetMemInfoByDumpsys(uint32_t pid, ProcessMemoryInfo* memoryInfo)
{
    std::string fullCmd = CMD_FORMAT + std::to_string(pid);

    std::unique_ptr<uint8_t[]> buffer {new (std::nothrow) uint8_t[BUF_MAX_LEN]};
    std::unique_ptr<FILE, int (*)(FILE*)> fp(popen(fullCmd.c_str(), "r"), pclose);
    if (!fp) {
        HILOG_ERROR(LOG_CORE, "popen error");
        return false;
    }

    fread(buffer.get(), 1, BUF_MAX_LEN, fp.get());
    buffer.get()[BUF_MAX_LEN - 1] = '\0';

    return ParseMemInfo(reinterpret_cast<char*>(buffer.get()), memoryInfo);
}

int MemoryDataPlugin::Report(uint8_t* data, uint32_t dataSize)
{
    MemoryData dataProto;
    uint32_t length;

    if (protoConfig_.report_process_tree()) {
        HILOG_DEBUG(LOG_CORE, "plugin:report process list");
        WriteProcesseList(dataProto);
    }

    if (protoConfig_.report_sysmem_mem_info()) {
        HILOG_DEBUG(LOG_CORE, "plugin:report system mem_info list");
        WriteMeminfo(dataProto);
    }

    if (protoConfig_.report_sysmem_vmem_info()) {
        HILOG_DEBUG(LOG_CORE, "plugin:report system vmem_info list");
        WriteVmstat(dataProto);
    }

    if (protoConfig_.pid().size() > 0) {
        for (int i = 0; i < protoConfig_.pid().size(); i++) {
            int32_t pid = protoConfig_.pid(i);
            auto* processinfo = dataProto.add_processesinfo();
            if (protoConfig_.report_process_mem_info()) {
                WriteProcinfoByPidfds(processinfo, pid);
            }

            if (protoConfig_.report_app_mem_info()) {
                if (protoConfig_.report_app_mem_by_dumpsys()) {
                    GetMemInfoByDumpsys(pid, processinfo);
                } else {
                    SmapsStats smapInfo;
                    smapInfo.ParseMaps(pid);
                    WriteAppsummary(processinfo, smapInfo);
                }
            }
        }
    }

    length = dataProto.ByteSizeLong();
    if (length > dataSize) {
        return -length;
    }
    if (dataProto.SerializeToArray(data, length) > 0) {
        return length;
    }
    return 0;
}

int MemoryDataPlugin::Stop()
{
    if (meminfoFd_ > 0) {
        close(meminfoFd_);
        meminfoFd_ = -1;
    }
    if (vmstatFd_ > 0) {
        close(vmstatFd_);
        vmstatFd_ = -1;
    }
    for (auto it = pidFds_.begin(); it != pidFds_.end(); it++) {
        for (int i = FILE_STATUS; i <= FILE_SMAPS; i++) {
            if (it->second[i] != -1) {
                close(it->second[i]);
            }
        }
    }
    HILOG_INFO(LOG_CORE, "plugin:stop success!");
    return 0;
}

void MemoryDataPlugin::WriteProcinfoByPidfds(ProcessMemoryInfo* processinfo, int32_t pid)
{
    char* end = nullptr;
    int32_t readSize;

    readSize = ReadFile(pidFds_[pid][FILE_STATUS]);
    if (readSize != RET_FAIL) {
        WriteProcess(processinfo, (char*)buffer_.get(), readSize, pid);
    } else {
        SetEmptyProcessInfo(processinfo);
    }
    if (ReadFile(pidFds_[pid][FILE_OOM]) != RET_FAIL) {
        processinfo->set_oom_score_adj(strtol((char*)buffer_.get(), &end, DEC_BASE));
    } else {
        processinfo->set_oom_score_adj(0);
    }
    return;
}

int32_t MemoryDataPlugin::ReadFile(int fd)
{
    if ((buffer_.get() == nullptr) || (fd == -1)) {
        HILOG_ERROR(LOG_CORE, "%s:Empty address, or invalid fd", __func__);
        return RET_FAIL;
    }
    int readsize = pread(fd, buffer_.get(), READ_BUFFER_SIZE - 1, 0);
    if (readsize <= 0) {
        HILOG_ERROR(LOG_CORE, "Failed to read(%d), errno=%d", fd, errno);
        err_ = errno;
        return RET_FAIL;
    }
    return readsize;
}

std::vector<int> MemoryDataPlugin::OpenProcPidFiles(int32_t pid)
{
    char fileName[PATH_MAX + 1] = {0};
    char realPath[PATH_MAX + 1] = {0};
    int count = sizeof(procfdMapping) / sizeof(procfdMapping[0]);
    std::vector<int> profds;

    for (int i = 0; i < count; i++) {
        if (snprintf_s(fileName, sizeof(fileName), sizeof(fileName) - 1,
            "%s/%d/%s", testpath_, pid, procfdMapping[i].file) < 0) {
            HILOG_ERROR(LOG_CORE, "snprintf_s error");
        }
        if (realpath(fileName, realPath) == nullptr) {
            HILOG_ERROR(LOG_CORE, "plugin:realpath failed, errno=%d", errno);
        }
        int fd = open(realPath, O_RDONLY | O_CLOEXEC);
        if (fd == -1) {
            HILOG_ERROR(LOG_CORE, "Failed to open(%s), errno=%d", fileName, errno);
        }
        profds.emplace(profds.begin() + i, fd);
    }
    return profds;
}

DIR* MemoryDataPlugin::OpenDestDir(const char* dirPath)
{
    DIR* destDir = nullptr;

    destDir = opendir(dirPath);
    if (destDir == nullptr) {
        HILOG_ERROR(LOG_CORE, "Failed to opendir(%s), errno=%d", dirPath, errno);
    }

    return destDir;
}

int32_t MemoryDataPlugin::GetValidPid(DIR* dirp)
{
    if (!dirp) return 0;
    while (struct dirent* dirEnt = readdir(dirp)) {
        if (dirEnt->d_type != DT_DIR) {
            continue;
        }

        int32_t pid = atoi(dirEnt->d_name);
        if (pid) {
            return pid;
        }
    }
    return 0;
}

int32_t MemoryDataPlugin::ReadProcPidFile(int32_t pid, const char* pFileName)
{
    char fileName[PATH_MAX + 1] = {0};
    char realPath[PATH_MAX + 1] = {0};
    int fd = -1;
    ssize_t bytesRead = 0;

    if (snprintf_s(fileName, sizeof(fileName), sizeof(fileName) - 1, "%s/%d/%s", testpath_, pid, pFileName) < 0) {
        HILOG_ERROR(LOG_CORE, "snprintf_s error");
    }
    if (realpath(fileName, realPath) == nullptr) {
        HILOG_ERROR(LOG_CORE, "plugin:realpath failed, errno=%d", errno);
        return RET_FAIL;
    }
    fd = open(realPath, O_RDONLY | O_CLOEXEC);
    if (fd == -1) {
        HILOG_INFO(LOG_CORE, "Failed to open(%s), errno=%d", fileName, errno);
        err_ = errno;
        return RET_FAIL;
    }
    if (buffer_.get() == nullptr) {
        HILOG_INFO(LOG_CORE, "%s:Empty address, buffer_ is NULL", __func__);
        err_ = RET_NULL_ADDR;
        close(fd);
        return RET_FAIL;
    }
    bytesRead = read(fd, buffer_.get(), READ_BUFFER_SIZE - 1);
    if (bytesRead <= 0) {
        close(fd);
        HILOG_INFO(LOG_CORE, "Failed to read(%s), errno=%d", fileName, errno);
        err_ = errno;
        return RET_FAIL;
    }
    close(fd);

    return bytesRead;
}

bool MemoryDataPlugin::BufnCmp(const char* src, int srcLen, const char* key, int keyLen)
{
    if (!src || !key || (srcLen < keyLen)) {
        return false;
    }
    for (int i = 0; i < keyLen; i++) {
        if (*src++ != *key++) {
            return false;
        }
    }
    return true;
}

bool MemoryDataPlugin::addPidBySort(int32_t pid)
{
    auto pidsEnd = seenPids_.end();
    auto it = std::lower_bound(seenPids_.begin(), pidsEnd, pid);
    if (it != pidsEnd && *it == pid) {
        return false;
    }
    it = seenPids_.insert(it, std::move(pid));
    return true;
}

int MemoryDataPlugin::GetProcStatusId(const char* src, int srcLen)
{
    int count = sizeof(procStatusMapping) / sizeof(procStatusMapping[0]);
    for (int i = 0; i < count; i++) {
        if (BufnCmp(src, srcLen, procStatusMapping[i].procstr, strlen(procStatusMapping[i].procstr))) {
            return procStatusMapping[i].procid;
        }
    }
    return RET_FAIL;
}

void MemoryDataPlugin::SetProcessInfo(ProcessMemoryInfo* processinfo, int key, const char* word)
{
    char* end = nullptr;

    switch (key) {
        case PRO_TGID:
            processinfo->set_pid(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_VMSIZE:
            processinfo->set_vm_size_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_VMRSS:
            processinfo->set_vm_rss_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_RSSANON:
            processinfo->set_rss_anon_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_RSSFILE:
            processinfo->set_rss_file_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_RSSSHMEM:
            processinfo->set_rss_shmem_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_VMSWAP:
            processinfo->set_vm_swap_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_VMLCK:
            processinfo->set_vm_locked_kb(strtoul(word, &end, DEC_BASE));
            break;
        case PRO_VMHWM:
            processinfo->set_vm_hwm_kb(strtoul(word, &end, DEC_BASE));
            break;
        default:
            break;
    }
    return;
}

void MemoryDataPlugin::WriteProcess(ProcessMemoryInfo* processinfo, const char* pFile, uint32_t fileLen, int32_t pid)
{
    BufferSplitter totalbuffer(const_cast<const char*>(pFile), fileLen + 1);

    do {
        totalbuffer.NextWord(':');
        if (!totalbuffer.CurWord()) {
            return;
        }

        int key = GetProcStatusId(totalbuffer.CurWord(), totalbuffer.CurWordSize());
        totalbuffer.NextWord('\n');
        if (!totalbuffer.CurWord()) {
            continue;
        }
        if (key == PRO_NAME) {
            processinfo->set_name(totalbuffer.CurWord(), totalbuffer.CurWordSize());
        }
        SetProcessInfo(processinfo, key, totalbuffer.CurWord());
    } while (totalbuffer.NextLine());
    // update process name
    int32_t ret = ReadProcPidFile(pid, "cmdline");
    if (ret > 0) {
        processinfo->set_name(reinterpret_cast<char*>(buffer_.get()), strlen(reinterpret_cast<char*>(buffer_.get())));
    }
}

void MemoryDataPlugin::SetEmptyProcessInfo(ProcessMemoryInfo* processinfo)
{
    processinfo->set_pid(-1);
    processinfo->set_name("null");
    processinfo->set_vm_size_kb(0);
    processinfo->set_vm_rss_kb(0);
    processinfo->set_rss_anon_kb(0);
    processinfo->set_rss_file_kb(0);
    processinfo->set_rss_shmem_kb(0);
    processinfo->set_vm_swap_kb(0);
    processinfo->set_vm_locked_kb(0);
    processinfo->set_vm_hwm_kb(0);
    processinfo->set_oom_score_adj(0);
}

void MemoryDataPlugin::WriteOomInfo(ProcessMemoryInfo* processinfo, int32_t pid)
{
    char* end = nullptr;

    if (ReadProcPidFile(pid, "oom_score_adj") == RET_FAIL) {
        processinfo->set_oom_score_adj(0);
        return;
    }
    if (buffer_.get() == nullptr) {
        processinfo->set_oom_score_adj(0);
        HILOG_ERROR(LOG_CORE, "%s:invalid params, read buffer_ is NULL", __func__);
        return;
    }
    processinfo->set_oom_score_adj(strtol((char*)buffer_.get(), &end, DEC_BASE));
}

void MemoryDataPlugin::WriteProcessInfo(MemoryData& data, int32_t pid)
{
    int32_t ret = ReadProcPidFile(pid, "status");
    if (ret == RET_FAIL) {
        SetEmptyProcessInfo(data.add_processesinfo());
        return;
    }
    if ((buffer_.get() == nullptr) || (ret == 0)) {
        HILOG_ERROR(LOG_CORE, "%s:invalid params, read buffer_ is NULL", __func__);
        return;
    }
    auto* processinfo = data.add_processesinfo();
    WriteProcess(processinfo, (char*)buffer_.get(), ret, pid);
    WriteOomInfo(processinfo, pid);
}

void MemoryDataPlugin::WriteProcesseList(MemoryData& data)
{
    DIR* procDir = nullptr;

    procDir = OpenDestDir(testpath_);
    if (procDir == nullptr) {
        return;
    }

    while (int32_t pid = GetValidPid(procDir)) {
        if (find(seenPids_.begin(), seenPids_.end(), pid) == seenPids_.end()) {
            addPidBySort(pid);
        }
    }

    for (unsigned int i = 0; i < seenPids_.size(); i++) {
        WriteProcessInfo(data, seenPids_[i]);
    }
    closedir(procDir);
}

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
#include "process_data_plugin.h"

#include <sstream>

#include "buffer_splitter.h"
#include "securec.h"

namespace {
constexpr size_t READ_BUFFER_SIZE = 1024 * 16;
constexpr int DEC_BASE = 10;
} // namespace

ProcessDataPlugin::ProcessDataPlugin()
    : buffer_(new (std::nothrow) uint8_t[READ_BUFFER_SIZE]), err_(-1)
{
    SetPath(const_cast<char*>("/proc"));
}

ProcessDataPlugin::~ProcessDataPlugin()
{
    HILOG_INFO(LOG_CORE, "%s:~ProcessDataPlugin!", __func__);

    buffer_ = nullptr;

    return;
}


int ProcessDataPlugin::Start(const uint8_t* configData, uint32_t configSize)
{
    if (buffer_ == nullptr) {
        HILOG_ERROR(LOG_CORE, "%s:buffer_ == null", __func__);
        return RET_FAIL;
    }

    if (protoConfig_.ParseFromArray(configData, configSize) <= 0) {
        HILOG_ERROR(LOG_CORE, "%s:parseFromArray failed!", __func__);
        return RET_FAIL;
    }

    HILOG_INFO(LOG_CORE, "%s:start success!", __func__);
    return RET_SUCC;
}

int ProcessDataPlugin::ParseNumber(std::string line)
{
    return atoi(line.substr(line.find_first_of("01234567890")).c_str());
}


int ProcessDataPlugin::Report(uint8_t* data, uint32_t dataSize)
{
    ProcessData dataProto;
    uint32_t length;

    if (protoConfig_.report_process_tree()) {
        WriteProcesseList(dataProto);
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

int ProcessDataPlugin::Stop()
{
    HILOG_INFO(LOG_CORE, "%s:stop success!", __func__);
    return 0;
}


int32_t ProcessDataPlugin::ReadFile(int fd)
{
    if ((buffer_.get() == nullptr) || (fd == -1)) {
        return RET_FAIL;
    }
    int readsize = pread(fd, buffer_.get(), READ_BUFFER_SIZE - 1, 0);
    if (readsize <= 0) {
        HILOG_ERROR(LOG_CORE, "%s:failed to read(%d), errno=%d", __func__, fd, errno);
        err_ = errno;
        return RET_FAIL;
    }
    return readsize;
}

DIR* ProcessDataPlugin::OpenDestDir(const char* dirPath)
{
    DIR* destDir = nullptr;

    destDir = opendir(dirPath);
    if (destDir == nullptr) {
        HILOG_ERROR(LOG_CORE, "%s:failed to opendir(%s), errno=%d", __func__, dirPath, errno);
    }

    return destDir;
}

int32_t ProcessDataPlugin::GetValidPid(DIR* dirp)
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

int32_t ProcessDataPlugin::ReadProcPidFile(int32_t pid, const char* pFileName)
{
    char fileName[PATH_MAX + 1] = {0};
    char realPath[PATH_MAX + 1] = {0};
    int fd = -1;
    ssize_t bytesRead = 0;

    if (snprintf_s(fileName, sizeof(fileName), sizeof(fileName) - 1, "%s/%d/%s", testpath_, pid, pFileName) < 0) {
        HILOG_ERROR(LOG_CORE, "%s:snprintf_s error", __func__);
    }
    if (realpath(fileName, realPath) == nullptr) {
        HILOG_ERROR(LOG_CORE, "%s:realpath failed, errno=%d", __func__, errno);
        return RET_FAIL;
    }
    fd = open(realPath, O_RDONLY | O_CLOEXEC);
    if (fd == -1) {
        HILOG_INFO(LOG_CORE, "%s:failed to open(%s), errno=%d", __func__, fileName, errno);
        err_ = errno;
        return RET_FAIL;
    }
    if (buffer_.get() == nullptr) {
        HILOG_INFO(LOG_CORE, "%s:empty address, buffer_ is NULL", __func__);
        err_ = RET_NULL_ADDR;
        close(fd);
        return RET_FAIL;
    }
    bytesRead = read(fd, buffer_.get(), READ_BUFFER_SIZE - 1);
    if (bytesRead <= 0) {
        close(fd);
        HILOG_INFO(LOG_CORE, "%s:failed to read(%s), errno=%d", __func__, fileName, errno);
        err_ = errno;
        return RET_FAIL;
    }
    buffer_.get()[bytesRead] = '\0';
    close(fd);

    return bytesRead;
}

bool ProcessDataPlugin::BufnCmp(const char* src, int srcLen, const char* key, int keyLen)
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

bool ProcessDataPlugin::addPidBySort(int32_t pid)
{
    auto pidsEnd = seenPids_.end();
    auto it = std::lower_bound(seenPids_.begin(), pidsEnd, pid);
    if (it != pidsEnd && *it == pid) {
        return false;
    }
    it = seenPids_.insert(it, std::move(pid));
    return true;
}

void ProcessDataPlugin::WriteProcess(ProcessInfo* processinfo, const char* pFile, uint32_t fileLen, int32_t pid)
{
    BufferSplitter totalbuffer(const_cast<const char*>(pFile), fileLen + 1);

    do {
        totalbuffer.NextWord(':');
        if (!totalbuffer.CurWord()) {
            return;
        }

        if (BufnCmp(totalbuffer.CurWord(), totalbuffer.CurWordSize(), "Name", strlen("Name"))) {
            totalbuffer.NextWord('\n');
            if (!totalbuffer.CurWord()) {
                return;
            }
            processinfo->set_name(totalbuffer.CurWord(), totalbuffer.CurWordSize());
        } else if (BufnCmp(totalbuffer.CurWord(), totalbuffer.CurWordSize(), "Tgid", strlen("Tgid"))) {
            totalbuffer.NextWord('\n');
            if (!totalbuffer.CurWord()) {
                return;
            }
            char* end = nullptr;
            int32_t value = static_cast<int32_t>(strtoul(totalbuffer.CurWord(), &end, DEC_BASE));
            if (value <= 0) {
                HILOG_ERROR(LOG_CORE, "%s:strtoull value failed", __func__);
            }
            processinfo->set_pid(value);
            break;
        }

        totalbuffer.NextWord('\n');
        if (!totalbuffer.CurWord()) {
            continue;
        }
    } while (totalbuffer.NextLine());
    // update process name
    int32_t ret = ReadProcPidFile(pid, "cmdline");
    if (ret > 0) {
        processinfo->set_name(reinterpret_cast<char*>(buffer_.get()), strlen(reinterpret_cast<char*>(buffer_.get())));
    }
}

void ProcessDataPlugin::SetEmptyProcessInfo(ProcessInfo* processinfo)
{
    processinfo->set_pid(-1);
    processinfo->set_name("null");
}

void ProcessDataPlugin::WriteProcessInfo(ProcessData& data, int32_t pid)
{
    int32_t ret = ReadProcPidFile(pid, "status");
    if (ret == RET_FAIL) {
        SetEmptyProcessInfo(data.add_processesinfo());
        return;
    }
    if ((buffer_.get() == nullptr) || (ret == 0)) {
        return;
    }
    auto* processinfo = data.add_processesinfo();
    WriteProcess(processinfo, (char*)buffer_.get(), ret, pid);
}

void ProcessDataPlugin::WriteProcesseList(ProcessData& data)
{
    DIR* procDir = nullptr;

    procDir = OpenDestDir(testpath_);
    if (procDir == nullptr) {
        return;
    }

    seenPids_.clear();
    while (int32_t pid = GetValidPid(procDir)) {
        addPidBySort(pid);
    }

    for (unsigned int i = 0; i < seenPids_.size(); i++) {
        WriteProcessInfo(data, seenPids_[i]);
    }
    closedir(procDir);
}

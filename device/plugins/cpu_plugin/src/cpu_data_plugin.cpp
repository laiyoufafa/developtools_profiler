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

#include "cpu_data_plugin.h"

#include <ctime>
#include <vector>

#include "buffer_splitter.h"

namespace {
constexpr size_t READ_BUFFER_SIZE = 1024 * 16;
constexpr int SYSTEM_STAT_COUNT = 9;
constexpr int STAT_COUNT = 17;
constexpr int STAT_START = 13;
constexpr int THREAD_NAME_POS = 1;
constexpr int THREAD_STATE_POS = 2;
constexpr int CPU_USER_HZ_L = 100;
constexpr int CPU_USER_HZ_H = 1000;
constexpr int CPU_HZ_H = 10;

const std::string FREQUENCY_PATH = "/sys/devices/system/cpu";
const std::string FREQUENCY_MIN_PATH = "/cpufreq/cpuinfo_min_freq";
const std::string FREQUENCY_MAX_PATH = "/cpufreq/cpuinfo_max_freq";
const std::string FREQUENCY_CUR_PATH = "/cpufreq/cpuinfo_cur_freq";
constexpr int CPU_FREQUENCY_KHZ = 1000;
} // namespace

CpuDataPlugin::CpuDataPlugin()
{
    buffer_ = nullptr;
    path_ = "/proc/";
    err_ = -1;
    pid_ = -1;
    prevProcessCpuTime_ = 0;
    prevSystemCpuTime_ = 0;
    prevSystemBootTime_ = 0;
    maxFreqIndex_ = -1;
    freqPath_ = FREQUENCY_PATH;
}

CpuDataPlugin::~CpuDataPlugin()
{
    HILOG_INFO(LOG_CORE, "plugin:~CpuDataPlugin!");
    if (buffer_ != nullptr) {
        free(buffer_);
        buffer_ = nullptr;
    }

    tidVec_.clear();
    prevThreadCpuTimeMap_.clear();
    prevCoreSystemCpuTimeMap_.clear();
    prevCoreSystemBootTimeMap_.clear();
    maxFrequencyVec_.clear();
    minFrequencyVec_.clear();
}

int CpuDataPlugin::Start(const uint8_t* configData, uint32_t configSize)
{
    buffer_ = malloc(READ_BUFFER_SIZE);
    if (buffer_ == nullptr) {
        HILOG_ERROR(LOG_CORE, "plugin:malloc buffer_ fail");
        return RET_FAIL;
    }

    if (protoConfig_.ParseFromArray(configData, configSize) <= 0) {
        HILOG_ERROR(LOG_CORE, "plugin:ParseFromArray failed");
        return RET_FAIL;
    }

    if (protoConfig_.pid() > 0) {
        pid_ = protoConfig_.pid();
    } else {
        HILOG_ERROR(LOG_CORE, "plugin:Invalid pid");
        return RET_FAIL;
    }
    HILOG_INFO(LOG_CORE, "plugin:start success!");
    return RET_SUCC;
}

int CpuDataPlugin::Report(uint8_t* data, uint32_t dataSize)
{
    CpuData dataProto;
    uint32_t length;

    WriteCpuUsageInfo(dataProto);
    WriteThreadInfo(dataProto);

    length = dataProto.ByteSizeLong();
    if (length > dataSize) {
        return -length;
    }
    if (dataProto.SerializeToArray(data, length) > 0) {
        return length;
    }
    return 0;
}

int CpuDataPlugin::Stop()
{
    if (buffer_ != nullptr) {
        free(buffer_);
        buffer_ = nullptr;
    }

    tidVec_.clear();
    prevThreadCpuTimeMap_.clear();
    prevCoreSystemCpuTimeMap_.clear();
    prevCoreSystemBootTimeMap_.clear();
    HILOG_INFO(LOG_CORE, "plugin:stop success!");
    return 0;
}

int32_t CpuDataPlugin::ReadFile(std::string& fileName)
{
    int fd = -1;
    ssize_t bytesRead = 0;

    fd = open(fileName.c_str(), O_RDONLY | O_CLOEXEC);
    if (fd == -1) {
        HILOG_ERROR(LOG_CORE, "Failed to open(%s), errno=%d", fileName.c_str(), errno);
        err_ = errno;
        return RET_FAIL;
    }
    if (buffer_ == nullptr) {
        HILOG_ERROR(LOG_CORE, "%s:Empty address, buffer_ is NULL", __func__);
        err_ = RET_NULL_ADDR;
        close(fd);
        return RET_FAIL;
    }
    bytesRead = read(fd, buffer_, READ_BUFFER_SIZE - 1);
    if (bytesRead <= 0) {
        close(fd);
        HILOG_ERROR(LOG_CORE, "Failed to read(%s), errno=%d", fileName.c_str(), errno);
        err_ = errno;
        return RET_FAIL;
    }
    close(fd);

    return bytesRead;
}

void CpuDataPlugin::SetTimestamp(SampleTimeStamp& timestamp)
{
    timespec time;
    clock_gettime(CLOCK_MONOTONIC, &time);
    timestamp.set_tv_sec(time.tv_sec);
    timestamp.set_tv_nsec(time.tv_nsec);
}

int64_t CpuDataPlugin::GetUserHz()
{
    int64_t hz = -1;
    int64_t user_hz = sysconf(_SC_CLK_TCK);
    switch (user_hz) {
        case CPU_USER_HZ_L:
            hz = CPU_HZ_H;
            break;
        case CPU_USER_HZ_H:
            hz = 1;
            break;
        default:
            break;
    }
    return hz;
}

int64_t CpuDataPlugin::GetCpuUsageTime(std::vector<std::string>& cpuUsageVec)
{
    int64_t utime, stime, cutime, cstime, usageTime;
    utime = atoi(cpuUsageVec[PROCESS_UTIME].c_str());
    stime = atoi(cpuUsageVec[PROCESS_STIME].c_str());
    cutime = atoi(cpuUsageVec[PROCESS_CUTIME].c_str());
    cstime = atoi(cpuUsageVec[PROCESS_CSTIME].c_str());
    usageTime = (utime + stime + cutime + cstime) * GetUserHz();

    return usageTime;
}

void CpuDataPlugin::WriteProcessCpuUsage(CpuUsageInfo& cpuUsageInfo, const char* pFile, uint32_t fileLen)
{
    BufferSplitter totalbuffer(const_cast<char*>(pFile), fileLen + 1);
    std::vector<std::string> cpuUsageVec;
    for (int i = 0; i < STAT_COUNT; i++) {
        totalbuffer.NextWord(' ');
        if (!totalbuffer.CurWord()) {
            return;
        }

        if (i < STAT_START) {
            continue;
        } else {
            std::string curWord = std::string(totalbuffer.CurWord(), totalbuffer.CurWordSize());
            cpuUsageVec.push_back(curWord);
        }
    }

    // 获取到的数据不包含utime、stime、cutime、cstime四个数值时返回
    if (cpuUsageVec.size() != PROCESS_UNSPECIFIED) {
        HILOG_ERROR(LOG_CORE, "Failed to get process cpu usage, size=%d", cpuUsageVec.size());
        return;
    }

    int64_t usageTime = GetCpuUsageTime(cpuUsageVec);
    cpuUsageInfo.set_prev_process_cpu_time_ms(prevProcessCpuTime_);
    cpuUsageInfo.set_process_cpu_time_ms(usageTime);
    prevProcessCpuTime_ = usageTime;
}

int32_t CpuDataPlugin::GetCpuFrequency(std::string fileName)
{
    int32_t frequency = 0;
    int32_t ret = ReadFile(fileName);
    if (ret == RET_FAIL) {
        HILOG_ERROR(LOG_CORE, "read %s file failed", fileName.c_str());
    } else {
        frequency = atoi((char*)buffer_);
    }
    return frequency;
}

int CpuDataPlugin::GetCpuCoreSize()
{
    int coreSize = 0;
    DIR* procDir = nullptr;
    procDir = OpenDestDir(freqPath_);
    if (procDir == nullptr) {
        HILOG_ERROR(LOG_CORE, "procDir is nullptr");
        return -1;
    }

    while (struct dirent* dirEnt = readdir(procDir)) {
        if (dirEnt->d_type != DT_DIR) {
            continue;
        }
        if (strncmp(dirEnt->d_name, "cpu", strlen("cpu")) == 0) {
            coreSize++;
        }
    }

    return coreSize;
}

int32_t CpuDataPlugin::GetMaxCpuFrequencyIndex()
{
    int coreSize = GetCpuCoreSize();
    int index = -1;
    int32_t maxFreq = -1;
    maxFrequencyVec_.clear();
    minFrequencyVec_.clear();
    for (int i = 0; i < coreSize; i++) {
        std::string fileName = freqPath_ + "/cpu" + std::to_string(i) + FREQUENCY_MAX_PATH;
        int32_t maxFrequency = GetCpuFrequency(fileName);
        maxFrequencyVec_.push_back(maxFrequency);
        fileName = freqPath_ + "/cpu" + std::to_string(i) + FREQUENCY_MIN_PATH;
        int32_t minFrequency = GetCpuFrequency(fileName);
        minFrequencyVec_.push_back(minFrequency);

        if (maxFreq < maxFrequency) {
            maxFreq = maxFrequency;
            index = i;
        }
    }

    // 单核或所有核最大频率相同，默认小核
    if (coreSize == 1 || (coreSize > 1 && index == 0 && maxFreq == maxFrequencyVec_[1])) {
        index = -1;
    }

    return index;
}

void CpuDataPlugin::SetCpuFrequency(CpuCoreUsageInfo& cpuCore, int32_t coreNum)
{
    // 第一次获取最大频率核位置，并保存各核最大最小频率到vector
    if (maxFrequencyVec_.empty() || minFrequencyVec_.empty()) {
        maxFreqIndex_ = GetMaxCpuFrequencyIndex();
    }
    std::string fileName = freqPath_ + "/cpu" + std::to_string(coreNum) + FREQUENCY_CUR_PATH;
    int32_t curFrequency = GetCpuFrequency(fileName) / CPU_FREQUENCY_KHZ;
    int32_t maxFrequency = maxFrequencyVec_[coreNum] / CPU_FREQUENCY_KHZ;
    int32_t minFrequency = minFrequencyVec_[coreNum] / CPU_FREQUENCY_KHZ;

    if (coreNum == maxFreqIndex_) {
        cpuCore.set_is_little_core(false);
    } else {
        cpuCore.set_is_little_core(true);
    }
    CpuCoreFrequency* frequency = cpuCore.mutable_frequency();
    frequency->set_min_frequency_khz(minFrequency);
    frequency->set_max_frequency_khz(maxFrequency);
    frequency->set_cur_frequency_khz(curFrequency);
}

void CpuDataPlugin::GetSystemCpuTime(std::vector<std::string>& cpuUsageVec, int64_t& usageTime, int64_t& time)
{
    // 获取到的数据不包含user, nice, system, idle, iowait, irq, softirq, steal八个数值时返回
    if (cpuUsageVec.size() != SYSTEM_UNSPECIFIED) {
        HILOG_ERROR(LOG_CORE, "Failed to get system cpu usage, size=%d", cpuUsageVec.size());
        return;
    }

    int64_t user, nice, system, idle, iowait, irq, softirq, steal;
    user = atoi(cpuUsageVec[SYSTEM_USER].c_str());
    nice = atoi(cpuUsageVec[SYSTEM_NICE].c_str());
    system = atoi(cpuUsageVec[SYSTEM_SYSTEM].c_str());
    idle = atoi(cpuUsageVec[SYSTEM_IDLE].c_str());
    iowait = atoi(cpuUsageVec[SYSTEM_IOWAIT].c_str());
    irq = atoi(cpuUsageVec[SYSTEM_IRQ].c_str());
    softirq = atoi(cpuUsageVec[SYSTEM_SOFTIRQ].c_str());
    steal = atoi(cpuUsageVec[SYSTEM_STEAL].c_str());

    usageTime = (user + nice + system + irq + softirq + steal) * GetUserHz();
    time = (usageTime + idle + iowait) * GetUserHz();
}

void CpuDataPlugin::WriteSystemCpuUsage(CpuUsageInfo& cpuUsageInfo, const char* pFile, uint32_t fileLen)
{
    BufferSplitter totalbuffer(const_cast<char*>(pFile), fileLen + 1);
    std::vector<std::string> cpuUsageVec;
    int64_t usageTime, time;
    size_t cpuLength = strlen("cpu");

    do {
        totalbuffer.NextWord(' ');
        if (strncmp(totalbuffer.CurWord(), "cpu", cpuLength) != 0) {
            return;
        }

        for (int i = 0; i < SYSTEM_STAT_COUNT; i++) {
            if (!totalbuffer.CurWord()) {
                return;
            }
            std::string curWord = std::string(totalbuffer.CurWord(), totalbuffer.CurWordSize());
            cpuUsageVec.push_back(curWord);
            totalbuffer.NextWord(' ');
        }

        GetSystemCpuTime(cpuUsageVec, usageTime, time);
        if (strcmp(cpuUsageVec[0].c_str(), "cpu") == 0) {
            cpuUsageInfo.set_prev_system_cpu_time_ms(prevSystemCpuTime_);
            cpuUsageInfo.set_prev_system_boot_time_ms(prevSystemBootTime_);
            cpuUsageInfo.set_system_cpu_time_ms(usageTime);
            cpuUsageInfo.set_system_boot_time_ms(time);
            prevSystemCpuTime_ = usageTime;
            prevSystemBootTime_ = time;
        } else {
            std::string core = std::string(cpuUsageVec[0].c_str() + cpuLength, cpuUsageVec[0].size() - cpuLength);
            int32_t coreNum = atoi(core.c_str());
            // 第一次获取数据时需要将前一个数据置为0
            if (prevCoreSystemCpuTimeMap_.size() == static_cast<size_t>(coreNum)) {
                prevCoreSystemCpuTimeMap_[coreNum] = 0;
                prevCoreSystemBootTimeMap_[coreNum] = 0;
            }
            CpuCoreUsageInfo* cpuCore = cpuUsageInfo.add_cores();
            cpuCore->set_cpu_core(coreNum);
            cpuCore->set_prev_system_cpu_time_ms(prevCoreSystemCpuTimeMap_[coreNum]);
            cpuCore->set_prev_system_boot_time_ms(prevCoreSystemBootTimeMap_[coreNum]);
            cpuCore->set_system_cpu_time_ms(usageTime);
            cpuCore->set_system_boot_time_ms(time);

            SetCpuFrequency(*cpuCore, coreNum);
            prevCoreSystemCpuTimeMap_[coreNum] = usageTime;
            prevCoreSystemBootTimeMap_[coreNum] = time;
        }

        cpuUsageVec.clear();
        usageTime = 0;
        time = 0;
    } while (totalbuffer.NextLine());
}

void CpuDataPlugin::WriteCpuUsageInfo(CpuData& data)
{
    // write process info
    std::string fileName = path_ + std::to_string(pid_) + "/stat";
    int32_t ret = ReadFile(fileName);
    if (ret == RET_FAIL) {
        HILOG_ERROR(LOG_CORE, "read /proc/pid/stat file failed");
        return;
    }
    if ((buffer_ == nullptr) || (ret == 0)) {
        HILOG_ERROR(LOG_CORE, "%s:invalid params, read buffer_ is NULL", __func__);
        return;
    }
    auto* cpuUsageInfo = data.mutable_cpu_usage_info();
    WriteProcessCpuUsage(*cpuUsageInfo, (char*)buffer_, ret);

    // write system info
    fileName = path_ + "stat";
    ret = ReadFile(fileName);
    if (ret == RET_FAIL) {
        HILOG_ERROR(LOG_CORE, "read /proc/stat file failed");
        return;
    }
    if ((buffer_ == nullptr) || (ret == 0)) {
        HILOG_ERROR(LOG_CORE, "%s:invalid params, read buffer_ is NULL", __func__);
        return;
    }
    WriteSystemCpuUsage(*cpuUsageInfo, (char*)buffer_, ret);

    auto* timestamp = cpuUsageInfo->mutable_timestamp();
    SetTimestamp(*timestamp);
}

bool CpuDataPlugin::addTidBySort(int32_t tid)
{
    auto tidsEnd = tidVec_.end();
    auto it = std::lower_bound(tidVec_.begin(), tidsEnd, tid);
    if (it != tidsEnd && *it == tid) {
        return false;
    }
    it = tidVec_.insert(it, std::move(tid));
    return true;
}

DIR* CpuDataPlugin::OpenDestDir(std::string& dirPath)
{
    DIR* destDir = nullptr;

    destDir = opendir(dirPath.c_str());
    if (destDir == nullptr) {
        HILOG_ERROR(LOG_CORE, "Failed to opendir(%s), errno=%d", dirPath.c_str(), errno);
    }

    return destDir;
}

int32_t CpuDataPlugin::GetValidTid(DIR* dirp)
{
    if (!dirp) {
        return 0;
    }
    while (struct dirent* dirEnt = readdir(dirp)) {
        if (dirEnt->d_type != DT_DIR) {
            continue;
        }

        int32_t tid = atoi(dirEnt->d_name);
        if (tid) {
            return tid;
        }
    }
    return 0;
}

ThreadState CpuDataPlugin::GetThreadState(const char threadState)
{
    ThreadState state = THREAD_UNSPECIFIED;
    switch (threadState) {
        case 'R':
            state = THREAD_RUNNING;
            break;
        case 'S':
            state = THREAD_SLEEPING;
            break;
        case 'T':
            state = THREAD_STOPPED;
            break;
        case 'D':
            state = THREAD_WAITING;
            break;
        default:
            break;
    }

    return state;
}

void CpuDataPlugin::WriteThread(ThreadInfo& threadInfo, const char* pFile, uint32_t fileLen, int32_t tid)
{
    BufferSplitter totalbuffer(const_cast<char*>(pFile), fileLen + 1);
    std::vector<std::string> cpuUsageVec;
    for (int i = 0; i < STAT_COUNT; i++) {
        totalbuffer.NextWord(' ');
        if (!totalbuffer.CurWord()) {
            return;
        }

        if (i == THREAD_NAME_POS) {
            std::string curWord = std::string(totalbuffer.CurWord() + 1, totalbuffer.CurWordSize() - sizeof(")"));
            threadInfo.set_thread_name(curWord);
        } else if (i == THREAD_STATE_POS) {
            std::string curWord = std::string(totalbuffer.CurWord(), totalbuffer.CurWordSize());
            ThreadState state = GetThreadState(curWord[0]);
            threadInfo.set_thread_state(state);
        } else if (i >= STAT_START) {
            std::string curWord = std::string(totalbuffer.CurWord(), totalbuffer.CurWordSize());
            cpuUsageVec.push_back(curWord);
        }
    }

    // 获取到的数据不包含utime、stime、cutime、cstime四个数值时返回
    if (cpuUsageVec.size() != PROCESS_UNSPECIFIED) {
        HILOG_ERROR(LOG_CORE, "Failed to get thread cpu usage, size=%d", cpuUsageVec.size());
        return;
    }

    // 第一次获取该线程数据时需要将前一个数据置为0
    if (prevThreadCpuTimeMap_.find(tid) == prevThreadCpuTimeMap_.end()) {
        prevThreadCpuTimeMap_[tid] = 0;
    }

    int64_t usageTime = GetCpuUsageTime(cpuUsageVec);
    threadInfo.set_prev_thread_cpu_time_ms(prevThreadCpuTimeMap_[tid]);
    threadInfo.set_thread_cpu_time_ms(usageTime);
    prevThreadCpuTimeMap_[tid] = usageTime;
    threadInfo.set_tid(tid);

    auto* timestamp = threadInfo.mutable_timestamp();
    SetTimestamp(*timestamp);
}

void CpuDataPlugin::WriteSingleThreadInfo(CpuData& data, int32_t tid)
{
    std::string fileName = path_ + std::to_string(pid_) + "/task/" + std::to_string(tid) + "/stat";
    int32_t ret = ReadFile(fileName);
    if (ret == RET_FAIL) {
        HILOG_ERROR(LOG_CORE, "%s:read tid file failed", fileName.c_str());
        return;
    }
    if ((buffer_ == nullptr) || (ret == 0)) {
        HILOG_ERROR(LOG_CORE, "%s:invalid params, read buffer_ is NULL", __func__);
        return;
    }
    auto* threadInfo = data.add_thread_info();
    WriteThread(*threadInfo, (char*)buffer_, ret, tid);
}

void CpuDataPlugin::WriteThreadInfo(CpuData& data)
{
    DIR* procDir = nullptr;
    std::string path = path_ + std::to_string(pid_) + "/task";
    procDir = OpenDestDir(path);
    if (procDir == nullptr) {
        return;
    }

    while (int32_t tid = GetValidTid(procDir)) {
        if (find(tidVec_.begin(), tidVec_.end(), tid) == tidVec_.end()) {
            addTidBySort(tid);
        }
    }

    for (unsigned int i = 0; i < tidVec_.size(); i++) {
        WriteSingleThreadInfo(data, tidVec_[i]);
    }
    closedir(procDir);
}

// for UT
void CpuDataPlugin::SetFreqPath(std::string path)
{
    freqPath_ = path + FREQUENCY_PATH;
}

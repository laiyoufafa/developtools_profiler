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

#ifndef PROCESS_DATA_PLUGIN_H
#define PROCESS_DATA_PLUGIN_H

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
#include "process_plugin_config.pb.h"
#include "process_plugin_result.pb.h"

enum ErrorType {
    RET_NULL_ADDR,
    RET_IVALID_PID,
    RET_TGID_VALUE_NULL,
    RET_FAIL = -1,
    RET_SUCC = 0,
};

class ProcessDataPlugin {
public:
    ProcessDataPlugin();
    ~ProcessDataPlugin();
    int Start(const uint8_t* configData, uint32_t configSize);
    int Report(uint8_t* configData, uint32_t configSize);
    int Stop();
    void SetPath(char* path)
    {
        testpath_ = path;
    };
    void WriteProcesseList(ProcessData& data);
    void WriteProcinfoByPidfds(ProcessInfo* processinfo, int32_t pid);
    DIR* OpenDestDir(const char* dirPath);
    int32_t GetValidPid(DIR* dirp);
    // for test change static
    int ParseNumber(std::string line);

private:
    ProcessConfig protoConfig_;

    std::unique_ptr<uint8_t[]> buffer_;

    std::unordered_map<int32_t, std::vector<int>> pidFds_;
    std::vector<int32_t> seenPids_;
    char* testpath_;
    int32_t err_;
    int32_t ReadFile(int fd);
    std::vector<int> OpenProcPidFiles(int32_t pid);
    int32_t ReadProcPidFile(int32_t pid, const char* pFileName);
    void WriteProcessInfo(ProcessData& data, int32_t pid);
    void SetEmptyProcessInfo(ProcessInfo* processinfo);
    void WriteProcess(ProcessInfo* processinfo, const char* pFile, uint32_t fileLen, int32_t pid);
    void SetProcessInfo(ProcessInfo* processinfo, int key, const char* word);

    bool BufnCmp(const char* src, int srcLen, const char* key, int keyLen);
    bool addPidBySort(int32_t pid);
    int GetProcStatusId(const char* src, int srcLen);
};

#endif

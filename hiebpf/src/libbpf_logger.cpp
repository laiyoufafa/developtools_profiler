/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
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

#include <sstream>
#include <ctime>

#include <stdio.h>
#include <sys/time.h>


#include "libbpf_logger.h"
#include "hhlog.h"

std::unique_ptr<LIBBPFLogger> LIBBPFLogger::MakeUnique(const std::string& logFile, int logLevel)
{
    std::unique_ptr<LIBBPFLogger> logger {new(std::nothrow) LIBBPFLogger {logLevel}};
    if (logger == nullptr) {
        return nullptr;
    }
#if defined(BPF_LOGGER_DEBUG) || defined(BPF_LOGGER_INFO) || defined(BPF_LOGGER_WARN) || defined(BPF_LOGGER_ERROR) || defined(BPF_LOGGER_FATAL)
    if (logger->OpenLogFile(logFile) != 0) {
        return nullptr;
    }
#endif
    return logger;
}

int LIBBPFLogger::Printf(int logLevel, const char* format, ...)
{
    HHLOGI(true, "current libbpf log level = %d, target level = %d", logLevel, logLevel_);
    if (logLevel > logLevel_) {
        return 0;
    }
#if defined(BPF_LOGGER_DEBUG) || defined(BPF_LOGGER_INFO) || defined(BPF_LOGGER_WARN) || defined(BPF_LOGGER_ERROR) || defined(BPF_LOGGER_FATAL)
    char buffer[MAX_LIBBPF_LOG_LEN];
    va_list args;
    int ret = sprintf(buffer, format, args);
    return write(fd_, buffer, ret);
#endif
    return 0;
}

int LIBBPFLogger::OpenLogFile(const std::string& logFile)
{
    if (logFile.compare("stdout") == 0) {
        if (fcntl(STDOUT_FILENO, F_GETFL)) {
            fd_ = open("/dev/stdout", O_WRONLY);
        } else {
            fd_ = STDOUT_FILENO;
        }
        if (fd_ < 0) {
            return -1;
        }
        return 0;
    }
    auto fileName = GetLogFileName();
    if (fileName.length() == 0) {
        return -1;
    }
    fileName = "/data/local/tmp/" + fileName;
    fd_ = open(fileName.c_str(), O_WRONLY | O_CREAT);
    if (fd_ < 0) {
        return -1;
    }
    unlink(logFile.c_str());
    if (link(fileName.c_str(), logFile.c_str()) != 0) {
        return -1;
    }

    return 0;
}

std::string LIBBPFLogger::GetLogFileName() const
{
    struct timeval timer;
    gettimeofday(&timer, nullptr);
    time_t now = (time_t) timer.tv_sec;
    struct tm* tmPtr {nullptr};
    tmPtr = localtime(&now);
    if (tmPtr == nullptr) {
        return "";
    }
    std::stringstream ss;
    constexpr int yearStart {1900};
    constexpr int monthStart {1};
    ss << std::to_string(tmPtr->tm_year + yearStart) << ".";
    ss << std::to_string(tmPtr->tm_mon + monthStart) << ".";
    ss << std::to_string(tmPtr->tm_mday) << "_";
    ss << std::to_string(tmPtr->tm_hour) << ".";
    ss << std::to_string(tmPtr->tm_min) << ".";
    ss << std::to_string(tmPtr->tm_sec) << ".libbpf.log";
    return ss.str();
}
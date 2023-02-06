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

#ifndef _HHLOG_H
#define _HHLOG_H

#include <iostream>
#include <sstream>
#include <strstream>
#include <thread>
#include <string>
#include <memory>
#include <atomic>
#include <ctime>
#include <cstring>

#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdarg.h>
#include <sys/time.h>
#include <securec.h>

#include "ringbuffer.h"

enum HHLOG_LEVEL:int {
    HHLOG_DEBUG = 0,
    HHLOG_INFO = 1,
    HHLOG_WARN = 2,
    HHLOG_ERROR = 3,
    HHLOG_FATAL = 4,
    HHLOG_NONE = 5,
};

class HHLogger {
public:
    ~HHLogger();
    static inline HHLogger& GetInstance()
    {
        static HHLogger logger {};
        return logger;
    }

    int Start(const int logLevel = HHLOG_DEBUG, const std::string& logFile = "./hhlog.txt");
    
    inline int PutLog(const char* format, ...)
    {
        va_list args;
        va_start(args, format);
        auto ftime = GetFormatTime();
        std::size_t nbytes = ftime.length();
        if (nbytes == 0 or nbytes >= MAX_HHLOG_SIZE) {
            return -1;
        }
        char buffer[MAX_HHLOG_SIZE];
        if (memcpy_s(buffer, sizeof(buffer), ftime.c_str(), nbytes) != EOK) {
            return -1;
        }
        int ret = vsnprintf_s(buffer + nbytes, sizeof(buffer) - nbytes,
                              sizeof(buffer) - nbytes - 1, format, args);
        va_end(args);
        if (ret < 0) {
            return -1;
        }
        nbytes += static_cast<std::size_t>(ret);
        if (nbytes >= MAX_HHLOG_SIZE) {
            return -1;
        }
        buffer[nbytes++] = '\n';
        return buf_->Put(buffer, nbytes);
    }

    inline int GetLogLevel() const
    {
        return logLevel_;
    }

    inline bool IsStopped()
    {
        return stop_.load();
    }

    enum SizeConsts:std::size_t {
        RING_BUF_SIZE = 4096,
        MAX_FORMAT_SIZE = 512,
        MAX_HHLOG_SIZE = 1024,
    };

private:
    HHLogger() = default;
    std::string GetLogFileName() const;
    std::string GetFormatTime() const;
    int SaveLog();
    int InitLogger(const int logLevel, const std::string& logFile);
    int UpdateTimer();

    int fd_ {-1};
    int logLevel_ {HHLOG_NONE};
    std::atomic<bool> stop_ {false};
    std::atomic<struct timeval> timer_;
    std::unique_ptr<RingBuffer> buf_ {nullptr};
    std::thread logSaver_;
};

#define __HHLOG(level, expression, format, ...) {                           \
    if ((expression) and                                                    \
        (!HHLogger::GetInstance().IsStopped()) and                          \
        (HHLogger::GetInstance().GetLogLevel() <= HHLOG_##level)) {         \
        const char prefix[] {" [" #level "] %s %d %s: %s"};                 \
        char buffer[HHLogger::MAX_FORMAT_SIZE];                             \
        snprintf_s(buffer, sizeof(buffer), sizeof(buffer) -1, prefix, __FILE__, __LINE__, __FUNCTION__, format);  \
        HHLogger::GetInstance().PutLog(buffer, ##__VA_ARGS__);              \
    }                                                                       \
}

#if defined(HH_LOGGER_DEBUG)

#define HHLOGD(expression, format, ...) __HHLOG(DEBUG, expression, format, ##__VA_ARGS__)
#define HHLOGI(expression, format, ...) __HHLOG(INFO, expression, format, ##__VA_ARGS__)
#define HHLOGW(expression, format, ...) __HHLOG(WARN, expression, format, ##__VA_ARGS__)
#define HHLOGE(expression, format, ...) __HHLOG(ERROR, expression, format, ##__VA_ARGS__)
#define HHLOGF(expression, format, ...) __HHLOG(FATAL, expression, format, ##__VA_ARGS__)

#elif defined(HH_LOGGER_INFO)

#define HHLOGD(expression, format, ...) {}
#define HHLOGI(expression, format, ...) __HHLOG(INFO, expression, format, ##__VA_ARGS__)
#define HHLOGW(expression, format, ...) __HHLOG(WARN, expression, format, ##__VA_ARGS__)
#define HHLOGE(expression, format, ...) __HHLOG(ERROR, expression, format, ##__VA_ARGS__)
#define HHLOGF(expression, format, ...) __HHLOG(FATAL, expression, format, ##__VA_ARGS__)

#elif defined(HH_LOGGER_WARN)

#define HHLOGD(expression, format, ...) {}
#define HHLOGI(expression, format, ...) {}
#define HHLOGW(expression, format, ...) __HHLOG(WARN, expression, format, ##__VA_ARGS__)
#define HHLOGE(expression, format, ...) __HHLOG(ERROR, expression, format, ##__VA_ARGS__)
#define HHLOGF(expression, format, ...) __HHLOG(FATAL, expression, format, ##__VA_ARGS__)

#elif defined(HH_LOGGER_ERROR)

#define HHLOGD(expression, format, ...) {}
#define HHLOGI(expression, format, ...) {}
#define HHLOGW(expression, format, ...) {}
#define HHLOGE(expression, format, ...) __HHLOG(ERROR, expression, format, ##__VA_ARGS__)
#define HHLOGF(expression, format, ...) __HHLOG(FATAL, expression, format, ##__VA_ARGS__)

#elif defined(HH_LOGGER_FATAL)

#define HHLOGD(expression, format, ...) {}
#define HHLOGI(expression, format, ...) {}
#define HHLOGW(expression, format, ...) {}
#define HHLOGE(expression, format, ...) {}
#define HHLOGF(expression, format, ...) __HHLOG(FATAL, expression, format, ##__VA_ARGS__)

#else 

#define HHLOGD(expression, format, ...) {}
#define HHLOGI(expression, format, ...) {}
#define HHLOGW(expression, format, ...) {}
#define HHLOGE(expression, format, ...) {}
#define HHLOGF(expression, format, ...) {}

#endif
#endif  // HHLOG_H
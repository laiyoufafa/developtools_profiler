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

#ifndef LOGGING_H
#define LOGGING_H

#ifndef LOG_TAG
#define LOG_TAG ""
#endif

#ifdef HAVE_HILOG
#include "hilog/log.h"

#undef LOG_TAG
#define LOG_TAG "Hiprofiler"

#else
#include <mutex>
#include <string>
#include <securec.h>
#include <sys/syscall.h>
#include <stdarg.h>
#include <time.h>
#include <unistd.h>
#include <vector>

static inline long GetTid()
{
    return syscall(SYS_gettid);
}

enum {
    HILOG_UNKNOWN = 0,
    HILOG_DEFAULT,
    HILOG_VERBOSE,
    HILOG_DEBUG,
    HILOG_INFO,
    HILOG_WARN,
    HILOG_ERROR,
    HILOG_FATAL,
    HILOG_SILENT,
};

namespace {
constexpr int NS_PER_MS_LOG = 1000 * 1000;
}

static inline std::string GetTimeStr()
{
    char timeStr[64];
    struct timespec ts;
    struct tm tmStruct;
    clock_gettime(CLOCK_REALTIME, &ts);
    localtime_r(&ts.tv_sec, &tmStruct);
    size_t used = strftime(timeStr, sizeof(timeStr), "%m-%d %H:%M:%S", &tmStruct);
    snprintf_s(&timeStr[used], sizeof(timeStr) - used, sizeof(timeStr) - used - 1, ".%03ld",
               ts.tv_nsec / NS_PER_MS_LOG);
    return timeStr;
}

typedef const char *ConstCharPtr;

static inline int HiLogPrintArgs(int prio, ConstCharPtr tag, ConstCharPtr fmt, va_list vargs)
{
    static std::mutex mtx;
    static std::vector<std::string> prioNames = {"U", " ", "V", "D", "I", "W", "E", "F", "S"};
    std::unique_lock<std::mutex> lock(mtx);
    int count =
        fprintf(stderr, "%s %7d %7ld %5s %s ", GetTimeStr().c_str(), getpid(), GetTid(), prioNames[prio].c_str(), tag);
    count += vfprintf(stderr, fmt, vargs);
    count += fprintf(stderr, "\n");
    return count;
}

static inline int __hilog_log_print(int prio, ConstCharPtr tag, ConstCharPtr fmt, ...)
{
    int count;
    va_list vargs;

    va_start(vargs, fmt);
    count = HiLogPrintArgs(prio, tag, fmt, vargs);
    va_end(vargs);
    return count;
}

#define HILOG_DEBUG(LOG_CORE, fmt, ...) __hilog_log_print(HILOG_DEBUG, LOG_TAG, fmt, ##__VA_ARGS__)
#define HILOG_INFO(LOG_CORE, fmt, ...) __hilog_log_print(HILOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)
#define HILOG_WARN(LOG_CORE, fmt, ...) __hilog_log_print(HILOG_WARN, LOG_TAG, fmt, ##__VA_ARGS__)
#define HILOG_ERROR(LOG_CORE, fmt, ...) __hilog_log_print(HILOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)

#endif

#define STD_PTR(K, T) std::K##_ptr<T>

#define CHECK_NOTNULL(ptr, retval, fmt, ...)                                                                          \
    do {                                                                                                              \
        if (ptr == nullptr) {                                                                                         \
            HILOG_WARN(LOG_CORE, "CHECK_NOTNULL(%s) in %s:%d FAILED, " fmt, #ptr, __func__, __LINE__, ##__VA_ARGS__); \
            return retval;                                                                                            \
        }                                                                                                             \
    } while (0)

#define CHECK_TRUE(expr, retval, fmt, ...)                                                                          \
    do {                                                                                                            \
        if (!(expr)) {                                                                                              \
            HILOG_WARN(LOG_CORE, "CHECK_TRUE(%s) in %s:%d FAILED, " fmt, #expr, __func__, __LINE__, ##__VA_ARGS__); \
            return retval;                                                                                          \
        }                                                                                                           \
    } while (0)

#define RETURN_IF(expr, retval, fmt, ...)             \
    do {                                              \
        if ((expr)) {                                 \
            HILOG_WARN(LOG_CORE, fmt, ##__VA_ARGS__); \
            return retval;                            \
        }                                             \
    } while (0)

#endif

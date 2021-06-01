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

#ifndef BYTRACE_COMMON_TYPES_H
#define BYTRACE_COMMON_TYPES_H

#include <string>

namespace SysTuning {
namespace TraceStreamer {
enum ParseResult { ERROR = 0, SUCCESS };
enum RawType { RAW_CPU_IDLE = 1, RAW_SCHED_WAKEUP = 2, RAW_SCHED_WAKING = 3 };

enum Stat : uint32_t {
    RUNNABLE = 0,
    INTERRUPTABLESLEEP = 1,
    UNINTERRUPTIBLESLEEP = 2,
    STOPPED = 4,
    TRACED = 8,
    EXITDEAD = 16,
    EXITZOMBIE = 32,
    TASKDEAD = 64,
    WAKEKILL = 128,
    WAKING = 256,
    PARKED = 512,
    NOLOAD = 1024,
    TASKNEW = 2048,
    MAXSTAT = 2048,
    VALID = 0X8000,
};

struct BytraceLine {
    uint64_t ts = 0;
    uint32_t pid = 0;
    uint32_t cpu = 0;

    std::string task;    // thread name
    std::string pidStr;  // thread str
    std::string tGidStr; // process thread_group
    std::string eventName;
    std::string argsStr;
};

struct TracePoint {
    char phase_ = '\0';
    uint32_t tgid_ = 0;
    std::string name_;
    double value_ = 0;
    std::string categoryGroup_;
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // _BYTRACE_COMMON_TYPES_H_

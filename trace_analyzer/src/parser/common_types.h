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

#include <atomic>
#include <string>
#include <unordered_map>
#include "services/common_types.pb.h"
#include "types/plugins/ftrace_data/trace_plugin_result.pb.h"
#include "types/plugins/memory_data/memory_plugin_result.pb.h"

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
enum ParseStatus {
    Status_Init = 0,
    Status_Seprated = 1,
    Status_Parsing = 2,
    Status_Parsed = 3,
    Status_Invalid = 4
};
struct DataSegment {
    std::string seg;
    BytraceLine bufLine;
    std::unordered_map<std::string, std::string> args;
    uint32_t tgid;
    std::atomic<ParseStatus> status{Status_Init};
};
struct TracePoint {
    char phase_ = '\0';
    uint32_t tgid_ = 0;
    std::string name_ = "";
    uint64_t value_ = 0;
    std::string categoryGroup_ = "";
    // Distributed Data
    std::string chainId_ = "";
    std::string spanId_ = "";
    std::string parentSpanId_ = "";
    std::string flag_ = "";
    std::string args_ = "";
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // _BYTRACE_COMMON_TYPES_H_

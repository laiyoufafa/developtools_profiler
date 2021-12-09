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

#ifndef SRC_TRACE_BASE_TS_COMMON_H
#define SRC_TRACE_BASE_TS_COMMON_H

#include <limits>
#include <map>
#include <cstdint>
#include <string>

const uint64_t INVALID_UTID = std::numeric_limits<uint32_t>::max();
const uint64_t INVALID_UINT64 = std::numeric_limits<uint64_t>::max();
const uint64_t MAX_UINT32 = std::numeric_limits<uint32_t>::max();
const uint64_t MAX_UINT64 = std::numeric_limits<uint64_t>::max();
const uint32_t INVALID_UINT32 = std::numeric_limits<uint32_t>::max();
const uint32_t INVALID_INT32 = std::numeric_limits<int32_t>::max();
const uint64_t INVALID_DATAINDEX = std::numeric_limits<uint64_t>::max();
const size_t MAX_SIZE_T = std::numeric_limits<size_t>::max();
const uint32_t INVALID_ID = std::numeric_limits<uint32_t>::max();
const uint64_t SEC_TO_NS = 1000 * 1000 * 1000;
enum BuiltinClocks {
    TS_CLOCK_UNKNOW = 0,
    TS_CLOCK_BOOTTIME = 1,
    TS_CLOCK_REALTIME = 2,
    TS_CLOCK_REALTIME_COARSE = 3,
    TS_MONOTONIC = 4,
    TS_MONOTONIC_COARSE = 5,
    TS_MONOTONIC_RAW = 6,
};

enum RefType {
    K_REF_NO_REF = 0,
    K_REF_ITID = 1,
    K_REF_CPUID = 2,
    K_REF_IRQ = 3,
    K_REF_SOFT_IRQ = 4,
    K_REF_IPID = 5,
    K_REF_ITID_LOOKUP_IPID = 6,
    K_REF_MAX
};

enum EndState {
    // (R) ready state or running state, the process is ready to run, but not necessarily occupying the CPU
    TASK_RUNNABLE = 0,
    // (S) Indicates that the process is in light sleep, waiting for the resource state, and can respond to the signal.
    // Generally, the process actively sleeps into 'S' state.
    TASK_INTERRUPTIBLE = 1,
    // (D) Indicates that the process is in deep sleep, waiting for resources, and does not respond to signals.
    // Typical scenario: process acquisition semaphore blocking.
    TASK_UNINTERRUPTIBLE = 2,
    // (Running) Indicates that the thread is running
    TASK_RUNNING = 3,
    // (I) Thread in interrupt state
    TASK_INTERRUPTED = 4,
    // (X) Exit status, the process is about to be destroyed.
    TASK_EXIT_DEAD = 16,
    // (Z) Zombie state
    TASK_ZOMBIE = 32,
    // (I) clone thread
    TASK_CLONE = 64,
    // (K) Process killed
    TASK_KILLED = 128,
    // (DK)
    TASK_DK = 130,
    // the process is being debug now
    TASK_TRACED_KILL = 136,
    // (W) The process is in a deep sleep state and will be killed directly after waking up
    TASK_WAKEKILL = 256,
    // (R+) Process groups in the foreground
    TASK_FOREGROUND = 2048,
    TASK_MAX = 4096,
    TASK_INVALID = 9999
};
enum LogLevel {
    DEBUG = 68, // Debug
    ERROR = 69, // Error
    INFO = 73, // Info
    VERBOSE = 86, // Verbose
    WARN = 87 // Warn
};
enum SchedWakeType {
    SCHED_WAKING = 0, // sched_waking
    SCHED_WAKEUP = 1, // sched_wakeup
};
using DataIndex = uint64_t;
using TableRowId = uint64_t;
using InternalPid = uint32_t;
using InternalTid = uint32_t;
using InternalTime = uint64_t;
using FilterId = uint32_t;

enum BaseDataType {
    BASE_DATA_TYPE_INT,
    BASE_DATA_TYPE_STRING,
    BASE_DATA_TYPE_DOUBLE,
    BASE_DATA_TYPE_BOOLEAN
};
namespace SysTuning {
namespace TraceStreamer {
struct ArgsData {
    BaseDataType type;
    int64_t value;
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif

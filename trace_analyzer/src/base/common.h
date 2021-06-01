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

#ifndef SRC_TRACE_BASE_COMMON_H
#define SRC_TRACE_BASE_COMMON_H

#include <limits>
#include <map>
#include <cstdint>
#include <string>

const uint64_t INVALID_UTID = std::numeric_limits<uint32_t>::max();
const uint64_t INVALID_UINT64 = std::numeric_limits<uint64_t>::max();
const uint64_t MAX_UINT32 = std::numeric_limits<uint32_t>::max();
const uint64_t MAX_UINT64 = std::numeric_limits<uint64_t>::max();
const uint32_t INVALID_UINT32 = std::numeric_limits<uint32_t>::max();
const size_t MAX_SIZE_T = std::numeric_limits<size_t>::max();

enum RefType {
    K_REF_NO_REF = 0,
    K_REF_UTID = 1,
    K_REF_CPUID = 2,
    K_REF_IRQ = 3,
    K_REF_SOFT_IRQ = 4,
    K_REF_UPID = 5,
    K_REF_UTID_LOOKUP_UPID = 6,
    K_REF_MAX
};

enum EndState {
    TASK_RUNNABLE = 0,      // R 就绪态或者运行态，进程就绪可以运行，但是不一定正在占有CPU
    TASK_INTERRUPTIBLE = 1, // S 浅度睡眠，等待资源，可以响应信号，一般是进程主动sleep进入的状态
    TASK_UNINTERRUPTIBLE = 2, // D 深度睡眠，等待资源，不响应信号，典型场景是进程获取信号量阻塞
    TASK_RUNNING = 3,         // Running 线程处于运行状态
    TASK_INTERRUPTED = 4, // I 线程处于中断状态
    TASK_EXIT_DEAD = 16,  // X 退出状态，进程即将被销毁。
    TASK_ZOMBIE = 32, // Z 僵尸态，进程已退出或者结束，但是父进程还不知道，没有回收时的状态
    TASK_CLONE = 64,        // I 多线程，克隆线程
    TASK_KILLED = 128,      // K TASK DEAD 进程被杀死
    TASK_DK = 130,          // DK
    TASK_WAKEKILL = 256,    // W TASK_WAKEKILL 深度睡眠进程，唤醒后直接杀死
    TASK_FOREGROUND = 2048, // R+ 位于后台的进程组
    TASK_INVALID = 9999
};

enum SchedWakeType {
    SCHED_WAKING = 0, // sched_waking
    SCHED_WAKEUP = 1, // sched_wakeup
};

using DataIndex = size_t;
using TableRowId = uint64_t;
using InternalPid = uint32_t;
using InternalTid = uint32_t;
using InternalTime = uint64_t;

#define STACK_HASK_COUNT 2

#endif

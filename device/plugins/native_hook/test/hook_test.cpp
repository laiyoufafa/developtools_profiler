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
#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <new>
#include <pthread.h>
#include <sys/syscall.h>
#include <unistd.h>

#pragma clang optimize off


namespace {
constexpr int MALLOC_SIZE = 1000;
constexpr int TIME_BASE = 1000;
constexpr int DATA_SIZE = 200;
constexpr int SLEEP_TIME = 200;
constexpr int ARGC_NUM_MAX = 4;
constexpr int ARGC_NUM_MUST = 3;
constexpr int ARGC_MALLOC_TIMES = 2;
constexpr int ARGC_STICK_DEPTH = 3;
unsigned int g_stickDepth = 100;

using StaticSpace = struct {
    int data[DATA_SIZE];
};

class Timer {
public:
    using Clock = std::chrono::steady_clock;
    using TimePoint = Clock::time_point;

    Timer() : startTime_(Now()) {}

    ~Timer() {}

    long ElapsedUs()
    {
        auto currentTime = Now();
        return std::chrono::duration_cast<std::chrono::microseconds>(currentTime - startTime_).count();
    }

    void Reset()
    {
        startTime_ = Now();
    }

protected:
    TimePoint Now()
    {
        return Clock::now();
    }

private:
    TimePoint startTime_;
};

char *DepthMalloc(int depth, int mallocSize)
{
    if (mallocSize <= 0) {
        return nullptr;
    }
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return new char[mallocSize];
    }
    return (DepthMalloc(depth - 1, mallocSize));
}

void DepthFree(int depth, char *p)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        delete []p;
        return;
    }
    return (DepthFree(depth - 1, p));
}

void* thread_func_cpp(void* param)
{
    char *p = nullptr;
    long tid = syscall(SYS_gettid);
    printf("start thread %ld\n", tid);
    int times = *static_cast<int*>(param);
    int idx = 0;
    while (idx < times) {
        p = DepthMalloc(g_stickDepth, MALLOC_SIZE);
        if (idx % TIME_BASE == 0) {
            printf("thread %ld malloc %d times\n", tid, idx);
        }
        if (p) {
            DepthFree(g_stickDepth, p);
        }
        idx++;
    }
    return nullptr;
}
} // namespace

int ThreadTimeCost(int threadNum, int mallocTimes) {
    Timer timer = {};
    pthread_t* thr_array = new (std::nothrow) pthread_t[threadNum];
    if (!thr_array) {
        printf("new thread array failed.\n");
        return 1;
    }
    int idx;
    for (idx = 0; idx < threadNum; ++idx) {
        if (pthread_create(thr_array + idx, nullptr, thread_func_cpp, static_cast<void*>(&mallocTimes)) != 0) {
            printf("Creating thread failed.\n");
        }
    }
    for (idx = 0; idx < threadNum; ++idx) {
        pthread_join(thr_array[idx], nullptr);
    }
    delete []thr_array;
    auto timeCost = timer.ElapsedUs();
    printf("Before hook, time cost %ldus.\nAfter hook test sleeping 200 ......., please send signal\n", timeCost);
    sleep(SLEEP_TIME);
    printf("Hook test start\n");
    Timer hooktimer = {};
    pthread_t* thr_array_hook = new (std::nothrow) pthread_t[threadNum];
    if (!thr_array_hook) {
        printf("new thread lock array failed.\n");
        return 1;
    }
    for (idx = 0; idx < threadNum; ++idx) {
        if (pthread_create(thr_array_hook + idx, nullptr, thread_func_cpp, static_cast<void*>(&mallocTimes)) !=
            0) {
            printf("Creating thread failed.\n");
        }
    }
    for (idx = 0; idx < threadNum; ++idx) {
        pthread_join(thr_array_hook[idx], nullptr);
    }
    delete []thr_array_hook;
    auto hookCost = hooktimer.ElapsedUs();
    printf("After hook, time cost %ldus.\nPerformance test finish!", hookCost);
    return 0;
}

int main(int argc, char *argv[])
{
    int threadNum = 1;
    int mallocTimes = 0;
    if  (argc >= ARGC_NUM_MUST) {
        if (atoi(argv[1]) > 0) {
            threadNum = atoi(argv[1]);
        }
        mallocTimes = atoi(argv[ARGC_MALLOC_TIMES]);
        if (argc == ARGC_NUM_MAX) {
            g_stickDepth = atoi(argv[ARGC_STICK_DEPTH]);
        }
    } else {
        printf("command error\n");
        return 0;
    }
    printf("Test start %d thread, malloc %d times\n", threadNum, mallocTimes);
    if (!ThreadTimeCost(threadNum, mallocTimes)) {
        printf("Test success end!\n");
    } else {
        printf("Test failure end!\n");
    }
}

#pragma clang optimize on
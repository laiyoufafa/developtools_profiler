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

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <pthread.h>
#include <time.h>
#pragma clang optimize off

#define DEFAULT_THREAD_NUM 1
#define DEFAULT_MALLOC_SIZE 100
#define DEFAULT_REALLOC_SIZE 100
#define SLEEP_TIME_SEC 1
#define TEST_BRANCH_NUM 3
#define ARG_CASE_NUM_THREADNUM 3
#define ARG_CASE_MALLOCSIZE 2
#define ARG_THREADNUM 2
#define STATIC_DEPTH 5
#define DATA_SIZE 50

typedef struct {
    int data[DATA_SIZE];
} StaticSpace;

static int g_runing = 1;
static double g_mallocDuration = 0;
static double g_callocDuration = 0;
static double g_reallocDuration = 0;
static double g_freeDuration = 0;

char *DepthMalloc(int depth, int mallocSize)
{
    if (mallocSize <= 0) {
        return NULL;
    }
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return (char *)malloc(mallocSize);
    }
    return (DepthMalloc(depth - 1, mallocSize));
}

char *DepthCalloc(int depth, int callocSize)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return (char *)calloc(sizeof(char), callocSize);
    }
    return (DepthCalloc(depth - 1, callocSize));
}

char *DepthRealloc(int depth, void *p, int reallocSize)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return (char *)realloc(p, reallocSize);
    }
    return (DepthRealloc(depth - 1, p, reallocSize));
}

void DepthFree(int depth, void *p)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        free(p);
        return;
    }
    return (DepthFree(depth - 1, p));
}

void ApplyForMalloc(int mallocSize)
{
    printf("\nstart malloc apply (size = %d)\n", mallocSize);
    clock_t timerStart, timerStop;
    double duration = 0;
    timerStart = clock();
    char *p = DepthMalloc(STATIC_DEPTH, mallocSize);
    timerStop = clock();
    if (!p) {
        printf("malloc failure\n");
        return;
    }
    duration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_mallocDuration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("malloc success, malloc (%d) time is %f\n", mallocSize, duration);
    printf("\nReady for free -- ");
    timerStart = clock();
    DepthFree(STATIC_DEPTH, p);
    timerStop = clock();
    duration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_freeDuration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("free success, free time is %f\n", (double)(timerStop - timerStart) / CLOCKS_PER_SEC);
    printf("malloc apply success, total time is %f\n", duration);
}

void ApplyForCalloc(int mallocSize)
{
    int callocSize = mallocSize / sizeof(char);
    printf("\nstart calloc apply (size = %d)\n", callocSize);
    clock_t timerStart, timerStop;
    double duration = 0;
    timerStart = clock();
    char *p = DepthCalloc(STATIC_DEPTH, callocSize);
    timerStop = clock();
    if (!p) {
        printf("calloc failure\n");
        return;
    }
    duration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_callocDuration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("calloc success, calloc (%d) time is %f\n", callocSize, duration);
    printf("\nReady for free -- ");
    timerStart = clock();
    DepthFree(STATIC_DEPTH, p);
    timerStop = clock();
    duration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_freeDuration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("free success, free time is %f\n", (double)(timerStop - timerStart) / CLOCKS_PER_SEC);
    printf("calloc apply success, total time is %f\n", duration);
}

void ApplyForRealloc(int mallocSize)
{
    int reallocSize = mallocSize * DEFAULT_REALLOC_SIZE;
    printf("\nstart realloc apply (size = %d)\n", reallocSize);
    if (mallocSize <= 0) {
        printf("Invalid mallocSize.\n");
        return;
    }
    clock_t timerStart, timerStop;
    double duration = 0;
    char *p = (char *)malloc(mallocSize);
    if (!p) {
        printf("malloc failure\n");
        return;
    }
    timerStart = clock();
    char *np = DepthRealloc(STATIC_DEPTH, p, reallocSize);
    timerStop = clock();
    if (!np) {
        free(p);
        printf("realloc failure\n");
        return;
    }
    duration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_reallocDuration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("realloc success, realloc (%d) time is %f\n", reallocSize, duration);
    printf("\nReady for free -- ");
    timerStart = clock();
    DepthFree(STATIC_DEPTH, np);
    timerStop = clock();
    duration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_freeDuration += (double)(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("free success, free time is %f\n", (double)(timerStop - timerStart) / CLOCKS_PER_SEC);
    printf("realloc apply success, total time is %f\n", duration);
}

void* ThreadFuncC(void* param)
{
    int mallocCount = 0;
    int callocCount = 0;
    int reallocCount = 0;
    int freeCount = 0;
    int randNum = 0;
    int tid = syscall(SYS_gettid);
    int mallocSize = *(int*)param;
    printf("start thread %d\n", tid);
    time_t tv = time(NULL);
    if (tv == -1) {
        tv = 1;
    }
    unsigned int seed = (unsigned int)tv;
    while (g_runing) {
        randNum = rand_r(&seed) % TEST_BRANCH_NUM;
        if (randNum == 0) {
            ApplyForMalloc(mallocSize);
            mallocCount++;
        } else if (randNum == 1) {
            ApplyForCalloc(mallocSize);
            callocCount++;
        } else {
            ApplyForRealloc(mallocSize);
            reallocCount++;
        }
        freeCount++;
        sleep(SLEEP_TIME_SEC);
    }
    printf("thread %d  malloc count[%d] totalTime[%f] meanTime[%f].\n", tid,
        mallocCount, g_mallocDuration, g_mallocDuration / mallocCount);
    printf("thread %d  calloc count[%d] totalTime[%f] meanTime[%f].\n", tid,
        callocCount, g_callocDuration, g_callocDuration / callocCount);
    printf("thread %d realloc count[%d] totalTime[%f] meanTime[%f].\n", tid,
        reallocCount, g_reallocDuration, g_reallocDuration / reallocCount);
    printf("thread %d    free count[%d] totalTime[%f] meanTime[%f].\n", tid,
        freeCount, g_freeDuration, g_freeDuration / freeCount);
    printf("finish thread %d\n", tid);
    return NULL;
}
#define INVALID_THREAD_NUM_RET 1
#define INVALID_MALLOC_SIZE_RET 2
int main(int argc, char *argv[])
{
    int threadNum = DEFAULT_THREAD_NUM;
    int mallocSize = DEFAULT_MALLOC_SIZE;
    switch (argc) {
        case ARG_CASE_NUM_THREADNUM:
            threadNum = atoi(argv[ARG_THREADNUM]);
            mallocSize = atoi(argv[1]);
            break;
        case ARG_CASE_MALLOCSIZE:
            mallocSize = atoi(argv[1]);
            break;
        case 1:
            break;
        default:
            printf("Usage: nativetest_c <mallocSize> <threadNum>\n");
            return 0;
    }
    if (threadNum <= 0) {
        printf("Invalid threadNum.\n");
        return INVALID_THREAD_NUM_RET;
    }
    if (mallocSize <= 0) {
        printf("Invalid mallocSize\n");
        return INVALID_MALLOC_SIZE_RET;
    }
    pid_t pid = getpid();
    printf("Process pid %d, Test start %d thread, malloc %d size\n", pid, threadNum, mallocSize);

    pthread_t* thrArray = (pthread_t*)malloc(sizeof(pthread_t) * threadNum);
    if (thrArray == nullptr) {
        printf("new thread failed.\n");
    }
    int idx;
    for (idx = 0; idx < threadNum; ++idx) {
        if (pthread_create(thrArray + idx, NULL, ThreadFuncC, (void*)(&mallocSize))) {
            printf("Creating thread failed.\n");
        }
    }
    while (getchar() != '\n') {};
    g_runing = 0;

    for (idx = 0; idx < threadNum; ++idx) {
        pthread_join(thrArray[idx], NULL);
    }
    free(thrArray);
    printf("Exit Process (pid %d)\n", pid);
    return 0;
}

#pragma clang optimize on

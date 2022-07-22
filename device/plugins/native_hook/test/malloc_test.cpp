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

#include <fcntl.h>
#include <pthread.h>
#include <cstdio>
#include <cstdlib>
#include <string>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <ctime>
#include <unistd.h>
#include "memory_tag.h"
#include "securec.h"
#pragma clang optimize off

#define PAGE_SIZE 4096
#define SLEEP_TIME_SEC 1
#define RESPONSE_SPEED 300
#define DATA_SIZE 50
#define ALLOC_FLAG (1 << 0)
#define MMAP_FLAG (1 << 1)

const int DEFAULT_REALLOC_SIZE = 100;
const int TEST_BRANCH_NUM = 3;
const int STATIC_DEPTH = 5;

typedef struct {
    int data[DATA_SIZE];
} StaticSpace;

static double g_mallocDuration = 0;
static double g_callocDuration = 0;
static double g_reallocDuration = 0;
static double g_freeDuration = 0;

static int g_fd = -1;
static int g_runing = 1;
static int g_threadNum = 1;
static int g_mallocSize = 1;
static const char* g_fileName = "./mmapTest";
static unsigned int g_hook_flag = 0;

static char* DepthMalloc(int depth, int mallocSize)
{
    if (mallocSize <= 0) {
        return nullptr;
    }
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return (char*)malloc(mallocSize);
    }
    return (DepthMalloc(depth - 1, mallocSize));
}

static char* DepthCalloc(int depth, int callocSize)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return (char*)calloc(sizeof(char), callocSize);
    }
    return (DepthCalloc(depth - 1, callocSize));
}

static char* DepthRealloc(int depth, void* p, int reallocSize)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        return (char*)realloc(p, reallocSize);
    }
    return (DepthRealloc(depth - 1, p, reallocSize));
}

static void DepthFree(int depth, void* p)
{
    StaticSpace staticeData;
    if (depth == 0) {
        staticeData.data[0] = 1;
        free(p);
        return;
    }
    return (DepthFree(depth - 1, p));
}

static void ApplyForMalloc(int mallocSize)
{
    printf("\nstart malloc apply (size = %d)\n", mallocSize);
    clock_t timerStart, timerStop;
    double duration = 0;
    timerStart = clock();
    char* p = DepthMalloc(STATIC_DEPTH, mallocSize);
    timerStop = clock();
    if (!p) {
        printf("malloc failure\n");
        return;
    }
    duration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_mallocDuration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("malloc success, malloc (%d) time is %f\n", mallocSize, duration);
    printf("\nReady for free -- ");
    timerStart = clock();
    DepthFree(STATIC_DEPTH, p);
    timerStop = clock();
    duration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_freeDuration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("free success, free time is %f\n", static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC);
    printf("malloc apply success, total time is %f\n", duration);
}

static void ApplyForCalloc(int mallocSize)
{
    int callocSize = mallocSize / sizeof(char);
    printf("\nstart calloc apply (size = %d)\n", callocSize);
    clock_t timerStart, timerStop;
    double duration = 0;
    timerStart = clock();
    char* p = DepthCalloc(STATIC_DEPTH, callocSize);
    timerStop = clock();
    if (!p) {
        printf("calloc failure\n");
        return;
    }
    duration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_callocDuration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("calloc success, calloc (%d) time is %f\n", callocSize, duration);
    printf("\nReady for free -- ");
    timerStart = clock();
    DepthFree(STATIC_DEPTH, p);
    timerStop = clock();
    duration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_freeDuration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("free success, free time is %f\n", static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC);
    printf("calloc apply success, total time is %f\n", duration);
}

static void ApplyForRealloc(int mallocSize)
{
    int reallocSize = mallocSize * DEFAULT_REALLOC_SIZE;
    printf("\nstart realloc apply (size = %d)\n", reallocSize);
    if (mallocSize <= 0) {
        printf("Invalid mallocSize.\n");
        return;
    }
    clock_t timerStart, timerStop;
    double duration = 0;
    char* p = (char*)malloc(mallocSize);
    if (!p) {
        printf("malloc failure\n");
        return;
    }
    timerStart = clock();
    char* np = DepthRealloc(STATIC_DEPTH, p, reallocSize);
    timerStop = clock();
    if (!np) {
        free(p);
        printf("realloc failure\n");
        return;
    }
    duration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_reallocDuration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("realloc success, realloc (%d) time is %f\n", reallocSize, duration);
    printf("\nReady for free -- ");
    timerStart = clock();
    DepthFree(STATIC_DEPTH, np);
    timerStop = clock();
    duration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    g_freeDuration += static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC;
    printf("free success, free time is %f\n", static_cast<double>(timerStop - timerStart) / CLOCKS_PER_SEC);
    printf("realloc apply success, total time is %f\n", duration);
}

static void NewString()
{
    std::string* sp = new std::string("hello world");
    printf("string  sp = %s\n", sp->c_str());
    delete sp;
}

static void UniqueString()
{
    auto pName = std::make_unique<std::string>("Hello");
}


static void* ThreadFuncC(void* param)
{
    int mallocCount = 0;
    int callocCount = 0;
    int reallocCount = 0;
    int freeCount = 0;
    int randNum = 0;
    int tid = syscall(SYS_gettid);
    int mallocSize = *(int*)param;
    printf("start thread %d\n", tid);
    time_t tv = time(nullptr);
    if (tv == -1) {
        tv = 1;
    }
    unsigned int seed = static_cast<unsigned int>tv;
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

        NewString();
        UniqueString();
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

    return nullptr;
}

// 打开文件到内存中
static int OpenFile(const char* fileName)
{
    int fd = open(fileName, O_RDWR | O_CREAT, static_cast<mode_t>0777);
    if (fd == -1) {
        printf("can not open the file\n");
        return -1;
    }
    return fd;
}

// 关闭文件
static void CloseFile(void)
{
    if (g_fd > 0) {
        close(g_fd);
        g_fd = -1;
    }
}

// 给文件建立内存映射
static char* CreateMmap(void)
{
    if (g_fd == -1) {
        return nullptr;
    }

    int size = PAGE_SIZE;
    lseek(g_fd, size + 1, SEEK_SET);
    write(g_fd, "", 1);

    char* pMap = (char*)mmap(nullptr, PAGE_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, g_fd, 0);

    const char *tag = "memtesttag";
    MEM_TYPESET(pMap, PAGE_SIZE, tag, strlen(tag)+1);
    if (pMap == MAP_FAILED) {
        printf("mmap fail\n");
        CloseFile();
    }
    return pMap;
}

// 关闭文件内存映射
static void RemoveMmap(char* pMap)
{
    munmap(pMap, PAGE_SIZE);
}

// 给文件映射中写入
static void MmapWriteFile(char* pMap, int length, char* data)
{
    if (memcpy_s(pMap, length, data, length) != EOK) {
        printf("memcpy_s type fail\n");
        return;
    }
    msync(pMap, length, MS_SYNC);
}

// 从文件映射中读取
static char* MmapReadFile(char* pMap, int length)
{
    if (length <= 0) {
        printf("fail:malloc %d memory", length);
        return nullptr;
    }
    char* data = (char*)malloc(length + 1);
    if (data != nullptr) {
        memcpy_s(data, length, pMap, length);
        data[length] = '\0';
    }
    return data;
}

static void RandSrand(void)
{
    srand(static_cast<unsigned>time(nullptr));
}

// 10 ~ 4096
static int RandInt(int Max, int Min)
{
    int value = (rand() % (Max - Min)) + Min;
    return value;
}

// 生成一个随机字符 (0x20 ~ 0x7E)
static char RandChar(void)
{
    // 可显示字符的范围
    int section = '~' - ' ';
    int randSection = RandInt(0, section);
    char randChar = '~' + randSection;
    return randChar;
}

// 获取随机长度的字符串
static char* RandString(int maxLength)
{
    int strLength = RandInt(10, maxLength);
    if (strLength <= 0) {
        printf("fail:malloc %d memory", strLength);
        return nullptr;
    }
    char* data = (char*)malloc(strLength + 1);
    if (data != nullptr) {
        for (int i = 0; i < strLength; i++) {
            data[i] = RandChar();
        }
    data[strLength] = '\0';
    }
    return data;
}

// 初始化函数
static void mmapInit(void)
{
    // 设置随机种子
    RandSrand();
    // 设置全局映射的目标文件
    g_fd = OpenFile(g_fileName);
}

// 写映射
static void WriteMmap(char* data)
{
    // 建立映射
    char* pMap = CreateMmap();
    // 写入
    MmapWriteFile(pMap, strlen(data), data);

    // 关闭映射
    RemoveMmap(pMap);
}

// 读映射
static char* ReadMmap(int length)
{
    // 建立映射
    char* pMap = CreateMmap();

    // 写入
    char* outTestchar = MmapReadFile(pMap, length);

    // 关闭映射
    RemoveMmap(pMap);

    return outTestchar;
}

static void* ThreadMmap(void* param)
{
    while (g_runing) {
        // 获取随机字符
        char* randString = RandString(PAGE_SIZE);

        // 写入映射
        WriteMmap(randString);

        // 从映射中读取
        char* outchar = ReadMmap(strlen(randString));
        printf("thread %ld : Mmap test OK! \n", syscall(SYS_gettid));
        free(randString);
        free(outchar);
        sleep(SLEEP_TIME_SEC);
    }
    return nullptr;
}

// 维护hook test类型管理
static int bitMapNum(unsigned int data)
{
    unsigned int tmp = data;
    int num = 0;
    while (tmp) {
        if (tmp & 1) {
            num++;
        }
        tmp >>= 1;
    }
    return num;
}

// 参数解析
static int CommandParse(int argc, char** argv)
{
    int result;
    opterr = 0;
    while ((result = getopt(argc, argv, "t:s:n:o:h:")) != -1) {
        switch (result) {
            case 't':
                // hook test的类型
                if (!strcmp("mmap", optarg)) {
                    printf("Type: %s \n", optarg);
                    g_hook_flag |= MMAP_FLAG;
                } else if (!strcmp("alloc", optarg)) {
                    printf("Type: %s \n", optarg);
                    g_hook_flag |= ALLOC_FLAG;
                } else if (!strcmp("all", optarg)) {
                    printf("Type: %s \n", optarg);
                    g_hook_flag |= ALLOC_FLAG;
                    g_hook_flag |= MMAP_FLAG;
                }
                break;
            case 's':
                // 栈大小
                g_mallocSize = atoi(optarg);
                if (g_mallocSize <= 0) {
                    printf("Invalid mallocSize\n");
                    return -1;
                }
                break;
            case 'n':
                // 线程数
                g_threadNum = atoi(optarg);
                if (g_threadNum <= 0) {
                    printf("Invalid threadNum.\n");
                    return -1;
                }
                break;
            case 'o':
                g_fileName = optarg;
                break;
            case 'h':
            default:
                printf("%s -t <alloc/mmap>\n", argv[0]);
                printf("\talloc : -s [alloc mallocSize] -n [thread Num]\n");
                printf("\t mmap : -o [mmap datafile]\n");
                return -1;
        }
    }
    return opterr;
}

int main(int argc, char* argv[])
{
    // 参数解析
    int ret = CommandParse(argc, argv);
    if (ret == -1) {
        return 0;
    }
    int typeNum = bitMapNum(g_hook_flag);
    printf(" g_hook_flag =  [%u] \n", g_hook_flag);
    if (typeNum == 0) {
        // 未设置type时默认启动alloc
        g_hook_flag |= ALLOC_FLAG;
        typeNum++;
    }

    pthread_t** thrArrayList = (pthread_t**)malloc(sizeof(pthread_t*) * typeNum);
    if (thrArrayList == nullptr) {
        printf("malloc thrArrayList fail\n");
        return 0;
    }
    int type = 0;
    if (g_hook_flag & ALLOC_FLAG) {
        int threadNum = g_threadNum;
        int mallocSize = g_mallocSize;

        pid_t pid = getpid();
        printf("Process pid %d, Test start %d thread, malloc %d size\n", pid, threadNum, mallocSize);

        thrArrayList[type] = (pthread_t*)malloc(sizeof(pthread_t) * threadNum);
        // pthread_t* thrArray
        if (thrArrayList[type] == nullptr) {
            printf("new thread failed.\n");
        }
        int idx;
        for (idx = 0; idx < threadNum; ++idx) {
            if (pthread_create((thrArrayList[type]) + idx, nullptr, ThreadFuncC, static_cast<void*>(&mallocSize))) {
                printf("Creating thread failed.\n");
            }
        }
        type++;
    }

    if (g_hook_flag & MMAP_FLAG) {
        int threadNum = g_threadNum;
        // 初始化
        mmapInit();

        thrArrayList[type] = (pthread_t*)malloc(sizeof(pthread_t) * threadNum);
        if (thrArrayList[type] == nullptr) {
            printf("new thread failed.\n");
        }

        int idx;
        for (idx = 0; idx < threadNum; ++idx) {
            if (pthread_create((thrArrayList[type]) + idx, nullptr, ThreadMmap, nullptr)) {
                printf("Creating thread failed.\n");
            }
        }
    }

    while (getchar() != '\n') {
        usleep(RESPONSE_SPEED);
    };
    g_runing = 0;
    int idx;
    for (type = 0; type < typeNum; type++) {
        for (idx = 0; idx < g_threadNum; ++idx) {
            pthread_join((thrArrayList[type])[idx], nullptr);
        }
        if (thrArrayList[type] != nullptr) {
            free(thrArrayList[type]);
            thrArrayList[type] = nullptr;
        }
    }
    if (thrArrayList != nullptr) {
        free(thrArrayList);
        thrArrayList = nullptr;
    }
    CloseFile();
    return 0;
}

#pragma clang optimize on

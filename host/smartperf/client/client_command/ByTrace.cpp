/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <iostream>
#include <pthread.h>
#include "include/ByTrace.h"

namespace OHOS {
namespace SmartPerf {
void *ByTrace::thread_get_trace(void *arg)
{
    char cmd_trace[100];
    GPUtils::safeSprintf(cmd_trace, "bytrace --trace_begin --overwrite");
    std::string res = GPUtils::readFile(cmd_trace);
    pthread_exit(nullptr);
    return nullptr;
}
void *ByTrace::thread_finish_trace(void *pathName)
{
    char cmd_trace_finish[64];
    char cmd_trace_overwrite[256];
    std::string fileName = *(std::string *)pathName;
    std::cout << "ByTrace::thread_finish_trace:called fileName:" << fileName << std::endl;
    GPUtils::safeSprintf(cmd_trace_finish, "bytrace --trace_finish");
    GPUtils::safeSprintf(cmd_trace_overwrite, 
        "bytrace --overwrite sched ace app disk ohos graphic sync workq ability > /data/mynewtrace%ss.ftrace", 
        fileName.c_str()
    );    
    GPUtils::readFile(cmd_trace_finish);
    GPUtils::readFile(cmd_trace_overwrite);
    pthread_exit(nullptr);
    return nullptr;
}

TraceStatus ByTrace::init_trace(bool isStart)
{
    if (isStart) {
        return TRACE_START;
    } else {
        return TRACE_NO;
    }
}

TraceStatus ByTrace::check_fps_jitters(std::vector<long long> jitters, int curProfilerNum)
{
    if (curNum <= sum) {
        for (size_t i = 0; i < jitters.size(); i++) {
            long long normalJitter = jitters[i] / 1e6;
            if (normalJitter > threshold) {
                if ((flagProfilerNum != -1) && curProfilerNum < (flagProfilerNum + interval)) {
                    // 如果不是第一次抓取 并且 小于抓取周期间隔 则放弃抓取
                    return TRACE_NO;
                }
                curNum++;
                flagProfilerNum = curProfilerNum;
                std::cout << "***************************************************************************" << std::endl;
                std::cout << "***************************************************************************" << std::endl;
                std::cout << "*************  ByTrace::getTrace:curJitter:" << normalJitter << "*******************" <<
                    std::endl;
                std::cout << "***************************************************************************" << std::endl;
                std::cout << "***************************************************************************" << std::endl;
                return TRACE_FINISH;
            }
        }
    }
    return TRACE_NO;
}
}
}

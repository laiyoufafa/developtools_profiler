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
#ifndef BY_TRACE_H
#define BY_TRACE_H
#include "gp_utils.h"
#include "singleton.h"
namespace OHOS {
namespace SmartPerf {
enum TraceStatus {
    TRACE_START,
    TRACE_FINISH,
    TRACE_NO
};
class ByTrace : public DelayedSingleton<ByTrace> {
public:
    // 开始抓trace线程
    static void *thread_get_trace(void *arg);
    // 结束抓trace线程
    static void *thread_finish_trace(void *pathName);
    // 初始化抓取
    TraceStatus init_trace(bool isStart);
    // 校验fps-jitters
    TraceStatus check_fps_jitters(std::vector<long long> jitters, int curProfilerNum);
    // 抓trace总次数 默认2次
    int sum = 2;
    // 当前触发的次数
    int curNum = 0;

private:
    // 抓trace间隔 默认60s
    int interval = 60;
    // 当前触发标记次数
    int flagProfilerNum = -1;
    // 抓trace触发条件:默认 某一帧的某个jitter>100 ms触发
    long long threshold = 100;
};
}
}
#endif
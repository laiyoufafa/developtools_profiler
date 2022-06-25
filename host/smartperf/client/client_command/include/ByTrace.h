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
#include "common.h"
namespace OHOS {
namespace SmartPerf {
class ByTrace {
public:
    static ByTrace &GetInstance()
    {
        static ByTrace instance;
        return instance;
    }
    // trace配置
    void setTraceConfig(int mSum, int mInterval, long long mThreshold);
    // 开始抓trace线程
    void threadGetTrace();
    // 结束抓trace线程
    void threadFinishTrace(std::string &pathName);
    // 校验fps-jitters
    TraceStatus checkFpsJitters(std::vector<long long> jitters);
    // 触发trace
    void TriggerCatch(std::vector<long long> jitters, long long curTime);

private:
    ByTrace() {};
    ByTrace(const ByTrace &);
    ByTrace &operator = (const ByTrace &);

    // 抓trace总次数 默认2次
    int sum = 2;
    // 当前触发的次数
    int curNum = 0;
    // 抓trace间隔(两次抓取的间隔时间 默认60*1000 ms)
    int interval = 60000;
    // 抓trace耗时(start 到 finish的预留时间 默认10*1000 ms)
    int cost = 10000;
    // 抓trace触发条件:默认 某一帧的某个jitter>100 ms触发
    long long threshold = 100;
    // 上一次触发时间
    long long lastTriggerTime = -1;
    // 当前是否触发
    long long curTriggerFlag = -1;
};
}
}
#endif
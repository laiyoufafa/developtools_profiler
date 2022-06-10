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
#ifndef PROFILER_H
#define PROFILER_H
#include <iostream>
#include <map>
#include "CPU.h"
#include "GPU.h"
#include "DDR.h"
#include "FPS.h"
#include "RAM.h"
#include "Temperature.h"
#include "Power.h"
#include "gp_data.h"

namespace OHOS {
namespace SmartPerf {
class Profiler : public DelayedSingleton<Profiler>{
public:
    void initProfiler();
    void createCpu(std::map<std::string, std::string> &gpMap);
    void createGpu(std::map<std::string, std::string> &gpMap);
    void createDdr(std::map<std::string, std::string> &gpMap);
    void createFps(int isVideo, int isCamera, int isCatchTrace, int curProfilerNum,
        std::map<std::string, std::string> &gpMap);
    void createTemp(std::map<std::string, std::string> &gpMap);
    void createPower(std::map<std::string, std::string> &gpMap);
    void createRam(const std::string &pkg_name, std::map<std::string, std::string> &gpMap, int pid);
    void createSnapshot(std::map<std::string, std::string> &gpMap, long long timestamp);
    std::shared_ptr<CPU> mCpu = nullptr;
    std::shared_ptr<GPU> mGpu = nullptr;
    std::shared_ptr<DDR> mDdr = nullptr;
    std::shared_ptr<FPS> mFps = nullptr;
    std::shared_ptr<RAM> mRam = nullptr;
    std::shared_ptr<Temperature> mTemperature = nullptr;
    std::shared_ptr<Power> mPower = nullptr;
};
}
}
#endif
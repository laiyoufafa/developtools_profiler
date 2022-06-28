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
#ifndef CPU_H
#define CPU_H
#include <iostream>
#include <vector>
#include "sp_profiler.h"
namespace OHOS {
namespace SmartPerf {
class CPU : public SpProfiler {
public:
    static CPU &GetInstance()
    {
        static CPU instance;
        return instance;
    }
    std::map<std::string, std::string> ItemData() override;

    int getCpuNum();
    int getCpuFreq(int cpuId);
    std::vector<float> getCpuLoad();

private:
    CPU() {};
    CPU(const CPU &);
    CPU &operator = (const CPU &);

    const std::string CpuBasePath = "/sys/devices/system/cpu";
    const std::string ProcStat = "/proc/stat";
    inline const std::string CpuScalingCurFreq(int CPUID)
    {
        return CpuBasePath + "/cpu" + std::to_string(CPUID) + "/cpufreq/scaling_cur_freq";
    }

    int mCpuNum = -1;
    float cacWorkload(const char *buffer, const char *pre_buffer);
};
}
}
#endif
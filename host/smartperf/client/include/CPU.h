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
#include <string>
#include <sstream>
#include <map>
#include <cstdlib>
#include "gp_utils.h"
namespace OHOS {
namespace SmartPerf {
class CPU {
public:
    int get_cpu_num();
    int get_cpu_freq(int cpu_id);
    std::vector<float> get_cpu_load();

    static CPU *getInstance();
    static pthread_mutex_t mutex;

private:
    const std::map<std::string, std::string> support_map = {
        { "Frequency", "/sys/devices/system/cpu" },
        { "Load", "/proc/stat" },
    };
    const std::string CPU_BASE_PATH = "/sys/devices/system/cpu";
    const std::string PROC_STAT = "/proc/stat";
    inline const std::string CPU_SCALING_CUR_FREQ(int CPUID)
    {
        return CPU_BASE_PATH + "/cpu" + std::to_string(CPUID) + "/cpufreq/scaling_cur_freq";
    }
    inline const std::string CPU_SCALING_MAX_FREQ(int CPUID)
    {
        return CPU_BASE_PATH + "/cpu" + std::to_string(CPUID) + "/cpufreq/scaling_max_freq";
    }
    inline const std::string CPU_SCALING_MIN_FREQ(int CPUID)
    {
        return CPU_BASE_PATH + "/cpu" + std::to_string(CPUID) + "/cpufreq/scaling_min_freq";
    }
    inline const std::string CPUINFO_MAX_FREQ(int CPUID)
    {
        return CPU_BASE_PATH + "/cpu" + std::to_string(CPUID) + "/cpufreq/cpuinfo_max_freq";
    }
    inline const std::string CPUINFO_MIN_FREQ(int CPUID)
    {
        return CPU_BASE_PATH + "/cpu" + std::to_string(CPUID) + "/cpufreq/cpuinfo_min_freq";
    }

    CPU();
    ~CPU();

    static CPU *instance;
    int m_cpu_num;
    float cac_workload(const char *buffer, const char *pre_buffer) const;
};
}
}
#endif
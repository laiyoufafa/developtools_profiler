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

#ifndef GPU_H
#define GPU_H
#include <string>
#include <sstream>
#include <map>
#include "gp_utils.h"
#include "singleton.h"

namespace OHOS {
namespace SmartPerf {
class GPU : public DelayedSingleton<GPU> {
public:
    static constexpr const char *GPU_CUR_FREQ_PATH[] = {
        "/sys/class/devfreq/fde60000.gpu/cur_freq", // rk3568
        "/sys/class/devfreq/gpufreq/cur_freq",      // wgr
    };
    static constexpr const char *GPU_CUR_WORKLOAD_PATH[] = {
        "/sys/class/devfreq/gpufreq/gpu_scene_aware/utilisation", // wgr
        "/sys/class/devfreq/fde60000.gpu/load",                   // rk3568
    };
    int get_gpu_freq();
    float get_gpu_load();
    void init_gpu_node();    
private:  
    std::string gpu_cur_freq_path;
    std::string gpu_cur_load_path;
    float calc_workload(const char *buffer) const;
};
}
}
#endif // GPU_H

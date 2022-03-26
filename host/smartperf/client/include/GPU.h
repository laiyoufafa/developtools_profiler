/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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

#ifndef GPU_H
#define GPU_H
#include <string>
#include <vector>
#include <stdlib.h>
#include <sstream>
#include <map>
#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <pthread.h>

#include "gp_utils.h"

namespace OHOS
{
    namespace SmartPerf
    {
        static const char *GPU_CUR_FREQ_PATH[] = {
            "/sys/class/devfreq/fde60000.gpu/cur_freq", // rk3568
            "/sys/class/devfreq/gpufreq/cur_freq",      // wgr
        };
        static const char *GPU_CUR_WORKLOAD_PATH[] = {
            "/sys/class/devfreq/gpufreq/gpu_scene_aware/utilisation", // wgr
            "/sys/class/devfreq/fde60000.gpu/load",                   // rk3568
        };

        class GPU
        {
        private:
            std::string gpu_cur_freq_path;
            std::string gpu_cur_load_path;
            GPU();
            ~GPU();
            static GPU *instance;
            float calc_workload(const char *buffer);

        public:
            int get_gpu_freq();
            float get_gpu_load();
            static GPU *getInstance();
            static pthread_mutex_t mutex;
        };
    }
}
#endif // GPU_H

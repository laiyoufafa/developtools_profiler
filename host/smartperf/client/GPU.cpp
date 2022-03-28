/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
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

#include <ctime>
#include "include/GPU.h"

namespace OHOS {
    namespace SmartPerf {
        pthread_mutex_t GPU::mutex;
        GPU *GPU::instance = nullptr;

        GPU *GPU::getInstance()
        {
            if (instance == nullptr) {
                pthread_mutex_lock(&mutex);
                if (instance == nullptr) {
                    instance = new GPU();
                }
                pthread_mutex_unlock(&mutex);
            }
            return instance;
        }

        GPU::GPU()
        {
            pthread_mutex_init(&mutex, nullptr);

            for (int i = 0; i < sizeof(GPU_CUR_FREQ_PATH) / sizeof(const char *); ++i) {
                if (GPUtils::canOpen(GPU_CUR_FREQ_PATH[i])) {
                    gpu_cur_freq_path = std::string(GPU_CUR_FREQ_PATH[i]);
                }
            }

            for (int i = 0; i < sizeof(GPU_CUR_WORKLOAD_PATH) / sizeof(const char *); ++i) {
                if (GPUtils::canOpen(GPU_CUR_WORKLOAD_PATH[i])) {
                    gpu_cur_load_path = std::string(GPU_CUR_WORKLOAD_PATH[i]);
                }
            }
        }

        int GPU::get_gpu_freq()
        {
            const int unit = 1000;
            static char buffer[128];
            FILE *fp;
            fp = fopen(gpu_cur_freq_path.c_str(), "r");
            if (fp == nullptr) {
                return -1;
            }
            buffer[0] = '\0';
            fgets(buffer, sizeof(buffer), fp);

            fclose(fp);
            int curGpuFreq = -1;
            int tmp;
            sscanf(buffer, "%d %d", &tmp, &curGpuFreq);
            if (curGpuFreq != -1) {
                return curGpuFreq * unit;
            } else {
                return atoi(buffer);
            }
        }

        float GPU::calc_workload(const char *buffer) const 
        {
            std::vector<std::string> sps;
            std::string buffer_line = buffer;
            GPUtils::mSplit(buffer, "@", sps);
            if (sps.size() > 0) {
                // rk3568
                float loadRk = std::stof(sps[0]);
                return loadRk;
            } else {
                // wgr
                float loadWgr = std::stof(buffer);
                return loadWgr;
            }
            return -1.0;
        }

        float GPU::get_gpu_load()
        {

            static char buffer[128];
            FILE *fp;
            fp = fopen(gpu_cur_load_path.c_str(), "r");
            if (fp == nullptr) {
                return -1.0f;
            }
            buffer[0] = '\0';
            fgets(buffer, sizeof(buffer), fp);
            fclose(fp);
            return calc_workload(buffer);
        }
    }
}

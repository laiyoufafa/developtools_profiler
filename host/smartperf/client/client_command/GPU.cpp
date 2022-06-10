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

#include <iostream>
#include <unistd.h>
#include "include/GPU.h"  

namespace OHOS {
    namespace SmartPerf {
        void GPU::init_gpu_node()
        {
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
            std::string gpu_freq = GPUtils::freadFile(std::string(gpu_cur_freq_path.c_str()));
            return atoi(gpu_freq.c_str());
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
            if (access(gpu_cur_load_path.c_str(), F_OK) == -1) {
                return -1.0;
            }
            FILE *fp = fopen(gpu_cur_load_path.c_str(), "r");
            if (fp == nullptr) {
                return EOF;
            }
            buffer[0] = '\0';
            while (fgets(buffer, sizeof(buffer), fp) == nullptr) {
                std::cout << "fgets fail";
            }
            if (fclose(fp) == EOF) {
                return -1.0;
            }
            return calc_workload(buffer);
        }
    }
}

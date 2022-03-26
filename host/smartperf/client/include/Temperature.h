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
#ifndef TEMPERATURE_H
#define TEMPERATURE_H

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
        const static char *thermal_path[] = {
            "/sys/devices/virtual/thermal",
            "/sys/class/thermal"};

        const static std::map<std::string, std::string> collect_nodes = {
            {"soc_thermal", "soc_thermal"},  
            {"system_h", "system_h"},     
            {"soc-thermal", "soc-thermal"},
            {"gpu-thermal", "gpu-thermal"},
            {"shell_frame", "shell_frame"},
            {"shell_front", "shell_front"},
            {"shell_back", "shell_back"}
        };

        class Temperature
        {
        private:
            Temperature();
            ~Temperature() {}

            static Temperature *instance;
            std::string thermal_base_path;
            std::map<std::string, std::string> thermal_node_path_map;
            void initThermalNode();

        public:
            static Temperature *getInstance();
            std::map<std::string, float> getThermalMap();

            static pthread_mutex_t mutex;
        };

    }
}
#endif
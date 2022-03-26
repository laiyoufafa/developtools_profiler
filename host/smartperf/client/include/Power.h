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
#ifndef POWER_H
#define POWER_H

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
        const static char *power_path[] = {
            "/sys/class/power_supply/battery",
            "/sys/class/power_supply/Battery",
            "/data/local/tmp/battery"
        };

        static const char *default_collect_power_info[] = {
            "current_now", 
            "voltage_now", 
        };

        class Power
        {
        private:
            Power();
            ~Power();

            static Power *instance;
            std::string power_base_path;
            std::map<std::string, std::string> power_node_path_map;

        public:
            static Power *getInstance();
            std::map<std::string, std::string> getPowerMap();

            static pthread_mutex_t mutex;
        };

    }
}
#endif

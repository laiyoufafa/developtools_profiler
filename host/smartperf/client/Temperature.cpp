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
#include <time.h>
#include <cmath>
#include "include/Temperature.h"
namespace OHOS
{
    namespace SmartPerf
    {
        pthread_mutex_t Temperature::mutex;
        Temperature *Temperature::instance = nullptr;
        Temperature *Temperature::getInstance()
        {
            if (instance == nullptr)
            {
                pthread_mutex_lock(&mutex);
                if (instance == nullptr)
                {
                    instance = new Temperature();
                }
                pthread_mutex_unlock(&mutex);
            }
            return instance;
        }

        Temperature::Temperature()
        {
            pthread_mutex_init(&mutex, nullptr);
            int cnt = sizeof(thermal_path) / sizeof(const char *);
            for (int i = 0; i < cnt; ++i)
            {
                if (GPUtils::canOpen(std::string(thermal_path[i]) + "/thermal_zone1/temp"))
                {
                    thermal_base_path = std::string(thermal_path[i]);
                    initThermalNode();
                }
            }
        }

        void Temperature::initThermalNode()
        {
            char type_node[256];
            char temp_node[256];
            char buffer[256];
            FILE *fp;
            for (int zone = 0; zone < 100; ++zone)
            {
                sprintf(type_node, "%s/thermal_zone%d/type", thermal_base_path.c_str(), zone);
                if (access(type_node, F_OK) == 0)
                {
                    fp = fopen(type_node, "r");
                    if (fp == NULL)
                    {
                        printf("Thermal()-fopen %s, err=%s\n", type_node, strerror(errno));
                        continue;
                    }
                    buffer[0] = '\0';
                    fgets(buffer, sizeof(buffer), fp);
                    fclose(fp);

                    if (strlen(buffer) == 0)
                    {
                        continue;
                    }
                    if (buffer[strlen(buffer) - 1] == '\n')
                        buffer[strlen(buffer) - 1] = '\0';
                    std::string type = std::string(buffer);

                    if (collect_nodes.count(type) == 0)
                    {
                        continue;
                    }
                    sprintf(temp_node, "%s/thermal_zone%d/temp", thermal_base_path.c_str(), zone);
                    thermal_node_path_map[type] = std::string(temp_node);
                }
            }
        }

        std::map<std::string, float> Temperature::getThermalMap()
        {
            std::map<std::string, float> thermal_map;

            FILE *fp;
            char buffer[256];
            std::map<std::string, std::string>::iterator iter;
            for (iter = thermal_node_path_map.begin(); iter != thermal_node_path_map.end(); ++iter)
            {
                std::string type = iter->first;
                std::string temp_node = thermal_node_path_map[type];
                fp = fopen(temp_node.c_str(), "r");
                if (fp == NULL)
                {
                    thermal_map[type] = -1.0f;
                    continue;
                }
                buffer[0] = '\0';
                fgets(buffer, sizeof(buffer), fp);
                float temp = (float)std::fabs(atof(buffer));
                while (temp >= 200)
                {
                    temp /= 1000;
                }
                fclose(fp);
                if (strlen(buffer) == 0)
                {
                    thermal_map[type] = -1.0f;
                    continue;
                }
                thermal_map[type] = temp;
            }
            return thermal_map;
        }

    }
}

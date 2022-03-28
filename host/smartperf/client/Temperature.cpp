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
#include <cmath>
#include "include/Temperature.h"
namespace OHOS {
    namespace SmartPerf {
        pthread_mutex_t Temperature::mutex;
        Temperature *Temperature::instance = nullptr;
        Temperature *Temperature::getInstance()
        {
            if (instance == nullptr) {
                pthread_mutex_lock(&mutex);
                if (instance == nullptr) {
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
            for (int i = 0; i < cnt; ++i) {
                if (GPUtils::canOpen(std::string(thermal_path[i]) + "/thermal_zone1/temp")) {
                    thermal_base_path = std::string(thermal_path[i]);
                    initThermalNode();
                }
            }
        }

        void Temperature::initThermalNode() 
        {
            char typeNode[256];
            char tempNode[256];
            char buffer[256];
            FILE *fp = nullptr;
            const int zoneTravelNum=100;
            for (int zone = 0; zone < zoneTravelNum; ++zone) {
                sprintf(typeNode, "%s/thermal_zone%d/type", thermal_base_path.c_str(), zone);
          
                fp = fopen(typeNode, "r");
                if (fp == nullptr) {
                    printf("Thermal()-fopen %s, err=%s\n", typeNode, strerror(errno));
                    continue;
                }
                buffer[0] = '\0';
                fgets(buffer, sizeof(buffer), fp);
                fclose(fp);

                if (strlen(buffer) == 0) {
                    continue;
                }
                if (buffer[strlen(buffer) - 1] == '\n')
                    buffer[strlen(buffer) - 1] = '\0';
                std::string type = std::string(buffer);

                if (collect_nodes.count(type) == 0) {
                    continue;
                }
                sprintf(tempNode, "%s/thermal_zone%d/temp", thermal_base_path.c_str(), zone);
                thermal_node_path_map[type] = std::string(tempNode);
            }
        }

        std::map<std::string, float> Temperature::getThermalMap()
        {
            std::map<std::string, float> thermal_map;

            FILE *fp = nullptr;
            char buffer[256];
            std::map<std::string, std::string>::iterator iter;
            for (iter = thermal_node_path_map.begin(); iter != thermal_node_path_map.end(); ++iter) {
                std::string type = iter->first;
                std::string tempNode = thermal_node_path_map[type];
                fp = fopen(tempNode.c_str(), "r");
                if (fp == nullptr) {
                    thermal_map[type] = -1.0f;
                    continue;
                }
                buffer[0] = '\0';
                fgets(buffer, sizeof(buffer), fp);
                float temp = std::fabs(atof(buffer));
                fclose(fp);
                if (strlen(buffer) == 0) {
                    thermal_map[type] = -1.0f;
                    continue;
                }
                thermal_map[type] = temp;
            }
            return thermal_map;
        }

    }
}

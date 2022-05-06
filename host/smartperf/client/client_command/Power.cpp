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
#include <iostream>
#include <cmath>
#include <unistd.h>
#include "securec.h"
#include "include/gp_utils.h"
#include "include/Power.h"

namespace OHOS {
namespace SmartPerf {
void Power::init_power()
{
    for (int i = 0; i < sizeof(power_path) / sizeof(const char *); ++i) {
        if (GPUtils::canOpen(std::string(power_path[i]))) {
            power_base_path = std::string(power_path[i]);
        }
    }

    char powerNode[256];
    for (int j = 0; j < sizeof(default_collect_power_info) / sizeof(const char *); ++j) {
        if (snprintf_s(powerNode, sizeof(powerNode), sizeof(powerNode), "%s/%s", power_base_path.c_str(), 
            default_collect_power_info[j]) < 0) {
            std::cout << "snprintf fail";
        }
        // file exists
        std::string type = std::string(default_collect_power_info[j]);
        if (power_node_path_map.count(type) > 0) {
            continue;
        }
        power_node_path_map[type] = std::string(powerNode);
    }
}

std::map<std::string, std::string> Power::getPowerMap()
{
    std::map<std::string, std::string> power_map;
    FILE *fp = nullptr;
    char buffer[256];
    std::map<std::string, std::string>::iterator iter;
    int charging = 1;
    for (iter = power_node_path_map.begin(); iter != power_node_path_map.end(); ++iter) {
        std::string type = iter->first;
        std::string powerNode = power_node_path_map[type];
        if (access(powerNode.c_str(), F_OK) == -1) {
            continue;
        }
        fp = fopen(powerNode.c_str(), "r");
        if (fp == nullptr) {
            power_map[type] = "-1.0";
            continue;
        }
        buffer[0] = '\0';
        while (fgets(buffer, sizeof(buffer), fp) == nullptr) {
            std::cout << "fgets fail";
        }
        if (fclose(fp) == EOF) {
            std::cout << "fclose fail";
        }
        std::string power_value = std::string(buffer);
        if (iter->first == "status") {
            if (power_value.find("Charging") == std::string::npos && power_value.find("Full") == std::string::npos) {
                charging = 0;
            }
            if (power_value.find("Discharging") != std::string::npos) {
                charging = 0;
            }
        } else if (iter->first == "enable_hiz") {
            if (strcmp(buffer, "1") == 0) {
                charging = 0;
            }
            if (std::stoi(power_value) == 1) {
                charging = 0;
            }
        } else if (iter->first == "current_now") {
            // 若current now 大于 100000 单位归一化为 1000，大于10000 单位归一化为100，大于3000 单位归一化为10
            double tmp = fabs(std::stof(power_value));
            power_value = std::to_string(tmp);
        } else if (iter->first == "voltage_now") {
            // 若voltage now 大于100 单位归一化为100
            double tmp = std::stof(power_value);
            power_value = std::to_string(tmp);
        }
        power_map[type] = power_value;
    }
    if (power_map.count("voltage_now") == 0 && power_map.count("bat_id_0") > 0 && power_map.count("bat_id_1") > 0) {
        power_map["voltage_now"] = std::to_string(std::stof(power_map["bat_id_0"]) + std::stof(power_map["bat_id_1"]));
    }
    if (power_map.count("current_now") > 0 && charging) {
        power_map["current_now"] = "-" + power_map["current_now"];
    }
    if (power_map.count("status") > 0 && charging == 0) {
        power_map["status"] = "Discharging";
    }
    return power_map;
}
}
}
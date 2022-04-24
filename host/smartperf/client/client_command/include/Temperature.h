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
#ifndef TEMPERATURE_H
#define TEMPERATURE_H

#include <string>
#include <vector>
#include <sstream>
#include <map>
#include "gp_utils.h"
#include "singleton.h"
namespace OHOS {
namespace SmartPerf {
class Temperature : public DelayedSingleton<Temperature> {
public:
    static constexpr const char *thermal_path[] = {
        "/sys/devices/virtual/thermal",
        "/sys/class/thermal"
    };
    const std::map<std::string, std::string> collect_nodes = {
        { "soc_thermal", "soc_thermal" }, { "system_h", "system_h" },       { "soc-thermal", "soc-thermal" },
        { "gpu-thermal", "gpu-thermal" }, { "shell_frame", "shell_frame" }, { "shell_front", "shell_front" },
        { "shell_back", "shell_back" }
    };
    std::map<std::string, float> getThermalMap();
    void init_thermal_node();
    void init_temperature();
private:
    std::string thermal_base_path;
    std::map<std::string, std::string> thermal_node_path_map;
};
}
}
#endif
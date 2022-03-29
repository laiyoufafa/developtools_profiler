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
#ifndef DDR_H
#define DDR_H

#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <map>
#include "gp_utils.h"
namespace OHOS {
namespace SmartPerf {
class DDR {
public:
    static constexpr const char *DDR_CUR_FREQ_PATH[] = {
        "/sys/devices/platform/10012000.dvfsrc/helio-dvfsrc/dvfsrc_dump",
        "/sys/devices/platform/10012000.dvfsrc/helio-dvfsrc/dvfsrc_cur_freq",
        "/sys/class/devfreq/ddrfreq/cur_freq",
        "/sys/class/devfreq/mmc0/cur_freq",
        "/sys/kernel/debug/clk/bimc_clk/clk_measure",
        "/sys/kernel/debug/clk/measure_only_bimc_clk/clk_measure",
        "/sys/kernel/debug/clk/bimc_clk/clk_rate",
        "/sys/kernel/debug/clk/measure_only_mccc_clk/clk_measure"

    };
    static constexpr const char *DDR_AVAILABLE_FREQ_PATH[] = {
        "/sys/class/devfreq/ddrfreq/available_frequencies",
        "/sys/class/devfreq/mmc0/available_frequencies",
    };
    ~DDR();
    static DDR *getInstance();
    long long getDdrFreq();
    static pthread_mutex_t mutex;

private:
    DDR();
    static DDR *instance;
    std::map<std::string, int> is_support;
    std::string ddr_cur_freq_path;
};
};
}
#endif

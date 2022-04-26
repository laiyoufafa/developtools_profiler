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
#include <sstream>
#include <map>
#include "gp_utils.h"
#include "singleton.h"
namespace OHOS {
namespace SmartPerf {
class DDR : public DelayedSingleton<DDR> {
public:
    static constexpr const char *DDR_CUR_FREQ_PATH[] = {
        "/sys/class/devfreq/ddrfreq/cur_freq",
    };
    static constexpr const char *DDR_AVAILABLE_FREQ_PATH[] = {
        "/sys/class/devfreq/ddrfreq/available_frequencies",
    };
    long long get_ddr_freq();

private:
    std::string ddr_cur_freq_path;
};
};
}
#endif

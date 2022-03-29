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
#include "include/DDR.h"
namespace OHOS {
    namespace SmartPerf {
        pthread_mutex_t DDR::mutex;
        DDR *DDR::instance = nullptr;
        DDR *DDR::getInstance()
        {
            if (instance == nullptr) {
                pthread_mutex_lock(&mutex);
                if (instance == nullptr) {
                    instance = new DDR();
                }
                pthread_mutex_unlock(&mutex);
            }
            return instance;
        }

        DDR::DDR()
        {
            pthread_mutex_init(&mutex, nullptr);
        }

        long long DDR::getDdrFreq()
        {
            long long curFreq;

            FILE *fp;
            static char buffer[256];
            const int defaultUnit = 1000;
            const int defaultHalf = 2;
            fp = fopen(ddr_cur_freq_path.c_str(), "r");
            if (fp == nullptr) {
                printf("getDDRInfoFromNode()-fopen %s, err=%s\n", ddr_cur_freq_path.c_str(), strerror(errno));
                curFreq = -1;
            } else {
                buffer[0] = '\0';
                long long curDDR = -1;
                while (fgets(buffer, sizeof(buffer), fp)) {
                    if (sscanf(buffer, "DDR :%lld", &curDDR) < 0) {
                        continue;
                    }
                }
                if (curDDR != -1) {
                    curFreq = curDDR * defaultUnit / defaultHalf;
                } else {
                    curFreq = std::atoll(buffer);
                }
                printf("getDDRInfoFromNode()-cur_freq: %lld\n", curFreq);
                fclose(fp);
            }
            return curFreq;
        }
    }
}

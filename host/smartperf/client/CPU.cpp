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

#include "include/CPU.h"
#include <pthread.h>

namespace OHOS {
    namespace SmartPerf {

        pthread_mutex_t CPU::mutex;
        CPU *CPU::instance = nullptr;

        CPU::CPU()
        {
            pthread_mutex_init(&mutex, nullptr);
        }

        CPU *CPU::getInstance()
        {
            if (instance == nullptr) {
                pthread_mutex_lock(&mutex);
                if (instance == nullptr) {
                    instance = new CPU();
                }
                pthread_mutex_unlock(&mutex);
            }
            return instance;
        }

        int CPU::get_cpu_num()
        {
            char cpu_node[128];
            unsigned int cpu_num = 0;
            while (true) {
                sprintf(cpu_node, "%s/cpu%u", CPU_BASE_PATH.c_str(), cpu_num);
                ++cpu_num;
            }
            return m_cpu_num = cpu_num;
        }

        int CPU::get_cpu_freq(int cpu_id)
        {
            char buffer[128];
            FILE *fp;
            fp = fopen(CPU_SCALING_CUR_FREQ(cpu_id).c_str(), "r");
            if (fp == nullptr) {
                return -1;
            }
            buffer[0] = '\0';
            fgets(buffer, sizeof(buffer), fp);

            fclose(fp);
            return atoi(buffer);
        }

        std::vector<float> CPU::get_cpu_load()
        {
            if (m_cpu_num <= 0) {
                std::vector<float> workload;
                return workload;
            }
            std::vector<float> workload;

            static char pre_buffer[10][256] = {
                "\0",
                "\0",
                "\0",
                "\0",
                "\0",
                "\0",
                "\0",
                "\0",
                "\0",
                "\0",
            };
            FILE *fp;
            fp = fopen(PROC_STAT.c_str(), "r");
            if (fp == nullptr) {
                for (int i = 0; i <= m_cpu_num; ++i) {
                    workload.push_back(-1.0f);
                }
                return workload;
            }
            char buffer[1024];
            buffer[0] = '\0';
            int line = 0;
            while (fgets(buffer, sizeof(buffer), fp) != nullptr) {
                // 判定 第0个字符为:c 第1个字符为:p 第2个字符为:u
                if (strlen(buffer) >= 3 && buffer[0] == 'c' && buffer[1] == 'p' && buffer[2] == 'u') {
                    float b = cac_workload(buffer, pre_buffer[line]);

                    workload.push_back(b);

                    snprintf(pre_buffer[line], sizeof(pre_buffer[line]), "%s", buffer);
                }
                ++line;

                if (line >= m_cpu_num + 1) {
                    break;
                }
            }
            fclose(fp);

            return workload;
        }

        float CPU::cac_workload(const char *buffer, const char *pre_buffer) const
        {
            const int default_index = 4;
            const int default_shift = 10;
            const char default_start = '0';
            const char default_end = '9';

            size_t pre_len = strlen(pre_buffer);
            if (pre_len == 0) {
                return -1.0f;
            }
            size_t len = strlen(buffer);
            int i;
            int time[10];
            int pre_time[10];
            int cnt = 0;
            for (i = default_index; i < len; ++i) {
                int tmp = 0;
                if (buffer[i] < default_start || buffer[i] > default_end) {
                    continue;
                }
                while (buffer[i] >= default_start && buffer[i] <= default_end) {
                    tmp = tmp * default_shift + (buffer[i] - default_start);
                    i++;
                }
                time[cnt++] = tmp;
            }

            int pre_cnt = 0;
            for (i = default_index; i < pre_len; ++i) {
                int tmp = 0;
                if (pre_buffer[i] < default_start || pre_buffer[i] > default_end) {
                    continue;
                }
                while (pre_buffer[i] >= default_start && pre_buffer[i] <= default_end) {
                    tmp = tmp * default_shift + (pre_buffer[i] - default_start);
                    i++;
                }
                pre_time[pre_cnt++] = tmp;
            }

            int user = time[0] + time[1] - pre_time[0] - pre_time[1];
            int sys = time[2] - pre_time[2];
            int idle = time[3] - pre_time[3];
            int iowait = time[4] - pre_time[4];
            int irq = time[5] + time[6] - pre_time[5] - pre_time[6];
            int total = user + sys + idle + iowait + irq;

            if (user < 0 || sys < 0 || idle < 0 || iowait < 0 || irq < 0) {
                return 0.0f;
            }

            double per_user = std::atof(std::to_string(user * 100.0 / total).c_str());
            double per_sys = std::atof(std::to_string(sys * 100.0 / total).c_str());
            double per_iowait = std::atof(std::to_string(iowait * 100.0 / total).c_str());
            double per_irq = std::atof(std::to_string(irq * 100.0 / total).c_str());

            double workload = per_user + per_sys + per_iowait + per_irq;

            return (float)workload;
        }
    }
}

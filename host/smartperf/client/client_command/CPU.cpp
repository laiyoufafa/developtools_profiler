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
#include <pthread.h>
#include <unistd.h>
#include "securec.h"
#include "include/CPU.h"
namespace OHOS {
namespace SmartPerf {
int CPU::get_cpu_num()
{
    char cpu_node[128];
    int cpu_num = 0;
    while (true) {
        if (snprintf_s(cpu_node, sizeof(cpu_node), sizeof(cpu_node), "%s/cpu%d", CPU_BASE_PATH.c_str(), cpu_num) > 0) {
            if (access(cpu_node, F_OK) == -1) {
                break;
            }
        }
        ++cpu_num;
    }
    return m_cpu_num = cpu_num;
}
int CPU::get_cpu_freq(int cpu_id)
{
    char buffer[128];
    if (access(CPU_SCALING_CUR_FREQ(cpu_id).c_str(), F_OK) == -1) {
        return -1;
    }
    FILE *fp = fopen(CPU_SCALING_CUR_FREQ(cpu_id).c_str(), "r");
    if (fp == nullptr) {
        return -1;
    }
    buffer[0] = '\0';
    while (fgets(buffer, sizeof(buffer), fp) == nullptr) {
        std::cout << "fgets fail";
    }
    if (fclose(fp) == EOF) {
        return EOF;
    }
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
    if (access(PROC_STAT.c_str(), F_OK) == -1) {
        return workload;
    }
    FILE *fp = fopen(PROC_STAT.c_str(), "r");
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
        const int zeroPos = 0;
        const int firstPos = 1;
        const int secondPos = 2;
        const int length = 3;
        if (strlen(buffer) >= length && buffer[zeroPos] == 'c' && buffer[firstPos] == 'p' && buffer[secondPos] == 'u') {
            float b = cac_workload(buffer, pre_buffer[line]);
            workload.push_back(b);
            if (snprintf_s(pre_buffer[line], sizeof(pre_buffer[line]), sizeof(pre_buffer[line]), "%s", buffer) < 0) {
                std::cout << "snprintf_s fail";
            }
        }
        ++line;

        if (line >= m_cpu_num + 1) {
            break;
        }
    }
    if (fclose(fp) == EOF) {
        return workload;
    }

    return workload;
}

float CPU::cac_workload(const char *buffer, const char *pre_buffer)
{
    const size_t default_index = 4;
    const size_t default_shift = 10;
    const char default_start = '0';
    const char default_end = '9';

    size_t pre_len = strlen(pre_buffer);
    size_t len = strlen(buffer);
    if (pre_len == 0 || len == 0) {
        return -1.0f;
    }
    size_t time[10];
    size_t pre_time[10];
    size_t cnt = 0;

    for (size_t i = default_index; i < len; ++i) {
        size_t tmp = 0;
        if (buffer[i] < default_start || buffer[i] > default_end) {
            continue;
        }
        while (buffer[i] >= default_start && buffer[i] <= default_end) {
            tmp = tmp * default_shift + (buffer[i] - default_start);
            i++;
        }
        time[cnt++] = tmp;
    }

    size_t pre_cnt = 0;
    for (size_t i = default_index; i < pre_len; ++i) {
        size_t tmp = 0;
        if (pre_buffer[i] < default_start || pre_buffer[i] > default_end) {
            continue;
        }
        while (pre_buffer[i] >= default_start && pre_buffer[i] <= default_end) {
            tmp = tmp * default_shift + (pre_buffer[i] - default_start);
            i++;
        }
        pre_time[pre_cnt++] = tmp;
    }

    size_t user = time[0] + time[1] - pre_time[0] - pre_time[1];
    size_t sys = time[2] - pre_time[2];
    size_t idle = time[3] - pre_time[3];
    size_t iowait = time[4] - pre_time[4];
    size_t irq = time[5] + time[6] - pre_time[5] - pre_time[6];
    size_t total = user + sys + idle + iowait + irq;

    if (user < 0 || sys < 0 || idle < 0 || iowait < 0 || irq < 0) {
        return 0.0f;
    }

    double per_user = std::atof(std::to_string(user * 100.0 / total).c_str());
    double per_sys = std::atof(std::to_string(sys * 100.0 / total).c_str());
    double per_iowait = std::atof(std::to_string(iowait * 100.0 / total).c_str());
    double per_irq = std::atof(std::to_string(irq * 100.0 / total).c_str());

    double workload = per_user + per_sys + per_iowait + per_irq;

    return static_cast<float>(workload);
}
}
}

/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#include <dlfcn.h>
#include <unistd.h>

#include "cpu_plugin_config.pb.h"
#include "cpu_plugin_result.pb.h"
#include "plugin_module_api.h"

namespace {
constexpr int TEST_PID = 10;
int g_testCount = 10;
} // namespace

static void Report(PluginModuleStruct*& cpuPlugin, std::vector<uint8_t>& dataBuffer)
{
    while (g_testCount--) {
        int len = cpuPlugin->callbacks->onPluginReportResult(dataBuffer.data(), cpuPlugin->resultBufferSizeHint);
        std::cout << "test:filler buffer length = " << len << std::endl;

        if (len > 0) {
            CpuData cpuData;
            cpuData.ParseFromArray(dataBuffer.data(), len);
            std::cout << "test:ParseFromArray length = " << len << std::endl;

            CpuUsageInfo cpuUsageInfo = cpuData.cpu_usage_info();
            std::cout << "prev_process_cpu_time_ms:" << cpuUsageInfo.prev_process_cpu_time_ms() << std::endl;
            std::cout << "prev_system_cpu_time_ms:" << cpuUsageInfo.prev_system_cpu_time_ms() << std::endl;
            std::cout << "prev_system_boot_time_ms:" << cpuUsageInfo.prev_system_boot_time_ms() << std::endl;
            std::cout << "process_cpu_time_ms:" << cpuUsageInfo.process_cpu_time_ms() << std::endl;
            std::cout << "system_cpu_time_ms:" << cpuUsageInfo.system_cpu_time_ms() << std::endl;
            std::cout << "system_boot_time_ms:" << cpuUsageInfo.system_boot_time_ms() << std::endl;

            for (int i = 0; i < cpuUsageInfo.cores_size(); i++) {
                CpuCoreUsageInfo cpuCoreUsageInfo = cpuUsageInfo.cores()[i];
                std::cout << "cpu_core:" << cpuCoreUsageInfo.cpu_core() << std::endl;
                std::cout << "prev_system_cpu_time_ms:" << cpuCoreUsageInfo.prev_system_cpu_time_ms() << std::endl;
                std::cout << "prev_system_boot_time_ms:" << cpuCoreUsageInfo.prev_system_boot_time_ms() << std::endl;
                std::cout << "system_cpu_time_ms:" << cpuCoreUsageInfo.system_cpu_time_ms() << std::endl;
                std::cout << "system_boot_time_ms:" << cpuCoreUsageInfo.system_boot_time_ms() << std::endl;
                std::cout << "min_frequency_khz:" << cpuCoreUsageInfo.frequency().min_frequency_khz() << std::endl;
                std::cout << "max_frequency_khz:" << cpuCoreUsageInfo.frequency().max_frequency_khz() << std::endl;
                std::cout << "cur_frequency_khz:" << cpuCoreUsageInfo.frequency().cur_frequency_khz() << std::endl;
                std::cout << "is_little_core:" << cpuCoreUsageInfo.is_little_core() << std::endl;
            }
            std::cout << "timestamp.tv_sec : " << cpuUsageInfo.timestamp().tv_sec() << std::endl;
            std::cout << "timestamp.tv_nsec : " << cpuUsageInfo.timestamp().tv_nsec() << std::endl;

            for (int i = 0; i < cpuData.thread_info_size(); i++) {
                ThreadInfo threadInfo = cpuData.thread_info()[i];
                std::cout << "tid : " << threadInfo.tid() << std::endl;
                std::cout << "thread_name : " << threadInfo.thread_name() << std::endl;
                std::cout << "thread_state : " << threadInfo.thread_state() << std::endl;
                std::cout << "prev_thread_cpu_time_ms : " << threadInfo.prev_thread_cpu_time_ms() << std::endl;
                std::cout << "thread_cpu_time_ms : " << threadInfo.thread_cpu_time_ms() << std::endl;
                std::cout << "timestamp.tv_sec : " << threadInfo.timestamp().tv_sec() << std::endl;
                std::cout << "timestamp.tv_nsec : " << threadInfo.timestamp().tv_nsec() << std::endl;
            }
        }

        std::cout << "test:sleep...................." << std::endl;
        sleep(1);
    }
}

int main()
{
    CpuConfig protoConfig;
    void* handle = dlopen("./libcpudataplugin.z.so", RTLD_LAZY);
    if (handle == nullptr) {
        std::cout << "test:dlopen err: " << dlerror() << std::endl;
        return 0;
    }
    std::cout << "test:handle = " << handle << std::endl;
    PluginModuleStruct* cpuPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
    std::cout << "test:name = " << cpuPlugin->name << std::endl;
    std::cout << "test:buffer size = " << cpuPlugin->resultBufferSizeHint << std::endl;

    // Serialize config
    protoConfig.set_pid(TEST_PID);
    int configLength = protoConfig.ByteSizeLong();
    std::vector<uint8_t> configBuffer(configLength);
    int ret = protoConfig.SerializeToArray(configBuffer.data(), configLength);
    std::cout << "test:configLength = " << configLength << std::endl;
    std::cout << "test:serialize success start plugin ret = " << ret << std::endl;

    // run plugin
    std::vector<uint8_t> dataBuffer(cpuPlugin->resultBufferSizeHint);
    cpuPlugin->callbacks->onPluginSessionStart(configBuffer.data(), configLength);
    Report(cpuPlugin, dataBuffer);
    cpuPlugin->callbacks->onPluginSessionStop();

    return 0;
}

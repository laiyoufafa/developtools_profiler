/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <chrono>
#include <thread>
#include <mutex>
#include <unistd.h>
#include <array>
#include <fcntl.h>
#include <signal.h>
#include <sys/wait.h>

#include "common.h"
#include "hiebpf_plugin_config.pb.h"
#include "logging.h"
#include "plugin_module_api.h"

namespace {
constexpr uint32_t MAX_BUFFER_SIZE = 4 * 1024 * 1024;
std::mutex taskMutex;
constexpr int32_t RET_OK = 0;
constexpr int32_t RET_ERR = -1;
std::string HIEBPF_COMMAND = "hiebpf";
bool g_releaseResources = false;
std::unique_ptr<FILE, decltype(&pclose)> g_pipe(nullptr, nullptr);

void RunCmd(std::string cmd)
{
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(cmd.c_str(), "r"), pclose);
}
} // namespace

static int32_t HiebpfSessionStart(const uint8_t* configData, uint32_t configSize)
{
    std::lock_guard<std::mutex> guard(taskMutex);
    CHECK_TRUE(!g_releaseResources, 0, "%s: hiebpf released resources, return", __func__);
    HILOG_DEBUG(LOG_CORE, "enter");
    if (configData == nullptr || configSize < 0) {
        HILOG_ERROR(LOG_CORE, "Parameter error");
        return RET_ERR;
    }
    HiebpfConfig config;
    if (config.ParseFromArray(configData, configSize) <= 0) {
        HILOG_ERROR(LOG_CORE,"Parameter parsing failed");
        return RET_ERR;
    }

    size_t defaultSize = sizeof(g_pluginModule.outFileName);
    if (sizeof(config.outfile_name().c_str()) > defaultSize - 1) {
        HILOG_ERROR(LOG_CORE,"The out file path more than %zu bytes", defaultSize);
        return RET_ERR;
    }
    // int32_t ret = strncpy_s(g_pluginModule.outFileName, defaultSize, config.outfile_name().c_str(), defaultSize - 1);
    // if (ret != EOK) {
    //     HILOG_ERROR(LOG_CORE, "strncpy_s error! outfile is %s", config.outfile_name().c_str());
    //     return RET_ERR;
    // }
    std::string ret = config.cmd_line();
    ret += " --start true";
    RunCmd(ret);
    HILOG_DEBUG(LOG_CORE, "leave");
    return RET_OK;
}

static int32_t HiebpfSessionStop()
{
    std::lock_guard<std::mutex> guard(taskMutex);
    CHECK_TRUE(!g_releaseResources, 0, "%s: hiebpf released resources, return", __func__);
    HILOG_DEBUG(LOG_CORE, "enter");
    std::string stop = "hiebpf --stop true";
    RunCmd(stop);
    int sleepSeconds = 2;
    std::this_thread::sleep_for(std::chrono::seconds(sleepSeconds));
    HILOG_DEBUG(LOG_CORE, "leave");
    return RET_OK;
}

static PluginModuleCallbacks g_callbacks = {
    .onPluginSessionStart = HiebpfSessionStart,
    .onPluginReportResult = nullptr,
    .onPluginSessionStop = HiebpfSessionStop,
    .onRegisterWriterStruct = nullptr,
};

PluginModuleStruct g_pluginModule = {
    .callbacks = &g_callbacks,
    .name = "hiebpf-plugin",
    .resultBufferSizeHint = MAX_BUFFER_SIZE,
    .isStandaloneFileData = true,
    .outFileName = "/data/local/tmp/hiebpf.data",
};
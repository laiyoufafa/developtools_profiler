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

#include "bytrace_module.h"

#include <poll.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <vector>

#include "bytrace_plugin_config.pb.h"
#include "logging.h"
#include "securec.h"

namespace {
const std::string CMD_PATH = "/system/bin/bytrace";
int g_processNum = -1;
constexpr uint32_t MAX_BUFFER_SIZE = 4 * 1024 * 1024;

bool RunWithConfig(const BytracePluginConfig& config)
{
    std::vector<std::string> args;
    args.push_back("bytrace");
    if (config.buffe_size() != 0) {
        args.push_back("-b");
        args.push_back(std::to_string(config.buffe_size()));
    }
    if (config.time() != 0) {
        args.push_back("-t");
        args.push_back(std::to_string(config.time()));
    }
    if (!config.clock().empty()) {
        args.push_back("--trace_clock");
        args.push_back(config.clock());
    }
    if (!config.outfile_name().empty()) {
        args.push_back("-o");
        args.push_back(config.outfile_name());
    }
    if (!config.categories().empty()) {
        for (std::string category : config.categories()) {
            args.push_back(category);
        }
    }

    std::vector<char*> params;
    std::string cmdPrintStr = "";
    for (std::string& it : args) {
        cmdPrintStr += (it + " ");
        params.push_back(const_cast<char*>(it.c_str()));
    }
    params.push_back(nullptr);
    HILOG_INFO(LOG_CORE, "call bytrace::Run: %s", cmdPrintStr.c_str());

    execv(CMD_PATH.data(), &params[0]);
    return true;
}
} // namespace

int BytracePluginSessionStart(const uint8_t* configData, const uint32_t configSize)
{
    BytracePluginConfig config;
    HILOG_INFO(LOG_CORE, "BytracePluginSessionStart %u", configSize);
    CHECK_TRUE(config.ParseFromArray(configData, configSize), 0, "parse config FAILED!");

    g_processNum = fork();
    CHECK_TRUE(g_processNum >= 0, -1, "create process FAILED!");

    if (g_processNum == 0) {
        // child process
        CHECK_TRUE(RunWithConfig(config), 0, "run bytrace FAILED!");
        _exit(0);
    }

    return 0;
}

int BytraceRegisterWriterStruct(const WriterStruct* writer)
{
    return 0;
}

int BytracePluginSessionStop()
{
    if (g_processNum > 0) {
        // parent process
        int status = 0;
        // judge if child process have exited.
        if (waitpid(g_processNum, &status, WNOHANG) == 0) {
            // send SIGKILL to child process.
            if (kill(g_processNum, SIGINT)) {
                HILOG_WARN(LOG_CORE, "BytracePluginSessionStop kill child process failed.");
            } else {
                HILOG_INFO(LOG_CORE, "BytracePluginSessionStop kill child process success.");
            }
        }
        // report child process exit status.
        if (WIFEXITED(status)) {
            HILOG_INFO(LOG_CORE, "child %d exit with status %d!", g_processNum,
                       WEXITSTATUS(static_cast<unsigned>(status)));
        } else if (WIFSIGNALED(status)) {
            HILOG_INFO(LOG_CORE, "child %d exit with signal %d!", g_processNum,
                       WTERMSIG(static_cast<unsigned>(status)));
        } else if (WIFSTOPPED(status)) {
            HILOG_INFO(LOG_CORE, "child %d stopped by signal %d", g_processNum,
                       WSTOPSIG(static_cast<unsigned>(status)));
        } else {
            HILOG_INFO(LOG_CORE, "child %d otherwise", g_processNum);
        }
    }
    return 0;
}

static PluginModuleCallbacks g_callbacks = {
    BytracePluginSessionStart,
    nullptr, // onPluginReportResult
    BytracePluginSessionStop,
    BytraceRegisterWriterStruct,
};

PluginModuleStruct g_pluginModule = {&g_callbacks, "bytrace_plugin", MAX_BUFFER_SIZE};

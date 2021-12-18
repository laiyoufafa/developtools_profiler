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
#include <thread>

#include "command_poller.h"
#include "hook_manager.h"
#include "logging.h"
#include "plugin_service_types.pb.h"
#include "writer_adapter.h"
#include "hook_standalone.h"

namespace {
const int SLEEP_ONE_SECOND = 1000;
const int BUF_MAX_LEN = 10;
const int VC_ARG_TWAIN = 2;
const int VC_ARG_STEP_SIZE = 2;
const int SMBSIZE_BASE = 2;

bool ProcessExist(std::string pid)
{
    int ret = 0;
    std::string pid_path = std::string();
    struct stat stat_buf;
    if (pid.size() == 0) {
        return false;
    }
    pid_path = "/proc/" + pid + "/status";
    if (stat(pid_path.c_str(), &stat_buf) != 0) {
        return false;
    }
    return true;
}

bool ParseCommand(std::vector<std::string> args, HookData& hookData)
{
    int idx = 0;
    while (idx < args.size()) {
        if (args[idx] == "-o") {
            hookData.fileName = args[idx + 1].c_str();
        } else if (args[idx] == "-p") {
            hookData.pid = std::stoi(args[idx + 1], nullptr);
            if (std::to_string(hookData.pid) != args[idx + 1]) {
                return false;
            }
            if (!ProcessExist(args[idx + 1])) {
                printf("process does not exist\n");
                return false;
            }
        } else if (args[idx] == "-n") {
            hookData.processName = args[idx + 1];
        } else if (args[idx] == "-s") {
            hookData.smbSize = std::stoi(args[idx + 1], nullptr);
            if (std::to_string(hookData.smbSize) != args[idx + 1]) {
                return false;
            }
        } else if (args[idx] == "-f") {
            hookData.filterSize = std::stoi(args[idx + 1], nullptr);
            if (std::to_string(hookData.filterSize) != args[idx + 1]) {
                return false;
            }
        } else if (args[idx] == "-d") {
            hookData.maxStackDepth = std::stoi(args[idx + 1], nullptr);
            if (std::to_string(hookData.maxStackDepth) != args[idx + 1]) {
                return false;
            }
        } else if (args[idx] == "-L") {
            if (idx + 1 < args.size()) {
                hookData.duration = std::stoull(args[idx + 1]);
            }
        } else if (args[idx] == "-F") {
            if (idx + 1 < args.size()) {
                hookData.performance_filename = args[idx + 1];
            }
        } else {
            printf("args[%d] = %s\n", idx, args[idx].c_str());
            return false;
        }
        idx += VC_ARG_STEP_SIZE;
    }
    return true;
}

bool VerifyCommand(std::vector<std::string> args, HookData& hookData)
{
    if ((args.size() % VC_ARG_TWAIN) != 0) {
        return false;
    }
    hookData.duration = 0;
    hookData.performance_filename = "./performance.txt";
    hookData.fileName = "";
    if (!ParseCommand(args, hookData)) {
        return false;
    }
    if (!hookData.fileName.empty() && ((hookData.smbSize % SMBSIZE_BASE) == 0) &&
        (!hookData.processName.empty() || hookData.pid > 0)) {
        return true;
    }
    return false;
}

void GetHookedProceInfo(HookData& hookData)
{
    if (hookData.pid > 0) {
        printf("Please send signal to target process %d\n", hookData.pid);
    } else if (!hookData.processName.empty()) {
        std::string findpid = "pidof " + hookData.processName;
        std::unique_ptr<char[]> buffer {new (std::nothrow) char[BUF_MAX_LEN]};
        HILOG_INFO(LOG_CORE, "find pid command : %s", findpid.c_str());
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(findpid.c_str(), "r"), pclose);
        char line[LINE_SIZE];
        do {
            if (fgets(line, sizeof(line), pipe.get()) == nullptr) {
                printf("Please start process %s\n", hookData.processName.c_str());
                break;
            } else if (strlen(line) > 0 && isdigit((unsigned char)(line[0]))) {
                hookData.pid = (int)atoi(line);
                printf("Please send signal to target process %d\n", hookData.pid);
                break;
            }
        } while (1);
    }

    printf("Record file = %s, apply sharememory size = %u\n", hookData.fileName.c_str(), hookData.smbSize);
    if (hookData.maxStackDepth > 0) {
        printf("depth greater than %u will not display\n", hookData.maxStackDepth);
    }
    if (hookData.filterSize > 0) {
        printf("malloc size smaller than %u will not record\n", hookData.filterSize);
    }

    if (!OHOS::Developtools::Profiler::Hook::StartHook(hookData)) {
        return;
    }
    while (true) {
        std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_ONE_SECOND));
    }
}
} // namespace

int main(int argc, char* argv[])
{
    if (argc > 1) {
        std::vector<std::string> args;
        for (int i = 1; i < argc; i++) {
            args.push_back(argv[i]);
        }
        HookData hookData = {-1, 0, 0, 0, 0, "", "", ""};
        hookData.pid = -1;
        if (VerifyCommand(args, hookData)) {
            GetHookedProceInfo(hookData);
        } else {
            printf(
                "Usage: native_daemon [-o file] [-s smb_size] <-p pid> <-n process_name> "
                "<-f filter_size> <-d max_stack_depth>\n");
            return 0;
        }
    } else {
        auto hookManager = std::make_shared<HookManager>();
        CHECK_NOTNULL(hookManager, 1, "create PluginManager FAILED!");

        auto commandPoller = std::make_shared<CommandPoller>(hookManager);
        CHECK_NOTNULL(commandPoller, 1, "create CommandPoller FAILED!");
        hookManager->SetCommandPoller(commandPoller);
        hookManager->RegisterAgentPlugin("hookdaemon");
        while (true) {
            std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_ONE_SECOND));
        }
    }
    return 0;
}

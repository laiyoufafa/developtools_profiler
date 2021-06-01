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

#include "command_poller.h"
#include "logging.h"
#include "plugin_manager.h"
#include "plugin_service_types.pb.h"
#include "plugin_watcher.h"
#include "schedule_task_manager.h"
#include "writer_adapter.h"

namespace {
#if defined(__i386__) || defined(__x86_64__)
const char DEFAULT_PLUGIN_PATH[] = "./";
#else
const char DEFAULT_PLUGIN_PATH[] = "/data/local/tmp/";
#endif

const int SLEEP_ONE_SECOND = 1000;
} // namespace

int main(int argc, char* argv[])
{
    std::string pluginDir(DEFAULT_PLUGIN_PATH);
    if (argv[1] != nullptr) {
        HILOG_DEBUG(LOG_CORE, "pluginDir = %s", argv[1]);
        pluginDir = argv[1];
    }

    auto pluginManager = std::make_shared<PluginManager>();
    CHECK_NOTNULL(pluginManager, 1, "create PluginManager FAILED!");

    auto commandPoller = std::make_shared<CommandPoller>(pluginManager);
    CHECK_NOTNULL(commandPoller, 1, "create CommandPoller FAILED!");
    pluginManager->SetCommandPoller(commandPoller);

    PluginWatcher watcher(pluginManager);
    watcher.ScanPlugins(pluginDir);
    watcher.WatchPlugins(pluginDir);

    while (true) {
        std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_ONE_SECOND));
    }

    return 0;
}

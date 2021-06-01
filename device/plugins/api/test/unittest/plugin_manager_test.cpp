/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#include <google/protobuf/message.h>
#include <grpcpp/health_check_service_interface.h>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <thread>

#include "command_poller.h"
#include "grpc/impl/codegen/log.h"
#include "logging.h"
#include "plugin_manager.h"
#include "plugin_service.h"
#include "plugin_service.ipc.h"
#include "profiler_service.h"
#include "socket_context.h"

using google::protobuf::Message;
using namespace testing::ext;

namespace {
const static std::string SUCCESS_PLUGIN_NAME = "libmemdataplugin.z.so";
std::string g_testPluginDir("/data/local/tmp/");

class PluginManagerTest : public ::testing::Test {
protected:
    static constexpr auto TEMP_DELAY = std::chrono::milliseconds(20);
    static void SetUpTestCase()
    {
#if defined(__i386__) || defined(__x86_64__)
        char pluginDir[PATH_MAX + 1] = {0};
        if (readlink("/proc/self/exe", pluginDir, PATH_MAX) > 0) {
            char* pos = strrchr(pluginDir, '/');
            if (pos != nullptr) {
                *(pos++) = '\0';
                printf("-----> pluginDir = %s\n", pluginDir);
                g_testPluginDir = pluginDir;
            }
        }
#endif
        printf("======> pluginDir = %s\n", g_testPluginDir.c_str());

        std::this_thread::sleep_for(TEMP_DELAY);
        printf("SetUpTestCase success\n");
    }
    static void TearDownTestCase()
    {
    }
};

/**
 * @tc.name: plugin
 * @tc.desc: Plug-in normal loading and removal process test.
 * @tc.type: FUNC
 */
HWTEST_F(PluginManagerTest, SuccessPlugin, TestSize.Level1)
{
    auto pluginManage = std::make_shared<PluginManager>();
    auto commandPoller = std::make_shared<CommandPoller>(pluginManage);
    pluginManage->SetCommandPoller(commandPoller);

    const uint8_t configData[] = {0x30, 0x01, 0x38, 0x01, 0x42, 0x01, 0x01};
    ProfilerPluginConfig config;
    const std::vector<uint32_t> pluginIdsVector = {1};
    config.set_name(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME);
    config.set_config_data((const void*)configData, 7);
    config.set_sample_interval(1000);

    EXPECT_FALSE(pluginManage->LoadPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->UnloadPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->AddPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->AddPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->RemovePlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->RemovePlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->AddPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->LoadPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->LoadPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));
    EXPECT_FALSE(pluginManage->UnloadPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));

    EXPECT_FALSE(pluginManage->LoadPlugin(g_testPluginDir + "/" + SUCCESS_PLUGIN_NAME));

    std::vector<ProfilerPluginConfig> configVec;
    configVec.push_back(config);
    EXPECT_FALSE(pluginManage->CreatePluginSession(configVec));

    EXPECT_FALSE(pluginManage->StartPluginSession(pluginIdsVector, configVec));
    std::this_thread::sleep_for(TEMP_DELAY);

    EXPECT_FALSE(pluginManage->StopPluginSession(pluginIdsVector));

    EXPECT_FALSE(pluginManage->DestroyPluginSession(pluginIdsVector));
}

/**
 * @tc.name: plugin
 * @tc.desc: get sample Mode.
 * @tc.type: FUNC
 */
HWTEST_F(PluginManagerTest, GetSampleMode, TestSize.Level1)
{
    PluginModule pluginModule;
    pluginModule.GetSampleMode();
}

/**
 * @tc.name: plugin
 * @tc.desc: Plug-in data acquisition process test.
 * @tc.type: FUNC
 */
HWTEST_F(PluginManagerTest, PluginManager, TestSize.Level1)
{
    PluginManager pluginManager;
    PluginModuleInfo info;
    pluginManager.UnloadPlugin(0);
    PluginResult pluginResult;
    pluginManager.SubmitResult(pluginResult);
    pluginManager.PullResult(0);
    pluginManager.CreateWriter("", 0, -1);
    pluginManager.ResetWriter(-1);

    PluginModule pluginModule;
    pluginModule.ComputeSha256();
    pluginModule.Unload();
    pluginModule.GetInfo(info);
    std::string str("memory-plugin");
    pluginModule.GetPluginName(str);
    uint32_t num = 0;
    pluginModule.GetBufferSizeHint(num);
    pluginModule.IsLoaded();

    BufferWriter bufferWriter("", 0, -1, nullptr, 0);
    bufferWriter.Write(nullptr, 0);
    bufferWriter.Flush();
}
} // namespace

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

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "common_types.pb.h"
#include "plugin_service.h"
#include "plugin_service.ipc.h"
#include "profiler_data_repeater.h"
#include "profiler_service_types.pb.h"
#include "socket_context.h"

using namespace testing::ext;

namespace {
std::unique_ptr<PluginService> g_pluginService;
uint32_t g_pluginId;

class PluginClientTest final : public IPluginServiceClient {
public:
    bool OnGetCommandResponse(SocketContext& context, ::GetCommandResponse& response) override
    {
        return true;
    }
};

std::unique_ptr<PluginClientTest> g_pluginClient;
class UnitTestPluginService : public testing::Test {
public:
    static void SetUpTestCase()
    {
        g_pluginService = std::make_unique<PluginService>();
        usleep(100000); // sleep for 100000 us.
        g_pluginClient = std::make_unique<PluginClientTest>();
        ASSERT_FALSE(g_pluginClient->Connect(""));
    }

    static void TearDownTestCase()
    {
        g_pluginClient = nullptr;
        g_pluginService = nullptr;
    }
    void SetUp() {}
    void TearDown() {}
};

/**
 * @tc.name: Service
 * @tc.desc: Set plugin registration information.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, AddPluginInfo, TestSize.Level1)
{
    RegisterPluginRequest request;
    RegisterPluginResponse response;

    request.set_request_id(1);
    request.set_path("abc.so");
    request.set_sha256("asdfasdfasdfasfd");
    request.set_name("abc.so");
    ASSERT_TRUE(response.status() == 0);
    g_pluginId = response.plugin_id();
}

/**
 * @tc.name: Service
 * @tc.desc: Set plugin configuration information.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, CreatePluginSession1, TestSize.Level1)
{
    ProfilerPluginConfig ppc;
    ppc.set_name("abc.so");
    ppc.set_plugin_sha256("ASDFAADSF");
    ppc.set_sample_interval(20);

    g_pluginService->CreatePluginSession(ppc, std::make_shared<ProfilerDataRepeater>(4096));
}

/**
 * @tc.name: Service
 * @tc.desc: Set session configuration.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, CreatePluginSession2, TestSize.Level1)
{
    ProfilerPluginConfig ppc;
    ppc.set_name("abc.so");
    ppc.set_plugin_sha256("ASDFAADSF");
    ppc.set_sample_interval(20);

    ProfilerSessionConfig::BufferConfig bc;
    bc.set_pages(1);
    bc.set_policy(ProfilerSessionConfig_BufferConfig_Policy_RECYCLE);

    g_pluginService->CreatePluginSession(ppc, bc, std::make_shared<ProfilerDataRepeater>(4096));
}

/**
 * @tc.name: Service
 * @tc.desc: Start plugin session.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, StartPluginSession, TestSize.Level1)
{
    ProfilerPluginConfig ppc;
    ppc.set_name("abc.so");
    ppc.set_plugin_sha256("ASDFAADSF");
    ppc.set_sample_interval(20);

    g_pluginService->StartPluginSession(ppc);
}

/**
 * @tc.name: Service
 * @tc.desc: Stop plugin session.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, StopPluginSession, TestSize.Level1)
{
    g_pluginService->StopPluginSession("abc.so");
}

/**
 * @tc.name: Service
 * @tc.desc: Destroy receiving test.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, DestroyPluginSession, TestSize.Level1)
{
    g_pluginService->DestroyPluginSession("abc.so");
}

/**
 * @tc.name: Service
 * @tc.desc: Remove the specified plugin.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, RemovePluginInfo, TestSize.Level1)
{
    UnregisterPluginRequest request;
    PluginInfo pi;
    pi.id = 0;
    pi.name = "abc.so";
    pi.path = "abc.so";
    pi.sha256 = "asdfasdf";
    g_pluginService->RemovePluginInfo(pi);
}

/**
 * @tc.name: Service
 * @tc.desc: Setting report results.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, AppendResult, TestSize.Level1)
{
    NotifyResultRequest nrr;
    nrr.set_request_id(1);
    nrr.set_command_id(1);
}

/**
 * @tc.name: Service
 * @tc.desc: Get plugin status.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, GetPluginStatus, TestSize.Level1)
{
    auto status = g_pluginService->GetPluginStatus();
}

/**
 * @tc.name: Service
 * @tc.desc: Gets the plugin with the specified name.
 * @tc.type: FUNC
 */
HWTEST_F(UnitTestPluginService, GetPluginIdByName, TestSize.Level1)
{
    g_pluginService->GetPluginIdByName("abc.so");
}
} // namespace
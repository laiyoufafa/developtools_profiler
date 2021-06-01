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

#include "plugin_service.h"
#include "plugin_session.h"
#include "profiler_data_repeater.h"

using namespace testing::ext;
using PluginServicePtr = STD_PTR(shared, PluginService);

class ServicesPluginServiceTest : public ::testing::Test {
protected:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
};

/**
 * @tc.name: plugin
 * @tc.desc: Session flow test, get session id by plugin name.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesPluginServiceTest, PluginService1, TestSize.Level1)
{
    PluginServicePtr pluginService = std::make_shared<PluginService>();
    ProfilerPluginConfig ppc;
    ppc.set_name("abc.so");
    ppc.set_plugin_sha256("ASDFAADSF");
    ppc.set_sample_interval(20);

    pluginService->CreatePluginSession(ppc, std::make_shared<ProfilerDataRepeater>(4096));
    pluginService->StartPluginSession(ppc);
    pluginService->StopPluginSession("abc.so");
    pluginService->DestroyPluginSession("abc.so");
    pluginService->GetPluginIdByName("abc.so");
}

/**
 * @tc.name: plugin
 * @tc.desc: Session flow test,  get plugin status.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesPluginServiceTest, PluginService2, TestSize.Level1)
{
    PluginServicePtr pluginService = std::make_shared<PluginService>();
    ProfilerPluginConfig ppc;
    ppc.set_name("abc.so");
    ppc.set_plugin_sha256("ASDFAADSF");
    ppc.set_sample_interval(20);

    ProfilerSessionConfig::BufferConfig bc;
    bc.set_pages(1);
    bc.set_policy(ProfilerSessionConfig_BufferConfig_Policy_RECYCLE);

    pluginService->CreatePluginSession(ppc, bc, std::make_shared<ProfilerDataRepeater>(4096));
    pluginService->StartPluginSession(ppc);
    pluginService->StopPluginSession("abc.so");
    pluginService->GetPluginStatus();

    PluginInfo pi;
    pi.id = 0;
    pi.name = "abc.so";
    pi.path = "abc.so";
    pi.sha256 = "asdfasdf";
    pluginService->RemovePluginInfo(pi);
}

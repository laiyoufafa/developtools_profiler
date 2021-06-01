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

#include "plugin_service.ipc.h"
#include "service_entry.h"

using namespace testing::ext;
namespace {
class ServiceEntryTest : public testing::Test {
public:
    static void SetUpTestCase() {}

    static void TearDownTestCase() {}
    void SetUp() {}
    void TearDown() {}
};

/**
 * @tc.name: Service
 * @tc.desc: Server process monitoring.
 * @tc.type: FUNC
 */
HWTEST_F(ServiceEntryTest, AllCase, TestSize.Level1)
{
    ServiceEntry serviceEntry;
    IPluginServiceServer pluginService;
    serviceEntry.StartServer("test_unix_socket_service_entry");
    serviceEntry.RegisterService(pluginService);
    serviceEntry.FindServiceByName(pluginService.serviceName_);

    usleep(30000);

    IPluginServiceClient pluginClient;
    ASSERT_FALSE(pluginClient.Connect(""));

    usleep(30000);
}

/**
 * @tc.name: Service
 * @tc.desc: Gets the time in milliseconds.
 * @tc.type: FUNC
 */
HWTEST_F(ServiceEntryTest, GetTimeMS, TestSize.Level1)
{
    GetTimeMS();
}

/**
 * @tc.name: Service
 * @tc.desc: Gets the time in microseconds.
 * @tc.type: FUNC
 */
HWTEST_F(ServiceEntryTest, GetTimeUS, TestSize.Level1)
{
    GetTimeUS();
}

/**
 * @tc.name: Service
 * @tc.desc: Gets the time in nanoseconds.
 * @tc.type: FUNC
 */
HWTEST_F(ServiceEntryTest, GetTimeNS, TestSize.Level1)
{
    GetTimeNS();
}
} // namespace
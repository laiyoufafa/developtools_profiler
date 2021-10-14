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

#include "plugin_service.ipc.h"
#include "service_entry.h"
#include "unix_socket_client.h"
#include "unix_socket_server.h"

using namespace testing::ext;

namespace {
class ServicesIpcTest : public ::testing::Test {
protected:
    static void SetUpTestCase()
    {
        setenv("RECV_NO_WAIT", "1", 0);
    }
    static void TearDownTestCase()
    {
        unsetenv("RECV_NO_WAIT");
    }
};

/**
 * @tc.name: Service
 * @tc.desc: Socket send/recv interface..
 * @tc.type: FUNC
 */
HWTEST_F(ServicesIpcTest, ProtocolProc, TestSize.Level1)
{
    std::string s="abc";
    ServiceBase serviceBase;
    SocketContext socketContext;
    ASSERT_FALSE(serviceBase.ProtocolProc(socketContext, 0, (const int8_t *)s.c_str(), s.size()));
    ASSERT_TRUE(!socketContext.SendRaw(-1, (const int8_t *)s.c_str(), s.size(), 0));
    ASSERT_TRUE(!socketContext.SendRaw(-1, (const int8_t *)s.c_str(), s.size(), -1));
    ASSERT_TRUE(!socketContext.SendRaw(-1, (const int8_t *)s.c_str(), s.size(), 1));
    ASSERT_TRUE(!socketContext.SendRaw(1, (const int8_t *)s.c_str(), s.size(), 1));
    ASSERT_TRUE(!socketContext.SendFileDescriptor(-1));
    ASSERT_TRUE(!socketContext.SendFileDescriptor(1));
    ASSERT_EQ(socketContext.ReceiveFileDiscriptor(), -1);
    ASSERT_EQ(socketContext.RawProtocolProc(1, (const int8_t *)s.c_str(), s.size()), -1);
}

/**
 * @tc.name: Service
 * @tc.desc: Abnormal client link.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesIpcTest, unixSocketClient, TestSize.Level1)
{
    UnixSocketClient unixSocketClient;
    ServiceBase serviceBase;
    ASSERT_TRUE(!unixSocketClient.Connect("asdf", serviceBase));
}

/**
 * @tc.name: Service
 * @tc.desc: Start unixSocket Server.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesIpcTest, UnixSocketServer, TestSize.Level1)
{
    UnixSocketServer unixSocketServer;

    unixSocketServer.UnixSocketAccept();

    ServiceEntry serviceEntry;
    ASSERT_TRUE(unixSocketServer.StartServer("test_server_name", serviceEntry));
}

namespace {
const int SLEEP_TIME = 30000;
} // namespace

/**
 * @tc.name: Service
 * @tc.desc: Server process monitoring.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesIpcTest, ServiceEntry, TestSize.Level1)
{
    ServiceEntry serviceEntry;
    IPluginServiceServer pluginService;
    serviceEntry.StartServer("test_unix_socket_service_entry");
    serviceEntry.RegisterService(pluginService);
    serviceEntry.FindServiceByName(pluginService.serviceName_);

    usleep(SLEEP_TIME);

    IPluginServiceClient pluginClient;
    ASSERT_FALSE(pluginClient.Connect("invalid_name"));
    ASSERT_TRUE(pluginClient.Connect("test_unix_socket_service_entry"));
    usleep(SLEEP_TIME);
}
} // namespace
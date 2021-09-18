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
#include <thread>

#include "client_map.h"
#include "plugin_service.ipc.h"
#include "service_entry.h"
#include "socket_context.h"
#include "unix_socket_client.h"
#include "unix_socket_server.h"

using namespace testing::ext;

namespace {
class ServicesIpcTest : public ::testing::Test {
protected:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
};

HWTEST_F(ServicesIpcTest, ProtocolProc, TestSize.Level1)
{
    ServiceBase serviceBase;
    SocketContext socketContext;
    ASSERT_FALSE(serviceBase.ProtocolProc(socketContext, 0, nullptr, 0));
    ASSERT_TRUE(!socketContext.SendRaw(-1, nullptr, 0, 0));
    ASSERT_TRUE(!socketContext.SendFileDescriptor(-1));
    ASSERT_EQ(socketContext.ReceiveFileDiscriptor(), -1);
    ASSERT_EQ(socketContext.RawProtocolProc(-1, nullptr, -1), -1);
}

HWTEST_F(ServicesIpcTest, ClientSocket, TestSize.Level1)
{
    ServiceEntry serviceEntry;
    ClientMap::GetInstance().PutClientSocket(0, serviceEntry);
    ASSERT_EQ(ClientMap::GetInstance().AutoRelease(), 1);

    ClientConnection* clientConnection = new ClientConnection(0, serviceEntry);
    ASSERT_EQ(clientConnection->RawProtocolProc(-1, nullptr, 0), -1);
}

HWTEST_F(ServicesIpcTest, unixSocketClient, TestSize.Level1)
{
    UnixSocketClient unixSocketClient;
    ServiceBase serviceBase;
    ASSERT_TRUE(!unixSocketClient.Connect("asdf", serviceBase));
}

HWTEST_F(ServicesIpcTest, UnixSocketServer, TestSize.Level1)
{
    UnixSocketServer unixSocketServer;

    unixSocketServer.UnixSocketAccept();

    ServiceEntry serviceEntry;
    ASSERT_TRUE(unixSocketServer.StartServer("", serviceEntry));
}

HWTEST_F(ServicesIpcTest, ServiceEntry, TestSize.Level1)
{
    ServiceEntry serviceEntry;
    IPluginServiceServer pluginService;
    serviceEntry.StartServer("test_unix_socket_service_entry");
    serviceEntry.RegisterService(pluginService);
    serviceEntry.FindServiceByName(pluginService.serviceName_);

    usleep(30000);

    GetTimeMS();
    GetTimeUS();
    GetTimeNS();

    IPluginServiceClient pluginClient;
    ASSERT_FALSE(pluginClient.Connect(""));
    usleep(30000);
}
} // namespace
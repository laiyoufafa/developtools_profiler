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

#include <fcntl.h>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <semaphore.h>
#include <sys/wait.h>

#include "client_map.h"
#include "plugin_service.ipc.h"
#include "service_entry.h"
#include "share_memory_allocator.h"
#include "socket_context.h"
#include "unix_socket_client.h"
#include "unix_socket_server.h"

using namespace testing::ext;

namespace {
class PluginServiceTest final : public IPluginServiceServer {
public:
    int fileDescriptor_;
    bool GetCommand(SocketContext& context, ::GetCommandRequest& request, ::GetCommandResponse& response) override
    {
        SendResponseGetCommandResponse(context, response);
        context.SendFileDescriptor(fileDescriptor_);
        return false;
    }
};

class PluginClientTest final : public IPluginServiceClient {
public:
    int fileDescriptor_;
    bool OnGetCommandResponse(SocketContext& context, ::GetCommandResponse& response) override
    {
        fileDescriptor_ = context.ReceiveFileDiscriptor();
        return true;
    }
};

class SharedMemoryAllocatorTest : public testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() {}
    void TearDown() {}
};

/**
 * @tc.name: Service
 * @tc.desc: Creates a memory block of the specified size.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, CreateMemoryBlockLocal, TestSize.Level1)
{
    pid_t pid1 = fork();
    ASSERT_TRUE(pid1 >= 0);
    if (pid1 > 0) {
        waitpid(pid1, nullptr, 0);
        return;
    }

    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1) ==
                nullptr); // 创建内存块大小<1024，返回空
    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1024) != nullptr); // 成功创建
    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1024) ==
                nullptr); // 创建同名内存块返回空
    exit(0);
}

/**
 * @tc.name: Service
 * @tc.desc: Find memory block by name.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, FindMBByName, TestSize.Level1)
{
    pid_t pid1 = fork();
    ASSERT_TRUE(pid1 >= 0);
    if (pid1 > 0) {
        waitpid(pid1, nullptr, 0);
        return;
    }

    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().FindMBByName("err") == nullptr); // 查找不存在的内存块返回空

    exit(0);
}

/**
 * @tc.name: Service
 * @tc.desc: Create a memory block with a nonexistent file descriptor.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, CreateMemoryBlockRemote, TestSize.Level1)
{
    pid_t pid1 = fork();
    ASSERT_TRUE(pid1 >= 0);
    if (pid1 > 0) {
        waitpid(pid1, nullptr, 0);
        return;
    }

    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockRemote("err", 1024, 99) ==
                nullptr); // 使用不存在的文件描述符映射内存块返回空
    exit(0);
}

/**
 * @tc.name: Service
 * @tc.desc: Gets the size of the memory block with the specified name.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, GetDataSize, TestSize.Level1)
{
    pid_t pid1 = fork();
    ASSERT_TRUE(pid1 >= 0);
    if (pid1 > 0) {
        waitpid(pid1, nullptr, 0);
        return;
    }

    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1024) != nullptr); // 成功创建
    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().FindMBByName("testname")->GetDataSize() == 0);

    exit(0);
}

/**
 * @tc.name: Service
 * @tc.desc: Free a nonexistent memory block.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, ReleaseMemoryBlockLocal, TestSize.Level1)
{
    pid_t pid1 = fork();
    ASSERT_TRUE(pid1 >= 0);
    if (pid1 > 0) {
        waitpid(pid1, nullptr, 0);
        return;
    }

    ASSERT_FALSE(ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockLocal("or")); // 释放不存在的内存块返回-1
    exit(0);
}

/**
 * @tc.name: Service
 * @tc.desc: Free a nonexistent remote memory block.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, ReleaseMemoryBlockRemote, TestSize.Level1)
{
    pid_t pid1 = fork();
    ASSERT_TRUE(pid1 >= 0);
    if (pid1 > 0) {
        waitpid(pid1, nullptr, 0);
        return;
    }

    ASSERT_FALSE(ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockRemote("or")); // 释放不存在的内存块返回-1
    exit(0);
}

/**
 * @tc.name: Service
 * @tc.desc: Get command.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, GetCommand, TestSize.Level1)
{
    SocketContext socketContext;
    GetCommandRequest request;
    GetCommandResponse commandResponse;
    PluginServiceTest pluginServiceTest;
    ASSERT_FALSE(pluginServiceTest.GetCommand(socketContext, request, commandResponse));
}

/**
 * @tc.name: Service
 * @tc.desc: Socket send/recv interface.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, ProtocolProc, TestSize.Level1)
{
    ServiceBase serviceBase;
    SocketContext socketContext;
    ASSERT_FALSE(serviceBase.ProtocolProc(socketContext, 0, nullptr, 0));
}

/**
 * @tc.name: Service
 * @tc.desc:  Abnormal socket detection.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, RawProtocolProc, TestSize.Level1)
{
    ServiceEntry serviceEntry;
    ClientConnection* clientConnection = new ClientConnection(0, serviceEntry);
    ASSERT_EQ(clientConnection->RawProtocolProc(-1, nullptr, 0), -1);

    SocketContext socketContext;
    ASSERT_EQ(socketContext.RawProtocolProc(-1, nullptr, -1), -1);
    ASSERT_TRUE(!socketContext.SendRaw(-1, nullptr, 0, 0));
}

/**
 * @tc.name: Service
 * @tc.desc: Client link.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, ClientSocket, TestSize.Level1)
{
    ServiceEntry serviceEntry;
    ClientMap::GetInstance();
    ClientMap::GetInstance().PutClientSocket(0, serviceEntry);
    ASSERT_EQ(ClientMap::GetInstance().AutoRelease(), 1);
}

/**
 * @tc.name: Service
 * @tc.desc: Abnormal client link.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, unixSocketClient, TestSize.Level1)
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
HWTEST_F(SharedMemoryAllocatorTest, UnixSocketServer, TestSize.Level1)
{
    UnixSocketServer unixSocketServer;

    unixSocketServer.UnixSocketAccept(nullptr);

    ServiceEntry serviceEntry;
    ASSERT_TRUE(unixSocketServer.StartServer("", serviceEntry));
}

/**
 * @tc.name: Service
 * @tc.desc: Server process monitoring.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryAllocatorTest, ServiceEntry, TestSize.Level1)
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

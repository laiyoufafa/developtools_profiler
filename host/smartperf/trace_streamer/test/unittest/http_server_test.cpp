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
#include <netinet/in.h>
#include <sys/socket.h>

#include "http_server.h"
#include "http_socket.h"
#include "rpc/rpc_server.h"
#include "string_help.h"

using namespace testing::ext;
namespace SysTuning {
namespace TraceStreamer {
#define UNUSED(expr)  \
do {              \
    static_cast<void>(expr); \
} while (0)

const uint32_t MAX_TESET_BUF_SIZE = 1024;
std::string g_parserData(
    "ACCS0-2716  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3");
std::string g_sqlQuery("select * from measure;");
std::string g_sqlOPerateInsert("insert into measure values (1, 1, 1, 1);");
std::string g_sqlOPerateDelete("delete from measure;");
char g_clientRecvBuf[MAX_TESET_BUF_SIZE] = {0};
class HttpServerTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }
    void TearDown() {}

public:
    TraceStreamerSelector stream_ = {};
};

void ResultCallbackFunc(const std::string result)
{
    // unused
    UNUSED(result);
}

void* HttpServerThread(void* arg)
{
    HttpServer* httpServer = static_cast<HttpServer*>(arg);
    httpServer->Run();
    TS_LOGI("Server thread end");
    pthread_exit(nullptr);
}

int HttpClient(const char* buf)
{
    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htons(INADDR_ANY);
    const uint16_t listenPort = 9001;
    addr.sin_port = htons(listenPort);
    struct timeval recvTimeout = {1, 100000};

    int sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        TS_LOGI("CreateSocket socket error");
        return -1;
    }

    int ret = connect(sockfd, (struct sockaddr*)(&addr), sizeof(struct sockaddr));
    if (ret < 0) {
        TS_LOGE("Connect error");
        return -1;
    }

    ret = send(sockfd, buf, strlen(buf), 0);
    if (ret < 0) {
        TS_LOGE("Send error");
        return -1;
    }

    if (!memset_s(g_clientRecvBuf, strlen(g_clientRecvBuf), 0, strlen(g_clientRecvBuf))) {
        TS_LOGE("memset_s error");
        return -1;
    }
    int index = 0;
    ret = setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (char*)(&recvTimeout), sizeof(recvTimeout));
    if (ret != 0) {
        TS_LOGE("set recv time out error");
        return -1;
    }
    while (1) {
        ret = recv(sockfd, g_clientRecvBuf + index, MAX_TESET_BUF_SIZE, 0);
        if (ret < 0) {
            TS_LOGE("Recv timeout");
            break;
        }
        index += ret;
    }
    return 0;
}

/**
 * @tc.name: HttpCorrectRequest
 * @tc.desc: HTTP correct request
 * @tc.type: FUNC
 */
HWTEST_F(HttpServerTest, HttpCorrectRequest, TestSize.Level1)
{
    TS_LOGI("test15-1");
    HttpServer httpServer;
    RpcServer rpcServer;
    pthread_t pthreadId = 0;
    int ret = 0;

    ret = rpcServer.ParseData((const uint8_t*)g_parserData.c_str(), g_parserData.length(), ResultCallbackFunc);
    ret = rpcServer.ParseDataOver(nullptr, 0, ResultCallbackFunc);
    ret = rpcServer.SqlOperate((const uint8_t*)g_sqlOPerateInsert.c_str(),
                               g_sqlOPerateInsert.length(), ResultCallbackFunc);
    ret = rpcServer.SqlQuery((const uint8_t*)g_sqlQuery.c_str(), g_sqlQuery.length(), ResultCallbackFunc);

    httpServer.RegisterRpcFunction(&rpcServer);
    ret = pthread_create(&pthreadId, nullptr, HttpServerThread, &httpServer);
    if (ret != 0) {
        TS_LOGE("Server pthread create fail");
        pthread_exit(nullptr);
    }

    sleep(1);
    char bufToSend[MAX_TESET_BUF_SIZE] = {0};
    sprintf(bufToSend,
            "GET /sqlquery HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Length:\
                23\r\n\r\nselect * from measure\r\n");

    ret = HttpClient(bufToSend);
    if (ret < 0) {
        TS_LOGE("Client fail");
    }
    httpServer.Exit();
    ret = pthread_join(pthreadId, nullptr);
    if (ret != 0) {
        TS_LOGE("Server pthread jion fail");
    }
    char targetStr[MAX_TESET_BUF_SIZE] = {
        "HTTP/1.1 200 OK\r\nConnection: Keep-Alive\r\nContent-Type: "
        "application/json\r\nTransfer-Encoding: chunked\r\nContent-Length:"
        "72\r\n\r\nok\r\n{\"columns\":[\"type\",\"ts\",\"value\",\"filter_id\"],\"values\":[[1,1,1,1]]}\r\n"};

    ret = rpcServer.SqlOperate((const uint8_t*)g_sqlOPerateDelete.c_str(),
                               g_sqlOPerateDelete.length(), ResultCallbackFunc);
    ret = rpcServer.SqlQuery((const uint8_t*)g_sqlQuery.c_str(), g_sqlQuery.length(), ResultCallbackFunc);

    EXPECT_STREQ(targetStr, g_clientRecvBuf);
}
/**
 * @tc.name: OthreAgreement
 * @tc.desc: Use http1 1. Agreements other than agreements
 * @tc.type: FUNC
 */
HWTEST_F(HttpServerTest, OthreAgreement, TestSize.Level1)
{
    TS_LOGI("test15-2");
    HttpServer httpServer;
    RpcServer rpcServer;
    pthread_t pthreadId = 0;
    int ret = 0;

    httpServer.RegisterRpcFunction(&rpcServer);
    ret = pthread_create(&pthreadId, nullptr, HttpServerThread, &httpServer);
    if (ret != 0) {
        TS_LOGE("Server pthread create fail");
        pthread_exit(nullptr);
    }

    sleep(1);
    char bufToSend[MAX_TESET_BUF_SIZE] = {0};
    sprintf(bufToSend,
            "GET /sqlquery HTTP/0.9\r\nHost: 127.0.0.1\r\nContent-Length:\
                    23\r\n\r\nselect * from measure\r\n");

    ret = HttpClient(bufToSend);
    if (ret < 0) {
        TS_LOGE("Client fail");
    }
    httpServer.Exit();
    ret = pthread_join(pthreadId, nullptr);
    if (ret != 0) {
        TS_LOGE("Server pthread jion fail");
    }
    char targetStr[MAX_TESET_BUF_SIZE] = {"HTTP/1.1 400 Bad Request\r\nConnection: Keep-Alive\r\n\r\n"};
    EXPECT_STREQ(targetStr, g_clientRecvBuf);
}

/**
 * @tc.name: OthreProtocols
 * @tc.desc: Use protocols other than GET and POST
 * @tc.type: FUNC
 */
HWTEST_F(HttpServerTest, OthreProtocols, TestSize.Level1)
{
    TS_LOGI("test15-3");
    HttpServer httpServer;
    RpcServer rpcServer;
    pthread_t pthreadId = 0;
    int ret = 0;

    httpServer.RegisterRpcFunction(&rpcServer);
    ret = pthread_create(&pthreadId, nullptr, HttpServerThread, &httpServer);
    if (ret != 0) {
        TS_LOGE("Server pthread create fail");
        pthread_exit(nullptr);
    }

    sleep(1);
    char bufToSend[MAX_TESET_BUF_SIZE] = {0};
    sprintf(bufToSend,
            "HEAD /sqlquery HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Length:\
                23\r\n\r\nselect * from measure\r\n");

    ret = HttpClient(bufToSend);
    if (ret < 0) {
        TS_LOGE("Client fail");
    }
    httpServer.Exit();
    ret = pthread_join(pthreadId, nullptr);
    if (ret != 0) {
        TS_LOGE("Server pthread jion fail");
    }
    char targetStr[MAX_TESET_BUF_SIZE] = {"HTTP/1.1 405 Method Not Allowed\r\nConnection: Keep-Alive\r\n\r\n"};
    EXPECT_STREQ(targetStr, g_clientRecvBuf);
}

/**
 * @tc.name: RequestLineFormatError
 * @tc.desc: Request line format error
 * @tc.type: FUNC
 */
HWTEST_F(HttpServerTest, RequestLineFormatError, TestSize.Level1)
{
    TS_LOGI("test15-4");
    HttpServer httpServer;
    RpcServer rpcServer;
    pthread_t pthreadId = 0;
    int ret = 0;

    httpServer.RegisterRpcFunction(&rpcServer);
    ret = pthread_create(&pthreadId, nullptr, HttpServerThread, &httpServer);
    if (ret != 0) {
        TS_LOGE("Server pthread create fail");
        pthread_exit(nullptr);
    }

    sleep(1);
    char bufToSend[MAX_TESET_BUF_SIZE] = {0};
    sprintf(bufToSend,
            "POST /sqlqueryHTTP/0.9\r\nHost: 127.0.0.1\r\nContent-Length:\
                20\r\n\r\nselect * from meta\r\n");

    ret = HttpClient(bufToSend);
    if (ret < 0) {
        TS_LOGE("Client fail");
    }
    httpServer.Exit();
    ret = pthread_join(pthreadId, nullptr);
    if (ret != 0) {
        TS_LOGE("Server pthread jion fail");
    }
    char targetStr[MAX_TESET_BUF_SIZE] = {"HTTP/1.1 400 Bad Request\r\nConnection: Keep-Alive\r\n\r\n"};
    EXPECT_STREQ(targetStr, g_clientRecvBuf);
}

/**
 * @tc.name: RequestIsNotRPC
 * @tc.desc: The URI of HTTP request is not the method of RPC
 * @tc.type: FUNC
 */
HWTEST_F(HttpServerTest, RequestOverHttp, TestSize.Level1)
{
    TS_LOGI("test15-5");
    HttpServer httpServer;
    RpcServer rpcServer;
    pthread_t pthreadId = 0;
    int ret = 0;

    httpServer.RegisterRpcFunction(&rpcServer);
    ret = pthread_create(&pthreadId, nullptr, HttpServerThread, &httpServer);
    if (ret != 0) {
        TS_LOGE("Server pthread create fail");
        pthread_exit(nullptr);
    }

    sleep(1);
    char bufToSend[MAX_TESET_BUF_SIZE] = {0};
    sprintf(bufToSend,
            "POST /query HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Length:20\r\n\r\n\
            select * from meta\r\n");

    ret = HttpClient(bufToSend);
    if (ret < 0) {
        TS_LOGE("Client fail");
    }
    httpServer.Exit();
    ret = pthread_join(pthreadId, nullptr);
    if (ret != 0) {
        TS_LOGE("Server pthread jion fail");
    }
    char targetStr[MAX_TESET_BUF_SIZE] = {"HTTP/1.1 404 Not Found\r\nConnection: Keep-Alive\r\n\r\n"};
    EXPECT_STREQ(targetStr, g_clientRecvBuf);
}
/**
 * @tc.name: RequestTimeout
 * @tc.desc: Incomplete request content data
 * @tc.type: FUNC
 */
HWTEST_F(HttpServerTest, RequestOverHttpTimeout, TestSize.Level1)
{
    TS_LOGI("test15-6");
    HttpServer httpServer;
    RpcServer rpcServer;
    pthread_t pthreadId = 0;
    int ret = 0;

    httpServer.RegisterRpcFunction(&rpcServer);
    ret = pthread_create(&pthreadId, nullptr, HttpServerThread, &httpServer);
    if (ret != 0) {
        TS_LOGE("Server pthread create fail");
        pthread_exit(nullptr);
    }

    sleep(1);
    std::string buf =
        "GET /sqlquery HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Length:\
                28\r\n\r\nselect * from measure\r\n";

    ret = HttpClient(buf.c_str());
    if (ret < 0) {
        TS_LOGE("Client fail");
    }
    httpServer.Exit();
    ret = pthread_join(pthreadId, nullptr);
    if (ret != 0) {
        TS_LOGE("Server pthread jion fail");
    }
    char targetStr[MAX_TESET_BUF_SIZE] = {"HTTP/1.1 408 Request Time-out\r\nConnection: Keep-Alive\r\n\r\n"};
    EXPECT_STREQ(targetStr, g_clientRecvBuf);
}
} // namespace TraceStreamer
} // namespace SysTuning
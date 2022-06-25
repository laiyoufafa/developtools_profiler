/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
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
#ifndef SP_SERVERSOCKET_H
#define SP_SERVERSOCKET_H
#include <iostream>
#include <netinet/in.h>
namespace OHOS {
namespace SmartPerf {
class SpServerSocket {
public:
    SpServerSocket();
    ~SpServerSocket();
    // 创建一个套接字
    int Init();
    // IO操作
    int Sendto(std::string &sendBuf);
    int Recvfrom();
    // 关闭
    void Close();
    std::string RecvBuf();

private:
    int sock;
    struct sockaddr_in local;
    struct sockaddr_in client;
    const int SOCK_PORT = 8283;
    const static int BUFF_SIZE_RECV = 256;
    char rbuf[BUFF_SIZE_RECV];
};
}
}

#endif // !SPSERVERSOCKET
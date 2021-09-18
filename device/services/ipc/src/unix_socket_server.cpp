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

#include "unix_socket_server.h"

#include <cstdio>
#include <linux/un.h>
#include <pthread.h>
#include <sys/epoll.h>
#include <sys/socket.h>
#include <unistd.h>

#include "client_map.h"
#include "logging.h"
#include "securec.h"

UnixSocketServer::UnixSocketServer()
{
    sAddrName_ = "";
    socketHandle_ = -1;
    serviceEntry_ = nullptr;
}

UnixSocketServer::~UnixSocketServer()
{
    if (socketHandle_ != 1) {
        close(socketHandle_);
        socketHandle_ = -1;
        unlink(sAddrName_.c_str());
    }
    if (acceptThread_.joinable()) {
        acceptThread_.join();
    }
}

void UnixSocketServer::UnixSocketAccept()
{
    pthread_setname_np(pthread_self(), "UnixSocketAccept");

    CHECK_TRUE(socketHandle_ != -1, NO_RETVAL, "Unix Socket Accept socketHandle_ == -1");
    int epfd = epoll_create(1);
    struct epoll_event evt;
    evt.data.fd = socketHandle_;
    evt.events = EPOLLIN | EPOLLET;
    CHECK_TRUE(epoll_ctl(epfd, EPOLL_CTL_ADD, socketHandle_, &evt) != -1, NO_RETVAL, "Unix Socket Server Exit");
    while (socketHandle_ != -1) {
        int nfds = epoll_wait(epfd, &evt, 1, 1000); // timeout value set 1000.
        if (nfds > 0) {
            int clientSocket = accept(socketHandle_, nullptr, nullptr);
            HILOG_INFO(LOG_CORE, "Accept A Client %d", clientSocket);
            ClientMap::GetInstance().PutClientSocket(clientSocket, *serviceEntry_);
        }
    }
    close(epfd);
}

namespace {
const int UNIX_SOCKET_LISTEN_COUNT = 5;
}
bool UnixSocketServer::StartServer(const std::string& addrname, ServiceEntry& p)
{
    CHECK_TRUE(socketHandle_ == -1, false, "StartServer FAIL socketHandle_ != -1");

    struct sockaddr_un addr;
    int sock = socket(AF_UNIX, SOCK_STREAM, 0);
    CHECK_TRUE(sock != -1, false, "StartServer FAIL create socket ERR : %s", strerror(errno));

    if (memset_s(&addr, sizeof(struct sockaddr_un), 0, sizeof(struct sockaddr_un)) != EOK) {
        HILOG_ERROR(LOG_CORE, "memset_s error!");
    }
    addr.sun_family = AF_UNIX;
    if (strncpy_s(addr.sun_path, sizeof(addr.sun_path), addrname.c_str(), sizeof(addr.sun_path) - 1) != EOK) {
        HILOG_ERROR(LOG_CORE, "strncpy_s error!");
    }
    unlink(addrname.c_str());
    CHECK_TRUE(bind(sock, (struct sockaddr*)&addr, sizeof(struct sockaddr_un)) == 0, close(sock) != 0,
               "StartServer FAIL bind ERR : %s", strerror(errno));

    CHECK_TRUE(listen(sock, UNIX_SOCKET_LISTEN_COUNT) != -1, close(sock) != 0 && unlink(addrname.c_str()) == 0,
               "StartServer FAIL listen ERR : %s", strerror(errno));

    socketHandle_ = sock;
    acceptThread_ = std::thread(&UnixSocketServer::UnixSocketAccept, this);
    if (acceptThread_.get_id() == std::thread::id()) {
        close(socketHandle_);
        unlink(addrname.c_str());
        HILOG_ERROR(LOG_CORE, "StartServer FAIL pthread_create ERR : %s", strerror(errno));
        socketHandle_ = -1;
        return false;
    }

    serviceEntry_ = &p;
    sAddrName_ = addrname;
    return true;
}

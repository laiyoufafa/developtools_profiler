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

#ifndef SOCKET_CONTEXT_H
#define SOCKET_CONTEXT_H

#include <cstdint>
#include <google/protobuf/message.h>
#include <thread>

#if defined(__i386__) || defined(__x86_64__)
const static char DEFAULT_UNIX_SOCKET_PATH[] = "hiprofiler_unix_socket";
#else
const static char DEFAULT_UNIX_SOCKET_PATH[] = "/data/local/tmp/hiprofiler_unix_socket";
#endif

class SocketContext;
class ServiceBase;
class ClientMap;

enum ClientState {
    CLIENT_STAT_WORKING,
    CLIENT_STAT_WAIT_THREAD_EXIT,
    CLIENT_STAT_THREAD_EXITED,
};

enum RawProtocol {
    RAW_PROTOCOL_POINTTO_SERVICE = 1,
};
struct RawPointToService {
    char serviceName_[64];
};

class SocketContext {
public:
    SocketContext();
    virtual ~SocketContext();

    bool SendRaw(uint32_t pnum, const int8_t* data, uint32_t size, int sockfd = -1);
    bool SendProtobuf(uint32_t pnum, google::protobuf::Message& pmsg);
    bool SendFileDescriptor(int fd);
    int ReceiveFileDiscriptor();

protected:
    friend class ClientMap;
    int socketHandle_;
    bool CreateRecvThread();
    enum ClientState clientState_;
    uint64_t lastProcMS_;

    ServiceBase* serviceBase_;

    virtual int RawProtocolProc(uint32_t pnum, const int8_t* buf, const uint32_t size);

private:
    static void* UnixSocketRecv(void* pp);
    std::thread recvThread_;
};

#endif
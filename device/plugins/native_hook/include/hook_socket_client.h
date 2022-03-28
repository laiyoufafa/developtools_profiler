/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#ifndef HOOK_SOCKET_CLIENT
#define HOOK_SOCKET_CLIENT

#include "service_base.h"
#include "stack_writer.h"

extern uint32_t g_filterSize;

class UnixSocketClient;

class HookSocketClient : public ServiceBase {
public:
    HookSocketClient(int pid);
    ~HookSocketClient();
    bool Connect(const std::string addrname);
    bool ProtocolProc(SocketContext &context, uint32_t pnum, const int8_t *buf, const uint32_t size) override;
    int GetSmbFd()
    {
        return smbFd_;
    }
    int GetEventFd()
    {
        return eventFd_;
    }
    bool SendStack(const void* data, size_t size);
private:
    std::shared_ptr<UnixSocketClient> unixSocketClient_;
    int smbFd_;
    int eventFd_;
    int pid_;
    std::shared_ptr<StackWriter> stackWriter_;
};

#endif // HOOK_SOCKET_CLIENT
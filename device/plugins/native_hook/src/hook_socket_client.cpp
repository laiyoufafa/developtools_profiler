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

#include "hook_socket_client.h"

#include "hook_common.h"
#include "unix_socket_client.h"

namespace {
const int MOVE_BIT_8 = 8;
const int MOVE_BIT_32 = 32;
const int MOVE_BIT_48 = 48;
const int MOVE_BIT_56 = 56;
} // namespace

HookSocketClient::HookSocketClient(int pid) : pid_(pid)
{
    unixSocketClient_ = nullptr;
    serviceName_ = "HookService";
    mallocDisable_ = false;
    mmapDisable_ = false;
    freeStackData_ = false;
    munmapStackData_ = false;
    Connect(DEFAULT_UNIX_SOCKET_HOOK_PATH);
}

HookSocketClient::~HookSocketClient()
{
    unixSocketClient_ = nullptr;
    stackWriter_ = nullptr;
}

bool HookSocketClient::Connect(const std::string addrname)
{
    if (unixSocketClient_ != nullptr) {
        return false;
    }
    unixSocketClient_ = std::make_shared<UnixSocketClient>();
    if (!unixSocketClient_->Connect(addrname, *this)) {
        unixSocketClient_ = nullptr;
        return false;
    }

    unixSocketClient_->SendHookConfig(pid_);
    return true;
}

// config |F F        F F               F F F F       F F F F      F F F F|
//        malloctype  stack depth       filtersize    sharememory  size
bool HookSocketClient::ProtocolProc(SocketContext &context, uint32_t pnum, const int8_t *buf, const uint32_t size)
{
    if (size != sizeof(uint64_t)) {
        HILOG_ERROR(LOG_CORE, "HookSocketClient::config config size not match = %u\n", size);
        return true;
    }
    uint64_t config = *(uint64_t *)buf;
    uint32_t smbSize = (uint32_t)config;
    filterSize_ = (uint16_t)(config >> MOVE_BIT_32);

    uint16_t mask = (uint16_t)(config >> MOVE_BIT_48);
    maxStackDepth_ = (uint8_t)(mask >> MOVE_BIT_8);
    maxStackDepth_  = maxStackDepth_ > MAX_UNWIND_DEPTH ? MAX_UNWIND_DEPTH : maxStackDepth_;
    smbFd_ = context.ReceiveFileDiscriptor();
    eventFd_ = context.ReceiveFileDiscriptor();

    if (mask & MALLOCDISABLE) {
        mallocDisable_ = true;
    }
    if (mask & MMAPDISABLE) {
        mmapDisable_ = true;
    }
    if (mask & FREEMSGSTACK) {
        freeStackData_ = true;
    }
    if (mask & MUNMAPMSGSTACK) {
        munmapStackData_ = true;
    }
    if (mask & FPUNWIND) {
        fpunwind_ = true;
    }
    HILOG_INFO(LOG_CORE, "%s: mallocDisable = %d mmapDisable = %d", __func__, mallocDisable_, mmapDisable_);
    HILOG_INFO(LOG_CORE, "%s: freeStackData = %d munmapStackData = %d", __func__, freeStackData_, munmapStackData_);
    HILOG_INFO(LOG_CORE, "%s: filter size = %u smb size = %u", __func__, filterSize_, smbSize);
    HILOG_INFO(LOG_CORE, "%s: maxStackDepth = %u fpunwind = %d", __func__, maxStackDepth_, fpunwind_);
    stackWriter_ = std::make_shared<StackWriter>("hooknativesmb", smbSize, smbFd_, eventFd_);
    return true;
}

bool HookSocketClient::SendStack(const void* data, size_t size)
{
    if (stackWriter_ == nullptr || unixSocketClient_ == nullptr) {
        return true;
    }

    if (!unixSocketClient_->SendHeartBeat()) {
        return false;
    }

    stackWriter_->WriteTimeout(data, size);
    stackWriter_->Flush();

    return true;
}

bool HookSocketClient::SendStackWithPayload(const void* data, size_t size, const void* payload, size_t payloadSize)
{
    if (stackWriter_ == nullptr || unixSocketClient_ == nullptr) {
        return true;
    }

    if (!unixSocketClient_->SendHeartBeat()) {
        return false;
    }

    stackWriter_->WriteWithPayloadTimeout(data, size, payload, payloadSize);
    stackWriter_->Flush();

    return true;
}
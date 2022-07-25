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

#include "hook_service.h"

#include <cinttypes>

#include "logging.h"
#include "parameter.h"
#include "socket_context.h"

namespace {
const int BUF_MAX_LEN = 10;
}  // namespace

HookService::HookService(int smbFd,
                         int eventFd,
                         int pid,
                         std::string processName,
                         uint64_t config)
    : smbFd_(smbFd), eventFd_(eventFd), hookConfig_(config), pid_(pid), processName_(processName)
{
    serviceName_ = "HookService";
    StartService(DEFAULT_UNIX_SOCKET_HOOK_PATH);
}

HookService::~HookService()
{
    serviceEntry_ = nullptr;
}

bool HookService::StartService(const std::string& unixSocketName)
{
    serviceEntry_ = std::make_shared<ServiceEntry>();
    if (!serviceEntry_->StartServer(unixSocketName)) {
        serviceEntry_ = nullptr;
        HILOG_DEBUG(LOG_CORE, "Start IPC Service FAIL");
        return false;
    }
    serviceEntry_->RegisterService(*this);
    return true;
}

bool HookService::ProtocolProc(SocketContext &context, uint32_t pnum, const int8_t *buf, const uint32_t size)
{
    if (size != sizeof(uint64_t)) {
        HILOG_ERROR(LOG_CORE, "ProtocolProc hook config error");
    }
    uint64_t peerconfig = *const_cast<uint64_t *>(reinterpret_cast<const uint64_t *>(buf));

    if (peerconfig == -1u) {
        return true;
    }
    if (pid_ == 0) {
        // get target process from "param set libc.hook_mode startup:xxx"
        const int len = 128;
        char paramOutBuf[len] = {0};
        int ret = GetParameter("libc.hook_mode", "", paramOutBuf, len - 1);
        if (ret > 0) {
            std::string hookValue = paramOutBuf;
            if (hookValue.find("startup:") == 0) {
                processName_ = hookValue.substr(strlen("startup:"), hookValue.size());
            }
        }

        // check if the pid and process name is consistency
        std::string findpid = "pidof " + processName_;
        HILOG_INFO(LOG_CORE, "find pid command : %s", findpid.c_str());
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(findpid.c_str(), "r"), pclose);

        char line[LINE_SIZE];
        do {
            if (fgets(line, sizeof(line), pipe.get()) == nullptr) {
                return false;
            } else if (strlen(line) > 0 && isdigit((unsigned char)(line[0]))) {
                pid_ = (int)atoi(line);
                if (peerconfig != (uint64_t)pid_) {
                    return false;
                }
                printf("Process %" PRIu64 " hook started.\n", peerconfig);
                break;
            }
        } while (1);
    } else if (peerconfig != (uint64_t)pid_) {
        HILOG_ERROR(LOG_CORE, "ProtocolProc receive peerconfig %" PRIu64 " not expected", peerconfig);
        return false;
    }

    HILOG_DEBUG(LOG_CORE, "ProtocolProc, receive message from hook client, and send hook config to process %d", pid_);

    context.SendHookConfig(hookConfig_);
    context.SendFileDescriptor(smbFd_);
    context.SendFileDescriptor(eventFd_);
    return true;
}

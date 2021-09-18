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
#include "command_poller.h"
#include "buffer_writer.h"
#include "plugin_manager.h"
#include "socket_context.h"

#include <fcntl.h>
#include <unistd.h>

namespace {
constexpr int SLEEP_TIME = 10;
}

CommandPoller::CommandPoller(const ManagerInterfacePtr& p) : requestIdAutoIncrease_(1), pluginManager_(p)
{
    Connect(DEFAULT_UNIX_SOCKET_PATH);
}

CommandPoller::~CommandPoller() {}

uint32_t CommandPoller::GetRequestId()
{
    return requestIdAutoIncrease_++;
}

bool CommandPoller::OnCreateSessionCmd(const CreateSessionCmd& cmd, SocketContext& context) const
{
    HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd PROC");
    uint32_t bufferSize = cmd.buffer_sizes(0);
    ProfilerPluginConfig config = cmd.plugin_configs(0);
    std::vector<ProfilerPluginConfig> configVec;
    configVec.push_back(config);

    auto pluginManager = pluginManager_.lock(); // promote to shared_ptr
    CHECK_NOTNULL(pluginManager, false, "promote FAILED!");

    if (!pluginManager->LoadPlugin(config.name())) {
        HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd FAIL 1");
        return false;
    }
    int smbFd = -1;
    int eventFd = -1;
    if (bufferSize != 0) {
        HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd bufferSize = %d", bufferSize);
        smbFd = context.ReceiveFileDiscriptor();
        eventFd = context.ReceiveFileDiscriptor();
        int flags = fcntl(eventFd, F_GETFL);
        HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd smbFd = %d, eventFd = %d", smbFd, eventFd);
        HILOG_DEBUG(LOG_CORE, "eventFd flags = %X", flags);
    }
    if (!pluginManager->CreateWriter(config.name(), bufferSize, smbFd, eventFd)) {
        HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd CreateWriter FAIL");
        return false;
    }
    if (!pluginManager->CreatePluginSession(configVec)) {
        HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd CreatePluginSession FAIL");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "OnCreateSessionCmd OK");
    return true;
}

bool CommandPoller::OnDestroySessionCmd(const DestroySessionCmd& cmd) const
{
    HILOG_DEBUG(LOG_CORE, "OnDestroySessionCmd PROC");
    uint32_t pluginId = cmd.plugin_ids(0);
    std::vector<uint32_t> pluginIdVec;
    pluginIdVec.push_back(pluginId);

    auto pluginManager = pluginManager_.lock(); // promote to shared_ptr
    CHECK_NOTNULL(pluginManager, false, "promote FAILED!");

    if (!pluginManager->DestroyPluginSession(pluginIdVec)) {
        HILOG_DEBUG(LOG_CORE, "OnDestroySessionCmd DestroyPluginSession FAIL");
        return false;
    }
    if (!pluginManager->ResetWriter(pluginId)) {
        HILOG_DEBUG(LOG_CORE, "OnDestroySessionCmd ResetWriter FAIL");
        return false;
    }
    if (!pluginManager->UnloadPlugin(pluginId)) {
        HILOG_DEBUG(LOG_CORE, "OnDestroySessionCmd UnloadPlugin FAIL");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "OnDestroySessionCmd OK");
    return true;
}

bool CommandPoller::OnStartSessionCmd(const StartSessionCmd& cmd) const
{
    HILOG_DEBUG(LOG_CORE, "OnStartSessionCmd PROC");
    std::vector<uint32_t> pluginIds;
    pluginIds.push_back(cmd.plugin_ids(0));
    std::vector<ProfilerPluginConfig> configVec;
    configVec.push_back(cmd.plugin_configs(0));

    auto pluginManager = pluginManager_.lock(); // promote to shared_ptr
    CHECK_NOTNULL(pluginManager, false, "promote FAILED!");

    if (!pluginManager->StartPluginSession(pluginIds, configVec)) {
        HILOG_DEBUG(LOG_CORE, "OnStartSessionCmd FAIL");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "OnStartSessionCmd OK");
    return true;
}

bool CommandPoller::OnStopSessionCmd(const StopSessionCmd& cmd) const
{
    HILOG_DEBUG(LOG_CORE, "OnStopSessionCmd PROC");
    std::vector<uint32_t> pluginIds;
    pluginIds.push_back(cmd.plugin_ids(0));

    auto pluginManager = pluginManager_.lock(); // promote to shared_ptr
    CHECK_NOTNULL(pluginManager, false, "promote FAILED!");

    if (!pluginManager->StopPluginSession(pluginIds)) {
        HILOG_DEBUG(LOG_CORE, "OnStopSessionCmd FAIL");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "OnStopSessionCmd OK");
    return true;
}

bool CommandPoller::OnGetCommandResponse(SocketContext& context, ::GetCommandResponse& response)
{
    HILOG_DEBUG(LOG_CORE, "OnGetCommandResponse");
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME));
    NotifyResultRequest nrr;
    nrr.set_request_id(1);
    nrr.set_command_id(response.command_id());
    PluginResult* pr = nrr.add_result();
    ProfilerPluginState* status = pr->mutable_status();

    if (response.has_create_session_cmd()) {
        if (OnCreateSessionCmd(response.create_session_cmd(), context)) {
            status->set_state(ProfilerPluginState::LOADED);
        } else {
            status->set_state(ProfilerPluginState::REGISTERED);
        }
    } else if (response.has_destroy_session_cmd()) {
        if (OnDestroySessionCmd(response.destroy_session_cmd())) {
            status->set_state(ProfilerPluginState::REGISTERED);
        } else {
            status->set_state(ProfilerPluginState::LOADED);
        }
    } else if (response.has_start_session_cmd()) {
        if (OnStartSessionCmd(response.start_session_cmd())) {
            status->set_state(ProfilerPluginState::IN_SESSION);
        } else {
            status->set_state(ProfilerPluginState::LOADED);
        }
    } else if (response.has_stop_session_cmd()) {
        if (OnStopSessionCmd(response.stop_session_cmd())) {
            status->set_state(ProfilerPluginState::LOADED);
        } else {
            status->set_state(ProfilerPluginState::IN_SESSION);
        }
    } else {
        HILOG_DEBUG(LOG_CORE, "OnGetCommandResponse FAIL");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "OnGetCommandResponse OK %d", nrr.command_id());
    NotifyResult(nrr);
    return true;
}

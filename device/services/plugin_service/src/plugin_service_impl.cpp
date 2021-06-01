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

#include "plugin_service_impl.h"
#include "plugin_service.h"

PluginServiceImpl::PluginServiceImpl(PluginService& p)
{
    pluginService = &p;
}

PluginServiceImpl::~PluginServiceImpl() {}


bool PluginServiceImpl::RegisterPlugin(SocketContext& context,
                                       ::RegisterPluginRequest& request,
                                       ::RegisterPluginResponse& response)
{
    PluginInfo pluginInfo;

    pluginInfo.name = request.name();
    pluginInfo.path = request.path();
    pluginInfo.sha256 = request.sha256();
    pluginInfo.bufferSizeHint = request.buffer_size_hint();
    pluginInfo.context = &context;

    if (pluginService->AddPluginInfo(pluginInfo)) {
        response.set_status(0);
        response.set_plugin_id(pluginService->GetPluginIdByName(pluginInfo.name));
        HILOG_DEBUG(LOG_CORE, "RegisterPlugin OK");
        return true;
    }
    response.set_status(1);
    HILOG_DEBUG(LOG_CORE, "RegisterPlugin FAIL");
    return false;
}
bool PluginServiceImpl::UnregisterPlugin(SocketContext& context,
                                         ::UnregisterPluginRequest& request,
                                         ::UnregisterPluginResponse& response)
{
    PluginInfo pluginInfo;
    pluginInfo.id = request.plugin_id();

    if (pluginService->RemovePluginInfo(pluginInfo)) {
        response.set_status(0);
        HILOG_DEBUG(LOG_CORE, "UnregisterPlugin OK");
        return true;
    }
    response.set_status(1);
    HILOG_DEBUG(LOG_CORE, "UnregisterPlugin FAIL");
    return false;
}

bool PluginServiceImpl::GetCommand(SocketContext& context, ::GetCommandRequest& request, ::GetCommandResponse& response)
{
    return false;
}

bool PluginServiceImpl::NotifyResult(SocketContext& context,
                                     ::NotifyResultRequest& request,
                                     ::NotifyResultResponse& response)
{
    HILOG_DEBUG(LOG_CORE, "NotifyResult");
    if (pluginService->AppendResult(request)) {
        response.set_status(0);
        return true;
    }
    response.set_status(1);
    return true;
}

bool PluginServiceImpl::PushCommand(SocketContext& context, GetCommandResponsePtr command)
{
    SendResponseGetCommandResponse(context, *command.get());
    return true;
}

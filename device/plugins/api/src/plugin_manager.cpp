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

#include "plugin_manager.h"

#include <functional>

#include "command_poller.h"
#include "logging.h"
#include "plugin_service_types.pb.h"

PluginManager::~PluginManager() {}

void PluginManager::SetCommandPoller(const CommandPollerPtr& p)
{
    this->commandPoller_ = p;
}

bool PluginManager::AddPlugin(const std::string& pluginPath)
{
    PluginModuleInfo info;

    if (pluginIds_.find(pluginPath) != pluginIds_.end()) {
        HILOG_DEBUG(LOG_CORE, "already add");
        return false;
    }
    auto plugin = std::make_shared<PluginModule>(pluginPath);
    if (!plugin->Load()) {
        HILOG_DEBUG(LOG_CORE, "load failed");
        return false;
    }

    if (!plugin->BindFunctions()) {
        HILOG_DEBUG(LOG_CORE, "BindFunctions failed %s", pluginPath.c_str());
        return false;
    }

    if (!plugin->GetInfo(info)) {
        HILOG_DEBUG(LOG_CORE, "getinfo failed");
        return false;
    }

    HILOG_DEBUG(LOG_CORE, "add plugin name = %s", pluginPath.c_str());

    if (!plugin->Unload()) {
        HILOG_DEBUG(LOG_CORE, "unload failed");
        return false;
    }

    RegisterPluginRequest request;
    request.set_request_id(commandPoller_->GetRequestId());
    request.set_path(pluginPath);
    request.set_sha256("");
    request.set_name(pluginPath);
    request.set_buffer_size_hint(0);
    RegisterPluginResponse response;

    if (commandPoller_->RegisterPlugin(request, response)) {
        if (response.status() == 0) {
            HILOG_DEBUG(LOG_CORE, "response.plugin_id() = %d", response.plugin_id());
            pluginIds_[pluginPath] = response.plugin_id();
            pluginModules_.insert(std::pair<uint32_t, std::shared_ptr<PluginModule>>(response.plugin_id(), plugin));
            HILOG_DEBUG(LOG_CORE, "RegisterPlugin OK");
        } else {
            HILOG_DEBUG(LOG_CORE, "RegisterPlugin FAIL 1");
            return false;
        }
    } else {
        HILOG_DEBUG(LOG_CORE, "RegisterPlugin FAIL 2");
        return false;
    }

    return true;
}

bool PluginManager::RemovePlugin(const std::string& pluginPath)
{
    auto it = pluginIds_.find(pluginPath);
    if (it == pluginIds_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }
    uint32_t index = it->second;

    UnregisterPluginRequest request;
    request.set_request_id(commandPoller_->GetRequestId());
    request.set_plugin_id(index);
    UnregisterPluginResponse response;
    if (commandPoller_->UnregisterPlugin(request, response)) {
        if (response.status() != 0) {
            HILOG_DEBUG(LOG_CORE, "RegisterPlugin FAIL 1");
            return false;
        }
    } else {
        HILOG_DEBUG(LOG_CORE, "RegisterPlugin FAIL 2");
        return false;
    }

    auto itPluginModules = pluginModules_.find(index);
    if (it == pluginIds_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }
    pluginModules_.erase(itPluginModules);
    pluginIds_.erase(it);
    return true;
}

bool PluginManager::LoadPlugin(const std::string& pluginPath)
{
    HILOG_DEBUG(LOG_CORE, "size = %zu", pluginIds_.size());
    auto it = pluginIds_.find(pluginPath);
    if (it == pluginIds_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }
    uint32_t index = it->second;

    if (!pluginModules_[index]->Load()) {
        return false;
    }
    if (!pluginModules_[index]->BindFunctions()) {
        return false;
    }
    return true;
}

bool PluginManager::UnloadPlugin(const std::string& pluginPath)
{
    auto it = pluginIds_.find(pluginPath);
    if (it == pluginIds_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }

    return UnloadPlugin(it->second);
}

bool PluginManager::UnloadPlugin(const uint32_t pluginId)
{
    HILOG_INFO(LOG_CORE, "%s:UnloadPlugin ready!", __func__);
    if (pluginModules_.find(pluginId) == pluginModules_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }
    if (!pluginModules_[pluginId]->Unload()) {
        return false;
    }
    return true;
}

bool PluginManager::CreatePluginSession(const std::vector<ProfilerPluginConfig>& config)
{
    HILOG_DEBUG(LOG_CORE, "CreatePluginSession");

    for (size_t idx = 0; idx < config.size(); ++idx) {
        HILOG_DEBUG(LOG_CORE, "config->name() = %s", config[idx].name().c_str());
        auto it = pluginIds_.find(config[idx].name());
        if (it == pluginIds_.end()) {
            HILOG_DEBUG(LOG_CORE, "plugin not find");
            return false;
        }

        HILOG_DEBUG(LOG_CORE, "index = %d", it->second);
        pluginModules_[it->second]->SetConfigData(config[idx].config_data());
    }
    return true;
}

bool PluginManager::DestroyPluginSession(const std::vector<uint32_t>& pluginIds)
{
    for (uint32_t id : pluginIds) {
        auto it = pluginModules_.find(id);
        if (it == pluginModules_.end()) {
            HILOG_DEBUG(LOG_CORE, "plugin not find");
            return false;
        }
    }
    return true;
}

bool PluginManager::StartPluginSession(const std::vector<uint32_t>& pluginIds,
                                       const std::vector<ProfilerPluginConfig>& config)
{
    HILOG_INFO(LOG_CORE, "%s: ready!", __func__);
    size_t idx = 0;

    for (uint32_t id : pluginIds) {
        auto it = pluginModules_.find(id);
        if (it == pluginModules_.end()) {
            HILOG_DEBUG(LOG_CORE, "plugin not find");
            return false;
        }
        auto configData = pluginModules_[id]->GetConfigData();
        if (!pluginModules_[id]->StartSession(
            reinterpret_cast<const uint8_t*>(configData.c_str()), configData.size())) {
            return false;
        }
        if (pluginModules_[id]->GetSampleMode() == PluginModule::POLLING) {
            if (idx > config.size()) {
                HILOG_WARN(LOG_CORE, "idx %zu out of size %zu", idx, config.size());
                return false;
            }
            uint32_t sampleInterval = config[idx].sample_interval();
            std::string pluginName = config[idx].name();
            HILOG_DEBUG(LOG_CORE, "sampleInterval = %d", sampleInterval);
            HILOG_DEBUG(LOG_CORE, "name = %s", pluginName.c_str());
            if (!scheduleTaskManager_.ScheduleTask(pluginName, std::bind(&PluginManager::PullResult, this, id),
                ScheduleTaskManager::ms(sampleInterval))) {
                HILOG_DEBUG(LOG_CORE, "ScheduleTask failed");
                return false;
            }
        }

        idx++;
    }

    return true;
}

bool PluginManager::StopPluginSession(const std::vector<uint32_t>& pluginIds)
{
    HILOG_INFO(LOG_CORE, "%s:stop session ready!", __func__);
    for (uint32_t id : pluginIds) {
        if (pluginModules_.find(id) == pluginModules_.end()) {
            HILOG_DEBUG(LOG_CORE, "plugin not find");
            return false;
        }
        if (pluginModules_[id]->GetSampleMode() == PluginModule::POLLING) {
            for (auto it : pluginIds_) {
                if (it.second == id) {
                    HILOG_DEBUG(LOG_CORE, "find plugin name = %s", it.first.c_str());
                    if (!scheduleTaskManager_.UnscheduleTask(it.first)) {
                        return false;
                    }
                }
            }
        }
        if (!pluginModules_[id]->StopSession()) {
            return false;
        }
    }
    return true;
}

bool PluginManager::SubmitResult(const PluginResult& pluginResult)
{
    HILOG_DEBUG(LOG_CORE, "==================SubmitResult ===============");
    NotifyResultRequest request;
    if (commandPoller_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "SubmitResult:commandPoller_ is null");
        return false;
    }
    request.set_request_id(commandPoller_->GetRequestId());
    request.set_command_id(0);
    PluginResult* p = request.add_result();
    *p = pluginResult;
    NotifyResultResponse response;
    if (!commandPoller_->NotifyResult(request, response)) {
        HILOG_DEBUG(LOG_CORE, "SubmitResult FAIL 1");
        return false;
    }
    if (response.status() != 0) {
        HILOG_DEBUG(LOG_CORE, "SubmitResult FAIL 2");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "SubmitResult OK");
    return true;
}

bool PluginManager::PullResult(uint32_t pluginId)
{
    HILOG_INFO(LOG_CORE, "%s: ready!", __func__);
    uint32_t size = 0;
    std::string name;
    HILOG_DEBUG(LOG_CORE, "PullResult pluginId = %d", pluginId);
    auto it = pluginModules_.find(pluginId);
    if (it == pluginModules_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not find");
        return false;
    }
    pluginModules_[pluginId]->GetBufferSizeHint(size);
    pluginModules_[pluginId]->GetPluginName(name);
    std::unique_ptr<uint8_t[]> buffer {new (std::nothrow) uint8_t[size]};
    if (buffer == nullptr) {
        HILOG_DEBUG(LOG_CORE, "buffer new failed!");
    } else {
        HILOG_DEBUG(LOG_CORE, "buffer new success!");
    }

    int length = it->second->ReportResult(buffer.get(), size);
    if (length < 0) {
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "PullResult length = %d", length);
    HILOG_DEBUG(LOG_CORE, "PullResult name = %s", name.c_str());

    ProfilerPluginData pluginData;
    pluginData.set_name(name);
    pluginData.set_status(0);
    pluginData.set_data(buffer.get(), length);

    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    pluginData.set_clock_id(ProfilerPluginData::CLOCKID_REALTIME);
    pluginData.set_tv_sec(ts.tv_sec);
    pluginData.set_tv_nsec(ts.tv_nsec);

    auto writer = pluginModules_[pluginId]->GetWriter();
    CHECK_NOTNULL(writer, false, "PullResult GetWriter nullptr");

    std::static_pointer_cast<BufferWriter>(writer)->WriteProtobuf(pluginData);
    return true;
}

bool PluginManager::CreateWriter(std::string pluginName, uint32_t bufferSize, int fd)
{
    auto it = pluginIds_.find(pluginName);
    if (it == pluginIds_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }
    uint32_t index = it->second;

    if (bufferSize > 0) {
        HILOG_DEBUG(LOG_CORE, "%s Use ShareMemory %d", pluginName.c_str(), bufferSize);
        pluginModules_[index]->RegisterWriter(
            std::make_shared<BufferWriter>(pluginName, bufferSize, fd, commandPoller_, index));
    } else {
        HILOG_ERROR(LOG_CORE, "no shared memory buffer allocated!");
        return false;
    }
    return true;
}

bool PluginManager::ResetWriter(uint32_t pluginId)
{
    if (pluginModules_.find(pluginId) == pluginModules_.end()) {
        HILOG_DEBUG(LOG_CORE, "plugin not exist");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "ResetWriter %u", pluginId);
    pluginModules_[pluginId]->RegisterWriter(nullptr);
    return true;
}

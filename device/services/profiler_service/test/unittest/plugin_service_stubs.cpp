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
#define LOG_TAG "PluginServiceStub"
#include "plugin_service_stubs.h"

#include <map>
#include <memory>
#include "logging.h"
#include "profiler_capability_manager.h"
#include "profiler_data_repeater.h"

#ifdef USE_PLUGIN_SERVICE_STUB
using PluginServiceStubPtr = STD_PTR(shared, PluginServiceStub);
PluginServiceStubPtr PluginServiceStub::GetInstance()
{
    static std::weak_ptr<PluginServiceStub> instance;
    auto stub = instance.lock();
    if (stub) {
        return stub;
    }
    stub = std::make_shared<PluginServiceStub>();
    instance = stub;
    return stub;
}

void PluginServiceStub::SetCreateResult(bool value)
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, value);
    createResult_ = value;
}

bool PluginServiceStub::GetCreateResult() const
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, createResult_);
    return createResult_;
}

void PluginServiceStub::SetStartResult(bool value)
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, value);
    startResult_ = value;
}

bool PluginServiceStub::GetStartResult() const
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, startResult_);
    return startResult_;
}

void PluginServiceStub::SetStopResult(bool value)
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, value);
    stopResult_ = value;
}

bool PluginServiceStub::GetStopResult() const
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, stopResult_);
    return stopResult_;
}

void PluginServiceStub::SetDestroyResult(bool value)
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, value);
    destroyResult_ = value;
}

bool PluginServiceStub::GetDestroyResult() const
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, destroyResult_);
    return destroyResult_;
}

void PluginServiceStub::SetAddResult(bool value)
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, value);
    addResult_ = value;
}

bool PluginServiceStub::GetAddResult()
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, addResult_);
    return addResult_;
}

void PluginServiceStub::SetRemoveResult(bool value)
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, value);
    removeResult_ = value;
}

bool PluginServiceStub::GetRemoveResult()
{
    HILOG_DEBUG(LOG_CORE, "%s(%d)", __FUNCTION__, removeResult_);
    return removeResult_;
}
#endif

PluginService::PluginService()
{
    pluginIdAutoIncrease_ = 0;
    serviceEntry_ = NULL;
    pluginServiceImpl_ = NULL;
    pluginCommandBuilder_ = NULL;
    readShareMemoryThreadStatus_ = READ_SHARE_MEMORY_UNSPECIFIED;
    waitForCommandId_ = 0;
}

PluginService::~PluginService() {}

bool PluginService::CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                                        const ProfilerDataRepeaterPtr& dataRepeater)
{
    if (nameIndex_.find(pluginConfig.name()) == nameIndex_.end()) {
        HILOG_WARN(LOG_CORE, "CreatePluginSession for %s FAILED, plugin not found!", pluginConfig.name().c_str());
        return false;
    }
    uint32_t idx = nameIndex_[pluginConfig.name()];
    pluginContext_[idx].profilerDataRepeater = dataRepeater;
    pluginContext_[idx].shareMemoryBlock = NULL;
    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);
    HILOG_DEBUG(LOG_CORE, "CreatePluginSession for %s SUCCESS!", pluginConfig.name().c_str());
    return true;
}

bool PluginService::CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                                        const ProfilerSessionConfig::BufferConfig& bufferConfig,
                                        const ProfilerDataRepeaterPtr& dataRepeater)
{
    return CreatePluginSession(pluginConfig, dataRepeater);
}

bool PluginService::StartPluginSession(const ProfilerPluginConfig& pluginConfig)
{
    if (nameIndex_.find(pluginConfig.name()) == nameIndex_.end()) {
        HILOG_WARN(LOG_CORE, "StartPluginSession for %s FAILED, plugin not found!", pluginConfig.name().c_str());
        return false;
    }
    uint32_t idx = nameIndex_[pluginConfig.name()];
    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::IN_SESSION);
    HILOG_DEBUG(LOG_CORE, "StartPluginSession for %s SUCCESS!", pluginConfig.name().c_str());
    return true;
}

bool PluginService::StopPluginSession(const std::string& pluginName)
{
    if (nameIndex_.find(pluginName) == nameIndex_.end()) {
        HILOG_WARN(LOG_CORE, "StopPluginSession for %s FAILED, plugin not found!", pluginName.c_str());
        return false;
    }
    uint32_t idx = nameIndex_[pluginName];
    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);
    HILOG_DEBUG(LOG_CORE, "StopPluginSession for %s SUCCESS!", pluginName.c_str());
    return true;
}

bool PluginService::DestroyPluginSession(const std::string& pluginName)
{
    if (nameIndex_.find(pluginName) == nameIndex_.end()) {
        HILOG_WARN(LOG_CORE, "DestroyPluginSession for %s FAILED, plugin not found!", pluginName.c_str());
        return false;
    }
    uint32_t idx = nameIndex_[pluginName];
    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::REGISTERED);
    HILOG_DEBUG(LOG_CORE, "DestroyPluginSession for %s SUCCESS!", pluginName.c_str());
    return true;
}

bool PluginService::AddPluginInfo(const PluginInfo& pluginInfo)
{
    if (nameIndex_.find(pluginInfo.name) == nameIndex_.end()) { // add new plugin
        while (pluginContext_.find(pluginIdAutoIncrease_) != pluginContext_.end()) {
            pluginIdAutoIncrease_++;
        }

        ProfilerPluginCapability capability;
        capability.set_path(pluginInfo.path);
        capability.set_name(pluginInfo.name);
        if (!ProfilerCapabilityManager::GetInstance().AddCapability(capability)) {
            HILOG_WARN(LOG_CORE, "AddPluginInfo for %s FAILED, AddCapability failed!", pluginInfo.name.c_str());
            return false;
        }

        pluginContext_[pluginIdAutoIncrease_].path = pluginInfo.path;
        pluginContext_[pluginIdAutoIncrease_].context = pluginInfo.context;
        pluginContext_[pluginIdAutoIncrease_].config.set_name(pluginInfo.name);
        pluginContext_[pluginIdAutoIncrease_].config.set_plugin_sha256(pluginInfo.sha256);
        pluginContext_[pluginIdAutoIncrease_].profilerPluginState = std::make_shared<ProfilerPluginState>();
        pluginContext_[pluginIdAutoIncrease_].profilerPluginState->set_name(pluginInfo.name);
        pluginContext_[pluginIdAutoIncrease_].profilerPluginState->set_state(ProfilerPluginState::REGISTERED);

        pluginContext_[pluginIdAutoIncrease_].sha256 = pluginInfo.sha256;
        pluginContext_[pluginIdAutoIncrease_].bufferSizeHint = pluginInfo.bufferSizeHint;

        nameIndex_[pluginInfo.name] = pluginIdAutoIncrease_;
        pluginIdAutoIncrease_++;
    } else { // update sha256 or bufferSizeHint
        uint32_t idx = nameIndex_[pluginInfo.name];

        if (pluginInfo.sha256 != "") {
            pluginContext_[idx].sha256 = pluginInfo.sha256;
        }
        if (pluginInfo.bufferSizeHint != 0) {
            pluginContext_[idx].bufferSizeHint = pluginInfo.bufferSizeHint;
        }
    }

    HILOG_DEBUG(LOG_CORE, "AddPluginInfo for %s SUCCESS!", pluginInfo.name.c_str());
    return true;
}

bool PluginService::RemovePluginInfo(const PluginInfo& pluginInfo)
{
    if (pluginContext_.find(pluginInfo.id) == pluginContext_.end()) {
        HILOG_WARN(LOG_CORE, "RemovePluginInfo for %s FAILED, plugin not found!", pluginInfo.name.c_str());
        return false;
    }
    if (!ProfilerCapabilityManager::GetInstance().RemoveCapability(pluginContext_[pluginInfo.id].config.name())) {
        HILOG_WARN(LOG_CORE, "RemovePluginInfo for %s FAILED, remove capability FAIL.", pluginInfo.name.c_str());
        return false;
    }

    nameIndex_.erase(pluginContext_[pluginInfo.id].config.name());
    pluginContext_.erase(pluginInfo.id);
    HILOG_DEBUG(LOG_CORE, "RemovePluginInfo for %s SUCCESS!", pluginInfo.name.c_str());
    return true;
}

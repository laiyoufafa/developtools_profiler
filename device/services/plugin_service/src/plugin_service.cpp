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

#include "plugin_service.h"

#include <cinttypes>
#include <fcntl.h>
#include <sys/wait.h>
#include <unistd.h>

#include "plugin_command_builder.h"
#include "plugin_service_impl.h"
#include "profiler_capability_manager.h"
#include "profiler_data_repeater.h"
#include "securec.h"
#include "share_memory_allocator.h"
#include "socket_context.h"

namespace {
const int PAGE_BYTES = 4096;
const int DEFAULT_EVENT_POLLING_INTERVAL = 5000;
} // namespace

PluginService::PluginService()
{
    pluginIdCounter_ = 0;
    StartService(DEFAULT_UNIX_SOCKET_PATH);
    pluginCommandBuilder_ = std::make_shared<PluginCommandBuilder>();

    eventPoller_ = std::make_unique<EpollEventPoller>(DEFAULT_EVENT_POLLING_INTERVAL);
    CHECK_NOTNULL(eventPoller_, NO_RETVAL, "create event poller FAILED!");

    eventPoller_->Init();
    eventPoller_->Start();
}

PluginService::~PluginService()
{
    if (eventPoller_) {
        eventPoller_->Stop();
        eventPoller_->Finalize();
    }
}

SemaphorePtr PluginService::GetSemaphore(uint32_t id) const
{
    std::unique_lock<std::mutex> lock(mutex_);
    auto it = waitSemphores_.find(id);
    if (it != waitSemphores_.end()) {
        return it->second;
    }
    return nullptr;
}

bool PluginService::StartService(const std::string& unixSocketName)
{
    pluginServiceImpl_ = std::make_shared<PluginServiceImpl>(*this);
    serviceEntry_ = std::make_shared<ServiceEntry>();
    if (!serviceEntry_->StartServer(unixSocketName)) {
        pluginServiceImpl_ = nullptr;
        serviceEntry_ = nullptr;
        HILOG_DEBUG(LOG_CORE, "Start IPC Service FAIL");
        return false;
    }
    serviceEntry_->RegisterService(*pluginServiceImpl_.get());
    return true;
}

static ShareMemoryBlock::ReusePolicy GetReusePolicy(const ProfilerSessionConfig::BufferConfig& bufferConfig)
{
    if (bufferConfig.policy() == ProfilerSessionConfig::BufferConfig::RECYCLE) {
        return ShareMemoryBlock::DROP_OLD;
    }
    return ShareMemoryBlock::DROP_NONE;
}

bool PluginService::CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                                        const ProfilerSessionConfig::BufferConfig& bufferConfig,
                                        const ProfilerDataRepeaterPtr& dataRepeater)
{
    CHECK_TRUE(nameIndex_.find(pluginConfig.name()) != nameIndex_.end(), false,
               "CreatePluginSession can't find plugin name %s", pluginConfig.name().c_str());

    uint32_t idx = nameIndex_[pluginConfig.name()];
    pluginContext_[idx].profilerDataRepeater = dataRepeater;

    auto cmd = pluginCommandBuilder_->BuildCreateSessionCmd(pluginConfig, bufferConfig.pages() * PAGE_BYTES);
    CHECK_TRUE(cmd != nullptr, false, "CreatePluginSession BuildCreateSessionCmd FAIL %s", pluginConfig.name().c_str());

    auto smb = ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal(pluginConfig.name(),
                                                                          bufferConfig.pages() * PAGE_BYTES);
    CHECK_TRUE(smb != nullptr, false, "CreateMemoryBlockLocal FAIL %s", pluginConfig.name().c_str());

    auto policy = GetReusePolicy(bufferConfig);
    HILOG_DEBUG(LOG_CORE, "CreatePluginSession policy = %d", (int)policy);
    smb->SetReusePolicy(policy);

    auto notifier = EventNotifier::Create(0, EventNotifier::NONBLOCK);
    CHECK_NOTNULL(notifier, false, "create EventNotifier for %s failed!", pluginConfig.name().c_str());

    pluginContext_[idx].shareMemoryBlock = smb;
    pluginContext_[idx].eventNotifier = notifier;
    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);

    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, cmd);
    pluginContext_[idx].context->SendFileDescriptor(smb->GetfileDescriptor());
    pluginContext_[idx].context->SendFileDescriptor(notifier->GetFd());

    eventPoller_->AddFileDescriptor(notifier->GetFd(),
                                    std::bind(&PluginService::ReadShareMemory, this, pluginContext_[idx]));

    HILOG_DEBUG(LOG_CORE, "pluginContext_[idx].shareMemoryBlock->GetfileDescriptor = %d",
                pluginContext_[idx].shareMemoryBlock->GetfileDescriptor());
    return true;
}
bool PluginService::CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                                        const ProfilerDataRepeaterPtr& dataRepeater)
{
    CHECK_TRUE(nameIndex_.find(pluginConfig.name()) != nameIndex_.end(), false,
               "CreatePluginSession can't find plugin name %s", pluginConfig.name().c_str());

    uint32_t idx = nameIndex_[pluginConfig.name()];
    HILOG_INFO(LOG_CORE, "idx=%d", idx);
    pluginContext_[idx].profilerDataRepeater = dataRepeater;

    pluginContext_[idx].shareMemoryBlock = nullptr;

    auto cmd = pluginCommandBuilder_->BuildCreateSessionCmd(pluginConfig, 0);
    CHECK_TRUE(cmd != nullptr, false, "CreatePluginSession BuildCreateSessionCmd FAIL %s", pluginConfig.name().c_str());

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);

    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, cmd);
    return true;
}

bool PluginService::StartPluginSession(const ProfilerPluginConfig& config)
{
    CHECK_TRUE(nameIndex_.find(config.name()) != nameIndex_.end(), false,
               "StartPluginSession can't find plugin name %s", config.name().c_str());

    uint32_t idx = nameIndex_[config.name()];
    auto cmd = pluginCommandBuilder_->BuildStartSessionCmd(config, idx);
    CHECK_TRUE(cmd != nullptr, false, "StartPluginSession BuildStartSessionCmd FAIL %s", config.name().c_str());

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::IN_SESSION);
    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, cmd);
    return true;
}
bool PluginService::StopPluginSession(const std::string& pluginName)
{
    CHECK_TRUE(nameIndex_.find(pluginName) != nameIndex_.end(), false, "StopPluginSession can't find plugin name %s",
               pluginName.c_str());

    uint32_t idx = nameIndex_[pluginName];
    auto cmd = pluginCommandBuilder_->BuildStopSessionCmd(idx);
    CHECK_TRUE(cmd != nullptr, false, "StopPluginSession BuildStopSessionCmd FAIL %s", pluginName.c_str());

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);
    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, cmd);

    auto sem = GetSemaphoreFactory().Create(0);
    CHECK_NOTNULL(sem, false, "create Semaphore for stop %s FAILED!", pluginName.c_str());

    waitSemphores_[cmd->command_id()] = sem;
    HILOG_DEBUG(LOG_CORE, "=== StopPluginSession Waiting ... ===");
    // try lock for 30000 ms.
    if (sem->TimedWait(30)) {
        ReadShareMemory(pluginContext_[idx]);
        HILOG_DEBUG(LOG_CORE, "=== ShareMemory Clear ===");
    } else {
        HILOG_DEBUG(LOG_CORE, "=== StopPluginSession Waiting FAIL ===");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "=== StopPluginSession Waiting OK ===");
    return true;
}
bool PluginService::DestroyPluginSession(const std::string& pluginName)
{
    CHECK_TRUE(nameIndex_.find(pluginName) != nameIndex_.end(), false, "DestroyPluginSession can't find plugin name %s",
               pluginName.c_str());

    uint32_t idx = nameIndex_[pluginName];

    auto cmd = pluginCommandBuilder_->BuildDestroySessionCmd(idx);
    CHECK_TRUE(cmd != nullptr, false, "DestroyPluginSession BuildDestroySessionCmd FAIL %s", pluginName.c_str());

    if (pluginContext_[idx].shareMemoryBlock != nullptr) {
        ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockLocal(pluginName);
        pluginContext_[idx].shareMemoryBlock = nullptr;
    }

    if (pluginContext_[idx].eventNotifier) {
        eventPoller_->RemoveFileDescriptor(pluginContext_[idx].eventNotifier->GetFd());
        pluginContext_[idx].eventNotifier = nullptr;
    }

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::REGISTERED);

    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, cmd);
    return true;
}

bool PluginService::AddPluginInfo(const PluginInfo& pluginInfo)
{
    if (nameIndex_.find(pluginInfo.name) == nameIndex_.end()) { // add new plugin
        while (pluginContext_.find(pluginIdCounter_) != pluginContext_.end()) {
            pluginIdCounter_++;
        }

        ProfilerPluginCapability capability;
        capability.set_path(pluginInfo.path);
        capability.set_name(pluginInfo.name);
        CHECK_TRUE(ProfilerCapabilityManager::GetInstance().AddCapability(capability), false,
                   "AddPluginInfo AddCapability FAIL");

        pluginContext_[pluginIdCounter_].name = pluginInfo.name;
        pluginContext_[pluginIdCounter_].path = pluginInfo.path;
        pluginContext_[pluginIdCounter_].context = pluginInfo.context;
        pluginContext_[pluginIdCounter_].config.set_name(pluginInfo.name);
        pluginContext_[pluginIdCounter_].config.set_plugin_sha256(pluginInfo.sha256);
        pluginContext_[pluginIdCounter_].profilerPluginState = std::make_shared<ProfilerPluginState>();
        pluginContext_[pluginIdCounter_].profilerPluginState->set_name(pluginInfo.name);
        pluginContext_[pluginIdCounter_].profilerPluginState->set_state(ProfilerPluginState::REGISTERED);

        pluginContext_[pluginIdCounter_].sha256 = pluginInfo.sha256;
        pluginContext_[pluginIdCounter_].bufferSizeHint = pluginInfo.bufferSizeHint;

        nameIndex_[pluginInfo.name] = pluginIdCounter_;
        pluginIdCounter_++;
    } else { // update sha256 or bufferSizeHint
        uint32_t idx = nameIndex_[pluginInfo.name];

        if (pluginInfo.sha256 != "") {
            pluginContext_[idx].sha256 = pluginInfo.sha256;
        }
        if (pluginInfo.bufferSizeHint != 0) {
            pluginContext_[idx].bufferSizeHint = pluginInfo.bufferSizeHint;
        }
    }
    HILOG_DEBUG(LOG_CORE, "AddPluginInfo for %s done!", pluginInfo.name.c_str());

    return true;
}

bool PluginService::GetPluginInfo(const std::string& pluginName, PluginInfo& pluginInfo)
{
    uint32_t pluginId = 0;
    auto itId = nameIndex_.find(pluginName);
    CHECK_TRUE(itId != nameIndex_.end(), false, "plugin name %s not found!", pluginName.c_str());
    pluginId = itId->second;

    auto it = pluginContext_.find(pluginId);
    CHECK_TRUE(it != pluginContext_.end(), false, "plugin id %d not found!", pluginId);

    pluginInfo.id = pluginId;
    pluginInfo.name = it->second.name;
    pluginInfo.path = it->second.path;
    pluginInfo.sha256 = it->second.sha256;
    pluginInfo.bufferSizeHint = it->second.bufferSizeHint;
    return true;
}

bool PluginService::RemovePluginInfo(const PluginInfo& pluginInfo)
{
    CHECK_TRUE(pluginContext_.find(pluginInfo.id) != pluginContext_.end(), false,
               "RemovePluginInfo can't find plugin id %d", pluginInfo.id);

    CHECK_TRUE(ProfilerCapabilityManager::GetInstance().RemoveCapability(pluginContext_[pluginInfo.id].config.name()),
               false, "RemovePluginInfo RemoveCapability FAIL %d", pluginInfo.id);

    nameIndex_.erase(pluginContext_[pluginInfo.id].config.name());
    pluginContext_.erase(pluginInfo.id);
    HILOG_DEBUG(LOG_CORE, "RemovePluginInfo for %s done!", pluginInfo.name.c_str());
    return true;
}

void PluginService::ReadShareMemory(PluginContext& context)
{
    CHECK_NOTNULL(context.shareMemoryBlock, NO_RETVAL, "smb of %s is null!", context.path.c_str());
    uint64_t value = 0;
    if (context.eventNotifier) {
        value = context.eventNotifier->Take();
    }
    HILOG_DEBUG(LOG_CORE, "ReadShareMemory for %s %" PRIu64, context.path.c_str(), value);
    while (true) {
        auto pluginData = std::make_shared<ProfilerPluginData>();
        bool ret = context.shareMemoryBlock->TakeData([&](const int8_t data[], uint32_t size) -> bool {
            int retval = pluginData->ParseFromArray(reinterpret_cast<const char*>(data), size);
            CHECK_TRUE(retval, false, "parse %d bytes failed!", size);
            return true;
        });
        if (!ret) {
            break;
        }
        if (!context.profilerDataRepeater->PutPluginData(pluginData)) {
            break;
        }
    }
}

bool PluginService::AppendResult(NotifyResultRequest& request)
{
    pluginCommandBuilder_->GetedCommandResponse(request.command_id());
    auto sem = GetSemaphore(request.command_id());
    if (sem) {
        sem->Post();
    }

    int size = request.result_size();
    HILOG_DEBUG(LOG_CORE, "AppendResult size:%d, cmd id:%d", size, request.command_id());
    for (int i = 0; i < size; i++) {
        PluginResult pr = request.result(i);
        if (pr.data().size() > 0) {
            HILOG_DEBUG(LOG_CORE, "AppendResult Size : %zu", pr.data().size());
            uint32_t pluginId = pr.plugin_id();
            if (pluginContext_[pluginId].profilerDataRepeater == nullptr) {
                HILOG_DEBUG(LOG_CORE, "AppendResult profilerDataRepeater==nullptr %s %d", pr.status().name().c_str(),
                            pluginId);
                return false;
            }
            auto pluginData = std::make_shared<ProfilerPluginData>();
            pluginData->set_name(pr.status().name());
            pluginData->set_status(0);
            pluginData->set_data(pr.data());
            if (!pluginContext_[pluginId].profilerDataRepeater->PutPluginData(pluginData)) {
                return false;
            }
        } else {
            HILOG_DEBUG(LOG_CORE, "Flush?Data From ShareMemory?");
        }
    }
    return true;
}

std::vector<ProfilerPluginStatePtr> PluginService::GetPluginStatus()
{
    std::vector<ProfilerPluginStatePtr> ret;
    std::map<uint32_t, PluginContext>::iterator iter;
    for (iter = pluginContext_.begin(); iter != pluginContext_.end(); ++iter) {
        ret.push_back(iter->second.profilerPluginState);
    }
    return ret;
}

uint32_t PluginService::GetPluginIdByName(std::string name)
{
    if (nameIndex_.find(name) == nameIndex_.end()) {
        return 0;
    }
    return nameIndex_[name];
}

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
}

PluginService::PluginService()
{
    pluginIdAutoIncrease_ = 0;
    waitForCommandId_ = 0;
    StartService(DEFAULT_UNIX_SOCKET_PATH);
    pluginCommandBuilder_ = std::make_shared<PluginCommandBuilder>();

    waitStopSession_.lock();

    readShareMemoryThreadStatus_ = READ_SHARE_MEMORY_FREE;
    readShareMemoryThreadSleep_.lock();

    readShareMemoryThread_ = std::thread(&PluginService::ReadShareMemoryThread, this);
    if (readShareMemoryThread_.get_id() == std::thread::id()) {
        HILOG_ERROR(LOG_CORE, "CreateReadShareMemoryThread FAIL");
    }
}

PluginService::~PluginService()
{
    readShareMemoryThreadStatus_ = READ_SHARE_MEMORY_EXIT;
    readShareMemoryThreadSleep_.unlock();
    readShareMemoryThread_.join();
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

bool PluginService::CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                                        const ProfilerSessionConfig::BufferConfig& bufferConfig,
                                        const ProfilerDataRepeaterPtr& dataRepeater)
{
    CHECK_TRUE(nameIndex_.find(pluginConfig.name()) != nameIndex_.end(), false,
               "CreatePluginSession can't find plugin name %s", pluginConfig.name().c_str());

    uint32_t idx = nameIndex_[pluginConfig.name()];
    pluginContext_[idx].profilerDataRepeater = dataRepeater;

    auto gcr = pluginCommandBuilder_->BuildCreateSessionCmd(pluginConfig, bufferConfig.pages() * PAGE_BYTES);
    CHECK_TRUE(gcr != nullptr, false, "CreatePluginSession BuildCreateSessionCmd FAIL %s", pluginConfig.name().c_str());

    auto smb = ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal(pluginConfig.name(),
                                                                          bufferConfig.pages() * PAGE_BYTES);
    CHECK_TRUE(smb != nullptr, false, "CreateMemoryBlockLocal FAIL %s", pluginConfig.name().c_str());

    pluginContext_[idx].shareMemoryBlock = smb;
    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);

    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, gcr);
    pluginContext_[idx].context->SendFileDescriptor(pluginContext_[idx].shareMemoryBlock->GetfileDescriptor());

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

    auto gcr = pluginCommandBuilder_->BuildCreateSessionCmd(pluginConfig, 0);
    CHECK_TRUE(gcr != nullptr, false, "CreatePluginSession BuildCreateSessionCmd FAIL %s", pluginConfig.name().c_str());

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);

    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, gcr);
    return true;
}

bool PluginService::StartPluginSession(const ProfilerPluginConfig& config)
{
    CHECK_TRUE(nameIndex_.find(config.name()) != nameIndex_.end(), false,
               "StartPluginSession can't find plugin name %s", config.name().c_str());

    uint32_t idx = nameIndex_[config.name()];
    auto gcr = pluginCommandBuilder_->BuildStartSessionCmd(config, idx);
    CHECK_TRUE(gcr != nullptr, false, "StartPluginSession BuildStartSessionCmd FAIL %s", config.name().c_str());

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::IN_SESSION);
    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, gcr);

    if (readShareMemoryThreadStatus_ == READ_SHARE_MEMORY_FREE) { // Start the thread that reads shared memory
        readShareMemoryThreadStatus_ = READ_SHARE_MEMORY_WORKING;
        readShareMemoryThreadSleep_.unlock();
    }
    return true;
}
bool PluginService::StopPluginSession(const std::string& pluginName)
{
    CHECK_TRUE(nameIndex_.find(pluginName) != nameIndex_.end(), false, "StopPluginSession can't find plugin name %s",
               pluginName.c_str());

    uint32_t idx = nameIndex_[pluginName];
    auto gcr = pluginCommandBuilder_->BuildStopSessionCmd(idx);
    CHECK_TRUE(gcr != nullptr, false, "StopPluginSession BuildStopSessionCmd FAIL %s", pluginName.c_str());

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::LOADED);
    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, gcr);

    waitForCommandId_ = gcr->command_id();
    HILOG_DEBUG(LOG_CORE, "=== StopPluginSession Waiting ... ===");
    // try lock for 30000 ms.
    if (waitStopSession_.try_lock_for(std::chrono::milliseconds(30000))) { // Received command reply，and stopsession，
        ReadShareMemoryOneTime(); // Read the shared memory data again before exiting to avoid losing it
        HILOG_DEBUG(LOG_CORE, "=== ShareMemory Clear ===");
    } else {
        HILOG_DEBUG(LOG_CORE, "=== StopPluginSession Waiting FAIL ===");
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "=== StopPluginSession Waiting OK ===");

    if (readShareMemoryThreadStatus_ == READ_SHARE_MEMORY_WORKING) { // Stop the thread reading shared memory
        readShareMemoryThreadStatus_ = READ_SHARE_MEMORY_FREE;
    }

    return true;
}
bool PluginService::DestroyPluginSession(const std::string& pluginName)
{
    CHECK_TRUE(nameIndex_.find(pluginName) != nameIndex_.end(), false, "DestroyPluginSession can't find plugin name %s",
               pluginName.c_str());

    uint32_t idx = nameIndex_[pluginName];

    auto gcr = pluginCommandBuilder_->BuildDestroySessionCmd(idx);
    CHECK_TRUE(gcr != nullptr, false, "DestroyPluginSession BuildDestroySessionCmd FAIL %s", pluginName.c_str());

    if (pluginContext_[idx].shareMemoryBlock != nullptr) {
        ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockLocal(pluginName);
        pluginContext_[idx].shareMemoryBlock = nullptr;
    }

    pluginContext_[idx].profilerPluginState->set_state(ProfilerPluginState::REGISTERED);

    pluginServiceImpl_->PushCommand(*pluginContext_[idx].context, gcr);
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
        CHECK_TRUE(ProfilerCapabilityManager::GetInstance().AddCapability(capability), false,
                   "AddPluginInfo AddCapability FAIL");

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
    return true;
}

void PluginService::ReadShareMemoryOneTime()
{
    readShareMemory_.lock();
    for (auto it = pluginContext_.begin(); it != pluginContext_.end(); it++) {
        PluginContext* pluginContext = &it->second;
        if (pluginContext->shareMemoryBlock == nullptr) {
            continue;
        }
        do {
            uint32_t size = pluginContext->shareMemoryBlock->GetDataSize();
            if (size == 0) {
                break;
            }

            int8_t* p = const_cast<int8_t*>(pluginContext->shareMemoryBlock->GetDataPoint());
            auto pluginData = std::make_shared<ProfilerPluginData>();
            pluginData->ParseFromArray(reinterpret_cast<char*>(p), size);
            HILOG_DEBUG(LOG_CORE, "Read ShareMemory %d", size);
            if (!pluginContext->profilerDataRepeater->PutPluginData(pluginData)) {
            }
            if (!pluginContext->shareMemoryBlock->Next()) {
                break;
            }
        } while (true);
    }
    readShareMemory_.unlock();
}

void* PluginService::ReadShareMemoryThread(void* p)
{
    pthread_setname_np(pthread_self(), "ReadMemThread");

    PluginService* pluginService = (PluginService*)p;
    if (pluginService == nullptr) {
        return nullptr;
    }
    while (pluginService->readShareMemoryThreadStatus_ != READ_SHARE_MEMORY_EXIT) {
        // try lock for 60000000 ms.
        if (pluginService->readShareMemoryThreadSleep_.try_lock_for(std::chrono::milliseconds(60000000))) {
            while (pluginService->readShareMemoryThreadStatus_ == READ_SHARE_MEMORY_WORKING) {
                pluginService->ReadShareMemoryOneTime();
                usleep(10000); // sleep for 10000 us.
            }
        }
    }
    return nullptr;
}
bool PluginService::AppendResult(NotifyResultRequest& request)
{
    pluginCommandBuilder_->GetedCommandResponse(request.command_id());
    if (request.command_id() == waitForCommandId_) {
        waitStopSession_.unlock();
    }

    int size = request.result_size();
    HILOG_DEBUG(LOG_CORE, "AppendResult size:%d,cmdid:%d,waitid:%d", size, request.command_id(), waitForCommandId_);
    for (int i = 0; i < size; i++) {
        PluginResult pr = request.result(i);
        if (pr.data().size() > 0) {
            HILOG_DEBUG(LOG_CORE, "AppendResult Size : %zu", pr.data().size());
            uint32_t pluginId = pr.plugin_id();
            if (pluginContext_[pluginId].profilerDataRepeater == nullptr) {
                HILOG_DEBUG(LOG_CORE, "AppendResult profilerDataRepeater==nullptr %s %d",
                    pr.status().name().c_str(), pluginId);
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
    for (iter = pluginContext_.begin(); iter != pluginContext_.end(); iter++) {
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

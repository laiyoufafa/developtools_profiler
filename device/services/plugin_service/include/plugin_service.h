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

#ifndef PLUGIN_SERVICE_H
#define PLUGIN_SERVICE_H

#include <map>
#include <memory>
#include <string>
#include <thread>
#include <vector>

#include "common_types.pb.h"
#include "logging.h"
#include "plugin_service_types.pb.h"
#include "profiler_service_types.pb.h"
#include "service_entry.h"

class PluginServiceImpl;
class ProfilerDataRepeater;
class SocketContext;
class ShareMemoryBlock;
class PluginCommandBuilder;

using ProfilerDataRepeaterPtr = STD_PTR(shared, ProfilerDataRepeater);
using ProfilerPluginStatePtr = STD_PTR(shared, ProfilerPluginState);

struct PluginInfo {
    uint32_t id = 0;
    std::string name;
    std::string path;
    std::string sha256;
    uint32_t bufferSizeHint;
    SocketContext* context;
};

struct PluginContext {
    std::string path;
    SocketContext* context;
    ProfilerPluginConfig config;
    ProfilerDataRepeaterPtr profilerDataRepeater;
    std::shared_ptr<ShareMemoryBlock> shareMemoryBlock;
    ProfilerPluginStatePtr profilerPluginState;
    std::string sha256;
    uint32_t bufferSizeHint;
};

class PluginService {
public:
    PluginService();
    ~PluginService();

    bool CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                             const ProfilerSessionConfig::BufferConfig& bufferConfig,
                             const ProfilerDataRepeaterPtr& dataRepeater);
    bool CreatePluginSession(const ProfilerPluginConfig& pluginConfig,
                             const ProfilerDataRepeaterPtr& dataRepeater);
    bool StartPluginSession(const ProfilerPluginConfig& config);
    bool StopPluginSession(const std::string& pluginName);
    bool DestroyPluginSession(const std::string& pluginName);

    bool AddPluginInfo(const PluginInfo& pluginInfo);
    bool RemovePluginInfo(const PluginInfo& pluginInfo);

    bool AppendResult(NotifyResultRequest& request);

    std::vector<ProfilerPluginStatePtr> GetPluginStatus();
    uint32_t GetPluginIdByName(std::string name);

private:
    std::map<uint32_t, PluginContext> pluginContext_;
    bool StartService(const std::string& unixSocketName);

    std::map<std::string, uint32_t> nameIndex_;

    uint32_t pluginIdAutoIncrease_;
    std::shared_ptr<ServiceEntry> serviceEntry_;
    std::shared_ptr<PluginServiceImpl> pluginServiceImpl_;
    std::shared_ptr<PluginCommandBuilder> pluginCommandBuilder_;

    std::mutex readShareMemory_;
    void ReadShareMemoryOneTime();

    enum ReadShareMemoryStatus {
        READ_SHARE_MEMORY_FREE,
        READ_SHARE_MEMORY_WORKING,
        READ_SHARE_MEMORY_EXIT,
        READ_SHARE_MEMORY_UNSPECIFIED,
    };
    ReadShareMemoryStatus readShareMemoryThreadStatus_; // 0空闲等待，1工作中，2线程退出
    std::timed_mutex readShareMemoryThreadSleep_;
    static void* ReadShareMemoryThread(void* p);
    std::thread readShareMemoryThread_;

    uint32_t waitForCommandId_;
    std::timed_mutex waitStopSession_;
};

#endif // PLUGIN_SERVICE_H
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

#ifndef PLUGIN_SESSION_H
#define PLUGIN_SESSION_H

#include <mutex>
#include "logging.h"
#include "nocopyable.h"
#include "plugin_service_types.pb.h"
#include "profiler_service_types.pb.h"

class PluginService;
class ProfilerDataRepeater;

using PluginServicePtr = STD_PTR(shared, PluginService);
using ProfilerDataRepeaterPtr = STD_PTR(shared, ProfilerDataRepeater);

class PluginSession {
public:
    PluginSession(const ProfilerPluginConfig& pluginConfig,
                  const PluginServicePtr& pluginService,
                  const ProfilerDataRepeaterPtr& dataRepeater);
    PluginSession(const ProfilerPluginConfig& pluginConfig,
                  const ProfilerSessionConfig::BufferConfig& bufferConfig,
                  const PluginServicePtr& pluginService,
                  const ProfilerDataRepeaterPtr& dataRepeater);
    ~PluginSession();

    bool Start();
    bool Stop();
    bool Destroy();

    bool IsAvailable() const;

    enum State {
        INITIAL = 0,
        CREATED = 1,
        STARTED = 2,
    };
    State GetState() const;

private:
    bool Create();

private:
    mutable std::mutex mutex_;
    State state_;
    bool withBufferConfig_;
    ProfilerPluginConfig pluginConfig_;
    ProfilerSessionConfig::BufferConfig bufferConfig_;
    std::weak_ptr<PluginService> pluginService_;
    ProfilerDataRepeaterPtr dataRepeater_;

    DISALLOW_COPY_AND_MOVE(PluginSession);
};

#endif // !PLUGIN_SESSION_H

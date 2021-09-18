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

#include "plugin_module.h"

#include <dlfcn.h>
#include <iostream>

#include "logging.h"
#include "plugin_module_api.h"

PluginModule::PluginModule(const std::string& path) : handle_(nullptr), path_(path), structPtr_(nullptr) {}

PluginModule::~PluginModule() {}

std::string PluginModule::ComputeSha256()
{
    return "";
}

bool PluginModule::Load()
{
    char realPath[PATH_MAX + 1] = {0};
    if (handle_ != nullptr) {
        HILOG_DEBUG(LOG_CORE, "already open");
        return false;
    }

    if (realpath(path_.c_str(), realPath) == nullptr) {
        HILOG_ERROR(LOG_CORE, "so filename invalid, errno=%d", errno);
        return false;
    }

    std::string rpath = realPath; // for SC warning
    handle_ = dlopen(rpath.c_str(), RTLD_NOW);
    if (handle_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "dlopen err:%s.", dlerror());
        return false;
    }
    return true;
}

bool PluginModule::Unload()
{
    HILOG_INFO(LOG_CORE, "%s:unload ready!", __func__);
    if (handle_ != nullptr) {
        HILOG_INFO(LOG_CORE, "Unload plugin");
        int ret = dlclose(handle_);
        HILOG_INFO(LOG_CORE, "Unload plugin ret = %d", ret);
        handle_ = nullptr;
        structPtr_ = nullptr;
        return true;
    }

    return false;
}

bool PluginModule::GetInfo(PluginModuleInfo& info)
{
    if (handle_ != nullptr) {
        if (structPtr_ == nullptr) {
            return false;
        }
        info.bufferSizeHint = structPtr_->resultBufferSizeHint;
        info.name.assign(structPtr_->name);
        return true;
    }
    return false;
}

PluginModule::SampleMode PluginModule::GetSampleMode() const
{
    if (structPtr_ && structPtr_->callbacks) {
        if (structPtr_->callbacks->onPluginReportResult != nullptr) {
            return POLLING;
        } else if (structPtr_->callbacks->onRegisterWriterStruct != nullptr) {
            return STREAMING;
        }
    }
    return UNKNOWN;
}

void PluginModule::SetConfigData(const std::string& data)
{
    configData_ = data;
}

std::string PluginModule::GetConfigData() const
{
    return configData_;
}

bool PluginModule::GetPluginName(std::string& pluginName)
{
    if (handle_ != nullptr) {
        if (structPtr_ == nullptr) {
            return false;
        }
        pluginName.assign(structPtr_->name);
        return true;
    }
    return false;
}

bool PluginModule::GetBufferSizeHint(uint32_t& bufferSizeHint)
{
    if (handle_ != nullptr) {
        if (structPtr_ == nullptr) {
            return false;
        }
        bufferSizeHint = structPtr_->resultBufferSizeHint;
        return true;
    }
    return false;
}

bool PluginModule::IsLoaded()
{
    return (handle_ != nullptr);
}
bool PluginModule::BindFunctions()
{
    if (handle_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "plugin not load");
        return false;
    }
    if (structPtr_ == nullptr) {
        structPtr_ = static_cast<PluginModuleStruct*>(dlsym(handle_, "g_pluginModule"));
        if (structPtr_ == nullptr) {
            HILOG_DEBUG(LOG_CORE, "structPtr_ == nullptr");
            return false;
        }
    }

    if (structPtr_->callbacks == nullptr) {
        HILOG_DEBUG(LOG_CORE, "structPtr_->callbacks == nullptr");
        return false;
    }

    if ((structPtr_->callbacks->onPluginSessionStart == nullptr) ||
        (structPtr_->callbacks->onPluginSessionStop == nullptr)) {
        HILOG_DEBUG(LOG_CORE, "onPluginSessionStart == nullptr");
        return false;
    }

    return true;
}

bool PluginModule::StartSession(const uint8_t* buffer, uint32_t size)
{
    HILOG_DEBUG(LOG_CORE, "StartSession");
    if (handle_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "plugin not load");
        return false;
    }

    if (structPtr_ != nullptr && structPtr_->callbacks != nullptr) {
        if (structPtr_->callbacks->onPluginSessionStart) {
            return (structPtr_->callbacks->onPluginSessionStart(buffer, size) == 0);
        }
    }
    return false;
}

bool PluginModule::StopSession()
{
    HILOG_INFO(LOG_CORE, "%s:stop Session ready!", __func__);
    if (handle_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "plugin not load");
        return false;
    }
    if (structPtr_ != nullptr && structPtr_->callbacks != nullptr) {
        if (structPtr_->callbacks->onPluginSessionStop != nullptr) {
            return (structPtr_->callbacks->onPluginSessionStop() == 0);
        }
    }
    return false;
}

int32_t PluginModule::ReportResult(uint8_t* buffer, uint32_t size)
{
    if (handle_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "plugin not open");
        return -1;
    }
    if (first_) {
        lastTime_ = std::chrono::steady_clock::now();
        first_ = false;
    } else {
        std::chrono::steady_clock::time_point t1 = std::chrono::steady_clock::now();
        lastTime_ = t1;
    }

    if (structPtr_ != nullptr && structPtr_->callbacks != nullptr) {
        if (structPtr_->callbacks->onPluginReportResult != nullptr) {
            return structPtr_->callbacks->onPluginReportResult(buffer, size);
        }
    }

    return -1;
}

bool PluginModule::RegisterWriter(const BufferWriterPtr writer)
{
    writerAdapter_ = std::make_shared<WriterAdapter>();
    writerAdapter_->SetWriter(writer);

    if (writer == nullptr) {
        HILOG_INFO(LOG_CORE, "BufferWriter is null, update WriterAdapter only!");
        return true;
    }
    if (structPtr_ != nullptr && structPtr_->callbacks != nullptr) {
        if (structPtr_->callbacks->onRegisterWriterStruct != nullptr) {
            return structPtr_->callbacks->onRegisterWriterStruct(writerAdapter_->GetStruct());
        }
    }
    return true;
}

WriterPtr PluginModule::GetWriter()
{
    if (writerAdapter_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "PluginModule 111111, nullptr");
        return nullptr;
    }
    return writerAdapter_->GetWriter();
}

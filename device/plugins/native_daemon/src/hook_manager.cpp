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
#include "epoll_event_poller.h"
#include "event_notifier.h"
#include "hook_service.h"
#include "logging.h"
#include "plugin_service_types.pb.h"
#include "share_memory_allocator.h"
#include "utilities.h"
#include "virtual_runtime.h"
#include "hook_manager.h"

namespace {
const int DEFAULT_EVENT_POLLING_INTERVAL = 5000;
const int PAGE_BYTES = 4096;
} // namespace

void HookManager::writeFrames(int type, const struct timespec& ts, void* addr, uint32_t mallocSize,
    const std::vector<OHOS::Developtools::NativeDaemon::CallFrame>& callsFrames)
{
    if (type == 0) {
        fprintf(fpHookData_.get(), "malloc;%" PRId64 ";%ld;0x%" PRIx64 ";%u\n",
                (int64_t)ts.tv_sec, ts.tv_nsec, (uint64_t)addr, mallocSize);
    } else if (type == 1) {
        fprintf(fpHookData_.get(), "free;%" PRId64 ";%ld;0x%" PRIx64 "\n",
                (int64_t)ts.tv_sec, ts.tv_nsec, (uint64_t)addr);
    } else {
        return;
    }

    for (size_t idx = 0, size = callsFrames.size(); idx < size; ++idx) {
        (void)fprintf(fpHookData_.get(), "0x%" PRIx64 ";0x%" PRIx64 ";%s;%s;0x%" PRIx64 ";%" PRIu64 "\n",
            callsFrames[idx].ip_, callsFrames[idx].sp_, callsFrames[idx].symbolName_.c_str(),
            callsFrames[idx].filePath_.c_str(), callsFrames[idx].offset_, callsFrames[idx].symbolOffset_);
    }
}

HookManager::HookManager() : fpHookData_(nullptr, nullptr) { }

void HookManager::SetCommandPoller(const std::shared_ptr<CommandPoller>& p)
{
    commandPoller_ = p;
}
bool HookManager::RegisterAgentPlugin(const std::string& pluginPath)
{
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
            agentIndex_ = response.plugin_id();
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

bool HookManager::UnregisterAgentPlugin(const std::string& pluginPath)
{
    UnregisterPluginRequest request;
    request.set_request_id(commandPoller_->GetRequestId());
    request.set_plugin_id(agentIndex_);
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
    agentIndex_ = -1;

    return true;
}

bool HookManager::LoadPlugin(const std::string& pluginPath)
{
    return true;
}

bool HookManager::UnloadPlugin(const std::string& pluginPath)
{
    return true;
}

bool HookManager::UnloadPlugin(const uint32_t pluginId)
{
    return true;
}

bool HookManager::CreatePluginSession(const std::vector<ProfilerPluginConfig>& config)
{
    HILOG_DEBUG(LOG_CORE, "CreatePluginSession");
    UNUSED_PARAMETER(config);
    smbName_ = "hooknativesmb";
    // save config
    std::string cfgData = config[0].config_data();
    if (hookConfig_.ParseFromArray(reinterpret_cast<const uint8_t*>(cfgData.c_str()), cfgData.size()) <= 0) {
        HILOG_ERROR(LOG_CORE, "%s: ParseFromArray failed", __func__);
        return false;
    }

    runtime_instance = std::make_shared<OHOS::Developtools::NativeDaemon::VirtualRuntime>();
    // create file
    if (hookConfig_.save_file()) {
        HILOG_DEBUG(LOG_CORE, "save file name = %s", hookConfig_.file_name().c_str());
        FILE *fp = fopen(hookConfig_.file_name().c_str(), "wb+");
        if (fp) {
            fpHookData_.reset();
            fpHookData_ = std::unique_ptr<FILE, decltype(&fclose)>(fp, fclose);
        } else {
            fpHookData_.reset();
        }
    } else {
        HILOG_ERROR(LOG_CORE, "%s: Not support!", __func__);
        return false;
    }
    // create smb and eventNotifier
    uint32_t bufferSize = hookConfig_.smb_pages() * PAGE_BYTES; /* bufferConfig.pages() */
    HILOG_DEBUG(LOG_CORE, "smb bufferSize = %u", bufferSize);
    shareMemoryBlock_ = ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal(smbName_, bufferSize);
    CHECK_TRUE(shareMemoryBlock_ != nullptr, false, "CreateMemoryBlockLocal FAIL %s", smbName_.c_str());

    eventNotifier_ = EventNotifier::Create(0, EventNotifier::NONBLOCK);
    CHECK_NOTNULL(eventNotifier_, false, "create EventNotifier for %s failed!", smbName_.c_str());

    // start event poller task
    eventPoller_ = std::make_unique<EpollEventPoller>(DEFAULT_EVENT_POLLING_INTERVAL);
    CHECK_NOTNULL(eventPoller_, -1, "create event poller FAILED!");

    eventPoller_->Init();
    eventPoller_->Start();

    eventPoller_->AddFileDescriptor(eventNotifier_->GetFd(), std::bind(&HookManager::ReadShareMemory, this));

    HILOG_INFO(LOG_CORE, "hookservice smbFd = %d, eventFd = %d\n", shareMemoryBlock_->GetfileDescriptor(),
               eventNotifier_->GetFd());

    // start service init socket
    hookService_ = std::make_shared<HookService>(shareMemoryBlock_->GetfileDescriptor(), eventNotifier_->GetFd(),
                                                hookConfig_.filter_size(), bufferSize, hookConfig_.pid(), "");
    CHECK_NOTNULL(hookService_, -1, "ProfilerService create failed!");

    // send signal
    std::string stopCmd = "kill -36 " + std::to_string(hookConfig_.pid());
    HILOG_INFO(LOG_CORE, "stop command : %s", stopCmd.c_str());
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(stopCmd.c_str(), "r"), pclose);

    return true;
}

void HookManager::ReadShareMemory()
{
    CHECK_NOTNULL(shareMemoryBlock_, NO_RETVAL, "smb is null!");
    uint64_t value = eventNotifier_->Take();

    while (true) {
        bool ret = shareMemoryBlock_->TakeData([&](const int8_t data[], uint32_t size) -> bool {
            std::vector<u64> u64regs;
            uint32_t *regAddr = nullptr;
            uint32_t stackSize;
            pid_t tid;
            pid_t pid;
            void *addr = nullptr;
            int8_t* tmp = const_cast<int8_t *>(data);

            struct timespec ts = {};
            if (memcpy_s(&ts, sizeof(ts), data, sizeof(ts)) != EOK) {
                HILOG_ERROR(LOG_CORE, "memcpy_s ts failed");
            }
            uint32_t type = *(reinterpret_cast<uint32_t *>(tmp + sizeof(ts)));
            uint32_t mallocSize = *(reinterpret_cast<uint32_t *>(tmp + sizeof(ts) + sizeof(type)));
            addr = *(reinterpret_cast<void **>(tmp + sizeof(ts) + sizeof(type) + sizeof(mallocSize)));
            stackSize = *(reinterpret_cast<uint32_t *>(tmp + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *)));
            std::unique_ptr<uint8_t[]> stackData = std::make_unique<uint8_t[]>(stackSize);
            if (memcpy_s(stackData.get(), stackSize, tmp + sizeof(stackSize) + sizeof(ts) + sizeof(type)
                + sizeof(mallocSize) + sizeof(void *), stackSize) != EOK) {
                HILOG_ERROR(LOG_CORE, "memcpy_s data failed");
            }
            tid = *(reinterpret_cast<pid_t *>(tmp + sizeof(stackSize) + stackSize + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *)));
            pid = *(reinterpret_cast<pid_t *>(tmp + sizeof(stackSize) + stackSize + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *) + sizeof(tid)));
            regAddr = reinterpret_cast<uint32_t *>(tmp + sizeof(tid) + sizeof(pid) + sizeof(stackSize)
                + stackSize + sizeof(ts) + sizeof(type) + sizeof(mallocSize) + sizeof(void *));

            int reg_count = (size - sizeof(tid) - sizeof(pid) - sizeof(stackSize) - stackSize
                - sizeof(ts) - sizeof(type) - sizeof(mallocSize) - sizeof(void *))
                / sizeof(uint32_t);
            if (reg_count <= 0) {
                HILOG_ERROR(LOG_CORE, "data error size = %u", size);
            }
            for (int idx = 0; idx < reg_count; ++idx) {
                u64regs.push_back(*regAddr++);
            }
            std::vector<OHOS::Developtools::NativeDaemon::CallFrame> callsFrames;
            runtime_instance->UnwindStack(u64regs, stackData.get(), stackSize, pid, tid, callsFrames,
                                          (hookConfig_.max_stack_depth() > 0)
                                              ? hookConfig_.max_stack_depth()
                                              : OHOS::Developtools::NativeDaemon::MAX_CALL_FRAME_UNWIND_SIZE);

            if (hookConfig_.save_file()) {
                writeFrames(type, ts, addr, mallocSize, callsFrames);
            }
            return true;
        });
        if (!ret) {
            break;
        }
    }
}

bool HookManager::DestroyPluginSession(const std::vector<uint32_t>& pluginIds)
{
    // send signal
    std::string stopCmd = "kill -37 " + std::to_string(hookConfig_.pid());
    HILOG_INFO(LOG_CORE, "stop command : %s", stopCmd.c_str());
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(stopCmd.c_str(), "r"), pclose);

    // release hook service
    hookService_ = nullptr;

    // stop event poller
    if (eventPoller_) {
        HILOG_ERROR(LOG_CORE, "eventPoller_ unset!");
        eventPoller_->RemoveFileDescriptor(eventNotifier_->GetFd());
        eventPoller_->Stop();
        eventPoller_->Finalize();
        eventPoller_ = nullptr;
    }

    // release smb and eventNotifier
    if (shareMemoryBlock_) {
        ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockLocal(smbName_);
        shareMemoryBlock_ = nullptr;
    }

    if (eventNotifier_) {
        eventNotifier_ = nullptr;
    }

    HILOG_ERROR(LOG_CORE, "stop command : %s", stopCmd.c_str());
    fpHookData_ = nullptr;
    HILOG_ERROR(LOG_CORE, "fclose hook data file");
    runtime_instance = nullptr;
    return true;
}

bool HookManager::StartPluginSession(const std::vector<uint32_t>& pluginIds,
                                     const std::vector<ProfilerPluginConfig>& config)
{
    UNUSED_PARAMETER(config);
    return true;
}

bool HookManager::StopPluginSession(const std::vector<uint32_t>& pluginIds)
{
    return true;
}

bool HookManager::CreateWriter(std::string pluginName, uint32_t bufferSize, int smbFd, int eventFd)
{
    HILOG_DEBUG(LOG_CORE, "agentIndex_ %d", agentIndex_);
    return true;
}

bool HookManager::ResetWriter(uint32_t pluginId)
{
    return true;
}

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

#include <sys/stat.h>

#include "command_poller.h"
#include "epoll_event_poller.h"
#include "event_notifier.h"
#include "hook_service.h"
#include "logging.h"
#include "plugin_service_types.pb.h"
#include "share_memory_allocator.h"
#include "sys_param.h"
#include "utilities.h"
#include "virtual_runtime.h"
#include "hook_manager.h"

using namespace OHOS::Developtools::NativeDaemon;

namespace {
const int STACK_DATA_SIZE = 3000;
const int DEFAULT_EVENT_POLLING_INTERVAL = 5000;
const int PAGE_BYTES = 4096;
std::shared_ptr<BufferWriter> g_buffWriter;
constexpr uint32_t MAX_BUFFER_SIZE = 10 * 1024;
const std::string STARTUP = "startup:";
const std::string PARAM_NAME = "libc.hook_mode";
} // namespace

bool HookManager::CheckProcess()
{
    if (pid_ != 0) {
        int ret = 0;
        std::string pid_path = std::string();
        struct stat stat_buf;
        pid_path = "/proc/" + std::to_string(pid_) + "/status";
        if (stat(pid_path.c_str(), &stat_buf) != 0) {
            pid_ = 0;
            HILOG_ERROR(LOG_CORE, "%s: hook process does not exist", __func__);
            return false;
        } else {
            return true;
        }
    } else if (hookConfig_.process_name() != "") {
        // check if the pid and process name is consistency
        CheckProcessName();
    }

    return true;
}

void HookManager::CheckProcessName()
{
    std::string findpid = "pidof " + hookConfig_.process_name();
    HILOG_INFO(LOG_CORE, "find pid command : %s", findpid.c_str());
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(findpid.c_str(), "r"), pclose);

    char line[LINE_SIZE];
    do {
        if (fgets(line, sizeof(line), pipe.get()) == nullptr) {
            HILOG_INFO(LOG_CORE, "Process %s not exist, set param", hookConfig_.process_name().c_str());
            std::string cmd = STARTUP + hookConfig_.process_name();
            int ret = SystemSetParameter(PARAM_NAME.c_str(), cmd.c_str());
            if (ret < 0) {
                HILOG_WARN(LOG_CORE, "set param failed, please manually set param and start process(%s)",
                    hookConfig_.process_name().c_str());
            } else {
                HILOG_INFO(LOG_CORE, "set param success, please start process(%s)",
                    hookConfig_.process_name().c_str());
            }
            break;
        } else if (strlen(line) > 0 && isdigit((unsigned char)(line[0]))) {
            pid_ = (int)atoi(line);
            HILOG_INFO(LOG_CORE, "Process %s exist, pid = %d", hookConfig_.process_name().c_str(), pid_);
            break;
        }
    } while (1);
}

void HookManager::writeFrames(const struct timespec& ts, HookContext& hookContext,
    const std::vector<CallFrame>& callsFrames)
{
    if (hookContext.type == 0) {
        fprintf(fpHookData_.get(), "malloc;%d;%d;%" PRId64 ";%ld;0x%" PRIx64 ";%u\n", hookContext.pid, hookContext.tid,
            (int64_t)ts.tv_sec, ts.tv_nsec, (uint64_t)hookContext.addr, hookContext.mallocSize);
    } else if (hookContext.type == 1) {
        fprintf(fpHookData_.get(), "free;%d;%d;%" PRId64 ";%ld;0x%" PRIx64 "\n", hookContext.pid, hookContext.tid,
            (int64_t)ts.tv_sec, ts.tv_nsec, (uint64_t)hookContext.addr);
    } else {
        return;
    }

    for (size_t idx = FILTER_STACK_DEPTH; idx < callsFrames.size(); ++idx) {
        (void)fprintf(fpHookData_.get(), "0x%" PRIx64 ";0x%" PRIx64 ";%s;%s;0x%" PRIx64 ";%" PRIu64 "\n",
            callsFrames[idx].ip_, callsFrames[idx].sp_, std::string(callsFrames[idx].symbolName_).c_str(),
            std::string(callsFrames[idx].filePath_).c_str(), callsFrames[idx].offset_, callsFrames[idx].symbolOffset_);
    }
}

HookManager::HookManager() : fpHookData_(nullptr, nullptr), buffer_(new (std::nothrow) uint8_t[MAX_BUFFER_SIZE]) { }

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
        if (response.status() == ResponseStatus::OK) {
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
        if (response.status() != ResponseStatus::OK) {
            HILOG_DEBUG(LOG_CORE, "UnregisterPlugin FAIL 1");
            return false;
        }
    } else {
        HILOG_DEBUG(LOG_CORE, "UnregisterPlugin FAIL 2");
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
    pid_ = hookConfig_.pid();

    if (!CheckProcess()) {
        return false;
    }

    runtime_instance = std::make_shared<VirtualRuntime>();
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
        HILOG_DEBUG(LOG_CORE, "%s: need report native_hook data", __func__);
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
    CHECK_NOTNULL(eventPoller_, false, "create event poller FAILED!");

    eventPoller_->Init();
    eventPoller_->Start();

    eventPoller_->AddFileDescriptor(eventNotifier_->GetFd(), std::bind(&HookManager::ReadShareMemory, this));

    HILOG_INFO(LOG_CORE, "hookservice smbFd = %d, eventFd = %d\n", shareMemoryBlock_->GetfileDescriptor(),
               eventNotifier_->GetFd());

    // start service init socket
    hookService_ = std::make_shared<HookService>(shareMemoryBlock_->GetfileDescriptor(),
                                                eventNotifier_->GetFd(), hookConfig_.filter_size(),
                                                bufferSize, pid_, hookConfig_.process_name());
    CHECK_NOTNULL(hookService_, false, "HookService create failed!");

    stackData_ = std::make_shared<StackDataRepeater>(STACK_DATA_SIZE);
    CHECK_TRUE(stackData_ != nullptr, false, "Create StackDataRepeater FAIL");
    stackPreprocess_ = std::make_shared<StackPreprocess>(stackData_);
    CHECK_TRUE(stackPreprocess_ != nullptr, false, "Create StackPreprocess FAIL");
    stackPreprocess_->SetWriter(g_buffWriter);
    return true;
}

void HookManager::ReadShareMemory()
{
    CHECK_NOTNULL(shareMemoryBlock_, NO_RETVAL, "smb is null!");
    uint64_t value = eventNotifier_->Take();

    while (true) {
        auto batchNativeHookData = std::make_shared<BatchNativeHookData>();

        bool ret = shareMemoryBlock_->TakeData([&](const int8_t data[], uint32_t size) -> bool {
            std::vector<u64> u64regs;
            uint32_t *regAddr = nullptr;
            uint32_t stackSize;
            pid_t pid;
            pid_t tid;
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
            pid = *(reinterpret_cast<pid_t *>(tmp + sizeof(stackSize) + stackSize + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *)));
            tid = *(reinterpret_cast<pid_t *>(tmp + sizeof(stackSize) + stackSize + sizeof(ts)
                + sizeof(type) + sizeof(mallocSize) + sizeof(void *) + sizeof(pid)));
            regAddr = reinterpret_cast<uint32_t *>(tmp + sizeof(pid) + sizeof(tid) + sizeof(stackSize)
                + stackSize + sizeof(ts) + sizeof(type) + sizeof(mallocSize) + sizeof(void *));

            int reg_count = (size - sizeof(pid) - sizeof(tid) - sizeof(stackSize) - stackSize
                - sizeof(ts) - sizeof(type) - sizeof(mallocSize) - sizeof(void *))
                / sizeof(uint32_t);
            if (reg_count <= 0) {
                HILOG_ERROR(LOG_CORE, "data error size = %u", size);
            }
            for (int idx = 0; idx < reg_count; ++idx) {
                u64regs.push_back(*regAddr++);
            }
            std::vector<CallFrame> callsFrames;
            runtime_instance->UnwindStack(u64regs, stackData.get(), stackSize, pid, tid, callsFrames,
                                          (hookConfig_.max_stack_depth() > 0)
                                              ? hookConfig_.max_stack_depth() + FILTER_STACK_DEPTH
                                              : MAX_CALL_FRAME_UNWIND_SIZE);
            HookContext hookContext = {};
            hookContext.type = type;
            hookContext.pid = pid;
            hookContext.tid = tid;
            hookContext.addr = addr;
            hookContext.mallocSize = mallocSize;
            SetHookData(hookContext, ts, callsFrames, batchNativeHookData);
            return true;
        });
        if (!ret) {
            break;
        }

        if (!hookConfig_.save_file()) {
            if (!stackData_->PutStackData(batchNativeHookData)) {
                break;
            }
        }
    }
}

void HookManager::SetHookData(HookContext& hookContext, struct timespec ts,
    std::vector<CallFrame>& callsFrames, BatchNativeHookDataPtr& batchNativeHookData)
{
    if (hookConfig_.save_file()) {
        writeFrames(ts, hookContext, callsFrames);
    } else {
        NativeHookData* hookData = batchNativeHookData->add_events();
        hookData->set_tv_sec(ts.tv_sec);
        hookData->set_tv_nsec(ts.tv_nsec);

        if (hookContext.type == 0) {
            AllocEvent* allocEvent = hookData->mutable_alloc_event();
            allocEvent->set_pid(hookContext.pid);
            allocEvent->set_tid(hookContext.tid);
            allocEvent->set_addr((uint64_t)hookContext.addr);
            allocEvent->set_size(static_cast<uint64_t>(hookContext.mallocSize));
            for (size_t idx = FILTER_STACK_DEPTH; idx < callsFrames.size(); ++idx) {
                Frame* frame = allocEvent->add_frame_info();
                SetFrameInfo(*frame, callsFrames[idx]);
            }
        } else if (hookContext.type == 1) {
            FreeEvent* freeEvent = hookData->mutable_free_event();
            freeEvent->set_pid(hookContext.pid);
            freeEvent->set_tid(hookContext.tid);
            freeEvent->set_addr((uint64_t)hookContext.addr);
            for (size_t idx = FILTER_STACK_DEPTH; idx < callsFrames.size(); ++idx) {
                Frame* frame = freeEvent->add_frame_info();
                SetFrameInfo(*frame, callsFrames[idx]);
            }
        }
    }
}

bool HookManager::DestroyPluginSession(const std::vector<uint32_t>& pluginIds)
{
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

    fpHookData_ = nullptr;
    HILOG_ERROR(LOG_CORE, "fclose hook data file");
    runtime_instance = nullptr;
    stackPreprocess_ = nullptr;
    return true;
}

bool HookManager::StartPluginSession(const std::vector<uint32_t>& pluginIds,
                                     const std::vector<ProfilerPluginConfig>& config)
{
    UNUSED_PARAMETER(config);
    CHECK_TRUE(stackPreprocess_ != nullptr, false, "start StackPreprocess FAIL");
    stackPreprocess_->StartTakeResults();

    if (pid_ > 0) {
        std::string startCmd = "kill -36 " + std::to_string(pid_);
        HILOG_INFO(LOG_CORE, "start command : %s", startCmd.c_str());
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(startCmd.c_str(), "r"), pclose);
    } else {
        HILOG_INFO(LOG_CORE, "StartPluginSession: pid_(%d) is less or equal zero.", pid_);
    }

    return true;
}

bool HookManager::StopPluginSession(const std::vector<uint32_t>& pluginIds)
{
    // send signal
    if (pid_ > 0) {
        std::string stopCmd = "kill -37 " + std::to_string(pid_);
        HILOG_INFO(LOG_CORE, "stop command : %s", stopCmd.c_str());
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(stopCmd.c_str(), "r"), pclose);
    } else {
        HILOG_INFO(LOG_CORE, "StopPluginSession: pid_(%d) is less or equal zero.", pid_);
    }

    CHECK_TRUE(stackPreprocess_ != nullptr, false, "stop StackPreprocess FAIL");
    stackPreprocess_->StopTakeResults();
    return true;
}

bool HookManager::CreateWriter(std::string pluginName, uint32_t bufferSize, int smbFd, int eventFd)
{
    HILOG_DEBUG(LOG_CORE, "agentIndex_ %d", agentIndex_);
    RegisterWriter(std::make_shared<BufferWriter>(pluginName, bufferSize, smbFd, eventFd, agentIndex_));
    return true;
}

bool HookManager::ResetWriter(uint32_t pluginId)
{
    RegisterWriter(nullptr);
    return true;
}

void HookManager::RegisterWriter(const BufferWriterPtr& writer)
{
    g_buffWriter = writer;
    return;
}

void HookManager::SetFrameInfo(Frame& frame, CallFrame& callsFrame)
{
    frame.set_ip(callsFrame.ip_);
    frame.set_sp(callsFrame.sp_);
    frame.set_symbol_name(std::string(callsFrame.symbolName_));
    frame.set_file_path(std::string(callsFrame.filePath_));
    frame.set_offset(callsFrame.offset_);
    frame.set_symbol_offset(callsFrame.symbolOffset_);
}

bool HookManager::SendProtobufPackage(uint8_t *cache, size_t length)
{
    if (g_buffWriter == nullptr) {
        HILOG_ERROR(LOG_CORE, "HookManager:: BufferWriter empty, should set writer first");
        return false;
    }
    ProfilerPluginData pluginData;
    pluginData.set_name("nativehook");
    pluginData.set_status(0);
    pluginData.set_data(cache, length);

    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    pluginData.set_clock_id(ProfilerPluginData::CLOCKID_REALTIME);
    pluginData.set_tv_sec(ts.tv_sec);
    pluginData.set_tv_nsec(ts.tv_nsec);

    g_buffWriter->WriteMessage(pluginData);
    g_buffWriter->Flush();
    return true;
}

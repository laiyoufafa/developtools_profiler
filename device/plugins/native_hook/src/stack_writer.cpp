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

#include "stack_writer.h"
#include "logging.h"
#include "share_memory_allocator.h"

#include <algorithm>
#include <cinttypes>
#include <thread>
#include <unistd.h>

StackWriter::StackWriter(std::string name,
                         uint32_t size,
                         int smbFd,
                         int eventFd)
    : pluginName_(name)
{
    HILOG_INFO(LOG_CORE, "%s:%s %d [%d] [%d]", __func__, name.c_str(), size, smbFd, eventFd);
    shareMemoryBlock_ = ShareMemoryAllocator::GetInstance().CreateMemoryBlockRemote(name, size, smbFd);
    if (shareMemoryBlock_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "%s:create shareMemoryBlock_ failed!", __func__);
    }
    eventNotifier_ = EventNotifier::CreateWithFd(eventFd);

    lastFlushTime_ = std::chrono::steady_clock::now();
}

StackWriter::~StackWriter()
{
    HILOG_DEBUG(LOG_CORE, "%s:destroy eventfd = %d!", __func__, eventNotifier_ ? eventNotifier_->GetFd() : -1);
    eventNotifier_ = nullptr;
    ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockRemote(pluginName_);
}

void StackWriter::Report() const
{
    HILOG_DEBUG(LOG_CORE, "%s:stats B: %" PRIu64 ", P: %d, W:%" PRIu64 ", F: %d", __func__,
        bytesCount_.load(), bytesPending_.load(), writeCount_.load(), flushCount_.load());
}

void StackWriter::DoStats(long bytes)
{
    ++writeCount_;
    bytesCount_ += bytes;
    bytesPending_ += bytes;
}

long StackWriter::Write(const void* data, size_t size)
{
    if (shareMemoryBlock_ == nullptr || data == nullptr || size == 0) {
        return false;
    }
    return shareMemoryBlock_->PutRaw(reinterpret_cast<const int8_t*>(data), size);
}

bool StackWriter::Flush()
{
    ++flushCount_;
    eventNotifier_->Post(flushCount_.load());
    lastFlushTime_ = std::chrono::steady_clock::now();
    bytesPending_ = 0;
    return true;
}

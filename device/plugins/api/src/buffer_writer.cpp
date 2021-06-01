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

#include "buffer_writer.h"
#include "command_poller.h"
#include "logging.h"
#include "plugin_service_types.pb.h"
#include "share_memory_allocator.h"

BufferWriter::BufferWriter(std::string name,
                           uint32_t size,
                           int fd,
                           const CommandPollerPtr& cp,
                           uint32_t pluginId)
    : pluginName_(name)
{
    HILOG_DEBUG(LOG_CORE, "CreateMemoryBlockRemote %s %d %d", name.c_str(), size, fd);
    shareMemoryBlock_ = ShareMemoryAllocator::GetInstance().CreateMemoryBlockRemote(name, size, fd);
    if (shareMemoryBlock_ == nullptr) {
        HILOG_DEBUG(LOG_CORE, "shareMemoryBlock_ == nullptr=");
    }
    commandPoller_ = cp;
    pluginId_ = pluginId;
}

BufferWriter::~BufferWriter()
{
    ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockRemote(pluginName_);
}

long BufferWriter::Write(const void* data, size_t size)
{
    if (shareMemoryBlock_ == nullptr) {
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "BufferWriter Write %zu", size);
    return shareMemoryBlock_->PutRaw(reinterpret_cast<const int8_t*>(data), static_cast<uint32_t>(size));
}

bool BufferWriter::WriteProtobuf(google::protobuf::Message& pmsg)
{
    if (shareMemoryBlock_ == nullptr) {
        return false;
    }
    HILOG_DEBUG(LOG_CORE, "BufferWriter Write %zu", pmsg.ByteSizeLong());
    return shareMemoryBlock_->PutProtobuf(pmsg);
}

bool BufferWriter::Flush()
{
    return true;
}

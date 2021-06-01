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

#ifndef BUFFER_WRITER_H
#define BUFFER_WRITER_H

#include <memory>
#include "plugin_module_api.h"
#include "share_memory_allocator.h"
#include "writer.h"

class CommandPoller;

using CommandPollerPtr = STD_PTR(shared, CommandPoller);

class BufferWriter : public Writer {
public:
    BufferWriter(std::string name, uint32_t size, int fd, const CommandPollerPtr& cp, uint32_t pluginId);
    ~BufferWriter();
    long Write(const void* data, size_t size) override;
    bool Flush() override;

    bool WriteProtobuf(google::protobuf::Message& pmsg);

private:
    std::string pluginName_;
    std::shared_ptr<ShareMemoryBlock> shareMemoryBlock_;
    CommandPollerPtr commandPoller_;
    uint32_t pluginId_;
};

#endif // BUFFER_WRITER_H

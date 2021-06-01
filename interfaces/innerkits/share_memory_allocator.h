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

#ifndef SHARE_MEMORY_ALLOCATOR_H
#define SHARE_MEMORY_ALLOCATOR_H

#include "share_memory_block.h"
#include <iostream>

class ShareMemoryAllocator {
public:
    static ShareMemoryAllocator& GetInstance();

    std::shared_ptr<ShareMemoryBlock> CreateMemoryBlockLocal(std::string name, uint32_t size);
    std::shared_ptr<ShareMemoryBlock> CreateMemoryBlockRemote(std::string name, uint32_t size, int fd);

    bool ReleaseMemoryBlockLocal(std::string name);
    bool ReleaseMemoryBlockRemote(std::string name);

    std::shared_ptr<ShareMemoryBlock> FindMBByName(std::string name);

private:
    ShareMemoryAllocator();
    ~ShareMemoryAllocator();

    std::map<std::string, std::shared_ptr<ShareMemoryBlock>> memoryBlocks;
};

#endif
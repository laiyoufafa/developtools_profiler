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

#include "share_memory_allocator.h"
#include <cstring>
#include <sys/mman.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include "logging.h"
#include "share_memory_block.h"

namespace {
const int MIN_SHARE_MEMORY_SIZE = 1024;
}
ShareMemoryAllocator& ShareMemoryAllocator::GetInstance()
{
    static ShareMemoryAllocator shareMemoryAllocator;

    return shareMemoryAllocator;
}

ShareMemoryAllocator::ShareMemoryAllocator() {}
ShareMemoryAllocator::~ShareMemoryAllocator() {}

bool ShareMemoryAllocator::ReleaseMemoryBlockLocal(std::string name)
{
    auto pmb = FindMBByName(name);

    CHECK_NOTNULL(pmb, false, "FAIL %s", name.c_str());

    pmb->ReleaseBlock();
    memoryBlocks.erase(name);
    return true;
}

bool ShareMemoryAllocator::ReleaseMemoryBlockRemote(std::string name)
{
    auto pmb = FindMBByName(name);

    CHECK_NOTNULL(pmb, false, "FAIL %s", name.c_str());

    pmb->ReleaseBlockRemote();
    memoryBlocks.erase(name);
    return true;
}

ShareMemoryBlockPtr ShareMemoryAllocator::CreateMemoryBlockLocal(std::string name, uint32_t size)
{
    CHECK_TRUE(memoryBlocks.find(name) == memoryBlocks.end(), nullptr, "%s already used", name.c_str());

    CHECK_TRUE(size >= MIN_SHARE_MEMORY_SIZE, NULL, "%s %d size less than %d", name.c_str(), size,
               MIN_SHARE_MEMORY_SIZE);

    memoryBlocks[name] = std::make_shared<ShareMemoryBlock>();
    memoryBlocks[name]->CreateBlock(name, size);
    return memoryBlocks[name];
}

ShareMemoryBlockPtr ShareMemoryAllocator::CreateMemoryBlockRemote(std::string name, uint32_t size, int fd)
{
    memoryBlocks[name] = std::make_shared<ShareMemoryBlock>();
    if (memoryBlocks[name]->CreateBlockByFd(name, size, fd)) {
        return memoryBlocks[name];
    }
    memoryBlocks.erase(name);
    HILOG_INFO(LOG_CORE, "CreateMemoryBlockRemote FAIL");
    return nullptr;
}

ShareMemoryBlockPtr ShareMemoryAllocator::FindMBByName(std::string name)
{
    CHECK_TRUE(memoryBlocks.find(name) != memoryBlocks.end(), nullptr, "FAIL");
    return memoryBlocks[name];
}

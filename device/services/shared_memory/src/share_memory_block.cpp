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

#include "share_memory_block.h"

#include <cstring>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/socket.h>
#include <sys/syscall.h>
#include <sys/un.h>
#include <unistd.h>

#include "logging.h"
#include "securec.h"

namespace {
const int HEAD_OFFSET_LEN = 4;
}

ShareMemoryBlock::ShareMemoryBlock()
{
    memoryPoint_ = nullptr;
    memorySize_ = 0;
    fileDescriptor_ = -1;
    dropType_ = DropType::DROP_NONE;
    pMemory_ = nullptr;
    memoryName_ = "";
}

std::string ReplaceStr(std::string base, std::string _from, std::string _to)
{
    while (true) {
        size_t pos = base.find(_from, 0);
        if (pos == std::string::npos) {
            break;
        }
        base.replace(pos, _from.length(), _to);
    }
    return base;
}

bool ShareMemoryBlock::CreateBlockByFd(std::string name, uint32_t size, int fd)
{
    CHECK_TRUE(memorySize_ == 0, false, "%s already allocated memory", name.c_str());

    fileDescriptor_ = fd;

    CHECK_TRUE(fileDescriptor_ >= 0, false, "CreateBlock FAIL SYS_memfd_create");

    memoryPoint_ = mmap(nullptr, size, PROT_READ | PROT_WRITE, MAP_SHARED, fileDescriptor_, 0);
    if (memoryPoint_ == MAP_FAILED) {
        ReleaseBlockRemote();

        HILOG_ERROR(LOG_CORE, "CreateBlockByFd mmap ERR : %s", strerror(errno));
        return false;
    }

    memorySize_ = size;

    memoryName_ = name;
    pMemory_ = (struct ShareMemoryStruct*)memoryPoint_;
    return true;
}

bool ShareMemoryBlock::CreateBlock(std::string name, uint32_t size)
{
    HILOG_INFO(LOG_CORE, "CreateBlock %s %d", name.c_str(), size);

    CHECK_TRUE(memorySize_ == 0, false, "%s already allocated memory", name.c_str());

    fileDescriptor_ = syscall(SYS_memfd_create, name.c_str(), 0);

    CHECK_TRUE(fileDescriptor_ >= 0, false, "CreateBlock FAIL SYS_memfd_create");

    int check = ftruncate(fileDescriptor_, size);
    if (check < 0) {
        ReleaseBlock();

        HILOG_ERROR(LOG_CORE, "CreateBlock ftruncate ERR : %s", strerror(errno));
        return false;
    }

    memoryPoint_ = mmap(nullptr, size, PROT_READ | PROT_WRITE, MAP_SHARED, fileDescriptor_, 0);
    if (memoryPoint_ == MAP_FAILED) {
        ReleaseBlock();
        HILOG_ERROR(LOG_CORE, "CreateBlock mmap ERR : %s", strerror(errno));

        return false;
    }
    memorySize_ = size;

    memoryName_ = name;
    pMemory_ = (struct ShareMemoryStruct*)memoryPoint_;
    pMemory_->head.readOffset = 0;
    pMemory_->head.writeOffset = 0;
    pMemory_->head.memorySize_ = size - SHARE_MEMORY_HEAD_SIZE;
    return true;
}

bool ShareMemoryBlock::ReleaseBlock()
{
    if (memorySize_ > 0) {
        munmap(memoryPoint_, memorySize_);
        memoryPoint_ = nullptr;
        memorySize_ = 0;
    }

    if (fileDescriptor_ >= 0) {
        close(fileDescriptor_);
        fileDescriptor_ = -1;
    }
    return true;
}
bool ShareMemoryBlock::ReleaseBlockRemote()
{
    return ReleaseBlock();
}

int8_t* ShareMemoryBlock::GetCurrentFreeMemory(uint32_t size)
{
    uint32_t realSize = size + sizeof(uint32_t) + HEAD_OFFSET_LEN;
    uint32_t wp = pMemory_->head.writeOffset;
    if (wp + realSize > pMemory_->head.memorySize_) { // 后面部分放不下，从头开始放
        if (pMemory_->head.readOffset == 0) {
            return nullptr;
        }
        *((uint32_t*)(&pMemory_->data[wp])) = 0xffffffff;
        wp = 0;
    }
    if (wp < pMemory_->head.readOffset && pMemory_->head.readOffset < wp + realSize) { //
        return nullptr;
    }

    return &pMemory_->data[wp + sizeof(uint32_t)];
}

int8_t* ShareMemoryBlock::GetFreeMemory(uint32_t size)
{
    if (dropType_ == DropType::DROP_NONE) {
        return GetCurrentFreeMemory(size);
    }
    int8_t* ret = nullptr;
    while (true) {
        ret = GetCurrentFreeMemory(size);
        if (ret != nullptr) {
            break;
        }
        if (!Next()) {
            return nullptr;
        }
    }
    return ret;
}

bool ShareMemoryBlock::UseFreeMemory(int8_t* pmem, uint32_t size)
{
    uint32_t wp = pmem - sizeof(uint32_t) - pMemory_->data;
    *((int*)(&pMemory_->data[wp])) = size;
    pMemory_->head.writeOffset = wp + sizeof(uint32_t) + size;
    return true;
}

bool ShareMemoryBlock::PutRaw(const int8_t* data, uint32_t size)
{
    int8_t* rawMemory = GetFreeMemory(size);
    if (rawMemory == nullptr) {
        HILOG_INFO(LOG_CORE, "_PutRaw not enough space [%d]", size);
        return false;
    }
    if (memcpy_s(rawMemory, size, data, size) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s error");
    }

    UseFreeMemory(rawMemory, size);
    return true;
}

bool ShareMemoryBlock::PutProtobuf(google::protobuf::Message& pmsg)
{
    size_t size = pmsg.ByteSizeLong();

    int8_t* rawMemory = GetFreeMemory(size);
    if (rawMemory == nullptr) {
        HILOG_INFO(LOG_CORE, "PutProtobuf not enough space [%zu]", size);
        return false;
    }

    pmsg.SerializeToArray(rawMemory, size);
    UseFreeMemory(rawMemory, size);
    return true;
}

uint32_t ShareMemoryBlock::GetDataSize()
{
    if (pMemory_->head.readOffset == pMemory_->head.writeOffset) {
        return 0;
    }
    uint32_t ret = *((uint32_t*)(&pMemory_->data[pMemory_->head.readOffset]));
    if (ret == 0xffffffff) {
        ret = *((uint32_t*)(&pMemory_->data[0]));
    }
    return ret;
}

const int8_t* ShareMemoryBlock::GetDataPoint()
{
    if (*((uint32_t*)(&pMemory_->data[pMemory_->head.readOffset])) == 0xffffffff) {
        return &pMemory_->data[HEAD_OFFSET_LEN];
    }
    return &pMemory_->data[pMemory_->head.readOffset + HEAD_OFFSET_LEN];
}
bool ShareMemoryBlock::Next()
{
    if (pMemory_->head.readOffset == pMemory_->head.writeOffset) {
        return false;
    }
    uint32_t size = *((uint32_t*)(&pMemory_->data[pMemory_->head.readOffset]));
    if (size == 0xffffffff) {
        size = *((uint32_t*)(&pMemory_->data[0]));
        pMemory_->head.readOffset = size + sizeof(uint32_t);
    } else {
        pMemory_->head.readOffset += size + sizeof(uint32_t);
    }
    return true;
}

std::string ShareMemoryBlock::GetName()
{
    return memoryName_;
}

uint32_t ShareMemoryBlock::GetSize()
{
    return memorySize_;
}

int ShareMemoryBlock::GetfileDescriptor()
{
    return fileDescriptor_;
}

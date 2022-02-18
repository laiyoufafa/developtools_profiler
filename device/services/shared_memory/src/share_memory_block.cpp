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
#ifndef PAGE_SIZE
constexpr uint32_t PAGE_SIZE = 4096;
#endif
}

struct PthreadLocker {
    explicit PthreadLocker(pthread_mutex_t& mutex) : mutex_(mutex)
    {
        pthread_mutex_lock(&mutex_);
    }

    ~PthreadLocker()
    {
        pthread_mutex_unlock(&mutex_);
    }

private:
    pthread_mutex_t& mutex_;
};

ShareMemoryBlock::ShareMemoryBlock()
    : fileDescriptor_(-1),
      memoryPoint_(nullptr),
      memorySize_(0),
      memoryName_(),
      header_(nullptr),
      reusePloicy_(ReusePolicy::DROP_NONE)
{
}

bool ShareMemoryBlock::CreateBlockWithFd(std::string name, uint32_t size, int fd)
{
    CHECK_TRUE(fd >= 0, false, "CreateBlock FAIL SYS_memfd_create");

    auto ptr = mmap(nullptr, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (ptr == MAP_FAILED) {
        const int bufSize = 1024;
        char buf[bufSize] = { 0 };
        strerror_r(errno, buf, bufSize);
        HILOG_ERROR(LOG_CORE, "CreateBlockWithFd mmap ERR : %s", buf);
        return false;
    }

    fileDescriptor_ = fd;
    memoryPoint_ = ptr;
    memorySize_ = size;

    memoryName_ = name;
    header_ = reinterpret_cast<BlockHeader*>(ptr);
    return true;
}

bool ShareMemoryBlock::CreateBlock(std::string name, uint32_t size)
{
    HILOG_INFO(LOG_CORE, "CreateBlock %s %d", name.c_str(), size);
    CHECK_TRUE(size > sizeof(BlockHeader), false, "size %u too less!", size);
    CHECK_TRUE(size % PAGE_SIZE == 0, false, "size %u not times of %d!", size, PAGE_SIZE);

    int fd = syscall(SYS_memfd_create, name.c_str(), 0);
    CHECK_TRUE(fd >= 0, false, "CreateBlock FAIL SYS_memfd_create");

    int check = ftruncate(fd, size);
    if (check < 0) {
        close(fd);
        const int bufSize = 1024;
        char buf[bufSize] = { 0 };
        strerror_r(errno, buf, bufSize);
        HILOG_ERROR(LOG_CORE, "CreateBlock ftruncate ERR : %s", buf);
        return false;
    }

    auto ptr = mmap(nullptr, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (ptr == MAP_FAILED) {
        close(fd);
        const int bufSize = 1024;
        char buf[bufSize] = { 0 };
        strerror_r(errno, buf, bufSize);
        HILOG_ERROR(LOG_CORE, "CreateBlock mmap ERR : %s", buf);
        return false;
    }

    fileDescriptor_ = fd;
    memoryPoint_ = ptr;
    memorySize_ = size;

    memoryName_ = name;
    header_ = reinterpret_cast<BlockHeader*>(ptr);

    // initialize header infos
    header_->info.readOffset_ = 0;
    header_->info.writeOffset_ = 0;
    header_->info.memorySize_ = size - sizeof(BlockHeader);
    header_->info.bytesCount_ = 0;
    header_->info.chunkCount_ = 0;

    pthread_mutexattr_t muAttr;
    pthread_mutexattr_init(&muAttr);
    pthread_mutexattr_setpshared(&muAttr, PTHREAD_PROCESS_SHARED);
    pthread_mutex_init(&header_->info.mutex_, &muAttr);
    return true;
}

bool ShareMemoryBlock::Valid() const
{
    return header_ != nullptr;
}

ShareMemoryBlock::ShareMemoryBlock(const std::string& name, uint32_t size) : ShareMemoryBlock()
{
    CreateBlock(name, size);
}

ShareMemoryBlock::ShareMemoryBlock(const std::string& name, uint32_t size, int fd) : ShareMemoryBlock()
{
    CreateBlockWithFd(name, size, fd);
}

ShareMemoryBlock::~ShareMemoryBlock()
{
    ReleaseBlock();
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

int8_t* ShareMemoryBlock::GetCurrentFreeMemory(uint32_t size)
{
    CHECK_NOTNULL(header_, nullptr, "header not ready!");
    uint32_t realSize = size + sizeof(uint32_t) + HEAD_OFFSET_LEN;

    uint32_t wp = header_->info.writeOffset_;
    if (wp + realSize > header_->info.memorySize_) { // 后面部分放不下，从头开始放
        if (header_->info.readOffset_ == 0) {
            return nullptr;
        }
        *((uint32_t*)(&header_->data[wp])) = 0xffffffff;
        wp = 0;
    }
    if (wp < header_->info.readOffset_ && header_->info.readOffset_ < wp + realSize) { //
        return nullptr;
    }

    return &header_->data[wp + sizeof(uint32_t)];
}

int8_t* ShareMemoryBlock::GetFreeMemory(uint32_t size)
{
    if (reusePloicy_ == ReusePolicy::DROP_NONE) {
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
    uint32_t wp = pmem - sizeof(uint32_t) - header_->data;
    *((int*)(&header_->data[wp])) = size;

    header_->info.writeOffset_ = wp + sizeof(uint32_t) + size;
    return true;
}

bool ShareMemoryBlock::PutRaw(const int8_t* data, uint32_t size)
{
    CHECK_NOTNULL(header_, false, "header not ready!");
    PthreadLocker locker(header_->info.mutex_);
    int8_t* rawMemory = GetFreeMemory(size);
    if (rawMemory == nullptr) {
        HILOG_INFO(LOG_CORE, "_PutRaw not enough space [%d]", size);
        return false;
    }
    if (memcpy_s(rawMemory, size, data, size) != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s error");
        return false;
    }

    UseFreeMemory(rawMemory, size);
    ++header_->info.bytesCount_;
    ++header_->info.chunkCount_;
    return true;
}

#ifndef NO_PROTOBUF
bool ShareMemoryBlock::PutMessage(const google::protobuf::Message& pmsg)
{
    size_t size = pmsg.ByteSizeLong();

    CHECK_NOTNULL(header_, false, "header not ready!");
    PthreadLocker locker(header_->info.mutex_);
    int8_t* rawMemory = GetFreeMemory(size);
    if (rawMemory == nullptr) {
        HILOG_INFO(LOG_CORE, "PutMessage not enough space [%zu]", size);
        return false;
    }

    pmsg.SerializeToArray(rawMemory, size);
    UseFreeMemory(rawMemory, size);
    ++header_->info.bytesCount_;
    ++header_->info.chunkCount_;
    return true;
}
#endif

bool ShareMemoryBlock::TakeData(const DataHandler& func)
{
    CHECK_NOTNULL(header_, false, "header not ready!");
    CHECK_TRUE(static_cast<bool>(func), false, "func invalid!");

    PthreadLocker locker(header_->info.mutex_);
    auto size = GetDataSize();
    if (size == 0) {
        return false;
    }
    auto ptr = GetDataPoint();
    CHECK_TRUE(func(ptr, size), false, "call func FAILED!");
    CHECK_TRUE(Next(), false, "move read pointer FAILED!");
    --header_->info.chunkCount_;
    return true;
}

uint32_t ShareMemoryBlock::GetDataSize()
{
    if (header_->info.readOffset_ == header_->info.writeOffset_) {
        return 0;
    }
    uint32_t ret = *((uint32_t*)(&header_->data[header_->info.readOffset_]));
    if (ret == 0xffffffff) {
        ret = *((uint32_t*)(&header_->data[0]));
    }
    return ret;
}

const int8_t* ShareMemoryBlock::GetDataPoint()
{
    if (*((uint32_t*)(&header_->data[header_->info.readOffset_])) == 0xffffffff) {
        return &header_->data[HEAD_OFFSET_LEN];
    }
    return &header_->data[header_->info.readOffset_ + HEAD_OFFSET_LEN];
}

bool ShareMemoryBlock::Next()
{
    if (header_->info.readOffset_ == header_->info.writeOffset_) {
        return false;
    }
    uint32_t size = *((uint32_t*)(&header_->data[header_->info.readOffset_]));
    if (size == 0xffffffff) {
        size = *((uint32_t*)(&header_->data[0]));
        header_->info.readOffset_ = size + sizeof(uint32_t);
    } else {
        header_->info.readOffset_ += size + sizeof(uint32_t);
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

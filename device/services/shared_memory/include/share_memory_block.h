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

#ifndef SHARE_MEMORY_BLOCK_H
#define SHARE_MEMORY_BLOCK_H

#include "google/protobuf/message.h"
#include <cstdint>

#define SHARE_MEMORY_HEAD_SIZE 64

struct ShareMemoryStruct {
    struct alignas(SHARE_MEMORY_HEAD_SIZE) {
        uint32_t writeOffset;
        uint32_t readOffset;
        uint32_t memorySize_;
    } head;
    int8_t data[0];
};

class ShareMemoryBlock {
public:
    ShareMemoryBlock();
    ~ShareMemoryBlock() {}

    bool CreateBlock(std::string name, uint32_t size);
    bool ReleaseBlock();
    bool CreateBlockByFd(std::string name, uint32_t size, int fd);
    bool ReleaseBlockRemote();

    int8_t* GetFreeMemory(uint32_t size);
    bool UseFreeMemory(int8_t* pmem, uint32_t size);
    bool PutRaw(const int8_t* data, uint32_t size);
    bool PutProtobuf(google::protobuf::Message& pmsg);

    uint32_t GetDataSize();
    const int8_t* GetDataPoint();
    bool Next();

    std::string GetName();
    uint32_t GetSize();
    int GetfileDescriptor();

    enum DropType {
        DROP_OLD,  // buffer满时，丢弃最老的数据
        DROP_NONE, // buffer满时，不丢弃老数据，不放入新数据
    };
    void SetDropType(enum DropType dt)
    {
        dropType_ = dt;
    }

private:
    int8_t* GetCurrentFreeMemory(uint32_t size);

    std::string memoryName_;
    uint32_t memorySize_;
    int fileDescriptor_;

    void* memoryPoint_;
    struct ShareMemoryStruct* pMemory_;

    DropType dropType_;
};

#endif
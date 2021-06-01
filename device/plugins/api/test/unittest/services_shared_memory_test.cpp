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

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <thread>

#include "plugin_service_types.pb.h"
#include "share_memory_allocator.h"
#include "share_memory_block.h"

using namespace testing::ext;

namespace {
class ServicesSharedMemoryTest : public ::testing::Test {
protected:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
};

/**
 * @tc.name: server
 * @tc.desc: Shared memory flow test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesSharedMemoryTest, SharedMemoryBlock, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    shareMemoryBlock.GetName();
    shareMemoryBlock.GetSize();
    shareMemoryBlock.GetfileDescriptor();

    shareMemoryBlock.SetDropType(ShareMemoryBlock::DropType::DROP_NONE);
    int8_t data[100];
    for (int i = 0; i < 20; i++) {
        *((uint32_t*)data) = i;
        shareMemoryBlock.PutRaw(data, 100);
    }
    int8_t* p = shareMemoryBlock.GetFreeMemory(100);
    ASSERT_TRUE(p == nullptr);
    do {
        p = const_cast<int8_t*>(shareMemoryBlock.GetDataPoint());
        printf("%p,p=%d\n", p, *((int*)p));
    } while (shareMemoryBlock.Next() && shareMemoryBlock.GetDataSize() > 0);

    NotifyResultResponse response;
    response.set_status(123);
    ASSERT_TRUE(shareMemoryBlock.PutProtobuf(response));
    ASSERT_TRUE(shareMemoryBlock.GetDataSize() > 0);
    response.ParseFromArray(shareMemoryBlock.GetDataPoint(), shareMemoryBlock.GetDataSize());
    ASSERT_TRUE(response.status() == 123);

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: server
 * @tc.desc: Shared memory abnormal flow test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesSharedMemoryTest, SharedMemoryAllocator, TestSize.Level1)
{
    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1) ==
                nullptr); // 创建内存块大小<1024，返回空
    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1024) != nullptr); // 成功创建
    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().CreateMemoryBlockLocal("testname", 1024) ==
                nullptr); // 创建同名内存块返回空

    ASSERT_TRUE(ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockLocal("testname"));
    ASSERT_FALSE(ShareMemoryAllocator::GetInstance().ReleaseMemoryBlockLocal("or")); // 释放不存在的内存块返回-1
}
} // namespace
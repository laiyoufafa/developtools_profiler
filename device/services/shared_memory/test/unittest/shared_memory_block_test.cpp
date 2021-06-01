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

#include <cstring>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "plugin_service_types.pb.h"
#include "share_memory_block.h"

using namespace testing::ext;

namespace {
constexpr size_t ARRAYSIZE = 100;

class SharedMemoryBlockTest : public testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() {}
    void TearDown() {}
};

/**
 * @tc.name: share memory
 * @tc.desc: read lock.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, ReadLock, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: share memory
 * @tc.desc: get name.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, GetName, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    shareMemoryBlock.GetName();

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: share memory
 * @tc.desc: get size.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, GetSize, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    shareMemoryBlock.GetSize();

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: share memory
 * @tc.desc: get file descriptor.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, GetfileDescriptor, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    shareMemoryBlock.GetfileDescriptor();

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: share memory
 * @tc.desc: Shared memory type test.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, DROP_NONE, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    shareMemoryBlock.SetDropType(ShareMemoryBlock::DropType::DROP_NONE);

    int8_t data[ARRAYSIZE];
    for (int i = 0; i < 20; i++) {
        *((uint32_t*)data) = i;
        shareMemoryBlock.PutRaw(data, ARRAYSIZE);
    }
    int8_t* p = shareMemoryBlock.GetFreeMemory(ARRAYSIZE);
    ASSERT_TRUE(p == nullptr);

    do {
        p = const_cast<int8_t*>(shareMemoryBlock.GetDataPoint());
    } while (shareMemoryBlock.Next() && shareMemoryBlock.GetDataSize() > 0);

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: share memory
 * @tc.desc: Shared memory type test.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, DROP_OLD, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    shareMemoryBlock.SetDropType(ShareMemoryBlock::DropType::DROP_OLD);

    int8_t data[ARRAYSIZE];
    for (int i = 0; i < 20; i++) {
        *((uint32_t*)data) = i;
        shareMemoryBlock.PutRaw(data, ARRAYSIZE);
    }
    int8_t* p = shareMemoryBlock.GetFreeMemory(ARRAYSIZE);
    ASSERT_TRUE(p != nullptr);

    do {
        p = const_cast<int8_t*>(shareMemoryBlock.GetDataPoint());
    } while (shareMemoryBlock.Next() && shareMemoryBlock.GetDataSize() > 0);

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}

/**
 * @tc.name: share memory
 * @tc.desc: put protobuf.
 * @tc.type: FUNC
 */
HWTEST_F(SharedMemoryBlockTest, PutProtobuf, TestSize.Level1)
{
    ShareMemoryBlock shareMemoryBlock;
    ASSERT_TRUE(shareMemoryBlock.CreateBlock("testname", 1024));

    NotifyResultResponse response;
    response.set_status(123);
    ASSERT_TRUE(shareMemoryBlock.PutProtobuf(response));
    ASSERT_TRUE(shareMemoryBlock.GetDataSize() > 0);
    response.ParseFromArray(shareMemoryBlock.GetDataPoint(), shareMemoryBlock.GetDataSize());
    ASSERT_TRUE(response.status() == 123);

    ASSERT_TRUE(shareMemoryBlock.ReleaseBlock());
}
} // namespace

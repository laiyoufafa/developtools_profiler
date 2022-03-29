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

#include "bytrace_ops.h"

namespace {
using FTRACE_NS::BytraceOps;
using testing::ext::TestSize;

class BytraceOpsTest : public ::testing::Test {
protected:
    void SetUp() override {}
    void TearDown() override {}
};

/*
 * @tc.name: PrepareListCategoriesCmd
 * @tc.desc: test BytraceOps::PrepareListCategoriesCmd with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(BytraceOpsTest, PrepareListCategoriesCmd, TestSize.Level1)
{
    std::unique_ptr<BytraceOps> bytraceOps = std::make_unique<BytraceOps>();
    EXPECT_TRUE(bytraceOps->PrepareListCategoriesCmd());
}

/*
 * @tc.name: PrepareEnableCategoriesCmd
 * @tc.desc: test BytraceOps::PrepareEnableCategoriesCmd with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(BytraceOpsTest, PrepareEnableCategoriesCmd, TestSize.Level1)
{
    std::unique_ptr<BytraceOps> bytraceOps = std::make_unique<BytraceOps>();
    EXPECT_TRUE(bytraceOps->PrepareEnableCategoriesCmd());
}

/*
 * @tc.name: PrepareDisableCategoriesCmd
 * @tc.desc: test BytraceOps::PrepareDisableCategoriesCmd with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(BytraceOpsTest, PrepareDisableCategoriesCmd, TestSize.Level1)
{
    std::unique_ptr<BytraceOps> bytraceOps = std::make_unique<BytraceOps>();
    EXPECT_TRUE(bytraceOps->PrepareDisableCategoriesCmd());
}
} // namespace
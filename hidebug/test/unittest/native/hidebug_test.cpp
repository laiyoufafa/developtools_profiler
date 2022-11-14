/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
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

#include "hidebug_base.h"

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include <cstdlib>

using namespace testing::ext;

namespace {
class HidebugTest : public ::testing::Test {
protected:
    void SetUp() override {}
    void TearDown() override {}
};

/**
 * @tc.name: InitEnvironmentParam
 * @tc.desc: test InitEnvironmentParam
 * @tc.type: FUNC
 */
HWTEST_F(HidebugTest, InitEnvironmentParam, TestSize.Level1)
{
    system("param set hiviewdfx.debugenv.hidebug_test aaa:bbb");
    const char* inputName = "hidebug_test";
    EXPECT_TRUE(InitEnvironmentParam(inputName));
}
} // namespace

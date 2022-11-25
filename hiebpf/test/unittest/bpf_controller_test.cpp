/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
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
#include <string>
#include <thread>

#include <gtest/gtest.h>

#include "bpf_controller.h"

using namespace testing::ext;

namespace OHOS {
namespace Developtools {
namespace Hiebpf {
class BpfControllerTest : public ::testing::Test {
public:
    static void SetUpTestCase() {};
    static void TearDownTestCase() {};

    void SetUp() {}
    void TearDown() {}
};

/**
 * @tc.name: Normal
 * @tc.desc: Test framework
 * @tc.type: FUNC
 */
HWTEST_F(BpfControllerTest, Normal, TestSize.Level1)
{
    BPFConfig cfg;
    cfg.selectEventGroups_.insert(HiebpfEventGroup::FS_GROUP_OPEN);
    const uint32_t duration = 10;
    cfg.traceDuration_ = duration;
    std::unique_ptr<BPFController> pCtx = BPFController::MakeUnique(cfg);
    ASSERT_TRUE(pCtx != nullptr);
    std::thread threadContol([&]() {
        ASSERT_EQ(pCtx->Start(), 0);
    });

    sleep(1);
    pCtx->Stop();
    if (threadContol.joinable()) {
        threadContol.join();
    }
}
} // namespace Hiebpf
} // namespace Developtools
} // namespace OHOS

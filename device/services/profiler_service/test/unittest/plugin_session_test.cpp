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

#include "plugin_service.h"
#include "plugin_service_stubs.h"
#include "plugin_session.h"
#include "profiler_data_repeater.h"

using namespace testing::ext;

namespace {
constexpr int DATA_MAX_SIZE = 10; // set max size 10;

using PluginServicePtr = STD_PTR(shared, PluginService);

class PluginSessionTest : public ::testing::Test {
protected:
    ProfilerPluginConfig config;
    PluginInfo pluginInfo;
    PluginServicePtr service;
    ProfilerDataRepeaterPtr repeater;

    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() override
    {
        config.set_name("test_session");
        pluginInfo.name = config.name();
        service = std::make_shared<PluginService>();
        repeater = std::make_shared<ProfilerDataRepeater>(DATA_MAX_SIZE); // set max size 10;
        if (service) {
            service->AddPluginInfo(pluginInfo);
        }
    }
    void TearDown() override
    {
        if (service) {
            service->RemovePluginInfo(pluginInfo);
        }
    }
};

/**
 * @tc.name: server
 * @tc.desc: Session instantiation.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, CtorDtor, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);
    EXPECT_NE(session, nullptr);
}

/**
 * @tc.name: server
 * @tc.desc: Create session.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, Create, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session, nullptr);
    EXPECT_TRUE(session->IsAvailable());

    // create again must be failed
    EXPECT_FALSE(session->Create());

    config.set_name("test_session2");
    session = std::make_shared<PluginSession>(config, service, repeater);
    ASSERT_NE(session, nullptr);
    EXPECT_FALSE(session->IsAvailable());
}

/**
 * @tc.name: server
 * @tc.desc: Destroy session.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, Destroy, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session, nullptr);
    EXPECT_TRUE(session->IsAvailable());

    EXPECT_TRUE(session->Destroy());
    EXPECT_FALSE(session->IsAvailable());

    // destroy again must be failed
    EXPECT_FALSE(session->Destroy());

    // recreate is OK
    EXPECT_TRUE(session->Create());
    EXPECT_TRUE(session->Destroy());
}

/**
 * @tc.name: server
 * @tc.desc: get session state.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, GetState, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session, nullptr);
    EXPECT_EQ(session->GetState(), PluginSession::CREATED);

    EXPECT_TRUE(session->Start());
    EXPECT_EQ(session->GetState(), PluginSession::STARTED);

    EXPECT_TRUE(session->Stop());
    EXPECT_EQ(session->GetState(), PluginSession::CREATED);

    EXPECT_TRUE(session->Destroy());
    EXPECT_EQ(session->GetState(), PluginSession::INITIAL);
}

/**
 * @tc.name: server
 * @tc.desc: Is available session.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, IsAvailable, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session, nullptr);
    EXPECT_TRUE(session->IsAvailable());

    session.reset();
    config.set_name("test_session2");
    session = std::make_shared<PluginSession>(config, service, repeater);
    ASSERT_NE(session, nullptr);
    EXPECT_FALSE(session->IsAvailable());
}

/**
 * @tc.name: server
 * @tc.desc: start session.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, Start, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session, nullptr);
    EXPECT_TRUE(session->Start());

    session.reset();
    config.set_name("test_session2");
    session = std::make_shared<PluginSession>(config, service, repeater);
    ASSERT_NE(session, nullptr);

    EXPECT_FALSE(session->Start());
}

/**
 * @tc.name: server
 * @tc.desc: stop session.
 * @tc.type: FUNC
 */
HWTEST_F(PluginSessionTest, Stop, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session, nullptr);
    EXPECT_FALSE(session->Stop()); // stop without start must be failed

    EXPECT_TRUE(session->Start());
    EXPECT_TRUE(session->Stop());

    session.reset();
    config.set_name("test_session2");
    session = std::make_shared<PluginSession>(config, service, repeater);
    ASSERT_NE(session, nullptr);

    EXPECT_FALSE(session->Stop());
}
} // namespace
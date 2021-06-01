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

#define final // disable final keyword
#define LOG_TAG "ProfilerServiceTest"

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <thread>

#include "logging.h"
#include "plugin_service.h"
#include "profiler_capability_manager.h"
#include "profiler_data_repeater.h"
#include "profiler_service.h"

using namespace testing::ext;

using ProfilerServicePtr = STD_PTR(shared, ProfilerService);
class ProfilerServiceTest : public ::testing::Test {
protected:
    PluginServicePtr pluginService_;
    ProfilerServicePtr service_;

    PluginInfo pluginInfo;
    std::unique_ptr<grpc::ServerContext> context_;
    std::atomic<int> requestCounter{0};

    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() override
    {
        pluginInfo.name = "test_plugin";
        pluginService_ = std::make_shared<PluginService>();
        if (pluginService_) {
            pluginService_->AddPluginInfo(pluginInfo);
        }
        service_ = std::make_shared<ProfilerService>(pluginService_);
        context_ = std::make_unique<grpc::ServerContext>();
    }

    void TearDown() override
    {
        context_.reset();
        service_.reset();
        if (pluginService_) {
            pluginService_->RemovePluginInfo(pluginInfo);
        }
        pluginService_.reset();
        ProfilerCapabilityManager::GetInstance().pluginCapabilities_.clear();
    }

    grpc::Status StartSession(uint32_t sessionId)
    {
        StartSessionRequest request;
        StartSessionResponse response;

        request.set_session_id(sessionId);
        request.set_request_id(++requestCounter);
        return service_->StartSession(context_.get(), &request, &response);
    }

    grpc::Status StopSession(uint32_t sessionId)
    {
        StopSessionRequest request;
        StopSessionResponse response;

        request.set_session_id(sessionId);
        request.set_request_id(++requestCounter);
        return service_->StopSession(context_.get(), &request, &response);
    }

    grpc::Status DestroySession(uint32_t sessionId)
    {
        DestroySessionRequest request;
        DestroySessionResponse response;

        request.set_session_id(sessionId);
        request.set_request_id(++requestCounter);
        return service_->DestroySession(context_.get(), &request, &response);
    }

    void FetchDataOnlineSet(uint32_t& sessionId);
};

/**
 * @tc.name: server
 * @tc.desc: Abnormal test.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, CtorDtor, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
}

/**
 * @tc.name: server
 * @tc.desc: get plugin capabilities.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, GetCapabilities, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    GetCapabilitiesRequest request;
    GetCapabilitiesResponse response;

    ProfilerPluginCapability cap;
    cap.set_name("cap1");
    ProfilerCapabilityManager::GetInstance().AddCapability(cap);

    request.set_request_id(++requestCounter);
    auto status = service_->GetCapabilities(context_.get(), &request, &response);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
    EXPECT_GT(response.capabilities_size(), 0);
    HILOG_DEBUG(LOG_CORE, "GetCapabilities, capabilities_size = %d", response.capabilities_size());
}

/**
 * @tc.name: server
 * @tc.desc: create session.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, CreateSession, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    CreateSessionRequest request;
    CreateSessionResponse response;

    auto status = service_->CreateSession(context_.get(), &request, &response);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    auto sessionConfig = request.mutable_session_config();
    ASSERT_NE(sessionConfig, nullptr);
    sessionConfig->set_session_mode(ProfilerSessionConfig::OFFLINE);
    sessionConfig->set_result_file("trace.bin");

    auto pluginConfig = request.add_plugin_configs();
    ASSERT_NE(pluginConfig, nullptr);
    pluginConfig->set_name(pluginInfo.name);

    request.set_request_id(++requestCounter);
    status = service_->CreateSession(context_.get(), &request, &response);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
}

/**
 * @tc.name: server
 * @tc.desc: create session online.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, CreateSessionOnline, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    CreateSessionRequest request;
    CreateSessionResponse response;

    auto sessionConfig = request.mutable_session_config();
    ASSERT_NE(sessionConfig, nullptr);
    sessionConfig->set_session_mode(ProfilerSessionConfig::ONLINE);
    sessionConfig->clear_result_file();

    auto pluginConfig = request.add_plugin_configs();
    ASSERT_NE(pluginConfig, nullptr);
    pluginConfig->set_name(pluginInfo.name);

    request.set_request_id(++requestCounter);
    auto status = service_->CreateSession(context_.get(), &request, &response);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
}

/**
 * @tc.name: server
 * @tc.desc: destroy session.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, DestroySession, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);

    EXPECT_NE(DestroySession(0).error_code(), grpc::StatusCode::OK);

    uint32_t sessionId = 0;
    {
        CreateSessionRequest request;
        CreateSessionResponse response;

        auto sessionConfig = request.mutable_session_config();
        ASSERT_NE(sessionConfig, nullptr);
        sessionConfig->set_session_mode(ProfilerSessionConfig::OFFLINE);
        sessionConfig->set_result_file("trace.bin");

        auto pluginConfig = request.add_plugin_configs();
        ASSERT_NE(pluginConfig, nullptr);
        pluginConfig->set_name(pluginInfo.name);

        request.set_request_id(++requestCounter);
        auto status = service_->CreateSession(context_.get(), &request, &response);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
        sessionId = response.session_id();
    }

    EXPECT_EQ(DestroySession(sessionId).error_code(), grpc::StatusCode::OK);
}

/**
 * @tc.name: server
 * @tc.desc: start session.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, StartSession, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    auto status = StartSession(0);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    uint32_t sessionId = 0;
    {
        CreateSessionRequest request;
        CreateSessionResponse response;

        auto sessionConfig = request.mutable_session_config();
        ASSERT_NE(sessionConfig, nullptr);
        sessionConfig->set_session_mode(ProfilerSessionConfig::OFFLINE);
        sessionConfig->set_result_file("trace.bin");

        auto pluginConfig = request.add_plugin_configs();
        ASSERT_NE(pluginConfig, nullptr);
        pluginConfig->set_name(pluginInfo.name);

        request.set_request_id(++requestCounter);
        auto status = service_->CreateSession(context_.get(), &request, &response);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
        sessionId = response.session_id();
    }

    status = StartSession(sessionId);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
}

/**
 * @tc.name: server
 * @tc.desc: start session by update config.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, StartSessionUpdateConfigs, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    uint32_t sessionId = 0;
    {
        CreateSessionRequest request;
        CreateSessionResponse response;

        auto sessionConfig = request.mutable_session_config();
        ASSERT_NE(sessionConfig, nullptr);
        sessionConfig->set_session_mode(ProfilerSessionConfig::OFFLINE);
        sessionConfig->set_result_file("trace.bin");

        auto pluginConfig = request.add_plugin_configs();
        ASSERT_NE(pluginConfig, nullptr);
        pluginConfig->set_name(pluginInfo.name);

        request.set_request_id(++requestCounter);
        auto status = service_->CreateSession(context_.get(), &request, &response);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
        sessionId = response.session_id();
    }

    StartSessionRequest request;
    StartSessionResponse response;

    request.set_session_id(sessionId);
    request.set_request_id(++requestCounter);
    auto pluginConfig = request.add_update_configs();
    ASSERT_NE(pluginConfig, nullptr);

    pluginConfig->set_name(pluginInfo.name);
    pluginConfig->set_sample_interval(1000);
    auto status = service_->StartSession(context_.get(), &request, &response);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
}


/**
 * @tc.name: server
 * @tc.desc: stop session.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, StopSession, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    auto status = StopSession(0);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    uint32_t sessionId = 0;
    {
        CreateSessionRequest request;
        CreateSessionResponse response;

        auto sessionConfig = request.mutable_session_config();
        ASSERT_NE(sessionConfig, nullptr);
        sessionConfig->set_session_mode(ProfilerSessionConfig::OFFLINE);
        sessionConfig->set_result_file("trace.bin");

        auto pluginConfig = request.add_plugin_configs();
        ASSERT_NE(pluginConfig, nullptr);
        pluginConfig->set_name(pluginInfo.name);

        request.set_request_id(++requestCounter);
        auto status = service_->CreateSession(context_.get(), &request, &response);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);

        sessionId = response.session_id();
        {
            StartSessionRequest request;
            StartSessionResponse response;

            request.set_session_id(sessionId);
            request.set_request_id(++requestCounter);
            auto status = service_->StartSession(context_.get(), &request, &response);
            EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
        }
    }

    status = StopSession(sessionId);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
}

class FakeServerWriter : public ::grpc::ServerWriter<::FetchDataResponse> {
public:
    FakeServerWriter(::grpc::internal::Call* call, grpc::ServerContext* ctx)
        : ::grpc::ServerWriter<::FetchDataResponse>(call, ctx)
    {
    }
    ~FakeServerWriter() = default;
    using grpc::internal::WriterInterface<::FetchDataResponse>::Write;
    bool Write(const ::FetchDataResponse& msg, ::grpc::WriteOptions options) override
    {
        HILOG_DEBUG(LOG_CORE, "FakeServerWriter::Write %zu bytes!", msg.ByteSizeLong());
        return true;
    }
};

/**
 * @tc.name: server
 * @tc.desc: fetch data.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, FetchData, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    FetchDataRequest request;

    auto writer = std::make_unique<FakeServerWriter>(nullptr, context_.get());

    request.set_request_id(++requestCounter);
    auto status = service_->FetchData(context_.get(), &request, writer.get());
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    uint32_t sessionId = 0;
    {
        CreateSessionRequest request;
        CreateSessionResponse response;

        auto sessionConfig = request.mutable_session_config();
        ASSERT_NE(sessionConfig, nullptr);
        sessionConfig->set_session_mode(ProfilerSessionConfig::OFFLINE);
        sessionConfig->set_result_file("trace.bin");

        auto pluginConfig = request.add_plugin_configs();
        ASSERT_NE(pluginConfig, nullptr);
        pluginConfig->set_name(pluginInfo.name);

        request.set_request_id(++requestCounter);
        auto status = service_->CreateSession(context_.get(), &request, &response);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);

        sessionId = response.session_id();
        {
            StartSessionRequest request;
            StartSessionResponse response;

            request.set_session_id(sessionId);
            request.set_request_id(++requestCounter);
            auto status = service_->StartSession(context_.get(), &request, &response);
            EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
        }
    }

    request.set_session_id(sessionId);
    request.set_request_id(++requestCounter);
    status = service_->FetchData(context_.get(), &request, writer.get());
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);

    EXPECT_EQ(StopSession(sessionId).error_code(), grpc::StatusCode::OK);
    EXPECT_EQ(DestroySession(sessionId).error_code(), grpc::StatusCode::OK);
}

void ProfilerServiceTest::FetchDataOnlineSet(uint32_t& sessionId)
{
    CreateSessionRequest request;
    CreateSessionResponse response;

    auto sessionConfig = request.mutable_session_config();
    ASSERT_NE(sessionConfig, nullptr);
    sessionConfig->set_session_mode(ProfilerSessionConfig::ONLINE);

    auto pluginConfig = request.add_plugin_configs();
    ASSERT_NE(pluginConfig, nullptr);
    pluginConfig->set_name(pluginInfo.name);

    request.set_request_id(++requestCounter);
    auto status = service_->CreateSession(context_.get(), &request, &response);
    EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);

    sessionId = response.session_id();
    {
        StartSessionRequest request;
        StartSessionResponse response;

        request.set_session_id(sessionId);
        request.set_request_id(++requestCounter);
        auto status = service_->StartSession(context_.get(), &request, &response);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
    }
}

/**
 * @tc.name: server
 * @tc.desc: fetch data online.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, FetchDataOnline, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    FetchDataRequest request;

    auto writer = std::make_unique<FakeServerWriter>(nullptr, context_.get());

    request.set_request_id(++requestCounter);
    auto status = service_->FetchData(context_.get(), &request, writer.get());
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    uint32_t sessionId = 0;
    FetchDataOnlineSet(sessionId);

    auto sessionCtx = service_->GetSessionContext(sessionId);
    ASSERT_NE(sessionCtx->dataRepeater, nullptr);

    const int pluginDataCount = 10;
    std::thread dataProducer([&]() {
        for (int i = 0; i < pluginDataCount; i++) {
            auto data = std::make_shared<ProfilerPluginData>();
            ASSERT_NE(data, nullptr);
            data->set_name(pluginInfo.name);
            data->set_status(i);
            sessionCtx->dataRepeater->PutPluginData(data);
        }
    });

    request.set_session_id(sessionId);
    request.set_request_id(++requestCounter);

    std::thread dataConsumer([&]() {
        status = service_->FetchData(context_.get(), &request, writer.get());
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
    });

    dataProducer.join();

    usleep(100 * 1000); // wait for reader take done!
    sessionCtx->dataRepeater->Close();
    dataConsumer.join();

    EXPECT_EQ(StopSession(sessionId).error_code(), grpc::StatusCode::OK);
    EXPECT_EQ(DestroySession(sessionId).error_code(), grpc::StatusCode::OK);
}

/**
 * @tc.name: server
 * @tc.desc: start service.
 * @tc.type: FUNC
 */
HWTEST_F(ProfilerServiceTest, StartService, TestSize.Level1)
{
    auto service = std::make_unique<ProfilerService>();
    EXPECT_NE(service, nullptr);
    EXPECT_FALSE(service->StartService(""));

    std::thread waiterThread(&ProfilerService::WaitServiceDone, service.get());
    sleep(1);

    service->StopService();
    waiterThread.join();
}

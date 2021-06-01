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

#include "logging.h"
#include "plugin_service.h"
#include "plugin_session.h"
#include "profiler_capability_manager.h"
#include "profiler_data_repeater.h"
#include "profiler_service.h"
#include "result_demuxer.h"
#include "trace_file_reader.h"
#include "trace_file_writer.h"

using namespace testing::ext;

namespace {
#if defined(__i386__) || defined(__x86_64__)
const std::string DEFAULT_TEST_PATH("./");
#else
const std::string DEFAULT_TEST_PATH("/data/local/tmp/");
#endif

using PluginServicePtr = STD_PTR(shared, PluginService);
using ProfilerDataRepeaterPtr = STD_PTR(shared, ProfilerDataRepeater);
using ProfilerServicePtr = STD_PTR(shared, ProfilerService);
using ProfilerPluginDataPtr = STD_PTR(shared, ProfilerPluginData);

constexpr int DATA_MAX_SIZE = 10;

class ServicesProfilerServiceTest : public ::testing::Test {
protected:
    ProfilerPluginConfig config;
    ProfilerSessionConfig::BufferConfig bufferConfig;
    PluginInfo pluginInfo;
    PluginServicePtr service;
    ProfilerDataRepeaterPtr repeater;
    ProfilerServicePtr service_;
    std::unique_ptr<grpc::ServerContext> context_;
    std::atomic<int> requestCounter{0};

    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() override
    {
        config.set_name("test_session");
        bufferConfig.set_pages(0);
        pluginInfo.name = config.name();
        service = std::make_shared<PluginService>();
        repeater = std::make_shared<ProfilerDataRepeater>(DATA_MAX_SIZE);
        if (service) {
            service->AddPluginInfo(pluginInfo);
        }

        service_ = std::make_shared<ProfilerService>(service);
        context_ = std::make_unique<grpc::ServerContext>();
    }
    void TearDown() override
    {
        if (service) {
            service->RemovePluginInfo(pluginInfo);
        }
        ProfilerCapabilityManager::GetInstance().pluginCapabilities_.clear();
    }
};

/**
 * @tc.name: plugin
 * @tc.desc: Plugin session flow test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, PluginSession, TestSize.Level1)
{
    auto session = std::make_shared<PluginSession>(config, nullptr, nullptr);
    EXPECT_NE(session, nullptr);
    EXPECT_FALSE(session->IsAvailable());

    EXPECT_FALSE(session->Create());
    config.set_name("test_session2");
    session = std::make_shared<PluginSession>(config, service, repeater);
    repeater->Size();

    session.reset();
    config.set_name("test_session3");
    session = std::make_shared<PluginSession>(config, service, repeater);

    ASSERT_NE(session->GetState(), PluginSession::CREATED);
    EXPECT_FALSE(session->Start());
    ASSERT_NE(session->GetState(), PluginSession::STARTED);
    EXPECT_FALSE(session->Stop());
    ASSERT_NE(session->GetState(), PluginSession::CREATED);
    EXPECT_FALSE(session->Destroy());
    EXPECT_EQ(session->GetState(), PluginSession::INITIAL);

    // recreate is OK
    EXPECT_FALSE(session->Create());
    EXPECT_FALSE(session->Destroy());
    repeater->Reset();
}

/**
 * @tc.name: plugin
 * @tc.desc: Streaming session test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, TraceFileWriter, TestSize.Level1)
{
    std::string path = "trace.bin";
    auto writer = std::make_shared<TraceFileWriter>(path);
    EXPECT_NE(writer, nullptr);

    std::string testData = "Hello, Wrold!";
    EXPECT_EQ(writer->Write(testData.data(), testData.size()), sizeof(uint32_t) + testData.size());
    EXPECT_EQ(writer->Flush(), true);

    ProfilerPluginData pluginData;
    pluginData.set_name("ABC");
    pluginData.set_status(0);
    pluginData.set_data("DEF");
    EXPECT_GT(writer->Write(pluginData), 0);
}

/**
 * @tc.name: plugin
 * @tc.desc: Streaming session test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, TraceFileReader, TestSize.Level1)
{
    std::string path = "trace-write-msg.bin";
    auto writer = std::make_shared<TraceFileWriter>(path);
    ASSERT_NE(writer, nullptr);

    constexpr int n = 100;
    for (int i = 1; i <= n; i++) {
        ProfilerPluginData pluginData{};
        pluginData.set_name("test_name");
        pluginData.set_status(i);
        pluginData.set_data("Hello, Wrold!");
        long bytes = writer->Write(pluginData);
        EXPECT_EQ(bytes, sizeof(uint32_t) + pluginData.ByteSizeLong());
        HILOG_INFO(LOG_CORE, "[%d/%d] write %ld bytes to %s.", i, n, bytes, path.c_str());
    }
    writer.reset(); // make sure write done!

    auto reader = std::make_shared<TraceFileReader>();
    ASSERT_NE(reader, nullptr);
    ASSERT_TRUE(reader->Open(path));
    for (int i = 1; i <= n; i++) {
        ProfilerPluginData data{};
        long bytes = reader->Read(data);
        HILOG_INFO(LOG_CORE, "data = {%s, %d, %s}", data.name().c_str(), data.status(), data.data().c_str());
        HILOG_INFO(LOG_CORE, "read %ld bytes from %s", bytes, path.c_str());
    }

    ASSERT_TRUE(reader->Open(path));
    long bytes = 0;
    do {
        ProfilerPluginData data{};
        bytes = reader->Read(data);
        HILOG_INFO(LOG_CORE, "data = {%s, %d, %s}", data.name().c_str(), data.status(), data.data().c_str());
        HILOG_INFO(LOG_CORE, "read %ld bytes from %s", bytes, path.c_str());
    } while (bytes > 0);

    ASSERT_EQ(reader->Read(nullptr, 0), 0);
}

/**
 * @tc.name: service
 * @tc.desc: Streaming session report result test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, ResultDemuxer1, TestSize.Level1)
{
    std::string path = "demux.bin";
    ProfilerDataRepeaterPtr repeater = std::make_shared<ProfilerDataRepeater>(DATA_MAX_SIZE);

    auto demuxer = std::make_shared<ResultDemuxer>(repeater);
    EXPECT_NE(demuxer, nullptr);
    demuxer->SetTraceWriter(nullptr);

    auto writer = std::make_shared<TraceFileWriter>(path);
    EXPECT_NE(writer, nullptr);
    demuxer->SetTraceWriter(writer);

    const int putCount = 20;
    const int putDelayUs = 10 * 1000;
    demuxer->StartTakeResults();
    std::thread dataProducer([=] {
        for (int i = 0; i < putCount; i++) {
            auto pluginData = std::make_shared<ProfilerPluginData>();
            ASSERT_NE(pluginData, nullptr);
            pluginData->set_name("test-" + std::to_string(i));
            pluginData->set_status(i);
            repeater->PutPluginData(pluginData);
            HILOG_DEBUG(LOG_CORE, "put test data %d...", i);
            usleep(putDelayUs);
        }
        repeater->PutPluginData(nullptr);
    });

    HILOG_DEBUG(LOG_CORE, "wating producer thread done...");
    dataProducer.join();
}

/**
 * @tc.name: service
 * @tc.desc: Streaming session report result test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, ResultDemuxer2, TestSize.Level1)
{
    std::string path = "demux.bin";
    ProfilerDataRepeaterPtr repeater = std::make_shared<ProfilerDataRepeater>(DATA_MAX_SIZE);

    auto demuxer = std::make_shared<ResultDemuxer>(repeater);
    ASSERT_NE(demuxer, nullptr);

    auto writer = std::make_shared<TraceFileWriter>(path);
    EXPECT_NE(writer, nullptr);
    demuxer->SetTraceWriter(writer);

    const int putCount = 30;
    const int putDelayUs = 10 * 1000;
    demuxer->StartTakeResults();
    std::thread dataProducer([=] {
        for (int i = 0; i < putCount; i++) {
            auto pluginData = std::make_shared<ProfilerPluginData>();
            ASSERT_NE(pluginData, nullptr);
            pluginData->set_name("AB-" + std::to_string(i));
            pluginData->set_status(i);

            HILOG_DEBUG(LOG_CORE, "put test data %d...", i);
            if (!repeater->PutPluginData(pluginData)) {
                HILOG_WARN(LOG_CORE, "put test data %d FAILED!", i);
                break;
            }
            usleep(putDelayUs);
        }
    });

    usleep((putCount / 2) * putDelayUs);
    demuxer->StopTakeResults();

    repeater->Close();
    HILOG_DEBUG(LOG_CORE, "wating producer thread done...");
    dataProducer.join();
}

/**
 * @tc.name: server
 * @tc.desc: Profiler capacity management.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, ProfilerCapabilityManager, TestSize.Level1)
{
    std::vector<ProfilerPluginCapability> caps = ProfilerCapabilityManager::GetInstance().GetCapabilities();
    for (int i = 0; i < caps.size(); i++) {
        auto cap = caps[i];
        EXPECT_TRUE(ProfilerCapabilityManager::GetInstance().RemoveCapability(cap.name()));
    }
    caps.clear();

    EXPECT_EQ(ProfilerCapabilityManager::GetInstance().GetCapability("xxx"), nullptr);
    EXPECT_EQ(ProfilerCapabilityManager::GetInstance().GetCapabilities().size(), 0);

    caps = ProfilerCapabilityManager::GetInstance().GetCapabilities();
    EXPECT_EQ(caps.size(), 0);

    const int n = 10;
    for (int i = 0; i < n; i++) {
        ProfilerPluginCapability cap;
        cap.set_path("/system/lib/libcap_" + std::to_string(i) + ".so");
        cap.set_name("cap_" + std::to_string(i));
        EXPECT_TRUE(ProfilerCapabilityManager::GetInstance().AddCapability(cap));
        EXPECT_EQ(ProfilerCapabilityManager::GetInstance().GetCapabilities().size(), i + 1);
    }

    for (int i = 0; i < n; i++) {
        ProfilerPluginCapability cap;
        cap.set_name("cap_" + std::to_string(i));
        auto capPtr = ProfilerCapabilityManager::GetInstance().GetCapability(cap.name());
        ASSERT_NE(capPtr, nullptr);
        EXPECT_EQ(capPtr->name(), cap.name());
    }

    ProfilerPluginCapability cap1;
    cap1.set_path("/system/lib/libcap1.so");
    cap1.set_name("cap1");
    EXPECT_TRUE(ProfilerCapabilityManager::GetInstance().AddCapability(cap1));

    cap1.set_path("/system/lib/libcap2.so");
    EXPECT_TRUE(ProfilerCapabilityManager::GetInstance().UpdateCapability(cap1.name(), cap1));
    EXPECT_TRUE(ProfilerCapabilityManager::GetInstance().RemoveCapability(cap1.name()));

    caps = ProfilerCapabilityManager::GetInstance().GetCapabilities();
    EXPECT_EQ(caps.size(), n);
    for (int i = 0; i < caps.size(); i++) {
        auto cap = caps[i];
        EXPECT_TRUE(ProfilerCapabilityManager::GetInstance().RemoveCapability(cap.name()));

        EXPECT_EQ(ProfilerCapabilityManager::GetInstance().GetCapabilities().size(), n - (i + 1));
    }
}

/**
 * @tc.name: server
 * @tc.desc: Profiler data repeater.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, ProfilerDataRepeater, TestSize.Level1)
{
    const int itemCounts = 10000;
    const int bufferSize = itemCounts;
    auto inDataRepeater = std::make_shared<ProfilerDataRepeater>(bufferSize);
    ASSERT_NE(inDataRepeater, nullptr);

    auto outDataRepeater = std::make_shared<ProfilerDataRepeater>(bufferSize);
    ASSERT_NE(outDataRepeater, nullptr);

    auto f = [](int x) { return 2 * x + 1; };
    std::thread worker([&]() {
        for (int i = 0; i < itemCounts; i++) {
            auto xData = inDataRepeater->TakePluginData();

            // compute in worker thread
            int x = xData ? std::stoi(xData->data()) : 0;
            int y = f(x);

            auto yData = std::make_shared<ProfilerPluginData>();
            yData->set_data(std::to_string(y));
            outDataRepeater->PutPluginData(yData);
        }
    });

    std::vector<int> yVec;
    for (int i = 0; i < itemCounts; i++) {
        int x0 = i;
        auto xData = std::make_shared<ProfilerPluginData>();
        xData->set_data(std::to_string(x0));
        inDataRepeater->PutPluginData(xData);

        int y0 = f(x0);
        yVec.push_back(y0);
    }
    worker.join();

    std::vector<ProfilerPluginDataPtr> pluginDataVec;
    auto count = outDataRepeater->TakePluginData(pluginDataVec);
    EXPECT_EQ(count, yVec.size());

    for (size_t i = 0; i < pluginDataVec.size(); i++) {
        auto yData = pluginDataVec[i];
        int y = yData ? std::stoi(yData->data()) : 0;
        EXPECT_EQ(y, yVec[i]);
    }
}

/**
 * @tc.name: server
 * @tc.desc: Session flow test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, ProfilerService1, TestSize.Level1)
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
 * @tc.desc: Session flow test.
 * @tc.type: FUNC
 */
HWTEST_F(ServicesProfilerServiceTest, ProfilerService2, TestSize.Level1)
{
    ASSERT_NE(service_, nullptr);
    ASSERT_NE(context_, nullptr);

    CreateSessionRequest request;
    CreateSessionResponse response;

    auto status = service_->CreateSession(context_.get(), &request, &response);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    StartSessionRequest startrequest;
    StartSessionResponse startresponse;
    startrequest.set_session_id(0);
    startrequest.set_request_id(++requestCounter);
    status = service_->StartSession(context_.get(), &startrequest, &startresponse);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    StopSessionRequest stoprequest;
    StopSessionResponse stopresponse;
    stoprequest.set_session_id(0);
    stoprequest.set_request_id(++requestCounter);
    status = service_->StopSession(context_.get(), &stoprequest, &stopresponse);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);

    DestroySessionRequest destroyrequest;
    DestroySessionResponse destroyresponse;
    destroyrequest.set_session_id(0);
    destroyrequest.set_request_id(++requestCounter);
    status = service_->DestroySession(context_.get(), &destroyrequest, &destroyresponse);
    EXPECT_NE(status.error_code(), grpc::StatusCode::OK);
}
} // namespace

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
#define LOG_TAG "ProfilerService"
#include "profiler_service.h"
#include "logging.h"
#include "plugin_service.h"
#include "plugin_session.h"
#include "profiler_capability_manager.h"
#include "profiler_data_repeater.h"
#include "result_demuxer.h"
#include "trace_file_writer.h"

#include <grpcpp/grpcpp.h>

using namespace ::grpc;

#define CHECK_REQUEST_RESPONSE(arg, response)                                  \
    if (request == nullptr || response == nullptr) {                           \
        HILOG_WARN(LOG_CORE, "%s: FAILED, %s", __func__, "request or response invalid!");     \
        return {StatusCode::INVALID_ARGUMENT, "request or response invalid!"}; \
    }

#define CHECK_POINTER_NOTNULL(ptr, errorMessage)          \
    if (ptr == nullptr) {                                 \
        HILOG_WARN(LOG_CORE, "%s: FAILED, %s is null!", __func__, #ptr); \
        return {StatusCode::INTERNAL, errorMessage};      \
    }

#define CHECK_EXPRESSION_TRUE(expr, errorMessage)        \
    if (!(expr)) {                                       \
        HILOG_WARN(LOG_CORE, "%s: FAILED, %s", __func__, errorMessage); \
        return {StatusCode::INTERNAL, (errorMessage)};   \
    }

ProfilerService::ProfilerService(const PluginServicePtr& pluginService) : pluginService_(pluginService) {}

ProfilerService::~ProfilerService() {}

ProfilerService::SessionContext::~SessionContext()
{
    HILOG_INFO(LOG_CORE, "~SessionContext id = %d", id);
}

Status ProfilerService::GetCapabilities(ServerContext* context,
                                        const ::GetCapabilitiesRequest* request,
                                        ::GetCapabilitiesResponse* response)
{
    HILOG_INFO(LOG_CORE, "GetCapabilities from '%s'", context->peer().c_str());
    CHECK_REQUEST_RESPONSE(request, response);

    HILOG_INFO(LOG_CORE, "GetCapabilities %d start", request->request_id());
    std::vector<ProfilerPluginCapability> capabilities = ProfilerCapabilityManager::GetInstance().GetCapabilities();

    response->set_status(StatusCode::OK);
    for (size_t i = 0; i < capabilities.size(); i++) {
        *response->add_capabilities() = capabilities[i];
    }
    HILOG_INFO(LOG_CORE, "GetCapabilities %d done!", request->request_id());
    return Status::OK;
}

PluginSessionPtr ProfilerService::SessionContext::CreatePluginSession(
    const PluginServicePtr& pluginService, const ProfilerPluginConfig& pluginConfig)
{
    auto name = pluginConfig.name();
    CHECK_TRUE(pluginSessions.count(name) == 0, nullptr, "plugin name %s exists!", name.c_str());

    auto session = std::make_shared<PluginSession>(pluginConfig, pluginService, dataRepeater);
    CHECK_NOTNULL(session, nullptr, "allocate plugin session for %s failed!", name.c_str());
    CHECK_TRUE(session->IsAvailable(), nullptr, "config plugin for %s failed!", name.c_str());
    return session;
}

namespace {
constexpr uint32_t MAX_BUFFER_PAGES = 512 * 1024 * 1024 / 4096;
}

bool ProfilerService::SessionContext::CheckBufferConfig(const ProfilerSessionConfig::BufferConfig& bufferConfig)
{
    const uint32_t pages = bufferConfig.pages();
    const auto policy = bufferConfig.policy();
    return (pages > 0 && pages <= MAX_BUFFER_PAGES) && (policy == ProfilerSessionConfig::BufferConfig::RECYCLE ||
                                                        policy == ProfilerSessionConfig::BufferConfig::FLATTEN);
}

PluginSessionPtr ProfilerService::SessionContext::CreatePluginSession(
    const PluginServicePtr& pluginService,
    const ProfilerPluginConfig& pluginConfig,
    const ProfilerSessionConfig::BufferConfig& bufferConfig)
{
    auto name = pluginConfig.name();
    CHECK_TRUE(pluginSessions.count(name) == 0, nullptr, "plugin name %s exists!", name.c_str());
    CHECK_TRUE(CheckBufferConfig(bufferConfig), nullptr, "buffer config invalid!");

    auto session = std::make_shared<PluginSession>(pluginConfig, bufferConfig, pluginService, dataRepeater);
    CHECK_NOTNULL(session, nullptr, "allocate plugin session for %s failed!", name.c_str());
    CHECK_TRUE(session->IsAvailable(), nullptr, "config plugin for %s failed!", name.c_str());
    return session;
}

bool ProfilerService::SessionContext::CreatePluginSessions(const PluginServicePtr& pluginService)
{
    for (size_t i = 0; i < pluginConfigs.size(); i++) {
        PluginSessionPtr session;
        if (bufferConfigs.size() > 0) {
            session = CreatePluginSession(pluginService, pluginConfigs[i], bufferConfigs[i]);
        } else {
            session = CreatePluginSession(pluginService, pluginConfigs[i]);
        }
        CHECK_NOTNULL(session, false, "create plugin-%zu session failed!", i);
        pluginSessions[pluginConfigs[i].name()] = session;
    }
    return true;
}

bool ProfilerService::SessionContext::UpdatePluginSessions(const PluginServicePtr& pluginService,
                                                           const std::vector<int>& configIndexes)
{
    for (auto index : configIndexes) {
        auto config = pluginConfigs[index];
        auto session = CreatePluginSession(pluginService, config);
        CHECK_NOTNULL(session, false, "create plugin session failed!");
        pluginSessions[config.name()] = session;
    }
    return true;
}

bool ProfilerService::SessionContext::RemovePluginSessions(const std::vector<std::string>& nameList)
{
    if (nameList.empty()) {
        return false;
    }

    for (auto& name : nameList) {
        auto it = pluginSessions.find(name);
        if (it != pluginSessions.end()) {
            pluginSessions.erase(it);
        }
    }
    return true;
}

std::vector<int> ProfilerService::SessionContext::UpdatePluginConfigs(
    const std::vector<ProfilerPluginConfig>& profilerPluginConfigList)
{
    std::map<std::string, size_t> targetIndex;
    for (size_t i = 0; i < pluginConfigs.size(); i++) {
        targetIndex[pluginConfigs[i].name()] = i;
    }

    std::vector<int> updates;
    for (auto& cfg : profilerPluginConfigList) {
        auto it = targetIndex.find(cfg.name());
        if (it != targetIndex.end()) {
            pluginConfigs[it->second] = cfg;
            updates.push_back(it->second);
        }
    }
    return updates;
}

Status ProfilerService::CreateSession(ServerContext* context,
                                      const ::CreateSessionRequest* request,
                                      ::CreateSessionResponse* response)
{
    HILOG_INFO(LOG_CORE, "CreateSession from '%s'", context->peer().c_str());
    CHECK_REQUEST_RESPONSE(request, response);
    CHECK_POINTER_NOTNULL(pluginService_, "plugin service not ready!");

    HILOG_INFO(LOG_CORE, "CreateSession %d start", request->request_id());
    const int nConfigs = request->plugin_configs_size();
    CHECK_EXPRESSION_TRUE(nConfigs > 0, "no plugin configs!");

    ProfilerSessionConfig sessionConfig = request->session_config();
    const int nBuffers = sessionConfig.buffers_size();
    CHECK_EXPRESSION_TRUE(nBuffers == 0 || nBuffers == 1 || nBuffers == nConfigs, "buffers config invalid!");

    std::vector<ProfilerSessionConfig::BufferConfig> bufferConfigs;
    if (nBuffers == 1) {
        bufferConfigs.resize(nConfigs, sessionConfig.buffers(0));
    } else if (nBuffers > 0) {
        bufferConfigs.assign(sessionConfig.buffers().begin(), sessionConfig.buffers().end());
    }
    HILOG_INFO(LOG_CORE, "bufferConfigs: %zu", bufferConfigs.size());

    // copy plugin configs from request
    std::vector<ProfilerPluginConfig> pluginConfigsList;
    pluginConfigsList.reserve(nConfigs);
    for (int i = 0; i < nConfigs; i++) {
        pluginConfigsList.push_back(request->plugin_configs(i));
    }

    // create ProfilerDataRepeater
    auto dataRepeater = std::make_shared<ProfilerDataRepeater>(DEFAULT_REPEATER_BUFFER_SIZE);
    CHECK_POINTER_NOTNULL(dataRepeater, "alloc ProfilerDataRepeater failed!");

    // create ResultDemuxer
    auto resultDemuxer = std::make_shared<ResultDemuxer>(dataRepeater);
    CHECK_POINTER_NOTNULL(resultDemuxer, "alloc ResultDemuxer failed!");

    // create TraceFileWriter for offline mode
    TraceFileWriterPtr traceWriter;
    if (sessionConfig.session_mode() == ProfilerSessionConfig::OFFLINE) {
        auto resultFile = sessionConfig.result_file();
        CHECK_EXPRESSION_TRUE(resultFile.size() > 0, "result_file empty!");
        traceWriter = std::make_shared<TraceFileWriter>(resultFile);
        CHECK_POINTER_NOTNULL(traceWriter, "alloc TraceFileWriter failed!");
        resultDemuxer->SetTraceWriter(traceWriter);
    }

    // start prepare session context
    auto ctx = std::make_shared<SessionContext>();
    CHECK_POINTER_NOTNULL(ctx, "alloc SessionContext failed!");

    // fill fields of SessionContext
    ctx->dataRepeater = dataRepeater;
    ctx->resultDemuxer = resultDemuxer;
    ctx->traceFileWriter = traceWriter;
    ctx->sessionConfig = sessionConfig;
    ctx->pluginConfigs = std::move(pluginConfigsList);
    ctx->bufferConfigs = std::move(bufferConfigs);
    CHECK_EXPRESSION_TRUE(ctx->CreatePluginSessions(pluginService_), "create sessions failed!");

    // alloc new session id
    uint32_t sessionId = ++sessionIdCounter_;
    ctx->id = sessionId;

    // add {sessionId, ctx} to map
    CHECK_EXPRESSION_TRUE(AddSessionContext(sessionId, ctx), "sessionId conflict!");

    // prepare response data fields
    response->set_status(0);
    response->set_session_id(sessionId);

    HILOG_INFO(LOG_CORE, "CreateSession %d done!", request->request_id());
    return Status::OK;
}

bool ProfilerService::AddSessionContext(uint32_t sessionId, const SessionContextPtr& sessionCtx)
{
    std::unique_lock<std::mutex> lock(sessionContextMutex_);
    if (sessionContext_.count(sessionId) > 0) {
        HILOG_WARN(LOG_CORE, "sessionId already exists!");
        return false;
    }
    sessionContext_[sessionId] = sessionCtx;
    return true;
}

ProfilerService::SessionContextPtr ProfilerService::GetSessionContext(uint32_t sessionId) const
{
    std::unique_lock<std::mutex> lock(sessionContextMutex_);
    auto it = sessionContext_.find(sessionId);
    if (it != sessionContext_.end()) {
        HILOG_INFO(LOG_CORE, "use_count: %ld", it->second.use_count());
        return it->second;
    }
    return nullptr;
}

bool ProfilerService::RemoveSessionContext(uint32_t sessionId)
{
    std::unique_lock<std::mutex> lock(sessionContextMutex_);
    auto it = sessionContext_.find(sessionId);
    if (it != sessionContext_.end()) {
        HILOG_INFO(LOG_CORE, "use_count: %ld", it->second.use_count());
        sessionContext_.erase(it);
        return true;
    }
    return false;
}

Status ProfilerService::StartSession(ServerContext* context,
                                     const ::StartSessionRequest* request,
                                     ::StartSessionResponse* response)
{
    HILOG_INFO(LOG_CORE, "StartSession from '%s'", context->peer().c_str());
    CHECK_REQUEST_RESPONSE(request, response);

    uint32_t sessionId = request->session_id();
    HILOG_INFO(LOG_CORE, "StartSession %d start", request->request_id());

    // copy plugin configs from request
    std::vector<ProfilerPluginConfig> pluginConfigsList;
    pluginConfigsList.reserve(request->update_configs_size());
    for (int i = 0; i < request->update_configs_size(); i++) {
        HILOG_INFO(LOG_CORE, "update_configs %d, name = %s", i, request->update_configs(i).name().c_str());
        pluginConfigsList.push_back(request->update_configs(i));
    }

    std::vector<std::string> nameList;
    for (auto& config : pluginConfigsList) {
        nameList.push_back(config.name());
    }

    auto ctx = GetSessionContext(sessionId);
    CHECK_POINTER_NOTNULL(ctx, "session_id invalid!");

    // remove old plugin sessions
    ctx->RemovePluginSessions(nameList);

    // update plugin configs
    auto updates = ctx->UpdatePluginConfigs(pluginConfigsList);

    // update plugin sessions
    CHECK_EXPRESSION_TRUE(ctx->UpdatePluginSessions(pluginService_, updates), "update sessions failed!");

    // if dataRepeater exists, reset it to usable state.
    if (ctx->dataRepeater) {
        ctx->dataRepeater->Reset();
    }

    // start demuxer take result thread
    if (ctx->resultDemuxer) {
        if (ctx->sessionConfig.session_mode() == ProfilerSessionConfig::OFFLINE) {
            ctx->resultDemuxer->StartTakeResults();
        }
    }

    // start each plugin sessions
    for (auto sessionEntry : ctx->pluginSessions) {
        if (sessionEntry.second) {
            sessionEntry.second->Start();
        }
    }

    HILOG_INFO(LOG_CORE, "StartSession %d done!", request->request_id());
    return Status::OK;
}

Status ProfilerService::FetchData(ServerContext* context,
                                  const ::FetchDataRequest* request,
                                  ServerWriter<::FetchDataResponse>* writer)
{
    HILOG_INFO(LOG_CORE, "FetchData from '%s'", context->peer().c_str());
    CHECK_POINTER_NOTNULL(request, "request invalid!");
    CHECK_POINTER_NOTNULL(writer, "writer invalid!");

    uint32_t sessionId = request->session_id();
    HILOG_INFO(LOG_CORE, "FetchData %d start", request->request_id());

    auto ctx = GetSessionContext(sessionId);
    CHECK_POINTER_NOTNULL(ctx, "session_id invalid!");

    // check each plugin session states
    for (auto sessionEntry : ctx->pluginSessions) {
        if (sessionEntry.second) {
            CHECK_EXPRESSION_TRUE(sessionEntry.second->GetState() == PluginSession::STARTED,
                                  "plugin session state invalid");
            HILOG_INFO(LOG_CORE, "check plugin session %s OK!", sessionEntry.first.c_str());
        }
    }

    if (ctx->sessionConfig.session_mode() == ProfilerSessionConfig::ONLINE) {
        auto dataRepeater = ctx->dataRepeater;
        CHECK_POINTER_NOTNULL(dataRepeater, "repeater invalid!");

        bool sendSuccess = false;
        while (1) {
            FetchDataResponse response;
            response.set_status(StatusCode::OK);
            response.set_response_id(++responseIdCounter_);

            std::vector<ProfilerPluginDataPtr> pluginDataVec;
            int count = dataRepeater->TakePluginData(pluginDataVec);
            if (count > 0) {
                response.set_has_more(true);
                for (int i = 0; i < count; i++) {
                    auto data = response.add_plugin_data();
                    CHECK_POINTER_NOTNULL(data, "new plugin data invalid");
                    CHECK_POINTER_NOTNULL(pluginDataVec[i], "plugin data invalid");
                    *data = *pluginDataVec[i];
                    HILOG_INFO(LOG_CORE, "add plugin %s data to response", pluginDataVec[i]->name().c_str());
                }
                HILOG_INFO(LOG_CORE, "fill %d data to response-%d", count, response.response_id());
            } else {
                response.set_has_more(false);
                HILOG_INFO(LOG_CORE, "no more data need to fill to response!");
            }

            sendSuccess = writer->Write(response);
            if (count <= 0 || !sendSuccess) {
                HILOG_INFO(LOG_CORE, "count = %d, sendSuccess = %d", count, sendSuccess);
                break;
            }
        }
    }

    HILOG_INFO(LOG_CORE, "FetchData %d done!", request->request_id());
    return Status::OK;
}

Status ProfilerService::StopSession(ServerContext* context,
                                    const ::StopSessionRequest* request,
                                    ::StopSessionResponse* response)
{
    HILOG_INFO(LOG_CORE, "StopSession from '%s'", context->peer().c_str());
    CHECK_REQUEST_RESPONSE(request, response);

    uint32_t sessionId = request->session_id();
    HILOG_INFO(LOG_CORE, "StopSession %d start", request->request_id());

    auto ctx = GetSessionContext(sessionId);
    CHECK_POINTER_NOTNULL(ctx, "session_id invalid!");

    // stop each plugin sessions
    for (auto sessionEntry : ctx->pluginSessions) {
        if (sessionEntry.second) {
            sessionEntry.second->Stop();
        }
    }

    // stop demuxer take result thread
    if (ctx->resultDemuxer) {
        if (ctx->sessionConfig.session_mode() == ProfilerSessionConfig::OFFLINE) {
            ctx->resultDemuxer->StopTakeResults();
        }
    }

    // make sure FetchData thread exit
    if (ctx->dataRepeater) {
        ctx->dataRepeater->Close();
    }

    HILOG_INFO(LOG_CORE, "StopSession %d done!", request->request_id());
    return Status::OK;
}

Status ProfilerService::DestroySession(ServerContext* context,
                                       const ::DestroySessionRequest* request,
                                       ::DestroySessionResponse* response)
{
    HILOG_INFO(LOG_CORE, "DestroySession from '%s'", context->peer().c_str());
    CHECK_REQUEST_RESPONSE(request, response);

    uint32_t sessionId = request->session_id();
    HILOG_INFO(LOG_CORE, "DestroySession %d start", request->request_id());

    CHECK_EXPRESSION_TRUE(RemoveSessionContext(sessionId), "session_id invalid!");
    HILOG_INFO(LOG_CORE, "DestroySession %d done!", request->request_id());
    return Status::OK;
}

bool ProfilerService::StartService(const std::string& listenUri)
{
    if (listenUri == "") {
        return false;
    }

    ServerBuilder builder;
    builder.AddListeningPort(listenUri, grpc::InsecureServerCredentials());
    builder.RegisterService(this);

    server_ = builder.BuildAndStart();
    CHECK_NOTNULL(server_, false, "start service on %s failed!", listenUri.c_str());
    HILOG_INFO(LOG_CORE, "Server listening on %s", listenUri.c_str());

    return true;
}

void ProfilerService::WaitServiceDone()
{
    if (server_) {
        server_->Wait();
    }
}

void ProfilerService::StopService()
{
    if (server_) {
        server_->Shutdown();
    }
}

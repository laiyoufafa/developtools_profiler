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

#include <arpa/inet.h>
#include <cinttypes>
#include <cstdio>
#include <cstring>
#include <fstream>
#include <getopt.h>
#include <grpcpp/grpcpp.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <ostream>
#include <sys/types.h>
#include <thread>
#include <unistd.h>
#include <vector>

#include "command_line.h"
#include "google/protobuf/text_format.h"
#include "parse_plugin_config.h"
#include "profiler_service.grpc.pb.h"
#include "trace_plugin_config.pb.h"

using google::protobuf::TextFormat;

namespace {
constexpr int ADDR_BUFFER_SIZE = 128;
constexpr int MS_PER_S = 1000;
constexpr uint16_t SERVICE_PORT = 50051;
constexpr uint32_t US_PER_MS = 1000;
constexpr int KEEP_SESSION_TIMEOUT_MS = 5 * 1000;
constexpr int KEEP_SESSION_SLEEP_US = 3 * 1000 * 1000;

uint32_t g_sampleDuration = 0;
bool g_isConfigFile = false;

std::string GetLoopbackAddress()
{
    char addressBuffer[ADDR_BUFFER_SIZE] = "";
    struct ifaddrs* ifAddrStruct = nullptr;
    void* tmpAddrPtr = nullptr;

    getifaddrs(&ifAddrStruct);
    while (ifAddrStruct != nullptr) {
        if (ifAddrStruct->ifa_addr->sa_family == AF_INET) {
            // is a valid IP4 Address
            tmpAddrPtr = &((reinterpret_cast<struct sockaddr_in*>(ifAddrStruct->ifa_addr))->sin_addr);
            inet_ntop(AF_INET, tmpAddrPtr, addressBuffer, INET_ADDRSTRLEN);
            if (strcmp(addressBuffer, "127.0.0.1") == 0) {
                break;
            }
        } else if (ifAddrStruct->ifa_addr->sa_family == AF_INET6) { // check it is IP6
            // is a valid IP6 Address
            tmpAddrPtr = &((reinterpret_cast<struct sockaddr_in*>(ifAddrStruct->ifa_addr))->sin_addr);
            inet_ntop(AF_INET6, tmpAddrPtr, addressBuffer, INET6_ADDRSTRLEN);
        }
        ifAddrStruct = ifAddrStruct->ifa_next;
    }

    freeifaddrs(ifAddrStruct);
    return addressBuffer;
}

uint16_t GetServicePort()
{
    return SERVICE_PORT;
}

std::string ReadFileToString(const std::string& fileName)
{
    std::ifstream inputString(fileName, std::ios::in);
    if (!inputString) {
        printf("can't open %s\n", fileName.c_str());
        return "";
    }
    std::string content(std::istreambuf_iterator<char>(inputString), {});
    return content;
}

std::string ReadConfigContent(const std::string& configFile)
{
    std::string content;
    if (configFile == "-") { // Read configuration information from standard input
        std::string line;
        while (std::getline(std::cin, line)) {
            content += line + "\n";
        }
    } else {
        content = ReadFileToString(configFile);
    }
    return content;
}

std::unique_ptr<CreateSessionRequest> MakeCreateRequest(const std::string& config,
                                                        const std::string& keepSecond,
                                                        const std::string& outputFile)
{
    auto request = std::make_unique<CreateSessionRequest>();
    if (!request) {
        return nullptr;
    }

    std::string content;
    if (g_isConfigFile) {
        content = ReadConfigContent(config);
    } else {
        content = std::string(config.c_str());
    }
    if (content.empty()) {
        printf("config file empty!");
        return nullptr;
    }
    if (!TextFormat::ParseFromString(content, request.get())) {
        printf("config [%s] parse FAILED!\n", content.c_str());
        return nullptr;
    }

    auto sessionConfig = request->mutable_session_config();
    if (!sessionConfig) {
        return nullptr;
    }

    request->set_request_id(1);
    printf("keepSecond: %s, outputFileName: %s\n", keepSecond.c_str(), outputFile.c_str());
    if (!keepSecond.empty()) {
        int ks = std::stoi(keepSecond);
        if (ks > 0) {
            sessionConfig->set_sample_duration(ks * MS_PER_S);
        }
    }
    if (!outputFile.empty()) {
        sessionConfig->set_result_file(outputFile);
    }

    g_sampleDuration = sessionConfig->sample_duration();
    if (!g_isConfigFile) {
        for (int i = 0; i < request->plugin_configs().size(); i++) {
            auto pluginConfig = request->mutable_plugin_configs(i);
            if (!ParsePluginConfig::GetInstance().SetSerializePluginsConfig(pluginConfig->name(), *pluginConfig)) {
                printf("set %s plugin config failed\n", pluginConfig->name().c_str());
                return nullptr;
            }
        }
    }

    content.clear();
    if (!TextFormat::PrintToString(*request.get(), &content)) {
        printf("config message format FAILED!\n");
        return nullptr;
    }

    return request;
}

std::unique_ptr<IProfilerService::Stub> GetProfilerServiceStub()
{
    std::string serviceUri = GetLoopbackAddress() + ":" + std::to_string(GetServicePort());
    auto grpcChannel = grpc::CreateChannel(serviceUri, grpc::InsecureChannelCredentials());
    if (grpcChannel == nullptr) {
        printf("FAIL\nCreate gRPC channel failed!\n");
        return nullptr;
    }
    return IProfilerService::NewStub(grpcChannel);
}

bool GetCapabilities()
{
    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return false;
    }

    GetCapabilitiesRequest capRequest;
    GetCapabilitiesResponse capResponse;
    capRequest.set_request_id(0);
    grpc::ClientContext capContext;
    grpc::Status status = profilerStub->GetCapabilities(&capContext, capRequest, &capResponse);
    if (!status.ok()) {
        printf("FAIL\nService not started\n");
        return false;
    }

    std::string content;
    if (!TextFormat::PrintToString(capResponse, &content)) {
        printf("capabilities message format FAILED!\n");
        return false;
    }
    printf("support plugin list:\n%s\n", content.c_str());
    return true;
}

uint32_t CreateSession(const std::string& configFile, const std::string& keepSecond, const std::string& outputFile)
{
    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return 0;
    }

    auto request = MakeCreateRequest(configFile, keepSecond, outputFile);
    if (!request) {
        printf("FAIL\nMakeCreateRequest failed!\n");
        return 0;
    }

    CreateSessionResponse createResponse;
    grpc::ClientContext createSessionContext;
    grpc::Status status = profilerStub->CreateSession(&createSessionContext, *request, &createResponse);
    if (!status.ok()) {
        printf("FAIL\nCreateSession FAIL\n");
        return 0;
    }

    return createResponse.session_id();
}

bool CheckStartSession(std::unique_ptr<IProfilerService::Stub>& profilerStub, uint32_t& sessionId)
{
    StartSessionRequest startRequest;
    StartSessionResponse startResponse;
    startRequest.set_request_id(0);
    startRequest.set_session_id(sessionId);
    grpc::ClientContext startContext;
    grpc::Status status = profilerStub->StartSession(&startContext, startRequest, &startResponse);
    if (!status.ok()) {
        printf("FAIL\nStartSession FAIL\n");
        return false;
    }

    return true;
}

bool CheckStopSession(std::unique_ptr<IProfilerService::Stub>& profilerStub, uint32_t& sessionId)
{
    StopSessionRequest stopRequest;
    StopSessionResponse stopResponse;
    grpc::ClientContext stopContext;
    stopRequest.set_session_id(sessionId);
    grpc::Status status = profilerStub->StopSession(&stopContext, stopRequest, &stopResponse);
    if (!status.ok()) {
        return false;
    }

    return true;
}

bool CheckDestroySession(std::unique_ptr<IProfilerService::Stub>& profilerStub, uint32_t& sessionId)
{
    DestroySessionRequest destroyRequest;
    DestroySessionResponse destroyResponse;
    grpc::ClientContext destroyContext;
    destroyRequest.set_session_id(sessionId);
    grpc::Status status = profilerStub->DestroySession(&destroyContext, destroyRequest, &destroyResponse);
    if (!status.ok()) {
        return false;
    }

    return true;
}

bool DoCapture(const std::string& configFile, const std::string& keepSecond, const std::string& outputFile)
{
    uint32_t sessionId = CreateSession(configFile, keepSecond, outputFile);
    if (sessionId == 0) {
        return false;
    }

    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return false;
    }

    // 开启心跳线程确保会话正常，睡眠3s下发一次5s超时心跳
    bool sendHeart = true;
    std::thread keepSessionThread([&]() {
        while (sendHeart) {
            KeepSessionRequest keepRequest;
            keepRequest.set_request_id(0);
            keepRequest.set_session_id(sessionId);
            keepRequest.set_keep_alive_time(KEEP_SESSION_TIMEOUT_MS);
            grpc::ClientContext keepContext;
            KeepSessionResponse keepResponse;
            profilerStub->KeepSession(&keepContext, keepRequest, &keepResponse);
            usleep(KEEP_SESSION_SLEEP_US);
        }
    });

    if (!CheckStartSession(profilerStub, sessionId)) {
        return false;
    }

    printf("tracing %u ms....\n", g_sampleDuration);
    usleep(g_sampleDuration * US_PER_MS);

    if (!CheckStopSession(profilerStub, sessionId)) {
        sendHeart = false;
        keepSessionThread.join();
        return false;
    }
    printf("StopSession done!\n");

    if (!CheckDestroySession(profilerStub, sessionId)) {
        sendHeart = false;
        keepSessionThread.join();
        return false;
    }
    printf("DestroySession done!\n");

    sendHeart = false;
    keepSessionThread.join();
    return true;
}

struct DataContext {
    bool isGetGrpcAddr = false;
    std::string traceKeepSecond;
    std::string outputFile;
    bool isHelp = false;
    bool isShowPluginList = false;
};

void ParseCmdline(CommandLine* pCmdLine, std::string& config, DataContext& data)
{
    pCmdLine->AddParamSwitch("--getport", "-q", data.isGetGrpcAddr, "get grpc address");
    pCmdLine->AddParamText("--time", "-t", data.traceKeepSecond, "trace time");
    pCmdLine->AddParamText("--out", "-o", data.outputFile, "output file name");
    pCmdLine->AddParamSwitch("--help", "-h", data.isHelp, "make some help");
    pCmdLine->AddParamSwitch("--list", "-l", data.isShowPluginList, "plugin list");
    if (config.empty()) {
        g_isConfigFile = true;
        pCmdLine->AddParamText("--config", "-c", config, "start trace by config file");
    }
}

int CheckGrpcMsgSend()
{
    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return -1;
    }

    GetCapabilitiesRequest request;
    GetCapabilitiesResponse response;
    request.set_request_id(0);

    grpc::ClientContext context;
    grpc::Status status = profilerStub->GetCapabilities(&context, request, &response);
    if (!status.ok()) {
        printf("FAIL\nService not started\n");
        return -1;
    }
    printf("OK\n");
    return 0;
}
} // namespace

int main(int argc, char* argv[])
{
    std::string config = "";
    while (true) {
        int option = getopt(argc, argv, "q:c:t:o:h:l:");
        if (option == -1) {
            break;  // CONFIG.
        }

        if (option == 'c' && strcmp(optarg, "-") == 0) {
            std::string content;
            std::istreambuf_iterator<char> begin(std::cin), end;
            content.assign(begin, end);
            config = ParsePluginConfig::GetInstance().GetPluginsConfig(content);
            if (config == "") {
                printf("FAIL\nPlease check the configuration!\n");
                return -1;
            }
        }
    }

    DataContext data;
    CommandLine* pCmdLine = &CommandLine::GetInstance();
    ParseCmdline(pCmdLine, config, data);

    std::vector<std::string> argvVector;
    for (int i = 0; i < argc; i++) {
        if ((i + 1) < argc && strcmp(argv[i], "-c") == 0 && strcmp(argv[i+1], "-") == 0) {
            i++;
        } else {
            argvVector.push_back(argv[i]);
        }
    }
    if (argc < 1 || pCmdLine->AnalyzeParam(argvVector) < 0 || data.isHelp) {
        pCmdLine->PrintHelp();
        exit(0);
    }
    if (data.isGetGrpcAddr) { // handle get port
        return CheckGrpcMsgSend();
    }

    if (data.isShowPluginList) { // handle show plugin list
        GetCapabilities();
        return 0;
    }

    if (config.empty()) { // normal case
        printf("FAIL\nconfig file argument must sepcified!");
        return 1;
    }
    // do capture work
    if (DoCapture(config, data.traceKeepSecond, data.outputFile)) {
        printf("DONE\n");
    }

    return 0;
}

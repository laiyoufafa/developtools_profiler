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

#include "command_line.h"
#include "google/protobuf/text_format.h"
#include "profiler_service.grpc.pb.h"

#include <grpcpp/grpcpp.h>

#include <cinttypes>
#include <cstdio>
#include <cstring>
#include <fstream>
#include <ostream>
#include <vector>

#include <arpa/inet.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <unistd.h>

namespace {
constexpr int ADDR_BUFFER_SIZE = 128;
constexpr int MS_PER_S = 1000;
constexpr uint16_t SERVICE_PORT = 50051;
constexpr uint32_t US_PER_MS = 1000;

uint32_t g_sampleDuration = 0;

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

std::unique_ptr<CreateSessionRequest> MakeCreateRequest(const std::string& configFile,
                                                        const std::string& keepSecond,
                                                        const std::string& outputFile)
{
    auto request = std::make_unique<CreateSessionRequest>();
    if (!request) {
        return nullptr;
    }

    std::string content = ReadConfigContent(configFile);
    if (content.empty()) {
        printf("config file empty!");
        return nullptr;
    }
    printf("================================\n");
    printf("CONFIG: read %zu bytes from %s:\n%s", content.size(), configFile.c_str(), content.c_str());
    if (!google::protobuf::TextFormat::ParseFromString(content, request.get())) {
        printf("config file [%s] parse FAILED!\n", configFile.c_str());
        return nullptr;
    }

    auto sessionConfig = request->mutable_session_config();
    if (!sessionConfig) {
        return nullptr;
    }

    request->set_request_id(1);
    printf("--------------------------------\n");
    printf("keepSecond: %s,\noutputFileName: %s\n", keepSecond.c_str(), outputFile.c_str());
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
    content.clear();
    if (!google::protobuf::TextFormat::PrintToString(*request.get(), &content)) {
        printf("config message format FAILED!\n");
        return nullptr;
    }
    printf("--------------------------------\n");
    printf("CONFIG: final config content:\n%s", content.c_str());
    printf("================================\n");
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

bool DoCapture(const std::string& configFile, const std::string& keepSecond, const std::string& outputFile)
{
    uint32_t sessionId = CreateSession(configFile, keepSecond, outputFile);
    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return false;
    }

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

    printf("tracing %u ms....\n", g_sampleDuration);
    usleep(g_sampleDuration * US_PER_MS);

    StopSessionRequest stopRequest;
    StopSessionResponse stopResponse;
    grpc::ClientContext stopContext;
    stopRequest.set_session_id(sessionId);
    status = profilerStub->StopSession(&stopContext, stopRequest, &stopResponse);
    if (!status.ok()) {
        printf("FAIL\nStopSession FAIL\n");
        return false;
    }
    printf("StopSession done!\n");

    DestroySessionRequest destroyRequest;
    DestroySessionResponse destroyResponse;
    grpc::ClientContext destroyContext;
    destroyRequest.set_session_id(sessionId);
    status = profilerStub->DestroySession(&destroyContext, destroyRequest, &destroyResponse);
    if (!status.ok()) {
        printf("FAIL\nDestroySession FAIL\n");
        return false;
    }
    printf("DestroySession done!\n");
    return true;
}
} // namespace

int main(int argc, char* argv[])
{
    CommandLine* pCmdLine = &CommandLine::GetInstance();

    bool isGetGrpcAddr = false;
    pCmdLine->AddParamSwitch("--getport", "-q", isGetGrpcAddr, "get grpc address");

    std::string configFile;
    pCmdLine->AddParamText("--config", "-c", configFile, "start trace by config file");

    std::string traceKeepSecond;
    pCmdLine->AddParamText("--time", "-t", traceKeepSecond, "trace time");

    std::string outputFile;
    pCmdLine->AddParamText("--out", "-o", outputFile, "output file name");

    bool isHelp = false;
    pCmdLine->AddParamSwitch("--help", "-h", isHelp, "make some help");

    std::vector<std::string> argvVector;
    for (int i = 0; i < argc; i++) {
        argvVector.push_back(argv[i]);
    }
    if (argc < 1 || pCmdLine->AnalyzeParam(argvVector) < 0 || isHelp) {
        pCmdLine->PrintHelp();
        exit(0);
    }
    if (isGetGrpcAddr) { // handle get port
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
        printf("ip:%s\n", GetLoopbackAddress().c_str());
        printf("port:%u\n", GetServicePort());
        return 0;
    }

    if (configFile.empty()) { // normal case
        printf("FAIL\nconfig file argument must sepcified!");
        return 1;
    }
    // do capture work
    if (DoCapture(configFile, traceKeepSecond, outputFile)) {
        printf("DONE\n");
    }

    return 0;
}

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
#include "profiler_service.grpc.pb.h"
#include "google/protobuf/text_format.h"

#include <grpcpp/grpcpp.h>

#include <cstdio>
#include <cstring>
#include <fstream>
#include <ostream>
#include <vector>

#include <arpa/inet.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <sys/types.h>

namespace {
const int ADDR_BUFFER_SIZE = 128;
const int MS_PER_S = 1000;

std::string GetIpAddr()
{
    char addressBuffer[ADDR_BUFFER_SIZE] = {0};
    struct ifaddrs* ifAddrStruct = nullptr;
    void* tmpAddrPtr = nullptr;

    getifaddrs(&ifAddrStruct);

    while (ifAddrStruct != nullptr) {
        if (ifAddrStruct->ifa_addr->sa_family == AF_INET) {
            // is a valid IP4 Address
            tmpAddrPtr = &((reinterpret_cast<struct sockaddr_in*>(ifAddrStruct->ifa_addr))->sin_addr);
            inet_ntop(AF_INET, tmpAddrPtr, addressBuffer, INET_ADDRSTRLEN);
            if (strcmp(addressBuffer, "127.0.0.1") != 0) {
                return addressBuffer;
            }
        } else if (ifAddrStruct->ifa_addr->sa_family == AF_INET6) { // check it is IP6
            // is a valid IP6 Address
            tmpAddrPtr = &((reinterpret_cast<struct sockaddr_in*>(ifAddrStruct->ifa_addr))->sin_addr);
            inet_ntop(AF_INET6, tmpAddrPtr, addressBuffer, INET6_ADDRSTRLEN);
        }
        ifAddrStruct = ifAddrStruct->ifa_next;
    }
    return addressBuffer;
}

std::string ReadFileToString(const std::string& fileName)
{
    std::ifstream inputString(fileName, std::ios::in);
    if (!inputString) {
        printf("can't open %s\n", fileName.c_str());
        return "";
    }
    std::string content(std::istreambuf_iterator<char> {inputString}, std::istreambuf_iterator<char> {});
    return content;
}

std::string ReadConfigContent(const std::string& configFileName)
{
    std::string content;
    if (configFileName == "-") { // Read configuration information from standard input
        std::string line;
        while (std::getline(std::cin, line)) {
            content += line + "\n";
        }
    } else {
        content = ReadFileToString(configFileName);
    }
    return content;
}

std::unique_ptr<CreateSessionRequest> MakeCreateRequest(const std::string& configFileName,
    const std::string& keepSecond, const std::string& outputFileName)
{
    auto request = std::make_unique<CreateSessionRequest>();
    if (!request) {
        return nullptr;
    }

    std::string content = ReadConfigContent(configFileName);
    if (content.empty()) {
        printf("config file empty!");
        return nullptr;
    }
    printf("================================\n");
    printf("CONFIG: read %zu bytes from %s:\n%s", content.size(), configFileName.c_str(), content.c_str());
    if (!google::protobuf::TextFormat::ParseFromString(content, request.get())) {
        printf("config file [%s] parse FAILED!\n", configFileName.c_str());
        return nullptr;
    }

    auto sessionConfig = request->mutable_session_config();
    if (!sessionConfig) {
        return nullptr;
    }

    request->set_request_id(1);
    printf("--------------------------------\n");
    printf("keepSecond: %s,\noutputFileName: %s\n", keepSecond.c_str(), outputFileName.c_str());
    if (!keepSecond.empty()) {
        int ks = std::stoi(keepSecond);
        if (ks > 0) {
            sessionConfig->set_sample_duration(ks * MS_PER_S);
        }
    }
    if (!outputFileName.empty()) {
        sessionConfig->set_result_file(outputFileName);
    }

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
    auto grpcChannel = grpc::CreateChannel("localhost:50051", grpc::InsecureChannelCredentials());
    if (grpcChannel == nullptr) {
        printf("FAIL\nCreate gRPC channel failed!\n");
        return nullptr;
    }
    return IProfilerService::NewStub(grpcChannel);
}

uint32_t CreateSession(const std::string& configFileName,
    const std::string& keepSecond, const std::string& outputFileName)
{
    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return 0;
    }

    auto request = MakeCreateRequest(configFileName, keepSecond, outputFileName);
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

bool StartSession(const std::string& configFileName,
    const std::string& keepSecond, const std::string& outputFileName)
{
    uint32_t sessionId = CreateSession(configFileName, keepSecond, outputFileName);
    auto profilerStub = GetProfilerServiceStub();
    if (profilerStub == nullptr) {
        printf("FAIL\nGet profiler service stub failed!\n");
        return false;
    }

    StartSessionRequest startRequest;
    StartSessionResponse startResponse;
    startRequest.set_request_id(0);
    startRequest.set_session_id(sessionId);
    grpc::ClientContext startSessionContext;
    grpc::Status status = profilerStub->StartSession(&startSessionContext, startRequest, &startResponse);
    if (!status.ok()) {
        printf("FAIL\nStartSession FAIL\n");
        return false;
    }

    return true;
}
} // namespace

int main(int argc, char* argv[])
{
    CommandLine* pCmdLine = &CommandLine::GetInstance();

    bool isGetGrpcAddr = false;
    pCmdLine->AddParamSwitch("--getport", "-q", isGetGrpcAddr, "get grpc address");

    std::string configFileName;
    pCmdLine->AddParamText("--config", "-c", configFileName, "start trace by config file");

    std::string traceKeepSecond;
    pCmdLine->AddParamText("--time", "-t", traceKeepSecond, "trace time");

    std::string outputFileName;
    pCmdLine->AddParamText("--out", "-o", outputFileName, "output file name");

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

        printf("OK\nip:%s\nport:50051\n", GetIpAddr().c_str());
        return 0;
    }

    if (configFileName.empty()) { // normal case
        printf("FAIL\nconfig file argument must sepcified!");
        return 1;
    }
    // Read the configFileName, call 'CreateSession', and 'StartSession'
    if (StartSession(configFileName, traceKeepSecond, outputFileName)) {
        printf("OK\ntracing...\n");
    }

    return 0;
}

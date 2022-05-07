/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
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
#ifndef SOCKET_PROFILER_H
#define SOCKET_PROFILER_H
#include <sstream>
#include <netinet/in.h>
#include "CPU.h"
#include "GPU.h"
#include "DDR.h"
#include "FPS.h"
#include "RAM.h"
#include "Temperature.h"
#include "Power.h"
#include "ByTrace.h"
namespace OHOS {
namespace SmartPerf {
enum SockConstant {
    SOCK_PORT = 8283,
    BUFF_SIZE_RECV = 256,
    BUFF_SIZE_SEND = 2048
};
class SocketProfiler : public DelayedSingleton<SocketProfiler> {
public:
    void initSocketProfiler();
    int bufsendto(int sockLocal, const char *bufsend, int length, struct sockaddr *clientLocal, socklen_t len);
    void callSend(std::stringstream &sstream, std::string &str1, std::string &str2);
    void thread_udp_server();
    void initSocket();

    std::shared_ptr<CPU> mCpu = nullptr;
    std::shared_ptr<GPU> mGpu = nullptr;
    std::shared_ptr<DDR> mDdr = nullptr;
    std::shared_ptr<FPS> mFps = nullptr;
    std::shared_ptr<RAM> mRam = nullptr;
    std::shared_ptr<Temperature> mTemperature = nullptr;
    std::shared_ptr<Power> mPower = nullptr;
    std::shared_ptr<ByTrace> mByTrace = nullptr;
    
    int sock;
    struct sockaddr_in local;
    struct sockaddr_in client;
};
}
}
#endif
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
class SocketProfiler {
public:
    SocketProfiler();
    ~SocketProfiler();

    void initSocketProfiler();

    int bufsendto(int sock, const char *bufsend, int length, struct sockaddr *client, socklen_t len);

    FpsInfo gfpsInfo;
    static void *thread_get_fps(void *arg);
    static void *thread_udp_server(void *spThis);

    std::shared_ptr<CPU> mCpu = nullptr;
    std::shared_ptr<GPU> mGpu = nullptr;
    std::shared_ptr<DDR> mDdr = nullptr;
    std::shared_ptr<FPS> mFps = nullptr;
    std::shared_ptr<RAM> mRam = nullptr;
    std::shared_ptr<Temperature> mTemperature = nullptr;
    std::shared_ptr<Power> mPower = nullptr;
    std::shared_ptr<ByTrace> mByTrace = nullptr;
};
struct SmartPerfCommandParam {
    SocketProfiler *spThis;
};
// 采集fps应用 相机和视频应用特殊适配
struct parameter {
    int is_video;
    int is_camera;
    SocketProfiler *spThis;
};
}
}
#endif
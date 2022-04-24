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
#include <cstdio>
#include <sys/socket.h>
#include "pthread.h"
#include "securec.h"
#include "include/gp_utils.h"
#include "include/gp_constant.h"
#include "include/socket_profiler.h"

namespace OHOS {
namespace SmartPerf {
SocketProfiler::SocketProfiler()
{
    mCpu = CPU::GetInstance();
    mGpu = GPU::GetInstance();
    mDdr = DDR::GetInstance();
    mFps = FPS::GetInstance();
    mRam = RAM::GetInstance();
    mTemperature = Temperature::GetInstance();
    mPower = Power::GetInstance();
    mByTrace = ByTrace::GetInstance();

    mTemperature->init_temperature();
    mGpu->init_gpu_node();
    mPower->init_power();
}

void *SocketProfiler::thread_get_fps(void *arg)
{
    struct parameter *p = (parameter *)arg;
    int videoOn = p->is_video;
    int cameraOn = p->is_camera;
    p->spThis->gfpsInfo = p->spThis->mFps->getFpsInfo(videoOn, cameraOn);
    pthread_exit(nullptr);
    return nullptr;
}

int SocketProfiler::bufsendto(int sock, const char *bufsend, int length, struct sockaddr *client, socklen_t len)
{
    ssize_t echo_size = sendto(sock, bufsend, length, ZERO, client, len);
    if (echo_size < ZERO) {
        printf("sendto error, buf is %s\n", bufsend);
        return ERROR_MINUX;
    }
    return SUCCESS_ZERO;
}


void *SocketProfiler::thread_udp_server(void *spThis)
{
    SmartPerfCommandParam *SCP = (SmartPerfCommandParam *)spThis;
    SocketProfiler *SP = SCP->spThis;
    int sock = socket(AF_INET, SOCK_DGRAM, ZERO);
    if (sock < ZERO) {
        perror("socket error");
        exit(1);
    }
    struct sockaddr_in local;
    local.sin_family = AF_INET;
    local.sin_port = htons(SOCK_PORT);
    local.sin_addr.s_addr = htonl(INADDR_ANY);
    if (::bind(sock, reinterpret_cast<struct sockaddr*>(&local), sizeof(local)) < ZERO) {
        perror("bind error");
        exit(1);
    }
    struct sockaddr_in client;
    socklen_t len = sizeof(client);
    char bufsend[BUFF_SIZE_SEND];
    const int loopforever = 1;
    printf("enter while loop forever\n");
    while (loopforever) {
        char recvbuf[BUFF_SIZE_RECV];
        memset(recvbuf, '\0', sizeof(recvbuf));
        ssize_t _size = recvfrom(sock, recvbuf, sizeof(recvbuf) - 1, 0, 
        reinterpret_cast<struct sockaddr*>(&client), &len);
        if (_size > 0) {
            printf("server recvbuf:%s\n", recvbuf);
        }
        std::string recv = std::string(recvbuf);
        if (recv.find("get_cpu_num") != std::string::npos) {
            int length = GPUtils::safeSprintf(bufsend, "get_cpu_num::%d", SP->mCpu->get_cpu_num());
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_cpu_freq") != std::string::npos) {
            std::vector<std::string> sps;
            GPUtils::mSplit(recv, "_", sps);
            int cpu_id = std::stoi(sps[sps.size() - 1]);
            int ret = SP->mCpu->get_cpu_freq(cpu_id);
            int length = GPUtils::safeSprintf(bufsend, "%s::%d", recv.c_str(), ret);
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_cpu_load") != std::string::npos) {
            std::vector<float> workloads = SP->mCpu->get_cpu_load();
            std::string res = "";
            for (int i = 0; i < workloads.size(); ++i) {
                if (i != 0)
                    res += "==";
                res += std::to_string(workloads[i]);
            }
            int length = GPUtils::safeSprintf(bufsend, "%s::%s", recv.c_str(), res.c_str());
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("set_pkgName") != std::string::npos) {
            std::vector<std::string> sps;
            GPUtils::mSplit(recv, "::", sps);
            if (sps.size() > 1) {
                SP->mFps->setPackageName(sps[1]);
                SP->mRam->setPkgName(sps[1]);
            }
        } else if (recv.find("get_fps_and_jitters") != std::string::npos) {
            std::vector<std::string> sps;
            GPUtils::mSplit(recv, "::", sps);
            if (sps.size() > TWO) {
                int is_video = atoi(sps[1].c_str());
                int is_camera = atoi(sps[2].c_str());
                struct parameter *par = new parameter;
                par->is_video = is_video;
                par->is_camera = is_camera;
                par->spThis = SP;
                pthread_t t_fps;
                pthread_create(&t_fps, nullptr, thread_get_fps, static_cast<void*>(par));
                std::string res = "";
                res += "timestamp|";
                res += std::to_string(SP->gfpsInfo.current_fps_time);
                res += ";";
                res += "fps|";
                res += std::to_string(SP->gfpsInfo.fps);
                res += ";";
                res += "jitter|";
                for (int i = 0; i < SP->gfpsInfo.jitters.size(); ++i) {
                    res += std::to_string(SP->gfpsInfo.jitters[i]);
                    res += "==";
                }
                int length = GPUtils::safeSprintf(bufsend, "%s::%s", "get_fps_and_jitters", res.c_str());
                SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
            }
        } else if (recv.find("get_gpu_freq") != std::string::npos) {
            int ret = SP->mGpu->get_gpu_freq();
            int length = GPUtils::safeSprintf(bufsend, "%s::%d", recv.c_str(), ret);
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_gpu_load") != std::string::npos) {
            float workload = SP->mGpu->get_gpu_load();
            int length = GPUtils::safeSprintf(bufsend, "%s::%f", recv.c_str(), workload);
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_ddr_freq") != std::string::npos) {
            long long ret = SP->mDdr->get_ddr_freq();
            int length = GPUtils::safeSprintf(bufsend, "%s::%lld", recv.c_str(), ret);
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_ram_info") != std::string::npos) {
            std::vector<std::string> sps;
            GPUtils::mSplit(recv, "::", sps);
            if (sps.size() > 1) {
                std::map<std::string, std::string> gramInfo = SP->mRam->getRamInfo(sps[1], 0);
                std::string res = "Pss";
                std::map<std::string, std::string>::iterator iter;
                int i = 0;
                for (iter = gramInfo.begin(); iter != gramInfo.end(); ++iter) {
                    if (i != 0)
                        res += "==";
                    res += iter->second;
                    ++i;
                }
                int length = GPUtils::safeSprintf(bufsend, "%s::%s", sps[0].c_str(), res.c_str());
                SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
            }
        } else if (recv.find("get_temperature") != std::string::npos) {
            std::map<std::string, float> tempInfo = SP->mTemperature->getThermalMap();
            std::string res = "";
            std::map<std::string, float>::iterator iter;
            int i = 0;
            for (iter = tempInfo.begin(); iter != tempInfo.end(); ++iter) {
                if (i != 0)
                    res += "==";
                res += (iter->first + ",," + std::to_string(iter->second));
                ++i;
            }
            int length = GPUtils::safeSprintf(bufsend, "%s::%s", recv.c_str(), res.c_str());
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_power") != std::string::npos) {
            std::map<std::string, std::string> powerInfo;
            powerInfo = SP->mPower->getPowerMap();
            std::string res = "";
            std::map<std::string, std::string>::iterator iter;
            int i = 0;
            for (iter = powerInfo.begin(); iter != powerInfo.end(); ++iter) {
                if (i != 0)
                    res += "==";
                res += (iter->first + ",," + iter->second);
                ++i;
            }
            int length = GPUtils::safeSprintf(bufsend, "%s::%s", recv.c_str(), res.c_str());
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("get_capture") != std::string::npos) {
            char cmd_capture[20];
            GPUtils::safeSprintf(cmd_capture, "hi_snapshot");
            std::string res = GPUtils::readFile(cmd_capture);
            int length = GPUtils::safeSprintf(bufsend, "%s::%s", "get_capture", res.c_str());
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);
        } else if (recv.find("catch_trace_start") != std::string::npos) {   
            pthread_t t_trace_begin;
            pthread_create(&t_trace_begin, nullptr, SP->mByTrace->thread_get_trace, nullptr);
            int length = GPUtils::safeSprintf(bufsend, "%s::%s", "catch_trace_start", "trace_begin");
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);  
        } else if (recv.find("catch_trace_finish") != std::string::npos) {   
            std::vector<std::string> traces;
            GPUtils::mSplit(recv, "::", traces);          
            pthread_t t_trace_finish;
            pthread_create(&t_trace_finish, nullptr, SP->mByTrace->thread_finish_trace, (void *)&(traces[1]));
            int length = GPUtils::safeSprintf(bufsend, "%s::%s", "catch_trace", "trace_finish");
            SP->bufsendto(sock, bufsend, length, reinterpret_cast<struct sockaddr*>(&client), len);      
        }
    }
}
}
}

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
#include <sys/types.h>
#include <thread>
#include "include/gp_utils.h"
#include "include/gp_constant.h"
#include "include/socket_profiler.h"
namespace OHOS {
namespace SmartPerf {
void SocketProfiler::initSocketProfiler()
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

int SocketProfiler::bufsendto(int sockLocal, const char *bufsend, int length, 
    struct sockaddr *clientLocal, socklen_t len)
{
    ssize_t echo_size = sendto(sockLocal, bufsend, length, ZERO, clientLocal, len);
    if (echo_size < ZERO) {
        printf("sendto error, buf is %s\n", bufsend);
        return ERROR_MINUX;
    }
    return SUCCESS_ZERO;
}

void SocketProfiler::callSend(std::stringstream &sstream, std::string &str1, std::string &str2)
{
    sstream.str("");
    sstream.clear();
    sstream << str1 << "::" << str2;
    std::string streamSend = sstream.str();
    bufsendto(sock, streamSend.c_str(), streamSend.size(), 
        reinterpret_cast<struct sockaddr*>(&client), sizeof(sockaddr_in));
}

void SocketProfiler::initSocket() 
{
    sock = socket(AF_INET, SOCK_DGRAM, ZERO);
    if (sock < ZERO) {
        perror("socket error");
    }
    local.sin_family = AF_INET;
    local.sin_port = htons(SOCK_PORT);
    local.sin_addr.s_addr = htonl(INADDR_ANY);
    if (::bind(sock, reinterpret_cast<struct sockaddr*>(&local), sizeof(local)) < ZERO) {
        perror("bind error");
    }
}

void SocketProfiler::thread_udp_server()
{
    std::shared_ptr<SocketProfiler> SP = SocketProfiler::GetInstance();
    SP->initSocket();
    socklen_t len = sizeof(sockaddr_in);
    std::stringstream sstream;
    const int loopforever = 1;
    printf("enter while loop forever\n");
    while (loopforever) {
        char recvbuf[BUFF_SIZE_RECV];
        recvbuf[0] = '\0';
        ssize_t _size = recvfrom(sock, recvbuf, sizeof(recvbuf) - 1, 0, 
        reinterpret_cast<struct sockaddr*>(&client), &len);
        if (_size > 0) {
            recvbuf[_size] = '\0';
            printf("server recvbuf:%s\n", recvbuf);
        }
        sstream.str("");
        sstream.clear();
        std::string recv = std::string(recvbuf);
        if (recv.find("get_cpu_num") != std::string::npos) {
            std::string recvStr = "get_cpu_num";
            int ret = SP->mCpu->get_cpu_num();
            std::string str2 = std::to_string(ret);
            SP->callSend(sstream, recvStr, str2);
        } else if (recv.find("get_cpu_freq") != std::string::npos) {
            std::vector<std::string> sps;
            GPUtils::mSplit(recv, "_", sps);
            int cpu_id = std::stoi(sps[sps.size() - 1]);
            int ret = SP->mCpu->get_cpu_freq(cpu_id);
            std::string str2 = std::to_string(ret);
            SP->callSend(sstream, recv, str2);
        } else if (recv.find("get_cpu_load") != std::string::npos) {
            std::vector<float> workloads = SP->mCpu->get_cpu_load();
            std::string res = "";
            for (size_t i = 0; i < workloads.size(); ++i) {
                if (i != 0) {
                    res += "==";
                }
                res += std::to_string(workloads[i]);
            }
            SP->callSend(sstream, recv, res);
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
        
                FpsInfo gfpsInfo = SP->mFps->getFpsInfo(is_video, is_camera);
                std::string res = "";
                res += "timestamp|";
                res += std::to_string(gfpsInfo.current_fps_time);
                res += ";";
                res += "fps|";
                res += std::to_string(gfpsInfo.fps);
                res += ";";
                res += "jitter|";
                for (size_t i = 0; i < gfpsInfo.jitters.size(); ++i) {
                    res += std::to_string(gfpsInfo.jitters[i]);
                    res += "==";
                }
                std::string recvStr = "get_fps_and_jitters";
                SP->callSend(sstream, recvStr, res);
            }
        } else if (recv.find("get_gpu_freq") != std::string::npos) {
            int ret = SP->mGpu->get_gpu_freq();
            std::string str2 = std::to_string(ret);
            SP->callSend(sstream, recv, str2);
        } else if (recv.find("get_gpu_load") != std::string::npos) {
            float workload = SP->mGpu->get_gpu_load();
            std::string str2 = std::to_string(workload);
            SP->callSend(sstream, recv, str2);
        } else if (recv.find("get_ddr_freq") != std::string::npos) {
            long long ret = SP->mDdr->get_ddr_freq();
            std::string str2 = std::to_string(ret);
            SP->callSend(sstream, recv, str2);
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
                SP->callSend(sstream, sps[0], res);
            }
        } else if (recv.find("get_temperature") != std::string::npos) {
            std::map<std::string, float> tempInfo = SP->mTemperature->getThermalMap();
            std::string res = "";
            std::map<std::string, float>::iterator iter;
            int i = 0;
            for (iter = tempInfo.begin(); iter != tempInfo.end(); ++iter) {
                if (i != 0) {
                    res += "==";
                }
                res += (iter->first + ",," + std::to_string(iter->second));
                ++i;
            }
            SP->callSend(sstream, recv, res);
        } else if (recv.find("get_power") != std::string::npos) {
            std::map<std::string, std::string> powerInfo;
            powerInfo = SP->mPower->getPowerMap();
            std::string res = "";
            std::map<std::string, std::string>::iterator iter;
            int i = 0;
            for (iter = powerInfo.begin(); iter != powerInfo.end(); ++iter) {
                if (i != 0) {
                    res += "==";
                }
                res += (iter->first + ",," + iter->second);
                ++i;
            }
            SP->callSend(sstream, recv, res);
        } else if (recv.find("get_capture") != std::string::npos) {
            sstream << "snapshot_display";
            std::string cmd_capture = sstream.str();
            GPUtils::readFile(cmd_capture);
        } else if (recv.find("catch_trace_start") != std::string::npos) {   
            std::thread tStart(&ByTrace::thread_get_trace, SP->mByTrace);
        } else if (recv.find("catch_trace_finish") != std::string::npos) {   
            std::vector<std::string> traces;
            GPUtils::mSplit(recv, "::", traces);         
            std::thread tFinish(&ByTrace::thread_finish_trace, SP->mByTrace, std::ref(traces[1]));      
        }
    }
}
}
}

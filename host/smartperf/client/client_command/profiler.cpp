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
#include <iostream>
#include "securec.h"
#include "pthread.h"
#include "include/profiler.h"
namespace OHOS {
namespace SmartPerf {
Profiler::Profiler()
{
    // get singleton instance
    mCpu = CPU::GetInstance();
    mGpu = GPU::GetInstance();
    mDdr = DDR::GetInstance();
    mFps = FPS::GetInstance();
    mRam = RAM::GetInstance();
    mTemperature = Temperature::GetInstance();
    mPower = Power::GetInstance();
    mByTrace = ByTrace::GetInstance();

    // some init methods
    mTemperature->init_temperature();
    mGpu->init_gpu_node();
    mPower->init_power();
    if (mByTrace->init_trace(true) == TRACE_START) {
        pthread_t t_trace_begin;
        pthread_create(&t_trace_begin, nullptr, mByTrace->thread_get_trace, nullptr);
    }
}

void Profiler::createCpu(std::map<std::string, std::string> &gpMap)
{
    int cpuCoreNum = mCpu->get_cpu_num();
    for (int i = 0; i < cpuCoreNum; i++) {
        int curFreq = mCpu->get_cpu_freq(i);
        char desc[10];
        if (snprintf_s(desc, sizeof(desc), sizeof(desc), "cpu%dfreq", i) > 0) {
            gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(curFreq)));
        }
    }

    std::vector<float> workloads = mCpu->get_cpu_load();

    for (int i = 1; i < workloads.size(); ++i) {
        char desc[10];
        if (snprintf_s(desc, sizeof(desc), sizeof(desc), "cpu%dload", i - 1) > 0) {
            gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(workloads[i])));
        }
    }
}
void Profiler::createGpu(std::map<std::string, std::string> &gpMap)
{
    int ret = mGpu->get_gpu_freq();
    float workload = mGpu->get_gpu_load();
    char desc[10];
    if (snprintf_s(desc, sizeof(desc), sizeof(desc), "gpufreq") > 0) {
        gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(ret)));
    }
    if (snprintf_s(desc, sizeof(desc), sizeof(desc), "gpuload") > 0) {
        gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(workload)));
    }
}
void Profiler::createDdr(std::map<std::string, std::string> &gpMap)
{
    long long ret = mDdr->get_ddr_freq();
    char desc[10];
    if (snprintf_s(desc, sizeof(desc), sizeof(desc), "ddrfreq") > 0) {
        gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(ret)));
    }
}

void *Profiler::thread_get_fps(void *arg)
{
    struct ProfilerFps *p = (ProfilerFps *)arg;
    int videoOn = p->is_video;
    int cameraOn = p->is_camera;
    p->spThis->mFps->m_fpsInfo = p->spThis->mFps->getFpsInfo(videoOn, cameraOn);
    pthread_exit(nullptr);
    return nullptr;
}

void Profiler::createFps(int isVideo, int isCamera, int isCatchTrace, int curProfilerNum,
    std::map<std::string, std::string> &gpMap)
{
    struct ProfilerFps *par = new ProfilerFps;
    par->is_video = isVideo;
    par->is_camera = isCamera;
    par->spThis = this;
    pthread_t t_fps;
    pthread_create(&t_fps, nullptr, this->thread_get_fps, static_cast<void*>(par));
    FpsInfo gfpsInfo = mFps->m_fpsInfo;
    char desc[10];
    if (snprintf_s(desc, sizeof(desc), sizeof(desc), "fps") > 0) {
        gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(gfpsInfo.fps)));
    }
    if (isCatchTrace > 0) {
        if (mByTrace->check_fps_jitters(gfpsInfo.jitters, curProfilerNum) == TRACE_FINISH) {
            std::string profilerNum = std::to_string(curProfilerNum);
            pthread_t t_trace_finish;
            pthread_create(&t_trace_finish, nullptr, mByTrace->thread_finish_trace, static_cast<void*>(&profilerNum));
        }
    }
}
void Profiler::createTemp(std::map<std::string, std::string> &gpMap)
{
    std::map<std::string, float> tempInfo = mTemperature->getThermalMap();

    std::map<std::string, float>::iterator iter;
    for (iter = tempInfo.begin(); iter != tempInfo.end(); ++iter) {
        float value = iter->second;
        gpMap.insert(std::pair<std::string, std::string>(iter->first, std::to_string(value)));
    }
}
void Profiler::createPower(std::map<std::string, std::string> &gpMap)
{
    std::map<std::string, std::string> powerInfo;
    powerInfo = mPower->getPowerMap();
    std::map<std::string, std::string>::iterator iter;
    for (iter = powerInfo.begin(); iter != powerInfo.end(); ++iter) {
        gpMap.insert(std::pair<std::string, std::string>(iter->first, iter->second));
    }
}
void Profiler::createRam(const std::string &pkg_name, std::map<std::string, std::string> &gpMap, int pid)
{
    std::map<std::string, std::string> gramInfo;
    gramInfo = mRam->getRamInfo(pkg_name, pid);
    std::map<std::string, std::string>::iterator iter;
    for (iter = gramInfo.begin(); iter != gramInfo.end(); ++iter) {
        gpMap.insert(std::pair<std::string, std::string>(iter->first, iter->second));
    }
}
void Profiler::createSnapshot(std::map<std::string, std::string> &gpMap, long long timestamp)
{
    char pathstr[50];
    std::string path;
    if (snprintf_s(pathstr, sizeof(pathstr), sizeof(pathstr), "/data/local/tmp/capture/%lld", timestamp) > 0) {
        path = pathstr;
    }
    char cmdCapture[100];
    if (snprintf_s(cmdCapture, sizeof(cmdCapture), sizeof(cmdCapture), 
        "snapshot_display -f /data/local/tmp/capture/%lld.png", timestamp) > 0) {
        GPUtils::readFile(cmdCapture);
    }
    gpMap.insert(std::pair<std::string, std::string>(std::string("snapshotPath"), path));
}
}
}
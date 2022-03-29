/*
* Copyright (C) 2021 Huawei Device Co., Ltd.
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
#include "include/profiler.h"
namespace OHOS {
    namespace SmartPerf {
        Profiler::Profiler()
        {
            mCpu = CPU::getInstance();
            mGpu = GPU::getInstance();
            mDdr = DDR::getInstance();
            mFps = FPS::getInstance();
            mRam = RAM::getInstance();
            mTemperature = Temperature::getInstance();
            mPower = Power::getInstance();
        }

        void Profiler::createCpu(std::map<std::string, std::string> &gpMap)
        {
            int cpuCoreNum = mCpu->get_cpu_num();
            for (int i = 0; i < cpuCoreNum; i++) {
                int curFreq = mCpu->get_cpu_freq(i);
                char desc[10];
                sprintf(desc, "cpu%dfreq", i);
                gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(curFreq)));
            }

            std::vector<float> workloads;
            workloads = mCpu->get_cpu_load();

            for (int i = 1; i < workloads.size(); ++i) {
                char desc[10];
                sprintf(desc, "cpu%dload", i - 1);
                gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(workloads[i])));
            }
        }
        void Profiler::createGpu(std::map<std::string, std::string> &gpMap)
        {
            int ret = mGpu->get_gpu_freq();
            float workload = mGpu->get_gpu_load();
            char desc[10];
            sprintf(desc, "gpufreq");
            gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(ret)));
            sprintf(desc, "gpuload");
            gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(workload)));
        }
        void Profiler::createDdr(std::map<std::string, std::string> &gpMap)
        {
            long long ret = mDdr->getDdrFreq();
            char desc[10];
            sprintf(desc, "ddrfreq");
            gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(ret)));
        }
        void Profiler::createFps(int isVideo, int isCamera, std::map<std::string, std::string> &gpMap)
        {
            static int videoOn = isVideo;
            static int cameraOn = isCamera;

            FpsInfo gfpsInfo;
            gfpsInfo = mFps->getFpsInfo(videoOn, cameraOn);
            std::string res = "";
            res += "timestamp|";
            res += std::to_string(gfpsInfo.current_fps_time);
            res += ";";
            res += "fps|";
            res += std::to_string(gfpsInfo.fps);
            res += ";";
            res += "jitter|";
            for (int i = 0; i < gfpsInfo.jitters.size(); ++i) {
                res += std::to_string(gfpsInfo.jitters[i]);
                res += "==";
            }
            char desc[10];
            sprintf(desc, "fps");
            gpMap.insert(std::pair<std::string, std::string>(std::string(desc), std::to_string(gfpsInfo.fps)));
        }

        void Profiler::createTemp(std::map<std::string, std::string> &gpMap)
        {
            std::map<std::string, float> tempInfo;
            tempInfo = mTemperature->getThermalMap();

            std::map<std::string, float>::iterator iter;
            for (iter = tempInfo.begin(); iter != tempInfo.end(); ++iter) {
                float value = iter->second;
                gpMap.insert(
                    std::pair<std::string, std::string>(
                        iter->first,
                        std::to_string(value)));
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
            char cmdCapture[20];
            sprintf(cmdCapture, "hi_snapshot");
            std::string res = GPUtils::readFile(cmdCapture);
            char pathstr[50];
            sprintf(pathstr, "/data/local/tmp/capture/%lld", timestamp);
            std::string path = pathstr;
            gpMap.insert(std::pair<std::string, std::string>(std::string("snapshotPath"), path));
        }
    }
}
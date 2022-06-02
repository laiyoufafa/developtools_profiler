/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
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
 **/

#include <cstdio>
#include <algorithm>
#include <string>
#include <sstream>
#include <pthread.h>
#include "gp_utils.h"
#include "FPS.h"

pthread_mutex_t FPS::mutex;
FPS *FPS::instance = nullptr;
FPS *FPS::getInstance()
{
    if (instance == nullptr) {
        pthread_mutex_lock(&mutex);
        if (instance == nullptr) {
            instance = new FPS();
        }
        pthread_mutex_unlock(&mutex);
    }
    return instance;
}
FPS::FPS()
{
    pthread_mutex_init(&mutex, nullptr);
}
void FPS::setPackageName(std::string pkgName)
{
    pkg_name = std::move(pkgName);
}
FpsInfo FPS::getFpsInfo()
{
    FpsInfo fpsInfoMax;
    fpsInfoMax.fps = -1;

    std::string layerName;

    std::vector<std::string> sps;
    gpUtils::mSplit(this->pkg_name, ".", sps);
    std::string addEndChar = "0";
    const int pNameLastPos = 2;
    std::string pkgSuffix = sps[pNameLastPos];
    layerName = std::string( pkgSuffix.c_str()+ addEndChar);
    if (pkgSuffix.find("camera")!=std::string::npos) {
        layerName = std::string("RosenRenderXComponent");
    }

    FpsInfo fpsInfo = GetSurfaceFrameDataGB(layerName);
    if (fpsInfo.fps > fpsInfoMax.fps) {
        fpsInfoMax = fpsInfo;
    }
    return fpsInfoMax;
}
FpsInfo FPS::GetSurfaceFrameDataGB(std::string name)
{
    if (name == "") {
        return FpsInfo();
    }
    static std::map<std::string, FpsInfo> fps_map;
    if (fps_map.count(name) == 0) {
        FpsInfo tmp;
        tmp.fps = 0;
        tmp.pre_fps = 0;
        fps_map[name] = tmp;
    }
    FpsInfo &fpsInfo = fps_map[name];
    fpsInfo.fps = 0;
    FILE *fp;
    char tmp[1024];
    tmp[0] = '\0';
    std::string cmd = "hidumper -s 10 -a \"fps " + name + "\"";
    fp = popen(cmd.c_str(), "r");
    if (fp == nullptr) {
        return fpsInfo;
    }
    long long MOD = 1e9;
    long long lastReadyTime = -1;
    int fps_gb = 0;
    if (!(fpsInfo.time_stamp_q).empty()) {
        lastReadyTime = (fpsInfo.time_stamp_q).back();
    }
    bool jump = false;
    bool refresh = false;
    int cnt = 0;
    int zeroNum = 0;
    while (fgets(tmp, sizeof(tmp), fp) != nullptr) {
        long long frameReadyTime = 0;
        std::stringstream sstream;
        sstream << tmp;
        sstream >> frameReadyTime;
        cnt++;
        if (frameReadyTime == 0) {
            zeroNum++;
            continue;
        }
        if (lastReadyTime >= frameReadyTime) {
            lastReadyTime = -1;
            continue;
        }
        refresh = true;
        long long t_frameReadyTime = frameReadyTime / MOD;
        long long t_lastReadyTime = lastReadyTime / MOD;
        long long lastFrame = -1;
        if (t_frameReadyTime == t_lastReadyTime) {
            (fpsInfo.time_stamp_q).push(frameReadyTime);
        } else if (t_frameReadyTime == t_lastReadyTime + 1) {
            jump = true;
            lastFrame = fpsInfo.last_frame_ready_time;
            lastReadyTime = frameReadyTime;
            int fps_tmp = 0;
            fpsInfo.jitters.clear();
            while (!(fpsInfo.time_stamp_q).empty()) {
                fps_tmp++;
                long long currFrame = (fpsInfo.time_stamp_q.front());
                if (lastFrame != -1) {
                    long long jitter = currFrame - lastFrame;
                    fpsInfo.jitters.push_back(jitter);
                }
                lastFrame = currFrame;
                (fpsInfo.time_stamp_q).pop();
            }
            fps_gb = fps_tmp;
            (fpsInfo.time_stamp_q).push(frameReadyTime);
            fpsInfo.last_frame_ready_time = lastFrame;
        } else if (t_frameReadyTime > t_lastReadyTime + 1) {
            jump = true;
            lastReadyTime = frameReadyTime;
            while (!(fpsInfo.time_stamp_q).empty()) {
                (fpsInfo.time_stamp_q).pop();
            }
            (fpsInfo.time_stamp_q).push(frameReadyTime);
        }
    }
    pclose(fp);
    const int maxZeroNum = 120;
    if (zeroNum >= maxZeroNum) {
        while (!(fpsInfo.time_stamp_q.empty())) {
            fpsInfo.time_stamp_q.pop();
        }
        fpsInfo.fps = 0;
        return fpsInfo;
    }
    const int minPrintLine = 5;
    if (cnt < minPrintLine) {
        fpsInfo.fps = fpsInfo.pre_fps;
        return fpsInfo;
    }
    if (fps_gb > 0) {
        fpsInfo.fps = fps_gb;
        fpsInfo.pre_fps = fps_gb;
        return fpsInfo;
    } else if (refresh && !jump) {
        fpsInfo.fps = fpsInfo.pre_fps;
        return fpsInfo;
    } else {
        fpsInfo.fps = 0;
        return fpsInfo;
    }
}
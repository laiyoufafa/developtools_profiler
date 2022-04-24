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
#include <cstdio>
#include <cerrno>
#include <algorithm>
#include <iostream>
#include "securec.h"
#include "include/gp_utils.h"
#include "include/FPS.h"
namespace OHOS {
    namespace SmartPerf {
        void FPS::setPackageName(std::string pkgName)
        {
            pkg_name = std::move(pkgName);
            std::vector<std::string> sps;
            GPUtils::mSplit(pkg_name, ".", sps);
            if (sps.size() > 0) {
                cur_layer_name = std::string("");
                std::string cur_layer = sps[2];
                char cmd[100];
                if (snprintf_s(cmd, sizeof(cmd), sizeof(cmd), 
                    "hidumper -s 10 |grep surface |grep %s", cur_layer.c_str()) < 0) {
                    std::cout << "snprintf fail";
                }
                std::string layer_line = GPUtils::readFile(cmd);
                int flag = 0;
                for (int i = 0; i < layer_line.size(); i++) {
                    if (layer_line[i] == ']') {
                        flag = 0;
                        break;
                    }
                    if (flag) {
                        cur_layer_name += layer_line[i];
                    }
                    if (layer_line[i] == '[') {
                        flag = 1;
                    }
                }
            }
        }
        FpsInfo FPS::getFpsInfo(int is_video, int is_camera)
        {
            FpsInfo fpsInfoMax;
            fpsInfoMax.fps = -1;

            std::string layerName;
            if (is_video) {
                layerName = std::string("RosenRenderTexture");
            } else if (is_camera) {
                layerName = std::string("RosenRenderXComponent");
            } else {
                std::vector<std::string> sps;
                GPUtils::mSplit(this->pkg_name, ".", sps);
                std::string addEndChar = "0";
                const int pNameLastPos = 2;
                layerName = std::string(sps[pNameLastPos].c_str() + addEndChar);
            }
            FpsInfo fpsInfo = GetSurfaceFrame(layerName);
            if (fpsInfo.fps > fpsInfoMax.fps) {
                fpsInfoMax = fpsInfo;
            }
            return fpsInfoMax;
        }
        FpsInfo FPS::GetSurfaceFrame(std::string name)
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
            static char tmp[1024];
            std::string cmd = "hidumper -s 10 -a \"fps " + name + "\"";
            fp = popen(cmd.c_str(), "r");
            if (fp == nullptr) {
                printf("FPS--- fopen %s fail,err=%s\n", cmd.c_str(), strerror(errno));
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
                sscanf(tmp, "%lld", &frameReadyTime);
                cnt++;
                if (frameReadyTime == 0) {
                    zeroNum++;
                    continue;
                }
                if (lastReadyTime >= frameReadyTime) {
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
    }
}

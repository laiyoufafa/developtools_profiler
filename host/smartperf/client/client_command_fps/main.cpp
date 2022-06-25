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
#include <algorithm>
#include <iostream>
#include <sstream>
#include <queue>
#include <vector>
#include <map>
#include <string>
#include <unistd.h>
#include <sys/time.h>

struct DumpEntity {
    const std::string windowName;
    const std::string displayId;
    const std::string pid;
    const std::string windId;
};
struct FpsInfo {
    int fps;
    int pre_fps;
    std::vector<long long> jitters;
    std::queue<long long> time_stamp_q;
    long long last_frame_ready_time;
    long long current_fps_time;
    FpsInfo()
    {
        fps = 0;
        pre_fps = 0;
        last_frame_ready_time = 0;
        current_fps_time = 0;
    }
};
struct FpsConfig {
    const long long MOD = 1e9;
    long long lastReadyTime;
    int fps_gb;
    bool jump;
    bool refresh;
    int cnt;
    int zeroNum;
    FpsConfig()
    {
        lastReadyTime = -1;
        fps_gb = 0;
        jump = false;
        refresh = false;
        cnt = 0;
        zeroNum = 0;
    }
};

static void StrSplit(const std::string &content, const std::string &sp, std::vector<std::string> &out)
{
    size_t index = 0;
    while (index != std::string::npos) {
        size_t t_end = content.find_first_of(sp, index);
        std::string tmp = content.substr(index, t_end - index);
        if (tmp != "" && tmp != " ") {
            out.push_back(tmp);
        }
        if (t_end == std::string::npos) {
            break;
        }
        index = t_end + 1;
    }
}

static std::string getLayer()
{
    std::vector<DumpEntity> dumpEntityList;
    std::string curFocusId = "-1";
    const std::string cmd = "hidumper -s WindowManagerService -a -a";
    FILE *fd = popen(cmd.c_str(), "r");
    if (fd != nullptr) {
        int lineNum = 0;
        std::string line;
        char buf[1024] = {'\0'};

        const int ParamFifteen = 15;
        const int ParamFourteen = 14;
        const int ParamThree = 3;
        const int windowNameIndex = 0;
        const int windowIdIndex = 3;
        const int focusNameIndex = 2;

        while ((fgets(buf, sizeof(buf), fd)) != nullptr) {
            line = buf;
            if (line[0] == '-' || line[0] == ' ') {
                continue;
            }
            std::vector<std::string> params;
            StrSplit(line, " ", params);
            if (params[windowNameIndex].find("WindowName")!= std::string::npos &&
                params[windowIdIndex].find("WinId")!= std::string::npos) {
                continue;
            }
            if (params.size() == ParamFifteen) {
                DumpEntity dumpEntity { params[0], params[1], params[2], params[3] };
                dumpEntityList.push_back(dumpEntity);
            }
            if (params.size() == ParamFourteen) {
                DumpEntity dumpEntity { params[0], params[1], params[2].substr(0, 4),
                    params[2].substr(5, params[2].size() - 1) };
                dumpEntityList.push_back(dumpEntity);
            }
            if (params.size() == ParamThree) {
                curFocusId = params[focusNameIndex];
                break;
            }
            lineNum++;
        }
        pclose(fd);
    }

    std::string resultWindowName = "NA";
    int curId = std::stoi(curFocusId);
    for (size_t i = 0; i < dumpEntityList.size(); i++) {
        DumpEntity dumpItem = dumpEntityList[i];
        int curWinId = std::stoi(dumpItem.windId);
        if (curId == curWinId) {
            resultWindowName = dumpItem.windowName;
        }
    }
    return resultWindowName;
}

static void processResult(FILE *fp, FpsConfig &fpsConfig, FpsInfo &fpsInfo) 
{
    char tmp[1024];
    while (fgets(tmp, sizeof(tmp), fp) != nullptr) {
        long long frameReadyTime = 0;
        std::stringstream sstream;
        sstream << tmp;
        sstream >> frameReadyTime;
        fpsConfig.cnt++;
        if (frameReadyTime == 0) {
            fpsConfig.zeroNum++;
            continue;
        }
        
        if (fpsConfig.lastReadyTime >= frameReadyTime) {
            fpsConfig.lastReadyTime = -1;
            continue;
        }
        fpsConfig.refresh = true;
        long long t_frameReadyTime = frameReadyTime / fpsConfig.MOD;
        long long t_lastReadyTime = fpsConfig.lastReadyTime / fpsConfig.MOD;
        long long lastFrame = -1;
        if (t_frameReadyTime == t_lastReadyTime) {
            (fpsInfo.time_stamp_q).push(frameReadyTime);
        } else if (t_frameReadyTime == t_lastReadyTime + 1) {
            fpsConfig.jump = true;
            lastFrame = fpsInfo.last_frame_ready_time;
            fpsConfig.lastReadyTime = frameReadyTime;
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
            fpsConfig.fps_gb = fps_tmp;
            (fpsInfo.time_stamp_q).push(frameReadyTime);
            fpsInfo.last_frame_ready_time = lastFrame;
        } else if (t_frameReadyTime > t_lastReadyTime + 1) {
            fpsConfig.jump = true;
            fpsConfig.lastReadyTime = frameReadyTime;
            while (!(fpsInfo.time_stamp_q).empty()) {
                (fpsInfo.time_stamp_q).pop();
            }
            (fpsInfo.time_stamp_q).push(frameReadyTime);
        }
    }
}

static FpsInfo GetSurfaceFrame(std::string name, FpsConfig &fpsConfig)
{
    static std::map<std::string, FpsInfo> fps_map;
    if (fps_map.count(name) == 0) {
        FpsInfo tmp;
        tmp.fps = 0;
        tmp.pre_fps = 0;
        fps_map[name] = tmp;
    }
    FpsInfo &fpsInfo = fps_map[name];
    fpsInfo.fps = 0;

    struct timeval tv;
    gettimeofday(&tv, nullptr);
    fpsInfo.current_fps_time = tv.tv_sec * 1e3 + tv.tv_usec / 1e3;

    std::string cmd = "hidumper -s 10 -a \"fps " + name + "\"";
    FILE *fp = popen(cmd.c_str(), "r");
    if (fp == nullptr) {
        return fpsInfo;
    }
    if (!(fpsInfo.time_stamp_q).empty()) {
        fpsConfig.lastReadyTime = (fpsInfo.time_stamp_q).back();
    }
    processResult(fp, fpsConfig, fpsInfo);
    pclose(fp);
    const int maxZeroNum = 120;
    const int minPrintLine = 5;
    if (fpsConfig.zeroNum >= maxZeroNum) {
        while (!(fpsInfo.time_stamp_q.empty())) {
            fpsInfo.time_stamp_q.pop();
        }
        fpsInfo.fps = 0;
        return fpsInfo;
    }

    if (fpsConfig.cnt < minPrintLine) {
        fpsInfo.fps = fpsInfo.pre_fps;
        return fpsInfo;
    }
    if (fpsConfig.fps_gb > 0) {
        fpsInfo.fps = fpsConfig.fps_gb;
        fpsInfo.pre_fps = fpsConfig.fps_gb;
        return fpsInfo;
    } else if (fpsConfig.refresh && !fpsConfig.jump) {
        fpsInfo.fps = fpsInfo.pre_fps;
        return fpsInfo;
    } else {
        fpsInfo.fps = 0;
        return fpsInfo;
    }
}

int main(int argc, char *argv[])
{
    FpsInfo gfpsInfo;
    int num = 1;
    if (!strcmp(argv[1], "")) {
        printf("the args of num must be not-null!\n");
    } else {
        num = atoi(argv[1]);
        printf("set num:%d success\n", num);
        for (int i = 0; i < num; i++) {
            std::string layerName = getLayer();
            FpsConfig fpsConfig;
            gfpsInfo = GetSurfaceFrame(layerName, fpsConfig);
            printf("fps:%d|%lld\n", gfpsInfo.fps, gfpsInfo.current_fps_time);
            fflush(stdout);
            sleep(1);
        }
    }
    printf("GP_daemon_fps exec finished!\n");
    return 0;
}

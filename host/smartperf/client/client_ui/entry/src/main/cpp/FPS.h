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
#ifndef FPS_H
#define FPS_H

#include <map>
#include <string>
#include <vector>
#include <queue>
#include "pthread.h"
#include "gp_utils.h"

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
class FPS{
public:
    static FPS* getInstance();
    FpsInfo getFpsInfo();
    FpsInfo GetSurfaceFrameDataGB(std::string name);
    void setPackageName(std::string pkgName);
    std::string pkg_name;
    static pthread_mutex_t mutex;
private:
    FPS();
    ~FPS(){};
    static FPS* instance;
};

#endif // FPS_H

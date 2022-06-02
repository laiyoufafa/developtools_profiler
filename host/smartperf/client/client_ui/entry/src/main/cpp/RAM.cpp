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
 */
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include "gp_utils.h"
#include "RAM.h"

pthread_mutex_t RAM::mutex;
RAM *RAM::instance = nullptr;
RAM *RAM::getInstance()
{
    if (instance == nullptr)
    {
        pthread_mutex_lock(&mutex);
        if (instance == nullptr)
        {
            instance = new RAM();
        }
        pthread_mutex_unlock(&mutex);
    }
    return instance;
}
RAM::RAM()
{
    pthread_mutex_init(&mutex, nullptr);
}

std::map<std::string, std::string> RAM::getRamInfo(std::string pid)
{
    std::map<std::string, std::string> ramInfo;
    std::string pss_value = "";
    ramInfo["pss"] = "-1";
    if (pid.size() > 0) {
        std::ostringstream cmd_grep;
        cmd_grep << "hidumper --mem ";
        cmd_grep <<  pid;
        std::string cmd = cmd_grep.str();
        std::string pidLine = gpUtils::readCmd(cmd);
        std::vector<std::string> sps;
        gpUtils::mSplit(pidLine, " ", sps);
        if (sps.size() > 0) {
            pss_value = sps[1];
        }
    }
    if (atoi(pss_value.c_str()) > 0) {
        ramInfo["pss"] = gpUtils::extractNumber(pss_value.c_str());
    }
    return ramInfo;
}

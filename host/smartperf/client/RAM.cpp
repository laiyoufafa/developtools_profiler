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
#include <iostream>
#include "include/gp_utils.h"
#include "include/RAM.h"

namespace OHOS {
    namespace SmartPerf {
        RAM *RAM::instance = nullptr;
        RAM *RAM::getInstance()
        {
            if (instance == nullptr) {
                instance = new RAM();
            }
            return instance;
        }

        RAM::RAM() {}

        void RAM::setPkgName(std::string ss)
        {
            pkgName = std::move(ss);
        }

        std::map<std::string, std::string> RAM::getRamInfo(std::string pkg_name, int pid)
        {
            std::map<std::string, std::string> ramInfo;
            std::string pid_value = "";
            if (pid > 0) {
                pid_value = std::to_string(pid);
            } else {
                char pidStr[100];
                if (snprintf(pidStr, sizeof(pidStr),
                "ps -ef |grep -w %s |grep -v grep |grep -v SP_daemon", pkg_name.c_str()) < 0) {
                    std::cout << "snprintf fail";
                }
                std::string pidLine = GPUtils::readFile(pidStr);
                std::vector<std::string> sps;
                GPUtils::mSplit(pidLine, " ", sps);
                if (sps.size() > 0) {
                    pid_value = sps[1];
                }
            }

            std::string tmp = "-1";
            if (atoi(pid_value.c_str()) > 0) {
                char ram[50];
                if (snprintf(ram, sizeof(ram), "/proc/%s/smaps_rollup", pid_value.c_str()) < 0) {
                    std::cout << "snprintf fail";
                }
                std::string path = ram;
                FILE *fp;
                if ((fp = fopen(path.c_str(), "r")) != nullptr) {
                    char s[1024];
                    s[0] = '\0';
                    while (fgets(s, sizeof(s), fp) != nullptr) {
                        const int zeroPos = 0;
                        const int firstPos = 1;
                        const int secondPos = 2;
                        const int thirdPos = 3;
                        if (s[zeroPos] == 'P' && s[firstPos] == 's' && s[secondPos] == 's' && s[thirdPos] == ':') {
                            tmp += std::string(s);
                        }
                    }
                }
                if (fp != nullptr) {
                    pclose(fp);
                }
            }
            ramInfo["pss"] = GPUtils::getNumber(tmp);
            return ramInfo;
        }
    }
}

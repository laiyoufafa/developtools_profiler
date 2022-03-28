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
#include <errno.h>
#include "include/gp_utils.h"
#include "include/RAM.h"

namespace OHOS {
    namespace SmartPerf {
        pthread_mutex_t RAM::mutex;
        RAM *RAM::instance = nullptr;
        RAM *RAM::getInstance()
        {
            if (instance == nullptr) {
                pthread_mutex_lock(&mutex);
                if (instance == nullptr) {
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
                sprintf(pidStr, "ps -ef |grep -w %s |grep -v grep |grep -v SP_daemon", pkg_name.c_str());
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
                sprintf(ram, "/proc/%s/smaps_rollup", pid_value.c_str());
                std::string path = ram;
                FILE *fp;
                if ((fp = fopen(path.c_str(), "r")) != NULL) {
                    char s[1024];
                    s[0] = '\0';
                    while (fgets(s, sizeof(s), fp) != NULL) {
                        
                        // 获取物理内存Pss: "p" 过滤读取的行中第一个字符是否为'P',"s" 过滤读取的行中第二个字符是否为's','s  过滤读取的行中第三个字符是否为's'
                        if (s[0] == 'P' && s[1] == 's' && s[2] == 's' && s[3] == ':') {
                            tmp += std::string(s);
                        }
                    }
                }
                if (fp != NULL) {
                    pclose(fp);
                }
            }
           
            ramInfo["pss"] = GPUtils::getNumber(tmp);
            return ramInfo;
        }
    }
}

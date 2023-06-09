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
#include <map>
#include "sys/time.h"
#include "unistd.h"
#include "pthread.h"
#include "include/gp_data.h"
#include "include/gp_utils.h"
#include "include/smartperf_command.h"

namespace OHOS {
namespace SmartPerf {
SmartPerfCommand::SmartPerfCommand(int argc, char *argv[])
{
    if (argc == ONE_PARAM) {
        this->socketProfiler = new SocketProfiler();
        this->socketProfiler->initSocketProfiler();
        struct SmartPerfCommandParam *scp = new SmartPerfCommandParam;
        scp->spThis = this->socketProfiler;
        daemon(0, 0);
        pthread_t t_udp;
        pthread_create(&t_udp, nullptr, this->socketProfiler->thread_udp_server, static_cast<void*>(scp));
        pthread_join(t_udp, nullptr);
    }
    if (argc == TWO_PARAM) {
        char *cmd = argv[1];
        if (strcmp(cmd, "--help") == 0 || strcmp(cmd, "-h") == 0) {
            std::cout << SmartPerf_MSG;
        } else if (strcmp(cmd, "--version") == 0) {
            std::cout << SmartPerf_VERSION;
        } else {
            std::cout << SmartPerf_MSG_ERR;
        }
    }
    if (argc >= THREE_PARAM_MORE) {
        this->profiler = new Profiler();
        this->profiler->initProfiler();
        for (int i = 1; i <= argc - 1; i++) {
            if ((strcmp(argv[i], "-N") == 0) || (strcmp(argv[i], "--num") == 0)) {
                this->num = atoi(argv[i + 1]);
                if (this->num > 0) {
                    std::cout << "set num:" << this->num << std::endl;
                } else {
                    std::cout << "error input args: -N" << std::endl;
                }
            }
            if ((strcmp(argv[i], "-PKG") == 0) || (strcmp(argv[i], "--pkgname") == 0)) {
                this->pkgName = argv[i + 1];
                if (strcmp(this->pkgName.c_str(), "") != 0) {
                    this->profiler->mFps->setPackageName(this->pkgName);
                    std::cout << "set pkg name:" << this->pkgName << std::endl;
                } else {
                    std::cout << "empty input args: -PKG" << std::endl;
                }
            }
            if ((strcmp(argv[i], "-PID") == 0) || (strcmp(argv[i], "--processid") == 0)) {
                this->pid = atoi(argv[i + 1]);
                if (this->pid > 0) {
                    std::cout << "set test pid:" << this->pid << std::endl;
                } else {
                    std::cout << "error input args: -PID " << std::endl;
                }
            }

            if ((strcmp(argv[i], "-OUT") == 0) || (strcmp(argv[i], "--output") == 0)) {
                this->outPathParam = argv[i + 1];
                if (strcmp(this->outPathParam.c_str(), "") != 0) {
                    this->outPath = this->outPathParam + std::string(".csv");
                }
            }

            if (strcmp(argv[i], "-c") == 0 || strcmp(argv[i], "-g") == 0 || strcmp(argv[i], "-d") == 0 ||
                strcmp(argv[i], "-f") == 0 || strcmp(argv[i], "-f1") == 0 || strcmp(argv[i], "-f2") == 0 ||
                strcmp(argv[i], "-t") == 0 || strcmp(argv[i], "-p") == 0 || strcmp(argv[i], "-r") == 0 ||
                strcmp(argv[i], "-trace") == 0 || strcmp(argv[i], "-snapshot") == 0) {
                this->configs.push_back(argv[i]);
            }
        }
    }
}
std::string SmartPerfCommand::ExecCommand()
{
    int index = 0;
    std::vector<GPData> vmap;
    while (index < this->num) {
        std::map<std::string, std::string> gpMap;
        struct timeval tv;
        gettimeofday(&tv, nullptr);
        long long timestamp = tv.tv_sec * 1000 + tv.tv_usec / 1000;
        gpMap.insert(std::pair<std::string, std::string>(std::string("timestamp"), std::to_string(timestamp)));

        for (int j = 0; j < this->configs.size(); j++) {
            std::string curParam = this->configs[j];
            if (strcmp(curParam.c_str(), "-trace") == 0) {
                this->trace = 1;
            }
            if (strcmp(curParam.c_str(), "-c") == 0) {
                profiler->createCpu(gpMap);
            }
            if (strcmp(curParam.c_str(), "-g") == 0) {
                profiler->createGpu(gpMap);
            }
            if (strcmp(curParam.c_str(), "-d") == 0) {
                profiler->createDdr(gpMap);
            }
            if (strcmp(curParam.c_str(), "-f") == 0) {
                profiler->createFps(0, 0, this->trace, index, gpMap);
            } 
            if (strcmp(curParam.c_str(), "-f1") == 0) {
                profiler->createFps(1, 0, this->trace, index, gpMap);
            }
            if (strcmp(curParam.c_str(), "-f2") == 0) {
                profiler->createFps(0, 1, this->trace, index, gpMap);
            }
            if (strcmp(curParam.c_str(), "-t") == 0) {
                profiler->createTemp(gpMap);
            }
            if (strcmp(curParam.c_str(), "-p") == 0) {
                profiler->createPower(gpMap);
            }
            if (strcmp(curParam.c_str(), "-r") == 0) {
                if (strcmp(this->pkgName.c_str(), "") != 0 || this->pid > 0) {
                    profiler->createRam(this->pkgName, gpMap, pid);
                }
            }
            if (strcmp(curParam.c_str(), "-snapshot") == 0) {
                profiler->createSnapshot(gpMap, timestamp);
            }
        }

        printf("----------------------------------Print START------------------------------------\n");
        std::map<std::string, std::string>::iterator iter;
        int i = 0;
        for (iter = gpMap.begin(); iter != gpMap.end(); ++iter) {
            printf("order:%d %s=%s\n", i, iter->first.c_str(), iter->second.c_str());
            i++;
        }
        printf("----------------------------------Print END--------------------------------------\n");

        GPData gpdata;
        gpdata.values = gpMap;
        vmap.push_back(gpdata);
        sleep(1);
        index++;
    }

    GPUtils::writeCsv(std::string(outPath.c_str()), vmap);

    return std::string("command exec finished!");
}
}
}

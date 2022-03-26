/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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
#ifndef SMARTPERF_COMMAND_H
#define SMARTPERF_COMMAND_H
#include <iostream>
#include <vector>
#include "profiler.h"
namespace OHOS
{
    namespace SmartPerf
    {
        namespace
        {
            const std::string SmartPerf_EXE_NAME = "SP_daemon";

            const std::string SmartPerf_VERSION = "1.0.1\n";

            const std::string SmartPerf_MSG_ERR = "error input!\n use command '--help' get more information\n";

            const std::string SmartPerf_MSG = "usage: SP_daemon <options> <arguments> \n"
                                              "--------------------------------------------------------------------\n"
                                              "These are common commands list:\n"
                                              " -N             set num of profiler <must be non-null>\n"
                                              " -PKG           set pkg_name of profiler \n"
                                              " -PID           set process id of profiler \n"
                                              " -OUT           set output path of CSV\n"
                                              " -c             get cpuFreq and cpuLoad  \n"
                                              " -g             get gpuFreq and gpuLoad  \n"
                                              " -d             get ddrFreq  \n"
                                              " -f             get fps and fps jitters <dependent on hidumper capability> \n"
                                              " -t             get soc-temp gpu-temp .. \n"
                                              " -p             get current_now and voltage_now \n"
                                              " -r             get ram(pss) \n"
                                              "--------------------------------------------------------------------\n"
                                              "Example: SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r \n"
                                              "--------------------------------------------------------------------\n";
        }
        class SmartPerfCommand
        {
        public:
            SmartPerfCommand(int argc, char *argv[]);
            ~SmartPerfCommand(){};
            std::string ExecCommand();
            //采集次数
            int num = 0;
            //包名
            std::string pkgName = "";
            // csv输出路径
            std::string outPath = "/data/local/tmp/data.csv";
            std::string outPathParam = "";
            //指定进程pid
            int pid = 0;
            //采集配置项
            std::vector<std::string> configs;
            //采集器设置
            Profiler *profiler = nullptr;
        };
    }
}
#endif // SMARTPERF_COMMAND_H
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
#include "unistd.h"
#include <thread>
#include "include/editor_command.h"
#include "include/startup_delay.h"
#include "include/parse_trace.h"
#include "include/sp_utils.h"
#include "include/parse_click_complete_trace.h"
#include "include/parse_click_response_trace.h"

namespace OHOS {
namespace SmartPerf {
EditorCommand::EditorCommand(int argc, char *argv[])
{
    int TYPE = 2;
    if (argc >= threeParamMore){  
        float time = 0.0;
        if(strcmp(argv[TYPE], "coldStart") == 0)
        {  
            time = SmartPerf::EditorCommand::coldStart(argv);
        }
        else if (strcmp(argv[TYPE], "hotStart") == 0)
        {
            time = SmartPerf::EditorCommand::hotStart(argv);
        }
        else if (strcmp(argv[TYPE], "responseTime") == 0)
        {    
            time = SmartPerf::EditorCommand::responseTime(argv);
        }else if (strcmp(argv[TYPE], "completeTime") == 0)
        {
            time = SmartPerf::EditorCommand::completeTime(argv);
        }
        std::cout << "time:" << time << std::endl;
    }
}
float EditorCommand::responseTime(char *argv[])
{
        OHOS::SmartPerf::ParseClickResponseTrace pcrt;
        OHOS::SmartPerf::StartUpDelay sd;
        std::string cmdResult;
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
        std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "response" + ".ftrace";
        std::thread thGetTrace = sd.ThreadGetTrace("response", traceName);
        thGetTrace.join();
        float time = pcrt.parse_click_response_trace(traceName, argv[3]);
        return time;
}
float EditorCommand::completeTime(char *argv[])
{
        OHOS::SmartPerf::StartUpDelay sd;
        OHOS::SmartPerf::ParseClickCompleteTrace pcct;
        std::string cmdResult;
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
        std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "complete" + ".ftrace";
        std::thread thGetTrace = sd.ThreadGetTrace("complete", traceName);
        thGetTrace.join();
        float time = pcct.parse_click_complete_trace(traceName, argv[3]);
        return time;
}
float EditorCommand::coldStart(char *argv[])
{
        OHOS::SmartPerf::StartUpDelay sd;
        OHOS::SmartPerf::ParseTrace parseTrace;
        std::string cmdResult;
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
        std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "coldStart" + ".ftrace";
        std::thread thGetTrace = sd.ThreadGetTrace("coldStart", traceName);
        thGetTrace.join();
        float time = parseTrace.parse_trace_cold(traceName, argv[3]);
        return time;           
}
float EditorCommand::hotStart(char *argv[])
{
        OHOS::SmartPerf::StartUpDelay sd;
        OHOS::SmartPerf::ParseTrace parseTrace;
        std::string cmdResult;
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
        std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "hotStart" + ".ftrace";
        std::thread thGetTrace = sd.ThreadGetTrace("hotStart", traceName);
        thGetTrace.join();
        float time = parseTrace.parse_trace_hot(traceName, argv[3]);
        return time;         
}
}
}

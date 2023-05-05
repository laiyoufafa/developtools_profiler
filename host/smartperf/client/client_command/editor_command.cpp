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
#include <cstdio>
#include <cstring>
#include "include/editor_command.h"
#include "include/startup_delay.h"
#include "include/parse_trace.h"
#include "include/sp_utils.h"
#include "include/parse_click_complete_trace.h"
#include "include/parse_click_response_trace.h"
#include "include/sp_parse_fps.h"

namespace OHOS {
namespace SmartPerf {
EditorCommand::EditorCommand(int argc, std::vector<std::string> v)
{
    if (argc >= threeParamMore) {
        int ohType = 5;
        int type = 2;
        float time = 0.0;
        float noNameType = -1.0;
        if (v[ohType] == "ohtest") {
            isOhTest = true;
        }
        if (v[type] == "coldStart") {
            time = SmartPerf::EditorCommand::ColdStart(v);
        } else if (v[type] == "hotStart") {
            time = SmartPerf::EditorCommand::HotStart(v);
        } else if (v[type] == "responseTime") {
            time = SmartPerf::EditorCommand::ResponseTime();
        } else if (v[type] == "completeTime") {
            time = SmartPerf::EditorCommand::CompleteTime();
        } else if (v[type] == "fps") {

        }
        if (time == noNameType) {
            std::cout << "Startup error, unknown application or application not responding"<< std::endl;
        } else {
            std::cout << "time:" << time << std::endl;
        }
    }
}
std::string EditorCommand::SlideFPS(std::vector<std::string> v)
{
    OHOS::SmartPerf::StartUpDelay sd;
    ParseFPS parseFPS;
    std::string cmdResult;
    int type = 4;
    int typePKG = 3;
    SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.json", cmdResult);
    SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
    SPUtils::LoadCmd("uitest dumpLayout", cmdResult);
    sleep(1);
    size_t position = cmdResult.find(":");
    std::string pathJson = cmdResult.substr(position + 1);
    std::string deviceType = sd.GetDeviceType();
    sd.InitXY2(v[type], pathJson, v[typePKG]);
    std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "fps" + ".ftrace";
    std::string cmd = "uinput -T -d " + sd.pointXY + " -u " + sd.pointXY;
    sleep(1);
    SPUtils::LoadCmd(cmd, cmdResult);
    sleep(1);
    std::string topPkg = SPUtils::GetTopPkgName();
    std::string pid = sd.GetPidByPkg(v[typePKG]);
    if (topPkg.find(v[typePKG]) == std::string::npos || pid == "") {
        return "";
    }
    std::thread thGetTrace = sd.ThreadGetTrace("fps", traceName);
    cmd = "uinput -T -m 650 1500 650 500 30";
    SPUtils::LoadCmd(cmd, cmdResult);
    thGetTrace.join();
    std::string fps = parseFPS.ParseTraceFile(traceName, v[typePKG]);
    return fps;
}
float EditorCommand::ResponseTime()
{
    OHOS::SmartPerf::ParseClickResponseTrace pcrt;
    OHOS::SmartPerf::StartUpDelay sd;
    std::string cmdResult;
    SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
    std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "response" + ".ftrace";
    std::thread thGetTrace = sd.ThreadGetTrace("response", traceName);
    thGetTrace.join();
    float time = pcrt.ParseResponseTrace(traceName);
    return time;
}
float EditorCommand::CompleteTime()
{
    OHOS::SmartPerf::StartUpDelay sd;
    OHOS::SmartPerf::ParseClickCompleteTrace pcct;
    std::string cmdResult;
    SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
    std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "complete" + ".ftrace";
    std::thread thGetTrace = sd.ThreadGetTrace("complete", traceName);
    thGetTrace.join();
    float time = pcct.ParseCompleteTrace(traceName);
    return time;
}
float EditorCommand::ColdStart(std::vector<std::string> v)
{
    OHOS::SmartPerf::StartUpDelay sd;
    OHOS::SmartPerf::ParseTrace parseTrace;
    std::string cmdResult;
    int type = 4;
    int typePKG = 3;
    float noNameType = -1.0;
    SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.json", cmdResult);
    SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
    SPUtils::LoadCmd("uitest dumpLayout", cmdResult);
    sleep(1);
    size_t position = cmdResult.find(":");
    std::string pathJson = cmdResult.substr(position + 1);
    std::string deviceType = sd.GetDeviceType();
    sd.InitXY2(v[type], pathJson, v[typePKG]);
    if (sd.pointXY == "0 0") {
        return noNameType;
    } else {
        std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "coldStart" + ".ftrace";
        std::thread thGetTrace = sd.ThreadGetTrace("coldStart", traceName);
        std::string cmd = "uinput -T -d " + sd.pointXY + " -u " + sd.pointXY;
        sleep(1);
        SPUtils::LoadCmd(cmd, cmdResult);
        sleep(1);
        std::string topPkg = SPUtils::GetTopPkgName();
        std::string pid = sd.GetPidByPkg(v[typePKG]);
        thGetTrace.join();
        if (topPkg.find(v[typePKG]) == std::string::npos || pid == "") {
            return noNameType;
        }
        float time = 0.0;
        if (isOhTest) {
            time = parseTrace.ParseTraceCold(traceName, pid);
        } else {
            time = parseTrace.ParseTraceNoh(traceName, pid);
        }
        return time;
    }
}
float EditorCommand::HotStart(std::vector<std::string> v)
{
    OHOS::SmartPerf::StartUpDelay sd;
    OHOS::SmartPerf::ParseTrace parseTrace;
    std::string cmdResult;
    std::string deviceType = sd.GetDeviceType();
    if (isOhTest) {
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
        std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "hotStart" + ".ftrace";
        std::thread thGetTrace = sd.ThreadGetTrace("hotStart", traceName);
        thGetTrace.join();
        return parseTrace.ParseTraceHot(traceName);
    } else {
        int type = 4;
        int typePKG = 3;
        float noNameType = -1.0;
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.json", cmdResult);
        SPUtils::LoadCmd("rm -rfv /data/local/tmp/*.ftrace", cmdResult);
        SPUtils::LoadCmd("uitest dumpLayout", cmdResult);
        sleep(1);
        size_t position = cmdResult.find(":");
        std::string pathJson = cmdResult.substr(position + 1);
        sd.InitXY2(v[type], pathJson, v[typePKG]);
        if (sd.pointXY == "0 0") {
            return noNameType;
        } else {
            std::string cmd = "uinput -T -d " + sd.pointXY + " -u " + sd.pointXY;
            SPUtils::LoadCmd(cmd, cmdResult);
            sd.ChangeToBackground();
            std::string topPkgBefore = SPUtils::GetTopPkgName();
            if (topPkgBefore.find(v[typePKG]) != std::string::npos) {
                return noNameType;
            }
            std::string traceName = std::string("/data/local/tmp/") + std::string("sp_trace_") + "hotStart" + ".ftrace";
            std::thread thGetTrace = sd.ThreadGetTrace("hotStart", traceName);
            sleep(1);
            SPUtils::LoadCmd(cmd, cmdResult);
            sleep(1);
            std::string topPkg = SPUtils::GetTopPkgName();
            std::string pid = sd.GetPidByPkg(v[typePKG]);
            thGetTrace.join();
            if (topPkg.find(v[typePKG]) == std::string::npos || pid == "") {
                return noNameType;
            }
            return parseTrace.ParseTraceNoh(traceName, pid);
        }
    }
}
}
}

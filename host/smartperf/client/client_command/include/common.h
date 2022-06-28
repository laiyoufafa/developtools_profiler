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
#ifndef COMMON_H
#define COMMON_H
#include <unordered_map>
#include <string>
namespace OHOS {
namespace SmartPerf {
enum class MessageType {
    GetCpuNum,
    GetCpuFreq,
    GetCpuLoad,
    SetPkgName,
    SetProcessId,
    GetFpsAndJitters,
    GetGpuFreq,
    GetGpuLoad,
    GetDdrFreq,
    GetRamInfo,
    GetTemperature,
    GetPower,
    GetCapture,
    CatchTraceStart,
    CatchTraceFinish,
};

const std::unordered_map<MessageType, std::string> messageMap = {
    { MessageType::GetCpuNum, std::string("get_cpu_num") },
    { MessageType::GetCpuFreq, std::string("get_cpu_freq") },
    { MessageType::GetCpuLoad, std::string("get_cpu_load") },
    { MessageType::SetPkgName, std::string("set_pkgName") },
    { MessageType::SetProcessId, std::string("set_pid") },
    { MessageType::GetFpsAndJitters, std::string("get_fps_and_jitters") },
    { MessageType::GetGpuFreq, std::string("get_gpu_freq") },
    { MessageType::GetGpuLoad, std::string("get_gpu_load") },
    { MessageType::GetDdrFreq, std::string("get_ddr_freq") },
    { MessageType::GetRamInfo, std::string("get_ram_info") },
    { MessageType::GetTemperature, std::string("get_temperature") },
    { MessageType::GetPower, std::string("get_power") },
    { MessageType::GetCapture, std::string("get_capture") },
    { MessageType::CatchTraceStart, std::string("catch_trace_start") },
    { MessageType::CatchTraceFinish, std::string("catch_trace_finish") },
};

enum class CommandType {
    CT_N,
    CT_PKG,
    CT_PID,
    CT_OUT,
    CT_C,
    CT_G,
    CT_D,
    CT_F,
    CT_F1,
    CT_F2,
    CT_T,
    CT_P,
    CT_R,
    CT_TTRACE,
    CT_SNAPSHOT,
    CT_HW
};
enum class CommandHelp {
    HELP,
    VERSION
};

const std::unordered_map<std::string, CommandType> commandMap = {
    { std::string("-N"), CommandType::CT_N },          { std::string("-PKG"), CommandType::CT_PKG },
    { std::string("-PID"), CommandType::CT_PID },      { std::string("-OUT"), CommandType::CT_OUT },
    { std::string("-c"), CommandType::CT_C },          { std::string("-g"), CommandType::CT_G },
    { std::string("-f"), CommandType::CT_F },          { std::string("-f1"), CommandType::CT_F1 },
    { std::string("-f2"), CommandType::CT_F1 },        { std::string("-t"), CommandType::CT_T },
    { std::string("-p"), CommandType::CT_P },          { std::string("-r"), CommandType::CT_R },
    { std::string("-trace"), CommandType::CT_TTRACE }, { std::string("-snapshot"), CommandType::CT_SNAPSHOT },
    { std::string("-hw"), CommandType::CT_HW },        { std::string("-d"), CommandType::CT_D },
};

const std::unordered_map<CommandHelp, std::string> commandHelpMap = {
    { CommandHelp::HELP, std::string("--help") },
    { CommandHelp::VERSION, std::string("--version") },
};

enum class TraceStatus {
    TRACE_START,
    TRACE_FINISH,
    TRACE_NO
};
}
}
#endif
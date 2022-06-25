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
#include <iostream>
#include "include/CPU.h"
#include "include/DDR.h"
#include "include/GPU.h"
#include "include/FPS.h"
#include "include/RAM.h"
#include "include/Power.h"
#include "include/Temperature.h"
#include "include/sp_profiler_factory.h"
namespace OHOS {
namespace SmartPerf {
SpProfiler *SpProfilerFactory::getProfilerItem(MessageType messageType)
{
    SpProfiler *profiler = nullptr;
    switch (messageType) {
        case MessageType::GetCpuNum:
        case MessageType::GetCpuFreq:
        case MessageType::GetCpuLoad:
            profiler = &CPU::GetInstance();
            break;
        case MessageType::GetFpsAndJitters:
            profiler = &FPS::GetInstance();
            break;
        case MessageType::GetGpuFreq:
        case MessageType::GetGpuLoad:
            profiler = &GPU::GetInstance();
            break;
        case MessageType::GetDdrFreq:
            profiler = &DDR::GetInstance();
            break;
        case MessageType::GetRamInfo:
            profiler = &RAM::GetInstance();
            break;
        case MessageType::GetTemperature:
            profiler = &Temperature::GetInstance();
            break;
        case MessageType::GetPower:
            profiler = &Power::GetInstance();
            break;
        case MessageType::CatchTraceStart:
            FPS::GetInstance().setTraceCatch();
            break;
        case MessageType::GetCapture:
            FPS::GetInstance().setCaptureOn();
            break;    
        default:
            break;
    }
    return profiler;
}
void SpProfilerFactory::setProfilerPkg(std::string pkg)
{
    FPS &fps = FPS::GetInstance();
    fps.setPackageName(pkg);
}
void SpProfilerFactory::setProfilerPid(std::string pid)
{
    RAM &ram = RAM::GetInstance();
    ram.setProcessId(pid);
}
SpProfiler *SpProfilerFactory::getCmdProfilerItem(CommandType commandType)
{
    SpProfiler *profiler = nullptr;
    switch (commandType) {
        case CommandType::CT_C:
            profiler = &CPU::GetInstance();
            break;
        case CommandType::CT_G:
            profiler = &GPU::GetInstance();
            break;
        case CommandType::CT_F:
        case CommandType::CT_F1:
        case CommandType::CT_F2:
            profiler = &FPS::GetInstance();
            break;
        case CommandType::CT_D:
            profiler = &DDR::GetInstance();
            break;
        case CommandType::CT_P:
            profiler = &Power::GetInstance();
            break;
        case CommandType::CT_T:
            profiler = &Temperature::GetInstance();
            break;
        case CommandType::CT_R:
            profiler = &RAM::GetInstance();
            break;
        case CommandType::CT_TTRACE:
            FPS::GetInstance().setTraceCatch();
            break;
        case CommandType::CT_SNAPSHOT:
            FPS::GetInstance().setCaptureOn();
            break;        
        default:
            break;
    }
    return profiler;
}
}
}

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
#include <thread>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdio>
#include <sstream>
#include <iomanip>
#include "include/parse_click_complete_trace.h"
#include "include/sp_utils.h"
namespace OHOS {
    namespace SmartPerf {
        float ParseClickCompleteTrace::parse_click_complete_trace(std::string fileNamePath, std::string packageName)
        {
            std::string line;
            std::ifstream infile;
            std::string startTime = "0";
            std::string endTime = "0";
            std::string endTimeFlag = "0";
            std::string appPid = "0";
            std::string::size_type doComposition;
            int subNum = 5;
            float interval = 0.3;
            int CONVERSION = 1000;
            infile.open(fileNamePath);
            if (infile.fail())
            {
                std::cout << "File " << "open fail" << std::endl;
                return 0;
            }
            else 
            {
                while (getline(infile, line))
                {
                    appPid = SmartPerf::ParseClickCompleteTrace::getPid(line, "pid", appPid);
                    startTime = SmartPerf::ParseClickCompleteTrace::getStartTime(line, startTime);
                    doComposition = line.find("H:RSMainThread::DoComposition");
                    if (doComposition != std::string::npos)
                        {
                            int position1 = line.find("....");
                            int position2 = line.find(":");
                            endTime = line.substr(position1 + subNum, position2 - position1 - subNum);
                            if (std::stof(endTime) - std::stof(endTimeFlag) < interval)
                            {
                                endTimeFlag = endTime;
                            }else
                            {
                                    if (std::stof(endTimeFlag) != 0 && std::stof(startTime) != 0 && std::stof(endTime) - std::stof(startTime) > interval)
                                    {
                                        break;
                                    }else
                                    {
                                        endTimeFlag = endTime;
                                    }
                            }
                        }
                }
                completeTime = SmartPerf::ParseClickCompleteTrace::getTime(startTime, endTime);
            }
            infile.close();
            return completeTime * CONVERSION;
        }
        float  ParseClickCompleteTrace::getTime(std::string startTime, std::string endTime)
        {
                float displayTime = 0.032;
                float subNum = 2 ;
                int point = endTime.find(".");
                if (point != -1)
                {
                    endTime = endTime.substr(point - subNum);
                    startTime = startTime.substr(point - subNum);
                }
                if (std::stof(endTime) == 0 || std::stof(startTime) == 0)
                {
                }
                else
                {
                    completeTime = std::stof(endTime) - std::stof(startTime) + displayTime;
                }
                return completeTime;
        }
        std::string  ParseClickCompleteTrace::getPid(std::string line, std::string strPackgeName, std::string appPidBefore)
        {
            std::string::size_type positionPackgeName;
            std::string::size_type positionAppspawn;
            int subNum = 4;
            int packageNameNumSize = 5;
            std::string appPid;
            if(appPidnum == 0){
            if(strPackgeName.length() < packageNameNumSize)
            {
                    positionPackgeName = line.find("task_newtask: pid=");
                    positionAppspawn = line.find("comm=appspawn");
                    if(positionPackgeName != std::string::npos && positionAppspawn != std::string::npos )
                    {
                            int position1 = line.find("pid=");
                            int position2 = line.find(" comm=appspawn");
                            appPid = line.substr(position1 + subNum, position2 - position1 - subNum);
                            appPidnum++;
                    }else
                    {
                        appPid = appPidBefore;
                    }
            }else
            {
                    positionPackgeName = line.find(strPackgeName);
                    if (positionPackgeName != std::string::npos)
                    {
                        int position1 = line.find(strPackgeName);
                        int position2 = line.find(" prio");
                        appPid = line.substr(position1 + strPackgeName.length(), position2 - position1 - strPackgeName.length());
                        appPidnum++;
                    } else
                    {
                        appPid = appPidBefore;
                    }
            }
            }
            return appPid;
        }
        std::string  ParseClickCompleteTrace::getStartTime(std::string line, std::string startTimeBefore)
        {
            std::string::size_type mTouchEventDisPos;
            std::string::size_type touchEventDisPos;
            std::string startTime;
            int subNum = 5;
            int touchNum = 3;
            touchEventDisPos = line.find("H:touchEventDispatch");
            mTouchEventDisPos = line.find("H:TouchEventDispatch"); 
            if (mTouchEventDisPos != std::string::npos || touchEventDisPos != std::string::npos)
                {
                    if(flagTouch <= touchNum)
                    {
                    int position1 = line.find("....");
                    int position2 = line.find(":");
                    startTime = line.substr(position1 + subNum, position2 - position1 - subNum);
                    flagTime = "0";
                    flagTouch++;
                    }else
                    {
                        startTime = startTimeBefore;
                    }
                }
            return startTime;
        }
    }
}
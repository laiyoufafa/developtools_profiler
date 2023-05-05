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
#include "include/sp_parse_fps.h"
ParseFPS::ParseFPS()
{
    pattern = std::regex("(\\d+).(\\d{6})");
    pidPattern = std::regex("\\|(\\d+)\\|");
}
ParseFPS::~ParseFPS()
{
}
void ParseFPS::StrSplit(const SpString &content, const SpString &sp, std::vector<SpString> &out)
{
    size_t index = 0;
    while (index != SpString::npos) {
        size_t tEnd = content.find_first_of(sp, index);
        SpString tmp = content.substr(index, tEnd - index);
        if (tmp != "" && tmp != " ") {
            out.push_back(tmp);
        }
        if (tEnd == SpString::npos) {
            break;
        }
        index = tEnd + 1;
    }
}
void ParseFPS::GetAndSetPageType(Line& line1, PageType& pageType1)
{
    if (line1.empty()) {
        return;
    }  
    if (line1.find(ROSENRENDERWEB) != SpString::npos) {
        pageType1 = Web;
    } else if (line1.find(ROSENRENDERTEXTURE) != SpString::npos) {
        pageType1 = Video;
    } else {
        pageType1 = Large;
    }
}
unsigned int ParseFPS::GetTouchEventNum(Line& line1, TouchEvent& touchEvent1)
{
    if (line1.empty()) {
        return 0;
    }
    if (line1.find(TOUCHEVENT_FLAG) != SpString::npos || line1.find(HTOUCHEVENT_FLAG) != SpString::npos) {
        ++touchEvent1.tEventDisNum;
    }
    return touchEvent1.tEventDisNum;
}
const FpsResult ParseFPS::ParseBranch(FilePath& filePath, PackageName& packageName, PageType& pageType1, TouchEvent& touchEvent1)
{
    FpsResult fps = "0";
    if (touchEvent1.touchFlag) {
        std::vector<SpString> vecPackNames;
        //Get the time period in the renderservice
        float staticTime = 2.0f;
        this->StrSplit(packageName, ".", vecPackNames);
        SpString uiPoint = uniProcess + vecPackNames.back();
        switch (pageType1) {
            case PageType::Video:{
                if (filePath.find(FLING) != SpString::npos) {
                    staticTime = 0.5f;
                    fps =  PraseFPSTrace(filePath, staticTime, doPoint, uiPoint);
                } else {
                    fps =  PraseFPSTrace(filePath, staticTime, doPoint, videoPoint);
                }
                break;
            }
            case PageType::Web:{
                fps =  PraseFPSTrace(filePath, staticTime, doPoint, webPoint);
                break;
            }
            default:{
                fps =  PraseFPSTrace(filePath, staticTime, doPoint, uiPoint);
                break;
            }
        }
    }
    return fps;
}
FpsResult  ParseFPS::ParseTraceFile(FilePath& filePath, PackageName& packageName)
{
    if (filePath.empty() || packageName.empty()) {
        return PARAMS_EMPTY;
    }
    FpsResult fps;
    FileSteamPtr inFile(new std::ifstream());
    inFile->open(filePath);
    if (inFile->fail()) {
        std::cout<<"File: "<<filePath<<" open failed!"<<std::endl;
        return FILE_OPEN_FAILED;
    } else {
        while (std::getline(*inFile, line)) {
            if (this->GetTouchEventNum(line, touchEvent) > 0) {
                touchEvent.touchFlag = true;
            }
            this->GetAndSetPageType(line, pageType);
        }
        fps = this->ParseBranch(filePath, packageName, pageType, touchEvent);
    }
    return "FPS:"+fps+"fps";
}
void ParseFPS::StaticHandoffStartTime(Line& line1, RecordFpsVars& rfv)
{
    if (line1.empty())
        return;
    if (line1.find(TOUCHEVENT_FLAG) != SpString::npos || line1.find(HTOUCHEVENT_FLAG) != SpString::npos) {
        ++rfV.tEventDisNum;
        std::smatch result;
        int tNum = 4;
        if (tNum == rfV.tEventDisNum) {
            if (std::regex_search(line1, result, pattern)) {
                rfV.leaveStartTime = result[0];
            }
        }
        if (rfV.tEventDisNum == touchEvent.tEventDisNum) {
            if (std::regex_search(line1, result, pattern)) {
                rfV.isStaticsLeaveTime = true;
            }
        }
    }
}
void ParseFPS::DecHandOffTime(Line& line1, RecordFpsVars& rfv)
{
    if (line1.empty()) {
        return;
    }
    if (rfV.isStaticsLeaveTime) {
        if (this->line1.find(doPoint) != SpString::npos) {
            std::smatch result;
            if (std::regex_search(line1, result, pattern)) {
                if (0 == rfV.startFlag) {
                    rfV.leaveStartTime = rfV.leaveEndTime = result[0];
                }
                ++rfV.startFlag;
            }
            if (rfV.pidMatchStr.empty()) {
                if (std::regex_search(line1, result, pidPattern)) {
                    rfV.pidMatchStr = result[0];
                }
            }
            rfV.isAddFrame = true;
        }
    }
}
bool ParseFPS::CountRsEndTime(Line& line1, RecordFpsVars& rfv, float staticTime, SpString uiPoint)
{
    if (line1.empty()) {
        return false;
    }
    if (!rfV.pidMatchStr.empty() && rfV.isAddFrame) {
        SpString pid = rfV.pidMatchStr.substr(1, rfV.pidMatchStr.length() - 2);
        if (line1.find(uiPoint) != SpString::npos) {
            rfV.isHasUI = true;
        }
        if (line1.find("B|" + pid + "|") != SpString::npos && line1.find("-" + pid) != SpString::npos) {
            beQueue.push(line1);
        }
        if (line1.find("E|" + pid + "|") != SpString::npos && line1.find("-" + pid) != SpString::npos) {
            beQueue.pop();
        }
        if (beQueue.empty()) {
            rfV.isAddFrame = false;
            if (rfV.isHasUI) {
                rfV.isHasUI = false;
                if(std::stof(rfV.leaveEndTime) - std::stof(rfV.leaveStartTime) < staticTime) {
                    std::smatch result;
                    if (std::regex_search(line1, result, pattern)) {
                        float intervalTime = 0.1;
                        if (std::stof(result[0]) - std::stof(rfV.leaveEndTime) < intervalTime) {
                            ++rfV.frameNum;
                            rfV.leaveEndTime = result[0];
                        } else {
                            ++rfV.frameNum;
                            std::cout<<"NO."<<rfV.frameNum<<"fps Time: "<< std::stof(result[0]) - std::stof(rfV.leaveEndTime) << "s" <<std::endl;
                            rfV.leaveEndTime = result[0];
                        }
                    }
                } else {
                    return true;
                }
            }
        }
    }
    return false;
}
FpsResult  ParseFPS::PraseFPSTrace(FilePath& filePath, float staticTime, SpString uiPoint)
{
    if (!this->line.empty()) {
        this->line.clear();
    }
    FileSteamPtr inFile(new std::ifstream());
    inFile->open(filePath);
    if (inFile->fail()) {
        std::cout<<"File: "<<filePath<<" open failed!"<<std::endl;
        return FILE_OPEN_FAILED;
    } else {
        // std::cout<<"File: "<<filePath<<" open success!"<<std::endl;
        while (std::getline(*inFile, this->line)) {
            this->StaticHandoffStartTime(line, rfV);
            this->DecHandOffTime(line, rfV);
            if (this->CountRsEndTime(line, rfV, staticTime, uiPoint)) {
                break;
            }
        }
        const auto duration = std::stof(rfV.leaveEndTime) - std::stof(rfV.leaveStartTime);
        const auto complexFps1 =  rfV.frameNum / duration;
        SP_FAILED_OPERATION((duration > 0 && rfV.frameNum > 0));
        rfV.complexFps = std::to_string(complexFps1);
        int fpsNum = 60;
        if (complexFps1 > fpsNum) {
            rfV.complexFps = "60";
        }
    }
    return rfV.complexFps;
}
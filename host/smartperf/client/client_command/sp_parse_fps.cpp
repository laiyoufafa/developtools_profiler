#include "include/sp_parse_fps.h"
ParseFPS::ParseFPS ()
{
    pattern = std::regex("(\\d+).(\\d{6})");
    pidPattern = std::regex("\\|(\\d+)\\|");
}
ParseFPS::~ParseFPS ()
{
}
void ParseFPS::StrSplit (const SpString &content, const SpString &sp, std::vector<SpString> &out)
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
void ParseFPS::GetAndSetPageType (Line& line, PageType& pageType)
{
    if (line.empty())
        return;
    if (line.find(ROSENRENDERWEB) != SpString::npos) {
        pageType = Web;
    } else if(line.find(ROSENRENDERTEXTURE) != SpString::npos) {
        pageType = Video;
    } else {
        pageType = Large;
    }
}
unsigned int ParseFPS::GetTouchEventNum (Line& line, TouchEvent& touchEvent) {
    if (line.empty())
        return 0;
    if (line.find(TOUCHEVENT_FLAG) != SpString::npos || line.find(HTOUCHEVENT_FLAG) != SpString::npos) {
        ++touchEvent.tEventDisNum;
    }
    return touchEvent.tEventDisNum;
}
const FpsResult ParseFPS::ParseBranch (FilePath& filePath, PackageName& packageName, PageType& pageType, TouchEvent& touchEvent) {
    FpsResult fps = "0";
    if (touchEvent.touchFlag) {
        std::vector<SpString> vecPackNames;
        //Get the time period in the renderservice
        float staticTime = 2.0f;
        this->StrSplit(packageName, ".", vecPackNames);
        SpString uiPoint = uniProcess + vecPackNames.back();
        switch (pageType) {
            case PageType::Video:{
                if (filePath.find(FLING) != SpString::npos) {
                    staticTime = 0.5f;
                    fps =  parse_fps(filePath, staticTime, doPoint, uiPoint);
                } else {
                    fps =  parse_fps(filePath, staticTime, doPoint, videoPoint);
                }
                break;
            }
            case PageType::Web:{
                fps =  parse_fps(filePath, staticTime, doPoint, webPoint);
                break;
            }
            default:{
                fps =  parse_fps(filePath, staticTime, doPoint, uiPoint);
                break;
            }
        }
    }
    return fps;
}
FpsResult  ParseFPS::parse_tracefile(FilePath& filePath, PackageName& packageName)
{
    if (filePath.empty() || packageName.empty())
        return PARAMS_EMPTY;
    FpsResult fps;
    FileSteamPtr inFile(new std::ifstream());
    inFile->open(filePath);
    if (inFile->fail()) {
        std::cout<<"File: "<<filePath<<" open failed!"<<std::endl;
        return FILE_OPEN_FAILED;
    } else {
        // std::cout<<"File: "<<filePath<<" open success!"<<std::endl;
        while (std::getline(*inFile,line)) {
            if (this->GetTouchEventNum(line, touchEvent) > 0) {
                touchEvent.touchFlag = true;
            }
            this->GetAndSetPageType(line, pageType);
        }
        fps = this->ParseBranch(filePath, packageName, pageType, touchEvent);
    }
    return "FPS:"+fps+"fps";
}
void ParseFPS::StaticHandoffStartTime (Line& line,RecordFpsVars& rfv)
{
    if (line.empty())
        return;
    if (line.find(TOUCHEVENT_FLAG) != SpString::npos || line.find(HTOUCHEVENT_FLAG) != SpString::npos) {
        ++RFV.tEventDisNum;
        std::smatch result;
        if (4 == RFV.tEventDisNum) {
            if (std::regex_search(line, result, pattern)) {
                RFV.leaveStartTime = result[0];
            }
        }
        if (RFV.tEventDisNum == touchEvent.tEventDisNum) {
            if (std::regex_search(line, result, pattern)) {
                RFV.isStaticsLeaveTime = true;
            }
        }
    }
}
void ParseFPS::DecHandOffTime(Line& line, RecordFpsVars& rfv)
{
    if (line.empty())
        return;
    if (RFV.isStaticsLeaveTime) {
        if (this->line.find(doPoint) != SpString::npos) {
            std::smatch result;
            if (std::regex_search(line, result, pattern)) {
                if (0 == RFV.startFlag) {
                    RFV.leaveStartTime = RFV.leaveEndTime = result[0];
                }
                ++RFV.startFlag;
            }
            if (RFV.pidMatchStr.empty()) {
                if(std::regex_search(line, result, pidPattern)) {
                    RFV.pidMatchStr = result[0];
                }
            }
            RFV.isAddFrame = true;
        }
    }
}
bool ParseFPS::CountRsEndTime (Line& line, RecordFpsVars& rfv, float staticTime, SpString uiPoint)
{
    if (line.empty())
        return false;
    if (!RFV.pidMatchStr.empty() && RFV.isAddFrame) {
        SpString pid = RFV.pidMatchStr.substr(1, RFV.pidMatchStr.length() - 2);
        if (line.find(uiPoint) != SpString::npos)
            RFV.isHasUI = true;
        if (line.find("B|" + pid + "|") != SpString::npos && line.find("-" + pid) != SpString::npos)
            beQueue.push(line);
        if (line.find("E|" + pid + "|") != SpString::npos && line.find("-" + pid) != SpString::npos)
            beQueue.pop();
        if (beQueue.empty()) {
            RFV.isAddFrame = false;
            if(RFV.isHasUI) {
                RFV.isHasUI = false;
                if( std::stof(RFV.leaveEndTime) - std::stof(RFV.leaveStartTime) < staticTime) {
                    std::smatch result;
                    if (std::regex_search(line,result,pattern)) {
                        if (std::stof(result[0]) - std::stof(RFV.leaveEndTime) < 0.1) {
                            ++RFV.frameNum;
                            RFV.leaveEndTime = result[0];
                        } else {
                            return true;
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
FpsResult  ParseFPS::parse_fps (FilePath& filePath, float staticTime, SpString doPoint, SpString uiPoint) {
    if (!this->line.empty())
        this->line.clear();
    FileSteamPtr inFile(new std::ifstream());
    inFile->open(filePath);
    if (inFile->fail()) {
        std::cout<<"File: "<<filePath<<" open failed!"<<std::endl;
        return FILE_OPEN_FAILED;
    } else {
        // std::cout<<"File: "<<filePath<<" open success!"<<std::endl;
        while (std::getline(*inFile, this->line)) {
            this->StaticHandoffStartTime(line, RFV);
            this->DecHandOffTime(line, RFV);
            if(this->CountRsEndTime(line, RFV, staticTime, uiPoint)) {
                break;
            }
        }
        const auto duration = std::stof(RFV.leaveEndTime) - std::stof(RFV.leaveStartTime);
        const auto complexFps =  RFV.frameNum / duration;
        SP_ASSERT((duration > 0 && RFV.frameNum > 0));
        RFV.ComplexFps = std::to_string(complexFps);
        if(complexFps > 60){
            RFV.ComplexFps = "60"; 
        }
    }
    return RFV.ComplexFps;
}
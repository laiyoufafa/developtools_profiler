#include <thread>
#include <ios>
#include <vector>
#include <iostream>
#include <fstream>
#include <regex>
#include "include/startup_delay.h"
#include "include/sp_utils.h"
namespace OHOS {
namespace SmartPerf {
StartUpDelay::StartUpDelay(){}
StartUpDelay::~StartUpDelay(){}
void StartUpDelay::GetTrace(std::string sessionId, std::string traceName){
    std::string result;
    SPUtils::LoadCmd("bytrace -t 5 -b 20480 --overwrite idle ace app ohos ability graphic sched freq irq sync workq pagecache binder multimodalinput > " + traceName,result);
}

std::thread StartUpDelay::ThreadGetTrace(std::string sessionId, std::string traceName){
    std::thread thGetTrace(&StartUpDelay::GetTrace, this, sessionId, traceName);
    return thGetTrace;
}
void StartUpDelay::GetLayout(){
    std::string result;
    SPUtils::LoadCmd("uitest dumpLayout", result);
}

std::thread StartUpDelay::ThreadGetLayout(){
    std::thread thGetLayout(&StartUpDelay::GetLayout, this);
    return thGetLayout;
}

void StartUpDelay::ChangeToBackground(){
    std::string result;
    SPUtils::LoadCmd("uinput -k -d 2 -u 2", result);
}

std::vector<std::string> StartUpDelay::GetPidByPkg(std::string curPkgName){
    std::string resultPids;
    SPUtils::LoadCmd("pidof" + curPkgName, resultPids);
    std::vector<std::string> pidV;
    SPUtils::StrSplit(resultPids, " ", pidV);
    return pidV;
}

void StartUpDelay::KillCurApp(std::string curPkgName){
    std::vector<std::string> pidV;
    do
    {
        pidV = GetPidByPkg(curPkgName);
        std::string result;
        for(std::string pid : pidV){
            SPUtils::LoadCmd("Kill -9 " + pid, result);
        }
    }while (pidV.size() > 0);

}

void StartUpDelay::InitXY2(std::string curAppName,std::string fileName) {

    std::ifstream file(fileName, std::ios:: in);
    std::string strLine = "";
    std::regex pattern("\\d+");
    while (getline(file, strLine)) {
        size_t appIndex = strLine.find(curAppName);
        if (appIndex > 0) {
            size_t bounds = strLine.rfind("bounds", appIndex);
                if (bounds > 0) {
                    std::string boundStr = strLine.substr(bounds, 30);
                    std::cout << "boundStr:-->" << boundStr;
                    std::smatch result;
                    std::string::const_iterator iterStart = boundStr.begin();
                    std::string::const_iterator iterEnd = boundStr.end();
                    std::vector<std::string> pointVector;
                    while(std::regex_search(iterStart, iterEnd, result, pattern)) {
                        std::string startX = result[0];
                        iterStart = result[0].second;
                        pointVector.push_back(startX);
                    }

                    if (pointVector.size() > 3) {
                        int x = (std::atoi(pointVector[2].c_str()) + std::atoi(pointVector[0].c_str())) / 2;
                        int y = (std::atoi(pointVector[3].c_str()) + std::atoi(pointVector[1].c_str())) / 2;
                        pointXY = std::to_string(x) + " " + std::to_string(y);
                    } 
                    else {
                        size_t leftStart = boundStr.find_first_of("[");
                        size_t leftEnd = boundStr.find_first_of("]");
                        pointXY = boundStr.substr(leftStart + 1, leftEnd - leftStart - 1);
                        pointXY = pointXY.replace(pointXY.find(","), 1, " ");
                    }
                    break;
                }
            }
        }
}
}
}

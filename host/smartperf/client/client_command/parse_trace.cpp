#include <thread>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdio>
#include <sstream>
#include <iomanip>
#include "include/parse_trace.h"
#include "include/sp_utils.h"

namespace OHOS
{
    namespace SmartPerf 
    {
        float ParseTrace::parse_trace_cold(std::string fileNamePath, std::string packageName)
         {
            float code = -1;
            code = SmartPerf::ParseTrace::parse_codeTrace(fileNamePath);
            return code * 1000;
        }  
        float ParseTrace::parse_trace_hot(std::string fileNamePath, std::string packageName)
         {
            float code = -1;
            code = SmartPerf::ParseTrace::parse_hotTrace(fileNamePath);
            return code * 1000;
        }
        float ParseTrace::parse_codeTrace(std::string fileNamePath) 
        {
            std::string line;
            std::ifstream infile;
            std::string startTime = "0";
            std::string endTime = "0";
            std::string endTimeFlag = "0";
            std::string appPid = "0";
            float interval=0.3;
            infile.open(fileNamePath);
            std::string::size_type tracingMarkWrite;
            int subNum = 5;   
            if (infile.fail()) 
            {
                std::cout << "File " << "open fail" << std::endl;
                return 0;
            }
            else {
                while (getline(infile, line)) 
                {         
                    appPid=SmartPerf::ParseTrace::getPid(line, "pid",appPid);
                    startTime=SmartPerf::ParseTrace::getStartTime(line,startTime);              
                    tracingMarkWrite=line.find("tracing_mark_write: E|"+appPid);
                    if (tracingMarkWrite != std::string::npos)
                        {      
                            int position1 = line.find("....");
                            int position2 = line.find(":");                 
                            endTime = line.substr(position1 + subNum, position2 - position1 - subNum);
                              if (std::stof(endTime)-std::stof(endTimeFlag)< interval)
                            {
                                endTimeFlag=endTime;
                            }else
                            {
                                 if (std::stof(endTimeFlag)!=0 && std::stof(startTime)!=0 && std::stof(endTime)-std::stof(startTime) > interval)
                                    {
                                          break;
                                    }else{
                                        endTimeFlag=endTime;
                                    }       
                            }
                        }   
                }
                codeTime=SmartPerf::ParseTrace::getTime(startTime,endTime);
            }
            infile.close();
            return codeTime;
        }
        float ParseTrace::parse_hotTrace(std::string fileNamePath)
         {
            std::string line;
            std::ifstream infile;
            std::string startTime = "0";
            std::string endTime = "0";
            std::string endTimeFlag = "0";
            std::string appPid = "0";
            std::string::size_type doComposition;
            int subNum = 5;
            float interval=0.3;
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
                    appPid=SmartPerf::ParseTrace::getPid(line, "pid",appPid);
                    startTime=SmartPerf::ParseTrace::getStartTime(line,startTime);
                 
                     doComposition=line.find("H:RSMainThread::DoComposition");
                    if (doComposition != std::string::npos)
                        {      
                            int position1 = line.find("....");
                            int position2 = line.find(":");                 
                            endTime = line.substr(position1 + subNum, position2 - position1 - subNum);
                              if (std::stof(endTime)-std::stof(endTimeFlag)< interval)
                            {
                                endTimeFlag=endTime;
                            }else{
                                if (std::stof(endTimeFlag)==0)
                                {
                                      endTimeFlag=endTime;
                                }else{
                                    break;
                                }
                            }
                        }                      
                }
                codeTime=SmartPerf::ParseTrace::getTime(startTime,endTime);
            }
            infile.close();
            return codeTime;
        }
        float  ParseTrace::getTime(std::string startTime, std::string endTime)
        {
                float displayTime=0.040;
                float subNum=2;
                int point = endTime.find(".");
                if (point != -1) {
                    endTime = endTime.substr(point - subNum);
                    startTime = startTime.substr(point - subNum);
                }
                if (std::stof(endTime) == 0 || std::stof(startTime) == 0) {            
                }
                else {
                    codeTime = std::stof(endTime) - std::stof(startTime) + displayTime;
                }
                return codeTime;
        }
        std::string  ParseTrace::getPid(std::string line, std::string strPackgeName,std::string appPid)
        {
            std::string::size_type positionPackgeName;
            std::string::size_type positionAppspawn;
            int subNum=4;
            int packageNameNumSize=5;
            if(appPidnum==0){
            if(strPackgeName.length()<packageNameNumSize)
            {
                    positionPackgeName = line.find("task_newtask: pid=");
                    positionAppspawn = line.find("comm=appspawn");
                    if(positionPackgeName != std::string::npos && positionAppspawn != std::string::npos ) 
                    {
                            int position1 = line.find("pid=");
                            int position2 = line.find(" comm=appspawn");
                            appPid = line.substr(position1 + subNum, position2 - position1 - subNum);
                            appPidnum++;
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
                    }
            }
            }
            return appPid;
        }
        std::string  ParseTrace::getStartTime(std::string line,std::string startTime)
        {
            std::string::size_type mTouchEventDisPos;
            std::string::size_type touchEventDisPos;
            int subNum = 5;
            int touchNum=3;
            touchEventDisPos = line.find("H:touchEventDispatch");
            mTouchEventDisPos = line.find("H:TouchEventDispatch");         
            if (mTouchEventDisPos != std::string::npos || touchEventDisPos != std::string::npos)
                {
                    if(flagTouch<=touchNum){
                    int position1 = line.find("....");
                    int position2 = line.find(":");
                    startTime = line.substr(position1 + subNum, position2 - position1 - subNum);
                    flagTime = "0";
                    flagTouch++;
                    }
                }
            return startTime;
        }       
    }
}
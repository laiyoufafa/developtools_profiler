
#include <iostream>
#include <fstream>
#include <string>
#include <vector>

namespace OHOS
{
    namespace SmartPerf
    {

        class ParseClickCompleteTrace
        {
            public:
                float parse_click_complete_trace(std::string fileNamePath, std::string packageName);
                float getTime(std::string startTime, std::string endTime);
                std::string getPid(std::string line, std::string strPackgeName,std::string appPid);
                std::string getStartTime(std::string line, std::string startTime);
            private:
                std::string line;
                std::ifstream infile;
                 std::string flagTime="0";
                 int flagTouch=0;
                int appPidnum = 0;//标记取appid次数
                float completeTime = -1;
        };
    }
}
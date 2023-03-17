
#include <iostream>
#include <fstream>
#include <string>
#include <vector>

namespace OHOS {
namespace SmartPerf {
class ParseTrace {
public:
    float parse_trace_cold(std::string fileNamePath, std::string packageName);
    float parse_trace_hot(std::string fileNamePath, std::string packageName);
    float parse_codeTrace(std::string fileNamePath);  
    float parse_hotTrace(std::string fileNamePath);
    float getTime(std::string startTime, std::string endTime);
    std::string getPid(std::string line, std::string strPackgeName,std::string appPid);
    std::string getStartTime(std::string line, std::string startTime);
 
private:
    std::string line;
    std::ifstream infile;
    std::string flagTime="0";
    int flagTouch=0;
    int appPidnum = 0;//标记取appid次数
    float codeTime = -1;
};
}
}
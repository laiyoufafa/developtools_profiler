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
#ifndef SP_THREAD_SOCKET_H
#define SP_THREAD_SOCKET_H
#include "sp_profiler_factory.h"
#include "sp_server_socket.h"
#include "sp_utils.h"
namespace OHOS {
namespace SmartPerf {
class SpThreadSocket {
public:
    std::string MapToString(std::map<std::string, std::string> dataMap) const
    {
        std::string result;
        int i = 0;
        std::string splitStr = "";
        for (auto iter = dataMap.cbegin(); iter != dataMap.cend(); ++iter) {
            printf("%s = %s\n", iter->first.c_str(), iter->second.c_str());
            if (i > 0) {
                splitStr = "$$";
            }
            result += splitStr + iter->first.c_str() + "||" + iter->second.c_str();
            i++;
        }
        return result;
    }
    std::string ResPkgOrPid(const SpServerSocket &spSocket) const
    {
        std::vector<std::string> sps;
        SPUtils::StrSplit(spSocket.RecvBuf(), "::", sps);
        return sps[1];
    }

    void Process() const
    {
        SpServerSocket spSocket;
        spSocket.Init();
        while (1) {
            spSocket.Recvfrom();
            HandleMsg(spSocket);
        }
        std::cout << "Socket Process finished!" << std::endl;
        spSocket.Close();
    }
    void HandleMsg(SpServerSocket &spSocket) const
    {
        auto iterator = messageMap.begin();
        while (iterator != messageMap.end()) {
            if (SPUtils::IsSubString(spSocket.RecvBuf(), iterator->second)) {
                SpProfiler *profiler = SpProfilerFactory::GetProfilerItem(iterator->first);
                if (profiler == nullptr && (iterator->first == MessageType::SET_PKG_NAME)) {
                    std::string curPkgName = ResPkgOrPid(spSocket);
                    SpProfilerFactory::SetProfilerPkg(curPkgName);
                    std::string pidCmd = "pidof " + curPkgName;
                    std::string pidResult;
                    if (SPUtils::LoadCmd(pidCmd, pidResult)) {
                        SpProfilerFactory::SetProfilerPid(pidResult);
                    }
                    spSocket.Sendto(curPkgName);
                } else if (profiler == nullptr && (iterator->first == MessageType::SET_PROCESS_ID)) {
                    SpProfilerFactory::SetProfilerPid(ResPkgOrPid(spSocket));
                } else if (profiler == nullptr) {
                    std::string returnStr = iterator->second;
                    spSocket.Sendto(returnStr);
                } else {
                    std::map<std::string, std::string> data = profiler->ItemData();
                    std::string sendData = MapToString(data);
                    spSocket.Sendto(sendData);
                }
                break;
            }
            ++iterator;
        }
    }
};
}
}
#endif
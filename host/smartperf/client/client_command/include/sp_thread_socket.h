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
    std::string mapToString(std::map<std::string, std::string> dataMap)
    {
        std::string result;
        std::map<std::string, std::string>::iterator iter;
        int i = 0;
        std::string splitStr = "";
        for (iter = dataMap.begin(); iter != dataMap.end(); ++iter) {
            printf("%s = %s\n", iter->first.c_str(), iter->second.c_str());
            if (i > 0) {
                splitStr = "$$";
            }
            result += splitStr + iter->first.c_str() + "||" + iter->second.c_str();
            i++;
        }
        return result;
    }
    std::string resPkgOrPid(SpServerSocket *spSocket)
    {
        std::vector<std::string> sps;
        SPUtils::StrSplit(spSocket->RecvBuf(), "::", sps);
        return sps[1];
    }

    void Process()
    {
        std::string clientPkg = "";
        std::string clientPid = "";
        SpServerSocket *spSocket = new SpServerSocket();
        spSocket->Init();
        while (true) {
            spSocket->Recvfrom();
            handleMsg(spSocket);
        }
        std::cout << "Socket Process finished!" << std::endl;
        spSocket->Close();
    }
    void handleMsg(SpServerSocket *spSocket)
    {
        auto iterator = messageMap.begin();
        while (iterator != messageMap.end()) {
            if (SPUtils::IsSubString(spSocket->RecvBuf(), iterator->second)) {
                SpProfiler *profiler = SpProfilerFactory::getProfilerItem(iterator->first);
                if (profiler == nullptr && (iterator->first == MessageType::SetPkgName)) {
                    std::string curPkgName = resPkgOrPid(spSocket);
                    SpProfilerFactory::setProfilerPkg(curPkgName);
                    std::string pidCmd = "pidof " + curPkgName;
                    std::string pidResult;
                    if (SPUtils::LoadCmd(pidCmd, pidResult)) {
                        SpProfilerFactory::setProfilerPid(pidResult);
                    }
                } else if (profiler == nullptr && (iterator->first == MessageType::SetProcessId)) {
                    SpProfilerFactory::setProfilerPid(resPkgOrPid(spSocket));
                } else {
                    std::map<std::string, std::string> data = profiler->ItemData();
                    std::string sendData = mapToString(data);
                    spSocket->Sendto(sendData);
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
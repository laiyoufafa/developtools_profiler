/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "hidebug.h"

#include <stdlib.h>
#include <vector>

#include <hilog/log.h>
#include <parameter.h>
#include <sysparam_errno.h>

namespace OHOS {
namespace HiviewDFX{
static constexpr HiLogLabel LABEL = { LOG_CORE, 0xD002D0B, "HiDebug_Native" };
static constexpr char SPACE_CHR = ' ';
static constexpr char EQUAL_CHR = '=';

bool HiDebug::InitEnvironmentParam(std::string serviceName)
{
    const int PARAM_SIZE = 64;
    char paramOutBuf[PARAM_SIZE] = { 0 };
    std::string defStrValue;
    std::string paramValue(defStrValue);
    int retLen = GetParameter(serviceName.c_str(), defStrValue.c_str(), paramOutBuf, PARAM_SIZE);
    if (retLen > 0) {
        paramOutBuf[retLen] = '\0';
        paramValue.assign(paramOutBuf, retLen);
        HiLog::Debug(LABEL, "GetParameter successfully, param is %{public}s.", paramValue.c_str());
        std::map<std::string, std::string> params = this->ParseParam(paramValue);
        std::map<std::string, std::string>::iterator iter;
        for (iter = params.begin(); iter != params.end(); ++iter) {
            setenv(iter->first.c_str(), iter->second.c_str(), 1);
        }
        return true;
    }
    return false;
}

std::map<std::string, std::string> HiDebug::ParseParam(std::string paramStr)
{
    bool hasKey = false;
    bool hasValue = false;
    bool hasEql = false;
    std::string keyStr;
    std::string valueStr;
    std::map<std::string, std::string> params;
    int startIdx = 0;
    int cnt = 0;
    for (int i = 0; i < paramStr.size(); ++i) {
        if (paramStr[i] == SPACE_CHR) {
            if (hasKey) {
                keyStr = paramStr.substr(startIdx, cnt);
                hasKey = false;
            }
            if (hasValue) {
                valueStr = paramStr.substr(startIdx, cnt);
                params[keyStr] = valueStr;
                hasValue = false;
                hasEql = false;
            }
            cnt = 0;
            startIdx = i + 1;
        } else if (paramStr[i] == EQUAL_CHR) {
            if (hasKey) {
                keyStr = paramStr.substr(startIdx, cnt);
                hasKey = false;
            }
            hasEql = true;
            cnt = 0;
            startIdx = i + 1;
        } else {
            cnt++;
            hasEql ? hasValue = true : hasKey = true;
        }
    }
    if (hasValue) {
        params[keyStr] = paramStr.substr(startIdx, cnt);
    }
    return params;
}
} // HiviewDFX
} // OHOS
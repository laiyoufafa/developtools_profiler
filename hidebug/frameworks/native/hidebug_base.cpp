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

#include "hidebug_base.h"

#include <cerrno>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <inttypes.h>
#include <parameter.h>
#include <sysparam_errno.h>

#include "hilog/log.h"
#include "securec.h"

#undef LOG_DOMAIN
#undef LOG_TAG
#define LOG_DOMAIN 0xD002D0A
#define LOG_TAG "HiDebug_Native"

namespace {
const int MAX_PARA_LEN = 50;
const int MAX_PARA_CNT = 20;
const int PARAM_BUF_LEN = 128;
const int QUERYNAME_LEN = 80;

struct Params {
    char key[MAX_PARA_LEN];
    char value[MAX_PARA_LEN];
} params[MAX_PARA_CNT];

int GetKeyValue(const char *input)
{
    char key[MAX_PARA_LEN] = { 0 };
    char value[MAX_PARA_LEN] = { 0 };
    uint32_t len = 0;
    int cnt = 0;
    errno_t err = 0;
    while (sscanf(input, "%*[ ]%[^:]:%19s%n", key, value, &len) == 2) {
        err = strcpy_s(params[cnt].key, sizeof(params[cnt].key), key);
        if (err != 0) {
            HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
            break;
        }
        err = strcpy_s(params[cnt].value, sizeof(params[cnt].value), value);
        if (err != 0) {
            HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
            break;
        }
        input += len;
        cnt++;
    }
    return cnt;
}
}

bool InitEnvironmentParam(const char *serviceName)
{
    char paramOutBuf[PARAM_BUF_LEN];
    char defStrValue[PARAM_BUF_LEN];
    char queryName[QUERYNAME_LEN] = "hiviewdfx.debugenv.";
    errno_t err = strcat_s(queryName, sizeof(queryName), serviceName);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strcat_s failed.");
        return false;
    }
    int retLen = GetParameter(queryName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
    paramOutBuf[retLen] = '\0';
    int cnt = GetKeyValue(paramOutBuf);
    if (cnt < 1) {
        char persistName[QUERYNAME_LEN] = "persist.hiviewdfx.debugenv.";
        err = strcat_s(persistName, sizeof(persistName), serviceName);
        if (err != EOK) {
            HILOG_ERROR(LOG_CORE, "strcat_s failed.");
            return false;
        }
        retLen = GetParameter(persistName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
        paramOutBuf[retLen] = '\0';
        if (GetKeyValue(paramOutBuf) < 1) {
            HILOG_ERROR(LOG_CORE, "failed to capture environment params.");
            return false;
        }
    }
    for (int i = 0; i < cnt; ++i) {
        setenv(params[i].key, params[i].value, 1);
        if (errno != 0) {
            HILOG_ERROR(LOG_CORE, "setenv failed, errno = %{public}d.", errno);
        }
    }
    return true;
}

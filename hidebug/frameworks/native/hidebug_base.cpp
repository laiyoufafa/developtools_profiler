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
#include <cinttypes>
#include <cstdio>
#include <cstdlib>
#include <cstring>

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
const int FORMAT_NUM = 2;

struct Params {
    char key[MAX_PARA_LEN];
    char value[MAX_PARA_LEN];
} g_params[MAX_PARA_CNT];

int g_paramCnt = 0;

void GetKeyValue(const char *input)
{
    char key[MAX_PARA_LEN] = { 0 };
    char value[MAX_PARA_LEN] = { 0 };
    uint32_t len = 0;
    errno_t err = 0;
    while (sscanf(input, "%[^:]:%49s%n", key, value, &len) == FORMAT_NUM) {
        err = strcpy_s(g_params[g_paramCnt].key, sizeof(g_params[g_paramCnt].key), key);
        if (err != 0) {
            HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
            break;
        }
        err = strcpy_s(g_params[g_paramCnt].value, sizeof(g_params[g_paramCnt].value), value);
        if (err != 0) {
            HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
            break;
        }
        input += len;
        g_paramCnt++;
    }
}

void SplitParams(char *input)
{
    g_paramCnt = 0;
    const char space[] = " ";
    char *param;
    char *next = nullptr;
    param = strtok_s(input, space, &next);
    while (param != nullptr) {
        GetKeyValue(param);
        param = strtok_s(nullptr, space, &next);
    }
}
}

bool InitEnvironmentParam(const char *serviceName)
{
    char paramOutBuf[PARAM_BUF_LEN] = { 0 };
    char defStrValue[PARAM_BUF_LEN] = { 0 };
    char queryName[QUERYNAME_LEN] = "hiviewdfx.debugenv.";
    errno_t err = strcat_s(queryName, sizeof(queryName), serviceName);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strcat_s failed.");
        return false;
    }
    int retLen = GetParameter(queryName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
    paramOutBuf[retLen] = '\0';
    SplitParams(paramOutBuf);
    if (g_paramCnt < 1) {
        char persistName[QUERYNAME_LEN] = "persist.hiviewdfx.debugenv.";
        err = strcat_s(persistName, sizeof(persistName), serviceName);
        if (err != EOK) {
            HILOG_ERROR(LOG_CORE, "strcat_s failed.");
            return false;
        }
        retLen = GetParameter(persistName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
        paramOutBuf[retLen] = '\0';
        SplitParams(paramOutBuf);
        if (g_paramCnt < 1) {
            HILOG_ERROR(LOG_CORE, "failed to capture environment params.");
            return false;
        }
    }
    for (int i = 0; i < g_paramCnt; ++i) {
        setenv(g_params[i].key, g_params[i].value, 1);
        if (errno != 0) {
            HILOG_ERROR(LOG_CORE, "setenv failed, errno = %{public}d.", errno);
        }
    }
    return true;
}
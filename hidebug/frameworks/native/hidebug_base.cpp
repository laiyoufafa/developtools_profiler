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
const char COLON_CHR = ':';

struct Params {
    char key[MAX_PARA_LEN];
    char value[MAX_PARA_LEN];
} g_params[MAX_PARA_CNT];

int g_paramCnt = 0;

void ParseKeyValue(const char *input)
{
    if (g_paramCnt >= MAX_PARA_CNT) {
        HILOG_ERROR(LOG_CORE, "Parameters is Full.");
        return;
    }
    const char *colonPos = strchr(input, COLON_CHR);
    if (colonPos == nullptr) {
        HILOG_ERROR(LOG_CORE, "params is illegal.");
        return;
    }
    errno_t err = strncpy_s(g_params[g_paramCnt].key, MAX_PARA_LEN, input, colonPos - input);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s copy key strings failed.");
        return;
    }
    err = strncpy_s(g_params[g_paramCnt].value, MAX_PARA_LEN, colonPos + 1, strlen(colonPos + 1));
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "memcpy_s copy value strings failed.");
        return;
    }
    g_paramCnt++;
}

void SplitParams(char *input)
{
    g_paramCnt = 0;
    const char space[] = " ";
    char *param;
    char *next = nullptr;
    param = strtok_s(input, space, &next);
    while (param != nullptr) {
        ParseKeyValue(param);
        param = strtok_s(nullptr, space, &next);
    }
}

int QueryParams(const char *queryName)
{
    g_paramCnt = 0;
    char paramOutBuf[PARAM_BUF_LEN] = { 0 };
    char defStrValue[PARAM_BUF_LEN] = { 0 };
    int retLen = GetParameter(queryName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
    if (retLen == 0) {
        HILOG_ERROR(LOG_CORE, "get %{public}s parameters failed.", queryName);
        return 0;
    }
    paramOutBuf[retLen] = '\0';
    SplitParams(paramOutBuf);
    return g_paramCnt;
}
}

bool InitEnvironmentParam(const char *serviceName)
{
    if (serviceName == nullptr) {
        HILOG_ERROR(LOG_CORE, "input service name is null.");
        return false;
    }
    errno_t err = 0;
    char persistName[QUERYNAME_LEN] = "persist.hiviewdfx.debugenv.";
    char onceName[QUERYNAME_LEN] = "hiviewdfx.debugenv.";
    err = strcat_s(onceName, sizeof(onceName), serviceName);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strcat_s query name failed.");
        return 0;
    }
    err = strcat_s(persistName, sizeof(persistName), serviceName);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strcat_s persist query name failed.");
        return 0;
    }
    if (QueryParams(onceName) == 0 && QueryParams(persistName) == 0) {
        HILOG_ERROR(LOG_CORE, "failed to capture %{public}s environment params.", serviceName);
        return false;
    }
    for (int i = 0; i < g_paramCnt; ++i) {
        if (setenv(g_params[i].key, g_params[i].value, 1) != 0) { // 1 : overwrite
            HILOG_ERROR(LOG_CORE, "setenv failed, errno = %{public}d.", errno);
        }
    }
    return true;
}

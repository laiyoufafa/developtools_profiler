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

#include <parameter.h>
#include <sysparam_errno.h>

#include "hilog/log.h"
#include "securec.h"

#undef LOG_DOMAIN
#undef LOG_TAG
#define LOG_DOMAIN 0xD002D0A
#define LOG_TAG "HiDebug_Native"

namespace {
const char SPACE_CHR = ' ';
const char COLON_CHR = ':';
const int PARAM_BUF_LEN = 128;
const int QUERYNAME_LEN = 80;

struct Params {
    char key[MAX_PARA_LEN];
    char value[MAX_PARA_LEN];
} params[MAX_PARA_CNT];

int ParseParams(const char *input)
{
    bool hasKey = false;
    bool hasValue = false;
    bool hasEql = false;
    char key[MAX_PARA_LEN];
    char value[MAX_PARA_LEN];
    int startIdx = 0;
    int cnt = 0;
    int paramCnt = 0;
    errno_t err = 0;
    for (size_t i = 0; i < strlen(input); ++i) {
        if (input[i] == SPACE_CHR) {
            if (hasKey) {
                err = strncpy_s(key, MAX_PARA_LEN, input + startIdx, cnt);
                if (err != EOK) {
                    HILOG_ERROR(LOG_CORE, "strncpy_s failed.");
                    return 0;
                }
                key[cnt] = '\0';
                hasKey = false;
            }
            if (hasValue) {
                err = strncpy_s(value, MAX_PARA_LEN, input + startIdx, cnt);
                if (err != EOK) {
                    HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
                    return 0;
                }
                value[cnt] = '\0';
                err = strcpy_s(params[paramCnt].key, MAX_PARA_LEN, key);
                if (err != EOK) {
                    HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
                    return 0;
                }
                err = strcpy_s(params[paramCnt].value, MAX_PARA_LEN, value);
                if (err != EOK) {
                    HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
                    return 0;
                }
                paramCnt++;
                hasValue = false;
                hasEql = false;
            }
            cnt = 0;
            startIdx = i + 1;
        } else if (input[i] == COLON_CHR) {
            if (hasKey) {
                err = strncpy_s(key, MAX_PARA_LEN, input + startIdx, cnt);
                if (err != EOK) {
                    HILOG_ERROR(LOG_CORE, "strncpy_s failed.");
                    return 0;
                }
                key[cnt] = '\0';
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
        err = strncpy_s(value, MAX_PARA_LEN, input + startIdx, cnt);
        if (err != EOK) {
            HILOG_ERROR(LOG_CORE, "strncpy_s failed.");
            return 0;
        }
        value[cnt] = '\0';
        err = strcpy_s(params[paramCnt].key, MAX_PARA_LEN, key);
        if (err != EOK) {
            HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
            return 0;
        }
        err = strcpy_s(params[paramCnt].value, MAX_PARA_LEN, value);
        if (err != EOK) {
            HILOG_ERROR(LOG_CORE, "strcpy_s failed.");
            return 0;
        }
        paramCnt++;
    }
    return paramCnt;
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
    int cnt = ParseParams(paramOutBuf);
    if (cnt < 1) {
        char persistName[] = "persist.hiviewdfx.debugenv.";
        err = strcat_s(persistName, sizeof(queryName), serviceName);
        if (err != EOK) {
            HILOG_ERROR(LOG_CORE, "strcat_s failed.");
            return false;
        }
        retLen = GetParameter(persistName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
        paramOutBuf[retLen] = '\0';
        if (ParseParams(paramOutBuf) < 1) {
            HILOG_ERROR(LOG_CORE, "failed to capture environment params.");
            return false;
        }
    }
    for (int i = 0; i < cnt; ++i) {
        setenv(params[i].key, params[i].value, 1);
    }
    return true;
}

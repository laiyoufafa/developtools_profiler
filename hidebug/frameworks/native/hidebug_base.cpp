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
#include <climits>
#include <dlfcn.h>
#include <unistd.h>
#include <string>
#include <signal.h>

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
const char SLASH_CHR = '/';
const char * const LIBC_HOOK_PARAM = "libc.hook_mode";

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
        HILOG_ERROR(LOG_CORE, "strncpy_s copy key strings failed.");
        return;
    }
    err = strncpy_s(g_params[g_paramCnt].value, MAX_PARA_LEN, colonPos + 1, strlen(colonPos + 1));
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strncpy_s copy value strings failed.");
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
        return 0;
    }
    paramOutBuf[retLen] = '\0';
    SplitParams(paramOutBuf);
    return g_paramCnt;
}

const char* FilterServiceName(const char *inputName)
{
    const char *ret = strrchr(inputName, SLASH_CHR);
    if (ret == nullptr) {
        return inputName;
    }
    return ret + 1;
}

static int GetMallocHookStartupValue(const char *param, char *path, int size)
{
    if (path == nullptr || size <= 0) {
        return -1;
    }

    const char *ptr = param;
    const char *posColon = nullptr;

    while (*ptr && *ptr != ':') {
        ++ptr;
    }
    if (*ptr == ':') {
        posColon = ptr;
        ++ptr;
    }
    const int paramLength = 7;
    if (strncmp(param, "startup", paramLength) == 0) {
        if (*ptr == '\"') {
            ++ptr;
            int idx = 0;
            while (idx < size - 1 && *ptr && *ptr != '\"') {
                path[idx++] = *ptr++;
            }
            path[idx] = '\0';
        } else {
            int idx = 0;
            while (idx < size - 1 && *ptr) {
                path[idx++] = *ptr++;
            }
            path[idx] = '\0';
        }
    }
    return 0;
}

static bool MatchMallocHookStartupProp(const char *thisName)
{
    char paramOutBuf[PARAM_BUF_LEN] = { 0 };
    char defStrValue[PARAM_BUF_LEN] = { 0 };
    char targetProcName[PARAM_BUF_LEN] = { 0 };

    int retLen = GetParameter(LIBC_HOOK_PARAM, defStrValue, paramOutBuf, PARAM_BUF_LEN);
    if (retLen == 0) {
        return false;
    }
    const int paramLength = 8;
    if (strncmp(paramOutBuf, "startup:", paramLength) != 0) {
        return false;
    }
    retLen = GetMallocHookStartupValue(paramOutBuf, targetProcName, PARAM_BUF_LEN);
    if (retLen == -1) {
        HILOG_ERROR(LOG_CORE, "malloc hook parse startup value failed");
        return false;
    }
    if (strncmp(thisName, targetProcName, strlen(targetProcName) + 1) != 0) {
        return false;
    }
    if (strncmp(targetProcName, "init", strlen(targetProcName) + 1) == 0 ||
        strncmp(targetProcName, "appspawn", strlen(targetProcName) + 1) == 0) {
        HILOG_INFO(LOG_CORE, "malloc hook: this target proc '%{public}s' no hook", targetProcName);
        return false;
    }
    char *programName = (char *)calloc(PARAM_BUF_LEN, sizeof(char));
    if (programName == nullptr) {
        return false;
    }
    readlink("/proc/self/exe", programName, PARAM_BUF_LEN - 1);
    const char *fileName = programName;
    const char *posLastSlash = strrchr(programName, '/');
    if (posLastSlash != nullptr) {
        fileName = posLastSlash + 1;
    }
    bool res = false;
    if (strncmp(fileName, "init", strlen(fileName) + 1) == 0 ||
        strncmp(fileName, "appspawn", strlen(fileName) + 1) == 0) {
        res = true;
    }
    free(programName);
    return res;
}

static int SetupMallocHookAtStartup(const char *thisName)
{
    if (!MatchMallocHookStartupProp(thisName)) {
        return 0;
    }
    HILOG_INFO(LOG_CORE, "malloc send hook signal.");
    return raise(MUSL_SIGNAL_HOOK);
}
} // namespace

bool InitEnvironmentParam(const char *inputName)
{
    if (inputName == nullptr) {
        HILOG_ERROR(LOG_CORE, "input service name is null.");
        return false;
    }
    const char *serviceName = FilterServiceName(inputName);
    if (*serviceName == '\0') {
        HILOG_ERROR(LOG_CORE, "input service name is illegal.");
        return false;
    }
    errno_t err = 0;
    char persistName[QUERYNAME_LEN] = "persist.hiviewdfx.debugenv.";
    char onceName[QUERYNAME_LEN] = "hiviewdfx.debugenv.";
    err = strcat_s(onceName, sizeof(onceName), serviceName);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strcat_s query name failed.");
        return false;
    }
    err = strcat_s(persistName, sizeof(persistName), serviceName);
    if (err != EOK) {
        HILOG_ERROR(LOG_CORE, "strcat_s persist query name failed.");
        return false;
    }

#ifdef HAS_MUSL_STARTUP_MALLOC_HOOK_INTF
    setup_malloc_hook_mode();
#else
    SetupMallocHookAtStartup(serviceName);
#endif
    if (QueryParams(onceName) == 0 && QueryParams(persistName) == 0) {
        return false;
    }
    for (int i = 0; i < g_paramCnt; ++i) {
        if (setenv(g_params[i].key, g_params[i].value, 1) != 0) { // 1 : overwrite
            HILOG_ERROR(LOG_CORE, "setenv failed, errno = %{public}d.", errno);
        }
    }
    return true;
}

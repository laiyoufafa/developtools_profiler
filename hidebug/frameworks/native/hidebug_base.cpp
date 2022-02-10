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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <parameter.h>
#include <sysparam_errno.h>
#include <errno.h>

#include "hilog/log.h"

#undef LOG_DOMAIN
#undef LOG_TAG
#define LOG_DOMAIN 0xD002D0A
#define LOG_TAG "HiDebug_Native"

const char SPACE_CHR = ' ';
const char COLON_CHR = ':';
const int PARAM_BUF_LEN = 128;

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
	for (size_t i = 0; i < strlen(input); ++i) {
		if (input[i] == SPACE_CHR) {
			if (hasKey) {
				strncpy(key, input + startIdx, cnt);
				key[cnt] = '\0';
				hasKey = false;
			}
			if (hasValue) {
				strncpy(value, input + startIdx, cnt);
				value[cnt] = '\0';
				strcpy(params[paramCnt].key, key);
				strcpy(params[paramCnt].value, value);
				paramCnt++;
				hasValue = false;
				hasEql = false;
			}
			cnt = 0;
			startIdx = i + 1;
		} else if (input[i] == COLON_CHR) {
			if (hasKey) {
				strncpy(key, input + startIdx, cnt);
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
		strncpy(value, input + startIdx, cnt);
		value[cnt] = '\0';
		strcpy(params[paramCnt].key, key);
		strcpy(params[paramCnt].value, value);
		paramCnt++;
	}
	return paramCnt;
}

bool InitEnvironmentParam(const char *serviceName)
{
	HILOG_INFO(LOG_CORE, "enter InitEnvironmentParam Func.");
    char paramOutBuf[PARAM_BUF_LEN];
    char defStrValue[PARAM_BUF_LEN];
	char queryName[] = "hiviewdfx.debugenv.";
	strcat(queryName, serviceName);
    int retLen = GetParameter(queryName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
	paramOutBuf[retLen] = '\0';
	int cnt = ParseParams(paramOutBuf);
	printf("once query: queryName = %s, paramStr = %s, retLen = %d, cnt = %d.\n", queryName, paramOutBuf, retLen, cnt);
	if (cnt < 1) {
		char persistName[] = "persist.hiviewdfx.debugenv.";
		strcat(persistName, serviceName);
		retLen = GetParameter(persistName, defStrValue, paramOutBuf, PARAM_BUF_LEN);
		paramOutBuf[retLen] = '\0';
		cnt = ParseParams(paramOutBuf);
		printf("persist query: persistName = %s, paramStr = %s, retLen = %d, cnt = %d.\n", persistName, paramOutBuf, retLen, cnt);
	}
    if (cnt > 0) {
		HILOG_ERROR(LOG_CORE, "GET parameter successfully.");
		for (int i = 0; i < cnt; ++i) {
			errno = 0;
			setenv(params[i].key, params[i].value, 1);
			HILOG_INFO(LOG_CORE, "setenv errno = %{public}d.", errno);
			printf("setenv: key = %s, value = %s, errno = %d.\n", params[i].key, params[i].value, errno);
		}
        return true;
    }
	HILOG_ERROR(LOG_CORE, "failed to capture environment params.");
	printf("query failed.\n");
    return false;
}
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

#ifndef NAPI_HIDEBUG_H
#define NAPI_HIDEBUG_H

#include <string>

#include "napi/native_api.h"
#include "napi/native_node_api.h"

namespace OHOS {
namespace HiviewDFX {
static napi_value StartProfiling(napi_env env, napi_callback_info info);
static napi_value StopProfiling(napi_env env, napi_callback_info info);
static napi_value DumpHeapData(napi_env env, napi_callback_info info);
static napi_value GetPss(napi_env env, napi_callback_info info);
static napi_value GetSharedDirty(napi_env env, napi_callback_info info);
static napi_value GetPrivateDirty(napi_env env, napi_callback_info info);
static napi_value GetCpuUsage(napi_env env, napi_callback_info info);
static napi_value GetNativeHeapSize(napi_env env, napi_callback_info info);
static napi_value GetNativeHeapAllocatedSize(napi_env env, napi_callback_info info);
static napi_value GetNativeHeapFreeSize(napi_env env, napi_callback_info info);
static napi_value GetServiceDump(napi_env env, napi_callback_info info);

static napi_value CreateUndefined(napi_env env);
static napi_value CreateErrorMessage(napi_env env, std::string msg);
static bool MatchValueType(napi_env env, napi_value value, napi_valuetype targetType);
static std::string GetFileNameParam(napi_env env, napi_callback_info info);
static uint32_t GetServiceAbilityIdParam(napi_env env, napi_callback_info info);
static std::string GetLocalTimeStr();
static std::string SetDumpFilePath(uint32_t serviceAbilityId);
static bool CreateFile(const std::string &path);
} // HiviewDFX
} // OHOS
#endif // NAPI_HIDEBUG_H
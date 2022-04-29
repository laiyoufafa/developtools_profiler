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

#include "napi_hidebug.h"

#include <cerrno>
#include <fstream>
#include <string>
#include <memory>

#include <malloc.h>
#include "bundle_manager_helper.h"
#include "directory_ex.h"
#include "file_ex.h"
#include "file_util.h"
#include "hilog/log.h"
#include "ipc_skeleton.h"
#include "native_engine/native_engine.h"
#include "securec.h"
#include "unistd.h"
#include "dump_usage.h"

namespace OHOS {
namespace HiviewDFX {
namespace {
constexpr HiLogLabel LABEL = { LOG_CORE, 0xD002D0A, "HiDebug_NAPI" };
constexpr int ONE_VALUE_LIMIT = 1;
constexpr int ARRAY_INDEX_FIRST = 0;
constexpr int BUF_MAX = 128;
const std::string BASE_PATH = "/data/accounts/account_0/appdata/";
const std::string SUB_DIR = "/files/";
const std::string DEFAULT_FILENAME = "undefined";
const std::string JSON_FILE = ".json";
const std::string HEAPSNAPSHOT_FILE = ".heapsnapshot";
}

napi_value StartProfiling(napi_env env, napi_callback_info info)
{
    std::string fileName = GetFileNameParam(env, info);
    int callingUid = IPCSkeleton::GetCallingUid();
    std::string bundleName;
    if (!GetBundleNameByUid(callingUid, bundleName)) {
        return CreateErrorMessage(env, "search bundle name failed.");
    }
    std::string filePath = BASE_PATH + bundleName + SUB_DIR + fileName + JSON_FILE;
    if (!FileUtil::IsLegalPath(filePath)) {
        return CreateErrorMessage(env, "input fileName is illegal.");
    }
    if (!FileUtil::CreateFile(filePath)) {
        return CreateErrorMessage(env, "file created failed.");
    }
    NativeEngine *engine = reinterpret_cast<NativeEngine*>(env);
    engine->StartCpuProfiler(filePath);
    return CreateUndefined(env);
}

napi_value StopProfiling(napi_env env, napi_callback_info info)
{
    NativeEngine *engine = reinterpret_cast<NativeEngine*>(env);
    engine->StopCpuProfiler();
    return CreateUndefined(env);
}

napi_value DumpHeapData(napi_env env, napi_callback_info info)
{
    std::string fileName = GetFileNameParam(env, info);
    int callingUid = IPCSkeleton::GetCallingUid();
    std::string bundleName;
    if (!GetBundleNameByUid(callingUid, bundleName)) {
        return CreateErrorMessage(env, "search bundle name failed.");
    }
    std::string filePath = BASE_PATH + bundleName + SUB_DIR + fileName + HEAPSNAPSHOT_FILE;
    HiLog::Debug(LABEL, "filePath is %{public}s.", filePath.c_str());
    if (!FileUtil::IsLegalPath(filePath)) {
        return CreateErrorMessage(env, "input fileName is illegal.");
    }
    if (!FileUtil::CreateFile(filePath)) {
        return CreateErrorMessage(env, "file created failed.");
    }
    NativeEngine *engine = reinterpret_cast<NativeEngine*>(env);
    engine->DumpHeapSnapshot(filePath);
    return CreateUndefined(env);
}

napi_value CreateUndefined(napi_env env)
{
    napi_value res = nullptr;
    napi_get_undefined(env, &res);
    return res;
}

napi_value CreateErrorMessage(napi_env env, std::string msg)
{
    napi_value result = nullptr;
    napi_value message = nullptr;
    napi_create_string_utf8(env, (char *)msg.data(), msg.size(), &message);
    napi_create_error(env, nullptr, message, &result);
    return result;
}

static napi_value GetPss(napi_env env, napi_callback_info info)
{
    napi_value pss;
    std::unique_ptr<DumpUsage> dumpUsage = std::make_unique<DumpUsage>();
    if (dumpUsage) {
        int pid = getpid();
        uint64_t pssInfo = dumpUsage->GetPss(pid);
        napi_create_bigint_uint64(env, pssInfo, &pss);
    } else {
        napi_create_bigint_uint64(env, 0, &pss);
    }
    return pss;
}

static napi_value GetSharedDirty(napi_env env, napi_callback_info info)
{
    napi_value share_dirty;
    std::unique_ptr<DumpUsage> dumpUsage = std::make_unique<DumpUsage>();
    if (dumpUsage) {
        int pid = getpid();
        uint64_t shareDirtyInfo = dumpUsage->GetSharedDirty(pid);
        napi_create_bigint_uint64(env, shareDirtyInfo, &share_dirty);
    } else {
        napi_create_bigint_uint64(env, 0, &share_dirty);
    }
    return share_dirty;
}

static napi_value GetNativeHeapSize(napi_env env, napi_callback_info info)
{
    struct mallinfo mi = mallinfo();
    napi_value native_heap_size;
    if (mi.usmblks >= 0) {
        napi_create_bigint_uint64(env, mi.usmblks, &native_heap_size);
    } else {
        napi_create_bigint_uint64(env, 0, &native_heap_size);
    }
    return native_heap_size;
}

static napi_value GetNativeHeapAllocatedSize(napi_env env, napi_callback_info info)
{
    struct mallinfo mi = mallinfo();
    napi_value native_heap_allocated_size;
    if (mi.uordblks >= 0) {
        napi_create_bigint_uint64(env, mi.uordblks, &native_heap_allocated_size);
    } else {
        napi_create_bigint_uint64(env, 0, &native_heap_allocated_size);
    }
    return native_heap_allocated_size;
}

static napi_value GetNativeHeapFreeSize(napi_env env, napi_callback_info info)
{
    struct mallinfo mi = mallinfo();
    napi_value native_heap_free_size;
    if (mi.fordblks >= 0) {
        napi_create_bigint_uint64(env, mi.fordblks, &native_heap_free_size);
    } else {
        napi_create_bigint_uint64(env, 0, &native_heap_free_size);
    }
    return native_heap_free_size;
}

bool MatchValueType(napi_env env, napi_value value, napi_valuetype targetType)
{
    napi_valuetype valueType = napi_undefined;
    napi_typeof(env, value, &valueType);
    return valueType == targetType;
}

std::string GetFileNameParam(napi_env env, napi_callback_info info)
{
    size_t argc = ONE_VALUE_LIMIT;
    napi_value argv[ONE_VALUE_LIMIT] = { nullptr };
    napi_value thisVar = nullptr;
    void *data = nullptr;
    napi_get_cb_info(env, info, &argc, argv, &thisVar, &data);
    if (argc != ONE_VALUE_LIMIT) {
        HiLog::Error(LABEL, "invalid number = %{public}d of params.", ONE_VALUE_LIMIT);
        return DEFAULT_FILENAME;
    }
    if (!MatchValueType(env, argv[ARRAY_INDEX_FIRST], napi_string)) {
        HiLog::Error(LABEL, "Type error, should be string type!");
        return DEFAULT_FILENAME;
    }
    size_t bufLen = 0;
    napi_status status = napi_get_value_string_utf8(env, argv[0], NULL, 0, &bufLen);
    if (status != napi_ok) {
        HiLog::Error(LABEL, "Get input filename param length failed.");
        return DEFAULT_FILENAME;
    }
    if (bufLen > BUF_MAX || bufLen == 0) {
        HiLog::Error(LABEL, "input filename param length is illegal.");
        return DEFAULT_FILENAME;
    }
    char buf[bufLen + 1];
    napi_get_value_string_utf8(env, argv[0], buf, bufLen + 1, &bufLen);
    std::string fileName = buf;
    return fileName;
}

bool GetBundleNameByUid(std::int32_t uid, std::string& bname)
{
    std::shared_ptr<EventFwk::BundleManagerHelper> bundleManager = EventFwk::BundleManagerHelper::GetInstance();
    if (bundleManager == nullptr) {
        HiLog::Error(LABEL, "get BundleManagerHelper instance failed.");
        return false;
    }
    bname = bundleManager->GetBundleName(uid);
    return true;
}

napi_value DeclareHiDebugInterface(napi_env env, napi_value exports)
{
    napi_property_descriptor desc[] = {
        DECLARE_NAPI_FUNCTION("startProfiling", StartProfiling),
        DECLARE_NAPI_FUNCTION("stopProfiling", StopProfiling),
        DECLARE_NAPI_FUNCTION("dumpHeapData", DumpHeapData),
        DECLARE_NAPI_FUNCTION("getPss", GetPss),
        DECLARE_NAPI_FUNCTION("getSharedDirty", GetSharedDirty),
        DECLARE_NAPI_FUNCTION("getNativeHeapSize", GetNativeHeapSize),
        DECLARE_NAPI_FUNCTION("getNativeHeapAllocatedSize", GetNativeHeapAllocatedSize),
        DECLARE_NAPI_FUNCTION("getNativeHeapFreeSize", GetNativeHeapFreeSize)
    };
    NAPI_CALL(env, napi_define_properties(env, exports, sizeof(desc) / sizeof(desc[0]), desc));
    return exports;
}

static napi_module hidebugModule = {
    .nm_version = 1,
    .nm_flags = 0,
    .nm_filename = nullptr,
    .nm_register_func = HiviewDFX::DeclareHiDebugInterface,
    .nm_modname = "napi_hidebug",
    .nm_priv = ((void *)0),
    .reserved = {0}
};

extern "C" __attribute__((constructor)) void HiDebugRegisterModule(void)
{
    napi_module_register(&hidebugModule);
}
} // HiviewDFX
} // OHOS
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
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <ctime>
#include <iservice_registry.h>
#include <malloc.h>

#include "context.h"
#include "directory_ex.h"
#include "dump_usage.h"
#include "file_ex.h"
#include "file_util.h"
#include "hilog/log.h"
#include "native_engine/native_engine.h"
#include "securec.h"
#include "unistd.h"

namespace OHOS {
namespace HiviewDFX {
namespace {
constexpr HiLogLabel LABEL = { LOG_CORE, 0xD002D00, "HiDebug_NAPI" };
constexpr int ONE_VALUE_LIMIT = 1;
constexpr int ARRAY_INDEX_FIRST = 0;
constexpr int BUF_MAX = 128;
constexpr mode_t DEFAULT_MODE = S_IRUSR | S_IWUSR | S_IRGRP; // -rw-r-----
const std::string PROC_PATH = "/proc/";
const std::string ROOT_DIR = "/root";
const std::string SLASH_STR = "/";
const std::string DEFAULT_FILENAME = "undefined";
const std::string JSON_FILE = ".json";
const std::string HEAPSNAPSHOT_FILE = ".heapsnapshot";
}

napi_value StartProfiling(napi_env env, napi_callback_info info)
{
    std::string fileName = GetFileNameParam(env, info);
    auto context = OHOS::AbilityRuntime::Context::GetApplicationContext();
    if (context == nullptr) {
        return CreateErrorMessage(env, "Get ApplicationContext failed.");
    }
    std::string filesDir = context->GetFilesDir();
    if (filesDir.empty()) {
        return CreateErrorMessage(env, "Get App files dir failed.");
    }
    std::string filePath = PROC_PATH + std::to_string(getpid()) + ROOT_DIR + filesDir + SLASH_STR +
        fileName + JSON_FILE;
    if (!FileUtil::IsLegalPath(filePath)) {
        return CreateErrorMessage(env, "input fileName is illegal.");
    }
    if (!CreateFile(filePath)) {
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
    auto context = OHOS::AbilityRuntime::Context::GetApplicationContext();
    if (context == nullptr) {
        return CreateErrorMessage(env, "Get ApplicationContext failed.");
    }
    std::string filesDir = context->GetFilesDir();
    if (filesDir.empty()) {
        return CreateErrorMessage(env, "Get App files dir failed.");
    }
    std::string filePath = PROC_PATH + std::to_string(getpid()) + ROOT_DIR + filesDir + SLASH_STR +
        fileName + HEAPSNAPSHOT_FILE;
    if (!FileUtil::IsLegalPath(filePath)) {
        return CreateErrorMessage(env, "input fileName is illegal.");
    }
    if (!CreateFile(filePath)) {
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
    napi_value shareDirty;
    std::unique_ptr<DumpUsage> dumpUsage = std::make_unique<DumpUsage>();
    if (dumpUsage) {
        int pid = getpid();
        uint64_t shareDirtyInfo = dumpUsage->GetSharedDirty(pid);
        napi_create_bigint_uint64(env, shareDirtyInfo, &shareDirty);
    } else {
        napi_create_bigint_uint64(env, 0, &shareDirty);
    }
    return shareDirty;
}

static napi_value GetPrivateDirty(napi_env env, napi_callback_info info)
{
    napi_value privateDirtyValue;
    std::unique_ptr<DumpUsage> dumpUsage = std::make_unique<DumpUsage>();
    if (dumpUsage) {
        pid_t pid = getpid();
        uint64_t privateDirty = dumpUsage->GetPrivateDirty(pid);
        napi_create_bigint_uint64(env, privateDirty, &privateDirtyValue);
    } else {
        napi_create_bigint_uint64(env, 0, &privateDirtyValue);
    }
    return privateDirtyValue;
}

static napi_value GetCpuUsage(napi_env env, napi_callback_info info)
{
    napi_value cpuUsageValue;
    std::unique_ptr<DumpUsage> dumpUsage = std::make_unique<DumpUsage>();
    if (dumpUsage) {
        pid_t pid = getpid();
        float tmpCpuUsage = dumpUsage->GetCpuUsage(pid);
        double cpuUsage = double(tmpCpuUsage);
        napi_create_double(env, cpuUsage, &cpuUsageValue);
    } else {
        napi_create_double(env, 0, &cpuUsageValue);
    }
    return cpuUsageValue;
}

static napi_value GetNativeHeapSize(napi_env env, napi_callback_info info)
{
    struct mallinfo mi = mallinfo();
    napi_value nativeHeapSize;
    if (mi.usmblks >= 0) {
        napi_create_bigint_uint64(env, mi.usmblks, &nativeHeapSize);
    } else {
        napi_create_bigint_uint64(env, 0, &nativeHeapSize);
    }
    return nativeHeapSize;
}

static napi_value GetServiceDump(napi_env env, napi_callback_info info)
{
    napi_value errorStr;
    napi_value successStr;
    uint32_t serviceAbilityId = 0;
    serviceAbilityId = GetServiceAbilityIdParam(env, info);
    if (serviceAbilityId == 0) {
        HiLog::Error(LABEL, "invalid param.");
        std::string errorInfo = "Error: invalid param";
        napi_create_string_utf8(env, errorInfo.c_str(), NAPI_AUTO_LENGTH, &errorStr);
        return errorStr;
    }
    sptr<ISystemAbilityManager> sam = SystemAbilityManagerClient::GetInstance().GetSystemAbilityManager();
    if (!sam) {
        std::string errorInfo = "Error: get system ability manager failed";
        napi_create_string_utf8(env, errorInfo.c_str(), NAPI_AUTO_LENGTH, &errorStr);
        return errorStr;
    }
    sptr<IRemoteObject> sa = sam->CheckSystemAbility(serviceAbilityId);
    if (!sa) {
        HiLog::Error(LABEL, "no such system ability for ability id %{public}d!", serviceAbilityId);
        std::string errorInfo = "Error: no such system ability service.";
        napi_create_string_utf8(env, errorInfo.c_str(), NAPI_AUTO_LENGTH, &errorStr);
        return errorStr;
    }
    std::string dumpFilePath = SetDumpFilePath(serviceAbilityId);
    if (dumpFilePath == "") {
        std::string errorInfo = "Error: create dump file path failed";
        napi_create_string_utf8(env, errorInfo.c_str(), NAPI_AUTO_LENGTH, &errorStr);
        return errorStr;
    }
    int fd = open(dumpFilePath.c_str(), O_RDWR | O_APPEND | O_CREAT, 0644);
    if (fd == -1) {
        std::string errorInfo = "Error: open filepath failed, filepath: " + dumpFilePath;
        napi_create_string_utf8(env, errorInfo.c_str(), NAPI_AUTO_LENGTH, &errorStr);
        close(fd);
        return errorStr;
    }
    std::vector<std::u16string> args;
    int dumpResult = sa->Dump(fd, args);
    HiLog::Info(LABEL, "dump result returned by sa id %{public}d", dumpResult);
    close(fd);
    std::string successInfo = "Success: " + dumpFilePath;
    napi_create_string_utf8(env, successInfo.c_str(), NAPI_AUTO_LENGTH, &successStr);
    return successStr;
}

static std::string SetDumpFilePath(uint32_t serviceAbilityId)
{
    auto context = OHOS::AbilityRuntime::Context::GetApplicationContext();
    if (context == nullptr) {
        HiLog::Error(LABEL, "ApplicationContext is null.");
        return "";
    }
    std::string filesDir = context->GetFilesDir();
    if (filesDir.empty()) {
        HiLog::Error(LABEL, "The files dir obtained from context is empty.");
        return "";
    }
    std::string timeStr = GetLocalTimeStr();
    std::string dumpFilePath = PROC_PATH + std::to_string(getpid()) + ROOT_DIR + filesDir + SLASH_STR +
        "service_" + std::to_string(serviceAbilityId) + "_" + timeStr + ".dump";
    if (!FileUtil::IsLegalPath(dumpFilePath)) {
        HiLog::Error(LABEL, "dumpFilePath is not legal.");
        return "";
    }
    if (!CreateFile(dumpFilePath)) {
        HiLog::Error(LABEL, "dumpFilePath create failed.");
        return "";
    }
    return dumpFilePath;
}

static bool CreateFile(const std::string &path)
{
    if (FileUtil::FileExists(path)) {
        HiLog::Error(LABEL, "file existed.");
        return false;
    }
    int fd = creat(path.c_str(), DEFAULT_MODE);
    if (fd == -1) {
        HiLog::Error(LABEL, "file create failed, errno = %{public}d", errno);
        return false;
    } else {
        close(fd);
        return true;
    }
}

static napi_value GetNativeHeapAllocatedSize(napi_env env, napi_callback_info info)
{
    struct mallinfo mi = mallinfo();
    napi_value nativeHeapAllocatedSize;
    if (mi.uordblks >= 0) {
        napi_create_bigint_uint64(env, mi.uordblks, &nativeHeapAllocatedSize);
    } else {
        napi_create_bigint_uint64(env, 0, &nativeHeapAllocatedSize);
    }
    return nativeHeapAllocatedSize;
}

static napi_value GetNativeHeapFreeSize(napi_env env, napi_callback_info info)
{
    struct mallinfo mi = mallinfo();
    napi_value nativeHeapFreeSize;
    if (mi.fordblks >= 0) {
        napi_create_bigint_uint64(env, mi.fordblks, &nativeHeapFreeSize);
    } else {
        napi_create_bigint_uint64(env, 0, &nativeHeapFreeSize);
    }
    return nativeHeapFreeSize;
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

static std::string GetLocalTimeStr()
{
    time_t timep;
    (void)time(&timep);
    char tmp[128] = {0};
    struct tm* localTime = localtime(&timep);
    if (!localTime) {
        HiLog::Error(LABEL, "get local time error.");
        return "0";
    }
    (void)strftime(tmp, sizeof(tmp), "%Y%m%d_%H%M%S", localTime);
    std::string timeStr = tmp;
    return timeStr;
}

static uint32_t GetServiceAbilityIdParam(napi_env env, napi_callback_info info)
{
    size_t argc = ONE_VALUE_LIMIT;
    napi_value argv[ONE_VALUE_LIMIT] = { nullptr };
    napi_value thisVar = nullptr;
    void *data = nullptr;
    napi_get_cb_info(env, info, &argc, argv, &thisVar, &data);
    if (argc != ONE_VALUE_LIMIT) {
        HiLog::Error(LABEL, "invalid number = %{public}d of params.", ONE_VALUE_LIMIT);
        return 0;
    }
    if (!MatchValueType(env, argv[ARRAY_INDEX_FIRST], napi_number)) {
        HiLog::Error(LABEL, "Type error, should be number type!");
        return 0;
    }
    uint32_t serviceAbilityId = 0;
    napi_status status = napi_get_value_uint32(env, argv[0], &serviceAbilityId);
    if (status != napi_ok) {
        HiLog::Error(LABEL, "Get input serviceAbilityId failed.");
        return 0;
    }
    return serviceAbilityId;
}

napi_value DeclareHiDebugInterface(napi_env env, napi_value exports)
{
    napi_property_descriptor desc[] = {
        DECLARE_NAPI_FUNCTION("startProfiling", StartProfiling),
        DECLARE_NAPI_FUNCTION("stopProfiling", StopProfiling),
        DECLARE_NAPI_FUNCTION("dumpHeapData", DumpHeapData),
        DECLARE_NAPI_FUNCTION("getPss", GetPss),
        DECLARE_NAPI_FUNCTION("getSharedDirty", GetSharedDirty),
        DECLARE_NAPI_FUNCTION("getPrivateDirty", GetPrivateDirty),
        DECLARE_NAPI_FUNCTION("getCpuUsage", GetCpuUsage),
        DECLARE_NAPI_FUNCTION("getServiceDump", GetServiceDump),
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
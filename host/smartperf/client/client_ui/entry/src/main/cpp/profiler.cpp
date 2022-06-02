/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
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
#include <iostream>
#include <cstdio>
#include <string>
#include <vector>
#include <thread>
#include <future>
#include <climits>
#include <cmath>
#include <node_api.h>
#include <js_native_api.h>
#include "FPS.h"
#include "RAM.h"
#include "napi/native_api.h"
#include "gp_utils.h"

namespace {
    void collectFpsThread(std::promise<FpsInfo> &promiseObj) {
        FpsInfo fpsInfo = FPS::getInstance()->getFpsInfo();
        promiseObj.set_value(fpsInfo);
    }
}


static napi_value getFpsData(napi_env env, napi_callback_info info)
{
    size_t argc = 1;
    napi_value args[1] = { nullptr };
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    napi_valuetype valuetype0;
    napi_typeof(env, args[0], &valuetype0);
    char pkgName[64] = {0};
    size_t typeLen = 0;
    napi_get_value_string_utf8(env, args[0], pkgName, sizeof(pkgname) - 1, &typeLen);
    FPS::getInstance()->setPackageName(pkgName);

    std::promise<FpsInfo> promiseObj;
    std::future<FpsInfo> futureObj = promiseObj.get_future();
    std::thread tFps(collectFpsThread, ref(promiseObj));
    tFps.join();

    FpsInfo fpsInfo = futureObj.get();
    std::string fps = std::to_string(fpsInfo.fps);
    std::vector<long long> fpsJitters = fpsInfo.jitters;
    std::string fps_str = fps + "|";
    for (int i = 0; i < fpsJitters.size(); ++i) {
        fps_str += std::to_string(fpsJitters[i]);
        fps_str += "==";
    }
    napi_value fps_result;
    napi_create_string_utf8(env, fps_str.c_str(), fps_str.size(), &fps_result);
    return fps_result;
}

static napi_value getRamData(napi_env env, napi_callback_info info)
{
    size_t argc = 1;
    napi_value args[1] = { nullptr };
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    napi_valuetype valuetype0;
    napi_typeof(env, args[0], &valuetype0);
    char pidNumber[64] = {0};
    size_t typeLen = 0;
    napi_get_value_string_utf8(env, args[0], pidNumber, sizeof(pkgname) - 1, &typeLen);
    std::map<std::string, std::string> gramInfo = RAM::getInstance()->getRamInfo(pidNumber);
    std::string ram_pss = gramInfo["pss"];
    napi_value ram_result;
    napi_create_string_utf8(env, ram_pss.c_str(), ram_pss.size(), &ram_result);
    return ram_result;
}

static napi_value checkDaemon(napi_env env, napi_callback_info info)
{
    std::string status = "Dead";
    std::string spRunning = gpUtils::readCmd(std::string("ps -ef |grep SP_daemon |grep -v grep"));
    if (spRunning.find("NA")!= std::string::npos) {
        gpUtils::canCmd(std::string("SP_daemon"));
    } else {
        status = "Running";
    }
    napi_value result;
    napi_create_string_utf8(env, status.c_str(), status.size(), &result);
    return result;
}

static napi_value checkAccess(napi_env env, napi_callback_info info)
{
    size_t argc = 1;
    napi_value args[1] = { nullptr };
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    napi_valuetype valuetype0;
    napi_typeof(env, args[0], &valuetype0);
    char pathName[64] = {0};
    size_t typeLen = 0;
    napi_get_value_string_utf8(env, args[0], pathName, sizeof(pkgname) - 1, &typeLen);
    std::string pathNameStr = pathName;
    std::string status = "PermissionDenied";
    bool isAccess = gpUtils::canOpen(pathNameStr);
    if (isAccess) {
        status = "PermissionAccessed";
    }
    napi_value result;
    napi_create_string_utf8(env, status.c_str(), status.size(), &result);
    return result;
}

EXTERN_C_START
static napi_value Init(napi_env env, napi_value exports)
{
    napi_property_descriptor desc[] = {
    { "getFpsData", nullptr, getFpsData, nullptr, nullptr, nullptr, napi_default, nullptr },
    { "getRamData", nullptr, getRamData, nullptr, nullptr, nullptr, napi_default, nullptr },
    { "checkDaemon", nullptr, checkDaemon, nullptr, nullptr, nullptr, napi_default, nullptr },
    { "checkAccess", nullptr, checkAccess, nullptr, nullptr, nullptr, napi_default, nullptr },
    };
    napi_define_properties(env, exports, sizeof(desc) / sizeof(desc[0]), desc);
    return exports;
}
EXTERN_C_END

static napi_module demoModule = {
.nm_version = 1,
.nm_flags = 0,
.nm_filename = nullptr,
.nm_register_func = Init,
.nm_modname = "libsmartperf",
.nm_priv = ((void *)0),
.reserved = { 0 },
};

extern "C" __attribute__((constructor)) void RegisterModule(void)
{
napi_module_register(&demoModule);
}

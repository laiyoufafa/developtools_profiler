/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#include <cstdlib>
#include <cstdio>
#include <iostream>
#include <fstream>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <fcntl.h>
#include <securec.h>
#include <sys/prctl.h>
#include <sys/wait.h>
#include <sys/mman.h>
#include <pwd.h>
#include <string>
#include <policycoreutils.h>
#include <selinux/selinux.h>

#include "bundle_mgr_interface.h"
#include "bundle_mgr_proxy.h"
#include "iservice_registry.h"
#include "system_ability_definition.h"
#include "system_ability_manager_proxy.h"
#include "hilog/log.h"

using namespace std;

constexpr OHOS::HiviewDFX::HiLogLabel TRANS_LOG_LABLE = { LOG_CORE, 0xD002D0C, "TRANSITTO"};
constexpr int CODE_PATH_LEN = 1024;

struct AppInfo {
    int uid;
    bool debug;
    char codePath[CODE_PATH_LEN];
};

bool GetProcessPid(std::string& processName, int& pid)
{
   DIR* dir = opendir("/proc");
    if (dir == nullptr) {
        HILOG_ERROR(LOG_CORE, "open /proc dir failed");
        return false;
    }
    struct dirent* ptr;
    constexpr int BUFFER_SIZE = 1024;
    constexpr int FILE_PATH_SIZE = 1024;
    constexpr int INVALID_PID = -1;
    int pidValue = INVALID_PID;
    while ((ptr = readdir(dir)) != nullptr) {
        if ((strcmp(ptr->d_name, ".") == 0) || (strcmp(ptr->d_name, "..") == 0)) {
            continue;
        }
        if ((!isdigit(*ptr->d_name)) || ptr->d_type != DT_DIR) {
            continue;
        }
        char filePath[FILE_PATH_SIZE] = {0};
        int len = snprintf_s(filePath, FILE_PATH_SIZE, FILE_PATH_SIZE - 1, "/proc/%s/cmdline", ptr->d_name);
        if (len < 0) {
            HILOG_WARN(LOG_CORE, "maybe, the contents of cmdline had be cut off");
            continue;
        }
        FILE* fp = fopen(filePath, "r");
        if (fp == nullptr) {
            HILOG_WARN(LOG_CORE, "open file failed!");
            continue;
        }

        char buf[BUFFER_SIZE] = {0};
        if (fgets(buf, sizeof(buf) - 1, fp) == nullptr) {
            fclose(fp);
            continue;
        }
        std::string str(buf);
        size_t found = str.rfind("/");
        std::string fullProcess;
        if (found != std::string::npos) {
            fullProcess = str.substr(found + 1);
        } else {
            fullProcess = str;
        }
        if (fullProcess == processName) {
            pidValue = atoi(ptr->d_name);
            fclose(fp);
            break;
        }
        fclose(fp);
    }
    closedir(dir);
    if (pidValue != INVALID_PID) {
        pid = pidValue;
    }
    return pidValue != INVALID_PID;
}

std::string ReadFileToString(const std::string& fileName)
{
    std::ifstream inputString(fileName, std::ios::in);
    if (!inputString || !inputString.is_open()) {
        return "";
    }

    std::istreambuf_iterator<char> firstIt = {inputString};
    std::istreambuf_iterator<char> lastIt = {};

    std::string content(firstIt, lastIt);
    return content;
}

int main(int argc, char* argv[])
{
    if (argc <= 1) {
        cout << "argc is empty, usage is 'transitto <debugable bundleName> <commond>'" << endl;
        return -1;
    }

    int oldUid = getuid();
    // 0, root, 2000 shell, 20000000 20002000 container root shell
    if (oldUid != 0 && oldUid != 2000 && oldUid != 20000000 && oldUid != 20002000) {
        cout << "only root or shell can run this object, uid is " << oldUid << endl;
        return -1;
    }

    string bundleName = argv[1];
    AppInfo* app = static_cast<AppInfo*>(mmap(NULL, sizeof(AppInfo), PROT_READ | PROT_WRITE,
        MAP_SHARED | MAP_ANONYMOUS, -1, 0));
    if (app == MAP_FAILED) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "mmap fail.");
        return -1;
    }
    memset_s(app, sizeof(AppInfo), 0, sizeof(AppInfo));

    int pid = fork(); // for security_bounded_transition single thread
    if (pid == 0) {
        OHOS::sptr<OHOS::ISystemAbilityManager> systemAbilityManager =
            OHOS::SystemAbilityManagerClient::GetInstance().GetSystemAbilityManager();
        if (systemAbilityManager == nullptr) {
            OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to get system abilityManger.");
            _exit(-1);
        }

        OHOS::sptr<OHOS::IRemoteObject> remoteObject =
            systemAbilityManager->GetSystemAbility(OHOS::BUNDLE_MGR_SERVICE_SYS_ABILITY_ID);
        if (remoteObject == nullptr) {
            OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to get bundle service.");
            _exit(-1);
        }

        OHOS::sptr<OHOS::AppExecFwk::BundleMgrProxy> bundleMgrProxy =
            OHOS::iface_cast<OHOS::AppExecFwk::BundleMgrProxy>(remoteObject);
        if (bundleMgrProxy == nullptr) {
            OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to get bundle proxy.");
            _exit(-1);
        }

        OHOS::AppExecFwk::ApplicationInfo appInfo;
        OHOS::HiviewDFX::HiLog::Info(TRANS_LOG_LABLE, "start to get ApplicationInfo");
        // 0: GET_BASIC_APPLICATION_INFO
        if (!bundleMgrProxy->GetApplicationInfo(bundleName, 0, OHOS::AppExecFwk::Constants::ANY_USERID, appInfo)) {
            OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to get application info.");
            _exit(-1);
        }
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "get ApplicationInfo success uid is %{public}d.", appInfo.uid);
        app->uid = appInfo.uid;
        app->debug = appInfo.debug;
        int ret = memcpy_s(app->codePath, CODE_PATH_LEN - 1, appInfo.codePath.c_str(), appInfo.codePath.size());
        if (ret != EOK) {
            OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "mencpy appinfo fail, ret is %{public}d.", ret);
            _exit(-1);
        }
        _exit(0);
    } else {
        wait(NULL);
    }

    int uid = app->uid;
    int gid = app->uid;
    bool debug = app->debug;
    string codePath = app->codePath;
    munmap(app, sizeof(AppInfo));

    if (uid < 0) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "uid is %{public}d.", uid);
        return -1;
    }

    if (!debug) {
        cout << "app is not debugable" << endl;
        return -1;
    }

    OHOS::HiviewDFX::HiLog::Info(TRANS_LOG_LABLE, "start change uid gid.");
    if (setresgid(gid, gid, gid) < 0) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail set gid, errno is %{publci}d.", errno);
        return -1;
    }

    if (setresuid(uid, uid, uid) < 0) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail set uid, errno is %{publci}d.", errno);
        return -1;
    }

    OHOS::HiviewDFX::HiLog::Info(TRANS_LOG_LABLE, "start change selinux context.");
    int appPid = -1;
    if (!GetProcessPid(bundleName, appPid) || appPid < 0) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to get pid, errno is %{public}d.", errno);
        return -1;
    }

    string procPath = "/proc/" + to_string(appPid) + "/attr/current";
    string seContext = ReadFileToString(procPath);
    if (seContext.empty()) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE,
            "fail to get selinux context, procPath is %{public}s, errno is %{public}d.", procPath.c_str(), errno);
        return -1;
    }

    if (setcon(seContext.c_str()) != 0) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to set selinux context, errno is %{public}d.", errno);
        return -1;
    }
    OHOS::HiviewDFX::HiLog::Info(TRANS_LOG_LABLE, "change selinux context successfully.");

    OHOS::HiviewDFX::HiLog::Info(TRANS_LOG_LABLE, "start set env, app baseDir is %{public}s.", app->codePath.c_str());
    if (chdir(codePath.c_str()) == -1) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE,
            "fail to chdir, path is %{public}s, errno %{public}d.", codePath.c_str(), errno);
        return -1;
    }

    setenv("HOME", codePath.c_str(), 1);
    unsetenv("IFS");

    passwd* pw = getpwuid(uid);
    if (pw != nullptr) {
        setenv("LOGNAME", pw->pw_name, 1);
        setenv("SHELL", pw->pw_shell, 1);
        setenv("USER", pw->pw_name, 1);
    } else {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE, "fail to getpwuid, errno is %{public}d.", errno);
    }

    if (argc > 2 && execvp(argv[2], argv + 2) < 0) {
        OHOS::HiviewDFX::HiLog::Error(TRANS_LOG_LABLE,
            "fail to execvp, com is %{public}s, errno %{public}d.", argv[2], errno);
        return -1;
    }
    return 0;
}
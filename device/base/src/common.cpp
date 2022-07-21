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

#include "common.h"
#include "logging.h"
#include <cinttypes>
#include <csignal>
#include <fcntl.h>
#include <sys/file.h>
#include <sys/wait.h>
#include <unistd.h>

namespace COMMON {
constexpr int EXECVP_ERRNO = 2;
const std::string DEFAULT_PATH = "/data/local/tmp/";

bool IsProcessRunning()
{
    char buffer[PATH_MAX + 1] = {0};
    readlink("/proc/self/exe", buffer, PATH_MAX);
    std::string processName = buffer;
    int pos = (int)(processName.find_last_of('/'));
    if (pos != 0) {
        processName = processName.substr(pos + 1, processName.size());
    }

    std::string fileName = DEFAULT_PATH + processName + ".pid";
    int fd = open(fileName.c_str(), O_WRONLY | O_CREAT, (mode_t)0440);
    if (fd < 0) {
        const int bufSize = 256;
        char buf[bufSize] = { 0 };
        strerror_r(errno, buf, bufSize);
        HILOG_ERROR(LOG_CORE, "%s:failed to open(%s), errno(%d:%s)", __func__, fileName.c_str(), errno, buf);
        return false;
    }

    if (flock(fd, LOCK_EX | LOCK_NB) == -1) {
        // 进程正在运行，加锁失败
        close(fd);
        printf("%s is running, please don't start it again.\n", processName.c_str());
        HILOG_ERROR(LOG_CORE, "%s is running, please don't start it again.", processName.c_str());
        return true;
    }

    std::string pidStr = std::to_string(getpid());
    auto nbytes = write(fd, pidStr.data(), pidStr.size());
    CHECK_TRUE(static_cast<size_t>(nbytes) == pidStr.size(), false, "write pid FAILED!");
    return false;
}

bool IsProcessExist(std::string& processName, int& pid)
{
    std::string findpid = "pidof " + processName;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(findpid.c_str(), "r"), pclose);

    constexpr int lineSize = 1000;
    char line[lineSize];
    do {
        if (fgets(line, sizeof(line), pipe.get()) == nullptr) {
            return false;
        } else if (strlen(line) > 0 && isdigit((unsigned char)(line[0]))) {
            pid = atoi(line);
            return true;
        }
    } while (true);
}

int StartProcess(const std::string& processBin, std::vector<char*>& argv)
{
    int pid = fork();
    if (pid == 0) {
        argv.push_back(nullptr); // last item in argv must be NULL
        int retval = execvp(processBin.c_str(), argv.data());
        if (retval == -1 && errno == EXECVP_ERRNO) {
            printf("warning: %s does not exist!\n", processBin.c_str());
            HILOG_WARN(LOG_CORE, "warning: %s does not exist!", processBin.c_str());
        }
        _exit(EXIT_FAILURE);
    }

    return pid;
}

int KillProcess(int pid)
{
    if (pid == -1) {
        return -1;
    }

    int stat;
    kill(pid, SIGKILL);
    if (waitpid(pid, &stat, 0) == -1) {
        if (errno != EINTR) {
            stat = -1;
        }
    }

    return stat;
}
} // COMMON
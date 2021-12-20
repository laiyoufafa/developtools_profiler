/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <array>
#include <dlfcn.h>
#include <fcntl.h>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <sys/syscall.h>
#include <unistd.h>

#include "logging.h"
#include "openssl/sha.h"

using namespace testing::ext;

#define HHB(v) (((v) & 0xF0) >> 4)
#define LHB(v)  ((v) & 0x0F)

namespace {
const std::string DEFAULT_HIPROFILERD_PATH("/system/bin/hiprofilerd");
const std::string DEFAULT_HIPROFILER_PLUGINS_PATH("/system/bin/hiprofiler_plugins");
const std::string DEFAULT_HIPROFILER_CMD_PATH("/system/bin/hiprofiler_cmd");
const std::string FTRACE_PLUGIN_PATH("/data/local/tmp/libftrace_plugin.z.so");
std::string DEFAULT_PATH("/data/local/tmp/");
constexpr uint32_t READ_BUFFER_SIZE = 1024;
constexpr int SLEEP_TIME = 3;
constexpr int FILE_READ_CHUNK_SIZE = 4096;
constexpr char HEX_CHARS[] = "0123456789abcdef";


class HiprofilerCmdTest : public ::testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void StartServerStub(std::string name)
    {
        int processNum = fork();
        if (processNum == 0) {
            if (DEFAULT_HIPROFILERD_PATH == name) {
                // start running hiprofilerd
                execl(name.c_str(), nullptr, nullptr);
            } else if (DEFAULT_HIPROFILER_PLUGINS_PATH == name) {
                // start running hiprofiler_plugins
                execl(name.c_str(), DEFAULT_PATH.c_str(), nullptr);
            }
            _exit(1);
        } else if (DEFAULT_HIPROFILERD_PATH == name) {
            g_hiprofilerdPid = processNum;
        } else if (DEFAULT_HIPROFILER_PLUGINS_PATH == name) {
            g_hiprofilerPluginsPid = processNum;
        }
    }

    void StopProcessStub(int processNum)
    {
        std::string stopCmd = "kill " + std::to_string(processNum);
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(stopCmd.c_str(), "r"), pclose);
    }

    bool RunCommand(const std::string& cmd, std::string& content)
    {
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(cmd.c_str(), "r"), pclose);
        CHECK_TRUE(pipe, false, "BytraceCall::RunCommand: create popen FAILED!");

        std::array<char, READ_BUFFER_SIZE> buffer;
        while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
            content += buffer.data();
        }
        return true;
    }

    std::string ComputeFileSha256(const std::string& path)
    {
        uint8_t out[SHA256_DIGEST_LENGTH];
        uint8_t buffer[FILE_READ_CHUNK_SIZE];

        SHA256_CTX sha;
        SHA256_Init(&sha);

        size_t nbytes = 0;
        FILE* file = fopen(path.c_str(), "rb");
        if (file == nullptr) {
            return "";
        }

        std::unique_ptr<FILE, decltype(fclose)*> fptr(file, fclose);
        if (fptr == nullptr) {
            return "";
        }

        while ((nbytes = fread(buffer, 1, sizeof(buffer), fptr.get())) > 0) {
            SHA256_Update(&sha, buffer, nbytes);
        }
        SHA256_Final(out, &sha);

        std::string result;
        result.reserve(SHA256_DIGEST_LENGTH + SHA256_DIGEST_LENGTH);
        for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
            result.push_back(HEX_CHARS[HHB(out[i])]);
            result.push_back(HEX_CHARS[LHB(out[i])]);
        }

        HILOG_DEBUG(LOG_CORE, "%s:%s-(%s)", __func__, path.c_str(), result.c_str());
        return result;
    }

    void CreateConfigFile(const std::string configFile, const std::string sha256)
    {
        // 构建config文件
        std::string configStr =
            "request_id: 26\n"
            "session_config {\n"
            "  buffers {\n"
            "    pages: 1000\n"
            "  }\n"
            "  result_file: \"/data/local/tmp/hiprofiler_data.htrace\"\n"
            "  sample_duration: 10000\n"
            "}\n"
            "plugin_configs {\n"
            "  name: \"/data/local/tmp/libftrace_plugin.z.so\"\n"
            "  plugin_sha256: \"";
        configStr = configStr + sha256 + "\"\n";
        configStr +=
            "  sample_interval: 2000\n"
            "  config_data: \"\\n\\022sched/sched_waking\\n  \\020task/task_rename\\n"
            "    \\022sched/sched_switch\\n  \\030sched/sched_process_exit\\n"
            "    \\024power/suspend_resume\\n\\030sched/sched_process_free\\n"
            "    \\021task/task_newtask\\n\\022sched/sched_wakeup\\n"
            "    \\026sched/sched_wakeup_new \\200P(\\350\\a0\\200 8\\001B\\004monoP\\310\\001\"\n"
            "}\n";

        // 根据构建的config写文件
        FILE* writeFp = fopen(configFile.c_str(), "w");
        if (writeFp == nullptr) {
            HILOG_ERROR(LOG_CORE, "CreateConfigFile: fopen() error = %s", strerror(errno));
            return;
        }

        size_t len = fwrite(const_cast<char*>(configStr.c_str()), 1, configStr.length(), writeFp);
        if (len < 0) {
            HILOG_ERROR(LOG_CORE, "CreateConfigFile: fwrite() error = %s", strerror(errno));
            return;
        }

        int ret = fflush(writeFp);
        if (ret == EOF) {
            HILOG_ERROR(LOG_CORE, "CreateConfigFile: fflush() error = %s", strerror(errno));
            return;
        }

        fsync(fileno(writeFp));
        ret = fclose(writeFp);
        if (ret == 0) {
            HILOG_ERROR(LOG_CORE, "CreateConfigFile: fclose() error = %s", strerror(errno));
            return;
        }
    }

private:
    int g_hiprofilerdPid = -1;
    int g_hiprofilerPluginsPid = -1;
};

/**
 * @tc.name: native hook
 * @tc.desc: Test hiprofiler_cmd with -h -q.
 * @tc.type: FUNC
 */
HWTEST_F(HiprofilerCmdTest, DFX_DFR_Hiprofiler_0110, Function | MediumTest | Level1)
{
    std::string cmd = DEFAULT_HIPROFILER_CMD_PATH + " -h";
    std::string content = "";
    EXPECT_TRUE(RunCommand(cmd, content));
    std::string destStr = "help";
    EXPECT_EQ(strncmp(content.c_str(), destStr.c_str(), strlen(destStr.c_str())), 0);

    content = "";
    cmd = DEFAULT_HIPROFILER_CMD_PATH + " -q";
    EXPECT_TRUE(RunCommand(cmd, content));
    destStr = "FAIL";
    EXPECT_EQ(strncmp(content.c_str(), destStr.c_str(), strlen(destStr.c_str())), 0);

    StartServerStub(DEFAULT_HIPROFILERD_PATH);
    sleep(1);
    content = "";
    EXPECT_TRUE(RunCommand(cmd, content));
    destStr = "OK";
    EXPECT_EQ(strncmp(content.c_str(), destStr.c_str(), strlen(destStr.c_str())), 0);
    StopProcessStub(g_hiprofilerdPid);
}

/**
 * @tc.name: native hook
 * @tc.desc: Test hiprofiler_cmd with -c.
 * @tc.type: FUNC
 */
HWTEST_F(HiprofilerCmdTest, DFX_DFR_Hiprofiler_0120, Function | MediumTest | Level1)
{
    std::string cmd = "cp /system/lib/libftrace_plugin.z.so " + DEFAULT_PATH;
    system(cmd.c_str());

    // 测试不存在的config文件
    std::string configTestFile = DEFAULT_PATH + "1234.txt";
    std::string outFile = DEFAULT_PATH + "trace.htrace";
    std::string content = "";
    cmd = DEFAULT_HIPROFILER_CMD_PATH + " -c " + configTestFile + " -o " + outFile + " -t 3";
    EXPECT_TRUE(RunCommand(cmd, content));
    std::string destStr = "can't open " + configTestFile;
    EXPECT_EQ(strncmp(content.c_str(), destStr.c_str(), strlen(destStr.c_str())), 0);

    // 创建有效的config文件
    const std::string configFile = DEFAULT_PATH + "ftrace.config";
    std::string sha256 = ComputeFileSha256(FTRACE_PLUGIN_PATH);
    CreateConfigFile(configFile, sha256);

    // 测试有效的config文件，不开启hiprofilerd和hiprofiler_plugin进程
    content = "";
    cmd = DEFAULT_HIPROFILER_CMD_PATH + " -c " + configFile + " -o " + outFile + " -t 3";
    EXPECT_TRUE(RunCommand(cmd, content));
    sleep(SLEEP_TIME);
    EXPECT_NE(access(outFile.c_str(), F_OK), 0);

    // 开启hiprofilerd和hiprofiler_plugin进程，可以生成trace文件
    content = "";
    StartServerStub(DEFAULT_HIPROFILERD_PATH);
    sleep(1);
    StartServerStub(DEFAULT_HIPROFILER_PLUGINS_PATH);
    sleep(1);
    EXPECT_TRUE(RunCommand(cmd, content));
    sleep(SLEEP_TIME);
    EXPECT_EQ(access(outFile.c_str(), F_OK), 0);

    // 删除资源文件和生成的trace文件
    cmd = "rm " + FTRACE_PLUGIN_PATH + " " + configFile + " " + outFile;
    system(cmd.c_str());
    StopProcessStub(g_hiprofilerPluginsPid);
    StopProcessStub(g_hiprofilerdPid);
}
}

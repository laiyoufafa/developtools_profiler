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
#include <cinttypes>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <dlfcn.h>

#include "cpu_data_plugin.h"
#include "plugin_module_api.h"

using namespace testing::ext;

namespace {
#if defined(__i386__) || defined(__x86_64__)
const std::string DEFAULT_TEST_PATH = "./resources";
#else
const std::string DEFAULT_TEST_PATH = "/data/local/tmp/resources";
#endif

constexpr uint32_t BUF_SIZE = 4 * 1024 * 1024;
const std::string SO_PATH = "/system/lib/libcpudataplugin.z.so";
constexpr int TEST_PID = 1;

std::string g_path;
std::string g_testPath;
std::vector<int> g_tidList = {1872, 1965, 1966, 1967, 1968, 1995, 1996};

constexpr int CORE_NUM = 6;
constexpr int THREAD_NUM = 7;

struct TestSystemStat {
    int32_t core;
    int64_t user;
    int64_t nice;
    int64_t system;
    int64_t idle;
    int64_t iowait;
    int64_t irq;
    int64_t softirq;
    int64_t steal;
};

struct TestStat {
    int64_t utime;
    int64_t stime;
    int64_t cutime;
    int64_t cstime;
};

struct TestTidStat {
    int32_t tid;
    std::string name;
    ThreadState state;
    TestStat stat;
};

struct TestFreq {
    int32_t curFreq;
    int32_t maxFreq;
    int32_t minFreq;
};

TestSystemStat g_systemStat[CORE_NUM + 1] = {
    {-1, 24875428, 3952448, 11859815, 1193297105, 8980661, 0, 2607250, 0},
    {0, 4165400, 662862, 1966195, 196987024, 3571925, 0, 817371, 0},
    {1, 3861506, 676578, 1702753, 199535158, 1752008, 0, 401639, 0},
    {2, 3549890, 676286, 1544630, 200640747, 1133743, 0, 205972, 0},
    {3, 3336646, 676939, 1458898, 201176432, 854578, 0, 124812, 0},
    {4, 4566158, 601107, 2305309, 197166395, 929594, 0, 1007959, 0},
    {5, 5395826, 658673, 2882028, 197791346, 738811, 0, 49496, 0},
};

TestStat g_pidStat = {60, 10, 20, 30};

TestTidStat g_tidStat[THREAD_NUM] = {
    {1872, "ibus-x11", THREAD_RUNNING, {17, 5, 10, 10}},
    {1965, "ibus-x1:disk$0", THREAD_SLEEPING, {8, 1, 5, 8}},
    {1966, "ibus-x1:disk$1", THREAD_UNSPECIFIED, {0, 0, 0, 0}},
    {1967, "ibus-x1:disk$2", THREAD_SLEEPING, {10, 1, 5, 8}},
    {1968, "ibus-x1:disk$3", THREAD_STOPPED, {7, 0, 0, 0}},
    {1995, "gmain", THREAD_SLEEPING, {15, 3, 0, 4}},
    {1996, "gdbus", THREAD_WAITING, {5, 0, 0, 0}},
};

TestFreq g_Freq[CORE_NUM + 1] = {
    {1018, 3844, 509}, {1023, 2844, 509}, {1011, 3844, 509}, {1518, 3844, 1018}, {1245, 1844, 1018}, {1767, 3044, 1018},
};

class CpuDataPluginTest : public ::testing::Test {
public:
    static void SetUpTestCase() {}

    static void TearDownTestCase()
    {
        if (access(g_testPath.c_str(), F_OK) == 0) {
            std::string str = "rm -rf " + g_testPath;
            printf("TearDown--> %s\r\n", str.c_str());
            system(str.c_str());
        }
    }
};

string Getexepath()
{
    char buf[PATH_MAX] = "";
    std::string path = "/proc/self/exe";
    size_t rslt = readlink(path.c_str(), buf, sizeof(buf));
    if (rslt < 0 || (rslt >= sizeof(buf))) {
        return "";
    }
    buf[rslt] = '\0';
    for (int i = rslt; i >= 0; i--) {
        if (buf[i] == '/') {
            buf[i + 1] = '\0';
            break;
        }
    }
    return buf;
}

std::string GetFullPath(std::string path)
{
    if (path.size() > 0 && path[0] != '/') {
        return Getexepath() + path;
    }
    return path;
}

#if defined(__i386__) || defined(__x86_64__)
bool CreatTestResource(std::string path, std::string exepath)
{
    std::string str = "cp -r " + path + " " + exepath;
    printf("CreatTestResource:%s\n", str.c_str());

    pid_t status = system(str.c_str());
    if (-1 == status) {
        printf("system error!");
    } else {
        printf("exit status value = [0x%x]\n", status);
        if (WIFEXITED(status)) {
            if (WEXITSTATUS(status) == 0) {
                return true;
            } else {
                printf("run shell script fail, script exit code: %d\n", WEXITSTATUS(status));
                return false;
            }
        } else {
            printf("exit status = [%d]\n", WEXITSTATUS(status));
            return true;
        }
    }
    return false;
}
#endif

bool CheckTid(std::vector<int>& tidListTmp)
{
    sort(g_tidList.begin(), g_tidList.end());
    for (size_t i = 0; i < g_tidList.size(); i++) {
        if (tidListTmp.at(i) != g_tidList.at(i)) {
            return false;
        }
    }
    return true;
}

bool PluginCpuinfoStub(CpuDataPlugin& cpuPlugin, CpuData& cpuData, int pid, bool unusualBuff)
{
    CpuConfig protoConfig;
    protoConfig.set_pid(pid);

    // serialize
    std::vector<uint8_t> configData(protoConfig.ByteSizeLong());
    int ret = protoConfig.SerializeToArray(configData.data(), configData.size());

    // start
    ret = cpuPlugin.Start(configData.data(), configData.size());
    if (ret < 0) {
        return false;
    }
    printf("ut: serialize success start plugin ret = %d\n", ret);

    // report
    std::vector<uint8_t> bufferData(BUF_SIZE);
    if (unusualBuff) { // buffer异常，调整缓冲区长度为1，测试异常情况
        bufferData.resize(1, 0);
        printf("ut: bufferData resize\n");
    }

    ret = cpuPlugin.Report(bufferData.data(), bufferData.size());
    if (ret > 0) {
        cpuData.ParseFromArray(bufferData.data(), ret);
        return true;
    }

    return false;
}

void GetSystemCpuTime(TestSystemStat& stat, int64_t Hz, int64_t& usageTime, int64_t& time)
{
    usageTime = (stat.user + stat.nice + stat.system + stat.irq + stat.softirq + stat.steal) * Hz;
    time = (usageTime + stat.idle + stat.iowait) * Hz;
}

/**
 * @tc.name: cpu plugin
 * @tc.desc: Test whether the path exists.
 * @tc.type: FUNC
 */
HWTEST_F(CpuDataPluginTest, TestPath, TestSize.Level1)
{
    g_path = GetFullPath(DEFAULT_TEST_PATH);
    g_testPath = g_path;
    printf("g_path:%s\n", g_path.c_str());
    EXPECT_NE("", g_path);

#if defined(__i386__) || defined(__x86_64__)
    if (DEFAULT_TEST_PATH != g_path) {
        if ((access(g_path.c_str(), F_OK) != 0) && (access(DEFAULT_TEST_PATH.c_str(), F_OK) == 0)) {
            EXPECT_TRUE(CreatTestResource(DEFAULT_TEST_PATH, g_path));
        }
    }
#endif
}

/**
 * @tc.name: cpu plugin
 * @tc.desc: Tid list test in a specific directory.
 * @tc.type: FUNC
 */
HWTEST_F(CpuDataPluginTest, TestTidlist, TestSize.Level1)
{
    CpuDataPlugin cpuPlugin;
    std::string path = g_path + "/proc/1872/task/";
    printf("path:%s\n", path.c_str());
    DIR* dir = cpuPlugin.OpenDestDir(path);
    EXPECT_NE(nullptr, dir);

    std::vector<int> tidListTmp;
    while (int32_t pid = cpuPlugin.GetValidTid(dir)) {
        tidListTmp.push_back(pid);
        sort(tidListTmp.begin(), tidListTmp.end());
    }
    EXPECT_TRUE(CheckTid(tidListTmp));
}

/**
 * @tc.name: cpu plugin
 * @tc.desc: a part of cpu information test for specific pid.
 * @tc.type: FUNC
 */
HWTEST_F(CpuDataPluginTest, TestPluginInfo, TestSize.Level1)
{
    CpuDataPlugin cpuPlugin;
    CpuData cpuData;
    cpuPlugin.SetFreqPath(g_path);
    g_path += "/proc/";
    cpuPlugin.SetPath(g_path);
    EXPECT_TRUE(PluginCpuinfoStub(cpuPlugin, cpuData, 1872, false));

    int64_t systemCpuTime = 0;
    int64_t systemBootTime = 0;
    int64_t Hz = cpuPlugin.GetUserHz();
    printf("Hz : %" PRId64 "\n", Hz);
    int64_t processCpuTime = (g_pidStat.utime + g_pidStat.stime + g_pidStat.cutime + g_pidStat.cstime) * Hz;
    GetSystemCpuTime(g_systemStat[0], Hz, systemCpuTime, systemBootTime);

    CpuUsageInfo cpuUsageInfo = cpuData.cpu_usage_info();
    EXPECT_EQ(cpuUsageInfo.prev_process_cpu_time_ms(), 0);
    EXPECT_EQ(cpuUsageInfo.prev_system_cpu_time_ms(), 0);
    EXPECT_EQ(cpuUsageInfo.prev_system_boot_time_ms(), 0);
    EXPECT_EQ(cpuUsageInfo.process_cpu_time_ms(), processCpuTime);
    EXPECT_EQ(cpuUsageInfo.system_cpu_time_ms(), systemCpuTime);
    printf("systemCpuTime = %" PRId64 "\n", systemCpuTime);
    EXPECT_EQ(cpuUsageInfo.system_boot_time_ms(), systemBootTime);
    printf("systemBootTime = %" PRId64 "\n", systemBootTime);

    ASSERT_EQ(cpuUsageInfo.cores_size(), 6);
    for (int i = 1; i <= CORE_NUM; i++) {
        CpuCoreUsageInfo cpuCoreUsageInfo = cpuUsageInfo.cores()[i - 1];
        GetSystemCpuTime(g_systemStat[i], Hz, systemCpuTime, systemBootTime);
        EXPECT_EQ(cpuCoreUsageInfo.cpu_core(), g_systemStat[i].core);
        EXPECT_EQ(cpuCoreUsageInfo.prev_system_cpu_time_ms(), 0);
        EXPECT_EQ(cpuCoreUsageInfo.prev_system_boot_time_ms(), 0);
        EXPECT_EQ(cpuCoreUsageInfo.system_cpu_time_ms(), systemCpuTime);
        EXPECT_EQ(cpuCoreUsageInfo.system_boot_time_ms(), systemBootTime);

        EXPECT_EQ(cpuCoreUsageInfo.frequency().min_frequency_khz(), g_Freq[i - 1].minFreq);
        EXPECT_EQ(cpuCoreUsageInfo.frequency().max_frequency_khz(), g_Freq[i - 1].maxFreq);
        EXPECT_EQ(cpuCoreUsageInfo.frequency().cur_frequency_khz(), g_Freq[i - 1].curFreq);
        if (i == 1) { // cpu0为大核
            EXPECT_EQ(cpuCoreUsageInfo.is_little_core(), false);
        } else {
            EXPECT_EQ(cpuCoreUsageInfo.is_little_core(), true);
        }
    }
}

/**
 * @tc.name: cpu plugin
 * @tc.desc: cpu information test for specific pid.
 * @tc.type: FUNC
 */
HWTEST_F(CpuDataPluginTest, TestPlugin, TestSize.Level1)
{
    CpuDataPlugin cpuPlugin;
    CpuData cpuData;
    g_path = g_testPath;
    cpuPlugin.SetFreqPath(g_path);
    g_path += "/proc/";
    cpuPlugin.SetPath(g_path);
    EXPECT_TRUE(PluginCpuinfoStub(cpuPlugin, cpuData, 1872, false));

    int64_t Hz = cpuPlugin.GetUserHz();
    int64_t threadCpuTime;
    ASSERT_EQ(cpuData.thread_info_size(), 7);
    for (int i = 0; i < THREAD_NUM; i++) {
        threadCpuTime = (g_tidStat[i].stat.utime + g_tidStat[i].stat.stime +
            g_tidStat[i].stat.cutime + g_tidStat[i].stat.cstime) * Hz;
        ThreadInfo threadInfo = cpuData.thread_info()[i];
        EXPECT_EQ(threadInfo.tid(), g_tidStat[i].tid);
        EXPECT_STREQ(threadInfo.thread_name().c_str(), g_tidStat[i].name.c_str());
        EXPECT_EQ(threadInfo.thread_state(), g_tidStat[i].state);
        EXPECT_EQ(threadInfo.thread_cpu_time_ms(), threadCpuTime);
        EXPECT_EQ(threadInfo.prev_thread_cpu_time_ms(), 0);
    }

    EXPECT_EQ(cpuPlugin.Stop(), 0);

    // 缓冲区异常
    EXPECT_FALSE(PluginCpuinfoStub(cpuPlugin, cpuData, 1872, true));
    EXPECT_EQ(cpuPlugin.Stop(), 0);
}

/**
 * @tc.name: cpu plugin
 * @tc.desc: cpu information test for unusual path.
 * @tc.type: FUNC
 */
HWTEST_F(CpuDataPluginTest, TestPluginBoundary, TestSize.Level1)
{
    CpuDataPlugin cpuPlugin;
    CpuData cpuData;
    g_path += "/proc/";
    cpuPlugin.SetPath(g_path);
    EXPECT_FALSE(PluginCpuinfoStub(cpuPlugin, cpuData, -1, false));
    EXPECT_FALSE(PluginCpuinfoStub(cpuPlugin, cpuData, 12345, false));

    CpuDataPlugin cpuPlugin2;
    cpuPlugin2.SetPath("123");
    EXPECT_FALSE(PluginCpuinfoStub(cpuPlugin2, cpuData, 1872, false));
    EXPECT_FALSE(PluginCpuinfoStub(cpuPlugin2, cpuData, -1, false));
    EXPECT_FALSE(PluginCpuinfoStub(cpuPlugin2, cpuData, 12345, false));
}

/**
 * @tc.name: cpu plugin
 * @tc.desc: cpu plugin registration test.
 * @tc.type: FUNC
 */
HWTEST_F(CpuDataPluginTest, TestPluginRegister, TestSize.Level1)
{
    void* handle = dlopen(SO_PATH.c_str(), RTLD_LAZY);
    ASSERT_NE(handle, nullptr);
    PluginModuleStruct* cpuPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
    ASSERT_NE(cpuPlugin, nullptr);
    EXPECT_STREQ(cpuPlugin->name, "cpu-plugin");
    EXPECT_EQ(cpuPlugin->resultBufferSizeHint, BUF_SIZE);

    // Serialize config
    CpuConfig protoConfig;
    protoConfig.set_pid(TEST_PID);
    int configLength = protoConfig.ByteSizeLong();
    ASSERT_GT(configLength, 0);
    std::vector<uint8_t> configBuffer(configLength);
    EXPECT_TRUE(protoConfig.SerializeToArray(configBuffer.data(), configLength));

    // run plugin
    std::vector<uint8_t> dataBuffer(cpuPlugin->resultBufferSizeHint);
    EXPECT_EQ(cpuPlugin->callbacks->onPluginSessionStart(configBuffer.data(), configLength), RET_SUCC);
    ASSERT_GT(cpuPlugin->callbacks->onPluginReportResult(dataBuffer.data(), cpuPlugin->resultBufferSizeHint), 0);
    EXPECT_EQ(cpuPlugin->callbacks->onPluginSessionStop(), 0);

    // 反序列化失败导致的start失败
    EXPECT_EQ(cpuPlugin->callbacks->onPluginSessionStart(configBuffer.data(), configLength+1), RET_FAIL);
}
} // namespace
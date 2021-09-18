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

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "memory_data_plugin.h"

using namespace testing::ext;

namespace {
#if defined(__i386__) || defined(__x86_64__)
const std::string DEFAULT_TEST_PATH("./");
#else
const std::string DEFAULT_TEST_PATH("/data/local/tmp/");
#endif
constexpr uint32_t BUF_SIZE = 4 * 1024 * 1024;
constexpr uint32_t BIT_WIDTH = 35;

std::string g_path;
const std::vector<int> g_expectPidList = {
    1, 2, 11
};

struct TestElement {
    int32_t pid;
    std::string name;
    // data from /proc/$pid/stat
    uint64_t vm_size_kb;
    uint64_t vm_rss_kb;
    uint64_t rss_anon_kb;
    uint64_t rss_file_kb;
    uint64_t rss_shmem_kb;
    uint64_t vm_swap_kb;
    uint64_t vm_locked_kb;
    uint64_t vm_hwm_kb;
    int64_t oom_score_adj;

    uint64_t java_heap;
    uint64_t native_heap;
    uint64_t code;
    uint64_t stack;
    uint64_t graphics;
    uint64_t private_other;
};

TestElement g_singlepid = {-1, "null", 0, 0};
TestElement g_pidtarget[] = {
    {1, "systemd", 226208, 9388, 2984, 6404, 0, 0, 0, 9616, -1, 3036, 4256, 288, 748, 0, 1388},
    {2, "kthreadd", 0, 0, 0, 0, 0, 0, 0, 0, -100, 3036, 4260, 336, 760, 0, 4204},
    {11, "rcu_sched", 0, 0, 0, 0, 0, 0, 0, 0, 0, 3036, 4272, 392, 772, 0, 7168},
};

unsigned long g_meminfo[] = {16168696, 1168452, 12363564, 2726188, 7370484, 29260,    8450388,  4807668,
                             2535372,  658832,  4148836,  132,     0,       63999996, 62211580, 0};
std::string GetFullPath(std::string path);

class MemoryDataPluginTest : public ::testing::Test {
public:
    static void SetUpTestCase();

    static void TearDownTestCase()
    {
        if (access(g_path.c_str(), F_OK) == 0) {
            std::string str = "rm -rf " + GetFullPath(DEFAULT_TEST_PATH) + "utresources";
            printf("TearDown--> %s\r\n", str.c_str());
            system(str.c_str());
        }
    }
    void SetUp() {}
    void TearDown() {}
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

void MyPrintfProcessMemoryInfo(MemoryData memoryData)
{
    int index = memoryData.processesinfo_size();
    for (int i = 0; i < index; ++i) {
        ProcessMemoryInfo it = memoryData.processesinfo(i);
        std::cout << it.pid() << "\t";
        std::cout << std::setw(BIT_WIDTH) << std::setfill(' ') << it.name().c_str() << "\t";
        std::cout << it.vm_size_kb() << "\t";
        std::cout << it.vm_rss_kb() << "\t";
        std::cout << it.rss_anon_kb() << "\t";
        std::cout << it.rss_file_kb() << "\t";
        std::cout << it.rss_shmem_kb() << "\t";
        std::cout << it.vm_locked_kb() << "\t";
        std::cout << it.vm_hwm_kb() << "\t";

        std::cout << it.oom_score_adj() << "\t";
        if (it.has_memsummary()) {
            std::cout << "appsummary:\t";
            AppSummary app = it.memsummary();
            std::cout << app.java_heap() << "\t";
            std::cout << app.native_heap() << "\t";
            std::cout << app.code() << "\t";
            std::cout << app.stack() << "\t";
            std::cout << app.graphics() << "\t";
            std::cout << app.private_other() << "\t";
            std::cout << app.system() << "\t";
        }
        std::cout << std::endl;
    }
}

void SetPluginProcessConfig(std::vector<int> processList, MemoryConfig& protoConfig)
{
    if (processList.size() != 0) {
        // 具体进程
        protoConfig.set_report_process_mem_info(true);
        protoConfig.set_report_app_mem_info(true);
        for (size_t i = 0; i < processList.size(); i++) {
            protoConfig.add_pid(processList.at(i));
        }
    } else {
        // 进程树
        protoConfig.set_report_process_tree(true);
    }
}

void SetPluginSysMemConfig(MemoryConfig &protoConfig)
{
    protoConfig.set_report_sysmem_mem_info(true);

    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_MEM_TOTAL);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_MEM_FREE);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_MEM_AVAILABLE);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_BUFFERS);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_CACHED);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_SWAP_CACHED);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_ACTIVE);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_INACTIVE);

    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_ACTIVE_ANON);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_INACTIVE_ANON);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_INACTIVE_FILE);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_UNEVICTABLE);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_MLOCKED);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_SWAP_TOTAL);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_SWAP_FREE);
    protoConfig.add_sys_meminfo_counters(SysMeminfoType::MEMINFO_DIRTY);
}

void SetPluginDumpsysConfig(MemoryConfig& protoConfig)
{
    protoConfig.set_report_process_mem_info(true);
    protoConfig.set_report_app_mem_info(true);
    protoConfig.add_pid(1);
    protoConfig.set_report_app_mem_by_dumpsys(true);
}

bool PluginStub(MemoryDataPlugin& memoryPlugin, MemoryConfig& protoConfig, MemoryData& memoryData)
{
    // serialize
    int configSize = protoConfig.ByteSizeLong();
    std::vector<uint8_t> configData(configSize);
    int ret = protoConfig.SerializeToArray(configData.data(), configData.size());
    CHECK_TRUE(ret > 0, false, "PluginStub::SerializeToArray fail!!!");

    // start
    ret = memoryPlugin.Start(configData.data(), configData.size());
    CHECK_TRUE(ret == 0, false, "PluginStub::start plugin fail!!!");

    printf("ut: serialize success start plugin ret = %d\n", ret);

    // report
    std::vector<uint8_t> bufferData(BUF_SIZE);
    ret = memoryPlugin.Report(bufferData.data(), bufferData.size());
    if (ret >= 0) {
        memoryData.ParseFromArray(bufferData.data(), ret);
        return true;
    }

    return false;
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
    std::string str = "cp -r " + path + "utresources " + exepath;
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

void MemoryDataPluginTest::SetUpTestCase()
{
    g_path = GetFullPath(DEFAULT_TEST_PATH);
    printf("g_path:%s\n", g_path.c_str());
    EXPECT_NE("", g_path);
#if defined(__i386__) || defined(__x86_64__)
    if (DEFAULT_TEST_PATH != g_path) {
        if ((access(std::string(g_path + "utresources/proc").c_str(), F_OK) != 0) &&
            (access(std::string(DEFAULT_TEST_PATH + "utresources/proc").c_str(), F_OK) == 0)) {
            EXPECT_TRUE(CreatTestResource(DEFAULT_TEST_PATH, g_path));
        }
        g_path += "utresources/proc";
    }
#else
    g_path += "utresources/proc";
#endif
    printf("g_path:%s\n", g_path.c_str());
}

/**
 * @tc.name: memory plugin
 * @tc.desc: Test whether the path exists.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, TestUtpath, TestSize.Level1)
{
    EXPECT_NE(g_path, "");
    printf("g_path:%s\n", g_path.c_str());
}

/**
 * @tc.name: memory plugin
 * @tc.desc: Pid list test in a specific directory.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, Testpidlist, TestSize.Level1)
{
    MemoryDataPlugin* memoryPlugin = new MemoryDataPlugin();

    printf("g_path:%s\n", g_path.c_str());
    DIR* dir = memoryPlugin->OpenDestDir(g_path.c_str());
    EXPECT_NE(nullptr, dir);

    std::vector<int> cmpPidList;
    while (int32_t pid = memoryPlugin->GetValidPid(dir)) {
        printf("pid = %d\n", pid);
        cmpPidList.push_back(pid);
    }
    sort(cmpPidList.begin(), cmpPidList.end());
    closedir(dir);
    EXPECT_EQ(cmpPidList, g_expectPidList);
    delete memoryPlugin;
}

/**
 * @tc.name: memory plugin
 * @tc.desc: Mem information test for specific pid.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, Testpluginformeminfo, TestSize.Level1)
{
    MemoryDataPlugin memoryPlugin;
    MemoryData memoryData;
    MemoryConfig protoConfig;

    SetPluginSysMemConfig(protoConfig);
    EXPECT_TRUE(PluginStub(memoryPlugin, protoConfig, memoryData));

    EXPECT_EQ(16, memoryData.meminfo().size());
    int index = memoryData.processesinfo_size();
    for (int i = 0; i < index; ++i) {
        EXPECT_EQ(g_meminfo[i], memoryData.meminfo(i).value());
    }

    memoryPlugin.Stop();
}

/**
 * @tc.name: memory plugin
 * @tc.desc: pid list information test for process tree.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, Testpluginforlist, TestSize.Level1)
{
    MemoryDataPlugin memoryPlugin;
    MemoryData memoryData;
    MemoryConfig protoConfig;

    std::vector<int> cmpPidList;
    EXPECT_EQ((size_t)0, cmpPidList.size());

    memoryPlugin.SetPath(const_cast<char*>(g_path.c_str()));
    printf("Testpluginforlist:setPath=%s\n", g_path.c_str());

    SetPluginProcessConfig(cmpPidList, protoConfig);
    EXPECT_TRUE(PluginStub(memoryPlugin, protoConfig, memoryData));
    MyPrintfProcessMemoryInfo(memoryData);

    int index = memoryData.processesinfo_size();
    EXPECT_EQ(3, index);
    printf("Testpluginforlist:index=%d", index);
    for (int i = 0; i < index; ++i) {
        ProcessMemoryInfo it = memoryData.processesinfo(i);
        EXPECT_EQ(g_pidtarget[i].pid, it.pid());
        printf("%d:pid=%d\r\n", i, it.pid());
        EXPECT_EQ(g_pidtarget[i].name, it.name());
        EXPECT_EQ(g_pidtarget[i].vm_size_kb, it.vm_size_kb());
        EXPECT_EQ(g_pidtarget[i].vm_rss_kb, it.vm_rss_kb());
        EXPECT_EQ(g_pidtarget[i].rss_anon_kb, it.rss_anon_kb());
        EXPECT_EQ(g_pidtarget[i].rss_file_kb, it.rss_file_kb());
        EXPECT_EQ(g_pidtarget[i].rss_shmem_kb, it.rss_shmem_kb());
        EXPECT_EQ(g_pidtarget[i].vm_locked_kb, it.vm_locked_kb());
        EXPECT_EQ(g_pidtarget[i].vm_hwm_kb, it.vm_hwm_kb());

        EXPECT_EQ(g_pidtarget[i].oom_score_adj, it.oom_score_adj());

        EXPECT_FALSE(it.has_memsummary());
    }

    memoryPlugin.Stop();
}

/**
 * @tc.name: memory plugin
 * @tc.desc: pid list information test for specific pid.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, Testpluginforsinglepid, TestSize.Level1)
{
    MemoryDataPlugin memoryPlugin;
    MemoryData memoryData;
    MemoryConfig protoConfig;

    std::vector<int> pid = {5};

    memoryPlugin.SetPath(const_cast<char*>(g_path.c_str()));
    printf("Testpluginforsinglepid:setPath=%s\n", g_path.c_str());

    SetPluginProcessConfig(pid, protoConfig);
    EXPECT_TRUE(PluginStub(memoryPlugin, protoConfig, memoryData));
    MyPrintfProcessMemoryInfo(memoryData);

    int index = memoryData.processesinfo_size();
    EXPECT_EQ(1, index);
    printf("Testpluginforsinglepid:index=%d\n", index);

    ProcessMemoryInfo it = memoryData.processesinfo(0);
    EXPECT_EQ(g_singlepid.pid, it.pid());
    printf("pid=%d\r\n", it.pid());
    EXPECT_EQ(g_singlepid.name, it.name());
    EXPECT_EQ(g_singlepid.vm_size_kb, it.vm_size_kb());
    EXPECT_EQ(g_singlepid.vm_rss_kb, it.vm_rss_kb());
    EXPECT_EQ(g_singlepid.rss_anon_kb, it.rss_anon_kb());
    EXPECT_EQ(g_singlepid.rss_file_kb, it.rss_file_kb());
    EXPECT_EQ(g_singlepid.rss_shmem_kb, it.rss_shmem_kb());
    EXPECT_EQ(g_singlepid.vm_locked_kb, it.vm_locked_kb());
    EXPECT_EQ(g_singlepid.vm_hwm_kb, it.vm_hwm_kb());

    EXPECT_EQ(g_singlepid.oom_score_adj, it.oom_score_adj());

    EXPECT_TRUE(it.has_memsummary());
    AppSummary app = it.memsummary();
    EXPECT_EQ(g_singlepid.java_heap, app.java_heap());
    EXPECT_EQ(g_singlepid.native_heap, app.native_heap());
    EXPECT_EQ(g_singlepid.code, app.code());
    EXPECT_EQ(g_singlepid.stack, app.stack());
    EXPECT_EQ(g_singlepid.graphics, app.graphics());
    EXPECT_EQ(g_singlepid.private_other, app.private_other());

    memoryPlugin.Stop();
}

/**
 * @tc.name: memory plugin
 * @tc.desc: pid list information test for specific pids.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, Testpluginforpids, TestSize.Level1)
{
    MemoryDataPlugin memoryPlugin;
    MemoryData memoryData;
    MemoryConfig protoConfig;

    std::vector<int> cmpPidList = g_expectPidList;
    EXPECT_NE((size_t)0, cmpPidList.size());

    memoryPlugin.SetPath(const_cast<char*>(g_path.c_str()));
    printf("Testpluginforpids:setPath=%s\n", g_path.c_str());

    SetPluginProcessConfig(cmpPidList, protoConfig);
    EXPECT_TRUE(PluginStub(memoryPlugin, protoConfig, memoryData));
    MyPrintfProcessMemoryInfo(memoryData);

    int index = memoryData.processesinfo_size();
    EXPECT_EQ(3, index);
    printf("Testpluginforpids:index=%d\n", index);
    for (int i = 0; i < index; ++i) {
        ProcessMemoryInfo it = memoryData.processesinfo(i);
        EXPECT_EQ(g_pidtarget[i].pid, it.pid());
        printf("%d:pid=%d\r\n", i, it.pid());
        EXPECT_EQ(g_pidtarget[i].name, it.name());
        EXPECT_EQ(g_pidtarget[i].vm_size_kb, it.vm_size_kb());
        EXPECT_EQ(g_pidtarget[i].vm_rss_kb, it.vm_rss_kb());
        EXPECT_EQ(g_pidtarget[i].rss_anon_kb, it.rss_anon_kb());
        EXPECT_EQ(g_pidtarget[i].rss_file_kb, it.rss_file_kb());
        EXPECT_EQ(g_pidtarget[i].rss_shmem_kb, it.rss_shmem_kb());
        EXPECT_EQ(g_pidtarget[i].vm_locked_kb, it.vm_locked_kb());
        EXPECT_EQ(g_pidtarget[i].vm_hwm_kb, it.vm_hwm_kb());

        EXPECT_EQ(g_pidtarget[i].oom_score_adj, it.oom_score_adj());

        EXPECT_TRUE(it.has_memsummary());
    }

    memoryPlugin.Stop();
}

/**
 * @tc.name: memory plugin
 * @tc.desc: Smaps stats info test for specific pids.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, TestSmapsStatsInfo, TestSize.Level1)
{
    SmapsStats smap(std::string(g_path + "/"));
    for (size_t i = 0; i < g_expectPidList.size(); i++) {
        EXPECT_TRUE(smap.ParseMaps(g_expectPidList[i]));
        EXPECT_EQ(g_pidtarget[i].java_heap, (uint64_t)(smap.GetProcessJavaHeap()));
        EXPECT_EQ(g_pidtarget[i].native_heap, (uint64_t)(smap.GetProcessNativeHeap()));
        EXPECT_EQ(g_pidtarget[i].code, (uint64_t)(smap.GetProcessCode()));
        EXPECT_EQ(g_pidtarget[i].stack, (uint64_t)(smap.GetProcessStack()));
        EXPECT_EQ(g_pidtarget[i].graphics, (uint64_t)(smap.GetProcessGraphics()));
        EXPECT_EQ(g_pidtarget[i].private_other, (uint64_t)(smap.GetProcessPrivateOther()));
    }
}

/**
 * @tc.name: memory plugin
 * @tc.desc: Vmstat info test for specific pids.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, TestpluginWriteVmstat, TestSize.Level1)
{
    MemoryDataPlugin memoryPlugin;
    MemoryData memoryData;
    MemoryConfig protoConfig;

    protoConfig.set_report_sysmem_vmem_info(true);
    EXPECT_TRUE(PluginStub(memoryPlugin, protoConfig, memoryData));

    memoryPlugin.Stop();
}

/**
 * @tc.name: memory plugin
 * @tc.desc: Get information through Dumpsys.
 * @tc.type: FUNC
 */
HWTEST_F(MemoryDataPluginTest, TestpluginDumpsys, TestSize.Level1)
{
    MemoryDataPlugin memoryPlugin;
    MemoryData memoryData;
    MemoryConfig protoConfig;

    SetPluginDumpsysConfig(protoConfig);
    EXPECT_TRUE(PluginStub(memoryPlugin, protoConfig, memoryData));
    std::string line = "01234567890";
    memoryPlugin.ParseNumber(line);

    ProcessMemoryInfo it = memoryData.processesinfo(0);
    EXPECT_FALSE(it.has_memsummary());
    AppSummary app = it.memsummary();
    EXPECT_EQ((uint64_t)0, app.java_heap());
    EXPECT_EQ((uint64_t)0, app.native_heap());
    EXPECT_EQ((uint64_t)0, app.code());
    EXPECT_EQ((uint64_t)0, app.stack());
    EXPECT_EQ((uint64_t)0, app.graphics());
    EXPECT_EQ((uint64_t)0, app.private_other());

    memoryPlugin.Stop();
}
} // namespace

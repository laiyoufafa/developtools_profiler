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

#include <fcntl.h>
#include <gmock/gmock.h>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <vector>

#include "plugin_manager.h"
#include "plugin_watcher.h"

#include "logging.h"

using namespace testing::ext;

namespace {
static std::vector<std::string> g_cmpFileList;
static std::vector<std::string> g_createFileList = {
    "lib_6.so", "lib_5.so", "lib_8.so", "lib_4.so", "test1.txt"
};
std::vector<int> g_createFdList;

static std::vector<std::string> g_addFileList = {
    "libadd_6.so", "libadd_5.so", "libadd_8.so", "libadd_4.so", "test2.txt"
};

static std::vector<std::string> g_expectFileList = {
    "libadd_6.so", "libadd_5.so", "libadd_8.so", "libadd_4.so",
    "lib_6.so",    "lib_5.so",    "lib_8.so",    "lib_4.so"
};

static int g_defaultFileMode = 0777;

#if defined(__i386__) || defined(__x86_64__)
const static std::string DEFAULT_TEST_PATH = "./";
#else
const static std::string DEFAULT_TEST_PATH = "/data/local/tmp/";
#endif

class PluginWatchTest : public ::testing::Test {
protected:
    static constexpr int TEMP_DELAY = 10 * 1000;
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() override {}
    void TearDown() override {}
};

class MockPluginWatcher : public PluginWatcher {
public:
    MockPluginWatcher(const PluginManagerPtr& pluginManager) : PluginWatcher(pluginManager) {}
    ~MockPluginWatcher() = default;
    MOCK_METHOD1(OnPluginAdded, void(const std::string&));
    MOCK_METHOD1(OnPluginRemoved, void(const std::string&));
};

static void OnPluginAddedStub(const std::string& path)
{
    g_cmpFileList.push_back(path);
    sort(g_cmpFileList.begin(), g_cmpFileList.end());
    return;
}

static void OnPluginRemovedStub(const std::string& path)
{
    for (auto iter = g_cmpFileList.cbegin(); iter != g_cmpFileList.cend(); iter++) {
        if (*iter == path) {
            g_cmpFileList.erase(iter);
            break;
        }
    }

    return;
}

static void CreateFile()
{
    for (auto it : g_createFileList) {
        int fd = creat(it.c_str(), g_defaultFileMode);
        g_createFdList.push_back(fd);
    }
}

static void AddFile()
{
    for (auto it : g_addFileList) {
        int fd = creat(it.c_str(), g_defaultFileMode);
        if (fd < 0) {
            return;
        }
        write(fd, "testcase", 1);
        close(fd);
    }

    return;
}

static void DeleteFile()
{
    for (auto it : g_createFileList) {
        for (auto fd : g_createFdList) {
            close(fd);
        }
        remove(it.c_str());
    }
    for (auto it : g_addFileList) {
        remove(it.c_str());
    }
    return;
}

static bool CheckFileList()
{
    sort(g_expectFileList.begin(), g_expectFileList.end());
    if (g_expectFileList.size() != g_cmpFileList.size()) {
        return false;
    }

    for (size_t i = 0; i < g_expectFileList.size(); i++) {
        char fullpath[PATH_MAX + 1] = {0};
        realpath(g_expectFileList.at(i).c_str(), fullpath);
        if (g_cmpFileList.at(i) != fullpath) {
            return false;
        }
    }

    return true;
}

/**
 * @tc.name: plugin
 * @tc.desc: Monitor the plugin loading in the test directory.
 * @tc.type: FUNC
 */
HWTEST_F(PluginWatchTest, SingleWatchDirTest, TestSize.Level1)
{
    auto pluginManage = std::make_shared<PluginManager>();
    MockPluginWatcher watcher(pluginManage);

    EXPECT_CALL(watcher, OnPluginAdded(testing::_)).WillRepeatedly(testing::Invoke(OnPluginAddedStub));
    EXPECT_CALL(watcher, OnPluginRemoved(testing::_)).WillRepeatedly(testing::Invoke(OnPluginRemovedStub));

    g_createFdList.clear();
    CreateFile();
    watcher.ScanPlugins(DEFAULT_TEST_PATH);
    watcher.WatchPlugins(DEFAULT_TEST_PATH);
    usleep(TEMP_DELAY);
    AddFile();
    usleep(TEMP_DELAY);
    EXPECT_EQ(CheckFileList(), false);
    DeleteFile();
    usleep(TEMP_DELAY);
    EXPECT_EQ(g_cmpFileList.empty(), true);
}

/**
 * @tc.name: plugin
 * @tc.desc: Plug-in process exception test.
 * @tc.type: FUNC
 */
HWTEST_F(PluginWatchTest, OnPluginAdded, TestSize.Level1)
{
    const auto pluginManage = std::make_shared<PluginManager>();
    PluginWatcher pluginWatcher(pluginManage);
    pluginWatcher.OnPluginAdded("");
    pluginWatcher.OnPluginRemoved("");
}
} // namespace

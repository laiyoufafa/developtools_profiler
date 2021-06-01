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

#include <dlfcn.h>
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <iomanip>
#include <sys/time.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>
#include <vector>

#include "logging.h"
#include "memory_plugin_config.pb.h"
#include "memory_plugin_result.pb.h"
#include "plugin_module_api.h"

using namespace testing::ext;

#if defined(__i386__) || defined(__x86_64__)
const std::string LIB_PATH = "./hos/out/hos-arm/clang_x64/devtools/devtools/libmemdataplugin.z.so";
#else
const std::string LIB_PATH = "/system/lib/libmemdataplugin.z.so";
#endif

namespace {
enum NumType {
    BIT_WIDTH = 35,
    MS_S = 1000000,
    ERR_PARAM = 1,
    ERR_ADDR,
    ERR_START,
};

class PluginModuleApiTest : public ::testing::Test {
protected:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}

    void SetUp() override {}
    void TearDown() override {}

    bool MatchTail(const std::string& name, std::string str)
    {
        int index = name.size() - str.size();
        if (index < 0) {
            return false;
        }
        return (name.substr(index) == str);
    }

    bool ProcessErr(int errNum, void *handle, uint8_t* dataBuffer)
    {
        switch (errNum) {
            case ERR_ADDR:
                dlclose(handle);
                HILOG_DEBUG(LOG_CORE, "test：check addr err.");
                break;
            case ERR_START:
                dlclose(handle);
                free(dataBuffer);
                HILOG_DEBUG(LOG_CORE, "test：start err.");
                break;
            default:
                HILOG_DEBUG(LOG_CORE, "test：check param err.");
                break;
        }
        return false;
    }

    bool MemoryPluginTest(MemoryConfig& protoConfig, const std::string libPath)
    {
        MemoryData memoryData;
        if (!MatchTail(libPath, ".so")) {
            return ProcessErr(ERR_PARAM, nullptr, nullptr);
        }

        void* handle = dlopen(libPath.c_str(), RTLD_LAZY);
        if (handle == nullptr) {
            return ProcessErr(ERR_PARAM, nullptr, nullptr);
        }

        PluginModuleStruct* memPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
        if (memPlugin == nullptr) {
            return ProcessErr(ERR_ADDR, handle, nullptr);
        }
        uint8_t* dataBuffer = (uint8_t*)malloc(memPlugin->resultBufferSizeHint);
        if (dataBuffer == nullptr) {
            return ProcessErr(ERR_ADDR, handle, nullptr);
        }

        int configlength = protoConfig.ByteSizeLong();
        std::vector<uint8_t> config(configlength);
        protoConfig.SerializeToArray(config.data(), config.size());

        if (memPlugin->callbacks->onPluginSessionStart(config.data(), config.size()) < 0) {
            return ProcessErr(ERR_START, handle, dataBuffer);
        }

        int len = memPlugin->callbacks->onPluginReportResult(dataBuffer, memPlugin->resultBufferSizeHint);
        if (len > 0) {
            memoryData.ParseFromArray(dataBuffer, len);
        }

        memPlugin->callbacks->onPluginSessionStop();
        memPlugin->callbacks->onRegisterWriterStruct(nullptr);

        dlclose(handle);
        free(dataBuffer);
        return true;
    }
};

HWTEST_F(PluginModuleApiTest, ProtoConfigNullAndInvalidSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    EXPECT_FALSE(PluginModuleApiTest::MemoryPluginTest(protoConfig, "1111"));
}

HWTEST_F(PluginModuleApiTest, ProtoConfigNullAndEffectiveSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    EXPECT_TRUE(PluginModuleApiTest::MemoryPluginTest(protoConfig, LIB_PATH));
}

HWTEST_F(PluginModuleApiTest, ProtoConfigMemAndInvalidSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    protoConfig.set_report_process_mem_info(true);
    EXPECT_FALSE(PluginModuleApiTest::MemoryPluginTest(protoConfig, "1111"));
}

HWTEST_F(PluginModuleApiTest, ProtoConfigMemAndEffectiveSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    protoConfig.set_report_process_mem_info(true);
    EXPECT_TRUE(PluginModuleApiTest::MemoryPluginTest(protoConfig, LIB_PATH));
}

HWTEST_F(PluginModuleApiTest, ProtoConfigPidAndInvalidSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    protoConfig.set_report_app_mem_info(true);
    protoConfig.add_pid(1);
    EXPECT_FALSE(PluginModuleApiTest::MemoryPluginTest(protoConfig, "1111"));
}

HWTEST_F(PluginModuleApiTest, ProtoConfigPidAndEffectiveSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    protoConfig.set_report_app_mem_info(true);
    protoConfig.add_pid(1);
    EXPECT_TRUE(PluginModuleApiTest::MemoryPluginTest(protoConfig, LIB_PATH));
}

HWTEST_F(PluginModuleApiTest, MemoryPluginTreeAndInvalidSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    protoConfig.set_report_process_tree(true);
    EXPECT_FALSE(PluginModuleApiTest::MemoryPluginTest(protoConfig, "1111"));
}

HWTEST_F(PluginModuleApiTest, MemoryPluginTreeAndEffectiveSo, TestSize.Level1)
{
    MemoryConfig protoConfig;
    protoConfig.set_report_process_tree(true);
    EXPECT_TRUE(PluginModuleApiTest::MemoryPluginTest(protoConfig, LIB_PATH));
}

HWTEST_F(PluginModuleApiTest, ApiCallInvalidSoAndInvalidStruct, TestSize.Level1)
{
    PluginModuleStruct* memPlugin = nullptr;
    void* handle = dlopen(LIB_PATH.c_str(), RTLD_LAZY);
    ASSERT_NE(handle, nullptr);

    memPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
    ASSERT_NE(memPlugin, nullptr);

    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStart(nullptr, 0), 0);
    ASSERT_EQ(memPlugin->callbacks->onPluginReportResult(nullptr, 0), 0);
    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStop(), 0);
    ASSERT_EQ(memPlugin->callbacks->onRegisterWriterStruct(nullptr), 0);
}

HWTEST_F(PluginModuleApiTest, ApiCallEffectiveSoAndInvalidStruct, TestSize.Level1)
{
    PluginModuleStruct* memPlugin = nullptr;
    void* handle = dlopen(LIB_PATH.c_str(), RTLD_LAZY);
    ASSERT_NE(handle, nullptr);

    memPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
    ASSERT_NE(memPlugin, nullptr);

    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStart(nullptr, 0), 0);
    ASSERT_EQ(memPlugin->callbacks->onPluginReportResult(nullptr, 0), 0);
    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStop(), 0);
    ASSERT_EQ(memPlugin->callbacks->onRegisterWriterStruct(nullptr), 0);
}

HWTEST_F(PluginModuleApiTest, ApiCallInvalidSoAndEffectiveStruct, TestSize.Level1)
{
    PluginModuleStruct* memPlugin = nullptr;
    void* handle = dlopen(LIB_PATH.c_str(), RTLD_LAZY);
    ASSERT_NE(handle, nullptr);

    memPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
    ASSERT_NE(memPlugin, nullptr);

    WriterStruct writer;

    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStart(nullptr, 0), 0);
    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStop(), 0);
    ASSERT_EQ(memPlugin->callbacks->onRegisterWriterStruct(&writer), 0);
}

HWTEST_F(PluginModuleApiTest, ApiCallEffectiveSoAndEffectiveStruct, TestSize.Level1)
{
    PluginModuleStruct* memPlugin = nullptr;
    void* handle = dlopen(LIB_PATH.c_str(), RTLD_LAZY);
    ASSERT_NE(handle, nullptr);

    memPlugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");
    ASSERT_NE(memPlugin, nullptr);

    WriterStruct writer;
    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStart(nullptr, 0), 0);
    ASSERT_EQ(memPlugin->callbacks->onPluginSessionStop(), 0);
    ASSERT_EQ(memPlugin->callbacks->onRegisterWriterStruct(&writer), 0);
}
} // namespace

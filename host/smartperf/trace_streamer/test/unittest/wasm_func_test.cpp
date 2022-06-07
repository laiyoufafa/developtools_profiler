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

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "rpc/rpc_server.h"
#include "wasm_func.h"

using namespace testing::ext;
namespace SysTuning {
namespace TraceStreamer {
class WasmFuncTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }
    void TearDown() {}
public:
    TraceStreamerSelector stream_ = {};
};
constexpr int MAX_TESET_BUF_SIZE = 1024;

/**
 * @tc.name: CorrectTraceData
 * @tc.desc: Upload correct trace file data
 * @tc.type: FUNC
 */
HWTEST_F(WasmFuncTest, CorrectTraceData, TestSize.Level1)
{
    TS_LOGI("test23-1");
    std::string parseData(
        "ACCS0-2716  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3");
    std::string sqlQuery("select * from measure;");
    std::string sqlOperateInsert("insert into measure values (1, 1, 1, 1);");
    std::string sqlOperateDelete("delete from measure;");

    char out[MAX_TESET_BUF_SIZE] = {0};

    int ret = TraceStreamerParseData((const uint8_t*)parseData.c_str(), parseData.length());
    EXPECT_EQ(0, ret);
    ret = TraceStreamerParseDataOver();
    EXPECT_EQ(0, ret);
    ret = TraceStreamerSqlOperate((const uint8_t*)sqlOperateInsert.c_str(), sqlOperateInsert.length());
    EXPECT_EQ(0, ret);
    ret = TraceStreamerSqlQuery((const uint8_t*)sqlQuery.c_str(), sqlQuery.length(), (uint8_t*)out, MAX_TESET_BUF_SIZE);
    TS_LOGI("sql value:%s", out);
    EXPECT_NE(-1, ret);
    ret = TraceStreamerSqlOperate((const uint8_t*)sqlOperateDelete.c_str(), sqlOperateDelete.length());
    EXPECT_EQ(0, ret);
    ret = TraceStreamerSqlQuery((const uint8_t*)sqlQuery.c_str(), sqlQuery.length(), (uint8_t*)out, MAX_TESET_BUF_SIZE);
    TS_LOGI("sql value:%s", out);
    EXPECT_NE(-1, ret);
    ret = TraceStreamerReset();
    EXPECT_EQ(0, ret);
}

/**
 * @tc.name: WrongTraceData
 * @tc.desc: Upload wrong tracking file data
 * @tc.type: FUNC
 */
HWTEST_F(WasmFuncTest, WrongTraceData, TestSize.Level1)
{
    TS_LOGI("test23-2");
    std::string parseData(
        "ACCS0  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3");
    std::string sqlQuery("select * from measure_a;");
    std::string sqlOperateInsert("insert into measure_a values (1, 1, 1, 1);");
    std::string sqlOperateDelete("delete from measure_a;");

    char out[MAX_TESET_BUF_SIZE] = {0};

    int ret = TraceStreamerParseData((const uint8_t*)parseData.c_str(), parseData.length());
    EXPECT_EQ(-1, ret);
    ret = TraceStreamerParseDataOver();
    EXPECT_EQ(0, ret);
    ret = TraceStreamerSqlOperate((const uint8_t*)sqlOperateInsert.c_str(), sqlOperateInsert.length());
    EXPECT_EQ(-1, ret);
    ret = TraceStreamerSqlQuery((const uint8_t*)sqlQuery.c_str(), sqlQuery.length(), (uint8_t*)out, MAX_TESET_BUF_SIZE);
    TS_LOGI("sql value:%s", out);
    EXPECT_EQ(-1, ret);
    ret = TraceStreamerSqlOperate((const uint8_t*)sqlOperateDelete.c_str(), sqlOperateDelete.length());
    EXPECT_EQ(-1, ret);
    ret = TraceStreamerSqlQuery((const uint8_t*)sqlQuery.c_str(), sqlQuery.length(), (uint8_t*)out, MAX_TESET_BUF_SIZE);
    TS_LOGI("sql value:%s", out);
    EXPECT_EQ(-1, ret);
    ret = TraceStreamerReset();
    EXPECT_EQ(0, ret);
}
} // namespace TraceStreamer
} // namespace SysTuning
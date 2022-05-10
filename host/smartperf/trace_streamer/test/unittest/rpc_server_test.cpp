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

using namespace testing::ext;
namespace SysTuning {
namespace TraceStreamer {
class RpcServerTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }
    void TearDown() {}
public:
    TraceStreamerSelector stream_ = {};
};
std::string g_result;
void res(const std::string result)
{
    TS_LOGI("%s", result.c_str());
    g_result = result;
}

/**
 * @tc.name: CorrectTraceData
 * @tc.desc: Upload correct trace file data
 * @tc.type: FUNC
 */
HWTEST_F(RpcServerTest, CorrectTraceData, TestSize.Level1)
{
    TS_LOGI("test21-1");
    std::string PARSERDATA(
        "ACCS0-2716  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3");
    std::string SQLQUERY("select * from measure;");
    std::string SQLOPERATE_C("insert into measure values (1, 1, 1, 1);");
    std::string SQLOPERATE_D("delete from measure;");

    RpcServer rpcServer;
    auto ret = rpcServer.ParseData((const uint8_t*)PARSERDATA.c_str(), PARSERDATA.length(), res);
    EXPECT_TRUE(res);
    EXPECT_TRUE(ret);
    ret = rpcServer.ParseDataOver((const uint8_t*)PARSERDATA.c_str(), PARSERDATA.length(), res);
    EXPECT_TRUE(res);
    EXPECT_TRUE(ret);
    ret = rpcServer.SqlOperate((const uint8_t*)SQLOPERATE_C.c_str(), SQLOPERATE_C.length(), res);
    EXPECT_TRUE(res);
    EXPECT_TRUE(ret);
    ret = rpcServer.SqlQuery((const uint8_t*)SQLQUERY.c_str(), SQLQUERY.length(), res);
    EXPECT_TRUE(res);
    EXPECT_TRUE(ret);
    ret = rpcServer.SqlOperate((const uint8_t*)SQLOPERATE_D.c_str(), SQLOPERATE_D.length(), res);
    EXPECT_TRUE(res);
    EXPECT_TRUE(ret);
    ret = rpcServer.SqlQuery((const uint8_t*)SQLQUERY.c_str(), SQLQUERY.length(), res);
    EXPECT_TRUE(res);
    EXPECT_TRUE(ret);
}

/**
 * @tc.name: WrongTraceData
 * @tc.desc: Upload wrong tracking file data
 * @tc.type: FUNC
 */
HWTEST_F(RpcServerTest, WrongTraceData, TestSize.Level1)
{
    TS_LOGI("test21-2");
    std::string PARSERDATA(
        "ACCS0  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3");
    std::string SQLQUERY("select * from measure_e;");
    std::string SQLOPERATE_C("insert into measure_e values (1, 1, 1, 1);");
    std::string SQLOPERATE_D("delete from measure_e;");

    TS_LOGD("---TESR:Error Parse Data---\n");
    RpcServer rpcServer;
    auto ret = rpcServer.ParseData((const uint8_t*)PARSERDATA.c_str(), PARSERDATA.length(), res);
    EXPECT_FALSE(ret);
    TS_LOGD("---TESR:SQL operation statement error---\n");
    ret = rpcServer.SqlOperate((const uint8_t*)SQLOPERATE_C.c_str(), SQLOPERATE_C.length(), res);
    EXPECT_FALSE(ret);
    TS_LOGD("---TESR:SQL query statement error---\n");
    ret = rpcServer.SqlQuery((const uint8_t*)SQLQUERY.c_str(), SQLQUERY.length(), res);
    EXPECT_FALSE(ret);
    TS_LOGD("---TESR:SQL operation statement error---\n");
    ret = rpcServer.SqlOperate((const uint8_t*)SQLOPERATE_D.c_str(), SQLOPERATE_D.length(), res);
    EXPECT_FALSE(ret);
    TS_LOGD("---TESR:SQL query statement error---\n");
    ret = rpcServer.SqlQuery((const uint8_t*)SQLQUERY.c_str(), SQLQUERY.length(), res);
    EXPECT_FALSE(ret);
}
} // namespace TraceStreamer
} // namespace SysTuning
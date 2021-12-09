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
#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>
#include <thread>
#include <unistd.h>

#include "ftrace_fs_ops.h"
#include "ftrace_parser.h"

using FTRACE_NS::FtraceFsOps;
using FTRACE_NS::FtraceParser;
using FTRACE_NS::PerCpuStats;
using testing::ext::TestSize;

namespace {
#ifndef PAGE_SIZE
constexpr uint32_t PAGE_SIZE = 4096;
#endif
class FtraceParserTest : public ::testing::Test {
protected:
    void SetUp() override {}

    void TearDown() override {}
};

/*
 * @tc.name: Init
 * @tc.desc: test FtraceParser::Init with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(FtraceParserTest, Init, TestSize.Level1)
{
    FtraceParser parser;
    EXPECT_TRUE(parser.Init());
}

/*
 * @tc.name: SetupEvent
 * @tc.desc: test FtraceParser::Init with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(FtraceParserTest, SetupEvent, TestSize.Level1)
{
    FtraceParser parser;
    EXPECT_TRUE(parser.SetupEvent("sched", "sched_switch"));
}

/*
 * @tc.name: ParseSavedTgid
 * @tc.desc: test FtraceParser::ParseSavedTgid with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(FtraceParserTest, ParseSavedTgid, TestSize.Level1)
{
    FtraceParser parser;
    EXPECT_TRUE(parser.ParseSavedTgid(FtraceFsOps::GetInstance().GetSavedTgids()));
}

/*
 * @tc.name: ParseSavedCmdlines
 * @tc.desc: test FtraceParser::ParseSavedCmdlines with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(FtraceParserTest, ParseSavedCmdlines, TestSize.Level1)
{
    FtraceParser parser;
    EXPECT_TRUE(parser.ParseSavedCmdlines(FtraceFsOps::GetInstance().GetSavedCmdLines()));
}

/*
 * @tc.name: ParsePerCpuStatus
 * @tc.desc: test FtraceParser::ParsePerCpuStatus with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(FtraceParserTest, ParsePerCpuStatus, TestSize.Level1)
{
    FtraceParser parser;
    PerCpuStats perCpuStats = {};
    EXPECT_FALSE(parser.ParsePerCpuStatus(perCpuStats, ""));

    std::string perCpuStatsStr = FtraceFsOps::GetInstance().GetPerCpuStats(0);
    EXPECT_TRUE(parser.ParsePerCpuStatus(perCpuStats, perCpuStatsStr));
}

/*
 * @tc.name: ParsePage
 * @tc.desc: test SubEventParser::ParsePage with normal case.
 * @tc.type: FUNC
 */
HWTEST_F(FtraceParserTest, ParsePage, TestSize.Level1)
{
    std::vector<uint8_t> buffer(PAGE_SIZE, 0);
    std::string traceRaw = FtraceFsOps::GetInstance().GetRawTracePath(0);
    int fd = open(traceRaw.c_str(), O_EXCL);
    EXPECT_NE(fd, -1);

    EXPECT_TRUE(FtraceFsOps::GetInstance().ClearTraceBuffer());
    EXPECT_TRUE(FtraceFsOps::GetInstance().EnableEvent("sched", "sched_switch"));
    EXPECT_TRUE(FtraceFsOps::GetInstance().EnableTracing());
    std::this_thread::yield();
    EXPECT_EQ(read(fd, buffer.data(), buffer.size()), PAGE_SIZE);
    EXPECT_TRUE(FtraceFsOps::GetInstance().DisableTracing());

    FtraceCpuDetailMsg cpuDetailMsg = {};
    FtraceParser parser;
    EXPECT_TRUE(parser.ParsePage(cpuDetailMsg, buffer.data(), buffer.size()));
    EXPECT_EQ(close(fd), 0);
}
} // namespace
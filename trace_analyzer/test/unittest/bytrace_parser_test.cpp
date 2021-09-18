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
#include <memory>

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "parser/bytrace_parser/bytrace_parser.h"
#include "parser/common_types.h"
#include "trace_streamer_selector.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;

namespace SysTuning {
namespace TraceStreamer {
class BytraceParserTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }

    void TearDown() {}

public:
    SysTuning::TraceStreamer::TraceStreamerSelector stream_{};
    const char* dbPath = "/data/resource/out.db";
};

HWTEST_F(BytraceParserTest, ParseNoData, TestSize.Level1)
{
    TS_LOGI("test1-1");
    std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(1);
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    printf("xxx\n");
    bytraceParser.ParseTraceDataSegment(std::move(buf), 1);
    printf("xxx2\n");
    bytraceParser.WaitForParserEnd();
    printf("xxx3\n");
    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 0);
}

HWTEST_F(BytraceParserTest, ParseNoDataWhithLineFlag, TestSize.Level1)
{
    TS_LOGI("test1-2");
    constexpr uint32_t bufSize = 1024;
    std::unique_ptr<uint8_t[]> buf{new uint8_t[bufSize]{" \n"}};

    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), bufSize);
    bytraceParser.WaitForParserEnd();
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 1);
}

HWTEST_F(BytraceParserTest, ParseInvalidData, TestSize.Level1)
{
    TS_LOGI("test1-3");
    constexpr uint32_t bufSize = 1024;
    std::unique_ptr<uint8_t[]> buf{new uint8_t[bufSize]{"0123456789\n"}};
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), bufSize);
    bytraceParser.WaitForParserEnd();
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 1);
}

HWTEST_F(BytraceParserTest, ParseComment, TestSize.Level1)
{
    TS_LOGI("test1-4");
    constexpr uint32_t bufSize = 1024;
    std::unique_ptr<uint8_t[]> buf{new uint8_t[bufSize]{"TRACE: \n# tracer: nop \n# \n"}};
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), bufSize);
    bytraceParser.WaitForParserEnd();
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 2);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 1);
}

HWTEST_F(BytraceParserTest, ParseInvalidLines, TestSize.Level1)
{
    TS_LOGI("test1-5");
    constexpr uint32_t bufSize = 1024;
    std::unique_ptr<uint8_t[]> buf{new uint8_t[bufSize]{"\nafafda\n"}};
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataSegment(std::move(buf), bufSize);
    bytraceParser.WaitForParserEnd();
    stream_.traceDataCache_->ExportDatabase(dbPath);
    EXPECT_TRUE(access(dbPath, F_OK) == 0);
    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 2);
}

HWTEST_F(BytraceParserTest, ParseNormal, TestSize.Level1)
{
    TS_LOGI("test1-6");
    std::string str(
        "ACCS0-2716  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3\n");
    // BytraceLine line;
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataItem(str);
    bytraceParser.WaitForParserEnd();

    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 1);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 0);
}

HWTEST_F(BytraceParserTest, LineParser_abnormal_pid_err, TestSize.Level1)
{
    TS_LOGI("test1-7");
    std::string str(
        "ACCS0-27X6  ( 2519) [000] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3\n");
    // BytraceLine line;
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataItem(str);
    bytraceParser.WaitForParserEnd();

    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 1);
}

HWTEST_F(BytraceParserTest, LineParserWithInvalidCpu, TestSize.Level1)
{
    TS_LOGI("test1-8");
    std::string str(
        "ACCS0-2716  ( 2519) [00X] ...1 168758.662861: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3\n");
    // BytraceLine line;
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataItem(str);
    bytraceParser.WaitForParserEnd();

    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 1);
}

HWTEST_F(BytraceParserTest, LineParserWithInvalidTs, TestSize.Level1)
{
    TS_LOGI("test1-9");
    std::string str(
        "ACCS0-2716  ( 2519) [000] ...1 168758.662X61: binder_transaction: \
        transaction=25137708 dest_node=4336 dest_proc=924 dest_thread=0 reply=0 flags=0x10 code=0x3\n");
    // BytraceLine line;
    BytraceParser bytraceParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());
    bytraceParser.ParseTraceDataItem(str);
    bytraceParser.WaitForParserEnd();

    EXPECT_TRUE(bytraceParser.TraceCommentLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceValidLines() == 0);
    EXPECT_TRUE(bytraceParser.ParsedTraceInvalidLines() == 1);
}
} // namespace TraceStreamer
} // namespace SysTuning

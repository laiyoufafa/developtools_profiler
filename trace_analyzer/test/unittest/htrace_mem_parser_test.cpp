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
#include <unordered_map>

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "htrace_mem_parser.h"
#include "parser/common_types.h"
#include "trace_streamer_selector.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
class HtraceMemParserTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }

    void TearDown() {}

public:
    SysTuning::TraceStreamer::TraceStreamerSelector stream_;
    const char* dbPath_ = "out.db";
};

HWTEST_F(HtraceMemParserTest, ParseMemParse, TestSize.Level1)
{
    TS_LOGI("test7-1");
    HtraceMemParser* memParser = new HtraceMemParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());

    MemoryData tracePacket;
    ProcessMemoryInfo* memoryInfo = tracePacket.add_processesinfo();
    EXPECT_TRUE(memoryInfo != nullptr);
    int size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 1);
    uint64_t timeStamp = 1616439852302;
    BuiltinClocks clock = TS_CLOCK_REALTIME;

    memParser->Parse(tracePacket, timeStamp, clock);
    stream_.traceDataCache_->ExportDatabase(dbPath_);

    EXPECT_TRUE(access(dbPath_, F_OK) == 0);
    tracePacket.clear_processesinfo();
    delete memParser;

    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);
}

HWTEST_F(HtraceMemParserTest, ParseMemParseTestMeasureDataSize, TestSize.Level1)
{
    TS_LOGI("test7-1");
    HtraceMemParser* memParser = new HtraceMemParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());

    MemoryData tracePacket;
    ProcessMemoryInfo* memoryInfo = tracePacket.add_processesinfo();
    EXPECT_TRUE(memoryInfo != nullptr);
    int size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 1);
    uint64_t timeStamp = 1616439852302;
    BuiltinClocks clock = TS_CLOCK_REALTIME;
    memoryInfo->set_pid(12);
    memoryInfo->set_name("Process1");
    memoryInfo->set_vm_size_kb(1024);
    memoryInfo->set_vm_rss_kb(512);
    memoryInfo->set_rss_anon_kb(128);
    memoryInfo->set_rss_file_kb(128);

    memParser->Parse(tracePacket, timeStamp, clock);
    stream_.traceDataCache_->ExportDatabase(dbPath_);

    EXPECT_TRUE(access(dbPath_, F_OK) == 0);
    tracePacket.clear_processesinfo();
    delete memParser;

    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(1 == eventCount);

    EXPECT_TRUE(stream_.traceDataCache_->GetConstProcessData(1).pid_ == 12);
}

HWTEST_F(HtraceMemParserTest, ParseMemParseTestMeasureDataSize2, TestSize.Level1)
{
    TS_LOGI("test7-1");
    HtraceMemParser* memParser = new HtraceMemParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());

    MemoryData tracePacket;
    ProcessMemoryInfo* memoryInfo = tracePacket.add_processesinfo();
    EXPECT_TRUE(memoryInfo != nullptr);
    int size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 1);
    uint64_t timeStamp = 1616439852302;
    BuiltinClocks clock = TS_CLOCK_REALTIME;
    memoryInfo->set_pid(12);
    memoryInfo->set_name("Process1");
    memoryInfo->set_vm_size_kb(1024);
    memoryInfo->set_vm_rss_kb(512);
    memoryInfo->set_rss_anon_kb(128);
    memoryInfo->set_rss_file_kb(128);

    ProcessMemoryInfo* memoryInfo2 = tracePacket.add_processesinfo();
    EXPECT_TRUE(memoryInfo2 != nullptr);
    size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 2);
    timeStamp = 1616439852402;
    memoryInfo2->set_pid(13);
    memoryInfo2->set_name("Process2");
    memoryInfo2->set_vm_size_kb(1024);
    memoryInfo2->set_vm_rss_kb(512);
    memoryInfo2->set_rss_anon_kb(128);
    memoryInfo2->set_rss_file_kb(128);

    memParser->Parse(tracePacket, timeStamp, clock);
    stream_.traceDataCache_->ExportDatabase(dbPath_);

    EXPECT_TRUE(access(dbPath_, F_OK) == 0);
    tracePacket.clear_processesinfo();
    delete memParser;

    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(2 == eventCount);

    EXPECT_TRUE(stream_.traceDataCache_->GetConstProcessData(1).pid_ == 12);
    EXPECT_TRUE(stream_.traceDataCache_->GetConstProcessData(2).pid_ == 13);
}

HWTEST_F(HtraceMemParserTest, ParseMemParseTestMeasureDataSize3, TestSize.Level1)
{
    TS_LOGI("test7-1");
    HtraceMemParser* memParser = new HtraceMemParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());

    MemoryData tracePacket;
    ProcessMemoryInfo* memoryInfo = tracePacket.add_processesinfo();
    EXPECT_TRUE(memoryInfo != nullptr);
    int size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 1);
    uint64_t timeStamp = 1616439852302;
    BuiltinClocks clock = TS_CLOCK_REALTIME;

    ProcessMemoryInfo* memoryInfo2 = tracePacket.add_processesinfo();
    EXPECT_TRUE(memoryInfo2 != nullptr);
    size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 2);

    memParser->Parse(tracePacket, timeStamp, clock);
    stream_.traceDataCache_->ExportDatabase(dbPath_);

    EXPECT_TRUE(access(dbPath_, F_OK) == 0);
    tracePacket.clear_processesinfo();
    delete memParser;

    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(2 == eventCount);

    EXPECT_TRUE(stream_.traceDataCache_->GetConstMeasureData().Size() == MEM_MAX * 2);
}

HWTEST_F(HtraceMemParserTest, ParseMemParseInvalidData, TestSize.Level1)
{
    TS_LOGI("test7-1");
    HtraceMemParser* memParser = new HtraceMemParser(stream_.traceDataCache_.get(), stream_.streamFilters_.get());

    MemoryData tracePacket;
    int size = tracePacket.processesinfo_size();
    EXPECT_TRUE(size == 0);
    uint64_t timeStamp = 1616439852302;
    BuiltinClocks clock = TS_CLOCK_REALTIME;

    memParser->Parse(tracePacket, timeStamp, clock);
    delete memParser;

    auto eventCount = stream_.traceDataCache_->GetConstStatAndInfo().GetValue(TRACE_MEMORY, STAT_EVENT_RECEIVED);
    EXPECT_TRUE(0 == eventCount);
}
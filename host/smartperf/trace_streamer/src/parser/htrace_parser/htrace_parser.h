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

#ifndef HTRACE_PARSER_H
#define HTRACE_PARSER_H
#include <cstdint>
#include <limits>
#include <map>
#include <stdexcept>
#include <string>
#include <thread>
#include "common_types.h"
#include "htrace_clock_detail_parser.h"
#include "htrace_cpu_detail_parser.h"
#include "htrace_hidump_parser.h"
#include "htrace_hilog_parser.h"
#include "htrace_mem_parser.h"
#include "htrace_native_hook_parser.h"
#include "htrace_symbols_detail_parser.h"
#include "log.h"
#include "parser_base.h"
#include "trace_data/trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
class HtraceParser : public ParserBase {
public:
    HtraceParser(TraceDataCache* dataCache, const TraceStreamerFilters* filters);
    ~HtraceParser();
    void ParseTraceDataSegment(std::unique_ptr<uint8_t[]> bufferStr, size_t size) override;
    void WaitForParserEnd();
private:
    void ParseTraceDataItem(const std::string& buffer) override;
    void FilterData(HtraceDataSegment& dataSeg);
    void ParserData(HtraceDataSegment& dataSeg);
private:
    void ParseMemory(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg);
    void ParseHilog(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg);
    void ParseFtrace(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg);
    void ParseNativeHook(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg);
    void ParseFPS(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg);
    void ParseThread();
    int GetNextSegment();
    void FilterThread();
    enum ErrorCode {
        ERROR_CODE_EXIT = -2,
        ERROR_CODE_NODATA = -1
    };
    bool hasGotSegLength_ = false;
    bool hasGotHeader = false;
    uint32_t nextLength_ = 0;
    const size_t PACKET_SEG_LENGTH = 4;
    const size_t PACKET_HEADER_LENGTH = 1024;
    std::unique_ptr<HtraceCpuDetailParser> htraceCpuDetailParser_;
    std::unique_ptr<HtraceSymbolsDetailParser> htraceSymbolsDetailParser_;
    std::unique_ptr<HtraceMemParser> htraceMemParser_;
    std::unique_ptr<HtraceClockDetailParser> htraceClockDetailParser_;
    std::unique_ptr<HtraceHiLogParser> htraceHiLogParser_;
    std::unique_ptr<HtraceNativeHookParser> htraceNativeHookParser_;
    std::unique_ptr<HtraceHidumpParser> htraceHidumpParser_;
    std::atomic<bool> filterThreadStarted_{false};
    const int MAX_SEG_ARRAY_SIZE = 10000;
    std::unique_ptr<HtraceDataSegment[]> dataSegArray;
    int rawDataHead_ = 0;
    bool toExit_ = false;
    bool exited_ = false;
    int filterHead_ = 0;
    int parseHead_ = 0;
    const int sleepDur_ = 100;
    bool parseThreadStarted_ = false;
    const int maxThread_ = 4; // 4 is the best on ubuntu 113MB/s, max 138MB/s, 6 is best on mac m1 21MB/s,
    int parserThreadCount_ = 0;
    std::mutex dataSegMux_;
    bool noThread_ = true;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // HTRACE_PARSER_H_

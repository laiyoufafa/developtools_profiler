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
#ifndef TRACE_CONVERTER_H
#define TRACE_CONVERTER_H

#include <map>
#include "trace_file_reader.h"
#include "trace_plugin_result.pb.h"

class TracePluginResult;

class TraceConverter {
public:
    void SetInput(const std::string& input);
    void SetOutput(const std::string& output);
    bool Convert();
    void SetParseGid(bool parseGid);
    void SetParseFlags(bool parseFlags);

private:
    bool WriteInitialHeader();
    bool WriteFinalHeader();
    bool WriteEvent(const FtraceEvent& event, int cpu);
    std::string FormatEvent(const FtraceEvent& event, int cpu) noexcept;
    void ParseCpuStats(const FtraceCpuStatsMsg& cpuStats);
    void SummarizeStats();
    bool ReadAndParseEvents();
    bool ConvertAndWriteEvents();

    void PrintTextHeader(const std::string& msg);
    static void PrintCpuStats(const FtraceCpuStatsMsg& cpuStats);
    static void PrintTraceFileHeader(const TraceFileHeader& header);
    static std::string TraceFlagsToString(uint32_t flags, uint32_t preemptCount);

    std::string GenerateNewEntriesValue(uint32_t orginLength);
    std::string GenerateNewNumProcsValue(uint32_t originLength);

private:
    bool parseGid_ = false;
    bool parseFlags_ = false;
    int numProcessors_ = 0;
    std::vector<PerCpuStatsMsg> startStats_ = {};
    std::vector<PerCpuStatsMsg> endStats_ = {};
    PerCpuStatsMsg startSum_ = {};
    PerCpuStatsMsg endSum_ = {};
    uint32_t entriesInBuffer_ = 0;
    uint32_t entriesWritten_ = 0;
    uint32_t numberResults_ = 0;
    TraceFileReader reader_ = {};
    int outputFd_ = -1;
    std::string output_ = "";
    std::string input_ = "";
    std::string textHeader_ = "";
    std::vector<int> resultsIndex_ = {};
    std::map<uint64_t, std::string> kernelSymbols_ = {};
    std::vector<std::vector<FtraceEvent>> cpuEventQeueue_;
};
#endif // TRACE_CONVERTER_H
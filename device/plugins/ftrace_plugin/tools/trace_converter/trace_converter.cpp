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
#include "trace_converter.h"

#include <cinttypes>
#include <fcntl.h>
#include <sstream>
#include <tuple>
#include <regex>
#include <unistd.h>

#include "common_types.pb.h"
#include "event_formatter.h"
#include "file_utility.h"
namespace {
constexpr unsigned TS_MIN_LEN = 9;
constexpr unsigned US_DIGITS = 6;
constexpr unsigned PID_STR_MAX = 6;
constexpr unsigned CPU_STR_MAX = 3;
constexpr unsigned COMM_STR_MAX = 16;
constexpr unsigned TGID_STR_MAX = 5;
constexpr unsigned TRACE_TXT_HEADER_MAX = 1024;
constexpr char HEX_CHARS[] = "0123456789abcdef";
constexpr int OUTPUT_FILE_MODE = 0644;
constexpr int INVALID_FD = -1;
using Clock = std::chrono::steady_clock;
using TimePoint = Clock::time_point;
using MilliSeconds = std::chrono::milliseconds;
using EventFormatter = FTRACE_NS::EventFormatter;

/*
 * trace_flag_type is an enumeration that holds different
 * states when a trace occurs. These are:
 *  IRQS_OFF		- interrupts were disabled
 *  IRQS_NOSUPPORT	- arch does not support irqs_disabled_flags
 *  NEED_RESCHED	- reschedule is requested
 *  HARDIRQ		- inside an interrupt handler
 *  SOFTIRQ		- inside a softirq handler
 */
constexpr uint32_t TRACE_FLAG_IRQS_OFF = 0x01;
constexpr uint32_t TRACE_FLAG_IRQS_NOSUPPORT = 0x02;
constexpr uint32_t TRACE_FLAG_NEED_RESCHED = 0x04;
constexpr uint32_t TRACE_FLAG_HARDIRQ = 0x08;
constexpr uint32_t TRACE_FLAG_SOFTIRQ = 0x10;
constexpr uint32_t TRACE_FLAG_PREEMPT_RESCHED = 0x20;
constexpr uint32_t TRACE_FLAG_NMI = 0x40;

std::string ENTIRES_STATS_PREFIX = "# entries-in-buffer/entries-written:";
std::string PROCESSOR_NUMBER_PREFIX = "#P:";
std::string TRACE_TXT_HEADER_FORMAT = R"(# tracer: nop
#
# entries-in-buffer/entries-written: %lu/%lu   #P:%d
#
#                                      _-----=> irqs-off
#                                     / _----=> need-resched
#                                    | / _---=> hardirq/softirq
#                                    || / _--=> preempt-depth
#                                    ||| /     delay
#           TASK-PID    TGID   CPU#  ||||    TIMESTAMP  FUNCTION
#              | |        |      |   ||||       |         |
)";

long TimeDeltaMs(const TimePoint& a, const TimePoint& b)
{
    auto delta = std::chrono::duration_cast<MilliSeconds>(a - b);
    return static_cast<long>(delta.count());
}

constexpr uint8_t HALF_BYTE_SIZE = 4;
constexpr uint8_t HALF_BYTE_MASK = 0x0F;

uint8_t GetHighHalfByte(uint8_t value)
{
    return (value >> HALF_BYTE_SIZE) & HALF_BYTE_MASK;
}

uint8_t GetLowHalfByte(uint8_t value)
{
    return value & HALF_BYTE_MASK;
}
} // namespace

void TraceConverter::SetInput(const std::string& input)
{
    input_ = input;
}

void TraceConverter::SetOutput(const std::string& output)
{
    output_ = output;
}

void TraceConverter::PrintTraceFileHeader(const TraceFileHeader& header)
{
    HILOG_INFO(LOG_CORE, "======== TRACE FILE HEADER ========");
    HILOG_INFO(LOG_CORE, "magic: %" PRIx64, header.data_.magic_);
    HILOG_INFO(LOG_CORE, "length: %" PRIu64, header.data_.length_);
    HILOG_INFO(LOG_CORE, "version: %" PRIx32, header.data_.version_);
    HILOG_INFO(LOG_CORE, "segments: %" PRIx32, header.data_.segments_);
    std::string sha = "";
    for (size_t i = 0; i < std::size(header.data_.sha256_); i++) {
        sha.push_back(HEX_CHARS[GetHighHalfByte(header.data_.sha256_[i])]);
        sha.push_back(HEX_CHARS[GetLowHalfByte(header.data_.sha256_[i])]);
    }
    HILOG_INFO(LOG_CORE, "SHA256: %s", sha.c_str());
    HILOG_INFO(LOG_CORE, "-------- -------- -------- --------");
}

void TraceConverter::PrintCpuStats(const FtraceCpuStatsMsg& cpuStats)
{
    auto status = cpuStats.status();
    std::string statusName = "";
    if (status == FtraceCpuStatsMsg::TRACE_START) {
        statusName = "TRACE_START";
    } else if (status == FtraceCpuStatsMsg::TRACE_END) {
        statusName = "TRACE_END";
    }
    HILOG_INFO(LOG_CORE, "---- ---- ---- ---- ---- ----");
    HILOG_INFO(LOG_CORE, "FtraceCpuStatsMsg:");
    HILOG_INFO(LOG_CORE, "status: %s", statusName.c_str());
    for (int i = 0; i < cpuStats.per_cpu_stats_size(); i++) {
        HILOG_INFO(LOG_CORE, "per_cpu_stats[%d]: {", i);
        HILOG_INFO(LOG_CORE, "  cpu: %" PRIu64, cpuStats.per_cpu_stats(i).cpu());
        HILOG_INFO(LOG_CORE, "  entries: %" PRIu64, cpuStats.per_cpu_stats(i).entries());
        HILOG_INFO(LOG_CORE, "  overrun: %" PRIu64, cpuStats.per_cpu_stats(i).overrun());
        HILOG_INFO(LOG_CORE, "  commit_overrun: %" PRIu64, cpuStats.per_cpu_stats(i).commit_overrun());
        HILOG_INFO(LOG_CORE, "  bytes: %" PRIu64, cpuStats.per_cpu_stats(i).bytes());
        HILOG_INFO(LOG_CORE, "  oldest_event_ts: %.06f", cpuStats.per_cpu_stats(i).oldest_event_ts());
        HILOG_INFO(LOG_CORE, "  now_ts: %.06f", cpuStats.per_cpu_stats(i).now_ts());
        HILOG_INFO(LOG_CORE, "  dropped_events: %" PRIu64, cpuStats.per_cpu_stats(i).dropped_events());
        HILOG_INFO(LOG_CORE, "  read_events: %" PRIu64, cpuStats.per_cpu_stats(i).read_events());
        HILOG_INFO(LOG_CORE, "}");
    }
    HILOG_INFO(LOG_CORE, "---- ---- ---- ---- ---- ----");
}

void TraceConverter::ParseCpuStats(const FtraceCpuStatsMsg& cpuStats)
{
    auto status = cpuStats.status();
    decltype(&startStats_) pStats = nullptr;
    if (status == FtraceCpuStatsMsg::TRACE_START) {
        startStats_.clear();
        pStats = &startStats_;
    } else if (status == FtraceCpuStatsMsg::TRACE_END) {
        endStats_.clear();
        pStats = &endStats_;
    }
    for (int i = 0; i < cpuStats.per_cpu_stats_size(); i++) {
        if (pStats) {
            pStats->push_back(cpuStats.per_cpu_stats(i));
        }
    }
}

void TraceConverter::SummarizeStats()
{
    auto sumCpuStats = [](const std::vector<PerCpuStatsMsg>& cpuStats) -> PerCpuStatsMsg {
        PerCpuStatsMsg sum = {};
        for (auto stats : cpuStats) {
            sum.set_entries(sum.entries() + stats.entries());
            sum.set_read_events(sum.read_events() + stats.read_events());
        }
        return sum;
    };
    endSum_ = sumCpuStats(endStats_);
    startSum_ = sumCpuStats(startStats_);
    entriesInBuffer_ = endSum_.read_events() - startSum_.read_events();
}

// refers kernel function trace_print_lat_fmt
// https://github.com/torvalds/linux/blob/v4.19/kernel/trace/trace_output.c#L447
std::string TraceConverter::TraceFlagsToString(uint32_t flags, uint32_t preemptCount)
{
    std::string result = "";
    char irqsOff = '.';
    if (flags & TRACE_FLAG_IRQS_OFF) {
        irqsOff = 'd';
    } else if (flags & TRACE_FLAG_IRQS_NOSUPPORT) {
        irqsOff = 'X';
    }
    result.push_back(irqsOff);

    char needResched = '.';
    bool isNeedResched = flags & TRACE_FLAG_NEED_RESCHED;
    bool isPreemptResched = flags & TRACE_FLAG_PREEMPT_RESCHED;
    if (isNeedResched && isPreemptResched) {
        needResched = 'N';
    } else if (isNeedResched) {
        needResched = 'n';
    } else if (isPreemptResched) {
        needResched = 'p';
    }
    result.push_back(needResched);

    bool nmiFlag = flags & TRACE_FLAG_NMI;
    bool hardIrq = flags & TRACE_FLAG_HARDIRQ;
    bool softIrq = flags & TRACE_FLAG_SOFTIRQ;
    char irqChar = '.';
    if (nmiFlag && hardIrq) {
        irqChar = 'Z';
    } else if (nmiFlag) {
        irqChar = 'z';
    } else if (hardIrq && softIrq) {
        irqChar = 'H';
    } else if (hardIrq) {
        irqChar = 'h';
    } else if (softIrq) {
        irqChar = 's';
    }
    result.push_back(irqChar);

    if (preemptCount) {
        result.push_back("0123456789abcdef"[preemptCount & 0x0F]);
    } else {
        result.push_back('.');
    }
    return result;
}

std::string TraceConverter::FormatEvent(const FtraceEvent& event, int cpu) noexcept
{
    std::stringstream sout;
    int pid = event.common_fields().pid();

    // TASK(comm) part, refers __trace_find_cmdline
    // https://github.com/torvalds/linux/blob/v4.19/kernel/trace/trace.c#L1978
    std::string comm = "<...>";
    if (comm.size()) {
        comm = event.comm();
    }
    if (pid == 0) {
        comm = "<idle>";
    }
    if (comm.size() < COMM_STR_MAX) {
        comm = std::string(COMM_STR_MAX - comm.size(), ' ') + comm;
    }
    sout << comm << '-';

    // PID part
    std::string pidStr = std::to_string(pid);
    if (pidStr.size() < PID_STR_MAX) {
        pidStr.resize(PID_STR_MAX, ' ');
    }
    sout << pidStr;

    // TGID part
    std::string tgidStr = "-----";
    if (parseGid_ && event.tgid() != 0) {
        tgidStr = std::to_string(event.tgid());
        if (tgidStr.size() < TGID_STR_MAX) {
            tgidStr.insert(tgidStr.begin(), TGID_STR_MAX - tgidStr.size(), ' ');
        }
    }
    sout << '(' << tgidStr << ')';

    // CPU# part
    std::string cpuStr = std::to_string(cpu);
    if (cpuStr.size() < CPU_STR_MAX) {
        cpuStr = std::string(CPU_STR_MAX - cpuStr.size(), '0') + cpuStr;
    }
    sout << " [" << cpuStr << "] ";

    // flags part
    uint32_t flags = event.common_fields().flags();
    uint32_t preeptCount = event.common_fields().preempt_count();
    if (parseFlags_ && (flags | preeptCount)) {
        sout << TraceFlagsToString(flags, preeptCount) << " ";
    } else {
        sout << ".... ";
    }

    // TIMESTAMP part
    std::string timestamp = "";
    timestamp = std::to_string(event.timestamp());
    auto tsLength = timestamp.length();
    CHECK_TRUE(tsLength > TS_MIN_LEN, "", "invalid timestamp!");
    std::string tsSecs = timestamp.substr(0, timestamp.size() - TS_MIN_LEN);
    std::string tsMicroSecs = timestamp.substr(timestamp.size() - TS_MIN_LEN, US_DIGITS);
    sout << tsSecs << '.' << tsMicroSecs << ": ";

    // FUNCTION part
    sout << EventFormatter::GetInstance().FormatEvent(event) << "\n";
    return sout.str();
}

bool TraceConverter::WriteEvent(const FtraceEvent& event, int cpu)
{
    std::string line = FormatEvent(event, cpu);
    CHECK_TRUE(line.size() > 0, false, "format event failed!");
    auto nbytes = write(outputFd_, line.data(), line.size());
    CHECK_TRUE(static_cast<size_t>(nbytes) == line.size(), false, "write event line FAILED!");
    return true;
}

void TraceConverter::PrintTextHeader(const std::string& msg)
{
    HILOG_INFO(LOG_CORE, "==== ==== ==== %s ==== ==== ====", msg.c_str());
    HILOG_INFO(LOG_CORE, "%s", textHeader_.c_str());
    HILOG_INFO(LOG_CORE, "---- ---- ---- %s ---- ---- ----", msg.c_str());
}

bool TraceConverter::WriteInitialHeader()
{
    std::vector<char> buffer(TRACE_TXT_HEADER_MAX);
    int used = snprintf_s(buffer.data(), buffer.size(), buffer.size() - 1, TRACE_TXT_HEADER_FORMAT.c_str(), UINT32_MAX,
                          UINT32_MAX, UINT8_MAX);
    CHECK_TRUE(used > 0, false, "format initial header failed!");
    textHeader_.assign(&buffer[0], &buffer[used]);

    auto nbytes = write(outputFd_, textHeader_.data(), textHeader_.size());
    CHECK_TRUE(static_cast<size_t>(nbytes) == textHeader_.size(), false, "write inital header failed!");
    PrintTextHeader("INITAL TRACE HEADER");
    return true;
}

std::string TraceConverter::GenerateNewEntriesValue(uint32_t orginLength)
{
    std::string newEntriesValue = std::to_string(entriesInBuffer_);
    newEntriesValue += '/';
    newEntriesValue += std::to_string(entriesWritten_);
    if (newEntriesValue.size() < orginLength) {
        size_t paddingSpaces = orginLength - newEntriesValue.size();
        std::string leftPadding(paddingSpaces >> 1, ' ');
        std::string rightPadding(paddingSpaces - leftPadding.size(), ' ');
        newEntriesValue = leftPadding + newEntriesValue + rightPadding; // align center
    }
    return newEntriesValue;
}

std::string TraceConverter::GenerateNewNumProcsValue(uint32_t originLength)
{
    std::string newNumProcsValue = std::to_string(numProcessors_);
    if (newNumProcsValue.size() < originLength) {
        size_t padding = originLength - newNumProcsValue.size();
        std::string spaces(padding, ' ');
        newNumProcsValue = spaces + newNumProcsValue; // align right
    }
    return newNumProcsValue;
}

//                                                                      nppEnd
//                                                                         |
// format: # entries-in-buffer/entries-written: 4294967295/4294967295   #P:255\n
//         |                                   |                        |     |
//        esp                                espEnd                    npp   eol
bool TraceConverter::WriteFinalHeader()
{
    HILOG_INFO(LOG_CORE, "WriteFinalHeader start!");
    auto esp = textHeader_.find(ENTIRES_STATS_PREFIX); // entries stats prefix start
    CHECK_TRUE(esp != std::string::npos, false, "entries stats prefix not found!");
    auto espEnd = esp + ENTIRES_STATS_PREFIX.size(); // end of entries stats prefix

    auto npp = textHeader_.find(PROCESSOR_NUMBER_PREFIX, espEnd); // number processor prefix
    CHECK_TRUE(npp, false, "number processors prefix not found!");
    auto nppEnd = npp + PROCESSOR_NUMBER_PREFIX.size();

    auto eol = textHeader_.find('\n', nppEnd); // end of line
    CHECK_TRUE(eol != std::string::npos, false, "entries stats line end not found!");

    size_t entriesValueLen = npp - espEnd;
    std::string newEntriesValue = GenerateNewEntriesValue(entriesValueLen);
    CHECK_TRUE(newEntriesValue.size() == entriesValueLen, false, "entries value length mismatch: %zu/%zu",
               entriesValueLen, newEntriesValue.size());
    HILOG_DEBUG(LOG_CORE, "newEntriesValue: '%s'", newEntriesValue.c_str());

    size_t numProcsValueLen = eol - nppEnd;
    std::string newNumProcsValue = GenerateNewNumProcsValue(numProcsValueLen);
    CHECK_TRUE(newNumProcsValue.size() == numProcsValueLen, false, "numProcs value length mismatch: %zu/%zu",
               numProcsValueLen, newNumProcsValue.size());
    HILOG_DEBUG(LOG_CORE, "newNumProcsValue: '%s'", newNumProcsValue.c_str());

    auto headerLength = textHeader_.size();
    textHeader_.replace(nppEnd, newNumProcsValue.size(), newNumProcsValue);
    textHeader_.replace(espEnd, newEntriesValue.size(), newEntriesValue);
    CHECK_TRUE(textHeader_.size() == headerLength, false, "header size mismatch %zu => %zu!", headerLength,
               textHeader_.size());

    lseek(outputFd_, 0, SEEK_SET); // move write postion to file head
    auto nbytes = write(outputFd_, textHeader_.data(), textHeader_.size());
    CHECK_TRUE(static_cast<size_t>(nbytes) == textHeader_.size(), false, "write final header failed!");
    PrintTextHeader("FINAL TRACE HEADER");

    // flush file buffer
    std::string outputPath = CanonicalizeSpecPath(output_.c_str());
    if (outputPath == "") {
         HILOG_ERROR(LOG_CORE, "%s:path is invalid: %s, errno=%d", __func__, output_.c_str(), errno);
         return false;
    }
    CHECK_TRUE(fsync(outputFd_) == 0, false, "fsync %s FAILED, %d", outputPath.c_str(), errno);
    HILOG_INFO(LOG_CORE, "WriteFinalHeader done!");
    return true;
}

bool TraceConverter::ReadAndParseEvents()
{
    long resultCount = 0;
    ProfilerPluginData pluginData;
    auto parseStart = Clock::now();
    while (reader_.Read(pluginData) > 0) {
        resultCount++;
        if (pluginData.name().find("trace") == std::string::npos) {
            continue;
        }

        TracePluginResult result;
        auto& data = pluginData.data();
        CHECK_TRUE(result.ParseFromArray(data.data(), data.size()), false, "parse result failed!");

        std::string progresss = "[" + std::to_string(resultCount) + "/" + std::to_string(numberResults_) + "]";
        if (result.ftrace_cpu_stats_size() > 0) {
            HILOG_INFO(LOG_CORE, "%s parse %d cpu stats result...", progresss.c_str(), result.ftrace_cpu_stats_size());
            for (auto& cpuStats : result.ftrace_cpu_stats()) {
                PrintCpuStats(cpuStats);
                ParseCpuStats(cpuStats);
                numProcessors_ = std::max(numProcessors_, cpuStats.per_cpu_stats_size());
            }
            cpuEventQeueue_.resize(numProcessors_, {});
        } else if (result.symbols_detail_size() > 0) {
            HILOG_INFO(LOG_CORE, "%s parse %d kernel symbols ...", progresss.c_str(), result.symbols_detail_size());
            for (auto& msg : result.symbols_detail()) {
                kernelSymbols_[msg.symbol_addr()] = msg.symbol_name();
            }
        } else if (result.ftrace_cpu_detail_size() > 0) {
            HILOG_INFO(LOG_CORE, "%s parse %d cpu details...", progresss.c_str(), result.ftrace_cpu_detail_size());
            for (auto& details : result.ftrace_cpu_detail()) {
                const int cpu = details.cpu();
                auto& eventQ = cpuEventQeueue_[cpu];
                eventQ.reserve(eventQ.size() + details.event_size());
                for (int i = 0; i < details.event_size(); i++) {
                    eventQ.push_back(details.event(i));
                }
            }
        } else {
            HILOG_WARN(LOG_CORE, "WARNING: other types of result...");
        }
    }
    auto parseDone = Clock::now();
    HILOG_INFO(LOG_CORE, "parsed done, results: %ld, time cost: %ld ms!", resultCount,
               TimeDeltaMs(parseDone, parseStart));
    HILOG_INFO(LOG_CORE, "CPU numbers: %d", numProcessors_);
    return true;
}

bool TraceConverter::ConvertAndWriteEvents()
{
    uint64_t totalEvents = 0;
    HILOG_INFO(LOG_CORE, "summary of events on each CPU:");
    for (size_t i = 0; i < cpuEventQeueue_.size(); i++) {
        totalEvents += cpuEventQeueue_[i].size();
        HILOG_INFO(LOG_CORE, "   events on CPU%zu: %zu", i, cpuEventQeueue_[i].size());
    }
    HILOG_INFO(LOG_CORE, "events on all CPU: %" PRIu64, totalEvents);

    // GOAL: sort all events, and write its to file.
    // on each event list, the events are ordered by timestamp
    // so we can used multi-way merge algorithm.
    std::vector<uint32_t> queueFront(numProcessors_, 0);
    HILOG_INFO(LOG_CORE, "start convert and write events to output...");
    auto writeStart = Clock::now();
    for (uint64_t i = 0; i < totalEvents; i++) {
        // firstly, pick an event with smallest timestamp
        uint32_t cpuId = 0;
        uint64_t minTs = UINT64_MAX;
        for (int c = 0; c < numProcessors_; c++) {
            uint32_t cursor = queueFront[c];
            if (cursor >= cpuEventQeueue_[c].size()) { // no more event on this queue
                continue;
            }
            uint64_t ts = cpuEventQeueue_[c][cursor].timestamp();
            if (ts < minTs) {
                minTs = ts;
                cpuId = c;
            }
        }
        // secondly, write picked event to file
        CHECK_TRUE(WriteEvent(cpuEventQeueue_[cpuId][queueFront[cpuId]], cpuId), false,
                   "write event [%" PRIu64 "] for CPU%u failed!", i, cpuId);
        queueFront[cpuId]++;
        float progress = i * 100.0f / totalEvents;
        HILOG_INFO(LOG_CORE, "progress = % 3.2f, ts = %" PRIu64 ", cpu = %u", progress, minTs, cpuId);
    }
    printf("\n");
    auto writeDone = Clock::now();
    HILOG_INFO(LOG_CORE, "convert and write events done, time cost: %ld ms!", TimeDeltaMs(writeDone, writeStart));
    entriesWritten_ = static_cast<decltype(entriesWritten_)>(totalEvents);
    return totalEvents;
}

bool TraceConverter::Convert()
{
    auto startTime = Clock::now();
    // process file path
    std::string resolvedPathInput = CanonicalizeSpecPath(input_.c_str());
    if (resolvedPathInput == "") {
         HILOG_ERROR(LOG_CORE, "%s:path is invalid: %s, errno=%d", __func__, input_.c_str(), errno);
         return false;
    }
    CHECK_TRUE(access(resolvedPathInput.c_str(), R_OK) == 0, false, "input %s not found!", resolvedPathInput.c_str());
    CHECK_TRUE(reader_.Open(resolvedPathInput), false, "open %s failed!", resolvedPathInput.c_str());

    auto header = reader_.GetHeader();
    PrintTraceFileHeader(header);
    numberResults_ = (header.data_.segments_ >> 1); // pairs of (length segment, data segment)
    HILOG_INFO(LOG_CORE, "number of results in trace file header: %u", numberResults_);

    // process file path
    std::string resolvedPathOutput = CanonicalizeSpecPath(output_.c_str());
    if (resolvedPathOutput == "") {
         HILOG_ERROR(LOG_CORE, "%s:path is invalid: %s, errno=%d", __func__, output_.c_str(), errno);
         return false;
    }
    std::regex dirNameRegex("[~-]|[.]{2}");
    std::regex fileNameRegex("[\\/:*?\"<>|]");
    size_t pos = resolvedPathOutput.rfind("/");
    if (pos != std::string::npos) {
        std::string dirName = resolvedPathOutput.substr(0, pos + 1);
        std::string fileName = resolvedPathOutput.substr(pos + 1, resolvedPathOutput.length() - pos - 1);
        if (std::regex_search(dirName, dirNameRegex) || std::regex_search(fileName, fileNameRegex)) {
            HILOG_ERROR(LOG_CORE, "%s:path is invalid: %s, errno=%d", __func__, resolvedPathOutput.c_str(), errno);
            return false;
        }
    } else {
        if (std::regex_search(resolvedPathOutput, fileNameRegex)) {
            HILOG_ERROR(LOG_CORE, "%s:path is invalid: %s, errno=%d", __func__, resolvedPathOutput.c_str(), errno);
            return false;
        }
    }
    
    outputFd_ = open(resolvedPathOutput.c_str(), O_CREAT | O_RDWR, OUTPUT_FILE_MODE);
    CHECK_TRUE(outputFd_ != -1, false, "open %s failed!", resolvedPathOutput.c_str());
    CHECK_TRUE(WriteInitialHeader(), false, "write initial header failed!");

    CHECK_TRUE(ReadAndParseEvents(), false, "read and parse events failed!");
    CHECK_TRUE(ConvertAndWriteEvents(), false, "convert and write events failed!");
    SummarizeStats();

    CHECK_TRUE(WriteFinalHeader(), false, "write final header failed!");
    CHECK_TRUE(close(outputFd_) == 0, false, "close %s FAILED, %d", resolvedPathOutput.c_str(), errno);
    outputFd_ = INVALID_FD;

    auto endTime = Clock::now();
    HILOG_INFO(LOG_CORE, "convert %s to %s done!", resolvedPathInput.c_str(), resolvedPathOutput.c_str());
    HILOG_INFO(LOG_CORE, "total time cost: %ld ms!", TimeDeltaMs(endTime, startTime));
    return true;
}

void TraceConverter::SetParseGid(bool parseGid)
{
    parseGid_ = parseGid;
}

void TraceConverter::SetParseFlags(bool parseFlags)
{
    parseFlags_ = parseFlags;
}

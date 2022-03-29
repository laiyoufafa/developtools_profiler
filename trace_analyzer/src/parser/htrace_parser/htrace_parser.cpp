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

#include "htrace_parser.h"
#include <unistd.h>
#include "binder_filter.h"
#include "cpu_filter.h"
#include "ftrace_event.pb.h"
#include "log.h"
#include "memory_plugin_result.pb.h"
#include "services/common_types.pb.h"
#include "stat_filter.h"
#include "trace_plugin_config.pb.h"
#include "trace_plugin_result.pb.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceParser::HtraceParser(TraceDataCache* dataCache, const TraceStreamerFilters* filters)
    : ParserBase(filters),
      htraceCpuDetailParser_(std::make_unique<HtraceCpuDetailParser>(dataCache, filters)),
      htraceSymbolsDetailParser_(std::make_unique<HtraceSymbolsDetailParser>(dataCache, filters)),
      htraceMemParser_(std::make_unique<HtraceMemParser>(dataCache, filters)),
      htraceClockDetailParser_(std::make_unique<HtraceClockDetailParser>(dataCache, filters)),
      htraceHiLogParser_(std::make_unique<HtraceHiLogParser>(dataCache, filters)),
      dataSegArray(new HtraceDataSegment[MAX_SEG_ARRAY_SIZE])
{
}

HtraceParser::~HtraceParser()
{
    TS_LOGI("clockid 2 is for RealTime and 1 is for BootTime");
}

void HtraceParser::WaitForParserEnd()
{
    if (parseThreadStarted_ || filterThreadStarted_) {
        toExit_ = true;
        while (!exited_) {
            usleep(sleepDur_ * sleepDur_);
        }
    }
    streamFilters_->cpuFilter_->FinishCpuEvent();
    streamFilters_->binderFilter_->FinishBinderEvent();
    htraceHiLogParser_->Finish();
    htraceMemParser_->Finish();
}

void HtraceParser::ParseTraceDataItem(const std::string& buffer)
{
    while (!toExit_) {
        int head = rawDataHead_;
        if (dataSegArray[head].status.load() != TS_PARSE_STATUS_INIT) {
            usleep(sleepDur_);
            continue;
        }
        dataSegArray[head].seg = std::move(buffer);
        dataSegArray[head].status = TS_PARSE_STATUS_SEPRATED;
        rawDataHead_ = (rawDataHead_ + 1) % MAX_SEG_ARRAY_SIZE;
        break;
    }
    if (!parseThreadStarted_) {
        parseThreadStarted_ = true;
        int tmp = maxThread_;
        while (tmp--) {
            parserThreadCount_++;
            std::thread ParseTypeThread(&HtraceParser::ParseThread, this);
            ParseTypeThread.detach();
            TS_LOGI("parser Thread:%d/%d start working ...\n", maxThread_ - tmp, maxThread_);
        }
    }
}
void HtraceParser::FilterThread()
{
    while (1) {
        HtraceDataSegment& seg = dataSegArray[filterHead_];
        if (seg.status.load() == TS_PARSE_STATUS_INVALID) {
            seg.status = TS_PARSE_STATUS_INIT;
            filterHead_ = (filterHead_ + 1) % MAX_SEG_ARRAY_SIZE;
            streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_INVALID);
            TS_LOGD("seprateHead_d:\t%d, parseHead_:\t%d, filterHead_:\t%d\n", rawDataHead_, parseHead_, filterHead_);
            continue;
        }
        if (seg.status.load() != TS_PARSE_STATUS_PARSED) {
            if (toExit_ && !parserThreadCount_) {
                TS_LOGI("exiting ParseLine Thread");
                exited_ = true;
                filterThreadStarted_ = false;
                TS_LOGD("seprateHead:\t%d, parseHead_:\t%d, filterHead_:\t%d, status:%d\n", rawDataHead_, parseHead_,
                        filterHead_, seg.status.load());
                return;
            }
            TS_LOGD("seprateHead:\t%d, parseHead_:\t%d, filterHead_:\t%d, status:%d\n", rawDataHead_, parseHead_,
                    filterHead_, seg.status.load());
            usleep(sleepDur_);
            continue;
        }
        if (seg.dataType == DATA_SOURCE_TYPE_TRACE) {
            if (seg.traceData.ftrace_cpu_detail_size()) {
                htraceCpuDetailParser_->Parse(seg.traceData, clock_); // has Event
            }
            if (seg.traceData.symbols_detail_size()) {
                htraceSymbolsDetailParser_->Parse(seg.traceData); // has Event
            }
            if (seg.traceData.clocks_detail_size()) {
                htraceClockDetailParser_->Parse(seg.traceData); // has Event
            }
        } else if (seg.dataType == DATA_SOURCE_TYPE_MEM) {
            htraceMemParser_->Parse(seg.memData, seg.timeStamp, seg.clockId);
        } else if (seg.dataType == DATA_SOURCE_TYPE_HILOG) {
            htraceHiLogParser_->Parse(seg.logData);
        }
        filterHead_ = (filterHead_ + 1) % MAX_SEG_ARRAY_SIZE;
        seg.status = TS_PARSE_STATUS_INIT;
    }
}

void HtraceParser::ParseThread()
{
    while (1) {
        if (!filterThreadStarted_) {
            filterThreadStarted_ = true;
            std::thread ParserThread(&HtraceParser::FilterThread, this);
            ParserThread.detach();
        }
        int head = GetNextSegment();
        if (head < 0) {
            if (head == ERROR_CODE_EXIT) {
                return;
            } else if (head == ERROR_CODE_NODATA) {
                continue;
            }
        }
        HtraceDataSegment& dataSeg = dataSegArray[head];
        ProfilerPluginData pluginData;
        if (!pluginData.ParseFromArray(dataSeg.seg.data(), static_cast<int>(dataSeg.seg.length()))) {
            TS_LOGW("ProfilerPluginData ParseFromArray failed\n");
            dataSeg.status = TS_PARSE_STATUS_INVALID;
            continue;
        }
        if (pluginData.name() == "memory-plugin") {
            ParseMemory(pluginData, dataSeg);
        } else if (pluginData.name() == "/data/local/tmp/libhilogplugin.z.so") {
            ParseHilog(pluginData, dataSeg);
        } else if (pluginData.name() == "/data/local/tmp/libftrace_plugin.z.so"){
            ParseFtrace(pluginData, dataSeg);
        } else {
            TS_LOGW("unrecognized pluginData.name():%s", pluginData.name().c_str());
        }
    }
}

void HtraceParser::ParseMemory(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg)
{
    dataSeg.dataType = DATA_SOURCE_TYPE_MEM;
    auto timeStamp = pluginData.tv_nsec() + pluginData.tv_sec() * SEC_TO_NS;
    BuiltinClocks clockId = TS_CLOCK_REALTIME;
    auto clockIdTemp = pluginData.clock_id();
    if (clockIdTemp == ProfilerPluginData_ClockId_CLOCKID_REALTIME) {
        clockId = TS_CLOCK_REALTIME;
    }
    dataSeg.memData.Clear();
    if (!dataSeg.memData.ParseFromArray(pluginData.data().data(),
        static_cast<int>(pluginData.data().size()))) {
        TS_LOGW("tracePacketParseFromArray failed\n");
        dataSeg.status = TS_PARSE_STATUS_INVALID;
        return;
    }
    if (dataSeg.memData.processesinfo_size()) {
        dataSeg.dataType = DATA_SOURCE_TYPE_MEM;
        dataSeg.timeStamp = timeStamp;
        dataSeg.clockId = clockId;
        dataSeg.status = TS_PARSE_STATUS_PARSED;
    } else if (dataSeg.memData.meminfo_size()) {
        dataSeg.dataType = DATA_SOURCE_TYPE_MEM;
        dataSeg.timeStamp = timeStamp;
        dataSeg.clockId = clockId;
        dataSeg.status = TS_PARSE_STATUS_PARSED;
    } else if (dataSeg.memData.vmeminfo_size()) {
        dataSeg.dataType = DATA_SOURCE_TYPE_MEM;
        dataSeg.timeStamp = timeStamp;
        dataSeg.clockId = clockId;
        dataSeg.status = TS_PARSE_STATUS_PARSED;
    } else {
        dataSeg.status = TS_PARSE_STATUS_INVALID;
    }
}
void HtraceParser::ParseHilog(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg)
{
    dataSeg.dataType = DATA_SOURCE_TYPE_HILOG;
    dataSeg.traceData.Clear();
    if (!dataSeg.logData.ParseFromArray(pluginData.data().data(), static_cast<int>(pluginData.data().size()))) {
        TS_LOGW("tracePacketParseFromArray failed\n");
        dataSeg.status = TS_PARSE_STATUS_PARSED;
        return;
    }
    if (dataSeg.logData.info_size()) {
        dataSeg.status = TS_PARSE_STATUS_PARSED;
        return;
    }
    dataSeg.status = TS_PARSE_STATUS_INVALID;
}
void HtraceParser::ParseFtrace(const ProfilerPluginData& pluginData, HtraceDataSegment &dataSeg)
{
    dataSeg.dataType = DATA_SOURCE_TYPE_TRACE;
    dataSeg.traceData.Clear();
    if (!dataSeg.traceData.ParseFromArray(pluginData.data().data(), static_cast<int>(pluginData.data().size()))) {
        TS_LOGW("tracePacketParseFromArray failed\n");
        dataSeg.status = TS_PARSE_STATUS_INVALID;
        return;
    }
    if (dataSeg.traceData.ftrace_cpu_stats_size()) {
        auto cpuStats = dataSeg.traceData.ftrace_cpu_stats(0);
        auto s = cpuStats.per_cpu_stats(0);
        TS_LOGD("s.overrun():%lu", s.overrun());
        TS_LOGD("s.dropped_events():%lu", s.dropped_events());
        auto clock = cpuStats.trace_clock();
        if (clock == "boot") {
            clock_ = TS_CLOCK_BOOTTIME;
        }
        dataSeg.clockId = clock_;
        dataSeg.status = TS_PARSE_STATUS_PARSED;
        return;
    }
    if (dataSeg.traceData.clocks_detail_size() || dataSeg.traceData.ftrace_cpu_detail_size() ||
        dataSeg.traceData.symbols_detail_size()) {
        dataSeg.status = TS_PARSE_STATUS_PARSED;
        return;
    }
    dataSeg.status = TS_PARSE_STATUS_INVALID;
}
int HtraceParser::GetNextSegment()
{
    int head;
    dataSegMux_.lock();
    head = parseHead_;
    HtraceDataSegment& seg = dataSegArray[head];
    if (seg.status.load() != TS_PARSE_STATUS_SEPRATED) {
        if (toExit_) {
            parserThreadCount_--;
            TS_LOGI("exiting parser, parserThread Count:%d\n", parserThreadCount_);
            TS_LOGD("seprateHead_x:\t%d, parseHead_:\t%d, filterHead_:\t%d status:%d\n", rawDataHead_, parseHead_,
                    filterHead_, seg.status.load());
            dataSegMux_.unlock();
            if (!parserThreadCount_ && !filterThreadStarted_) {
                exited_ = true;
            }
            return ERROR_CODE_EXIT;
        }
        if (seg.status.load() == TS_PARSE_STATUS_PARSING) {
            dataSegMux_.unlock();
            usleep(sleepDur_);
            return ERROR_CODE_NODATA;
        }
        dataSegMux_.unlock();
        usleep(sleepDur_);
        return ERROR_CODE_NODATA;
    }
    parseHead_ = (parseHead_ + 1) % MAX_SEG_ARRAY_SIZE;
    seg.status = TS_PARSE_STATUS_PARSING;
    dataSegMux_.unlock();
    return head;
}
void HtraceParser::ParseTraceDataSegment(std::unique_ptr<uint8_t[]> bufferStr, size_t size)
{
    packagesBuffer_.insert(packagesBuffer_.end(), &bufferStr[0], &bufferStr[size]);
    auto packagesBegin = packagesBuffer_.begin();
    auto currentLength = packagesBuffer_.size();
    if (!hasGotHeader) {
        std::string start(reinterpret_cast<const char*>(bufferStr.get()), std::min<size_t>(size, 20));
        if (start.compare(0, std::string("OHOSPROF").length(), "OHOSPROF") == 0) {
            currentLength -= PACKET_HEADER_LENGTH;
            packagesBegin += PACKET_HEADER_LENGTH;
        }
        hasGotHeader = true;
    }
    while (1) {
        if (!hasGotSegLength_) {
            if (currentLength < PACKET_SEG_LENGTH) {
                break;
            }
            std::string bufferLine(packagesBegin, packagesBegin + PACKET_SEG_LENGTH);
            const uint32_t* len = reinterpret_cast<const uint32_t*>(bufferLine.data());
            nextLength_ = *len;
            hasGotSegLength_ = true;
            currentLength -= PACKET_SEG_LENGTH;
            packagesBegin += PACKET_SEG_LENGTH;
        }
        if (currentLength < nextLength_) {
            break;
        }
        std::string bufferLine(packagesBegin, packagesBegin + nextLength_);
        ParseTraceDataItem(bufferLine);
        hasGotSegLength_ = false;
        packagesBegin += nextLength_;
        currentLength -= nextLength_;
    }
    packagesBuffer_.erase(packagesBuffer_.begin(), packagesBegin);
    return;
}
} // namespace TraceStreamer
} // namespace SysTuning

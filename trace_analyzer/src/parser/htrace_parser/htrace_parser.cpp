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
#include "log.h"
#include "services/common_types.pb.h"
#include "stat_filter.h"
#include "types/plugins/ftrace_data/ftrace_event.pb.h"
#include "types/plugins/ftrace_data/trace_plugin_config.pb.h"
#include "types/plugins/ftrace_data/trace_plugin_result.pb.h"
#include "types/plugins/memory_data/memory_plugin_result.pb.h"
namespace SysTuning {
namespace TraceStreamer {
HtraceParser::HtraceParser(TraceDataCache* dataCache, const TraceStreamerFilters* filters)
    : ParserBase(filters),
      htraceCpuDetailParser_(std::make_unique<HtraceCpuDetailParser>(dataCache, filters)),
      htraceSymbolsDetailParser_(std::make_unique<HtraceSymbolsDetailParser>(dataCache, filters)),
      htraceMemParser_(std::make_unique<HtraceMemParser>(dataCache, filters)),
      htraceClockDetailParser_(std::make_unique<HtraceClockDetailParser>(dataCache, filters))
{
}

HtraceParser::~HtraceParser() = default;

void HtraceParser::ParseTraceDataItem(const std::string& buffer)
{
    ProfilerPluginData plugininData;
    if (!plugininData.ParseFromArray(buffer.data(), static_cast<int>(buffer.length()))) {
        TS_LOGW("ProfilerPluginData ParseFromArray failed\n");
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_INVALID);
        return;
    }
    if (plugininData.name() == "memory-plugin") {
        auto timeStamp = plugininData.tv_nsec() + plugininData.tv_sec() * SEC_TO_NS;
        BuiltinClocks clockId = TS_CLOCK_REALTIME;
        auto clockIdTemp = plugininData.clock_id();
        if (clockIdTemp == ProfilerPluginData_ClockId_CLOCKID_REALTIME) {
            clockId = TS_CLOCK_REALTIME;
        }
        MemoryData memData;
        if (!memData.ParseFromArray(plugininData.data().data(), static_cast<int>(plugininData.data().size()))) {
            TS_LOGW("tracePacketParseFromArray failed\n");
            streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_INVALID);
            return;
        }

        if (memData.processesinfo_size()) {
            htraceMemParser_->Parse(memData, timeStamp, clockId);
        }
    } else {
        TS_LOGD("plugininData.name():%s", plugininData.name().c_str());
        TracePluginResult tracePacket;
        if (!tracePacket.ParseFromArray(plugininData.data().data(), static_cast<int>(plugininData.data().size()))) {
            TS_LOGW("tracePacketParseFromArray failed\n");
            streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_OTHER, STAT_EVENT_DATA_INVALID);
            return;
        }
        if (tracePacket.ftrace_cpu_stats_size()) {
            auto cpuStats = tracePacket.ftrace_cpu_stats(0);
            auto clock = cpuStats.trace_clock();
            if (clock == "boot") {
                clock_ = TS_CLOCK_BOOTTIME;
            }
        }

        if (tracePacket.ftrace_cpu_detail_size()) {
            htraceCpuDetailParser_->Parse(tracePacket, clock_); // has Event
        }
        if (tracePacket.symbols_detail_size()) {
            htraceSymbolsDetailParser_->Parse(tracePacket); // has Event
        }
        if (tracePacket.clocks_detail_size()) {
            htraceClockDetailParser_->Parse(tracePacket); // has Event
        }
    }
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

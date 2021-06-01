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

#include "trace_streamer.h"

#include <algorithm>
#include <chrono>
#include <functional>

#include "clock_filter.h"
#include "cpu_filter.h"
#include "event_filter.h"
#include "file.h"
#include "filter_filter.h"
#include "measure_filter.h"
#include "parser/bytrace_parser.h"
#include "process_filter.h"
#include "slice_filter.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
FileType ParseFileType(const uint8_t* data, size_t size)
{
    if (size == 0) {
        return UN_KNOW;
    }
    std::string start(reinterpret_cast<const char*>(data), std::min<size_t>(size, 20));
    if (start.find("# tracer") != std::string::npos) {
        return BY_TRACE;
    }
    if ((start.compare(0, std::string("<!DOCTYPE html>").length(), "<!DOCTYPE html>") == 0) ||
        (start.compare(0, std::string("<html>").length(), "<html>") == 0)) {
        return BY_TRACE;
    }
    if (start.compare(0, std::string("\x0a").length(), "\x0a") == 0) {
        return PROTO;
    }
    return UN_KNOW;
}
} // namespace

TraceStreamer::TraceStreamer() : fileType_(UN_KNOW), bytraceParser_(nullptr)
{
    InitFilter();
    InitParser();
}
TraceStreamer::~TraceStreamer()
{
}

void TraceStreamer::InitFilter()
{
    streamFilters_ = std::make_unique<TraceStreamerFilters>();
    traceDataCache_ = std::make_unique<TraceDataCache>();
    streamFilters_ = std::make_unique<TraceStreamerFilters>();
    traceDataCache_ = std::make_unique<TraceDataCache>();
    streamFilters_->sliceFilter_ = std::make_unique<SliceFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->cpuFilter_ = std::make_unique<CpuFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->eventFilter_ = std::make_unique<EventFilter>(traceDataCache_.get(), streamFilters_.get());

    streamFilters_->processFilter_ = std::make_unique<ProcessFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->clockFilter_ = std::make_unique<ClockFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->filterFilter_ = std::make_unique<FilterFilter>(traceDataCache_.get(), streamFilters_.get());

    streamFilters_->threadCounterFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_THREADMEASURE_FILTER);
    streamFilters_->threadFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_THREAD_FILTER);
    streamFilters_->cpuCounterFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_CPU_MEASURE_FILTER);
    streamFilters_->processCounterFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_PROCESS_MEASURE_FILTER);
    streamFilters_->processFilterFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_PROCESS_FILTER_FILTER);
}
void TraceStreamer::InitParser()
{
    bytraceParser_ = std::make_unique<BytraceParser>(traceDataCache_.get(), streamFilters_.get());
}
bool TraceStreamer::Parse(std::unique_ptr<uint8_t[]> data, size_t size)
{
    if (size == 0) {
        return true;
    }
    if (fileType_ == UN_KNOW) {
        fileType_ = ParseFileType(data.get(), size);
        if (fileType_ == UN_KNOW) {
            base::SetAnalysisResult(base::FILE_TYPE_ERROR);
            fprintf(stdout, "File type is not supported!");
            return false;
        }
    }
    if (fileType_ == PROTO) {
        base::SetAnalysisResult(base::FILE_TYPE_ERROR);
        fprintf(stdout, "File type is not supported!");
        return false;
    }
    if (fileType_ == BY_TRACE) {
        if (!bytraceParser_->Parse(std::move(data), size)) {
            base::SetAnalysisResult(base::PARSE_ERROR);
            return false;
        }
    }
    base::SetAnalysisResult(base::NORMAL);
    return true;
}

int TraceStreamer::ExportDatabase(const std::string& outputName)
{
    return traceDataCache_->ExportDatabase(outputName);
}
} // namespace TraceStreamer
} // namespace SysTuning

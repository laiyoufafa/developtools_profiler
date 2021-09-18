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

#include "trace_streamer_selector.h"

#include <algorithm>
#include <chrono>
#include <functional>

#include "clock_filter.h"
#include "cpu_filter.h"
#include "file.h"
#include "filter_filter.h"
#include "measure_filter.h"
#include "parser/bytrace_parser/bytrace_parser.h"
#include "parser/htrace_parser/htrace_parser.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "stat_filter.h"
#include "symbols_filter.h"
using namespace SysTuning::base;
namespace SysTuning {
namespace TraceStreamer {
namespace {
TraceFileType GuessFileType(const uint8_t* data, size_t size)
{
    if (size == 0) {
        return TRACE_FILETYPE_UN_KNOW;
    }
    std::string start(reinterpret_cast<const char*>(data), std::min<size_t>(size, 20));
    if (start.find("# tracer") != std::string::npos) {
        return TRACE_FILETYPE_BY_TRACE;
    }
    if (start.find("# TRACE") != std::string::npos) {
        return TRACE_FILETYPE_BY_TRACE;
    }
    if ((start.compare(0, std::string("<!DOCTYPE html>").length(), "<!DOCTYPE html>") == 0) ||
        (start.compare(0, std::string("<html>").length(), "<html>") == 0)) {
        return TRACE_FILETYPE_BY_TRACE;
    }
    if (start.compare(0, std::string("\x0a").length(), "\x0a") == 0) {
        return TRACE_FILETYPE_H_TRACE;
    }
    if (start.compare(0, std::string("OHOSPROF").length(), "OHOSPROF") == 0) {
        return TRACE_FILETYPE_H_TRACE;
    }
    if (start.compare(0, std::string("\x0a").length(), "\x0a") == 0) {
        return TRACE_FILETYPE_UN_KNOW;
    }
    return TRACE_FILETYPE_UN_KNOW;
}
} // namespace

TraceStreamerSelector::TraceStreamerSelector()
    : fileType_(TRACE_FILETYPE_UN_KNOW), bytraceParser_(nullptr), htraceParser_(nullptr)
{
    InitFilter();
    InitParser();
}
TraceStreamerSelector::~TraceStreamerSelector() {}

void TraceStreamerSelector::InitFilter()
{
    streamFilters_ = std::make_unique<TraceStreamerFilters>();
    traceDataCache_ = std::make_unique<TraceDataCache>();
    streamFilters_->sliceFilter_ = std::make_unique<SliceFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->cpuFilter_ = std::make_unique<CpuFilter>(traceDataCache_.get(), streamFilters_.get());

    streamFilters_->processFilter_ = std::make_unique<ProcessFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->clockFilter_ = std::make_unique<ClockFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->filterFilter_ = std::make_unique<FilterFilter>(traceDataCache_.get(), streamFilters_.get());

    streamFilters_->threadMeasureFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_THREADMEASURE_FILTER);
    streamFilters_->threadFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_THREAD_FILTER);
    streamFilters_->cpuMeasureFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_CPU_MEASURE_FILTER);
    streamFilters_->processMeasureFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_PROCESS_MEASURE_FILTER);
    streamFilters_->processFilterFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_PROCESS_FILTER_FILTER);
    streamFilters_->symbolsFilter_ = std::make_unique<SymbolsFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->statFilter_ = std::make_unique<StatFilter>(traceDataCache_.get(), streamFilters_.get());
    streamFilters_->clockRateFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_CLOCK_RATE_FILTER);
    streamFilters_->clockEnableFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_CLOCK_ENABLE_FILTER);
    streamFilters_->clockDisableFilter_ =
        std::make_unique<MeasureFilter>(traceDataCache_.get(), streamFilters_.get(), E_CLOCK_DISABLE_FILTER);
}
void TraceStreamerSelector::InitParser()
{
    bytraceParser_ = std::make_unique<BytraceParser>(traceDataCache_.get(), streamFilters_.get());
    htraceParser_ = std::make_unique<HtraceParser>(traceDataCache_.get(), streamFilters_.get());
}

void TraceStreamerSelector::WaitForParserEnd()
{
    if (fileType_ == TRACE_FILETYPE_H_TRACE) {
        return;
    }
    if (fileType_ == TRACE_FILETYPE_BY_TRACE) {
        bytraceParser_->WaitForParserEnd();
    }
}

MetaData* TraceStreamerSelector::GetMetaData()
{
    return traceDataCache_->GetMetaData();
}
bool TraceStreamerSelector::ParseTraceDataSegment(std::unique_ptr<uint8_t[]> data, size_t size)
{
    if (size == 0) {
        return true;
    }
    if (fileType_ == TRACE_FILETYPE_UN_KNOW) {
        fileType_ = GuessFileType(data.get(), size);
        if (fileType_ == TRACE_FILETYPE_UN_KNOW) {
            SetAnalysisResult(TRACE_PARSER_FILE_TYPE_ERROR);
            fprintf(stdout, "File type is not supported!");
            return false;
        }
    }
    if (fileType_ == TRACE_FILETYPE_H_TRACE) {
        htraceParser_->ParseTraceDataSegment(std::move(data), size);
    }
    if (fileType_ == TRACE_FILETYPE_BY_TRACE) {
        bytraceParser_->ParseTraceDataSegment(std::move(data), size);
    }
    SetAnalysisResult(TRACE_PARSER_NORMAL);
    return true;
}
void TraceStreamerSelector::EnableMetaTable(bool enabled)
{
    traceDataCache_->EnableMetaTable(enabled);
}
int TraceStreamerSelector::ExportDatabase(const std::string& outputName) const
{
    return traceDataCache_->ExportDatabase(outputName);
}
int TraceStreamerSelector::SearchData(const std::string& outputName)
{
    return traceDataCache_->SearchData(outputName);
}
} // namespace TraceStreamer
} // namespace SysTuning

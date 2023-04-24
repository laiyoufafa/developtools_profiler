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
#include <regex>
#include "clock_filter.h"
#include "string_help.h"

using namespace SysTuning::base;
namespace SysTuning {
namespace TraceStreamer {
namespace {
TraceFileType GuessFileType(const uint8_t* data, size_t size)
{
    return TRACE_FILETYPE_UN_KNOW;
}
} // namespace

TraceStreamerSelector::TraceStreamerSelector()
{
    InitFilter();
    sdkDataParser_ = std::make_unique<SDKDataParser>(traceDataCache_.get(), streamFilters_.get());
}
TraceStreamerSelector::~TraceStreamerSelector() {}

void TraceStreamerSelector::InitFilter()
{
    streamFilters_ = std::make_unique<TraceStreamerFilters>();
    traceDataCache_ = std::make_unique<TraceDataCache>();
    streamFilters_->clockFilter_ = std::make_unique<ClockFilter>(traceDataCache_.get(), streamFilters_.get());
}

MetaData* TraceStreamerSelector::GetMetaData()
{
    return traceDataCache_->GetMetaData();
}

void TraceStreamerSelector::WaitForParserEnd() {}

bool TraceStreamerSelector::ParseTraceDataSegment(std::unique_ptr<uint8_t[]> data, size_t size)
{
    if (size == 0) {
        return true;
    }
    return true;
}
void TraceStreamerSelector::EnableMetaTable(bool enabled)
{
    traceDataCache_->EnableMetaTable(enabled);
}

void TraceStreamerSelector::SetCleanMode(bool cleanMode) {}
int TraceStreamerSelector::ExportDatabase(const std::string& outputName) const
{
    return traceDataCache_->ExportDatabase(outputName);
}
void TraceStreamerSelector::Clear()
{
    traceDataCache_->Prepare();
    traceDataCache_->Clear();
}
int TraceStreamerSelector::SearchData()
{
    return traceDataCache_->SearchData();
}
int TraceStreamerSelector::OperateDatabase(const std::string& sql)
{
    return traceDataCache_->OperateDatabase(sql);
}
int TraceStreamerSelector::SearchDatabase(const std::string& sql, TraceDataDB::ResultCallBack resultCallBack)
{
    return traceDataCache_->SearchDatabase(sql, resultCallBack);
}
int TraceStreamerSelector::SearchDatabase(const std::string& sql, uint8_t* out, int outLen)
{
    return traceDataCache_->SearchDatabase(sql, out, outLen);
}
void TraceStreamerSelector::SetCancel(bool cancel)
{
    traceDataCache_->SetCancel(cancel);
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "measure_filter.h"
#include "common.h"
#include "filter_filter.h"
#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
MeasureFilter::MeasureFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter, FilterType e)
    : FilterBase(dataCache, filter),
      tidStreamIdFilterIdMap_(INVALID_UINT64),
      cookieFilterIdMap_(INVALID_UINT64),
      filterType_(e)
{
}

MeasureFilter::~MeasureFilter()
{
}

void MeasureFilter::Init(FilterType e)
{
    filterType_ = e;
}

uint32_t MeasureFilter::GetOrCreateCertainFilterId(uint64_t internalTid, DataIndex nameIndex)
{
    auto filterId = tidStreamIdFilterIdMap_.Find(internalTid, nameIndex);
    if (filterId != INVALID_UINT64) {
        return static_cast<uint32_t>(filterId);
    }

    uint32_t newFilterId =
        streamFilters_->filterFilter_->AddFilter(filterTypeValue.at(filterType_),
            traceDataCache_->GetDataFromDict(nameIndex));
    AddCertainFilterId(internalTid, nameIndex, newFilterId);
    return newFilterId;
}

uint32_t MeasureFilter::GetOrCreateCertainFilterIdByCookie(uint64_t internalTid, DataIndex nameIndex, int64_t cookie)
{
    auto filterId = cookieFilterIdMap_.Find(static_cast<uint64_t>(cookie), nameIndex);
    if (filterId != INVALID_UINT64) {
        return static_cast<uint32_t>(filterId);
    }

    uint32_t newFilterId =
        streamFilters_->filterFilter_->AddFilter(filterTypeValue.at(filterType_),
            traceDataCache_->GetDataFromDict(nameIndex));
    cookieFilterIdMap_.Insert(static_cast<uint64_t>(cookie), nameIndex, newFilterId);
    traceDataCache_->GetProcessFilterData()->AppendProcessFilterData(static_cast<uint32_t>(newFilterId),
        nameIndex, static_cast<uint32_t>(internalTid));
    return newFilterId;
}

void MeasureFilter::AddCertainFilterId(uint64_t internalTid, DataIndex nameIndex, uint64_t filterId)
{
    tidStreamIdFilterIdMap_.Insert(internalTid, nameIndex, filterId);

    if (filterType_ == E_THREADMEASURE_FILTER) {
        traceDataCache_->GetThreadCounterFilterData()->AppendNewData(filterId,
            static_cast<uint32_t>(nameIndex), internalTid);
    } else if (filterType_ == E_THREAD_FILTER) {
        traceDataCache_->GetThreadFilterData()->AppendNewData(filterId, static_cast<uint32_t>(nameIndex), internalTid);
    } else if (filterType_ == E_PROCESS_MEASURE_FILTER) {
        traceDataCache_->GetProcessCounterFilterData()->AppendProcessCounterFilterData(static_cast<uint32_t>(filterId), 
            static_cast<uint32_t>(nameIndex), static_cast<uint32_t>(internalTid));
    } else if (filterType_ == E_PROCESS_FILTER_FILTER) {
        traceDataCache_->GetProcessFilterData()->AppendProcessFilterData(static_cast<uint32_t>(filterId), 
            static_cast<uint32_t>(nameIndex), static_cast<uint32_t>(internalTid));
    } else if (filterType_ == E_CPU_MEASURE_FILTER) {
        traceDataCache_->GetCpuCountersData()->AppendCpuCounter(filterId, 
            static_cast<uint32_t>(nameIndex), internalTid);
    }
}
} // namespace TraceStreamer
} // namespace SysTuning

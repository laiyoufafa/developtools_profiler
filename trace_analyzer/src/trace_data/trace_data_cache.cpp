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

#include "trace_data_cache.h"

#include <sqlite3.h>
#include "callstack_table.h"
#include "clock_event_filter_table.h"
#include "cpu_measure_filter_table.h"
#include "data_dict_table.h"
#include "filter_table.h"
#include "instants_table.h"
#include "measure_filter_table.h"
#include "measure_table.h"
#include "meta_table.h"
#include "process_filter_table.h"
#include "process_measure_filter_table.h"
#include "process_table.h"
#include "range_table.h"
#include "raw_table.h"
#include "sched_slice_table.h"
#include "stat_table.h"
#include "symbols_table.h"
#include "table_base.h"
#include "thread_filter_table.h"
#include "thread_state_table.h"
#include "thread_table.h"

namespace SysTuning {
namespace TraceStreamer {
TraceDataCache::TraceDataCache()
{
    InitDB();
}

TraceDataCache::~TraceDataCache()
{
}
void TraceDataCache::InitDB()
{
    if (dbInited) {
        return;
    }
    TableBase::TableDeclare<ProcessTable>(*db_, this, "process");
    TableBase::TableDeclare<SchedSliceTable>(*db_, this, "sched_slice");
    TableBase::TableDeclare<CallStackTable>(*db_, this, "callstack");
    TableBase::TableDeclare<DataDictTable>(*db_, this, "data_dict");
    TableBase::TableDeclare<ThreadStateTable>(*db_, this, "thread_state");
    TableBase::TableDeclare<InstantsTable>(*db_, this, "instant");
    TableBase::TableDeclare<MeasureTable>(*db_, this, "measure");
    TableBase::TableDeclare<RangeTable>(*db_, this, "trace_range");
    TableBase::TableDeclare<ThreadTable>(*db_, this, "thread");
    TableBase::TableDeclare<RawTable>(*db_, this, "raw");
    TableBase::TableDeclare<CpuMeasureFilterTable>(*db_, this, "cpu_measure_filter");
    TableBase::TableDeclare<FilterTable>(*db_, this, "measure_filter");
    TableBase::TableDeclare<ProcessMeasureFilterTable>(*db_, this, "process_measure_filter");
    TableBase::TableDeclare<StatTable>(*db_, this, "stat");
    TableBase::TableDeclare<ClockEventFilterTable>(*db_, this, "clock_event_filter");
    TableBase::TableDeclare<SymbolsTable>(*db_, this, "symbols");
    dbInited = true;
}
} // namespace trace_streamer
} // namespace SysTuning

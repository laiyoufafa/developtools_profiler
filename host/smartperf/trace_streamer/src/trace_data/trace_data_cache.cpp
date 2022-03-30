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
#include "args_table.h"
#include "callstack_table.h"
#include "clk_event_filter_table.h"
#include "clock_event_filter_table.h"
#include "cpu_measure_filter_table.h"
#include "data_dict_table.h"
#include "data_type_table.h"
#include "filter_table.h"
#include "heap_frame_table.h"
#include "heap_table.h"
#include "hidump_table.h"
#include "instants_table.h"
#include "irq_table.h"
#include "log_table.h"
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
#include "system_call_table.h"
#include "system_event_filter_table.h"
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

TraceDataCache::~TraceDataCache() {}

void TraceDataCache::InitDB()
{
    if (dbInited) {
        return;
    }
    TableBase::TableDeclare<ProcessTable>(*db_, this, "_process");
    TableBase::TableDeclare<SchedSliceTable>(*db_, this, "_sched_slice");
    TableBase::TableDeclare<CallStackTable>(*db_, this, "_callstack");
    TableBase::TableDeclare<IrqTable>(*db_, this, "_irq");
    TableBase::TableDeclare<DataDictTable>(*db_, this, "_data_dict");
    TableBase::TableDeclare<ThreadStateTable>(*db_, this, "_thread_state");
    TableBase::TableDeclare<InstantsTable>(*db_, this, "_instant");
    TableBase::TableDeclare<MeasureTable>(*db_, this, "_measure");
    TableBase::TableDeclare<RangeTable>(*db_, this, "_trace_range");
    TableBase::TableDeclare<ThreadTable>(*db_, this, "_thread");
    TableBase::TableDeclare<RawTable>(*db_, this, "_raw");
    TableBase::TableDeclare<CpuMeasureFilterTable>(*db_, this, "_cpu_measure_filter");
    TableBase::TableDeclare<FilterTable>(*db_, this, "_measure_filter");
    TableBase::TableDeclare<ProcessMeasureFilterTable>(*db_, this, "_process_measure_filter");
    TableBase::TableDeclare<StatTable>(*db_, this, "_stat");
    TableBase::TableDeclare<ClockEventFilterTable>(*db_, this, "_clock_event_filter");
    TableBase::TableDeclare<ClkEventFilterTable>(*db_, this, "_clk_event_filter");
    TableBase::TableDeclare<SymbolsTable>(*db_, this, "_symbols");
    TableBase::TableDeclare<SystemCallTable>(*db_, this, "_syscall");
    TableBase::TableDeclare<ArgsTable>(*db_, this, "_args");
    TableBase::TableDeclare<DataTypeTable>(*db_, this, "_data_type");
    TableBase::TableDeclare<MetaTable>(*db_, this, "_meta");
    TableBase::TableDeclare<LogTable>(*db_, this, "_log");
    TableBase::TableDeclare<HeapTable>(*db_, this, "_heap");
    TableBase::TableDeclare<HeapFrameTable>(*db_, this, "_heap_frame");
    TableBase::TableDeclare<HidumpTable>(*db_, this, "_hidump");
    TableBase::TableDeclare<SystemEventFilterTable>(*db_, this, "_sys_event_filter");
    dbInited = true;
}
} // namespace TraceStreamer
} // namespace SysTuning

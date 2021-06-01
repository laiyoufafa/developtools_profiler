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
#include "bound_table.h"
#include "cpu_measure_filter_table.h"
#include "data_dict_table.h"
#include "filter_table.h"
#include "instants_table.h"
#include "internal_slice_table.h"
#include "internal_thread.h"
#include "measure_filter_table.h"
#include "measure_table.h"
#include "process_filter_table.h"
#include "process_measure_filter_table.h"
#include "process_table.h"
#include "raw_table.h"
#include "sched_slice_table.h"
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
    TableBase::TableDeclare<ProcessTable>(*db_, this, "internal_process");
    TableBase::TableDeclare<SchedSliceTable>(*db_, this, "sched_slice");
    TableBase::TableDeclare<InternalSliceTable>(*db_, this, "internal_slice");
    TableBase::TableDeclare<DataDictTable>(*db_, this, "strings");
    TableBase::TableDeclare<ThreadStateTable>(*db_, this, "thread_state");
    TableBase::TableDeclare<InstantsTable>(*db_, this, "instant");
    TableBase::TableDeclare<MeasureTable>(*db_, this, "counter");
    TableBase::TableDeclare<BoundTable>(*db_, this, "trace_bounds");
    TableBase::TableDeclare<InternalThread>(*db_, this, "internal_thread");
    TableBase::TableDeclare<RawTable>(*db_, this, "raw");
    TableBase::TableDeclare<CpuMeasureFilterTable>(*db_, this, "cpu_counter_track");
    TableBase::TableDeclare<FilterTable>(*db_, this, "track");
    TableBase::TableDeclare<ThreadFilterTable>(*db_, this, "thread_track");
    TableBase::TableDeclare<MeasureFilterTable>(*db_, this, "thread_counter_track");
    TableBase::TableDeclare<ProcessMeasureFilterTable>(*db_, this, "process_counter_track");
    TableBase::TableDeclare<ProcessFilterTable>(*db_, this, "process_track");
}
} // namespace trace_streamer
} // namespace SysTuning

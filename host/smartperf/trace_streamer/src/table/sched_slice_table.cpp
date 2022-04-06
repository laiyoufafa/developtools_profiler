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

#include "sched_slice_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, TS, DUR, CPU, INTERNAL_TID, END_STATE, PRIORITY };
}
SchedSliceTable::SchedSliceTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("end_state", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("priority", "INT"));
    tablePriKey_.push_back("id");
}

SchedSliceTable::~SchedSliceTable() {}

void SchedSliceTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

SchedSliceTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstSchedSliceData().Size())),
      schedSliceObj_(dataCache->GetConstSchedSliceData())
{
}

SchedSliceTable::Cursor::~Cursor() {}

int SchedSliceTable::Cursor::Column(int col) const
{
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(CurrentRow()));
            break;
        case TYPE:
            sqlite3_result_text(context_, "sched_slice", STR_DEFAULT_LEN, nullptr);
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.TimeStamData()[CurrentRow()]));
            break;
        case DUR:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.DursData()[CurrentRow()]));
            break;
        case CPU:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.CpusData()[CurrentRow()]));
            break;
        case INTERNAL_TID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.InternalTidsData()[CurrentRow()]));
            break;
        case END_STATE: {
            const std::string& str = dataCache_->GetConstSchedStateData(schedSliceObj_.EndStatesData()[CurrentRow()]);
            sqlite3_result_text(context_, str.c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        }
        case PRIORITY:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.PriorityData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", col);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

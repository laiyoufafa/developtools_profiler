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

#include "raw_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, TS, NAME, CPU, INTERNAL_TID };
}
RawTable::RawTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "UNSIGNED INT"));
    tablePriKey_.push_back("id");
}

RawTable::~RawTable() {}

void RawTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

RawTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstRawTableData().Size())),
      rawObj_(dataCache->GetConstRawTableData())
{
}

RawTable::Cursor::~Cursor() {}

int RawTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, static_cast<int32_t>(CurrentRow()));
            break;
        case TYPE:
            sqlite3_result_text(context_, "raw", STR_DEFAULT_LEN, nullptr);
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(rawObj_.TimeStamData()[CurrentRow()]));
            break;
        case NAME: {
            if (rawObj_.NameData()[CurrentRow()] == CPU_IDLE) {
                sqlite3_result_text(context_, "cpu_idle", STR_DEFAULT_LEN, nullptr);
            } else if (rawObj_.NameData()[CurrentRow()] == SCHED_WAKEUP) {
                sqlite3_result_text(context_, "sched_wakeup", STR_DEFAULT_LEN, nullptr);
            } else {
                sqlite3_result_text(context_, "sched_waking", STR_DEFAULT_LEN, nullptr);
            }
            break;
        }
        case CPU:
            sqlite3_result_int64(context_, static_cast<int32_t>(rawObj_.CpuData()[CurrentRow()]));
            break;
        case INTERNAL_TID:
            sqlite3_result_int64(context_, static_cast<int32_t>(rawObj_.InternalTidData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

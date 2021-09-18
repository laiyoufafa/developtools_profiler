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

#include "thread_state_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, TS, DUR, CPU, INTERNAL_TID, STATE };
}
ThreadStateTable::ThreadStateTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("state", "STRING"));
    tablePriKey_.push_back("id");
}

ThreadStateTable::~ThreadStateTable() {}

void ThreadStateTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

ThreadStateTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstThreadStateData().Size())),
      threadStateObj_(dataCache->GetConstThreadStateData())
{
}

ThreadStateTable::Cursor::~Cursor() {}

int ThreadStateTable::Cursor::Column(int col) const
{
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(CurrentRow()));
            break;
        case TYPE:
            sqlite3_result_text(context_, "thread_state", STR_DEFAULT_LEN, nullptr);
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(threadStateObj_.TimeStamData()[CurrentRow()]));
            break;
        case DUR:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(threadStateObj_.DursData()[CurrentRow()]));
            break;
        case CPU:
            if (static_cast<int32_t>(threadStateObj_.CpusData()[CurrentRow()]) >= 0) {
                sqlite3_result_int64(context_, static_cast<sqlite3_int64>(threadStateObj_.CpusData()[CurrentRow()]));
            }
            break;
        case INTERNAL_TID:
            sqlite3_result_int64(context_,
                                 static_cast<sqlite3_int64>(threadStateObj_.InternalTidsData()[CurrentRow()]));
            break;
        case STATE: {
            const std::string& str = dataCache_->GetConstSchedStateData(threadStateObj_.StatesData()[CurrentRow()]);
            sqlite3_result_text(context_, str.c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        }
        default:
            TS_LOGF("Unregistered column : %d", col);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

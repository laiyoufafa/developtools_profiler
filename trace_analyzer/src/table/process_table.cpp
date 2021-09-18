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

#include "process_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, PID, NAME, START_TS, END_TS, PARENT_ID, UID, APP_ID };
}
ProcessTable::ProcessTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("pid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("start_ts", "UNSIGNED INT"));
    tablePriKey_.push_back("id");
}

ProcessTable::~ProcessTable() {}

void ProcessTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

ProcessTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->ProcessSize()))
{
}

ProcessTable::Cursor::~Cursor() {}

int ProcessTable::Cursor::Column(int column) const
{
    const auto& process = dataCache_->GetConstProcessData(CurrentRow());
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, CurrentRow());
            break;
        case TYPE:
            sqlite3_result_text(context_, "process", STR_DEFAULT_LEN, nullptr);
            break;
        case PID:
            sqlite3_result_int64(context_, process.pid_);
            break;
        case NAME:
            sqlite3_result_text(context_, process.cmdLine_.c_str(), static_cast<int>(process.cmdLine_.length()),
                                nullptr);
            break;
        case START_TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(process.startT_));
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "args_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, KEY, DATATYPE, VALUE, ARGSETID };
}
ArgsTable::ArgsTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("key", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("datatype", "UNSIGNED SHORT"));
    tableColumn_.push_back(TableBase::ColumnInfo("value", "BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("argset", "UNSIGNED INT"));
    tablePriKey_.push_back("id");
}

ArgsTable::~ArgsTable() {}

void ArgsTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

ArgsTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstArgSetData().Size())),
      argSet_(dataCache->GetConstArgSetData())
{
}

ArgsTable::Cursor::~Cursor() {}

int ArgsTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(argSet_.IdsData()[CurrentRow()]));
            break;
        case KEY:
            sqlite3_result_int64(context_, static_cast<int64_t>(argSet_.NamesData()[CurrentRow()]));
            break;
        case DATATYPE:
            sqlite3_result_int64(context_, static_cast<int64_t>(argSet_.DataTypes()[CurrentRow()]));
            break;
        case VALUE:
            sqlite3_result_int64(context_, static_cast<int64_t>(argSet_.ValuesData()[CurrentRow()]));
            break;
        case ARGSETID:
            sqlite3_result_int64(context_, static_cast<int64_t>(argSet_.ArgsData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

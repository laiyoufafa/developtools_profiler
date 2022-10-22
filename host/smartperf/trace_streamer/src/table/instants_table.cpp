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

#include "instants_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { TS = 0, NAME, REF, REF_TYPE };
}
InstantsTable::InstantsTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ref", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ref_type", "STRING"));
    tablePriKey_.push_back("ts");
    tablePriKey_.push_back("ref");
}

InstantsTable::~InstantsTable() {}

std::unique_ptr<TableBase::Cursor> InstantsTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

InstantsTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, static_cast<uint32_t>(dataCache->GetConstInstantsData().Size())),
      InstantsObj_(dataCache->GetConstInstantsData())
{
}

InstantsTable::Cursor::~Cursor() {}

int InstantsTable::Cursor::Column(int column) const
{
    size_t stringIdentity = static_cast<size_t>(InstantsObj_.NameIndexsData()[CurrentRow()]);
    switch (column) {
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(InstantsObj_.TimeStamData()[CurrentRow()]));
            break;
        case NAME: {
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(stringIdentity).c_str(),
                STR_DEFAULT_LEN, nullptr);
            break;
        }
        case REF:
            sqlite3_result_int64(context_, static_cast<int32_t>(InstantsObj_.InternalTidsData()[CurrentRow()]));
            break;
        case REF_TYPE: {
            sqlite3_result_text(context_, "itid", STR_DEFAULT_LEN, nullptr);
            break;
        }
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "measure_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { TYPE = 0, TS, VALUE, FILTER_ID };
}
MeasureTable::MeasureTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("value", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("filter_id", "UNSIGNED INT"));
    tablePriKey_.push_back("ts");
    tablePriKey_.push_back("filter_id");
}

MeasureTable::~MeasureTable() {}

void MeasureTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

MeasureTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstMeasureData().Size())),
      measureObj(dataCache->GetConstMeasureData())
{
}

MeasureTable::Cursor::~Cursor() {}

int MeasureTable::Cursor::Column(int column) const
{
    switch (column) {
        case TYPE:
            sqlite3_result_text(context_, "measure", STR_DEFAULT_LEN, nullptr);
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(measureObj.TimeStamData()[CurrentRow()]));
            break;
        case VALUE:
            sqlite3_result_int64(context_, static_cast<int64_t>(measureObj.ValuesData()[CurrentRow()]));
            break;
        case FILTER_ID:
            sqlite3_result_int64(context_, static_cast<int32_t>(measureObj.FilterIdData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

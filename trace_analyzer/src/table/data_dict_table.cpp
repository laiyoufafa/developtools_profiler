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

#include "data_dict_table.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, STR };
}
DataDictTable::DataDictTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
        tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED BIG INT"));
        tableColumn_.push_back(TableBase::ColumnInfo("data", "STRING"));
        tablePriKey_.push_back("id");
}

DataDictTable::~DataDictTable() {}

void DataDictTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

DataDictTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->DataDictSize()))
{
}

DataDictTable::Cursor::~Cursor() {}

int DataDictTable::Cursor::Column(int col) const
{
    DataIndex index = static_cast<DataIndex>(CurrentRow());
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(CurrentRow()));
            break;
        case STR:
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(index).c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        default:
            TUNING_LOGF("Unknown column %d", col);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

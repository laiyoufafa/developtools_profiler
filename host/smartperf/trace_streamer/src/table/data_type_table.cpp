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

#include "data_type_table.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPEID, DESC };
}
DataTypeTable::DataTypeTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("typeId", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("desc", "STRING"));
    tablePriKey_.push_back("id");
}

DataTypeTable::~DataTypeTable() {}

void DataTypeTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

DataTypeTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstDataTypeData().Size())),
      dataTypeObj_(dataCache->GetConstDataTypeData())
{
}

DataTypeTable::Cursor::~Cursor() {}

int DataTypeTable::Cursor::Column(int col) const
{
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(CurrentRow()));
            break;
        case TYPEID:
            sqlite3_result_int64(context_, static_cast<int64_t>(dataTypeObj_.DataTypes()[CurrentRow()]));
            break;
        case DESC:
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(dataTypeObj_.DataDesc()[CurrentRow()]).c_str(),
                STR_DEFAULT_LEN, nullptr);
            break;
        default:
            TS_LOGF("Unknown column %d", col);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

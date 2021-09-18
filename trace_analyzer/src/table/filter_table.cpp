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

#include "filter_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, NAME, SOURCE_ARG_SET_ID };
}
FilterTable::FilterTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("source_arg_set_id", "UNSIGNED BIG INT"));
    tablePriKey_.push_back("id");
}

FilterTable::~FilterTable() {}

void FilterTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

FilterTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstFilterData().Size())),
      filterObj_(dataCache->GetConstFilterData())
{
}

FilterTable::Cursor::~Cursor() {}

int FilterTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(filterObj_.IdsData()[CurrentRow()]));
            break;
        case TYPE:
            sqlite3_result_text(context_, filterObj_.TypeData()[CurrentRow()].c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        case NAME:
            sqlite3_result_text(context_, filterObj_.NameData()[CurrentRow()].c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        case SOURCE_ARG_SET_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(filterObj_.SourceArgSetIdData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "process_measure_filter_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, NAME, INTERNAL_PID };
}
ProcessMeasureFilterTable::ProcessMeasureFilterTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ipid", "INT"));
    tablePriKey_.push_back("id");
}

ProcessMeasureFilterTable::~ProcessMeasureFilterTable() {}

void ProcessMeasureFilterTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

ProcessMeasureFilterTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstProcessMeasureFilterData().Size()))
{
}

ProcessMeasureFilterTable::Cursor::~Cursor() {}

int ProcessMeasureFilterTable::Cursor::Column(int col) const
{
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(
                                               dataCache_->GetConstProcessMeasureFilterData().IdsData()[CurrentRow()]));
            break;
        case TYPE:
            sqlite3_result_text(context_, "process_measure_filter", STR_DEFAULT_LEN, nullptr);
            break;
        case NAME: {
            size_t strId =
                static_cast<size_t>(dataCache_->GetConstProcessMeasureFilterData().NamesData()[CurrentRow()]);
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(strId).c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        }
        case INTERNAL_PID:
            sqlite3_result_int64(
                context_,
                static_cast<sqlite3_int64>(dataCache_->GetConstProcessMeasureFilterData().UpidsData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", col);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "range_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { START_TS = 0, END_TS };
}
RangeTable::RangeTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("start_ts", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("end_ts", "INT"));
    tablePriKey_.push_back("start_ts");
}

RangeTable::~RangeTable() {}

void RangeTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

RangeTable::Cursor::Cursor(const TraceDataCache* dataCache) : TableBase::Cursor(dataCache, 0, 1) {}

RangeTable::Cursor::~Cursor() {}

int RangeTable::Cursor::Column(int column) const
{
    switch (column) {
        case START_TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(dataCache_->TraceStartTime()));
            break;
        case END_TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(dataCache_->TraceEndTime()));
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

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

#include "hidump_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { TS = 0, FPS };
}
HidumpTable::HidumpTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("fps", "UNSIGNED INT"));
    tablePriKey_.push_back("ts");
}

HidumpTable::~HidumpTable() {}

void HidumpTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

HidumpTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstHidumpData().Size())),
      hidumpObj_(dataCache->GetConstHidumpData())
{
}

HidumpTable::Cursor::~Cursor() {}

int HidumpTable::Cursor::Column(int column) const
{
    switch (column) {
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(hidumpObj_.TimeStamData()[CurrentRow()]));
            break;
        case FPS: {
            sqlite3_result_int64(context_, static_cast<int64_t>(hidumpObj_.Fpss()[CurrentRow()]));
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

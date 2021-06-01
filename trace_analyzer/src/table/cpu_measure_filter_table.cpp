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

#include "cpu_measure_filter_table.h"
#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, NAME, UNIT, CPU };
}
CpuMeasureFilterTable::CpuMeasureFilterTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("unit", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu", "UNSIGNED INT"));
    tablePriKey_.push_back("id");
}

CpuMeasureFilterTable::~CpuMeasureFilterTable() {}

void CpuMeasureFilterTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

CpuMeasureFilterTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstCpuCounterData().Size())),
      cpuCounterObj_(dataCache->GetConstCpuCounterData())
{
}

CpuMeasureFilterTable::Cursor::~Cursor() {}

int CpuMeasureFilterTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(cpuCounterObj_.IdsData()[CurrentRow()]));
            break;
        case TYPE:
            sqlite3_result_text(context_, "cpu_counter_track", STR_DEFAULT_LEN, nullptr);
            break;
        case NAME: {
            const std::string& str =
                dataCache_->GetDataFromDict(static_cast<size_t>(cpuCounterObj_.NameData()[CurrentRow()]));
            sqlite3_result_text(context_, str.c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        }
        case UNIT:
            sqlite3_result_int64(context_, static_cast<int32_t>(cpuCounterObj_.UnitData()[CurrentRow()]));
            break;
        case CPU:
            sqlite3_result_int64(context_, static_cast<int32_t>(cpuCounterObj_.CpuData()[CurrentRow()]));
            break;
        default:
            TUNING_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

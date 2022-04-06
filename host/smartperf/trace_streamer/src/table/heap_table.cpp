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

#include "heap_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index {
    EVENT_ID = 0,
    IPID,
    ITID,
    EVENT_TYPE,
    START_TS,
    END_TS,
    DURATION,
    ADDR,
    HEAP_SIZE,
    ALL_HEAP_SIZE,
    CURRENT_SIZE_DUR
};
}
HeapTable::HeapTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("eventId", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ipid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("event_type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("start_ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("end_ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("addr", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("heap_size", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("all_heap_size", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("current_size_dur", "UNSIGNED INT"));
    tablePriKey_.push_back("eventId");
}

HeapTable::~HeapTable() {}

void HeapTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

HeapTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstHeapData().Size())),
      heapInfoObj_(dataCache->GetConstHeapData())
{
}

HeapTable::Cursor::~Cursor() {}

int HeapTable::Cursor::Column(int column) const
{
    switch (column) {
        case EVENT_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.EventIds()[CurrentRow()]));
            break;
        case IPID:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.Ipids()[CurrentRow()]));
            break;
        case ITID:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.Itids()[CurrentRow()]));
            break;
        case EVENT_TYPE: {
            if (heapInfoObj_.EventTypes()[CurrentRow()] != INVALID_UINT64) {
                auto eventTypeDataIndex = static_cast<size_t>(heapInfoObj_.EventTypes()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(eventTypeDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case START_TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.TimeStamData()[CurrentRow()]));
            break;
        case END_TS:
            if (static_cast<int64_t>(heapInfoObj_.EndTimeStamps()[CurrentRow()]) != 0) {
                sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.EndTimeStamps()[CurrentRow()]));
            }
            break;
        case DURATION:
            if (static_cast<int64_t>(heapInfoObj_.Durations()[CurrentRow()]) != 0) {
                sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.Durations()[CurrentRow()]));
            }
            break;
        case ADDR: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.Addrs()[CurrentRow()]));
            break;
        }
        case HEAP_SIZE: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.HeapSizes()[CurrentRow()]));
            break;
        }
        case ALL_HEAP_SIZE: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.AllHeapSizes()[CurrentRow()]));
            break;
        }
        case CURRENT_SIZE_DUR: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapInfoObj_.CurrentSizeDurs()[CurrentRow()]));
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

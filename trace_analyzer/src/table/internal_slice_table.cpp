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

#include "internal_slice_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TS, DUR, INTERNAL_TID, FILTER_ID, CAT, NAME, DEPTH, STACK_ID, PARENT_STACK_ID, PARENT_ID };
}
InternalSliceTable::InternalSliceTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("utid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("track_id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cat", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("depth", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("stack_id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("parent_stack_id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("parent_id", "UNSIGNED INT"));
    tablePriKey_.push_back("utid");
    tablePriKey_.push_back("ts");
    tablePriKey_.push_back("depth");
}

InternalSliceTable::~InternalSliceTable() {}

void InternalSliceTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

InternalSliceTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstInternalSlicesData().Size())),
      slicesObj_(dataCache->GetConstInternalSlicesData())
{
}

InternalSliceTable::Cursor::~Cursor() {}

int InternalSliceTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, CurrentRow());
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.TimeStamData()[CurrentRow()]));
            break;
        case DUR:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.DursData()[CurrentRow()]));
            break;
        case INTERNAL_TID:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.InternalTidsData()[CurrentRow()]));
            break;
        case FILTER_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.FilterIdData()[CurrentRow()]));
            break;
        case CAT: {
            size_t stringIdentity = static_cast<size_t>(slicesObj_.CatsData()[CurrentRow()]);
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(stringIdentity).c_str(),
                STR_DEFAULT_LEN, nullptr);
            break;
        }
        case NAME: {
            size_t stringIdentity = static_cast<size_t>(slicesObj_.NamesData()[CurrentRow()]);
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(stringIdentity).c_str(),
                STR_DEFAULT_LEN, nullptr);
            break;
        }
        case DEPTH:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.Depths()[CurrentRow()]));
            break;
        case STACK_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.StackIdsData()[CurrentRow()]));
            break;
        case PARENT_STACK_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.ParentStackIds()[CurrentRow()]));
            break;
        case PARENT_ID: {
            if (slicesObj_.ParentIdData()[CurrentRow()].has_value()) {
                sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.ParentIdData()[CurrentRow()].value()));
            }
            break;
        }
        default:
            TUNING_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

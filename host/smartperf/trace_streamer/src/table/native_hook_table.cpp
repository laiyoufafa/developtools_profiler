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

#include "native_hook_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index {
    ID = 0,
    EVENT_ID,
    IPID,
    ITID,
    EVENT_TYPE,
    SUB_TYPE,
    START_TS,
    END_TS,
    DURATION,
    ADDR,
    MEM_SIZE,
    ALL_MEM_SIZE,
    CURRENT_SIZE_DUR
};
}
NativeHookTable::NativeHookTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("eventId", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ipid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("event_type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("sub_type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("start_ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("end_ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("addr", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("heap_size", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("all_heap_size", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("current_size_dur", "UNSIGNED INT"));
    tablePriKey_.push_back("eventId");
}

NativeHookTable::~NativeHookTable() {}

void NativeHookTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
{
    constexpr double filterBaseCost = 1000.0; // set-up and tear-down
    constexpr double indexCost = 2.0;
    ei.estimatedCost = filterBaseCost;

    auto rowCount = dataCache_->GetConstNativeHookData().Size();
    if (rowCount == 0 || rowCount == 1) {
        ei.estimatedRows = rowCount;
        ei.estimatedCost += indexCost * rowCount;
        return;
    }

    double filterCost = 0.0;
    auto constraints = fc.GetConstraints();
    if (constraints.empty()) { // scan all rows
        filterCost = rowCount;
    } else {
        for (int i = 0; i < static_cast<int>(constraints.size()); i++) {
            if (rowCount <= 1) {
                // only one row or nothing, needn't filter by constraint
                filterCost += rowCount;
                break;
            }
            const auto& c = constraints[i];
            switch (c.col) {
                case ID: {
                    if (CanFilterId(c.op, rowCount)) {
                        fc.UpdateConstraint(i, true);
                        filterCost += 1; // id can position by 1 step
                    } else {
                        filterCost += rowCount; // scan all rows
                    }
                    break;
                }
                default: // other column
                    filterCost += rowCount; // scan all rows
                    break;
            }
        }
    }
    ei.estimatedCost += filterCost;
    ei.estimatedRows = rowCount;
    ei.estimatedCost += rowCount * indexCost;

    ei.isOrdered = true;
    auto orderbys = fc.GetOrderBys();
    for (auto i = 0; i < orderbys.size(); i++) {
        switch (orderbys[i].iColumn) {
            case ID:
                break;
            default: // other columns can be sorted by SQLite
                ei.isOrdered = false;
                break;
        }
    }
}

bool NativeHookTable::CanFilterId(const char op, size_t& rowCount)
{
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            rowCount = 1;
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
        case SQLITE_INDEX_CONSTRAINT_GE:
        case SQLITE_INDEX_CONSTRAINT_LE:
        case SQLITE_INDEX_CONSTRAINT_LT:
            // assume filter out a half of rows
            rowCount = (rowCount >> 1);
            break;
        default:
            return false;
    }
    return true;
}

std::unique_ptr<TableBase::Cursor> NativeHookTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

NativeHookTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, static_cast<uint32_t>(dataCache->GetConstNativeHookData().Size())),
      nativeHookObj_(dataCache->GetConstNativeHookData())
{
}

NativeHookTable::Cursor::~Cursor() {}

int NativeHookTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
{
    // reset indexMap_
    indexMap_ = std::make_unique<IndexMap>(0, rowCount_);

    if (rowCount_ <= 0 ) {
        return SQLITE_OK;
    }

    auto& cs = fc.GetConstraints();
    for (size_t i = 0; i < cs.size(); i++) {
        const auto& c = cs[i];
        switch (c.col) {
            case ID:
                FilterId(c.op, argv[i]);
                break;
            default:
                break;
        }
    }

    auto orderbys = fc.GetOrderBys();
    for (auto i = orderbys.size(); i > 0;) {
        i--;
        switch (orderbys[i].iColumn) {
            case ID:
                indexMap_->SortBy(orderbys[i].desc);
                break;
            default:
                break;
        }
    }

    return SQLITE_OK;
}

int NativeHookTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, static_cast<int32_t>(CurrentRow()));
            break;
        case EVENT_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.EventIds()[CurrentRow()]));
            break;
        case IPID:
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.Ipids()[CurrentRow()]));
            break;
        case ITID:
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.Itids()[CurrentRow()]));
            break;
        case EVENT_TYPE: {
            if (!nativeHookObj_.EventTypes()[CurrentRow()].empty()) {
                sqlite3_result_text(context_, nativeHookObj_.EventTypes()[CurrentRow()].c_str(), STR_DEFAULT_LEN, nullptr);
            }
            break;
        }
        case SUB_TYPE: {
            if (nativeHookObj_.SubTypes()[CurrentRow()] != INVALID_UINT64) {
                auto subTypeIndex = static_cast<size_t>(nativeHookObj_.SubTypes()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(subTypeIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case START_TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.TimeStamData()[CurrentRow()]));
            break;
        case END_TS:
            if (static_cast<int64_t>(nativeHookObj_.EndTimeStamps()[CurrentRow()]) != 0) {
                sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.EndTimeStamps()[CurrentRow()]));
            }
            break;
        case DURATION:
            if (static_cast<int64_t>(nativeHookObj_.Durations()[CurrentRow()]) != 0) {
                sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.Durations()[CurrentRow()]));
            }
            break;
        case ADDR: {
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.Addrs()[CurrentRow()]));
            break;
        }
        case MEM_SIZE: {
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.MemSizes()[CurrentRow()]));
            break;
        }
        case ALL_MEM_SIZE: {
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.AllMemSizes()[CurrentRow()]));
            break;
        }
        case CURRENT_SIZE_DUR: {
            sqlite3_result_int64(context_, static_cast<int64_t>(nativeHookObj_.CurrentSizeDurs()[CurrentRow()]));
            break;
        }
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}

void NativeHookTable::Cursor::FilterId(unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_INTEGER) {
        // other type consider it NULL
        indexMap_->Intersect(0, 0);
        return;
    }

    auto v = static_cast<TableRowId>(sqlite3_value_int64(argv));
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            indexMap_->Intersect(v, v + 1);
            break;
        case SQLITE_INDEX_CONSTRAINT_GE:
            indexMap_->Intersect(v, rowCount_);
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
            v++;
            indexMap_->Intersect(v, rowCount_);
            break;
        case SQLITE_INDEX_CONSTRAINT_LE:
            v++;
            indexMap_->Intersect(0, v);
            break;
        case SQLITE_INDEX_CONSTRAINT_LT:
            indexMap_->Intersect(0, v);
            break;
        default:
            // can't filter, all rows
            break;
    }
}
} // namespace TraceStreamer
} // namespace SysTuning
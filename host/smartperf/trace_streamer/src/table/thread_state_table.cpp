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

#include "thread_state_table.h"

#include <cmath>

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, TS, DUR, CPU, INTERNAL_TID, TID, PID, STATE };
}
ThreadStateTable::ThreadStateTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "TEXT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("tid", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("pid", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("state", "TEXT"));
    tablePriKey_.push_back("id");
}

ThreadStateTable::~ThreadStateTable() {}

void ThreadStateTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
{
    constexpr double filterBaseCost = 1000.0; // set-up and tear-down
    constexpr double indexCost = 2.0;
    ei.estimatedCost = filterBaseCost;

    auto rowCount = dataCache_->GetConstThreadStateData().RowData().size();
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
        FilterByConstraint(fc, filterCost, rowCount);
    }
    ei.estimatedCost += filterCost;
    ei.estimatedRows = rowCount;
    ei.estimatedCost += rowCount * indexCost;

    ei.isOrdered = true;
    auto orderbys = fc.GetOrderBys();
    for (auto i = 0; i < orderbys.size(); i++) {
        switch (orderbys[i].iColumn) {
            case ID:
            case TS:
                break;
            default: // other columns can be sorted by SQLite
                ei.isOrdered = false;
                break;
        }
    }
}

void ThreadStateTable::FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount)
{
    auto fcConstraints = fc.GetConstraints();
    for (int i = 0; i < static_cast<int>(fcConstraints.size()); i++) {
        if (rowCount <= 1) {
            // only one row or nothing, needn't filter by constraint
            filterCost += rowCount;
            break;
        }
        const auto& c = fcConstraints[i];
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
            case TS: {
                auto oldRowCount = rowCount;
                if (CanFilterSorted(c.op, rowCount)) {
                    fc.UpdateConstraint(i, true);
                    filterCost += log2(oldRowCount); // binary search
                } else {
                    filterCost += oldRowCount;
                }
                break;
            }
            default:                    // other column
                filterCost += rowCount; // scan all rows
                break;
        }
    }
}

bool ThreadStateTable::CanFilterId(const char op, size_t& rowCount)
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

bool ThreadStateTable::CanFilterSorted(const char op, size_t& rowCount)
{
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            rowCount = rowCount / log2(rowCount);
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
        case SQLITE_INDEX_CONSTRAINT_GE:
        case SQLITE_INDEX_CONSTRAINT_LE:
        case SQLITE_INDEX_CONSTRAINT_LT:
            rowCount = (rowCount >> 1);
            break;
        default:
            return false;
    }
    return true;
}

std::unique_ptr<TableBase::Cursor> ThreadStateTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

ThreadStateTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, dataCache->GetConstThreadStateData().RowData().size()),
      rowData_(dataCache->GetConstThreadStateData().RowData())
{
}

ThreadStateTable::Cursor::~Cursor() {}

int ThreadStateTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
{
    // reset
    indexType_ = INDEX_TYPE_ID;
    indexMap_ = std::make_unique<IndexMap>(0, rowCount_);

    if (rowCount_ <= 0) {
        return SQLITE_OK;
    }
    auto& cs = fc.GetConstraints();
    for (size_t i = 0; i < cs.size(); i++) {
        const auto& c = cs[i];
        switch (c.col) {
            case ID:
                FilterId(c.op, argv[i]);
                break;
            case TS:
                FilterSorted(c.col, c.op, argv[i]);
                break;
            case INTERNAL_TID:
                FilterIndex(c.col, c.op, argv[i]);
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
            case TS:
                indexMap_->SortBy(orderbys[i].desc);
                break;
            default:
                break;
        }
    }

    return SQLITE_OK;
}

int ThreadStateTable::Cursor::Column(int col) const
{
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(CurrentRow()));
            break;
        case TYPE:
            sqlite3_result_text(context_, "thread_state", STR_DEFAULT_LEN, nullptr);
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(rowData_[CurrentRow()].timeStamp));
            break;
        case DUR:
            if (static_cast<sqlite3_int64>(rowData_[CurrentRow()].duration) != INVALID_TIME) {
                sqlite3_result_int64(context_, static_cast<sqlite3_int64>(rowData_[CurrentRow()].duration));
            }
            break;
        case CPU:
            if (rowData_[CurrentRow()].cpu != INVALID_CPU) {
                sqlite3_result_int64(context_, static_cast<sqlite3_int64>(rowData_[CurrentRow()].cpu));
            }
            break;
        case INTERNAL_TID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(rowData_[CurrentRow()].idTid));
            break;
        case TID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(rowData_[CurrentRow()].tid));
            break;
        case PID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(rowData_[CurrentRow()].pid));
            break;
        case STATE: {
            const std::string& str = dataCache_->GetConstSchedStateData(rowData_[CurrentRow()].idState);
            sqlite3_result_text(context_, str.c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        }
        default:
            TS_LOGF("Unregistered column : %d", col);
            break;
    }
    return SQLITE_OK;
}

void ThreadStateTable::Cursor::FilterId(unsigned char op, sqlite3_value* argv)
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
void ThreadStateTable::Cursor::FilterItid(unsigned char op, uint64_t value)
{
    rowIndex_.clear();
    auto size = rowData_.size();
    indexType_  = INDEX_TYPE_OUTER_INDEX;
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            for (auto i = 0; i < size; i++) {
                if (rowData_[i].idTid == value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        default:
            break;
    } // end of switch (op)
}
void ThreadStateTable::Cursor::FilterIndex(int col, unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_INTEGER) {
        // other type consider it NULL, filter out nothing
        indexMap_->Intersect(0, 0);
        return;
    }

    switch (col) {
        case INTERNAL_TID:
            FilterItid(op, static_cast<uint64_t>(sqlite3_value_int64(argv)));
            break;
        default:
            // we can't filter all rows
            break;
    }
}
void ThreadStateTable::Cursor::FilterTS(unsigned char op, sqlite3_value* argv)
{
    auto v = static_cast<uint64_t>(sqlite3_value_int64(argv));
    auto getValue = [](const ThreadState::ColumnData& row) {
        return row.timeStamp;
    };
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            indexMap_->IntersectabcEqual(rowData_, v, getValue);
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
            v++;
        case SQLITE_INDEX_CONSTRAINT_GE: {
            indexMap_->IntersectGreaterEqual(rowData_, v, getValue);
            break;
        }
        case SQLITE_INDEX_CONSTRAINT_LE:
            v++;
        case SQLITE_INDEX_CONSTRAINT_LT: {
            indexMap_->IntersectLessEqual(rowData_, v, getValue);
            break;
        }
        default:
            break;
    } // end of switch (op)
}
void ThreadStateTable::Cursor::FilterSorted(int col, unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_INTEGER) {
        // other type consider it NULL, filter out nothing
        indexMap_->Intersect(0, 0);
        return;
    }

    switch (col) {
        case TS:
            FilterTS(op, argv);
            break;
        default:
            // can't filter, all rows
            break;
    }
}
} // namespace TraceStreamer
} // namespace SysTuning

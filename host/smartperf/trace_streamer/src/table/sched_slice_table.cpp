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

#include "sched_slice_table.h"

#include <cmath>

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, TS, DUR, TS_END, CPU, INTERNAL_TID, INTERNAL_PID, END_STATE, PRIORITY };
}
SchedSliceTable::SchedSliceTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts_end", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("itid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ipid", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("end_state", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("priority", "INT"));
    tablePriKey_.push_back("id");
}

SchedSliceTable::~SchedSliceTable() {}

void SchedSliceTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
{
    constexpr double filterBaseCost = 1000.0; // set-up and tear-down
    constexpr double indexCost = 2.0;
    ei.estimatedCost = filterBaseCost;

    auto rowCount = dataCache_->GetConstSchedSliceData().Size();
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

void SchedSliceTable::FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount)
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

bool SchedSliceTable::CanFilterId(const char op, size_t& rowCount)
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

bool SchedSliceTable::CanFilterSorted(const char op, size_t& rowCount)
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

std::unique_ptr<TableBase::Cursor> SchedSliceTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

SchedSliceTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, static_cast<uint32_t>(dataCache->GetConstSchedSliceData().Size())),
      schedSliceObj_(dataCache->GetConstSchedSliceData())
{
}

SchedSliceTable::Cursor::~Cursor() {}

int SchedSliceTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
{
    // reset indexMap_
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

int SchedSliceTable::Cursor::Column(int col) const
{
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(CurrentRow()));
            break;
        case TYPE:
            sqlite3_result_text(context_, "sched_slice", STR_DEFAULT_LEN, nullptr);
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.TimeStamData()[CurrentRow()]));
            break;
        case DUR:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.DursData()[CurrentRow()]));
            break;
        case TS_END:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.TsEndData()[CurrentRow()]));
            break;
        case CPU:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.CpusData()[CurrentRow()]));
            break;
        case INTERNAL_TID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.InternalTidsData()[CurrentRow()]));
            break;
        case INTERNAL_PID:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.InternalPidsData()[CurrentRow()]));
            break;
        case END_STATE: {
            const std::string& str = dataCache_->GetConstSchedStateData(schedSliceObj_.EndStatesData()[CurrentRow()]);
            sqlite3_result_text(context_, str.c_str(), STR_DEFAULT_LEN, nullptr);
            break;
        }
        case PRIORITY:
            sqlite3_result_int64(context_, static_cast<sqlite3_int64>(schedSliceObj_.PriorityData()[CurrentRow()]));
            break;
        default:
            TS_LOGF("Unregistered column : %d", col);
            break;
    }
    return SQLITE_OK;
}

void SchedSliceTable::Cursor::FilterId(unsigned char op, sqlite3_value* argv)
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

void SchedSliceTable::Cursor::FilterSorted(int col, unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_INTEGER) {
        // other type consider it NULL, filter out nothing
        indexMap_->Intersect(0, 0);
        return;
    }

    switch (col) {
        case TS: {
            auto v = static_cast<uint64_t>(sqlite3_value_int64(argv));
            auto getValue = [](const uint64_t& row) {
                return row;
            };
            switch (op) {
                case SQLITE_INDEX_CONSTRAINT_EQ:
                    indexMap_->IntersectabcEqual(schedSliceObj_.TimeStamData(), v, getValue);
                    break;
                case SQLITE_INDEX_CONSTRAINT_GT:
                    v++;
                case SQLITE_INDEX_CONSTRAINT_GE: {
                    indexMap_->IntersectGreaterEqual(schedSliceObj_.TimeStamData(), v, getValue);
                    break;
                }
                case SQLITE_INDEX_CONSTRAINT_LE:
                    v++;
                case SQLITE_INDEX_CONSTRAINT_LT: {
                    indexMap_->IntersectLessEqual(schedSliceObj_.TimeStamData(), v, getValue);
                    break;
                }
                default:
                    break;
            } // end of switch (op)
        } // end of case TS
        default:
            // can't filter, all rows
            break;
    }
}

void SchedSliceTable::Cursor::FilterItid(unsigned char op, uint64_t value)
{
    rowIndex_.clear();
    auto size = schedSliceObj_.internalTids_.size();
    indexType_  = INDEX_TYPE_OUTER_INDEX;
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            for (auto i = 0; i < size; i++) {
                if (schedSliceObj_.internalTids_[i] == value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        default:
            break;
    } // end of switch (op)
}
void SchedSliceTable::Cursor::FilterIpid(unsigned char op, uint64_t value)
{
    rowIndex_.clear();
    auto size = schedSliceObj_.InternalPidsData().size();
    indexType_  = INDEX_TYPE_OUTER_INDEX;
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            for (auto i = 0; i < size; i++) {
                if (schedSliceObj_.InternalPidsData()[i] == value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        default:
            break;
    } // end of switch (op)
}
void SchedSliceTable::Cursor::FilterIndex(int col, unsigned char op, sqlite3_value* argv)
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
        case INTERNAL_PID:
            FilterIpid(op, static_cast<uint64_t>(sqlite3_value_int64(argv)));
            break;
        default:
            // we can't filter all rows
            break;
    }
}
void SchedSliceTable::Cursor::FilterTS(unsigned char op, sqlite3_value* argv)
{
    auto v = static_cast<uint64_t>(sqlite3_value_int64(argv));
    auto getValue = [](const uint32_t& row) {
        return row;
    };
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            indexMap_->IntersectabcEqual(schedSliceObj_.TimeStamData(), v, getValue);
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
            v++;
        case SQLITE_INDEX_CONSTRAINT_GE: {
            indexMap_->IntersectGreaterEqual(schedSliceObj_.TimeStamData(), v, getValue);
            break;
        }
        case SQLITE_INDEX_CONSTRAINT_LE:
            v++;
        case SQLITE_INDEX_CONSTRAINT_LT: {
            indexMap_->IntersectLessEqual(schedSliceObj_.TimeStamData(), v, getValue);
            break;
        }
        default:
            break;
    } // end of switch (op)
}
} // namespace TraceStreamer
} // namespace SysTuning

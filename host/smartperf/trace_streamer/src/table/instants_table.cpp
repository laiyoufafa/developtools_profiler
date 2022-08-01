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

#include "instants_table.h"
#include <cmath>

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { TS = 0, NAME, REF, WAKEUP_FROM, REF_TYPE, VALUE };
}
InstantsTable::InstantsTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("ref", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("wakeup_from", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ref_type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("value", "DOUBLE"));
    tablePriKey_.push_back("ts");
    tablePriKey_.push_back("ref");
}

InstantsTable::~InstantsTable() {}

std::unique_ptr<TableBase::Cursor> InstantsTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

InstantsTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, static_cast<uint32_t>(dataCache->GetConstInstantsData().Size())),
      InstantsObj_(dataCache->GetConstInstantsData())
{
}

InstantsTable::Cursor::~Cursor() {}

void InstantsTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
{
    constexpr double filterBaseCost = 1000.0; // set-up and tear-down
    constexpr double indexCost = 2.0;
    ei.estimatedCost = filterBaseCost;

    auto rowCount = dataCache_->GetConstMeasureData().Size();
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
            case TS:
                break;
            case NAME:
                break;
            case REF:
                break;
            case WAKEUP_FROM:
                break;
            default: // other columns can be sorted by SQLite
                ei.isOrdered = false;
                break;
        }
    }
}

void InstantsTable::FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount)
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

bool InstantsTable::CanFilterSorted(const char op, size_t& rowCount)
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

int InstantsTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
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
            case TS:
                FilterSorted(c.col, c.op, argv[i]);
                break;
            case NAME:
                FilterName(c.col, c.op, argv[i]);
                break;
            case REF:
                FilterRef(c.col, c.op, argv[i]);
                break;
            case WAKEUP_FROM:
                FilterWakeUP(c.col, c.op, argv[i]);
                break;
            default:
                break;
        }
    }

    auto orderbys = fc.GetOrderBys();
    for (auto i = orderbys.size(); i > 0;) {
        i--;
        switch (orderbys[i].iColumn) {
            case TS:
                indexMap_->SortBy(orderbys[i].desc);
                break;
            case NAME:
                indexMap_->SortBy(orderbys[i].desc);
                break;
            case REF:
                indexMap_->SortBy(orderbys[i].desc);
                break;
            case WAKEUP_FROM:
                indexMap_->SortBy(orderbys[i].desc);
                break;
            default:
                break;
        }
    }

    return SQLITE_OK;
}

void InstantsTable::Cursor::FilterSorted(int col, unsigned char op, sqlite3_value* argv)
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
                    indexMap_->IntersectabcEqual(InstantsObj_.TimeStamData(), v, getValue);
                    break;
                case SQLITE_INDEX_CONSTRAINT_GT:
                    v++;
                case SQLITE_INDEX_CONSTRAINT_GE: {
                    indexMap_->IntersectGreaterEqual(InstantsObj_.TimeStamData(), v, getValue);
                    break;
                }
                case SQLITE_INDEX_CONSTRAINT_LE:
                    v++;
                case SQLITE_INDEX_CONSTRAINT_LT: {
                    indexMap_->IntersectLessEqual(InstantsObj_.TimeStamData(), v, getValue);
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

void InstantsTable::Cursor::FilterName(int col, unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_TEXT) {
        // other type consider it NULL, filter out nothing
        indexMap_->Intersect(0, 0);
        return;
    }
    if (op != SQLITE_INDEX_CONSTRAINT_EQ) {
        return;
    }
    std::string value = (char*)sqlite3_value_text(argv);
    switch (col) {
        case NAME: {
            rowIndex_.clear();
            auto size = InstantsObj_.NameIndexsData().size();
            auto valueIndex = const_cast<TraceDataCache*>(dataCache_)->GetDataIndex(value);
            indexType_ = INDEX_TYPE_OUTER_INDEX;
            for (auto i = 0; i < size; i++) {
                size_t stringIdentity = static_cast<size_t>(InstantsObj_.NameIndexsData()[i]);
                if (valueIndex == stringIdentity) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
        } // end of case NAME
        default:
            // can't filter, all rows
            break;
    }
}

void InstantsTable::Cursor::FilterRef(int col, unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_INTEGER) {
        // other type consider it NULL, filter out nothing
        indexMap_->Intersect(0, 0);
        return;
    }
    auto value = sqlite3_value_int64(argv);
    switch (col) {
        case REF: {
            FilterFilterRef(op, value);
        } // end of case REF
        default:
            // can't filter, all rows
            break;
    }
}

void InstantsTable::Cursor::FilterFilterRef(unsigned char op, sqlite3_int64 value)
{
    rowIndex_.clear();
    auto size = InstantsObj_.InternalTidsData().size();
    indexType_ = INDEX_TYPE_OUTER_INDEX;
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.InternalTidsData()[i] == value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.InternalTidsData()[i] > value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_GE:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.InternalTidsData()[i] >= value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_LE:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.InternalTidsData()[i] <= value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_LT:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.InternalTidsData()[i] < value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        default:
            break;
    } // end of switch (op)
}

void InstantsTable::Cursor::FilterWakeUP(int col, unsigned char op, sqlite3_value* argv)
{
    auto type = sqlite3_value_type(argv);
    if (type != SQLITE_INTEGER) {
        // other type consider it NULL, filter out nothing
        indexMap_->Intersect(0, 0);
        return;
    }
    auto value = sqlite3_value_int64(argv);
    switch (col) {
        case WAKEUP_FROM: {
            FilterFilterWakeUP(op, value);
        } // end of case WAKEUP_FROM
        default:
            // can't filter, all rows
            break;
    }
}

void InstantsTable::Cursor::FilterFilterWakeUP(unsigned char op, sqlite3_int64 value)
{
    rowIndex_.clear();
    auto size = InstantsObj_.WakeupFromPidsData().size();
    indexType_ = INDEX_TYPE_OUTER_INDEX;
    switch (op) {
        case SQLITE_INDEX_CONSTRAINT_EQ:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.WakeupFromPidsData()[i] == value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_GT:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.WakeupFromPidsData()[i] > value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_GE:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.WakeupFromPidsData()[i] >= value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_LE:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.WakeupFromPidsData()[i] <= value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        case SQLITE_INDEX_CONSTRAINT_LT:
            for (auto i = 0; i < size; i++) {
                if (InstantsObj_.WakeupFromPidsData()[i] < value) {
                    rowIndex_.push_back(i);
                }
            }
            indexSize_ = rowIndex_.size();
            break;
        default:
            break;
    } // end of switch (op)
}

int InstantsTable::Cursor::Column(int column) const
{
    size_t stringIdentity = static_cast<size_t>(InstantsObj_.NameIndexsData()[CurrentRow()]);
    switch (column) {
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(InstantsObj_.TimeStamData()[CurrentRow()]));
            break;
        case NAME: {
            sqlite3_result_text(context_, dataCache_->GetDataFromDict(stringIdentity).c_str(),
                STR_DEFAULT_LEN, nullptr);
            break;
        }
        case REF:
            sqlite3_result_int64(context_, static_cast<int32_t>(InstantsObj_.InternalTidsData()[CurrentRow()]));
            break;
        case WAKEUP_FROM:
            sqlite3_result_int64(context_, static_cast<int32_t>(InstantsObj_.WakeupFromPidsData()[CurrentRow()]));
            break;
        case REF_TYPE: {
            sqlite3_result_text(context_, "itid", STR_DEFAULT_LEN, nullptr);
            break;
        }
        case VALUE: {
            sqlite3_result_double(context_, 0.0);
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

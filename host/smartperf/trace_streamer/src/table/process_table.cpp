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

#include "process_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index {ID = 0, TYPE, PID, NAME, START_TS };
}
ProcessTable::ProcessTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("pid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("start_ts", "UNSIGNED BIG INT"));
    tablePriKey_.push_back("id");
}

ProcessTable::~ProcessTable() {}

void ProcessTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
{
    constexpr double filterBaseCost = 1000.0; // set-up and tear-down
    constexpr double indexCost = 2.0;
    ei.estimatedCost = filterBaseCost;

    auto rowCount = dataCache_->ProcessSize();
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
                break;
            default: // other columns can be sorted by SQLite
                ei.isOrdered = false;
                break;
        }
    }
}

void ProcessTable::FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount)
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
            default:                    // other column
                filterCost += rowCount; // scan all rows
                break;
        }
    }
}

bool ProcessTable::CanFilterId(const char op, size_t& rowCount)
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

int ProcessTable::Update(int argc, sqlite3_value** argv, sqlite3_int64* pRowid)
{
    if (argc <= 1) {
        return SQLITE_READONLY;
    }
    if (sqlite3_value_type(argv[0]) == SQLITE_NULL) {
        return SQLITE_READONLY;
    }
    auto id = sqlite3_value_int64(argv[0]);
    auto process = wdataCache_->GetProcessData(static_cast<InternalPid>(id));
    constexpr int colOffset = 2;
    for (auto i = colOffset; i < argc; i++) {
        auto col = i - colOffset;
        if (col != NAME) {
            continue;
        }
        const char* name = reinterpret_cast<const char*>(sqlite3_value_text(argv[i]));
        if (name == nullptr) {
            process->cmdLine_.clear();
        } else {
            process->cmdLine_ = name;
        }
        break;
    }
    return SQLITE_OK;
}

std::unique_ptr<TableBase::Cursor> ProcessTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

ProcessTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, dataCache->ProcessSize())
{
}

ProcessTable::Cursor::~Cursor() {}

int ProcessTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
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

int ProcessTable::Cursor::Column(int col) const
{
    const auto& process = dataCache_->GetConstProcessData(CurrentRow());
    switch (col) {
        case ID:
            sqlite3_result_int64(context_, CurrentRow());
            break;
        case TYPE:
            sqlite3_result_text(context_, "process", STR_DEFAULT_LEN, nullptr);
            break;
        case PID:
            sqlite3_result_int64(context_, process.pid_);
            break;
        case NAME:
            if (process.cmdLine_.size()) {
                sqlite3_result_text(context_, process.cmdLine_.c_str(), static_cast<int>(process.cmdLine_.length()),
                                    nullptr);
            }
            break;
        case START_TS:
            if (process.startT_) {
                sqlite3_result_int64(context_, static_cast<int64_t>(process.startT_));
            }
            break;
        default:
            TS_LOGF("Unregistered column : %d", col);
            break;
    }
    return SQLITE_OK;
}

void ProcessTable::Cursor::FilterId(unsigned char op, sqlite3_value* argv)
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

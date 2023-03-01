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

#include "gpu_counter_object_table.h"
#include <cmath>

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { COUNTER_ID = 0, COUNTER_NAME = 1 };
}
GpuCounterObjectTable::GpuCounterObjectTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("counter_id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("counter_name", "REAL"));
    tablePriKey_.push_back("counter_id");
}

GpuCounterObjectTable::~GpuCounterObjectTable() {}

// void GpuCounterObjectTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
// {
//     constexpr double filterBaseCost = 1000.0; // set-up and tear-down
//     constexpr double indexCost = 2.0;
//     ei.estimatedCost = filterBaseCost;

//     auto rowCount = dataCache_->GetConstGpuCounterObjectData().Size();
//     if (rowCount == 0 || rowCount == 1) {
//         ei.estimatedRows = rowCount;
//         ei.estimatedCost += indexCost * rowCount;
//         return;
//     }

//     double filterCost = 0.0;
//     auto constraints = fc.GetConstraints();
//     if (constraints.empty()) { // scan all rows
//         filterCost = rowCount;
//     } else {
//         FilterByConstraint(fc, filterCost, rowCount);
//     }
//     ei.estimatedCost += filterCost;
//     ei.estimatedRows = rowCount;
//     ei.estimatedCost += rowCount * indexCost;

//     ei.isOrdered = true;
//     auto orderbys = fc.GetOrderBys();
//     for (auto i = 0; i < orderbys.size(); i++) {
//         switch (orderbys[i].iColumn) {
//             case COUNTER_ID:
//                 break;
//             default: // other columns can be sorted by SQLite
//                 ei.isOrdered = false;
//                 break;
//         }
//     }
// }

// void GpuCounterObjectTable::FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount)
// {
//     auto fcConstraints = fc.GetConstraints();
//     for (int i = 0; i < static_cast<int>(fcConstraints.size()); i++) {
//         if (rowCount <= 1) {
//             // only one row or nothing, needn't filter by constraint
//             filterCost += rowCount;
//             break;
//         }
//         const auto& c = fcConstraints[i];
//         switch (c.col) {
//             case COUNTER_ID: {
//                 if (CanFilterId(c.op, rowCount)) {
//                     fc.UpdateConstraint(i, true);
//                     filterCost += 1; // binary search
//                 } else {
//                     filterCost += rowCount;
//                 }
//                 break;
//             }
//             default:                    // other column
//                 filterCost += rowCount; // scan all rows
//                 break;
//         }
//     }
// }

// bool GpuCounterObjectTable::CanFilterId(const char op, size_t& rowCount)
// {
//     switch (op) {
//         case SQLITE_INDEX_CONSTRAINT_EQ:
//             rowCount = 1;
//             break;
//         case SQLITE_INDEX_CONSTRAINT_GT:
//         case SQLITE_INDEX_CONSTRAINT_GE:
//         case SQLITE_INDEX_CONSTRAINT_LE:
//         case SQLITE_INDEX_CONSTRAINT_LT:
//             rowCount = (rowCount >> 1);
//             break;
//         default:
//             return false;
//     }
//     return true;
// }

std::unique_ptr<TableBase::Cursor> GpuCounterObjectTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

GpuCounterObjectTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, static_cast<uint32_t>(dataCache->GetConstGpuCounterObjectData().Size())),
      gpuCounterObjectDataObj_(dataCache->GetConstGpuCounterObjectData())
{
}

GpuCounterObjectTable::Cursor::~Cursor() {}

// int GpuCounterObjectTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
// {
//     // reset indexMap_
//     indexMap_ = std::make_unique<IndexMap>(0, rowCount_);

//     if (rowCount_ <= 0) {
//         return SQLITE_OK;
//     }

//     auto& cs = fc.GetConstraints();
//     for (size_t i = 0; i < cs.size(); i++) {
//         const auto& c = cs[i];
//         switch (c.col) {
//             case COUNTER_ID:
//                 FilterId(c.op, argv[i]);
//                 break;
//             default:
//                 break;
//         }
//     }

//     auto orderbys = fc.GetOrderBys();
//     for (auto i = orderbys.size(); i > 0;) {
//         i--;
//         switch (orderbys[i].iColumn) {
//             case COUNTER_ID:
//                 indexMap_->SortBy(orderbys[i].desc);
//                 break;
//             default:
//                 break;
//         }
//     }

//     return SQLITE_OK;
// }

int GpuCounterObjectTable::Cursor::Column(int column) const
{
    switch (column) {
        case COUNTER_ID: {
            sqlite3_result_int64(context_, static_cast<int64_t>(gpuCounterObjectDataObj_.CounterId()[CurrentRow()]));
            break;
        }
        case COUNTER_NAME: {
            sqlite3_result_text(context_, gpuCounterObjectDataObj_.CounterName()[CurrentRow()].c_str(), STR_DEFAULT_LEN,
                                nullptr);
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

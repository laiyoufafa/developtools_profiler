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

#include "perf_sample_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index {
    ID = 0,
    CALLCHAIN_ID,
    TIMESTAMP,
    THREAD_ID,
    EVENT_COUNT,
    EVENT_TYPE_ID,
    TIMESTAMP_TRACE,
    CPU_ID,
    THREAD_STATE
};
}
PerfSampleTable::PerfSampleTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("callchain_id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("timestamp", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("thread_id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("event_count", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("event_type_id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("timestamp_trace", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("cpu_id", "INTEGER"));
    tableColumn_.push_back(TableBase::ColumnInfo("thread_state", "TEXT"));
    tablePriKey_.push_back("id");
}

PerfSampleTable::~PerfSampleTable() {}

void PerfSampleTable::EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei)
{
    constexpr double filterBaseCost = 1000.0; // set-up and tear-down
    constexpr double indexCost = 2.0;
    ei.estimatedCost = filterBaseCost;

    auto rowCount = dataCache_->GetConstPerfSampleData().Size();
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

void PerfSampleTable::FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount)
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

bool PerfSampleTable::CanFilterId(const char op, size_t& rowCount)
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

std::unique_ptr<TableBase::Cursor> PerfSampleTable::CreateCursor()
{
    return std::make_unique<Cursor>(dataCache_, this);
}

PerfSampleTable::Cursor::Cursor(const TraceDataCache* dataCache, TableBase* table)
    : TableBase::Cursor(dataCache, table, static_cast<uint32_t>(dataCache->GetConstPerfSampleData().Size())),
      perfSampleObj_(dataCache->GetConstPerfSampleData())
{
}

PerfSampleTable::Cursor::~Cursor() {}

int PerfSampleTable::Cursor::Filter(const FilterConstraints& fc, sqlite3_value** argv)
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
            case CALLCHAIN_ID:
                indexMap_->MixRange(c.op, static_cast<uint64_t>(sqlite3_value_int64(argv[i])),
                                    perfSampleObj_.SampleIds());
                break;
            case THREAD_ID:
                indexMap_->MixRange(c.op, static_cast<uint64_t>(sqlite3_value_int64(argv[i])), perfSampleObj_.Tids());
                break;
            case EVENT_TYPE_ID:
                indexMap_->MixRange(c.op, static_cast<uint64_t>(sqlite3_value_int64(argv[i])),
                                    perfSampleObj_.EventTypeIds());
                break;
            case CPU_ID:
                indexMap_->MixRange(c.op, static_cast<uint64_t>(sqlite3_value_int64(argv[i])), perfSampleObj_.CpuIds());
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

int PerfSampleTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, static_cast<int32_t>(perfSampleObj_.IdsData()[CurrentRow()]));
            break;
        case CALLCHAIN_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.SampleIds()[CurrentRow()]));
            break;
        case TIMESTAMP:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.TimeStamData()[CurrentRow()]));
            break;
        case THREAD_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.Tids()[CurrentRow()]));
            break;
        case EVENT_COUNT:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.EventCounts()[CurrentRow()]));
            break;
        case EVENT_TYPE_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.EventTypeIds()[CurrentRow()]));
            break;
        case TIMESTAMP_TRACE:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.TimestampTraces()[CurrentRow()]));
            break;
        case CPU_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(perfSampleObj_.CpuIds()[CurrentRow()]));
            break;
        case THREAD_STATE:
            if (perfSampleObj_.ThreadStates()[CurrentRow()] != INVALID_UINT64) {
                auto threadStateIndex = static_cast<size_t>(perfSampleObj_.ThreadStates()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(threadStateIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

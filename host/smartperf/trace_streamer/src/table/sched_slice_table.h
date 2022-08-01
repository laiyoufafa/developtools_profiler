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

#ifndef SCHED_SLICE_TABLE_H
#define SCHED_SLICE_TABLE_H

#include "table_base.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
class SchedSliceTable : public TableBase {
public:
    explicit SchedSliceTable(const TraceDataCache* dataCache);
    ~SchedSliceTable() override;
    std::unique_ptr<TableBase::Cursor> CreateCursor() override;

private:
    void EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei) override;
    // filter out by operator[=, >, <...] from column(ID)
    bool CanFilterId(const char op, size_t& rowCount);
    // the column is sorted
    bool CanFilterSorted(const char op, size_t& rowCount);
    void FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount);

    class Cursor : public TableBase::Cursor {
    public:
        explicit Cursor(const TraceDataCache* dataCache, TableBase* table);
        ~Cursor() override;
        int Filter(const FilterConstraints& fc, sqlite3_value** argv) override;
        int Column(int col) const override;

        void FilterId(unsigned char op, sqlite3_value* argv);
        void FilterSorted(int col, unsigned char op, sqlite3_value* argv);
        void FilterTS(unsigned char op, sqlite3_value* argv);
        void FilterIndex(int col, unsigned char op, sqlite3_value* argv);
        void FilterItid(unsigned char op, uint64_t value);
        void FilterIpid(unsigned char op, uint64_t value);
        uint32_t CurrentRow() const override
        {
            switch (indexType_) {
                case INDEX_TYPE_ID:
                    return indexMap_->CurrentRow();
                case INDEX_TYPE_OUTER_INDEX:
                    return rowIndex_[index_];
                default:
                    break;
            }
            return INVALID_UINT32;
        }
        int Next() override
        {
            switch (indexType_) {
                case INDEX_TYPE_ID:
                    /* code */
                    indexMap_->Next();
                    break;
                case INDEX_TYPE_OUTER_INDEX:
                    /* code */
                    index_++;
                    break;
                default:
                    break;
            }
            return SQLITE_OK;
        }
        int Eof() override
        {
            switch (indexType_) {
                case INDEX_TYPE_ID:
                    return dataCache_->Cancel() || indexMap_->Eof();
                case INDEX_TYPE_OUTER_INDEX:
                    return dataCache_->Cancel() || (index_ == indexSize_);
                default:
                    break;
            }
            return INVALID_UINT32;
        }

    private:
        const SchedSlice& schedSliceObj_;
        std::deque<uint64_t> rowIndex_;
        enum IndexType {
            INDEX_TYPE_ID,
            INDEX_TYPE_OUTER_INDEX,
        };
        IndexType indexType_ = INDEX_TYPE_ID;
        uint32_t index_ = 0;
        uint32_t indexSize_ = 0;
    };
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // SCHED_SLICE_TABLE_H

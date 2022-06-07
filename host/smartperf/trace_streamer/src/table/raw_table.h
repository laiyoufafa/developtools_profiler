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

#ifndef RAW_TABLE_H
#define RAW_TABLE_H

#include "table_base.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
class RawTable : public TableBase {
public:
    enum EventName : uint32_t { CPU_IDLE = 1, SCHED_WAKEUP = 2, SCHED_WAKING = 3 };
    explicit RawTable(const TraceDataCache* dataCache);
    ~RawTable() override;
    std::unique_ptr<TableBase::Cursor> CreateCursor() override;

private:
    void EstimateFilterCost(FilterConstraints& fc, EstimatedIndexInfo& ei) override;
    // filter out by operator[=, >, <...] from column(ID)
    bool CanFilterId(const char op, size_t& rowCount);
    void FilterByConstraint(FilterConstraints& fc, double& filterCost, size_t rowCount);

    class Cursor : public TableBase::Cursor {
    public:
        explicit Cursor(const TraceDataCache* dataCache, TableBase* table);
        ~Cursor() override;
        int Filter(const FilterConstraints& fc, sqlite3_value** argv) override;
        int Column(int column) const override;

        void FilterId(unsigned char op, sqlite3_value* argv);

    private:
        const Raw& rawObj_;
    };
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // RAW_TABLE_H

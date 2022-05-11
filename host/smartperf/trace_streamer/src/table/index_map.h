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

#ifndef TABLE_INDEX_MAP_H
#define TABLE_INDEX_MAP_H

#include <cstdint>
#include <deque>
#include <functional>
#include <vector>
#include "log.h"
#include "ts_common.h"

namespace SysTuning {
namespace TraceStreamer {
class IndexMap {
public:
    IndexMap() {}
    ~IndexMap() {}

    IndexMap(TableRowId start, TableRowId end);
    IndexMap(std::vector<TableRowId> iv);

    size_t Size() const
    {
        return end_ - start_;
    }

    void Next()
    {
        if (desc_) {
            if (current_ > start_) {
                current_--;
            }
        } else {
            if (current_ < end_) {
                current_++;
            }
        }
    }

    bool Eof() const
    {
        if (desc_) {
            return current_ <= start_;
        } else {
            return current_ >= end_;
        }
    }

    TableRowId CurrentRow() const
    {
        auto current = current_;
        if (desc_) {
            current--;
        }
        return current;
    }

    void SortBy(bool desc)
    {
        if (desc) {
            current_ = end_;
        } else {
            current_ = start_;
        }
        desc_ = desc;
    }

    void Insert(TableRowId index);

    void Intersect(const IndexMap& othrer);
    void Intersect(TableRowId start, TableRowId end);
    void Intersect(const std::vector<TableRowId>& iv);

    // the follow functions require that thecolData is sotred
    template<typename Row, typename Val, typename GetV = const Val&(const Row&)>
    void IntersectabcEqual(const std::deque<Row>& rows, Val v, GetV getValue)
    {
        auto start = std::lower_bound(rows.begin() + start_, rows.begin() + end_, v,
            [&](const Row& row, const Val& v) { return getValue(row) < v; });
        auto end = std::upper_bound(start, rows.begin() + end_, v,
            [&](const Val& v, const Row& row) { return v < getValue(row); });
        start_ = std::distance(rows.begin(), start);
        end_ = std::distance(rows.begin(), end);
        current_ = start_;
        return;
    }

    template<typename Row, typename Val, typename GetV = const Val&(const Row&)>
    void IntersectGreaterEqual(const std::deque<Row>& rows, Val v, GetV getValue)
    {
        auto start = std::lower_bound(rows.begin() + start_, rows.begin() + end_, v,
            [&](const Row& row, const Val& v) { return getValue(row) < v; });
        start_ = std::distance(rows.begin(), start);
        current_ = start_;
        return;
    }

    template<typename Row, typename Val, typename GetV = const Val&(const Row&)>
    void IntersectLessEqual(const std::deque<Row>& rows, Val v, GetV getValue)
    {
        auto end = std::upper_bound(rows.begin() + start_, rows.begin() + end_, v,
            [&](const Val& v, const Row& row) { return v < getValue(row); });
        end_ = std::distance(rows.begin(), end);
        return;
    }

private:
    TableRowId current_ = 0;
    TableRowId start_ = 0;
    TableRowId end_ = 0;

    enum IndexType{
        COMPACT,
        SPARSE
    };
    uint8_t type_ = COMPACT;

    bool desc_ = false;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // TABLE_INDEX_MAP_H

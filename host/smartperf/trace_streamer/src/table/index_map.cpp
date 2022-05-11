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

#include "index_map.h"

#include <algorithm>

#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
IndexMap::IndexMap(TableRowId start, TableRowId end)
    : current_(start), start_(start), end_(end), type_(COMPACT) {}


void IndexMap::Insert(TableRowId index)
{
    if (index >= start_ && index < end_) {
        return;
    } else if (index + 1 == start_) {
        start_ = index;
        current_ = start_;
        return;
    } else if (index == end_) {
        end_++;
        return;
    }
}

void IndexMap::Intersect(const IndexMap& other)
{
    if (Size() == 0) {
        return;
    }
    if (other.Size() == 0) {
        current_ = 0;
        start_ = 0;
        end_ = 0;
        // indexVec_.clear();
        return;
    }

    Intersect(other.start_, other.end_);
}

void IndexMap::Intersect(TableRowId start, TableRowId end)
{
    start_ = std::max(start_, start);
    end_ = std::min(end_, end);
    current_ = start_;
}

void IndexMap::Intersect(const std::vector<TableRowId>& iv)
{
    auto start = start_;
    auto end = end_;
    // indexVec_ = iv;
    current_ = 0;
    start_ = 0;
    // end_ = static_cast<TableRowId>(indexVec_.size());
    type_ = SPARSE;
    return Intersect(start, end);
}
} // namespace TraceStreamer
} // namespace SysTuning

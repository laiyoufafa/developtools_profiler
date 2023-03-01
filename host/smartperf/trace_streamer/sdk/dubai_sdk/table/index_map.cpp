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

void IndexMap::CovertToIndexMap()
{
    if (converted_) {
        indexType_ = INDEX_TYPE_OUTER_INDEX;
        return;
    }
    converted_ = true;
    if (indexType_ == INDEX_TYPE_ID && HasData()) {
        for (auto i = start_; i < end_; i++) {
            rowIndex_.push_back(i);
        }
        current_ = start_ = 0;
        end_ = rowIndex_.size();
        empty_ = !rowIndex_.size();
    }
    indexType_ = INDEX_TYPE_OUTER_INDEX;
}
bool IndexMap::HasData() {
    return (start_ != 0 || end_ != INVALID_UINT32) || !empty_;
}
void IndexMap::Intersect(TableRowId start, TableRowId end)
{
    if (indexType_ == INDEX_TYPE_OUTER_INDEX) {
        for (auto i = rowIndex_.begin(); i != rowIndex_.end();) {
            if (*i >= start && *i < end) {
                i++;
            } else {
                i = rowIndex_.erase(i);
            }
        }
        start_ = current_ = 0;
        end_ = rowIndex_.size();
    } else {
        start_ = std::max(start_, start);
        end_ = std::min(end_, end);
        current_ = start_;
    }
    empty_ = false;
}
} // namespace TraceStreamer
} // namespace SysTuning

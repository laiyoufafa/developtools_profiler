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

#ifndef HEAP_FRAME_TABLE_H
#define HEAP_FRAME_TABLE_H

#include "table_base.h"
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
class HeapFrameTable : public TableBase {
public:
    explicit HeapFrameTable(const TraceDataCache* dataCache);
    ~HeapFrameTable() override;
    void CreateCursor() override;

private:
    class Cursor : public TableBase::Cursor {
    public:
        explicit Cursor(const TraceDataCache* dataCache);
        ~Cursor() override;
        int Column(int column) const override;

    private:
        const HeapFrameInfo& heapFrameInfoObj_;
    };
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // HEAP_FRAME_TABLE_H

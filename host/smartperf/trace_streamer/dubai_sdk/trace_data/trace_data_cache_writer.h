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

#ifndef TRACE_DATA_CACHE_WRITER_H
#define TRACE_DATA_CACHE_WRITER_H

#include "trace_data_cache_reader.h"

namespace SysTuning {
namespace TraceStreamer {
using namespace TraceStdtype;
class TraceDataCacheWriter : virtual public TraceDataCacheBase {
public:
    TraceDataCacheWriter() = default;
    TraceDataCacheWriter(const TraceDataCacheWriter&) = delete;
    TraceDataCacheWriter& operator=(const TraceDataCacheWriter&) = delete;
    ~TraceDataCacheWriter() override;
    void Clear();

public:
    GpuCounter* GetGpuCounterData();
    GpuCounterObject* GetGpuCounterObjectData();
    SliceObject* GetSliceObjectData();
    SliceData* GetSliceTableData();
    MetaData* GetMetaData();
    void MixTraceTime(uint64_t timestampMin, uint64_t timestampMax);
    // ThreadState* GetThreadStateData();
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif

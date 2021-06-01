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

#ifndef TRACE_STREAMER_H
#define TRACE_STREAMER_H

#include <functional>
#include <memory>
#include "trace_data/trace_data_cache.h"
#include "trace_streamer_filters.h"

namespace SysTuning {
namespace TraceStreamer {
class BytraceParser;
constexpr size_t G_FILE_PERMISSION = 664;
constexpr int G_MIN_PARAM_NUM = 2;
enum FileType { BY_TRACE, PROTO, UN_KNOW };
class TraceStreamer {
public:
    TraceStreamer();
    ~TraceStreamer();

    bool Parse(std::unique_ptr<uint8_t[]>, size_t);
    int ExportDatabase(const std::string& outputName);
private:
    void InitFilter();
    void InitParser();
    FileType fileType_;

    std::unique_ptr<TraceStreamerFilters> streamFilters_;
    std::unique_ptr<TraceDataCache> traceDataCache_;

    std::unique_ptr<BytraceParser> bytraceParser_;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // TRACE_STREAMER_H

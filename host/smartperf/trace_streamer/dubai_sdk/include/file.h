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

#ifndef INCLUDE_TUNING_BASE_FILE_UTILS_H_
#define INCLUDE_TUNING_BASE_FILE_UTILS_H_

#include <string>

namespace SysTuning {
namespace base {
constexpr uint32_t kFileModeInvalid = 0xFFFFFFFF;
enum TraceParserStatus {
    TRACE_PARSER_NORMAL = 0,
    TRACE_PARSER_FILE_TYPE_ERROR = 1,
    TRACE_PARSE_ERROR = 2,
    TRACE_PARSER_ABNORMAL = 3
};
struct ProfilerTraceFileHeader {
    // Some space is reserved to facilitate the subsequent addition of fields in the header
    static constexpr uint32_t HEADER_SIZE = 1024;
    static constexpr uint32_t SHA256_SIZE = 256 / 8;
    static constexpr uint64_t HEADER_MAGIC = 0x464F5250534F484FuLL;
    static constexpr uint32_t V_MAJOR = 0x0001;
    static constexpr uint32_t V_MAJOR_BITS = 16;
    static constexpr uint32_t V_MINOR = 0x0000;
    static constexpr uint32_t TRACE_VERSION = (V_MAJOR << V_MAJOR_BITS) | V_MINOR;
    enum DataType {
        HIPROFILER_PROTOBUF_BIN = 0,
        HIPERF_DATA,
        UNKNOW_TYPE = 1024,
    };
    struct HeaderData {
        // Magic number, used to distinguish offline files
        uint64_t magic = HEADER_MAGIC;
        // Total length, which can be used to check whether the document is truncated;
        uint64_t length = HEADER_SIZE;
        uint32_t version = TRACE_VERSION;
        // The number of segments in the load data. The number of segments is even. One describes the length L and the
        // other describes the next data v
        uint32_t segments = 0;
        // Sha256 of load data is used to verify whether the load data is complete;
        uint8_t sha256[SHA256_SIZE] = {};
        uint32_t dataType = UNKNOW_TYPE;
        // clock
        uint64_t boottime = 0;
        uint64_t realtime = 0;
        uint64_t realtimeCoarse = 0;
        uint64_t monotonic = 0;
        uint64_t monotonicCoarse = 0;
        uint64_t monotonicRaw = 0;
    } __attribute__((packed));
    HeaderData data = {};
    uint8_t padding_[HEADER_SIZE - sizeof(data)] = {};
};

void SetAnalysisResult(TraceParserStatus stat);

TraceParserStatus GetAnalysisResult();

ssize_t Read(int fd, uint8_t* dst, size_t dstSize);

int OpenFile(const std::string& path, int flags, uint32_t mode = kFileModeInvalid);

std::string GetExecutionDirectoryPath();
} // namespace base
} // namespace SysTuning
#endif // INCLUDE_TUNING_BASE_FILE_UTILS_H_

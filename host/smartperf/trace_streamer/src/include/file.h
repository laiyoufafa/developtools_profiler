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
    static constexpr uint32_t HEADER_SIZE = 1024; // 预留了一些空间，方便后续在头部添加字段
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
        uint64_t magic_ = HEADER_MAGIC;  // 魔数，用于区分离线文件
        uint64_t length_ = HEADER_SIZE;  // 总长度，可用于检验文件是否被截断；
        uint32_t version_ = TRACE_VERSION;
        uint32_t segments_ = 0; // 载荷数据中的段个数, 段个数为偶数，一个描述长度 L，一个描述接下来的数据 V
        uint8_t sha256_[SHA256_SIZE] = {}; // 载荷数据 的 SHA256 ，用于校验 载荷数据是否完整；
        DataType data_type = UNKNOW_TYPE;
    } __attribute__((packed));
    HeaderData data_ = {};
    uint8_t padding_[HEADER_SIZE - sizeof(data_)] = {};
};

void SetAnalysisResult(TraceParserStatus stat);

TraceParserStatus GetAnalysisResult();

ssize_t Read(int fd, uint8_t* dst, size_t dstSize);

int OpenFile(const std::string& path, int flags, uint32_t mode = kFileModeInvalid);

std::string GetExecutionDirectoryPath();
} // namespace base
} // namespace SysTuning
#endif // INCLUDE_TUNING_BASE_FILE_UTILS_H_

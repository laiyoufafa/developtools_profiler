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
#ifndef TRACE_FILE_READER_H
#define TRACE_FILE_READER_H

#include "logging.h"
#include "nocopyable.h"
#include "writer.h"

#include <cstdint>
#include <fstream>
#include <google/protobuf/message_lite.h>
#include <string>

using google::protobuf::MessageLite;

class TraceFileReader {
public:
    TraceFileReader() = default;

    ~TraceFileReader();

    bool Open(const std::string& path);

    long Read(MessageLite& message);

    using BytePtr = int8_t *;

    long Read(BytePtr data, long size);

private:
    std::string path_;
    std::ifstream stream_;

    DISALLOW_COPY_AND_MOVE(TraceFileReader);
};

using TraceFileReaderPtr = STD_PTR(shared, TraceFileReader);

#endif // !TRACE_FILE_READER_H
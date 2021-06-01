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
#include "trace_file_writer.h"
#include <memory>

#include "logging.h"

using CharPtr = std::unique_ptr<char>::pointer;
using ConstCharPtr = std::unique_ptr<const char>::pointer;

TraceFileWriter::TraceFileWriter(const std::string& path) : path_(path)
{
    Open(path);
}

TraceFileWriter::~TraceFileWriter()
{
    Flush();
    if (stream_.is_open()) {
        stream_.close();
    }
}

bool TraceFileWriter::Open(const std::string& path)
{
    stream_.open(path, std::ios_base::out | std::ios_base::binary);
    CHECK_TRUE(stream_.is_open(), false, "open %s failed, %s!", path.c_str(), strerror(errno));
    path_ = path;
    return true;
}

long TraceFileWriter::Write(const void* data, size_t size)
{
    CHECK_TRUE(stream_.is_open(), 0, "binary file %s not open or open failed!", path_.c_str());

    uint32_t dataLen = size;
    stream_.write(reinterpret_cast<CharPtr>(&dataLen), sizeof(dataLen));
    CHECK_TRUE(stream_, 0, "binary file %s write raw buffer size failed!", path_.c_str());

    stream_.write(reinterpret_cast<ConstCharPtr>(data), size);
    CHECK_TRUE(stream_, 0, "binary file %s write raw buffer data failed!", path_.c_str());
    return sizeof(dataLen) + size;
}

long TraceFileWriter::Write(const MessageLite& message)
{
    CHECK_TRUE(stream_.is_open(), 0, "binary file %s not open or open failed!", path_.c_str());
    uint32_t msgLen = message.ByteSizeLong();
    stream_.write(reinterpret_cast<CharPtr>(&msgLen), sizeof(msgLen));
    CHECK_TRUE(stream_, 0, "write msg head failed!");

    std::vector<char> msgData(message.ByteSizeLong());
    CHECK_TRUE(message.SerializeToArray(msgData.data(), msgData.size()), 0, "SerializeToArray failed!");

    stream_.write(msgData.data(), msgData.size());
    CHECK_TRUE(stream_, 0, "write msg body failed!");
    HILOG_DEBUG(LOG_CORE, "write %zu bytes to file %s", sizeof(msgLen) + msgLen, path_.c_str());
    return sizeof(msgLen) + message.ByteSizeLong();
}

bool TraceFileWriter::Flush()
{
    CHECK_TRUE(stream_.is_open(), false, "binary file %s not open or open failed!", path_.c_str());
    CHECK_TRUE(stream_.flush(), false, "binary file %s flush failed!", path_.c_str());
    return true;
}

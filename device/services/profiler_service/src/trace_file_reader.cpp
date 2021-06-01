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
#include <memory>

#include "trace_file_reader.h"

#include "logging.h"

using CharPtr = std::unique_ptr<char>::pointer;

TraceFileReader::~TraceFileReader()
{
    if (stream_.is_open()) {
        stream_.close();
    }
}

bool TraceFileReader::Open(const std::string& path)
{
    stream_.open(path, std::ios_base::in | std::ios_base::binary);
    CHECK_TRUE(stream_.is_open(), false, "open %s failed, %s!", path.c_str(), strerror(errno));
    path_ = path;
    return true;
}

long TraceFileReader::Read(MessageLite& message)
{
    CHECK_TRUE(stream_.is_open(), 0, "binary file %s not open or open failed!", path_.c_str());
    CHECK_TRUE(!stream_.eof(), 0, "no more data in file %s stream", path_.c_str());

    uint32_t msgLen = 0;
    stream_.read(reinterpret_cast<CharPtr>(&msgLen), sizeof(msgLen));
    CHECK_TRUE(stream_, 0, "read msg head failed!");

    std::vector<char> msgData(msgLen);
    stream_.read(msgData.data(), msgData.size());
    CHECK_TRUE(stream_, 0, "read msg body failed!");

    CHECK_TRUE(message.ParseFromArray(msgData.data(), msgData.size()), 0, "ParseFromArray failed!");
    return sizeof(msgLen) + msgData.size();
}

long TraceFileReader::Read(BytePtr data, long size)
{
    CHECK_TRUE(stream_.is_open(), 0, "binary file %s not open or open failed!", path_.c_str());
    CHECK_TRUE(!stream_.eof(), 0, "no more data in file %s stream", path_.c_str());
    CHECK_TRUE(stream_.read(reinterpret_cast<CharPtr>(data), size), 0, "binary file %s write raw buffer data failed!",
               path_.c_str());
    return size;
}

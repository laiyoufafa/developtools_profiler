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

#include <cinttypes>
#include <memory>

#include "logging.h"

using CharPtr = std::unique_ptr<char>::pointer;
using ConstCharPtr = std::unique_ptr<const char>::pointer;

TraceFileWriter::TraceFileWriter(const std::string& path) : path_(path), writeBytes_(0)
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

std::string TraceFileWriter::Path() const
{
    return path_;
}

bool TraceFileWriter::SetPluginConfig(const void* data, size_t size)
{
    Write(data, size);
    return true;
}

void TraceFileWriter::SetTimeStamp()
{
    constexpr uint64_t nanoSeconds = 1000000000;
    struct timespec ts;
    clock_gettime(CLOCK_BOOTTIME, &ts);
    header_.data_.boottime = static_cast<uint64_t>(ts.tv_sec) * nanoSeconds +
        static_cast<uint64_t>(ts.tv_nsec);
    clock_gettime(CLOCK_REALTIME, &ts);
    header_.data_.realtime = static_cast<uint64_t>(ts.tv_sec) * nanoSeconds +
        static_cast<uint64_t>(ts.tv_nsec);
    clock_gettime(CLOCK_REALTIME_COARSE, &ts);
    header_.data_.realtimeCoarse = static_cast<uint64_t>(ts.tv_sec) * nanoSeconds +
        static_cast<uint64_t>(ts.tv_nsec);
    clock_gettime(CLOCK_MONOTONIC, &ts);
    header_.data_.monotonic = static_cast<uint64_t>(ts.tv_sec) * nanoSeconds +
        static_cast<uint64_t>(ts.tv_nsec);
    clock_gettime(CLOCK_MONOTONIC_COARSE, &ts);
    header_.data_.monotonicCoarse = static_cast<uint64_t>(ts.tv_sec) * nanoSeconds +
        static_cast<uint64_t>(ts.tv_nsec);
    clock_gettime(CLOCK_MONOTONIC_RAW, &ts);
    header_.data_.monotonicRaw = static_cast<uint64_t>(ts.tv_sec) * nanoSeconds +
        static_cast<uint64_t>(ts.tv_nsec);
}

bool TraceFileWriter::Open(const std::string& path)
{
    stream_.open(path, std::ios_base::out | std::ios_base::binary);
    CHECK_TRUE(stream_.is_open(), false, "open %s failed, %d!", path.c_str(), errno);

    // write initial header, makes file write position move forward
    stream_.write(reinterpret_cast<CharPtr>(&header_), sizeof(header_));
    CHECK_TRUE(stream_, false, "write initial header to %s failed!", path_.c_str());
    path_ = path;
    return true;
}

long TraceFileWriter::Write(const void* data, size_t size)
{
    uint32_t dataLen = size;
    CHECK_TRUE(stream_.is_open(), 0, "binary file %s not open or open failed!", path_.c_str());

    // write 4B data length.
    stream_.write(reinterpret_cast<CharPtr>(&dataLen), sizeof(dataLen));
    CHECK_TRUE(stream_, 0, "binary file %s write raw buffer size failed!", path_.c_str());
    CHECK_TRUE(helper_.AddSegment(reinterpret_cast<uint8_t*>(&dataLen), sizeof(dataLen)),
        0, "Add payload for size %u FAILED!", dataLen);

    // write data bytes
    stream_.write(reinterpret_cast<ConstCharPtr>(data), size);
    CHECK_TRUE(stream_, 0, "binary file %s write raw buffer data failed!", path_.c_str());
    CHECK_TRUE(helper_.AddSegment(reinterpret_cast<uint8_t*>(const_cast<void*>(data)), size),
        0, "Add payload for data bytes %zu FAILED!", size);

    long nbytes = sizeof(dataLen) + size;
    writeBytes_ += nbytes;
    ++writeCount_;
    return nbytes;
}

long TraceFileWriter::Write(const MessageLite& message)
{
    // serialize message to bytes array
    std::vector<char> msgData(message.ByteSizeLong());
    CHECK_TRUE(message.SerializeToArray(msgData.data(), msgData.size()), 0, "SerializeToArray failed!");

    return Write(msgData.data(), msgData.size());
}

bool TraceFileWriter::Finish()
{
    // update header info
    helper_.Update(header_);

    // move write position to begin of file
    stream_.seekp(0);
    CHECK_TRUE(stream_, false, "seek write position to head for %s failed!", path_.c_str());

    SetTimeStamp(); // add timestamp in header
    // write final header
    stream_.write(reinterpret_cast<CharPtr>(&header_), sizeof(header_));
    CHECK_TRUE(stream_, false, "write final header to %s failed!", path_.c_str());
    return true;
}

bool TraceFileWriter::Flush()
{
    CHECK_TRUE(stream_.is_open(), false, "binary file %s not open or open failed!", path_.c_str());
    CHECK_TRUE(stream_.flush(), false, "binary file %s flush failed!", path_.c_str());
    HILOG_INFO(LOG_CORE, "flush: %s, bytes: %" PRIu64 ", count: %" PRIu64, path_.c_str(), writeBytes_, writeCount_);
    return true;
}

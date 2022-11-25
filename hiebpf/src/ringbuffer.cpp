/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
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

#include "ringbuffer.h"

#include <memory>
#include <sys/uio.h>
#include <securec.h>
#include <cstring>

RingBuffer::RingBuffer(const std::size_t bufSize, const enum MemAlignShift shift):
    bufSize_ {bufSize},
    alignShift_ {shift}
{
    if (bufSize_ <= DEFAULT_SIZE) {
        bufSize_ = DEFAULT_SIZE;
    }
    switch (shift) {
        case B_ALIGN_SHIFT:
        {
            bufSize_ = (bufSize_ >> B_ALIGN_SHIFT);
            buffer_ = new(std::nothrow) char[bufSize_];
            bufSize_ = (bufSize_ << B_ALIGN_SHIFT);
            break;
        }
        case H_ALIGN_SHIFT:
        {
            bufSize_ = (bufSize_ >> H_ALIGN_SHIFT);
            uint16_t *temp = new(std::nothrow) uint16_t[bufSize_];
            buffer_ = (char *) temp;
            bufSize_ = (bufSize_ << H_ALIGN_SHIFT);
            break;
        }
        case W_ALIGN_SHIFT:
        {
            bufSize_ = (bufSize_ >> W_ALIGN_SHIFT);
            uint32_t *temp = new(std::nothrow) uint32_t[bufSize_];
            buffer_ = (char *) temp;
            bufSize_ = (bufSize_ << W_ALIGN_SHIFT);
            break;
        }
        case D_ALIGN_SHIFT:
        {
            bufSize_ = (bufSize_ >> D_ALIGN_SHIFT);
            uint64_t *temp = new(std::nothrow) uint64_t[bufSize_];
            buffer_ = (char *) temp;
            bufSize_ = (bufSize_ << D_ALIGN_SHIFT);
            break;
        }
    }
}

ssize_t RingBuffer::Read(const int fd, const std::size_t len)
{
    if (fd < 0) {
        return -1;
    }
    if (len == 0) {
        return 0;
    }
    constexpr std::size_t numDests {2};
    struct iovec destBufs[numDests];
    // resize if free space is not big enough
    std::lock_guard<std::mutex> lk {mtx_};
    while (len >= FreeSize()) {
        // the equal sign makes sure the buffer will not be fully filled
        if (Resize() != 0) {
            return -1;
        }
    }
    // now we have enough free space to read in from fd
    destBufs[0].iov_base = buffer_ + tail_;
    if (tail_ + len < bufSize_) {
        // continuous free space
        destBufs[0].iov_len = len;
        destBufs[1].iov_base = buffer_ + tail_ + len;
        destBufs[1].iov_len = 0;
    } else {
        // free space splitted
        destBufs[0].iov_len = bufSize_ - tail_;
        destBufs[1].iov_base = buffer_;
        destBufs[1].iov_len = len + tail_ - bufSize_;
    }
    ssize_t ret = readv(fd, destBufs, numDests);
    if (ret != -1) {
        // update buffer status
        tail_ += ret;
        while (tail_ >= bufSize_) {
            tail_ -= bufSize_;
        }
    }
    return ret;
}

ssize_t RingBuffer::Write(const int fd, std::size_t len)
{
    if (fd < 0) {
        return -1;
    }
    constexpr std::size_t numSrcs {2};
    struct iovec srcBufs[numSrcs];
    std::lock_guard<std::mutex> lk {mtx_};
    std::size_t dataSize = DataSize();
    if (dataSize < len) {
        len = dataSize;
    }
    if (len == 0) {
        return 0;
    }
    // now we are sure there is at least 'len' bytes data in the buffer
    srcBufs[0].iov_base = buffer_ + head_;
    if (head_ + len > bufSize_) {
        // data splitted
        srcBufs[0].iov_len = bufSize_ - head_;
        srcBufs[1].iov_base = buffer_;
        srcBufs[1].iov_len = len + head_- bufSize_;
    } else {
        // continuous data
        srcBufs[0].iov_len = len;
        srcBufs[1].iov_base = buffer_ + head_ + len;
        srcBufs[1].iov_len = 0;
    }
    ssize_t ret = writev(fd, srcBufs, numSrcs);
    if (ret != -1) {
        // update buffer status
        head_ += ret;
        while (head_ >= bufSize_) {
            head_ -= bufSize_;
        }
    }
    return ret;
}

std::size_t RingBuffer::GetLine(char* dest, char eol)
{
    if (dest == nullptr) {
        return 0;
    }
    std::size_t ret {0};
    std::lock_guard<std::mutex> lk {mtx_};
    auto dataSize = DataSize();
    if (dataSize == 0) {
        // empty buffer
        return ret;
    }
    // look up end of line character
    std::size_t start {head_};
    const std::size_t end {dataSize + head_};
    for (; start < end; ++start) {
        std::size_t index = start;
        while (index >= bufSize_) {
            index -= bufSize_;
        }
        if (buffer_[index] == eol) {
            break;
        }
    }
    if (start < end) {
        // end of line found
        ret = start - head_;
        // update buffer status
        head_ = start + 1;
        while (head_ >= bufSize_) {
            head_ -= bufSize_;
        }
        // copy line of data to destination
        if (start > bufSize_) {
            // data splitted
            memcpy_s(dest, bufSize_ - head_, buffer_ + head_, bufSize_ - head_);
            memcpy_s(dest + bufSize_ - head_, start - bufSize_, buffer_, start - bufSize_);
        } else {
            memcpy_s(dest, start - head_, buffer_ + head_, start - head_);
        }
    }
    // end of line not found
    return ret;
}

std::size_t RingBuffer::GetLine(char* dest, const std::string &eol)
{
    if (dest == nullptr) {
        return 0;
    }
    std::size_t ret {0};
    std::lock_guard<std::mutex> lk {mtx_};
    auto dataSize = DataSize();
    if (dataSize == 0) {
        // empty buffer
        return ret;
    }
    // look up end of line character
    std::size_t start {head_};
    const std::size_t end {dataSize + head_ + 1 - eol.length()};
    for (; start != end; ++start) {
        std::size_t index = start;
        while (index >= bufSize_) {
            index -= bufSize_;
        }
        if (eol.compare(std::string(buffer_ + index, eol.length())) == 0) {
            break;
        }
    }
    if (start != end) {
        // end of line found
        ret = start - head_;
        // update buffer status
        head_ = start + eol.length();
        while (head_ >= bufSize_) {
            head_ -= bufSize_;
        }
        // copy line of data to destination
        if (start > bufSize_) {
            // data splitted
            memcpy_s(dest, bufSize_ - head_, buffer_ + head_, bufSize_ - head_);
            memcpy_s(dest + bufSize_ - head_, start - bufSize_, buffer_, start - bufSize_);
        } else {
            memcpy_s(dest, start - head_, buffer_ + head_, start - head_);
        }
    }
    // end of line not found
    return ret;
}

std::string RingBuffer::GetLine(char eol)
{
    std::string ret {};
    std::lock_guard<std::mutex> lk {mtx_};
    auto dataSize = DataSize();
    if (dataSize == 0) {
        // empty buffer
        return ret;
    }
    // look up end of line character
    std::size_t start {head_};
    const std::size_t end {dataSize + head_};
    for (; start != end; ++start) {
        std::size_t index = start;
        if (index >= bufSize_) {
            index -= bufSize_;
        }
        if (buffer_[index] == eol) {
            break;
        }
    }
    if (start != end) {
        // end of line found
        // update buffer status
        head_ = start + 1;
        while (head_ >= bufSize_) {
            head_ -= bufSize_;
        }
        // copy line of data to destination
        if (start > bufSize_) {
            // data splitted
            ret = std::string(buffer_ + head_, bufSize_ - head_);
            ret += std::string(buffer_, start - bufSize_);
        } else {
            ret = std::string(buffer_ + head_, start - head_);
        }
    }
    // end of line not found
    return ret;
}

std::string RingBuffer::GetLine(const std::string &eol)
{
    std::string ret {};
    std::lock_guard<std::mutex> lk {mtx_};
    auto dataSize = DataSize();
    if (dataSize == 0) {
        // empty buffer
        return ret;
    }
    // look up end of line character
    std::size_t start {head_};
    const std::size_t end {dataSize + head_ + 1 - eol.length()};
    for (; start != end; ++start) {
        std::size_t index = start;
        if (index >= bufSize_) {
            index -= bufSize_;
        }
        if (eol.compare(std::string(buffer_ + index, eol.length())) == 0) {
            break;
        }
    }
    if (start != end) {
        // end of line found
        // update buffer status
        head_ = start + eol.length();
        while (head_ >= bufSize_) {
            head_ -= bufSize_;
        }
        // copy line of data to destination
        if (start > bufSize_) {
            // data splitted
            ret = std::string(buffer_ + head_, bufSize_ - head_);
            ret += std::string(buffer_, start - bufSize_);
        } else {
            ret = std::string(buffer_ + head_, start - head_);
        }
    }
    // end of line not found
    return ret;
}

std::size_t RingBuffer::Get(char* dest, const std::size_t len)
{
    if (dest == nullptr) {
        return 0;
    }
    if (len == 0) {
        return 0;
    }
    std::lock_guard<std::mutex> lk {mtx_};
    auto dataSize = DataSize();
    if (len > dataSize) {
        return 0;
    }
    if (head_ + len > bufSize_) {
        // data splitted
        memcpy_s(dest, bufSize_ - head_, buffer_ + head_, bufSize_ - head_);
        memcpy_s(dest + bufSize_ - head_, len + head_ - bufSize_, buffer_, len + head_ - bufSize_);
    } else {
        memcpy_s(dest, len, buffer_ + head_, len);
    }
    // update buffer status
    head_ += len;
    while (head_ >= bufSize_) {
        head_ -= bufSize_;
    }
    return len;
}

int RingBuffer::Put(const char* str, const std::size_t len)
{
    if (str == nullptr) {
        return -1;
    }
    if (len == 0) {
        return 0;
    }
    // resize if free space is not big enough
    std::lock_guard<std::mutex> lk {mtx_};
    while (len >= FreeSize()) {
        // the equal sign makes sure the buffer will not be fully filled
        if (Resize() != 0) {
            return -1;
        }
    }
    if (tail_ + len < bufSize_) {
        // continuous free space
        memcpy_s(buffer_ + tail_, len, str, len);
    } else {
        // splitted free space
        memcpy_s(buffer_ + tail_, bufSize_ - tail_, str, bufSize_ - tail_);
        memcpy_s(buffer_, len + tail_ - bufSize_, str + bufSize_ - tail_, len + tail_ - bufSize_);
    }
    // update buffer status
    tail_ += len;
    while (tail_ >= bufSize_) {
        tail_ -= bufSize_;
    }
    return len;
}

int RingBuffer::Put(const std::string& str)
{
    if (str.empty()) {
        return -1;
    }
    int len = str.length();
    if (len == 0) {
        return 0;
    }
    // resize if free space is not big enough
    std::lock_guard<std::mutex> lk {mtx_};
    while (len >= FreeSize()) {
        // the equal sign makes sure the buffer will not be fully filled
        if (Resize() != 0) {
            return -1;
        }
    }
    if (tail_ + len < bufSize_) {
        // continuous free space
        memcpy_s(buffer_ + tail_, len, str.c_str(), len);
    } else {
        // splitted free space
        memcpy_s(buffer_ + tail_, bufSize_ - tail_, str.c_str(), bufSize_ - tail_);
        memcpy_s(buffer_, len + tail_ - bufSize_, str.c_str() + bufSize_ - tail_, len + tail_ - bufSize_);
    }
    // update buffer status
    tail_ += len;
    while (tail_ >= bufSize_) {
        tail_ -= bufSize_;
    }
    return len;
}

char* RingBuffer::Allocate(std::size_t bufSize)
{
    char *newBuffer {nullptr};
    switch (alignShift_) {
        case B_ALIGN_SHIFT:
        {
            bufSize = (bufSize >> B_ALIGN_SHIFT);
            newBuffer = new(std::nothrow) char[bufSize];
            break;
        }
        case H_ALIGN_SHIFT:
        {
            bufSize = (bufSize >> H_ALIGN_SHIFT);
            uint16_t *temp = new(std::nothrow) uint16_t[bufSize];
            newBuffer = (char *) temp;
            break;
        }
        case W_ALIGN_SHIFT:
        {
            bufSize = (bufSize >> W_ALIGN_SHIFT);
            uint32_t *temp = new(std::nothrow) uint32_t[bufSize];
            newBuffer = (char *) temp;
            break;
        }
        case D_ALIGN_SHIFT:
        {
            bufSize = (bufSize >> D_ALIGN_SHIFT);
            uint64_t *temp = new(std::nothrow) uint64_t[bufSize];
            newBuffer = (char *) temp;
            break;
        }
    }
    return newBuffer;
}

int RingBuffer::Resize()
{
    std::size_t expandedSize {bufSize_ << 1};
    char* newBuf = Allocate(expandedSize);
    if (newBuf == nullptr) {
        return -1;
    }
    //copy data to the new buffer
    auto dataSize = DataSize();
    if (head_ + dataSize > bufSize_) {
        // data splitted
        memcpy_s(newBuf, bufSize_ - head_, buffer_ + head_, bufSize_ - head_);
        memcpy_s(newBuf + bufSize_ - head_, dataSize + head_ - bufSize_, buffer_, dataSize + head_ - bufSize_);
    } else {
        // continuous data
        memcpy_s(newBuf, dataSize, buffer_ + head_, dataSize);
    }
    // update buffer status
    delete[] buffer_;
    buffer_ = newBuf;
    bufSize_ = expandedSize;
    head_ = 0;
    tail_ = dataSize;

    return 0;
}
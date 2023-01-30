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
#include "trace_file_helper.h"

#include <climits>
#include <cstring>
#if defined(is_mingw) && is_mingw
#include <openssl/sha.h>
#endif
#include <securec.h>
#include "logging.h"

TraceFileHelper::TraceFileHelper()
#if defined(is_mingw) && is_mingw
    : shaCtx_(std::make_shared<SHA256_CTX>())
#endif
{
#if defined(is_mingw) && is_mingw
    SHA256_Init(shaCtx_.get());
#endif
}

TraceFileHelper::~TraceFileHelper()
{
}

bool TraceFileHelper::AddSegment(const uint8_t data[], uint32_t size)
{
    if (size > std::numeric_limits<decltype(header_.data_.length)>::max() - header_.data_.length - sizeof(size)) {
        return false;
    }
#if defined(is_mingw) && is_mingw
    int retval = 0;
    header_.data_.segments += 1;

    header_.data_.length += size;
    retval = SHA256_Update(shaCtx_.get(), data, size);
    CHECK_TRUE(retval, false, "[%u] SHA256_Update FAILED, s:%u, d:%p!", header_.data_.segments, size, data);
#endif
    return true;
}

bool TraceFileHelper::Finish()
{
#if defined(is_mingw) && is_mingw
    int retval = 0;
    retval = SHA256_Final(header_.data_.sha256, shaCtx_.get());
    CHECK_TRUE(retval, false, "[%u] SHA256_Final FAILED!", header_.data_.segments);
#endif
    return true;
}

bool TraceFileHelper::Update(TraceFileHeader& header)
{
#if defined(is_mingw) && is_mingw
    CHECK_TRUE(Finish(), false, "Finish FAILED!");
    if (memcpy_s(&header, sizeof(header), &header_, sizeof(header)) != 0) {
        return false;
    }
#endif
    return true;
}

bool TraceFileHelper::Validate(const TraceFileHeader& header)
{
#if defined(is_mingw) && is_mingw
    CHECK_TRUE(Finish(), false, "Finish FAILED!");
    return memcmp(&header_, &header, sizeof(header_)) == 0;
#endif
    return true;
}

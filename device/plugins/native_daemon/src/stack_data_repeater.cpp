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
#include "stack_data_repeater.h"

using namespace OHOS::Developtools::NativeDaemon;

StackDataRepeater::StackDataRepeater(size_t maxSize)
{
    maxSize_ = maxSize;
    closed_ = false;
}

StackDataRepeater::~StackDataRepeater()
{
    Close();
}

size_t StackDataRepeater::Size()
{
    std::unique_lock<std::mutex> lock(mutex_);
    return rawDataQueue_.size();
}

void StackDataRepeater::Reset()
{
    std::unique_lock<std::mutex> lock(mutex_);
    closed_ = false;
}

void StackDataRepeater::Close()
{
    {
        std::unique_lock<std::mutex> lock(mutex_);
        rawDataQueue_.clear();
        closed_ = true;
    }
    slotCondVar_.notify_all();
    itemCondVar_.notify_all();
}

bool StackDataRepeater::PutRawStack(const RawStackPtr& rawData)
{
    std::unique_lock<std::mutex> lock(mutex_);
    while (rawDataQueue_.size() >= maxSize_ && !closed_) {
        slotCondVar_.wait(lock);
    }
    if (closed_) {
        return false;
    }

    rawDataQueue_.push_back(rawData);
    lock.unlock();

    itemCondVar_.notify_one();
    return true;
}

RawStackPtr StackDataRepeater::TakeRawData(uint32_t during, uint32_t max_size)
{
    std::unique_lock<std::mutex> lock(mutex_);
    while (rawDataQueue_.empty() && !closed_) {
        itemCondVar_.wait(lock);
    }
    if (closed_) {
        return nullptr;
    }
    auto result = rawDataQueue_.front();
    rawDataQueue_.pop_front();
    uint32_t size = (max_size > rawDataQueue_.size()) ? rawDataQueue_.size() : max_size;
    if ((result != nullptr) && (result->stackConext.type == MALLOC_MSG) && (size > 0)) {
        for (unsigned i = 0; i < size; i++) {
            auto it = rawDataQueue_.at(i);
            if (it == nullptr) {
                break;
            }
            uint64_t diff = (it->stackConext.ts.tv_nsec - result->stackConext.ts.tv_nsec) / 1000000
                + (it->stackConext.ts.tv_sec - result->stackConext.ts.tv_sec) * 1000;

            if (diff > during) {
                break;
            }
            if ((result->stackConext.addr == it->stackConext.addr) && (it->stackConext.type == FREE_MSG)) {
                result->reportFlag = false;
                it->reportFlag = false;
                break;
            }
        }
    }

    lock.unlock();
    slotCondVar_.notify_one();
    return result;
}
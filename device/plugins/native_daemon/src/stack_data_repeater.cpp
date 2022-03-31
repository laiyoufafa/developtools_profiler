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
    return dataQueue_.size();
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
        dataQueue_.clear();
        closed_ = true;
    }
    slotCondVar_.notify_all();
    itemCondVar_.notify_all();
}

bool StackDataRepeater::PutStackData(const BatchNativeHookDataPtr& pluginData)
{
    std::unique_lock<std::mutex> lock(mutex_);
    while (dataQueue_.size() >= maxSize_ && !closed_) {
        slotCondVar_.wait(lock);
    }
    if (closed_) {
        return false;
    }

    dataQueue_.push_back(pluginData);
    lock.unlock();

    itemCondVar_.notify_one();
    return true;
}

BatchNativeHookDataPtr StackDataRepeater::TakeStackData()
{
    std::unique_lock<std::mutex> lock(mutex_);
    while (dataQueue_.empty() && !closed_) {
        itemCondVar_.wait(lock);
    }
    if (closed_) {
        return nullptr;
    }

    auto result = dataQueue_.front();
    dataQueue_.pop_front();
    lock.unlock();

    slotCondVar_.notify_one();
    return result;
}

int StackDataRepeater::TakeStackData(std::vector<BatchNativeHookDataPtr>& dataVec)
{
    std::unique_lock<std::mutex> lock(mutex_);
    while (dataQueue_.empty() && !closed_) {
        itemCondVar_.wait(lock);
    }
    if (closed_) {
        return -1;
    }

    int count = 0;
    while (dataQueue_.size() > 0) {
        auto result = dataQueue_.front();
        dataVec.push_back(result);
        dataQueue_.pop_front();
        count++;
    }
    lock.unlock();

    slotCondVar_.notify_one();
    return count;
}

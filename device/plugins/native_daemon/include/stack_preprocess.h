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
#ifndef STACK_PREPROCESS_H
#define STACK_PREPROCESS_H

#include <chrono>
#include <thread>

#include "logging.h"
#include "nocopyable.h"
#include "stack_data_repeater.h"
#include "buffer_writer.h"

class StackPreprocess {
public:
    explicit StackPreprocess(const StackDataRepeaterPtr& dataRepeater);

    ~StackPreprocess();

    void SetWriter(const std::shared_ptr<BufferWriter>& writer);

    bool StartTakeResults();

    bool StopTakeResults();

private:
    void TakeResults();

private:
    std::shared_ptr<BufferWriter> writer_ = nullptr;
    StackDataRepeaterPtr dataRepeater_ = nullptr;
    std::thread thread_ {};
    std::unique_ptr<uint8_t[]> buffer_;
    bool isStopTakeData_ = false;
    DISALLOW_COPY_AND_MOVE(StackPreprocess);
};

#endif // STACK_PREPROCESS_H
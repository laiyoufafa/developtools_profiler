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
#include "stack_preprocess.h"

#include <unistd.h>

#include "logging.h"
#include "plugin_service_types.pb.h"

constexpr uint32_t MAX_BUFFER_SIZE = 10 * 1024;

StackPreprocess::StackPreprocess(const StackDataRepeaterPtr& dataRepeater)
    : dataRepeater_(dataRepeater),  buffer_(new (std::nothrow) uint8_t[MAX_BUFFER_SIZE])
{
}

StackPreprocess::~StackPreprocess()
{
    isStopTakeData_= true;
    if (dataRepeater_) {
        dataRepeater_->Close();
    }
    if (thread_.joinable()) {
        thread_.join();
    }
}

void StackPreprocess::SetWriter(const std::shared_ptr<BufferWriter>& writer)
{
    writer_ = writer;
}

bool StackPreprocess::StartTakeResults()
{
    CHECK_NOTNULL(dataRepeater_, false, "data repeater null");

    std::thread demuxer(&StackPreprocess::TakeResults, this);
    CHECK_TRUE(demuxer.get_id() != std::thread::id(), false, "demuxer thread invalid");

    thread_ = std::move(demuxer);
    isStopTakeData_ = false;
    return true;
}

bool StackPreprocess::StopTakeResults()
{
    CHECK_NOTNULL(dataRepeater_, false, "data repeater null");
    CHECK_TRUE(thread_.get_id() != std::thread::id(), false, "thread invalid");

    isStopTakeData_= true;
    dataRepeater_->PutStackData(nullptr);
    if (thread_.joinable()) {
        thread_.join();
    }
    return true;
}

void StackPreprocess::TakeResults()
{
    if (!dataRepeater_) {
        return;
    }

    HILOG_INFO(LOG_CORE, "TakeResults thread %d, start!", gettid());
    while (1) {
        auto stackData = dataRepeater_->TakeStackData();
        if (!stackData || isStopTakeData_) {
            break;
        }

        size_t length = stackData->ByteSizeLong();
        if (length < MAX_BUFFER_SIZE) {
            stackData->SerializeToArray(buffer_.get(), length);
            ProfilerPluginData pluginData;
            pluginData.set_name("nativehook");
            pluginData.set_status(0);
            pluginData.set_data(buffer_.get(), length);

            struct timespec ts;
            clock_gettime(CLOCK_REALTIME, &ts);

            pluginData.set_clock_id(ProfilerPluginData::CLOCKID_REALTIME);
            pluginData.set_tv_sec(ts.tv_sec);
            pluginData.set_tv_nsec(ts.tv_nsec);

            writer_->WriteMessage(pluginData);
            writer_->Flush();
        }
    }
    HILOG_INFO(LOG_CORE, "TakeResults thread %d, exit!", gettid());
}

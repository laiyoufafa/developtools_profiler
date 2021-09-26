/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "stream_plugin.h"
#include "securec.h"

#include <sys/syscall.h>
#include <sys/types.h>
#include <unistd.h>

namespace {
constexpr int INTERVAL_TIME_BASE = 10000;  // 间隔时间base
constexpr int INTERVAL_TIME_MAX = 9;       // 最大间隔
constexpr int INTERVAL_TIME_STEP = 2;       // 间隔步长
constexpr int BYTE_BUFFER_SIZE = 128;
constexpr int MS_PER_S = 1000;
constexpr int NS_PER_MS = 1000000;
} // namespace

StreamPlugin::StreamPlugin() {}

StreamPlugin::~StreamPlugin() {}

int StreamPlugin::Start(const uint8_t* configData, uint32_t configSize)
{
    // 反序列化
    if (protoConfig_.ParseFromArray(configData, configSize) <= 0) {
        HILOG_ERROR(LOG_CORE, "StreamPlugin: ParseFromArray failed");
        return -1;
    }
    // 启动线程写数据
    std::unique_lock<std::mutex> locker(mutex_);
    running_ = true;
    writeThread_ = std::thread(&StreamPlugin::Loop, this);

    return 0;
}

int StreamPlugin::Stop()
{
    std::unique_lock<std::mutex> locker(mutex_);
    running_ = false;
    locker.unlock();
    if (writeThread_.joinable()) {
        writeThread_.join();
    }
    HILOG_INFO(LOG_CORE, "StreamPlugin: stop success!");
    return 0;
}

int StreamPlugin::SetWriter(WriterStruct* writer)
{
    resultWriter_ = writer;
    return 0;
}

uint64_t StreamPlugin::GetTimeMS()
{
    struct timespec ts;
    clock_gettime(CLOCK_BOOTTIME, &ts);
    return ts.tv_sec * MS_PER_S + ts.tv_nsec / NS_PER_MS;
}

void StreamPlugin::Loop(void)
{
    HILOG_INFO(LOG_CORE, "StreamPlugin thread %{public}d start !!!!!", gettid());
    uint32_t i = 1;
    while (running_) {
        StreamData dataProto;
        uint64_t tm = GetTimeMS();
        dataProto.set_time_ms(tm);

        // 序列化
        buffer_.resize(dataProto.ByteSizeLong());
        dataProto.SerializeToArray(buffer_.data(), buffer_.size());

        usleep(i * INTERVAL_TIME_BASE); // 间隔时间不固定

        if (i < INTERVAL_TIME_MAX) {
            i += INTERVAL_TIME_STEP;
        } else {
            i = 1;
        }

        if (resultWriter_->write != nullptr) {
            resultWriter_->write(resultWriter_, buffer_.data(), buffer_.size());
        }

        nbyte_ += buffer_.size();
        if (nbyte_ >= BYTE_BUFFER_SIZE) {
            resultWriter_->flush(resultWriter_);
            nbyte_ = 0;
        }
    }
    resultWriter_->flush(resultWriter_);
    HILOG_INFO(LOG_CORE, "Transporter thread %{public}d exit !!!!!", gettid());
}

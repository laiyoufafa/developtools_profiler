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
#include "result_demuxer.h"

#include <unistd.h>
#include "logging.h"

#define CHECK_POINTER_NOTNULL(ptr)                        \
    if (ptr == nullptr) {                                 \
        HILOG_WARN(LOG_CORE, "%s: FAILED, %s is null!", __func__, #ptr); \
        return false;                                     \
    }

#define CHECK_THREAD_ID_VALID(t)                           \
    if (t.get_id() == std::thread::id()) {                 \
        HILOG_WARN(LOG_CORE, "%s: FAILED, %s id invalid!", __func__, #t); \
        return false;                                      \
    }

ResultDemuxer::ResultDemuxer(const ProfilerDataRepeaterPtr& dataRepeater)
{
    dataRepeater_ = dataRepeater;
}

ResultDemuxer::~ResultDemuxer()
{
    if (dataRepeater_) {
        dataRepeater_->Close();
    }
    if (demuxerThread_.joinable()) {
        demuxerThread_.join();
    }
}

void ResultDemuxer::SetTraceWriter(const TraceFileWriterPtr& traceWriter)
{
    traceWriter_ = traceWriter;
}

bool ResultDemuxer::StartTakeResults()
{
    CHECK_POINTER_NOTNULL(dataRepeater_);

    std::thread demuxer(&ResultDemuxer::TakeResults, this);
    CHECK_THREAD_ID_VALID(demuxer);

    demuxerThread_ = std::move(demuxer);
    return true;
}

bool ResultDemuxer::StopTakeResults()
{
    CHECK_POINTER_NOTNULL(dataRepeater_);
    CHECK_THREAD_ID_VALID(demuxerThread_);

    dataRepeater_->PutPluginData(nullptr);
    if (demuxerThread_.joinable()) {
        demuxerThread_.join();
    }
    return true;
}

void ResultDemuxer::TakeResults()
{
    if (!dataRepeater_) {
        return;
    }

    HILOG_INFO(LOG_CORE, "TakeResults thread %d, start!", gettid());
    while (1) {
        auto pluginData = dataRepeater_->TakePluginData();
        if (!pluginData) {
            break;
        }

        if (traceWriter_) {
            traceWriter_->Write(*pluginData);
        } else {
            HILOG_WARN(LOG_CORE, "no writer, drop data!");
        }
    }
    HILOG_INFO(LOG_CORE, "TakeResults thread %d, exit!", gettid());
}

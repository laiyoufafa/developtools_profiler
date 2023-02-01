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

#ifndef BPF_LOG_READER_H
#define BPF_LOG_READER_H

#include <string>
#include <thread>
#include <memory>

#include "bpf_log.h"

class BPFLogReader {
public:
    ~BPFLogReader();
    static std::unique_ptr<BPFLogReader> MakeUnique(const std::string& logFile = "/data/local/tmp/bpf_log.txt");
    inline int Start()
    {
#if defined(BPF_LOGGER_DEBUG) || defined(BPF_LOGGER_INFO) || defined(BPF_LOGGER_WARN) ||  \
    defined(BPF_LOGGER_ERROR) || defined(BPF_LOGGER_FATAL)
        worker_ = std::thread(&BPFLogReader::MoveBPFLog, this);
#endif
        return 0;
    }
    inline int Stop()
    {
        stop_ = true;
        return 0;
    }

private:
    BPFLogReader() = default;
    int OpenTracePipe();
    int EnableTracePipe() const;
    std::string GetLogFileName() const;
    int OpenLogFile(const std::string& logFile);
    int MoveBPFLog();

    const std::string confPath_ {"/sys/kernel/debug/tracing/tracing_on"};
    const std::string pipePath_ {"/sys/kernel/debug/tracing/trace_pipe"};

    bool stop_ {false};
    int ifd_ {-1};
    int ofd_ {-1};
    std::thread worker_;
};
#endif  // BPF_LOG_READER_H
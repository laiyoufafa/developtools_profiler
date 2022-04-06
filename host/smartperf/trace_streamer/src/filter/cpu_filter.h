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

#ifndef CPU_FILTER_H
#define CPU_FILTER_H

#include <cstdint>
#include <limits>
#include <map>
#include <string_view>
#include <tuple>

#include "filter_base.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"
#include "ts_common.h"

namespace SysTuning {
namespace TraceStreamer {
class TraceStreamerFilters;
class CpuFilter : private FilterBase {
public:
    CpuFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter);
    CpuFilter(const CpuFilter&) = delete;
    CpuFilter& operator=(const CpuFilter&) = delete;
    ~CpuFilter() override;

public:
    void InsertSwitchEvent(uint64_t ts,
                           uint64_t cpu,
                           uint64_t prevPid,
                           uint64_t prevPior,
                           uint64_t prevState,
                           uint64_t nextPid,
                           uint64_t nextPior);
    void InsertWakeupEvent(uint64_t ts, uint64_t internalTid);
    bool InsertProcessExitEvent(uint64_t ts, uint64_t cpu, uint64_t pid);
    bool InsertProcessFreeEvent(uint64_t ts, uint64_t pid);
    void FinishCpuEvent();

private:
    void MaybeDealEvent();

    class TSSwitchEvent {
    public:
        TSSwitchEvent(uint64_t ts,
                      uint64_t cpu,
                      uint64_t prevPid,
                      uint64_t prevPior,
                      uint64_t prevState,
                      uint64_t nextPid,
                      uint64_t nextPior)
            : ts_(ts),
              cpu_(cpu),
              prevPid_(prevPid),
              prevPior_(prevPior),
              prevState_(prevState),
              nextPid_(nextPid),
              nextPior_(nextPior)

        {
        }
        ~TSSwitchEvent() {}
        uint64_t ts_;
        uint64_t cpu_;
        uint64_t prevPid_;
        uint64_t prevPior_;
        uint64_t prevState_;
        uint64_t nextPid_;
        uint64_t nextPior_;
    };
    class TSWakeupEvent {
    public:
        TSWakeupEvent(uint64_t ts, uint64_t pid) : ts_(ts), pid_(pid) {}
        ~TSWakeupEvent() {}
        uint64_t ts_;
        uint64_t pid_;
    };
    class TSProcessExitEvent {
    public:
        TSProcessExitEvent(uint64_t ts, uint64_t cpu, uint64_t pid) : ts_(ts), cpu_(cpu), pid_(pid) {}
        ~TSProcessExitEvent() {}
        uint64_t ts_;
        uint64_t cpu_;
        uint64_t pid_;
    };
    enum TSCpuEventType {
        TS_EVENT_THREAD_SWITCH,
        TS_EVENT_THREAD_WAKING,
        TS_EVENT_PROCESS_EXIT,
        TS_EVENT_PROCESS_FREE
    };
    class TSCpuEvent {
    public:
        TSCpuEvent() {}
        ~TSCpuEvent() {}
        TSCpuEventType type_;
        // us union below will be a good choice
        // but union with unique_ptr can bring about runtime error on windows and mac,only work well on linux
        std::unique_ptr<TSSwitchEvent> switchEvent_ = {};
        std::unique_ptr<TSWakeupEvent> wakeupEvent_ = {};
        std::unique_ptr<TSProcessExitEvent> processExitEvent_ = {};
    };
    void DealEvent(const TSCpuEvent* event);
    void ExecInsertSwitchEvent(uint64_t ts,
                               uint64_t cpu,
                               uint64_t prevPid,
                               uint64_t prevPior,
                               uint64_t prevState,
                               uint64_t nextPid,
                               uint64_t nextPior);
    void ExecInsertWakeupEvent(uint64_t ts, uint64_t internalTid);
    bool ExecInsertProcessExitEvent(uint64_t ts, uint64_t cpu, uint64_t pid);
    void CheckWakeupEvent(uint64_t internalTid);
    uint64_t RemberInternalTidInStateTable(uint64_t uid, uint64_t row, uint64_t state = TASK_INVALID);
    uint64_t RowOfInternalTidInStateTable(uint64_t uid) const;
    uint64_t StateOfInternalTidInStateTable(uint64_t uid) const;
    std::multimap<uint64_t, std::unique_ptr<TSCpuEvent>> tsCpuEventQueue_;
    std::map<uint64_t, uint64_t> cpuToRowThreadState_ = {};
    std::map<uint64_t, uint64_t> cpuToRowSched_ = {};
    std::map<uint64_t, uint64_t> lastWakeUpMsg = {};

    struct TPthread {
        uint64_t row_;
        uint64_t state_;
    };
    std::map<uint64_t, TPthread> internalTidToRowThreadState_ = {};
    // timestamp of ftrace events from different cpu can be outof order
    // keep a cache of ftrace events in memory and keep msg in order
    // the value below is the count of msg, maybe you can change it
    const size_t MAX_CACHE_SIZE = 10000;
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // CPU_FILTER_H

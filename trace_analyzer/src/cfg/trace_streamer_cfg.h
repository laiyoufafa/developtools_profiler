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

#ifndef TRACE_STREAMER_CFG_H
#define TRACE_STREAMER_CFG_H
#include <map>
#include <string>
namespace SysTuning {
namespace TraceCfg {
// all supported events should be defined here
enum SupportedTraceEventType {
    TRACE_EVENT_START = 0,
    TRACE_EVENT_BINDER_TRANSACTION = TRACE_EVENT_START,
    TRACE_EVENT_BINDER_TRANSACTION_RECEIVED,
    TRACE_EVENT_SCHED_SWITCH,
    TRACE_EVENT_TASK_RENAME,
    TRACE_EVENT_TASK_NEWTASK,
    TRACE_EVENT_TRACING_MARK_WRITE,
    TRACE_EVENT_SCHED_WAKEUP,
    TRACE_EVENT_SCHED_WAKING,
    TRACE_EVENT_CPU_IDLE,
    TRACE_EVENT_CPU_FREQUENCY,
    TRACE_EVENT_WORKQUEUE_EXECUTE_START,
    TRACE_EVENT_WORKQUEUE_EXECUTE_END,
    TRACE_EVENT_CLOCK_SET_RATE,
    TRACE_EVENT_CLOCK_ENABLE,
    TRACE_EVENT_CLOCK_DISABLE,
    TRACE_EVENT_REGULATOR_SET_VOLTAGE,
    TRACE_EVENT_REGULATOR_SET_VOLTAGE_COMPLETE,
    TRACE_EVENT_REGULATOR_DISABLE,
    TRACE_EVENT_REGULATOR_DISABLE_COMPLETE,
    TRACE_EVENT_IPI_ENTRY,
    TRACE_EVENT_IPI_EXIT,
    TRACE_EVENT_IRQ_HANDLER_ENTRY,
    TRACE_EVENT_IRQ_HANDLER_EXIT,
    TRACE_EVENT_SOFTIRQ_RAISE,
    TRACE_EVENT_SOFTIRQ_ENTRY,
    TRACE_EVENT_SOFTIRQ_EXIT,
    TRACE_EVENT_BINDER_TRANSACTION_ALLOC_BUF,
    TRACE_EVENT_SCHED_WAKEUP_NEW,
    TRACE_EVENT_PROCESS_EXIT,
    TRACE_EVENT_CLOCK_SYNC,
    TRACE_MEMORY,
    TRACE_EVENT_OTHER,
    TRACE_EVENT_MAX
};
enum MemInfoType {
    MEM_VM_SIZE,
    MEM_VM_RSS,
    MEM_VM_ANON,
    MEM_RSS_FILE,
    MEM_RSS_SHMEM,
    MEM_VM_SWAP,
    MEM_VM_LOCKED,
    MEM_VM_HWM,
    MEM_OOM_SCORE_ADJ,
    MEM_MAX
};
enum StatType {
    STAT_EVENT_START = 0,
    STAT_EVENT_RECEIVED = STAT_EVENT_START,
    STAT_EVENT_DATA_LOST,
    STAT_EVENT_NOTMATCH,
    STAT_EVENT_NOTSUPPORTED,
    STAT_EVENT_DATA_INVALID,
    STAT_EVENT_MAX
};

// there maybe some error while parser trace msgs, here defined the error levels
enum StatSeverityLevel {
    STAT_SEVERITY_LEVEL_START = 0,
    STAT_SEVERITY_LEVEL_INFO = STAT_SEVERITY_LEVEL_START,
    STAT_SEVERITY_LEVEL_WARN,
    STAT_SEVERITY_LEVEL_ERROR,
    STAT_SEVERITY_LEVEL_FATAL,
    STAT_SEVERITY_LEVEL_MAX
};


// the supported metadata
enum MetaDataItem {
    METADATA_ITEM_START = 0,
    METADATA_ITEM_DATASIZE = METADATA_ITEM_START,
    METADATA_ITEM_PARSETOOL_NAME,
    METADATA_ITEM_PARSERTOOL_VERSION,
    METADATA_ITEM_PARSERTOOL_PUBLISH_DATETIME,
    METADATA_ITEM_SOURCE_FILENAME,
    METADATA_ITEM_OUTPUT_FILENAME,
    METADATA_ITEM_PARSERTIME,  // the data time while the data parsed
    METADATA_ITEM_SOURCE_DATETYPE,  // proto-based-trace or txt-based-trace
    METADATA_ITEM_MAX
};

class TraceStreamConfig {
public:
    TraceStreamConfig();
    ~TraceStreamConfig() = default;
public:
    std::map<SupportedTraceEventType, std::string> eventNameMap_{};
    std::map<StatType, std::string> eventErrorDescMap_{};
    std::map<StatSeverityLevel, std::string> serverityLevelDescMap_{};
    // different msg may have STAT_EVENT_MAX types of exception when parse, and they have different error level
    // if you think some error level should be improve or depress, you can edit this map
    std::map<SupportedTraceEventType, std::map<StatType, StatSeverityLevel>> eventParserStatSeverityDescMap_{};
    std::map<MemInfoType, std::string> memNameMap_{};
private:
    void InitSecurityMap();
    // all supported events should be defined here, these str can be find in text-based trace
    const std::string TRACE_ACTION_BINDER_TRANSACTION  = "binder_transaction";
    const std::string TRACE_ACTION_BINDER_TRANSACTION_RECEIVED = "binder_transaction_received";
    const std::string TRACE_ACTION_SCHED_SWITCH = "sched_switch";
    const std::string TRACE_ACTION_TASK_RENAME = "task_rename";
    const std::string TRACE_ACTION_TASK_NEWTASK = "task_newtask";
    const std::string TRACE_ACTION_TRACING_MARK_WRITE = "tracing_mark_write";
    const std::string TRACE_ACTION_SCHED_WAKEUP = "sched_wakeup";
    const std::string TRACE_ACTION_SCHED_WAKING = "sched_waking";
    const std::string TRACE_ACTION_CPU_IDLE = "cpu_idle";
    const std::string TRACE_ACTION_CPU_FREQUENCY = "cpu_frequency";
    const std::string TRACE_ACTION_WORKQUEUE_EXECUTE_START = "workqueue_execute_start";
    const std::string TRACE_ACTION_WORKQUEUE_EXECUTE_END = "workqueue_execute_end";

    const std::string TRACE_ACTION_CLOCK_SET_RATE = "clock_set_rate";
    const std::string TRACE_ACTION_CLOCK_ENABLE = "clock_enable";
    const std::string TRACE_ACTION_CLOCK_DISABLE = "clock_disable";
    const std::string TRACE_ACTION_REGULATOR_SET_VOLTAGE = "regulator_set_voltage";
    const std::string TRACE_ACTION_REGULATOR_SET_VOLTAGE_COMPLETE = "regulator_set_voltage_complete";
    const std::string TRACE_ACTION_REGULATOR_DISABLE = "regulator_disable";
    const std::string TRACE_ACTION_REGULATOR_DISABLE_COMPLETE = "regulator_disable_complete";
    const std::string TRACE_ACTION_IPI_ENTRY = "ipi_entry";
    const std::string TRACE_ACTION_IPI_EXIT = "ipi_exit";
    const std::string TRACE_ACTION_IRQ_HANDLER_ENTRY = "irq_handler_entry";
    const std::string TRACE_ACTION_IRQ_HANDLER_EXIT = "irq_handler_exit";
    const std::string TRACE_ACTION_SOFTIRQ_RAISE = "softirq_raise";
    const std::string TRACE_ACTION_SOFTIRQ_ENTRY = "softirq_entry";
    const std::string TRACE_ACTION_SOFTIRQ_EXIT = "softirq_exit";
    const std::string TRACE_ACTION_BINDER_TRANSACTION_ALLOC_BUF = "binder_transaction_alloc_buf";
    const std::string TRACE_ACTION_SCHED_WAKEUP_NEW = "sched_wakeup_new";
    const std::string TRACE_ACTION_PROCESS_EXIT = "sched_process_exit";
    const std::string TRACE_ACTION_CLOCK_SYNC = "trace_event_clock_sync";
    const std::string TRACE_ACTION_MEMORY = "memory";
    const std::string TRACE_ACTION_OTHER = "other";

    const std::string MEM_INFO_VM_SIZE_DESC = "mem.vm.size";
    const std::string MEM_INFO_LOCKED_DESC = "mem.locked";
    const std::string MEM_INFO_RSS_DESC = "mem.rss";
    const std::string MEM_INFO_RSS_ANON_DESC = "mem.rss.anon";
    const std::string MEM_INFO_RSS_FILE_DESC = "mem.rss.file";
    const std::string MEM_INFO_RSS_SCHEM_DESC = "mem.rss.schem";
    const std::string MEM_INFO_SWAP_DESC = "mem.swap";
    const std::string MEM_INFO_VIRT_DESC = "mem.virt";
    const std::string MEM_INFO_HWM_DESC = "mem.hwm";
    const std::string MEM_INFO_SCORE_ADJ_DESC = "oom_score_adj";

    const std::string TRACE_STAT_TYPE_RECEIVED_DESC = "received";
    const std::string TRACE_STAT_TYPE_LOST_DESC = "data_lost";
    const std::string TRACE_STAT_TYPE_NOTMATCH_DESC = "not_match";
    const std::string TRACE_STAT_TYPE_NOTSUPPORTED_DESC = "not_supported";
    const std::string TRACE_STAT_TYPE_DATA_INVALID_DESC = "invalid_data";

    const std::string STAT_SEVERITY_LEVEL_INFO_DESC = "info";
    const std::string STAT_SEVERITY_LEVEL_WARN_DESC = "warn";
    const std::string STAT_SEVERITY_LEVEL_ERROR_DESC = "error";
    const std::string STAT_SEVERITY_LEVEL_FATAL_DESC = "fatal";
};
} // namespace TraceCfg
} // namespace SysTuning
#endif

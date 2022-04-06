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

#include "thread_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { ID = 0, TYPE, TID, NAME, START_TS, END_TS, INTERNAL_PID, IS_MAIN_THREAD };
}
ThreadTable::ThreadTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("type", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("tid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("start_ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("end_ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ipid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("is_main_thread", "UNSIGNED INT"));
    tablePriKey_.push_back("id");
}

ThreadTable::~ThreadTable() {}

void ThreadTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

ThreadTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->ThreadSize()))
{
}

ThreadTable::Cursor::~Cursor() {}

int ThreadTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID: {
            sqlite3_result_int64(context_, CurrentRow());
            break;
        }
        case TYPE: {
            sqlite3_result_text(context_, "thread", strlen("thread"), nullptr);
            break;
        }
        case TID: {
            const auto& process = dataCache_->GetConstThreadData(CurrentRow());
            sqlite3_result_int64(context_, static_cast<int>(process.tid_));
            break;
        }
        case NAME: {
            const auto& thread = dataCache_->GetConstThreadData(CurrentRow());
            const auto& name = dataCache_->GetDataFromDict(thread.nameIndex_);
            sqlite3_result_text(context_, name.c_str(), static_cast<int>(name.length()), nullptr);
            break;
        }
        case START_TS: {
            const auto& thread = dataCache_->GetConstThreadData(CurrentRow());
            if (thread.startT_) {
                sqlite3_result_int64(context_, static_cast<int64_t>(thread.startT_));
            }
            break;
        }
        case END_TS: {
            const auto& thread = dataCache_->GetConstThreadData(CurrentRow());
            if (thread.endT_) {
                sqlite3_result_int64(context_, static_cast<int64_t>(thread.endT_));
            }
            break;
        }
        case INTERNAL_PID: {
            const auto& thread = dataCache_->GetConstThreadData(CurrentRow());
            if (thread.internalPid_) {
                sqlite3_result_int(context_, static_cast<int>(thread.internalPid_));
            }
            break;
        }
        case IS_MAIN_THREAD: {
            const auto& thread = dataCache_->GetConstThreadData(CurrentRow());
            // When it is not clear which process the thread belongs to, is_main_thread should be set to null
            if (!thread.internalPid_) {
                break;
            }
            const auto& process = dataCache_->GetConstProcessData(thread.internalPid_);
            sqlite3_result_int(context_, thread.tid_ == process.pid_);
            break;
        }
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

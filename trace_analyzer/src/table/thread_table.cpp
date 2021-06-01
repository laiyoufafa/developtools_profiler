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
enum Index { INTERNAL_TID = 0, INTERNAL_PID, NAME, TID };
}
ThreadTable::ThreadTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("utid", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("upid", "INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("tid", "INT"));
    tablePriKey_.push_back("utid");
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
    const auto& thread = dataCache_->GetConstThreadData(CurrentRow());
    switch (column) {
        case INTERNAL_TID: {
            sqlite3_result_int64(context_, CurrentRow());
            break;
        }
        case INTERNAL_PID: {
            sqlite3_result_int64(context_, thread.internalPid_);
            break;
        }
        case NAME: {
            const auto& name = dataCache_->GetDataFromDict(thread.nameIndex_);
            sqlite3_result_text(context_, name.c_str(), static_cast<int>(name.length()), nullptr);
            break;
        }
        case TID: {
            sqlite3_result_int64(context_, thread.tid_);
            break;
        }
        default: {
            TUNING_LOGF("Unregistered column : %d", column);
            break;
        }
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

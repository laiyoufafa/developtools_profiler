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

#include "log_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { SEQ = 0, TS, PID, TID, LEVEL, TAG, CONTEXT, ORIGINTS };
}
LogTable::LogTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("seq", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("pid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("tid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("level", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("tag", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("context", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("origints", "UNSIGNED BIG INT"));
    tablePriKey_.push_back("ts");
}

LogTable::~LogTable() {}

void LogTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

LogTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstHilogData().Size())),
      logInfoObj_(dataCache->GetConstHilogData())
{
}

LogTable::Cursor::~Cursor() {}

int LogTable::Cursor::Column(int column) const
{
    switch (column) {
        case SEQ:
            sqlite3_result_int64(context_, static_cast<int64_t>(logInfoObj_.HilogLineSeqs()[CurrentRow()]));
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(logInfoObj_.TimeStamData()[CurrentRow()]));
            break;
        case PID: {
            sqlite3_result_int64(context_, static_cast<int64_t>(logInfoObj_.Pids()[CurrentRow()]));
            break;
        }
        case TID:
            sqlite3_result_int64(context_, static_cast<int64_t>(logInfoObj_.Tids()[CurrentRow()]));
            break;
        case LEVEL: {
            if (logInfoObj_.Levels()[CurrentRow()] != INVALID_UINT64) {
                auto levelDataIndex = static_cast<size_t>(logInfoObj_.Levels()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(levelDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case TAG: {
            if (logInfoObj_.Tags()[CurrentRow()] != INVALID_UINT64) {
                auto tagDataIndex = static_cast<size_t>(logInfoObj_.Tags()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(tagDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case CONTEXT: {
            if (logInfoObj_.Contexts()[CurrentRow()] != INVALID_UINT64) {
                auto contextDataIndex = static_cast<size_t>(logInfoObj_.Contexts()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(contextDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case ORIGINTS: {
            sqlite3_result_int64(context_, static_cast<int64_t>(logInfoObj_.OriginTimeStamData()[CurrentRow()]));
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

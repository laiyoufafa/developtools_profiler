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

#include "callstack_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index {
    ID = 0,
    TS,
    DUR,
    CALL_ID,
    CAT,
    NAME,
    DEPTH,
    COOKIE_ID,
    PARENT_ID,
    ARGSET,
    CHAIN_ID,
    SPAN_ID,
    PARENT_SPAN_ID,
    FLAG,
    ARGS
};
}
CallStackTable::CallStackTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("id", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ts", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("dur", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("callid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cat", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("depth", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("cookie", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("parent_id", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("argsetid", "UNSIGNED INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("chainId", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("spanId", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("parentSpanId", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("flag", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("args", "STRING"));
    tablePriKey_.push_back("callid");
    tablePriKey_.push_back("ts");
    tablePriKey_.push_back("depth");
}

CallStackTable::~CallStackTable() {}

void CallStackTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

CallStackTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstInternalSlicesData().Size())),
      slicesObj_(dataCache->GetConstInternalSlicesData())
{
}

CallStackTable::Cursor::~Cursor() {}

int CallStackTable::Cursor::Column(int column) const
{
    switch (column) {
        case ID:
            sqlite3_result_int64(context_, CurrentRow());
            break;
        case TS:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.TimeStamData()[CurrentRow()]));
            break;
        case DUR:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.DursData()[CurrentRow()]));
            break;
        case CALL_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.CallIds()[CurrentRow()]));
            break;
        case CAT: {
            if (slicesObj_.CatsData()[CurrentRow()] != INVALID_UINT64) {
                auto catsDataIndex = static_cast<size_t>(slicesObj_.CatsData()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(catsDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case NAME: {
            if (slicesObj_.NamesData()[CurrentRow()] != INVALID_UINT64) {
                auto nameDataIndex = static_cast<size_t>(slicesObj_.NamesData()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(nameDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case DEPTH:
            sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.Depths()[CurrentRow()]));
            break;
        case COOKIE_ID:
            if (slicesObj_.Cookies()[CurrentRow()] != INVALID_UINT64) {
                sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.Cookies()[CurrentRow()]));
            }
            break;
        case PARENT_ID: {
            if (slicesObj_.ParentIdData()[CurrentRow()].has_value()) {
                sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.ParentIdData()[CurrentRow()].value()));
            }
            break;
        }
        case ARGSET:
            if (slicesObj_.ArgSetIdsData()[CurrentRow()] != INVALID_UINT32) {
                sqlite3_result_int64(context_, static_cast<int64_t>(slicesObj_.ArgSetIdsData()[CurrentRow()]));
            }
            break;
        case CHAIN_ID:
            sqlite3_result_text(context_, dataCache_->GetConstInternalSlicesData().ChainIds()[CurrentRow()].c_str(),
                                STR_DEFAULT_LEN, nullptr);
            break;
        case SPAN_ID:
            sqlite3_result_text(context_, dataCache_->GetConstInternalSlicesData().SpanIds()[CurrentRow()].c_str(),
                                STR_DEFAULT_LEN, nullptr);
            break;
        case PARENT_SPAN_ID:
            sqlite3_result_text(context_,
                                dataCache_->GetConstInternalSlicesData().ParentSpanIds()[CurrentRow()].c_str(),
                                STR_DEFAULT_LEN, nullptr);
            break;
        case FLAG:
            sqlite3_result_text(context_, dataCache_->GetConstInternalSlicesData().Flags()[CurrentRow()].c_str(),
                                STR_DEFAULT_LEN, nullptr);
            break;
        case ARGS:
            sqlite3_result_text(context_, dataCache_->GetConstInternalSlicesData().ArgsData()[CurrentRow()].c_str(),
                                STR_DEFAULT_LEN, nullptr);
            break;
        default:
            TS_LOGF("Unregistered column : %d", column);
            break;
    }
    return SQLITE_OK;
}
} // namespace TraceStreamer
} // namespace SysTuning

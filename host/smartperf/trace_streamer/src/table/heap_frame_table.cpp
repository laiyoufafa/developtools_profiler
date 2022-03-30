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

#include "heap_frame_table.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
enum Index { EVENT_ID = 0, DEPTH, IP, SP, SYMBOL_NAME, FILE_PATH, OFFSET, SYMBOL_OFFSET };
}
HeapFrameTable::HeapFrameTable(const TraceDataCache* dataCache) : TableBase(dataCache)
{
    tableColumn_.push_back(TableBase::ColumnInfo("eventId", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("depth", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("ip", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("sp", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("symbol_name", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("file_path", "STRING"));
    tableColumn_.push_back(TableBase::ColumnInfo("offset", "UNSIGNED BIG INT"));
    tableColumn_.push_back(TableBase::ColumnInfo("symbol_offset", "UNSIGNED BIG INT"));
    tablePriKey_.push_back("eventId");
}

HeapFrameTable::~HeapFrameTable() {}

void HeapFrameTable::CreateCursor()
{
    cursor_ = std::make_unique<Cursor>(dataCache_);
}

HeapFrameTable::Cursor::Cursor(const TraceDataCache* dataCache)
    : TableBase::Cursor(dataCache, 0, static_cast<uint32_t>(dataCache->GetConstHeapFrameData().Size())),
      heapFrameInfoObj_(dataCache->GetConstHeapFrameData())
{
}

HeapFrameTable::Cursor::~Cursor() {}

int HeapFrameTable::Cursor::Column(int column) const
{
    switch (column) {
        case EVENT_ID:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapFrameInfoObj_.EventIds()[CurrentRow()]));
            break;
        case DEPTH:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapFrameInfoObj_.Depths()[CurrentRow()]));
            break;
        case IP:
            sqlite3_result_int64(context_, static_cast<int64_t>(heapFrameInfoObj_.Ips()[CurrentRow()]));
            break;
        case SP: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapFrameInfoObj_.Sps()[CurrentRow()]));
            break;
        }
        case SYMBOL_NAME:
            if (heapFrameInfoObj_.SymbolNames()[CurrentRow()] != INVALID_UINT64) {
                auto symbolNameDataIndex = static_cast<int64_t>(heapFrameInfoObj_.SymbolNames()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(symbolNameDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        case FILE_PATH: {
            if (heapFrameInfoObj_.FilePaths()[CurrentRow()] != INVALID_UINT64) {
                auto filePathDataIndex = static_cast<size_t>(heapFrameInfoObj_.FilePaths()[CurrentRow()]);
                sqlite3_result_text(context_, dataCache_->GetDataFromDict(filePathDataIndex).c_str(), STR_DEFAULT_LEN,
                                    nullptr);
            }
            break;
        }
        case OFFSET: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapFrameInfoObj_.Offsets()[CurrentRow()]));
            break;
        }
        case SYMBOL_OFFSET: {
            sqlite3_result_int64(context_, static_cast<int64_t>(heapFrameInfoObj_.SymbolOffsets()[CurrentRow()]));
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

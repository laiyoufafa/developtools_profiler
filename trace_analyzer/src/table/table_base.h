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

#ifndef TABLE_H
#define TABLE_H

#include <memory>
#include <sqlite3.h>
#include <string>
#include <vector>
#include "trace_data_cache.h"

namespace SysTuning {
namespace TraceStreamer {
class TableBase;
constexpr int STR_DEFAULT_LEN = -1;
using TabTemplate = std::unique_ptr<TableBase>(*)(const TraceDataCache*);
class TableBase : public sqlite3_vtab {
public:
    virtual ~TableBase();
    TableBase(const TableBase&) = delete;
    TableBase& operator=(const TableBase&) = delete;

    template<typename T>
    static void TableDeclare(sqlite3& db, TraceDataCache* dataCache, const std::string& name)
    {
        TableRegister(db, dataCache, name,
            [](const TraceDataCache* cache) {
                return std::unique_ptr<TableBase>(std::make_unique<T>(cache));
            });
        dataCache->AppendNewTable(name);
    }

    std::string CreateTableSql() const;

    class Cursor : public sqlite3_vtab_cursor {
    public:
        Cursor(const TraceDataCache*, uint32_t, uint32_t);
        virtual ~Cursor();
        virtual int Next();
        virtual int Eof();
        virtual int Column(int) const = 0;
        sqlite3_context* context_;
    protected:
        uint32_t CurrentRow() const;
        const TraceDataCache* dataCache_;
    private:
        uint32_t currentRow_;
        uint32_t rowsTotalNum_;
    };

    struct ColumnInfo {
        ColumnInfo(const std::string& name, const std::string& type) : name_(name), type_(type) {}
        std::string name_;
        std::string type_;
    };

protected:
    explicit TableBase(const TraceDataCache* dataCache) : dataCache_(dataCache), cursor_(nullptr) {}
    virtual void CreateCursor() = 0;
    std::vector<ColumnInfo> tableColumn_ {};
    std::vector<std::string> tablePriKey_ {};
    const TraceDataCache* dataCache_;
    std::unique_ptr<Cursor> cursor_;
private:
    static void TableRegister(sqlite3& db, const TraceDataCache*, const std::string& name, TabTemplate);
    int Open(sqlite3_vtab_cursor**);
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // TABLE_H

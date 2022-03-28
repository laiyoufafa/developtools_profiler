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

#include "table_base.h"
#include <cctype>
#include <cstring>
#include "log.h"

#define UNUSED(expr)  \
    do {              \
        static_cast<void>(expr); \
    } while (0)

namespace SysTuning {
namespace TraceStreamer {
namespace {
struct TableContext {
    TabTemplate tmplate;
    const TraceDataCache* dataCache;
    sqlite3_module module;
};
} // namespace

TableBase::~TableBase()
{
    dataCache_ = nullptr;
    cursor_ = nullptr;
}

void TableBase::TableRegister(sqlite3& db,
                              const TraceDataCache* cache,
                              const std::string& tableName,
                              TabTemplate tmplate)
{
    std::unique_ptr<TableContext> context(std::make_unique<TableContext>());
    context->dataCache = cache;
    context->tmplate = tmplate;
    sqlite3_module& module = context->module;

    auto createFn = [](sqlite3* xdb, void* arg, int argc, const char* const* argv, sqlite3_vtab** tab, char** other) {
        UNUSED(argc);
        UNUSED(argv);
        UNUSED(other);
        auto xdesc = static_cast<const TableContext*>(arg);
        auto table = xdesc->tmplate(xdesc->dataCache);
        std::string createStmt = table->CreateTableSql();
        int res = sqlite3_declare_vtab(xdb, createStmt.c_str());
        if (res != SQLITE_OK) {
            return res;
        }
        *tab = table.release();
        return SQLITE_OK;
    };

    auto destroyFn = [](sqlite3_vtab* t) {
        delete static_cast<TableBase*>(t);
        return SQLITE_OK;
    };

    module.xCreate = createFn;
    module.xConnect = createFn;
    module.xBestIndex = [](sqlite3_vtab*, sqlite3_index_info*) { return SQLITE_OK; };
    module.xDisconnect = destroyFn;
    module.xDestroy = destroyFn;
    module.xOpen = [](sqlite3_vtab* t, sqlite3_vtab_cursor** c) {
        return (static_cast<TableBase*>(t))->Open(c);
    };
    module.xClose = [](sqlite3_vtab_cursor* c) {
        UNUSED(c);
        return SQLITE_OK;
    };
    module.xFilter = [](sqlite3_vtab_cursor* c, int arg1, const char* arg2, int, sqlite3_value** sqlite) {
        UNUSED(c);
        UNUSED(arg1);
        UNUSED(arg2);
        UNUSED(sqlite);
        return SQLITE_OK;
    };
    module.xNext = [](sqlite3_vtab_cursor* c) { return static_cast<TableBase::Cursor*>(c)->Next(); };
    module.xEof = [](sqlite3_vtab_cursor* c) { return static_cast<TableBase::Cursor*>(c)->Eof(); };
    module.xColumn = [](sqlite3_vtab_cursor* c, sqlite3_context* a, int b) {
        static_cast<TableBase::Cursor*>(c)->context_ = a;
        return static_cast<TableBase::Cursor*>(c)->Column(b);
    };

    sqlite3_create_module_v2(&db, tableName.c_str(), &module, context.release(),
                             [](void* arg) { delete static_cast<TableContext*>(arg); });
}

std::string TableBase::CreateTableSql() const
{
    std::string stmt = "CREATE TABLE x(";
    for (const auto& col : tableColumn_) {
        stmt += " " + col.name_ + " " + col.type_;
        stmt += ",";
    }
    stmt += " PRIMARY KEY(";
    for (size_t i = 0; i < tablePriKey_.size(); i++) {
        if (i != 0)
            stmt += ", ";
        stmt += tablePriKey_.at(i);
    }
    stmt += ")) WITHOUT ROWID;";
    return stmt;
}

int TableBase::Open(sqlite3_vtab_cursor** ppCursor)
{
    CreateCursor();
    *ppCursor = static_cast<sqlite3_vtab_cursor*>(cursor_.get());
    return SQLITE_OK;
}

TableBase::Cursor::Cursor(const TraceDataCache* dataCache, uint32_t row, uint32_t totalRows)
    : context_(nullptr), dataCache_(dataCache), currentRow_(row), rowsTotalNum_(totalRows)
{
}

TableBase::Cursor::~Cursor()
{
    context_ = nullptr;
    dataCache_ = nullptr;
}

int TableBase::Cursor::Next()
{
    currentRow_++;
    return SQLITE_OK;
}

int TableBase::Cursor::Eof()
{
    return currentRow_ >= rowsTotalNum_;
}

uint32_t TableBase::Cursor::CurrentRow() const
{
    return currentRow_;
}
} // namespace TraceStreamer
} // namespace SysTuning

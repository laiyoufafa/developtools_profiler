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

#ifndef TRACE_DATA_DB_H
#define TRACE_DATA_DB_H

#include <list>
#include <string>

extern "C"
{
    struct sqlite3;
    struct sqlite3_stmt;
    extern int sqlite3_close(sqlite3*);
    extern int sqlite3_finalize(sqlite3_stmt* pStmt);
}

namespace SysTuning {
namespace TraceStreamer {
class TraceDataDB {
public:
    TraceDataDB();
    TraceDataDB(const TraceDataDB&) = delete;
    TraceDataDB& operator=(const TraceDataDB&) = delete;
    virtual ~TraceDataDB();
    virtual void InitDB() = 0;

public:
    int ExportDatabase(const std::string& outputName);
    int SearchData(const std::string& outputName);
    void AppendNewTable(std::string tableName);
    void EnableMetaTable(bool enabled);

public:
    sqlite3* db_;

private:
    void ExecuteSql(const std::string_view&);
    std::list<std::string> internalTables_{};
    bool exportMetaTable_ = false;
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif

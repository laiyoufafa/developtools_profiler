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

#include "trace_data_db.h"
#include <cstring>
#include <fcntl.h>
#include <functional>
#include <sqlite3.h>
#include <string_view>
#include <unistd.h>

#include "file.h"
#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
TraceDataDB::TraceDataDB() : db_(nullptr)
{
    sqlite3_open(":memory:", &db_);
}
TraceDataDB::~TraceDataDB()
{
    sqlite3_close(db_);
}

void TraceDataDB::AppendNewTable(std::string tableName)
{
    internalTables_.push_back(tableName);
}
int TraceDataDB::ExportDatabase(const std::string& outputName)
{
    {
        int fd(base::OpenFile(outputName, O_CREAT | O_RDWR, 0600));
        if (!fd) {
            fprintf(stdout, "Failed to create file: %s", outputName.c_str());
            return 1;
        }
        ftruncate(fd, 0);
        close(fd);
    }

    std::string attachSql("ATTACH DATABASE '" + outputName + "' AS systuning_export");
    ExecuteSql(attachSql);

    for (auto itor = internalTables_.begin(); itor != internalTables_.end(); itor++) {
        std::string exportSql("CREATE TABLE systuning_export." + *itor + " AS SELECT * FROM " + *itor);
        ExecuteSql(exportSql);
    }

    std::string detachSql("DETACH DATABASE systuning_export");
    ExecuteSql(detachSql);
    return 0;
}
void TraceDataDB::ExecuteSql(const std::string_view& sql)
{
    sqlite3_stmt* stmt = nullptr;
    int ret = sqlite3_prepare_v2(db_, sql.data(), static_cast<int>(sql.size()), &stmt, nullptr);

    while (!ret) {
        int err = sqlite3_step(stmt);
        if (err == SQLITE_ROW) {
            continue;
        }
        if (err == SQLITE_DONE) {
            break;
        }
        ret = err;
    }

    sqlite3_finalize(stmt);
}
}
}

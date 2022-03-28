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

#include "codec_cov.h"
#include "file.h"
#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
int PrintQueryResult(void* para, int column, char** columnValue, char** columnName)
{
    int i;
    printf("Query results include %d column\n", column);
    for (i = 0; i < column; i++) {
        printf("name : %s \t value : %s\n", columnName[i], columnValue[i]);
    }
    printf("------------------\n");
    return 0;
}

TraceDataDB::TraceDataDB() : db_(nullptr)
{
    if (sqlite3_open(":memory:", &db_)) {
        TS_LOGF("open :memory db failed");
    }
}
TraceDataDB::~TraceDataDB()
{
    sqlite3_close(db_);
}

void TraceDataDB::AppendNewTable(std::string tableName)
{
    internalTables_.push_back(tableName);
}
void TraceDataDB::EnableMetaTable(bool enabled)
{
    exportMetaTable_ = enabled;
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
#ifdef _WIN32
    if (!base::GetCoding(reinterpret_cast<const uint8_t*>(attachSql.c_str()), attachSql.length())) {
        attachSql = base::GbkToUtf8(attachSql.c_str());
    }
#endif
    ExecuteSql(attachSql);

    for (auto itor = internalTables_.begin(); itor != internalTables_.end(); itor++) {
        if (*itor == "meta") {
            if (exportMetaTable_) {
                std::string exportSql("CREATE TABLE systuning_export." + *itor + " AS SELECT * FROM " + *itor);
                ExecuteSql(exportSql);
            }
        } else {
            std::string exportSql("CREATE TABLE systuning_export." + *itor + " AS SELECT * FROM " + *itor);
            ExecuteSql(exportSql);
        }
    }
    std::string createArgsView =
        "create view systuning_export.args_view AS select A.argset, V2.data as keyName, A.id, D.desc, (case when "
        "A.datatype==1 then V.data else A.value end) as strValue from args as A left join data_type as D on (D.typeId "
        "= A.datatype) left join data_dict as V on V.id = A.value left join data_dict as V2 on V2.id = A.key";
    ExecuteSql(createArgsView);
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

int TraceDataDB::SearchData(const std::string& outputName)
{
    {
        int fd(base::OpenFile(outputName, O_RDWR, 0600));
        if (!fd) {
            fprintf(stdout, "Failed to open file: %s", outputName.c_str());
            return 1;
        }
        ftruncate(fd, 0);
        close(fd);
    }

    std::string attachSql("ATTACH DATABASE '" + outputName + "' AS systuning_export");
#ifdef _WIN32
    if (!base::GetCoding(reinterpret_cast<const uint8_t*>(attachSql.c_str()), attachSql.length())) {
        attachSql = base::GbkToUtf8(attachSql.c_str());
    }
#endif
    ExecuteSql(attachSql);

    int result;
    char* errmsg = nullptr;
    std::string line;
    for (;;) {
        std::cout << "> ";
        getline(std::cin, line);
        if (line.empty()) {
            std::cout << "If you want to quit either type -q or press CTRL-Z" << std::endl;
            continue;
        }
        if (!line.compare("-q") || !line.compare("-quit")) {
            break;
        } else if (!line.compare("-help") || !line.compare("-h")) {
            std::cout << "use info" << std::endl;
            continue;
        }
        result = sqlite3_exec(db_, line.c_str(), PrintQueryResult, NULL, &errmsg);
    }

    std::string detachSql("DETACH DATABASE systuning_export");
    ExecuteSql(detachSql);
    return 0;
}
} // namespace TraceStreamer
} // namespace SysTuning

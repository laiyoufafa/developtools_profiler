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

package ohos.devtools.datasources.databases.datatable;

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * memory data
 *
 * @since 2021/10/22
 */
public class MemoryTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryTable.class);
    private static final String MEMORY_DB_NAME = "memory";

    /**
     * Memory Table initialize
     */
    public MemoryTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> processMemInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("sessionId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("Data BLOB NOT NULL");
            }
        };
        List<String> processMemInfoIndex = new ArrayList() {
            {
                add("sessionId");
                add("timeStamp");
            }
        };
        createTable(MEMORY_DB_NAME, "processMemInfo", processMemInfo);
        createIndex("processMemInfo", "processMemInfoIndex", processMemInfoIndex);
    }

    /**
     * insertProcessMemInfo
     *
     * @param processMemInfo processMemInfo
     * @return boolean
     */
    public boolean insertProcessMemInfo(List<ProcessMemInfo> processMemInfo) {
        return insertAppInfoBatch(processMemInfo);
    }

    private boolean insertAppInfoBatch(List<ProcessMemInfo> processMemInfos) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertAppInfoBatch");
        }
        Optional<Connection> option = getConnectByTable("processMemInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                conn.setAutoCommit(false);
                pst = conn.prepareStatement("INSERT OR IGNORE "
                    + "INTO "
                    + "processMemInfo("
                    + "session, "
                    + "sessionId, "
                    + "timeStamp, "
                    + "Data) "
                    + "VALUES (?, ?, ?, ?)");
                if (processMemInfos != null && processMemInfos.size() > 0) {
                    for (ProcessMemInfo processMemoryInfo : processMemInfos) {
                        pst.setLong(1, processMemoryInfo.getLocalSessionId());
                        pst.setInt(2, processMemoryInfo.getSessionId());
                        pst.setLong(3, processMemoryInfo.getTimeStamp());
                        if (processMemoryInfo.getData() != null) {
                            pst.setBytes(4, processMemoryInfo.getData().toByteArray());
                        }
                        pst.addBatch();
                    }
                    pst.executeBatch();
                    conn.commit();
                    conn.setAutoCommit(true);
                    return true;
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert AppInfo {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }
}

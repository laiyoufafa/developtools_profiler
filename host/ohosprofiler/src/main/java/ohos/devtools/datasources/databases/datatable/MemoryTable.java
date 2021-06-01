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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static ohos.devtools.views.common.LayoutConstants.INDEX_FOUR;
import static ohos.devtools.views.common.LayoutConstants.INDEX_ONE;
import static ohos.devtools.views.common.LayoutConstants.INDEX_THREE;
import static ohos.devtools.views.common.LayoutConstants.TWO;

/**
 * memory数据
 *
 * @version 1.0
 * @date 2021/02/20 16:51
 **/
public class MemoryTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryTable.class);
    private static final String MEMORY_DB_NAME = "memory";

    public MemoryTable() {
        initialize();
    }

    /**
     * initialization
     **/
    private void initialize() {
        // processMemInfo
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
                add("id");
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
        Optional<Connection> option = getConnectByTable("processMemInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            try {
                PreparedStatement pst = conn.prepareStatement(
                    "INSERT OR IGNORE INTO processMemInfo(session, sessionId, timeStamp, Data) VALUES (?, ?, ?, ?)");
                conn.setAutoCommit(false);
                processMemInfos.forEach(processMemoryInfo -> {
                    try {
                        pst.setLong(INDEX_ONE, processMemoryInfo.getLocalSessionId());
                        pst.setInt(TWO, processMemoryInfo.getSessionId());
                        pst.setLong(INDEX_THREE, processMemoryInfo.getTimeStamp());
                        pst.setBytes(INDEX_FOUR, processMemoryInfo.getData().toByteArray());
                        pst.addBatch();
                    } catch (SQLException sqlException) {
                        LOGGER.info("insert AppInfo {}", sqlException.getMessage());
                    }
                });
                try {
                    int[] results = pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException throwAbles) {
                    LOGGER.info("insert AppInfo {}", throwAbles.getMessage());
                } finally {
                    if (pst != null) {
                        pst.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                }
            } catch (SQLException exception) {
                LOGGER.error("insert AppInfo {}", exception.getMessage());
            }
        }
        return false;
    }
}

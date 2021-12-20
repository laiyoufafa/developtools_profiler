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
import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
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
 * DiskIo DB and Table Construct
 *
 * @since 2021/10/26
 */
public class DiskIoTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoTable.class);
    private static final String DISK_IO_DB_NAME = "diskIoDB";
    private static final String INSERT_SQL = "INSERT OR IGNORE "
        + "INTO "
        + "sysDiskIoInfo("
        + "session, "
        + "sessionId, "
        + "timeStamp, "
        + "readSectorsKb, "
        + "writeSectorsKb) "
        + "VALUES (?, ?, ?, ?, ?)";

    /**
     * constructor
     */
    public DiskIoTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> sysDiskIoInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("sessionId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("readSectorsKb BigDecimal NOT NULL");
                add("writeSectorsKb BigDecimal NOT NULL");
            }
        };
        List<String> sysDiskIoInfoIndex = new ArrayList() {
            {
                add("sessionId");
                add("timeStamp");
            }
        };
        createTable(DISK_IO_DB_NAME, "sysDiskIoInfo", sysDiskIoInfo);
        createIndex("sysDiskIoInfo", "sysDiskIoInfoIndex", sysDiskIoInfoIndex);
    }

    /**
     * insertSysInfoBatch
     *
     * @param sysDiskInfoList sysDiskInfoList
     * @return boolean
     */
    public boolean insertSysDiskIoInfo(List<DiskIOData> sysDiskInfoList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertSysDiskIoInfo");
        }
        Optional<Connection> option = getConnectByTable("sysDiskIoInfo");
        if (option.isPresent()) {
            try (Connection conn = option.get();
                PreparedStatement pst = conn.prepareStatement(INSERT_SQL)) {
                conn.setAutoCommit(false);
                sysDiskInfoList.forEach(sysDiskIoInfo -> {
                    try {
                        pst.setLong(1, sysDiskIoInfo.getLocalSessionId());
                        pst.setInt(2, sysDiskIoInfo.getSessionId());
                        pst.setLong(3, sysDiskIoInfo.getTimeStamp());
                        pst.setBigDecimal(4, sysDiskIoInfo.getReadSectorsKb());
                        pst.setBigDecimal(5, sysDiskIoInfo.getWriteSectorsKb());
                        pst.addBatch();
                    } catch (SQLException sqlException) {
                        LOGGER.info("insert SysDiskIoInfo {}", sqlException.getMessage());
                    }
                });
                try {
                    pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.info("insert SysDiskIoInfo {}", exception.getMessage());
                    }
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert SysDiskIoInfo {}", exception.getMessage());
                }
            }
        }
        return false;
    }
}

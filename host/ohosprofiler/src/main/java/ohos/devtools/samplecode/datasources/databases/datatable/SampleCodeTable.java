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

package ohos.devtools.samplecode.datasources.databases.datatable;

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.samplecode.datasources.databases.datatable.enties.SampleCodeInfo;
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
 * @since 2021/11/24
 */
public class SampleCodeTable extends AbstractDataStore<SampleCodeInfo> {
    private static final Logger LOGGER = LogManager.getLogger(SampleCodeTable.class);
    private static final String DB_NAME = "SampleDB";
    private static final String INSERT_SQL =
        "INSERT "
            + "OR "
            + "IGNORE INTO "
            + "sampleIoInfo("
            + "session, "
            + "sessionId, "
            + "timeStamp, "
            + "int_value, "
            + "double_value) "
            + "VALUES "
            + "(?, ?, ?, ?, ?)";

    /**
     * SampleCodeTable
     */
    public SampleCodeTable() {
        initialize();
    }

    /**
     * initialize
     */
    private void initialize() {
        List<String> sampleInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("sessionId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("int_value BigDecimal NOT NULL");
                add("double_value BigDecimal NOT NULL");
            }
        };
        List<String> sampleInfoIndex = new ArrayList() {
            {
                add("id");
                add("sessionId");
                add("timeStamp");
            }
        };
        createTable(DB_NAME, "sampleIoInfo", sampleInfo);
        createIndex("sampleIoInfo", "sampleIoInfoIndex", sampleInfoIndex);
    }

    /**
     * insertData
     *
     * @param dataList dataList
     * @return boolean
     */
    public boolean insertData(List<SampleCodeInfo> dataList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertData");
        }
        Optional<Connection> option = getConnectByTable("sampleIoInfo");
        if (option.isPresent()) {
            try (Connection conn = option.get(); PreparedStatement pst = conn.prepareStatement(INSERT_SQL)) {
                conn.setAutoCommit(false);
                dataList.forEach(sampleCodeInfo -> {
                    try {
                        pst.setLong(1, sampleCodeInfo.getLocalSessionId());
                        pst.setInt(2, sampleCodeInfo.getSessionId());
                        pst.setLong(3, sampleCodeInfo.getTimeStamp());
                        pst.setInt(4, sampleCodeInfo.getIntData());
                        pst.setDouble(5, sampleCodeInfo.getDoubleData());
                        pst.addBatch();
                    } catch (SQLException sqlException) {
                        if (ProfilerLogManager.isErrorEnabled()) {
                            LOGGER.error("insert sampleIoInfo {}", sqlException.getMessage());
                        }
                    }
                });
                try {
                    pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.info("insert sampleIoInfo {}", exception.getMessage());
                    }
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.info("insert sampleIoInfo {}", exception.getMessage());
                }
            }
        }
        return false;
    }
}

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
import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cpu table
 *
 * @since 2021/10/26
 */
public class CpuTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(CpuTable.class);
    private static final String CPU_DB_NAME = "cpuDb";

    /**
     * Cpu Table initialize
     */
    public CpuTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        // processCpuInfo
        List<String> processCpuInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("sessionId INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("Data BLOB NOT NULL");
            }
        };
        List<String> processCpuInfoIndex = new ArrayList() {
            {
                add("sessionId");
                add("timeStamp");
            }
        };
        createTable(CPU_DB_NAME, "processCpuInfo", processCpuInfo);
        createIndex("processCpuInfo", "processCpuInfoIndex", processCpuInfoIndex);
        List<String> deadThreadInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("session LONG NOT NULL");
                add("timeStamp Long NOT NULL");
                add("tid INTEGER NOT NULL");
                add("threadName String NOT NULL");

            }
        };
        createTable(CPU_DB_NAME, "deadThread", deadThreadInfo);
    }

    /**
     * insertProcessCpuInfo
     *
     * @param processCpuData processCpuData
     * @return boolean
     */
    public boolean insertProcessCpuInfo(List<ProcessCpuData> processCpuData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertProcessCpuInfo");
        }
        return insertAppInfoBatch(processCpuData);
    }

    private boolean insertAppInfoBatch(List<ProcessCpuData> processCpuDataList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertAppInfoBatch");
        }
        Optional<Connection> option = getConnectByTable("processCpuInfo");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("INSERT OR IGNORE "
                    + "INTO "
                    + "processCpuInfo("
                    + "session, "
                    + "sessionId, "
                    + "timeStamp, "
                    + "Data) "
                    + "VALUES (?, ?, ?, ?)");
                conn.setAutoCommit(false);
                if (processCpuDataList != null && processCpuDataList.size() > 0) {
                    for (ProcessCpuData processCpuData : processCpuDataList) {
                        pst.setLong(1, processCpuData.getLocalSessionId());
                        pst.setInt(2, processCpuData.getSessionId());
                        pst.setLong(3, processCpuData.getTimeStamp());
                        pst.setBytes(4, processCpuData.getData().toByteArray());
                        pst.addBatch();
                    }
                    pst.executeBatch();
                    conn.commit();
                    conn.setAutoCommit(true);
                    return true;
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert CPU data {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }


    /**
     * insertDeadThreadInfo
     *
     * @param deadThreadInfo deadThreadInfo
     * @param localSessionId localSessionId
     * @return boolean
     */
    public boolean insertDeadThreadInfo(Map<Long, ChartDataModel> deadThreadInfo, Long localSessionId) {
        Optional<Connection> option = getConnectByTable("deadThread");
        if (option.isPresent()) {
            Connection conn = option.get();
            PreparedStatement pst = null;
            try {
                pst = conn.prepareStatement("INSERT OR IGNORE "
                    + "INTO "
                    + "deadThread("
                    + "session, "
                    + "timeStamp, "
                    + "tid, "
                    + "threadName) "
                    + "VALUES (?, ?, ?, ?)");
                conn.setAutoCommit(false);
                if (deadThreadInfo != null && deadThreadInfo.size() > 0) {
                    Set<Map.Entry<Long, ChartDataModel>> entries = deadThreadInfo.entrySet();
                    for (Map.Entry<Long, ChartDataModel> entry : entries) {
                        Long timeStamp = entry.getKey();
                        ChartDataModel value = entry.getValue();
                        pst.setLong(1, localSessionId);
                        pst.setLong(2, timeStamp);
                        pst.setInt(3, value.getIndex());
                        pst.setString(4, value.getName());
                        pst.addBatch();
                    }
                    pst.executeBatch();
                    conn.commit();
                    conn.setAutoCommit(true);
                    return true;
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert CPU data {}", exception.getMessage());
                }
            } finally {
                close(pst, conn);
            }
        }
        return false;
    }
}


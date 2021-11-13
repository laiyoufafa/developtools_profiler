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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.hilog.HiLogBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Log Table
 *
 * @since: 2021/10/22 15:22
 */
public class LogTable extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(LogTable.class);
    private static final String LOG_DB_NAME = "LogDb";

    /**
     * constructor
     */
    public LogTable() {
        initialize();
    }

    /**
     * initialization
     */
    private void initialize() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initialize");
        }
        List<String> logInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("deviceName varchar(256) NOT NULL");
                add("date varchar(50) NOT NULL");
                add("time varchar(50) NOT NULL");
                add("pid varchar(12) NOT NULL");
                add("tid varchar(12) NOT NULL");
                add("logType varchar(10) NOT NULL");
                add("message varchar(1024) NOT NULL");
            }
        };
        createTable(LOG_DB_NAME, "logTable", logInfo);
    }

    /**
     * insertLogInfo
     *
     * @param hiLogBeans hiLogBeans
     * @return boolean
     */
    public boolean insertLogInfo(Queue<HiLogBean> hiLogBeans) {
        return insertLogInfoBatch(hiLogBeans);
    }

    private boolean insertLogInfoBatch(Queue<HiLogBean> hiLogBeans) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertLogInfoBatch");
        }
        Optional<Connection> option = getConnectByTable("logTable");
        if (option.isPresent()) {
            Connection conn = option.get();
            try {
                PreparedStatement pst = conn.prepareStatement(getIsertLogSql());
                conn.setAutoCommit(false);
                while (true) {
                    HiLogBean hiLogBean = hiLogBeans.poll();
                    if (hiLogBean != null) {
                        try {
                            pst.setString(1, hiLogBean.getDeviceName());
                            pst.setString(2, hiLogBean.getDate());
                            pst.setString(3, hiLogBean.getTime());
                            pst.setString(4, hiLogBean.getPid());
                            pst.setString(5, hiLogBean.getTid());
                            pst.setString(6, hiLogBean.getLogType());
                            pst.setString(7, hiLogBean.getMessage());
                            pst.addBatch();
                        } catch (SQLException sqlException) {
                            LOGGER.error("insert log data {}", sqlException.getMessage());
                        }
                    } else {
                        break;
                    }
                }
                try {
                    pst.executeBatch();
                    conn.commit();
                    return true;
                } catch (SQLException throwAbles) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("insert log data {}", throwAbles.getMessage());
                    }
                } finally {
                    closeConnection(pst, conn);
                }
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert log data {}", exception.getMessage());
                }
            }
        }
        return false;
    }

    private void closeConnection(PreparedStatement pst, Connection conn) {
        {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert log data {}", sqlException.getMessage());
                }
            }
        }
    }

    private String getIsertLogSql() {
        return "INSERT OR IGNORE "
            + "INTO "
            + "logTable("
            + "deviceName, "
            + "date, "
            + "time, "
            + "pid, "
            + "tid, "
            + "logType, "
            + "message) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }
}


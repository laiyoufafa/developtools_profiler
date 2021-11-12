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

package ohos.devtools.services.diskio;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
import ohos.devtools.datasources.utils.datahandler.datapoller.DiskIoDataConsumer;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ohos.devtools.services.diskio.DiskIoDao.DiskIoSelectStatements.SELECT_ALL_SYS_DISK_IO_INFO;
import static ohos.devtools.services.diskio.DiskIoDao.DiskIoSelectStatements.SELECT_DISK_AFTER_TAIL;
import static ohos.devtools.services.diskio.DiskIoDao.DiskIoSelectStatements.SELECT_DISK_BEFORE_HEAD;
import static ohos.devtools.services.diskio.DiskIoDao.DiskIoSelectStatements.SELECT_SYS_DISK_IO_INFO;

/**
 * DiskIo Classes that interact with the database
 *
 * @since 2021/8/25
 */
public class DiskIoDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoDao.class);
    private static volatile DiskIoDao singleton;

    private final Map<DiskIoSelectStatements, PreparedStatement> diskIoSelectMap = new HashMap<>();

    /**
     * DiskIoDao
     */
    private DiskIoDao() {
    }

    /**
     * Get an instance
     *
     * @return DiskIoDao
     */
    public static DiskIoDao getInstance() {
        if (singleton == null) {
            synchronized (DiskIoDao.class) {
                if (singleton == null) {
                    singleton = new DiskIoDao();
                }
            }
        }
        return singleton;
    }

    /**
     * MemorySelectStatements
     */
    public enum DiskIoSelectStatements {
        SELECT_SYS_DISK_IO_INFO("SELECT "
            + "timeStamp, "
            + "readSectorsKb, "
            + "writeSectorsKb "
            + "from "
            + "sysDiskIoInfo "
            + "where "
            + "session = "
            + "? "
            + "and "
            + "timeStamp > "
            + "? "
            + "and "
            + "timeStamp < "
            + "?"),

        SELECT_ALL_SYS_DISK_IO_INFO(
            "SELECT "
                + "timeStamp, "
                + "readSectorsKb, "
                + "writeSectorsKb "
                + "from "
                + "sysDiskIoInfo "
                + "where "
                + "session = "
                + "?"),

        DELETE_SYS_DISK_IO_INFO("delete "
            + "from "
            + "sysDiskIoInfo "
            + "where "
            + "session = "
            + "?"),

        SELECT_DISK_BEFORE_HEAD(
            "SELECT "
                + "timeStamp, "
                + "readSectorsKb, "
                + "writeSectorsKb "
                + "from "
                + "sysDiskIoInfo "
                + "where "
                + "session ="
                + " ? "
                + "and "
                + "timeStamp < ? "
                + "order by "
                + "timeStamp "
                + "desc "
                + "limit 1"),

        SELECT_DISK_AFTER_TAIL("SELECT "
            + "timeStamp, "
            + "readSectorsKb, "
            + "writeSectorsKb "
            + "from "
            + "sysDiskIoInfo "
            + "where "
            + "session ="
            + " ? "
            + "and "
            + "timeStamp > "
            + "? "
            + "order by "
            + "timeStamp "
            + "asc "
            + "limit 1");

        private final String sqlStatement;

        DiskIoSelectStatements(String sqlStatement) {
            this.sqlStatement = sqlStatement;
        }

        /**
         * sql Statement
         *
         * @return String
         */
        public String getStatement() {
            return sqlStatement;
        }
    }

    /**
     * getAllData
     *
     * @param sessionId sessionId
     * @return List <DiskIOData>
     */
    public List<DiskIOData> getAllData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllData");
        }
        Optional<Connection> connection = getConnectBydbName("diskIoDB");
        Connection conn = null;
        List<DiskIOData> result = new ArrayList<>();
        if (connection.isPresent()) {
            conn = connection.get();
            PreparedStatement pst = null;
            ResultSet resultSet = null;
            try {
                pst = conn.prepareStatement(SELECT_ALL_SYS_DISK_IO_INFO.getStatement());
                if (pst != null) {
                    pst.setLong(1, sessionId);
                    resultSet = pst.executeQuery();
                    while (resultSet.next()) {
                        long timeStamp = resultSet.getLong("timeStamp");
                        BigDecimal readSectorsKb = resultSet.getBigDecimal("readSectorsKb");
                        BigDecimal writeSectorsKb = resultSet.getBigDecimal("writeSectorsKb");
                        DiskIOData sysDiskIoInfo = new DiskIOData();
                        sysDiskIoInfo.setTimeStamp(timeStamp);
                        sysDiskIoInfo.setReadSectorsKb(readSectorsKb);
                        sysDiskIoInfo.setWriteSectorsKb(writeSectorsKb);
                        sysDiskIoInfo.setSession(sessionId);
                        result.add(sysDiskIoInfo);
                    }
                }
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(" SQLException", sqlException);
                }
            } finally {
                close(pst, resultSet, conn);
            }
        }
        return result;
    }

    /**
     * getData
     *
     * @param sessionId sessionId
     * @param min min
     * @param max max
     * @param startTimeStamp startTimeStamp
     * @param isNeedHeadTail isNeedHeadTail
     * @return LinkedHashMap <Integer, List<ChartDataModel>>
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getData(long sessionId, int min, int max, long startTimeStamp,
        boolean isNeedHeadTail) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getData");
        }
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        if (isNeedHeadTail && min > 0) {
            result.putAll(getTargetData(sessionId, min, startTimeStamp, true));
        }
        Optional<Connection> connection = getConnectBydbName("diskIoDB");
        if (connection.isPresent()) {
            Connection conn = connection.get();
            ResultSet rs = null;
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = conn.prepareStatement(SELECT_SYS_DISK_IO_INFO.getStatement());
                preparedStatement.setLong(1, sessionId);
                preparedStatement.setLong(2, startTimeStamp + min);
                preparedStatement.setLong(3, startTimeStamp + max);
                rs = preparedStatement.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        long timeStamp = rs.getLong("timeStamp");
                        BigDecimal readSectorsKb = rs.getBigDecimal("readSectorsKb");
                        BigDecimal writeSectorsKb = rs.getBigDecimal("writeSectorsKb");
                        DiskIOData sysDiskIoInfo = new DiskIOData();
                        sysDiskIoInfo.setSession(sessionId);
                        sysDiskIoInfo.setTimeStamp(timeStamp);
                        sysDiskIoInfo.setReadSectorsKb(readSectorsKb);
                        sysDiskIoInfo.setWriteSectorsKb(writeSectorsKb);
                        result.put((int) (timeStamp - startTimeStamp),
                            DiskIoDataConsumer.sysDiskIoSummary(sysDiskIoInfo));
                    }
                }
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(" SQLException ", sqlException);
                }
            } finally {
                close(preparedStatement, rs, conn);
            }
        }
        // Take the last point after the last point for Chart drawing fill in the blanks, and solve the boundary flicker
        if (isNeedHeadTail) {
            result.putAll(getTargetData(sessionId, max, startTimeStamp, false));
        }
        return result;
    }

    /**
     * Get the data before head or after tail
     *
     * @param sessionId Session id
     * @param offset time offset
     * @param startTs start/first timestamp
     * @param beforeHead true: before head, false: after tail
     * @return LinkedHashMap <Integer, List<ChartDataModel>>
     */
    private LinkedHashMap<Integer, List<ChartDataModel>> getTargetData(long sessionId, int offset, long startTs,
        boolean beforeHead) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTargetData");
        }
        Optional<Connection> connection = getConnectBydbName("diskIoDB");
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        if (connection.isPresent()) {
            Connection conn = connection.get();
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                if (beforeHead) {
                    pst = conn.prepareStatement(SELECT_DISK_BEFORE_HEAD.getStatement());
                } else {
                    pst = conn.prepareStatement(SELECT_DISK_AFTER_TAIL.getStatement());
                }
                pst.setLong(1, sessionId);
                pst.setLong(2, offset + startTs);
                rs = pst.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        long timeStamp = rs.getLong("timeStamp");
                        BigDecimal readSectorsKb = rs.getBigDecimal("readSectorsKb");
                        BigDecimal writeSectorsKb = rs.getBigDecimal("writeSectorsKb");

                        DiskIOData sysDiskIoInfo = new DiskIOData();
                        sysDiskIoInfo.setSession(sessionId);
                        sysDiskIoInfo.setTimeStamp(timeStamp);
                        sysDiskIoInfo.setReadSectorsKb(readSectorsKb);
                        sysDiskIoInfo.setWriteSectorsKb(writeSectorsKb);
                        result.put((int) (timeStamp - startTs), DiskIoDataConsumer.sysDiskIoSummary(sysDiskIoInfo));
                    }
                }
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(" SQLException ", sqlException);
                }
            } finally {
                close(pst, rs, conn);
            }
        }
        return result;
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSessionData");
        }
        Optional<Connection> sysDiskIoInfo = DataBaseApi.getInstance().getConnectByTable("sysDiskIoInfo");
        Connection connection = null;
        if (sysDiskIoInfo.isPresent()) {
            try {
                connection = sysDiskIoInfo.get();
                return execute(connection, "DELETE FROM " + "sysDiskIoInfo" + " WHERE session = " + sessionId);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException exception) {
                        if (ProfilerLogManager.isInfoEnabled()) {
                            LOGGER.info("deleteSessionData connection close error{}", exception.getMessage());
                        }
                    }
                }
            }
        } else {
            return false;
        }
    }
}

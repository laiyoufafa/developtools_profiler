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

package ohos.devtools.samplecode.services.samplecode;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.samplecode.datasources.databases.datatable.enties.SampleCodeInfo;
import ohos.devtools.samplecode.datasources.utils.datahandler.datapoller.SampleCodeConsumer;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static ohos.devtools.samplecode.services.samplecode.SampleCodeDao.SelectStatements.SELECT_AFTER_TAIL;
import static ohos.devtools.samplecode.services.samplecode.SampleCodeDao.SelectStatements.SELECT_ALL_INFO;
import static ohos.devtools.samplecode.services.samplecode.SampleCodeDao.SelectStatements.SELECT_BEFORE_HEAD;
import static ohos.devtools.samplecode.services.samplecode.SampleCodeDao.SelectStatements.SELECT_INFO;

/**
 * SampleCodeDao Classes that interact with the database
 *
 * @since 2021/9/20
 */
public class SampleCodeDao extends AbstractDataStore<SampleCodeInfo> {
    private static final Logger LOGGER = LogManager.getLogger(SampleCodeDao.class);
    private static final SampleCodeDao SAMPLE_CODE_DAO = new SampleCodeDao();

    /**
     * SampleCodeDao
     */
    private SampleCodeDao() {
    }

    /**
     * Get an instance
     *
     * @return SampleCodeDao
     */
    public static SampleCodeDao getInstance() {
        return SampleCodeDao.SAMPLE_CODE_DAO;
    }

    /**
     * MemorySelectStatements
     */
    public enum SelectStatements {
        SELECT_INFO("SELECT " + "timeStamp, " + "int_value, " + "double_value " + "from " + "sampleIoInfo " + "where "
            + "session = ? " + "and " + "timeStamp > ? " + "and " + "timeStamp < ?"),

        SELECT_ALL_INFO(
            "SELECT " + "timeStamp, " + "int_value, " + "double_value " + "from " + "sampleIoInfo " + "where "
                + "session = ?"),

        DELETE_INFO("delete " + "from " + "sampleIoInfo " + "where " + "session = ?"),

        SELECT_BEFORE_HEAD(
            "SELECT " + "timeStamp, " + "int_value, " + "double_value " + "from " + "sampleIoInfo " + "where "
                + "session = ? " + "and " + "timeStamp < ? " + "order by " + "timeStamp " + "desc " + "limit 1"),

        SELECT_AFTER_TAIL(
            "SELECT " + "timeStamp, " + "int_value, " + "double_value " + "from " + "sampleIoInfo " + "where "
                + "session = ? " + "and " + "timeStamp > ? " + "order by " + "timeStamp " + "asc " + "limit 1");

        private final String sqlStatement;

        SelectStatements(String sqlStatement) {
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
     * @return List <SampleCodeInfo>
     */
    public List<SampleCodeInfo> getAllData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllData");
        }
        List<SampleCodeInfo> result = new ArrayList<>();
        Optional<Connection> connectByTable = getConnectByTable("sampleIoInfo");
        if (connectByTable.isPresent()) {
            Connection connection = connectByTable.get();
            PreparedStatement preparedStatement = null;
            ResultSet rs = null;
            try {
                preparedStatement = connection.prepareStatement(SELECT_ALL_INFO.getStatement());
                preparedStatement.setLong(1, sessionId);
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    int intValue = rs.getInt("int_value");
                    double doubleValue = rs.getDouble("double_value");
                    SampleCodeInfo sampleCodeInfo = new SampleCodeInfo();
                    sampleCodeInfo.setTimeStamp(timeStamp);
                    sampleCodeInfo.setIntData(intValue);
                    sampleCodeInfo.setDoubleData(doubleValue);
                    sampleCodeInfo.setSession(sessionId);
                    result.add(sampleCodeInfo);
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            } finally {
                close(preparedStatement, rs, connection);
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
        Optional<Connection> connectByTable = getConnectByTable("sampleIoInfo");
        if (connectByTable.isPresent()) {
            Connection connection = connectByTable.get();
            PreparedStatement preparedStatement = null;
            ResultSet rs = null;
            try {
                preparedStatement = connection.prepareStatement(SELECT_INFO.getStatement());
                preparedStatement.setLong(1, sessionId);
                preparedStatement.setLong(2, startTimeStamp + min);
                preparedStatement.setLong(3, startTimeStamp + max);
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    int intValue = rs.getInt("int_value");
                    double doubleValue = rs.getDouble("double_value");
                    SampleCodeInfo sampleCodeInfo = new SampleCodeInfo();
                    sampleCodeInfo.setSession(sessionId);
                    sampleCodeInfo.setTimeStamp(timeStamp);
                    sampleCodeInfo.setIntData(intValue);
                    sampleCodeInfo.setDoubleData(doubleValue);
                    result.put((int) (timeStamp - startTimeStamp), SampleCodeConsumer.sampleSummary(sampleCodeInfo));
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            } finally {
                close(preparedStatement, rs, connection);
            }
        }
        if (isNeedHeadTail && min > 0) {
            result.putAll(getTargetData(sessionId, min, startTimeStamp, true));
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
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        PreparedStatement pst = null;
        ResultSet rs = null;
        Optional<Connection> connectByTable = getConnectByTable("sampleIoInfo");
        if (connectByTable.isPresent()) {
            Connection connection = connectByTable.get();
            try {
                if (beforeHead) {
                    pst = connection.prepareStatement(SELECT_BEFORE_HEAD.getStatement());
                } else {
                    pst = connection.prepareStatement(SELECT_AFTER_TAIL.getStatement());
                }
                pst.setLong(1, sessionId);
                pst.setLong(2, offset + startTs);
                rs = pst.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        long timeStamp = rs.getLong("timeStamp");
                        int intValue = rs.getInt("int_value");
                        double doubleValue = rs.getDouble("double_value");
                        SampleCodeInfo sampleCodeInfo = new SampleCodeInfo();
                        sampleCodeInfo.setSession(sessionId);
                        sampleCodeInfo.setTimeStamp(timeStamp);
                        sampleCodeInfo.setIntData(intValue);
                        sampleCodeInfo.setDoubleData(doubleValue);
                        result.put((int) (timeStamp - startTs), SampleCodeConsumer.sampleSummary(sampleCodeInfo));
                    }
                }

            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(" SQLException {}", throwAbles.getMessage());
                }
            } finally {
                close(pst, rs, connection);
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
        Optional<Connection> sampleIoInfo = DataBaseApi.getInstance().getConnectByTable("sampleIoInfo");
        if (sampleIoInfo.isPresent()) {
            return execute(sampleIoInfo.get(), "DELETE FROM  sampleIoInfo  WHERE session = " + sessionId);
        } else {
            return false;
        }
    }
}

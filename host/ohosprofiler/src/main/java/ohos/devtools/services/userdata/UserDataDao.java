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

package ohos.devtools.services.userdata;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.UserData;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Optional;

import static ohos.devtools.services.userdata.UserDataDao.SdkDataDaoStatements.SELECT_DISK_AFTER_TAIL;
import static ohos.devtools.services.userdata.UserDataDao.SdkDataDaoStatements.SELECT_DISK_BEFORE_HEAD;
import static ohos.devtools.services.userdata.UserDataDao.SdkDataDaoStatements.SELECT_SYS_DISK_IO_INFO;

/**
 * UserDataDao Classes that interact with the database
 *
 * @since 2021/11/22
 */
public class UserDataDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(UserDataDao.class);
    private static final UserDataDao SINGLETON = new UserDataDao();

    private UserDataDao() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("create UserDataDao");
        }
    }

    /**
     * Get an instance
     *
     * @return UserDataDao
     */
    public static UserDataDao getInstance() {
        return UserDataDao.SINGLETON;
    }

    /**
     * MemorySelectStatements
     */
    public enum SdkDataDaoStatements {
        SELECT_SYS_DISK_IO_INFO(
            "SELECT "
                + "timeStamp, "
                + "sdkData "
                + "from "
                + "sdkDataTable "
                + "where "
                + "sessionId = ? "
                + "and "
                + "timeStamp > ? "
                + "and "
                + "timeStamp < ?"),

        SELECT_ALL_SYS_DISK_IO_INFO(
            "SELECT "
                + "timeStamp, "
                + "sdkData "
                + "from "
                + "sdkDataTable "
                + "where "
                + "session = ?"),

        DELETE_SYS_DISK_IO_INFO("delete from "
            + "sdkDataTable "
            + "where "
            + "session = ?"),

        SELECT_DISK_BEFORE_HEAD(
            "SELECT "
                + "timeStamp, "
                + "sdkData "
                + "from "
                + "sdkDataTable "
                + "where "
                + "sessionId = ? "
                + "and "
                + "timeStamp < ? "
                + "order by "
                + "timeStamp "
                + "desc "
                + "limit 1"),

        SELECT_DISK_AFTER_TAIL(
            "SELECT "
                + "timeStamp, "
                + "sdkData "
                + "from "
                + "sdkDataTable "
                + "where "
                + "sessionId = ? "
                + "and "
                + "timeStamp > ? "
                + "order by "
                + "timeStamp "
                + "asc "
                + "limit 1");

        private final String sqlStatement;

        SdkDataDaoStatements(String sqlStatement) {
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
     * getData
     *
     * @param sessionId sessionId
     * @param min min
     * @param max max
     * @param startTimeStamp startTimeStamp
     * @param isNeedHeadTail isNeedHeadTail
     * @return LinkedHashMap
     */
    public LinkedHashMap<Integer, UserData> getData(long sessionId, int min, int max, long startTimeStamp,
        boolean isNeedHeadTail) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getData");
        }
        LinkedHashMap<Integer, UserData> result = new LinkedHashMap<>();
        if (isNeedHeadTail && min > 0) {
            result.putAll(getTargetData(sessionId, min, startTimeStamp, true));
        }

        Optional<Connection> sdkDataTable = getConnectByTable("sdkDataTable");
        if (!sdkDataTable.isPresent()) {
            return result;
        }
        Connection connection = sdkDataTable.get();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = connection.prepareStatement(SELECT_SYS_DISK_IO_INFO.getStatement());
            preparedStatement.setLong(1, sessionId);
            preparedStatement.setLong(2, startTimeStamp + min);
            preparedStatement.setLong(3, startTimeStamp + max);
            rs = preparedStatement.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    byte[] data = rs.getBytes("sdkData");
                    UserData sysDiskIoInfo = new UserData();
                    sysDiskIoInfo.setSession(sessionId);
                    sysDiskIoInfo.setTimeStamp(timeStamp);
                    sysDiskIoInfo.setBytes(data);
                    result.put((int) (timeStamp - startTimeStamp), sysDiskIoInfo);
                }
            }
        } catch (SQLException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(throwAbles.getMessage());
            }
        } finally {
            close(preparedStatement, rs, connection);
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
     * @return Map
     */
    private LinkedHashMap<Integer, UserData> getTargetData(long sessionId, int offset, long startTs,
        boolean beforeHead) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTargetData");
        }
        LinkedHashMap<Integer, UserData> result = new LinkedHashMap<>();
        Optional<Connection> sdkDataTable = getConnectByTable("sdkDataTable");
        if (!sdkDataTable.isPresent()) {
            return result;
        }
        Connection connection = sdkDataTable.get();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            if (beforeHead) {
                pst = connection.prepareStatement(SELECT_DISK_BEFORE_HEAD.getStatement());
            } else {
                pst = connection.prepareStatement(SELECT_DISK_AFTER_TAIL.getStatement());
            }
            pst.setLong(1, sessionId);
            pst.setLong(2, offset + startTs);
            rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    byte[] data = rs.getBytes("sdkData");
                    UserData sysDiskIoInfo = new UserData();
                    sysDiskIoInfo.setSession(sessionId);
                    sysDiskIoInfo.setTimeStamp(timeStamp);
                    sysDiskIoInfo.setBytes(data);
                    result.put((int) (timeStamp - startTs), sysDiskIoInfo);
                }
            }
        } catch (SQLException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(throwables.getMessage());
            }
        } finally {
            close(pst, rs, connection);
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
        Optional<Connection> sysSdkInfo = DataBaseApi.getInstance().getConnectByTable("sdkDataTable");
        if (sysSdkInfo.isPresent()) {
            return execute(sysSdkInfo.get(), "DELETE FROM sdkDataTable WHERE sessionId = " + sessionId);
        } else {
            return false;
        }
    }
}

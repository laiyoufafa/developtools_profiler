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

package ohos.devtools.services.memory.memorydao;

import com.google.protobuf.InvalidProtocolBufferException;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.utils.datahandler.datapoller.MemoryDataConsumer;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

import static ohos.devtools.services.memory.memorydao.MemoryDao.MemorySelectStatements.SELECT_AFTER_TAIL;
import static ohos.devtools.services.memory.memorydao.MemoryDao.MemorySelectStatements.SELECT_ALL_APP_MEM_INFO;
import static ohos.devtools.services.memory.memorydao.MemoryDao.MemorySelectStatements.SELECT_APP_MEM_INFO;
import static ohos.devtools.services.memory.memorydao.MemoryDao.MemorySelectStatements.SELECT_BEFORE_HEAD;

/**
 * memory And Database Interaction Class
 *
 * @since 2021/5/19 16:39
 */
public class MemoryDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryDao.class);
    private static volatile MemoryDao singleton;

    /**
     * get Instance
     *
     * @return MemoryDao
     */
    public static MemoryDao getInstance() {
        if (singleton == null) {
            synchronized (MemoryDao.class) {
                if (singleton == null) {
                    singleton = new MemoryDao();
                }
            }
        }
        return singleton;
    }

    private Map<MemorySelectStatements, PreparedStatement> memorySelectMap = new HashMap<>();

    /**
     * Memory Select Statements
     */
    public enum MemorySelectStatements {
        SELECT_APP_MEM_INFO(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processMemInfo "
                + "where "
                + "session = ? "
                + "and "
                + "timeStamp > ? "
                + "and "
                + "timeStamp < ?"),

        SELECT_ALL_APP_MEM_INFO(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processMemInfo "
                + "where "
                + "session = ?"),

        DELETE_APP_MEM_INFO("delete from "
            + "processMemInfo "
            + "where "
            + "session = ?"),

        SELECT_BEFORE_HEAD(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processMemInfo "
                + "where session = ? "
                + "and "
                + "timeStamp < ? "
                + "order by "
                + "timeStamp "
                + "desc limit 1"),

        SELECT_AFTER_TAIL(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processMemInfo "
                + "where "
                + "session = ? "
                + "and "
                + "timeStamp > ? "
                + "order by "
                + "timeStamp "
                + "asc "
                + "limit 1");

        private final String sqlStatement;

        MemorySelectStatements(String sqlStatement) {
            this.sqlStatement = sqlStatement;
        }

        /**
         * get Statement
         *
         * @return String
         */
        public String getStatement() {
            return sqlStatement;
        }
    }

    private Connection conn;

    private MemoryDao() {
        if (conn == null) {
            Optional<Connection> connection = getConnectBydbName("memory");
            if (connection.isPresent()) {
                conn = connection.get();
            }
            createPrePareStatements();
        }
    }

    private void createPrePareStatements() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createPrePareStatements");
        }
        MemorySelectStatements[] values = MemorySelectStatements.values();
        for (MemorySelectStatements sta : values) {
            PreparedStatement psmt = null;
            try {
                psmt = conn.prepareStatement(sta.getStatement());
                memorySelectMap.put(sta, psmt);
            } catch (SQLException throwAbles) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(" SQLException {}", throwAbles.getMessage());
                }
            }
        }
    }

    /**
     * get All Data
     *
     * @param sessionId sessionId
     * @return List <ProcessMemInfo>
     */
    public List<ProcessMemInfo> getAllData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllData");
        }
        PreparedStatement pst = memorySelectMap.get(SELECT_ALL_APP_MEM_INFO);
        List<ProcessMemInfo> result = new ArrayList<>();
        try {
            if (pst != null) {
                pst.setLong(1, sessionId);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    byte[] data = rs.getBytes("Data");
                    if (data == null) {
                        continue;
                    }
                    ProcessMemInfo processMem = new ProcessMemInfo();
                    MemoryPluginResult.AppSummary.Builder builders = MemoryPluginResult.AppSummary.newBuilder();
                    MemoryPluginResult.AppSummary appSummary = builders.mergeFrom(data).build();
                    processMem.setTimeStamp(timeStamp);
                    processMem.setData(appSummary);
                    processMem.setSession(sessionId);
                    result.add(processMem);
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwables.getMessage());
            }
            close();
        }
        return result;
    }

    /**
     * get Data
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
        PreparedStatement pst = memorySelectMap.get(SELECT_APP_MEM_INFO);
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        if (pst == null) {
            return result;
        }
        // 当startTime > 0时（Chart铺满界面时），需要取第一个点的前一个点用于Chart绘制，填充空白，解决边界闪烁
        if (isNeedHeadTail && min > 0) {
            result.putAll(getTargetData(sessionId, min, startTimeStamp, true));
        }
        try {
            pst.setLong(1, sessionId);
            pst.setLong(2, startTimeStamp + min);
            pst.setLong(3, startTimeStamp + max);
            ResultSet rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    byte[] data = rs.getBytes("Data");
                    if (data == null) {
                        continue;
                    }
                    MemoryPluginResult.AppSummary.Builder builders = MemoryPluginResult.AppSummary.newBuilder();
                    MemoryPluginResult.AppSummary appSummary = builders.mergeFrom(data).build();
                    result.put((int) (timeStamp - startTimeStamp), MemoryDataConsumer.processAppSummary(appSummary));
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
            close();
        }

        // 取最后一个点的后一个点用于Chart绘制，填充空白，解决边界闪烁
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
        PreparedStatement pst;
        if (beforeHead) {
            pst = memorySelectMap.get(SELECT_BEFORE_HEAD);
        } else {
            pst = memorySelectMap.get(SELECT_AFTER_TAIL);
        }
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        if (pst == null) {
            return result;
        }

        try {
            pst.setLong(1, sessionId);
            pst.setLong(2, offset + startTs);
            ResultSet rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    byte[] data = rs.getBytes("Data");
                    if (data == null) {
                        continue;
                    }
                    MemoryPluginResult.AppSummary.Builder builders = MemoryPluginResult.AppSummary.newBuilder();
                    MemoryPluginResult.AppSummary appSummary = builders.mergeFrom(data).build();
                    result.put((int) (timeStamp - startTs), MemoryDataConsumer.processAppSummary(appSummary));
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
            close();
        }
        return result;
    }

    private void close() {
        if (conn != null) {
            try {
                memorySelectMap.values().forEach(preparedStatement -> {
                    try {
                        preparedStatement.close();
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                });
                memorySelectMap.clear();
                conn.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            } finally {
                conn = null;
            }
        }
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
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ");
        deleteSql.append("processMemInfo").append(" WHERE session = ").append(sessionId);
        Optional<Connection> processMemInfo = DataBaseApi.getInstance().getConnectByTable("processMemInfo");
        Connection connection = null;
        if (processMemInfo.isPresent()) {
            connection = processMemInfo.get();
        } else {
            return false;
        }
        return execute(connection, deleteSql.toString());
    }
}

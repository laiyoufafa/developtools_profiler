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

package ohos.devtools.services.cpu;

import com.google.protobuf.InvalidProtocolBufferException;
import com.intellij.ui.JBColor;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.datasources.transport.grpc.service.CpuPluginResult;
import ohos.devtools.datasources.utils.datahandler.datapoller.CpuDataConsumer;
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

import static ohos.devtools.services.cpu.CpuDao.CpuSelectStatements.SELECT_AFTER_TAIL;
import static ohos.devtools.services.cpu.CpuDao.CpuSelectStatements.SELECT_ALL_APP_CPU_INFO;
import static ohos.devtools.services.cpu.CpuDao.CpuSelectStatements.SELECT_APP_CPU_INFO;
import static ohos.devtools.services.cpu.CpuDao.CpuSelectStatements.SELECT_BEFORE_HEAD;
import static ohos.devtools.services.cpu.CpuDao.CpuSelectStatements.SELECT_DEAD_THREAD_INFO;

/**
 * CpuDao
 *
 * @since 2021/11/22
 */
public class CpuDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(CpuDao.class);
    private static volatile CpuDao singleton;

    private Map<CpuDao.CpuSelectStatements, PreparedStatement> cpuSelectMap = new HashMap<>();
    private Map<CpuDao.CpuSelectStatements, PreparedStatement> threadSelectMap = new HashMap<>();
    private Connection conn;

    private CpuDao() {
        if (conn == null) {
            Optional<Connection> connection = getConnectBydbName("cpuDb");
            if (connection.isPresent()) {
                conn = connection.get();
            }
            createPrePareStatements();
        }
    }

    /**
     * getInstance
     *
     * @return CpuDao
     */
    public static CpuDao getInstance() {
        if (singleton == null) {
            synchronized (CpuDao.class) {
                if (singleton == null) {
                    singleton = new CpuDao();
                }
            }
        }
        return singleton;
    }

    /**
     * Cpu Select Statements
     */
    public enum CpuSelectStatements {
        SELECT_APP_CPU_INFO(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processCpuInfo "
                + "where "
                + "session = ? "
                + "and "
                + "timeStamp > ? "
                + "and "
                + "timeStamp < ?"),
        SELECT_ALL_APP_CPU_INFO(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processCpuInfo "
                + "where "
                + "session = ?"),
        DELETE_APP_CPU_INFO(
            "delete "
                + "from "
                + "processCpuInfo "
                + "where "
                + "session = ?"),
        SELECT_BEFORE_HEAD(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processCpuInfo "
                + "where "
                + "session = ? "
                + "and "
                + "timeStamp < ? "
                + "order by "
                + "timeStamp "
                + "desc "
                + "limit 1"),
        SELECT_AFTER_TAIL(
            "SELECT "
                + "timeStamp, "
                + "Data "
                + "from "
                + "processCpuInfo "
                + "where "
                + "session = ? "
                + "and "
                + "timeStamp > ? "
                + "order by "
                + "timeStamp "
                + "asc "
                + "limit 1"),
        SELECT_DEAD_THREAD_INFO(
            "SELECT "
                + "timeStamp, "
                + "tid, "
                + "threadName "
                + "from "
                + "deadThread "
                + "where "
                + "session = ? "
                + "and "
                + "timeStamp > ? "
                + "and "
                + "timeStamp < ?");

        CpuSelectStatements(String sqlStatement) {
            this.sqlStatement = sqlStatement;
        }

        private final String sqlStatement;

        /**
         * getStatement
         *
         * @return String
         */
        public String getStatement() {
            return sqlStatement;
        }
    }

    private void createPrePareStatements() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createPrePareStatements");
        }
        CpuDao.CpuSelectStatements[] values = CpuDao.CpuSelectStatements.values();
        for (CpuDao.CpuSelectStatements sta : values) {
            PreparedStatement psmt = null;
            try {
                psmt = conn.prepareStatement(sta.getStatement());
                cpuSelectMap.put(sta, psmt);
                threadSelectMap.put(sta, psmt);
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
     * @return List <ProcessCpuInfo>
     */
    public List<ProcessCpuData> getAllData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllData");
        }
        PreparedStatement pst = cpuSelectMap.get(SELECT_ALL_APP_CPU_INFO);
        List<ProcessCpuData> result = new ArrayList<>();
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
                    ProcessCpuData processCpu = new ProcessCpuData();
                    CpuPluginResult.CpuData.Builder builders = CpuPluginResult.CpuData.newBuilder();
                    CpuPluginResult.CpuData appSummary = builders.mergeFrom(data).build();
                    processCpu.setData(appSummary);
                    processCpu.setTimeStamp(timeStamp);
                    processCpu.setSession(sessionId);
                    result.add(processCpu);
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwables.getMessage());
            }
        }
        return result;
    }

    /**
     * get Cpu Data
     *
     * @param sessionId sessionId
     * @param min min
     * @param max max
     * @param startTimeStamp startTimeStamp
     * @param isNeedHeadTail isNeedHeadTail
     * @return LinkedHashMap <Integer, List<ChartDataModel>>
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getCpuData(long sessionId, int min, int max,
        long startTimeStamp, boolean isNeedHeadTail) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getCpuData");
        }
        PreparedStatement pst = cpuSelectMap.get(SELECT_APP_CPU_INFO);
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        if (pst == null) {
            return result;
        }
        // 当startTime > 0时（Chart铺满界面时），需要取第一个点的前一个点用于Chart绘制，填充空白，解决边界闪烁
        if (isNeedHeadTail && min > 0) {
            result.putAll(getCpuTargetData(sessionId, min, startTimeStamp, true));
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
                    CpuPluginResult.CpuData.Builder builders = CpuPluginResult.CpuData.newBuilder();
                    CpuPluginResult.CpuData cpuData = builders.mergeFrom(data).build();
                    result.put((int) (timeStamp - startTimeStamp), CpuDataConsumer.getProcessData(cpuData));
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
        }
        // 取最后一个点的后一个点用于Chart绘制，填充空白，解决边界闪烁
        if (isNeedHeadTail) {
            result.putAll(getCpuTargetData(sessionId, max, startTimeStamp, false));
        }
        return result;
    }

    /**
     * get ThreadData
     *
     * @param sessionId sessionId
     * @param min min
     * @param max max
     * @param startTimeStamp startTimeStamp
     * @param isNeedHeadTail isNeedHeadTail
     * @return LinkedHashMap <Integer, List<ChartDataModel>>
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getThreadData(long sessionId, int min, int max,
        long startTimeStamp, boolean isNeedHeadTail) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getThreadData");
        }
        PreparedStatement pst = threadSelectMap.get(SELECT_APP_CPU_INFO);
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        if (pst == null) {
            return result;
        }
        // 当startTime > 0时（Chart铺满界面时），需要取第一个点的前一个点用于Chart绘制，填充空白，解决边界闪烁
        if (isNeedHeadTail && min > 0) {
            result.putAll(getThreadTargetData(sessionId, min, startTimeStamp, true));
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
                    CpuPluginResult.CpuData.Builder builders = CpuPluginResult.CpuData.newBuilder();
                    CpuPluginResult.CpuData cpuData = builders.mergeFrom(data).build();
                    result.put((int) (timeStamp - startTimeStamp), getThreadStatus(cpuData));
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
        }
        // 取最后一个点的后一个点用于Chart绘制，填充空白，解决边界闪烁
        if (isNeedHeadTail) {
            result.putAll(getThreadTargetData(sessionId, max, startTimeStamp, false));
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
    private LinkedHashMap<Integer, List<ChartDataModel>> getCpuTargetData(long sessionId, int offset, long startTs,
        boolean beforeHead) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getCpuTargetData");
        }
        PreparedStatement pst;
        if (beforeHead) {
            pst = cpuSelectMap.get(SELECT_BEFORE_HEAD);
        } else {
            pst = cpuSelectMap.get(SELECT_AFTER_TAIL);
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
                    CpuPluginResult.CpuData.Builder builders = CpuPluginResult.CpuData.newBuilder();
                    CpuPluginResult.CpuData appSummary = builders.mergeFrom(data).build();
                    result.put((int) (timeStamp - startTs), CpuDataConsumer.getProcessData(appSummary));
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
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
    private LinkedHashMap<Integer, List<ChartDataModel>> getThreadTargetData(long sessionId, int offset, long startTs,
        boolean beforeHead) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getThreadTargetData");
        }
        PreparedStatement pst;
        if (beforeHead) {
            pst = cpuSelectMap.get(SELECT_BEFORE_HEAD);
        } else {
            pst = cpuSelectMap.get(SELECT_AFTER_TAIL);
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
                    CpuPluginResult.CpuData.Builder builders = CpuPluginResult.CpuData.newBuilder();
                    CpuPluginResult.CpuData appSummary = builders.mergeFrom(data).build();
                    result.put((int) (timeStamp - startTs), getThreadStatus(appSummary));
                }
            }
        } catch (SQLException | InvalidProtocolBufferException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
        }
        return result;
    }

    /**
     * Get the dead Thread Info
     *
     * @param sessionId Session id
     * @param min min offset
     * @param max max offset
     * @param startTimeStamp start/first timestamp
     * @return LinkedHashMap <Integer, List<ChartDataModel>>
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getDeadThreadData(long sessionId, int min, int max,
        long startTimeStamp) {
        Optional<Connection> connect = getConnectByTable("deadThread");
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        connect.ifPresent(connection -> {
            PreparedStatement preparedStatement = null;
            ResultSet rs = null;
            try {
                preparedStatement = connection.prepareStatement(SELECT_DEAD_THREAD_INFO.getStatement());
                preparedStatement.setLong(1, sessionId);
                preparedStatement.setLong(2, startTimeStamp + min);
                preparedStatement.setLong(3, startTimeStamp + max);
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    long timeStamp = rs.getLong("timeStamp");
                    int treadId = rs.getInt("tid");
                    String threadName = rs.getString("threadName");
                    ChartDataModel threadInfoModel = new ChartDataModel();
                    threadInfoModel.setIndex(treadId);
                    threadInfoModel.setColor(JBColor.GREEN);
                    threadInfoModel.setName(threadName);
                    int key = (int) (timeStamp - startTimeStamp);
                    List<ChartDataModel> chartDataModels = result.get(key);
                    if (chartDataModels != null) {
                        chartDataModels.add(threadInfoModel);
                        result.put(key, chartDataModels);
                    } else {
                        List<ChartDataModel> deadThread = new ArrayList<>();
                        deadThread.add(threadInfoModel);
                        result.put(key, deadThread);
                    }
                }
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("SQLException ", sqlException);
                }
            } finally {
                close(preparedStatement, rs, connection);
            }
        });
        return result;
    }

    /**
     * delete SessionData by sessionId
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSessionData");
        }
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ");
        deleteSql.append("processCpuInfo").append(" WHERE session = ").append(sessionId);
        Optional<Connection> processCpuInfo = DataBaseApi.getInstance().getConnectByTable("processCpuInfo");
        if (processCpuInfo.isPresent()) {
            Connection connection = processCpuInfo.get();
            return execute(connection, deleteSql.toString());
        }
        return true;
    }

    /**
     * getThreadStatus
     *
     * @param cpuData cpuData
     * @return List <ChartDataModel>
     */
    public List<ChartDataModel> getThreadStatus(CpuPluginResult.CpuData cpuData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getThreadStatus");
        }
        List<ChartDataModel> list = new ArrayList<>();
        // add the thread info data
        long elapsedTime =
            (cpuData.getCpuUsageInfo().getSystemBootTimeMs() - cpuData.getCpuUsageInfo().getPrevSystemBootTimeMs());
        cpuData.getThreadInfoList().forEach(threadInfo -> {
            BigDecimal threadValue = elapsedTime == 0 ? BigDecimal.valueOf(0D) : BigDecimal
                .valueOf(100.0 * (threadInfo.getThreadCpuTimeMs() - threadInfo.getPrevThreadCpuTimeMs()) / elapsedTime);
            ChartDataModel threadInfoModel = new ChartDataModel();
            threadInfoModel.setIndex(threadInfo.getTid());
            threadInfoModel.setColor(JBColor.GREEN);
            threadInfoModel.setName(threadInfo.getThreadName());
            threadInfoModel.setValue(threadInfo.getThreadStateValue());
            threadInfoModel.setDoubleValue(threadValue.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            list.add(threadInfoModel);
        });
        return list;
    }
}

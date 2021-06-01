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

package ohos.devtools.services.memory;

import com.google.protobuf.InvalidProtocolBufferException;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.views.common.LayoutConstants;
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

import static ohos.devtools.services.memory.MemoryDao.MemorySelectStatements.SELECT_AFTER_TAIL;
import static ohos.devtools.services.memory.MemoryDao.MemorySelectStatements.SELECT_ALL_APP_MEM_INFO;
import static ohos.devtools.services.memory.MemoryDao.MemorySelectStatements.SELECT_APP_MEM_INFO;
import static ohos.devtools.services.memory.MemoryDao.MemorySelectStatements.SELECT_BEFORE_HEAD;

/**
 * @Description Memory与数据库交互的类
 * @Date 2021/2/7 14:01
 **/
public class MemoryDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryDao.class);

    private static volatile MemoryDao singleton;

    /**
     * 获取实例
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
     * MemorySelectStatements
     **/
    public enum MemorySelectStatements {
        SELECT_APP_MEM_INFO(
            "SELECT timeStamp, Data from processMemInfo where session = ? and timeStamp > ? and timeStamp < ?"),

        SELECT_ALL_APP_MEM_INFO("SELECT timeStamp, Data from processMemInfo where session = ?"),

        DELETE_APP_MEM_INFO("delete from processMemInfo where session = ?"),

        SELECT_BEFORE_HEAD("SELECT timeStamp, Data from processMemInfo where session ="
            + " ? and timeStamp < ? order by timeStamp desc limit 1"),

        SELECT_AFTER_TAIL("SELECT timeStamp, Data from processMemInfo where session ="
            + " ? and timeStamp > ? order by timeStamp asc limit 1");

        private final String sqlStatement;

        MemorySelectStatements(String sqlStatement) {
            this.sqlStatement = sqlStatement;
        }

        /**
         * 获取sql语句
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
        MemorySelectStatements[] values = MemorySelectStatements.values();
        for (MemorySelectStatements sta : values) {
            PreparedStatement psmt = null;
            try {
                psmt = conn.prepareStatement(sta.getStatement());
                memorySelectMap.put(sta, psmt);
            } catch (SQLException throwAbles) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
        }
    }

    /**
     * getAllData
     *
     * @param sessionId sessionId
     * @return List<ProcessMemInfo>
     */
    public List<ProcessMemInfo> getAllData(long sessionId) {
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
            LOGGER.error(" SQLException {}", throwables.getMessage());
        }
        return result;
    }

    /**
     * getData
     *
     * @param sessionId      sessionId
     * @param min            min
     * @param max            max
     * @param startTimeStamp startTimeStamp
     * @param isNeedHeadTail isNeedHeadTail
     * @return LinkedHashMap<Long, MemoryPluginResult.AppSummary>
     */
    public LinkedHashMap<Long, MemoryPluginResult.AppSummary> getData(long sessionId, int min, int max,
        long startTimeStamp, boolean isNeedHeadTail) {
        PreparedStatement pst = memorySelectMap.get(SELECT_APP_MEM_INFO);
        LinkedHashMap<Long, MemoryPluginResult.AppSummary> result = new LinkedHashMap();
        // 当startTime > 0时（Chart铺满界面时），需要取第一个点的前一个点用于Chart绘制，填充空白，解决边界闪烁
        if (isNeedHeadTail && min > 0) {
            LinkedHashMap<Long, MemoryPluginResult.AppSummary> head = getBeforeHead(sessionId, startTimeStamp + min);
            if (head.size() > 0) {
                Map.Entry headEntry = head.entrySet().iterator().next();
                Long key = -1L;
                MemoryPluginResult.AppSummary value = null;
                if ((headEntry.getKey()) instanceof Long) {
                    key = (Long) headEntry.getKey();
                }
                if ((headEntry.getValue()) instanceof MemoryPluginResult.AppSummary) {
                    value = (MemoryPluginResult.AppSummary) headEntry.getValue();
                }
                if (value != null) {
                    result.put(key, value);
                }
            }
        }
        if (pst != null) {
            try {
                long startTime = startTimeStamp + min;
                long endTime = startTimeStamp + max;
                pst.setLong(1, sessionId);
                pst.setLong(LayoutConstants.TWO, startTime);
                pst.setLong(LayoutConstants.THREE, endTime);
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
                        result.put(timeStamp, appSummary);
                    }
                }
            } catch (SQLException | InvalidProtocolBufferException throwAbles) {
                throwAbles.printStackTrace();
            }
        }
        // 取最后一个点的后一个点用于Chart绘制，填充空白，解决边界闪烁
        result = getOutsideDataLinkedHashMap(sessionId, max, startTimeStamp, isNeedHeadTail, result);
        return result;
    }

    private LinkedHashMap<Long, MemoryPluginResult.AppSummary> getOutsideDataLinkedHashMap(long sessionId, int max,
        long startTimeStamp, boolean isNeedHeadTail, LinkedHashMap<Long, MemoryPluginResult.AppSummary> result) {
        LinkedHashMap<Long, MemoryPluginResult.AppSummary> resultData = result;
        if (isNeedHeadTail) {
            LinkedHashMap<Long, MemoryPluginResult.AppSummary> tail = getAfterTail(sessionId, startTimeStamp + max);
            if (tail.size() > 0) {
                Map.Entry tailEntry = tail.entrySet().iterator().next();
                MemoryPluginResult.AppSummary value = null;
                Long key = -1L;
                if ((tailEntry.getKey()) instanceof Long) {
                    key = (Long) tailEntry.getKey();
                }
                if ((tailEntry.getValue()) instanceof MemoryPluginResult.AppSummary) {
                    value = (MemoryPluginResult.AppSummary) tailEntry.getValue();
                }
                if (value != null) {
                    resultData.put(key, value);
                }
            }
        }
        return resultData;
    }

    /**
     * 获取目标时间的前一个时间的数据
     *
     * @param sessionId       缓存名称
     * @param targetTimeStamp 开始时间
     * @return LinkedHashMap
     */
    public LinkedHashMap<Long, MemoryPluginResult.AppSummary> getBeforeHead(long sessionId, long targetTimeStamp) {
        PreparedStatement pst = memorySelectMap.get(SELECT_BEFORE_HEAD);
        LinkedHashMap<Long, MemoryPluginResult.AppSummary> result = new LinkedHashMap();
        if (pst != null) {
            try {
                pst.setLong(1, sessionId);
                pst.setLong(LayoutConstants.TWO, targetTimeStamp);
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
                        result.put(timeStamp, appSummary);
                    }
                }
            } catch (SQLException | InvalidProtocolBufferException throwAbles) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
            }
        }
        return result;
    }

    /**
     * 获取目标时间的后一个时间的数据
     *
     * @param sessionId       缓存名称
     * @param targetTimeStamp 开始时间
     * @return LinkedHashMap
     */
    public LinkedHashMap<Long, MemoryPluginResult.AppSummary> getAfterTail(long sessionId, long targetTimeStamp) {
        PreparedStatement pst = memorySelectMap.get(SELECT_AFTER_TAIL);
        LinkedHashMap<Long, MemoryPluginResult.AppSummary> result = new LinkedHashMap();
        if (pst != null) {
            try {
                pst.setLong(1, sessionId);
                pst.setLong(LayoutConstants.TWO, targetTimeStamp);
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
                        result.put(timeStamp, appSummary);
                    }
                }
            } catch (SQLException | InvalidProtocolBufferException throwAbles) {
                LOGGER.error(" SQLException {}", throwAbles.getMessage());
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
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ");
        deleteSql.append("processMemInfo").append(" WHERE session = ").append(sessionId);
        Connection connection = DataBaseApi.getInstance().getConnectByTable("processMemInfo").get();
        return execute(connection, deleteSql.toString());
    }
}

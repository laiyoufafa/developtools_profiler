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

package ohos.devtools.services.memory.agentdao;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.common.util.CloseResourceUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;

/**
 * heap data processing object
 */
public class MemoryHeapDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryHeapDao.class);

    /**
     * Singleton MemoryHeapDao.
     */
    private static final MemoryHeapDao SINGLETON = new MemoryHeapDao();

    /**
     * getInstance
     *
     * @return MemoryHeapDao
     */
    public static MemoryHeapDao getInstance() {
        return MemoryHeapDao.SINGLETON;
    }

    /**
     * MemoryHeapDao
     */
    public MemoryHeapDao() {
        createMemoryHeapInfo();
    }

    /**
     * get database connection
     *
     * @param tableName tableName
     * @return Connection
     */
    private Connection getConnection(String tableName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getConnection");
        }
        Optional<Connection> optionalConnection = getConnectByTable(tableName);
        Connection conn = null;
        if (optionalConnection.isPresent()) {
            conn = optionalConnection.get();
        }
        return conn;
    }

    /**
     * heap information data table creation
     *
     * @return boolean
     */
    public boolean createMemoryHeapInfo() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createMemoryHeapInfo");
        }
        boolean createResult = false;
        String dbName = JVMTI_AGENT_PLUG;
        String memoryHeapInfoTable = "MemoryHeapInfo";
        String sql = "CREATE TABLE "
            + "MemoryHeapInfo "
            + "(cId int(100) not null, "
            + "heapId int(100) not null, "
            + "instanceId int(100) not null, "
            + "sessionId Long(100) not null, "
            + "allocations int(100) not null, "
            + "deallocations int(100) not null, "
            + "totalCount int(100) not null, "
            + "shallowSize int(100) not null, "
            + "createTime int(200) not null, "
            + "updateTime int(200) DEFAULT -1"
            + ");";
        createResult = createTable(dbName, memoryHeapInfoTable, sql);
        return createResult;
    }

    /**
     * insert Memory HeapInfos
     *
     * @param memoryHeapInfos memoryHeapInfos
     * @return boolean
     */
    public boolean insertMemoryHeapInfos(List<MemoryHeapInfo> memoryHeapInfos) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertMemoryHeapInfos");
        }
        if (memoryHeapInfos.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryHeapInfo");
            conn.setAutoCommit(false);
            String sql = "insert into "
                + "MemoryHeapInfo("
                + "cId, "
                + "heapId, "
                + "sessionId, "
                + "allocations, "
                + "deallocations, "
                + "totalCount, "
                + "shallowSize, "
                + "createTime, "
                + "instanceId) "
                + "values(?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            setPreparedStatement(memoryHeapInfos, ps);
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            ps.clearParameters();
            return true;
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("insert MemoryHeap {}", sqlException.getMessage());
            }
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    private void setPreparedStatement(List<MemoryHeapInfo> memoryHeapInfos, PreparedStatement ps) {
        for (MemoryHeapInfo memoryHeapInfo : memoryHeapInfos) {
            try {
                ps.setInt(1, memoryHeapInfo.getcId());
                ps.setInt(2, memoryHeapInfo.getHeapId());
                ps.setLong(3, memoryHeapInfo.getSessionId());
                ps.setLong(4, memoryHeapInfo.getAllocations());
                ps.setLong(5, memoryHeapInfo.getDeallocations());
                ps.setInt(6, memoryHeapInfo.getTotalCount());
                ps.setLong(7, memoryHeapInfo.getShallowSize());
                ps.setLong(8, memoryHeapInfo.getCreateTime());
                ps.setLong(9, memoryHeapInfo.getInstanceId());
                ps.addBatch();
            } catch (SQLException sqlException) {
                LOGGER.info("insert AppInfo {}", sqlException.getMessage());
            }
        }
    }

    /**
     * get All MemoryHeapInfos by sessionId
     *
     * @param sessionId sessionId
     * @return ArrayList <MemoryHeapInfo>
     */
    public ArrayList<MemoryHeapInfo> getAllMemoryHeapInfos(Long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllMemoryHeapInfos");
        }
        Connection conn = getConnection("MemoryHeapInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();
        try {
            String sql =
                "select "
                    + "cId, "
                    + "heapId, "
                    + "sessionId, "
                    + "allocations, "
                    + "deallocations, "
                    + "totalCount, "
                    + "shallowSize, "
                    + "createTime, "
                    + "instanceId, "
                    + "updateTime "
                    + "from "
                    + "MemoryHeapInfo "
                    + "where "
                    + "sessionId = ?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, sessionId);
            ResultSet rs = ps.executeQuery();
            MemoryHeapInfo memoryHeapInfo = null;
            getMemoryHeapData(sessionId, memoryHeapInfos, rs);
            ps.clearParameters();
            return memoryHeapInfos;
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", sqlException.getMessage());
            }
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryHeapInfos;
    }

    private void getMemoryHeapData(Long sessionId, ArrayList<MemoryHeapInfo> memoryHeapInfos, ResultSet rs)
        throws SQLException {
        MemoryHeapInfo memoryHeapInfo;
        while (rs.next()) {
            memoryHeapInfo = new MemoryHeapInfo();
            Integer cId = rs.getInt("cId");
            Integer heapId = rs.getInt("heapId");
            Long msessionId = rs.getLong("sessionId");
            Integer allocations = rs.getInt("allocations");
            Integer deallocations = rs.getInt("deallocations");
            Integer totalCount = rs.getInt("totalCount");
            Long shallowSize = rs.getLong("shallowSize");
            Long createTime = rs.getLong("createTime");
            Integer instanceId = rs.getInt("instanceId");
            Long updateTime = rs.getLong("updateTime");
            memoryHeapInfo.setHeapId(heapId);
            memoryHeapInfo.setcId(cId);
            memoryHeapInfo.setSessionId(msessionId);
            memoryHeapInfo.setSessionId(sessionId);
            memoryHeapInfo.setAllocations(allocations);
            memoryHeapInfo.setDeallocations(deallocations);
            memoryHeapInfo.setTotalCount(totalCount);
            memoryHeapInfo.setShallowSize(shallowSize);
            memoryHeapInfo.setCreateTime(createTime);
            memoryHeapInfo.setInstanceId(instanceId);
            memoryHeapInfo.setUpdateTime(updateTime);
            memoryHeapInfos.add(memoryHeapInfo);
        }
    }

    /**
     * get MemoryHeapInfos
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param endTime endTime
     * @return ArrayList <MemoryHeapInfo>
     */
    public List<AgentHeapBean> getMemoryHeapInfos(Long sessionId, Long startTime, Long endTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getMemoryHeapInfos");
        }
        Connection conn = getConnection("MemoryHeapInfo");
        PreparedStatement ps = null;
        List<AgentHeapBean> memoryHeapInfos = new ArrayList<>();
        try {
            String sql = getSql();
            ps = conn.prepareStatement(sql);
            setPreparedStatementData(sessionId, startTime, endTime, ps);
            ResultSet rs = ps.executeQuery();
            AgentHeapBean memoryHeapInfo = null;
            while (rs.next()) {
                memoryHeapInfo = new AgentHeapBean();
                Integer cId = rs.getInt("cId");
                String className = rs.getString("className");
                Integer allocations = rs.getInt("allocations");
                Integer deallocations = rs.getInt("deallocations");
                Integer totalCount = rs.getInt("totalCount");
                Long shallowSize = rs.getLong("shallowSize");
                memoryHeapInfo.setAgentClazzId(cId);
                memoryHeapInfo.setAgentHeapId(0);
                memoryHeapInfo.setSessionId(sessionId);
                memoryHeapInfo.setAgentClazzName(className);
                memoryHeapInfo.setAgentAllocationsCount(allocations - deallocations);
                memoryHeapInfo.setAgentDeAllocationsCount(deallocations);
                memoryHeapInfo.setAgentTotalInstanceCount(totalCount);
                memoryHeapInfo.setAgentTotalshallowSize(shallowSize);
                memoryHeapInfos.add(memoryHeapInfo);
            }
            ps.clearParameters();
            return memoryHeapInfos;
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", sqlException.getMessage());
            }
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryHeapInfos;
    }

    /**
     * setPreparedStatementData
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param endTime endTime
     * @param ps ps
     * @throws SQLException
     */
    private void setPreparedStatementData(Long sessionId, Long startTime, Long endTime, PreparedStatement ps)
        throws SQLException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setPreparedStatementData");
        }
        ps.setLong(1, sessionId);
        ps.setLong(2, startTime);
        ps.setLong(3, endTime);
        ps.setLong(4, startTime);
        ps.setLong(5, endTime);
        ps.setLong(6, sessionId);
        ps.setLong(7, startTime);
        ps.setLong(8, endTime);
        ps.setLong(9, sessionId);
        ps.setLong(10, startTime);
        ps.setLong(11, endTime);
        ps.setLong(12, startTime);
        ps.setLong(13, endTime);
        ps.setLong(14, sessionId);
        ps.setLong(15, startTime);
        ps.setLong(16, endTime);
        ps.setLong(17, sessionId);
        ps.setLong(18, startTime);
        ps.setLong(19, endTime);
        ps.setLong(20, startTime);
        ps.setLong(21, endTime);
        ps.setLong(22, sessionId);
        ps.setLong(23, endTime);
    }

    /**
     * delete by SessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSessionData");
        }
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ");
        deleteSql.append("MemoryHeapInfo").append(" WHERE sessionId = ").append(sessionId);
        Optional<Connection> memoryHeapInfo = DataBaseApi.getInstance().getConnectByTable("MemoryHeapInfo");
        if (memoryHeapInfo.isPresent()) {
            return execute(memoryHeapInfo.get(), deleteSql.toString());
        }
        return false;
    }

    /**
     * updateMemoryHeapInfoList
     *
     * @param memoryUpdateInfos memoryUpdateInfos
     * @return boolean
     */
    public boolean updateMemoryHeapInfoList(List<MemoryUpdateInfo> memoryUpdateInfos) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateMemoryHeapInfoList");
        }
        if (memoryUpdateInfos.isEmpty()) {
            return true;
        }
        Connection conn = getConnection("MemoryHeapInfo");
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(
                "UPDATE "
                    + "MemoryHeapInfo "
                    + "SET "
                    + "deallocations = 1, "
                    + "updateTime = ?  "
                    + "where "
                    + "instanceId = ? ");
            for (MemoryUpdateInfo memoryUpdateInfo : memoryUpdateInfos) {
                ps.setLong(1, memoryUpdateInfo.getUpdateTime());
                ps.setLong(2, memoryUpdateInfo.getInstanceId());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            ps.clearParameters();
            return true;
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", sqlException.getMessage());
            }
            return false;
        } finally {
            close(ps, conn);
        }
    }

    @NotNull
    private String getSql() {
        String str1 = "SELECT d.cid,d.className,IFNULL( d.allocations, 0 ) AS allocations,"
            + "IFNULL( d.deallocations, 0 ) AS deallocations,IFNULL( f.totalCount, 0 ) AS totalCount,"
            + "IFNULL( f.shallowSize, 0 ) AS shallowSize "
            + "FROM ((SELECT c.cId,c.className,sum( IFNULL( heaptable.allocations, 0 ) ) AS allocations,"
            + "sum( IFNULL( heaptable.deallocations, 0 ) ) AS deallocations FROM ClassInfo c LEFT JOIN "
            + "(SELECT m.cId,m.heapId,m.instanceId,m.sessionId,m.allocations,m.totalCount,m.shallowSize,m.createTime,"
            + "instance.deallocTime,1 AS deallocations FROM MemoryHeapInfo m LEFT JOIN MemoryInstanceInfo instance ON"
            + " instance.instanceId = m.instanceId WHERE m.sessionId = ? "
            + "AND (( m.createTime >= ? AND m.createTime <= ? ) "
            + "AND ( instance.deallocTime >= ? AND instance.deallocTime <= ? ) ) UNION "
            + "SELECT m.cId,m.heapId,m.instanceId,m.sessionId,m.allocations,m.totalCount,m.shallowSize,m.createTime,"
            + "IFNULL( instance.deallocTime, 0 ) AS deallocTime,0 AS deallocations FROM MemoryHeapInfo m LEFT JOIN "
            + "MemoryInstanceInfo instance ON instance.instanceId = m.instanceId WHERE m.sessionId = ? "
            + "AND ( ( m.createTime >= ? AND m.createTime <= ? ) ) AND m.instanceId NOT IN (SELECT m.instanceId "
            + "FROM MemoryHeapInfo m LEFT JOIN MemoryInstanceInfo instance ON instance.instanceId = m.instanceId "
            + "WHERE m.sessionId = ? AND (( m.createTime >= ? AND m.createTime <= ? ) AND ( instance.deallocTime >= ? "
            + "AND instance.deallocTime <= ? ) ) ) UNION SELECT m.cId,m.heapId,m.instanceId,m.sessionId,"
            + "0 AS allocations,m.totalCount,m.shallowSize,m.createTime,"
            + "IFNULL( instance.deallocTime, 0 ) AS deallocTime,1 AS deallocations FROM MemoryHeapInfo m "
            + "LEFT JOIN MemoryInstanceInfo instance ON instance.instanceId = m.instanceId "
            + "WHERE m.sessionId = ? AND ( ( instance.deallocTime >= ? AND instance.deallocTime <= ? ) ) "
            + "AND m.instanceId NOT IN (SELECT m.instanceId "
            + "FROM MemoryHeapInfo m LEFT JOIN MemoryInstanceInfo instance "
            + "ON instance.instanceId = m.instanceId WHERE m.sessionId = ? "
            + "AND (( m.createTime >= ? AND m.createTime <= ? ) AND ( instance.deallocTime >= ? "
            + "AND instance.deallocTime <= ? ) ) ) ) AS heaptable ON c.cId = heaptable.cId GROUP BY c.cId ) "
            + "AS d LEFT JOIN (SELECT e.cId AS cId,sum( IFNULL( e.totalCount, 0 ) ) AS totalCount,"
            + "sum( IFNULL( e.shallowSize, 0 ) ) AS shallowSize FROM ClassInfo l LEFT JOIN MemoryHeapInfo e "
            + "ON e.cId = l.cId WHERE e.sessionId = ? AND e.createTime <= ? GROUP BY l.cId ) AS f ON d.cId = f.cId) "
            + "ORDER BY shallowSize DESC";
        return str1;
    }
}

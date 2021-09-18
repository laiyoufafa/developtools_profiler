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
import ohos.devtools.services.memory.agentbean.MemoryInstanceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;

/**
 * instance data processing object
 */
public class MemoryInstanceDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryInstanceDao.class);
    private static volatile MemoryInstanceDao singleton;

    /**
     * get Instance
     *
     * @return MemoryInstanceDao
     */
    public static MemoryInstanceDao getInstance() {
        if (singleton == null) {
            synchronized (MemoryInstanceDao.class) {
                if (singleton == null) {
                    singleton = new MemoryInstanceDao();
                }
            }
        }
        return singleton;
    }

    /**
     * MemoryInstanceDao
     */
    public MemoryInstanceDao() {
        createMemoryInstance();
    }

    /**
     * get database connection
     *
     * @param tableName tableName
     * @return Connection
     */
    private Connection getConnection(String tableName) {
        Optional<Connection> optionalConnection = getConnectByTable(tableName);
        Connection conn = null;
        if (optionalConnection.isPresent()) {
            conn = optionalConnection.get();
        }
        return conn;
    }

    /**
     * INSTANCE OBJECT TABLE CREATION
     *
     * @return boolean
     */
    public boolean createMemoryInstance() {
        String dbName = JVMTI_AGENT_PLUG;
        String memoryInstanceInfoTable = "MemoryInstanceInfo";
        String sql = "CREATE TABLE MemoryInstanceInfo (instanceId  int(100) not null, deallocTime int(100));";
        return createTable(dbName, memoryInstanceInfoTable, sql);
    }

    /**
     * Save the instance information obtained from the end-side in the database
     *
     * @param memoryUpdateInfo memoryUpdateInfo
     */
    public void insertMemoryInstanceInfo(MemoryUpdateInfo memoryUpdateInfo) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryInstanceInfo");
            String sql = "insert into MemoryInstanceInfo(instanceId,deallocTime) values (?,?)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, memoryUpdateInfo.getInstanceId());
            ps.setLong(2, memoryUpdateInfo.getUpdateTime());
            ps.executeUpdate();
        } catch (SQLException throwAbles) {
            LOGGER.error("SQLException error: " + throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * insert MemoryInstanceInfos
     *
     * @param memoryUpdateInfos memoryInstanceInfos
     * @return boolean
     */
    public boolean insertMemoryInstanceInfos(List<MemoryUpdateInfo> memoryUpdateInfos) {
        if (memoryUpdateInfos.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryInstanceInfo");
            conn.setAutoCommit(false);
            String sql = "insert into MemoryInstanceInfo(instanceId,deallocTime) values (?,?)";
            ps = conn.prepareStatement(sql);
            for (MemoryUpdateInfo memoryInstanceInfo : memoryUpdateInfos) {
                try {
                    ps.setLong(1, memoryInstanceInfo.getInstanceId());
                    ps.setLong(2, memoryInstanceInfo.getUpdateTime());
                    ps.addBatch();
                } catch (SQLException sqlException) {
                    LOGGER.info("insert AppInfo {}", sqlException.getMessage());
                }
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            ps.clearParameters();
            return true;
        } catch (SQLException throwables) {
            LOGGER.error("SQLException error: " + throwables.getMessage());
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * get MemoryInstanceInfos by cId
     *
     * @param cId cId
     * @param startTime startTime
     * @param endTime endTime
     * @return ArrayList<MemoryInstanceInfo>
     */
    public ArrayList<MemoryInstanceInfo> getMemoryInstanceInfos(Integer cId, Long startTime, Long endTime) {
        Connection conn = getConnection("MemoryHeapInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryInstanceInfo> memoryInstanceInfos = new ArrayList<>();
        try {
            String sql = "select memory.instanceId," + "memory.createTime," + "memory.cid,"
                + "IFNULL( instance.deallocTime , 0 ) AS deallocTime" + " from MemoryHeapInfo as memory "
                + "LEFT JOIN MemoryInstanceInfo AS instance "
                + "ON instance.instanceId = memory.instanceId "
                + "WHERE  memory.cid = ? and ((createTime >= ? and createTime <= ?) or (deallocTime  >= ? "
                + "and deallocTime  <= ?))";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cId);
            ps.setLong(2, startTime);
            ps.setLong(3, endTime);
            ps.setLong(4, startTime);
            ps.setLong(5, endTime);
            ResultSet rs = ps.executeQuery();
            MemoryInstanceInfo memoryInstanceInfo = null;
            while (rs.next()) {
                memoryInstanceInfo = new MemoryInstanceInfo();
                Integer instanceId = rs.getInt("instanceId");
                Long allocTime = rs.getLong("createTime");
                Long deallocTime = rs.getLong("deallocTime");
                memoryInstanceInfo.setInstanceId(instanceId);
                memoryInstanceInfo.setcId(cId);
                memoryInstanceInfo.setAllocTime(allocTime);
                memoryInstanceInfo.setDeallocTime(deallocTime);
                memoryInstanceInfo.setCreateTime(allocTime);
                memoryInstanceInfos.add(memoryInstanceInfo);
            }
            ps.clearParameters();
            return memoryInstanceInfos;
        } catch (SQLException throwAbles) {
            LOGGER.error(throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryInstanceInfos;
    }

    /**
     * get All Memory InstanceInfos
     *
     * @return ArrayList<MemoryUpdateInfo>
     */
    public ArrayList<MemoryUpdateInfo> getAllMemoryInstanceInfos() {
        Connection conn = getConnection("MemoryInstanceInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryUpdateInfo> memoryInstanceInfos = new ArrayList<>();
        try {
            String sql = "select instanceId,deallocTime from MemoryInstanceInfo";
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            MemoryUpdateInfo memoryInstanceInfo = null;
            while (rs.next()) {
                Integer instanceId = rs.getInt("instanceId");
                Long deallocTime = rs.getLong("deallocTime");
                memoryInstanceInfo = new MemoryUpdateInfo(instanceId, deallocTime);
                memoryInstanceInfos.add(memoryInstanceInfo);
            }
            ps.clearParameters();
            return memoryInstanceInfos;
        } catch (SQLException throwAbles) {
            LOGGER.error(throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryInstanceInfos;
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        StringBuffer deleteSql = new StringBuffer("DELETE FROM MemoryInstanceInfo");
        Optional<Connection> connection = DataBaseApi.getInstance().getConnectByTable("MemoryInstanceInfo");
        if (connection.isPresent()) {
            return execute(connection.get(), deleteSql.toString());
        }
        return true;
    }

}

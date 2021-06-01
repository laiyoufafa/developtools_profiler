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

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.common.util.CloseResourceUtil;
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
import static ohos.devtools.services.memory.Option.SQL_INDEX_TWO;

/**
 * 实例数据处理对象
 *
 * @version 1.0
 * @date 2021/03/30 18:47
 **/
public class MemoryInstanceDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryInstanceDao.class);
    private static volatile MemoryInstanceDao singleton;

    /**
     * 获取单例对象
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
     * 获取数据库连接
     *
     * @param tableName 表名
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
     * 实例对象表创建
     *
     * @return boolean
     */
    public boolean createMemoryInstance() {
        String dbName = JVMTI_AGENT_PLUG;
        String memoryInstanceInfoTable = "MemoryInstanceInfo";
        String sql = "CREATE TABLE MemoryInstanceInfo " + "( "
            + "    id             Integer primary key autoincrement not null,   "
            + "    instanceId         int(100) not null, " + "    cId            int(100) not null, "
            + "    instance       varchar(200), " + "    createTime     Long(200), " + "    allocTime      int(100), "
            + "    deallocTime    int(100) " + ");";
        return createTable(dbName, memoryInstanceInfoTable, sql);
    }

    /**
     * 将端侧获取的实例信息保存在数据库
     *
     * @param memoryInstanceInfo memoryInstanceInfo
     */
    public void insertMemoryInstanceInfo(MemoryInstanceInfo memoryInstanceInfo) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryInstanceInfo");
            String sql =
                "insert into MemoryInstanceInfo(id,instanceId,cId,instance,createTime,allocTime,deallocTime) values"
                    + "(null,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, memoryInstanceInfo.getInstanceId());
            ps.setInt(SQL_INDEX_TWO, memoryInstanceInfo.getcId());
            ps.setString(Option.SQL_INDEX_THREE, memoryInstanceInfo.getInstance());
            ps.setLong(Option.SQL_INDEX_FOUR, memoryInstanceInfo.getCreateTime());
            ps.setLong(Option.SQL_INDEX_FIVE, memoryInstanceInfo.getAllocTime());
            ps.setLong(Option.SQL_INDEX_SIX, memoryInstanceInfo.getDeallocTime());
            ps.executeUpdate();
        } catch (SQLException throwAbles) {
            LOGGER.error("SQLException error: " + throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * insertMemoryInstanceInfos
     *
     * @param memoryInstanceInfos memoryInstanceInfos
     * @return boolean
     */
    public boolean insertMemoryInstanceInfos(List<MemoryInstanceInfo> memoryInstanceInfos) {
        if (memoryInstanceInfos.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryInstanceInfo");
            conn.setAutoCommit(false);
            String sql =
                "insert into MemoryInstanceInfo(id,instanceId,cId,instance,createTime,allocTime,deallocTime) values"
                    + "(null,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            for (MemoryInstanceInfo memoryInstanceInfo : memoryInstanceInfos) {
                try {
                    ps.setInt(1, memoryInstanceInfo.getInstanceId());
                    ps.setInt(SQL_INDEX_TWO, memoryInstanceInfo.getcId());
                    ps.setString(Option.SQL_INDEX_THREE, memoryInstanceInfo.getInstance());
                    ps.setLong(Option.SQL_INDEX_FOUR, memoryInstanceInfo.getCreateTime());
                    ps.setLong(Option.SQL_INDEX_FIVE, memoryInstanceInfo.getAllocTime());
                    ps.setLong(Option.SQL_INDEX_SIX, memoryInstanceInfo.getDeallocTime());
                    ps.addBatch();
                } catch (SQLException sqlException) {
                    LOGGER.info("insert AppInfo {}", sqlException.getMessage());
                }
            }
            int[] results = ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException throwables) {
            LOGGER.error("SQLException error: " + throwables.getMessage());
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * 根据该实例的父Id(对应的类对象hId)，从数据库获取具体的实例信息
     *
     * @param cId       父cd
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return ArrayList<MemoryInstanceInfo>
     */
    public ArrayList<MemoryInstanceInfo> getMemoryInstanceInfos(Integer cId, Long startTime, Long endTime) {
        Connection conn = getConnection("MemoryInstanceInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryInstanceInfo> memoryInstanceInfos = new ArrayList<>();
        try {
            String sql = "select * from MemoryInstanceInfo where cId = ? and createTime >= ? and createTime <= ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cId);
            ps.setLong(SQL_INDEX_TWO, startTime);
            ps.setLong(Option.SQL_INDEX_THREE, endTime);

            ResultSet rs = ps.executeQuery();
            MemoryInstanceInfo memoryInstanceInfo = null;
            while (rs.next()) {
                memoryInstanceInfo = new MemoryInstanceInfo();
                Integer id = rs.getInt("id");
                Integer instanceId = rs.getInt("instanceId");
                String instance = rs.getString("instance");
                Long allocTime = rs.getLong("allocTime");
                Long deallocTime = rs.getLong("deallocTime");
                Long createTime = rs.getLong("createTime");
                memoryInstanceInfo.setId(id);
                memoryInstanceInfo.setInstanceId(instanceId);
                memoryInstanceInfo.setcId(cId);
                memoryInstanceInfo.setInstance(instance);
                memoryInstanceInfo.setAllocTime(allocTime);
                memoryInstanceInfo.setDeallocTime(deallocTime);
                memoryInstanceInfo.setCreateTime(createTime);
                memoryInstanceInfos.add(memoryInstanceInfo);
            }
            return memoryInstanceInfos;
        } catch (SQLException throwAbles) {
            LOGGER.error(throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryInstanceInfos;
    }

    /**
     * 从数据库获取全部的实例信息
     *
     * @return ArrayList<MemoryInstanceInfo>
     */
    public ArrayList<MemoryInstanceInfo> getAllMemoryInstanceInfos() {
        Connection conn = getConnection("MemoryInstanceInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryInstanceInfo> memoryInstanceInfos = new ArrayList<>();
        try {
            String sql = "select id,instanceId,cId,instance,createTime,allocTime,deallocTime from MemoryInstanceInfo";
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            MemoryInstanceInfo memoryInstanceInfo = null;
            while (rs.next()) {
                memoryInstanceInfo = new MemoryInstanceInfo();
                Integer id = rs.getInt("id");
                Integer instanceId = rs.getInt("instanceId");
                Integer cId = rs.getInt("cId");
                String instance = rs.getString("instance");
                Long allocTime = rs.getLong("allocTime");
                Long deallocTime = rs.getLong("deallocTime");
                Long createTime = rs.getLong("createTime");
                memoryInstanceInfo.setId(id);
                memoryInstanceInfo.setInstanceId(instanceId);
                memoryInstanceInfo.setcId(cId);
                memoryInstanceInfo.setInstance(instance);
                memoryInstanceInfo.setAllocTime(allocTime);
                memoryInstanceInfo.setDeallocTime(deallocTime);
                memoryInstanceInfo.setCreateTime(createTime);
                memoryInstanceInfos.add(memoryInstanceInfo);
            }
            return memoryInstanceInfos;
        } catch (SQLException throwAbles) {
            LOGGER.error(throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryInstanceInfos;
    }

    /**
     * updateInstanceInfos
     *
     * @param timeStamp  timeStamp
     * @param instanceId instanceId
     * @return boolean
     */
    public boolean updateInstanceInfos(Long timeStamp, int instanceId) {
        Connection conn = getConnection("MemoryInstanceInfo");
        String updateSql = "UPDATE MemoryInstanceInfo SET deallocTime = ? where instanceId = ?";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(updateSql);
            preparedStatement.setLong(1, timeStamp);
            preparedStatement.setInt(SQL_INDEX_TWO, instanceId);
            return preparedStatement.executeUpdate() == 1 ? true : false;
        } catch (SQLException throwables) {
            LOGGER.error("SQLException {}", throwables.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, preparedStatement, null);
        }
        return false;
    }

    /**
     * 删除一次场景下的堆信息数据
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        StringBuffer deleteSql = new StringBuffer("DELETE FROM MemoryInstanceInfo");
        Connection connection = DataBaseApi.getInstance().getConnectByTable("MemoryInstanceInfo").get();
        return execute(connection, deleteSql.toString());
    }

}

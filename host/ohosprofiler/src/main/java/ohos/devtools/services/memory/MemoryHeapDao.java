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

/**
 * heap数据处理对象
 *
 * @version 1.0
 * @date 2021/03/30 10:52
 **/
public class MemoryHeapDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryHeapDao.class);
    private static volatile MemoryHeapDao singleton;

    /**
     * getInstance
     *
     * @return MemoryHeapDao
     */
    public static MemoryHeapDao getInstance() {
        if (singleton == null) {
            synchronized (MemoryHeapDao.class) {
                if (singleton == null) {
                    singleton = new MemoryHeapDao();
                }
            }
        }
        return singleton;
    }

    /**
     * MemoryHeapDao
     */
    public MemoryHeapDao() {
        createMemoryHeapInfo();
    }

    /**
     * 获取数据库连接
     *
     * @param tableName 表名
     * @return Connection
     * @date 2021/03/30 11:00
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
     * 堆信息数据表创建
     *
     * @return boolean
     */
    public boolean createMemoryHeapInfo() {
        boolean createResult = false;
        String dbName = JVMTI_AGENT_PLUG;
        String memoryHeapInfoTable = "MemoryHeapInfo";
        String sql =
            "CREATE TABLE MemoryHeapInfo " + "( " + "    id             Integer primary key autoincrement not null, "
                + "    cId            int(100) not null, " + "    heapId         int(100) not null, "
                + "    instanceId     int(100) not null, " + "    sessionId      Long(100) not null, "
                + "    arrangeStyle   varchar(200), " + "    allocations    int(100) not null, "
                + "    deallocations  int(100) not null, " + "    totalCount     int(100) not null, "
                + "    shallowSize    int(100) not null, " + "    createTime     int(200) not null " + ");";

        createResult = createTable(dbName, memoryHeapInfoTable, sql);
        return createResult;
    }

    /**
     * 将端侧获取的堆信息保存在数据库
     *
     * @param memoryHeapInfos 堆实例
     * @return boolean
     */
    public boolean insertMemoryHeapInfos(List<MemoryHeapInfo> memoryHeapInfos) {
        if (memoryHeapInfos.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryHeapInfo");
            conn.setAutoCommit(false);
            String sql = "insert into MemoryHeapInfo(id,cId,heapId,sessionId,arrangeStyle,allocations,"
                + "deallocations,totalCount,shallowSize,createTime,instanceId)values(null,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            for (MemoryHeapInfo memoryHeapInfo : memoryHeapInfos) {
                try {
                    ps.setInt(1, memoryHeapInfo.getcId());
                    ps.setInt(Option.SQL_INDEX_TWO, memoryHeapInfo.getHeapId());
                    ps.setLong(Option.SQL_INDEX_THREE, memoryHeapInfo.getSessionId());
                    ps.setString(Option.SQL_INDEX_FOUR, memoryHeapInfo.getArrangeStyle());
                    ps.setLong(Option.SQL_INDEX_FIVE, memoryHeapInfo.getAllocations());
                    ps.setLong(Option.SQL_INDEX_SIX, memoryHeapInfo.getDeallocations());
                    ps.setInt(Option.SQL_INDEX_SEVEN, memoryHeapInfo.getTotalCount());
                    ps.setLong(Option.SQL_INDEX_EIGHT, memoryHeapInfo.getShallowSize());
                    ps.setLong(Option.SQL_INDEX_NINE, memoryHeapInfo.getCreateTime());
                    ps.setLong(Option.SQL_INDEX_TEN, memoryHeapInfo.getInstanceId());
                    ps.addBatch();
                } catch (SQLException sqlException) {
                    LOGGER.info("insert AppInfo {}", sqlException.getMessage());
                }
            }
            int[] results = ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException throwables) {
            LOGGER.info("insert MemoryHeap {}", throwables.getMessage());
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * 获取所有的SessionId数据
     *
     * @param sessionId sessionId
     * @return ArrayList<MemoryHeapInfo>
     */
    public ArrayList<MemoryHeapInfo> getAllMemoryHeapInfos(Long sessionId) {
        Connection conn = getConnection("MemoryHeapInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();
        try {
            String sql = "select id,cId,heapId,sessionId,arrangeStyle,allocations,deallocations,totalCount,"
                + "shallowSize,createTime,instanceId from MemoryHeapInfo where sessionId = ?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, sessionId);
            ResultSet rs = ps.executeQuery();
            MemoryHeapInfo memoryHeapInfo = null;
            while (rs.next()) {
                memoryHeapInfo = new MemoryHeapInfo();
                Integer id = rs.getInt("id");
                Integer cId = rs.getInt("cId");
                Integer heapId = rs.getInt("heapId");
                Long msessionId = rs.getLong("sessionId");
                Integer allocations = rs.getInt("allocations");
                Integer deallocations = rs.getInt("deallocations");
                Integer totalCount = rs.getInt("totalCount");
                Long shallowSize = rs.getLong("shallowSize");
                Long createTime = rs.getLong("createTime");
                Integer instanceId = rs.getInt("instanceId");
                memoryHeapInfo.setId(id);
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
                memoryHeapInfos.add(memoryHeapInfo);
            }
            return memoryHeapInfos;
        } catch (SQLException throwables) {
            LOGGER.info("memoryHeapInfo Exception {}", throwables.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryHeapInfos;
    }

    /**
     * 从数据库获取堆信息数据
     *
     * @param sessionId sessionId
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return ArrayList<MemoryHeapInfo>
     * @date 2021/3/31 16:04
     */
    public ArrayList<MemoryHeapInfo> getMemoryHeapInfos(Long sessionId, Long startTime, Long endTime) {
        Connection conn = getConnection("MemoryHeapInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();
        try {
            String sql = getSqlStr();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, sessionId);
            ps.setLong(Option.SQL_INDEX_TWO, startTime);
            ps.setLong(Option.SQL_INDEX_THREE, endTime);
            ps.setLong(Option.SQL_INDEX_FOUR, sessionId);
            ps.setLong(Option.SQL_INDEX_FIVE, startTime);
            ps.setLong(Option.SQL_INDEX_SIX, endTime);
            ps.setLong(Option.SQL_INDEX_SEVEN, sessionId);
            ps.setLong(Option.SQL_INDEX_EIGHT, endTime);

            ResultSet rs = ps.executeQuery();
            MemoryHeapInfo memoryHeapInfo = null;
            while (rs.next()) {
                memoryHeapInfo = new MemoryHeapInfo();
                Integer id = rs.getInt("id");
                Integer cId = rs.getInt("cId");
                String className = rs.getString("className");
                Integer allocations = rs.getInt("allocations");
                Integer deallocations = rs.getInt("deallocations");
                Integer totalCount = rs.getInt("totalCount");
                Long shallowSize = rs.getLong("shallowSize");
                memoryHeapInfo.setId(id);
                memoryHeapInfo.setcId(cId);
                memoryHeapInfo.setHeapId(0);
                memoryHeapInfo.setSessionId(sessionId);
                memoryHeapInfo.setClassName(className);
                memoryHeapInfo.setAllocations(allocations);
                memoryHeapInfo.setDeallocations(deallocations);
                memoryHeapInfo.setTotalCount(totalCount);
                memoryHeapInfo.setShallowSize(shallowSize);
                memoryHeapInfos.add(memoryHeapInfo);
            }
            return memoryHeapInfos;
        } catch (SQLException throwAbles) {
            LOGGER.info("memoryHeapInfo Exception {}", throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryHeapInfos;
    }

    /**
     * 删除一次场景下的堆信息数据
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ");
        deleteSql.append("MemoryHeapInfo").append(" WHERE sessionId = ").append(sessionId);
        Connection connection = DataBaseApi.getInstance().getConnectByTable("MemoryHeapInfo").get();
        return execute(connection, deleteSql.toString());
    }

    /**
     * updateMemoryHeapInfo
     *
     * @param instanceId instanceId
     * @return boolean
     */
    public boolean updateMemoryHeapInfo(int instanceId) {
        Connection conn = getConnection("MemoryHeapInfo");
        String updateSql = "UPDATE MemoryHeapInfo SET deallocations = 1 where instanceId = ?";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(updateSql);
            preparedStatement.setLong(1, instanceId);
            return preparedStatement.executeUpdate() == 1 ? true : false;
        } catch (SQLException throwables) {
            LOGGER.error("SQLException {}", throwables.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, preparedStatement, null);
        }
        return false;
    }

    private String getSqlStr() {
        String sql = "SELECT d.id,d.cid,d.className,IFNULL(d.allocations, 0) as allocations, "
            + "IFNULL(d.deallocations, 0) as deallocations ,IFNULL(f.totalCount, 0) as totalCount , "
            + "IFNULL(f.shallowSize, 0) as shallowSize FROM(("
            + "SELECT c.id, c.cId, c.className,sum( IFNULL( m.allocations, 0 ) ) AS allocations,"
            + "sum( IFNULL( m.deallocations, 0 ) ) AS deallocations FROM ClassInfo c "
            + "LEFT JOIN MemoryHeapInfo m ON m.cId = c.cId WHERE m.sessionId = ? "
            + "AND m.createTime >= ? AND m.createTime <= ? GROUP BY c.cId"
            + " UNION  SELECT c.id,c.cId,c.className,0 AS allocations,0 AS deallocations FROM ClassInfo c "
            + "WHERE c.cId NOT IN ( SELECT b.cId FROM MemoryHeapInfo b WHERE b.sessionId = ? "
            + "AND b.createTime >= ? AND b.createTime <= ? ) ) AS d LEFT JOIN("
            + "SELECT e.cId as cId,sum( IFNULL( e.totalCount, 0 ) ) AS totalCount,"
            + "sum( IFNULL( e.shallowSize, 0 )) AS shallowSize FROM ClassInfo l LEFT JOIN MemoryHeapInfo e"
            + " ON e.cId = l.cId WHERE e.sessionId = ? AND e.createTime <= ? "
            + "GROUP BY l.cId ) AS f on d.cId = f.cId) ORDER BY shallowSize DESC";

        return sql;
    }

}

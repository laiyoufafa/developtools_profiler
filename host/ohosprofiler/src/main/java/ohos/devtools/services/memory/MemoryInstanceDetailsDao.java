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
 * 实例详情数据处理对象
 *
 * @version 1.0
 * @date 2021/03/30 18:50
 **/
public class MemoryInstanceDetailsDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryInstanceDetailsDao.class);

    private static volatile MemoryInstanceDetailsDao singleton;

    /**
     * getInstance
     *
     * @return MemoryInstanceDetailsDao
     */
    public static MemoryInstanceDetailsDao getInstance() {
        if (singleton == null) {
            synchronized (MemoryInstanceDetailsDao.class) {
                if (singleton == null) {
                    singleton = new MemoryInstanceDetailsDao();
                }
            }
        }
        return singleton;
    }

    /**
     * MemoryInstanceDetailsDao
     */
    public MemoryInstanceDetailsDao() {
        createMemoryInstanceDetails();
    }

    /**
     * 获取数据库连接
     *
     * @param tableName 表名
     * @return Connection
     * @date 2021/03/30 18:49
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
     * 具体实例对象信息详情表的创建
     *
     * @return boolean
     */
    public boolean createMemoryInstanceDetails() {
        String dbName = JVMTI_AGENT_PLUG;
        String memoryInstanceDetailsInfoTable = "MemoryInstanceDetailsInfo";
        String sql = "CREATE TABLE MemoryInstanceDetailsInfo " + "( "
            + "    id             Integer primary key autoincrement not null, "
            + "    instanceId     int(100) not null, " + "    frameId        int(100) not null, "
            + "    className      varchar(200) not null, " + "    methodName     varchar(200) not null, "
            + "    fieldName      varchar(200) not null, " + "    lineNumber     int(100)    " + ");";
        return createTable(dbName, memoryInstanceDetailsInfoTable, sql);
    }

    /**
     * 将端侧获取的实例对象对应的调用栈信息保存在数据库
     *
     * @param memoryInstanceDetailsInfo memoryInstanceDetailsInfo
     */
    public void insertMemoryInstanceDetailsInfo(MemoryInstanceDetailsInfo memoryInstanceDetailsInfo) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryInstanceDetailsInfo");
            String sql = "insert into MemoryInstanceDetailsInfo(id,instanceId,frameId,className,methodName,fieldName,"
                + "lineNumber) values(null,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, memoryInstanceDetailsInfo.getInstanceId());
            ps.setInt(Option.SQL_INDEX_TWO, memoryInstanceDetailsInfo.getFrameId());
            ps.setString(Option.SQL_INDEX_THREE, memoryInstanceDetailsInfo.getClassName());
            ps.setString(Option.SQL_INDEX_FOUR, memoryInstanceDetailsInfo.getMethodName());
            ps.setString(Option.SQL_INDEX_FIVE, memoryInstanceDetailsInfo.getFieldName());
            ps.setInt(Option.SQL_INDEX_SIX, memoryInstanceDetailsInfo.getLineNumber());
            ps.executeUpdate();
        } catch (SQLException throwAbles) {
            LOGGER.error("insert Exception {}", throwAbles.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * insertMemoryInstanceDetailsInfo
     *
     * @param memoryInstanceDetailsInfos memoryInstanceDetailsInfos
     * @return boolean
     */
    public boolean insertMemoryInstanceDetailsInfo(List<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos) {
        if (memoryInstanceDetailsInfos.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("MemoryInstanceDetailsInfo");
            conn.setAutoCommit(false);
            String sql = "insert into MemoryInstanceDetailsInfo(id,instanceId,frameId,className,methodName,fieldName,"
                + "lineNumber) values(null,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            for (MemoryInstanceDetailsInfo memoryInstanceDetailsInfo : memoryInstanceDetailsInfos) {
                try {
                    ps.setInt(1, memoryInstanceDetailsInfo.getInstanceId());
                    ps.setInt(Option.SQL_INDEX_TWO, memoryInstanceDetailsInfo.getFrameId());
                    ps.setString(Option.SQL_INDEX_THREE, memoryInstanceDetailsInfo.getClassName());
                    ps.setString(Option.SQL_INDEX_FOUR, memoryInstanceDetailsInfo.getMethodName());
                    ps.setString(Option.SQL_INDEX_FIVE, memoryInstanceDetailsInfo.getFieldName());
                    ps.setInt(Option.SQL_INDEX_SIX, memoryInstanceDetailsInfo.getLineNumber());
                    ps.addBatch();
                } catch (SQLException sqlException) {
                    LOGGER.info("insert AppInfo {}", sqlException.getMessage());
                }
            }
            int[] results = ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException throwables) {
            LOGGER.error("insert Exception {}", throwables.getMessage());
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * getMemoryInstanceDetails
     *
     * @param instanceId instanceId
     * @return ArrayList<MemoryInstanceDetailsInfo>
     */
    public ArrayList<MemoryInstanceDetailsInfo> getMemoryInstanceDetails(Integer instanceId) {
        Connection conn = getConnection("MemoryInstanceDetailsInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();
        try {
            String sql = "select * from MemoryInstanceDetailsInfo where instanceId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, instanceId);
            ResultSet rs = ps.executeQuery();
            MemoryInstanceDetailsInfo memoryInstanceDetailsInfo = null;
            while (rs.next()) {
                memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
                Integer id = rs.getInt("id");
                Integer frameId = rs.getInt("frameId");
                String className = rs.getString("className");
                String methodName = rs.getString("methodName");
                String fieldName = rs.getString("fieldName");
                Integer lineNumber = rs.getInt("lineNumber");
                memoryInstanceDetailsInfo.setId(id);
                memoryInstanceDetailsInfo.setInstanceId(instanceId);
                memoryInstanceDetailsInfo.setFrameId(frameId);
                memoryInstanceDetailsInfo.setClassName(className);
                memoryInstanceDetailsInfo.setMethodName(methodName);
                memoryInstanceDetailsInfo.setFieldName(fieldName);
                memoryInstanceDetailsInfo.setLineNumber(lineNumber);
                memoryInstanceDetailsInfos.add(memoryInstanceDetailsInfo);
            }
            return memoryInstanceDetailsInfos;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryInstanceDetailsInfos;
    }

    /**
     * getAllMemoryInstanceDetails
     *
     * @return ArrayList<MemoryInstanceDetailsInfo>
     */
    public List<MemoryInstanceDetailsInfo> getAllMemoryInstanceDetails() {
        Connection conn = getConnection("MemoryInstanceDetailsInfo");
        PreparedStatement ps = null;
        ArrayList<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();
        try {
            String sql = "select id,instanceId,frameId,className,methodName,fieldName,"
                + "lineNumber from MemoryInstanceDetailsInfo";
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            MemoryInstanceDetailsInfo memoryInstanceDetailsInfo = null;
            while (rs.next()) {
                memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();

                Integer id = rs.getInt("id");
                Integer instanceId = rs.getInt("instanceId");
                Integer frameId = rs.getInt("frameId");
                String className = rs.getString("className");
                String methodName = rs.getString("methodName");
                String fieldName = rs.getString("fieldName");
                Integer lineNumber = rs.getInt("lineNumber");

                memoryInstanceDetailsInfo.setId(id);
                memoryInstanceDetailsInfo.setInstanceId(instanceId);
                memoryInstanceDetailsInfo.setFrameId(frameId);
                memoryInstanceDetailsInfo.setClassName(className);
                memoryInstanceDetailsInfo.setMethodName(methodName);
                memoryInstanceDetailsInfo.setFieldName(fieldName);
                memoryInstanceDetailsInfo.setLineNumber(lineNumber);
                memoryInstanceDetailsInfos.add(memoryInstanceDetailsInfo);
            }
            return memoryInstanceDetailsInfos;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return memoryInstanceDetailsInfos;
    }

    /**
     * 删除一次场景下的堆信息数据
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        StringBuffer deleteSql = new StringBuffer("DELETE FROM MemoryInstanceDetailsInfo");
        Connection connection = DataBaseApi.getInstance().getConnectByTable("MemoryInstanceDetailsInfo").get();
        return execute(connection, deleteSql.toString());
    }
}

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
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
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
 * Memory Instance Details Dao
 *
 * @since 2021/5/19 16:39
 */
public class MemoryInstanceDetailsDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(MemoryInstanceDetailsDao.class);

    /**
     * Memory InstanceDetailsDao
     */
    public MemoryInstanceDetailsDao() {
        createMemoryInstanceDetails();
    }

    /**
     * Creation of a detailed table of object information for specific instances
     *
     * @return boolean
     */
    public boolean createMemoryInstanceDetails() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createMemoryInstanceDetails");
        }
        String dbName = JVMTI_AGENT_PLUG;
        String memoryInstanceDetailsInfoTable = "MemoryInstanceDetailsInfo";
        String sql = "CREATE TABLE "
            + "MemoryInstanceDetailsInfo "
            + "(instanceId int(100) not null, "
            + "frameId int(100) not null, "
            + "className varchar(200) not null, "
            + "methodName varchar(200) not null, "
            + "fieldName varchar(200) not null, "
            + "lineNumber int(100));";
        return createTable(dbName, memoryInstanceDetailsInfoTable, sql);
    }

    /**
     * insert Memory InstanceDetailsInfo
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
        Optional<Connection> optionalConnection = getConnectByTable("MemoryInstanceDetailsInfo");
        if (optionalConnection.isPresent()) {
            try {
                conn = optionalConnection.get();
                conn.setAutoCommit(false);
                String sql = "insert into "
                    + "MemoryInstanceDetailsInfo("
                    + "instanceId, "
                    + "frameId, "
                    + "className, "
                    + "methodName, "
                    + "fieldName, "
                    + "lineNumber) "
                    + "values(?,?,?,?,?,?)";
                ps = conn.prepareStatement(sql);
                for (MemoryInstanceDetailsInfo memoryInstanceDetailsInfo : memoryInstanceDetailsInfos) {
                    try {
                        ps.setInt(1, memoryInstanceDetailsInfo.getInstanceId());
                        ps.setInt(2, memoryInstanceDetailsInfo.getFrameId());
                        ps.setString(3, memoryInstanceDetailsInfo.getClassName());
                        ps.setString(4, memoryInstanceDetailsInfo.getMethodName());
                        ps.setString(5, memoryInstanceDetailsInfo.getFieldName());
                        ps.setInt(6, memoryInstanceDetailsInfo.getLineNumber());
                        ps.addBatch();
                    } catch (SQLException sqlException) {
                        LOGGER.error("insert AppInfo {}", sqlException.getMessage());
                    }
                }
                ps.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
                ps.clearParameters();
                return true;
            } catch (SQLException throwables) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("insert Exception {}", throwables.getMessage());
                }
                return false;
            } finally {
                CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
            }
        }
        return false;
    }

    /**
     * get Memory InstanceDetails
     *
     * @param instanceId instanceId
     * @return ArrayList <MemoryInstanceDetailsInfo>
     */
    public ArrayList<MemoryInstanceDetailsInfo> getMemoryInstanceDetails(Integer instanceId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getMemoryInstanceDetails");
        }
        Optional<Connection> connection = getConnectByTable("MemoryInstanceDetailsInfo");
        ArrayList<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();
        if (connection.isPresent()) {
            Connection conn = connection.get();
            PreparedStatement ps = null;
            try {
                String sql = "select "
                    + "* "
                    + "from "
                    + "MemoryInstanceDetailsInfo "
                    + "where "
                    + "instanceId = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, instanceId);
                ResultSet rs = ps.executeQuery();
                MemoryInstanceDetailsInfo memoryInstanceDetailsInfo = null;
                while (rs.next()) {
                    memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
                    Integer frameId = rs.getInt("frameId");
                    String className = rs.getString("className");
                    String methodName = rs.getString("methodName");
                    String fieldName = rs.getString("fieldName");
                    Integer lineNumber = rs.getInt("lineNumber");
                    memoryInstanceDetailsInfo.setInstanceId(instanceId);
                    memoryInstanceDetailsInfo.setFrameId(frameId);
                    memoryInstanceDetailsInfo.setClassName(className);
                    memoryInstanceDetailsInfo.setMethodName(methodName);
                    memoryInstanceDetailsInfo.setFieldName(fieldName);
                    memoryInstanceDetailsInfo.setLineNumber(lineNumber);
                    memoryInstanceDetailsInfos.add(memoryInstanceDetailsInfo);
                }
                ps.clearParameters();
                return memoryInstanceDetailsInfos;
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(sqlException.getMessage());
                }
            } finally {
                CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
            }
        }
        return memoryInstanceDetailsInfos;
    }

    /**
     * get All Memory InstanceDetails
     *
     * @return ArrayList <MemoryInstanceDetailsInfo>
     */
    public List<MemoryInstanceDetailsInfo> getAllMemoryInstanceDetails() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllMemoryInstanceDetails");
        }
        Optional<Connection> optionalConnection = getConnectByTable("MemoryInstanceDetailsInfo");
        ArrayList<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();
        if (optionalConnection.isPresent()) {
            Connection conn = optionalConnection.get();
            PreparedStatement ps = null;
            try {
                String sql = "select "
                    + "instanceId, "
                    + "frameId, "
                    + "className, "
                    + "methodName, "
                    + "fieldName, "
                    + "lineNumber "
                    + "from "
                    + "MemoryInstanceDetailsInfo";
                ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                MemoryInstanceDetailsInfo memoryInstanceDetailsInfo = null;
                while (rs.next()) {
                    memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
                    Integer instanceId = rs.getInt("instanceId");
                    Integer frameId = rs.getInt("frameId");
                    String className = rs.getString("className");
                    String methodName = rs.getString("methodName");
                    String fieldName = rs.getString("fieldName");
                    Integer lineNumber = rs.getInt("lineNumber");
                    memoryInstanceDetailsInfo.setInstanceId(instanceId);
                    memoryInstanceDetailsInfo.setFrameId(frameId);
                    memoryInstanceDetailsInfo.setClassName(className);
                    memoryInstanceDetailsInfo.setMethodName(methodName);
                    memoryInstanceDetailsInfo.setFieldName(fieldName);
                    memoryInstanceDetailsInfo.setLineNumber(lineNumber);
                    memoryInstanceDetailsInfos.add(memoryInstanceDetailsInfo);
                }
                ps.clearParameters();
                return memoryInstanceDetailsInfos;
            } catch (SQLException throwables) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(throwables.getMessage());
                }
            } finally {
                CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
            }
        }
        return memoryInstanceDetailsInfos;
    }

    /**
     * delete Session Data by sessionId
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSessionData");
        }
        StringBuffer deleteSql = new StringBuffer("DELETE FROM MemoryInstanceDetailsInfo");
        Optional<Connection> memoryInstanceDetailsInfo =
            DataBaseApi.getInstance().getConnectByTable("MemoryInstanceDetailsInfo");
        if (memoryInstanceDetailsInfo.isPresent()) {
            return execute(memoryInstanceDetailsInfo.get(), deleteSql.toString());
        }
        return false;
    }
}

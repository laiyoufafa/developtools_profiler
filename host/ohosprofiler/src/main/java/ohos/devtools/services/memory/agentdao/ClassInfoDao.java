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
import ohos.devtools.services.memory.agentbean.ClassInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;

/**
 * processing the class data obtained on the end side
 *
 * @since 2021/5/19 16:39
 */
public class ClassInfoDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(ClassInfoDao.class);

    /**
     * ClassInfoDao constructor
     */
    public ClassInfoDao() {
        createClassInfo();
    }

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
     * createClassInfo
     *
     * @return boolean
     */
    public boolean createClassInfo() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createClassInfo");
        }
        boolean createResult = false;
        String dbName = JVMTI_AGENT_PLUG;
        String classInfoTable = "ClassInfo";
        String sql = "CREATE TABLE ClassInfo " + "("
            + "    cId        int not null, " + "  className  varchar  not null " + ");";
        return createTable(dbName, classInfoTable, sql);
    }

    /**
     * insertClassInfo
     *
     * @param classInfo classInfo
     */
    public void insertClassInfo(ClassInfo classInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertClassInfo");
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("ClassInfo");
            String sql = "insert into ClassInfo (cId,className) values (?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, classInfo.getcId());
            ps.setString(2, classInfo.getClassName());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", throwables.getMessage());
            }
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * insertClassInfos
     *
     * @param classInfos List<ClassInfo>
     * @return boolean
     */
    public boolean insertClassInfos(List<ClassInfo> classInfos) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertClassInfos");
        }
        if (classInfos.isEmpty()) {
            return false;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("ClassInfo");
            conn.setAutoCommit(false);
            String sql = "insert into ClassInfo (cId,className) values (?,?)";
            ps = conn.prepareStatement(sql);
            for (ClassInfo classInfo : classInfos) {
                ps.setInt(1, classInfo.getcId());
                ps.setString(2, classInfo.getClassName());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", throwables.getMessage());
            }
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * get all class info data.
     *
     * @param sessionId sessionId
     * @return List <ClassInfo>
     */
    public List<ClassInfo> getAllClassInfoData(Long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllClassInfoData");
        }
        Connection conn = getConnection("ClassInfo");
        PreparedStatement ps = null;
        ArrayList<ClassInfo> classInfos = new ArrayList<>();
        try {
            String sql = "select cId,className from ClassInfo";
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ClassInfo classInfo = null;
            while (rs.next()) {
                classInfo = new ClassInfo();
                Integer cId = rs.getInt("cId");
                String className = rs.getString("className");
                classInfo.setcId(cId);
                classInfo.setClassName(className);
                classInfos.add(classInfo);
            }
            return classInfos;
        } catch (SQLException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", throwables.getMessage());
            }
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return classInfos;
    }

    /**
     * get all class info data by classname
     *
     * @param className className
     * @return int cid
     */
    public int getClassIdByClassName(String className) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getClassIdByClassName");
        }
        Connection conn = getConnection("ClassInfo");
        PreparedStatement ps = null;
        int cId = 0;
        try {
            String sql = "select cId from ClassInfo where className = '" + className + "'";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                cId = rs.getInt("cId");
            }
            return cId;
        } catch (SQLException throwables) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("memoryHeapInfo Exception {}", throwables.getMessage());
            }
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return cId;
    }

    /**
     * delete the heap information data in a scene
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSessionData");
        }
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ClassInfo");
        Optional<Connection> classInfo = DataBaseApi.getInstance().getConnectByTable("ClassInfo");
        if (classInfo.isPresent()) {
            return execute(classInfo.get(), deleteSql.toString());
        }
        return false;
    }
}
